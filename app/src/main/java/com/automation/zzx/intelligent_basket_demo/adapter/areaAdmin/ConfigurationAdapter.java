package com.automation.zzx.intelligent_basket_demo.adapter.areaAdmin;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.automation.zzx.intelligent_basket_demo.R;
import com.automation.zzx.intelligent_basket_demo.entity.ConfigurationItem;

import java.util.List;

public class ConfigurationAdapter extends RecyclerView.Adapter<ConfigurationAdapter.ViewHolder> {

    // 控件
    private Context mContext;
    private List<ConfigurationItem> mConfigurationList;
    private OnItemClickListener mOnItemClickListener;


    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        View mView;
        TextView tvType;//吊篮种类
        EditText tvNum;//吊篮个数
        TextView tvUnit;//单位

        private ConfigurationAdapter.OnItemClickListener onItemClickListener;

        public ViewHolder(View itemView, final ConfigurationAdapter.OnItemClickListener onItemClickListener) {
            super(itemView);
            // 控件初始化
            mView = itemView;
            tvType = itemView.findViewById(R.id.txt_type);
            tvNum = itemView.findViewById(R.id.txt_number);
            tvUnit = itemView.findViewById(R.id.txt_unit);

            // 消息监听
            this.onItemClickListener = onItemClickListener;
            itemView.setOnClickListener(this);

            tvNum.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onItemClickListener.onItemNumber(v,getAdapterPosition());
                }
            });
        }

        @Override
        public void onClick(View v) {
            // getpostion()为Viewholder自带的一个方法，用来获取RecyclerView当前的位置，将此作为参数，传出去
            onItemClickListener.onItemClick(v, getAdapterPosition());
        }
    }

    /*
     * 构造函数
     */
    public ConfigurationAdapter(Context mContext, List<ConfigurationItem> mConfigurationList){
        this.mContext = mContext;
        this.mConfigurationList = mConfigurationList;
    }

    @NonNull
    @Override
    public ConfigurationAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(
                R.layout.item_configuration, viewGroup, false);
        ViewHolder viewHolder = new ViewHolder(view, mOnItemClickListener);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ConfigurationAdapter.ViewHolder viewHolder, int i) {
        ConfigurationItem mConfiguration = mConfigurationList.get(i);
        viewHolder.tvType.setText(mConfiguration.getName());

        if(mConfiguration.getUnit() != null )  viewHolder.tvNum.setHint(mConfiguration.getNumber());

        if(mConfiguration.getUnit() == null || mConfiguration.getUnit().equals("")) viewHolder.tvUnit.setText("个");
        else  viewHolder.tvUnit.setText(mConfiguration.getUnit());
    }

    @Override
    public int getItemCount() {
        return mConfigurationList.size();
    }

    /*
     * 设置监听
     */
    public void setOnItemClickListener(ConfigurationAdapter.OnItemClickListener mOnItemClickListener) {
        this.mOnItemClickListener = mOnItemClickListener;
    }

    /*
     * 点击接口函数
     */
    public interface OnItemClickListener {
        /**
         * 当RecyclerView某个被点击的时候回调
         *
         * @param view     点击item的视图
         * @param position 点击得到位置
         */
        public void onItemClick(View view, int position);

        /**
         * 当RecyclerView某个被点击的时候回调
         *
         * @param view     点击item的视图
         * @param position 点击得到位置
         */
        public void onItemNumber(View view, int position);
    }

}
