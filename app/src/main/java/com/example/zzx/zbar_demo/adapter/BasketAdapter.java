package com.example.zzx.zbar_demo.adapter;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.zzx.zbar_demo.R;
import com.example.zzx.zbar_demo.entity.BasketInfo;

import java.util.ArrayList;
import java.util.List;

// Created by $USER_NAME on 2018/12/12/012.
public class BasketAdapter extends ArrayAdapter<BasketInfo> {

    private int resourceId;
    private List<BasketInfo> basketItems = new ArrayList<>();
    private  Context context;

    public BasketAdapter(@NonNull Context context, @NonNull int resource, @NonNull List<BasketInfo> objects) {
        super(context, resource, objects);
        this.context = context;
        this.basketItems = objects;
        resourceId = resource;
    }


    @Override
    public View getView(int position, View converView, ViewGroup parent){
        BasketInfo basketItems = getItem(position);//获取当前实例
        View view ;
        ViewHolder viewHolder;
        if (converView == null){
            view = LayoutInflater.from(getContext()).inflate(resourceId, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.basketId = (TextView) view.findViewById(R.id.txt_id);
            viewHolder.basketState = (ImageView) view.findViewById(R.id.txt_state);
            viewHolder.workerId = (TextView) view.findViewById(R.id.txt_worker);

            view.setTag(viewHolder);
        }else{
            view = converView;
            viewHolder = (ViewHolder) view.getTag();//重新获取ViewHolder
        }

        viewHolder.basketId.setText("吊篮编号：" + basketItems.getBasketId());
        if (basketItems.getState().equals("RESTING")) {
            viewHolder.basketState.setBackgroundColor(Color.rgb(255, 0, 0));
        } else if (basketItems.getState().equals("WORKING")) {
            viewHolder.basketState.setBackgroundColor(Color.rgb(0, 255, 0));
        }

        viewHolder.workerId.setText("负责人：" + basketItems.getWorkerId());

        //TODO null 的显示设置
        return view;
    }
    class ViewHolder{
        TextView basketId;
        ImageView basketState;
        TextView workerId;
    }
}
