package com.example.zzx.zbar_demo.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.example.zzx.zbar_demo.Adapter.ProjectAdapter;
import com.example.zzx.zbar_demo.PdfRead.PDFStartActivity;
import com.example.zzx.zbar_demo.R;
import com.example.zzx.zbar_demo.Util.HttpUtil;
import com.example.zzx.zbar_demo.activity.loginRegist.LoginActivity;
import com.example.zzx.zbar_demo.entity.ProjectInfo;
import com.google.gson.Gson;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Response;

public class ProDetailActivity extends AppCompatActivity {

    private ProjectInfo projectInfo;
    private String mProjectId;

    private TextView txtProName;
    private TextView txtProNumber;
    private TextView txtProState;
    private LinearLayout llChangeProState;
    private LinearLayout llProContract;
    private TextView txtProStart;
    private TextView txtProEnd;
    private TextView txtProRent;
    private TextView txtProArea;
    private TextView txtProBuilder;
    private TextView txtProAreaUser;

    public SharedPreferences pref;
    private String token;

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    projectInfo = JSON.parseObject(String.valueOf(msg.obj), ProjectInfo.class);
                    if(projectInfo!=null){
                        txtProName.setText("项目名称:"+projectInfo.getProjectName());
                        txtProNumber.setText("No:"+projectInfo.getProjectId());
                        txtProState.setText(projectInfo.getProjectState());
                        txtProStart.setText(projectInfo.getProjectStart());
                        txtProEnd.setText(projectInfo.getProjectEnd());
                        txtProRent.setText(projectInfo.getAdminRentId());
                        txtProArea.setText(projectInfo.getAdminAreaId());
                        txtProBuilder.setText(projectInfo.getProjectBuilders());
                    }
                    break;
                case 1:
                    Toast.makeText(ProDetailActivity.this, "没有权限访问！", Toast.LENGTH_LONG).show();
                    break;
                default:
                    break;
            }
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pro_detail);

        // 顶部导航栏
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        TextView titleText = (TextView) findViewById(R.id.toolbar_title);
        toolbar.setTitle("");
        titleText.setText(getString(R.string.projectDetail_tile));
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true); //设置返回键可用

        txtProName = findViewById(R.id.txt_pro_name);
        txtProNumber =  findViewById(R.id.txt_pro_number);
        txtProState =  findViewById(R.id.txt_pro_state);
        llChangeProState = findViewById(R.id.ll_change_pro_state);
        llProContract = findViewById(R.id.ll_pro_contract);
        txtProStart = findViewById(R.id.txt_pro_start_time);
        txtProEnd = findViewById(R.id.txt_pro_end_time);
        txtProRent = findViewById(R.id.txt_pro_area_admin);
        txtProArea = findViewById(R.id.txt_pro_rent_admin);
        txtProBuilder = findViewById(R.id.txt_pro_builder);
        txtProAreaUser = findViewById(R.id.txt_pro_area_user);

        pref = PreferenceManager.getDefaultSharedPreferences(this);
        token = pref.getString("loginToken", "");
        if (token == null) {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
        }

        Intent intent = getIntent();
        mProjectId = intent.getStringExtra("projectId");

        initProState( );


        //项目状态更改跳转
        llChangeProState.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        //项目合同书界面跳转
        llProContract.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ProDetailActivity.this,PDFStartActivity.class);
                startActivity(intent);
            }
        });


    }

    private void initProState( ) {
        HttpUtil.getProjectDetailInfoOkHttpRequest(new okhttp3.Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                //异常情况处理
                Looper.prepare();
                Toast.makeText(ProDetailActivity.this, "网络连接失败！", Toast.LENGTH_LONG).show();
                Looper.loop();
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                // 返回服务器数据
                String responseData = response.body().string();
                try {
                    JSONObject jsonObject = JSON.parseObject(responseData);
                    String isAllowed = jsonObject.getString("isAllowed");
                    Message msg = new Message();
                    if(isAllowed.equals("true")){
                        msg.obj = jsonObject.get("projectDetail");
                        msg.what = 0;
                    } else{
                        msg.what = 1;
                    }
                    handler.sendMessage(msg);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        },token,mProjectId);
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
