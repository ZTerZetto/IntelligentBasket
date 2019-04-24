package com.automation.zzx.intelligent_basket_demo.activity.areaAdmin;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.automation.zzx.intelligent_basket_demo.R;
import com.automation.zzx.intelligent_basket_demo.entity.AppConfig;
import com.automation.zzx.intelligent_basket_demo.fragment.areaAdmin.AreaAdminMgProjectFragment;
import com.automation.zzx.intelligent_basket_demo.utils.ToastUtil;
import com.automation.zzx.intelligent_basket_demo.utils.okhttp.BaseCallBack;
import com.automation.zzx.intelligent_basket_demo.utils.okhttp.BaseOkHttpClient;

import java.io.IOException;
import java.util.regex.Pattern;

import okhttp3.Call;

import static com.automation.zzx.intelligent_basket_demo.entity.AppConfig.AREA_ADMIN_APPLY_INSTALL;

public class UploadPreStopInfoActivity extends AppCompatActivity implements View.OnClickListener {

    private final static  String TAG = "UploadPreStopInfo";

    // 消息处理
    private final static int APPLY_SUCESS = 101;
    private final static int APPLY_FAILURE = 102;

    // 控件
    // 顶部导航栏
    private Toolbar mToolbar;  // 顶部导航栏
    private TextView mSendTextView; // 发送 按钮

    // 主体
    private EditText mProjectIdEv; // 项目编号
    private EditText mProjectNameEv; // 项目名称
    private EditText mBasketsNumberEv; // 吊篮数目
    private EditText mPreEndDaysEv; // 吊篮时间

    // 项目信息
    private String mProjectId;
    private String mProjectName;
    private int mBasketsNumInUse;  // 使用中的吊篮数目

    // token信息
    private SharedPreferences mPreferences;
    private String mToken;

    // 消息处理
    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case APPLY_SUCESS:
                    finish();
                    break;
                case APPLY_FAILURE:
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_pre_stop_info);

        // 获取上个页面的信息
        mProjectId = getIntent().getStringExtra(AreaAdminMgProjectFragment.PROJECT_ID);
        mProjectName = getIntent().getStringExtra(AreaAdminMgProjectFragment.PROJECT_NAME);
        mBasketsNumInUse = getIntent().getIntExtra(AreaAdminMgProjectFragment.BASKETS_NUM, 0);

        // 初始化控件
        initWidgetResource();
    }

    /*
     * 控件初始化
     */
    private void initWidgetResource() {
        // 顶部导航栏
        mToolbar = (Toolbar) findViewById(R.id.upload_toolbar);
        mToolbar.setTitle("上传预报停信息");
        setSupportActionBar(mToolbar);
        getSupportActionBar().setHomeButtonEnabled(true); //设置返回键可用
        mSendTextView = (TextView) findViewById(R.id.toolbar_send_textview);
        mSendTextView.setOnClickListener(this);

        // 主体
        mProjectIdEv = (EditText) findViewById(R.id.project_id);
        mProjectIdEv.setText(mProjectId);
        mProjectNameEv = (EditText) findViewById(R.id.project_name);
        mProjectNameEv.setText(mProjectName);
        mBasketsNumberEv = (EditText) findViewById(R.id.pre_stop_number);
        mBasketsNumberEv.requestFocus();
        mPreEndDaysEv = (EditText) findViewById(R.id.pre_stop_days);
    }

    /*
     * 消息监听
     */
    // 顶部导航栏消息响应
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home: // 返回按钮
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
    // 按钮点击响应
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.toolbar_send_textview:  // 发送监听
                if(checkInput())
                    uploadPreStopInfo();
                break;
        }
    }

    /*
     * 网络相关
     */
    // 上报预报停信息
    private void uploadPreStopInfo(){
        BaseOkHttpClient.newBuilder()
                .addHeader("Authorization", getTokenInfo())
                .addParam("days", Integer.parseInt(mPreEndDaysEv.getText().toString()))
                .addParam("num", Integer.parseInt(mBasketsNumberEv.getText().toString()))
                .addParam("projectId", mProjectId)
                .post()
                .url(AppConfig.AREA_ADMIN_SEND_PRE_STOP_INFO)
                .build()
                .enqueue(new BaseCallBack() {
                    @Override
                    public void onSuccess(Object o) {
                        JSONObject jsonObject = JSON.parseObject(o.toString());
                        String increse = jsonObject.getString("increase");
                        if(increse != null || increse.contains("成功")) {
                            Log.d(TAG, "成功提交预报停信息");
                            mHandler.sendEmptyMessage(APPLY_SUCESS);
                        }else{
                            Log.d(TAG, "提交预报停信息失败");
                        }
                    }

                    @Override
                    public void onError(int code) {
                        Log.d(TAG, "提交预报停信息错误，错误编码："+code);
                    }

                    @Override
                    public void onFailure(Call call, IOException e) {
                        Log.d(TAG, "提交预报停信息失败");
                    }
                });
    }

    // 输入检测
    private boolean checkInput(){
        // 吊篮数目
        String number = mBasketsNumberEv.getText().toString();
        // 检查是否为空
        if(number==null || number.equals("") || number.equals("0")) {
            ToastUtil.showToastTips(UploadPreStopInfoActivity.this, "吊篮数目不能为空");
            mBasketsNumberEv.requestFocus();  // 获取光标，并定位光标
            //showSoftInputFromWindow(mBasketsNumberEv);
            mBasketsNumberEv.setSelection(number.length());
            return false;
        }
        // 检查是否为数字
        if(!isNumeric(number)){
            ToastUtil.showToastTips(UploadPreStopInfoActivity.this, "吊篮数目不能为非数字");
            mBasketsNumberEv.setText("");
            mBasketsNumberEv.requestFocus();  // 获取光标，并定位光标
            return false;
        }
        // 检查数目是否超过正在运行中吊篮的数目
        if(Integer.parseInt(number) > mBasketsNumInUse){
            ToastUtil.showToastTips(UploadPreStopInfoActivity.this, "预报停数目大于正在使用的吊篮数目");
            mBasketsNumberEv.setText("");
            mBasketsNumberEv.requestFocus();  // 获取光标，并定位光标
            return false;
        }

        // 预报停时间
        String days = mPreEndDaysEv.getText().toString();
        if(days==null || days.equals("") || days.equals("0")) {
            ToastUtil.showToastTips(UploadPreStopInfoActivity.this, "预报停时间不能为空");
            mPreEndDaysEv.requestFocus();  // 获取光标，并定位光标
            mPreEndDaysEv.setSelection(days.length());
            //showSoftInputFromWindow(mPreEndDaysEv);
            return false;
        }
        if(!isNumeric(days)){
            ToastUtil.showToastTips(UploadPreStopInfoActivity.this, "吊篮数目不能为非数字");
            mPreEndDaysEv.setText("");
            mPreEndDaysEv.requestFocus();  // 获取光标，并定位光标
            //showSoftInputFromWindow(mPreEndDaysEv);
            return false;
        }

        return true;
    }

    /*
     * 其它函数
     */
    // 正则表达式，判断是否为数字
    private  boolean isNumeric(String str) {
        Pattern pattern = Pattern.compile("[0-9]*");
        return pattern.matcher(str).matches();
    }

    // 获取token信息
    private String getTokenInfo(){
        mPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        mToken = mPreferences.getString("loginToken", "");
        return mToken;
    }

}
