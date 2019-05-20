package com.automation.zzx.intelligent_basket_demo.activity.inspectionPerson;

import android.annotation.SuppressLint;
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
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.automation.zzx.intelligent_basket_demo.R;
import com.automation.zzx.intelligent_basket_demo.activity.worker.WorkerPrimaryActivity;
import com.automation.zzx.intelligent_basket_demo.adapter.inspectPerson.AddedBasketAdapter;
import com.automation.zzx.intelligent_basket_demo.entity.AppConfig;
import com.automation.zzx.intelligent_basket_demo.entity.MgBasketStatement;
import com.automation.zzx.intelligent_basket_demo.utils.ToastUtil;
import com.automation.zzx.intelligent_basket_demo.utils.okhttp.BaseCallBack;
import com.automation.zzx.intelligent_basket_demo.utils.okhttp.BaseOkHttpClient;
import com.automation.zzx.intelligent_basket_demo.widget.zxing.activity.CaptureActivity;
import com.hjq.permissions.OnPermission;
import com.hjq.permissions.Permission;
import com.hjq.permissions.XXPermissions;
import com.yanzhenjie.recyclerview.swipe.SwipeMenuRecyclerView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import okhttp3.Call;

import static com.automation.zzx.intelligent_basket_demo.widget.zxing.activity.CaptureActivity.QR_CODE_RESULT;

/**
 * Created by pengchenghu on 2019/5/14.
 * Author Email: 15651851181@163.com
 * Describe: 巡检人员扫码出库
 * limits:
 */

public class OutAndInStorageActivity extends AppCompatActivity {

    private final static String TAG = "OutStorageActivity";

    // 页面跳转标识
    public final static int CAPTURE_ACTIVITY_REQUEST = 101;

    // 消息处理标识
    private final static int EXIT_ACTIVITY_FLAG = 101;
    private final static int UPDATE_PRO_INFO_FLAG = 102;
    private final static int UPDATE_BASKET_LIST_FLAG = 103;

    // 控件
    private TextView mProjectIdTextView; // 项目id
    private TextView mProjectNameTextView; //项目名称
    private TextView mProjectStartTextView; // 项目开始日期
    private ImageView mAddBasketImageView; // 添加吊篮的图片按钮
    private RecyclerView mAddedBasketsRecyclerView; // 已添加吊篮列表控件

    // 项目相关信息
    private String mProjectId;  // 项目ID
    private int mOperateType;  // 0: 添加吊篮， 1：删除吊篮
    private List<MgBasketStatement> mAddedBasketList = new ArrayList<>();
    private AddedBasketAdapter mAddedBasketAdapter;

    // 后台请求必要信息
    private String mToken;
    private SharedPreferences mPref;

    // mHandler 处理消息
    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch(msg.what){
                case EXIT_ACTIVITY_FLAG:  // 退出页面
                    ToastUtil.showToastTips(OutAndInStorageActivity.this, "错误的项目号，请检查是否存在该项目");
                    finish();
                    break;
                case UPDATE_PRO_INFO_FLAG:  // 更新项目信息
                    parseProjectDetail((String)msg.obj);
                    break;
                case UPDATE_BASKET_LIST_FLAG:
                    parseBasketListInfo((String)msg.obj);
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_out_and_in_storage);

        Intent intent = getIntent();
        mProjectId = intent.getStringExtra(SearchProjectActivity.PROJECT_ID);  // 获取吊篮id
        mOperateType = intent.getIntExtra(SearchProjectActivity.OPERATE_TYPE, 0); // 获取操作类型
        if(mProjectId==null || mProjectId.equals("")){
            ToastUtil.showToastTips(OutAndInStorageActivity.this, "无效的项目号！");
            finish();
        }

        if(!isHasPermission()) requestPermission();  // 申请权限

        getUserToken();     // 获取Token
        getProInfo();
        getBasketList();
        initWidgetResource();  // 初始化控件
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
        if(mOperateType==0) titleText.setText("出库吊篮设备");
        else if(mOperateType==1) titleText.setText("入库吊篮设备");
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true); //设置返回键可用

        // 项目信息相关
        mProjectIdTextView = (TextView) findViewById(R.id.project_id_textview);
        mProjectNameTextView = (TextView) findViewById(R.id.project_name_textview);
        mProjectStartTextView = (TextView) findViewById(R.id.project_start_time_textview);

        // 吊篮列表
        mAddedBasketsRecyclerView = (RecyclerView) findViewById(R.id.added_basket_recyclerview);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);     //
        mAddedBasketsRecyclerView.setLayoutManager(layoutManager);
        mAddedBasketAdapter = new AddedBasketAdapter(this, mAddedBasketList);
        mAddedBasketsRecyclerView.setAdapter(mAddedBasketAdapter);

        // 添加/删除吊篮按钮
        mAddBasketImageView = (ImageView) findViewById(R.id.basket_add_image_view);
        if(mOperateType==0) mAddBasketImageView.setImageResource(R.mipmap.ic_round_add_fill);
        else if(mOperateType==1) mAddBasketImageView.setImageResource(R.mipmap.ic_round_minus_fill);
        mAddBasketImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "You has clicked the operater button");
                startActivityForResult(new Intent(OutAndInStorageActivity.this,
                                CaptureActivity.class), CAPTURE_ACTIVITY_REQUEST);
            }
        });
    }

    /*
     * 处理Activity返回结果
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
            case CAPTURE_ACTIVITY_REQUEST:  // 扫描工人二维码名片返回结果
                if(resultCode == RESULT_OK){
                    String basketId = data.getStringExtra(QR_CODE_RESULT);
                    Log.i(TAG, "QR_Content: "+ basketId);
                    if(mOperateType==0)
                        outStorage(basketId);
                    else if(mOperateType==1)
                        inStorage(basketId);
                }
                break;
            default:
                break;
        }
    }

    /*
     * 消息响应
     */
    // 顶部导航栏消息响应
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
     * 从后台获取数据
     */
    // 获取项目信息
    private void getProInfo(){
        BaseOkHttpClient.newBuilder()
                .addHeader("Authorization", mToken)
                .addParam("projectId", mProjectId)
                .get()
                .url(AppConfig.INSPECTION_PERSON_GET_PRO_INFO)
                .build()
                .enqueue(new BaseCallBack() {
                    @Override
                    public void onSuccess(Object o) {
                        Log.d(TAG, "成功获取项目信息");
                        JSONObject jsonObject = JSON.parseObject(o.toString());
                        String projectDetail = jsonObject.getString("projectDetail");
                        if(projectDetail == null || projectDetail.equals("")){
                            // 退出页面并提示
                            mHandler.sendEmptyMessage(EXIT_ACTIVITY_FLAG);
                        }else{
                            Message msg = new Message();
                            msg.what = UPDATE_PRO_INFO_FLAG;
                            msg.obj = projectDetail;
                            mHandler.sendMessage(msg);
                        }
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
    // 解析项目基本信息
    private void parseProjectDetail(String data){
        JSONObject jsonObject = JSON.parseObject(data);
        String projectName = jsonObject.getString("projectName");
        if(projectName!=null || !projectName.equals("")) mProjectNameTextView.setText(projectName);
        mProjectId = jsonObject.getString("projectId");
        if(mProjectId!=null || !mProjectId.equals("")) mProjectIdTextView.setText(mProjectId);
        String projectStart= jsonObject.getString("projectStart");
        if(projectStart!=null || !projectStart.equals("")) mProjectStartTextView.setText(projectStart);
    }

    // 获取项目吊篮列表
    private void getBasketList(){
        BaseOkHttpClient.newBuilder()
                .addHeader("Authorization", mToken)
                .addParam("projectId", mProjectId)
                .get()
                .url(AppConfig.INSPECTION_PERSON_GET_BASKET_LIST_INFO)
                .build()
                .enqueue(new BaseCallBack() {
                    @Override
                    public void onSuccess(Object o) {
                        Log.d(TAG, "成功获取吊篮列表");
                        String responseData = o.toString();
                        Message message = new Message();
                        message.what = UPDATE_BASKET_LIST_FLAG;
                        message.obj = responseData;
                        mHandler.sendMessage(message);
                    }

                    @Override
                    public void onError(int code) {
                        Log.d(TAG, "获取吊篮列表信息错误，错误编码："+code);
                    }

                    @Override
                    public void onFailure(Call call, IOException e) {
                        Log.d(TAG, "获取吊篮列表信息失败");
                    }
                });
    }
    // 解析项目中的吊篮列表信息
    private void parseBasketListInfo(String responseDate) {
        mAddedBasketList.clear();

        JSONObject jsonObject = JSON.parseObject(responseDate);
        Iterator<String> iterator = jsonObject.keySet().iterator();  // 迭代获取吊篮信息
        while (iterator.hasNext()) {
            String key = iterator.next();
            if (!key.contains("storage")) continue;
            String value = jsonObject.getString(key);
            if (value == null || value.equals("")) continue;
            JSONObject basketObj = JSON.parseObject(value);
            String deviceId = basketObj.getString("deviceId");
            if (deviceId == null || deviceId.equals("")) continue;
            mAddedBasketList.add(new MgBasketStatement(basketObj.getString("deviceId"),
                    null, basketObj.getString("storageState")));
        }

        mAddedBasketAdapter.notifyDataSetChanged();
    }
    // 出库
    private void outStorage(String basketId){
        BaseOkHttpClient.newBuilder()
                .addHeader("Authorization", mToken)
                .addParam("projectId", mProjectId)
                .addParam("boxId", basketId)
                .post()
                .url(AppConfig.INSPECTION_PERSON_OUT_STORAGE)
                .build()
                .enqueue(new BaseCallBack() {
                    @Override
                    public void onSuccess(Object o) {
                        JSONObject jsonObject = JSON.parseObject(o.toString());
                        String isIncrease = jsonObject.getString("increase");
                        if(isIncrease.contains("失败")){
                            Log.i(TAG, "新增吊篮失败");
                            ToastUtil.showToastTips(OutAndInStorageActivity.this, "新增吊篮失败");
                        }else{
                            Log.i(TAG, "添加吊篮成功");
                            ToastUtil.showToastTips(OutAndInStorageActivity.this, "添加吊篮成功");
                            getBasketList();
                        }
                    }

                    @Override
                    public void onError(int code) {
                        Log.d(TAG, "添加吊篮错误，错误编码："+code);
                    }

                    @Override
                    public void onFailure(Call call, IOException e) {
                        Log.d(TAG, "添加吊篮失败");
                    }
                });
    }
    // 入库
    private void inStorage(String basketId){
        BaseOkHttpClient.newBuilder()
                .addHeader("Authorization", mToken)
                .addParam("projectId", mProjectId)
                .addParam("deviceId", basketId)
                .post()
                .url(AppConfig.INSPECTION_PERSON_IN_STORAGE)
                .build()
                .enqueue(new BaseCallBack() {
                    @Override
                    public void onSuccess(Object o) {
                        JSONObject jsonObject = JSON.parseObject(o.toString());
                        String result = jsonObject.getString("result");
                        if(result==null || result.equals("")){
                            Log.d(TAG, "入库失败");
                            ToastUtil.showToastTips(OutAndInStorageActivity.this, "入库失败");
                        }else if(result.contains("成功")){
                            Log.d(TAG, "入库成功");
                            ToastUtil.showToastTips(OutAndInStorageActivity.this, "入库成功");
                            getBasketList();
                        }else{
                            Log.d(TAG, "吊篮已入库");
                            ToastUtil.showToastTips(OutAndInStorageActivity.this, "吊篮已入库");
                        }
                    }

                    @Override
                    public void onError(int code) {
                        Log.d(TAG, "入库错误，错误编码："+code);
                    }

                    @Override
                    public void onFailure(Call call, IOException e) {
                        Log.d(TAG, "入库失败");
                    }
                });
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
     * 权限管理
     */
     /*
        用xxpermissions申请权限
     */
    // 申请权限
    private void requestPermission() {
        XXPermissions.with(OutAndInStorageActivity.this)
                .constantRequest() //可设置被拒绝后继续申请，直到用户授权或者永久拒绝
                .permission(Permission.CAMERA) //支持请求6.0悬浮窗权限8.0请求安装权限
                .request(new OnPermission() {
                    @Override
                    public void hasPermission(List<String> granted, boolean isAll) {
                        if (isAll) {
                            //initCamera(scanPreview.getHolder());
                            onResume();
                        }else {
                            Toast.makeText(OutAndInStorageActivity.this,
                                    "必须同意所有的权限才能使用本程序",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void noPermission(List<String> denied, boolean quick) {
                        if(quick) {
                            Toast.makeText(OutAndInStorageActivity.this,
                                    "被永久拒绝授权，请手动授予权限",
                                    Toast.LENGTH_SHORT).show();
                            // 如果是被永久拒绝就跳转到应用权限系统设置页面
                            XXPermissions.gotoPermissionSettings(OutAndInStorageActivity.this);
                        }else {
                            Toast.makeText(OutAndInStorageActivity.this, "获取权限失败",
                                    Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    }
                });
    }

    // 是否有权限：摄像头
    private boolean isHasPermission() {
        if (XXPermissions.isHasPermission(OutAndInStorageActivity.this, Permission.CAMERA))
            return true;
        return false;
    }
}
