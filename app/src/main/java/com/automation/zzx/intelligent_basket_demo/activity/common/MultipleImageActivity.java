package com.automation.zzx.intelligent_basket_demo.activity.common;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.TextView;

import com.automation.zzx.intelligent_basket_demo.R;
import com.automation.zzx.intelligent_basket_demo.activity.basket.BasketDetailActivity;
import com.automation.zzx.intelligent_basket_demo.activity.basket.BasketPhotoActivity;
import com.automation.zzx.intelligent_basket_demo.adapter.basket.WorkPhotoAdapter;
import com.automation.zzx.intelligent_basket_demo.entity.AppConfig;
import com.automation.zzx.intelligent_basket_demo.utils.ToastUtil;
import com.automation.zzx.intelligent_basket_demo.utils.ftp.FTPUtil;
import com.automation.zzx.intelligent_basket_demo.widget.ScaleImageView;
import com.automation.zzx.intelligent_basket_demo.widget.image.WebImage;
import com.scwang.smartrefresh.header.BezierCircleHeader;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.constant.SpinnerStyle;
import com.scwang.smartrefresh.layout.footer.BallPulseFooter;
import com.scwang.smartrefresh.layout.listener.OnLoadMoreListener;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MultipleImageActivity extends AppCompatActivity {
    private final static int PULL_DOWN = 102; // 下拉刷新
    private final static int UPDATE_IMAGE = 103; // 更新图片
    private final static int NO_MORE_IMAGE = 104; // 尚无更多的截图
    private final static int UPDATE_FAILED = 105; // 更新失败
    private final static int UPDATE_SUCESS = 106; // 更新成功

    // 控件声明
    private RefreshLayout mSmartRefreshLayout;
    private GridView mWorkPhotoGv;  // 网格布局

    // work photo gridview
    private List<Bitmap> mWorkPhotos = new ArrayList<>();  // bitmap 位图
    private List<String> mWorkPtotoUrls = new ArrayList<>(); // 文件Url
    private List<String> mFileNameList = new ArrayList<>(); // 文件名
    private WorkPhotoAdapter mWorkPhotoAdapter;

    // 吊篮相关
    private String mBasketId; // 吊篮ID

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
                case PULL_DOWN:  // 下拉刷新
                    //downloadWorkPhoto(1, mFileNameList.get(0));
                    displayPhoto(1, mFileNameList.get(0));
                    break;
                case UPDATE_IMAGE:  // 更新图片
                    if(msg.arg1==1){
                        // 下拉刷新：加载最新图片，并且定位在第一行
                        mFileNameList.addAll(0, (List<String>)msg.obj);
                        generatePhotoUrls((List<String>)msg.obj, msg.arg1);
                        mWorkPhotoAdapter.notifyDataSetChanged();
                        mWorkPhotoGv.smoothScrollToPosition(0);
                    }else{
                        // 上拉加载更多：加载历史图片，并且定位在最后一行
                        mFileNameList.addAll((List<String>)msg.obj);
                        generatePhotoUrls((List<String>)msg.obj, msg.arg1);
                        mWorkPhotoAdapter.notifyDataSetChanged();
                        mWorkPhotoGv.smoothScrollToPosition(mFileNameList.size());
                    }
                    break;
                case NO_MORE_IMAGE: // 尚无更多的图片
                    ToastUtil.showToastTips(MultipleImageActivity.this, "尚无更多的图片");
                    mSmartRefreshLayout.finishRefresh(500); // 刷新动画结束
                    mSmartRefreshLayout.finishLoadMore(500); // 加载动画结束
                    break;
                case UPDATE_FAILED: // 更新失败
                    ToastUtil.showToastTips(MultipleImageActivity.this, "加载失败，请检查网络");
                    mSmartRefreshLayout.finishRefresh(500, false); // 刷新动画结束
                    mSmartRefreshLayout.finishLoadMore(500); // 加载动画结束
                    break;
                case UPDATE_SUCESS:  // 更新成功
                    mSmartRefreshLayout.finishRefresh(500); // 刷新动画结束
                    mSmartRefreshLayout.finishLoadMore(500); // 加载动画结束
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_basket_photo);

        Intent intent = getIntent();
        mBasketId = intent.getStringExtra(BasketDetailActivity.BASKET_ID);  // 获取吊篮ID
        if(mBasketId==null || mBasketId.equals("")) mBasketId = "js_nj_00003";

        initFTPClient();  // 初始化FTP连接
        initWidgetResource(); // 初始化控件资源
        displayPhoto(0, "");  // 获取相关图片

    }

    // 控件初始化
    private void initWidgetResource() {
        // 上拉、下拉刷新
        mSmartRefreshLayout = (SmartRefreshLayout) findViewById(R.id.smart_refresh_layout);
        mSmartRefreshLayout.setRefreshHeader(  //设置 Header 为 贝塞尔雷达 样式
                new BezierCircleHeader(this));
        mSmartRefreshLayout.setRefreshFooter(  //设置 Footer 为 球脉冲 样式
                new BallPulseFooter(this).setSpinnerStyle(SpinnerStyle.Scale));
        mSmartRefreshLayout.setPrimaryColorsId(R.color.smart_loading_background_color);
        mSmartRefreshLayout.setOnRefreshListener(new OnRefreshListener() { // 添加下拉刷新监听
            @Override
            public void onRefresh(RefreshLayout refreshlayout) {
                if(mFileNameList.size() > 0)
                    mHandler.sendEmptyMessage(PULL_DOWN);
            }
        });

        // 顶部导航栏
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        TextView titleText = (TextView) findViewById(R.id.toolbar_title);
        toolbar.setTitle("");
        titleText.setText(getString(R.string.work_photo_title));
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true); //设置返回键可用

        // work photo gridview
        mWorkPhotoGv = (GridView) findViewById(R.id.work_photo_gv);
        mWorkPhotoAdapter = new WorkPhotoAdapter(MultipleImageActivity.this,
                R.layout.item_work_photo, mWorkPtotoUrls);
        mWorkPhotoGv.setAdapter(mWorkPhotoAdapter);
        mWorkPhotoGv.setOnItemClickListener(new AdapterView.OnItemClickListener() {  // 点击图片响应
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                getBitmaps();

                // 显示dislog
                ScaleImageView scaleImageView = new ScaleImageView(MultipleImageActivity.this);
                scaleImageView.setUrls_and_Bitmaps(mFileNameList, mWorkPhotos, position);
                scaleImageView.create();
            }
        });
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

    // FTP初始化
    private void initFTPClient(){
        mFTPClient = new FTPUtil(AppConfig.FILE_SERVER_YBLIU_IP, AppConfig.FILE_SERVER_YBLIU_PORT,
                AppConfig.FILE_SERVER_USERNAME, AppConfig.FILE_SERVER_PASSWORD);
    }

    // 获取要显示图片的url
    private void displayPhoto(final int direction, final String filename){
        new Thread(){
            public void run(){
                try{
                    mFTPClient.openConnect();  // 建立连接
                    mFTPClient.downloadingInit(REMOTE_WORK_PHOTO_PATH + "/" + mBasketId);  // 切换工作环境
                    List<String> newFileNames = mFTPClient.getDownloadFileName(direction, filename);
                    if(newFileNames.size() == 0){  // 没有更多的图片
                        mHandler.sendEmptyMessage(NO_MORE_IMAGE);
                    }else {   // 图片更新
                        Message msg = new Message();  // 通知页面更新
                        msg.what = UPDATE_IMAGE;
                        msg.arg1 = direction;
                        msg.obj = newFileNames;
                        mHandler.sendMessage(msg);
                    }
                    mFTPClient.closeConnect();  // 关闭连接
                    mHandler.sendEmptyMessage(UPDATE_SUCESS);
                }catch(IOException e){
                    e.printStackTrace();
                    mHandler.sendEmptyMessage(UPDATE_FAILED);
                }
            }
        }.start();
    }

    /*
     * 其他函数
     */

    // 初始化图片地址
    private void generatePhotoUrls(List<String> newAddFileName, int direction){
        String root_url = AppConfig.FILE_SERVER_YBLIU_PATH + File.separator + REMOTE_WORK_PHOTO_PATH +
                File.separator + mBasketId + File.separator;

        if(direction==1){
            for(String filename : newAddFileName)
                mWorkPtotoUrls.add(0, root_url + filename);
        }else{
            for(String filename : newAddFileName)
                mWorkPtotoUrls.add(root_url + filename);
        }
    }

    // 初始化图片位图:直接从缓存中获取
    private void getBitmaps(){
        mWorkPhotos.clear();

        for(int i=0; i < mWorkPtotoUrls.size(); i++){
            String url = mWorkPtotoUrls.get(i);
            mWorkPhotos.add(null);
            mWorkPhotos.set(i, WebImage.webImageCache.get(url));
        }
    }

}
