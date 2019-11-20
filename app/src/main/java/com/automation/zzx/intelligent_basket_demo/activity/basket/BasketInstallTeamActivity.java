package com.automation.zzx.intelligent_basket_demo.activity.basket;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ListView;
import android.support.v7.widget.Toolbar;
import android.widget.TextView;

import com.automation.zzx.intelligent_basket_demo.R;

public class BasketInstallTeamActivity extends AppCompatActivity {

    // Intent相关
    private String mDeviceId; // 吊篮ID
    private String mProjectId; // 项目ID

    // 控件
    private ListView lvInstallTeam;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_basket_install_team);

        initWidgetResource();  // 初始化控件
        initData();

    }

    private void initWidgetResource(){
        // 顶部导航栏
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        TextView titleText = (TextView) findViewById(R.id.toolbar_title);
        toolbar.setTitle("");
        titleText.setText("安装队伍");
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true); //设置返回键可用

        //获取资源控件
        lvInstallTeam = findViewById(R.id.install_team_listview);
    }

    private void initData(){
        Intent intent = getIntent();
        mDeviceId = intent.getStringExtra(BasketDetailActivity.BASKET_ID);  // 获取吊篮ID
        mProjectId = intent.getStringExtra(BasketDetailActivity.UPLOAD_PROJECT_ID);  // 获取项目ID
        if(mDeviceId==null || mDeviceId.equals("")) mDeviceId = "1";
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
