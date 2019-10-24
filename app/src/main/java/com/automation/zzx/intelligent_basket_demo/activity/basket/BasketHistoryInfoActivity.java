package com.automation.zzx.intelligent_basket_demo.activity.basket;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.GridView;
import android.widget.TextView;

import com.automation.zzx.intelligent_basket_demo.R;
import com.automation.zzx.intelligent_basket_demo.entity.AppConfig;
import com.automation.zzx.intelligent_basket_demo.utils.ToastUtil;
import com.automation.zzx.intelligent_basket_demo.utils.ftp.FTPUtil;
import com.automation.zzx.intelligent_basket_demo.widget.image.WebImage;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class BasketHistoryInfoActivity extends AppCompatActivity {

    private final static int UPDATE_IMAGE = 103; // 更新图片
    private final static int NO_MORE_IMAGE = 104; // 尚无更多的截图

    // 吊篮相关
    private String mDeviceId; // 吊篮ID

    //控件
    private GridView mInstallPhotoGv;  // 预验收照片网格布局
    private GridView mCertificatePhotoGv;  // 安监证书照片网格布局
    private GridView mPreStopPhotoGv;  // 预报停照片网格布局

    // FTP 相关
    private FTPUtil mFTPClient;
    public static final String REMOTE_WORK_PHOTO_PATH = "workPhoto";
    public static final String LOCAL_WORK_PHOTO_PATH = Environment.getExternalStorageDirectory() +
            File.separator + "IntelligenceBasket" + File.separator+"workPhoto"+ File.separator;


    // 消息Handler
    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case UPDATE_IMAGE:  // 更新图片
                    if(msg.arg1==1){

                    }else{

                    }
                    break;
                case NO_MORE_IMAGE: // 尚无更多的图片
                    ToastUtil.showToastTips(BasketHistoryInfoActivity.this, "尚无更多的图片");
                    break;
            }
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_basket_history_info);

        Intent intent = getIntent();
        mDeviceId = intent.getStringExtra(BasketDetailActivity.BASKET_ID);  // 获取吊篮ID
        if(mDeviceId==null || mDeviceId.equals("")) mDeviceId = "1";

        initWidgetResource();  // 初始化控件
        initFTPClient();  // 初始化FTP连接
        //displayWorkPhoto(0, "");  // 获取相关图片

    }
    private void initWidgetResource(){
        // 顶部导航栏
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        TextView titleText = (TextView) findViewById(R.id.toolbar_title);
        toolbar.setTitle("");
        titleText.setText(getString(R.string.history_info_title));
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true); //设置返回键可用

        //初始化控件
        mInstallPhotoGv = findViewById(R.id.install_photo_gv);
        mCertificatePhotoGv = findViewById(R.id.certificate_photo_gv);
        mPreStopPhotoGv = findViewById(R.id.pre_stop_photo_gv);

    }


    // FTP初始化
    private void initFTPClient(){
        mFTPClient = new FTPUtil(AppConfig.FILE_SERVER_YBLIU_IP, AppConfig.FILE_SERVER_YBLIU_PORT,
                AppConfig.FILE_SERVER_USERNAME, AppConfig.FILE_SERVER_PASSWORD);
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
}
