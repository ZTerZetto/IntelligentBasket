package com.automation.zzx.intelligent_basket_demo.fragment.proAdmin;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.menu.MenuBuilder;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.automation.zzx.intelligent_basket_demo.R;
import com.automation.zzx.intelligent_basket_demo.activity.areaAdmin.CheckCompactActivity;
import com.automation.zzx.intelligent_basket_demo.activity.areaAdmin.ProDetailActivity;
import com.automation.zzx.intelligent_basket_demo.activity.areaAdmin.UploadPreStopInfoActivity;
import com.automation.zzx.intelligent_basket_demo.activity.basket.BasketDetailActivity;
import com.automation.zzx.intelligent_basket_demo.activity.common.RepairInfoListActivity;
import com.automation.zzx.intelligent_basket_demo.activity.common.UploadImageFTPActivity;
import com.automation.zzx.intelligent_basket_demo.activity.loginRegist.LoginActivity;
import com.automation.zzx.intelligent_basket_demo.activity.proAdmin.ProAdminPreActivity;
import com.automation.zzx.intelligent_basket_demo.adapter.areaAdmin.MgBasketStatementAdapter;
import com.automation.zzx.intelligent_basket_demo.adapter.areaAdmin.MgStateAdapter;
import com.automation.zzx.intelligent_basket_demo.entity.MgBasketStatement;
import com.automation.zzx.intelligent_basket_demo.entity.ProjectInfo;
import com.automation.zzx.intelligent_basket_demo.entity.UserInfo;
import com.automation.zzx.intelligent_basket_demo.utils.ToastUtil;
import com.automation.zzx.intelligent_basket_demo.utils.okhttp.BaseCallBack;
import com.automation.zzx.intelligent_basket_demo.utils.okhttp.BaseOkHttpClient;
import com.automation.zzx.intelligent_basket_demo.widget.TimeLineView;
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
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import okhttp3.Call;

import static android.app.Activity.RESULT_OK;
import static com.automation.zzx.intelligent_basket_demo.entity.AppConfig.AREA_ADMIN_ADD_BASKET_INTO_PROJECT;
import static com.automation.zzx.intelligent_basket_demo.entity.AppConfig.AREA_ADMIN_GET_ALL_BASKET_INFO;
import static com.automation.zzx.intelligent_basket_demo.entity.AppConfig.PRO_ADMIN_GET_PROINFO;
import static com.automation.zzx.intelligent_basket_demo.widget.zxing.activity.CaptureActivity.QR_CODE_RESULT;

/**
 * Created by zzx on 2019/5/20.
 * Describe:项目管理员项目页面
 */
public class ProAdminMgProjectFragment extends Fragment implements View.OnClickListener {

    private final static String TAG = "ProAdminMgProject";
    // Handler 消息类型
    private final static int UPDATE_BASKET_STATEMENT_MSG = 101;  // 更新吊篮状态列表
    private final static int UPDATE_PROJECT_AND_BASKET_MSG = 102; // 更换项目，重新获取吊篮列表
    private final static int UPDATE_PRO_ADMIN_PROJECT_MSG = 103; // 更新项目管理员的项目详情
    private final static int UPDATE_PROJECT_LIST_FROM_INTERNET_MSG = 104; // 从网络重新获取指定项目的吊篮信息

    // 页面跳转
    private final static int CAPTURE_ACTIVITY_RESULT = 1;  // 扫码返回
    private final static int UPLOAD_BASKET_IMAGE_RESULT = 2;  // 上传预验收图片
    private final static int UPLOAD_CERTIFICATE_IMAGE_RESULT = 3;  // 上传安监证书页面
    private final static int UPLOAD_PRE_STOP_BASKET_IMAGE_RESULT = 4;  // 上传吊篮预报停图片页面

    // intent 消息参数
    public final static String PROJECT_ID = "projectId";  // 上传图片的项目Id
    public final static String PROJECT_NAME = "projectName";  // 上传图片的项目Id
    public final static String BASKETS_NUM = "basketsNumber";  // 吊篮数目
    public final static String UPLOAD_IMAGE_TYPE  = "uploadImageType"; // 上传图片的类型
    public final static String UPLOAD_BASKETS_PRE_INSTALL_IMAGE = "basketsPreInstall"; // 预验收
    public final static String UPLOAD_CERTIFICATE_IMAGE = "certificate"; // 安监证书
    public final static String BASKET_ID = "basketId"; // 上传图片的吊篮ID
    public final static String UPLOAD_BASKETS_PRE_STOP_IMAGE = "basketsPreStop"; // 预验收

    // 控件
    // 顶部导航栏
    private Toolbar mProjectMoreTb;
    private TextView mProjectTitleTv; // 项目名称
    private AlertDialog mSelectProjectDialog;  // 切换项目弹窗
    private List<String> mProjectNameList;  // 项目名字列表（网络请求）
    private List<ProjectInfo> mProjectInfoList; // 项目详情列表
    private int currentSelectedProject = 0; // 当前项目号位置
    private int tmpSelectedProject = 0; // 临时项目号
    private String mLastProjectId;// 上次退出时项目号

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
    // 无吊篮或项目
    private RelativeLayout mBlankRelativeLayout;
    private TextView mBlankHintTextView;
    // 项目管理视图
    private LinearLayout mMgProjectLinearLayout;
    private TimeLineView mProjectScheduleTimeLine;  // 时间进度条
    private float currentProjectScheduleFlag = 0;
    private List<String> mProjectScheduleList;
    private TextView mProjectIdTextView; // 项目id
    private TextView mProjectNameTextView; //项目名称
    private RelativeLayout mProjectStartRelativeLayout; // 项目开始日期
    private TextView mProjectStartTextView;
    private RelativeLayout mExamineCompactRelativeLayout; // 查看合同
    private RelativeLayout mPreApplyRelativeLayout; // 预验收申请
    private TextView mPreApplyCountTextView;
    private RelativeLayout mSendOrExamineCertificateRelativeLayout; // 上传或查看安监证书
    private TextView mSendOrExamineCertificateTextView; //
    private TextView mSendOrExamineCertificateCountTextView;
    private RelativeLayout rlGetRepairInfo; //获取报修历史
    private RelativeLayout mUploadPreStopInfoRelativeLayout; // 预报停信息上传

    // 悬浮按钮
    private ImageView mAddBasketImageView; // 添加吊篮

    // 上下左右滑动监听
    private static enum State{ VISIBLE,ANIMATIONING,INVISIBLE,}
    private State state = State.INVISIBLE;
    protected static final float FLIP_DISTANCE = 150;
    private GestureDetector mGestureDetector;
    private ProAdminPreActivity.MyOnTouchListener myOnTouchListener;
    private SVCGestureListener mGestureListener = new SVCGestureListener();

    // 个人信息相关
    private UserInfo mUserInfo;
    private String mToken;
    private SharedPreferences mPref;
    private SharedPreferences.Editor editor;

    /*
     * 消息函数
     */
    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch(msg.what) {
                case UPDATE_BASKET_STATEMENT_MSG:  // 更新吊篮列表
                    mgBasketStatementList.clear();
                    mgBasketStatementList.addAll(mgBasketStatementClassifiedList.get(pre_selectedPosition));
                    updateBodyContentView();
                    break;
                case UPDATE_PROJECT_AND_BASKET_MSG:  // 更换项目，重新获取吊篮列表
                    updateProjectContentView(); // 更新项目信息
                    mgBasketStatementList.clear();
                    mgBasketStatementClassifiedList.clear();
                    parseBasketListInfo((String)msg.obj);  // 更新吊篮
                    //pre_selectedPosition = 0;
                    mgStateAdapter.setSelectedPosition(pre_selectedPosition);
                    sendEmptyMessage(UPDATE_BASKET_STATEMENT_MSG);
                    break;
                case UPDATE_PRO_ADMIN_PROJECT_MSG: // 更新项目管理员的项目详情
                    mProjectNameList.clear();
                    mProjectInfoList.clear();
                    parseProjectInfo((String)msg.obj);  // 解析数据
                    setLastLoginProjectId();  // 设置默认项目
                    //currentSelectedProject = 0; // 项目位置
                    //pre_selectedPosition = 0;  // 筛选框位置

                    if(mProjectNameList.size()<=0) { // 没有项目
                        mProjectTitleTv.setText("暂无项目");
                        parseMgBasketStatementList(mgBasketStatementList);
                        updateBodyContentView();
                    }else{
                        areaAdminGetAllBasket();
                    }
                    break;
                case UPDATE_PROJECT_LIST_FROM_INTERNET_MSG: // 从网络重新获取指定项目的吊篮信息
                    areaAdminGetAllBasket();
                default:
                    break;
            }
        }
    };

    /*
     * 生命周期函数
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if(!isHasPermission()) requestPermission();

        View view = inflater.inflate(R.layout.fragment_area_admin_manage_project,
                container, false);

        // 顶部toolbar
        mProjectMoreTb = (Toolbar) view.findViewById(R.id.project_more_toolbar);
        mProjectTitleTv = (TextView) view.findViewById(R.id.project_title);
        mProjectMoreTb.setTitle("项目");
        ((AppCompatActivity) getActivity()).setSupportActionBar(mProjectMoreTb);
        mProjectNameList = new ArrayList<>();
        mProjectInfoList = new ArrayList<>();
        proAdminGetAllProject();

        // 状态选择栏初
        // 初始化
        mBasketStateGv = (GridView) view.findViewById(R.id.mg_basket_state);
        mStateLists = new ArrayList<>();
        initStateList();
        mgStateAdapter = new MgStateAdapter(getContext(), R.layout.item_basket_state_switch, mStateLists);
        mgStateAdapter.setSelectedPosition(pre_selectedPosition);
        mBasketStateGv.setAdapter(mgStateAdapter);
        // 消息响应
        mBasketStateGv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                pre_selectedPosition = position;
                mgStateAdapter.setSelectedPosition(pre_selectedPosition);
                if(mProjectNameList.size() > 0)  // 当且仅当存在项目时更新吊篮状态列表
                    mHandler.sendEmptyMessage(UPDATE_BASKET_STATEMENT_MSG);  // 更新列表
            }
        });

        /*
         主体内容部分
          */
        // 下拉刷新
        mSmartRefreshLayout = (SmartRefreshLayout) view.findViewById(R.id.smart_refresh_layout);
        mSmartRefreshLayout.setRefreshHeader(  //设置 Header 为 贝塞尔雷达 样式
                new BezierCircleHeader(getActivity()));
        mSmartRefreshLayout.setPrimaryColorsId(R.color.smart_loading_background_color);
        mSmartRefreshLayout.setOnRefreshListener(new OnRefreshListener() { // 添加下拉刷新监听
            @Override
            public void onRefresh(RefreshLayout refreshlayout) {
                proAdminGetAllProject();
            }
        });

        // 吊篮列表
        mListRelativeLayout = (RelativeLayout) view.findViewById(R.id.basket_avaliable);
        mBasketListRecyclerView = (RecyclerView) view.findViewById(R.id.basket_recycler_view);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        mBasketListRecyclerView.setLayoutManager(layoutManager);
        mgBasketStatementList = new ArrayList<>();
        mgBasketStatementClassifiedList = new ArrayList<>();
        mgBasketStatementAdapter = new MgBasketStatementAdapter(getContext(), mgBasketStatementList);
        mBasketListRecyclerView.setAdapter(mgBasketStatementAdapter);
        mgBasketStatementAdapter.setOnItemClickListener(new MgBasketStatementAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                // 点击item响应
                Log.i(TAG, "You have clicked the "+position+" item");
                // 跳转至吊篮详情页面
                Intent intent = new Intent(getActivity(), BasketDetailActivity.class);
                intent.putExtra("project_id",mProjectInfoList.get(currentSelectedProject).getProjectId());
                intent.putExtra("basket_id", mgBasketStatementList.get(position).getBasketId());
//                intent.putExtra("principal_name", mProjectInfoList.
//                        get(currentSelectedProject).getAdminAreaUser().getUserName());
                startActivity(intent);
            }

            @Override
            public void onAddInstall(View view, int position) {

            }

            @Override
            public void onInstallDetail(View view, int position) {

            }

            @Override
            public void onInstallPreAccept(View view, int position) {

            }

            /*@Override
            public void onAddInstall(View view, int position) {

            }
*/
           /* @Override
            public void onUploadAccept(View view, int position) {

            }*/

            @Override
            public void onAcceptInstallClick(View view, int position) {
                // 点击安监证书
                Log.i(TAG, "You have clicked the "+ position+" item's PreAssAndAcept");
                Intent intent;
                intent = new Intent(getActivity(), UploadImageFTPActivity.class);
                intent.putExtra(PROJECT_ID, mProjectInfoList.get(currentSelectedProject).getProjectId());
                intent.putExtra(BASKET_ID, mgBasketStatementList.get(position).getBasketId());
                intent.putExtra(UPLOAD_IMAGE_TYPE, UPLOAD_CERTIFICATE_IMAGE);
                startActivityForResult(intent, UPLOAD_PRE_STOP_BASKET_IMAGE_RESULT);
            }

            @Override
            public void onWatchCreditClick(View view, int position) {

            }

            @Override
            public void onPreApplyStopClick(View view, int position) {
                // 点击预报停申请
                Log.i(TAG, "You have clicked the "+ position +" item's PreApplyStop");
//                Intent intent;
//                intent = new Intent(getActivity(), UploadImageFTPActivity.class);
//                intent.putExtra(PROJECT_ID, mProjectInfoList.get(currentSelectedProject).getProjectId());
//                intent.putExtra(BASKET_ID, mgBasketStatementList.get(position).getBasketId());
//                intent.putExtra(UPLOAD_IMAGE_TYPE, UPLOAD_BASKETS_PRE_STOP_IMAGE);
//                startActivityForResult(intent, UPLOAD_PRE_STOP_BASKET_IMAGE_RESULT);
            }
        });

        // 无吊篮提示信息
        mBlankRelativeLayout = (RelativeLayout) view.findViewById(R.id.basket_no_avaliable);
        mBlankHintTextView = (TextView) view.findViewById(R.id.no_basket_hint);

        // 项目管理
        mMgProjectLinearLayout = (LinearLayout) view.findViewById(R.id.project_manage);
        mProjectScheduleTimeLine = (TimeLineView) view.findViewById(R.id.project_schedule_timelineview);
        mProjectScheduleList = new ArrayList<>();
        initProjectScheduleList();
        mProjectScheduleTimeLine.setPointStrings(mProjectScheduleList, currentProjectScheduleFlag);
        mProjectIdTextView = (TextView) view.findViewById(R.id.project_id_textview);
        mProjectNameTextView = (TextView) view.findViewById(R.id.project_name_textview);
        mProjectStartRelativeLayout = (RelativeLayout) view.findViewById(R.id.project_start_time_layout);
        mProjectStartTextView = (TextView) view.findViewById(R.id.project_start_time_textview);
        mExamineCompactRelativeLayout = (RelativeLayout) view.findViewById(R.id.examine_compact_layout);
        mExamineCompactRelativeLayout.setOnClickListener(this);  // 查看合同
        mPreApplyRelativeLayout = (RelativeLayout) view.findViewById(R.id.project_pre_apply_layout); // 预验收
        mPreApplyRelativeLayout.setOnClickListener(this);
        mPreApplyCountTextView = (TextView) view.findViewById(R.id.project_pre_apply_tv_count);
        mSendOrExamineCertificateRelativeLayout = (RelativeLayout) view.findViewById(R.id.send_examine_certificate_layout);  // 安监证书
        mSendOrExamineCertificateRelativeLayout.setOnClickListener(this);
        mSendOrExamineCertificateTextView = (TextView) view.findViewById(R.id.send_examine_certification_tv);
        mSendOrExamineCertificateCountTextView = (TextView) view.findViewById(R.id.send_examine_certificate_tv_count);
        rlGetRepairInfo   = (RelativeLayout) view.findViewById(R.id.rl_get_repair_info);
        rlGetRepairInfo.setOnClickListener(this);
        mUploadPreStopInfoRelativeLayout = (RelativeLayout) view.findViewById(R.id.pre_stop_info_layout);  // 上传预报停信息
        mUploadPreStopInfoRelativeLayout.setOnClickListener(this);

        /*
         * 悬浮框
          */
        mAddBasketImageView = (ImageView) view.findViewById(R.id.basket_add_image_view);

        //添加吊篮不可见
        mAddBasketImageView.setVisibility(View.GONE);

        mAddBasketImageView.setOnClickListener(new View.OnClickListener() {  // 点击响应
            @Override
            public void onClick(View v) {
                if(mProjectNameList.size() <= 0)
                    DialogToast("错误", "您尚无授权的项目");
                else {
                    if(!isHasPermission()) requestPermission();
                    startActivityForResult(new Intent(getActivity(), CaptureActivity.class),
                            CAPTURE_ACTIVITY_RESULT);
                }
            }
        });

        // 设置手势监听
        setGestureListener();

        return view;
    }

    /*
     * 重构函数
     */
    // 控件点击响应
    @Override
    public void onClick(View v) {
        Intent intent;
        switch (v.getId()){
            case R.id.examine_compact_layout:  // 查看合同
                Log.i(TAG, "You have clicked the examine compact button");
                intent = new Intent(getActivity(), CheckCompactActivity.class);
                intent.putExtra("projectId", mProjectInfoList.get(currentSelectedProject).getProjectId());
                startActivity(intent);
                break;
            case R.id.project_pre_apply_layout:  // 预验收申请
                Log.i(TAG, "You have clicked the pre_apply compact button");
                intent = new Intent(getActivity(), UploadImageFTPActivity.class);
                intent.putExtra(PROJECT_ID, mProjectInfoList.get(currentSelectedProject).getProjectId());
                intent.putExtra(UPLOAD_IMAGE_TYPE, UPLOAD_BASKETS_PRE_INSTALL_IMAGE);
                startActivityForResult(intent, UPLOAD_BASKET_IMAGE_RESULT);
                break;
            case R.id.send_examine_certificate_layout: // 上传/查看安监证书(已放弃)
                Log.i(TAG, "You have clicked the examine certification button");
                intent = new Intent(getActivity(), UploadImageFTPActivity.class);
                intent.putExtra(PROJECT_ID, mProjectInfoList.get(currentSelectedProject).getProjectId());
                intent.putExtra(UPLOAD_IMAGE_TYPE, UPLOAD_CERTIFICATE_IMAGE);
                startActivityForResult(intent, UPLOAD_CERTIFICATE_IMAGE_RESULT);
                break;
            case R.id.rl_get_repair_info: // 获取全部保修记录
                Log.i(TAG, "You have clicked the repair information button");
                intent = new Intent(getActivity(), RepairInfoListActivity.class);
                intent.putExtra(PROJECT_ID, mProjectInfoList.get(currentSelectedProject).getProjectId());
                intent.putExtra(PROJECT_NAME, mProjectInfoList.get(currentSelectedProject).getProjectName());
                startActivity(intent);
                break;
            case R.id.pre_stop_info_layout:
                Log.i(TAG, "You have clicked the pre stop info button");
                intent = new Intent(getActivity(), UploadPreStopInfoActivity.class);
                intent.putExtra(PROJECT_ID, mProjectInfoList.get(currentSelectedProject).getProjectId());
                intent.putExtra(PROJECT_NAME, mProjectNameList.get(currentSelectedProject));
                intent.putExtra(BASKETS_NUM, mgBasketStatementClassifiedList.get(3).size());
                startActivity(intent);
                break;
        }
    }
    // 溢出栏消息响应
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.current_project:  // 当前项目详情
                Log.i(TAG, "You have clicked the project more");
                Intent intent = new Intent(getActivity(), ProDetailActivity.class);
                intent.putExtra("projectId", mProjectInfoList.get(currentSelectedProject).getProjectId());
                startActivity(intent);
                break;
            case R.id.switch_project:  // 切换项目
                Log.i(TAG, "You have clicked the switch project");
                showSingleAlertDialog();
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }
    // 初始化溢出栏弹窗
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // 这里设置另外的menu
        menu.clear();
        inflater.inflate(R.menu.area_admin_project_more, menu);

        //隐藏menu
        MenuItem menuItem;
        menuItem = menu.findItem(R.id.switch_project);
        menuItem.setVisible(false);

        // 通过反射让menu的图标可见
        if (menu != null) {
            if (menu.getClass() == MenuBuilder.class) {
                try {
                    Method m = menu.getClass().getDeclaredMethod("setOptionalIconsVisible", Boolean.TYPE);
                    m.setAccessible(true);
                    m.invoke(menu, true);
                } catch (Exception e) {

                }
            }
        }

        //这一行不能忘，否则看不到图标
        //拿到ActionBar后，可以进行设置
        ((AppCompatActivity) getActivity()).getSupportActionBar();
        super.onCreateOptionsMenu(menu, inflater);
    }

    /*
     * 处理Activity返回结果
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
            case CAPTURE_ACTIVITY_RESULT:  // 扫描工人二维码名片返回结果
                if(resultCode == RESULT_OK){
                    String basketId = data.getStringExtra(QR_CODE_RESULT);
                    Log.i(TAG, "QR_Content: "+ basketId);
                    if(isBasketInProject(basketId))  // 已存在于项目中
                        DialogToast("提示", "吊篮已经在项目中").show();
                    else  // 待添加
                        areaAdminAddBasketIntoProject(basketId);
                }
                break;
            case UPLOAD_BASKET_IMAGE_RESULT:  // 上传预验收图片返回
                if(resultCode == RESULT_OK) {
                    // 更新进度页面
                    proAdminGetAllProject();
                }
                break;
            case UPLOAD_CERTIFICATE_IMAGE_RESULT:  // 上传安监证书返回值
                break;
            default:
                break;
        }
    }

    /*
     * 网络相关
     */
    // 获取项目管理员的项目详情
    private void proAdminGetAllProject(){
        BaseOkHttpClient.newBuilder()
                .addHeader("Authorization", mToken)
                .addParam("userId", mUserInfo.getUserId())
                .get()
                .url(PRO_ADMIN_GET_PROINFO)
                .build()
                .enqueue(new BaseCallBack() {
                    @Override
                    public void onSuccess(Object o) {
                        Log.d(TAG, "成功获取项目管理员的项目信息");
                        Message message = new Message();
                        message.what = UPDATE_PRO_ADMIN_PROJECT_MSG;  // 更新项目信息

                        message.obj = o.toString();
                        mHandler.sendMessage(message);
                    }

                    @Override
                    public void onError(int code) {
                        Log.d(TAG, "获取项目信息错误，错误编码："+code);

                    }

                    @Override
                    public void onFailure(Call call, IOException e) {
                        Log.d(TAG, "获取项目信息失败");
                    }
                });
    }

    // 解析项目列表
    private void parseProjectInfo(String responseData){
        JSONObject jsonObject = JSON.parseObject(responseData);
        String projectStr = jsonObject.getString("project");
        ProjectInfo projectInfo= JSON.parseObject(projectStr,ProjectInfo.class);
        mProjectNameList.add(projectInfo.getProjectName());
        mProjectInfoList.add(projectInfo);

    }
    // 默认登录上次项目号
    private void setLastLoginProjectId(){
        for(int i=0; i<mProjectInfoList.size(); i++){
            if(mLastProjectId.equals(mProjectInfoList.get(i).getProjectId())){
                currentSelectedProject = i;
                return;
            }
        }
        currentSelectedProject = 0;
    }

    // 获取项目对应的所有吊篮信息
    private void areaAdminGetAllBasket(){
        BaseOkHttpClient.newBuilder()
                .addHeader("Authorization", mToken)
                .addParam("projectId", mProjectInfoList.get(currentSelectedProject).getProjectId())
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
                        message.what = UPDATE_PROJECT_AND_BASKET_MSG;
                        message.obj = responseData;
                        mHandler.sendMessage(message);

                        mSmartRefreshLayout.finishRefresh(100);
                    }

                    @Override
                    public void onError(int code) {
                        Log.i(TAG, "错误：" + code);
                        switch (code){
                            case 401: // 未授权
                                ToastUtil.showToastTips(getActivity(), "登录已过期，请重新登陆");
                                startActivity(new Intent(getActivity(), LoginActivity.class));
                                getActivity().finish();
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
        for(int i=0; i<mStateLists.size();i++){
            mgBasketStatementClassifiedList.add(new ArrayList<MgBasketStatement>());
        }
        // 将数据装载进对应位置
        for(int i=0; i<mgBasketStatements.size(); i++){
            MgBasketStatement mgBasketStatement = mgBasketStatements.get(i);
            mgBasketStatementClassifiedList.get(Integer.valueOf(mgBasketStatement.getBasketStatement())).
                    add(mgBasketStatement);
        }
        mgBasketStatementClassifiedList.get(0).addAll(mgBasketStatements);
    }

    // 将吊篮添加至项目
    private void areaAdminAddBasketIntoProject(String basketId){
        BaseOkHttpClient.newBuilder()
                .addHeader("Authorization", mToken)
                .addParam("projectId", mProjectInfoList.get(currentSelectedProject).getProjectId())
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
                                ToastUtil.showToastTips(getActivity(), "登陆已过期，请重新登录");
                                startActivity(new Intent(getActivity(), LoginActivity.class));
                                getActivity().finish();
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
     * 业务逻辑相关
     */
    // 判断扫描到的吊篮是否在项目中
    private boolean isBasketInProject(String basketId){
        ProjectInfo projectInfo = mProjectInfoList.get(currentSelectedProject);
        String basketList = projectInfo.getBoxList();
        basketList = (basketList==null) ? "":basketList;
        String[] basketIds = basketList.split(",");
        for(int i=0; i<basketIds.length; i++){
            if(basketId.equals(basketIds[i]))
                return true;
        }
        return false;
    }

    /*
     * 初始化状态筛选栏
     */
    private void initStateList(){
        // mStateLists = new ArrayList<>();
        mStateLists.add("进度");
        mStateLists.add("待安装");
        mStateLists.add("安装审核");
        mStateLists.add("使用中");
        mStateLists.add("待报停");
        mStateLists.add("报停审核");
    }

    /*
     * 初始化进度轴
     */
    public void initProjectScheduleList(){
        mProjectScheduleList.add("立项");
        mProjectScheduleList.add("配置清单");
        mProjectScheduleList.add("安装");
        mProjectScheduleList.add("安监证书");
        mProjectScheduleList.add("进行中");
        mProjectScheduleList.add("结束");
    }

    /*
     * 设置手势监听
     */
    private void setGestureListener(){
        mGestureDetector = new GestureDetector(getActivity(), mGestureListener);
        mGestureDetector.setIsLongpressEnabled(true);
        mGestureDetector.setOnDoubleTapListener(mGestureListener);

        myOnTouchListener = new ProAdminPreActivity.MyOnTouchListener() {

            @Override
            public boolean onTouch(MotionEvent ev) {
                return mGestureDetector.onTouchEvent(ev);
            }
        };
        ((ProAdminPreActivity)getActivity()).registerMyOnTouchListener(myOnTouchListener);
    }

    /*
     * 滑动、触摸监听类
     */
    public class SVCGestureListener implements GestureDetector.OnGestureListener,
            GestureDetector.OnDoubleTapListener {

        @Override
        public boolean onDown(MotionEvent e) {
            return false;
        }

        @Override
        public void onShowPress(MotionEvent e) {

        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            return false;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2,float distanceX, float distanceY) {
            return false;
        }

        @Override
        public void onLongPress(MotionEvent e) {
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,float velocityY) {
            if(Math.abs(e1.getX()-e2.getX()) > Math.abs(e1.getY()-e2.getY())) {
                // 水平滑动的距离大于竖直滑动的距离
                if (e1.getX() - e2.getX() > FLIP_DISTANCE) {
                    Log.i(TAG, "向左滑...");
                    int tmp_position = pre_selectedPosition + 1;
                    pre_selectedPosition = (tmp_position < mStateLists.size()) ? tmp_position : (mStateLists.size() - 1);
                    mgStateAdapter.setSelectedPosition(pre_selectedPosition);
                    mHandler.sendEmptyMessage(UPDATE_BASKET_STATEMENT_MSG);  // 更新列表
                    return true;
                }
                if (e2.getX() - e1.getX() > FLIP_DISTANCE) {
                    Log.i(TAG, "向右滑...");
                    int tmp_position = pre_selectedPosition - 1;
                    pre_selectedPosition = (tmp_position > 0) ? tmp_position : 0;
                    mgStateAdapter.setSelectedPosition(pre_selectedPosition);
                    mHandler.sendEmptyMessage(UPDATE_BASKET_STATEMENT_MSG);  // 更新列表
                    return true;
                }
            }else {
                if (e1.getY() - e2.getY() > FLIP_DISTANCE) {
                    Log.i(TAG, "向上滑...");
                    return true;
                }
                if (e2.getY() - e1.getY() > FLIP_DISTANCE) {
                    Log.i(TAG, "向下滑...");
                    return true;
                }
            }

            Log.d(TAG, e2.getX() + " " + e2.getY());

            return false;
        }
        //点击的时候执行
        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            //          toolbar.changeVisibility();
            if(state == State.VISIBLE) {

            } else {

            }
            return true;
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            return false;
        }

        @Override
        public boolean onDoubleTapEvent(MotionEvent e) {
            return false;
        }
    }

    /*
     * UI 更新类
     */
    // 主体页面显示逻辑控制
    public void updateBodyContentView(){
        if(pre_selectedPosition > 0) {  // 吊篮
            mMgProjectLinearLayout.setVisibility(View.GONE); // 隐藏项目管理视图
            if (mgBasketStatementList.size() == 0) {  // 显示无操作吊篮
                mListRelativeLayout.setVisibility(View.GONE);
                mBlankRelativeLayout.setVisibility(View.VISIBLE);
                mBlankHintTextView.setText("您还没有相关的吊篮");
            } else {                                   // 显示可操作吊篮列表
                mBlankRelativeLayout.setVisibility(View.GONE);
                mListRelativeLayout.setVisibility(View.VISIBLE);
                mgBasketStatementAdapter.notifyDataSetChanged();
            }
            // 添加吊篮按钮
            if(pre_selectedPosition == 1)
                mAddBasketImageView.setVisibility(View.GONE);
            else
                mAddBasketImageView.setVisibility(View.GONE);
        }else if(pre_selectedPosition == 0){ // 项目
            mListRelativeLayout.setVisibility(View.GONE);  // 隐藏列表
            mAddBasketImageView.setVisibility(View.GONE);  // 隐藏添加按钮

            if(mProjectNameList.size() == 0){  // 显示无项目操作
                mMgProjectLinearLayout.setVisibility(View.GONE);
                mBlankRelativeLayout.setVisibility(View.VISIBLE);
                mBlankHintTextView.setText("您还没有相关的项目");
            }else{                               // 显示项目进度
                mBlankRelativeLayout.setVisibility(View.GONE);
                mMgProjectLinearLayout.setVisibility(View.VISIBLE);
            }
        }
    }
    // 项目进度页面更新
    public void updateProjectContentView(){
        if(mProjectInfoList.size() < 1){
            DialogToast("提示", "暂无项目");
            return;
        }

        ProjectInfo projectInfo = mProjectInfoList.get(currentSelectedProject);
        mProjectTitleTv.setText(projectInfo.getProjectName());
        mProjectIdTextView.setText(projectInfo.getProjectId());
        mProjectNameTextView.setText(projectInfo.getProjectName());
        switch(projectInfo.getProjectState()){
            case "1":
            case "0":/* 立项、安装 */
                mProjectStartTextView.setText("暂未开始");
                /*
                if(projectInfo.getBoxList()==null || projectInfo.getBoxList().equals("")){ // 无吊篮
                    currentProjectScheduleFlag = 1;
                    mProjectScheduleTimeLine.setPointStrings(mProjectScheduleList, currentProjectScheduleFlag);  //
                    mPreApplyRelativeLayout.setVisibility(View.GONE);
                }
                else{  // 很多吊篮
                    currentProjectScheduleFlag = 2;
                    mProjectScheduleTimeLine.setPointStrings(mProjectScheduleList, currentProjectScheduleFlag);   //
                    mPreApplyRelativeLayout.setVisibility(View.VISIBLE);
                    mPreApplyCountTextView.setVisibility(View.VISIBLE);
                }*/
                currentProjectScheduleFlag = 1;
                mProjectScheduleTimeLine.setPointStrings(mProjectScheduleList, currentProjectScheduleFlag);  //
                mPreApplyRelativeLayout.setVisibility(View.GONE);
                mSendOrExamineCertificateRelativeLayout.setVisibility(View.GONE);
                break;
            case "11": // 清单待配置
                currentProjectScheduleFlag = 2;
                mProjectScheduleTimeLine.setStep(currentProjectScheduleFlag);  //
                mPreApplyRelativeLayout.setVisibility(View.GONE);
                mSendOrExamineCertificateRelativeLayout.setVisibility(View.GONE);
                break;
            case "12": // 清单待审核
                currentProjectScheduleFlag = (float)2.5;
                mProjectScheduleTimeLine.setStep(currentProjectScheduleFlag);  //
                mPreApplyRelativeLayout.setVisibility(View.GONE);
                mSendOrExamineCertificateRelativeLayout.setVisibility(View.GONE);
                break;
            case "2": /*  预安装申请图片 */
                if(projectInfo.getStoreOut() == null || projectInfo.getStoreOut().equals("")){
                    // 尚未上传预申请图片
                    currentProjectScheduleFlag = 3;
                    mProjectScheduleTimeLine.setStep(currentProjectScheduleFlag);
                    mPreApplyRelativeLayout.setVisibility(View.VISIBLE);
                    mPreApplyCountTextView.setVisibility(View.VISIBLE);
                    mSendOrExamineCertificateRelativeLayout.setVisibility(View.GONE);
                }else{
                    // 已经上传预验收申请图片
                    currentProjectScheduleFlag = (float)3.5;
                    mProjectScheduleTimeLine.setStep(currentProjectScheduleFlag);
                    mPreApplyRelativeLayout.setVisibility(View.VISIBLE);
                    mPreApplyCountTextView.setVisibility(View.GONE);
                    //mPreApplyRelativeLayout.setClickable(false);
                    mSendOrExamineCertificateRelativeLayout.setVisibility(View.GONE);
                    mSendOrExamineCertificateTextView.setText("上传安监证书");
                    mSendOrExamineCertificateCountTextView.setVisibility(View.VISIBLE);
                }
                break;
            case "21": /* 上传安监证书 */
                if(projectInfo.getProjectCertUrl()==null || projectInfo.getProjectCertUrl().equals("")){ // 尚未上传安监证书
                    // 尚未上传
                    currentProjectScheduleFlag = (float)3.5;
                    mProjectScheduleTimeLine.setStep(currentProjectScheduleFlag);
                    mPreApplyRelativeLayout.setVisibility(View.VISIBLE);
                    mPreApplyCountTextView.setVisibility(View.GONE);
                    //mPreApplyRelativeLayout.setClickable(false);
                    mSendOrExamineCertificateRelativeLayout.setVisibility(View.GONE);
                    mSendOrExamineCertificateTextView.setText("上传安监证书");
                    mSendOrExamineCertificateCountTextView.setVisibility(View.VISIBLE);
                }else{
                    currentProjectScheduleFlag = (float)4;
                    mProjectScheduleTimeLine.setStep(currentProjectScheduleFlag);
                    mPreApplyRelativeLayout.setVisibility(View.VISIBLE);
                    mPreApplyCountTextView.setVisibility(View.GONE);
                    //mPreApplyRelativeLayout.setClickable(false);
                    mSendOrExamineCertificateRelativeLayout.setVisibility(View.GONE);
                    mSendOrExamineCertificateTextView.setText("查看安监证书");
                    mSendOrExamineCertificateCountTextView.setVisibility(View.GONE);
                }
                break;
            case "3": /* 使用中 */
                currentProjectScheduleFlag = (float)5;
                mProjectScheduleTimeLine.setStep(currentProjectScheduleFlag);
                mProjectStartTextView.setText(projectInfo.getProjectStart());
                mPreApplyCountTextView.setVisibility(View.GONE);
                mSendOrExamineCertificateRelativeLayout.setVisibility(View.GONE);
                mSendOrExamineCertificateTextView.setText("查看安监证书");
                mSendOrExamineCertificateCountTextView.setVisibility(View.GONE);
                mUploadPreStopInfoRelativeLayout.setVisibility(View.VISIBLE);
                break;
            case "4": /* 结束 */
                currentProjectScheduleFlag = (float)6;
                mProjectScheduleTimeLine.setStep(currentProjectScheduleFlag);
                mProjectStartTextView.setText(projectInfo.getProjectStart());
                mPreApplyCountTextView.setVisibility(View.GONE);
                mSendOrExamineCertificateRelativeLayout.setVisibility(View.GONE);
                mSendOrExamineCertificateTextView.setText("查看安监证书");
                mSendOrExamineCertificateCountTextView.setVisibility(View.GONE);
                break;
        }
    }

    // 弹出项目选择框
    public void showSingleAlertDialog(){
        final AlertDialog.Builder alertBuilder = new AlertDialog.Builder(getContext());
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
                if(mProjectNameList.size() > 0)  // 当且仅当存在且选择项目时，从网络获取数据
                    areaAdminGetAllBasket(); // 获取项目中吊篮数据
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
     * 生命周期函数
     */
    protected void onAttachToContext(Context context) {
        //d o something
        mUserInfo = ((ProAdminPreActivity) context).pushUserInfo();
        mToken = ((ProAdminPreActivity) context).pushToken();
        mLastProjectId = ((ProAdminPreActivity) context).pushProjectId();
    }
    @TargetApi(23)
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        onAttachToContext(context);
    }
    @SuppressWarnings("deprecation")
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            onAttachToContext(activity);
        }
    }
    // 销毁
    @Override
    public void onDestroy() {
        super.onDestroy();
        if(mProjectInfoList.size()>0) {
            mPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
            editor = mPref.edit();
            editor.putString("projectId", mProjectInfoList.get(currentSelectedProject).getProjectId());
            editor.commit();
        }
        ((ProAdminPreActivity) getActivity()).unregisterMyOnTouchListener(myOnTouchListener);
    }

    /*
     * 用xxpermissions申请权限
     */
    // 申请权限
    private void requestPermission() {
        XXPermissions.with(getActivity())
                .constantRequest() //可设置被拒绝后继续申请，直到用户授权或者永久拒绝
                .permission(Permission.CAMERA) //支持请求6.0悬浮窗权限8.0请求安装权限
                .request(new OnPermission() {
                    @Override
                    public void hasPermission(List<String> granted, boolean isAll) {
                        if (isAll) {
                            onResume();
                        }else {
                            Toast.makeText(getActivity(),
                                    "必须同意所有的权限才能使用本程序", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void noPermission(List<String> denied, boolean quick) {
                        if(quick) {
                            Toast.makeText(getActivity(), "被永久拒绝授权，请手动授予权限",
                                    Toast.LENGTH_SHORT).show();
                            // 如果是被永久拒绝就跳转到应用权限系统设置页面
                            XXPermissions.gotoPermissionSettings(getActivity());
                        }else {
                            Toast.makeText(getActivity(), "获取权限失败",
                                    Toast.LENGTH_SHORT).show();
                            getActivity().finish();
                        }
                    }
                });
    }

    // 是否有权限：摄像头
    private boolean isHasPermission() {
        if (XXPermissions.isHasPermission(getActivity(), Permission.CAMERA))
            return true;
        return false;
    }

    /*
     * 提示弹框
     */
    private CommonDialog DialogToast(String mTitle, String mMsg){
        return new CommonDialog(getActivity(), R.style.dialog, mMsg,
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
    private String getRandomStatement(){
        int num = (int) (Math.random()*5 + 1);
        return String.valueOf(num);
    }

}
