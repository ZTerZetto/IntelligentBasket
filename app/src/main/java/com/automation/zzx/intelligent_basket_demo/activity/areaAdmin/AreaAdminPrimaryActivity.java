package com.automation.zzx.intelligent_basket_demo.activity.areaAdmin;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.automation.zzx.intelligent_basket_demo.R;
import com.automation.zzx.intelligent_basket_demo.activity.basket.BasketStateListActivity;
import com.automation.zzx.intelligent_basket_demo.activity.basket.BasketStickerActivity;
import com.automation.zzx.intelligent_basket_demo.activity.basket.PlaneFigureActivity;
import com.automation.zzx.intelligent_basket_demo.activity.common.RepairInfoListActivity;
import com.automation.zzx.intelligent_basket_demo.activity.common.UploadImageFTPActivity;
import com.automation.zzx.intelligent_basket_demo.activity.loginRegist.LoginActivity;
import com.automation.zzx.intelligent_basket_demo.adapter.basket.FunctionAdapter;
import com.automation.zzx.intelligent_basket_demo.application.CustomApplication;
import com.automation.zzx.intelligent_basket_demo.entity.Function;
import com.automation.zzx.intelligent_basket_demo.entity.MgBasketStatement;
import com.automation.zzx.intelligent_basket_demo.entity.ProjectInfo;
import com.automation.zzx.intelligent_basket_demo.entity.UserInfo;
import com.automation.zzx.intelligent_basket_demo.utils.ToastUtil;
import com.automation.zzx.intelligent_basket_demo.utils.okhttp.BaseCallBack;
import com.automation.zzx.intelligent_basket_demo.utils.okhttp.BaseOkHttpClient;
import com.automation.zzx.intelligent_basket_demo.utils.xiaomi.mipush.MiPushUtil;
import com.automation.zzx.intelligent_basket_demo.widget.TimeLineView;
import com.automation.zzx.intelligent_basket_demo.widget.dialog.CommonDialog;
import com.scwang.smartrefresh.header.BezierCircleHeader;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;


import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import okhttp3.Call;

import static com.automation.zzx.intelligent_basket_demo.entity.AppConfig.AREA_ADMIN_GET_ALL_BASKET_INFO;
import static com.automation.zzx.intelligent_basket_demo.entity.AppConfig.AREA_ADMIN_GET_ALL_PROJECT_INFO;
import static com.automation.zzx.intelligent_basket_demo.widget.zxing.activity.CaptureActivity.QR_CODE_RESULT;

public class AreaAdminPrimaryActivity extends AppCompatActivity implements View.OnClickListener {

    private final static String TAG = "AreaAdminMgProject";
    //handler 消息类型
    private final static int UPDATE_CURRENT_PROJECT_MSG = 102; // 更换当前项目，更新UI
    private final static int UPDATE_AREA_ADMIN_PROJECT_LIST_MSG = 103; // 更新区域管理员的项目列表


    // intent 消息参数
    public final static String PROJECT_ID = "projectId";  // 上传图片的项目Id
    public final static String PROJECT_NAME = "projectName";  // 上传图片的项目名称
    public final static String BASKETS_NUM = "basketsNumber";  // 吊篮数目
    public final static String UPLOAD_IMAGE_TYPE  = "uploadImageType"; // 上传图片的类型
    public final static String UPLOAD_BASKETS_PRE_INSTALL_IMAGE = "basketsPreInstall"; // 预验收
    public final static String UPLOAD_CERTIFICATE_IMAGE = "certificate"; // 安监证书
    public final static String BASKET_ID = "basketId"; // 上传图片的吊篮ID
    public final static String UPLOAD_BASKETS_PRE_STOP_IMAGE = "basketsPreStop"; // 预验收

    // 页面跳转
    private final static int CAPTURE_ACTIVITY_RESULT = 1;  // 扫码返回
    private final static int UPLOAD_BASKET_IMAGE_RESULT = 2;  // 上传预验收图片
    private final static int UPLOAD_CERTIFICATE_IMAGE_RESULT = 3;  // 上传安监证书页面
    private final static int UPLOAD_PRE_STOP_BASKET_IMAGE_RESULT = 4;  // 上传吊篮预报停图片页面
    private final static int UPLOAD_CONFIGURATION_RESULT = 5;  // 上传配置清单结果



    // 用户登录信息相关
    private UserInfo mUserInfo; //用户信息
    private String mProjectId;  //項目ID
    private String mToken;
    private SharedPreferences mPref;


    // 控件
    // 顶部导航栏
    private Toolbar mToolbar;  // 顶部导航栏
    private LinearLayout mBack; //返回按钮
    private ImageView mMessageView; // 左侧消息图标
    private ImageView mMineView;   // 右侧个人图标
    private ImageView mSelectProList; //项目列表加载图标
    private AlertDialog mSelectProjectDialog;  // 切换项目弹窗
    private List<String> mProjectNameList;  // 项目名字列表（网络请求）
    private List<ProjectInfo> mProjectInfoList; // 项目详情列表
    private int currentSelectedProject = 0; // 当前项目号位置
    private int tmpSelectedProject = 0; // 临时项目号
    private ProjectInfo projectInfo;

    //吊篮数据
    private List<MgBasketStatement> mgBasketStatementList;
    private List<List<MgBasketStatement>> mgBasketStatementClassifiedList;

    //标题栏内容
    private TextView txtProNum;  //项目编号
    private TextView mProjectTitleTv; // 项目名称
    private TextView txtStartTime;  //项目开始时间

    // 无吊篮或项目
    private RelativeLayout mBlankRelativeLayout;
    private TextView mBlankHintTextView;

    //项目进度条
    private TimeLineView mProjectScheduleTimeLine;  // 时间进度条
    private float currentProjectScheduleFlag = 0;
    private List<String> mProjectScheduleList;

    /* 主体内容部分*/
    private SmartRefreshLayout mSmartRefreshLayout; // 下拉刷新
    private RelativeLayout rlProInfo; //项目基本信息
    private RelativeLayout rlConfiguration; //配置清单
    private RelativeLayout rlInstall; //安装信息
    private RelativeLayout rlBasket; //吊篮列表
    private RelativeLayout rlCompact; //项目合同


    // function gridview
    private RelativeLayout rlFunction; //功能控件及进度条
    private RelativeLayout rlProTips;  //分状态操作提示
    private TextView txtProTips;  //分状态操作提示-文字
    private GridView mGvRecord; // 项目管理
    private RelativeLayout rlRecordManage;
    private List<Function> mRecordFunctions;  // 项目功能列表
    private FunctionAdapter mRecordAdapter;   // 项目功能适配器


    /*
     * 消息函数
     */
    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch(msg.what) {
                case UPDATE_CURRENT_PROJECT_MSG:  //更换当前项目，更新UI
                    mgBasketStatementList.clear();
                    mgBasketStatementClassifiedList.clear();
                    parseBasketListInfo((String)msg.obj);  // 更新吊篮
                    break;

                case UPDATE_AREA_ADMIN_PROJECT_LIST_MSG: // 更新区域管理员的项目列表
                    updateProjectContentView(); // 更新项目信息及控件显示
                    areaAdminGetAllBasket();  //根据项目ID获取吊篮信息
                    break;

                default:
                    break;
            }
        }
    };



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_area_admin_primary_try);
        getBaseInfoFromPred();
        getUserInfo();
        initWidget();
        mHandler.sendEmptyMessage(UPDATE_AREA_ADMIN_PROJECT_LIST_MSG);

        MiPushUtil.initMiPush(AreaAdminPrimaryActivity.this, mUserInfo.getUserId(), null);

    }

    // 項目和吊籃信息获取
    public void getBaseInfoFromPred() {
        Intent intent = getIntent();
        projectInfo = (ProjectInfo)intent.getSerializableExtra("project_info");
        mProjectId = projectInfo.getProjectId();
    }
    /*
     * 解析用户信息
     */
    // 获取用户数据
    private void getUserInfo(){
        // 从本地获取数据
        mPref = PreferenceManager.getDefaultSharedPreferences(this);
        mUserInfo = new UserInfo();
        mUserInfo.setUserId(mPref.getString("userId", ""));
        mUserInfo.setUserPhone(mPref.getString("userPhone", ""));
        mUserInfo.setUserRole(mPref.getString("userRole", ""));
        mUserInfo.setUserName(mPref.getString("userName", ""));
        mToken = mPref.getString("loginToken","");

        //获取该区域管理员的项目列表
        mProjectNameList = new ArrayList<>();
        mProjectInfoList = new ArrayList<>();

    }


    /*
    * 初始化页面
    * */
    //初始化句柄
    private void initWidget(){

        // 顶部导航栏
        mToolbar = (Toolbar) findViewById(R.id.areaAdmin_primary_toolbar);
        mToolbar.setTitle("");
        setSupportActionBar(mToolbar);
        mMessageView = (ImageView) findViewById(R.id.entrance_info);
        mMessageView.setOnClickListener(this);
        mMineView = (ImageView) findViewById(R.id.entrance_mine);
        mMineView.setOnClickListener(this);
        mSelectProList = findViewById(R.id.project_list);
        mSelectProList.setOnClickListener(this);
        mBack = findViewById(R.id.iv_back);
        mBack.setOnClickListener(this);

        // 无吊篮提示信息
        mBlankRelativeLayout = (RelativeLayout) findViewById(R.id.basket_no_avaliable);
        mBlankHintTextView = (TextView) findViewById(R.id.no_basket_hint);

        //吊篮
        mgBasketStatementList = new ArrayList<>();
        mgBasketStatementClassifiedList = new ArrayList<>();

        //初始化标题栏及进度条，并显示内容
        mProjectTitleTv = (TextView) findViewById(R.id.project_title);
        txtProNum = (TextView) findViewById(R.id.tv_pro_num);
        txtStartTime = (TextView) findViewById(R.id.tv_pro_start);
        rlProTips = (RelativeLayout) findViewById(R.id.rl_project_state_tips);
        txtProTips = (TextView) findViewById(R.id.tv_project_state_tips);

        rlProInfo = (RelativeLayout) findViewById(R.id.rl_project_info);
        rlProInfo.setOnClickListener(this);
        rlConfiguration = (RelativeLayout) findViewById(R.id.rl_configuration_info);
        rlConfiguration.setOnClickListener(this);
        rlInstall = (RelativeLayout) findViewById(R.id.rl_install_info);
        rlInstall.setOnClickListener(this);
        rlBasket = (RelativeLayout) findViewById(R.id.rl_basket_info);
        rlBasket.setOnClickListener(this);
        rlCompact = (RelativeLayout) findViewById(R.id.rl_compact_info);
        rlCompact.setOnClickListener(this);

        rlFunction = (RelativeLayout)findViewById(R.id.function_content);
        rlRecordManage = (RelativeLayout) findViewById(R.id.rl_project);

        mProjectScheduleTimeLine = (TimeLineView) findViewById(R.id.project_schedule_timelineview);
        mProjectScheduleList = new ArrayList<>();

        initProjectScheduleList();
        mProjectScheduleTimeLine.setPointStrings(mProjectScheduleList, currentProjectScheduleFlag);
        txtProNum.setText(mProjectId);
        txtStartTime.setText("-");

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
                if(mProjectInfoList.size()>0){
                    areaAdminGetAllBasket();
                }else{
                    mSmartRefreshLayout.finishRefresh();
                }
            }
        });

        //功能列表初始化
        mGvRecord = (GridView)findViewById(R.id.gv_record_function);
        initFunctionList();

        //历史记录功能列表点击事件
        mRecordAdapter = new FunctionAdapter(AreaAdminPrimaryActivity.this,R.layout.item_function,mRecordFunctions);
        mGvRecord.setAdapter(mRecordAdapter);
        mGvRecord.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent;
                switch (position) {
                    case 0: //安装信息
                        Log.i(TAG, "You have clicked the examine compact button");
                        intent = new Intent(AreaAdminPrimaryActivity.this, InstallInfoListActivity.class);
                        intent.putExtra(PROJECT_ID, projectInfo.getProjectId());
                        startActivity(intent);
                        break;
                    case 1: //报警记录
                        Log.i(TAG, "You have clicked the pre_apply compact button");
                        intent = new Intent(AreaAdminPrimaryActivity.this, AlarmRecordProjectActivity.class);
                        intent.putExtra(PROJECT_ID, projectInfo.getProjectId());
                        startActivity(intent);
                        break;
                    case 2: //报修记录
                        Log.i(TAG, "You have clicked the repair information button");
                        intent = new Intent(AreaAdminPrimaryActivity.this, RepairInfoListActivity.class);
                        intent.putExtra(PROJECT_ID, projectInfo.getProjectId());
                        intent.putExtra(PROJECT_NAME, projectInfo.getProjectName());
                        startActivity(intent);
                        break;
                    case 3: //报停记录
                        Log.i(TAG, "You have clicked the pre stop info button");
                        intent = new Intent(AreaAdminPrimaryActivity.this, StopRecordActivity.class);
                        intent.putExtra(PROJECT_ID, projectInfo.getProjectId());
                        startActivity(intent);
                        break;
                    default:
                        break;
                }
            }
        });

    }

    /*
     * 初始化进度轴
     */
    public void initProjectScheduleList(){
        mProjectScheduleList.add("合同执行");
        mProjectScheduleList.add("配置清单");
        mProjectScheduleList.add("吊篮安装");
        mProjectScheduleList.add("安监证书");
        mProjectScheduleList.add("进行中");
        mProjectScheduleList.add("结束");
    }


    // 初始化测试功能列表（根据项目状态）
    private void initFunctionList(){
        mRecordFunctions = new ArrayList<>();
        Function compact = new Function("安装信息", R.mipmap.icon_func_contact,true);
        mRecordFunctions.add(compact);
        Function preApply = new Function("报警记录", R.mipmap.icon_func_confirm,true);
        mRecordFunctions.add(preApply);
        Function certification = new Function("报修记录", R.mipmap.icon_func_certificate,true);
        mRecordFunctions.add(certification);
        Function preFinish = new Function("报停记录", R.mipmap.icon_func_pre_end,true);
        mRecordFunctions.add(preFinish);

    }

    @Override
    public void onClick(View v) {
        Intent intent;
        switch (v.getId()) {
            case R.id.iv_back:  //返回按钮
                onBackPressed();
            case R.id.project_list:  // 切换项目
                Log.i(TAG, "You have clicked the select projectList button");
                //showSingleAlertDialog();
                break;
            case R.id.entrance_info:  // 点击消息入口
                Log.i(TAG, "You have clicked the select projectList button");
                intent = new Intent(AreaAdminPrimaryActivity.this, AreaAdminMessageActivity.class);
                startActivity(intent);
                break;
            case R.id.entrance_mine:  // 点击我的入口
                Log.i(TAG, "You have clicked the select projectList button");
                intent = new Intent(AreaAdminPrimaryActivity.this, AreaAdminSetActivity.class);
                startActivity(intent);
                break;
            case R.id.rl_project_info:  // 项目基本信息
                Log.i(TAG, "You have clicked the select projectList button");
                intent = new Intent(AreaAdminPrimaryActivity.this, ProDetailActivity.class);
                intent.putExtra("projectId", mProjectId);
                startActivity(intent);
                break;
            case R.id.rl_configuration_info:  // 配置清单
                Log.i(TAG, "You have clicked the configuration button");
                intent = new Intent(AreaAdminPrimaryActivity.this, ConfigurationActivity.class);
                intent.putExtra(PROJECT_ID, mProjectId);
                startActivityForResult(intent, UPLOAD_CONFIGURATION_RESULT);
                break;
            case R.id.rl_install_info:  // 查看安装方案
                Log.i(TAG, "You have clicked the select projectList button");
                intent = new Intent(AreaAdminPrimaryActivity.this, PlaneFigureActivity.class);
                startActivity(intent);
                break;
            case R.id.rl_basket_info:  // 查看吊篮列表
                Log.i(TAG, "You have clicked the basket button");
                intent = new Intent(AreaAdminPrimaryActivity.this, BasketStateListActivity.class);
                intent.putExtra(PROJECT_ID, projectInfo.getProjectId());
                intent.putExtra(PROJECT_NAME, projectInfo.getProjectName());
                startActivity(intent);
                break;
            case R.id.rl_compact_info:  // 查看合同
                Log.i(TAG, "You have clicked the examine compact button");
                intent = new Intent(AreaAdminPrimaryActivity.this, CheckCompactActivity.class);
                startActivity(intent);
                break;
        }
    }

    /*
     * 处理Activity返回结果
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
            case CAPTURE_ACTIVITY_RESULT:  // 扫描工人二维码名片返回结果

                break;
            case UPLOAD_BASKET_IMAGE_RESULT:  // 上传预验收图片返回

                break;
            case UPLOAD_CERTIFICATE_IMAGE_RESULT:  // 上传安监证书返回值
                break;

            case UPLOAD_CONFIGURATION_RESULT: // 上传配置清单返回值

                break;
            default:
                break;
        }
    }


    /*
     * 网络相关
     */
    // 获取区域管理员的项目列表
    private void areaAdminGetAllProject(){
        BaseOkHttpClient.newBuilder()
                .addHeader("Authorization", mToken)
                .addParam("userId", mUserInfo.getUserId())
                .get()
                .url(AREA_ADMIN_GET_ALL_PROJECT_INFO)
                .build()
                .enqueue(new BaseCallBack() {
                    @Override
                    public void onSuccess(Object o) {
                        Log.d(TAG, "成功获取区域管理员所有项目信息");
                        Message message = new Message();
                        message.what = UPDATE_AREA_ADMIN_PROJECT_LIST_MSG;  // 更新项目列表
                        message.obj = o.toString();
                        mHandler.sendMessage(message);
                    }

                    @Override
                    public void onError(int code) {
                        Log.d(TAG, "获取所有项目信息错误，错误编码："+code);

                    }

                    @Override
                    public void onFailure(Call call, IOException e) {
                        Log.d(TAG, "获取所有项目信息失败");
                    }
                });
    }


    // 获取项目对应的所有吊篮信息
    private void areaAdminGetAllBasket(){
        BaseOkHttpClient.newBuilder()
                .addHeader("Authorization", mToken)
                .addParam("projectId", projectInfo.getProjectId())
                //.addParam("userId", mUserInfo.getUserId())
                .get()
                .url(AREA_ADMIN_GET_ALL_BASKET_INFO)
                .build()
                .enqueue(new BaseCallBack() {
                    @Override
                    public void onSuccess(Object o) {
                        Log.i(TAG, "成功" );
                        String responseData = o.toString();
                        Message message = new Message();
                        message.what = UPDATE_CURRENT_PROJECT_MSG;
                        message.obj = responseData;
                        mHandler.sendMessage(message);
                        mSmartRefreshLayout.finishRefresh(100);
                    }

                    @Override
                    public void onError(int code) {
                        Log.i(TAG, "错误：" + code);
                        switch (code){
                            case 401: // 未授权
                                ToastUtil.showToastTips(AreaAdminPrimaryActivity.this, "登录已过期，请重新登陆");
                                startActivity(new Intent(AreaAdminPrimaryActivity.this, LoginActivity.class));
                                AreaAdminPrimaryActivity.this.finish();
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
                    null, basketObj.getString("storageState"),basketObj.getString("workingState")));
        }
        parseMgBasketStatementList(mgBasketStatementList);
    }

    // 解析吊篮状态
    private void parseMgBasketStatementList(List<MgBasketStatement> mgBasketStatements){
        // 初始化吊篮分类列表
        for(int i=0; i<6;i++){
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



    /*
     * UI 更新类
     */
    // 项目进度页面更新
    public void updateProjectContentView(){
        mProjectTitleTv.setText(projectInfo.getProjectName());
        txtProNum.setText(projectInfo.getProjectId());
        txtStartTime.setText(projectInfo.getProjectStart());
        switch(projectInfo.getProjectState()){
            case "1":
            case "0":/* 立项、安装 */
                txtStartTime.setText("暂未开始");
                currentProjectScheduleFlag = 1;
                mProjectScheduleTimeLine.setPointStrings(mProjectScheduleList, currentProjectScheduleFlag);
                txtProTips.setText("项目立项中...");
                break;
            case "11": // 清单待配置
                currentProjectScheduleFlag = 2;
                mProjectScheduleTimeLine.setStep(currentProjectScheduleFlag);  //
                txtProTips.setText("项目已立项，请配置清单！");
                break;
            case "12": // 清单待审核
                currentProjectScheduleFlag = (float)2.5;
                mProjectScheduleTimeLine.setStep(currentProjectScheduleFlag);  //
                txtProTips.setText("配置清单等待审核中...");
                break;
            case "2": /*  预安装申请图片 */

                if(projectInfo.getStoreOut() == null || projectInfo.getStoreOut().equals("")){
                    // 尚未上传预申请图片
                    currentProjectScheduleFlag = 3;
                    mProjectScheduleTimeLine.setStep(currentProjectScheduleFlag);
                    txtProTips.setText("请完成现场吊篮安装，完毕后上传预验收照片！");

                }else{
                    // 已经上传预验收申请图片
                    currentProjectScheduleFlag = (float)3.5;
                    mProjectScheduleTimeLine.setStep(currentProjectScheduleFlag);
                    txtProTips.setText("预验收照片已成功上传，等待审核中...");
                }
                break;
            case "21": /* 上传安监证书 */

                if(projectInfo.getProjectCertUrl()==null || projectInfo.getProjectCertUrl().equals("")){ // 尚未上传安监证书
                    // 尚未上传
                    currentProjectScheduleFlag = (float)3.5;
                    mProjectScheduleTimeLine.setStep(currentProjectScheduleFlag);
                    txtProTips.setText("请等候安监审查，结束后上传安监证书！");
                }else{
                    // 已上传安检证书
                    currentProjectScheduleFlag = (float)4;
                    mProjectScheduleTimeLine.setStep(currentProjectScheduleFlag);
                    txtProTips.setText("安监证书已成功上传，等待审核中...");
                }
                break;
            case "3": /* 使用中 */
                currentProjectScheduleFlag = (float)5;
                mProjectScheduleTimeLine.setStep(currentProjectScheduleFlag);
                rlProTips.setVisibility(View.GONE); //使用中无提示
                break;
            case "4": /* 结束 */
                currentProjectScheduleFlag = (float)6;
                mProjectScheduleTimeLine.setStep(currentProjectScheduleFlag);
                txtProTips.setText("项目已结束。");
                break;
        }
    }

    // 弹出项目选择框
    public void showSingleAlertDialog(){
        final AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
        alertBuilder.setTitle("这是单选框");
        alertBuilder.setSingleChoiceItems(listToArray(mProjectNameList), currentSelectedProject, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int position) {
                tmpSelectedProject = position;
            }
        });

        alertBuilder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                currentSelectedProject = tmpSelectedProject;
                areaAdminGetAllBasket();
                mSelectProjectDialog.dismiss();
            }
        });

        alertBuilder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                mSelectProjectDialog.dismiss();
            }
        });

        mSelectProjectDialog = alertBuilder.create();
        mSelectProjectDialog.show();
    }


    /*
     * 提示弹框
     */
    private CommonDialog DialogToast(String mTitle, String mMsg){
        return new CommonDialog(this, R.style.dialog, mMsg,
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
     */
    private String[] listToArray(List<String> list ){
        String[] strings = new String[list.size()];
        list.toArray(strings);
        return strings;
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        CustomApplication.setMainActivity(null);
    }

}
