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
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.automation.zzx.intelligent_basket_demo.R;
import com.automation.zzx.intelligent_basket_demo.activity.areaAdmin.AreaAdminPrimaryActivity;
import com.automation.zzx.intelligent_basket_demo.activity.loginRegist.LoginActivity;
import com.automation.zzx.intelligent_basket_demo.entity.UserInfo;

/**
 * Created by pengchenghu on 2019/3/27.
 * Author Email: 15651851181@163.com
 * Describe:租方管理员用户页面
 */
public class AreaAdminFragment extends Fragment implements View.OnClickListener {

    private final static String TAG = "AreaAdminFragment";

    private RelativeLayout mLogout; // 退出登录

    // 用户信息
    private UserInfo userInfo;
    private String token;
    private SharedPreferences mPref;

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {

            }
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_area_admin, container, false);

        mLogout = (RelativeLayout) view.findViewById(R.id.log_out_layout);
        mLogout.setOnClickListener(this);

        return view;
    }

    /*
     * 点击响应
     */
    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.log_out_layout:
                Log.i(TAG, "You have clicked in regard to button");
                logoutHttp();
                break;
        }
    }

    /*
     * 登录相关
     */
    protected void onAttachToContext(Context context) {
        //do something
        userInfo = ((AreaAdminPrimaryActivity) context).pushUserInfo();
        token = ((AreaAdminPrimaryActivity) context).pushToken();
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

}
