package com.automation.zzx.intelligent_basket_demo.activity.basket;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.automation.zzx.intelligent_basket_demo.R;
import com.automation.zzx.intelligent_basket_demo.activity.areaAdmin.AlarmRecordProjectActivity;
import com.automation.zzx.intelligent_basket_demo.activity.loginRegist.LoginActivity;
import com.automation.zzx.intelligent_basket_demo.activity.worker.WorkerHomePageActivity;
import com.automation.zzx.intelligent_basket_demo.adapter.basket.FunctionAdapter;
import com.automation.zzx.intelligent_basket_demo.entity.AppConfig;
import com.automation.zzx.intelligent_basket_demo.entity.Function;
import com.automation.zzx.intelligent_basket_demo.entity.UserInfo;
import com.automation.zzx.intelligent_basket_demo.utils.ToastUtil;
import com.automation.zzx.intelligent_basket_demo.utils.okhttp.BaseCallBack;
import com.automation.zzx.intelligent_basket_demo.utils.okhttp.BaseOkHttpClient;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import okhttp3.Call;

/**
 * Created by pengchenghu on 2019/2/23.
 * Author Email: 15651851181@163.com
 * Describe: 选择测试功能
 */

public class BasketDetailActivity extends AppCompatActivity implements View.OnClickListener {

    // 页面跳转消息
    public final static String BASKET_ID = "basket_id";

    //Handler消息
    private final static int GET_WORKERS_BY_BASKET = 1; //根据吊篮获取施工人员列表

    //返回結果
    public final static int UPLOAD_BASKET_REPAIR_RESULT = 101; //報修返回

    // 控件声明
    private TextView txtBasketId;  //吊篮编号
    private LinearLayout llLocation;  //现场编号
    private TextView txtLocationId;  //现场编号
    private LinearLayout llArea;  //位置区域
    private TextView txtAreaId;  //位置区域
    private TextView txtBasketState; //吊篮状态
    private GridView mMonitorGridView;  // 功能测试
    private GridView mFunctionGridView;
    private GridView mInfoGridView;
    private TextView tvWorkerName_1;
    private TextView tvWorkerName_2;
    private TextView tvWorkerPhone_1;
    private TextView tvWorkerPhone_2;
    private RelativeLayout llWorker_1;
    private RelativeLayout llWorker_2;
    private TextView tvNoWorker;


    // function gridview
    private List<Function> mFunctions1;  // 功能列表
    private List<Function> mFunctions2;  // 功能列表
    private List<Function> mFunctions3;  // 功能列表
    private FunctionAdapter mFunction1Adapter;  // 功能适配器
    private FunctionAdapter mFunction2Adapter;  // 功能适配器
    private FunctionAdapter mFunction3Adapter;  // 功能适配器


    // others
    private String mProjectId;  //項目ID
    private String mBasketId;  // 吊篮id
    private String areaNum;  // 楼号
    private String locationNum;  // 现场编号
    private String mBasketState;  // 吊篮状态  0 待安装/1 待安监/2 进行中/3 预报停 /4 报停审核
    private UserInfo mPrincipal_1; //1号操作人员
    private UserInfo mPrincipal_2; //2号操作人员

    // 用户登录信息相关
    private UserInfo mUserInfo; //用户信息
    private String mToken;
    private SharedPreferences mPref;

    public final static String UPLOAD_PROJECT_ID  = "project_id"; // 項目ID
    public final static String UPLOAD_BASKET_ID = "basket_id"; // 報修操作
    public final static String UPLOAD_IMAGE_TEXT_TYPE  = "uploadImageTextType"; // 上传图片的类型
    public final static String UPLOAD_BASKET_REPAIR_IMAGE = "basketRepair"; // 報修操作


    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            switch(msg.what) {
                case GET_WORKERS_BY_BASKET:
                    //处理收到的项目列表信息
                    mPrincipal_1 = mPrincipal_2 = null;
                    parseProjectListInfo((String)msg.obj);  // 解析数据
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_basket_detail);

        // 顶部导航栏
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        TextView titleText = (TextView) findViewById(R.id.toolbar_title);
        toolbar.setTitle("");
        titleText.setText(getString(R.string.basketDetail_tile));
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true); //设置返回键可用

        initWidgetResource();
        getBaseInfoFromPred();
        getWorkerIdByBasket();
    }

    // 項目和吊籃信息获取
    @SuppressLint("ResourceAsColor")
    public void getBaseInfoFromPred() {
        Intent intent = getIntent();
        mProjectId = intent.getStringExtra("project_id");
        mBasketId = intent.getStringExtra("basket_id");
        mBasketState = intent.getStringExtra("basket_state");
        mBasketState = mBasketState.substring(0,1);

        //区域楼号
        if(intent.getStringExtra("area_num") != null){
            areaNum = intent.getStringExtra("area_num");
            llArea.setVisibility(View.VISIBLE);
            txtAreaId.setText(areaNum+"号楼");
        } else {
            // 隐藏区域
            llArea.setVisibility(View.GONE);
        }

        //现场编号
        if(intent.getStringExtra("location_num") != null){
            locationNum = intent.getStringExtra("location_num");
            llLocation.setVisibility(View.VISIBLE);
            txtLocationId.setText("#"+locationNum);
        } else {
            //隐藏区域
            llLocation.setVisibility(View.GONE);
        }

        txtBasketId.setText(mBasketId);
        //0 待安装/1 待安监/2 进行中/3 预报停 /4 报停审核
        switch (mBasketState){
            case "1":
                txtBasketState.setText("待安装");
                txtBasketState.setTextColor(R.color.gray01);
                break;
            case "2":
                txtBasketState.setText("安装验收");
                txtBasketState.setTextColor(R.color.gray01);
                break;
            case "3":
                txtBasketState.setText("进行中");
                txtBasketState.setTextColor(R.color.colorPrimary);
                break;
            case "4":
                txtBasketState.setText("待报停");
                txtBasketState.setTextColor(R.color.light_red);
                break;
            case "5":
                txtBasketState.setText("报停审核");
                txtBasketState.setTextColor(R.color.light_red);
                break;
            default:
                txtBasketState.setVisibility(View.GONE);
                break;
        }


        // 从本地获取数据
        mPref = PreferenceManager.getDefaultSharedPreferences(this);
        mUserInfo = new UserInfo();
        mUserInfo.setUserId(mPref.getString("userId", ""));
        mUserInfo.setUserPhone(mPref.getString("userPhone", ""));
        mUserInfo.setUserRole(mPref.getString("userRole", ""));
        mUserInfo.setUserName(mPref.getString("userName", ""));
        mToken = mPref.getString("loginToken","");
    }

    // 资源句柄初始化及监听
    public void initWidgetResource(){
        // 获取控件句柄
        mMonitorGridView = (GridView) findViewById(R.id.function_gridview_1);
        mFunctionGridView = (GridView) findViewById(R.id.function_gridview_2);
        mInfoGridView = (GridView) findViewById(R.id.function_gridview_3);

        //初始化控件并显示内容
        txtBasketId = (TextView) findViewById(R.id.basket_id);


        llLocation = findViewById(R.id.ll_location);
        txtLocationId = findViewById(R.id.location_id);

        llArea = findViewById(R.id.ll_area);
        txtAreaId =findViewById(R.id.area_id);

        txtBasketState = (TextView) findViewById(R.id.basket_state);



        // 初始化功能列表
        initFunctionList();
        mFunction1Adapter = new FunctionAdapter(BasketDetailActivity.this,
                R.layout.item_function, mFunctions1); // 初始化适配器
        mMonitorGridView.setAdapter(mFunction1Adapter); // 装载适配器
        mMonitorGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() { // GridView 监听
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // do something
                Intent intent;
                switch(position){
                    case 0:  // 监控视频
                        intent = new Intent(BasketDetailActivity.this, BasketVideoActivity.class);
                        intent.putExtra(BASKET_ID, mBasketId);
                        startActivity(intent);
                        break;
                    case 1:  // 工况图片
                        intent = new Intent(BasketDetailActivity.this, BasketPhotoActivity.class);
                        intent.putExtra(BASKET_ID, mBasketId);
                        startActivity(intent);
                        break;
                    case 2:  // 实时参数
                        intent = new Intent(BasketDetailActivity.this, BasketParameterNewActivity.class);
                        intent.putExtra(BASKET_ID, mBasketId);
                        startActivity(intent);
                        break;
                    default:break;
                }
            }
        });

        mFunction2Adapter = new FunctionAdapter(BasketDetailActivity.this,
                R.layout.item_function, mFunctions2); // 初始化适配器
        mFunctionGridView.setAdapter(mFunction2Adapter); // 装载适配器
        mFunctionGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() { // GridView 监听
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // do something
                Intent intent;
                switch(position){
                    case 0:  // 参数设置
                        intent = new Intent(BasketDetailActivity.this, BasketSettleActivity.class);
                        intent.putExtra(BASKET_ID, mBasketId);
                        startActivity(intent);
                        break;
                    case 1:  // 申请报修
                        intent = new Intent(BasketDetailActivity.this, BasketRepairActivity.class);
                        intent.putExtra(UPLOAD_PROJECT_ID, mProjectId);
                        intent.putExtra(UPLOAD_BASKET_ID, mBasketId);
                        intent.putExtra(UPLOAD_IMAGE_TEXT_TYPE, UPLOAD_BASKET_REPAIR_IMAGE);
                        startActivityForResult(intent, UPLOAD_BASKET_REPAIR_RESULT);
                        break;
                    case 2:  // 报停

                        break;
                    default:break;
                }
            }
        });

        mFunction3Adapter = new FunctionAdapter(BasketDetailActivity.this,
                R.layout.item_function, mFunctions3); // 初始化适配器
        mInfoGridView.setAdapter(mFunction3Adapter); // 装载适配器
        mInfoGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() { // GridView 监听
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // do something
                Intent intent;
                switch(position){
                    case 0:  // 安监证书
                        intent = new Intent(BasketDetailActivity.this, BasketCertActivity.class);
                        intent.putExtra(UPLOAD_PROJECT_ID, mProjectId);
                        intent.putExtra(UPLOAD_BASKET_ID, mBasketId);
                        startActivity(intent);
                        break;
                    case 1:  // 设备产权
                        intent = new Intent(BasketDetailActivity.this, EquipCopyRightActivity.class);
                        intent.putExtra(UPLOAD_BASKET_ID, mBasketId);
                        startActivity(intent);
                        break;
                    case 2:  // 报警记录
                        intent = new Intent(BasketDetailActivity.this, AlarmRecordBasketActivity.class);
                        intent.putExtra(UPLOAD_BASKET_ID, mBasketId);
                        startActivity(intent);
                        break;
                    default:break;
                }
            }
        });

        // 初始化操作人员控件
        tvWorkerName_1 = findViewById(R.id.tv_worker1_name);
        tvWorkerName_2 = findViewById(R.id.tv_worker2_name);
        tvWorkerPhone_1 = findViewById(R.id.tv_worker1_phone);
        tvWorkerPhone_2 = findViewById(R.id.tv_worker2_phone);
        llWorker_1 = findViewById(R.id.rl_worker1_all);
        llWorker_1.setClickable(false);
        llWorker_1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(BasketDetailActivity.this,WorkerHomePageActivity.class);
                intent.putExtra("worker_id", mPrincipal_1.getUserId());
                startActivity(intent);
            }
        });
        llWorker_2 = findViewById(R.id.rl_worker2_all);
        llWorker_2.setClickable(false);
        llWorker_2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(BasketDetailActivity.this,WorkerHomePageActivity.class);
                intent.putExtra("worker_id", mPrincipal_2.getUserId());
                startActivity(intent);
            }
        });
        tvNoWorker = findViewById(R.id.tv_no_worker);
    }

    //TODO 获取当前操作人员ID
    public void getWorkerIdByBasket(){
        BaseOkHttpClient.newBuilder()
                .addHeader("Authorization", mToken)
                .addParam("type", 1)
                .addParam("deviceId", mBasketId)
                .get()
                .url(AppConfig.GET_WORKERS_BY_BASKET)
                .build()
                .enqueue(new BaseCallBack() {
                    @Override
                    public void onSuccess(Object o) {
                        String responseData = o.toString();
                        Message message = new Message();
                        message.what = GET_WORKERS_BY_BASKET;
                        message.obj = responseData;
                        handler.sendMessage(message);

                        //mSmartRefreshLayout.finishRefresh(500); // 刷新成功
                    }

                    @Override
                    public void onError(int code) {
                        switch (code){
                            case 401: // 未授权
                                ToastUtil.showToastTips(BasketDetailActivity.this, "登录已过期，请重新登陆");
                                startActivity(new Intent(BasketDetailActivity.this, LoginActivity.class));
                                BasketDetailActivity.this.finish();
                                break;
                            case 403: // 禁止
                                break;
                            case 404: // 404
                                break;
                        }

                        //mSmartRefreshLayout.finishRefresh(500, false); // 刷新失败
                    }

                    @Override
                    public void onFailure(Call call, IOException e) {
                        //mSmartRefreshLayout.finishRefresh(500, false); // 刷新失败
                    }
                });
    }

    // 解析项目列表数据
    private void parseProjectListInfo(String responseData){
        JSONObject jsonObject = JSON.parseObject(responseData);
        JSONObject workerInfo = jsonObject.getJSONObject("workerList");
        JSONObject workerString1 = workerInfo.getJSONObject("worker1");
        JSONObject workerString2 = workerInfo.getJSONObject("worker2");

        if (workerString1 == null) {
            llWorker_1.setVisibility(View.GONE);
            llWorker_2.setVisibility(View.GONE);
            tvNoWorker.setVisibility(View.VISIBLE);
        } else {
            tvNoWorker.setVisibility(View.GONE);
            mPrincipal_1 = new UserInfo(workerString1.getString("userId"), workerString1.getString("userName"),
                    workerString1.getString("userPhone"), workerString1.getString("userPassword"), workerString1.getString("userRole"));
            tvWorkerName_1.setText(mPrincipal_1.getUserName());
            tvWorkerPhone_1.setText(mPrincipal_1.getUserPhone());
            llWorker_1.setClickable(true);

            if (workerString2 != null) {
                mPrincipal_2 = new UserInfo(workerString2.getString("userId"), workerString2.getString("userName"),
                        workerString2.getString("userPhone"), workerString2.getString("userPassword"), workerString2.getString("userRole"));
                tvWorkerName_2.setText(mPrincipal_2.getUserName());
                tvWorkerPhone_2.setText(mPrincipal_2.getUserPhone());
                llWorker_2.setClickable(true);
            } else {
                llWorker_2.setVisibility(View.GONE);
                /*tvWorkerName_2.setText("暂无");
                tvWorkerPhone_2.setText(" ");
                llWorker_2.setClickable(false);*/
            }
        }
    }

    /*
     * 消息响应
     */
    // 消息想要
    @Override
    public void onClick(View v) {

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

    // 初始化测试功能列表
    private void initFunctionList(){
        //先初始化
        mFunctions1 = new ArrayList<>();
        Function video = new Function("监控视频", R.mipmap.ic_video_192,true);
        //Function video = new Function("监控", R.mipmap.ic_video);
        mFunctions1.add(video);
        Function image = new Function("工况图片", R.mipmap.ic_image_192,true);
        //Function image = new Function("图片", R.mipmap.ic_image);
        mFunctions1.add(image);
        Function parameter = new Function("实时参数", R.mipmap.ic_parameter_192,true);
        //Function parameter = new Function("参数", R.mipmap.ic_parameter);
        mFunctions1.add(parameter);

        mFunctions2 = new ArrayList<>();
        Function setting = new Function("参数设置", R.mipmap.ic_setting_192,true);
        //Function video = new Function("监控", R.mipmap.ic_video);
        mFunctions2.add(setting);
        Function repair = new Function("申请报修", R.mipmap.ic_repair_192,true);
        //Function video = new Function("监控", R.mipmap.ic_video);
        mFunctions2.add(repair);
        Function stop = new Function("报停", R.mipmap.ic_repair_192,true);
        //Function video = new Function("监控", R.mipmap.ic_video);
        mFunctions2.add(stop);

        mFunctions3 = new ArrayList<>();
        Function history = new Function("安监证书", R.mipmap.ic_history_info_192,true);
        //Function video = new Function("监控", R.mipmap.ic_video);
        mFunctions3.add(history);
        Function property = new Function("设备产权", R.mipmap.ic_setting_192,true);
        //Function video = new Function("监控", R.mipmap.ic_video);
        mFunctions3.add(property);
        Function equipment = new Function("报警信息", R.mipmap.ic_device_192,true);
        //Function video = new Function("监控", R.mipmap.ic_video);
        mFunctions3.add(equipment);


        /*//根据吊篮状态隐藏功能列表
        if(mBasketState < 2 ){ //未投入使用
            for(int i = 4; i <  mFunctions.size() ;i++){
                mFunctions.get(i).setViewState(false);
            }
        }
        if(mBasketState < 1 ){ //未上传安装照片
            for(int i = 2; i < 4 ;i++){
                mFunctions.get(i).setViewState(false);
            }
        }*/
    }


    /*
     * 活动返回监听
     */
    //页面返回数据监听
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case UPLOAD_BASKET_REPAIR_RESULT:        //报修结果显示
                if(resultCode ==RESULT_OK ) {
                    Toast.makeText(BasketDetailActivity.this, "报修成功！", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                break;
        }
    }
}
