package com.automation.zzx.intelligent_basket_demo.activity.worker;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
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
import com.automation.zzx.intelligent_basket_demo.activity.areaAdmin.AreaAdminSumitReportActivity;
import com.automation.zzx.intelligent_basket_demo.activity.basket.BasketRepairFinishActivity;
import com.automation.zzx.intelligent_basket_demo.adapter.rentAdmin.MgBasketContentFragmentAdapter;
import com.automation.zzx.intelligent_basket_demo.entity.UserInfo;
import com.automation.zzx.intelligent_basket_demo.fragment.worker.BlueDeviceListFragment;
import com.automation.zzx.intelligent_basket_demo.fragment.worker.CurrentDeviceFragment;
import com.automation.zzx.intelligent_basket_demo.utils.CustomTimeTask;
import com.automation.zzx.intelligent_basket_demo.utils.HexAndByte;
import com.automation.zzx.intelligent_basket_demo.utils.http.HttpUtil;
import com.automation.zzx.intelligent_basket_demo.widget.NoScrollViewPager;
import com.automation.zzx.intelligent_basket_demo.widget.dialog.LoadingDialog;
import com.automation.zzx.intelligent_basket_demo.widget.dialog.PictureDialog;
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
import java.util.TimerTask;
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
    public final static int CONNECT_DEVICE_SUCCESS = 108;
    public final static int TIMER_TASK_FAIL = 109;


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

    private PictureDialog mPictureDialog;
    private LoadingDialog mLoadingDialogOpen;
    private LoadingDialog mLoadingDialogClose;

    private String mOpenMsg;
    private String mCheckMsg;
    private CustomTimeTask customTimeTask;
    private int checkCount = 0;//查询次数


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
                    mLoadingDialogOpen.show();
                    checkBeforeWork();  //上工
                    break;
                case CHECK_BEFORE_WORK_FAIL:
                    // 上工前操作确认失败
                    mLoadingDialogOpen.dismiss();
                    mLoadingDialogClose.dismiss();
                    int state = (int)msg.obj;
                    String mMsg ;
                    switch (state){
                        case 11:  // 吊篮与施工人员不匹配
                            //Toast.makeText(BlueToothControlActivity.this, "此吊篮不是您的工作吊篮，无法打开。请联系现场管理员进行处理！"
                            // ,Toast.LENGTH_SHORT).show();
                            mMsg = "此吊篮不是您的工作吊篮，无法打开。请联系现场管理员进行处理！";
                            mPictureDialog = initDialog(2,mMsg);
                            mPictureDialog.show();
                            break;
                        case 200:  // 吊篮上有人，只执行下工，不关闭吊篮
                            //Toast.makeText(BlueToothControlActivity.this, "下工成功，吊篮尚有其他作业人员，等待其他施工人员断电!",
                            //      Toast.LENGTH_SHORT).show();
                            mMsg = "下工成功，吊篮尚有其他作业人员，等待其他施工人员断电!";
                            mPictureDialog = initDialog(0,mMsg);
                            mPictureDialog.show();
                            break;
                        case 21:  // 吊篮与施工人员不匹配
                            //Toast.makeText(BlueToothControlActivity.this, "此吊篮不是您的工作吊篮，无法下工！", Toast.LENGTH_SHORT).show();
                            mMsg = "此吊篮不是您的工作吊篮，无法下工！";
                            mPictureDialog = initDialog(2,mMsg);
                            mPictureDialog.show();
                            break;
                        default:
                            break;
                    }

                    break;

                case CONNECT_DEVICE_SUCCESS:
                    setupChatNotify();   // 开启接受数据监听
                    initTimer();// 开启定时器，在定时器中查询蓝牙状态
                    customTimeTask.start();
                    break;
                case CONNECT_DEVICE_FAIL:
                    mLoadingDialogOpen.dismiss();
                    checkCount=0;
                    // 连接失败提示
                    mMsg = "蓝牙连接失败，请检查电柜是否上电!";
                    mPictureDialog = initDialog(2,mMsg);
                    mPictureDialog.show();
                    break;

                case OPENNING_WORK_SUCCESS:  //----------------------上工成功----------------------
                    mLoadingDialogOpen.dismiss();
                    //存储已连接的设备信息：MAC地址/吊篮ID
                    curMacAddress = newMacAddress;  // 保存已成功连接的地址
                    curBasketId = newBasketId;  // 保存已成功连接的吊篮ID

                    //断开吊篮连接，以确保第二个人可连
                    mBluetoothClient.disconnect(curMacAddress);

                    //工作状态更改
                    operatingState = WORKING;

                    editor = mPref.edit();
                    editor.putString("curMacAddress", curMacAddress);
                    editor.putString("curBasketId", curBasketId);
                    editor.apply();


                    if (mPictureDialog == null) {
                        mMsg = "您已成功操作标号为"+ curBasketId + "的吊篮上机，请谨慎操作。";
                        mPictureDialog = initDialog(1,mMsg);
                    }
                    mPictureDialog.show();

                    /*//开始计时
                    currentDeviceFragment.chronometer.setBase(SystemClock.elapsedRealtime());
                    currentDeviceFragment.chronometer.start();*/
                    mViewPager.setCurrentItem(0);//页面跳转到“当前连接”
                    currentDeviceFragment.handler.sendEmptyMessage(CurrentDeviceFragment.GET_WORK_INFO);

                    break;

                case DISCONNECT_BLE_DEVICE:
                    // 下工请求操作
                    mLoadingDialogClose.show();
                    requestBeginOrEndWork(1);
                    break;
                case DISCONNECT_DEVICE_SUCCESS://----------------------下工成功----------------------
                    mLoadingDialogClose.dismiss();

                    //清空已连接的设备信息：MAC地址/吊篮ID
                    editor = mPref.edit();
                    editor.remove("curMacAddress");
                    editor.remove("curBasketId");
                    editor.commit();

                    //需要再次上工
                    if (operatingState == OPENING_AFTER_CLOSING){
                        // 切换吊篮提示
                        Toast.makeText(BlueToothControlActivity.this, "正在切换上工吊篮...", Toast.LENGTH_SHORT).show();
                        /*mMsg = "正在切换上工吊篮...";
                        mPictureDialog = initDialog(1,mMsg);
                        mPictureDialog.show();*/
                        mHandler.sendEmptyMessage(CONNECT_NEW_BLE_DEVICE);
                    } else {
                        if((int)msg.obj == 200){
                            mMsg = "下工成功，吊篮尚有其他作业人员，等待其他施工人员断电!";
                            mPictureDialog = initDialog(0,mMsg);
                            mPictureDialog.show();
                        } else {
                            // 下工成功提示
                            Toast.makeText(BlueToothControlActivity.this, "下工成功，正在关闭吊篮...", Toast.LENGTH_SHORT).show();
                        }
                    }
                    operatingState = AVAILABLE_STATE;
                    currentDeviceFragment.handler.sendEmptyMessage(CurrentDeviceFragment.STOP_WORK);

                    break;
                case DISCONNECT_DEVICE_FAIL:
                    mLoadingDialogClose.dismiss();
                    mMsg = "下工失败，请确认网络状态是否良好!";
                    mPictureDialog = initDialog(2,mMsg);
                    mPictureDialog.show();
                    break;
                case TIMER_TASK_FAIL:
                    customTimeTask.stop(); // 关闭定时任务
                    mHandler.sendEmptyMessage(CONNECT_DEVICE_FAIL);
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
        initLoadingDialog();

        initMsg();
    }

    private void initWidgetResource() {

        // 顶部导航栏
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbarTitle = (TextView) findViewById(R.id.toolbar_title);
        mToolbar.setTitle("");
        toolbarTitle.setText("蓝牙上下机");
        setSupportActionBar(mToolbar);
        getSupportActionBar().setHomeButtonEnabled(true); //设置返回键可用


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
        newBasketId = curBasketId;
        newMacAddress = curMacAddress;
        mHandler.sendEmptyMessage(CONNECT_NEW_BLE_DEVICE);
    }

    /*
    *  连接蓝牙
    * */
    private void connectBLE() {
        BleConnectOptions options = new BleConnectOptions.Builder()
                .setConnectRetry(3)   // 连接如果失败重试3次
                .setConnectTimeout(10000)   // 连接超时30s
                .setServiceDiscoverRetry(3)  // 发现服务如果失败重试3次
                .setServiceDiscoverTimeout(20000)  // 发现服务超时20s
                .build();
        mBluetoothClient.connect(newMacAddress, options, new BleConnectResponse() {  // 连接蓝牙
            @Override
            public void onResponse(int code, BleGattProfile data) {
                if (code == REQUEST_SUCCESS) {   // 连接成功
                    Log.d(TAG, "Connect Sucessfully");
                    // 断开上一次的连接
                    //mBluetoothClient.disconnect(curMacAddress);
                    //mBluetoothClient.unregisterConnectStatusListener(curMacAddress, mBleConnectStatusListener);
                    // 添加蓝牙连接状态监听
                    mBluetoothClient.registerConnectStatusListener(newMacAddress, mBleConnectStatusListener);
                    List<BleGattService> services = data.getServices();
                    bluetoothService = services.get(services.size()-1);  // 服务通道待确认（和硬件调试）
                    sendMessage(bluetoothService, mOpenMsg); // 发送开启蓝牙指令
                    mHandler.sendEmptyMessage(CONNECT_DEVICE_SUCCESS);
                } else {
                    Log.d(TAG, String.valueOf(code));

                }
            }
        });
    }

    // 开启接受数据监听
    private void setupChatNotify(){
        Log.d(TAG, "Setup Chat Notify" );
        mBluetoothClient.notify(newMacAddress, bluetoothService.getUUID(),
                bluetoothService.getCharacters().get(0).getUuid(), new BleNotifyResponse() {
                    @Override
                    public void onNotify(UUID service, UUID character, byte[] value) {
                        customTimeTask.stop();
                        String response = HexAndByte.bytesToCharArray(value);
                        if(parseBLEMessage(response)) {
                            //------------------3-后台通信：上工请求------------------------
                            requestBeginOrEndWork(0);
                        }else {
                            mHandler.sendEmptyMessage(CONNECT_DEVICE_FAIL);
                        }
                        Log.d(TAG, response);
                    }

                    @Override
                    public void onResponse(int code) {
                        customTimeTask.stop();
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
//        byte [] bytes = new byte[0];
//        bytes = message.getBytes();

        int lens = message.length();
        int MAX_LENGTH = 20;
        int times = message.length() / MAX_LENGTH + 1;

        for(int i=0; i < times; i++){
            String temp_str = message.substring(i*MAX_LENGTH, Math.min(lens, (i+1)*MAX_LENGTH));
            byte [] temp_bytes = temp_str.getBytes();

            mBluetoothClient.write(newMacAddress, service.getUUID(),
                service.getCharacters().get(0).getUuid(), temp_bytes,
                new BleWriteResponse() {
                @Override
                public void onResponse(int code) {
                    if (code == REQUEST_SUCCESS) {
                        // Log.i(TAG, "指令发送成功");
                    }
                }
            });

            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

//        mBluetoothClient.write(newMacAddress, service.getUUID(),
//                service.getCharacters().get(0).getUuid(), bytes,
//                new BleWriteResponse() {
//            @Override
//            public void onResponse(int code) {
//                if (code == REQUEST_SUCCESS) {
//                    // Log.i(TAG, "指令发送成功");
//                }
//            }
//        });
    }

    // 通过蓝牙接收数据
    private void readMessage(BleGattService service){
        mBluetoothClient.read(newMacAddress, service.getUUID(), service.getCharacters().get(0).getUuid(),
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

    // 蓝牙开启指令 & 蓝牙状态查询指令
    private void initMsg(){
        //创建蓝牙开机指令
        JSONObject jsonObject_open = new JSONObject();
        JSONObject jsonObject_open_1 = new JSONObject();
        jsonObject_open_1.put("BketStat","1");
        jsonObject_open.put("params",jsonObject_open_1);
        jsonObject_open.put("method","thing.service.property.set");
        mOpenMsg = jsonObject_open.toJSONString();

        //创建蓝牙状态查询指令
        JSONObject jsonObject_check = new JSONObject();
        JSONObject jsonObject_check_1 = new JSONObject();
        jsonObject_check_1.put("ReadStatus","1");
        jsonObject_check.put("params",jsonObject_check_1);
        jsonObject_check.put("method","thing.service.property.set");
        mCheckMsg =jsonObject_check.toJSONString();
    }

    //解析接收到的数据
    private boolean parseBLEMessage(String response){
        JSONObject jsonObject = JSONObject.parseObject(response);
        JSONObject params = jsonObject.getJSONObject("params");
        JSONObject bketStat = params.getJSONObject("BketStat");
        String stateValue = bketStat.getString("value");
        if(stateValue.equals("1")){
            //开机成功
            return true;
        }else{
            return false;
        }
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
        mUserInfo.setUserName(mPref.getString("userName", ""));
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
                    int state = parseBeforeBeginOrEndWork(data,0);
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
            newBasketId = curBasketId;
            //newMacAddress = curMacAddress;
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
                    }else if(state == 201 || state == 200) {
                        msg.what = DISCONNECT_DEVICE_SUCCESS;
                        msg.obj = state;
                        mHandler.sendMessage(msg);
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
    private static int parseBeforeBeginOrEndWork(String data,int mWorkState){
        JSONObject jsonObject = JSON.parseObject(data);
        int state;
        if(mWorkState == 0) { // 下工状态，等待开工
            //if (jsonObject.getBoolean("judgeAndroidBeginWork")!=null){
            boolean beginWork = jsonObject.getBoolean("judgeAndroidBeginWork");
            if(beginWork) {  // 允许上工
                state = 10;
            } else {  // 吊篮与工人不匹配
                state = 11;
            }
            //}
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

    // 解析上下工消息
    private static int parseBeginOrEndWork(String data,int mWorkState){
        JSONObject jsonObject = JSON.parseObject(data);
        int state;
        if(mWorkState == 0) { // 下工状态，等待开工
            //if (jsonObject.getBoolean("judgeAndroidBeginWork")!=null){
            boolean beginWork = jsonObject.getBoolean("beginWork");
            if(beginWork) {  // 允许上工
                state = 10;
            } else {  // 吊篮与工人不匹配
                state = 11;
            }
            //}
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

    private void initTimer(){
        customTimeTask = new CustomTimeTask(1000, new TimerTask() {
            @Override
            public void run() {
                checkCount++;
                if(checkCount >= 3){ // 查询三次后停止
                    mHandler.sendEmptyMessage(TIMER_TASK_FAIL);
                } else {
                    sendMessage(bluetoothService, mCheckMsg);
                    Log.d(TAG, "定时任务：获取最新吊篮数据");
                }
            }
        });
    }


    /*
     * 弹窗提示
     */
    // 加载弹窗
    private void initLoadingDialog(){
        mLoadingDialogOpen = new LoadingDialog(BlueToothControlActivity.this, "正在上工，请等待...");
        mLoadingDialogClose = new LoadingDialog(BlueToothControlActivity.this, "正在下工，请等待...");
        mLoadingDialogOpen.setCancelable(false);
        mLoadingDialogClose.setCancelable(false);
    }

    /*
     * 提示弹框
     */
    private PictureDialog initDialog(int result, String mMsg){
        return new PictureDialog(this, R.style.dialog, mMsg, result,
                new PictureDialog.OnCloseListener() {
                    @Override
                    public void onClick(Dialog dialog, boolean confirm) {
                        if(confirm){
                            dialog.dismiss();
                        }else{
                            dialog.dismiss();
                        }
                    }
                });
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
