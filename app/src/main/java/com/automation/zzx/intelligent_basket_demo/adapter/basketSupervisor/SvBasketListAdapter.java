package com.automation.zzx.intelligent_basket_demo.adapter.basketSupervisor;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.automation.zzx.intelligent_basket_demo.R;
import com.automation.zzx.intelligent_basket_demo.entity.MgBasketStatement;
import java.util.List;



/**
 * Created by pengchenghu on 2019/7/8.
 * Describe: 安检人员监控吊篮使用
 */
public class SvBasketListAdapter extends RecyclerView.Adapter<SvBasketListAdapter.ViewHolder>{

    private Context mContext;
    private List<MgBasketStatement> mgBasketStatementList;
    private OnItemClickListener mOnItemClickListener;  // 消息监听

    /*
     * viewholder
     */
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        // 控件
        TextView basketStatementTextView;
        TextView basketIdTextView;


        private SvBasketListAdapter.OnItemClickListener onItemClickListener;
        public ViewHolder(@NonNull View itemView,final OnItemClickListener onItemClickListener) {
            super(itemView);

            // 初始化控件
            basketStatementTextView = (TextView)  itemView.findViewById(R.id.basket_statement_textview);
            basketIdTextView = (TextView)  itemView.findViewById(R.id.basket_id);
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

    /*
     * 构造函数
     */
    public SvBasketListAdapter(Context mContext, List<MgBasketStatement> mgBasketStatementList){
        this.mContext = mContext;
        this.mgBasketStatementList = mgBasketStatementList;
    }

    @NonNull
    @Override
    public SvBasketListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int position) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(
                R.layout.item_inspect_person_mg_basket, viewGroup, false);
        ViewHolder viewHolder = new ViewHolder(view,mOnItemClickListener);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull SvBasketListAdapter.ViewHolder viewHolder, int position) {
        MgBasketStatement mgBasketStatement = mgBasketStatementList.get(position);

        switch(mgBasketStatement.getBasketStatement()){ // 吊篮状态
            /*case "0":
                viewHolder.basketStatementTextView.setText("待入库");
                break;
            case "1":
                viewHolder.basketStatementTextView.setText("待安装");
                break;
            case "2":
                viewHolder.basketStatementTextView.setText("安装审核");
                break;*/
            case "3":
                viewHolder.basketStatementTextView.setText("使用中");
                break;
            /*case "4":
                viewHolder.basketStatementTextView.setText("待报停");
                break;
            case "5":
                viewHolder.basketStatementTextView.setText("报停审核");
                break;*/
            default:
                break;
        }
        viewHolder.basketIdTextView.setText(mgBasketStatement.getBasketId()); // 吊篮id
    }

    @Override
    public int getItemCount() {
        return mgBasketStatementList.size();
    }

    /*
     * 设置监听
     */
    public void setOnItemClickListener(SvBasketListAdapter.OnItemClickListener mOnItemClickListener) {
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
    }
}
