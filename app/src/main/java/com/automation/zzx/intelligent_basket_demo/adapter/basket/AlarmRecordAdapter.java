package com.automation.zzx.intelligent_basket_demo.adapter.basket;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.automation.zzx.intelligent_basket_demo.R;
import com.automation.zzx.intelligent_basket_demo.entity.AlarmInfo;

import java.util.List;

public class AlarmRecordAdapter extends ArrayAdapter<AlarmInfo> {
    private int resourceId;

    public AlarmRecordAdapter(Context context, int textViewResourceId, List<AlarmInfo> objects) {
        super(context, textViewResourceId, objects);
        resourceId = textViewResourceId;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent ){
        View view;
        AlarmRecordAdapter.ViewHolder viewHolder;
        AlarmInfo mStopRecord = getItem(position);

        if(convertView == null){
            view = LayoutInflater.from(getContext()).inflate(resourceId, parent, false);
            viewHolder = new AlarmRecordAdapter.ViewHolder();
            viewHolder.basketId = (TextView) view.findViewById(R.id.tv_alarm_detail);
            viewHolder.stopTime = (TextView) view.findViewById(R.id.tv_stop_time);
            view.setTag(viewHolder);
        }else{
            view = convertView;
            viewHolder = (AlarmRecordAdapter.ViewHolder) view.getTag();  //重新获取ViewHolder
        }
        viewHolder.basketId.setText(mStopRecord.getAlarm_detail()); // 设置报警详情
        viewHolder.stopTime.setText(mStopRecord.getTime()); // 设置报警时间
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
