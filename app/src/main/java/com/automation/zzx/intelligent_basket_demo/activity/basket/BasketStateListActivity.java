package com.automation.zzx.intelligent_basket_demo.activity.basket;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.automation.zzx.intelligent_basket_demo.R;
import com.automation.zzx.intelligent_basket_demo.activity.InstallInfo.BasketInstallInfoActivity;
import com.automation.zzx.intelligent_basket_demo.activity.areaAdmin.AreaAdminPrimaryActivity;
import com.automation.zzx.intelligent_basket_demo.activity.common.UploadImageFTPActivity;
import com.automation.zzx.intelligent_basket_demo.activity.loginRegist.LoginActivity;
import com.automation.zzx.intelligent_basket_demo.adapter.areaAdmin.MgBasketStatementAdapter;
import com.automation.zzx.intelligent_basket_demo.adapter.areaAdmin.MgStateAdapter;
import com.automation.zzx.intelligent_basket_demo.entity.AppConfig;
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
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import okhttp3.Call;

import static com.automation.zzx.intelligent_basket_demo.entity.AppConfig.AREA_ADMIN_GET_ALL_BASKET_INFO;
import static com.automation.zzx.intelligent_basket_demo.widget.zxing.activity.CaptureActivity.QR_CODE_RESULT;

public class BasketStateListActivity extends AppCompatActivity {

    private final static String TAG = "BasketStateListActivity";

    // Handler 消息类型
    private final static String SORT_TYPE_WORKING = "working";  // 排序关键词——正在工作优先
    private final static String SORT_TYPE_RESTING = "resting";   // 排序关键词——空闲状态优先

    // Handler 消息类型
    private final static int UPDATE_BASKET_STATEMENT_MSG = 101;  // 更新吊篮状态列表
    private final static int UPDATE_BASKET_SORT_MSG = 102;  // 更新排序后吊篮列表
    private final static int GET_BASKET_MSG =103;   // 获取指定项目的吊篮列表
    private final static int UPDATE_PROJECT_LIST_FROM_INTERNET_MSG = 104; // 从网络重新获取指定项目的吊篮信息

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
    private final static int ADD_INSTALL_RESULT = 2;  // 获取安装人员信息
    private final static int UPLOAD_CERTIFICATE_IMAGE_RESULT = 3;  // 上传安监证书页面

    /* 控件部分 */
    // 顶部导航栏
    private Toolbar toolbar;
    private TextView titleText;

    // 吊篮状态选择栏
    private GridView mBasketStateGv; // 吊篮状态
    private List<String> mStateLists; // 状态名称
    private MgStateAdapter mgStateAdapter; //适配器
    private int pre_selectedPosition = 2;

    /* 主体内容部分*/
    private SmartRefreshLayout mSmartRefreshLayout; // 下拉刷新

    // 吊篮列表视图
    private RelativeLayout mListRelativeLayout;
    private RecyclerView mBasketListRecyclerView;
    private String mInstallBasketId;
    private List<MgBasketStatement> mgBasketStatementList;
    private List<MgBasketStatement> mgBasketToAllocate;  //待分配安装任务的吊篮列表
    private List<List<MgBasketStatement>> mgBasketStatementClassifiedList;
    private MgBasketStatementAdapter mgBasketStatementAdapter;


    // 无吊篮或项目
    private RelativeLayout mBlankRelativeLayout;
    private TextView mBlankHintTextView;

    //批量分配安装队伍
    private Button btnInstall;

    // 底部筛选框
    private LinearLayout llChoose;
    private Spinner mySpinner_1;
    private Spinner mySpinner_2;
    private TextView myTextView;
    private ArrayAdapter<String> adapter;//创建一个数组适配器
    private List<String> list_1 = new ArrayList<String>();
    private List<String> list_2 = new ArrayList<String>();//创建一个String类型的数组列表。


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
                case UPDATE_BASKET_STATEMENT_MSG:  // 点击状态切换时，更新吊篮列表
                    //筛选实现
                    mgBasketStatementList.clear();
                    mgBasketStatementList.addAll(mgBasketStatementClassifiedList.get(pre_selectedPosition+1));
                    updateBodyContentView();
                    break;
                case UPDATE_BASKET_SORT_MSG:  // 更新排序后吊篮列表
                    updateBodyContentView();
                    break;
                case GET_BASKET_MSG:  // 获取吊篮列表
                    mgBasketStatementList.clear();
                    mgBasketStatementClassifiedList.clear();
                    mgBasketToAllocate.clear();
                    parseBasketListInfo((String)msg.obj);  // 更新吊篮
                    mgStateAdapter.setSelectedPosition(pre_selectedPosition);
                    sendEmptyMessage(UPDATE_BASKET_STATEMENT_MSG);
                    break;
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
        updateBodyContentView();
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

        llChoose = findViewById(R.id.share_main_layout);
        mySpinner_1 = (Spinner) findViewById(R.id.spinner_1);
        mySpinner_2 = (Spinner) findViewById(R.id.spinner_2);
        myTextView = findViewById(R.id.text1);

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

        btnInstall = findViewById(R.id.install_image_view);
        btnInstall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(BasketStateListActivity.this, BasketInstallByListActivity.class);
                intent.putExtra("projectName",mProjectName);
                intent.putExtra("project_id",mProjectId);
                intent.putExtra("basket_list", (Serializable)mgBasketToAllocate);
                startActivityForResult(intent,ADD_INSTALL_RESULT);
            }
        });

        // 吊篮列表
        mListRelativeLayout = (RelativeLayout) findViewById(R.id.basket_avaliable);
        mBasketListRecyclerView = (RecyclerView) findViewById(R.id.basket_recycler_view);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        mBasketListRecyclerView.setLayoutManager(layoutManager);
        mgBasketStatementList = new ArrayList<>();
        mgBasketToAllocate = new ArrayList<>();
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
                intent.putExtra("project_state",pre_selectedPosition);
                intent.putExtra("project_id",mProjectId);
                intent.putExtra("basket_id", mgBasketStatementList.get(position).getBasketId());
                intent.putExtra("basket_state", mgBasketStatementList.get(position).getBasketStatement());
                startActivity(intent);
            }

            @Override
            public void onAddInstall(View view, int position) {
                Intent intent = new Intent(BasketStateListActivity.this, BasketInstallByListActivity.class);
                intent.putExtra("projectName",mProjectName);
                intent.putExtra("project_id",mProjectId);
                intent.putExtra("basket_list", (Serializable)mgBasketToAllocate);
                startActivity(intent);
            }

            @Override
            public void onInstallDetail(View view, int position) {
                // 跳转至吊篮安装详情页面
                mInstallBasketId = mgBasketStatementList.get(position).getBasketId();
                if(mInstallBasketId==null || mInstallBasketId.equals("")){
                    DialogToast("提示", "该吊篮信息有误，无法查看安装详情");
                }else {
                    Intent intent = new Intent(BasketStateListActivity.this, BasketInstallInfoActivity.class);
                    intent.putExtra("project_id", mProjectId);
                    intent.putExtra("basket_id", mgBasketStatementList.get(position).getBasketId());
                    startActivity(intent);
                }
            }
/*
            @Override
            public void onUploadAccept(View view, int position) {
                // 点击上传预验收照片
                Log.i(TAG, "You have clicked the "+ position+" item's PreAssAndAccept");
                Intent intent;
                intent = new Intent(BasketStateListActivity.this, UploadImageFTPActivity.class);
                intent.putExtra(PROJECT_ID, mProjectId);
                intent.putExtra(AreaAdminPrimaryActivity.BASKET_ID, mgBasketStatementList.get(position).getBasketId());
                intent.putExtra(AreaAdminPrimaryActivity.UPLOAD_IMAGE_TYPE, UPLOAD_BASKETS_PRE_INSTALL_IMAGE);
                startActivityForResult(intent, UPLOAD_BASKET_IMAGE_RESULT);
            }*/

            //验收安装结果
            @Override
            public void onAcceptInstallClick(View view, int position) {
                mInstallBasketId = mgBasketStatementList.get(position).getBasketId();
                // 验收安装结果
                if(mInstallBasketId==null || mInstallBasketId.equals("")){
                    DialogToast("提示", "该吊篮信息有误，无法验收安装结果");
                }else {
                    Intent intent = new Intent(BasketStateListActivity.this, BasketInstallInfoActivity.class);
                    intent.putExtra("project_id", mProjectId);
                    intent.putExtra("basket_id", mgBasketStatementList.get(position).getBasketId());
                    startActivity(intent);
                }
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
    }


    /*
     * 初始化
     */
    private void initStateList(){
        //初始化状态筛选栏
        mStateLists.add("待安装");
        mStateLists.add("安装审核");
        mStateLists.add("使用中");
        mStateLists.add("待报停");
        mStateLists.add("报停审核");

        //初始化底部筛选栏
        list_1.add("所有区域");
        list_1.add("按楼号从小到大");
        list_1.add("按楼号从大到小");

        list_2.add("所有状态");
        list_2.add("正在施工"); //workState == 1
        list_2.add("暂未使用"); //workState == 0
        setData();
    }

    public void setData(){
        if(list_1 != null && list_2 != null){
            llChoose.setVisibility(View.VISIBLE);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                mySpinner_1.setDropDownVerticalOffset(0);
                mySpinner_2.setDropDownVerticalOffset(0);
                mySpinner_1.setBackgroundColor(0x0);
                mySpinner_2.setBackgroundColor(0x0);
            }

            adapter = new ArrayAdapter<String>(BasketStateListActivity.this,R.layout.spinner_center_item,list_1);
            adapter.setDropDownViewResource(R.layout.spinner_dropdown_center_item);
            mySpinner_1.setAdapter(adapter);
            mySpinner_1.setSelection(0, true);
            mySpinner_1.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    switch (mySpinner_1.getSelectedItem().toString()){
                        case "按楼号从小到大" :
                            //TODO 筛选楼层并修改UI

                            break;
                        case "按楼号从大到小" :

                            break;
                        default:break;
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                    myTextView.setText("区域选择");
                }
            });

            adapter = new ArrayAdapter<String>(BasketStateListActivity.this,R.layout.spinner_center_item,list_2);
            adapter.setDropDownViewResource(R.layout.spinner_dropdown_center_item);
            mySpinner_2.setAdapter(adapter);

            mySpinner_2.setSelection(0, true);
            mySpinner_2.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    switch (mySpinner_2.getSelectedItem().toString()){
                        case "正在施工" :
                            //TODO 筛选状态并修改UI
                            sortByWorkingState(SORT_TYPE_WORKING);
                            break;
                        case "暂未使用" :
                            sortByWorkingState(SORT_TYPE_RESTING);
                            break;
                        default:break;
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                    myTextView.setText("状态选择");
                }
            });
        } else {
            llChoose.setVisibility(View.GONE);
        }
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


    // 添加施工人员
    private void addInstallWithBasket(String installId){
        BaseOkHttpClient.newBuilder()
                .addHeader("Authorization", mToken)
                .addParam("projectId", mProjectId)
                .addParam("userId", installId)
                .addParam("deviceList", mInstallBasketId)
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
                                areaAdminGetAllBasket();
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
            String workingState = basketObj.getString("workingState");
            if(workingState==null || workingState.equals("")) continue;
            mgBasketStatementList.add(new MgBasketStatement(basketObj.getString("deviceId"),
                    null, basketObj.getString("storageState"),basketObj.getString("workingState")));
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
            if(mgBasketStatement.getBasketStatement().equals("1")){ //加入吊篮状态为待分配安装队伍，则将其加入列表
                mgBasketToAllocate.add(mgBasketStatement);
            }
        }
        mgBasketStatementClassifiedList.get(0).addAll(mgBasketStatements);
    }


    /*
     * UI 更新类
     */
    // 主体页面显示逻辑控制
    public void updateBodyContentView(){
       //有无吊篮显示
        if (mgBasketStatementList.size() == 0) {  // 显示无操作吊篮
            mListRelativeLayout.setVisibility(View.GONE);
            mBlankRelativeLayout.setVisibility(View.VISIBLE);
            mBlankHintTextView.setText("暂无相关吊篮");
            llChoose.setVisibility(View.GONE);
        } else {                                   // 显示可操作吊篮列表
            mBlankRelativeLayout.setVisibility(View.GONE);
            mListRelativeLayout.setVisibility(View.VISIBLE);
            mgBasketStatementAdapter.notifyDataSetChanged();

            //仅在使用中状态展示筛选栏
            llChoose.setVisibility(View.GONE);
            btnInstall.setVisibility(View.GONE);
            switch (pre_selectedPosition){
                case 0:
                    btnInstall.setVisibility(View.VISIBLE);
                    break;
                case 2:
                    llChoose.setVisibility(View.VISIBLE);
                    break;
            }
        }
    }

    /*
     * 处理Activity返回结果
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case ADD_INSTALL_RESULT:  // 上传分配安装人员返回
                if (resultCode == RESULT_OK) {
                    if (mProjectId != null) {
                        areaAdminGetAllBasket();
                    }
                }
                break;
            case UPLOAD_CERTIFICATE_IMAGE_RESULT:  // 上传安监证书返回值
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


    /*
    * 工具类
    * */
    public static int dip2px(Context context, float dpValue) {
        float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    /*
    * 冒泡排序
    * */
    private void sortByWorkingState( String sortType) {
        for (int i = 0; i < mgBasketStatementList.size() - 1; i++) {
            for (int j = 0; j < mgBasketStatementList.size() - 1 - i; j++) {
                //按工作吊篮在前排序
                if(sortType.equals(SORT_TYPE_WORKING)){
                    if (mgBasketStatementList.get(j).getWorkStatement().equals("1")) {
                        Collections.swap(mgBasketStatementList,j,j+1);
                    }
                }
                //按空闲吊篮在前排序
                else if(sortType.equals(SORT_TYPE_RESTING)){
                    if (mgBasketStatementList.get(j).getWorkStatement().equals("0")) {
                        Collections.swap(mgBasketStatementList,j,j+1);
                    }
                }
            }
        }
        mHandler.sendEmptyMessage(UPDATE_BASKET_SORT_MSG);
    }

}
