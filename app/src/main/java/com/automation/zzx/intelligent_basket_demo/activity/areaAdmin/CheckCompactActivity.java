package com.automation.zzx.intelligent_basket_demo.activity.areaAdmin;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Canvas;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.automation.zzx.intelligent_basket_demo.R;
import com.automation.zzx.intelligent_basket_demo.entity.AppConfig;
import com.automation.zzx.intelligent_basket_demo.entity.ProjectInfo;
import com.automation.zzx.intelligent_basket_demo.utils.ToastUtil;
import com.automation.zzx.intelligent_basket_demo.utils.ftp.FTPUtil;
import com.hjq.permissions.OnPermission;
import com.hjq.permissions.Permission;
import com.hjq.permissions.XXPermissions;
import com.joanzapata.pdfview.PDFView;
import com.joanzapata.pdfview.listener.OnDrawListener;
import com.joanzapata.pdfview.listener.OnPageChangeListener;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Created by pengchenghu on 2019/4/14.
 * Author Email: 15651851181@163.com
 * Describe: 查看项目合同书
 * limits:
 */

public class CheckCompactActivity extends AppCompatActivity {

    private final static String TAG = "CheckCompactActivity";

    private String projectId;

    // Message
    private final static int DISPLAY_COMPACT = 101;
    private final static int DISPLAY_EMPTY = 102;

    // 控件
    private PDFView mCompactPDFView;
    private ProgressBar mCompactLoadingProgressBar;
    private RelativeLayout mCompactLoadingRv;

    // FTP
    private FTPUtil mFTPClient;
    private String mRemotePath ;
    private String mFileName;
    // 合同位置
    public static final String CONTRACT_PATH = Environment.getExternalStorageDirectory() +
            File.separator + "IntelligenceBasket" + File.separator+"contract"+ File.separator;
    /*
     * Handler 线程消息
     */
    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what){
                case DISPLAY_COMPACT:
                    showCompactPDFView();
                    break;
                case DISPLAY_EMPTY:
                    mCompactLoadingRv.setVisibility(View.GONE);
                    ToastUtil.showToastTips(CheckCompactActivity.this, "暂无文件，请联系超级管理员");
                    break;

            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_compact);

        if(!isHasPermission()) requestPermission();

        Intent intent = this.getIntent();
        projectId = intent.getStringExtra("projectId");
        mRemotePath = "project/" + projectId;
        initWidgetResource();  // 初始化控件
        initFTPClient();
        initLocalDir();

        if(new File(CONTRACT_PATH +mRemotePath + projectId + ".pdf").exists()){
            mFileName =projectId+ ".pdf";
            mHandler.sendEmptyMessage(DISPLAY_COMPACT);
        }else if(new File(CONTRACT_PATH + mRemotePath +projectId+ ".jpg").exists()) {
            mFileName = projectId+ ".jpg";
            mHandler.sendEmptyMessage(DISPLAY_COMPACT);
        }else{
            new Thread() {
                public void run() {
                    downloadCompactFile();
                }
            }.start();
        }
    }

    /*
     * 控件初始化
     */
    private void initWidgetResource() {
        // 顶部导航栏
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        TextView titleText = (TextView) findViewById(R.id.toolbar_title);
        toolbar.setTitle("项目合同");
        titleText.setText("");
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true); //设置返回键可用

        // 主体
        mCompactPDFView = (PDFView) findViewById(R.id.compact_pdfView);
        mCompactLoadingProgressBar = (ProgressBar) findViewById(R.id.compact_loading_progressbar);
        mCompactLoadingRv = (RelativeLayout) findViewById(R.id.compact_loading_rv);

    }

    /*
     * 重构函数
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

    /*
     * FTP 相关
     */
    // 本地文件初始化
    private void initLocalDir(){
        File file = new File(CONTRACT_PATH);
        if(!file.exists())
            file.mkdirs();
    }
    // FTP 初始化
    private void initFTPClient(){
        mFTPClient = new FTPUtil(AppConfig.FILE_SERVER_YBLIU_IP, AppConfig.FILE_SERVER_YBLIU_PORT,AppConfig.FILE_SERVER_USERNAME,
                AppConfig.FILE_SERVER_PASSWORD);

    }
    // 下载文件
    private void downloadCompactFile(){
        try {
            mFTPClient.openConnect();  // 建立连接
            //先看看有哪个文件存在
            mFTPClient.uploadingInit(mRemotePath); // 上传文件初始化
            if(mFTPClient.listCurrentFileNames()!=null ){
                List<String>  filenames = mFTPClient.listCurrentFileNames();
                if (filenames.contains(projectId+".pdf")){
                    mFileName = projectId+".pdf";
                    mFTPClient.download(mRemotePath, mFileName, CONTRACT_PATH);
                    mFTPClient.closeConnect();  // 关闭连接
                    mHandler.sendEmptyMessage(DISPLAY_COMPACT);
                }else if(filenames.contains(projectId+".jpg")){
                    mFileName = projectId+".jpg";
                    mFTPClient.download(mRemotePath, mFileName, CONTRACT_PATH);
                    mFTPClient.closeConnect();  // 关闭连接
                    mHandler.sendEmptyMessage(DISPLAY_COMPACT);
                } else {
                    //无可下载文件
                    mFTPClient.closeConnect();  // 关闭连接
                    mHandler.sendEmptyMessage(DISPLAY_EMPTY);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /*
     * UI 相关
     */
    // 显示下载的文件
    private void showCompactPDFView() {
        mCompactLoadingRv.setVisibility(View.GONE);
        mCompactPDFView.setVisibility(View.VISIBLE);

        mCompactPDFView.fromFile(new File(CONTRACT_PATH + mFileName))
                .defaultPage(1)          //设置默认显示第1页
                .showMinimap(false)      //pdf放大的时候，是否在屏幕的右上角生成小地图
                .swipeVertical(false)  //pdf文档翻页是否是垂直翻页，默认是左右滑动翻页
                .enableSwipe(true)       //是否允许翻页，默认是允许翻
                .onDraw(new OnDrawListener() {  //绘图监听
                    @Override
                    public void onLayerDrawn(Canvas canvas, float pageWidth, float pageHeight, int displayedPage) {

                    }
                })
                .onPageChange(new OnPageChangeListener() {  //用户翻页时回调
                    @Override
                    public void onPageChanged(int page, int pageCount) {
                        //Toast.makeText(getApplicationContext(),page + "/" + pageCount,Toast.LENGTH_SHORT).show();
                    }
                })
                .load();
    }

    /*
     * 申请权限
     */
    private void requestPermission() {
        XXPermissions.with(CheckCompactActivity.this)
                .constantRequest() //可设置被拒绝后继续申请，直到用户授权或者永久拒绝
                .permission(Permission.Group.STORAGE) //支持请求6.0悬浮窗权限8.0请求安装权限
                .request(new OnPermission() {
                    @Override
                    public void hasPermission(List<String> granted, boolean isAll) {
                        if (isAll) {
                            onResume();

                        }else {
                            Toast.makeText(CheckCompactActivity.this,
                                    "必须同意所有的权限才能使用本程序", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void noPermission(List<String> denied, boolean quick) {
                        if(quick) {
                            Toast.makeText(CheckCompactActivity.this, "被永久拒绝授权，请手动授予权限",
                                    Toast.LENGTH_SHORT).show();
                            // 如果是被永久拒绝就跳转到应用权限系统设置页面
                            XXPermissions.gotoPermissionSettings(CheckCompactActivity.this);
                        }else {
                            Toast.makeText(CheckCompactActivity.this, "获取权限失败",
                                    Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    }
                });
    }
    // 是否有权限：摄像头、拨打电话
    private boolean isHasPermission() {
        if (XXPermissions.isHasPermission(CheckCompactActivity.this, Permission.Group.STORAGE))
            return true;
        return false;
    }
}
