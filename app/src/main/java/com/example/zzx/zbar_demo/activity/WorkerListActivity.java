package com.example.zzx.zbar_demo.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.example.zzx.zbar_demo.activity.basket.BasketListActivity;
import com.example.zzx.zbar_demo.adapter.WorkerAdapter;
import com.example.zzx.zbar_demo.R;
import com.example.zzx.zbar_demo.activity.loginRegist.LoginActivity;
import com.example.zzx.zbar_demo.entity.UserInfo;
import com.example.zzx.zbar_demo.utils.HttpUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Response;

public class WorkerListActivity extends AppCompatActivity {

    private ListView mLv;
    private WorkerAdapter adapter;
    private List<UserInfo> userInfoArrayList = new ArrayList<>();
    private Context mContext = WorkerListActivity.this;

    private TextView txtSearch;
    private Button btnSearch;
    private TextView txtResult;

    private String mProjectId;
    public SharedPreferences pref;
    private String token;



    @SuppressLint("HandlerLeak")
        private Handler handler = new Handler() {
            public void handleMessage(Message msg) {
                switch(msg.what) {
                    case 0:
                        String jso = msg.obj.toString();
                        userInfoArrayList = JSON.parseArray(jso, UserInfo.class);

                        if(null != userInfoArrayList) {
                            adapter = new WorkerAdapter(mContext,R.layout.item_worker,userInfoArrayList);
                            mLv.setAdapter(adapter);
                            mLv.setVisibility(View.VISIBLE);
                            txtResult.setVisibility(View.GONE);
                        }
                        break;
                    case 1:
                        Toast.makeText(WorkerListActivity.this, "没有权限访问！", Toast.LENGTH_LONG).show();
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
        setContentView(R.layout.activity_worker_list);

        // 顶部导航栏
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        TextView titleText = (TextView) findViewById(R.id.toolbar_title);
        toolbar.setTitle("");
        titleText.setText(getString(R.string.workerList_tile));
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true); //设置返回键可用

        mLv = findViewById(R.id.list_view);
        txtSearch = findViewById(R.id.txt_input_search);
        btnSearch = findViewById(R.id.search_button);
        txtResult = findViewById(R.id.txt_search_result);

        pref = PreferenceManager.getDefaultSharedPreferences(this);
        token = pref.getString("loginToken", "");
        if (token == null) {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
        }
        Intent intent = getIntent();
        mProjectId = intent.getStringExtra("projectId");

        initList();

        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text = txtSearch.getText().toString();
                if (text.isEmpty()) {
                    showList(userInfoArrayList);
                } else {
                    List<UserInfo> arrayList = new ArrayList<>();
                    for (int i = 0; i < userInfoArrayList.size(); i++) {
                        UserInfo userInfo = userInfoArrayList.get(i);
                        if (userInfo.getUserId().equals(text)) {
                            arrayList.add(userInfo);
                        } else if (userInfo.getUserName().equals(text)){
                            arrayList.add(userInfo);
                        }
                    }
                    if (arrayList.isEmpty()) {
                        mLv.setVisibility(View.GONE);
                        txtResult.setVisibility(View.VISIBLE);
                    } else {
                        userInfoArrayList.clear();
                        userInfoArrayList = arrayList;
                        showList(userInfoArrayList);
                    }
                }
            }
        });

        //listview点击事件
        mLv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                //跳转至缩略图显示
                //Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:"+userInfoArrayList.get(i).getUserPhone()));
                //Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:"+"15051860168"));
                //startActivity(intent);
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

    private void showList(List<UserInfo> arrayList) {
        adapter = new WorkerAdapter(mContext,R.layout.item_worker,arrayList);
        mLv.setAdapter(adapter);
        mLv.setVisibility(View.VISIBLE);
        txtResult.setVisibility(View.GONE);
    }

    private void initList() {

        HttpUtil.getWorkerListOkHttpRequest(new okhttp3.Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                //异常情况处理
                Looper.prepare();
                Toast.makeText(WorkerListActivity.this, "网络连接失败！", Toast.LENGTH_LONG).show();
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
                        msg.obj = jsonObject.get("userList");
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

}
