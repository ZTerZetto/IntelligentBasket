package com.automation.zzx.intelligent_basket_demo.activity.common;


import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.method.PasswordTransformationMethod;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.automation.zzx.intelligent_basket_demo.R;
import com.automation.zzx.intelligent_basket_demo.activity.loginRegist.LoginActivity;
import com.automation.zzx.intelligent_basket_demo.entity.AppConfig;
import com.automation.zzx.intelligent_basket_demo.utils.ToastUtil;
import com.automation.zzx.intelligent_basket_demo.utils.okhttp.BaseCallBack;
import com.automation.zzx.intelligent_basket_demo.utils.okhttp.BaseOkHttpClient;
import com.automation.zzx.intelligent_basket_demo.widget.dialog.CommonDialog;

import java.io.IOException;

import okhttp3.Call;

public class PasswordChangeActivity extends AppCompatActivity implements View.OnClickListener{


    private final static int UPDATE_SUCCESS = 100;  // 修改密码成功
    private final static int UPDATE_FAIL = 101;  // 修改密码失败
    private final static int UPDATE_FAIL_PASSWORD = 102;  // 旧密码错误

    // 控件
    private TextView mUserIdTv;
    private TextView mUserNameTv;
    private EditText oldPasswordEt;
    private EditText newPasswordEt;
    private EditText newPasswordEt2;


    // 顶部导航栏
    private Toolbar mToolbar;  // 顶部导航栏
    private ImageView mSendImageView; // 发送 图标
    private String userId;
    private String userName;

    //基本信息
    private SharedPreferences pref;
    private SharedPreferences.Editor editor;

    // 上传基本参数
    private String token; // 验证token




    @SuppressLint("HandlerLeak")
    public Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case UPDATE_SUCCESS:  // 上传图片成功
                    DialogToast("提示", "修改密码成功" ).show();
                    //更改本地保存的密码
                    editor = pref.edit();
                    editor.putString("password", newPasswordEt.getText().toString());
                    break;
                case UPDATE_FAIL: // 上传图片失败
                    Toast.makeText(getApplicationContext(), "修改密码失败！", Toast.LENGTH_LONG).show();
                    break;
                case UPDATE_FAIL_PASSWORD:
                    Toast.makeText(getApplicationContext(), "旧密码错误，请核对后重新输入！", Toast.LENGTH_LONG).show();
                    oldPasswordEt.getText().clear();
                    break;
                default:
                    break;
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password_change);

        //初始化信息
        initInfo();
        // 初始化控件
        initWidgetResource();

    }


    /*
     * Intent 初始化
     */
    private void initInfo(){
        Intent intent = getIntent();
        userId = intent.getStringExtra("userId");
        userName = intent.getStringExtra("userName");

        pref = PreferenceManager.getDefaultSharedPreferences(this);
        token = pref.getString("loginToken", "");
    }

    /*
     * 控件初始化
     */
    private void initWidgetResource() {
        // 顶部导航栏
        mToolbar = (Toolbar) findViewById(R.id.upload_toolbar);
        mToolbar.setTitle("");
        setSupportActionBar(mToolbar);
        getSupportActionBar().setHomeButtonEnabled(true); //设置返回键可用
        mSendImageView = (ImageView) findViewById(R.id.toolbar_send_imageview);
        mSendImageView.setOnClickListener(this);

        mUserIdTv = (TextView) findViewById(R.id.user_id_textview);
        mUserIdTv.setText(userId);
        mUserNameTv = (TextView) findViewById(R.id.user_name_textview);
        mUserNameTv.setText(userName);
        oldPasswordEt = findViewById(R.id.password_old_editView);
        oldPasswordEt.setTransformationMethod(PasswordTransformationMethod.getInstance());
        newPasswordEt = findViewById(R.id.password_new_editView);
        newPasswordEt.setTransformationMethod(PasswordTransformationMethod.getInstance());
        newPasswordEt2 = findViewById(R.id.password_new_again_editView);
        newPasswordEt2.setTransformationMethod(PasswordTransformationMethod.getInstance());

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
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // 按钮点击响应
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.toolbar_send_imageview: // 发送监听
                //判断信息填写是否合格
                isInfoOk();

                break;
        }
    }

    //新密码确认是否通过
    private void isInfoOk(){
        //判断信息是否完全
        String password = newPasswordEt.getText().toString();
        String password_2 = newPasswordEt2.getText().toString();

        if(password.length() < 6 || password_2.length() < 6){
            Toast.makeText(getApplicationContext(), "请输入符合格式要求的密码", Toast.LENGTH_LONG).show();
            newPasswordEt.getText().clear();
            newPasswordEt2.getText().clear();
        } else if (!password.equals(password_2)) {
            Toast.makeText(getApplicationContext(), "两次密码输入不一致！", Toast.LENGTH_LONG).show();
            newPasswordEt.getText().clear();
            newPasswordEt2.getText().clear();
        } else {
            passwordChangeHttp();
        }
    }

    /*
    * 修改密码申请
    * */
    private void passwordChangeHttp(){
        BaseOkHttpClient.newBuilder()
                .addHeader("Authorization", token)
                .addParam("userId",userId)
                .addParam("oldPassword",oldPasswordEt.getText().toString())
                .addParam("newPassword",newPasswordEt.getText().toString())
                .post()
                .url(AppConfig.UPDATE_PASSWORD)
                .build()
                .enqueue(new BaseCallBack() {
                    @Override
                    public void onSuccess(Object o) {
                        JSONObject jsonObject = JSON.parseObject(o.toString());
                        if(jsonObject.getString("result").equals("success")) {
                            // 申请成功
                            mHandler.sendEmptyMessage(UPDATE_SUCCESS);
                        }else if (jsonObject.getString("result").equals("IncorrectPassword")){
                            // 申请失败
                            mHandler.sendEmptyMessage(UPDATE_FAIL_PASSWORD);
                        } else {
                            // 申请失败
                            mHandler.sendEmptyMessage(UPDATE_FAIL);
                        }
                    }

                    @Override
                    public void onError(int code) {
                        switch (code){
                            case 401: // 未授权
                                ToastUtil.showToastTips(PasswordChangeActivity.this, "登录已过期，请重新登陆");
                                startActivity(new Intent(PasswordChangeActivity.this, LoginActivity.class));
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
                        mHandler.sendEmptyMessage(UPDATE_FAIL);
                    }
                });
    }

    /*
     * 弹窗
     */
    // 提示弹框
    private CommonDialog DialogToast(String mTitle, String mMsg){
        return new CommonDialog(PasswordChangeActivity.this, R.style.dialog, mMsg,
                new CommonDialog.OnCloseListener() {
                    @Override
                    public void onClick(Dialog dialog, boolean confirm) {
                        if (confirm) {
                            dialog.dismiss();
                            finish();
                        } else {
                            dialog.dismiss();
                        }
                    }
                }).setTitle(mTitle);
    }


}
