package com.automation.zzx.intelligent_basket_demo.activity.rentAdmin;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
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
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.automation.zzx.intelligent_basket_demo.R;
import com.automation.zzx.intelligent_basket_demo.activity.worker.WorkerHomePageActivity;
import com.automation.zzx.intelligent_basket_demo.adapter.rentAdmin.MgWorkerListAdapter;
import com.automation.zzx.intelligent_basket_demo.entity.MessageInfo;
import com.automation.zzx.intelligent_basket_demo.entity.MgWorkerInfo;
import com.automation.zzx.intelligent_basket_demo.entity.UserInfo;
import com.automation.zzx.intelligent_basket_demo.fragment.rentAdmin.RentAdminMessageFragment;
import com.automation.zzx.intelligent_basket_demo.utils.http.HttpUtil;
import com.automation.zzx.intelligent_basket_demo.utils.okhttp.BaseCallBack;
import com.automation.zzx.intelligent_basket_demo.utils.okhttp.BaseOkHttpClient;
import com.hjq.permissions.OnPermission;
import com.hjq.permissions.Permission;
import com.hjq.permissions.XXPermissions;
import com.scwang.smartrefresh.header.BezierCircleHeader;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import static com.automation.zzx.intelligent_basket_demo.entity.AppConfig.RENT_ADMIN_GET_ALL_WORKER_INFO;

public class AlarmMessageActivity extends AppCompatActivity {

    private final static String TAG = "AlarmMessage";

    // mHandler 消息列表
    private final static int MG_WORKER_LIST_MSG = 1; // 更新施工人员列表->视图更新

    // 控件声明
    private TextView mBasketIdTv;  // 吊篮编号
    private RecyclerView mWorkerRv;  // 工人列表
    private List<MgWorkerInfo> mgWorkerInfoList = new ArrayList<>();
    private MgWorkerListAdapter mgWorkerListAdapter;
    private TextView mAlarmContentTv; // 报警内容说明

    // 页面传递消息
    private MessageInfo mAlarmMessage;

    // 后台请求必要信息
    private String mToken;
    private SharedPreferences mPref;

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch(msg.what) {
                case MG_WORKER_LIST_MSG:
                    mgWorkerListAdapter.notifyDataSetChanged();
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm_message);

        mAlarmMessage = (MessageInfo) getIntent().getExtras().get(RentAdminMessageFragment.ALARM_MESSAGE_MSG);

        getUserToken();
        if(!isHasPermission()) requestPermission();
        initWidgetResource();  // 初始化控件
        initDisplayView();  // 页面加载
    }

    /*
     * 页面初始化
     */
    // 控件初始化
    private void initWidgetResource() {
        // 顶部导航栏
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        TextView titleText = (TextView) findViewById(R.id.toolbar_title);
        toolbar.setTitle("");
        titleText.setText("报警详情");
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true); //设置返回键可用

        // 控件
        mBasketIdTv = (TextView) findViewById(R.id.device_id_textview);
        mWorkerRv = (RecyclerView) findViewById(R.id.worker_recycler_view);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(AlarmMessageActivity.this);
        mWorkerRv.setLayoutManager(linearLayoutManager);
        mgWorkerListAdapter = new MgWorkerListAdapter(AlarmMessageActivity.this,
                                                        mgWorkerInfoList, false);
        mWorkerRv.setAdapter(mgWorkerListAdapter);
        mgWorkerListAdapter.setOnItemClickListener(new MgWorkerListAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                // 点击Item响应
                Log.i(TAG, "You have clicked the "+ position +" item");
                Intent intent = new Intent(AlarmMessageActivity.this, WorkerHomePageActivity.class);
                intent.putExtra("worker_id", mgWorkerInfoList.get(position).getId());
                startActivity(intent);
            }

            @Override
            public void onCheckChanged(View view, int position, boolean checked) {
            }

            @Override
            public void onPhoneCallClick(View view, int position) {
                Log.i(TAG, "You have clicked warning button");
                // 检查权限
                if(!isHasPermission()) requestPermission();
                // 点击拨号响应
                Intent intent;
                intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:"+ mgWorkerInfoList.get(position).getPhone()));
                startActivity(intent);
            }
        });

        mAlarmContentTv = (TextView) findViewById(R.id.alarm_content_textview);
    }

    // 加载页面数据
    private void initDisplayView(){
        mBasketIdTv.setText(mAlarmMessage.getmBasketId());
        mAlarmContentTv.setText(mAlarmMessage.getmDescription());

        String[] workerIds = mAlarmMessage.getmWorkerList().split(",");
        for(int i=0; i<workerIds.length; i++){
            getWorkerInfo(workerIds[i]);
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /*
     * 消息响应
     */
    // 顶部导航栏消息响应
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home: // 返回按钮
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /*
     * 网络相关
     */
    // 获取施工人员信息
    private void getWorkerInfo(String workerId){
        HttpUtil.getWorkerAllInfoOkHttpRequest(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d(TAG, e.toString());
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.code() == 200) {
                    Log.d(TAG, "Http Server Success");
                    String data = response.body().string();
                    parseUserInfoFromInternet(data);
                }else{
                    Log.d(TAG, "Http Server Error " + response.code());

                }
            }
        }, mToken, workerId);
    }
    // 解析后台返回数据
    private void parseUserInfoFromInternet(String data){
        Log.d(TAG, "parse data:" + data);
        JSONObject jsonObject = JSON.parseObject(data);
        String userInfo = jsonObject.getString("userInfo");
        UserInfo mWorkerInfo = JSON.parseObject(userInfo, UserInfo.class);
        MgWorkerInfo mgWorkerInfo = new MgWorkerInfo();
        mgWorkerInfo.setId(mWorkerInfo.getUserId());
        mgWorkerInfo.setName(mWorkerInfo.getUserName());
        mgWorkerInfo.setPhone(mWorkerInfo.getUserPhone());
        mgWorkerInfoList.add(mgWorkerInfo);
        mHandler.sendEmptyMessage(MG_WORKER_LIST_MSG);  // 更新人员信息状态
    }

    /*
     * 解析用户信息
     */
    // 获取用户数据
    private void getUserToken(){
        // 从本地获取数据
        mPref = PreferenceManager.getDefaultSharedPreferences(this);
        mToken = mPref.getString("loginToken","");
    }

    /*
        用xxpermissions申请权限
     */
    // 申请权限
    private void requestPermission() {
        XXPermissions.with(AlarmMessageActivity.this)
                .constantRequest() //可设置被拒绝后继续申请，直到用户授权或者永久拒绝
                .permission(Permission.CAMERA) //支持请求6.0悬浮窗权限8.0请求安装权限
                .permission(Permission.CALL_PHONE) //支持请求6.0悬浮窗权限8.0请求安装权限
                .request(new OnPermission() {
                    @Override
                    public void hasPermission(List<String> granted, boolean isAll) {
                        if (isAll) {
                            onResume();
                        }else {
                            Toast.makeText(AlarmMessageActivity.this,
                                    "必须同意所有的权限才能使用本程序", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void noPermission(List<String> denied, boolean quick) {
                        if(quick) {
                            Toast.makeText(AlarmMessageActivity.this, "被永久拒绝授权，请手动授予权限",
                                    Toast.LENGTH_SHORT).show();
                            // 如果是被永久拒绝就跳转到应用权限系统设置页面
                            XXPermissions.gotoPermissionSettings(AlarmMessageActivity.this);
                        }else {
                            Toast.makeText(AlarmMessageActivity.this, "获取权限失败",
                                    Toast.LENGTH_SHORT).show();
                            AlarmMessageActivity.this.finish();
                        }
                    }
                });
    }

    // 是否有权限：摄像头、拨打电话
    private boolean isHasPermission() {
        if (XXPermissions.isHasPermission(AlarmMessageActivity.this, Permission.CAMERA)
                && XXPermissions.isHasPermission(AlarmMessageActivity.this, Permission.CALL_PHONE))
            return true;
        return false;
    }


}
