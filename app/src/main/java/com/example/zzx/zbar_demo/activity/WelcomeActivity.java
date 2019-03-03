package com.example.zzx.zbar_demo.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;

import com.example.zzx.zbar_demo.R;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by pengchenghu on 2019/2/22.
 * Author Email: 15651851181@163.com
 * Describe: 欢迎活动
 */

public class WelcomeActivity extends AppCompatActivity {

    public SharedPreferences pref;
    private String token;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        pref = PreferenceManager.getDefaultSharedPreferences(this);
        token = pref.getString("loginToken", "");
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);  // 设置全屏模式
        hideBottomUIMenu();  // 隐藏底部导航栏

        startMainActivity(); // 跳转至主活动
    }

    // 定时跳转至主页面
    public void startMainActivity(){
        TimerTask delayTask = new TimerTask() {
            @Override
            public void run() {
                if(token == null){
                    Intent mainIntent = new Intent(WelcomeActivity.this,LoginActivity.class);
                    startActivity(mainIntent);
                } else {
                    Intent mainIntent = new Intent(WelcomeActivity.this,ManageMainActivity.class);
                    startActivity(mainIntent);
                }
                WelcomeActivity.this.finish();
            }
        };
        Timer timer = new Timer();
        timer.schedule(delayTask,2000);//延时两秒执行 run 里面的操作
    }

    // 全屏模式
    protected void hideBottomUIMenu(){
        if(Build.VERSION.SDK_INT > 11 && Build.VERSION.SDK_INT < 19){  // lower api
            View v = this.getWindow().getDecorView();
            v.setSystemUiVisibility(View.GONE);
        }else if(Build.VERSION.SDK_INT >= 19){
            View decorView = getWindow().getDecorView();
            int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_FULLSCREEN;
            decorView.setSystemUiVisibility(uiOptions);
        }
    }

    // 当WelcomeActivity不可见时，销毁WelcomeActivity
    @Override
    protected void onStop(){
        super.onStop();
        finish();
    }
}
