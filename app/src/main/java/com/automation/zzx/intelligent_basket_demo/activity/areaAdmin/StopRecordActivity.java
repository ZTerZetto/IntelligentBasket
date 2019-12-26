package com.automation.zzx.intelligent_basket_demo.activity.areaAdmin;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
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
import com.automation.zzx.intelligent_basket_demo.entity.AlarmInfo;
import com.automation.zzx.intelligent_basket_demo.entity.AppConfig;
import com.automation.zzx.intelligent_basket_demo.entity.StopRecord;
import com.automation.zzx.intelligent_basket_demo.entity.UserInfo;
import com.automation.zzx.intelligent_basket_demo.utils.ToastUtil;
import com.automation.zzx.intelligent_basket_demo.utils.okhttp.BaseCallBack;
import com.automation.zzx.intelligent_basket_demo.utils.okhttp.BaseOkHttpClient;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import okhttp3.Call;

import static android.content.ContentValues.TAG;

public class StopRecordActivity extends AppCompatActivity implements View.OnClickListener{
    // intent 消息参数
    public final static String PROJECT_ID = "projectId";  // 项目Id

    // Handler消息
    private final static int MG_STOP_LIST_INFO = 1;
    private final static int UPDATE_LIST_INFO = 2;

    //搜索框控件
    private SearchView mSearchView;
    private AutoCompleteTextView mAutoCompleteTextView;//搜索输入框
    private ImageView mDeleteButton;//搜索框中的删除按钮

    private List<StopRecord> stopRecordList = new ArrayList<>();
    private List<StopRecord> showRecordList = new ArrayList<>();;
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
                    showRecordList.clear();
                    parseAlarmListInfo((String) msg.obj);
                    adapter.notifyDataSetChanged();
                    updateContentView();
                    break;
                case UPDATE_LIST_INFO:
                    adapter.notifyDataSetChanged();
                    updateContentView(); // 更新项目信息及控件显示
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

        //搜索框
        mSearchView=findViewById(R.id.view_search);
        mAutoCompleteTextView=mSearchView.findViewById(R.id.search_src_text);
        mDeleteButton=mSearchView.findViewById(R.id.search_close_btn);
        mDeleteButton.setOnClickListener(this);
        mSearchView.setIconifiedByDefault(false);//设置搜索图标是否显示在搜索框内
        mAutoCompleteTextView.clearFocus(); //默认失去焦点
        mSearchView.setImeOptions(3);//设置输入法搜索选项字段，1:回车2:前往3:搜索4:发送5:下一項6:完成
//      mSearchView.setInputType(1);//设置输入类型
//      mSearchView.setMaxWidth(200);//设置最大宽度
        mSearchView.setQueryHint("输入吊篮ID或描述详情");//设置查询提示字符串
        mSearchView.setSubmitButtonEnabled(true);//设置是否显示搜索框展开时的提交按钮
        mAutoCompleteTextView.setTextColor(Color.GRAY);
        //设置SearchView下划线透明
        setUnderLinetransparent(mSearchView);
        setListener();


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
        showRecordList.addAll(stopRecordList);
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


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.search_close_btn:
                mAutoCompleteTextView.setText("");
                break;
            default:
                break;
        }
    }

    private void setListener(){

        // 设置搜索文本监听
        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            //当点击搜索按钮时触发该方法
            @Override
            public boolean onQueryTextSubmit(String query) {
                showRecordList.clear();
                if (query == null  || query.equals("")) {
                    showRecordList.addAll(stopRecordList);
                }else{
                    for (int i = 0; i < stopRecordList.size(); i++) {
                        StopRecord alarmInfo = stopRecordList.get(i);
                        if (alarmInfo.getDevice_id().contains(query)) {
                            showRecordList.add(alarmInfo);
                        }
                    }
                }
                handler.sendEmptyMessage(UPDATE_LIST_INFO);
                return true;
            }

            //当搜索内容改变时触发该方法
            @Override
            public boolean onQueryTextChange(String newText) {
                showRecordList.clear();
                if (newText == null || newText.equals("")) {
                    showRecordList.addAll(stopRecordList);
                }else{
                    for (int i = 0; i < stopRecordList.size(); i++) {
                        StopRecord alarmInfo = stopRecordList.get(i);
                        if (alarmInfo.getDevice_id().contains(newText)) {
                            stopRecordList.add(alarmInfo);
                        }
                    }
                }
                handler.sendEmptyMessage(UPDATE_LIST_INFO);
                return true;
            }
        });
    }

    /**设置SearchView下划线透明**/
    private void setUnderLinetransparent(SearchView searchView){
        try {
            Class<?> argClass = searchView.getClass();
            Field ownField = argClass.getDeclaredField("mSearchPlate");
            ownField.setAccessible(true);
            View mView = (View) ownField.get(searchView);
            mView.setBackgroundColor(Color.TRANSPARENT);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

}
