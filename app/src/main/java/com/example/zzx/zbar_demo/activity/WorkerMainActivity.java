package com.example.zzx.zbar_demo.activity;

import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import com.example.zzx.zbar_demo.R;
import com.example.zzx.zbar_demo.fragment.InfoFragment;
import com.example.zzx.zbar_demo.fragment.UserFragment;



public class WorkerMainActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView tabInfo;
    private TextView tabUser;

    private InfoFragment infoFragment;
    private UserFragment userFragment;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_worker_main);
        bindView();
    }

    //UI组件初始化与事件绑定
    private void bindView() {
        tabInfo = this.findViewById(R.id.txt_infoFrag);
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
        tabUser.setOnClickListener(this);
        //tabMore.setOnClickListener(this);
    }

    //重置所有文本的选中状态
    public void selected() {
        tabInfo.setSelected(false);
        tabUser.setSelected(false);
        //tabMore.setSelected(false);
    }

    //隐藏所有Fragment
    public void hideAllFragment(FragmentTransaction transaction) {
        if (infoFragment != null) {
            transaction.hide(infoFragment);
        }
        /*if(mapViewFragment!=null){
            transaction.hide(mapViewFragment);
        }
        if(userFragment!=null){
            transaction.hide(userFragment);
        }
        if(f4!=null){
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

            case R.id.txt_userFrag:
                selected();
                tabUser.setSelected(true);
                if (userFragment == null) {
                    userFragment = new UserFragment();
                    transaction.add(R.id.fragment_container, userFragment);
                } else {
                    transaction.show(userFragment);
                }
                break;
        }
        transaction.commit();
    }
}
