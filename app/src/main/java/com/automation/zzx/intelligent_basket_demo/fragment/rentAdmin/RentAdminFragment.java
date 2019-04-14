package com.automation.zzx.intelligent_basket_demo.fragment.rentAdmin;

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
import com.automation.zzx.intelligent_basket_demo.activity.areaAdmin.ProDetailActivity;
import com.automation.zzx.intelligent_basket_demo.activity.common.PersonalInformationActivity;
import com.automation.zzx.intelligent_basket_demo.activity.loginRegist.LoginActivity;
import com.automation.zzx.intelligent_basket_demo.activity.rentAdmin.RentAdminPrimaryActivity;
import com.automation.zzx.intelligent_basket_demo.entity.AppConfig;
import com.automation.zzx.intelligent_basket_demo.entity.UserInfo;
import com.automation.zzx.intelligent_basket_demo.utils.okhttp.BaseCallBack;
import com.automation.zzx.intelligent_basket_demo.utils.okhttp.BaseOkHttpClient;
import com.automation.zzx.intelligent_basket_demo.widget.image.SmartImageView;

import java.io.IOException;

import okhttp3.Call;

import static com.automation.zzx.intelligent_basket_demo.entity.AppConfig.FILE_SERVER_HPC_PATH;

/**
 * Created by pengchenghu on 2019/3/22.
 * Author Email: 15651851181@163.com
 * Describe: 租方管理员用户页面
 */
public class RentAdminFragment extends Fragment implements View.OnClickListener {

    private final static String TAG = "RentAdminFragment";

    // Handler消息
    private final static int UPDATE_USER_DISPLAY_MSG = 101;

    // header
    private RelativeLayout mWorkerLoginLayout; // 登录总布局
    private SmartImageView mWorkerHead; // 头像
    private TextView mWorkerName;  // 名字
    private TextView mWorkerProjectState;  // 项目

    // 功能区
    private RelativeLayout mProjectDetail; // 项目详情

    // 其它功能
    private RelativeLayout mGiveHighPrice; // 给个好评
    private RelativeLayout mFeedbackComment; // 反馈意见
    private RelativeLayout mContactService; // 联系客服
    private RelativeLayout mCheckUpdate; // 检查更新
    private RelativeLayout mInRegardTo; //关于

    // 退出登录
    private RelativeLayout mLogout; // 退出登录

    // 页面信息
    private String mWorkProjectId;  // 项目ID
    private String mWorkProjectName; // 项目名称
    private String mUserHeadUrl = FILE_SERVER_HPC_PATH + "/head/default_user_head.png";

    // 用户信息
    private UserInfo mUserInfo;
    private String mToken;
    private SharedPreferences mPref;

    // 项目信息
    private String mProjectId;

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case UPDATE_USER_DISPLAY_MSG:
                    updateUserDisplayView();
                    break;
            }
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getRentAdminInfoFromInternet();  // 从网络获取其余信息

        View view = inflater.inflate(R.layout.fragment_rent_admin, container, false);
        // header
        mWorkerLoginLayout = (RelativeLayout) view.findViewById(R.id.login_layout);
        mWorkerLoginLayout.setOnClickListener(this);
        mWorkerHead = (SmartImageView) view.findViewById(R.id.login_head); // 头像
        mWorkerHead.setCircle(true);
        //mWorkerHead.setImageUrl(mUserHeadUrl);
        mWorkerName = (TextView) view.findViewById(R.id.login_username);  // 用户名
        mWorkerProjectState = (TextView) view.findViewById(R.id.worker_project_state); // 项目状态

        // function
        mProjectDetail = (RelativeLayout) view.findViewById(R.id.project_detail);
        mProjectDetail.setOnClickListener(this);

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
            case R.id.project_detail:
                intent = new Intent(getActivity(), ProDetailActivity.class);
                intent.putExtra("projectId", mProjectId);
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
                .url(AppConfig.RENT_ADMIN_ALL_INFO)
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
        mWorkProjectId = jsonObject.getString("inProject");
        mWorkProjectName = jsonObject.getString("projectName");
        mHandler.sendEmptyMessage(UPDATE_USER_DISPLAY_MSG);  // 更新人员信息状态
    }

    /*
     * 登录相关
     */
    // 从本地获取用户信息
    protected void onAttachToContext(Context context) {
        //do something
        mUserInfo = ((RentAdminPrimaryActivity) context).pushUserInfo();
        mProjectId = ((RentAdminPrimaryActivity) context).pushProjectId();
        mToken = ((RentAdminPrimaryActivity) context).pushToken();
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
        editor.clear();
        editor.commit();
        startActivity(new Intent(getActivity(), LoginActivity.class));
        getActivity().finish();
    }

    /*
     * UI 更新相关
     */
    // 更新页面个人信息显示
    private void updateUserDisplayView(){
        mWorkerHead.setImageUrl(mUserHeadUrl); // 头像
        mWorkerName.setText(mUserInfo.getUserName()); // 用户名
        if(mWorkProjectId == null || mWorkProjectId.equals("")) // 项目状态
            mWorkerProjectState.setText(R.string.worker_no_project);
        else {
            if (mWorkProjectName == null || mWorkProjectName.equals(""))
                mWorkerProjectState.setText(mWorkProjectId);
            else
                mWorkerProjectState.setText(mWorkProjectName);
        }
    }
}
