package com.automation.zzx.intelligent_basket_demo.activity.InstallInfo;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.automation.zzx.intelligent_basket_demo.R;
import com.automation.zzx.intelligent_basket_demo.entity.AppConfig;
import com.automation.zzx.intelligent_basket_demo.entity.PortionMap;
import com.automation.zzx.intelligent_basket_demo.utils.ftp.FTPUtil;
import com.automation.zzx.intelligent_basket_demo.widget.dialog.LoadingDialog;
import com.automation.zzx.intelligent_basket_demo.widget.image.SmartImageView;

import java.io.File;

public class MultiImgUploadActivity extends AppCompatActivity implements View.OnClickListener{

    private final static String TAG = "MultiImgUploadActivity";
    // 页面跳转全局变量
    public static final String PROJECT_ID = "project_id";  // 项目ID
    public static final String BASKET_ID = "basket_id";  // 吊篮ID
    public static final String IMAGE_TYPE_ID = "image_type_id";  // 上传图片类型

    // 控件
    private SmartImageView mLeftUploadImageIv;  // 左图
    private SmartImageView mRightUploadImageIv;  // 右图

    // 全局变量
    private String projectId;  // 项目
    private String basketId;  // 吊篮
    private int imageType;  // 图片类型

    private String mUploadImageType = "提升机";  // 上传图片类型，如提升机、安全锁等；
    private String remoteFileName; // 远程文件名
    private String leftFileName;  // 左图片名
    private File leftPhotoFile ; // 图片文件
    private Uri leftPhotoUrl ; // 图片URL
    private String leftRemoteFileUrl;
    private String rightFileName;  // 右图片名
    private File rightPhotoFile ; // 图片文件
    private Uri rightPhotoUrl ; // 图片URL
    private String rightRemoteFileUrl;


    //文件服务器
    private String mRemotePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multi_img_upload);
        initIntent();
        initWidgets();
    }

    /* 初始化
     * */
    private void initWidgets(){
        // 顶部导航栏
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        TextView titleText = (TextView) findViewById(R.id.toolbar_title);
        toolbar.setTitle("");
        titleText.setText(mUploadImageType);
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true); //设置返回键可用

        mLeftUploadImageIv = (SmartImageView) findViewById(R.id.left_image_display_iv);
        mLeftUploadImageIv.setImageUrl(leftRemoteFileUrl, R.mipmap.ic_empty);
        mLeftUploadImageIv.setOnClickListener(this);
        mRightUploadImageIv = (SmartImageView) findViewById(R.id.right_image_display_iv);
        mRightUploadImageIv.setImageUrl(rightRemoteFileUrl, R.mipmap.ic_empty);
    }

    /* 消息响应
     */
    // 一般按钮响应
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.left_image_display_iv:  // 点击图片，查看大图，尚未完成
                break;

            case R.id.right_image_display_iv:  // 点击图片，查看大图，尚未完成
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
     * 工具类
     */
    // 页面传递数据
    private void initIntent(){
        Intent intent = getIntent();
        projectId = intent.getStringExtra(PROJECT_ID);
        basketId = intent.getStringExtra(BASKET_ID);
        imageType = intent.getIntExtra(IMAGE_TYPE_ID, -1);
        mRemotePath = "project/" + projectId + "/" + basketId + "/";  // 图片上传地址
        mUploadImageType = PortionMap.chinesePortion.get(imageType);
        remoteFileName = PortionMap.englishPortion.get(imageType);
        leftRemoteFileUrl = AppConfig.FILE_SERVER_YBLIU_PATH + mRemotePath + remoteFileName + "_left.jpg";
        rightRemoteFileUrl = AppConfig.FILE_SERVER_YBLIU_PATH + mRemotePath + remoteFileName + "_right.jpg";
    }


}
