package com.automation.zzx.intelligent_basket_demo.activity.common;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.automation.zzx.intelligent_basket_demo.R;

/**
 * Created by pengchenghu on 2019/3/28.
 * Author Email: 15651851181@163.com
 * Describe: 个人信息页面
 */

public class PersonalInformationActivity extends AppCompatActivity implements View.OnClickListener {

    private final static String TAG = "PersonalInformation";

    // 控件
    // 账号信息
    private RelativeLayout mUserHeadRv;
    private ImageView mUserHeadIv; // 头像
    private RelativeLayout mUserNameRv; // 用户名
    private TextView mUserNameTv;
    private RelativeLayout mUserPhoneRv; // 手机号
    private TextView mUserPhoneTv;
    private RelativeLayout mUserPasswordRv; // 密码
    private RelativeLayout mUserQRcodeRv; // 二维码名片
    private RelativeLayout mUserMoreRv; // 更多

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personal_information);

        initWidgetResource();
    }

    /*
     * 控件初始化
     */
    private void initWidgetResource(){
        // 顶部导航栏
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        TextView titleText = (TextView) findViewById(R.id.toolbar_title);
        toolbar.setTitle("个人信息");
        titleText.setText("");
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true); //设置返回键可用

        // 个人信息栏
        mUserHeadRv = (RelativeLayout) findViewById(R.id.user_head_layout);
        mUserHeadRv.setOnClickListener(this);
        mUserHeadIv = (ImageView) findViewById(R.id.user_head_imageview);
        mUserHeadIv.setOnClickListener(this);
        mUserNameRv = (RelativeLayout) findViewById(R.id.user_name_layout);
        mUserNameRv.setOnClickListener(this);
        mUserNameTv = (TextView) findViewById(R.id.user_name_textview);
        mUserPhoneRv = (RelativeLayout) findViewById(R.id.user_phone_layout);
        mUserPhoneTv = (TextView) findViewById(R.id.user_phone_textview);
        mUserPasswordRv = (RelativeLayout) findViewById(R.id.user_password_layout);
        mUserPasswordRv.setOnClickListener(this);
        mUserQRcodeRv = (RelativeLayout) findViewById(R.id.user_qrcode_layout);
        mUserQRcodeRv.setOnClickListener(this);
        mUserMoreRv = (RelativeLayout) findViewById(R.id.user_more_layout);
        mUserMoreRv.setOnClickListener(this);
    }

    /*
     * 消息监听
     */
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

    // 控件点击响应
    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.user_head_layout:
                Log.i(TAG, "You have clicked the user_head_layout");
                break;
            case R.id.user_head_imageview:
                Log.i(TAG, "You have clicked the user_head_image");
                break;
            case R.id.user_name_layout:
                Log.i(TAG, "You have clicked the user_name_layout");
                break;
            case R.id.user_password_layout:
                Log.i(TAG, "You have clicked the user_password_layout");
                break;
            case R.id.user_qrcode_layout:
                Log.i(TAG, "You have clicked the user_qrcode_layout");
                startActivity(new Intent(PersonalInformationActivity.this, QRcodeCardActivity.class));
                break;
            case R.id.user_more_layout:
                Log.i(TAG, "You have clicked the user_more_layout");
                break;
        }
    }
}
