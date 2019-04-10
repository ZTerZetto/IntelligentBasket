package com.automation.zzx.intelligent_basket_demo.activity.oldversion;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.automation.zzx.intelligent_basket_demo.activity.areaAdmin.ProDetailActivity;
import com.automation.zzx.intelligent_basket_demo.adapter.ProjectAdapter;
import com.automation.zzx.intelligent_basket_demo.R;
import com.automation.zzx.intelligent_basket_demo.utils.HttpUtil;
import com.automation.zzx.intelligent_basket_demo.activity.loginRegist.LoginActivity;
import com.automation.zzx.intelligent_basket_demo.entity.ProjectInfo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Response;

public class ProListActivity extends AppCompatActivity {

    private ListView mLv;
    private ProjectAdapter adapter;
    private List<ProjectInfo> projectArrayList = new ArrayList<>();
    private Context mContext = ProListActivity.this;
    public SharedPreferences pref;
    private String token;


    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    projectArrayList = JSON.parseArray(String.valueOf(msg.obj), ProjectInfo.class);
                    if (null != projectArrayList) {
                        adapter = new ProjectAdapter(mContext, R.layout.item_basket, projectArrayList);
                        mLv.setAdapter(adapter);
                        mLv.setVisibility(View.VISIBLE);
                        showList(projectArrayList);
                    }
                    break;
                case 1:
                    Toast.makeText(ProListActivity.this, "没有权限访问！", Toast.LENGTH_LONG).show();
                    finish();
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_project_list);

        // 顶部导航栏
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        TextView titleText = (TextView) findViewById(R.id.toolbar_title);
        toolbar.setTitle("");
        titleText.setText(getString(R.string.projectList_title));
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true); //设置返回键可用

        mLv = findViewById(R.id.list_view);
        pref = PreferenceManager.getDefaultSharedPreferences(this);
        token = pref.getString("loginToken", "");
        if (token == null) {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
        }
        initList();

        //listview点击事件
        mLv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(ProListActivity.this, ProDetailActivity.class);
                //intent.putExtra("projectId",projectArrayList.get(i).getProjectId());
                intent.putExtra("projectDetail",projectArrayList.get(i));
                startActivity(intent);
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

    private void showList(List<ProjectInfo> arrayList) {
        adapter = new ProjectAdapter(mContext,R.layout.item_basket,arrayList);
        mLv.setAdapter(adapter);
        mLv.setVisibility(View.VISIBLE);
    }

    private void initList() {
        HttpUtil.getProjectInfoOkHttpRequest(new okhttp3.Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                //异常情况处理
                Looper.prepare();
                Toast.makeText(ProListActivity.this, "网络连接失败！", Toast.LENGTH_LONG).show();
                Looper.loop();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                // 返回服务器数据
                String responseData = response.body().string();
                try {
                    JSONObject jsonObject = JSON.parseObject(responseData);
                    //String isAllowed = jsonObject.getString("isAllowed");
                    Message msg = new Message();
                   // if(isAllowed.equals("true")){
                        msg.obj = jsonObject.get("projectList");
                        msg.what = 0;
                    /*} else {
                        msg.what = 1;
                    }*/
                    handler.sendMessage(msg);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        },token,"1");
    }
}
