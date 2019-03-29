package com.automation.zzx.intelligent_basket_demo.fragment.rentAdmin;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.automation.zzx.intelligent_basket_demo.R;
import com.automation.zzx.intelligent_basket_demo.adapter.rentAdmin.MgWorkerListAdapter;
import com.automation.zzx.intelligent_basket_demo.entity.MgWorkerInfo;
import com.automation.zzx.intelligent_basket_demo.utils.ToastUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by pengchenghu on 2019/3/22.
 * Author Email: 15651851181@163.com
 * Describe: 租方管理员管理工人
 */
public class ManageWorkerFragment extends Fragment implements View.OnClickListener {

    private final static String TAG = "ManageWorkerFragment";

    // 工人列表
    private RecyclerView workerRv;
    private List<MgWorkerInfo> mgWorkerInfoList;
    private MgWorkerListAdapter mgWorkerListAdapter;

    // 底部统计
    private CheckBox workerAllCheckBox;
    private TextView workerCheckedNumberTv;
    private TextView workerDeleteTv;

    //悬浮按钮
    private ImageView workerAddIv;

    private String mProjectId;
    public SharedPreferences pref;
    private String token;

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            switch(msg.what) {
                default:
                    break;
            }
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_rent_mg_worker, container, false);

        // 列表初始化
        workerRv = (RecyclerView) view.findViewById(R.id.worker_recycler_view);
        initWorkerInfoList();
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        workerRv.setLayoutManager(layoutManager);
        mgWorkerListAdapter = new MgWorkerListAdapter(getContext(), mgWorkerInfoList);
        workerRv.setAdapter(mgWorkerListAdapter);
        mgWorkerListAdapter.setOnItemClickListener(new MgWorkerListAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                // 点击Item响应
                Log.i(TAG, "You have clicked the "+ position +" item");
            }

            @Override
            public void onCheckChanged(View view, int position, boolean checked) {
                // 点击复选框响应
                Log.i(TAG, "You have changed the "+ position +" item checkbox");
                int basketNumberSelected = mgWorkerListAdapter.checkedBasket();
                workerCheckedNumberTv.setText(String.valueOf(basketNumberSelected));
                workerAllCheckBox.setChecked(basketNumberSelected == mgWorkerInfoList.size());
            }

            @Override
            public void onPhoneCallClick(View view, int position) {
                Log.i(TAG, "You have clicked warning button");
                // 点击拨号响应
                Intent intent;
                intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:15651851181"));
                startActivity(intent);
            }
        });

        // 底部合计
        // 控件初始化
        workerAllCheckBox = (CheckBox) view.findViewById(R.id.worker_all_checkbox);
        workerAllCheckBox.setChecked(false);
        workerCheckedNumberTv = (TextView) view.findViewById(R.id.worker_number);
        workerDeleteTv = (TextView) view.findViewById(R.id.worker_apply_delete);
        // 消息监听
        workerAllCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Map<Integer,Boolean> isCheck = mgWorkerListAdapter.getMap();
                if(!isChecked){  // 规避减一个checkbox导致取消全选的问题
                    if(isCheck.size() != mgWorkerListAdapter.checkedBasket())
                        return;
                }
                mgWorkerListAdapter.initCheck(isChecked);
                mgWorkerListAdapter.notifyDataSetChanged();
            }
        });
        workerDeleteTv.setOnClickListener(this);  // 删除工人

        // 悬浮窗
        workerAddIv = (ImageView) view.findViewById(R.id.worker_add_image_view);
        workerAddIv.setOnClickListener(this);  // 添加工人

        return view;
    }

    /*
     * 控件点击消息响应
     */
    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.worker_apply_delete:
                Log.i(TAG, "You have clicked the apply_stop button");
                if(Integer.parseInt(workerCheckedNumberTv.getText().toString()) == 0) {
                    ToastUtil.showToastTips(getActivity(), "您尚未选择任何施工人员!");
                    break;
                }
                break;
            case R.id.worker_add_image_view:
                Log.i(TAG, "You have clicked the add_worker button");
                break;

        }
    }

    /*
     * 列表初始化
     */
    private void initWorkerInfoList(){
        mgWorkerInfoList = new ArrayList<>();

        for(int i=0; i<8; i++){
            mgWorkerInfoList.add(new MgWorkerInfo());
        }
    }

}
