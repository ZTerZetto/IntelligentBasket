package com.automation.zzx.intelligent_basket_demo.activity;

import android.app.Dialog;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import com.automation.zzx.intelligent_basket_demo.R;
import com.automation.zzx.intelligent_basket_demo.activity.loginRegist.LoginActivity;
import com.automation.zzx.intelligent_basket_demo.entity.UserInfo;
import com.automation.zzx.intelligent_basket_demo.fragment.InfoFragment;
import com.automation.zzx.intelligent_basket_demo.fragment.MapViewFragment;
import com.automation.zzx.intelligent_basket_demo.fragment.UserFragment;
import com.automation.zzx.intelligent_basket_demo.widget.dialog.CommonDialog;

import java.util.ArrayList;


public class ManageMainActivity extends AppCompatActivity implements View.OnClickListener {

    /*获取登陆消息*/
    private SharedPreferences pref;

    private TextView tabInfo;
    private TextView tabMap;
    private TextView tabUser;

    private InfoFragment infoFragment;
    private MapViewFragment mapViewFragment;
    private UserFragment userFragment;

    private UserInfo userInfo;
    private String token;
    private CommonDialog mCommonDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_manage_main);

        pref = PreferenceManager.getDefaultSharedPreferences(this);
        if (pref.getString("loginToken","") != null){
            token = pref.getString("loginToken", "");

            if(pref.getString("userId","")!= null
                &&  pref.getString("userRole","") != null
                &&  pref.getString("userPhone","") != null
                && pref.getString("password", "") != null
                && pref.getString("userName","") != null) {
                userInfo = new UserInfo(pref.getString("userId", ""), pref.getString("userName", ""),
                        pref.getString("password", ""), pref.getString("userPhone", ""),
                        pref.getString("userRole", ""),null,null,true);
            }
        } else {
            if (mCommonDialog == null) {
                mCommonDialog = initDialog(getString(R.string.dialog_main_fail_login));
            }
            mCommonDialog.show();
        }

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
        transaction.commit();

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

    /*
     * 提示弹框
     */
    private CommonDialog initDialog(String mMsg) {
        return new CommonDialog(this, R.style.dialog, mMsg,
                new CommonDialog.OnCloseListener() {
                    @Override
                    public void onClick(Dialog dialog, boolean confirm) {
                        if (confirm) {
                            Intent intent = new Intent(ManageMainActivity.this, LoginActivity.class);
                            startActivity(intent);
                        } else {
                            dialog.dismiss();
                        }
                    }
                }).setTitle("提示");
    }


    public UserInfo getInfo(){
        return userInfo;
    }
}
