package com.automation.zzx.intelligent_basket_demo.adapter.basket;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.automation.zzx.intelligent_basket_demo.R;
import com.automation.zzx.intelligent_basket_demo.entity.PositionInfo;

import java.util.List;

public class BasketPlaneAdapter extends ArrayAdapter<PositionInfo> {
    private int resourceId;

    public BasketPlaneAdapter(Context context, int textViewResourceId, List<PositionInfo> objects) {
        super(context, textViewResourceId, objects);
        resourceId = textViewResourceId;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent ){
        View view;
        BasketPlaneAdapter.ViewHolder viewHolder;
        PositionInfo mStopRecord = getItem(position);

        if(convertView == null){
            view = LayoutInflater.from(getContext()).inflate(resourceId, parent, false);
            viewHolder = new BasketPlaneAdapter.ViewHolder();
            viewHolder.itemId = (TextView) view.findViewById(R.id.tv_item_id);
            viewHolder.id = (TextView) view.findViewById(R.id.tv_id);
            view.setTag(viewHolder);
        }else{
            view = convertView;
            viewHolder = (BasketPlaneAdapter.ViewHolder) view.getTag();  //重新获取ViewHolder
        }
        viewHolder.itemId.setText(mStopRecord.getItemId()); // 设置报警详情
        viewHolder.id.setText(mStopRecord.getId()); // 设置报警时间
        return view;
    }

    /*
     * 视图类
     */
    class ViewHolder {
        TextView itemId;
        TextView id;
    }
}
