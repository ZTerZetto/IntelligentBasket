package com.automation.zzx.intelligent_basket_demo.adapter.areaAdmin;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.automation.zzx.intelligent_basket_demo.R;
import com.automation.zzx.intelligent_basket_demo.entity.MgBasketStatement;
import com.automation.zzx.intelligent_basket_demo.widget.image.SmartImageView;

import java.util.List;


/**
 * Created by pengchenghu on 2019/3/27.
 * Changed by zhangzixuan on 2019/10/16.
 * Author Email: 15651851181@163.com
 * Describe: 区域管理员按状态管理吊篮
 */
public class MgBasketStatementAdapter extends RecyclerView.Adapter<MgBasketStatementAdapter.ViewHolder>{

    private Context mContext;
    private List<MgBasketStatement> mgBasketStatementList;
    private OnItemClickListener mOnItemClickListener;  // 消息监听

    /*
     * viewholder
     */
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        // 控件
        SmartImageView basketIndexImageView;
        TextView basketStatementTextView;
        ImageView workStateImageView;
        TextView basketIdTextView;
        TextView basketAllotTeam; // 分配安装队伍
        TextView basketInstallTv; //预验收文字状态修改
        TextView basketInstallPhoto; //预验收照片查看
        TextView basketUploadCert; // 安监证书
        TextView basketPreApplyStop; // 预报停申请

        private MgBasketStatementAdapter.OnItemClickListener onItemClickListener;

        public ViewHolder(@NonNull View itemView, final OnItemClickListener onItemClickListener) {
            super(itemView);

            // 初始化控件
            basketStatementTextView = (TextView)  itemView.findViewById(R.id.basket_statement_textview);
            basketIndexImageView = (SmartImageView) itemView.findViewById(R.id.basket_index_imageview);
            workStateImageView = itemView.findViewById(R.id.basket_index_state_iv);
            basketIdTextView = (TextView)  itemView.findViewById(R.id.basket_id);
            basketAllotTeam = (TextView)  itemView.findViewById(R.id.pre_assessment_acceptance);  // 分配安装队伍
            basketInstallTv = (TextView)  itemView.findViewById(R.id.pre_assessment_acceptance_2); //查看安装详情
            basketInstallPhoto = (TextView)  itemView.findViewById(R.id.pre_assessment_acceptance_3);
            basketUploadCert = (TextView)  itemView.findViewById(R.id. upload_certificate); // 安监证书上传
            basketPreApplyStop = (TextView)  itemView.findViewById(R.id.pre_apply_stop); // 预报停申请

            // 消息监听
            this.onItemClickListener = onItemClickListener;
            basketAllotTeam.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onItemClickListener.onAddInstall(v, getAdapterPosition());
                }
            });
            basketInstallTv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onItemClickListener.onInstallDetail(v, getAdapterPosition());
                }
            });

            basketUploadCert.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onItemClickListener.onUploadCertClick(v, getAdapterPosition());
                }
            });
            basketPreApplyStop.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onItemClickListener.onPreApplyStopClick(v, getAdapterPosition());
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
    public MgBasketStatementAdapter(Context mContext, List<MgBasketStatement> mgBasketStatementList){
        this.mContext = mContext;
        this.mgBasketStatementList = mgBasketStatementList;
    }

    @NonNull
    @Override
    public MgBasketStatementAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int position) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(
                R.layout.item_area_admin_mg_basket, viewGroup, false);
        ViewHolder viewHolder = new ViewHolder(view, mOnItemClickListener);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull MgBasketStatementAdapter.ViewHolder viewHolder, int position) {
        MgBasketStatement mgBasketStatement = mgBasketStatementList.get(position);

        //viewHolder.basketIndexImageView.setImageUrl(mgBasketStatement.getBasketId()); // 图像
        viewHolder.basketAllotTeam.setVisibility(View.GONE); // 分配安装队伍
        viewHolder.basketInstallTv.setVisibility(View.GONE);
        viewHolder.basketInstallPhoto.setVisibility(View.GONE);
        viewHolder.workStateImageView.setVisibility(View.GONE);
        viewHolder.basketInstallTv.setVisibility(View.GONE);
        viewHolder.basketUploadCert.setVisibility(View.GONE); // 安监证书
        viewHolder.basketPreApplyStop.setVisibility(View.GONE); // 预报停
        switch(mgBasketStatement.getBasketStatement()){ // 吊篮状态
            case "0":
                viewHolder.basketStatementTextView.setText("待入库");
                break;
            case "1":
                viewHolder.basketStatementTextView.setText("待安装");
                viewHolder.basketAllotTeam.setVisibility(View.VISIBLE);
                break;
            case "11":
                viewHolder.basketStatementTextView.setText("安装进行中");
                viewHolder.basketInstallTv.setVisibility(View.VISIBLE);
                break;
            case "2":
                viewHolder.basketStatementTextView.setText("安装审核");
                viewHolder.basketUploadCert.setVisibility(View.VISIBLE);
                break;
            case "3":
                viewHolder.basketStatementTextView.setText("使用中");
                //TODO 吊篮工作状态显示
                viewHolder.workStateImageView.setVisibility(View.GONE);
                //根据工作状态显示图标
                if(mgBasketStatement.getWorkStatement().equals("1")){
                    viewHolder.workStateImageView.setImageResource(R.mipmap.icon_working);
                }else if(mgBasketStatement.getWorkStatement().equals("0")){
                    viewHolder.workStateImageView.setImageResource(R.mipmap.icon_resting);
                } else {
                    viewHolder.workStateImageView.setImageResource(R.mipmap.icon_warning);
                }
                break;
            case "4":
                viewHolder.basketStatementTextView.setText("待报停");
                //viewHolder.basketPreApplyStop.setVisibility(View.VISIBLE);
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

    /*
     * 设置监听
     */
    public void setOnItemClickListener(MgBasketStatementAdapter.OnItemClickListener mOnItemClickListener) {
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
        public void onAddInstall(View view, int position);
        /**
         * 当RecyclerView某个被点击的时候回调
         *
         * @param view     点击item的视图
         * @param position 点击得到位置
         */
        public void onInstallDetail(View view, int position);
        /**
         * 当RecyclerView某个被点击的时候回调
         *
         * @param view     点击item的视图
         * @param position 点击得到位置
         */
       /* public void onUploadAccept(View view, int position);
        *//**
         * 当RecyclerView某个被点击的时候回调
         *
         * @param view     点击item的视图
         * @param position 点击得到位置
         */
        public void onUploadCertClick(View view, int position);
        /**
         * 当RecyclerView某个被点击的时候回调
         *
         * @param view     点击item的视图
         * @param position 点击得到位置
         */
        public void onPreApplyStopClick(View view, int position);
    }
}
