package com.example.zzx.zbar_demo.adapter.rentAdmin;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by pengchenghu on 2019/3/22.
 * Author Email: 15651851181@163.com
 * Describe: 租方管理员管理吊篮页面适配器
 */
public class MgBasketContentFragmentAdapter extends FragmentPagerAdapter {

    private List<Fragment> fragments;
    private List<String> names;

    /*
     * 构造函数
     */
    public MgBasketContentFragmentAdapter(FragmentManager fm) {
        super(fm);
        this.fragments = new ArrayList<>();
        this.names = new ArrayList<>();
    }

    public MgBasketContentFragmentAdapter(FragmentManager fm, List<Fragment> fragments,
                                          List<String> names) {
        super(fm);
        this.fragments = fragments;
        this.names = names;
    }

    /*
     * Bean 函数组
     */
    public void setFragments(List<Fragment> fragments) {
        this.fragments.clear();
        this.fragments.addAll(fragments);
        notifyDataSetChanged();
    }
    public void setNames(List<String> names) {
        this.names.clear();
        this.names.addAll(names);
        notifyDataSetChanged();
    }

    @Override
    public Fragment getItem(int position) {
        Fragment fragment = null;
        if(position < fragments.size()){
            fragment = fragments.get(position);
        }else{
            fragment = fragments.get(0);
        }

        return fragment;
    }

    @Override
    public String getPageTitle(int position){
        if(names != null && names.size() > 0)
            return names.get(position);
        return null;
    }

    @Override
    public int getCount() {
        return names.size();
    }
}
