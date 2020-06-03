package com.automation.zzx.intelligent_basket_demo.activity.areaAdmin;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.automation.zzx.intelligent_basket_demo.R;
import com.automation.zzx.intelligent_basket_demo.entity.AppConfig;
import com.automation.zzx.intelligent_basket_demo.utils.ToastUtil;
import com.automation.zzx.intelligent_basket_demo.utils.okhttp.BaseCallBack;
import com.automation.zzx.intelligent_basket_demo.utils.okhttp.BaseOkHttpClient;
import com.automation.zzx.intelligent_basket_demo.widget.dialog.CommonDialog;
import com.automation.zzx.intelligent_basket_demo.widget.dialog.LoadingDialog;
import com.automation.zzx.intelligent_basket_demo.widget.zxing.activity.CaptureActivity;
import com.hjq.permissions.OnPermission;
import com.hjq.permissions.Permission;
import com.hjq.permissions.XXPermissions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.Null;

import okhttp3.Call;

import static com.automation.zzx.intelligent_basket_demo.widget.zxing.activity.CaptureActivity.QR_CODE_RESULT;

/**
 * Created by pengchenghu on 2019/5/20.
 * Author Email: 15651851181@163.com
 * Describe: 巡检人员异常上报
 * limits:
 */


public class AreaAdminSumitReportActivity extends AppCompatActivity implements View.OnClickListener {

    private final static String TAG = "ExceptionReportActivity";

    // 消息处理标识
    private final static int LOADING_DIALOG_DISMISS = 101;
    private final static int CLEAR_EXCEPTION_CONTENT = 102;
    private final static int GET_UPLOAD_INFO = 103;  // 提交成功
    private final static int GET_UPLOAD_WRONG = 104;  // 提交失败

    // intent 消息参数
    public final static String PROJECT_ID = "projectId";  // 上传图片的项目Id
    public final static String BASKET_ID = "basketId"; // 上传图片的吊篮ID


    // 控件
    private Spinner spinnerType;
    private EditText mExceptionEditView;
    private Button mConfirmButton;

    // 弹窗
    private LoadingDialog mLoadingDialog;

    // 用户信息
    private String mUserId;
    private String mToken;
    private SharedPreferences mSharedPred;

    //吊篮/项目信息
    private String basketId;
    private String projectId;

    // 审批信息
    private List<String> check_list;//审批选择器，通过or不通过
    private int isCheck; //审批结果
    private String description;//审批意见
    private ArrayAdapter<String> adapter;

    //网络通信消息
    private int isSuccess = 0; //是否上传成功


    // mHandler 处理消息
    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch(msg.what){
                case LOADING_DIALOG_DISMISS:
                    mLoadingDialog.dismiss();
                    break;
                case GET_UPLOAD_INFO:  // 上传图片成功
                    DialogToast("提示", "成功提交！" ).show();
                    isSuccess = 1;
                    sendEmptyMessage(LOADING_DIALOG_DISMISS);
                    break;
                case GET_UPLOAD_WRONG: // 上传图片失败
                    DialogToast("提示", "提交失败，请稍后再试！").show();
                    isSuccess = 0;
                    sendEmptyMessage(LOADING_DIALOG_DISMISS);
                    break;
                case CLEAR_EXCEPTION_CONTENT:
                    mExceptionEditView.setText("");
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_area_admin_submit_report);

        initLoadingDialog();
        getUserInfo();
        initWidget();
    }

    /*
     * 初始化页面
     */
    private void initWidget(){

        isSuccess = 0;

        // 顶部导航栏
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        TextView titleText = (TextView) findViewById(R.id.toolbar_title);
        toolbar.setTitle("");
        titleText.setText("预检意见提交");
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true); //设置返回键可用

        // 控件
        spinnerType = (Spinner) findViewById(R.id.spinner_type_choose);
        mExceptionEditView = (EditText) findViewById(R.id.exception_content_editview);
        mExceptionEditView.setOnClickListener(this);
        mConfirmButton = (Button) findViewById(R.id.confirm_button);
        mConfirmButton.setOnClickListener(this);

        //性别选择
        check_list = new ArrayList<>();
        check_list.add("不通过");
        check_list.add("通过");

        //审批结果选择适配器初始化
        isCheck = 0;
        if (check_list == null || check_list.isEmpty()) {
            return;
        }
        spinnerType.setDropDownVerticalOffset(50); //下拉的纵向偏移
        adapter = new ArrayAdapter<String>(this,R.layout.spinner_left_item,check_list);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerType.setAdapter(adapter);
        spinnerType.setSelection(0, true); // 设置默认值为:不通过

        spinnerType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String type = (String) spinnerType.getSelectedItem();
                switch (type){
                    case "不通过":
                        isCheck = 0;
                    case "通过":
                        isCheck = 1;
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    /*
     * 消息响应
     */
    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.exception_content_editview:  // 异常信息
                break;
            case R.id.confirm_button:  // 确认按钮
                String mExceptionContent = mExceptionEditView.getText().toString();
                if(mExceptionContent==null || mExceptionContent.equals("") || mExceptionContent.length()<=10){
                    ToastUtil.showToastTips(AreaAdminSumitReportActivity.this,
                            "信息过少，请补充内容");
                    mExceptionEditView.setSelection(mExceptionContent.length());
                    return;
                }else if(mExceptionContent.length() >=140){
                    ToastUtil.showToastTips(AreaAdminSumitReportActivity.this,
                            "信息过长，请控制在140字以内");
                    return;
                }
                description = mExceptionContent;
                submitCheckResult(description,isCheck);
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
     * 后台通信相关
     */
    // 提交预检结果
    private void submitCheckResult(String description,int check){
        BaseOkHttpClient.newBuilder()
                .addHeader("Authorization", mToken)
                .addParam("projectId", projectId)
                .addParam("deviceId",basketId)
                .addParam("managerId", null)
                .addParam("dealerId", mUserId)
                .addParam("description", description)
                .addParam("check", check)
                .post()
                .url(AppConfig.AREA_ADMIN_CHECK_INSTALL)
                .build()
                .enqueue(new BaseCallBack() {
                    @Override
                    public void onSuccess(Object o) {
                        Log.d(TAG, "成功提交安监证书申请");
                        mHandler.sendEmptyMessage(GET_UPLOAD_INFO);
                    }

                    @Override
                    public void onError(int code) {
                        Log.d(TAG, "安监证书申请错误，错误编码："+code);
                        mHandler.sendEmptyMessage(GET_UPLOAD_WRONG);
                    }

                    @Override
                    public void onFailure(Call call, IOException e) {
                        Log.d(TAG, "安监证书申请失败");
                        mHandler.sendEmptyMessage(GET_UPLOAD_WRONG);
                    }
                });
    }



    /*
     * 弹窗提示
     */
    // 加载弹窗
    private void initLoadingDialog(){
        mLoadingDialog = new LoadingDialog(AreaAdminSumitReportActivity.this, "正在上传...");
        mLoadingDialog.setCancelable(false);
    }
    // 提示弹框
    private CommonDialog DialogToast(String mTitle, String mMsg){
        return new CommonDialog(AreaAdminSumitReportActivity.this, R.style.dialog, mMsg,
                new CommonDialog.OnCloseListener() {
                    @Override
                    public void onClick(Dialog dialog, boolean confirm) {
                        if (confirm) {
                            dialog.dismiss();
                            if(isSuccess==1){
                                Intent intent = new Intent();
                                if(isCheck == 1){
                                    setResult(RESULT_OK, intent);
                                }else if(isCheck == 0){
                                    setResult(RESULT_FIRST_USER, intent);
                                }
                            }
                            finish();
                        } else {
                            dialog.dismiss();
                        }
                    }
                }).setTitle(mTitle);
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

        Intent intent = getIntent();
        projectId = intent.getStringExtra(PROJECT_ID);
        basketId = intent.getStringExtra(BASKET_ID);
    }

    /*
     * 生命周期
     */
    @Override
    public void onBackPressed(){
        Log.i(TAG, "onBackPressed");
        if(isSuccess==1){
            Intent intent = new Intent();
            if(isCheck == 1){
                setResult(RESULT_OK, intent);
            }else if(isCheck == 0){
                setResult(RESULT_FIRST_USER, intent);
            }
        }
        finish();
    }

}
