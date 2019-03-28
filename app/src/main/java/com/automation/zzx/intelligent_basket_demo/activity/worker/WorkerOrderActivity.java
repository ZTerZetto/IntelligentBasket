package com.automation.zzx.intelligent_basket_demo.activity.worker;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import com.automation.zzx.intelligent_basket_demo.R;
import com.automation.zzx.intelligent_basket_demo.adapter.worker.WorkerOrderAdapter;
import com.automation.zzx.intelligent_basket_demo.entity.WorkerOrder;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by pengchenghu on 2019/3/17.
 * Author Email: 15651851181@163.com
 * Describe: 施工人员工单查询页面
 * limits:
 */

public class WorkerOrderActivity extends AppCompatActivity {

    private final static String TAG = "WorkerOrderActivity";

    private TextView mNearOrderTv; // 近
    private ImageView mFilterIv; // 筛选

    // RecyclerView相关变量
    private RecyclerView mWorkerOrderRv; // 工单列表
    private List<List<WorkerOrder>> mWorkerOrderList; // 内容
    private WorkerOrderAdapter mWorkerOrderAdapter; // 适配器

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_worker_order);

        initWidgetResource();

    }

    /*
     * 初始化控件
     */
    private void initWidgetResource(){
        // 顶部导航栏
        android.support.v7.widget.Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        TextView titleText = (TextView) findViewById(R.id.toolbar_title);
        toolbar.setTitle("");
        titleText.setText(getString(R.string.order_title));
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true); //设置返回键可用

        // 初始化筛选
        mNearOrderTv = (TextView) findViewById(R.id.near_order_tv);
        mFilterIv = (ImageView) findViewById(R.id.order_filter_iv);

        // 工单列表
        mWorkerOrderRv = (RecyclerView) findViewById(R.id.parent_order_recyclerview);
        mWorkerOrderRv.setLayoutManager(new LinearLayoutManager(this));
        initWorkOrders();
        mWorkerOrderAdapter = new WorkerOrderAdapter(this,mWorkerOrderList);
        mWorkerOrderRv.setAdapter(mWorkerOrderAdapter);
    }

    /*
     * d顶部导航栏消息响应
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home: // 返回按钮
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /*
     * 初始化工单
     */
    private void initWorkOrders(){
        mWorkerOrderList = new ArrayList<>();

        List<WorkerOrder> workerOrderList = new ArrayList<>();
        WorkerOrder workerOrder1 = new WorkerOrder("2019-03-17", "13:30",
                "18:30", "300min", "东南大学四牌楼");
        workerOrderList.add(workerOrder1);
        WorkerOrder workerOrder2 = new WorkerOrder("2019-03-17", "06:30",
                "12:00", "330min", "东南大学四牌楼");
        workerOrderList.add(workerOrder2);
        WorkerOrder workerOrder3 = new WorkerOrder("2019-03-15", "06:30",
                "13:30", "420min", "万达广场");
        workerOrderList.add(workerOrder3);
        WorkerOrder workerOrder4 = new WorkerOrder("2019-03-14", "13:30",
                "18:30", "300min", "万达广场");
        workerOrderList.add(workerOrder4);
        WorkerOrder workerOrder5 = new WorkerOrder("2019-03-10", "12:30",
                "18:30", "360min", "新世界百货中心");
        workerOrderList.add(workerOrder5);
        WorkerOrder workerOrder6 = new WorkerOrder("2019-03-02", "13:30",
                "18:30", "300min", "新月花园");
        workerOrderList.add(workerOrder6);
        mWorkerOrderList.add(workerOrderList);

        List<WorkerOrder> workerOrderList2 = new ArrayList<>();
        workerOrder1 = new WorkerOrder("2019-02-25", "13:30",
                "18:30", "300min", "东南大学九龙湖校区");
        workerOrderList2.add(workerOrder1);
        workerOrder2 = new WorkerOrder("2019-02-25", "06:30",
                "12:00", "330min", "东南大学九龙湖校区");
        workerOrderList2.add(workerOrder2);
        workerOrder3 = new WorkerOrder("2019-02-15", "06:30",
                "13:30", "420min", "紫峰大厦");
        workerOrderList2.add(workerOrder3);
        workerOrder4 = new WorkerOrder("2019-02-14", "13:30",
                "18:30", "300min", "紫峰大厦");
        workerOrderList2.add(workerOrder4);
        workerOrder5 = new WorkerOrder("2019-02-10", "12:30",
                "18:30", "360min", "金逸国际");
        workerOrderList2.add(workerOrder5);
        workerOrder6 = new WorkerOrder("2019-02-02", "13:30",
                "18:30", "300min", "上汤国际展览中心");
        workerOrderList2.add(workerOrder6);
        mWorkerOrderList.add(workerOrderList2);
    }


}
