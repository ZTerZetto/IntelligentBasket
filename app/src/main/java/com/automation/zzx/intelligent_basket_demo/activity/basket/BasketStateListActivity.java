package com.automation.zzx.intelligent_basket_demo.activity.basket;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.automation.zzx.intelligent_basket_demo.R;
import com.automation.zzx.intelligent_basket_demo.activity.areaAdmin.AreaAdminPrimaryActivity;
import com.automation.zzx.intelligent_basket_demo.activity.areaAdmin.AreaAdminPrimaryTRYActivity;
import com.automation.zzx.intelligent_basket_demo.activity.common.UploadImageFTPActivity;
import com.automation.zzx.intelligent_basket_demo.activity.loginRegist.LoginActivity;
import com.automation.zzx.intelligent_basket_demo.adapter.areaAdmin.MgBasketStatementAdapter;
import com.automation.zzx.intelligent_basket_demo.adapter.areaAdmin.MgStateAdapter;
import com.automation.zzx.intelligent_basket_demo.entity.MgBasketStatement;
import com.automation.zzx.intelligent_basket_demo.entity.ProjectInfo;
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
import java.util.Iterator;
import java.util.List;

import okhttp3.Call;

import static com.automation.zzx.intelligent_basket_demo.entity.AppConfig.AREA_ADMIN_ADD_BASKET_INTO_PROJECT;
import static com.automation.zzx.intelligent_basket_demo.entity.AppConfig.AREA_ADMIN_GET_ALL_BASKET_INFO;
import static com.automation.zzx.intelligent_basket_demo.widget.zxing.activity.CaptureActivity.QR_CODE_RESULT;

public class BasketStateListActivity extends AppCompatActivity {

    private final static String TAG = "BasketStateListActivity";

    // Handler 消息类型
    private final static int UPDATE_BASKET_STATEMENT_MSG = 101;  // 更新吊篮状态列表
    private final static int GET_BASKET_MSG =102;   // 获取指定项目的吊篮列表
    private final static int UPDATE_PROJECT_LIST_FROM_INTERNET_MSG = 103; // 从网络重新获取指定项目的吊篮信息

    // intent 消息参数
    public final static String PROJECT_ID = "projectId";  // 上传图片的项目Id
    public final static String PROJECT_NAME = "projectName";  // 上传图片的项目名称
    public final static String BASKETS_NUM = "basketsNumber";  // 吊篮数目
    public final static String UPLOAD_IMAGE_TYPE  = "uploadImageType"; // 上传图片的类型
    public final static String UPLOAD_BASKETS_PRE_INSTALL_IMAGE = "basketsPreInstall"; // 预验收
    public final static String UPLOAD_CERTIFICATE_IMAGE = "certificate"; // 安监证书
    public final static String BASKET_ID = "basketId"; // 上传图片的吊篮ID


    //页面返回消息
    private final static int CAPTURE_ACTIVITY_RESULT = 1;  // 扫码返回
    private final static int UPLOAD_BASKET_IMAGE_RESULT = 2;  // 上传预验收图片
    private final static int UPLOAD_CERTIFICATE_IMAGE_RESULT = 3;  // 上传安监证书页面

    /* 控件部分 */
    // 顶部导航栏
    private Toolbar toolbar;
    private TextView titleText;

    // 吊篮状态选择栏
    private GridView mBasketStateGv; // 吊篮状态
    private List<String> mStateLists; // 状态名称
    private MgStateAdapter mgStateAdapter; //适配器
    private int pre_selectedPosition = 0;

    /* 主体内容部分*/
    private SmartRefreshLayout mSmartRefreshLayout; // 下拉刷新

    // 吊篮列表视图
    private RelativeLayout mListRelativeLayout;
    private RecyclerView mBasketListRecyclerView;
    private List<MgBasketStatement> mgBasketStatementList;
    private List<List<MgBasketStatement>> mgBasketStatementClassifiedList;
    private MgBasketStatementAdapter mgBasketStatementAdapter;

    // 悬浮按钮
    private ImageView mAddBasketImageView; // 添加吊篮

    // 无吊篮或项目
    private RelativeLayout mBlankRelativeLayout;
    private TextView mBlankHintTextView;


    /* 数据部分*/
    // 用户登录信息相关
    private UserInfo mUserInfo; //用户信息
    private String mProjectId;  //項目ID
    private String mProjectName;  //項目ID
    private String mToken;
    private SharedPreferences mPref;



    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case UPDATE_BASKET_STATEMENT_MSG:  // 更新吊篮列表
                    mgBasketStatementList.clear();
                    mgBasketStatementList.addAll(mgBasketStatementClassifiedList.get(pre_selectedPosition+1));
                    updateBodyContentView();
                    break;
                case GET_BASKET_MSG:  // 更新吊篮列表
                    mgBasketStatementList.clear();
                    mgBasketStatementClassifiedList.clear();
                    parseBasketListInfo((String)msg.obj);  // 更新吊篮
                    mgStateAdapter.setSelectedPosition(pre_selectedPosition);
                    sendEmptyMessage(UPDATE_BASKET_STATEMENT_MSG);
                    break;
                case UPDATE_PROJECT_LIST_FROM_INTERNET_MSG: // 从网络重新获取指定项目的吊篮信息
                    areaAdminGetAllBasket();
                default:
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_basket_state_list);

        if(!isHasPermission()) requestPermission(); //开启摄像头权限

        initInfo();
        initWidget();
        areaAdminGetAllBasket();


       /*  //模糊搜索筛选吊篮-依据吊篮编号
       btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text = edtSearch.getText().toString();
                if (text.isEmpty()) {
                    showList(basketInfoArrayList);
                } else {
                    List<BasketInfo> arrayList = new ArrayList<>();
                    for (int i = 0; i < basketInfoArrayList.size(); i++) {
                        BasketInfo basketInfo = basketInfoArrayList.get(i);
                        if (basketInfo.getBasketId().equals(text)) {
                            arrayList.add(basketInfo);
                        }
                    }
                    if (arrayList.isEmpty()) {
                        mLv.setVisibility(View.GONE);
                        txtResult.setVisibility(View.VISIBLE);
                    } else {
                        basketInfoArrayList.clear();
                        basketInfoArrayList = arrayList;
                        showList(basketInfoArrayList);
                    }
                }
            }
        });*/

    }

    /*
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

    }


    /*
     * 初始化页面
     * */
    //初始化句柄
    private void initWidget(){
        // 顶部导航栏
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        titleText = (TextView) findViewById(R.id.toolbar_title);
        toolbar.setTitle("");
        titleText.setText(mProjectName);
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true); //设置返回键可用

         /*
         主体内容部分
          */
        // 下拉刷新
        mSmartRefreshLayout = (SmartRefreshLayout) findViewById(R.id.smart_refresh_layout);
        mSmartRefreshLayout.setRefreshHeader(  //设置 Header 为 贝塞尔雷达 样式
                new BezierCircleHeader(this));
        mSmartRefreshLayout.setPrimaryColorsId(R.color.smart_loading_background_color);
        mSmartRefreshLayout.setOnRefreshListener(new OnRefreshListener() { // 添加下拉刷新监听
            @Override
            public void onRefresh(RefreshLayout refreshlayout) {
                //TODO 重新获取吊篮列表
                if (mProjectId != null) {
                    areaAdminGetAllBasket();
                } else {
                    mSmartRefreshLayout.finishRefresh();
                }
            }
        });

        //初始化菜单栏
        mBasketStateGv = (GridView) findViewById(R.id.mg_basket_state);
        mStateLists = new ArrayList<>();
        initStateList();
        mgStateAdapter = new MgStateAdapter(BasketStateListActivity.this, R.layout.item_basket_state_switch, mStateLists);
        mgStateAdapter.setSelectedPosition(pre_selectedPosition);
        mBasketStateGv.setAdapter(mgStateAdapter);

        // 消息响应
        mBasketStateGv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                pre_selectedPosition = position;
                mgStateAdapter.setSelectedPosition(pre_selectedPosition);
                if(mProjectId != null)  // 当且仅当存在项目时更新吊篮状态列表
                    mHandler.sendEmptyMessage(UPDATE_BASKET_STATEMENT_MSG);  // 更新列表
            }
        });

        /*
         主体内容部分
          */
        // 下拉刷新
        mSmartRefreshLayout = (SmartRefreshLayout) findViewById(R.id.smart_refresh_layout);
        mSmartRefreshLayout.setRefreshHeader(  //设置 Header 为 贝塞尔雷达 样式
                new BezierCircleHeader(BasketStateListActivity.this));
        mSmartRefreshLayout.setPrimaryColorsId(R.color.smart_loading_background_color);
        mSmartRefreshLayout.setOnRefreshListener(new OnRefreshListener() { // 添加下拉刷新监听
            @Override
            public void onRefresh(RefreshLayout refreshlayout) {
                if(mProjectId != null){
                    //更新吊篮列表UI
                }else{
                    mSmartRefreshLayout.finishRefresh();
                }
            }
        });

        // 吊篮列表
        mListRelativeLayout = (RelativeLayout) findViewById(R.id.basket_avaliable);
        mBasketListRecyclerView = (RecyclerView) findViewById(R.id.basket_recycler_view);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        mBasketListRecyclerView.setLayoutManager(layoutManager);
        mgBasketStatementList = new ArrayList<>();
        mgBasketStatementClassifiedList = new ArrayList<>();
        mgBasketStatementAdapter = new MgBasketStatementAdapter(BasketStateListActivity.this, mgBasketStatementList);
        mBasketListRecyclerView.setAdapter(mgBasketStatementAdapter);
        mgBasketStatementAdapter.setOnItemClickListener(new MgBasketStatementAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                // 点击item响应
                Log.i(TAG, "You have clicked the "+position+" item");
                // 跳转至吊篮详情页面
                Intent intent = new Intent(BasketStateListActivity.this, BasketDetailActivity.class);
                intent.putExtra("project_id",mProjectId);
                intent.putExtra("basket_id", mgBasketStatementList.get(position).getBasketId());
                startActivity(intent);
            }

            //上传安检证书
            @Override
            public void onUploadCertClick(View view, int position) {
                // 点击安监证书
                Log.i(TAG, "You have clicked the "+ position+" item's PreAssAndAccept");
                Intent intent;
                intent = new Intent(BasketStateListActivity.this, UploadImageFTPActivity.class);
                intent.putExtra(PROJECT_ID, mProjectId);
                intent.putExtra(AreaAdminPrimaryTRYActivity.BASKET_ID, mgBasketStatementList.get(position).getBasketId());
                intent.putExtra(AreaAdminPrimaryTRYActivity.UPLOAD_IMAGE_TYPE, AreaAdminPrimaryTRYActivity.UPLOAD_CERTIFICATE_IMAGE);
                startActivityForResult(intent, UPLOAD_CERTIFICATE_IMAGE_RESULT);
            }

            @Override
            public void onPreApplyStopClick(View view, int position) {
                // 点击预报停申请
                Log.i(TAG, "You have clicked the "+ position +" item's PreApplyStop");
            }
        });

        // 无吊篮提示信息
        mBlankRelativeLayout = (RelativeLayout) findViewById(R.id.basket_no_avaliable);
        mBlankHintTextView = (TextView) findViewById(R.id.no_basket_hint);

        /*
         * 悬浮框
         */
        mAddBasketImageView = (ImageView) findViewById(R.id.basket_add_image_view);
        mAddBasketImageView.setOnClickListener(new View.OnClickListener() {  // 点击响应
            @Override
            public void onClick(View v) {
                if(mProjectId == null)
                    DialogToast("错误", "您尚无授权的项目");
                else {
                    if(!isHasPermission()) requestPermission();
                    startActivityForResult(new Intent(BasketStateListActivity.this, CaptureActivity.class),
                            CAPTURE_ACTIVITY_RESULT);
                }
            }
        });

    }


    /*
     * 初始化
     */
    //状态筛选栏
    private void initStateList(){
        mStateLists.add("待安装");
        mStateLists.add("安装审核");
        mStateLists.add("使用中");
        mStateLists.add("待报停");
        mStateLists.add("报停审核");
    }


    /*
    * 网络请求
    * */
    // 获取项目对应的所有吊篮信息
    private void areaAdminGetAllBasket(){
        BaseOkHttpClient.newBuilder()
                .addHeader("Authorization", mToken)
                .addParam("projectId", mProjectId)
                .get()
                .url(AREA_ADMIN_GET_ALL_BASKET_INFO)
                .build()
                .enqueue(new BaseCallBack() {
                    @Override
                    public void onSuccess(Object o) {
                        Log.i(TAG, "成功" );
                        String responseData = o.toString();
                        Message message = new Message();
                        message.what = GET_BASKET_MSG;
                        message.obj = responseData;
                        mHandler.sendMessage(message);
                        mSmartRefreshLayout.finishRefresh(100);
                    }

                    @Override
                    public void onError(int code) {
                        Log.i(TAG, "错误：" + code);
                        switch (code){
                            case 401: // 未授权
                                ToastUtil.showToastTips(BasketStateListActivity.this, "登录已过期，请重新登陆");
                                startActivity(new Intent(BasketStateListActivity.this, LoginActivity.class));
                                BasketStateListActivity.this.finish();
                                break;
                            case 403: // 禁止
                                break;
                            case 404: // 404
                                break;
                        }
                    }

                    @Override
                    public void onFailure(Call call, IOException e) {
                        Log.i(TAG, "失败：" + e.toString());
                    }
                });
    }


    // 解析项目中的吊篮列表信息
    private void parseBasketListInfo(String responseDate){
        JSONObject jsonObject = JSON.parseObject(responseDate);
        Iterator<String> iterator = jsonObject.keySet().iterator();  // 迭代获取吊篮信息
        while(iterator.hasNext()) {
            String key = iterator.next();
            if(!key.contains("storage")) continue;
            String value = jsonObject.getString(key);
            if(value==null || value.equals("")) continue;
            JSONObject basketObj = JSON.parseObject(value);
            String deviceId = basketObj.getString("deviceId");
            if(deviceId==null || deviceId.equals("")) continue;
            mgBasketStatementList.add(new MgBasketStatement(basketObj.getString("deviceId"),
                    null, basketObj.getString("storageState")));
        }
        parseMgBasketStatementList(mgBasketStatementList);
    }
    // 解析吊篮状态
    private void parseMgBasketStatementList(List<MgBasketStatement> mgBasketStatements){
        // 初始化吊篮分类列表
        for(int i=0; i<=mStateLists.size();i++){
            mgBasketStatementClassifiedList.add(new ArrayList<MgBasketStatement>());
        }
        // 将数据装载进对应位置
        for(int i=0; i<mgBasketStatements.size(); i++){
            MgBasketStatement mgBasketStatement = mgBasketStatements.get(i);
            mgBasketStatementClassifiedList.get(Integer.valueOf(mgBasketStatement.getBasketStatement().substring(0,1))).
                    add(mgBasketStatement);
        }
        mgBasketStatementClassifiedList.get(0).addAll(mgBasketStatements);
    }


    // 将吊篮添加至项目
    private void areaAdminAddBasketIntoProject(String basketId){
        BaseOkHttpClient.newBuilder()
                .addHeader("Authorization", mToken)
                .addParam("projectId", mProjectId)
                .addParam("boxId", basketId)
                .post()
                .url(AREA_ADMIN_ADD_BASKET_INTO_PROJECT)
                .build()
                .enqueue(new BaseCallBack() {
                    @Override
                    public void onSuccess(Object o) {
                        JSONObject jsonObject = JSON.parseObject(o.toString());
                        String isIncrease = jsonObject.getString("increase");
                        if(isIncrease.contains("失败")){
                            Log.i(TAG, "新增吊篮失败");
                            DialogToast("提示", "该吊篮已存在于其他项目中！").show();
                        }else{
                            Log.i(TAG, "添加吊篮成功");
                            DialogToast("提示", "您已成功添加该吊篮！").show();
                            mHandler.sendEmptyMessage(UPDATE_PROJECT_LIST_FROM_INTERNET_MSG);
                        }
                    }

                    @Override
                    public void onError(int code) {
                        Log.i(TAG, "添加吊篮错误：" + code);
                        switch(code){
                            case 401:
                                ToastUtil.showToastTips(BasketStateListActivity.this, "登陆已过期，请重新登录");
                                startActivity(new Intent(BasketStateListActivity.this, LoginActivity.class));
                                BasketStateListActivity.this.finish();
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
     * UI 更新类
     */
    // 主体页面显示逻辑控制
    public void updateBodyContentView(){
        if (mgBasketStatementList.size() == 0) {  // 显示无操作吊篮
            mListRelativeLayout.setVisibility(View.GONE);
            mBlankRelativeLayout.setVisibility(View.VISIBLE);
            mBlankHintTextView.setText("暂无相关吊篮");
        } else {                                   // 显示可操作吊篮列表
            mBlankRelativeLayout.setVisibility(View.GONE);
            mListRelativeLayout.setVisibility(View.VISIBLE);
            mgBasketStatementAdapter.notifyDataSetChanged();
        }
        // 添加吊篮按钮
        if(pre_selectedPosition == 0)
            mAddBasketImageView.setVisibility(View.VISIBLE);
        else
            mAddBasketImageView.setVisibility(View.GONE);
    }

    /*
     * 处理Activity返回结果
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case CAPTURE_ACTIVITY_RESULT:  // 扫描二维码名片返回结果
                if (resultCode == RESULT_OK) {
                    String basketId = data.getStringExtra(QR_CODE_RESULT);
                    Log.i(TAG, "QR_Content: " + basketId);
                    if (isBasketInProject(basketId))  // 已存在于项目中
                        DialogToast("提示", "吊篮已经在项目中").show();
                    else  // 待添加
                        areaAdminAddBasketIntoProject(basketId);
                }
                break;
            case UPLOAD_BASKET_IMAGE_RESULT:  // 上传预验收图片返回
                if (resultCode == RESULT_OK) {
                    // TODO 隐藏该吊篮的预验收按钮
                }
                break;
            case UPLOAD_CERTIFICATE_IMAGE_RESULT:  // 上传安监证书返回值
                break;

            default:
                break;
        }
    }

    /*
     * 业务逻辑相关
     */
    // 判断扫描到的吊篮是否在项目中
    private boolean isBasketInProject(String basketId) {
        for (int i = 0; i < mgBasketStatementList.size(); i++) {
            if (basketId.equals(mgBasketStatementList.get(i).getBasketId()))
                return true;
        }
        return false;
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

    /*
     * 用xxpermissions申请权限
     */
    // 申请权限
    private void requestPermission() {
        XXPermissions.with(BasketStateListActivity.this)
                .constantRequest() //可设置被拒绝后继续申请，直到用户授权或者永久拒绝
                .permission(Permission.CAMERA) //支持请求6.0悬浮窗权限8.0请求安装权限
                .request(new OnPermission() {
                    @Override
                    public void hasPermission(List<String> granted, boolean isAll) {
                        if (isAll) {
                            onResume();
                        }else {
                            Toast.makeText(BasketStateListActivity.this,
                                    "必须同意所有的权限才能使用本程序", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void noPermission(List<String> denied, boolean quick) {
                        if(quick) {
                            Toast.makeText(BasketStateListActivity.this, "被永久拒绝授权，请手动授予权限",
                                    Toast.LENGTH_SHORT).show();
                            // 如果是被永久拒绝就跳转到应用权限系统设置页面
                            XXPermissions.gotoPermissionSettings(BasketStateListActivity.this);
                        }else {
                            Toast.makeText(BasketStateListActivity.this, "获取权限失败",
                                    Toast.LENGTH_SHORT).show();
                            BasketStateListActivity.this.finish();
                        }
                    }
                });
    }

    // 是否有权限：摄像头
    private boolean isHasPermission() {
        if (XXPermissions.isHasPermission(BasketStateListActivity.this, Permission.CAMERA))
            return true;
        return false;
    }

    /*
     * 提示弹框
     */
    private CommonDialog DialogToast(String mTitle, String mMsg){
        return new CommonDialog(BasketStateListActivity.this, R.style.dialog, mMsg,
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
