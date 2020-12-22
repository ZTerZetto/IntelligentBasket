package com.automation.zzx.intelligent_basket_demo.activity.areaAdmin;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.automation.zzx.intelligent_basket_demo.R;
import com.automation.zzx.intelligent_basket_demo.activity.InstallInfo.BasketInstallInfoActivity;
import com.automation.zzx.intelligent_basket_demo.adapter.areaAdmin.MgStateAdapter;
import com.automation.zzx.intelligent_basket_demo.adapter.basket.InstallInfoAdapter;
import com.automation.zzx.intelligent_basket_demo.entity.AppConfig;
import com.automation.zzx.intelligent_basket_demo.entity.MgBasketInstallInfo;
import com.automation.zzx.intelligent_basket_demo.entity.UserInfo;
import com.automation.zzx.intelligent_basket_demo.utils.okhttp.BaseCallBack;
import com.automation.zzx.intelligent_basket_demo.utils.okhttp.BaseOkHttpClient;
import com.scwang.smartrefresh.header.BezierCircleHeader;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import okhttp3.Call;


public class InstallInfoListActivity extends AppCompatActivity implements View.OnTouchListener{

    private final static String TAG = "InstallInfoListActivity";

    // 页面消息传递
    public final static String PROJECT_ID = "project_id";
    public final static String BASKET_INFO = "basket_info";

    // Handler 消息
    private final static int SWITCH_BASKET_STATE_MSG = 101;

    //页面返回变量
    private final static int UPLOAD_CERTIFICATE_IMAGE_RESULT = 4;  // 上传安检证书返回页面

    // 吊篮状态选择
    private GridView mBasketStateGv;  // 吊篮状态
    private List<String> mStateLists; // 状态名称
    private MgStateAdapter mgStateAdapter; // 适配器
    private int pre_selectedPosition = 0;

    // 主体
    //搜索框控件
    private SearchView mSearchView;
    /* 主体内容部分*/
    private SmartRefreshLayout mSmartRefreshLayout; // 下拉刷新
    // 工单列表视图
    private RelativeLayout mListRelativeLayout;
    private RecyclerView mBasketListRecyclerView;
    private List<List<MgBasketInstallInfo>>  mBasketSummaryList = new ArrayList<>();
    private List<MgBasketInstallInfo> mBasketSelectedList = new ArrayList<>();
    private InstallInfoAdapter mBasketAdapter;
    private AutoCompleteTextView mAutoCompleteTextView;//搜索输入框
    private ImageView mDeleteButton;//搜索框中的删除按钮


    // 上下左右滑动监听
    private static final int FLING_MIN_DISTANCE = 50;   //最小距离
    private static final int FLING_MIN_VELOCITY = 0;   //最小速度
    private GestureDetector mGestureDetector;

    // 空空如也
    private RelativeLayout noRecordListRelativeLayout;

    // 用户登录信息相关
    private UserInfo mUserInfo;
    private String mToken;
    private SharedPreferences mPref;
    // 业务数据
    private String mProjectId;

    /*
     * 消息函数
     */
    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch(msg.what) {
                case SWITCH_BASKET_STATE_MSG:  // 切换吊篮状态
                    mBasketSelectedList.clear();
                    if(mBasketSummaryList.size() > 0 ){
                        mBasketSelectedList.addAll(mBasketSummaryList.get(pre_selectedPosition));
                        mBasketAdapter.notifyDataSetChanged();
                    }
                    if(mBasketSelectedList.size()==0){  // 暂无工单，则隐藏
                        mListRelativeLayout.setVisibility(View.GONE);
                        noRecordListRelativeLayout.setVisibility(View.VISIBLE);
                    }else{
                        noRecordListRelativeLayout.setVisibility(View.GONE);
                        mListRelativeLayout.setVisibility(View.VISIBLE);
                    }
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
        setContentView(R.layout.activity_install_info_list);

        getUserInfo();
        getBasketInfoFromInternet();
        initView();

    }
    // 获取用户数据
    private void getUserInfo(){
        // 从本地获取数据
        mPref = PreferenceManager.getDefaultSharedPreferences(this);
        mUserInfo = new UserInfo();
        mUserInfo.setUserId(mPref.getString("userId", ""));
        mUserInfo.setUserPhone(mPref.getString("userPhone", ""));
        mUserInfo.setUserRole(mPref.getString("userRole", ""));
        mToken = mPref.getString("loginToken","");

        //获取当前项目ID
        Intent intent = getIntent();
        mProjectId = intent.getStringExtra((ProjectOperatingActivity.PROJECT_ID));

    }

    private void initView() {

        // 顶部导航栏
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        TextView titleText = (TextView) findViewById(R.id.toolbar_title);
        toolbar.setTitle("安装进度");
        titleText.setText("");
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true); //设置返回键可用

        //搜索框
        mSearchView=findViewById(R.id.view_search);
        mAutoCompleteTextView=mSearchView.findViewById(R.id.search_src_text);
        mDeleteButton=mSearchView.findViewById(R.id.search_close_btn);
        mDeleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAutoCompleteTextView.setText("");
            }
        });
        mSearchView.setIconifiedByDefault(false);//设置搜索图标是否显示在搜索框内
        mAutoCompleteTextView.clearFocus(); //默认失去焦点
        mSearchView.setImeOptions(3);//设置输入法搜索选项字段，1:回车2:前往3:搜索4:发送5:下一項6:完成
//      mSearchView.setInputType(1);//设置输入类型
//      mSearchView.setMaxWidth(200);//设置最大宽度
        mSearchView.setQueryHint("输入吊篮ID或描述详情");//设置查询提示字符串
        mSearchView.setSubmitButtonEnabled(true);//设置是否显示搜索框展开时的提交按钮
        mAutoCompleteTextView.setTextColor(Color.GRAY);
        //设置SearchView下划线透明
        setUnderLinetransparent(mSearchView);
        setListener();

        /*
         主体内容部分
          */
        // 下拉刷新
        mSmartRefreshLayout = (SmartRefreshLayout)findViewById(R.id.smart_refresh_layout);
        mSmartRefreshLayout.setRefreshHeader(new BezierCircleHeader(this)); // 设置 Header 为 贝塞尔雷达 样式
        mSmartRefreshLayout.setPrimaryColorsId(R.color.smart_loading_background_color);
        mSmartRefreshLayout.setOnRefreshListener(new OnRefreshListener() { // 添加下拉刷新监听
            @Override
            public void onRefresh(RefreshLayout refreshlayout) {
                getBasketInfoFromInternet();
            }
        });

        // 状态选择栏初始化
        mBasketStateGv = (GridView) findViewById(R.id.mg_basket_state);
        mStateLists = new ArrayList<>();
        initStateList();
        mgStateAdapter = new MgStateAdapter(this, R.layout.item_basket_state_switch, mStateLists);
        mgStateAdapter.setSelectedPosition(pre_selectedPosition);
        mBasketStateGv.setAdapter(mgStateAdapter);
        mBasketStateGv.setOnItemClickListener(new AdapterView.OnItemClickListener() {  // 点击选择不同状态的工单
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                pre_selectedPosition = position;
                mgStateAdapter.setSelectedPosition(pre_selectedPosition);
                mHandler.sendEmptyMessage(SWITCH_BASKET_STATE_MSG);
            }
        });

        // 无工单
        noRecordListRelativeLayout = (RelativeLayout) findViewById(R.id.basket_no_avaliable);

        // 工单列表
        mListRelativeLayout = (RelativeLayout) findViewById(R.id.basket_avaliable_rl);
        mBasketListRecyclerView = (RecyclerView) findViewById(R.id.basket_rv) ;
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        mBasketListRecyclerView.setLayoutManager(layoutManager);
        mBasketAdapter = new InstallInfoAdapter(this, mBasketSelectedList);
        mBasketListRecyclerView.setAdapter(mBasketAdapter);
        mBasketAdapter.setOnItemClickListener(new InstallInfoAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                // 点击item响应
                Log.i(TAG, "You have clicked the "+position+" item");
                Intent intent = new Intent(InstallInfoListActivity.this, BasketInstallInfoActivity.class);
                intent.putExtra(PROJECT_ID, mProjectId);
                intent.putExtra(BASKET_INFO, mBasketSelectedList.get(position));
                startActivityForResult(intent,UPLOAD_CERTIFICATE_IMAGE_RESULT);
            }
        });
//        initBasketList();

        // 设置手势监听
        mGestureDetector = new GestureDetector(this, myGestureListener);
        mSmartRefreshLayout.setOnTouchListener(this); // 将主容器的监听交给本activity，本activity再交给mGestureDetector
        mSmartRefreshLayout.setLongClickable(true); // 必需设置这为true 否则也监听不到手势
    }

    private void setListener(){
/*
        // 设置搜索文本监听
        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            //当点击搜索按钮时触发该方法
            @Override
            public boolean onQueryTextSubmit(String query) {
                showAlarmInfoList.clear();
                if (query == null  || query.equals("")) {
                    showAlarmInfoList.addAll(alarmInfoList);
                }else{
                    for (int i = 0; i < alarmInfoList.size(); i++) {
                        MgBasketStatement alarmInfo = alarmInfoList.get(i);
                        if (alarmInfo.getDevice_id().contains(query)) {
                            showAlarmInfoList.add(alarmInfo);
                        } else if (alarmInfo.getAlarm_detail().contains(query)) {
                            showAlarmInfoList.add(alarmInfo);
                        }
                    }
                }
                mHandler.sendEmptyMessage(UPDATE_LIST_INFO);
                return true;
            }

            //当搜索内容改变时触发该方法
            @Override
            public boolean onQueryTextChange(String newText) {
                showAlarmInfoList.clear();
                if (newText == null || newText.equals("")) {
                    showAlarmInfoList.addAll(alarmInfoList);
                }else{
                    for (int i = 0; i < alarmInfoList.size(); i++) {
                        AlarmInfo alarmInfo = alarmInfoList.get(i);
                        if (alarmInfo.getDevice_id().contains(newText)) {
                            showAlarmInfoList.add(alarmInfo);
                        } else if (alarmInfo.getAlarm_detail().contains(newText)) {
                            showAlarmInfoList.add(alarmInfo);
                        }
                    }
                }
                mHandler.sendEmptyMessage(UPDATE_LIST_INFO);
                return true;
            }
        });*/
    }


    /*
     * 后台通信
     */
    private void getBasketInfoFromInternet(){
        BaseOkHttpClient.newBuilder()
                .addHeader("Authorization", mToken)
                .addParam("projectId", mProjectId)
                .get()
                .url(AppConfig.GET_PROJECT_BY_PROJECTID)
                .build()
                .enqueue(new BaseCallBack() {
                    @Override
                    public void onSuccess(Object o) {
                        String data = o.toString();
                        JSONObject jsonObject = JSON.parseObject(data);
                        boolean isLogin = jsonObject.getBooleanValue("isLogin");
                        if(isLogin)
                            parseProjectDetails(jsonObject.getString("info"));
                    }

                    @Override
                    public void onError(int code) {
                        Log.i(TAG, "Error:" + code);
                    }

                    @Override
                    public void onFailure(Call call, IOException e) {
                        Log.i(TAG, "Failure:" + e.toString());
                    }
                });
    }

    private void parseProjectDetails(String data){
        mBasketSummaryList.clear();
        initBasketList();

        JSONObject jsonObject = JSON.parseObject(data);
        Iterator<String> basketIds = jsonObject.keySet().iterator();
        while(basketIds.hasNext()) {
            String basketId = basketIds.next();
            JSONObject basketObject = JSON.parseObject(jsonObject.getString(basketId));
            JSONObject basketInfo = JSON.parseObject(basketObject.getString(basketId));
            MgBasketInstallInfo basket = new MgBasketInstallInfo();
            basket.setBasketId(basketId);
            basket.setUserState(basketObject.getIntValue(basketId+"_userState"));
            basket.setDeviceState(basketObject.getIntValue(basketId+"_deviceState"));
            //获取吊篮项目中状态
            basket.setStateInPro(basketObject.getIntValue(basketId+"_stateInPro"));
            basket.setSiteNo(basketObject.getString(basketId+"_siteNo"));

            basket.setProjectId(mProjectId);
            JSONObject installTeam = JSON.parseObject(basketObject.getString("installTeamName"));
            basket.setUserName(installTeam.getString("userName"));

            basket.setUserId(basketInfo.getString("user_id"));
            basket.setPicFlag(basketInfo.getIntValue("pic_flg"));
            basket.setFlag(basketInfo.getIntValue("flag"));
            basket.setStartTime(basketInfo.getString("start_time").substring(0,10));
            if(basketInfo.getString("end_time") != null) {
                basket.setEndTime(basketInfo.getString("end_time").substring(0,10));
            }

            int flag = basketInfo.getIntValue("flag");   // flag: 0 进行中 1 未完成
            if(flag==0) mBasketSummaryList.get(0).add(basket);
            else if(flag==1) mBasketSummaryList.get(1).add(basket);
        }
        mHandler.sendEmptyMessage(SWITCH_BASKET_STATE_MSG);
    }

    /**设置SearchView下划线透明**/
    private void setUnderLinetransparent(SearchView searchView){
        try {
            Class<?> argClass = searchView.getClass();
            Field ownField = argClass.getDeclaredField("mSearchPlate");
            ownField.setAccessible(true);
            View mView = (View) ownField.get(searchView);
            mView.setBackgroundColor(Color.TRANSPARENT);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
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

    /* 手势监听
     */
    /*
     * 手势监听类
     */
    GestureDetector.SimpleOnGestureListener myGestureListener = new GestureDetector.SimpleOnGestureListener(){
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            float x = e1.getX()-e2.getX();
            float x2 = e2.getX()-e1.getX();

            if(x>FLING_MIN_DISTANCE && Math.abs(velocityX)>FLING_MIN_VELOCITY){
                int tmp_position = pre_selectedPosition + 1;
                pre_selectedPosition = (tmp_position < mStateLists.size()) ? tmp_position : (mStateLists.size() - 1);
                mgStateAdapter.setSelectedPosition(pre_selectedPosition);
                mHandler.sendEmptyMessage(SWITCH_BASKET_STATE_MSG);

            }else if(x2>FLING_MIN_DISTANCE && Math.abs(velocityX)>FLING_MIN_VELOCITY){
                int tmp_position = pre_selectedPosition - 1;
                pre_selectedPosition = (tmp_position > 0) ? tmp_position : 0;
                mgStateAdapter.setSelectedPosition(pre_selectedPosition);
                mHandler.sendEmptyMessage(SWITCH_BASKET_STATE_MSG);
            }
            return false;
        };
    };

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        return mGestureDetector.onTouchEvent(event);
    }

    /*Others
     */
    private void initStateList(){
        mStateLists.add("进行中");
        mStateLists.add("已完成");
    }
    private void initBasketList() {
        for (int i = 0; i < 2; i++) {
            mBasketSummaryList.add(new ArrayList<MgBasketInstallInfo>());
        }
    }

    /*
     * 处理Activity返回结果
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case UPLOAD_CERTIFICATE_IMAGE_RESULT:  // 上传安监证书返回值
                if(resultCode == RESULT_OK) {
                    getBasketInfoFromInternet();
                }
                break;

            default:
                break;
        }
    }


}
