package com.automation.zzx.intelligent_basket_demo.fragment.rentAdmin;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.automation.zzx.intelligent_basket_demo.R;
import com.automation.zzx.intelligent_basket_demo.adapter.rentAdmin.MgBasketContentFragmentAdapter;
import com.automation.zzx.intelligent_basket_demo.widget.NoScrollViewPager;

import java.util.ArrayList;
import java.util.List;

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

        List<Fragment> fragmentList = new ArrayList<>();   // 添加fragment
        //fragmentList.add(new MgBasketMapFragment());
        fragmentList.add(new MgBasketListFragment());

        List<String> titleList = new ArrayList<>();  // 添加fragment说明
        //titleList.add("地图");
        titleList.add("吊篮列表");

        MgBasketContentFragmentAdapter mgBasketContentFragmentAdapter =
                new MgBasketContentFragmentAdapter(getFragmentManager(), fragmentList, titleList);
        mViewPager.setAdapter(mgBasketContentFragmentAdapter);

        mTabLayout.setupWithViewPager(mViewPager);

        return view;
    }

}
