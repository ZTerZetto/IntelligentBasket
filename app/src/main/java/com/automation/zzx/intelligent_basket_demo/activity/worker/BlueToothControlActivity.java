package com.automation.zzx.intelligent_basket_demo.activity.worker;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.automation.zzx.intelligent_basket_demo.R;
import com.automation.zzx.intelligent_basket_demo.adapter.rentAdmin.MgBasketContentFragmentAdapter;
import com.automation.zzx.intelligent_basket_demo.entity.UserInfo;
import com.automation.zzx.intelligent_basket_demo.fragment.worker.BlueDeviceListFragment;
import com.automation.zzx.intelligent_basket_demo.fragment.worker.CurrentDeviceFragment;
import com.automation.zzx.intelligent_basket_demo.utils.HexAndByte;
import com.automation.zzx.intelligent_basket_demo.utils.http.HttpUtil;
import com.automation.zzx.intelligent_basket_demo.widget.NoScrollViewPager;
import com.inuker.bluetooth.library.BluetoothClient;
import com.inuker.bluetooth.library.connect.listener.BleConnectStatusListener;
import com.inuker.bluetooth.library.connect.options.BleConnectOptions;
import com.inuker.bluetooth.library.connect.response.BleConnectResponse;
import com.inuker.bluetooth.library.connect.response.BleNotifyResponse;
import com.inuker.bluetooth.library.connect.response.BleReadResponse;
import com.inuker.bluetooth.library.connect.response.BleWriteResponse;
import com.inuker.bluetooth.library.model.BleGattProfile;
import com.inuker.bluetooth.library.model.BleGattService;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import static com.automation.zzx.intelligent_basket_demo.entity.AppConfig.CHECK_BEFORE_START;
import static com.automation.zzx.intelligent_basket_demo.entity.AppConfig.WORKER_BEGIN_WORK;
import static com.automation.zzx.intelligent_basket_demo.entity.AppConfig.WORKER_END_WORK;
import static com.inuker.bluetooth.library.Code.REQUEST_SUCCESS;
import static com.inuker.bluetooth.library.Constants.STATUS_CONNECTED;
import static com.inuker.bluetooth.library.Constants.STATUS_DISCONNECTED;

import java.text.SimpleDateFormat;

public class BlueToothControlActivity extends AppCompatActivity {


    private Toolbar mToolbar;
    private TabLayout mTabLayout; // 顶部导航栏
    private TextView toolbarTitle;
    public static NoScrollViewPager mViewPager; // 页面布局

    // 消息处理
    public final static int TRANGE_DEVICE = 100;
    public final static int CONNECT_NEW_BLE_DEVICE = 101;
    public final static int CHECK_BEFORE_WORK_FAIL = 102;
    public final static int CONNECT_DEVICE_FAIL = 103;
    public final static int OPENNING_WORK_SUCCESS = 104;
    public final static int DISCONNECT_BLE_DEVICE = 105;
    public final static int DISCONNECT_DEVICE_SUCCESS = 106;
    public final static int DISCONNECT_DEVICE_FAIL = 107;


    //Intent参数
    private String mWorkProjectId;

    // 用户登录信息相关
    private UserInfo mUserInfo;
    private String mToken;
    public static SharedPreferences mPref;
    public static SharedPreferences.Editor editor;

    // 全局变量：管理所有的BLE设备
    public  BluetoothClient mBluetoothClient;  // 蓝牙管理类
    public  String newMacAddress; // 新请求连接的mac地址
    public  String curMacAddress; // 连接中的mac地址
    private BluetoothAdapter mBluetoothAdapter = null;  // 蓝牙适配器
    private static BleGattService bluetoothService;

    //操作吊篮
    public  String newBasketId; // 新请求连接的吊篮ID
    public  String curBasketId;  // 连接中的吊篮ID

    // 静态变量
    private static final String TAG = "BlueToothControlActivity";
    private static final int REQUEST_ENABLE_BT = 2;

    //相关变量-上工前是否需要先下工判断
    public static final int WORKING = 2; //工作状态
    public static final int OPENING_AFTER_CLOSING = 1;//需先下工后上工
    public static final int AVAILABLE_STATE = 0; // 下工状态
    public int operatingState = 0;  // 操作类型：

    // fragment页面
    CurrentDeviceFragment currentDeviceFragment = new CurrentDeviceFragment();
    BlueDeviceListFragment blueDeviceListFragment = new BlueDeviceListFragment();


    // 消息处理
    // mHandler 处理消息
    @SuppressLint("HandlerLeak")
    public Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case TRANGE_DEVICE:
                    //上工操作：1-请求后台确认能否连接  2-允许则连接蓝牙  3-成功则向后台接口请求上工
                    if (operatingState == OPENING_AFTER_CLOSING){
                        mHandler.sendEmptyMessage(DISCONNECT_BLE_DEVICE);//先下工后上工
                    } else {
                        mHandler.sendEmptyMessage(CONNECT_NEW_BLE_DEVICE);//直接上工
                    }
                case CONNECT_NEW_BLE_DEVICE:
                    //---------------------1-请求后台确认能否连接--------------------
                    checkBeforeWork();  //上工
                    break;
                case CHECK_BEFORE_WORK_FAIL:
                    // 上工前操作确认失败
                    int state = (int)msg.obj;
                    switch (state){
                        case 11:  // 吊篮与施工人员不匹配
                            Toast.makeText(BlueToothControlActivity.this, "此吊篮不是您的工作吊篮，无法打开！",
                                Toast.LENGTH_SHORT).show();
                            break;
                        case 200:  // 吊篮上有人，只执行下工，不关闭吊篮
                            Toast.makeText(BlueToothControlActivity.this, "下工成功，吊篮尚有其他作业人员，等待其他施工人员断电!",
                                    Toast.LENGTH_SHORT).show();
                            break;
                        case 21:  // 吊篮与施工人员不匹配
                            Toast.makeText(BlueToothControlActivity.this, "此吊篮不是您的工作吊篮，无法下工！", Toast.LENGTH_SHORT).show();
                            break;
                        default:
                            break;
                    }
                    break;
                case CONNECT_DEVICE_FAIL:
                    // 连接失败提示

                    break;

                case OPENNING_WORK_SUCCESS:  //----------------------上工成功----------------------
                    //存储已连接的设备信息：MAC地址/吊篮ID
                    curMacAddress = newMacAddress;  // 保存已成功连接的地址
                    curBasketId = newBasketId;  // 保存已成功连接的吊篮ID

                    //工作状态更改
                    operatingState = WORKING;

                    editor = mPref.edit();
                    editor.putString("curMacAddress", curMacAddress);
                    editor.putString("curBasketId", curBasketId);
                    editor.apply();

                    setupChatButton();         // 开启按键区域功能
                    setupChatNotify();   // 开启接受数据监听

                    /*//开始计时
                    currentDeviceFragment.chronometer.setBase(SystemClock.elapsedRealtime());
                    currentDeviceFragment.chronometer.start();*/
                    mViewPager.setCurrentItem(0);//页面跳转到“当前连接”
                    currentDeviceFragment.handler.sendEmptyMessage(CurrentDeviceFragment.GET_WORK_INFO);

                    break;

                case DISCONNECT_BLE_DEVICE:
                    // 下工请求操作
                    requestBeginOrEndWork(1);

                    break;
                case DISCONNECT_DEVICE_SUCCESS://----------------------下工成功----------------------
                    //清空已连接的设备信息：MAC地址/吊篮ID
                    editor = mPref.edit();
                    editor.remove("curMacAddress");
                    editor.remove("curBasketId");
                    editor.commit();

                    //需要再次上工
                    if (operatingState == OPENING_AFTER_CLOSING){
                        // 切换吊篮提示
                        Toast.makeText(BlueToothControlActivity.this, "正在切换上工吊篮...", Toast.LENGTH_SHORT).show();
                        mHandler.sendEmptyMessage(CONNECT_NEW_BLE_DEVICE);
                    } else {
                        // 下工成功提示
                        Toast.makeText(BlueToothControlActivity.this, "下工成功，正在关闭吊篮...", Toast.LENGTH_SHORT).show();
                    }
                    operatingState = AVAILABLE_STATE;
                    currentDeviceFragment.handler.sendEmptyMessage(CurrentDeviceFragment.STOP_WORK);

                    break;

            }
        }
    };



    /*
     * 页面初始化
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blue_tooth_control);

        Intent intent = getIntent();
        mWorkProjectId = intent.getStringExtra("work_project_id");

        initWidgetResource();
        getUserInfo();

        //自动连接蓝牙
        autoConnectBLE();


    }

    private void initWidgetResource() {
        // 绑定控件
        mToolbar = findViewById(R.id.toolbar);
        toolbarTitle = findViewById(R.id.toolbar_title);
        mToolbar.setTitle("");
        toolbarTitle.setText("蓝牙上下机");

        mTabLayout = (TabLayout) findViewById(R.id.head_tab_layout);
        mViewPager = (NoScrollViewPager) findViewById(R.id.view_pager);

        List<Fragment> fragmentList = new ArrayList<>();   // 添加fragment

        fragmentList.add(currentDeviceFragment);
        fragmentList.add(blueDeviceListFragment);

        List<String> titleList = new ArrayList<>();  // 添加fragment说明
        titleList.add("当前连接");
        titleList.add("其他设备");

        MgBasketContentFragmentAdapter mgBasketContentFragmentAdapter =
                new MgBasketContentFragmentAdapter(getSupportFragmentManager(), fragmentList, titleList);
        mViewPager.setAdapter(mgBasketContentFragmentAdapter);
        mTabLayout.setupWithViewPager(mViewPager);

        mViewPager.setCurrentItem(0);

        //蓝牙
        mBluetoothClient = new BluetoothClient(getApplicationContext());
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {   // 设备没有蓝牙，关闭软件
            Toast.makeText(this, "Bluetooth is not available",
                    Toast.LENGTH_LONG).show();
            finish();
            return;
        }
    }


    // 顶部导航栏消息响应
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home: // 返回按钮
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    /*
     * 蓝牙通信
     */

    private void autoConnectBLE(){
        curBasketId = mPref.getString("curBasketId","");
        curMacAddress = mPref.getString("curMacAddress","");
        mHandler.sendEmptyMessage(CONNECT_NEW_BLE_DEVICE);
    }

    /*
    *  连接蓝牙
    * */
    private void connectBLE() {
        BleConnectOptions options = new BleConnectOptions.Builder()
                .setConnectRetry(3)   // 连接如果失败重试3次
                .setConnectTimeout(30000)   // 连接超时30s
                .setServiceDiscoverRetry(3)  // 发现服务如果失败重试3次
                .setServiceDiscoverTimeout(20000)  // 发现服务超时20s
                .build();
        mBluetoothClient.connect(newMacAddress, new BleConnectResponse() {  // 连接蓝牙
            @Override
            public void onResponse(int code, BleGattProfile data) {
                if (code == REQUEST_SUCCESS) {   // 连接成功
                    Log.d(TAG, "Connect Sucessfully");
                    // 断开上一次的连接
                    mBluetoothClient.disconnect(curMacAddress);
                    mBluetoothClient.unregisterConnectStatusListener(curMacAddress, mBleConnectStatusListener);
                    // 添加蓝牙连接状态监听
                    mBluetoothClient.registerConnectStatusListener(newMacAddress, mBleConnectStatusListener);
                    List<BleGattService> services = data.getServices();
                    bluetoothService = services.get(2);  // TODO 服务通道待确认（和硬件调试）
                    //------------------3-后台通信：上工请求------------------------
                    requestBeginOrEndWork(0);
                } else {
                    Log.d(TAG, String.valueOf(code));
                    mHandler.sendEmptyMessage(CONNECT_DEVICE_FAIL);
                }
            }
        });
    }

    // 开启接受数据监听
    private void setupChatNotify(){
        Log.d(TAG, "Setup Chat Notify" );
        mBluetoothClient.notify(curMacAddress, bluetoothService.getUUID(),
                bluetoothService.getCharacters().get(0).getUuid(), new BleNotifyResponse() {
                    @Override
                    public void onNotify(UUID service, UUID character, byte[] value) {
                        // TODO: 蓝牙接收数据处理
                        String response = HexAndByte.bytesToCharArray(value);
                        Log.d(TAG, response);
                    }

                    @Override
                    public void onResponse(int code) {
                        if(code == REQUEST_SUCCESS){
                            Log.d(TAG, "Open Notify Sucessfully");
                        }
                    }
                });
    }

    // 蓝牙连接状态监听变量
    private final static BleConnectStatusListener mBleConnectStatusListener = new BleConnectStatusListener() {
        @Override
        public void onConnectStatusChanged(String mac, int status) {
            if (status == STATUS_CONNECTED) {
                Log.d(TAG, "STATUS_CONNECTED");
                setupChatButton();
            } else if (status == STATUS_DISCONNECTED) {
                Log.d(TAG, "STATUS_DISCONNECTED");
                closeChatButton();
            }
        }
    };

    // 可以对串口的按键操作
    private static void setupChatButton(){

    }

    // 禁止对串口的按键操作
    private static void closeChatButton(){

    }

    /*
     * 蓝牙通讯 API
     */
    // 通过蓝牙发送数据
    private void sendMessage(BleGattService service, final String message) {
        byte [] bytes = new byte[0];
        bytes = message.getBytes();

        mBluetoothClient.write(curMacAddress, service.getUUID(),
                service.getCharacters().get(0).getUuid(), bytes,
                new BleWriteResponse() {
            @Override
            public void onResponse(int code) {
                if (code == REQUEST_SUCCESS) {
                    // Log.i(TAG, "指令发送成功");
                }
            }
        });
    }

    // 通过蓝牙接收数据
    private void readMessage(BleGattService service){
        mBluetoothClient.read(curMacAddress, service.getUUID(), service.getCharacters().get(0).getUuid(),
                new BleReadResponse() {
            @Override
            public void onResponse(int code, byte[] data) {
                if (code == REQUEST_SUCCESS) {
                    String response = null;
                    try {
                        response = new String(data, "UTF-8");
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                    Log.i(TAG, "串口收到消息：" + response);
                }
            }
        });
    }


    /*
        * 信息获取
     */
    // 获取用户数据
    private void getUserInfo(){
        // 从本地获取数据
        mPref = PreferenceManager.getDefaultSharedPreferences(this);
        mUserInfo = new UserInfo();
        mUserInfo.setUserId(mPref.getString("userId", ""));
        mUserInfo.setUserPhone(mPref.getString("userPhone", ""));
        mUserInfo.setUserRole(mPref.getString("userRole", ""));
        mToken = mPref.getString("loginToken","");
    }

    /*
     * 上工前确认状态
     */
    private void checkBeforeWork(){
        String url=CHECK_BEFORE_START;
        HttpUtil.workerBeginOrEndWorkOkHttpRequest(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d(TAG, e.toString());
                e.printStackTrace();
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.code() == 200) {
                    Log.d(TAG, "Http Server Success");
                    String data = response.body().string();
                    int state = parseBeginOrEndWork(data,0);
                    Message msg = new Message();
                    if (state == 10){
                        //-----------------2-连接设备蓝牙------------------------
                        connectBLE();
                    } else {
                    msg.what = CHECK_BEFORE_WORK_FAIL;
                    msg.obj = state;
                    mHandler.sendMessage(msg);
                    }
                }else{
                    Log.d(TAG, "Http Server Error" + response.code());
                }
            }
        }, url, mToken, mUserInfo.getUserId(), newBasketId, mWorkProjectId);
    }

    /*
     * 上/下工请求
     */
    private void requestBeginOrEndWork(final int mWorkState){
        String url;
        if(mWorkState == 0) { // 等待上工
            Log.i(TAG, "Now, you can open the basket");
            url = WORKER_BEGIN_WORK;
        }
        else { //等待下工
            Log.i(TAG, "Now, you can close the basket");
            url = WORKER_END_WORK;
        }
        HttpUtil.workerBeginOrEndWorkOkHttpRequest(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d(TAG, e.toString());
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.code() == 200) {
                    Log.d(TAG, "Http Server Success");
                    String data = response.body().string();
                    int state = parseBeginOrEndWork(data,mWorkState);
                    Message msg = new Message();
                    if (state == 10){
                        // 上工成功刷新展示
                        mHandler.sendEmptyMessage(OPENNING_WORK_SUCCESS);

                    }else if(state == 201) {
                        mHandler.sendEmptyMessage(DISCONNECT_DEVICE_SUCCESS);
                    } else {
                        msg.what = CHECK_BEFORE_WORK_FAIL;
                        msg.obj = state;
                        mHandler.sendMessage(msg);
                    }
                }else{
                    Log.d(TAG, "Http Server Error" + response.code());
                }
            }
        }, url, mToken, mUserInfo.getUserId(), newBasketId, mWorkProjectId);
    }

    // 解析上下工消息
    private static int parseBeginOrEndWork(String data,int mWorkState){
        JSONObject jsonObject = JSON.parseObject(data);
        int state;
        if(mWorkState == 0) { // 下工状态，等待开工
            boolean beginWork = jsonObject.getBoolean("beginWork");
            if(beginWork) {  // 允许上工
                state = 10;
            } else {  // 吊篮与工人不匹配
                state = 11;
            }
        }else if(mWorkState == 1){ // 上工状态，等待下工
            boolean endWork = jsonObject.getBoolean("endWork");
            boolean hasPeople = jsonObject.getBoolean("hasPeople");
            if(endWork) {
                if(hasPeople) {  // 吊篮上还有作业人员
                    state = 200;
                }else{  // 吊篮上无其他作业人员
                    state = 201;
                }
            } else {  // 吊篮与工人不匹配
                state = 21;
            }
        }else{  // 错误状态
            state = 0;
        }
        return state;
    }


    // 将用户信息传递给子Fragment
    public UserInfo pushUserInfo(){
        return mUserInfo;
    }
    // 将用户token传递给子Fragment
    public String pushToken(){
        return mToken;
    }

    /*
     * 页面生命周期
     */
    @Override
    public void onStart() {
        super.onStart();
        if (!mBluetoothAdapter.isEnabled()) { // 动态请求打开蓝牙
            Intent enableIntent = new Intent(
                    BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);

        }
    }

    @Override
    public synchronized void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mBluetoothClient.disconnect(curMacAddress);
        mBluetoothClient.unregisterConnectStatusListener(curMacAddress, mBleConnectStatusListener);
    }
}
