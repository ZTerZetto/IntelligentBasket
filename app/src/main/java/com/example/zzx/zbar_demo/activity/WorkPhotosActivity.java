package com.example.zzx.zbar_demo.activity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.TextView;

import com.example.zzx.zbar_demo.R;
import com.example.zzx.zbar_demo.Adapter.WorkPhotoAdapter;
import com.example.zzx.zbar_demo.widget.ScaleImageView;
import com.example.zzx.zbar_demo.widget.image.WebImage;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by pengchenghu on 2019/2/23.
 * Author Email: 15651851181@163.com
 * Describe: 吊篮运行截图监视活动
 * limits:
 */

public class WorkPhotosActivity extends AppCompatActivity {

    // 控件声明
    private GridView mWorkPhotoGv;  // 网格布局

    // work photo gridview
    private boolean mIsReload;  // 是否重新加载位图
    private List<String> mWorkPtotoUrls;
    private List<Bitmap> mWorkPhotos;
    private WorkPhotoAdapter mWorkPhotoAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_work_photos);

        initWidgetResource();
    }

    // 控件初始化
    private void initWidgetResource() {
        // 顶部导航栏
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        TextView titleText = (TextView) findViewById(R.id.toolbar_title);
        toolbar.setTitle("");
        titleText.setText(getString(R.string.work_photo_title));
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true); //设置返回键可用

        // work photo gridview
        mIsReload = true;
        mWorkPhotoGv = (GridView) findViewById(R.id.work_photo_gv);
        initWorkPhotoUrls();  // 初始化吊篮图片
        mWorkPhotoAdapter = new WorkPhotoAdapter(WorkPhotosActivity.this,
                R.layout.item_work_photo, mWorkPtotoUrls);
        mWorkPhotoGv.setAdapter(mWorkPhotoAdapter);
        mWorkPhotoGv.setOnItemClickListener(new AdapterView.OnItemClickListener() {  // 点击图片响应
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(mIsReload){
                    getBitmaps();  // 重新加载位图
                    mIsReload = false;
                }

                // 显示dislog
                Bitmap bm = BitmapFactory.decodeResource(getResources(), R.mipmap.test);
                ScaleImageView scaleImageView = new ScaleImageView(WorkPhotosActivity.this);
                scaleImageView.setUrls_and_Bitmaps(mWorkPtotoUrls, mWorkPhotos, position);
                scaleImageView.create();
            }
        });

        //

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
       * 其他函数
     */
    // 初始化图片地址
    private void initWorkPhotoUrls(){
        mWorkPtotoUrls = new ArrayList<>();

        String root_url = "http://10.193.0.20:8089/basket/001/";

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
        for(int i=0; i<mWorkPtotoUrls.size(); i++){
            String url = mWorkPtotoUrls.get(i);
            mWorkPhotos.add(null);
            mWorkPhotos.set(i, WebImage.webImageCache.get(url));
        }
    }

}
