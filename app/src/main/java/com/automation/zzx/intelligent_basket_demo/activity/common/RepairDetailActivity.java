package com.automation.zzx.intelligent_basket_demo.activity.common;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.automation.zzx.intelligent_basket_demo.R;
import com.automation.zzx.intelligent_basket_demo.adapter.worker.ImageGridAdapter;
import com.automation.zzx.intelligent_basket_demo.application.CustomApplication;
import com.automation.zzx.intelligent_basket_demo.entity.AppConfig;
import com.automation.zzx.intelligent_basket_demo.entity.MessageInfo;
import com.automation.zzx.intelligent_basket_demo.entity.RepairInfo;
import com.automation.zzx.intelligent_basket_demo.fragment.proAdmin.ProAdminMessageFragment;
import com.automation.zzx.intelligent_basket_demo.utils.okhttp.BaseCallBack;
import com.automation.zzx.intelligent_basket_demo.utils.okhttp.BaseOkHttpClient;
import com.automation.zzx.intelligent_basket_demo.widget.ScaleImageView;
import com.automation.zzx.intelligent_basket_demo.widget.image.WebImage;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import okhttp3.Call;

/**
 * Created by pengchenghu on 2019/2/23.
 * Author Email: 15651851181@163.com
 * Describe: 吊篮报修详情
 * limits:
 */

public class RepairDetailActivity extends AppCompatActivity implements View.OnClickListener {

    private final static String TAG = "RepairDetailActivity";

    // Handler 消息处理
    private final static int UPDATE_VIEW_MSG = 1;

    // 控件
    private TextView mProjectIdTv; // 项目ID
    private TextView mProjectNameTv; // 项目名称
    private TextView mBasketIdTv; // 吊篮编号
    private TextView mRepairDescribeTv; // 报修文字描述
    private GridView mRepairDescribeGv; // 报修图片描述
    private TextView mRepairDateTv; // 报修时间
    private LinearLayout mMaintainDescribeLy; //维修
    private TextView mMaintainDescribeTv; // 维修文字描述
    private GridView mMaintainDescribeGv; // 维修图片描述
    private TextView mMaintainDateTv; // 维修时间
    private Button mRepairBtn;  // 结束报修按钮

    // 网格列表适配器
    private ImageGridAdapter mRepairImageAdapter;  // 报修
    private List<String> mRepairImageNameList = new ArrayList<>();
    private List<String> mRepairImageUrlList = new ArrayList<>();
    private List<Bitmap> mRepairBitmapList = new ArrayList<>();
    private ImageGridAdapter mRepairEndImageAdapter; // 报修结束
    private List<String> mRepairEndImageNameList = new ArrayList<>();
    private List<String> mRepairEndImageUrlList = new ArrayList<>();
    private List<Bitmap> mRepairEndBitmapList = new ArrayList<>();

    // 页面传递信息
    private MessageInfo mRepairMessageInfo;
    private RepairInfo mRepairInfo;
    private String mProjectId;
    private String mProjectName;
    private String mBasketId;
    private String mRepairDate;

    // 远程地址
    public static final String REMOTE_REPAIR_IMAGE_PATH = AppConfig.FILE_SERVER_YBLIU_PATH + "/storageRepair/";

    // 后台请求必要信息
    private String mToken;
    private SharedPreferences mPref;

    // mHandler 处理消息
    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch(msg.what){
                case UPDATE_VIEW_MSG:  // 更新页面参数
                    updateRepairInfo();
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_repair_detail);

        getProjectInfo();
        getUserToken();
        initWidgetResource();
        initDisplayView();  // 页面加载
        getRepairBoxOne();
    }

    /*
     * 页面初始化
     */
    // 控件初始化
    private void initWidgetResource(){
        // 顶部导航栏
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        TextView titleText = (TextView) findViewById(R.id.toolbar_title);
        toolbar.setTitle("");
        titleText.setText("报修详情");
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true); //设置返回键可用

        // 获取控件资源
        mProjectIdTv = (TextView) findViewById(R.id.project_id_textview);
        mProjectNameTv = (TextView) findViewById(R.id.project_name_textview);
        mBasketIdTv = (TextView) findViewById(R.id.basket_id_textview);
        mRepairDescribeTv = (TextView) findViewById(R.id.repair_describe_textview);
        mRepairDescribeGv = (GridView) findViewById(R.id.repair_describe_gridview);
        mRepairDateTv = (TextView) findViewById(R.id.repair_date_gridview);
        mRepairImageAdapter = new ImageGridAdapter(RepairDetailActivity.this,
                R.layout.item_work_photo, mRepairImageUrlList);
        mRepairDescribeGv.setAdapter(mRepairImageAdapter);
        mRepairDescribeGv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                getRepairBitmaps();
                // 显示dislog
                ScaleImageView scaleImageView = new ScaleImageView(RepairDetailActivity.this);
                scaleImageView.setUrls_and_Bitmaps(mRepairImageNameList, mRepairBitmapList, position);
                scaleImageView.create();
            }
        });

        mMaintainDescribeLy = (LinearLayout) findViewById(R.id.maintain_describe_layout);
        mMaintainDescribeTv = (TextView) findViewById(R.id.maintain_describe_textview);
        mMaintainDescribeGv = (GridView) findViewById(R.id.maintain_describe_gridview);
        mMaintainDateTv = (TextView) findViewById(R.id.maintain_date_textview);
        mRepairEndImageAdapter = new ImageGridAdapter(RepairDetailActivity.this,
                R.layout.item_work_photo, mRepairEndImageUrlList);
        mMaintainDescribeGv.setAdapter(mRepairEndImageAdapter);
        mMaintainDescribeGv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                getRepairEndBitmaps();
                // 显示dislog
                ScaleImageView scaleImageView = new ScaleImageView(RepairDetailActivity.this);
                scaleImageView.setUrls_and_Bitmaps(mRepairEndImageNameList, mRepairEndBitmapList, position);
                scaleImageView.create();
            }
        });

        mRepairBtn = (Button) findViewById(R.id.repair_over_button);
        mRepairBtn.setOnClickListener(this);
    }

    /*
     * 消息响应
     */
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
    // 控件点击响应
    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.repair_over_button:  // 结束报修按钮

                break;
        }
    }

    /*
     * 网络加载
     */
    // 获取单个报修信息
    private void getRepairBoxOne(){
        BaseOkHttpClient.newBuilder()
                .addHeader("Authorization", mToken)
                .addParam("deviceId", mBasketId)
                .get()
                .url(AppConfig.PRO_ADMIN_GET_REPAIR_SINGLE)
                .build()
                .enqueue(new BaseCallBack() {
                    @Override
                    public void onSuccess(Object o) {
                        JSONObject jsonObject = JSON.parseObject(o.toString());
                        String isSuccess = jsonObject.getString("get");
                        if(isSuccess!=null && isSuccess.equals("success")){
                            Log.d(TAG, "成功单个报修信息");
                            parseRepairInfo(jsonObject);
                        }else{
                            Log.d(TAG, "获取单个报修信息失败");
                        }
                    }

                    @Override
                    public void onError(int code) {
                        Log.d(TAG, "获取单个报修信息错误，错误编码："+code);

                    }

                    @Override
                    public void onFailure(Call call, IOException e) {
                        Log.d(TAG, "获取单个报修信息失败");
                    }
                });
    }
    // 解析报修消息
    private void parseRepairInfo(JSONObject jsonObject){
        String repairInfo =jsonObject.getString("repairInfo");
        JSONArray jsonArray = JSON.parseArray(repairInfo);
        Iterator<Object> iterator = jsonArray.iterator();  // 迭代获取项目信息
        while(iterator.hasNext()) {
            JSONObject repairObj = (JSONObject) iterator.next();
            //时间字符串处理
            String timeDate = repairObj.getString("startTimeS").substring(0,10);
            String timeHM = repairObj.getString("startTimeS").substring(11,16);
            String startTime = timeDate + " " + timeHM;
            try {
                Date date = CustomApplication.dateSavedFormat.parse(mRepairDate);
                String repairTime =  CustomApplication.dateInternetFormat.format(date);

                if(repairTime.equals(startTime)){
                    mRepairInfo = new RepairInfo(repairObj.getString("deviceId"),
                            repairObj.getString("projectId"), repairObj.getString("managerId"),
                            repairObj.getString("reason"),repairObj.getString("imageStart"),
                            mRepairDate);
                    String endTime = repairObj.getString("endTimeS");
                    if(endTime==null || endTime.equals("")){
                    }else {
                        mRepairInfo.setEndTime(endTime);
                        mRepairInfo.setDiscription(repairObj.getString("description"));
                        mRepairInfo.setImageEnd(repairObj.getString("imageEnd"));
                    }
                    mHandler.sendEmptyMessage(UPDATE_VIEW_MSG);
                    break;
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
    }
    // 获取报修位图
     private void getRepairBitmaps(){
        mRepairBitmapList.clear();
        for(int i=0; i<mRepairImageUrlList.size(); i++){
            String url = mRepairImageUrlList.get(i);
            mRepairBitmapList.add(WebImage.webImageCache.get(url));
        }
     }
    // 获取报修结束位图
    private void getRepairEndBitmaps(){
        mRepairEndBitmapList.clear();
        for(int i=0; i<mRepairEndImageUrlList.size(); i++){
            String url = mRepairEndImageUrlList.get(i);
            mRepairEndBitmapList.add(WebImage.webImageCache.get(url));
        }
    }

    /*
     * 页面更新
     */
    // 初始加载页面数据
    private void initDisplayView(){
        mProjectIdTv.setText(mProjectId);
        mProjectNameTv.setText(mProjectName);
        mBasketIdTv.setText(mBasketId);
    }
    private void updateRepairInfo(){
        // 报修
        mRepairDescribeTv.setText(mRepairInfo.getReason());
        if(mRepairInfo.getImageStart().contains(".jpg")){
            String[] startImageNames = mRepairInfo.getImageStart().split(",");
            for(int index=0; index<startImageNames.length; index++){
                mRepairImageNameList.add(startImageNames[index]);
                mRepairImageUrlList.add(REMOTE_REPAIR_IMAGE_PATH + startImageNames[index]);
            }
        }
        mRepairImageAdapter.notifyDataSetChanged();
        mRepairDateTv.setText(mRepairInfo.getStartTime());

        // 报修完成
        if(mRepairInfo.getEndTime()== null || mRepairInfo.getEndTime().equals("")){
            mMaintainDescribeLy.setVisibility(View.GONE);
            mRepairBtn.setVisibility(View.VISIBLE);
        }else{
            mMaintainDescribeLy.setVisibility(View.VISIBLE);
            mRepairBtn.setVisibility(View.GONE);

            mMaintainDescribeTv.setText(mRepairInfo.getDiscription());
            String[] endImageNames = mRepairInfo.getImageEnd().split(",");
            for(int index=0; index<endImageNames.length; index++){
                mRepairEndImageNameList.add(endImageNames[index]);
                mRepairEndImageUrlList.add(REMOTE_REPAIR_IMAGE_PATH + endImageNames[index]);
            }
            mRepairEndImageAdapter.notifyDataSetChanged();
            mMaintainDateTv.setText(mRepairInfo.getEndTime());
        }
    }

    /*
     * 解析项目信息
     */
    private void getProjectInfo(){
        Intent intent = getIntent();
        mProjectId = intent.getStringExtra(ProAdminMessageFragment.PROJECT_ID_MSG);
        mProjectName = intent.getStringExtra(ProAdminMessageFragment.PROJECT_NAME_MSG);
        mBasketId = intent.getStringExtra(ProAdminMessageFragment.BASKET_ID_MSG);
        mRepairDate = intent.getStringExtra(ProAdminMessageFragment.REPAIR_DATE_MSG);
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


}
