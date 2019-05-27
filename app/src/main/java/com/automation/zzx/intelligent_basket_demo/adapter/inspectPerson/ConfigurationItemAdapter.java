package com.automation.zzx.intelligent_basket_demo.adapter.inspectPerson;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.automation.zzx.intelligent_basket_demo.R;
import com.automation.zzx.intelligent_basket_demo.adapter.basket.VarSwitchAdapter;
import com.automation.zzx.intelligent_basket_demo.entity.ConfigurationItem;
import com.automation.zzx.intelligent_basket_demo.entity.VarSwitch;

import java.util.List;

import static android.view.View.GONE;

/**
 * Created by pengchenghu on 2019/5/27.
 * Author Email: 15651851181@163.com
 * Describe: 配置清单列表的适配器
 */
public class ConfigurationItemAdapter extends ArrayAdapter<ConfigurationItem> {

    private int resourceId;

    public ConfigurationItemAdapter(Context context, int textViewResourceId, List<ConfigurationItem> objects){
        super(context, textViewResourceId, objects);
        resourceId = textViewResourceId;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent ){
        View view;
        ConfigurationItemAdapter.ViewHolder viewHolder;
        ConfigurationItem configurationItem = getItem(position);

        if(convertView == null){
            view = LayoutInflater.from(getContext()).inflate(resourceId, parent, false);
            viewHolder = new ConfigurationItemAdapter.ViewHolder();
            viewHolder.configurationName = (TextView) view.findViewById(R.id.name);
            viewHolder.configurationNumber = (TextView) view.findViewById(R.id.number);
            viewHolder.configurationUnit = (TextView) view.findViewById(R.id.unit);
            view.setTag(viewHolder);
        }else{
            view = convertView;
            viewHolder = (ConfigurationItemAdapter.ViewHolder) view.getTag();  //重新获取ViewHolder
        }

        viewHolder.configurationName.setText(configurationItem.getName());
        viewHolder.configurationNumber.setText(configurationItem.getNumber());
        viewHolder.configurationUnit.setText(configurationItem.getUnit());

        return view;
    }

    class ViewHolder {
        TextView configurationName;
        TextView configurationNumber;
        TextView configurationUnit;
    }
}
