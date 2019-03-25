package com.example.zzx.zbar_demo.adapter.worker;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.zzx.zbar_demo.R;
import com.example.zzx.zbar_demo.entity.WorkerOrder;
import com.example.zzx.zbar_demo.utils.DateUtil;

import java.util.Calendar;
import java.util.List;

/**
 * Created by pengchenghu on 2019/3/18.
 * Author Email: 15651851181@163.com
 * Describe: 工单双列表的子列表适配器
 * More:
 */
public class WorkerOrderChildAdapter extends RecyclerView.Adapter<WorkerOrderChildAdapter.ViewHolder>{

    private List<WorkerOrder> mWorkerOrderList;
    private Context mContext;
    private OnItemClickListener mOnItemClickListener;

    class ViewHolder  extends RecyclerView.ViewHolder implements View.OnClickListener {

        View rootView;
        LinearLayout orderDateLy; // 日 周布局
        TextView orderDayTv;  // 日
        TextView orderWeekTv; // 星期
        TextView projectNameTv; // 项目名称
        TextView orderTimeTv; // 项目起止时间
        TextView timingLengthTv; // 项目时长
        View splitLineView;

        private OnItemClickListener onItemClickListener;

        public ViewHolder(@NonNull View itemView, OnItemClickListener onItemClickListener) {
            super(itemView);
            // 控件初始化
            rootView = itemView;
            orderDateLy = (LinearLayout) itemView.findViewById(R.id.order_date_layout);
            orderDayTv = (TextView)itemView.findViewById(R.id.order_day_tv);
            orderWeekTv = (TextView) itemView.findViewById(R.id.order_week_tv);
            projectNameTv = (TextView) itemView.findViewById(R.id.order_project_name);
            orderTimeTv = (TextView) itemView.findViewById(R.id.order_time);
            timingLengthTv = (TextView) itemView.findViewById(R.id.order_timing_length);
            splitLineView = (View) itemView.findViewById(R.id.split_line_view);

            // 控件绑定监听
            this.onItemClickListener = onItemClickListener;
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            // getpostion()为Viewholder自带的一个方法，用来获取RecyclerView当前的位置，将此作为参数，传出去
            onItemClickListener.onItemClick(v, getAdapterPosition());
        }
    }

    public WorkerOrderChildAdapter(Context context, List<WorkerOrder> workerOrders){
        mWorkerOrderList = workerOrders;
        mContext = context;
    }

    @NonNull
    @Override
    public WorkerOrderChildAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, final int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(
                R.layout.item_worker_order_child, viewGroup,false);
        ViewHolder holder = new ViewHolder(view, mOnItemClickListener);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull WorkerOrderChildAdapter.ViewHolder viewHolder, int position) {
        WorkerOrder workerOrder= mWorkerOrderList.get(position);
        String dateStr = workerOrder.getDate();  // 默认格式2019-03-16
        Calendar calendar = DateUtil.parseDateString(dateStr);
        viewHolder.orderDayTv.setText(DateUtil.getMonth(calendar.get(Calendar.DAY_OF_MONTH)));
        viewHolder.orderWeekTv.setText(DateUtil.getWeekDay(calendar.get(Calendar.DAY_OF_WEEK)));
        viewHolder.projectNameTv.setText(workerOrder.getProject_name());
        viewHolder.orderTimeTv.setText(workerOrder.getStart_time() + "-" + workerOrder.getStop_time());
        viewHolder.timingLengthTv.setText("+ " + workerOrder.getTiming_length());

        // 最后一行数据清除分割线
        if(position == getItemCount())
            viewHolder.splitLineView.setVisibility(View.INVISIBLE);
        // 如果当前行和上一行的日期相同，不显示OorderDateLy
        if((position > 0) && (mWorkerOrderList.get(position).getDate()).
                equals(mWorkerOrderList.get(position-1).getDate())) {
            viewHolder.orderDayTv.setVisibility(View.INVISIBLE);
            viewHolder.orderWeekTv.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        return mWorkerOrderList.size();
    }

    /*
     * 设置监听
     */
    public void setOnItemClickListener(OnItemClickListener mOnItemClickListener) {
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
