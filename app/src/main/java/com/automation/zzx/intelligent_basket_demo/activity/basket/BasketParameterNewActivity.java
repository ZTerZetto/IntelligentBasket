package com.automation.zzx.intelligent_basket_demo.activity.basket;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.automation.zzx.intelligent_basket_demo.R;
import com.automation.zzx.intelligent_basket_demo.adapter.basket.VarSwitchAdapter;
import com.automation.zzx.intelligent_basket_demo.entity.VarSwitch;
import com.automation.zzx.intelligent_basket_demo.utils.CustomTimeTask;
import com.automation.zzx.intelligent_basket_demo.utils.http.HttpUtil;
import com.automation.zzx.intelligent_basket_demo.widget.SmartGridView;
import com.scwang.smartrefresh.header.BezierCircleHeader;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.TimerTask;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class BasketParameterNewActivity extends AppCompatActivity {

    private static final String TAG = "ParameterNewActivity";

    // 文件缓存
    public SharedPreferences pref;

    // 消息处理
    private static final int PARAMETER_INFO = 1; // 更新ui
    private static final int TIMER = 2; // 定时任务

    // 控件声明
    private SmartRefreshLayout mSmartRefreshLayout; // 下拉刷新
    private SmartGridView mVarSwitchGv;  // 开关变量网格控件
    private ImageView mMotorLeft;  // 左电机
    private ImageView mMotorRight; // 右电机
    private TextView mDeviceWight; // 设备总重
    private TextView mClinometerDegree; // 倾斜仪角度
    private TextView mLocationMsg;  // 位置信息
    private TextView mDateMsg; // 时间信息

    // var switch gridview
    private List<VarSwitch> mVarSwitches;  // 开关变量列表
    private VarSwitchAdapter mVarSwitchAdapter;  // 开关变量适配器

    // 其他变量
    private String mBasketId;
    private CustomTimeTask customTimeTask;

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case PARAMETER_INFO:  // 控件更新
                    updateWidgetState(msg.obj);
                    break;
                case TIMER: // 定时任务
                    //getDeviceParameter();
                    deviceParameterHttp(false);
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_basket_parameter_new);

        Intent intent = getIntent();
        mBasketId = intent.getStringExtra(BasketDetailActivity.BASKET_ID);  // 获取吊篮id
        if(mBasketId==null || mBasketId.equals("")) mBasketId = "js_nj_00003";
//        mBasketId = "36FFD6054154393207782457";

        pref = PreferenceManager.getDefaultSharedPreferences(this);

        initWidgetResource();  // 初始化控件
        //getDeviceParameter();
        deviceParameterHttp(false);
        setTimer();  // 开启定时任务
    }

    // 控件初始化
    private void initWidgetResource(){
        // 下拉刷新
        mSmartRefreshLayout = (SmartRefreshLayout) findViewById(R.id.smart_refresh_layout);
        mSmartRefreshLayout.setRefreshHeader(  //设置 Header 为 贝塞尔雷达 样式
                new BezierCircleHeader(this));
        mSmartRefreshLayout.setPrimaryColorsId(R.color.smart_loading_background_color);
        mSmartRefreshLayout.setOnRefreshListener(new OnRefreshListener() { // 添加下拉刷新监听
            @Override
            public void onRefresh(RefreshLayout refreshlayout) {
                //getDeviceParameter();
                deviceParameterHttp(true);
            }
        });

        // 顶部导航栏
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        TextView titleText = (TextView) findViewById(R.id.toolbar_title);
        toolbar.setTitle("");
        titleText.setText(getString(R.string.parameter_title));
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true); //设置返回键可用

        // GridView:开关变量状态显示
        mVarSwitchGv = (SmartGridView) findViewById(R.id.var_switch_gv);  // 获取资源控件
        initVarSwitchList();    // 初始化列表内容
        mVarSwitchAdapter = new VarSwitchAdapter(BasketParameterNewActivity.this,
                R.layout.item_var_switch, mVarSwitches);  // 初始化适配器
        mVarSwitchGv.setAdapter(mVarSwitchAdapter);  // 装载适配器

        // 控制输入
        mMotorLeft = (ImageView) findViewById(R.id.motor_left);
        mMotorRight = (ImageView) findViewById(R.id.motor_right);

        // 吊篮数据
        mDeviceWight = (TextView) findViewById(R.id.weight_tv);
        mClinometerDegree = (TextView) findViewById(R.id.clinometer_degree_tv);

        // 其他数据
        mLocationMsg = (TextView) findViewById(R.id.location_msg_tv);
        mDateMsg = (TextView) findViewById(R.id.date_msg_tv);

    }

    // 定时器初始化
    private void setTimer(){
        customTimeTask = new CustomTimeTask(1000, new TimerTask() {
            @Override
            public void run() {
                mHandler.sendEmptyMessage(TIMER);
                Log.d(TAG, "定时任务：获取最新吊篮数据");
            }
        });
        customTimeTask.start();
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
     * 生命周期
     */
    @Override
    protected void onResume(){
        super.onResume();
    }
    @Override
    protected void onDestroy(){
        super.onDestroy();
        customTimeTask.stop();
    }

    /*
     * 网络相关
     * boolean fresh: true 下拉刷新 false:timer fresh
     */
    private void deviceParameterHttp(final boolean fresh){
        // 获取token
        String token = pref.getString("loginToken", null);

        HttpUtil.getDeviceParameterOkHttpRequest(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.i(TAG, "失败：" + e.toString());
                mSmartRefreshLayout.finishRefresh(500,false); // 刷新失败
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                // 成功
                String responseData = response.body().string();
                Log.i(TAG, "成功：" +  responseData);
                Message msg = new Message();
                msg.what = PARAMETER_INFO;
                msg.obj = responseData;
                mHandler.sendMessage(msg);

                if(fresh)
                    mSmartRefreshLayout.finishRefresh(500); // 刷新成功

            }
        }, token, mBasketId);
    }

    /*
     * 其它函数
     */
    // 初始化开关列表
    private void initVarSwitchList(){
        mVarSwitches = new ArrayList<>();
        VarSwitch cable = new VarSwitch("电缆", R.mipmap.ic_param_cable, 0);
        mVarSwitches.add(cable);
        VarSwitch angle = new VarSwitch("角度", R.mipmap.ic_param_angle, 0);
        mVarSwitches.add(angle);
        VarSwitch weight = new VarSwitch("重量", R.mipmap.ic_param_weight, 0);
        mVarSwitches.add(weight);
        VarSwitch leftLimit = new VarSwitch("左上限", R.mipmap.ic_param_left_limit, 0);
        mVarSwitches.add(leftLimit);
//        mVarSwitches.add(new VarSwitch());
        VarSwitch rightLimit = new VarSwitch("右上限", R.mipmap.ic_param_right_limit, 0);
        mVarSwitches.add(rightLimit);
    }

    // 更新控件状态
    private void updateWidgetState(Object obj) {
        String json = obj.toString();  // object 转 string
        JSONObject jsonObject = JSON.parseObject(json);  // string 转 jsonobject
        String electric_data_json = jsonObject.getString("realTimeData");
        JSONObject electric_data_json_object = JSON.parseObject(electric_data_json);
        if(electric_data_json_object == null){
            initialState();
            mSmartRefreshLayout.finishRefresh(1500,false); // 刷新失败
            return;
        }

        /*
         * bool 数据
         */
        String boolData32 = electric_data_json_object.getString("bool_data_int32");
        boolData32 = Integer.toBinaryString(Integer.valueOf(boolData32));
        if(boolData32.length() < 8){
            boolData32 = String.format("%8s", boolData32);
            boolData32 = boolData32.replace(" ", "0");
        }
        /* 应学弟请求，将字符串反置 */
        StringBuffer stringBuffer = new StringBuffer(boolData32);
        boolData32 = stringBuffer.reverse().toString();
        // 开关变量
        String varswitch = boolData32.substring(0, 5);
        updateVarSwitch(varswitch);
        // 控制输入
        String control_input = boolData32.substring(5, 8);
        updateControInput(control_input);

        /*
         * 吊篮数据
         */
        // 称重
        String device_weight = electric_data_json_object.getString("weight");
        mDeviceWight.setText(device_weight + " Kg");

        // 倾斜仪
        String clinometer_degree = electric_data_json_object.getString("degree");
        mClinometerDegree.setText(clinometer_degree + "°");

        /*
         * 其他数据
         */
        // 经纬度
        String longitude = electric_data_json_object.getString("longitude");
        String latitude = electric_data_json_object.getString("latitude");

        mLocationMsg.setText("(" + longitude + "°," + latitude + "°)");
        // 时间
        String timestamp = electric_data_json_object.getString("timestamp");
        mDateMsg.setText(timestamp);
    }

    // 开关变量
    private void updateVarSwitch(String varswitch){
        mVarSwitches.get(0).setState(varswitch.charAt(0) - 48);  // 拉紧
        mVarSwitches.get(1).setState(varswitch.charAt(3) - 48);  // 限重
        mVarSwitches.get(2).setState(varswitch.charAt(4) - 48);  // 角度
        mVarSwitches.get(3).setState(varswitch.charAt(1) - 48);  // 左限位器
        mVarSwitches.get(4).setState(varswitch.charAt(2) - 48);  // 右限位器
        mVarSwitchAdapter.notifyDataSetChanged();
    }

    // 控制输入
    private void updateControInput(String control_input){
        if (control_input.length() == 3) {
            if (control_input.charAt(0) == '0') { // 左右同
                if (control_input.charAt(1) == '0') { // 静止
                    mMotorLeft.setImageResource(R.mipmap.ic_motor_stop);
                    mMotorRight.setImageResource(R.mipmap.ic_motor_stop);
                } else if(control_input.charAt(1) == '1'){ // 运动
                    if (control_input.charAt(2) == '0') { // 向下
                        mMotorLeft.setImageResource(R.mipmap.ic_motor_down);
                        mMotorRight.setImageResource(R.mipmap.ic_motor_down);
                    } else if(control_input.charAt(2) == '1'){ // 向上
                        mMotorLeft.setImageResource(R.mipmap.ic_motor_up);
                        mMotorRight.setImageResource(R.mipmap.ic_motor_up);
                    }
                }
            } else if (control_input.charAt(0) == '1') { // 左右异
                if (control_input.charAt(1) == '0') { // 左电机运动，右电机静止
                    mMotorRight.setImageResource(R.mipmap.ic_motor_stop);
                    if (control_input.charAt(2) == '0') { // 向下
                        mMotorLeft.setImageResource(R.mipmap.ic_motor_down);
                    } else if (control_input.charAt(2) == '1'){ // 向上
                        mMotorLeft.setImageResource(R.mipmap.ic_motor_up);
                    }
                } else if (control_input.charAt(1) == '1') { // 右电机运动，左电机静止
                    mMotorLeft.setImageResource(R.mipmap.ic_motor_stop);
                    if (control_input.charAt(2) == '0') { // 向下
                        mMotorRight.setImageResource(R.mipmap.ic_motor_down);
                    } else if (control_input.charAt(2) == '1'){ // 向上
                        mMotorRight.setImageResource(R.mipmap.ic_motor_up);
                    }
                }
            }
        }
    }


    // 没有数据时,恢复初始状态
    private void initialState(){
        for (int i = 0; i < mVarSwitches.size(); i++){
            mVarSwitches.get(i).setState(2);
        }
        mVarSwitchAdapter.notifyDataSetChanged();
        mMotorLeft.setImageResource(R.mipmap.ic_motor_stop);
        mMotorRight.setImageResource(R.mipmap.ic_motor_stop);
        mDeviceWight.setText(" -- Kg");
        mClinometerDegree.setText("(--,--)");
        mLocationMsg.setText("(--°,--°,--m");
    }
}
