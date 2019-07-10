package com.automation.zzx.intelligent_basket_demo.activity.basketSupervisor;


import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;

import android.preference.PreferenceManager;
import com.automation.zzx.intelligent_basket_demo.activity.common.PersonalInformationActivity;
import com.automation.zzx.intelligent_basket_demo.activity.common.UserMessageActivity;
import com.automation.zzx.intelligent_basket_demo.activity.inspectionPerson.ExceptionReportActivity;
import com.automation.zzx.intelligent_basket_demo.activity.loginRegist.LoginActivity;
import com.automation.zzx.intelligent_basket_demo.utils.okhttp.BaseCallBack;
import com.automation.zzx.intelligent_basket_demo.utils.okhttp.BaseOkHttpClient;
import com.automation.zzx.intelligent_basket_demo.widget.image.SmartImageView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.automation.zzx.intelligent_basket_demo.R;
import com.automation.zzx.intelligent_basket_demo.entity.AppConfig;
import com.automation.zzx.intelligent_basket_demo.entity.UserInfo;
import com.automation.zzx.intelligent_basket_demo.utils.xiaomi.mipush.MiPushUtil;

import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.IOException;

import okhttp3.Call;


/**
 * Created by zzx on 2019/7/7.
 * Describe:吊篮监管人员
 */

public class SupervisorPrimaryActivity extends AppCompatActivity implements View.OnClickListener {


    private final static String TAG = "InspectPersonPrimary";

    // 消息处理Flag
    private final static int UPDATE_USER_DISPLAY_MSG = 101;

    // header
    private RelativeLayout mUserLoginLayout; // 登录总布局
    private SmartImageView mUserHead; // 头像
    private TextView mUserName;  // 名字


    // function choose
    private LinearLayout mBasketLayout; // 吊篮监管
    private LinearLayout mExceptionReportLayout; // 异常信息上报
    private LinearLayout mMessageLayout; // 消息

    // 其它功能
    private RelativeLayout mGiveHighPrice; // 给个好评
    private RelativeLayout mFeedbackComment; // 反馈意见
    private RelativeLayout mContactService; // 联系客服
    private RelativeLayout mCheckUpdate; // 检查更新
    private RelativeLayout mInRegardTo; //关于

    // 退出登录
    private RelativeLayout mLogout; // 退出登录

    // 用户信息
    private UserInfo mUserInfo;
    private String mUserHeadUrl = AppConfig.FILE_SERVER_YBLIU_PATH + "/head/default_user_head.png";
    private String mToken;
    private SharedPreferences mPref;
    private String mProjectId;

    // mHandler 处理消息
    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch(msg.what){
                case UPDATE_USER_DISPLAY_MSG:
                    mUserHead.setImageUrl(mUserHeadUrl); // 头像
                    mUserName.setText(mUserInfo.getUserName());
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_supervisor_primary);

        getUserInfo();
        initWidget();  // 初始化控件

        MiPushUtil.initMiPush(SupervisorPrimaryActivity.this, mUserInfo.getUserId(), "ConfigurationList");

    }

    /*
     * 页面初始化
     */
    // 初始化控件
    private void initWidget(){
        // header
        mUserLoginLayout = (RelativeLayout) findViewById(R.id.login_layout);
        mUserLoginLayout.setOnClickListener(this);
        mUserHead = (SmartImageView) findViewById(R.id.login_head);
        mUserHead.setCircle(true);
        mUserName = (TextView) findViewById(R.id.login_username);

        // function choose
        mBasketLayout  = (LinearLayout) findViewById(R.id.basket_layout);
        mBasketLayout.setOnClickListener(this);
        mMessageLayout = (LinearLayout) findViewById(R.id.message_layout);
        mMessageLayout.setOnClickListener(this);

        mGiveHighPrice = (RelativeLayout) findViewById(R.id.more_item_comment_layout); // 给个好评
        mGiveHighPrice.setOnClickListener(this);
        mFeedbackComment = (RelativeLayout) findViewById(R.id.more_item_feedback_layout); // 反馈意见
        mFeedbackComment.setOnClickListener(this);
        mContactService = (RelativeLayout) findViewById(R.id.more_item_contact_kefu_layout); // 联系客服
        mContactService.setOnClickListener(this);
        mCheckUpdate = (RelativeLayout) findViewById(R.id.more_item_check_update_layout); // 检查更新
        mCheckUpdate.setOnClickListener(this);
        mInRegardTo = (RelativeLayout) findViewById(R.id.more_item_about_layout); // 关于
        mInRegardTo.setOnClickListener(this);

        // logout
        mLogout = (RelativeLayout) findViewById(R.id.more_item_log_out_layout); // 退出登录
        mLogout.setOnClickListener(this);
    }

    /*
     * 点击事件消息响应
     */
    @Override
    public void onClick(View v) {
        Intent intent;
        switch (v.getId()){
            case R.id.login_layout:  // 跳转至项目信息页面
                Log.i(TAG, "You have clicked login layout");
                //点击进入项目详情界面
                break;
            case R.id.basket_layout:  // 吊篮页面
                Log.i(TAG, "You have clicked open/close work button");
                intent = new Intent(SupervisorPrimaryActivity.this, SupervisorBasketActivity.class);
                //intent.putExtra(SearchProjectActivity.OPERATE_TYPE, 0);
                startActivity(intent);
                break;
            case R.id.message_layout:  // 消息
                Log.i(TAG, "You have clicked message button");
                intent = new Intent(SupervisorPrimaryActivity.this, UserMessageActivity.class);
                intent.putExtra("user_type", "basket_supervisor"); // 用户类型
                startActivity(intent);
                break;
            case R.id.more_item_comment_layout:  // 给个好评
                Log.i(TAG, "You have clicked high price button");
                break;
            case R.id.more_item_feedback_layout:  // 反馈意见
                Log.i(TAG, "You have clicked feedback button");
                break;
            case R.id.more_item_contact_kefu_layout: // 联系客服
                Log.i(TAG, "You have clicked contact service button");
                break;
            case R.id.more_item_check_update_layout: // 检查更新
                Log.i(TAG, "You have clicked check update button");
                break;
            case R.id.more_item_about_layout: // 关于
                Log.i(TAG, "You have clicked in regard to button");
                break;
            case R.id.more_item_log_out_layout:
                Log.i(TAG, "You have clicked log out button");
                logoutHttp();
                break;
        }
    }

    /*
     * 解析用户信息
     */
    // 获取用户数据
    private void getUserInfo(){
        // 从本地获取数据
        mPref = PreferenceManager.getDefaultSharedPreferences(this);
        mUserInfo = new UserInfo();
        mUserInfo.setUserId(mPref.getString("userId", ""));
        mUserInfo.setUserPhone(mPref.getString("userPhone", ""));
        mUserInfo.setUserRole(mPref.getString("userRole", ""));
        mToken = mPref.getString("loginToken","");

        // 从后台获取其他数据
        getUserInfoFromInternet();
    }

    // 将用户信息传递给子Fragment
    public UserInfo pushUserInfo(){
        return mUserInfo;
    }
    // 将用户token传递给子Fragment
    public String pushToken(){
        return mToken;
    }
    // 获取projectID
    public void getProjectId(String projectId){
        mProjectId = projectId;
    }
    // 将ProjectId传递给子Fragment
    public String pushProjectId(){
        return mProjectId;
    }


    // 从网络获取用户信息
    private void getUserInfoFromInternet(){
        BaseOkHttpClient.newBuilder()
                .addHeader("Authorization", mToken)
                .addParam("userId", mUserInfo.getUserId())
                .post()
                .url(AppConfig.AREA_ADMIN_ALL_INFO)
                .build()
                .enqueue(new BaseCallBack() {
                    @Override
                    public void onSuccess(Object o) {
                        String data = o.toString();
                        parseUserInfoFromInternet(data);
                    }

                    @Override
                    public void onError(int code) {
                        Log.i(TAG, "Error:" + code);
                    }

                    @Override
                    public void onFailure(Call call, IOException e) {
                        Log.i(TAG, "Failure:" + e.toString());
                    }
                });
    }
    // 解析后台返回数据
    private void parseUserInfoFromInternet(String data){
        Log.d(TAG, "parse data:" + data);
        JSONObject jsonObject = JSON.parseObject(data);
        String userInfo = jsonObject.getString("userInfo");
        mUserInfo = JSON.parseObject(userInfo, UserInfo.class);
        mHandler.sendEmptyMessage(UPDATE_USER_DISPLAY_MSG);  // 更新人员信息状态
    }

    //退出登录
    private void logoutHttp() {
        SharedPreferences.Editor editor = mPref.edit();
        editor.clear();
        editor.commit();
        startActivity(new Intent(SupervisorPrimaryActivity.this, LoginActivity.class));
        this.finish();  // 销毁此活动
    }

}
