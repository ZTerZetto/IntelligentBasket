package com.automation.zzx.intelligent_basket_demo.adapter.inspectPerson;

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
 * Created by pengchenghu on 2019/5/14.
 * Author Email: 15651851181@163.com
 * Describe: 巡检人员查看项目中吊篮列表适配器
 */
public class AddedBasketAdapter extends RecyclerView.Adapter<AddedBasketAdapter.ViewHolder>{
    private Context mContext;
    private List<MgBasketStatement> mgBasketStatementList;

    /*
     * viewholder
     */
    public class ViewHolder extends RecyclerView.ViewHolder{

        // 控件
        TextView basketStatementTextView;
        TextView basketIdTextView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            // 初始化控件
            basketStatementTextView = (TextView)  itemView.findViewById(R.id.basket_statement_textview);
            basketIdTextView = (TextView)  itemView.findViewById(R.id.basket_id);
        }
    }

    /*
     * 构造函数
     */
    public AddedBasketAdapter(Context mContext, List<MgBasketStatement> mgBasketStatementList){
        this.mContext = mContext;
        this.mgBasketStatementList = mgBasketStatementList;
    }

    @NonNull
    @Override
    public AddedBasketAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int position) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(
                R.layout.item_inspect_person_mg_basket, viewGroup, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull AddedBasketAdapter.ViewHolder viewHolder, int position) {
        MgBasketStatement mgBasketStatement = mgBasketStatementList.get(position);

        switch(mgBasketStatement.getBasketStatement()){ // 吊篮状态
            case "0":
                viewHolder.basketStatementTextView.setText("待入库");
                break;
            case "1":
                viewHolder.basketStatementTextView.setText("待安装");
                break;
            case "2":
                viewHolder.basketStatementTextView.setText("安装审核");
                break;
            case "3":
                viewHolder.basketStatementTextView.setText("使用中");
                break;
            case "4":
                viewHolder.basketStatementTextView.setText("待报停");
                break;
            case "5":
                viewHolder.basketStatementTextView.setText("报停审核");
                break;
            default:
                break;
        }
        viewHolder.basketIdTextView.setText(mgBasketStatement.getBasketId()); // 吊篮id
    }

    @Override
    public int getItemCount() {
        return mgBasketStatementList.size();
    }
}
