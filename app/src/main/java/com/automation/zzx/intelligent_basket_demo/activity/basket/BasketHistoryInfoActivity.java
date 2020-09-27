package com.automation.zzx.intelligent_basket_demo.activity.basket;

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
import com.automation.zzx.intelligent_basket_demo.activity.common.RepairDetailActivity;
import com.automation.zzx.intelligent_basket_demo.adapter.basket.WorkPhotoAdapter;
import com.automation.zzx.intelligent_basket_demo.entity.AppConfig;
import com.automation.zzx.intelligent_basket_demo.utils.ToastUtil;
import com.automation.zzx.intelligent_basket_demo.utils.ftp.FTPUtil;
import com.automation.zzx.intelligent_basket_demo.widget.ScaleImageView;
import com.automation.zzx.intelligent_basket_demo.widget.image.WebImage;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.automation.zzx.intelligent_basket_demo.activity.basket.BasketDetailActivity.UPLOAD_PROJECT_ID;

public class BasketHistoryInfoActivity extends AppCompatActivity {

    private final static int UPDATE_INSTALL_IMAGE = 101; // 更新预验收图片
    private final static int UPDATE_CERTIFIVATE_IMAGE = 102; // 更新安监证书图片
    private final static int UPDATE_PRESTOP_IMAGE = 103; // 更新预报停图片
    private final static int NO_MORE_IMAGE = 104; // 尚无更多的截图
    private final static int UPDATE_FAIL = 104; // 更新图片失败

    private final static int INSTALL_IMAGE = 111; // 预验收图片
    private final static int CERTIFIVATE_IMAGE = 112; // 安监证书图片
    private final static int PRESTOP_IMAGE = 113; // 预报停图片


    private String mDeviceId; // 吊篮ID
    private String mProjectId; // 项目ID

    //控件
    private GridView mInstallPhotoGv;  // 预验收照片网格布局
    private GridView mCertificatePhotoGv;  // 安监证书照片网格布局
    private GridView mPreStopPhotoGv;  // 预报停照片网格布局

    // work photo gridview
    private List<Bitmap> mInstallPhotos = new ArrayList<>();  // bitmap 位图
    private List<String> mInstallPhotoUrls = new ArrayList<>(); // 文件Url
    private WorkPhotoAdapter mInstallPhotoAdapter;

    private List<Bitmap> mCertificatePhotos = new ArrayList<>();  // bitmap 位图
    private List<String> mCertificatePhotoUrls = new ArrayList<>(); // 文件Url
    private WorkPhotoAdapter mCertificatePhotoAdapter;

    private List<Bitmap> mPreStopPhotos = new ArrayList<>();  // bitmap 位图
    private List<String> mPreStopPhotoUrls = new ArrayList<>(); // 文件Url
    private WorkPhotoAdapter mPreStopPhotoAdapter;

    private List<String> mFileNameList1 = new ArrayList<>(); // 文件名
    private List<String> mFileNameList2 = new ArrayList<>(); // 文件名
    private List<String> mFileNameList3 = new ArrayList<>(); // 文件名

    // FTP 相关
    private FTPUtil mFTPClient;
    public static final String REMOTE_INSTALL_PHOTO_PATH = "project";
    public static final String REMOTE_CERTIFICATE_PHOTO_PATH = "cert";
    public static final String REMOTE_PRE_STOP_PHOTO_PATH = "storeIn";

    public static final String CAMERA_PATH = Environment.getExternalStorageDirectory() +
            File.separator + "IntelligenceBasket" + File.separator+"Camera"+ File.separator;
    // CAMERA_PATH+ "IMAGE_"+fileName+".jpg"

    // 消息Handler
    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case UPDATE_INSTALL_IMAGE:  // 更新图片
                    mFileNameList1.addAll(0, (List<String>)msg.obj);
                    generatePhotoUrls((List<String>)msg.obj, msg.arg1);
                    mInstallPhotoAdapter.notifyDataSetChanged();
                    mInstallPhotoGv.smoothScrollToPosition(0);
                    break;
                case UPDATE_CERTIFIVATE_IMAGE:  // 更新图片
                    mFileNameList2.addAll(0, (List<String>)msg.obj);
                    generatePhotoUrls((List<String>)msg.obj, msg.arg1);
                    mCertificatePhotoAdapter.notifyDataSetChanged();
                    mCertificatePhotoGv.smoothScrollToPosition(0);
                    break;
                case UPDATE_PRESTOP_IMAGE:  // 更新图片
                    mFileNameList3.addAll(0, (List<String>)msg.obj);
                    generatePhotoUrls((List<String>)msg.obj, msg.arg1);
                    mPreStopPhotoAdapter.notifyDataSetChanged();
                    mPreStopPhotoGv.smoothScrollToPosition(0);
                    break;
                case UPDATE_FAIL: // 尚无更多的图片
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
        mProjectId = intent.getStringExtra(BasketDetailActivity.UPLOAD_PROJECT_ID);  // 获取项目ID

        if(mDeviceId==null || mDeviceId.equals("")) mDeviceId = "js_nj_00003";

        initFTPClient();  // 初始化FTP连接
        initWidgetResource();  // 初始化控件
        //displayPhotoLists(REMOTE_INSTALL_PHOTO_PATH,0, "",UPDATE_INSTALL_IMAGE);  // 获取预验收相关图片
        displayCertificatePhoto(REMOTE_CERTIFICATE_PHOTO_PATH,0, "",UPDATE_CERTIFIVATE_IMAGE);  // 获取安监证书图片
        //displayPhotoLists(REMOTE_PRE_STOP_PHOTO_PATH,0, "",UPDATE_PRESTOP_IMAGE);  // 获取预报停相关图片

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
        /* 预验收照片 */
        mInstallPhotoGv = findViewById(R.id.install_photo_gv);
        mInstallPhotoAdapter = new WorkPhotoAdapter(BasketHistoryInfoActivity.this,
                R.layout.item_work_photo,mInstallPhotoUrls);


        mInstallPhotoGv.setAdapter(mInstallPhotoAdapter);
        mInstallPhotoGv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                getInstallBitmaps();

                // 显示dislog
                ScaleImageView scaleImageView = new ScaleImageView(BasketHistoryInfoActivity.this);
                scaleImageView.setUrls_and_Bitmaps(mFileNameList1, mInstallPhotos, position);
                scaleImageView.create();
            }
        });
        /* 安监证书照片 */
        mCertificatePhotoGv = findViewById(R.id.certificate_photo_gv);
        mCertificatePhotoAdapter = new WorkPhotoAdapter(BasketHistoryInfoActivity.this,
                R.layout.item_work_photo,mCertificatePhotoUrls);
        mCertificatePhotoGv.setAdapter(mCertificatePhotoAdapter);
        mCertificatePhotoGv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                getCertificateBitmaps();

                // 显示dislog
                ScaleImageView scaleImageView = new ScaleImageView(BasketHistoryInfoActivity.this);
                scaleImageView.setUrls_and_Bitmaps(mFileNameList2, mCertificatePhotos, position-1);
                scaleImageView.create();
            }
        });


        /* 安监证书照片 */
        mPreStopPhotoGv = findViewById(R.id.pre_stop_photo_gv);
        mPreStopPhotoAdapter = new WorkPhotoAdapter(BasketHistoryInfoActivity.this,
                R.layout.item_work_photo,mPreStopPhotoUrls);
        mPreStopPhotoGv.setAdapter(mPreStopPhotoAdapter);
        mPreStopPhotoGv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                getPreStopBitmaps();

                // 显示dislog
                ScaleImageView scaleImageView = new ScaleImageView(BasketHistoryInfoActivity.this);
                scaleImageView.setUrls_and_Bitmaps(mFileNameList3, mPreStopPhotos, position);
                scaleImageView.create();
            }
        });

    }


    // FTP初始化
    private void initFTPClient(){
        mFTPClient = new FTPUtil(AppConfig.FILE_SERVER_YBLIU_IP, AppConfig.FILE_SERVER_YBLIU_PORT,
                AppConfig.FILE_SERVER_USERNAME, AppConfig.FILE_SERVER_PASSWORD);
    }


    // 获取要显示图片的url
    private void displayCertificatePhoto(final String pathType,final int direction, final String filename, final int messageType){
        new Thread(){
            public void run(){
                try{
                    mFTPClient.openConnect();  // 建立连接
                    mFTPClient.downloadingInit(pathType + "/");  // 切换工作环境
                    List<String> newFileNames = mFTPClient.getDownloadFileName(direction, filename);
                    if(newFileNames == null){  // 没有更多的图片
                        mHandler.sendEmptyMessage(NO_MORE_IMAGE);
                    }else {   // 图片更新
                        Message msg = new Message();  // 通知页面更新
                        msg.what = messageType;
                        msg.arg1 = CERTIFIVATE_IMAGE;
                        msg.obj = newFileNames;
                        mHandler.sendMessage(msg);
                    }
                    mFTPClient.closeConnect();  // 关闭连接
                }catch(IOException e){
                    e.printStackTrace();
                    mHandler.sendEmptyMessage(UPDATE_FAIL);
                }
            }
        }.start();
    }

    // 获取要显示图片的url
    private void displayPhotoLists(final String pathType,final int direction, final String filename, final int messageType){
        new Thread(){
            public void run(){
                try{
                    mFTPClient.openConnect();  // 建立连接
                    mFTPClient.downloadingInit(pathType + "/" + mProjectId+"_"+mDeviceId);  // 切换工作环境
                    List<String> newFileNames = mFTPClient.getDownloadFileName(direction, filename);
                    if(newFileNames.size() == 0){  // 没有更多的图片
                        mHandler.sendEmptyMessage(NO_MORE_IMAGE);
                    }else {   // 图片更新
                        Message msg = new Message();  // 通知页面更新
                        msg.what = messageType;
                        msg.arg1 = direction;
                        msg.obj = newFileNames;
                        mHandler.sendMessage(msg);
                    }
                    mFTPClient.closeConnect();  // 关闭连接
                }catch(IOException e){
                    e.printStackTrace();
                    mHandler.sendEmptyMessage(UPDATE_FAIL);
                }
            }
        }.start();
    }

    /*
     * 其他函数
     */

    // 初始化图片地址
    private void generatePhotoUrls(List<String> newAddFileName, int type){
        String root_url;
        switch (type) {
            case INSTALL_IMAGE:
                root_url = AppConfig.FILE_SERVER_YBLIU_PATH  + REMOTE_INSTALL_PHOTO_PATH +
                        File.separator + mDeviceId + File.separator;
                for(String filename : newAddFileName)
                    mInstallPhotoUrls.add(0, root_url + filename);
                break;
            case CERTIFIVATE_IMAGE:
                root_url = AppConfig.FILE_SERVER_YBLIU_PATH + REMOTE_CERTIFICATE_PHOTO_PATH +
                        File.separator + mProjectId+"_"+ mDeviceId + ".jpg";
                mCertificatePhotoUrls.add(0, root_url);
                break;
            case PRESTOP_IMAGE:
                root_url = AppConfig.FILE_SERVER_YBLIU_PATH  + REMOTE_PRE_STOP_PHOTO_PATH +
                        File.separator + mDeviceId + File.separator;
                for(String filename : newAddFileName)
                    mPreStopPhotoUrls.add(0, root_url + filename);
                break;
            default:break;

        }


    }


    // 初始化图片位图:直接从缓存中获取
    private void getInstallBitmaps(){
        mInstallPhotos.clear();
        for(int i=0; i < mInstallPhotoUrls.size(); i++){
            String url = mInstallPhotoUrls.get(i);
            mInstallPhotos.add(null);
            mInstallPhotos.set(i, WebImage.webImageCache.get(url));
        }
    }

    private void getCertificateBitmaps(){
        mCertificatePhotos.clear();
        for(int i=0; i < mCertificatePhotoUrls.size(); i++){
            String url = mCertificatePhotoUrls.get(i);
            mCertificatePhotos.add(null);
            mCertificatePhotos.set(i, WebImage.webImageCache.get(url));
        }
    }

    private void getPreStopBitmaps(){
        mPreStopPhotos.clear();
        for(int i=0; i < mPreStopPhotoUrls.size(); i++){
            String url = mPreStopPhotoUrls.get(i);
            mPreStopPhotos.add(null);
            mPreStopPhotos.set(i, WebImage.webImageCache.get(url));
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


}
