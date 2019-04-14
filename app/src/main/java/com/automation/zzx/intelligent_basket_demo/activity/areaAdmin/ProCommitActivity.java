package com.automation.zzx.intelligent_basket_demo.activity.areaAdmin;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.TextView;

import com.automation.zzx.intelligent_basket_demo.R;
import com.automation.zzx.intelligent_basket_demo.activity.loginRegist.LoginActivity;
import com.automation.zzx.intelligent_basket_demo.adapter.CommitPhotoAdapter;
import com.automation.zzx.intelligent_basket_demo.entity.AppConfig;
import com.automation.zzx.intelligent_basket_demo.entity.ProjectInfo;
import com.automation.zzx.intelligent_basket_demo.widget.ScaleImageView;
import com.automation.zzx.intelligent_basket_demo.widget.image.WebImage;

import java.util.ArrayList;
import java.util.List;

import static com.automation.zzx.intelligent_basket_demo.entity.AppConfig.FILE_SERVER_HPC_PATH;


public class ProCommitActivity extends AppCompatActivity {

    private ProjectInfo projectInfo;
    private String mProjectId;

    private TextView txtProName;
    private TextView txtProNumber;
    private EditText edtCommit;
    private GridView gvPhotoCommit;
    private Button btnCommit;

    public SharedPreferences pref;
    private String token;

    //photo gridview
    private boolean mIsReload;  // 是否重新加载位图
    private List<Bitmap> mWorkPhotos;
    private ArrayList<String> mPicList = new ArrayList<>();
    private CommitPhotoAdapter mCommitPhotoAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pro_commit);

        // 顶部导航栏
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        TextView titleText = (TextView) findViewById(R.id.toolbar_title);
        toolbar.setTitle("");
        titleText.setText(getString(R.string.proCommit_tile));
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true); //设置返回键可用

        txtProName = findViewById(R.id.txt_pro_name);
        txtProNumber = findViewById(R.id.txt_pro_number);
        edtCommit = findViewById(R.id.edt_commit);
        gvPhotoCommit = findViewById(R.id.gv_photo_commit);
        btnCommit = findViewById(R.id.btn_pro_commit);

        pref = PreferenceManager.getDefaultSharedPreferences(this);
        token = pref.getString("loginToken", "");
        if (token == null) {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
        }

        Intent intent = this.getIntent();
        projectInfo = (ProjectInfo)intent.getSerializableExtra("projectInfo");
        if(projectInfo!=null){
            txtProName.setText(projectInfo.getProjectName());
            txtProNumber.setText(projectInfo.getProjectId());
            initGridView();
        } else {
            finish();
        }

        btnCommit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO commit photos and discribe;
            }
        });

    }
    private void initGridView() {
        mPicList = new ArrayList<>();
        initWorkPhotoUrls();  // 初始化图片
        mCommitPhotoAdapter = new CommitPhotoAdapter(this, R.layout.item_pro_photo, mPicList);
        gvPhotoCommit.setAdapter(mCommitPhotoAdapter);
        gvPhotoCommit.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position == parent.getChildCount() - 1) {
                    //如果“增加按钮形状的”图片的位置是最后一张，且添加了的图片的数量不超过5张，才能点击
                    if (mPicList.size() == AppConfig.MAX_SELECT_PIC_NUM) {
                        //查看大图
                        viewPluImg(position);
                    } else {
                        //添加凭证图片
                        selectPic(AppConfig.MAX_SELECT_PIC_NUM - mPicList.size());
                    }
                } else {
                    //查看大图
                    viewPluImg(position);
                }
            }
        });
    }

    //查看大图
    private void viewPluImg(int position) {
        getBitmaps();
        ScaleImageView scaleImageView = new ScaleImageView(ProCommitActivity.this);
        scaleImageView.setUrls_and_Bitmaps(mPicList, mWorkPhotos, position);
        scaleImageView.create();
    }

    /**
     * 打开相册或者照相机选择凭证图片，最多5张
     *
     * @param maxTotal 最多选择的图片的数量
     */
    private void selectPic(int maxTotal) {
        //PictureSelectorConfig.initMultiConfig(this, maxTotal);
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

    // 初始化图片地址
    private void initWorkPhotoUrls(){
        String root_url = FILE_SERVER_HPC_PATH + "/basket/001/";
        mPicList.add(root_url + "201902251424.jpg");
        mPicList.add(root_url + "201902251425.jpg");
    }

    // 初始化图片位图:直接从缓存中获取
    private void getBitmaps(){
        mWorkPhotos = new ArrayList<>();
        for(int i=0; i<mPicList.size(); i++){
            String url = mPicList.get(i);
            mWorkPhotos.add(null);
            mWorkPhotos.set(i, WebImage.webImageCache.get(url));
        }
    }

}
