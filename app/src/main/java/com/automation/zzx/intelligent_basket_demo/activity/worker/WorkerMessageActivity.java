package com.automation.zzx.intelligent_basket_demo.activity.worker;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.automation.zzx.intelligent_basket_demo.R;
import com.automation.zzx.intelligent_basket_demo.adapter.rentAdmin.MgRentMessageAdapter;
import com.automation.zzx.intelligent_basket_demo.adapter.worker.WorkerMessageAdapter;
import com.automation.zzx.intelligent_basket_demo.entity.MessageInfo;
import com.hjq.permissions.OnPermission;
import com.hjq.permissions.Permission;
import com.hjq.permissions.XXPermissions;

import org.litepal.crud.DataSupport;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by pengchenghu on 2019/3/17.
 * Author Email: 15651851181@163.com
 * Describe: 施工人员消息查询页面
 * limits:
 */

public class WorkerMessageActivity extends AppCompatActivity {

    // Message
    private final static int UPDATE_HISTORY_MESSAGE_INFO = 1;;

    // 列表
    private RecyclerView recyclerView;
    private WorkerMessageAdapter mgWorkerMessageAdapter;
    private List<MessageInfo> mMessageInfoList = new ArrayList<>();

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what){
                case UPDATE_HISTORY_MESSAGE_INFO:
                    getHistoryMessageInfo();
                    mgWorkerMessageAdapter.notifyDataSetChanged();
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_worker_message);

        if(!isHasPermission()) requestPermission();
        initWidgetResource();
    }

    /*
     * 初始化控件
     */
    private void initWidgetResource() {
        // 顶部导航栏
        android.support.v7.widget.Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        TextView titleText = (TextView) findViewById(R.id.toolbar_title);
        toolbar.setTitle("");
        titleText.setText("历史消息");
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true); //设置返回键可用

        recyclerView = (RecyclerView) findViewById(R.id.rv_message);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        mgWorkerMessageAdapter = new WorkerMessageAdapter(mMessageInfoList);
        recyclerView.setAdapter(mgWorkerMessageAdapter);
        mHandler.sendEmptyMessage(UPDATE_HISTORY_MESSAGE_INFO);
    }

    /*
     * d顶部导航栏消息响应
     */
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
     * 获取历史消息
     */
    private void getHistoryMessageInfo(){
        if(!isHasPermission()) requestPermission();
        List<MessageInfo> messageInfos = DataSupport.findAll(MessageInfo.class);
        mMessageInfoList.clear();
        mMessageInfoList.addAll(messageInfos);
    }

    /*
        用xxpermissions申请权限
     */
    // 申请权限
    private void requestPermission() {
        XXPermissions.with(WorkerMessageActivity.this)
                .constantRequest() //可设置被拒绝后继续申请，直到用户授权或者永久拒绝
                .permission(Permission.Group.STORAGE) //支持请求6.0悬浮窗权限8.0请求安装权限
                .request(new OnPermission() {
                    @Override
                    public void hasPermission(List<String> granted, boolean isAll) {
                        if (isAll) {
                            //initCamera(scanPreview.getHolder());
                            onResume();
                        }else {
                            Toast.makeText(WorkerMessageActivity.this,
                                    "必须同意所有的权限才能使用本程序", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void noPermission(List<String> denied, boolean quick) {
                        if(quick) {
                            Toast.makeText(WorkerMessageActivity.this, "被永久拒绝授权，请手动授予权限",
                                    Toast.LENGTH_SHORT).show();
                            // 如果是被永久拒绝就跳转到应用权限系统设置页面
                            XXPermissions.gotoPermissionSettings(WorkerMessageActivity.this);
                        }else {
                            Toast.makeText(WorkerMessageActivity.this, "获取权限失败",
                                    Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    }
                });
    }

    // 是否有权限：摄像头、拨打电话
    private boolean isHasPermission() {
        if (XXPermissions.isHasPermission(WorkerMessageActivity.this, Permission.Group.STORAGE))
            return true;
        return false;
    }
}
