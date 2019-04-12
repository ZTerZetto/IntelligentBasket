package com.automation.zzx.intelligent_basket_demo.adapter.rentAdmin;


import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.automation.zzx.intelligent_basket_demo.R;
import com.automation.zzx.intelligent_basket_demo.entity.MessageInfo;

import java.util.List;


public class MgRentMessageAdapter extends RecyclerView.Adapter<MgRentMessageAdapter.ViewHolder> {

    private Context mContext;
    private List<MessageInfo> mMessageInfoList;
    private MgRentMessageAdapter.OnItemClickListener mOnItemClickListener;  // 消息监听

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        View mView;
        TextView tvTime;//消息时间
        LinearLayout llMessage;
        TextView tvTitle;//消息标题
        ImageView ivChecked; // 消息未读提示
        TextView tvContent;//消息内容

        private OnItemClickListener onItemClickListener;

        public ViewHolder(View itemView, final MgRentMessageAdapter.OnItemClickListener onItemClickListener) {
            super(itemView);
            // 控件初始化
            mView = itemView;
            tvTime = itemView.findViewById(R.id.tv_message_time);
            llMessage = itemView.findViewById(R.id.ll_message);
            tvTitle = itemView.findViewById(R.id.tv_message_title);
            ivChecked = itemView.findViewById(R.id.iv_message_no_read);
            tvContent = itemView.findViewById(R.id.tv_message_description);

            // 消息监听
            this.onItemClickListener = onItemClickListener;
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            // getpostion()为Viewholder自带的一个方法，用来获取RecyclerView当前的位置，将此作为参数，传出去
            onItemClickListener.onItemClick(v, getAdapterPosition());
        }
    }

    public MgRentMessageAdapter(Context mContext, List<MessageInfo> messageInfoList){
        this.mContext = mContext;
        mMessageInfoList = messageInfoList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_message_info,viewGroup,false);
        final ViewHolder holder = new ViewHolder(view, mOnItemClickListener);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        MessageInfo mMessageInfo = mMessageInfoList.get(i);
        viewHolder.tvTime.setText(mMessageInfo.getmTime());
        viewHolder.tvTitle.setText(mMessageInfo.getmTitle());
        if(mMessageInfo.ismIsChecked()) viewHolder.ivChecked.setVisibility(View.GONE);
        else viewHolder.ivChecked.setVisibility(View.VISIBLE);
        viewHolder.tvContent.setText(mMessageInfo.getmDescription());
    }

    @Override
    public int getItemCount() {
        return mMessageInfoList.size();
    }

    /*
     * 设置监听
     */
    public void setOnItemClickListener(MgRentMessageAdapter.OnItemClickListener mOnItemClickListener) {
        this.mOnItemClickListener = mOnItemClickListener;
    }

    /*
     * 点击接口函数
     */
    public interface OnItemClickListener {
        /**
         * 当RecyclerView某个被点击的时候回调
         * @param view 点击item的视图
         * @param position 点击得到位置
         */
        public void onItemClick(View view, int position);
    }

}
