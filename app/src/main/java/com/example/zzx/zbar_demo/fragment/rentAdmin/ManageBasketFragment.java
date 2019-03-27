package com.example.zzx.zbar_demo.fragment.rentAdmin;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.zzx.zbar_demo.R;
import com.example.zzx.zbar_demo.activity.loginRegist.LoginActivity;
import com.example.zzx.zbar_demo.activity.rentAdmin.RentAdminPrimaryActivity;
import com.example.zzx.zbar_demo.adapter.rentAdmin.MgBasketContentFragmentAdapter;
import com.example.zzx.zbar_demo.entity.UserInfo;
import com.example.zzx.zbar_demo.utils.HttpUtil;
import com.example.zzx.zbar_demo.utils.ToastUtil;
import com.example.zzx.zbar_demo.widget.NoScrollViewPager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import static com.example.zzx.zbar_demo.entity.AppConfig.RENT_ADMIN_MG_ALL_BASKET_INFO;

/**
 * Created by pengchenghu on 2019/3/22.
 * Author Email: 15651851181@163.com
 * Describe:租方管理员吊篮管理
 */
public class ManageBasketFragment extends Fragment {

    private TabLayout mTabLayout; // 顶部导航栏
    private NoScrollViewPager mViewPager; // 页面布局


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_rent_mg_basket, container, false);

        // 绑定控件
        mTabLayout = (TabLayout) view.findViewById(R.id.head_tab_layout);
        mViewPager = (NoScrollViewPager) view.findViewById(R.id.view_pager);

        List<Fragment> fragmentList = new ArrayList<>();
        fragmentList.add(new MgBasketListFragment());
        fragmentList.add(new MgBasketMapFragment());

        List<String> titleList = new ArrayList<>();
        titleList.add("列表");
        titleList.add("地图");

        MgBasketContentFragmentAdapter mgBasketContentFragmentAdapter =
                new MgBasketContentFragmentAdapter(getFragmentManager(), fragmentList, titleList);
        mViewPager.setAdapter(mgBasketContentFragmentAdapter);

        mTabLayout.setupWithViewPager(mViewPager);

        return view;
    }

}
