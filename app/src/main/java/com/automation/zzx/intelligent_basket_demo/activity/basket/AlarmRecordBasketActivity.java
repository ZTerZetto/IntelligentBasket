package com.automation.zzx.intelligent_basket_demo.activity.basket;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.automation.zzx.intelligent_basket_demo.R;
import com.automation.zzx.intelligent_basket_demo.activity.loginRegist.LoginActivity;
import com.automation.zzx.intelligent_basket_demo.adapter.basket.AlarmRecordAdapter;
import com.automation.zzx.intelligent_basket_demo.entity.AlarmInfo;
import com.automation.zzx.intelligent_basket_demo.entity.AppConfig;
import com.automation.zzx.intelligent_basket_demo.entity.UserInfo;
import com.automation.zzx.intelligent_basket_demo.utils.ToastUtil;
import com.automation.zzx.intelligent_basket_demo.utils.okhttp.BaseCallBack;
import com.automation.zzx.intelligent_basket_demo.utils.okhttp.BaseOkHttpClient;
import com.automation.zzx.intelligent_basket_demo.widget.dialog.CommonDialog;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import okhttp3.Call;

import static android.content.ContentValues.TAG;

public class AlarmRecordBasketActivity extends AppCompatActivity implements View.OnClickListener {

    // intent 消息参数
    public final static String BASKET_ID = "basket_id";  // 项目Id
    // Handler消息
    private final static int MG_ALARM_LIST_INFO = 1;
    private final static int UPDATE_LIST_INFO = 2;

    // 主体
    //搜索框控件
    private SearchView mSearchView;
    private AutoCompleteTextView mAutoCompleteTextView;//搜索输入框
    private ImageView mDeleteButton;//搜索框中的删除按钮

    private List<AlarmInfo> alarmInfoList = new ArrayList<>();
    private List<AlarmInfo> showAlarmInfoList = new ArrayList<>();
    private AlarmRecordAdapter adapter;

    private LinearLayout llRecordSum;
    private TextView tvRecordSum;
    private ListView lvStop;

    private CommonDialog mCommonDialog; //报警详情弹窗

    // 空空如也
    private RelativeLayout noRecordListRelativeLayout;
    private TextView noRecordListTextView;

    // 用户登录信息相关
    private UserInfo mUserInfo;
    private String mToken;
    private SharedPreferences mPref;

    // 吊篮信息
    private String mBasketId;

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            switch(msg.what) {
                case MG_ALARM_LIST_INFO: // 获取报停记录
                    alarmInfoList.clear();
                    showAlarmInfoList.clear();
                    parseAlarmListInfo((String) msg.obj);
                    adapter.notifyDataSetChanged();
                    updateContentView();
                    break;
                case UPDATE_LIST_INFO:
                    adapter.notifyDataSetChanged();
                    updateContentView(); // 更新项目信息及控件显示
                default:
                    break;
            }
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm_record_basket);
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
        mBasketId = intent.getStringExtra(BASKET_ID);

    }


    private void initView() {

        // 顶部导航栏
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        TextView titleText = (TextView) findViewById(R.id.toolbar_title);
        toolbar.setTitle("吊篮报警记录");
        titleText.setText("");
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true); //设置返回键可用

        //搜索框
        mSearchView=findViewById(R.id.view_search);
        mAutoCompleteTextView=mSearchView.findViewById(R.id.search_src_text);
        mDeleteButton=mSearchView.findViewById(R.id.search_close_btn);
        mDeleteButton.setOnClickListener(this);
        mSearchView.setIconifiedByDefault(false);//设置搜索图标是否显示在搜索框内
        mAutoCompleteTextView.clearFocus(); //默认失去焦点
        mSearchView.setImeOptions(3);//设置输入法搜索选项字段，1:回车2:前往3:搜索4:发送5:下一項6:完成
//      mSearchView.setInputType(1);//设置输入类型
//      mSearchView.setMaxWidth(200);//设置最大宽度
        mSearchView.setQueryHint("输入描述详情");//设置查询提示字符串
        mSearchView.setSubmitButtonEnabled(true);//设置是否显示搜索框展开时的提交按钮
        mAutoCompleteTextView.setTextColor(Color.GRAY);
        //设置SearchView下划线透明
        setUnderLinetransparent(mSearchView);
        setListener();


        // 空空如也
        noRecordListRelativeLayout = (RelativeLayout) findViewById(R.id.record_no_avaliable);
        noRecordListTextView = (TextView)findViewById(R.id.no_record_hint);

        llRecordSum = findViewById(R.id.ll_record_sum);
        tvRecordSum = findViewById(R.id.tv_record_sum);
        lvStop = findViewById(R.id.record_list_id);

        adapter=new AlarmRecordAdapter(this,R.layout.item_basket_alarm_record,alarmInfoList);
        lvStop.setAdapter(adapter);
        lvStop.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String basketId = alarmInfoList.get(position).getDevice_id();
                String locateId = alarmInfoList.get(position).getId();
                String worker = alarmInfoList.get(position).getWorkerName();
                String workerId = alarmInfoList.get(position).getWorkerId();
                String time = alarmInfoList.get(position).getTime();
                String detail = alarmInfoList.get(position).getAlarm_detail();
                mCommonDialog = initDialog("吊篮编号:"+basketId+" / 现场编号:"+locateId
                        +"\n工人:"+worker+"("+workerId+")\n"+detail+'\n'+time);
                mCommonDialog.show();
            }
        });
    }

    private void setListener(){

        // 设置搜索文本监听
        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            //当点击搜索按钮时触发该方法
            @Override
            public boolean onQueryTextSubmit(String query) {
                showAlarmInfoList.clear();
                if (query == null  || query.equals("")) {
                    showAlarmInfoList.addAll(alarmInfoList);
                }else{
                    for (int i = 0; i < alarmInfoList.size(); i++) {
                        AlarmInfo alarmInfo = alarmInfoList.get(i);
                        if (alarmInfo.getDevice_id().contains(query)) {
                            showAlarmInfoList.add(alarmInfo);
                        } else if (alarmInfo.getAlarm_detail().contains(query)) {
                            showAlarmInfoList.add(alarmInfo);
                        }
                    }
                }
                handler.sendEmptyMessage(UPDATE_LIST_INFO);
                return true;
            }

            //当搜索内容改变时触发该方法
            @Override
            public boolean onQueryTextChange(String newText) {
                showAlarmInfoList.clear();
                if (newText == null || newText.equals("")) {
                    showAlarmInfoList.addAll(alarmInfoList);
                }else{
                    for (int i = 0; i < alarmInfoList.size(); i++) {
                        AlarmInfo alarmInfo = alarmInfoList.get(i);
                        if (alarmInfo.getDevice_id().contains(newText)) {
                            showAlarmInfoList.add(alarmInfo);
                        } else if (alarmInfo.getAlarm_detail().contains(newText)) {
                            showAlarmInfoList.add(alarmInfo);
                        }
                    }
                }
                handler.sendEmptyMessage(UPDATE_LIST_INFO);
                return true;
            }
        });
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.search_close_btn:
                mAutoCompleteTextView.setText("");
                break;
            default:
                break;
        }
    }

    /*
     * 网络请求相关
     */
    // 从后台获取报警记录
    public void getAlarmInfoListByProjectId() {
        BaseOkHttpClient.newBuilder()
                .addHeader("Authorization",mToken)
                .addParam("deviceId",mBasketId)
                .addParam("pageSize",50000)//取无穷大时，表明不限制单页数据条数
                .addParam("pageIndex",1) //默认页码为1
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
                                ToastUtil.showToastTips(AlarmRecordBasketActivity.this, "登录已过期，请重新登陆");
                                startActivity(new Intent(AlarmRecordBasketActivity.this, LoginActivity.class));
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
                        repairObj.getString("id"),time,repairObj.getString("alarm_detail"),
                        repairObj.getString("worker"),repairObj.getString("workerName"));
                alarmInfoList.add(mAlarmInfo);
            }
        }
        showAlarmInfoList.addAll(alarmInfoList);
    }

    /*
     * UI 更新相关
     */
    private void updateContentView() {
        if (alarmInfoList.size() < 1) { // 暂无记录
            lvStop.setVisibility(View.GONE);
            llRecordSum.setVisibility(View.GONE);
            noRecordListRelativeLayout.setVisibility(View.VISIBLE);
            noRecordListTextView.setText("暂无报警记录！");
        } else if (showAlarmInfoList.size() < 1 ){
            lvStop.setVisibility(View.GONE);
            llRecordSum.setVisibility(View.GONE);
            noRecordListRelativeLayout.setVisibility(View.VISIBLE);
            noRecordListTextView.setText("未搜索出相关报警记录！");
        }else{  // 好多记录
            noRecordListRelativeLayout.setVisibility(View.GONE);
            lvStop.setVisibility(View.VISIBLE);
            llRecordSum.setVisibility(View.VISIBLE);
            tvRecordSum.setText(String.valueOf(alarmInfoList.size()));
        }
    }

    /**设置SearchView下划线透明**/
    private void setUnderLinetransparent(SearchView searchView){
        try {
            Class<?> argClass = searchView.getClass();
            Field ownField = argClass.getDeclaredField("mSearchPlate");
            ownField.setAccessible(true);
            View mView = (View) ownField.get(searchView);
            mView.setBackgroundColor(Color.TRANSPARENT);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }


    /*
     * 提示弹框
     */
    private CommonDialog initDialog(String mMsg){
        return new CommonDialog(this, R.style.dialog, mMsg,
                new CommonDialog.OnCloseListener() {
                    @Override
                    public void onClick(Dialog dialog, boolean confirm) {
                        if(confirm){
                            dialog.dismiss();
                        }else{
                            dialog.dismiss();
                        }
                    }
                }).setTitle("报警详情");
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
