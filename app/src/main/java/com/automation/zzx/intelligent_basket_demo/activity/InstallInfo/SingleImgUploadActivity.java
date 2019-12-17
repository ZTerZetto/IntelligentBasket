package com.automation.zzx.intelligent_basket_demo.activity.InstallInfo;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.automation.zzx.intelligent_basket_demo.R;
import com.automation.zzx.intelligent_basket_demo.entity.AppConfig;
import com.automation.zzx.intelligent_basket_demo.entity.PortionMap;
import com.automation.zzx.intelligent_basket_demo.utils.ftp.FTPUtil;
import com.automation.zzx.intelligent_basket_demo.widget.dialog.LoadingDialog;
import com.automation.zzx.intelligent_basket_demo.widget.image.SmartImageView;
import com.heynchy.compress.CompressImage;
import com.heynchy.compress.compressinterface.CompressLubanListener;

import java.io.File;

public class SingleImgUploadActivity extends AppCompatActivity implements View.OnClickListener{

    private final static String TAG = "SingleImgUploadActivity";

    // 页面跳转全局变量
    public static final String PROJECT_ID = "project_id";  // 项目ID
    public static final String BASKET_ID = "basket_id";  // 吊篮ID
    public static final String IMAGE_TYPE_ID = "image_type_id";  // 上传图片类型

    // 全局变量
    private String projectId;  // 项目
    private String basketId;  // 吊篮
    private int imageType;  // 图片类型


    private String mUploadImageType;  // 上传图片类型，如电柜、摄像头等；
    private String fileName;  // 图片名
    private String remoteFileName; // 远程文件名
    private String remoteFileUrl;
    private File photoFile ; // 图片文件
    private Uri photoUrl ; // 图片URL

    // 控件
    private SmartImageView mUploadImageIv;

    // FTP 文件服务器
    private FTPUtil mFTPClient;
    private String mRemotePath;
    private LoadingDialog mLoadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_img_upload);

        initIntent();
        initWidgets();
        //initFTPClient(); // 初始化文件服务器
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

        mUploadImageIv = (SmartImageView) findViewById(R.id.image_display_iv);
        mUploadImageIv.setImageUrl(remoteFileUrl, R.mipmap.ic_empty);
         /*mUploadImageIv.setOnClickListener(this);
       mUploadImageIv.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                startCameraActivity();  // 长按进入拍摄模式
                return false;
            }
        });*/

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
        remoteFileUrl = AppConfig.FILE_SERVER_YBLIU_PATH + mRemotePath + remoteFileName + ".jpg";
    }

/*
    // Luban算法压缩图片
    private void compressImage(final String filePath, final String savePath){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                CompressImage.getInstance().imageLubrnCompress(filePath, savePath, new CompressLubanListener() {
                    @Override
                    public void onCompressLubanSuccessed(String imgPath, Bitmap bitmap) {
                        */
/**
                         * 返回值: imgPath----压缩后图片的绝对路径
                         *        bitmap----返回的图片
                         *//*

                        Log.i(TAG, "Compress Success:" + imgPath);
                    }

                    @Override
                    public void onCompressLubanFailed(String imgPath, String msg) {
                        */
/**
                         * 返回值: imgPath----原图片的绝对路径
                         *        msg----返回的错误信息
                         *//*

                        Log.i(TAG, "Compress Failed:"+ imgPath + " " + msg);
                    }

                });
            }
        });
    }
*/

   /* // FTP 初始化
    private void initFTPClient(){
        mFTPClient = new FTPUtil(AppConfig.FILE_SERVER_YBLIU_IP, AppConfig.FILE_SERVER_YBLIU_PORT,
                AppConfig.FILE_SERVER_USERNAME, AppConfig.FILE_SERVER_PASSWORD);
    }
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

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.image_display_iv:  // 点击图片，查看大图，尚未完成


                break;
        }
    }
}
