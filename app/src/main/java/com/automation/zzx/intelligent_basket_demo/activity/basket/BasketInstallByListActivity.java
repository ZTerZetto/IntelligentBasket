package com.automation.zzx.intelligent_basket_demo.activity.basket;

import android.annotation.SuppressLint;
import android.app.Dialog;
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
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.automation.zzx.intelligent_basket_demo.R;
import com.automation.zzx.intelligent_basket_demo.activity.loginRegist.LoginActivity;
import com.automation.zzx.intelligent_basket_demo.adapter.areaAdmin.MgBasketStatementAdapter;
import com.automation.zzx.intelligent_basket_demo.adapter.rentAdmin.MgBasketListAdapter;
import com.automation.zzx.intelligent_basket_demo.entity.AppConfig;
import com.automation.zzx.intelligent_basket_demo.entity.MgBasketInfo;
import com.automation.zzx.intelligent_basket_demo.entity.MgBasketStatement;
import com.automation.zzx.intelligent_basket_demo.entity.UserInfo;
import com.automation.zzx.intelligent_basket_demo.utils.ToastUtil;
import com.automation.zzx.intelligent_basket_demo.utils.okhttp.BaseCallBack;
import com.automation.zzx.intelligent_basket_demo.utils.okhttp.BaseOkHttpClient;
import com.automation.zzx.intelligent_basket_demo.widget.dialog.CommonDialog;
import com.automation.zzx.intelligent_basket_demo.widget.zxing.activity.CaptureActivity;
import com.hjq.permissions.OnPermission;
import com.hjq.permissions.Permission;
import com.hjq.permissions.XXPermissions;
import com.scwang.smartrefresh.header.BezierCircleHeader;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import okhttp3.Call;

import static com.automation.zzx.intelligent_basket_demo.widget.zxing.activity.CaptureActivity.QR_CODE_RESULT;

public class BasketInstallByListActivity extends AppCompatActivity implements View.OnClickListener{

    private final static String TAG = "BasketInstallByList";

    // Handler 消息类型
    private final static int UPDATE_BASKET_STATEMENT_MSG = 101;  // 更新吊篮状态列表
    private final static int GET_BASKET_MSG =103;   // 获取指定项目的吊篮列表

    // intent 消息参数
    public final static String PROJECT_ID = "project_id";  // 上传图片的项目Id
    public final static String PROJECT_NAME = "projectName";  // 上传图片的项目名称
    public final static String BASKETS_NUM = "basketsNumber";  // 吊篮数目
    public final static String UPLOAD_IMAGE_TYPE  = "uploadImageType"; // 上传图片的类型
    public final static String UPLOAD_BASKETS_PRE_INSTALL_IMAGE = "basketsPreInstall"; // 预验收
    public final static String UPLOAD_CERTIFICATE_IMAGE = "certificate"; // 安监证书
    public final static String BASKET_ID = "basketId"; // 上传图片的吊篮ID


    //页面返回消息
    private final static int ADD_INSTALL_RESULT = 1;  // 获取安装人员信息

    /* 控件部分 */
    // 顶部导航栏
    private Toolbar toolbar;
    private TextView titleText;

    // 主体
    private SmartRefreshLayout mSmartRefreshLayout; // 下拉刷新
    private RecyclerView basketRv; // 吊篮列表
    private List<MgBasketInfo> mgBasketInfoList; //MgBasketStatement

    private MgBasketListAdapter mgBasketListAdapter;
    private RelativeLayout noBasketListRelativeLayout; // 空空如也
    private TextView noBasketListTextView;

    // 底部合计
    private CheckBox basketAllSelected;  // 全选复选框
    private TextView basketNumber;  // 已选择吊篮个数
    private TextView basketApplyStop; // 吊篮预报停

    /* 数据部分*/
    // 用户登录信息相关
    private UserInfo mUserInfo; //用户信息
    private String mProjectId;  //項目ID
    private String mProjectName;  //項目ID
    private String mToken;
    private SharedPreferences mPref;
    private List<MgBasketStatement> mgBasketStatementList;

    //其他数据
    private String mBasketIdList; //选中的吊篮id集合（以,分割）

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            switch(msg.what) {
                case UPDATE_BASKET_STATEMENT_MSG:  // 吊篮列表页面更新
                   /* mgBasketInfoList.clear();
                    mgBasketInfoList.addAll(parseBasketListInfo((String) msg.obj));
                    mgBasketListAdapter.notifyDataSetChanged();
                    updateContentView();*/
                    break;
                case GET_BASKET_MSG: // 获取吊篮列表信息
                    mSmartRefreshLayout.finishRefresh();
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_basket_install_by_list);

        if(!isHasPermission()) requestPermission(); //开启摄像头权限

        initInfo();
        initWidget();
        updateBodyContentView();


    }
    /*
     * 解析用户信息
     * 解析用户信息
     */
    // 获取用户数据
    private void initInfo(){
        // 从本地获取数据
        mPref = PreferenceManager.getDefaultSharedPreferences(this);
        mUserInfo = new UserInfo();
        mUserInfo.setUserId(mPref.getString("userId", ""));
        mUserInfo.setUserPhone(mPref.getString("userPhone", ""));
        mUserInfo.setUserRole(mPref.getString("userRole", ""));
        mUserInfo.setUserName(mPref.getString("userName", ""));
        mToken = mPref.getString("loginToken","");
        mProjectId = mPref.getString("projectId","");

        Intent intent =getIntent();
        mProjectId = intent.getStringExtra(PROJECT_ID);
        mProjectName = intent.getStringExtra(PROJECT_NAME);
        mgBasketStatementList = (List<MgBasketStatement>) getIntent().getSerializableExtra("basket_list");

    }

    /*
     * 初始化页面
     * */
    //初始化句柄
    private void initWidget() {
        // 顶部导航栏
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        titleText = (TextView) findViewById(R.id.toolbar_title);
        toolbar.setTitle("");
        titleText.setText(mProjectName);
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true); //设置返回键可用

        // 下拉刷新
        mSmartRefreshLayout = (SmartRefreshLayout) findViewById(R.id.smart_refresh_layout);
        mSmartRefreshLayout.setRefreshHeader(  //设置 Header 为 贝塞尔雷达 样式
                new BezierCircleHeader(this));
        mSmartRefreshLayout.setPrimaryColorsId(R.color.smart_loading_background_color);
        mSmartRefreshLayout.setOnRefreshListener(new OnRefreshListener() { // 添加下拉刷新监听
            @Override
            public void onRefresh(RefreshLayout refreshlayout) {
                handler.sendEmptyMessage(GET_BASKET_MSG);
            }
        });

        // 初始化吊篮列表
        basketRv = (RecyclerView) findViewById(R.id.basket_recycler_view);
        mgBasketInfoList = new ArrayList<>();
        LinearLayoutManager layoutManager = new LinearLayoutManager(BasketInstallByListActivity.this);
        basketRv.setLayoutManager(layoutManager);
        mgBasketListAdapter = new MgBasketListAdapter(this, mgBasketInfoList);
        basketRv.setAdapter(mgBasketListAdapter);
        mgBasketListAdapter.setOnItemClickListener(new MgBasketListAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                // item 点击响应
                Log.i(TAG, "You have clicked the "+ position +" item");if(mBasketIdList==null || mBasketIdList.equals("")){
                    DialogToast("提示", "该吊篮信息有误，无法分配安装队伍");
                }
                startActivityForResult(new Intent(BasketInstallByListActivity.this, CaptureActivity.class), ADD_INSTALL_RESULT);
               /* Intent intent = new Intent(BasketInstallByListActivity.this, BasketDetailActivity.class);
                intent.putExtra("project_id",mProjectId);
                intent.putExtra("basket_id", mgBasketInfoList.get(position).getId());
                intent.putExtra("principal_name", mgBasketInfoList.get(position).getPrincipal());
                startActivity(intent);*/
            }

            @Override
            public void onCheckChanged(View view, int position, boolean checked) {
                // checkbox 状态更换
                Log.i(TAG, "You have changed the "+ position +" item checkbox");
                int basketNumberSelected = mgBasketListAdapter.checkedBasket();
                basketNumber.setText(String.valueOf(basketNumberSelected));
                basketAllSelected.setChecked(basketNumberSelected == mgBasketInfoList.size());
            }
        });
        // 空空如也
        noBasketListRelativeLayout = (RelativeLayout) findViewById(R.id.basket_no_avaliable);
        noBasketListTextView = (TextView) findViewById(R.id.no_basket_hint);

        // 底部合计
        // 控件初始化
        basketAllSelected = (CheckBox) findViewById(R.id.basket_all_checkbox);
        basketAllSelected.setChecked(false);
        basketNumber = (TextView) findViewById(R.id.basket_number);
        basketApplyStop = (TextView) findViewById(R.id.basket_apply_stop);

        // 消息监听
        // 全选按钮
        basketAllSelected.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Map<Integer,Boolean> isCheck = mgBasketListAdapter.getMap();
                if(!isChecked){  // 规避减一个checkbox导致取消全选的问题
                    if(isCheck.size() != mgBasketListAdapter.checkedBasket())
                        return;
                }
                mgBasketListAdapter.initCheck(isChecked);
                mgBasketListAdapter.notifyDataSetChanged();
            }
        });

        // 报停按钮点击
        basketApplyStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


            }
        });

    }

    /*
     * UI 更新类
     */
    // 主体页面显示逻辑控制
    public void updateBodyContentView() {
        //有无吊篮显示
        if (mgBasketInfoList.size() < 1) { // 暂无吊篮
            basketRv.setVisibility(View.GONE);
            noBasketListRelativeLayout.setVisibility(View.VISIBLE);
            noBasketListTextView.setText("您还没有相关的吊篮");
        } else {  // 好多吊篮
            noBasketListRelativeLayout.setVisibility(View.GONE);
            basketRv.setVisibility(View.VISIBLE);
        }
    }

    /*
     * 处理Activity返回结果
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case ADD_INSTALL_RESULT:  // 添加安装队伍
                if (resultCode == RESULT_OK) {
                    String userInfo = data.getStringExtra(QR_CODE_RESULT);
                    int colon = userInfo.indexOf(":");
                    String installId = userInfo.substring(colon+1);
                    if(userInfo.contains("InstallTeam")){  // 是安装队伍Id
                        addInstallWithBasket(installId,mBasketIdList);
                    }else // 二维码有误
                        DialogToast("错误", "此非安装人员身份二维码，请核实后重新扫描").show();
                }
                break;
            default:
                break;
        }
    }


    // 顶部导航栏消息响应
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home: // 返回按钮
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onClick(View v) {

    }

    // 添加施工人员
    private void addInstallWithBasket(String installId,String mBasketIdList){
        BaseOkHttpClient.newBuilder()
                .addHeader("Authorization", mToken)
                .addParam("projectId", mProjectId)
                .addParam("userId", installId)
                .addParam("deviceList", mBasketIdList)
                .post()
                .url(AppConfig.CREATE_INSTALL_INFO)
                .build()
                .enqueue(new BaseCallBack() {
                    @Override
                    public void onSuccess(Object o) {
                        String response = o.toString();
                        JSONObject jsonObject = JSON.parseObject(response);
                        String create = jsonObject.getString("create");
                        Boolean isLogin = jsonObject.getBoolean("isLogin");
                        if(isLogin.equals(true)){
                            if(create.contains("成功")) {
                                Log.i(TAG, "分配安装队伍成功");
                                DialogToast("提示", "您已成功分配安装队伍").show();
                            }else if(create.contains("存在项目中")){
                                DialogToast("提示", "分配安装队伍失败").show();
                            }
                        }else{
                            DialogToast("提示", "权限认证失败,分配安装队伍失败").show();
                        }
                    }

                    @Override
                    public void onError(int code) {
                        switch(code){
                            case 401:
                                ToastUtil.showToastTips(BasketInstallByListActivity.this, "登陆已过期，请重新登录");
                                startActivity(new Intent(BasketInstallByListActivity.this, LoginActivity.class));
                                BasketInstallByListActivity.this.finish();
                                break;
                            case 403:
                                break;
                        }
                    }

                    @Override
                    public void onFailure(Call call, IOException e) {

                    }
                });
    }
    /*
     * 用xxpermissions申请权限
     */
    // 申请权限
    private void requestPermission() {
        XXPermissions.with(BasketInstallByListActivity.this)
                .constantRequest() //可设置被拒绝后继续申请，直到用户授权或者永久拒绝
                .permission(Permission.CAMERA) //支持请求6.0悬浮窗权限8.0请求安装权限
                .request(new OnPermission() {
                    @Override
                    public void hasPermission(List<String> granted, boolean isAll) {
                        if (isAll) {
                            onResume();
                        }else {
                            Toast.makeText(BasketInstallByListActivity.this,
                                    "必须同意所有的权限才能使用本程序", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void noPermission(List<String> denied, boolean quick) {
                        if(quick) {
                            Toast.makeText(BasketInstallByListActivity.this, "被永久拒绝授权，请手动授予权限",
                                    Toast.LENGTH_SHORT).show();
                            // 如果是被永久拒绝就跳转到应用权限系统设置页面
                            XXPermissions.gotoPermissionSettings(BasketInstallByListActivity.this);
                        }else {
                            Toast.makeText(BasketInstallByListActivity.this, "获取权限失败",
                                    Toast.LENGTH_SHORT).show();
                            BasketInstallByListActivity.this.finish();
                        }
                    }
                });
    }
    // 是否有权限：摄像头
    private boolean isHasPermission() {
        if (XXPermissions.isHasPermission(BasketInstallByListActivity.this, Permission.CAMERA))
            return true;
        return false;
    }

    /*
     * 提示弹框
     */
    private CommonDialog DialogToast(String mTitle, String mMsg){
        return new CommonDialog(BasketInstallByListActivity.this, R.style.dialog, mMsg,
                new CommonDialog.OnCloseListener() {
                    @Override
                    public void onClick(Dialog dialog, boolean confirm) {
                        if(confirm){
                            dialog.dismiss();
                        }else{
                            dialog.dismiss();
                        }
                    }
                }).setTitle(mTitle);
    }
}
