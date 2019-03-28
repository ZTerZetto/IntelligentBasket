package com.automation.zzx.intelligent_basket_demo.activity.worker;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.automation.zzx.intelligent_basket_demo.R;
import com.automation.zzx.intelligent_basket_demo.activity.common.PersonalInformationActivity;
import com.automation.zzx.intelligent_basket_demo.activity.loginRegist.LoginActivity;
import com.automation.zzx.intelligent_basket_demo.entity.UserInfo;
import com.automation.zzx.intelligent_basket_demo.utils.HttpUtil;
import com.automation.zzx.intelligent_basket_demo.application.CustomApplication;
import com.automation.zzx.intelligent_basket_demo.utils.ToastUtil;
import com.automation.zzx.intelligent_basket_demo.utils.xiaomi.mipush.MiPushUtil;
import com.automation.zzx.intelligent_basket_demo.widget.dialog.CommonDialog;
import com.automation.zzx.intelligent_basket_demo.widget.dialog.VerifyWorkDialog;
import com.automation.zzx.intelligent_basket_demo.widget.image.SmartImageView;
import com.automation.zzx.intelligent_basket_demo.widget.zxing.activity.CaptureActivity;
import com.hjq.permissions.OnPermission;
import com.hjq.permissions.Permission;
import com.hjq.permissions.XXPermissions;

import java.io.IOException;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import static com.automation.zzx.intelligent_basket_demo.entity.AppConfig.FILE_SERVER_PATH;
import static com.automation.zzx.intelligent_basket_demo.entity.AppConfig.WORKER_BEGIN_WORK;
import static com.automation.zzx.intelligent_basket_demo.entity.AppConfig.WORKER_ENG_WORK;

/**
 * Created by pengchenghu on 2019/3/15.
 * Author Email: 15651851181@163.com
 * Describe: 施工人员主页面
 * limits:
 */

public class WorkerPrimaryActivity extends AppCompatActivity implements View.OnClickListener {
    private final static String TAG = "WorkerPrimaryActivity";

    // 页面跳转
    private final static int CAPTURE_ACTIVITY_RESULT = 1;

    // Handler消息
    private final static int CHANGE_WORK_STATE_MSG = 101;
    private final static int OPEN_VERIFY_DIALOG_MSG = 102;
    private final static int UPDATE_USER_DISPLAY_MSG = 103;

    // header
    private RelativeLayout mWorkerLoginLayout; // 登录总布局
    private SmartImageView mWorkerHead; // 头像
    private TextView mWorkerName;  // 名字
    private TextView mWorkerProjectState;  // 项目

    // function choose
    private LinearLayout mWorkLayout; // 开工/下工
    private ImageView mWorkIv; // 开工状态
    private TextView mWorkTv;
    private LinearLayout mOrderLayout; // 工单
    private LinearLayout mMessageLayout; // 消息
    private LinearLayout mWarningLayout; //警告

    // 其它功能
    private RelativeLayout mGiveHighPrice; // 给个好评
    private RelativeLayout mFeedbackComment; // 反馈意见
    private RelativeLayout mContactService; // 联系客服
    private RelativeLayout mCheckUpdate; // 检查更新
    private RelativeLayout mInRegardTo; //关于

    // 退出登录
    private RelativeLayout mLogout; // 退出登录

    // 页面信息
    private String mWorkProject;  // 项目ID
    private int mWorkState = 0; // 0:等待上工 1:等待下工
    //private String mBasketId; //s 吊篮ID
    private String mUserHeadUrl = FILE_SERVER_PATH + "/head/hdImg_default.jpg";

    // dialog
    private CommonDialog mCommonDialog;
    private VerifyWorkDialog mVerifyWorkDialog;

    // 用户信息
    private UserInfo mUserInfo;
    private String mToken;
    private SharedPreferences mPref;

    // handler 处理消息
    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case CHANGE_WORK_STATE_MSG:  // 更改上工状态
                    if(mWorkState == 0){
                        mWorkTv.setText(R.string.worker_start_basket);
                        mWorkIv.setImageResource(R.mipmap.ic_worker_open);
                    }else if(mWorkState ==1){
                        mWorkTv.setText(R.string.worker_stop_basket);
                        mWorkIv.setImageResource(R.mipmap.ic_worker_close);
                    }
                    break;

                case OPEN_VERIFY_DIALOG_MSG:  // 打开上/下工确认
                    final String basketId = msg.obj.toString();
                    mVerifyWorkDialog = new VerifyWorkDialog(WorkerPrimaryActivity.this,
                            R.style.verify_dialog, mWorkState, mUserHeadUrl, basketId, new VerifyWorkDialog.OnDialogOperateListener() {
                        @Override
                        public void getVerifyResult(String result) {
                            if(result.contains("Success")) {  // 密码验证通过
                                Log.i(TAG, "Now, you can open/close the basket");
                                requestBeginOrEndWork(basketId);
                            }
                        }
                    });
                    mVerifyWorkDialog.show();

                case UPDATE_USER_DISPLAY_MSG:  // 更新状态
                    mWorkerHead.setImageUrl(mUserHeadUrl); // 头像
                    mWorkerName.setText(mUserInfo.getUserName()); // 用户名
                    if(mWorkProject == null || mWorkProject.equals("")) // 项目状态
                        mWorkerProjectState.setText(R.string.worker_no_project);
                    else
                        mWorkerProjectState.setText(mWorkProject);
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_worker_primary);

        getUserInfo();
        if(!isHasPermission()) requestPermission();  // 权限申请
        initWidget();  // 初始化控件

        MiPushUtil.initMiPush(WorkerPrimaryActivity.this, mUserInfo.getUserId(), null);
    }

    // 初始化控件
    private void initWidget(){
        // header
        mWorkerLoginLayout = (RelativeLayout) findViewById(R.id.login_layout);
        mWorkerLoginLayout.setOnClickListener(this);
        mWorkerHead = (SmartImageView) findViewById(R.id.login_head); // 头像
        mWorkerHead.setCircle(true);
        mWorkerHead.setImageUrl(mUserHeadUrl);
        mWorkerName = (TextView) findViewById(R.id.login_username);  // 用户名
        mWorkerProjectState = (TextView) findViewById(R.id.worker_project_state); // 项目状态

        // function choose
        mWorkLayout = (LinearLayout) findViewById(R.id.work_layout);  // 开工/下工
        mWorkLayout.setOnClickListener(this);
        mWorkIv = (ImageView) findViewById(R.id.work_imageview);
        mWorkTv = (TextView) findViewById(R.id.work_textview);
        mOrderLayout = (LinearLayout) findViewById(R.id.order_layout);
        mOrderLayout.setOnClickListener(this);
        mMessageLayout = (LinearLayout) findViewById(R.id.message_layout);
        mMessageLayout.setOnClickListener(this);
        mWarningLayout = (LinearLayout) findViewById(R.id.warning_layout);
        mWarningLayout.setOnClickListener(this);

        // other function
        mGiveHighPrice = (RelativeLayout) findViewById(R.id.more_item_comment_layout); // 给个好评
        mGiveHighPrice.setOnClickListener(this);
        mFeedbackComment = (RelativeLayout) findViewById(R.id.more_item_feedback_layout); // 反馈意见
        mFeedbackComment.setOnClickListener(this);
        mContactService = (RelativeLayout) findViewById(R.id.more_item_contact_kefu_layout); // 联系客服
        mContactService.setOnClickListener(this);
        mCheckUpdate = (RelativeLayout) findViewById(R.id.more_item_check_update_layout); // 检查更新
        mCheckUpdate.setOnClickListener(this);
        mInRegardTo = (RelativeLayout) findViewById(R.id.more_item_about_layout); // 关于
        mInRegardTo.setOnClickListener(this);

        // logout
        mLogout = (RelativeLayout) findViewById(R.id.more_item_log_out_layout); // 退出登录
        mLogout.setOnClickListener(this);
    }

    /*
     * 点击事件消息响应
     */
    @Override
    public void onClick(View v) {
        Intent intent;
        switch (v.getId()){
            case R.id.login_layout:  // 跳转至个人信息页面
                Log.i(TAG, "You have clicked login layout");
                startActivity(new Intent(WorkerPrimaryActivity.this, PersonalInformationActivity.class));
                break;
            case R.id.work_layout:  // 开工/下工
                Log.i(TAG, "You have clicked open/close work button");
                if(mWorkProject == null || mWorkProject.equals("")){
                    if(mCommonDialog == null){
                        mCommonDialog = initDialog();
                    }
                    mCommonDialog.show();
                    break;
                }
                intent = new Intent(WorkerPrimaryActivity.this, CaptureActivity.class);
                startActivityForResult(intent, CAPTURE_ACTIVITY_RESULT);
                break;
            case R.id.order_layout:  // 工单
                Log.i(TAG, "You have clicked order button");
                intent = new Intent(WorkerPrimaryActivity.this, WorkerOrderActivity.class);
                startActivity(intent);
                break;
            case R.id.message_layout:  // 消息
                Log.i(TAG, "You have clicked message button");
                break;
            case R.id.warning_layout:  // 报警
                Log.i(TAG, "You have clicked warning button");
                intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:15651851181"));
                startActivity(intent);
                break;
            case R.id.more_item_comment_layout:  // 给个好评
                Log.i(TAG, "You have clicked high price button");
                break;
            case R.id.more_item_feedback_layout:  // 反馈意见
                Log.i(TAG, "You have clicked feedback button");
                break;
            case R.id.more_item_contact_kefu_layout: // 联系客服
                Log.i(TAG, "You have clicked contact service button");
                break;
            case R.id.more_item_check_update_layout: // 检查更新
                Log.i(TAG, "You have clicked check update button");
                break;
            case R.id.more_item_about_layout: // 关于
                Log.i(TAG, "You have clicked in regard to button");
                break;
            case R.id.more_item_log_out_layout:
                Log.i(TAG, "You have clicked log out button");
                logoutHttp();
                break;
        }
    }

    /*
     * 生命周期
     */
    @Override
    protected void onResume(){
        super.onResume();
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        CustomApplication.setMainActivity(null);
    }

    /*
     * 活动返回
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        switch (requestCode){
            case CAPTURE_ACTIVITY_RESULT:
                if(resultCode == RESULT_OK){
                    String basket_id = data.getStringExtra("basket_id");
                    Log.i(TAG, "Device ID: "+basket_id);
                    Message msg = new Message();
                    msg.what = OPEN_VERIFY_DIALOG_MSG;
                    msg.obj = basket_id;
                    mHandler.sendMessage(msg);
                }
                break;
        }
    }

    /*
     * 上/下工切换
     */
    private void requestBeginOrEndWork(String basketId){
        String url;
        if(mWorkState == 0) { // 等待上工
            Log.i(TAG, "Now, you can open the basket");
            url = WORKER_BEGIN_WORK;
        }
        else if(mWorkState == 1) { //等待下工
            Log.i(TAG, "Now, you can close the basket");
            url = WORKER_ENG_WORK;
        }
        else
            return;

        HttpUtil.workerBeginOrEndWorkOkHttpRequest(new Callback() {
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
                    parseBeginOrEndWork(data);
                }else{
                    Log.d(TAG, "Http Server Error" + response.code());
                }
            }
        }, url, mToken, mUserInfo.getUserId(), basketId, mWorkProject);
    }
    private void parseBeginOrEndWork(String data){
        JSONObject jsonObject = JSON.parseObject(data);
        if(mWorkState == 0) { // 等待开工
            boolean beginWork = jsonObject.getBoolean("beginWork");
            if(beginWork) {
                mWorkState = 1;
                mHandler.sendEmptyMessage(CHANGE_WORK_STATE_MSG);
                return;
            }
            else
                ToastUtil.showToastTips(WorkerPrimaryActivity.this, "打开吊篮失败");
        }else if(mWorkState == 1){ // 等待下工
            boolean endWork = jsonObject.getBoolean("endWork");
            if(endWork) {
                mWorkState = 0;
                mHandler.sendEmptyMessage(CHANGE_WORK_STATE_MSG);
                return;
            }
            else
                ToastUtil.showToastTips(WorkerPrimaryActivity.this, "关闭吊篮失败");
        }

    }
    private void changeWorkState(int workState){
        if(workState == 0){
            mWorkState = 1;
            mWorkIv.setImageResource(R.mipmap.ic_worker_close);
            mWorkTv.setText(R.string.worker_stop_basket);
        }else if(workState == 1){
            mWorkState = 0;
            mWorkIv.setImageResource(R.mipmap.ic_worker_open);
            mWorkTv.setText(R.string.worker_start_basket);
        }
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
        mToken = mPref.getString("loginToken","");

        // 从后台获取其他数据
        getUserInfoFromInternet();
    }
    // 获取后台数据
    private void getUserInfoFromInternet(){
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
        }, mToken, mUserInfo.getUserId());
    }
    // 解析后台返回数据
    private void parseUserInfoFromInternet(String data){
        Log.d(TAG, "parse data:" + data);
        JSONObject jsonObject = JSON.parseObject(data);
        String userInfo = jsonObject.getString("userInfo");
        mUserInfo = JSON.parseObject(userInfo, UserInfo.class);
        mWorkProject = jsonObject.getString("inProject");
        mWorkState = jsonObject.getIntValue("electricState");
        mHandler.sendEmptyMessage(UPDATE_USER_DISPLAY_MSG);  // 更新人员信息状态
        mHandler.sendEmptyMessage(CHANGE_WORK_STATE_MSG);    // 更新上/下工状态

    }
    //退出登录
    private void logoutHttp() {
        SharedPreferences.Editor editor = mPref.edit();
        editor.clear();
        editor.commit();
        startActivity(new Intent(WorkerPrimaryActivity.this, LoginActivity.class));
    }

    /*
     * 提示弹框
     */
    private CommonDialog initDialog(){
        return new CommonDialog(this, R.style.dialog, "您尚无参与的吊篮项目，请与管理员联系上工！",
                new CommonDialog.OnCloseListener() {
                    @Override
                    public void onClick(Dialog dialog, boolean confirm) {
                        if(confirm){
                            //ToastUtil.showToastTips(WorkerPrimaryActivity.this, "点击确定");
                            dialog.dismiss();
                        }else{
                            //ToastUtil.showToastTips(WorkerPrimaryActivity.this, "点击取消");
                            dialog.dismiss();
                        }
                    }
                }).setTitle("提示");
    }

    /*
        用xxpermissions申请权限
     */
    // 申请权限
    private void requestPermission() {
        XXPermissions.with(WorkerPrimaryActivity.this)
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
                                Toast.makeText(WorkerPrimaryActivity.this,
                                    "必须同意所有的权限才能使用本程序", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void noPermission(List<String> denied, boolean quick) {
                        if(quick) {
                            Toast.makeText(WorkerPrimaryActivity.this, "被永久拒绝授权，请手动授予权限",
                                    Toast.LENGTH_SHORT).show();
                            // 如果是被永久拒绝就跳转到应用权限系统设置页面
                            XXPermissions.gotoPermissionSettings(WorkerPrimaryActivity.this);
                        }else {
                            Toast.makeText(WorkerPrimaryActivity.this, "获取权限失败",
                                    Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    }
                });
    }

    // 是否有权限：摄像头、拨打电话
    private boolean isHasPermission() {
        if (XXPermissions.isHasPermission(WorkerPrimaryActivity.this, Permission.CAMERA)
                && XXPermissions.isHasPermission(WorkerPrimaryActivity.this, Permission.CALL_PHONE))
            return true;
        return false;
    }

    // 跳转到设置界面
    private void gotoPermissionSettings(View view) {
        XXPermissions.gotoPermissionSettings(this);
    }

}
