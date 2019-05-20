package com.automation.zzx.intelligent_basket_demo.activity.inspectionPerson;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.automation.zzx.intelligent_basket_demo.R;
import com.automation.zzx.intelligent_basket_demo.activity.loginRegist.RegistWorkerActivity;
import com.automation.zzx.intelligent_basket_demo.entity.AppConfig;
import com.automation.zzx.intelligent_basket_demo.utils.ToastUtil;
import com.automation.zzx.intelligent_basket_demo.utils.okhttp.BaseCallBack;
import com.automation.zzx.intelligent_basket_demo.utils.okhttp.BaseOkHttpClient;
import com.automation.zzx.intelligent_basket_demo.widget.dialog.LoadingDialog;
import com.automation.zzx.intelligent_basket_demo.widget.zxing.activity.CaptureActivity;
import com.hjq.permissions.OnPermission;
import com.hjq.permissions.Permission;
import com.hjq.permissions.XXPermissions;

import java.io.IOException;
import java.util.List;

import okhttp3.Call;

import static com.automation.zzx.intelligent_basket_demo.widget.zxing.activity.CaptureActivity.QR_CODE_RESULT;

/**
 * Created by pengchenghu on 2019/5/20.
 * Author Email: 15651851181@163.com
 * Describe: 巡检人员异常上报
 * limits:
 */


public class ExceptionReportActivity extends AppCompatActivity implements View.OnClickListener {

    private final static String TAG = "ExceptionReportActivity";

    // 页面跳转标识
    public final static int CAPTURE_ACTIVITY_REQUEST = 101;

    // 消息处理标识
    private final static int LOADING_DIALOG_DISMISS = 101;
    private final static int CLEAR_EXCEPTION_CONTENT = 102;

    // 控件
    private EditText mBasketIdEdittext;
    private ImageView mScanImageView;
    private EditText mExceptionEditView;
    private Button mConfirmButton;

    // 弹窗
    private LoadingDialog mLoadingDialog;

    // 用户信息
    private String mUserId;
    private String mToken;
    private SharedPreferences mSharedPred;

    // mHandler 处理消息
    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch(msg.what){
                case LOADING_DIALOG_DISMISS:
                    mLoadingDialog.dismiss();
                    break;
                case CLEAR_EXCEPTION_CONTENT:
                    mBasketIdEdittext.setText("");
                    mExceptionEditView.setText("");
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exception_report);

        if(!isHasPermission()) requestPermission();
        initLoadingDialog();
        getUserInfo();
        initWidget();
    }

    /*
     * 初始化页面
     */
    private void initWidget(){
        // 顶部导航栏
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        TextView titleText = (TextView) findViewById(R.id.toolbar_title);
        toolbar.setTitle("");
        titleText.setText("异常信息上报");
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true); //设置返回键可用

        // 控件
        mBasketIdEdittext = (EditText) findViewById(R.id.basket_id_editview);
        mBasketIdEdittext.setOnClickListener(this);
        mScanImageView = (ImageView) findViewById(R.id.scan_imageview);
        mScanImageView.setOnClickListener(this);
        mExceptionEditView = (EditText) findViewById(R.id.exception_content_editview);
        mExceptionEditView.setOnClickListener(this);
        mConfirmButton = (Button) findViewById(R.id.confirm_button);
        mConfirmButton.setOnClickListener(this);
    }

    /*
     * 消息响应
     */
    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.basket_id_editview: // 设置吊篮ID
                break;
            case R.id.scan_imageview: // 扫码
                Log.i(TAG, "You has clicked the operater button");
                if(!isHasPermission()) requestPermission();
                startActivityForResult(new Intent(ExceptionReportActivity.this,
                        CaptureActivity.class), CAPTURE_ACTIVITY_REQUEST);
                break;
            case R.id.exception_content_editview:  // 异常信息
                break;
            case R.id.confirm_button:  // 确认按钮
                String mBasketId = mBasketIdEdittext.getText().toString();
                String mExceptionContent = mExceptionEditView.getText().toString();
                if(mBasketId==null || mBasketId.equals("")){
                    ToastUtil.showToastTips(ExceptionReportActivity.this,
                            "无效的吊篮编号，请重新输入");
                    mBasketIdEdittext.setSelection(mBasketId.length());
                    return;
                }
                if(mExceptionContent==null || mExceptionContent.equals("") || mExceptionContent.length()<=10){
                    ToastUtil.showToastTips(ExceptionReportActivity.this,
                            "异常信息过少，请补充内容");
                    mExceptionEditView.setSelection(mExceptionContent.length());
                    return;
                }
                reportException(mBasketId, mExceptionContent);
                mLoadingDialog.show();
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
     * 页面返回消息
     */
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
                    mBasketIdEdittext.setText(basketId);
                    mBasketIdEdittext.setSelection(basketId.length());
                }
                break;
            default:
                break;
        }
    }

    /*
     * 后台通信相关
     */
    // 获取项目吊篮列表
    private void reportException(String basketId, String content){
        BaseOkHttpClient.newBuilder()
                .addHeader("Authorization", mToken)
                .addParam("deviceId", basketId)
                .addParam("managerId", mUserId)
                .addParam("reason", content)
                .post()
                .url(AppConfig.INSPECTION_PERSON_EXCEPTION_REPORT)
                .build()
                .enqueue(new BaseCallBack() {
                    @Override
                    public void onSuccess(Object o) {
                        Log.d(TAG, "成功获取吊篮列表");
                        String responseData = o.toString();
                        JSONObject jsonObject = JSON.parseObject(responseData);
                        String crateState = jsonObject.getString("create");
                        mHandler.sendEmptyMessage(LOADING_DIALOG_DISMISS);
                        switch(crateState){
                            case "success":
                                ToastUtil.showToastTips(ExceptionReportActivity.this, "上传成功");
                                mHandler.sendEmptyMessage(CLEAR_EXCEPTION_CONTENT);
                                break;
                            case "notStop":
                                ToastUtil.showToastTips(ExceptionReportActivity.this, "吊篮尚未报停");
                                break;
                            case "notExistProject":
                                ToastUtil.showToastTips(ExceptionReportActivity.this, "吊篮不存在");
                                break;
                        }
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


    /*
     * 权限管理
     */
     /*
        用xxpermissions申请权限
     */
    // 申请权限
    private void requestPermission() {
        XXPermissions.with(ExceptionReportActivity.this)
                .constantRequest() //可设置被拒绝后继续申请，直到用户授权或者永久拒绝
                .permission(Permission.CAMERA) //支持请求6.0悬浮窗权限8.0请求安装权限
                .request(new OnPermission() {
                    @Override
                    public void hasPermission(List<String> granted, boolean isAll) {
                        if (isAll) {
                            //initCamera(scanPreview.getHolder());
                            onResume();
                        }else {
                            Toast.makeText(ExceptionReportActivity.this,
                                    "必须同意所有的权限才能使用本程序",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void noPermission(List<String> denied, boolean quick) {
                        if(quick) {
                            Toast.makeText(ExceptionReportActivity.this,
                                    "被永久拒绝授权，请手动授予权限",
                                    Toast.LENGTH_SHORT).show();
                            // 如果是被永久拒绝就跳转到应用权限系统设置页面
                            XXPermissions.gotoPermissionSettings(ExceptionReportActivity.this);
                        }else {
                            Toast.makeText(ExceptionReportActivity.this, "获取权限失败",
                                    Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    }
                });
    }

    // 是否有权限：摄像头
    private boolean isHasPermission() {
        if (XXPermissions.isHasPermission(ExceptionReportActivity.this, Permission.CAMERA))
            return true;
        return false;
    }

    /*
     * 弹窗提示
     */
    // 加载弹窗
    private void initLoadingDialog(){
        mLoadingDialog = new LoadingDialog(ExceptionReportActivity.this, "正在上传...");
        mLoadingDialog.setCancelable(false);
    }

    /*
     * 用户信息相关
     */
    // 从本地假造用户信息
    private void getUserInfo(){
        // 从本地获取数据
        mSharedPred = PreferenceManager.getDefaultSharedPreferences(this);
        mToken = mSharedPred.getString("loginToken","");
        mUserId = mSharedPred.getString("userId", "");
    }
}
