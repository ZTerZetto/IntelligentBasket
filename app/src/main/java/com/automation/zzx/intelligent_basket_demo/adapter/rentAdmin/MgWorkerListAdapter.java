package com.automation.zzx.intelligent_basket_demo.adapter.rentAdmin;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.automation.zzx.intelligent_basket_demo.R;
import com.automation.zzx.intelligent_basket_demo.entity.MgWorkerInfo;
import com.automation.zzx.intelligent_basket_demo.widget.image.SmartImageView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by pengchenghu on 2019/3/25.
 * Author Email: 15651851181@163.com
 * Describe:
 */
public class MgWorkerListAdapter extends RecyclerView.Adapter<MgWorkerListAdapter.ViewHolder>{

    private Context mContext;
    private List<MgWorkerInfo> mgWorkerInfoList;
    private static Map<Integer, Boolean> isCheck = new HashMap<Integer, Boolean>();
    private OnItemClickListener mOnItemClickListener;  // 消息监听

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        // 控件
        CheckBox workerSelected; // 复选框
        SmartImageView workerHeadImg; // 头像
        TextView workerName;
        TextView workerState;
        TextView workerBasketId;
        TextView workerTotalTime;
        ImageView workerPhone; // 联系方式

        private OnItemClickListener onItemClickListener;

        public ViewHolder(@NonNull View itemView, final OnItemClickListener onItemClickListener) {
            super(itemView);

            // 初始化控件
            workerSelected = (CheckBox) itemView.findViewById(R.id.worker_checkbox);
            workerHeadImg = (SmartImageView) itemView.findViewById(R.id.worker_headimage);
            workerName = (TextView) itemView.findViewById(R.id.worker_name);
            workerState = (TextView) itemView.findViewById(R.id.worker_state);
            workerBasketId = (TextView) itemView.findViewById(R.id.worker_basket_id);
            workerTotalTime = (TextView) itemView.findViewById(R.id.worker_total_time);
            workerPhone = (ImageView) itemView.findViewById(R.id.worker_call);

            // 消息监听
            this.onItemClickListener = onItemClickListener;
            // checkbox 点击事件
            workerSelected.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    isCheck.put(getAdapterPosition(), isChecked);  // 用map集合保存
                    onItemClickListener.onCheckChanged(buttonView, getAdapterPosition(),isChecked);
                }
            });
            itemView.setOnClickListener(this);
            // 拨号联系
            workerPhone.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onItemClickListener.onPhoneCallClick(v, getAdapterPosition());
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
    public MgWorkerListAdapter(Context mContext, List<MgWorkerInfo> mgWorkerInfoList){
        this.mContext = mContext;
        this.mgWorkerInfoList = mgWorkerInfoList;
        initCheck(false);
    }

    @NonNull
    @Override
    public MgWorkerListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int position) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(
                R.layout.item_rent_admin_mg_worker, viewGroup, false);
        MgWorkerListAdapter.ViewHolder viewHolder = new MgWorkerListAdapter.ViewHolder(view, mOnItemClickListener);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull MgWorkerListAdapter.ViewHolder viewHolder, int position) {
        MgWorkerInfo mgWorkerInfo = mgWorkerInfoList.get(position);
        if(isCheck.get(position) == null){
            isCheck.put(position, false);
        }
        viewHolder.workerSelected.setChecked(isCheck.get(position));  // 设置状态

//        viewHolder.workerHeadImg.setImageUrl(mgWorkerInfo.getHeadImg());
        viewHolder.workerHeadImg.setCircle(true);  // 圆形头像
        viewHolder.workerName.setText(mgWorkerInfo.getName());
//        viewHolder.workerState.setText(mgWorkerInfo.getState());
//        viewHolder.workerBasketId.setText(mgWorkerInfo.getBasketId());
//        viewHolder.workerTotalTime.setText(mgWorkerInfo.getTotalTime());
    }

    @Override
    public int getItemCount() {
        return mgWorkerInfoList.size();
    }

    /*
     * 复选框相关
     */
    // 初始化map集合
    public void initCheck(boolean flag){
        for(int i=0; i<mgWorkerInfoList.size(); i++){
            // 设置默认的显示
            isCheck.put(i, flag);
        }
    }
    // 全选button获取状态
    public Map<Integer, Boolean> getMap() {
        // 返回状态
        return isCheck;
    }
    // 获取已选中button的数目
    public int checkedBasket(){
        int sum=0;
        for(int i=0; i<isCheck.size(); i++){
            if(isCheck.get(i))
                sum++;
        }
        return sum;
    }

    /*
     * 设置监听
     */
    public void setOnItemClickListener(MgWorkerListAdapter.OnItemClickListener mOnItemClickListener) {
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
         * 当RecyclerView某个CheckBox被点击的时候回调
         * @param view 点击item的视图
         *  @param position 位置
         * @param checked 或者状态
         */
        public void onCheckChanged(View view, int position,boolean checked);
        /**
         * 当RecyclerView某个被点击的时候回调
         *
         * @param view     点击item的视图
         * @param position 点击得到位置
         */
        public void onPhoneCallClick(View view, int position);
    }

}
