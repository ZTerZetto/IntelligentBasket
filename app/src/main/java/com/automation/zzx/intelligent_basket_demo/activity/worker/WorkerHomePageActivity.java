package com.automation.zzx.intelligent_basket_demo.activity.worker;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
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
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.automation.zzx.intelligent_basket_demo.R;
import com.automation.zzx.intelligent_basket_demo.activity.loginRegist.LoginActivity;
import com.automation.zzx.intelligent_basket_demo.adapter.worker.ImageGridAdapter;
import com.automation.zzx.intelligent_basket_demo.entity.AppConfig;
import com.automation.zzx.intelligent_basket_demo.entity.UserInfo;
import com.automation.zzx.intelligent_basket_demo.entity.enums.CardType;
import com.automation.zzx.intelligent_basket_demo.entity.enums.WorkerType;
import com.automation.zzx.intelligent_basket_demo.utils.ToastUtil;
import com.automation.zzx.intelligent_basket_demo.utils.http.HttpUtil;
import com.automation.zzx.intelligent_basket_demo.utils.okhttp.BaseCallBack;
import com.automation.zzx.intelligent_basket_demo.utils.okhttp.BaseOkHttpClient;
import com.automation.zzx.intelligent_basket_demo.widget.ScaleImageView;
import com.automation.zzx.intelligent_basket_demo.widget.image.SmartImageView;
import com.automation.zzx.intelligent_basket_demo.widget.image.WebImage;
import com.hjq.permissions.OnPermission;
import com.hjq.permissions.Permission;
import com.hjq.permissions.XXPermissions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by pengchenghu on 2019/5/21.
 * Author Email: 15651851181@163.com
 * Describe: 施工人员个人主页
 * limits:
 */

public class WorkerHomePageActivity extends AppCompatActivity implements View.OnClickListener {

    private final static String TAG = "WorkerHomePageActivity";

    // 消息响应标识
    private final static int UPDATE_WORKER_INFO_MSG = 101;

    private TextView txtEdit ;// 进入编辑

    // 控件
    private SmartImageView mWorkerHeadImageView; // 头像
    private TextView mWorkerNameTextView; // 用户名
    private TextView mWorkerIdCardTextView; // IDCard
    private TextView mWorkerTypeTextView; // 用户工种
    private LinearLayout mWorkerCallPhoneLayout; // 拨打电话

    // 项目数据统计
    private TextView mWorkerTimeTextView;  // 工作时长
    private TextView mWorkerGoodRateTextView; // 好评率
    private TextView mWorkerViolateTimesTextView; // 违规次数

    // 技能展示页面
    private GridView mWorkerCapacityGridView; // 图片展示区
    private ImageGridAdapter mWorkerCapacityImageAdapter; // 适配器
    private List<String> mWorkerCapacityImageNameList = new ArrayList<>();  // 图片url
    private List<String> mWorkerCapacityImageUrlList = new ArrayList<>();  // 图片url
    private List<Bitmap> mWorkerCapacityBitmapList = new ArrayList<>(); // 位图集合
    private TextView mWorkerNoContentTextView; // 空空如也

    // 远程地址
    public static final String REMOTE_WORKER_IMAGE_PATH = AppConfig.FILE_SERVER_YBLIU_PATH + "/userImage/";

    // 用户相关信息
    private UserInfo mWorkerInfo = new UserInfo();
    private String mToken;
    private SharedPreferences mPref;

    // mHandler 处理消息
    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case UPDATE_WORKER_INFO_MSG:
                    updateWorkerInfoView();
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_worker_home_page);

        if(!isHasPermission()) requestPermission();
        getUserToken();
        getWorkerInfo(getIntent().getStringExtra("worker_id"));
        initWidgets();
    }

    /*
     * 活动初始化
     */
    // 控件初始化
    private void initWidgets(){

        // 顶部导航栏
        Toolbar toolbar = (Toolbar) findViewById(R.id.worker_toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        txtEdit = findViewById(R.id.entrance_edit_skill);
        txtEdit.setOnClickListener(this);
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true); //设置返回键可用


        // 控件初始化
        txtEdit = findViewById(R.id.entrance_edit_skill);
        mWorkerHeadImageView = (SmartImageView) findViewById(R.id.login_head);
        mWorkerNameTextView = (TextView) findViewById(R.id.login_user_name);
        mWorkerIdCardTextView = (TextView) findViewById(R.id.login_id_card);
        mWorkerTypeTextView = (TextView) findViewById(R.id.login_user_type);
        mWorkerCallPhoneLayout = (LinearLayout) findViewById(R.id.login_call_phone);
        mWorkerCallPhoneLayout.setOnClickListener(this);
        mWorkerTimeTextView = (TextView) findViewById(R.id.login_work_time);
        mWorkerGoodRateTextView = (TextView) findViewById(R.id.login_good_rate);
        mWorkerViolateTimesTextView = (TextView) findViewById(R.id.login_violate_times);

        // 技能展示界面
        mWorkerCapacityGridView = (GridView) findViewById(R.id.login_capacity_display);
        mWorkerCapacityImageAdapter = new ImageGridAdapter(WorkerHomePageActivity.this,
                R.layout.item_work_photo, mWorkerCapacityImageUrlList);
        mWorkerCapacityGridView.setAdapter(mWorkerCapacityImageAdapter);
        mWorkerCapacityGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                getBitmaps();
                // 显示dislog
                ScaleImageView scaleImageView = new ScaleImageView(WorkerHomePageActivity.this);
                scaleImageView.setUrls_and_Bitmaps(mWorkerCapacityImageNameList, mWorkerCapacityBitmapList, position);
                scaleImageView.create();
            }
        });

        mWorkerNoContentTextView = (TextView) findViewById(R.id.login_no_content);
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

    // 按钮消息响应
    @Override
    public void onClick(View v) {
        Intent intent;
        switch(v.getId()){
            case R.id.login_call_phone:
                if(!isHasPermission()) requestPermission();
                intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:"+ mWorkerInfo.getUserPhone()));
                startActivity(intent);
                break;
            case R.id.entrance_edit_skill:
                intent = new Intent(WorkerHomePageActivity.this,SkillEditActivity.class);
                //intent.putExtra("",);
                startActivity(intent);
        }
    }

    /*
     * 与后台通讯
     */
    // 获取施工人员信息
    private void getWorkerInfo(String workerId){
        HttpUtil.getWorkerAllInfoOkHttpRequest(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d(TAG, e.toString());
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.code() == 200) {
                    Log.d(TAG, "Http Server Success");
                    String data = response.body().string();
                    parseUserInfoFromInternet(data);
                }else{
                    Log.d(TAG, "Http Server Error" + response.code());

                }
            }
        }, mToken, workerId);
    }

    // 解析后台返回数据
    private void parseUserInfoFromInternet(String data){
        Log.d(TAG, "parse data:" + data);
        JSONObject jsonObject = JSON.parseObject(data);
        String userInfo = jsonObject.getString("userInfo");
        mWorkerInfo = JSON.parseObject(userInfo, UserInfo.class);
        mHandler.sendEmptyMessage(UPDATE_WORKER_INFO_MSG);  // 更新人员信息状态
    }

    /*// 获取资质证书url
    private void workerUpdateCapacityImage(){
        BaseOkHttpClient.newBuilder()
                .addHeader("Authorization", mToken)
                .addParam("userId", mWorkerInfo.getUserId())
                .post()
                .url(AppConfig.WORKER_GET_CAPACITY_IMAGE)
                .build()
                .enqueue(new BaseCallBack() {
                    @Override
                    public void onSuccess(Object o) {
                        Log.i(TAG, "获取资质证书成功" );
                        JSONObject jsonObject = JSON.parseObject(o.toString());
                        String data = "";
                        parseImageUrl(data);
                    }

                    @Override
                    public void onError(int code) {
                        Log.i(TAG, "获取资质证书错误：" + code);
                        switch (code){
                            case 401: // 未授权
                                ToastUtil.showToastTips(WorkerHomePageActivity.this, "登录已过期，请重新登陆");
                                startActivity(new Intent(WorkerHomePageActivity.this, LoginActivity.class));
                                finish();
                                break;
                            case 403: // 禁止
                                break;
                            case 404: // 404
                                break;
                        }
                    }

                    @Override
                    public void onFailure(Call call, IOException e) {
                        Log.i(TAG, "更新资质证书失败：" + e.toString());
                    }
                });
    }*/

    // 解析图片地址
    private void parseImageUrl(String data){
        if(data==null || data.equals("")){
            mWorkerCapacityGridView.setVisibility(View.GONE);
            mWorkerNoContentTextView.setVisibility(View.VISIBLE);
        }else{
            mWorkerCapacityImageUrlList.clear();
            String[] typeSums = data.split(",");
            for(int index=0; index<typeSums.length; index++){
                String[] imageNames = typeSums[index].split("_");

                //1_2 表示第一类证件一共有2张，typeNum_imageSum
                Integer typeNum = Integer.valueOf(imageNames[0]);
                Integer imageSum = Integer.valueOf(imageNames[1]);
                CardType mCardType = CardType.getByType(typeNum);
                if(mCardType != null){
                    for(int order=1;order<=imageSum;order++){
                        String mImageName = mWorkerInfo.getUserId()+"_"+typeNum+"_"+order;
                        mWorkerCapacityImageNameList.add(mImageName);
                        mWorkerCapacityImageUrlList.add(REMOTE_WORKER_IMAGE_PATH + mWorkerInfo.getUserId()
                                + "/" + mCardType.getEnglish()+"/"+mImageName+".jpg"); //图片存放路径+图片名
                    }
                }
            }
        }
    }
    // 初始化图片位图:直接从缓存中获取
    private void getBitmaps(){
        mWorkerCapacityBitmapList.clear();

        for(int i = 0; i < mWorkerCapacityImageUrlList.size(); i++){
            String url = mWorkerCapacityImageUrlList.get(i);
            mWorkerCapacityBitmapList.add(WebImage.webImageCache.get(url));
        }
    }

    /*
     * 页面更新
     */
    // 更新工人基本信息
    private void updateWorkerInfoView(){
        mWorkerNameTextView.setText(mWorkerInfo.getUserName());
        mWorkerIdCardTextView.setText(mWorkerInfo.getUserAccount());
        WorkerType workerType = WorkerType.getByDetailtype(mWorkerInfo.getUserRole());
        if(workerType != null){
            mWorkerTypeTextView.setText(workerType.getChineseType() + " " + workerType.getChineseDetail());
        } else {
            mWorkerTypeTextView.setText(WorkerType.ELECTRIC.getChineseType() + "-" + WorkerType.ELECTRIC.getChineseDetail() );
        }

        /* 用户头像设置 */
        String mUserHeadUrl = AppConfig.FILE_SERVER_YBLIU_PATH + "/userImage/" + mWorkerInfo.getUserId() + "/head.png";
        mWorkerHeadImageView.setImageUrl(mUserHeadUrl, R.mipmap.ic_default_user_head); // 头像

        // 资质证书
        parseImageUrl(mWorkerInfo.getUserPerm());
        mWorkerCapacityImageAdapter.notifyDataSetChanged();
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

    /*
     * 权限申请
     */
    /*
        用xxpermissions申请权限
     */
    // 申请权限
    private void requestPermission() {
        XXPermissions.with(WorkerHomePageActivity.this)
                .constantRequest() //可设置被拒绝后继续申请，直到用户授权或者永久拒绝
                .permission(Permission.CAMERA) //支持请求6.0悬浮窗权限8.0请求安装权限
                .permission(Permission.CALL_PHONE) //支持请求6.0悬浮窗权限8.0请求安装权限
                .request(new OnPermission() {
                    @Override
                    public void hasPermission(List<String> granted, boolean isAll) {
                        if (isAll) {
                            //initCamera(scanPreview.getHolder());
                            onResume();
                        }else {
                            Toast.makeText(WorkerHomePageActivity.this,
                                    "必须同意所有的权限才能使用本程序", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void noPermission(List<String> denied, boolean quick) {
                        if(quick) {
                            Toast.makeText(WorkerHomePageActivity.this,
                                    "被永久拒绝授权，请手动授予权限",
                                    Toast.LENGTH_SHORT).show();
                            // 如果是被永久拒绝就跳转到应用权限系统设置页面
                            XXPermissions.gotoPermissionSettings(WorkerHomePageActivity.this);
                        }else {
                            Toast.makeText(WorkerHomePageActivity.this, "获取权限失败",
                                    Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    }
                });
    }

    // 是否有权限：摄像头、拨打电话
    private boolean isHasPermission() {
        if (XXPermissions.isHasPermission(WorkerHomePageActivity.this, Permission.CAMERA)
                && XXPermissions.isHasPermission(WorkerHomePageActivity.this, Permission.CALL_PHONE))
            return true;
        return false;
    }
}
