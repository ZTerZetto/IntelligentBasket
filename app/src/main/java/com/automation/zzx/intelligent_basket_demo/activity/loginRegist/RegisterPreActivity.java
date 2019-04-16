package com.automation.zzx.intelligent_basket_demo.activity.loginRegist;

import android.app.Dialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.automation.zzx.intelligent_basket_demo.R;
import com.automation.zzx.intelligent_basket_demo.widget.dialog.CommonDialog;

public class RegisterPreActivity extends AppCompatActivity implements View.OnClickListener{

    private Button btnWorker;
    private Button btnRent;
    private Button btnRegion;
    private CommonDialog mCommonDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_pre);

        // 顶部导航栏
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        TextView titleText = (TextView) findViewById(R.id.toolbar_title);
        toolbar.setTitle("");
        titleText.setText(getString(R.string.registPre_title));
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true); //设置返回键可用

        btnWorker = findViewById(R.id.btn_regist_worker);
        btnRent = findViewById(R.id.btn_regist_rent_manager);
        btnRegion = findViewById(R.id.btn_regist_region_manager);

        btnWorker.setOnClickListener(this);
        btnRent.setOnClickListener(this);
        btnRegion.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        Intent intent;
        switch (v.getId()){
            case R.id.btn_regist_worker:
                intent = new Intent(this,RegistWorkerActivity.class);
                startActivityForResult(intent,1);
                finish();
                break;
            case R.id.btn_regist_rent_manager:
                intent = new Intent(this,RegistRentManActivity.class);
                startActivityForResult(intent,1);
                finish();
                break;
            case R.id.btn_regist_region_manager:
                intent = new Intent(this,RegistAreaManActivity.class);
                startActivityForResult(intent,1);
                finish();
                break;
                default: finish(); break;
        }
    }

    /*
     * 提示弹框
     */
    private CommonDialog initDialog(String mMsg) {
        return new CommonDialog(this, R.style.dialog, mMsg,
                new CommonDialog.OnCloseListener() {
                    @Override
                    public void onClick(Dialog dialog, boolean confirm) {
                        if (confirm) {
                            dialog.dismiss();
                        } else {
                            dialog.dismiss();
                        }
                    }
                }).setTitle("提示");
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
    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

}
