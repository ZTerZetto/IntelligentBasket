package com.example.zzx.zbar_demo.adapter.rentAdmin;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.example.zzx.zbar_demo.R;
import com.example.zzx.zbar_demo.adapter.worker.WorkerOrderChildAdapter;
import com.example.zzx.zbar_demo.entity.MgBasketInfo;
import com.example.zzx.zbar_demo.widget.image.SmartImageView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by pengchenghu on 2019/3/25.
 * Author Email: 15651851181@163.com
 * Describe: 租方管理员管理吊篮列表
 */
public class MgBasketListAdapter extends RecyclerView.Adapter<MgBasketListAdapter.ViewHolder> {

    private Context mContext;
    private List<MgBasketInfo> mgBasketInfoList;
    private static Map<Integer, Boolean> isCheck = new HashMap<Integer, Boolean>(); // 存储勾选框状态的map集合
    private OnItemClickListener mOnItemClickListener;  // 消息监听

    /*
     * viewholder
     */
    static public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        // 控件
        CheckBox basketSelected;  // 复选框
        SmartImageView basketImage;
        TextView basketId;
        TextView basketState;
        TextView basketOutStorage;
        TextView basketPrincipal;

        private OnItemClickListener onItemClickListener;

        public ViewHolder(@NonNull View itemView, final OnItemClickListener onItemClickListener) {
            super(itemView);
            // 初始化控件
            basketSelected = (CheckBox) itemView.findViewById(R.id.basket_checkbox);
            basketImage = (SmartImageView) itemView.findViewById(R.id.basket_realimage);
            basketId = (TextView) itemView.findViewById(R.id.basket_id);
            basketState = (TextView) itemView.findViewById(R.id.basket_state);
            basketOutStorage = (TextView) itemView.findViewById(R.id.basket_out_storage);
            basketPrincipal = (TextView) itemView.findViewById(R.id.basket_principal);

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
    public MgBasketListAdapter(Context mContext, List<MgBasketInfo> mgBasketInfoList){
        this.mContext = mContext;
        this.mgBasketInfoList = mgBasketInfoList;
        initCheck(false);
    }

    /*
     * 其它函数
     */
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int position) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(
                R.layout.item_basket_recycler_view, viewGroup, false);
        ViewHolder viewHolder = new ViewHolder(view, mOnItemClickListener);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int position) {
        MgBasketInfo mgBasketInfo = mgBasketInfoList.get(position);
        if(isCheck.get(position) == null){
            isCheck.put(position, false);
        }
        viewHolder.basketSelected.setChecked(isCheck.get(position));  // 设置状态
        //viewHolder.basketImage.setImageUrl();
        //viewHolder.basketId.setText(mgBasketInfo.getId());
        //viewHolder.basketState.setText(mgBasketInfo.getState());
        //viewHolder.basketOutStorage.setText(mgBasketInfo.getOutStorage());
        //viewHolder.basketPrincipal.setText(mgBasketInfo.getPrincipal());
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
    public void setOnItemClickListener(MgBasketListAdapter.OnItemClickListener mOnItemClickListener) {
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
        public void onCheckChanged(View view, int position,boolean checked);
    }
}
