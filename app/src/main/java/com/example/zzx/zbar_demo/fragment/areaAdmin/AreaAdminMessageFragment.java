package com.example.zzx.zbar_demo.fragment.areaAdmin;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.zzx.zbar_demo.R;

/**
 * Created by pengchenghu on 2019/3/27.
 * Author Email: 15651851181@163.com
 * Describe:区域管理员消息页面
 */
public class AreaAdminMessageFragment extends Fragment {


    /*
     * 构造函数
     */
    public AreaAdminMessageFragment() {
        // Required empty public constructor

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_area_admin_message, container, false);
    }

}
