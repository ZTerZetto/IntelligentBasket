package com.example.zzx.zbar_demo.activity.loginRegist;


import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
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
import com.example.zzx.zbar_demo.R;
import com.example.zzx.zbar_demo.utils.HttpUtil;
import com.example.zzx.zbar_demo.activity.ManageMainActivity;
import com.example.zzx.zbar_demo.activity.worker.WorkerPrimaryActivity;
import com.example.zzx.zbar_demo.entity.UserInfo;
import com.google.gson.Gson;
import java.io.IOException;
import java.lang.reflect.Field;

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
    private String account;
    private String password;

    private AlertDialog alertDialog;


    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    dialogToRegist(getString(R.string.dialog_login_audit));
                    break;
                case 2:
                    dialogToRegist(getString(R.string.dialog_login_fail));
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
        //getSupportActionBar().setHomeButtonEnabled(false); //设置返回键可用

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
                    loginHttp(userInfo);
                } else {
                    Toast.makeText(LoginActivity.this, "账户或密码不能为空", Toast.LENGTH_SHORT).show();
                }
            }
        });

        //不联网测试
        test.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // StartAndFinishActicity(null,ManageMainActivity.class);
                StartAndFinishActicity(null, WorkerPrimaryActivity.class); // 跳转到施工人员界面
            }
        });
    }


    //登录请求连接
    private void loginHttp(UserInfo userInfo){
        final String json = new Gson().toJson(userInfo);
        HttpUtil.sendLoginOkHttpRequest(new okhttp3.Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                //异常情况处理
                Looper.prepare();
                Toast.makeText(LoginActivity.this, "网络连接失败！", Toast.LENGTH_LONG).show();
                Looper.loop();
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                // 返回服务器数据
                String responseData = response.body().string();
                try {
                    JSONObject jsonObject = JSON.parseObject(responseData);
                    String isLogin = jsonObject.getString("isLogin");
                    String state = jsonObject.getString("registerState");
                    // pengchenghu
                    String userInfo = jsonObject.getString("userInfo");
                    JSONObject userInfoJsonObj = JSON.parseObject(userInfo);
                    // pengchenghu
                    if (isLogin.equals("true")) {
                        String token = jsonObject.getString("token");
                        String userRole = userInfoJsonObj.getString("userRole");
                        String userId = userInfoJsonObj.getString("userId");
                        savePref(token, userId, userRole);
                        switch (userRole){
                            case "worker":  // 工人主页面
                                Intent intent = new Intent(LoginActivity.this,
                                        WorkerPrimaryActivity.class);
                                startActivity(intent);
                                break;
                            case "rentAdmin":  // 租房管理员
                                StartManageMainActicity(isLogin);
                                break;
                            case "areaAdmin":  // 区域管理员
                                StartManageMainActicity(isLogin);
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
    private void savePref(String token, String userId, String userRole) {
        SharedPreferences sp = getSharedPreferences("loginToken",0);
        editor = sp.edit();

        editor = pref.edit();
        if (rememberPass.isChecked()) {
            editor.putBoolean("remember_password", true);
            editor.putString("userId",userId);
            editor.putString("userPhone", account);
            editor.putString("password", password);
            editor.putString("loginToken",token);
            editor.putString("userRole",userRole);
        } else {
            editor.clear();
            //清空数据
        }
        editor.commit();
    }

    //跳转到管理员主界面
    public void StartManageMainActicity(String isLogin) {
        Intent intent = new Intent(LoginActivity.this, ManageMainActivity.class);
        //intent.putExtra("userRole",isLogin);
        startActivity(intent);
        finish();
    }

    //跳转到施工人员主界面
    public void StartAndFinishActicity(String isLogin ,Class<?> cls ) {
        Intent intent = new Intent(LoginActivity.this,  cls);
        startActivity(intent);
        finish();
    }

    //跳转到注册界面
    public void StartActicity( Class<?> cls ) {
        Intent intent = new Intent(LoginActivity.this, cls);
        startActivity(intent);
    }

    private void dialogToRegist(String mMsg) {
        alertDialog = new AlertDialog.Builder(this)
                .setTitle("登陆失败")
                .setMessage(mMsg)
                .setIcon(R.mipmap.ic_warning)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        alertDialog.cancel();
                    }
                })
                .create();
        alertDialog.show();
        try {
            Field mAlert = AlertDialog.class.getDeclaredField("mAlert");
            mAlert.setAccessible(true);
            Object mAlertController = mAlert.get(alertDialog);
            Field mMessage = mAlertController.getClass().getDeclaredField("mMessageView");
            mMessage.setAccessible(true);
            TextView mMessageView = (TextView) mMessage.get(mAlertController);
            mMessageView.setTextColor(Color.GRAY);
            Field mTitle = mAlertController.getClass().getDeclaredField("mTitleView");
            mTitle.setAccessible(true);
            TextView mTitleView = (TextView) mTitle.get(mAlertController);
            mTitleView.setTextColor(Color.GRAY);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
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


    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
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
    }
}
