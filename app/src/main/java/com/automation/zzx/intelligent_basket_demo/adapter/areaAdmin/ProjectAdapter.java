package com.automation.zzx.intelligent_basket_demo.adapter.areaAdmin;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.support.v7.widget.RecyclerView;
import android.widget.TextView;

import com.automation.zzx.intelligent_basket_demo.R;
import com.automation.zzx.intelligent_basket_demo.entity.ProjectInfo;
import java.util.List;

// Created by $USER_NAME on 2018/12/12/012.
public class ProjectAdapter extends RecyclerView.Adapter<ProjectAdapter.ViewHolder> {
    
    private List<ProjectInfo> projectItems;
    private  Context context;
    private ProjectAdapter.OnItemClickListener mOnItemClickListener;  // 消息监听

    public ProjectAdapter(Context context, List<ProjectInfo> objects) {
        this.context = context;
        this.projectItems = objects;
    }

    /*
     * viewholder
     */
    static public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        // 控件
        TextView title;
        TextView id;
        TextView projectState;
        TextView startTime;
        TextView endTime;
        TextView company;

        private OnItemClickListener onItemClickListener;

        public ViewHolder(@NonNull View itemView, final OnItemClickListener onItemClickListener) {
            super(itemView);
            // 初始化控件
            title = (TextView) itemView.findViewById(R.id.project_title);
            id = (TextView) itemView.findViewById(R.id.project_id);
            startTime = (TextView) itemView.findViewById(R.id.project_start_time);
            projectState = (TextView) itemView.findViewById(R.id.project_state);
            endTime = (TextView) itemView.findViewById(R.id.project_end_time);
            company = (TextView) itemView.findViewById(R.id.project_company);

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

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(
                R.layout.item_area_admin_mg_project, viewGroup, false);
        ViewHolder viewHolder = new ViewHolder(view, mOnItemClickListener);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {

        ProjectInfo projectInfo = projectItems.get(i);
        switch (projectInfo.getProjectState().charAt(0)){
            case '0':
                viewHolder.projectState.setText("立项中");
                break;
            case '1':
            case '2':
                viewHolder.projectState.setText("吊篮安装中");
                break;
            case '3':
                viewHolder.projectState.setText("进行中");
                break;
            case '4':
                viewHolder.projectState.setText("已结束");
                break;
        }
        viewHolder.title.setText(projectInfo.getProjectName());
        viewHolder.id.setText(projectInfo.getProjectId());
        viewHolder.startTime.setText(projectInfo.getProjectStart());
        viewHolder.endTime.setText(projectInfo.getProjectEnd());
        viewHolder.company.setText(projectInfo.getCompanyName());
    }

    @Override
    public int getItemCount() {
        return projectItems.size();
    }
    
    /*
     * 设置监听
     */
    public void setOnItemClickListener(ProjectAdapter.OnItemClickListener mOnItemClickListener) {
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
