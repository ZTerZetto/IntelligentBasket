package com.automation.zzx.intelligent_basket_demo.adapter.rentAdmin;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.automation.zzx.intelligent_basket_demo.R;
import com.automation.zzx.intelligent_basket_demo.entity.UserInfo;

import java.util.ArrayList;
import java.util.List;

// Created by $USER_NAME on 2018/12/12/012.
public class WorkerAdapter extends ArrayAdapter<UserInfo> {

    private int resourceId;
    private List<UserInfo> userItems = new ArrayList<>();
    private  Context context;

    public WorkerAdapter(@NonNull Context context, @NonNull int resource, @NonNull List<UserInfo> objects) {
        super(context, resource, objects);
        this.context = context;
        this.userItems = objects;
        resourceId = resource;

    }


    @Override
    public View getView(int position, View converView, ViewGroup parent){
        UserInfo userItems = getItem(position);//获取当前实例
        View view ;
        ViewHolder viewHolder;
        if (converView == null){
            view = LayoutInflater.from(getContext()).inflate(resourceId, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.workerName = (TextView) view.findViewById(R.id.txt_worker_name);
            viewHolder.userState = (ImageView) view.findViewById(R.id.txt_worker_state);
            viewHolder.workerId = (TextView) view.findViewById(R.id.txt_worker_Id);

            view.setTag(viewHolder);
        }else{
            view = converView;
            viewHolder = (ViewHolder) view.getTag();//重新获取ViewHolder
        }

        viewHolder.workerName.setText("姓名：" + userItems.getUserName());
        viewHolder.workerId.setText("编号：" + userItems.getUserId());

        //TODO null 的显示设置
        return view;
    }
    class ViewHolder{
        TextView workerName;
        ImageView userState;
        TextView workerId;
    }
}
