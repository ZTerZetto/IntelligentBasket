package com.example.zzx.zbar_demo.adapter.basket;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;


import com.example.zzx.zbar_demo.R;
import com.example.zzx.zbar_demo.widget.image.SmartImageView;

import java.util.List;

/**
 * Created by pengchenghu on 2019/2/25.
 * Author Email: 15651851181@163.com
 * Describe: 设备运行状态截图适配器
 */
public class WorkPhotoAdapter extends ArrayAdapter<String> {
    private int resourceId;

    public WorkPhotoAdapter(Context context, int textViewResourceId, List<String> objects){
        super(context, textViewResourceId, objects);
        resourceId = textViewResourceId;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent ){
        View view;
        ViewHolder viewHolder;
        // Bitmap bitmap = getItem(position);
        String url = getItem(position);

        if(convertView == null){
            view = LayoutInflater.from(getContext()).inflate(resourceId, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.workPhotoIv = (SmartImageView) view.findViewById(R.id.work_photo);
            view.setTag(viewHolder);
        }else{
            view = convertView;
            viewHolder = (ViewHolder) view.getTag();  //重新获取ViewHolder
        }

        viewHolder.workPhotoIv.setImageUrl(url); // 设置workphoto

        return view;
    }

    class ViewHolder {
        SmartImageView workPhotoIv;
    }
}
