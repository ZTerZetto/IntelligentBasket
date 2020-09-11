package com.automation.zzx.intelligent_basket_demo.activity.common;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.automation.zzx.intelligent_basket_demo.R;
import com.automation.zzx.intelligent_basket_demo.activity.basket.BasketDetailActivity;
import com.automation.zzx.intelligent_basket_demo.activity.inspectionPerson.ConfigurationListActivity;
import com.automation.zzx.intelligent_basket_demo.activity.inspectionPerson.OutAndInStorageActivity;
import com.automation.zzx.intelligent_basket_demo.activity.inspectionPerson.SearchProjectActivity;
import com.automation.zzx.intelligent_basket_demo.adapter.common.UserMessageAdapter;
import com.automation.zzx.intelligent_basket_demo.entity.MessageInfo;
import com.automation.zzx.intelligent_basket_demo.entity.enums.WorkerType;
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

public class UserMessageActivity extends AppCompatActivity {

    // Message
    private final static int UPDATE_HISTORY_MESSAGE_INFO = 1;;

    // 列表
    private RecyclerView recyclerView;
    private UserMessageAdapter mgUserMessageAdapter;
    private List<MessageInfo> mMessageInfoList = new ArrayList<>();

    // 页面类型
    private String mUserMessageType;

    // 用户信息
    private String mUserId;

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what){
                case UPDATE_HISTORY_MESSAGE_INFO:
                    getHistoryMessageInfo();
                    mgUserMessageAdapter.notifyDataSetChanged();
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_message);

        if(!isHasPermission()) requestPermission();
        Intent intent = getIntent();
        mUserMessageType = intent.getStringExtra("user_type");
        mUserId = intent.getStringExtra("user_id");

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
        linearLayoutManager.setStackFromEnd(true);//列表在底部开始展示，反转后由上面开始展示
        linearLayoutManager.setReverseLayout(true);//列表翻转
        recyclerView.setLayoutManager(linearLayoutManager);
        mgUserMessageAdapter = new UserMessageAdapter(this,mMessageInfoList);
        mgUserMessageAdapter.setOnItemClickListener(new UserMessageAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                MessageInfo messageInfo = mMessageInfoList.get(position);
                Intent intent;
                switch(messageInfo.getmType()){
                    case "1": // 报警消息
                        intent = new Intent(UserMessageActivity.this, BasketDetailActivity.class);
                        intent.putExtra(SearchProjectActivity.PROJECT_ID, messageInfo.getmProjectId());  // 传入项目Id
                        intent.putExtra("basket_id", messageInfo.getmBasketId());
                        intent.putExtra("project_id",messageInfo.getmProjectId());
                        intent.putExtra("basket_state", "3");//3-进行中
                        startActivity(intent);
                        break;
                    case "5": // 配置清单
                        intent = new Intent(UserMessageActivity.this, ConfigurationListActivity.class);
                        intent.putExtra(SearchProjectActivity.PROJECT_ID, messageInfo.getmProjectId());  // 传入项目Id
                        startActivity(intent);
                        break;
                }

                // 更新页面与数据库
                if(!messageInfo.ismIsChecked()) {
                    // 更新页面
                    mMessageInfoList.get(position).setmIsChecked(true);
                    mgUserMessageAdapter.notifyDataSetChanged();
                    // 更新数据库
                    messageInfo.updateAll("mTime = ?", messageInfo.getmTime());
                }
            }
        });
        recyclerView.setAdapter(mgUserMessageAdapter);
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

        List<MessageInfo> messageInfos = new ArrayList<>();
        switch (mUserMessageType){
           /* case "worker_1":
            case "worker_2":
            case "worker_3":
            case "worker_4":
            case "worker":
                // 施工人员消息：报警
                messageInfos = DataSupport.where("mType = 1 or mType = 4")
                        .find(MessageInfo.class);
                break;*/
            case "inspect_person":
                // 巡检人员：配置清单
                messageInfos = DataSupport.where("mType = ?", "5")
                        .find(MessageInfo.class);
                break;
            default:
                if(mUserMessageType.contains("worker")||(WorkerType.getByDetailtype(mUserMessageType)!=null)){
//                   messageInfos = DataSupport.where("mType = 1 or mType = 4")
//                          .find(MessageInfo.class);
                    messageInfos = DataSupport.where(String.format("mType in (%s) and mWorkerList = ?","1,4"),
                            mUserId).find(MessageInfo.class);
                }
                break;
        }

        mMessageInfoList.clear();
        mMessageInfoList.addAll(messageInfos);
    }

    /*
        用xxpermissions申请权限
     */
    // 申请权限
    private void requestPermission() {
        XXPermissions.with(UserMessageActivity.this)
                .constantRequest() //可设置被拒绝后继续申请，直到用户授权或者永久拒绝
                .permission(Permission.Group.STORAGE) //支持请求6.0悬浮窗权限8.0请求安装权限
                .request(new OnPermission() {
                    @Override
                    public void hasPermission(List<String> granted, boolean isAll) {
                        if (isAll) {
                            //initCamera(scanPreview.getHolder());
                            onResume();
                        }else {
                            Toast.makeText(UserMessageActivity.this,
                                    "必须同意所有的权限才能使用本程序", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void noPermission(List<String> denied, boolean quick) {
                        if(quick) {
                            Toast.makeText(UserMessageActivity.this, "被永久拒绝授权，请手动授予权限",
                                    Toast.LENGTH_SHORT).show();
                            // 如果是被永久拒绝就跳转到应用权限系统设置页面
                            XXPermissions.gotoPermissionSettings(UserMessageActivity.this);
                        }else {
                            Toast.makeText(UserMessageActivity.this, "获取权限失败",
                                    Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    }
                });
    }

    // 是否有权限：摄像头、拨打电话
    private boolean isHasPermission() {
        if (XXPermissions.isHasPermission(UserMessageActivity.this, Permission.Group.STORAGE))
            return true;
        return false;
    }
}
