package com.automation.zzx.intelligent_basket_demo.adapter.rentAdmin;


import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.automation.zzx.intelligent_basket_demo.R;
import com.automation.zzx.intelligent_basket_demo.entity.MessageInfo;

import java.util.List;


public class MgrRentMessageAdapter extends RecyclerView.Adapter<MgrRentMessageAdapter.ViewHolder> {

    private List<MessageInfo> mMessageInfoList;

    public class ViewHolder extends RecyclerView.ViewHolder{
        View mView;
        TextView tvTime;//消息时间
        LinearLayout llMessage;
        TextView tvTitle;//消息标题
        TextView tvContent;//消息内容

        public ViewHolder(View itemView) {
            super(itemView);
            // 控件初始化
            mView = itemView;
            tvTime = itemView.findViewById(R.id.tv_message_time);
            llMessage = itemView.findViewById(R.id.ll_message);
            tvTitle = itemView.findViewById(R.id.tv_message_title);
            tvContent = itemView.findViewById(R.id.tv_message_content);
        }
    }

    public MgrRentMessageAdapter(List<MessageInfo> messageInfoList){
        mMessageInfoList = messageInfoList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_message_info,viewGroup,false);
        final ViewHolder holder = new ViewHolder(view);
        holder.llMessage.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                int position = holder.getAdapterPosition();
                MessageInfo mMessageInfo = mMessageInfoList.get(position);
                Toast.makeText(v.getContext(),"RentAdmin报警提示："+mMessageInfo.getmContent(),Toast.LENGTH_SHORT).show();
            }
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        MessageInfo mMessageInfo = mMessageInfoList.get(i);
        viewHolder.tvTime.setText(mMessageInfo.getmTime());
        viewHolder.tvTitle.setText(mMessageInfo.getmTitle());
        viewHolder.tvContent.setText(mMessageInfo.getmContent());
    }

    @Override
    public int getItemCount() {
        return mMessageInfoList.size();
    }

}
