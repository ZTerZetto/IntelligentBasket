package com.automation.zzx.intelligent_basket_demo.fragment.areaAdmin;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.automation.zzx.intelligent_basket_demo.R;
import com.automation.zzx.intelligent_basket_demo.activity.areaAdmin.AreaAdminPrimaryOldActivity;
import com.automation.zzx.intelligent_basket_demo.activity.common.PersonalInformationActivity;
import com.automation.zzx.intelligent_basket_demo.activity.loginRegist.LoginActivity;
import com.automation.zzx.intelligent_basket_demo.entity.AppConfig;
import com.automation.zzx.intelligent_basket_demo.entity.UserInfo;
import com.automation.zzx.intelligent_basket_demo.utils.okhttp.BaseCallBack;
import com.automation.zzx.intelligent_basket_demo.utils.okhttp.BaseOkHttpClient;
import com.automation.zzx.intelligent_basket_demo.utils.xiaomi.mipush.MiPushUtil;
import com.automation.zzx.intelligent_basket_demo.widget.image.SmartImageView;

import java.io.IOException;

import okhttp3.Call;


/**
 * Created by pengchenghu on 2019/3/27.
 * Author Email: 15651851181@163.com
 * Describe:租方管理员用户页面
 */
public class AreaAdminFragment extends Fragment implements View.OnClickListener {

    private final static String TAG = "AreaAdminFragment";

    // Handler消息
    private final static int UPDATE_USER_DISPLAY_MSG = 101;

    // header
    private RelativeLayout mWorkerLoginLayout; // 登录总布局
    private SmartImageView mWorkerHead; // 头像
    private TextView mWorkerName;  // 名字
    private TextView mWorkerProjectState;  // 项目

    // 其它功能
    private RelativeLayout mGiveHighPrice; // 给个好评
    private RelativeLayout mFeedbackComment; // 反馈意见
    private RelativeLayout mContactService; // 联系客服
    private RelativeLayout mCheckUpdate; // 检查更新
    private RelativeLayout mInRegardTo; //关于

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
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getRentAdminInfoFromInternet();

        View view = inflater.inflate(R.layout.fragment_area_admin, container, false);

        // header
        mWorkerLoginLayout = (RelativeLayout) view.findViewById(R.id.login_layout);
        mWorkerLoginLayout.setOnClickListener(this);
        mWorkerHead = (SmartImageView) view.findViewById(R.id.login_head); // 头像
        mWorkerHead.setCircle(true);
        //mWorkerHead.setImageUrl(mUserHeadUrl);
        mWorkerName = (TextView) view.findViewById(R.id.login_username);  // 用户名
        mWorkerProjectState = (TextView) view.findViewById(R.id.worker_project_state); // 项目状态

        // other function
        mGiveHighPrice = (RelativeLayout) view.findViewById(R.id.more_item_comment_layout); // 给个好评
        mGiveHighPrice.setOnClickListener(this);
        mFeedbackComment = (RelativeLayout) view.findViewById(R.id.more_item_feedback_layout); // 反馈意见
        mFeedbackComment.setOnClickListener(this);
        mContactService = (RelativeLayout) view.findViewById(R.id.more_item_contact_kefu_layout); // 联系客服
        mContactService.setOnClickListener(this);
        mCheckUpdate = (RelativeLayout) view.findViewById(R.id.more_item_check_update_layout); // 检查更新
        mCheckUpdate.setOnClickListener(this);
        mInRegardTo = (RelativeLayout) view.findViewById(R.id.more_item_about_layout); // 关于
        mInRegardTo.setOnClickListener(this);

        // logout
        mLogout = (RelativeLayout) view.findViewById(R.id.more_item_log_out_layout); // 退出登录
        mLogout.setOnClickListener(this);

        return view;
    }

    /*
     * 点击响应
     */
    @Override
    public void onClick(View v) {
        Intent intent;
        switch(v.getId()){
            case R.id.login_layout:  // 跳转至个人信息页面
                Log.i(TAG, "You have clicked login layout");
                intent = new Intent(getActivity(), PersonalInformationActivity.class);
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
    private void getRentAdminInfoFromInternet(){
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

    /*
     * 登录相关
     */
    protected void onAttachToContext(Context context) {
        //do something
        mUserInfo = ((AreaAdminPrimaryOldActivity) context).pushUserInfo();
        mToken = ((AreaAdminPrimaryOldActivity) context).pushToken();
    }
    @TargetApi(23)
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        onAttachToContext(context);
    }
    @SuppressWarnings("deprecation")
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            onAttachToContext(activity);
        }
    }
    //退出登录
    private void logoutHttp() {
        // 清空本地账号
        mPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        SharedPreferences.Editor editor = mPref.edit();
        editor.remove("loginToken");
        editor.commit();

        // 200911: 退出登录，去除别名设置
        MiPushUtil.clearAlias(getActivity(), mUserInfo.getUserId());

        startActivity(new Intent(getActivity(), LoginActivity.class));
        getActivity().finish();
    }

}
