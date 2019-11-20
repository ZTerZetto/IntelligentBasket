package com.automation.zzx.intelligent_basket_demo.activity.areaAdmin;

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
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.automation.zzx.intelligent_basket_demo.R;
import com.automation.zzx.intelligent_basket_demo.activity.loginRegist.LoginActivity;
import com.automation.zzx.intelligent_basket_demo.adapter.areaAdmin.StopRecordAdapter;
import com.automation.zzx.intelligent_basket_demo.entity.AppConfig;
import com.automation.zzx.intelligent_basket_demo.entity.StopRecord;
import com.automation.zzx.intelligent_basket_demo.entity.UserInfo;
import com.automation.zzx.intelligent_basket_demo.utils.ToastUtil;
import com.automation.zzx.intelligent_basket_demo.utils.okhttp.BaseCallBack;
import com.automation.zzx.intelligent_basket_demo.utils.okhttp.BaseOkHttpClient;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import okhttp3.Call;

import static android.content.ContentValues.TAG;

public class StopRecordActivity extends AppCompatActivity {
    // intent 消息参数
    public final static String PROJECT_ID = "projectId";  // 项目Id

    // Handler消息
    private final static int MG_STOP_LIST_INFO = 1;

    private List<StopRecord> stopRecordList = new ArrayList<>();;
    private StopRecordAdapter adapter;

    private LinearLayout llRecordSum;
    private TextView tvRecordSum;
    private ListView lvStop;

    // 空空如也
    private RelativeLayout noRecordListRelativeLayout;
    private TextView noRecordListTextView;

    // 用户登录信息相关
    private UserInfo mUserInfo;
    private String mToken;
    private SharedPreferences mPref;

    // 项目信息
    private String mProjectId;

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            switch(msg.what) {
                case MG_STOP_LIST_INFO: // 获取报停记录
                    stopRecordList.clear();
                    parseAlarmListInfo((String) msg.obj);
                    adapter.notifyDataSetChanged();
                    updateContentView();
                    break;
                default:
                    break;
            }
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stop_record);

        //获取用户数据
        getUserInfo();
        //获取记录数据
        getAlarmInfoListByProjectId();
        //初始化控件
        initView();
    }

    // 获取用户数据
    private void getUserInfo(){
        // 从本地获取数据
        mPref = PreferenceManager.getDefaultSharedPreferences(this);
        mUserInfo = new UserInfo();
        mUserInfo.setUserId(mPref.getString("userId", ""));
        mUserInfo.setUserPhone(mPref.getString("userPhone", ""));
        mUserInfo.setUserRole(mPref.getString("userRole", ""));
        mToken = mPref.getString("loginToken","");

        //获取当前项目ID
        Intent intent = getIntent();
        mProjectId = intent.getStringExtra(PROJECT_ID);

    }



    private void initView() {

        // 顶部导航栏
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        TextView titleText = (TextView) findViewById(R.id.toolbar_title);
        toolbar.setTitle("报停记录");
        titleText.setText("");
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true); //设置返回键可用

        // 空空如也
        noRecordListRelativeLayout = (RelativeLayout) findViewById(R.id.record_no_avaliable);
        noRecordListTextView = (TextView)findViewById(R.id.no_record_hint);

        llRecordSum = findViewById(R.id.ll_record_sum);
        tvRecordSum = findViewById(R.id.tv_record_sum);
        lvStop = findViewById(R.id.record_list_id);
        adapter=new StopRecordAdapter(this,R.layout.item_stop_record,stopRecordList);
        lvStop.setAdapter(adapter);

    }

    /*
     * 网络请求相关
     */
    // 从后台获取报警记录
    public void getAlarmInfoListByProjectId() {
        BaseOkHttpClient.newBuilder()
                .addHeader("Authorization",mToken)
                .addParam("type",1) //获取方式 1：按照项目ID获取
                .addParam("value",mProjectId)
                .get()
                .url(AppConfig.GET_STOP_RECORD)
                .build()
                .enqueue(new BaseCallBack() {
                    @Override
                    public void onSuccess(Object o) {
                        Log.i(TAG, "成功" );
                        String responseData = o.toString();
                        Message message = new Message();
                        message.what = MG_STOP_LIST_INFO;
                        message.obj = responseData;
                        handler.sendMessage(message);
                    }

                    @Override
                    public void onError(int code) {
                        Log.i(TAG, "错误：" + code);
                        switch (code){
                            case 401: // 未授权
                                ToastUtil.showToastTips(StopRecordActivity.this, "登录已过期，请重新登陆");
                                startActivity(new Intent(StopRecordActivity.this, LoginActivity.class));
                                finish();
                                break;
                            case 403: // 禁止
                                break;
                            case 404: // 404
                                break;
                        }
                    }

                    @Override
                    public void onFailure(Call call, IOException e) {
                        Log.i(TAG, "失败：" + e.toString());
                    }
                });
    }

    //解析报警记录信息
    private void parseAlarmListInfo(String responseDate){
        JSONObject jsonObject = JSON.parseObject(responseDate);
        String alarmInfo =jsonObject.getString("electricBoxStopInfo");

        JSONArray jsonArray = JSON.parseArray(alarmInfo);
        if(jsonArray.size() >= 1){
            Iterator<Object> iterator = jsonArray.iterator(); //迭代获取项目信息
            while(iterator.hasNext()) {
                JSONObject repairObj = (JSONObject) iterator.next();
                //时间字符串处理
                String timeDate = repairObj.getString("time").substring(0,10);
                String timeHM = repairObj.getString("time").substring(11,19);
                String time = timeDate + " " + timeHM;
                StopRecord mStopInfo = new StopRecord(repairObj.getString("device_id"),repairObj.getString("project_id"),
                        repairObj.getString("id"),time);
                stopRecordList.add(mStopInfo);
            }
        }
    }

    /*
     * UI 更新相关
     */
    private void updateContentView() {
        if (stopRecordList.size() < 1) { // 暂无报停记录
            lvStop.setVisibility(View.GONE);
            llRecordSum.setVisibility(View.GONE);
            noRecordListRelativeLayout.setVisibility(View.VISIBLE);
            noRecordListTextView.setText("暂无报警记录！");
        } else {  // 好多吊篮
            noRecordListRelativeLayout.setVisibility(View.GONE);
            lvStop.setVisibility(View.VISIBLE);
            llRecordSum.setVisibility(View.VISIBLE);
            tvRecordSum.setText(String.valueOf(stopRecordList.size()));
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


}
