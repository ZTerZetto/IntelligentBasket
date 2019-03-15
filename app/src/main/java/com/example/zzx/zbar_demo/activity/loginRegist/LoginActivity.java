package com.example.zzx.zbar_demo.activity.loginRegist;


import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.example.zzx.zbar_demo.R;
import com.example.zzx.zbar_demo.Util.HttpUtil;
import com.example.zzx.zbar_demo.activity.ManageMainActivity;
import com.example.zzx.zbar_demo.activity.WorkerMainActivity;
import com.example.zzx.zbar_demo.entity.UserInfo;
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
    private String account;
    private String password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

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
            account = pref.getString("user_id", "");
            password = pref.getString("password", "");
            accountEdit.setText(account);
            accountEdit.setSelection(account.length());//光标定位到最后
            passwordEdit.setText(password);
        }



        //跳转至注册界面
        to_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                StartRegistActicity();
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
                StartManageMainActicity(null);
            }
        });
    }

    //登录请求连接
    private void loginHttp(UserInfo userInfo){
        String json = new Gson().toJson(userInfo);
//        UserInfo userInfo1 = new UserInfo();
//        userInfo1.setUserId("admin");
//        userInfo1.setUserPassword("admin");
//        String json = new Gson().toJson(userInfo1);
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
                    switch (isLogin) {
                        case "true":
                            //TODO 保存Token
                            String token = jsonObject.getString("token");
                            savePref(token);
                            StartManageMainActicity(isLogin);
                            break;
                        case "rentAdmin":
                            //StartWorkerMainActicity(isLogin);
                            break;
                        case "areaAdmin":
                            //StartWorkerMainActicity(isLogin);
                            break;
                        default:
                            //StartWorkerMainActicity("wrong");

                            break;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        },json);
    }

 /*   private void GetUser(String responseData) {
        JSONObject jsonObject = JSON.parseObject(responseData);
        String role = jsonObject.getString("role");
        String ticket = jsonObject.getString("ticket");
        String username = jsonObject.getString("username");
        String permissionstr = jsonObject.getString("permission");
        int userid = jsonObject.getInteger("userId");
        List<String> permission = JSON.parseArray(permissionstr,String.class);
        //设置当前用户
        User.setUser(username,userid,ticket,role,permission);
    }*/

    //保存Token
    private void savePref(String token) {
        SharedPreferences sp = getSharedPreferences("loginToken",0);
        editor = sp.edit();

        editor = pref.edit();
        if (rememberPass.isChecked()) {
            editor.putBoolean("remember_password", true);
            editor.putString("user_id", account);
            editor.putString("password", password);
            editor.putString("loginToken",token);
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
    public void StartWorkerMainActicity(String isLogin) {
        Intent intent = new Intent(LoginActivity.this,  WorkerMainActivity.class);
        startActivity(intent);
        finish();
    }

    //跳转到注册界面
    public void StartRegistActicity( ) {
        Intent intent = new Intent(LoginActivity.this, RegisterPreActivity.class);
        startActivity(intent);
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
