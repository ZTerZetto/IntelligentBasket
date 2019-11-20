package com.automation.zzx.intelligent_basket_demo.activity.areaAdmin;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.widget.TextView;
import android.support.v7.widget.Toolbar;

import com.automation.zzx.intelligent_basket_demo.R;
import com.automation.zzx.intelligent_basket_demo.adapter.rentAdmin.MgBasketContentFragmentAdapter;
import com.automation.zzx.intelligent_basket_demo.application.CustomApplication;
import com.automation.zzx.intelligent_basket_demo.entity.UserInfo;
import com.automation.zzx.intelligent_basket_demo.fragment.areaAdmin.AreaAdminProListFragment;
import com.automation.zzx.intelligent_basket_demo.fragment.areaAdmin.AreaAdminProMapFragment;
import com.automation.zzx.intelligent_basket_demo.widget.NoScrollViewPager;


import java.util.ArrayList;
import java.util.List;

public class AreaAdminProListActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private TabLayout mTabLayout; // 顶部导航栏
    private TextView toolbarTitle;
    private NoScrollViewPager mViewPager; // 页面布局

    // 用户登录信息相关
    private String projectType;
    private UserInfo mUserInfo;
    private String mToken;
    private SharedPreferences mPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_area_admin_pro_list);

        getUserInfo();
        getIntentInfo();

        // 绑定控件
        mToolbar = findViewById(R.id.toolbar);
        toolbarTitle = findViewById(R.id.toolbar_title);
        mToolbar.setTitle("");
        switch (projectType){
            case "operatingProjectList":
                toolbarTitle.setText("运营中项目");
                break;
            case "installingProjectList":
                toolbarTitle.setText("安装中项目");
                break;
            case "endProjectList":
                toolbarTitle.setText("已结束项目");
                break;

        }
        setSupportActionBar(mToolbar);
        getSupportActionBar().setHomeButtonEnabled(true); //设置返回键可用


        mTabLayout = (TabLayout) findViewById(R.id.head_tab_layout);
        mViewPager = (NoScrollViewPager) findViewById(R.id.view_pager);

        List<Fragment> fragmentList = new ArrayList<>();   // 添加fragment
        fragmentList.add(new AreaAdminProMapFragment());
        fragmentList.add(new AreaAdminProListFragment());

        List<String> titleList = new ArrayList<>();  // 添加fragment说明
        titleList.add("地图");
        titleList.add("列表");

        MgBasketContentFragmentAdapter mgBasketContentFragmentAdapter =
                new MgBasketContentFragmentAdapter(getSupportFragmentManager(), fragmentList, titleList);
        mViewPager.setAdapter(mgBasketContentFragmentAdapter);

        mTabLayout.setupWithViewPager(mViewPager);
    }

    /*
     * 解析信息
     */
    //获取传递数据
    private void getIntentInfo(){
        Intent intent = getIntent();
        projectType=intent.getStringExtra("project_type");
        if(projectType == null || projectType.equals(""))  projectType = "operatingProjectList";
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
    // 将项目类型传递给子fragment
    public String pushType(){
        return projectType;
    }


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


    @Override
    protected void onDestroy(){
        super.onDestroy();
        CustomApplication.setMainActivity(null);
    }

    // 退出但不销毁
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            moveTaskToBack(false);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

}
