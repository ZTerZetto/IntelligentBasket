package com.automation.zzx.intelligent_basket_demo.activity.rentAdmin;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;

import com.automation.zzx.intelligent_basket_demo.R;
import com.automation.zzx.intelligent_basket_demo.entity.UserInfo;
import com.automation.zzx.intelligent_basket_demo.fragment.rentAdmin.ManageBasketFragment;
import com.automation.zzx.intelligent_basket_demo.fragment.rentAdmin.ManageWorkerFragment;
import com.automation.zzx.intelligent_basket_demo.fragment.rentAdmin.RentAdminFragment;
import com.automation.zzx.intelligent_basket_demo.fragment.rentAdmin.RentAdminMessageFragment;
import com.hjm.bottomtabbar.BottomTabBar;


public class RentAdminPrimaryActivity extends AppCompatActivity {

    private final static String TAG = "RentAdminPrimary";

    // 控件
    private BottomTabBar mBottomTabBar;

    // 屏幕素质
    private int mScreenWidth;
    private int mScreenHeight;

    // 用户登录信息相关
    private UserInfo mUserInfo;
    private String mToken;
    private String mProjectId;
    private SharedPreferences mPref;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rent_admin_primary);

        getUserInfo();
        initWidget();
    }

    private void initWidget(){

        mBottomTabBar = (BottomTabBar) findViewById(R.id.bottom_tab_bar);

        getScreenSize();
        mBottomTabBar.init(getSupportFragmentManager(), mScreenWidth, mScreenHeight)
                .setImgSize(90, 90)
                .setFontSize(30)
                //.setTabPadding(5,0,5)
                .setChangeColor(Color.parseColor("#009688"),Color.parseColor("#cccccc"))
                .addTabItem("吊篮", R.mipmap.ic_navi_basket, ManageBasketFragment.class)
                .addTabItem("工人", R.mipmap.ic_navi_worker, ManageWorkerFragment.class)
                .addTabItem("消息", R.mipmap.ic_navi_message, RentAdminMessageFragment.class)
                .addTabItem("我", R.mipmap.ic_navi_me,RentAdminFragment.class)
                .isShowDivider(false)
                //.setDividerColor(Color.parseColor("#FF0000"))
                //.setTabBarBackgroundColor(Color.parseColor("#00FF0000"))
                .setOnTabChangeListener(new BottomTabBar.OnTabChangeListener() {
                    @Override
                    public void onTabChange(int position, String name, View view) {
                        if(position == 2)
                            mBottomTabBar.setSpot(2, false);
                    }
                })
                .setSpot(2, true);
    }



    /*
     * 获取用户信息
     */
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

    /*
     * 获取屏幕素质
     */
    // 获取屏幕的宽高度
    private void getScreenSize(){
        DisplayMetrics dm2 = getResources().getDisplayMetrics();
        mScreenHeight = dm2.heightPixels;
        mScreenWidth = dm2.widthPixels;
    }

}
