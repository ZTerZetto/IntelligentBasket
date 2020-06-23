package com.automation.zzx.intelligent_basket_demo.activity.proAdmin;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.automation.zzx.intelligent_basket_demo.R;
import com.automation.zzx.intelligent_basket_demo.activity.common.PersonalInformationActivity;
import com.automation.zzx.intelligent_basket_demo.activity.loginRegist.LoginActivity;
import com.automation.zzx.intelligent_basket_demo.activity.worker.WorkerPrimaryActivity;
import com.automation.zzx.intelligent_basket_demo.entity.AppConfig;
import com.automation.zzx.intelligent_basket_demo.entity.UserInfo;
import com.automation.zzx.intelligent_basket_demo.utils.okhttp.BaseCallBack;
import com.automation.zzx.intelligent_basket_demo.utils.okhttp.BaseOkHttpClient;
import com.automation.zzx.intelligent_basket_demo.widget.image.SmartImageView;

import java.io.IOException;

import okhttp3.Call;

public class ProAdminSetActivity extends AppCompatActivity implements View.OnClickListener {

    private final static String TAG = "ProAdminSetActivity";

    // Handler消息
    private final static int UPDATE_USER_DISPLAY_MSG = 101;

    // header
    private RelativeLayout mWorkerLoginLayout; // 登录总布局
    private SmartImageView mWorkerHead; // 头像
    private TextView mWorkerName;  // 名字
    private TextView mWorkerProjectState;  // 项目
    private TextView tvRole;//角色名

    // 其它功能
    private RelativeLayout mGiveHighPrice; // 给个好评
    private RelativeLayout mFeedbackComment; // 反馈意见
    private RelativeLayout mContactService; // 联系客服
    private RelativeLayout mCheckUpdate; // 检查更新
    private RelativeLayout mInRegardTo; //关于

    //切换至施工人员
    private LinearLayout llSwitch;
    private RelativeLayout rlSwitch;

    // 退出登录
    private RelativeLayout mLogout; // 退出登录

    // 页面信息
    private String mUserHeadUrl = AppConfig.FILE_SERVER_YBLIU_PATH + "/head/default_user_head.png";

    // 用户信息
    private UserInfo mUserInfo;
    private String mToken;
    private SharedPreferences mPref;

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case UPDATE_USER_DISPLAY_MSG:
                    mWorkerHead.setImageUrl(mUserHeadUrl); // 头像
                    mWorkerName.setText(mUserInfo.getUserName());
                    break;
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getUserInfo();
        setContentView(R.layout.fragment_area_admin);
        initWidget();
    }

    /*
     * 解析用户信息
     */
    // 获取用户数据
    private void getUserInfo() {
        // 从本地获取数据
        mPref = PreferenceManager.getDefaultSharedPreferences(this);
        mUserInfo = new UserInfo();
        mUserInfo.setUserId(mPref.getString("userId", ""));
        mUserInfo.setUserPhone(mPref.getString("userPhone", ""));
        mUserInfo.setUserRole(mPref.getString("userRole", ""));
        mUserInfo.setUserName(mPref.getString("userName", ""));
        mToken = mPref.getString("loginToken", "");
        getRentAdminInfoFromInternet();

    }
    /*
     * 初始化页面
     * */
    //初始化句柄
    private void initWidget() {
        // 顶部导航栏
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        TextView titleText = (TextView) findViewById(R.id.toolbar_title);
        toolbar.setTitle("我的");
        titleText.setText("");
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true); //设置返回键可用

        // header
        mWorkerLoginLayout = (RelativeLayout) findViewById(R.id.login_layout);
        mWorkerLoginLayout.setOnClickListener(this);
        mWorkerHead = (SmartImageView) findViewById(R.id.login_head); // 头像
        mWorkerHead.setCircle(true);
        //mWorkerHead.setImageUrl(mUserHeadUrl);
        mWorkerName = (TextView) findViewById(R.id.login_username);  // 用户名
        tvRole = (TextView) findViewById(R.id.tv_role_name);//角色名
        tvRole.setText("现场管理员");
        mWorkerProjectState = (TextView) findViewById(R.id.worker_project_state); // 项目状态

        // other function
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

        //切换角色
        llSwitch = (LinearLayout) findViewById(R.id.switch_layout); //切换角色
        llSwitch.setVisibility(View.VISIBLE);
        rlSwitch =  (RelativeLayout) findViewById(R.id.more_item_switch_layout); //切换角色
        rlSwitch.setOnClickListener(this);

        // logout
        mLogout = (RelativeLayout) findViewById(R.id.more_item_log_out_layout); // 退出登录
        mLogout.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        Intent intent;
        switch (v.getId()) {
            case R.id.login_layout:  // 跳转至个人信息页面
                Log.i(TAG, "You have clicked login layout");
                intent = new Intent(ProAdminSetActivity.this, PersonalInformationActivity.class);
                intent.putExtra("userInfo", (Parcelable) mUserInfo);
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
            case R.id.more_item_switch_layout: //切换角色
                Log.i(TAG, "You have clicked in regard to button");
                switchToWorker();
                break;
            case R.id.more_item_log_out_layout:  // 退出登录
                Log.i(TAG, "You have clicked in regard to button");
                logoutHttp();
                break;
        }
    }

    /*
     * 网络相关
     */
    // 从网络获取用户信息
    private void getRentAdminInfoFromInternet() {
        BaseOkHttpClient.newBuilder()
                .addHeader("Authorization", mToken)
                .addParam("userId", mUserInfo.getUserId())
                .post()
                .url(AppConfig.PRO_ADMIN_ALL_INFO)
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
    private void parseUserInfoFromInternet(String data) {
        Log.d(TAG, "parse data:" + data);
        JSONObject jsonObject = JSON.parseObject(data);
        String userInfo = jsonObject.getString("userInfo");
        mUserInfo = JSON.parseObject(userInfo, UserInfo.class);
        mHandler.sendEmptyMessage(UPDATE_USER_DISPLAY_MSG);  // 更新人员信息状态
    }

    //切换角色
    private void switchToWorker() {
        startActivity(new Intent(ProAdminSetActivity.this, WorkerPrimaryActivity.class));
        this.finish();
    }

    //退出登录
    private void logoutHttp() {
        // 清空本地账号
        mPref = PreferenceManager.getDefaultSharedPreferences(ProAdminSetActivity.this);
        SharedPreferences.Editor editor = mPref.edit();
        editor.remove("loginToken");
        editor.commit();
        startActivity(new Intent(ProAdminSetActivity.this, LoginActivity.class));
        ProAdminSetActivity.this.finish();
    }


    /*
     * 重构函数
     */
    // 顶部导航栏消息响应
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: // 返回按钮
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
