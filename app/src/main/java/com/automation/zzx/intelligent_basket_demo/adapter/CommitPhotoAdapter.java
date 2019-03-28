package com.automation.zzx.intelligent_basket_demo.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.automation.zzx.intelligent_basket_demo.R;
import com.automation.zzx.intelligent_basket_demo.entity.AppConfig;
import com.automation.zzx.intelligent_basket_demo.widget.image.SmartImageView;

import java.util.List;

/**
 * Describe: 添加多张图片适配器
 */
public class CommitPhotoAdapter extends ArrayAdapter<String> {
    private int resourceId;
    private List<String> objects;

    public CommitPhotoAdapter(Context context, int textViewResourceId, List<String> objects){
        super(context, textViewResourceId, objects);
        resourceId = textViewResourceId;
        this.objects = objects;
    }
    @Override
    public int getCount() {
        //return objects.size() + 1;//因为最后多了一个添加图片的ImageView
        int count = objects == null ? 1 : objects.size() + 1;
        if (count > AppConfig.MAX_SELECT_PIC_NUM) {
            return objects.size();
        } else {
            return count;
        }
    }



    @Override
    public View getView(int position, View convertView, ViewGroup parent ){
        View view;
        ViewHolder viewHolder;

        if(convertView == null){
            view = LayoutInflater.from(getContext()).inflate(resourceId, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.workPhotoIv = (SmartImageView) view.findViewById(R.id.pro_photo);
            view.setTag(viewHolder);
        }else {
            view = convertView;
            viewHolder = (ViewHolder) view.getTag();  //重新获取ViewHolder
        }

        if (position < objects.size()) {
            //代表+号之前的需要正常显示图片
            viewHolder.workPhotoIv.setImageUrl(getItem(position)); // 设置photo
        } else {
            viewHolder.workPhotoIv.setImageResource(R.mipmap.icon_add);//最后一个显示加号图片
        }
        return view;
    }

    class ViewHolder {
        SmartImageView workPhotoIv;
    }
}
