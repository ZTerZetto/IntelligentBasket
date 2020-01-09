package com.automation.zzx.intelligent_basket_demo.activity.worker;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.automation.zzx.intelligent_basket_demo.R;
import com.automation.zzx.intelligent_basket_demo.activity.common.PersonalInformationActivity;
import com.automation.zzx.intelligent_basket_demo.activity.common.QRcodeCardActivity;
import com.automation.zzx.intelligent_basket_demo.activity.common.UploadImageFTPActivity;

public class WorkerMoreActivity extends AppCompatActivity implements View.OnClickListener{

    private final static String TAG = "WorkerMoreActivity";

    // Intent 消息
    public final static String UPLOAD_IMAGE_TYPE = "uploadImageType";
    public final static String UPLOAD_WORKER_CAPACITY_IMAGE = "userCapacityImage"; // 施工人员技能
    public final static String WORKER_ID = "worker_id";

    // 控件
    private RelativeLayout mWorkerUploadCapacityImageRv;
    private RelativeLayout mCheckHomePageRv;

    // 个人信息
    private String mWorkerId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_worker_more);

        mWorkerId = getIntent().getStringExtra("worker_id");
        initWidgets();
    }

    /*
     * 活动初始化
     */
    // 控件初始化
    private void initWidgets(){
        // 顶部导航栏
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        TextView titleText = (TextView) findViewById(R.id.toolbar_title);
        toolbar.setTitle("更多");
        titleText.setText("");
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true); //设置返回键可用

        // 页面控件
        mWorkerUploadCapacityImageRv = (RelativeLayout) findViewById(R.id.upload_capacity_layout);
        mWorkerUploadCapacityImageRv.setOnClickListener(this);
        mCheckHomePageRv = (RelativeLayout) findViewById(R.id.check_homepage_layout);
        mCheckHomePageRv.setOnClickListener(this);
    }

    /*
     * 消息监听
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

    // 控件点击响应
    @Override
    public void onClick(View v) {
        Intent intent;
        switch(v.getId()){
            case R.id.upload_capacity_layout:
                // 点击安监证书
                Log.i(TAG, "You have clicked the upload_capacity button");
               /* intent = new Intent(WorkerMoreActivity.this, UploadImageFTPActivity.class);
                intent.putExtra(WORKER_ID, mWorkerId);
                intent.putExtra(UPLOAD_IMAGE_TYPE, UPLOAD_WORKER_CAPACITY_IMAGE);
                startActivity(intent);*/
                intent = new Intent(WorkerMoreActivity.this, SkillEditActivity.class);
                startActivity(intent);
                break;
            case R.id.check_homepage_layout:
                intent = new Intent(WorkerMoreActivity.this, WorkerHomePageActivity.class);
                intent.putExtra("worker_id", mWorkerId);
                startActivity(intent);
                break;
        }
    }
}
