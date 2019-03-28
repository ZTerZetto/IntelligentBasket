package com.automation.zzx.intelligent_basket_demo.activity.basket;

import android.annotation.SuppressLint;
import android.content.Context;
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
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.automation.zzx.intelligent_basket_demo.adapter.BasketAdapter;
import com.automation.zzx.intelligent_basket_demo.R;
import com.automation.zzx.intelligent_basket_demo.activity.loginRegist.LoginActivity;
import com.automation.zzx.intelligent_basket_demo.entity.BasketInfo;
import com.automation.zzx.intelligent_basket_demo.utils.HttpUtil;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Response;

public class BasketListActivity extends AppCompatActivity {

    private ListView mLv;
    private BasketAdapter adapter;
    //private List<BasketInfo> basketInfoArrayList = new ArrayList<>();
    private List<BasketInfo> basketInfoArrayList;
    private Context mContext = BasketListActivity.this;

    private TextView edtSearch;
    private Button btnSearch;
    private TextView txtResult;
    private CheckBox checkBox;
    private LinearLayout llChoose;
    private Button btnBatch;
    private Button btnAll;
    private String mProjectId;
    public SharedPreferences pref;
    private String token;



    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    basketInfoArrayList = new ArrayList<>();
                    String basketListString = String.valueOf(msg.obj);
                    String[] basket = basketListString.split("[,]");
                    for(int i = 0;i<basket.length;i++){
                        BasketInfo mBasketInfo = new BasketInfo(basket[i],"RESTING","x");
                        basketInfoArrayList.add(mBasketInfo);
                    }

                    if(basketInfoArrayList!=null){
                        adapter = new BasketAdapter(mContext,R.layout.item_basket,basketInfoArrayList);
                        mLv.setAdapter(adapter);
                        mLv.setVisibility(View.VISIBLE);
                        txtResult.setVisibility(View.GONE);
                    }
                    break;
                case 1:
                    Toast.makeText(BasketListActivity.this, "没有权限访问！", Toast.LENGTH_LONG).show();
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
        setContentView(R.layout.activity_basket_list);

        // 顶部导航栏
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        TextView titleText = (TextView) findViewById(R.id.toolbar_title);
        toolbar.setTitle("");
        titleText.setText(getString(R.string.basketList_title));
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true); //设置返回键可用

        mLv = findViewById(R.id.list_view);
        edtSearch = findViewById(R.id.edit_input_search);
        btnSearch = findViewById(R.id.search_button);
        txtResult = findViewById(R.id.txt_search_result);
        llChoose = findViewById(R.id.ll_choose);
        btnBatch = findViewById(R.id.btn_choose_batch);
        btnAll = findViewById(R.id.btn_choose_all);

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
                String text = edtSearch.getText().toString();
                if (text.isEmpty()) {
                    showList(basketInfoArrayList);
                } else {
                    List<BasketInfo> arrayList = new ArrayList<>();
                    for (int i = 0; i < basketInfoArrayList.size(); i++) {
                        BasketInfo basketInfo = basketInfoArrayList.get(i);
                        if (basketInfo.getBasketId().equals(text)) {
                            arrayList.add(basketInfo);
                        }
                    }
                    if (arrayList.isEmpty()) {
                        mLv.setVisibility(View.GONE);
                        txtResult.setVisibility(View.VISIBLE);
                    } else {
                        basketInfoArrayList.clear();
                        basketInfoArrayList = arrayList;
                        showList(basketInfoArrayList);
                    }
                }
            }
        });

        btnBatch.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {



            }
        });

        //listview点击事件
        mLv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                //跳转至缩略图显示
                Intent intent = new Intent(mContext, BasketDetailActivity.class);
                //intent.putExtra("basket_id", basketInfoArrayList.get(i).getBasketId());
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

    private void showList(List<BasketInfo> arrayList) {
        adapter = new BasketAdapter(mContext,R.layout.item_basket,arrayList);
        mLv.setAdapter(adapter);
        mLv.setVisibility(View.VISIBLE);
        txtResult.setVisibility(View.GONE);
    }

    private void initList() {
        HttpUtil.getBasketListOkHttpRequest(new okhttp3.Callback() {

            @Override
            public void onFailure(Call call, IOException e) {
                //异常情况处理
                Looper.prepare();
                Toast.makeText(BasketListActivity.this, "网络连接失败！", Toast.LENGTH_LONG).show();
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
                    if (isAllowed.equals("true")) {
                        msg.obj = jsonObject.get("basketList");
                        msg.what = 0;
                    } else {
                        msg.what = 1;
                    }
                    handler.sendMessage(msg);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, token, mProjectId);
    }
}
