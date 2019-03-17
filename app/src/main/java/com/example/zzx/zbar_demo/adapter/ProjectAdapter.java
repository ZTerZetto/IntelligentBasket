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
import com.example.zzx.zbar_demo.entity.ProjectInfo;

import java.util.ArrayList;
import java.util.List;

// Created by $USER_NAME on 2018/12/12/012.
public class ProjectAdapter extends ArrayAdapter<ProjectInfo> {

    private int resourceId;
    private List<ProjectInfo> projectItems = new ArrayList<>();
    private  Context context;

    public ProjectAdapter(@NonNull Context context, @NonNull int resource, @NonNull List<ProjectInfo> objects) {
        super(context, resource, objects);
        this.context = context;
        this.projectItems = objects;
        resourceId = resource;
    }


    @Override
    public View getView(int position, View converView, ViewGroup parent){
        ProjectInfo projectItems = getItem(position);//获取当前实例
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

        viewHolder.basketId.setText(projectItems.getProjectId());
        if(projectItems.getProjectState().equals("WORKING")){
            viewHolder.basketState.setBackgroundColor(Color.rgb(0, 255, 0));
        } else if(projectItems.getProjectState().equals("RESTING")) {
            viewHolder.basketState.setBackgroundColor(Color.rgb(255, 0, 0));
        }

        viewHolder.workerId.setText(projectItems.getProjectName());

        return view;
    }
    class ViewHolder{
        TextView basketId;
        ImageView basketState;
        TextView workerId;
    }
}
