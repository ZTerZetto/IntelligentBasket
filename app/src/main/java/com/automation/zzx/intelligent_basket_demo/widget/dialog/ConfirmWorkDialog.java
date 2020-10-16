package com.automation.zzx.intelligent_basket_demo.widget.dialog;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.automation.zzx.intelligent_basket_demo.R;
import com.automation.zzx.intelligent_basket_demo.widget.image.SmartImageView;
import com.jungly.gridpasswordview.GridPasswordView;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by pengchenghu on 2020/10/13.
 * Author Email: 15651851181@163.com
 * Describe:
 */
public class ConfirmWorkDialog extends Dialog implements View.OnClickListener{
    // Handler 消息
    private final static int OPEN_KEYBOARD_MSG = 101;  // 打开软键盘
    private final static int HIDE_KEYBOARD_MSG = 102;  // 关闭软键盘
    private final static int CONFIRM_WORK_MSG = 103;   // 确认上下工操作
    private final static int CANCEL_WORK_MSG = 104;    // 取消上下工操作
    private final static int TIMER_SCHDULE_MSG = 105;  // 滴答定时任务

    private ImageView closeImg;  // 关闭按钮
    private SmartImageView headImg; // 头像
    private TextView scanTypeTxt; // 扫码类型
    private TextView basketIdTxt;  // 吊篮ID
    private Button confirmBtn;
    private Button cancelBtn;

    private Context mContext;
    private int mScanType;    // 扫码确认工作类型 0:上工 1:下工
    private String mUserHeadUrl;  // 头像地址
    private String mBasketId; // 吊篮ID

    private Timer mTimer;
    private int delaySeconds = 10; // 验证延时时间

    // 消息监听
    private OnDialogOperateListener mOnDialogOperateListener;

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            InputMethodManager imm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
            switch (msg.what) {
                case OPEN_KEYBOARD_MSG:
                    imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
                    break;
                case HIDE_KEYBOARD_MSG:
                    imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
                    break;
                case CONFIRM_WORK_MSG:
                    mOnDialogOperateListener.getVerifyResult("Confirm Success");
                    ConfirmWorkDialog.this.dismiss();  // 密码框消失
                    break;
                case CANCEL_WORK_MSG:
                    ConfirmWorkDialog.this.dismiss();  // 密码框消失
                    break;
                case TIMER_SCHDULE_MSG:
                    delaySeconds -= 1;
                    confirmBtn.setText("确认( " + delaySeconds + " s)");
                    if(delaySeconds == 0){
                        mHandler.sendEmptyMessage(CONFIRM_WORK_MSG);
                    }
                    break;
            }
        }
    };

    /*
     * 构造函数
     */
    public ConfirmWorkDialog(Context context) {
        super(context);
    }

    public ConfirmWorkDialog(Context context, int themeResId) {
        super(context, themeResId);
    }

    public ConfirmWorkDialog(Context context, int themeResId, int mScanType, String mUserHeadUrl, String mBasketId
            ,OnDialogOperateListener mOnDialogOperateListener) {
        super(context, themeResId);
        this.mContext = context;
        this.mScanType = mScanType;
        this.mUserHeadUrl = mUserHeadUrl;
        this.mBasketId = mBasketId;
        this.mOnDialogOperateListener = mOnDialogOperateListener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_confirm_action);
        setCanceledOnTouchOutside(false);
        initView();
        startTimerSchedule();
    }

    private void initView(){
        // head
        closeImg = (ImageView) findViewById(R.id.close_iv);
        closeImg.setOnClickListener(this);
        headImg = (SmartImageView) findViewById(R.id.head_iv);
        headImg.setImageUrl(mUserHeadUrl);  // 设定头像
        headImg.setCircle(true);

        // body
        scanTypeTxt = (TextView) findViewById(R.id.scan_type);
        if(mScanType == 0) scanTypeTxt.setText(R.string.open_basket);
        else if(mScanType == 1) scanTypeTxt.setText(R.string.close_basket);
        basketIdTxt = (TextView) findViewById(R.id.basket_id);
        basketIdTxt.setText(mBasketId);

        // password
        confirmBtn = (Button) findViewById(R.id.confirm_btn);
        cancelBtn = (Button) findViewById(R.id.cancel_btn);
        confirmBtn.setOnClickListener(this);
        cancelBtn.setOnClickListener(this);
        mHandler.sendEmptyMessage(HIDE_KEYBOARD_MSG);  // 打开软键盘
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.close_iv:
                this.dismiss();
                break;
            case R.id.confirm_btn:
                mHandler.sendEmptyMessage(CONFIRM_WORK_MSG);
                break;
            case R.id.cancel_btn:
                mHandler.sendEmptyMessage(CANCEL_WORK_MSG);
                break;
        }
    }

    /*
     * 定时任务
     */
    // 定时跳转至主页面
    public void startTimerSchedule(){
        TimerTask delayTask = new TimerTask() {
            @Override
            public void run() {
               mHandler.sendEmptyMessage(TIMER_SCHDULE_MSG);
            }
        };
        mTimer = new Timer();
        mTimer.schedule(delayTask,10, 1000); // 延时10毫秒没1秒钟执行 run 里面的操作
    }

    /*
     * 生命周期
     */
    @Override
    public void onStart(){
        super.onStart();
    }

    @Override
    public void onStop(){
        super.onStop();
        mTimer.cancel();
    }

    /*
     * dialog 弹出位置
     */
    public void setProperty() {
        Window dialogWindow = this.getWindow();
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        dialogWindow.setGravity(Gravity.CENTER_HORIZONTAL);

        // 屏幕高度
        DisplayMetrics dm2 = Resources.getSystem().getDisplayMetrics();
        int mScreenHeight = dm2.heightPixels;

        lp.y = -(int)(mScreenHeight * 0.25);
        dialogWindow.setAttributes(lp);
    }


    /*
     * 自定义Dialog监听器
     */
    public interface OnDialogOperateListener{
        void getVerifyResult(String result);
    }

}
