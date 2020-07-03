package com.automation.zzx.intelligent_basket_demo.activity.worker;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.automation.zzx.intelligent_basket_demo.R;
import com.automation.zzx.intelligent_basket_demo.activity.basket.BasketDetailActivity;
import com.automation.zzx.intelligent_basket_demo.activity.loginRegist.LoginActivity;
import com.automation.zzx.intelligent_basket_demo.adapter.worker.WorkerOrderAdapter;
import com.automation.zzx.intelligent_basket_demo.entity.AppConfig;
import com.automation.zzx.intelligent_basket_demo.entity.UserInfo;
import com.automation.zzx.intelligent_basket_demo.entity.WorkerOrder;
import com.automation.zzx.intelligent_basket_demo.utils.ToastUtil;
import com.automation.zzx.intelligent_basket_demo.utils.okhttp.BaseCallBack;
import com.automation.zzx.intelligent_basket_demo.utils.okhttp.BaseOkHttpClient;
import com.automation.zzx.intelligent_basket_demo.widget.dialog.VerifyWorkDialog;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import okhttp3.Call;

/**
 * Created by pengchenghu on 2019/3/17.
 * Author Email: 15651851181@163.com
 * Describe: 施工人员工单查询页面
 * limits:
 */

public class WorkerOrderActivity extends AppCompatActivity {

    private final static String TAG = "WorkerOrderActivity";
    // Handler消息
    private final static int UPDATE_WORK_TIME_MSG = 101;

    private TextView mNearOrderTv; // 近
    private ImageView mFilterIv; // 筛选

    // RecyclerView相关变量
    private RecyclerView mWorkerOrderRv; // 工单列表
    private List<List<WorkerOrder>> mWorkerOrderList = new ArrayList<>(); // 内容
    private WorkerOrderAdapter mWorkerOrderAdapter; // 适配器

    // 用户信息
    private UserInfo mUserInfo;
    private String mToken;
    private SharedPreferences mPref;

    // mHandler 处理消息
    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case UPDATE_WORK_TIME_MSG:
                    mWorkerOrderAdapter.notifyDataSetChanged();
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_worker_order);

        getUserInfo();
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
//        initWorkOrders();
        mWorkerOrderAdapter = new WorkerOrderAdapter(this,mWorkerOrderList);
        mWorkerOrderRv.setAdapter(mWorkerOrderAdapter);

        // 获取工单时常
        getWorkerWorkTime();
    }

    /*
     * 顶部导航栏消息响应
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
     * 后台通信相关
     */
    public void getWorkerWorkTime(){
        BaseOkHttpClient.newBuilder()
                .addHeader("Authorization", mToken)
                .addParam("userId", mUserInfo.getUserId())
                .get()
                .url(AppConfig.WORKER_GET_WORE_TIME)
                .build()
                .enqueue(new BaseCallBack() {
                    @Override
                    public void onSuccess(Object o) {
                        Log.d(TAG, "成功获取工时信息");
                        JSONObject jsonObject = JSON.parseObject(o.toString());
                        boolean isAllowed = jsonObject.getBoolean("isAllowed");
                        if (isAllowed){
                            String workTimeInfo = jsonObject.getString("get");
                            parseWorkInfo(workTimeInfo);
                        }
                    }

                    @Override
                    public void onError(int code) {
                        Log.d(TAG, "获取工时信息错误，错误编码："+code);
                    }

                    @Override
                    public void onFailure(Call call, IOException e) {
                        Log.d(TAG, "获取工时信息失败");
                    }
                });
    }

    private void parseWorkInfo(String infos){
        if (infos==null || infos.equals("")){
            return;
        }

        mWorkerOrderList.clear();
        JSONArray jsonArray = JSON.parseArray(infos);
        List<WorkerOrder> tmpArray = new ArrayList<>();
        String pre_month = "";
        for (int i=jsonArray.size()-1; i >= 0; i--){
            JSONObject jsonObject = (JSONObject) jsonArray.get(i);
            String startTime = jsonObject.getString("startTime");
            String endTime = "--:--";
            int timeWork = jsonObject.getIntValue("timeWork");
            String strTimeWork = Integer.toString(timeWork) + "Min";
            String projectName = jsonObject.getString("projectName");
            WorkerOrder workerOrder = new WorkerOrder(startTime.substring(0,10), startTime.substring(11, 16),
                                                        endTime, strTimeWork, projectName);
            String cur_month = startTime.substring(5,7);
            if (pre_month.equals("")) {
                tmpArray.add(workerOrder);
                pre_month = cur_month;
            }
            else{
                if (pre_month.equals(cur_month)){
                    tmpArray.add(workerOrder);
                }else{
                    pre_month = cur_month;
                    List<WorkerOrder> workerOrders = new ArrayList<>();
                    workerOrders.addAll(tmpArray);
                    mWorkerOrderList.add(workerOrders);
                    tmpArray.clear();
                }
            }
        }
        mHandler.sendEmptyMessage(UPDATE_WORK_TIME_MSG);
    }

    /*
     * 获取本地信息
     */
    // 获取用户数据
    private void getUserInfo(){
        // 从本地获取数据
        mPref = PreferenceManager.getDefaultSharedPreferences(this);
        mUserInfo = new UserInfo();
        mUserInfo.setUserId(mPref.getString("userId", ""));
        mUserInfo.setUserPhone(mPref.getString("userPhone", ""));
        mUserInfo.setUserRole(mPref.getString("userRole", ""));
        mToken = mPref.getString("loginToken","");
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
