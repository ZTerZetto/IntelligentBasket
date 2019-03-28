package com.automation.zzx.intelligent_basket_demo.fragment;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.automation.zzx.intelligent_basket_demo.R;

// Created by $USER_NAME on 2018/12/3/003.
@SuppressLint("ValidFragment")
public class InfoFragment extends Fragment {

    private View mView;

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0: {

                    break;
                }
                default: {

                    break;
                }
            }
        }
    };


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (mView == null) {
            mView = inflater.inflate(R.layout.fragment_info, container, false);

            // 顶部导航栏
            Toolbar toolbar = (Toolbar) mView.findViewById(R.id.toolbar);
            TextView titleText = (TextView) mView.findViewById(R.id.toolbar_title);
            toolbar.setTitle("");
            titleText.setText(getString(R.string.info_title));
            ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
            //隐藏那个箭头
            ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        }
        return mView;
    }

}
