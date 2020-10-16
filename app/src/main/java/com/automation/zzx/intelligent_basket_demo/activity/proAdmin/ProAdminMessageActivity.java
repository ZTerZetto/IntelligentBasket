package com.automation.zzx.intelligent_basket_demo.activity.proAdmin;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.automation.zzx.intelligent_basket_demo.R;
import com.automation.zzx.intelligent_basket_demo.activity.common.RepairDetailActivity;
import com.automation.zzx.intelligent_basket_demo.adapter.areaAdmin.MgAreaMessageAdapter;
import com.automation.zzx.intelligent_basket_demo.entity.MessageInfo;
import com.hjq.permissions.OnPermission;
import com.hjq.permissions.Permission;
import com.hjq.permissions.XXPermissions;
import com.scwang.smartrefresh.header.BezierCircleHeader;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;

import org.litepal.crud.DataSupport;

import java.util.ArrayList;
import java.util.List;

public class ProAdminMessageActivity extends AppCompatActivity {

    // Message
    private final static int UPDATE_HISTORY_MESSAGE_INFO = 1;;

    // 页面间消息传递标识
    public final static String REPAIR_MESSAGE_MSG = "repair_message_msg";
    public final static String PROJECT_ID_MSG = "project_id";
    public final static String PROJECT_NAME_MSG = "project_name";
    public final static String BASKET_ID_MSG = "basket_id";
    public final static String REPAIR_DATE_MSG = "repair_date";

    private ImageView ivBack;
    private SmartRefreshLayout mSmartRefreshLayout; // 下拉刷新
    private MgAreaMessageAdapter mgAreaMessageAdapter;
    private List<MessageInfo> mMessageInfoList = new ArrayList<>();
    private RecyclerView recyclerView;


    @SuppressLint("HandlerLeak")
    public final Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what){
                case UPDATE_HISTORY_MESSAGE_INFO:
                    getHistoryMessageInfo();
                    mSmartRefreshLayout.finishRefresh(100);
                    break;
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_area_admin_message);
        // 顶部导航栏
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        TextView titleText = (TextView) findViewById(R.id.toolbar_title);
        toolbar.setTitle("");
        titleText.setText(getString(R.string.info_title));
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true); //设置返回键可用

        mSmartRefreshLayout = (SmartRefreshLayout) findViewById(R.id.smart_refresh_layout);
        mSmartRefreshLayout.setRefreshHeader(  //设置 Header 为 贝塞尔雷达 样式
                new BezierCircleHeader(ProAdminMessageActivity.this));
        mSmartRefreshLayout.setPrimaryColorsId(R.color.smart_loading_background_color);
        mSmartRefreshLayout.setOnRefreshListener(new OnRefreshListener() { // 添加下拉刷新监听
            @Override
            public void onRefresh(RefreshLayout refreshlayout) {
                mHandler.sendEmptyMessage(UPDATE_HISTORY_MESSAGE_INFO);
            }
        });
        recyclerView = findViewById(R.id.rv_message);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        mgAreaMessageAdapter = new MgAreaMessageAdapter(ProAdminMessageActivity.this,mMessageInfoList);
        // 点击消息列表item响应
        mgAreaMessageAdapter.setOnItemClickListener(new MgAreaMessageAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {

            }

            @Override
            public void onPicRead(View view, int position) {

            }

            @Override
            public void onDetail(View view, int position) {
                MessageInfo messageInfo = mMessageInfoList.get(position);
                // do something
                switch(messageInfo.getmType()){
                    case "4": // 报修消息
                        Intent intent = new Intent(ProAdminMessageActivity.this, RepairDetailActivity.class); // 跳转至维修详情页面
                        intent.putExtra(PROJECT_ID_MSG, messageInfo.getmProjectId());
                        intent.putExtra(PROJECT_NAME_MSG, messageInfo.getmProjectName());
                        intent.putExtra(BASKET_ID_MSG, messageInfo.getmBasketId());
                        intent.putExtra(REPAIR_DATE_MSG, messageInfo.getmTime());
                        startActivity(intent);
                        break;
                }
                // 更新页面与数据库
                if(!messageInfo.ismIsChecked()) {
                    // 更新页面
                    mMessageInfoList.get(position).setmIsChecked(true);
                    mgAreaMessageAdapter.notifyDataSetChanged();
                    // 更新数据库
                    messageInfo.updateAll("mTime = ?", messageInfo.getmTime());
                }
            }
        });
        recyclerView.setAdapter(mgAreaMessageAdapter);
        mHandler.sendEmptyMessage(UPDATE_HISTORY_MESSAGE_INFO);
    }


    /*
     * 获取历史消息
     */
    private void getHistoryMessageInfo(){
        if(!isHasPermission()) requestPermission();
        List<MessageInfo> messageInfos = DataSupport.where("mType = 3 or mType = 2")
                .find(MessageInfo.class);
        mMessageInfoList.clear();
        mMessageInfoList.addAll(messageInfos);
        mgAreaMessageAdapter.notifyDataSetChanged();
    }


    /*
        用xxpermissions申请权限
     */
    // 申请权限
    private void requestPermission() {
        XXPermissions.with(ProAdminMessageActivity.this)
                .constantRequest() //可设置被拒绝后继续申请，直到用户授权或者永久拒绝
                .permission(Permission.Group.STORAGE) //支持请求6.0悬浮窗权限8.0请求安装权限
                .request(new OnPermission() {
                    @Override
                    public void hasPermission(List<String> granted, boolean isAll) {
                        if (isAll) {
                            //initCamera(scanPreview.getHolder());
                            onResume();
                        }else {
                            Toast.makeText(ProAdminMessageActivity.this, "必须同意所有的权限才能使用本程序",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void noPermission(List<String> denied, boolean quick) {
                        if(quick) {
                            Toast.makeText(ProAdminMessageActivity.this, "被永久拒绝授权，请手动授予权限",
                                    Toast.LENGTH_SHORT).show();
                            // 如果是被永久拒绝就跳转到应用权限系统设置页面
                            XXPermissions.gotoPermissionSettings(ProAdminMessageActivity.this);
                        }else {
                            Toast.makeText(ProAdminMessageActivity.this, "获取权限失败",
                                    Toast.LENGTH_SHORT).show();
                            ProAdminMessageActivity.this.finish();
                        }
                    }
                });
    }



    // 是否有权限：摄像头、拨打电话
    private boolean isHasPermission() {
        if (XXPermissions.isHasPermission(ProAdminMessageActivity.this, Permission.Group.STORAGE))
            return true;
        return false;
    }
}
