package com.automation.zzx.intelligent_basket_demo.activity.loginRegist;


import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
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
import com.automation.zzx.intelligent_basket_demo.R;
import com.automation.zzx.intelligent_basket_demo.activity.areaAdmin.AreaAdminPrimaryActivity;
import com.automation.zzx.intelligent_basket_demo.activity.areaAdmin.AreaAdminPrimaryOldActivity;
import com.automation.zzx.intelligent_basket_demo.activity.basketSupervisor.SupervisorPrimaryActivity;
import com.automation.zzx.intelligent_basket_demo.activity.inspectionPerson.InspectPersonPrimaryActivity;
import com.automation.zzx.intelligent_basket_demo.activity.proAdmin.ProAdminPrimaryActivity;
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

    private CheckBox rememberPass;  //记住密码
    private CheckBox autoLogin;  //自动登录
    private int shareState = 0;  //本地存储状态

    private UserInfo userInfo;
    private UserInfo mUserInfo;
    private String account;
    private String password;

    private String[] mProAdminRoleList;  // 项管登录角色列表
    private int currentSelected = 0; // 当前角色位置
    private int tmpSelected = 0; // 临时角色位置

    private CommonDialog mCommonDialog;
    private AlertDialog mSelectProjectDialog;  // 选择角色弹窗


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
                case 4:
                    //弹出选择框
                    showSelectDialog();
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
        autoLogin = findViewById(R.id.auto_login);
        login =  findViewById(R.id.Login_Button);
        to_register = findViewById(R.id.To_Register);
        test = findViewById(R.id.Test_Button);

        initProAdminRoleList();

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
                StartAndFinishActicity(AreaAdminPrimaryActivity.class); // 跳转到区域管理人员界面
            }
        });


    }


    private void initProAdminRoleList(){
        mProAdminRoleList = new String[2];
        mProAdminRoleList[0] = "施工人员";
        mProAdminRoleList[1] = "项目管理员";
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
                                //判断是否为项目管理员
                                judgeProAdminHttp(token);
                                break;
                            case "rentAdmin":  // 租方管理员
                                StartAndFinishActicity(RentAdminPrimaryActivity.class);
                                break;
                            case "areaAdmin":  // 区域管理员
                                StartAndFinishActicity(AreaAdminPrimaryOldActivity.class);
                                break;
                            case "inspector":  // 巡检人员
                                StartAndFinishActicity(InspectPersonPrimaryActivity.class);
                                break;
                            case "basketSupervisor":  // 吊篮监管人员（安监局）
                                StartAndFinishActicity(SupervisorPrimaryActivity.class);
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

    //判断是否为项管请求连接
    private void judgeProAdminHttp(String token){
        HttpUtil.sendIsProAdminOkHttpRequest(new okhttp3.Callback() {
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
                    String isProAdmin = jsonObject.getString("projectAdmin");
                    //保存至本地pref
                    SharedPreferences sp = getSharedPreferences("loginToken",0);
                    editor = sp.edit();
                    editor = pref.edit();
                    editor.putString("isProAdmin", isProAdmin);
                    editor.commit();
                    if(isProAdmin.equals("1")){
                        //审核中
                        Message msg = new Message();
                        msg.what = 4;
                        handler.sendMessage(msg);
                    } else {
                        StartAndFinishActicity(WorkerPrimaryActivity.class);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }


        }, mUserInfo.getUserId(),token);
    }


    //保存Token
    private void savePref(String token) {
        SharedPreferences sp = getSharedPreferences("loginToken",0);
        editor = sp.edit();
        editor = pref.edit();
        //记住密码时
        if(rememberPass.isChecked()){
            saveAccountPref();
            saveTokenPref(token);
        } else {
            editor.putBoolean("remember_password", false);
            saveTokenPref(token);
        }
        editor.commit();
    }

    //保存账号密码
    private void saveAccountPref( ) {
        editor.putBoolean("remember_password", true);
        editor.putString("userPhone", account);
        editor.putString("password", password);
    }

    //保存自动登录数据
    private void saveTokenPref(String token ) {
        editor.putBoolean("auto_login", true);
        editor.putString("password", password);
        editor.putString("userId",mUserInfo.getUserId());
        editor.putString("loginToken",token);
        editor.putString("userPhone", account);
        editor.putString("userRole",mUserInfo.getUserRole());
        editor.putString("userName",mUserInfo.getUserName());
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



    // 弹出身份选择框
    public void showSelectDialog(){
        final AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
        alertBuilder.setTitle("登录角色选择");
        alertBuilder.setSingleChoiceItems(mProAdminRoleList, currentSelected, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int position) {
                tmpSelected = position;
            }
        });

        alertBuilder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                currentSelected = tmpSelected;
                //跳转至所选择的角色主界面
                if(currentSelected == 0){
                    StartAndFinishActicity(WorkerPrimaryActivity.class);
                } else {
                    StartAndFinishActicity(ProAdminPrimaryActivity.class);
                }
                mSelectProjectDialog.dismiss();
            }
        });

        alertBuilder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                mSelectProjectDialog.dismiss();
            }
        });

        mSelectProjectDialog = alertBuilder.create();
        mSelectProjectDialog.show();
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
