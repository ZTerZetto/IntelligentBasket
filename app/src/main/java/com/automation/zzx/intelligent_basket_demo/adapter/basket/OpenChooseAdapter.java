package com.automation.zzx.intelligent_basket_demo.adapter.basket;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.automation.zzx.intelligent_basket_demo.R;
import com.automation.zzx.intelligent_basket_demo.entity.MgBasketStatement;
import com.automation.zzx.intelligent_basket_demo.widget.image.SmartImageView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OpenChooseAdapter extends RecyclerView.Adapter<OpenChooseAdapter.ViewHolder>{
    private Context mContext;
    private List<MgBasketStatement> mgBasketInfoList;
    private static Map<Integer, Boolean> isCheck = new HashMap<Integer, Boolean>(); // 存储勾选框状态的map集合
    private OpenChooseAdapter.OnItemClickListener mOnItemClickListener;  // 消息监听

    /*
     * viewholder
     */
    static public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        // 控件
        CheckBox basketSelected;  // 复选框
        SmartImageView basketImage;
        TextView basketId;
        TextView basketState;

        private OpenChooseAdapter.OnItemClickListener onItemClickListener;

        public ViewHolder(@NonNull View itemView, final OpenChooseAdapter.OnItemClickListener onItemClickListener) {
            super(itemView);
            // 初始化控件
            basketSelected = (CheckBox) itemView.findViewById(R.id.basket_checkbox);
            basketImage = (SmartImageView) itemView.findViewById(R.id.basket_realimage);
            basketId = (TextView) itemView.findViewById(R.id.basket_id);
            basketState = (TextView) itemView.findViewById(R.id.basket_state);

            // 消息监听
            this.onItemClickListener = onItemClickListener;
            // checkbox 点击事件
            basketSelected.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    isCheck.put(getAdapterPosition(), isChecked);  // 用map集合保存
                    onItemClickListener.onCheckChanged(buttonView, getAdapterPosition(),isChecked);
                }
            });
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
    public OpenChooseAdapter(Context mContext, List<MgBasketStatement> mgBasketInfoList){
        this.mContext = mContext;
        this.mgBasketInfoList = mgBasketInfoList;
        initCheck(false);
    }

    /*
     * 其它函数
     */
    @NonNull
    @Override
    public OpenChooseAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int position) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(
                R.layout.item_area_admin_open_basket, viewGroup, false);
        OpenChooseAdapter.ViewHolder viewHolder = new OpenChooseAdapter.ViewHolder(view, mOnItemClickListener);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull OpenChooseAdapter.ViewHolder viewHolder, int position) {
        MgBasketStatement mgBasketInfo = mgBasketInfoList.get(position);
        if(isCheck.get(position) == null){
            isCheck.put(position, false);
        }
        viewHolder.basketSelected.setChecked(isCheck.get(position));  // 设置状态
        //viewHolder.basketImage.setImageUrl();
        viewHolder.basketId.setText(mgBasketInfo.getBasketId());
        if(mgBasketInfo.getWorkStatement().equals("0")){
            viewHolder.basketState.setText("未上机");
        }else {
            viewHolder.basketState.setText("上机中");
        }

    }

    @Override
    public int getItemCount() {
        return mgBasketInfoList.size();
    }

    /*
     * 复选框相关
     */
    // 初始化map集合
    public void initCheck(boolean flag){
        for(int i=0; i<mgBasketInfoList.size(); i++){
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
    public void setOnItemClickListener(OpenChooseAdapter.OnItemClickListener mOnItemClickListener) {
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
        /**
         * 当RecyclerView某个CheckBox被点击的时候回调
         * @param view 点击item的视图
         *  @param position 位置
         * @param checked 或者状态
         */
        public void onCheckChanged(View view, int position, boolean checked);
    }

}
