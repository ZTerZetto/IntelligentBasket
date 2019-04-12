package com.automation.zzx.intelligent_basket_demo.activity.loginRegist;


import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.automation.zzx.intelligent_basket_demo.R;
import com.automation.zzx.intelligent_basket_demo.activity.areaAdmin.AreaAdminPrimaryActivity;
import com.automation.zzx.intelligent_basket_demo.activity.rentAdmin.RentAdminPrimaryActivity;
import com.automation.zzx.intelligent_basket_demo.utils.http.HttpUtil;
import com.automation.zzx.intelligent_basket_demo.activity.worker.WorkerPrimaryActivity;
import com.automation.zzx.intelligent_basket_demo.entity.UserInfo;
import com.automation.zzx.intelligent_basket_demo.widget.dialog.CommonDialog;
import com.google.gson.Gson;
import java.io.IOException;

import okhttp3.Call;
import okhttp3.Response;

public class LoginActivity extends AppCompatActivity {

    /*可本地保存密码，
        * 未和服务器端相连接*/
    public SharedPreferences pref;
    private SharedPreferences.Editor editor;

    private EditText accountEdit;
    private EditText passwordEdit;

    private Button login;
    private Button to_register;
    private Button test;

    private CheckBox rememberPass;

    private UserInfo userInfo;
    private UserInfo mUserInfo;
    private String account;
    private String password;

    private CommonDialog mCommonDialog;


    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    if (mCommonDialog == null) {
                        mCommonDialog = initDialog(getString(R.string.dialog_login_audit));
                    }
                    mCommonDialog.show();
                break;
                case 2:

                    if (mCommonDialog == null) {
                        mCommonDialog = initDialog(getString(R.string.dialog_login_audit_fail));
                    }
                    mCommonDialog.show();
                    break;
                case 3:
                    if (mCommonDialog == null) {
                        mCommonDialog = initDialog(getString(R.string.dialog_login_fail));
                    }
                    mCommonDialog.show();
                    break;
                default:
                    break;
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // 顶部导航栏
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        TextView titleText = (TextView) findViewById(R.id.toolbar_title);
        toolbar.setTitle("");
        titleText.setText(getString(R.string.login_title));
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false); //设置返回键可用

        pref = PreferenceManager.getDefaultSharedPreferences(this);
        accountEdit =  findViewById(R.id.text_input_username);
        passwordEdit = findViewById(R.id.text_input_password);
        rememberPass = findViewById(R.id.remember_pass);
        login =  findViewById(R.id.Login_Button);
        to_register = findViewById(R.id.To_Register);
        test = findViewById(R.id.Test_Button);

        boolean isRemember = pref.getBoolean("remember_password", false);
        if (isRemember) {
            //将账号和密码都设置到文本框中
            account = pref.getString("userPhone", "");
            password = pref.getString("password", "");
            accountEdit.setText(account);
            accountEdit.setSelection(account.length());//光标定位到最后
            passwordEdit.setText(password);
        }

        //跳转至注册界面
        to_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                StartActicity(RegisterPreActivity.class);
            }
        });

        //登录功能
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                account = accountEdit.getText().toString();
                password = passwordEdit.getText().toString();
                if (!TextUtils.isEmpty(account) && !TextUtils.isEmpty(password)) {
                    userInfo = new UserInfo(account,password);
                    loginHttp();
                } else {
                    Toast.makeText(LoginActivity.this, "账户或密码不能为空", Toast.LENGTH_SHORT).show();
                }
            }
        });

        //不联网测试
        test.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //StartAndFinishActicity(WorkerPrimaryActivity.class); // 跳转到施工人员界面
                //StartAndFinishActicity(RentAdminPrimaryActivity.class); // 跳转到租方管理人员界面
                StartAndFinishActicity(AreaAdminPrimaryActivity.class); // 跳转到区域管理人员界面
            }
        });
    }


    //登录请求连接
    private void loginHttp( ){
        final String json = new Gson().toJson(userInfo);
        HttpUtil.sendLoginOkHttpRequest(new okhttp3.Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                //异常情况处理
                Looper.prepare();
                Toast.makeText(LoginActivity.this, "网络连接失败！", Toast.LENGTH_SHORT).show();
                Looper.loop();
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if(response.code() != 200){
                    Looper.prepare();
                    Toast.makeText(LoginActivity.this, "网络连接超时,请稍后重试！", Toast.LENGTH_SHORT).show();
                    Looper.loop();
                }
                // 返回服务器数据
                String responseData = response.body().string();
                try {
                    JSONObject jsonObject = JSON.parseObject(responseData);
                    String isLogin = jsonObject.getString("isLogin");
                    String state = jsonObject.getString("registerState");
                    // pengchenghu
                    String userString = jsonObject.getString("userInfo");
                    mUserInfo =JSON.parseObject(userString,UserInfo.class);
                    JSONObject userInfoJsonObj = JSON.parseObject(userString);
                    // pengchenghu
                    if (isLogin.equals("true")) {
                        String token = jsonObject.getString("token");
                        String userRole = userInfoJsonObj.getString("userRole");
                        savePref(token);
                        switch (userRole){
                            case "worker":  // 工人主页面
                            case "worker_1":
                            case "worker_2":
                            case "worker_3":
                            case "worker_4":
                            case "worker_5":
                                StartAndFinishActicity(WorkerPrimaryActivity.class);
                                break;
                            case "rentAdmin":  // 租房管理员
                                StartAndFinishActicity(RentAdminPrimaryActivity.class);
                                break;
                            case "areaAdmin":  // 区域管理员
                                StartAndFinishActicity(AreaAdminPrimaryActivity.class);
                                break;
                        }
                    } else {
                        switch (state) {
                            case "1":
                                //审核中
                                Message msg = new Message();
                                msg.what = 1;
                                handler.sendMessage(msg);
                                break;
                            case "2":
                                //审核失败
                                Message msg2 = new Message();
                                msg2.what = 2;
                                handler.sendMessage(msg2);
                                break;
                            case "3":
                                //账号密码错误
                                Message msg3 = new Message();
                                msg3.what = 3;
                                handler.sendMessage(msg3);
                                break;
                            default:
                                break;
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, json);
    }

    //保存Token
    private void savePref(String token) {
        SharedPreferences sp = getSharedPreferences("loginToken",0);
        editor = sp.edit();

        editor = pref.edit();
        if (rememberPass.isChecked()) {
            editor.putBoolean("remember_password", true);
            editor.putString("userId",mUserInfo.getUserId());
            editor.putString("userPhone", account);
            editor.putString("password", password);
            editor.putString("loginToken",token);
            editor.putString("userRole",mUserInfo.getUserRole());
            editor.putString("userName",mUserInfo.getUserName());
        } else {
            editor.clear();
            //清空数据
        }
        editor.commit();
    }

    //跳转到用户主界面
    public void StartAndFinishActicity( Class<?> cls ) {
        Intent intent = new Intent(LoginActivity.this,  cls);
        startActivity(intent);
        finish();
    }

    //跳转到注册界面
    public void StartActicity( Class<?> cls ) {
        Intent intent = new Intent(LoginActivity.this, cls);
        startActivity(intent);
    }

    /*
     * 提示弹框
     */
    private CommonDialog initDialog(String mMsg){
        return new CommonDialog(this, R.style.dialog, mMsg,
                new CommonDialog.OnCloseListener() {
                    @Override
                    public void onClick(Dialog dialog, boolean confirm) {
                        if(confirm){
                            dialog.dismiss();
                        }else{
                            dialog.dismiss();
                        }
                    }
                }).setTitle("提示");
    }

    @Override
    protected void onActivityResult(int requestCode,int resultCode,Intent data){
        switch (requestCode){
            case 1:
                if (resultCode == RESULT_OK){
                    String nick = data.getStringExtra("nick_return");
                    accountEdit.setText(nick);
                    passwordEdit.getText().clear();
                    passwordEdit.selectAll();//光标定位到最后

                } else if(resultCode == RESULT_CANCELED){
                    accountEdit.getText().clear();
                    passwordEdit.getText().clear();
                }
                break;
            default:
                break;
        }
    }


}
