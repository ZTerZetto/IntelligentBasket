package com.example.zzx.zbar_demo.fragment.rentAdmin;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.zzx.zbar_demo.R;
import com.example.zzx.zbar_demo.activity.basket.BasketListActivity;
import com.example.zzx.zbar_demo.adapter.BasketAdapter;
import com.example.zzx.zbar_demo.entity.BasketInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by pengchenghu on 2019/3/22.
 * Author Email: 15651851181@163.com
 * Describe:
 */
public class MgBasketListFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_rent_mg_basket_list, container, false);



        return view;
    }

}
