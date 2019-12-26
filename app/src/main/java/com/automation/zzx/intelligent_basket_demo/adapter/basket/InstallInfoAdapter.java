package com.automation.zzx.intelligent_basket_demo.adapter.basket;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.automation.zzx.intelligent_basket_demo.R;
import com.automation.zzx.intelligent_basket_demo.entity.MgBasketInstallInfo;
import com.automation.zzx.intelligent_basket_demo.entity.MgBasketStatement;
import com.automation.zzx.intelligent_basket_demo.widget.image.SmartImageView;

import java.util.List;

public class InstallInfoAdapter extends RecyclerView.Adapter<InstallInfoAdapter.ViewHolder>{
    private Context mContext;
    private List<MgBasketInstallInfo> mBasketList;
    private OnItemClickListener mOnItemClickListener;  // 消息监听

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        View mView;
        SmartImageView ivLogo;  // logo
        TextView tvId;  // 吊篮名称
        TextView ivWorkerInfo;  // 安装人员
        TextView ivFinishTime;  // 预计完成时间


        private OnItemClickListener onItemClickListener;

        public ViewHolder(@NonNull View itemView, final InstallInfoAdapter.OnItemClickListener onItemClickListener) {
            super(itemView);

            // 控件初始化
            mView = itemView;
            ivLogo = itemView.findViewById(R.id.basket_logo_smartImg);
            tvId = itemView.findViewById(R.id.basket_id_tv);
            ivWorkerInfo = itemView.findViewById(R.id.worker_info_iv);
            ivFinishTime = itemView.findViewById(R.id.end_time_tv);

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

    /* 构造函数
     */
    public InstallInfoAdapter(Context mContext, List<MgBasketInstallInfo> basketList){
        this.mContext = mContext;
        mBasketList = basketList;
    }

    @NonNull
    @Override
    public InstallInfoAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_install_info,viewGroup,false);
        final InstallInfoAdapter.ViewHolder holder = new InstallInfoAdapter.ViewHolder(view, mOnItemClickListener);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull InstallInfoAdapter.ViewHolder viewHolder, int i) {
        MgBasketInstallInfo basket = mBasketList.get(i);
        viewHolder.tvId.setText(basket.getBasketId());
        if(basket.getUserId() != null){
            viewHolder.ivWorkerInfo.setText(basket.getUserId());
        }else{
            viewHolder.ivWorkerInfo.setText("待分配");
            viewHolder.ivWorkerInfo.setTextColor(Color.DKGRAY);
        }
        if(basket.getEndTime() != null){
            viewHolder.ivFinishTime.setText(basket.getEndTime());
        } else {
            viewHolder.ivFinishTime.setText("暂无");
            viewHolder.ivFinishTime.setTextColor(Color.DKGRAY);
        }

        //TODO 安装信息完善

    }

    @Override
    public int getItemCount() {
        return mBasketList.size();
    }

    /*
     * 设置监听
     */
    public void setOnItemClickListener(InstallInfoAdapter.OnItemClickListener mOnItemClickListener) {
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
