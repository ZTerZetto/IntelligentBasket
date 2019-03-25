package com.example.zzx.zbar_demo.fragment.rentAdmin;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.example.zzx.zbar_demo.R;
import com.example.zzx.zbar_demo.activity.basket.BasketDetailActivity;
import com.example.zzx.zbar_demo.adapter.rentAdmin.MgBasketListAdapter;
import com.example.zzx.zbar_demo.entity.MgBasketInfo;
import com.example.zzx.zbar_demo.utils.ToastUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by pengchenghu on 2019/3/22.
 * Author Email: 15651851181@163.com
 * Describe:
 */
public class MgBasketListFragment extends Fragment implements View.OnClickListener {

    private final static String TAG = "MgBasketListFragment";

    // 吊篮列表
    private RecyclerView basketRv; // 吊篮列表
    private List<MgBasketInfo> mgBasketInfoList;
    private MgBasketListAdapter mgBasketListAdapter;

    // 底部合计
    private CheckBox basketAllSelected;  // 全选复选框
    private TextView basketNumber;  // 已选择吊篮个数
    private TextView basketApplyStop; // 吊篮预报停

    // 本地存储
    private String projectId;
    public SharedPreferences pref;
    private String token;

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            switch(msg.what) {
                default:
                    break;
            }
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_rent_mg_basket_list, container, false);

        // 初始化吊篮列表
        basketRv = (RecyclerView) view.findViewById(R.id.basket_recycler_view);
        initBaksetList();
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        basketRv.setLayoutManager(layoutManager);
        mgBasketListAdapter = new MgBasketListAdapter(getContext(), mgBasketInfoList);
        basketRv.setAdapter(mgBasketListAdapter);
        mgBasketListAdapter.setOnItemClickListener(new MgBasketListAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                // item 点击响应
                Log.i(TAG, "You have clicked the "+ position +" item");
                Intent intent = new Intent(getActivity(), BasketDetailActivity.class);
                //intent.putExtra("basket_id", mgBasketInfoList.get(position).getId());
                startActivity(intent);
            }

            @Override
            public void onCheckChanged(View view, int position, boolean checked) {
                // checkbox 状态更换
                Log.i(TAG, "You have changed the "+ position +" item checkbox");
                int basketNumberSelected = mgBasketListAdapter.checkedBasket();
                basketNumber.setText(String.valueOf(basketNumberSelected));
                basketAllSelected.setChecked(basketNumberSelected == mgBasketInfoList.size());
            }
        });

        // 底部合计
        // 控件初始化
        basketAllSelected = (CheckBox) view.findViewById(R.id.basket_all_checkbox);
        basketAllSelected.setChecked(false);
        basketNumber = (TextView) view.findViewById(R.id.basket_number);
        basketApplyStop = (TextView) view.findViewById(R.id.basket_apply_stop);
        // 消息监听
        // 全选按钮
        basketAllSelected.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Map<Integer,Boolean> isCheck = mgBasketListAdapter.getMap();
                mgBasketListAdapter.initCheck(isChecked);
                mgBasketListAdapter.notifyDataSetChanged();
            }
        });
        // 预报停按钮点击
        basketApplyStop.setOnClickListener(this);

        return view;
    }

    /*
     * 控件点击响应
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.basket_apply_stop:
                Log.i(TAG, "You have clicked the apply_stop button");
                if(Integer.parseInt(basketNumber.getText().toString()) == 0) {
                    ToastUtil.showToastTips(getActivity(), "您尚未选择任何吊篮");
                    break;
                }
                break;
        }
    }

    /*
     * 初始化列表
     */
    private void initBaksetList(){
        mgBasketInfoList = new ArrayList<>();

        mgBasketInfoList.add(new MgBasketInfo());
        mgBasketInfoList.add(new MgBasketInfo());
        mgBasketInfoList.add(new MgBasketInfo());
        mgBasketInfoList.add(new MgBasketInfo());
        mgBasketInfoList.add(new MgBasketInfo());
        mgBasketInfoList.add(new MgBasketInfo());
    }

}
