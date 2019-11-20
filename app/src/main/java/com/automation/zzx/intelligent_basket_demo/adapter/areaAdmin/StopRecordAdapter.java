package com.automation.zzx.intelligent_basket_demo.adapter.areaAdmin;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.automation.zzx.intelligent_basket_demo.R;
import com.automation.zzx.intelligent_basket_demo.entity.StopRecord;

import java.util.List;

public class StopRecordAdapter extends ArrayAdapter<StopRecord> {
    private int resourceId;

    public StopRecordAdapter(Context context, int textViewResourceId, List<StopRecord> objects) {
        super(context, textViewResourceId, objects);
        resourceId = textViewResourceId;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent ){
        View view;
        StopRecordAdapter.ViewHolder viewHolder;
        StopRecord mStopRecord = getItem(position);

        if(convertView == null){
            view = LayoutInflater.from(getContext()).inflate(resourceId, parent, false);
            viewHolder = new StopRecordAdapter.ViewHolder();
            viewHolder.basketId = (TextView) view.findViewById(R.id.tv_basket_id);
            viewHolder.stopTime = (TextView) view.findViewById(R.id.tv_stop_time);
            view.setTag(viewHolder);
        }else{
            view = convertView;
            viewHolder = (StopRecordAdapter.ViewHolder) view.getTag();  //重新获取ViewHolder
        }

        viewHolder.basketId.setText(mStopRecord.getDevice_id()); // 设置吊篮名称
        viewHolder.stopTime.setText(mStopRecord.getTime()); // 设置吊篮名称
        return view;
    }

    /*
     * 视图类
     */
    class ViewHolder {
        TextView basketId;
        TextView stopTime;
    }
}
