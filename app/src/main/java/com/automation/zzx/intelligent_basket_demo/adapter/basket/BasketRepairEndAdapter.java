package com.automation.zzx.intelligent_basket_demo.adapter.basket;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.automation.zzx.intelligent_basket_demo.R;
import com.automation.zzx.intelligent_basket_demo.entity.RepairInfo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by zzx on 2019/5/28.
 * Describe: 吊篮报修记录适配器
 */
public class BasketRepairEndAdapter extends RecyclerView.Adapter<BasketRepairEndAdapter.ViewHolder> {

    private Context mContext;
    private List<RepairInfo> mRepairInfoList;
    private static Map<Integer, Boolean> isCheck = new HashMap<Integer, Boolean>(); // 存储勾选框状态的map集合
    private BasketRepairEndAdapter.OnItemClickListener mOnItemClickListener;  // 消息监听

    /*
     * viewholder
     */
    static public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        // 控件
        TextView basketId;
        TextView repairStartTime;
        TextView repairEndTime;

        private BasketRepairEndAdapter.OnItemClickListener onItemClickListener;

        public ViewHolder(@NonNull View itemView, final BasketRepairEndAdapter.OnItemClickListener onItemClickListener) {
            super(itemView);
            // 初始化控件
            basketId = (TextView) itemView.findViewById(R.id.basket_id);
            repairStartTime= (TextView) itemView.findViewById(R.id.basket_repair_start_time);
            repairEndTime = (TextView) itemView.findViewById(R.id.basket_repair_end_time);

            // 消息监听
            this.onItemClickListener = onItemClickListener;
            // checkbox 点击事件
            itemView.setOnClickListener(this);
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
    public BasketRepairEndAdapter(Context mContext, List<RepairInfo> mRepairInfoList){
        this.mContext = mContext;
        this.mRepairInfoList = mRepairInfoList;
        initCheck(false);
    }

    /*
     * 其它函数
     */
    @NonNull
    @Override
    public BasketRepairEndAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int position) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(
                R.layout.item_basket_repair_finish, viewGroup, false);
        BasketRepairEndAdapter.ViewHolder viewHolder = new BasketRepairEndAdapter.ViewHolder(view, mOnItemClickListener);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull BasketRepairEndAdapter.ViewHolder viewHolder, int position) {
        RepairInfo mRepairInfo = mRepairInfoList.get(position);
        if(isCheck.get(position) == null){
            isCheck.put(position, false);
        }
        viewHolder.basketId.setText(mRepairInfo.getDeviceId());
        viewHolder.repairStartTime.setText(mRepairInfo.getStartTime());
        viewHolder.repairEndTime.setText(mRepairInfo.getEndTime());
    }

    @Override
    public int getItemCount() {
        return mRepairInfoList.size();
    }

    /*
     * 复选框相关
     */
    // 初始化map集合
    public void initCheck(boolean flag){
        for(int i=0; i<mRepairInfoList.size(); i++){
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
    public void setOnItemClickListener(BasketRepairEndAdapter.OnItemClickListener mOnItemClickListener) {
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
