package com.example.zzx.zbar_demo.adapter.basket;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.zzx.zbar_demo.R;
import com.example.zzx.zbar_demo.entity.Function;
import com.shehuan.niv.NiceImageView;

import java.util.List;

/**
 * Created by pengchenghu on 2019/2/23.
 * Author Email: 15651851181@163.com
 * Describe: 测试功能适配器
 */
public class FunctionAdapter extends ArrayAdapter<Function> {
    private int resourceId;

    public FunctionAdapter(Context context, int textViewResourceId, List<Function> objects){
        super(context, textViewResourceId, objects);
        resourceId = textViewResourceId;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent ){
        View view;
        ViewHolder viewHolder;
        Function function = getItem(position);

        if(convertView == null){
            view = LayoutInflater.from(getContext()).inflate(resourceId, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.functionName = (TextView) view.findViewById(R.id.function_name);
            viewHolder.functionImage = (NiceImageView) view.findViewById(R.id.function_icon);
            view.setTag(viewHolder);
        }else{
            view = convertView;
            viewHolder = (ViewHolder) view.getTag();  //重新获取ViewHolder
        }

        viewHolder.functionName.setText(function.getName()); // 设置功能名称
        viewHolder.functionImage.setImageResource(function.getImageId());    // 设置功能图片

        return view;
    }

    class ViewHolder {
        NiceImageView functionImage;
        TextView functionName;
    }
}
