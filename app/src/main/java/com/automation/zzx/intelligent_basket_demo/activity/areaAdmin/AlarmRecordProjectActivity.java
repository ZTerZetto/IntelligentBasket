package com.automation.zzx.intelligent_basket_demo.activity.areaAdmin;

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
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.automation.zzx.intelligent_basket_demo.R;
import com.automation.zzx.intelligent_basket_demo.activity.loginRegist.LoginActivity;
import com.automation.zzx.intelligent_basket_demo.adapter.areaAdmin.ExpandableListviewAdapter;
import com.automation.zzx.intelligent_basket_demo.entity.AlarmInfo;
import com.automation.zzx.intelligent_basket_demo.entity.AppConfig;
import com.automation.zzx.intelligent_basket_demo.entity.UserInfo;
import com.automation.zzx.intelligent_basket_demo.utils.ToastUtil;
import com.automation.zzx.intelligent_basket_demo.utils.okhttp.BaseCallBack;
import com.automation.zzx.intelligent_basket_demo.utils.okhttp.BaseOkHttpClient;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import okhttp3.Call;

import static android.content.ContentValues.TAG;

public class AlarmRecordProjectActivity extends AppCompatActivity {

    // intent 消息参数
    public final static String PROJECT_ID = "projectId";  // 项目Id

    // Handler消息
    private final static int MG_ALARM_LIST_INFO = 1;


    private ExpandableListView expandListView;
    private List<AlarmInfo> alarmInfoList;
    private List<String> mBasketList;
    private List<List<String>> mRecordList;
    private ExpandableListviewAdapter adapter;

    // 空空如也
    private RelativeLayout noRepairListRelativeLayout;
    private TextView noRepairListTextView;

    // 用户登录信息相关
    private UserInfo mUserInfo;
    private String mToken;
    private SharedPreferences mPref;

    // 项目信息
    private String mProjectId;

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            switch(msg.what) {
                case MG_ALARM_LIST_INFO: // 获取报警记录
                    mBasketList.clear();
                    mRecordList.clear();
                    alarmInfoList.clear();
                    parseAlarmListInfo((String) msg.obj);
                    adapter.refresh(mBasketList,mRecordList);
                    updateContentView();
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm_record_project);

        // 顶部导航栏
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        TextView titleText = (TextView) findViewById(R.id.toolbar_title);
        toolbar.setTitle("报警记录");
        titleText.setText("");
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true); //设置返回键可用

        mBasketList = new ArrayList<>();
        mRecordList = new ArrayList<>();
        alarmInfoList = new ArrayList<>();

        //获取用户数据
        getUserInfo();
        //获取记录数据
        getAlarmInfoListByProjectId();
        //初始化控件
        initView();

    }

    // 获取用户数据
    private void getUserInfo(){
        // 从本地获取数据
        mPref = PreferenceManager.getDefaultSharedPreferences(this);
        mUserInfo = new UserInfo();
        mUserInfo.setUserId(mPref.getString("userId", ""));
        mUserInfo.setUserPhone(mPref.getString("userPhone", ""));
        mUserInfo.setUserRole(mPref.getString("userRole", ""));
        mToken = mPref.getString("loginToken","");

        //获取当前项目ID
        Intent intent = getIntent();
        mProjectId = intent.getStringExtra(PROJECT_ID);

    }

    private void initView() {

        // 空空如也
        noRepairListRelativeLayout = (RelativeLayout) findViewById(R.id.basket_no_avaliable);
        noRepairListTextView = (TextView)findViewById(R.id.no_basket_hint);

        expandListView = findViewById(R.id.expand_list_id);
        adapter=new ExpandableListviewAdapter(this,mBasketList,mRecordList);
        expandListView.setAdapter(adapter);

        //关闭数组某个数组，可以通过该属性来实现全部展开和只展开一个列表功能
        //expand_list_id.collapseGroup(0);
        expandListView.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView expandableListView, View view, int groupPosition, long l) {
                Toast.makeText(AlarmRecordProjectActivity.this, "点击"+mBasketList.get(groupPosition), Toast.LENGTH_SHORT).show();
                return false;
            }
        });

        //子视图的点击事件
        expandListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView expandableListView, View view, int groupPosition, int childPosition, long l) {
                Toast.makeText(AlarmRecordProjectActivity.this, "点击"+mRecordList.get(groupPosition).get(childPosition), Toast.LENGTH_SHORT).show();
                return true;
            }
        });

        //用于当组项折叠时的通知。
        expandListView.setOnGroupCollapseListener(new ExpandableListView.OnGroupCollapseListener() {
            @Override
            public void onGroupCollapse(int groupPosition) {

            }
        });


        //用于当组项展开时的通知。
        expandListView.setOnGroupExpandListener(new ExpandableListView.OnGroupExpandListener() {
            @Override
            public void onGroupExpand(int groupPosition) {

            }
        });
    }

    /*
     * 网络请求相关
     */
    // 从后台获取报警记录
    public void getAlarmInfoListByProjectId() {
        BaseOkHttpClient.newBuilder()
                .addHeader("Authorization",mToken)
                .addParam("type",2) //获取方式 1：按照吊篮ID获取 2：按照项目ID获取
                .addParam("value",mProjectId)
                .get()
                .url(AppConfig.GET_ALARM_INFO)
                .build()
                .enqueue(new BaseCallBack() {
                    @Override
                    public void onSuccess(Object o) {
                        Log.i(TAG, "成功" );
                        String responseData = o.toString();
                        Message message = new Message();
                        message.what = MG_ALARM_LIST_INFO;
                        message.obj = responseData;
                        handler.sendMessage(message);
                    }

                    @Override
                    public void onError(int code) {
                        Log.i(TAG, "错误：" + code);
                        switch (code){
                            case 401: // 未授权
                                ToastUtil.showToastTips(AlarmRecordProjectActivity.this, "登录已过期，请重新登陆");
                                startActivity(new Intent(AlarmRecordProjectActivity.this, LoginActivity.class));
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
                        Log.i(TAG, "失败：" + e.toString());
                    }
                });
    }

    //解析报警记录信息
    private void parseAlarmListInfo(String responseDate){
        JSONObject jsonObject = JSON.parseObject(responseDate);
        String alarmInfo =jsonObject.getString("alarmInfo");

        JSONArray jsonArray = JSON.parseArray(alarmInfo);
        if(jsonArray.size() >= 1){
            Iterator<Object> iterator = jsonArray.iterator(); //迭代获取项目信息
            while(iterator.hasNext()) {
                JSONObject repairObj = (JSONObject) iterator.next();
                //时间字符串处理
                String timeDate = repairObj.getString("time").substring(0,10);
                String timeHM = repairObj.getString("time").substring(11,19);
                String time = timeDate + " " + timeHM;
                AlarmInfo mAlarmInfo = new AlarmInfo(repairObj.getString("device_id"),repairObj.getString("alarm_type"),
                        repairObj.getString("id"),time,repairObj.getString("alarm_detail"));
                alarmInfoList.add(mAlarmInfo);
            }
        }

        Map<String , String> map1 = new HashMap<>();
        Map<String , String> map2 = new HashMap<>();
        for(int i = 0; i < alarmInfoList.size();i++){
            map1.put(alarmInfoList.get(i).getDevice_id(),alarmInfoList.get(i).getTime()+"  "+alarmInfoList.get(i).getAlarm_detail());
            map2.put(alarmInfoList.get(i).getTime()+"  "+alarmInfoList.get(i).getAlarm_detail(),alarmInfoList.get(i).getDevice_id());
        }

        mBasketList =  new ArrayList<>(map1.keySet());

        for(int j=0;j<mBasketList.size();j++) {
            List<Object> objects = getKey(map2,mBasketList.get(j));
            List<String> mList = new ArrayList<>();
            for(int k=0;k<objects.size();k++)
                mList.add(String.valueOf(objects.get(k)));
            mRecordList.add(mList);
        }
    }


    /*
     * UI 更新相关
     */
    private void updateContentView() {
        if (mBasketList.size() < 1) { // 暂无吊篮有报警记录
            expandListView.setVisibility(View.GONE);
            noRepairListRelativeLayout.setVisibility(View.VISIBLE);
            noRepairListTextView.setText("暂无报警记录！");
        } else {  // 好多吊篮
            noRepairListRelativeLayout.setVisibility(View.GONE);
            expandListView.setVisibility(View.VISIBLE);
        }
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


    //功能函数
    public static List<Object> getKey(Map map, Object value){
        List<Object> keyList = new ArrayList<>();
        for(Object key: map.keySet()){
            if(map.get(key).equals(value)){
                keyList.add(key);
            }
        }
        return keyList;
    }

}
