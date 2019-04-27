package com.automation.zzx.intelligent_basket_demo.adapter.basket;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.RelativeLayout;


import com.automation.zzx.intelligent_basket_demo.R;
import com.automation.zzx.intelligent_basket_demo.widget.image.BitmapImage;
import com.automation.zzx.intelligent_basket_demo.widget.image.SmartImageView;

import java.util.List;

/**
 * Created by pengchenghu on 2019/2/25.
 * Author Email: 15651851181@163.com
 * Describe: 设备运行状态截图适配器
 */
public class WorkPhotoAdapter extends ArrayAdapter<String> {
    private Context context;
    private int resourceId;

    public WorkPhotoAdapter(Context context, int textViewResourceId, List<String> objects){
        super(context, textViewResourceId, objects);
        this.context = context;
        resourceId = textViewResourceId;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent ){
        View view;
        ViewHolder viewHolder;
        //Bitmap bitmap = getItem(position);
        String url = getItem(position);

        if(convertView == null){
            view = LayoutInflater.from(getContext()).inflate(resourceId, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.workPhotoIv = (SmartImageView) view.findViewById(R.id.work_photo);

            // 设置图片大小
            int widthPix = ((Activity) context).getResources().getDisplayMetrics().widthPixels;
            widthPix = widthPix / 4 - 20;
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(widthPix, widthPix);
            viewHolder.workPhotoIv.setLayoutParams(params);

            view.setTag(viewHolder);
        }else{
            view = convertView;
            viewHolder = (ViewHolder) view.getTag();  //重新获取ViewHolder
        }

        viewHolder.workPhotoIv.setImageUrl(url); // 设置workphoto
        //viewHolder.workPhotoIv.setImageBitmap(bitmap); // 设置workphoto

        return view;
    }

    class ViewHolder {
        SmartImageView workPhotoIv;
    }
}
