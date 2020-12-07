package com.automation.zzx.intelligent_basket_demo.activity.basket;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.automation.zzx.intelligent_basket_demo.R;
import com.automation.zzx.intelligent_basket_demo.activity.loginRegist.LoginActivity;
import com.automation.zzx.intelligent_basket_demo.adapter.basket.InstallChooseAdapter;
import com.automation.zzx.intelligent_basket_demo.adapter.basket.OpenChooseAdapter;
import com.automation.zzx.intelligent_basket_demo.entity.AppConfig;
import com.automation.zzx.intelligent_basket_demo.entity.MgBasketStatement;
import com.automation.zzx.intelligent_basket_demo.entity.UserInfo;
import com.automation.zzx.intelligent_basket_demo.utils.ToastUtil;
import com.automation.zzx.intelligent_basket_demo.utils.okhttp.BaseCallBack;
import com.automation.zzx.intelligent_basket_demo.utils.okhttp.BaseOkHttpClient;
import com.automation.zzx.intelligent_basket_demo.widget.dialog.CommonDialog;
import com.automation.zzx.intelligent_basket_demo.widget.zxing.activity.CaptureActivity;
import com.hjq.permissions.OnPermission;
import com.hjq.permissions.Permission;
import com.hjq.permissions.XXPermissions;
import com.scwang.smartrefresh.header.BezierCircleHeader;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import okhttp3.Call;

import static com.automation.zzx.intelligent_basket_demo.widget.zxing.activity.CaptureActivity.QR_CODE_RESULT;


public class BasketOpenByListActivity extends AppCompatActivity implements View.OnClickListener{

    private final static String TAG = "BasketOpenByList";

    private final static int BASKET_ACT_TYPE_ONLINE = 1;
    private final static int BASKET_ACT_TYPE_OFFLINE = 0;

    // Handler 消息类型
    private final static int UPDATE_BASKET_STATEMENT_MSG = 101;  // 更新吊篮状态列表
    private final static int GET_BASKET_MSG =103;   // 获取指定项目的吊篮列表

    // intent 消息参数
    public final static String BASKET_ID = "basketId"; // 上传图片的吊篮ID
    public final static String ACT_TYPE = "act_type"; // 上下电操作标识
    
    /* 控件部分 */
    // 顶部导航栏
    private Toolbar toolbar;
    private TextView titleText;

    // 主体
    private SmartRefreshLayout mSmartRefreshLayout; // 下拉刷新
    private RecyclerView basketRv; // 吊篮列表
    /*private List<MgBasketInfo> mgBasketInfoList; //MgBasketStatement*/

    private OpenChooseAdapter mgBasketListAdapter;
    private RelativeLayout noBasketListRelativeLayout; // 空空如也
    private TextView noBasketListTextView;

    // 底部合计
    private CheckBox basketAllSelected;  // 全选复选框
    private TextView basketNumber;  // 已选择吊篮个数
    private TextView basketAllOpen; // 吊篮一键上电

    /* 数据部分*/
    // 用户登录信息相关
    private UserInfo mUserInfo; //用户信息
    private String mProjectId;  //項目ID
    private String mToken;
    private SharedPreferences mPref;
    private List<MgBasketStatement> mgBasketStatementList;

    //其他数据
    private String mBasketIdList; //选中的吊篮id集合（以,分割）
    private int type; //上下电，0=上电，1=下电

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            switch(msg.what) {
                case UPDATE_BASKET_STATEMENT_MSG:  // 吊篮列表页面更新
                    break;
                case GET_BASKET_MSG: // 获取吊篮列表信息
                    mSmartRefreshLayout.finishRefresh();
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_basket_open_by_list);

        initInfo();
        initWidget();
        updateBodyContentView();
    }
    /*
     * 解析用户信息
     * 解析用户信息
     */
    // 获取用户数据
    private void initInfo(){
        // 从本地获取数据
        mPref = PreferenceManager.getDefaultSharedPreferences(this);
        mUserInfo = new UserInfo();
        mUserInfo.setUserId(mPref.getString("userId", ""));
        mUserInfo.setUserPhone(mPref.getString("userPhone", ""));
        mUserInfo.setUserRole(mPref.getString("userRole", ""));
        mUserInfo.setUserName(mPref.getString("userName", ""));
        mToken = mPref.getString("loginToken","");
        mProjectId = mPref.getString("projectId","");

        mgBasketStatementList =(List<MgBasketStatement>) getIntent().getSerializableExtra("basket_list");
        type = getIntent().getIntExtra(ACT_TYPE,0);
    }

    /*
     * 初始化页面
     * */
    //初始化句柄
    private void initWidget() {
        // 顶部导航栏
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        titleText = (TextView) findViewById(R.id.toolbar_title);
        toolbar.setTitle("一键上下机");
        titleText.setText("");
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true); //设置返回键可用

        // 下拉刷新
        mSmartRefreshLayout = (SmartRefreshLayout) findViewById(R.id.smart_refresh_layout);
        mSmartRefreshLayout.setRefreshHeader(  //设置 Header 为 贝塞尔雷达 样式
                new BezierCircleHeader(this));
        mSmartRefreshLayout.setPrimaryColorsId(R.color.smart_loading_background_color);
        mSmartRefreshLayout.setOnRefreshListener(new OnRefreshListener() { // 添加下拉刷新监听
            @Override
            public void onRefresh(RefreshLayout refreshlayout) {
                handler.sendEmptyMessage(GET_BASKET_MSG);
            }
        });

        // 初始化吊篮列表
        basketRv = (RecyclerView) findViewById(R.id.basket_recycler_view);
        LinearLayoutManager layoutManager = new LinearLayoutManager(BasketOpenByListActivity.this);
        basketRv.setLayoutManager(layoutManager);
        mgBasketListAdapter = new OpenChooseAdapter(this, mgBasketStatementList);
        basketRv.setAdapter(mgBasketListAdapter);
        mgBasketListAdapter.setOnItemClickListener(new OpenChooseAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                // item 点击响应
                Log.i(TAG, "You have clicked the "+ position +" item");
                if( mBasketIdList==null || mBasketIdList.equals("")){
                    DialogToast("提示", "吊篮信息有误，无法进行上下机操作，请关闭页面后下拉刷新重试。");
                }
            }

            @Override
            public void onCheckChanged(View view, int position, boolean checked) {
                // checkbox 状态更换
                Log.i(TAG, "You have changed the "+ position +" item checkbox");
                int basketNumberSelected = mgBasketListAdapter.checkedBasket();
                basketNumber.setText(String.valueOf(basketNumberSelected));
                basketAllSelected.setChecked(basketNumberSelected == mgBasketStatementList.size());
            }
        });

        // 空空如也
        noBasketListRelativeLayout = (RelativeLayout) findViewById(R.id.basket_no_avaliable);
        noBasketListTextView = (TextView) findViewById(R.id.no_basket_hint);

        // 底部合计
        // 控件初始化
        basketAllSelected = (CheckBox) findViewById(R.id.basket_all_checkbox);
        basketAllSelected.setChecked(false);
        basketNumber = (TextView) findViewById(R.id.basket_number);
        basketAllOpen = (TextView) findViewById(R.id.basket_all_open);

        // 消息监听
        // 全选按钮
        basketAllSelected.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Map<Integer,Boolean> isCheck = mgBasketListAdapter.getMap();
                if(!isChecked){  // 规避减一个checkbox导致取消全选的问题
                    if(isCheck.size() != mgBasketListAdapter.checkedBasket())
                        return;
                }
                mgBasketListAdapter.initCheck(isChecked);
                mgBasketListAdapter.notifyDataSetChanged();
            }
        });

        // 按钮文案展示
        if(type == BASKET_ACT_TYPE_ONLINE){
            basketAllOpen.setText("一键上机");
        } else {
            basketAllOpen.setText("一键下机");
        }
        // 一键上电按钮点击
        basketAllOpen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(Integer.parseInt(basketNumber.getText().toString()) == 0) {  // 尚未选择吊篮
                    ToastUtil.showToastTips(BasketOpenByListActivity.this, "您尚未选择任何吊篮");
                } else {
                    mBasketIdList = getInstallBasketList();
                    String content = null;
                    // 弹窗二次确认,请求一键上电接口
                    if(type == BASKET_ACT_TYPE_ONLINE){
                        content = "您请求上机的吊篮编号为" + mBasketIdList;
                    } else if(type == BASKET_ACT_TYPE_OFFLINE){
                        content = "您请求下机的吊篮编号为" + mBasketIdList;
                    }
                    new CommonDialog(BasketOpenByListActivity.this, R.style.dialog, content, new CommonDialog.OnCloseListener() {
                        @Override
                        public void onClick(Dialog dialog, boolean confirm) {
                            if (confirm) {
                                addOpenWithBasket(mBasketIdList);
                                dialog.dismiss();
                            } else {
                                dialog.dismiss();
                            }
                        }
                    }).setTitle("提示").show(); } }
        });
    }

    /*
     * UI 更新类
     */
    // 主体页面显示逻辑控制
    public void updateBodyContentView() {
        //有无吊篮显示
        if (mgBasketStatementList.size() < 1) { // 暂无吊篮
            basketRv.setVisibility(View.GONE);
            noBasketListRelativeLayout.setVisibility(View.VISIBLE);
            noBasketListTextView.setText("您还没有相关的吊篮");
        } else {  // 好多吊篮
            noBasketListRelativeLayout.setVisibility(View.GONE);
            basketRv.setVisibility(View.VISIBLE);
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

    // 获取选中吊篮列表
    private String getInstallBasketList(){
        String results = "";
        Map<Integer,Boolean> isCheck = mgBasketListAdapter.getMap();
        for(int i=0; i<isCheck.size(); i++){
            if(isCheck.get(i)){
                results += mgBasketStatementList.get(i).getBasketId() + ",";
            }
        }
        results = results.substring(0, results.length()-1);
        return results;
    }

    @Override
    public void onClick(View v) {

    }

    // 一键上电申请
    private void addOpenWithBasket(String mBasketIdList){
        BaseOkHttpClient.newBuilder()
                .addHeader("Authorization", mToken)
                .addParam("deviceList", mBasketIdList)
                .addParam("type", type) // 下电=0，上电=1
                .post()
                .url(AppConfig.SEND_TO_ALL_DEVICE)
                .build()
                .enqueue(new BaseCallBack() {
                    @Override
                    public void onSuccess(Object o) {
                        String response = o.toString();
                        JSONObject jsonObject = JSON.parseObject(response);
                        String create = jsonObject.getString("create");
                        Boolean isLogin = jsonObject.getBoolean("isLogin");
                        if(isLogin.equals(true)){
                            if(type == BASKET_ACT_TYPE_ONLINE){
                                if(create.contains("success")) {
                                    Log.i(TAG, "成功发送上机请求");
                                    CloseDialog("提示", "您的请求已发送，该批吊篮将会在2分钟内陆续上机。若有部分吊篮因现场信号问题未及时上机，请重试，或安排施工人员扫码上电。").show();
                                }else {
                                    CloseDialog("提示", "一键上机请求失败，请关闭页面后下拉刷新，重新进入一键上下机页面再次尝试。").show();
                                }
                            } else if(type == BASKET_ACT_TYPE_OFFLINE){
                                if(create.contains("success")) {
                                    Log.i(TAG, "成功发送下机请求");
                                    CloseDialog("提示", "您的请求已发送，该批吊篮将会在2分钟内陆续下机。若有部分吊篮因现场信号问题未及时下机，请重试，或安排施工人员扫码上电。").show();
                                } else {
                                    CloseDialog("提示", "一键上机请求失败，请关闭页面后下拉刷新，重新进入一键上下机页面再次尝试。").show();
                                }
                            }
                        } else {
                            DialogToast("提示", "权限认证失败,请求发送失败。请稍后重试").show();
                        }
                    }

                    @Override
                    public void onError(int code) {
                        switch(code){
                            case 401:
                                ToastUtil.showToastTips(BasketOpenByListActivity.this, "登陆已过期，请重新登录");
                                startActivity(new Intent(BasketOpenByListActivity.this, LoginActivity.class));
                                BasketOpenByListActivity.this.finish();
                                break;
                            case 403:
                                break;
                        }
                    }

                    @Override
                    public void onFailure(Call call, IOException e) {

                    }
                });
    }

    /*
     * 提示弹框
     */
    private CommonDialog DialogToast(String mTitle, String mMsg){
        return new CommonDialog(BasketOpenByListActivity.this, R.style.dialog, mMsg,
                new CommonDialog.OnCloseListener() {
                    @Override
                    public void onClick(Dialog dialog, boolean confirm) {
                        if(confirm){
                            dialog.dismiss();
                        }else{
                            dialog.dismiss();
                        }
                    }
                }).setTitle(mTitle);
    }

    /*
     * 确定提示弹框
     */
    private CommonDialog CloseDialog(String mTitle, String mMsg){
        return new CommonDialog(BasketOpenByListActivity.this, R.style.dialog, mMsg,
                new CommonDialog.OnCloseListener() {
                    @Override
                    public void onClick(Dialog dialog, boolean confirm) {
                        if(confirm){
                            dialog.dismiss();
                            Intent intent = new Intent();
                            setResult(RESULT_OK, intent);
                            finish();
                        }else{
                            dialog.dismiss();
                            finish();
                        }
                    }
                }).setTitle(mTitle);
    }
}
