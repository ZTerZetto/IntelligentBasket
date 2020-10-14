package com.automation.zzx.intelligent_basket_demo.activity.worker;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.automation.zzx.intelligent_basket_demo.R;
import com.automation.zzx.intelligent_basket_demo.activity.common.PersonalInformationActivity;
import com.automation.zzx.intelligent_basket_demo.activity.common.UserMessageActivity;
import com.automation.zzx.intelligent_basket_demo.activity.loginRegist.LoginActivity;
import com.automation.zzx.intelligent_basket_demo.activity.loginRegist.RegistWorkerActivity;
import com.automation.zzx.intelligent_basket_demo.activity.proAdmin.ProAdminPrimaryOldActivity;
import com.automation.zzx.intelligent_basket_demo.entity.AppConfig;
import com.automation.zzx.intelligent_basket_demo.entity.UserInfo;
import com.automation.zzx.intelligent_basket_demo.utils.CustomTimeTask;
import com.automation.zzx.intelligent_basket_demo.utils.StringUtil;
import com.automation.zzx.intelligent_basket_demo.utils.ToastUtil;
import com.automation.zzx.intelligent_basket_demo.utils.http.HttpUtil;
import com.automation.zzx.intelligent_basket_demo.utils.okhttp.BaseCallBack;
import com.automation.zzx.intelligent_basket_demo.utils.okhttp.BaseOkHttpClient;
import com.automation.zzx.intelligent_basket_demo.utils.xiaomi.mipush.MiPushUtil;
import com.automation.zzx.intelligent_basket_demo.widget.dialog.CommonDialog;
import com.automation.zzx.intelligent_basket_demo.widget.dialog.ConfirmWorkDialog;
import com.automation.zzx.intelligent_basket_demo.widget.dialog.LoadingDialog;
import com.automation.zzx.intelligent_basket_demo.widget.dialog.VerifyWorkDialog;
import com.automation.zzx.intelligent_basket_demo.widget.image.SmartImageView;
import com.automation.zzx.intelligent_basket_demo.widget.zxing.activity.CaptureActivity;
import com.hjq.permissions.OnPermission;
import com.hjq.permissions.Permission;
import com.hjq.permissions.XXPermissions;

import java.io.IOException;
import java.util.List;
import java.util.TimerTask;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import static com.automation.zzx.intelligent_basket_demo.entity.AppConfig.WORKER_BEGIN_WORK;
import static com.automation.zzx.intelligent_basket_demo.entity.AppConfig.WORKER_END_WORK;
import static com.automation.zzx.intelligent_basket_demo.widget.zxing.activity.CaptureActivity.QR_CODE_RESULT;

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
    private final static int NOTIFY_DEVICE_OPERATION_MSG = 104;
    private final static int PARAMETER_INFO_MSG = 105;
    private final static int TIMER_START_MSG = 106;
    private final static int TIMER_STOP_MSG = 107;
    private final static int TIMER_TASK_MSG = 108;
    private final static int SHOW_LOADING_MSG = 109;
    private final static int DISMISS_LOADING_MSG = 110;
    private final static int SHOW_BASKET_MSG= 111;


    // header
    private RelativeLayout mWorkerLoginLayout; // 登录总布局
    private SmartImageView mWorkerHead; // 头像
    private TextView mWorkerName;  // 名字
    private TextView mWorkerProjectState;  // 项目

    // basket
    private LinearLayout mBasketLayout; // 吊篮信息
    private TextView tvBasketID;
    private TextView tvSiteNo;

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

    //切换角色
    private LinearLayout llSwitch;
    private RelativeLayout rlSwitch;

    // 退出登录
    private RelativeLayout mLogout; // 退出登录

    // 页面信息
    private String mWorkProjectId;  // 项目ID
    private String mWorkProjectName; // 项目名称
    private int mWorkState = 0; // 0:等待上工 1:等待下工
    private String mBasketId; //s 吊篮ID
    private String mSiteNo; //s 现场ID
    private String mUserHeadUrl;

    // dialog
    private CommonDialog mCommonDialog;
//    private VerifyWorkDialog mVerifyWorkDialog;
    private ConfirmWorkDialog mConfirmWorkDialog;
    private LoadingDialog mLoadingDialog;

    // 用户信息
    private UserInfo mUserInfo;
    private String mToken;
    private SharedPreferences mPref;

    //是否为项目负责人
    private String isProAdmin;

    // 定时器，每秒钟轮询一次的定时器
    private CustomTimeTask customTimeTask;
    // 轮询次数
    private int mQueryCnt = 0;
    private int mMaxQueryCnt = 60;

    // mHandler 处理消息
    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case CHANGE_WORK_STATE_MSG:  // 更改上工状态
                    if(mWorkState == 0){
                        //当前为下工状态，展示改为可上工
                        mWorkTv.setText(R.string.worker_start_basket);
                        mWorkIv.setImageResource(R.mipmap.ic_worker_open);
                        //下工状态隐藏并清空吊篮信息
                        mBasketLayout.setVisibility(View.GONE);
                    }else if(mWorkState ==1){
                        //当前为上工状态，展示改为可下工
                        mWorkTv.setText(R.string.worker_stop_basket);
                        mWorkIv.setImageResource(R.mipmap.ic_worker_close);
                        //上工状态获取吊篮编号+现场编号（获取——>显示）
                        getBasketInfo();
                    }
                    break;

                case OPEN_VERIFY_DIALOG_MSG:  // 打开上/下工确认
                    final String basketId = msg.obj.toString();
                    mConfirmWorkDialog = new ConfirmWorkDialog(WorkerPrimaryActivity.this,
                            R.style.verify_dialog, mWorkState, mUserHeadUrl, basketId,
                            new ConfirmWorkDialog.OnDialogOperateListener() {
                                @Override
                                public void getVerifyResult(String result) {
                                    if(result.contains("Success")) {  // 密码验证通过
                                        Log.i(TAG, "Now, you can open/close the basket");
                                        requestBeginOrEndWork(basketId);
                                        mBasketId = basketId;  // 吊篮ID
                                    }
                                }
                            });
                    mConfirmWorkDialog.show();
                    break;  // 原来没有
                case UPDATE_USER_DISPLAY_MSG:  // 更新状态
                    mUserHeadUrl = AppConfig.FILE_SERVER_YBLIU_PATH + "/userImage/" + mUserInfo.getUserId() + "/head.png";
                    mWorkerHead.setImageUrl(mUserHeadUrl, R.mipmap.ic_default_user_head); // 头像
                    mWorkerName.setText(mUserInfo.getUserName()); // 用户名
                    if(mWorkProjectId == null || mWorkProjectId.equals("")) // 项目状态
                        mWorkerProjectState.setText(R.string.worker_no_project);
                    else {
                        if (mWorkProjectName == null || mWorkProjectName.equals(""))
                            mWorkerProjectState.setText(mWorkProjectId);
                        else
                            mWorkerProjectState.setText(mWorkProjectName);
                    }
                    break;

                case NOTIFY_DEVICE_OPERATION_MSG:  // 通知硬件
                    int state = (int)msg.obj;
                    switch (state){
                        case 10:  // 可以执行打开吊篮操作
                            Toast.makeText(WorkerPrimaryActivity.this,
                                    "正在打开吊篮...", Toast.LENGTH_SHORT).show();
                            notifyHardDivice();
                            break;
                        case 11:  // 吊篮与施工人员不匹配
                            Toast.makeText(WorkerPrimaryActivity.this,
                                    "此吊篮不是您的工作吊篮，无法打开！", Toast.LENGTH_SHORT).show();

                            break;
                        case 200:  // 吊篮上有人，只执行下工，不关闭吊篮
                            Toast.makeText(WorkerPrimaryActivity.this,
                                    "下工成功，吊篮尚有其他作业人员，等待其他施工人员断电!",
                                    Toast.LENGTH_SHORT).show();
                            mHandler.sendEmptyMessage(CHANGE_WORK_STATE_MSG);  // 调整页面显示
                            break;
                        case 201:  // 吊篮上没人其他工人，下工+关闭吊篮
                            Toast.makeText(WorkerPrimaryActivity.this,
                                    "下工成功，正在关闭吊篮...",
                                    Toast.LENGTH_SHORT).show();
                            notifyHardDivice();
                            break;
                        case 21:  // 吊篮与施工人员不匹配
                            Toast.makeText(WorkerPrimaryActivity.this,
                                    "此吊篮不是您的工作吊篮，无法下工！", Toast.LENGTH_SHORT).show();
                            break;
                        default:
                            break;
                    }
                    break;
                case PARAMETER_INFO_MSG:  // 吊篮状态
                    int basket_state = parseParameterInfo(msg.obj); // 获取吊篮参数
                    if(basket_state == mWorkState){
                        mQueryCnt = 0;
                        ToastUtil.showToastTips(WorkerPrimaryActivity.this, "操作成功");
                        mHandler.sendEmptyMessage(TIMER_STOP_MSG);
                        mHandler.sendEmptyMessage(DISMISS_LOADING_MSG);
                        mHandler.sendEmptyMessage(CHANGE_WORK_STATE_MSG);  // 调整页面显示
                    }else{
                        mQueryCnt += 1;
                        if (mQueryCnt == mMaxQueryCnt){  // 达到最大的轮询上限
                            mQueryCnt = 0;
                            ToastUtil.showToastTips(WorkerPrimaryActivity.this, "操作失败");
                            mWorkState = Math.abs(1-mWorkState);  // 还原状态
                            mHandler.sendEmptyMessage(TIMER_STOP_MSG);
                            mHandler.sendEmptyMessage(DISMISS_LOADING_MSG);
                        }
                    }
                    break;

                case SHOW_BASKET_MSG:
                    tvBasketID.setText(mBasketId);
                    tvSiteNo.setText(mSiteNo);
                    mBasketLayout.setVisibility(View.VISIBLE);
                    break;

                case TIMER_TASK_MSG:
                    deviceParameterHttp();  // 获取吊篮最新的数据
                    break;

                case TIMER_START_MSG:  // 打开定时器
                    customTimeTask.start();
                    break;

                case TIMER_STOP_MSG:  // 停止定时器
                    customTimeTask.stop();
                    break;

                case SHOW_LOADING_MSG:  // 弹出弹窗
                    mLoadingDialog.show();
                    break;

                case DISMISS_LOADING_MSG:  // 关闭弹窗
                    mLoadingDialog.dismiss();
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
        initTimer();
        initLoadingDialog();

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

        // 吊篮信息
        mBasketLayout = (LinearLayout) findViewById(R.id.ll_basket_info);  // 吊篮信息（仅上工状态）
        tvBasketID = (TextView) findViewById(R.id.tv_basket_num);
        tvSiteNo = (TextView) findViewById(R.id.tv_basket_siteno);

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

        //switch to projectAdmin
        llSwitch = (LinearLayout) findViewById(R.id.switch_layout); //切换角色
        rlSwitch =  (RelativeLayout) findViewById(R.id.more_item_switch_layout); //切换角色
        rlSwitch.setOnClickListener(this);

        // logout
        mLogout = (RelativeLayout) findViewById(R.id.more_item_log_out_layout); // 退出登录
        mLogout.setOnClickListener(this);

        if(isProAdmin.equals("1")){
            llSwitch.setVisibility(View.VISIBLE);
        } else {
            llSwitch.setVisibility(View.GONE);
        }
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
                intent = new Intent(WorkerPrimaryActivity.this, PersonalInformationActivity.class);
                intent.putExtra("userInfo", (Parcelable) mUserInfo);
                startActivity(intent);
                break;
            case R.id.work_layout:  // 开工/下工
                Log.i(TAG, "You have clicked open/close work button");
                if(mWorkProjectId == null || mWorkProjectId.equals("")){  // 工人无项目
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
                intent = new Intent(WorkerPrimaryActivity.this, UserMessageActivity.class);
                intent.putExtra("user_type", "worker");
                intent.putExtra("user_id", mUserInfo.getUserId());
                startActivity(intent);
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
            case R.id.more_item_switch_layout:// 项目管理员
                Log.i(TAG, "You have clicked switch button");
                switchToProAdmin();
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
        //CustomApplication.setMainActivity(null);
        customTimeTask.stop();
    }
    // 退出但不销毁
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            moveTaskToBack(false);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    /*
     * 活动返回
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        switch (requestCode){
            case CAPTURE_ACTIVITY_RESULT:
                if(resultCode == RESULT_OK){
                    String basket_id = data.getStringExtra(QR_CODE_RESULT);
                    basket_id = StringUtil.replaceBlank(basket_id);  // 过滤空白、制表符等无效字符
                    Log.i(TAG, "Device ID: "+ basket_id);
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
        else { //等待下工
            Log.i(TAG, "Now, you can close the basket");
            url = WORKER_END_WORK;
        }

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
                    //String hint = parseBeginOrEndWork(data);
                    int state = parseBeginOrEndWork(data);
                    Message msg = new Message();
                    msg.what = NOTIFY_DEVICE_OPERATION_MSG;
                    msg.obj = state;
                    mHandler.sendMessage(msg);
                }else{
                    Log.d(TAG, "Http Server Error" + response.code());
                }
            }
        }, url, mToken, mUserInfo.getUserId(), basketId, mWorkProjectId);
    }

    // 解析上下工消息
    private int parseBeginOrEndWork(String data){
        JSONObject jsonObject = JSON.parseObject(data);
        int state;
        if(mWorkState == 0) { // 下工状态，等待开工
            boolean beginWork = jsonObject.getBoolean("beginWork");
            if(beginWork) {  // 允许上工
                mWorkState = 1;
                state = 10;
            } else {  // 吊篮与工人不匹配
                state = 11;
            }
        }else if(mWorkState == 1){ // 上工状态，等待下工
            boolean endWork = jsonObject.getBoolean("endWork");
            boolean hasPeople = jsonObject.getBoolean("hasPeople");
            if(endWork) {
                mWorkState = 0;
                if(hasPeople) {  // 吊篮上还有作业人员
                    state = 200;
                }else{  // 吊篮上无其他作业人员
                    state = 201;
                }
            } else {  // 吊篮与工人不匹配
                state = 21;
            }
        }else{  // 错误状态
            state = 0;
        }
        return state;
    }

    // 通知硬件上下工
    private void notifyHardDivice(){
        HttpUtil.operateHardDevice(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.i(TAG, "失败：" + e.toString());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                switch (response.code()){
                    case 200:
                        Log.i(TAG, "成功返回值");
                        customTimeTask.start();  // 开启定时器
                        mHandler.sendEmptyMessage(SHOW_LOADING_MSG);
//                        // 返回服务器数据
//                        String responseData = response.body().string();
//                        if(responseData.equals("success")){
//                            mHandler.sendEmptyMessage(CHANGE_WORK_STATE_MSG);
//                            Looper.prepare();
//                            Toast.makeText(WorkerPrimaryActivity.this,
//                                    "操作成功！", Toast.LENGTH_SHORT).show();
//                            Looper.loop();
//                        }else{
//                            Looper.prepare();
//                            Toast.makeText(WorkerPrimaryActivity.this,
//                                    "操作失败！", Toast.LENGTH_SHORT).show();
//                            Looper.loop();
//                        }
                        break;
                    default:
                        Log.i(TAG, "错误编码：" + response.code());
                        Looper.prepare();
                        Toast.makeText(WorkerPrimaryActivity.this,
                                "网络连接超时,请稍后重试！", Toast.LENGTH_SHORT).show();
                        Looper.loop();
                        break;
                }
            }
        }, mBasketId, mWorkState);
    }

    // 更换工作状态显示
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

    // 轮询查看状态改变与否
    private void deviceParameterHttp(){
        HttpUtil.getDeviceParameterOkHttpRequest(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.i(TAG, "失败：" + e.toString());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                // 成功
                String responseData = response.body().string();
                Log.i(TAG, "成功：" +  responseData);
                Message msg = new Message();
                msg.what = PARAMETER_INFO_MSG;
                msg.obj = responseData;
                mHandler.sendMessage(msg);

            }
        }, mToken, mBasketId);
    }

    // 解析设备参数
    private int parseParameterInfo(Object obj) {
        String json = obj.toString();  // object 转 string
        JSONObject jsonObject = JSON.parseObject(json);  // string 转 jsonobject
        String electric_data_json = jsonObject.getString("realTimeData");
        JSONObject electric_data_json_object = JSON.parseObject(electric_data_json);
        if (electric_data_json_object == null) {
            return -1;
        }

        // 设备状态
        int deviceState = electric_data_json_object.getIntValue("bketStat");
        return deviceState;  // 吊篮状态：0-关闭，1打开
    }

    /*
     * 定时器
     */
    private void initTimer(){
        customTimeTask = new CustomTimeTask(1000, new TimerTask() {
            @Override
            public void run() {
                mHandler.sendEmptyMessage(TIMER_TASK_MSG);
                Log.d(TAG, "定时任务：获取最新吊篮数据");
            }
        });
    }


    /*
    * 获取吊篮信息
    * */
    public void getBasketInfo() {
        BaseOkHttpClient.newBuilder()
               .addHeader("Authorization",mToken)
                .addParam("userId",mUserInfo.getUserId())
                .get()
                .url(AppConfig.GET_ELECTRIC_RES_INFO)
                .build()
                .enqueue(new BaseCallBack() {
                    @Override
                    public void onSuccess(Object o) {
                        String data = o.toString();
                        JSONObject jsonObject = JSON.parseObject(data);
                        boolean isLogin = jsonObject.getBooleanValue("isLogin");
                        if (isLogin) {
                            JSONArray result = jsonObject.getJSONArray("result");
                            JSONObject object = result.getJSONObject(0);
                            mSiteNo = object.getString("siteNo");
                            JSONObject electricRes = object.getJSONObject("electricRes");
                            mBasketId = electricRes.getString("deviceId");
                            if(mSiteNo != null && !mSiteNo.equals("") && mBasketId != null && !mBasketId.equals("") ){
                                mHandler.sendEmptyMessage(SHOW_BASKET_MSG);
                            }
                        }
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
        mToken = mPref.getString("loginToken","");
        isProAdmin = mPref.getString("isProAdmin","");

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
        mWorkProjectId = jsonObject.getString("inProject");
        mWorkProjectName = jsonObject.getString("projectName");
        mWorkState = jsonObject.getIntValue("userState");
        mHandler.sendEmptyMessage(UPDATE_USER_DISPLAY_MSG);  // 更新人员信息状态
        mHandler.sendEmptyMessage(CHANGE_WORK_STATE_MSG);    // 更新上/下工状态
    }

    //切换角色
    private void switchToProAdmin() {
        startActivity(new Intent(this, ProAdminPrimaryOldActivity.class));
        this.finish();
    }

    //退出登录
    private void logoutHttp() {
        SharedPreferences.Editor editor = mPref.edit();
        editor.remove("loginToken");
        editor.commit();

        // 200911: 退出登录，去除别名设置
        MiPushUtil.clearAlias(this, mUserInfo.getUserId());

        startActivity(new Intent(WorkerPrimaryActivity.this, LoginActivity.class));
        this.finish();  // 销毁此活动
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
                            dialog.dismiss();
                        }else{
                            dialog.dismiss();
                        }
                    }
                }).setTitle("提示");
    }
    // 加载弹窗
    private void initLoadingDialog(){
        mLoadingDialog = new LoadingDialog(WorkerPrimaryActivity.this, "正在操作中...");
        mLoadingDialog.setCancelable(false);
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
