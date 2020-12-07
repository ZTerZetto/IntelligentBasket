package com.automation.zzx.intelligent_basket_demo.activity.areaAdmin;

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
import android.widget.ExpandableListView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.automation.zzx.intelligent_basket_demo.R;
import com.automation.zzx.intelligent_basket_demo.activity.basket.AlarmRecordBasketActivity;
import com.automation.zzx.intelligent_basket_demo.activity.basket.BasketStateListActivity;
import com.automation.zzx.intelligent_basket_demo.activity.loginRegist.LoginActivity;
import com.automation.zzx.intelligent_basket_demo.adapter.areaAdmin.ExpandableListviewAdapter;
import com.automation.zzx.intelligent_basket_demo.adapter.areaAdmin.MgStateAdapter;
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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import okhttp3.Call;

import static android.content.ContentValues.TAG;

public class AlarmRecordProjectActivity extends AppCompatActivity implements View.OnClickListener {

    // intent 消息参数
    public final static String PROJECT_ID = "projectId";  // 项目Id

    // Handler消息
    private final static int MG_ALARM_LIST_INFO = 1;
    private final static int UPDATE_LIST_INFO = 2;
    private final static int UPDATE_STATEMENT_MSG = 3;

    // 主体
    // 报警类型选择栏
    private GridView mBasketStateGv; // 报警类型
    private List<String> mStateLists; // 类型名称
    private MgStateAdapter mgStateAdapter; //适配器
    private int pre_selectedPosition = 0;

    //搜索框控件
    private SearchView mSearchView;
    private AutoCompleteTextView mAutoCompleteTextView;//搜索输入框
    private ImageView mDeleteButton;//搜索框中的删除按钮

    private ExpandableListView expandListView;
    private List<AlarmInfo> alarmInfoList;
    private List<AlarmInfo> showAlarmInfoList;
    private List<String> mBasketList;
    private List<List<String>> mRecordList;
    private ExpandableListviewAdapter adapter;

    private CommonDialog mCommonDialog; //报警详情弹窗

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
                    showAlarmInfoList.clear();
                    parseAlarmListInfo((String) msg.obj);
                    adapter.refresh(mBasketList,mRecordList);
                    updateContentView();
                    break;
                case UPDATE_LIST_INFO:
                    mBasketList.clear();
                    mRecordList.clear();
                    alarmListToRecordList();
                    adapter.refresh(mBasketList,mRecordList);
                    updateContentView(); // 更新项目信息及控件显示

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
        toolbar.setTitle("项目报警记录");
        titleText.setText("");
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true); //设置返回键可用

        mBasketList = new ArrayList<>();
        mRecordList = new ArrayList<>();
        alarmInfoList = new ArrayList<>();
        showAlarmInfoList = new ArrayList<>();

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

       /* //初始化菜单栏
        mBasketStateGv = (GridView) findViewById(R.id.mg_basket_state);
        mStateLists = new ArrayList<>();

        initStateList();

        mgStateAdapter = new MgStateAdapter(AlarmRecordProjectActivity.this, R.layout.item_basket_state_switch, mStateLists);
        mgStateAdapter.setSelectedPosition(pre_selectedPosition);
        mBasketStateGv.setAdapter(mgStateAdapter);

        // 消息响应
        mBasketStateGv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                pre_selectedPosition = position;
                mgStateAdapter.setSelectedPosition(pre_selectedPosition);
                if(mProjectId != null)  // 当且仅当存在项目时更新吊篮状态列表
                    handler.sendEmptyMessage(UPDATE_STATEMENT_MSG);  // 更新列表
            }
        });*/


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
        mSearchView.setQueryHint("输入吊篮ID或描述详情");//设置查询提示字符串
        mSearchView.setSubmitButtonEnabled(true);//设置是否显示搜索框展开时的提交按钮
        mAutoCompleteTextView.setTextColor(Color.GRAY);
        //设置SearchView下划线透明
        setUnderLinetransparent(mSearchView);
        setListener();

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
                //Toast.makeText(AlarmRecordProjectActivity.this, "点击"+mBasketList.get(groupPosition), Toast.LENGTH_SHORT).show();
                return false;
            }
        });

        //子视图的点击事件
        expandListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView expandableListView, View view, int groupPosition, int childPosition, long l) {
                String basketId = mBasketList.get(groupPosition);
                String time = mRecordList.get(groupPosition).get(childPosition).substring(0,19);
                String detail = mRecordList.get(groupPosition).get(childPosition).substring(21);
                mCommonDialog = initDialog("吊篮编号："+basketId+'\n'+detail+'\n'+time,basketId);
                mCommonDialog.show();
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
                .addParam("projectId",mProjectId)
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
                        repairObj.getString("id"),time,repairObj.getString("alarm_detail"),
                        repairObj.getString("worker"),repairObj.getString("workerName"));
                alarmInfoList.add(mAlarmInfo);
            }
        }
        showAlarmInfoList.addAll(alarmInfoList);
        alarmListToRecordList();
    }

    private void alarmListToRecordList( ){
        Map<String , String> map1 = new HashMap<>();
        Map<String , String> map2 = new HashMap<>();
        for(int i = 0; i < showAlarmInfoList.size();i++){
            map1.put(showAlarmInfoList.get(i).getDevice_id(),showAlarmInfoList.get(i).getTime()+"  "+showAlarmInfoList.get(i).getAlarm_detail());
            map2.put(showAlarmInfoList.get(i).getTime()+"  "+showAlarmInfoList.get(i).getAlarm_detail(),showAlarmInfoList.get(i).getDevice_id());
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
        if(alarmInfoList.size() == 0 ){  // 显示暂无报警记录
            expandListView.setVisibility(View.GONE);
            noRepairListRelativeLayout.setVisibility(View.VISIBLE);
            noRepairListTextView.setText("暂无报警记录！");
        }else if(mBasketList.size() == 0 ){ // 显示未搜索
            expandListView.setVisibility(View.GONE);
            noRepairListRelativeLayout.setVisibility(View.VISIBLE);
            noRepairListTextView.setText("未搜索出相关报警记录！");
        }else{                                          // 显示项目列表
            noRepairListRelativeLayout.setVisibility(View.GONE);
            expandListView.setVisibility(View.VISIBLE);
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
    private CommonDialog initDialog(String mMsg, final String id){
        return new CommonDialog(this, R.style.dialog, mMsg,
                new CommonDialog.OnCloseListener() {
                    @Override
                    public void onClick(Dialog dialog, boolean confirm) {
                        if(confirm){
                            dialog.dismiss();
                            Intent intent = new Intent(AlarmRecordProjectActivity.this, AlarmRecordBasketActivity.class);
                            intent.putExtra("basket_id", id);
                            startActivity(intent);
                        }else{
                            dialog.dismiss();
                        }
                    }
                }).setTitle("跳转至吊篮报警详情");
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

    /*
     * 初始化
     */
    private void initStateList(){
        //初始化状态筛选栏
        mStateLists.add("行为报警");
        mStateLists.add("故障报警");

    }


}
