package com.automation.zzx.intelligent_basket_demo.activity.basketSupervisor;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;

import com.automation.zzx.intelligent_basket_demo.R;
import com.automation.zzx.intelligent_basket_demo.adapter.rentAdmin.MgBasketContentFragmentAdapter;
import com.automation.zzx.intelligent_basket_demo.entity.UserInfo;
import com.automation.zzx.intelligent_basket_demo.fragment.basket.SvBasketListFragment;
import com.automation.zzx.intelligent_basket_demo.fragment.rentAdmin.MgBasketMapFragment;
import com.automation.zzx.intelligent_basket_demo.widget.NoScrollViewPager;

import java.util.ArrayList;
import java.util.List;

public class SupervisorBasketActivity extends AppCompatActivity {

    // 用户登录信息相关
    private UserInfo mUserInfo;
    private String mToken;
    private String mProjectId;
    private SharedPreferences mPref;

    // 屏幕素质
    private int mScreenWidth;
    private int mScreenHeight;

    private TabLayout mTabLayout; // 顶部导航栏
    private NoScrollViewPager mViewPager; // 页面布局

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_basket_supervisor);

        getUserInfo();
        initWidget();

    }

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

    private void initWidget(){
        // 绑定控件
        mTabLayout = (TabLayout) findViewById(R.id.head_tab_layout);
        mViewPager = (NoScrollViewPager) findViewById(R.id.view_pager);

        getScreenSize();

        List<Fragment> fragmentList = new ArrayList<>();   // 添加fragment
        fragmentList.add(new SvBasketListFragment());
        fragmentList.add(new MgBasketMapFragment());

        List<String> titleList = new ArrayList<>();  // 添加fragment说明
        titleList.add("列表");
        titleList.add("地图");

        MgBasketContentFragmentAdapter mgBasketContentFragmentAdapter =
                new MgBasketContentFragmentAdapter(getSupportFragmentManager(), fragmentList, titleList);
        mViewPager.setAdapter(mgBasketContentFragmentAdapter);

        mTabLayout.setupWithViewPager(mViewPager);
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
