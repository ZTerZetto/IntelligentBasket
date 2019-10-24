package com.automation.zzx.intelligent_basket_demo.activity.basket;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.TextView;

import com.automation.zzx.intelligent_basket_demo.R;

public class BasketDeviceActivity extends AppCompatActivity {

    // 吊篮相关
    private String mDeviceId; // 吊篮ID

    // 控件
    private TextView deviceNum1;
    private TextView deviceNum2;
    private TextView deviceNum3;
    private TextView deviceNum4;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_basket_device);

        initWidgetResource();  // 初始化控件

        Intent intent = getIntent();
        mDeviceId = intent.getStringExtra(BasketDetailActivity.BASKET_ID);  // 获取吊篮ID
        if(mDeviceId==null || mDeviceId.equals("")) mDeviceId = "1";

    }

    private void initWidgetResource(){
        // 顶部导航栏
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        TextView titleText = (TextView) findViewById(R.id.toolbar_title);
        toolbar.setTitle("");
        titleText.setText(getString(R.string.device_activity_title));
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true); //设置返回键可用

        //获取资源控件
        deviceNum1 = findViewById(R.id.device_number_1);
        deviceNum2  = findViewById(R.id.device_number_2);
        deviceNum3  = findViewById(R.id.device_number_3);
        deviceNum4  = findViewById(R.id.device_number_4);

        deviceNum1.setText("未绑定");
        deviceNum2.setText("未绑定");
        deviceNum3.setText("未绑定");
        deviceNum4.setText("未绑定");

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
