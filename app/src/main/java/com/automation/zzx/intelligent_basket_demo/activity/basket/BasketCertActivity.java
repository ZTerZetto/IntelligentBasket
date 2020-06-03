package com.automation.zzx.intelligent_basket_demo.activity.basket;

import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.TextView;

import com.automation.zzx.intelligent_basket_demo.R;
import com.automation.zzx.intelligent_basket_demo.adapter.basket.WorkPhotoAdapter;
import com.automation.zzx.intelligent_basket_demo.adapter.worker.ImageGridAdapter;
import com.automation.zzx.intelligent_basket_demo.entity.AppConfig;
import com.automation.zzx.intelligent_basket_demo.widget.ScaleImageView;
import com.automation.zzx.intelligent_basket_demo.widget.image.WebImage;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class BasketCertActivity extends AppCompatActivity {


    private String mDeviceId; // 吊篮ID
    private String mProjectId; // 项目ID

    private GridView mCertificatePhotoGv;  // 安监证书照片网格布局


    private List<String> mFileNameList = new ArrayList<>(); // 文件名

    private List<Bitmap> mCertificatePhotos = new ArrayList<>();  // bitmap 位图
    private List<String> mCertificatePhotoUrls = new ArrayList<>(); // 文件Url
    private ImageGridAdapter mCertificatePhotoAdapter;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_basket_cert);

        Intent intent = getIntent();
        mDeviceId = intent.getStringExtra(BasketDetailActivity.BASKET_ID);  // 获取吊篮ID
        mProjectId = intent.getStringExtra(BasketDetailActivity.UPLOAD_PROJECT_ID);  // 获取项目ID

        if(mDeviceId==null || mDeviceId.equals("")) mDeviceId = "js_nj_00003";

        initWidgetResource();

        String root_url = AppConfig.FILE_SERVER_YBLIU_PATH + File.separator +"cert" + File.separator + mProjectId+"_"+ mDeviceId + ".jpg";
        mCertificatePhotoUrls.add(root_url);
        String cert_name =  mProjectId+"_"+ mDeviceId + ".jpg";
        mFileNameList.add(cert_name);

    }

    /*
     * 页面初始化
     */
    // 控件初始化
    private void initWidgetResource() {
        // 顶部导航栏
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        TextView titleText = (TextView) findViewById(R.id.toolbar_title);
        toolbar.setTitle("");
        titleText.setText(getString(R.string.history_info_title));
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true); //设置返回键可用


        /* 安监证书照片 */
        mCertificatePhotoGv = findViewById(R.id.certificate_photo_gv);
        mCertificatePhotoAdapter = new ImageGridAdapter(BasketCertActivity.this,
                R.layout.item_work_photo,mCertificatePhotoUrls);
        mCertificatePhotoGv.setAdapter(mCertificatePhotoAdapter);
        mCertificatePhotoGv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                getCertificateBitmaps();

                // 显示dislog
                ScaleImageView scaleImageView = new ScaleImageView(BasketCertActivity.this);
                scaleImageView.setUrls_and_Bitmaps(mFileNameList, mCertificatePhotos, position);
                scaleImageView.create();
            }
        });

    }

    private void getCertificateBitmaps(){
        mCertificatePhotos.clear();
        for(int i=0; i < mCertificatePhotoUrls.size(); i++){
            String url = mCertificatePhotoUrls.get(i);
            mCertificatePhotos.add(null);
            mCertificatePhotos.set(i, WebImage.webImageCache.get(url));
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
