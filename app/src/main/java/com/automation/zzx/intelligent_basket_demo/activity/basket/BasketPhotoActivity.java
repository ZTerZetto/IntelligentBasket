package com.automation.zzx.intelligent_basket_demo.activity.basket;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.TextView;

import com.automation.zzx.intelligent_basket_demo.R;
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

import static com.automation.zzx.intelligent_basket_demo.entity.AppConfig.FILE_SERVER_HPC_PATH;

/**
 * Created by pengchenghu on 2019/2/23.
 * Author Email: 15651851181@163.com
 * Describe: 吊篮运行截图监视活动
 * limits:
 */

public class BasketPhotoActivity extends AppCompatActivity {

    private final static int PULL_UP = 101;  // 上拉加载更多
    private final static int PULL_DOWN = 102; // 下拉刷新
    private final static int UPDATE_IMAGE = 103; // 更新图片
    private final static int NO_MORE_IMAGE = 104; // 尚无更多的截图
    private final static int UPDATE_FAILED = 105; // 更新失败
    private final static int UPDATE_SUCESS = 106; // 更新成功

    // 控件声明
    private RefreshLayout mSmartRefreshLayout;
    private GridView mWorkPhotoGv;  // 网格布局

    // work photo gridview
    private List<Bitmap> mWorkPhotos = new ArrayList<>();
    private List<String> mFileNameList = new ArrayList<>();
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
                case PULL_UP:  // 上拉加载更多
                    downloadWorkPhoto(2, mFileNameList.get(mFileNameList.size()-1));
                    break;
                case PULL_DOWN:  // 下拉刷新
                    downloadWorkPhoto(1, mFileNameList.get(0));
                    break;
                case UPDATE_IMAGE:  // 更新图片
                    if(msg.what==1){
                        // 下拉刷新：加载最新图片，并且定位在第一行
                        mFileNameList.add(0, (String)msg.obj);
                        mWorkPhotos.add(0, (Bitmap) BitmapFactory.decodeFile(
                                LOCAL_WORK_PHOTO_PATH + mBasketId + File.separator +  (String)msg.obj));
                        mWorkPhotoAdapter.notifyDataSetChanged();
                        mWorkPhotoGv.smoothScrollToPosition(0);
                    }else{
                        // 上拉加载更多：加载历史图片，并且定位在最后一行
                        mFileNameList.add((String)msg.obj);
                        mWorkPhotos.add((Bitmap) BitmapFactory.decodeFile(LOCAL_WORK_PHOTO_PATH
                                + mBasketId + File.separator +  (String)msg.obj));
                        mWorkPhotoAdapter.notifyDataSetChanged();
                        mWorkPhotoGv.smoothScrollToPosition(mFileNameList.size());
                    }
                    break;
                case NO_MORE_IMAGE: // 尚无更多的图片
                    ToastUtil.showToastTips(BasketPhotoActivity.this, "尚无更多的图片");
                    mSmartRefreshLayout.finishRefresh(500); // 刷新动画结束
                    mSmartRefreshLayout.finishLoadMore(500); // 加载动画结束
                    break;
                case UPDATE_FAILED: // 更新失败
                    ToastUtil.showToastTips(BasketPhotoActivity.this, "加载失败，请检查网络");
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
        //mBasketId = intent.getStringExtra(HANGING_BASKET_ID);  // 获取吊篮ID
        if(mBasketId==null || mBasketId.equals("")) mBasketId = "js_nj_00003";

        initLocalDir();  // 初始化本地资源目录
        initFTPClient();  // 初始化FTP连接
        initWidgetResource(); // 初始化控件资源
        downloadWorkPhoto(0, "");  // 获取相关图片
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
        mSmartRefreshLayout.setOnLoadMoreListener(new OnLoadMoreListener() { // 添加上拉加载更多监听
            @Override
            public void onLoadMore(RefreshLayout refreshlayout) {
                if(mFileNameList.size() > 0)
                    mHandler.sendEmptyMessage(PULL_UP);
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
        mWorkPhotoAdapter = new WorkPhotoAdapter(BasketPhotoActivity.this,
                R.layout.item_work_photo, mWorkPhotos);
        mWorkPhotoGv.setAdapter(mWorkPhotoAdapter);
        mWorkPhotoGv.setOnItemClickListener(new AdapterView.OnItemClickListener() {  // 点击图片响应
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // 显示dislog
                ScaleImageView scaleImageView = new ScaleImageView(BasketPhotoActivity.this);
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

    /*
     * FTP 相关
     */
    // 本地文件初始化
    private void initLocalDir(){
        File file = new File(LOCAL_WORK_PHOTO_PATH + mBasketId + File.separator);
        if(!file.exists()) file.mkdirs();
    }

    // FTP初始化
    private void initFTPClient(){
        mFTPClient = new FTPUtil(AppConfig.FILE_SERVER_YBLIU_IP, AppConfig.FILE_SERVER_YBLIU_PORT,
                AppConfig.FILE_SERVER_USERNAME, AppConfig.FILE_SERVER_PASSWORD);
    }

    // 下载图片
    private void downloadWorkPhoto(final int direction, final String filename){
        new Thread(){
            public void run(){
                try{
                    mFTPClient.openConnect();  // 建立连接
                    mFTPClient.downloadingInit(REMOTE_WORK_PHOTO_PATH + "/" + mBasketId);  // 切换工作环境
                    List<String> newFileNames = mFTPClient.getDownloadFileName(direction, filename);
                    if(newFileNames.size() == 0){  // 没有更多的图片
                        mHandler.sendEmptyMessage(NO_MORE_IMAGE);
                        return;
                    }
                    for(String filename: newFileNames){
                        if(! new File(LOCAL_WORK_PHOTO_PATH + mBasketId, filename).exists()){
                            // 文件不存在
                            mFTPClient.downloadFile(filename, LOCAL_WORK_PHOTO_PATH + mBasketId);
                        }
                        Message msg = new Message();  // 通知页面更新
                        msg.what = UPDATE_IMAGE;
                        msg.obj = filename;
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
    private void initWorkPhotoUrls(){
        mWorkPtotoUrls = new ArrayList<>();

        String root_url = FILE_SERVER_HPC_PATH + "/basket/001/";

        mWorkPtotoUrls.add(root_url + "201902251424.jpg");
        mWorkPtotoUrls.add(root_url + "201902251425.jpg");
        mWorkPtotoUrls.add(root_url + "201902251426.jpg");
        mWorkPtotoUrls.add(root_url + "201902251427.jpg");
        mWorkPtotoUrls.add(root_url + "201902251428.jpg");
        mWorkPtotoUrls.add(root_url + "201902251429.jpg");
        mWorkPtotoUrls.add(root_url + "201902251430.jpg");
        mWorkPtotoUrls.add(root_url + "201902251431.jpg");
    }

    // 初始化图片位图:直接从缓存中获取
    private void getBitmaps(){
        mWorkPhotos = new ArrayList<>();
        for(int i=0; i < mWorkPtotoUrls.size(); i++){
            String url = mWorkPtotoUrls.get(i);
            mWorkPhotos.add(null);
            mWorkPhotos.set(i, WebImage.webImageCache.get(url));
        }
    }

}
