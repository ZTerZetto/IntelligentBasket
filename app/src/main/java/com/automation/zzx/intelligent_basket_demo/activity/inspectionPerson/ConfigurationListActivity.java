package com.automation.zzx.intelligent_basket_demo.activity.inspectionPerson;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.automation.zzx.intelligent_basket_demo.R;
import com.automation.zzx.intelligent_basket_demo.activity.loginRegist.LoginActivity;
import com.automation.zzx.intelligent_basket_demo.adapter.inspectPerson.ConfigurationItemAdapter;
import com.automation.zzx.intelligent_basket_demo.entity.AppConfig;
import com.automation.zzx.intelligent_basket_demo.entity.ConfigurationItem;
import com.automation.zzx.intelligent_basket_demo.entity.ProjectInfo;
import com.automation.zzx.intelligent_basket_demo.utils.ToastUtil;
import com.automation.zzx.intelligent_basket_demo.utils.okhttp.BaseCallBack;
import com.automation.zzx.intelligent_basket_demo.utils.okhttp.BaseOkHttpClient;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import okhttp3.Call;

/**
 * Created by pengchenghu on 2019/5/14.
 * Author Email: 15651851181@163.com
 * Describe: 巡检人员查看配置清单
 * limits:
 */

public class ConfigurationListActivity extends AppCompatActivity {

    private final static String TAG = "ConfigurationList";

    // handler消息
    private static final int UPDATE_CONFIGURATION_LIST = 101;

    // 控件
    private ListView mConfigurationList;
    private ConfigurationItemAdapter mConfigurationItemAdapter;
    private List<ConfigurationItem> mConfigurationItems = new ArrayList<>();

    // 项目信息
    private String mProjectId;

    // 人员信息
    private String mToken;
    private SharedPreferences mPref;

    // 异步消息
    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler(){
        public void handleMessage(Message msg){
            switch (msg.what){
                case UPDATE_CONFIGURATION_LIST:
                    mConfigurationItemAdapter.notifyDataSetChanged();
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configuration_list);

        Intent intent = getIntent();
        mProjectId = intent.getStringExtra(SearchProjectActivity.PROJECT_ID);  // 获取吊篮id
        if(mProjectId==null || mProjectId.equals("")){
            ToastUtil.showToastTips(ConfigurationListActivity.this, "无效的项目号！");
            finish();
        }
        getUserToken();


        initWidgets();
    }

    /*
     * 页面初始化
     */
    // 初始化控件
    private void initWidgets(){
        // 顶部导航栏
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        TextView titleText = (TextView) findViewById(R.id.toolbar_title);
        toolbar.setTitle("");
        titleText.setText("吊篮配置详情清单");
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true); //设置返回键可用

        // 列表
        mConfigurationList = (ListView) findViewById(R.id.configuration_list);
        mConfigurationItemAdapter = new ConfigurationItemAdapter(ConfigurationListActivity.this,
                R.layout.item_configuration_list, mConfigurationItems);  // 初始化适配器
        mConfigurationList.setAdapter(mConfigurationItemAdapter);
        getConfigurationList();
    }

    /*
     * 消息响应监听
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home: // 返回按钮
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /*
     * 后台通信
     */
    // 工人更新资质证书
    private void getConfigurationList(){
        BaseOkHttpClient.newBuilder()
                .addHeader("Authorization", mToken)
                .addParam("projectId", mProjectId)
                .get()
                .url(AppConfig.INSPECTION_PERSON_CHECK_CONFIGURATION_LIST)
                .build()
                .enqueue(new BaseCallBack() {
                    @Override
                    public void onSuccess(Object o) {
                        JSONObject jsonObject = JSON.parseObject(o.toString());
                        String success = jsonObject.getString("get");
                        if(success.equals("success")) {
                            Log.i(TAG, "获取配置清单成功" );
                            parseConfigurationList(jsonObject.getString("partsList"));
                        }else{
                            Log.i(TAG, "获取配置清单失败" );
                            ToastUtil.showToastTips(ConfigurationListActivity.this, "获取配置清单成功");
                        }
                    }

                    @Override
                    public void onError(int code) {
                        Log.i(TAG, "获取配置清单错误：" + code);
                        switch (code){
                            case 401: // 未授权
                                ToastUtil.showToastTips(ConfigurationListActivity.this,
                                        "登录已过期，请重新登陆");
                                startActivity(new Intent(ConfigurationListActivity.this,
                                        LoginActivity.class));
                                finish();
                                break;
                            case 403: // 禁止
                                break;
                            case 404: // 404
                                break;
                        }
                    }

                    @Override
                    public void onFailure(Call call, IOException e) {
                        Log.i(TAG, "更新资质证书失败：" + e.toString());

                    }
                });
    }

    // 解析配置清单
    private void parseConfigurationList(String data){
        mConfigurationItems.clear();
        JSONArray partsList = JSON.parseArray(data);  // 解析列表
        Iterator<Object> iterator = partsList.iterator();  // 迭代获取项目信息
        while(iterator.hasNext()) {
            JSONObject partsInfoJsonObject = (JSONObject) iterator.next();
            String name = partsInfoJsonObject.getString("part");
            String number = partsInfoJsonObject.getString("quantity");
            String unit = partsInfoJsonObject.getString("unit");
            ConfigurationItem configurationItem = new ConfigurationItem(name, number, unit);
            mConfigurationItems.add(configurationItem);
        }
        mHandler.sendEmptyMessage(UPDATE_CONFIGURATION_LIST);
    }


    /*
     * 解析用户信息
     */
    // 获取用户数据
    private void getUserToken(){
        // 从本地获取数据
        mPref = PreferenceManager.getDefaultSharedPreferences(this);
        mToken = mPref.getString("loginToken","");
    }
}
