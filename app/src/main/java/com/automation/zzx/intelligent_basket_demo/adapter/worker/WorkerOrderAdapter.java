package com.automation.zzx.intelligent_basket_demo.adapter.worker;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.automation.zzx.intelligent_basket_demo.R;
import com.automation.zzx.intelligent_basket_demo.entity.WorkerOrder;
import com.automation.zzx.intelligent_basket_demo.utils.DateUtil;
import com.automation.zzx.intelligent_basket_demo.utils.ToastUtil;

import java.util.Calendar;
import java.util.List;

/**
 * Created by pengchenghu on 2019/3/18.
 * Author Email: 15651851181@163.com
 * Describe:
 */
public class WorkerOrderAdapter extends RecyclerView.Adapter<WorkerOrderAdapter.ViewHolder> {

    private List<List<WorkerOrder>> mWorkerOrderList;
    private Context mContext;

    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView orderDateTv;  // 03月/2019
        RecyclerView orderListRv;  // 工单列表

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            orderDateTv = (TextView) itemView.findViewById(R.id.order_date);
            orderListRv = (RecyclerView) itemView.findViewById(R.id.child_order_recyclerview);
        }
    }

    /*
     * 构造函数
     */
    public WorkerOrderAdapter(Context mContext ,List<List<WorkerOrder>> workerOrderList){
        this.mContext = mContext;
        mWorkerOrderList = workerOrderList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int position) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_worker_order,
                viewGroup, false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int position) {
        List<WorkerOrder> workerOrders = mWorkerOrderList.get(position);
        if(workerOrders.size() > 0){
            String dateStr = workerOrders.get(0).getDate();  // 默认格式2019-03-16
            Calendar calendar = DateUtil.parseDateString(dateStr);
            viewHolder.orderDateTv.setText(DateUtil.getMonth(calendar.get(Calendar.MONTH)) + "月/" + // 显示年月
                    calendar.get(Calendar.YEAR));

            // 关键代码
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(mContext);
            linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
            viewHolder.orderListRv.setLayoutManager(linearLayoutManager);
            WorkerOrderChildAdapter workerOrderChildAdapter = new WorkerOrderChildAdapter(mContext,workerOrders);
            viewHolder.orderListRv.setAdapter(workerOrderChildAdapter);  // 加载适配器
            viewHolder.orderListRv.setVisibility(View.VISIBLE);

            // 点击工单消息响应
            workerOrderChildAdapter.setOnItemClickListener(new WorkerOrderChildAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(View view, int position) {
                    ToastUtil.showToastTips(mContext, "You clicked the " + position + " item");
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return mWorkerOrderList.size();
    }

}
