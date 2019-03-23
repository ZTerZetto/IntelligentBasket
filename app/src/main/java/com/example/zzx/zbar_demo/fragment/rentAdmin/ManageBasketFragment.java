package com.example.zzx.zbar_demo.fragment.rentAdmin;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.zzx.zbar_demo.R;
import com.example.zzx.zbar_demo.adapter.rentAdmin.MgBasketContentFragmentAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by pengchenghu on 2019/3/22.
 * Author Email: 15651851181@163.com
 * Describe:
 */
public class ManageBasketFragment extends Fragment {

    private TabLayout mTabLayout; // 顶部导航栏
    private ViewPager mViewPager; // 页面布局



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_rent_mg_basket, container, false);

        // 绑定控件
        mTabLayout = (TabLayout) view.findViewById(R.id.head_tab_layout);
        mViewPager = (ViewPager) view.findViewById(R.id.view_pager);

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
