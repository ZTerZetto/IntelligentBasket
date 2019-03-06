package com.example.zzx.zbar_demo.activity;

import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import com.example.zzx.zbar_demo.R;
import com.example.zzx.zbar_demo.entity.UserInfo;
import com.example.zzx.zbar_demo.fragment.InfoFragment;
import com.example.zzx.zbar_demo.fragment.MapViewFragment;
import com.example.zzx.zbar_demo.fragment.UserFragment;

import java.util.ArrayList;


public class ManageMainActivity extends AppCompatActivity implements View.OnClickListener {

    /*获取登陆消息*/
    private SharedPreferences pref;
    private SharedPreferences.Editor editor;

    private TextView tabInfo;
    private TextView tabMap;
    private TextView tabUser;

    private InfoFragment infoFragment;
    private MapViewFragment mapViewFragment;
    private UserFragment userFragment;

    private UserInfo userInfo;
    private String token;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_manage_main);

        pref = PreferenceManager.getDefaultSharedPreferences(this);
        userInfo = new UserInfo(pref.getString("user_id",""),pref.getString("password",""));
        token = pref.getString("loginToken","");
        bindView();
    }

    //UI组件初始化与事件绑定
    private void bindView() {
        tabInfo = this.findViewById(R.id.txt_infoFrag);
        tabMap = this.findViewById(R.id.txt_mapFrag);
        tabUser = this.findViewById(R.id.txt_userFrag);

        //flagment初始化
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        infoFragment = new InfoFragment();
        transaction.add(R.id.fragment_container, infoFragment);
        selected();
        tabInfo.setSelected(true);
        transaction.show(infoFragment);//默认显示第一个页面*/
        transaction.commit();//

        tabInfo.setOnClickListener(this);
        tabMap.setOnClickListener(this);
        tabUser.setOnClickListener(this);

    }

    //重置所有文本的选中状态
    public void selected() {
        tabInfo.setSelected(false);
        tabMap.setSelected(false);
        tabUser.setSelected(false);
    }

    //隐藏所有Fragment
    public void hideAllFragment(FragmentTransaction transaction) {
        if (infoFragment != null) {
            transaction.hide(infoFragment);
        }
      if(mapViewFragment!=null){
            transaction.hide(mapViewFragment);
        }
        if(userFragment!=null){
            transaction.hide(userFragment);
        }
          /*if(f4!=null){
            transaction.hide(f4);
        }*/
    }

    @Override
    public void onClick(View v) {
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        hideAllFragment(transaction);
        switch (v.getId()) {
            case R.id.txt_infoFrag:
                selected();
                tabInfo.setSelected(true);
                if (infoFragment == null) {
                    infoFragment = new InfoFragment();
                    transaction.add(R.id.fragment_container, infoFragment);
                } else {
                    transaction.show(infoFragment);
                }
                break;

            case R.id.txt_mapFrag:
                selected();
                tabMap.setSelected(true);
                if (mapViewFragment == null) {
                    mapViewFragment = new MapViewFragment();
                    transaction.add(R.id.fragment_container, mapViewFragment);
                } else {
                    transaction.show(mapViewFragment);
                }
                break;

            case R.id.txt_userFrag:
                selected();
                tabUser.setSelected(true);
                if (userFragment == null) {
                    userFragment = new UserFragment();
                    transaction.add(R.id.fragment_container, userFragment);
                    Bundle bundle = new Bundle();
                    bundle.putString("loginToken",token);
                    userFragment.setArguments(bundle);
                } else {
                    transaction.show(userFragment);
                }
                break;
        }
        transaction.commit();
    }

    /*
     * 以下为在fragment中注册触摸监听
     */
    public interface MyTouchListener {
        public void onTouchEvent(MotionEvent event);
    }
    // 保存MyTouchListener接口的列表
    private ArrayList<MyTouchListener> myTouchListeners = new ArrayList<MyTouchListener>();
    /**
     * 提供给Fragment通过getActivity()方法来注册自己的触摸事件的方法
     * @param listener
     */
    public void registerMyTouchListener(MyTouchListener listener) {
        myTouchListeners.add(listener);
    }

    /**
     * 提供给Fragment通过getActivity()方法来取消注册自己的触摸事件的方法
     * @param listener
     */
    public void unRegisterMyTouchListener(MyTouchListener listener) {
        myTouchListeners.remove( listener );
    }

    /**
     * 分发触摸事件给所有注册了MyTouchListener的接口
     */
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        for (MyTouchListener listener : myTouchListeners) {
            listener.onTouchEvent(ev);
        }
        return super.dispatchTouchEvent(ev);
    }
}
