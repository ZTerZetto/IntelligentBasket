package com.automation.zzx.intelligent_basket_demo.activity.InstallInfo;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.automation.zzx.intelligent_basket_demo.R;
import com.automation.zzx.intelligent_basket_demo.activity.areaAdmin.AreaAdminSumitReportActivity;
import com.automation.zzx.intelligent_basket_demo.activity.basket.BasketHistoryInfoActivity;
import com.automation.zzx.intelligent_basket_demo.activity.common.UploadImageFTPActivity;
import com.automation.zzx.intelligent_basket_demo.adapter.basket.PortionAdapter;
import com.automation.zzx.intelligent_basket_demo.entity.AppConfig;
import com.automation.zzx.intelligent_basket_demo.entity.MgBasketInstallInfo;
import com.automation.zzx.intelligent_basket_demo.entity.Portion;
import com.automation.zzx.intelligent_basket_demo.entity.PortionMap;
import com.automation.zzx.intelligent_basket_demo.utils.ftp.FTPUtil;
import com.automation.zzx.intelligent_basket_demo.utils.okhttp.BaseCallBack;
import com.automation.zzx.intelligent_basket_demo.utils.okhttp.BaseOkHttpClient;
import com.automation.zzx.intelligent_basket_demo.widget.SmartGridView;
import com.scwang.smartrefresh.header.BezierCircleHeader;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;

import static com.automation.zzx.intelligent_basket_demo.widget.zxing.activity.CaptureActivity.QR_CODE_RESULT;

public class BasketInstallInfoActivity extends AppCompatActivity {

    private static final String TAG = "FinishImgActivity";

    // 消息处理变量
    private static final int UPDATE_IMAGE_STATE_MSG = 101;

    // 吊篮状态及页面显示动态更新
    private static final int UPDATE_BASKET_STATE = 104;

    // 页面跳转全局变量
    public static final String PROJECT_ID = "project_id";  // 项目ID
    public static final String BASKET_ID = "basket_id";  // 吊篮ID
    public static final String IMAGE_TYPE_ID = "image_type_id";  // 上传图片类型

    public final static String UPLOAD_IMAGE_TYPE = "uploadImageType"; // 上传图片的类型
    public final static String UPLOAD_CERTIFICATE_IMAGE = "certificate"; // 安监证书


    //页面返回变量
    private final static int UPLOAD_PRE_CHECK_RESULT = 3;  // 提交预检结果返回页面
    private final static int UPLOAD_CERTIFICATE_IMAGE_RESULT = 4;  // 上传安检证书返回页面

    // 全局变量
    private String projectId = "201910110001";
    private MgBasketInstallInfo basketinfo;

    // 控件声明
    private SmartRefreshLayout mSmartRefreshLayout; // 下拉刷新
    private SmartGridView mPortionGv;  // 部件网格控件
    private TextView txtBasketId;  //吊篮编号
    private TextView txtBasketState; //吊篮状态

    private RelativeLayout rlPreCheck; //安装预检
    private RelativeLayout rlCertificateUpdate; //上传安监证书
    private RelativeLayout rlCertificate; //查看安监证书

    // var switch gridview
    private List<Portion> mPortions;  // 部件变量列表
    private PortionAdapter mPortionAdapter;  // 部件变量适配器

    // FTP 文件服务器
    private FTPUtil mFTPClient;
    private String mRemotePath;

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case UPDATE_IMAGE_STATE_MSG:
                    mPortionAdapter.notifyDataSetChanged();
                    break;
                    case UPDATE_BASKET_STATE:
                        updateUI();
                        break;
                default: break;
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_basket_install_info);

        getIntentData();
        initWidgets();
        initFTPClient();

    }

    /* 初始化控件
     * */
    @SuppressLint("ResourceAsColor")
    private void initWidgets() {
        // 下拉刷新
        mSmartRefreshLayout = (SmartRefreshLayout) findViewById(R.id.smart_refresh_layout);
        mSmartRefreshLayout.setRefreshHeader(  //设置 Header 为 贝塞尔雷达 样式
                new BezierCircleHeader(this));
        mSmartRefreshLayout.setPrimaryColorsId(R.color.smart_loading_background_color);
        mSmartRefreshLayout.setOnRefreshListener(new OnRefreshListener() { // 添加下拉刷新监听
            @Override
            public void onRefresh(RefreshLayout refreshlayout) {
                mSmartRefreshLayout.finishRefresh();
            }
        });

        // 顶部导航栏
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        TextView titleText = (TextView) findViewById(R.id.toolbar_title);
        toolbar.setTitle("");
        titleText.setText("完工图片");
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true); //设置返回键可用

        //头部信息栏
        txtBasketId = (TextView) findViewById(R.id.basket_id);
        txtBasketId.setText(basketinfo.getBasketId());
        txtBasketState = (TextView) findViewById(R.id.basket_state);
        txtBasketState.setTextColor(R.color.gray01);

        // GridView: 部件变量状态显示
        mPortionGv = (SmartGridView) findViewById(R.id.main_portion_gv);  // 获取资源控件
        initPortionList();    // 初始化列表内容
        mPortionAdapter = new PortionAdapter(BasketInstallInfoActivity.this,
                R.layout.item_portion, mPortions);  // 初始化适配器
        mPortionGv.setAdapter(mPortionAdapter);  // 装载适配器
        mPortionGv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position < 4){
                    // 单张图片
                    Intent intent = new Intent(BasketInstallInfoActivity.this, SingleImgUploadActivity.class);
                    intent.putExtra(PROJECT_ID, projectId);
                    intent.putExtra(BASKET_ID, basketinfo.getBasketId());
                    intent.putExtra(IMAGE_TYPE_ID, position);
                    startActivity(intent);
                }else{
                    // 左、右图片
                    Intent intent = new Intent(BasketInstallInfoActivity.this, MultiImgUploadActivity.class);
                    intent.putExtra(PROJECT_ID, projectId);
                    intent.putExtra(BASKET_ID, basketinfo.getBasketId());
                    intent.putExtra(IMAGE_TYPE_ID, position);
                    startActivity(intent);
                }
            }
        });

        //提交安装预检结果
        rlPreCheck = findViewById(R.id.rl_pre_check);
        rlPreCheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //跳转至填写预检信息页面
                Intent intent;
                intent = new Intent(BasketInstallInfoActivity.this, AreaAdminSumitReportActivity.class);
                intent.putExtra("projectId", projectId);
                intent.putExtra("basketId",  basketinfo.getBasketId());
                startActivityForResult(intent, UPLOAD_PRE_CHECK_RESULT);
            }
        });

        //上传终检证明--安检证书
        rlCertificateUpdate = findViewById(R.id.rl_certificate_update);
        rlCertificateUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //跳转至上传安检证书页面
                Intent intent;
                intent = new Intent(BasketInstallInfoActivity.this, UploadImageFTPActivity.class);
                intent.putExtra("projectId", projectId);
                intent.putExtra("basketId",  basketinfo.getBasketId());
                intent.putExtra(UPLOAD_IMAGE_TYPE, UPLOAD_CERTIFICATE_IMAGE);
                startActivityForResult(intent, UPLOAD_CERTIFICATE_IMAGE_RESULT);
            }
        });

        rlCertificate = findViewById(R.id.rl_certificate_info);
        rlCertificate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //跳转至查看安检证书页面
                Intent intent;
                intent = new Intent(BasketInstallInfoActivity.this, BasketHistoryInfoActivity.class);
                intent.putExtra("project_id", projectId);
                intent.putExtra("basket_id", basketinfo.getBasketId());
                startActivity(intent);
            }
        });

        updateUI();
    }


    private void updateUI(){
        //根据flag显示不同状态, flag: 0 进行中 1 已完成
        if(basketinfo.getFlag()==0){
            txtBasketState.setText("安装中");
            rlPreCheck.setVisibility(View.GONE);
            rlCertificateUpdate.setVisibility(View.GONE);
            rlCertificate.setVisibility(View.GONE);
        } else {
            txtBasketState.setText("已完成");
            if(basketinfo.getStateInPro()==12){
                txtBasketState.setText("安装预检中");
                rlPreCheck.setVisibility(View.VISIBLE);
                rlCertificateUpdate.setVisibility(View.GONE);
                rlCertificate.setVisibility(View.GONE);
            }
            else if(basketinfo.getStateInPro()==2){
                txtBasketState.setText("安装终检中");
                //安检证书未上传
                rlPreCheck.setVisibility(View.GONE);
                rlCertificateUpdate.setVisibility(View.VISIBLE);
                rlCertificate.setVisibility(View.GONE);
            }else if(basketinfo.getStateInPro()==21){
                txtBasketState.setText("安装已完成");
                //安检证书已上传
                rlPreCheck.setVisibility(View.GONE);
                rlCertificateUpdate.setVisibility(View.GONE);
                rlCertificate.setVisibility(View.VISIBLE);
            } else {
                txtBasketState.setText("已完成");
                //使用中or已停用
                rlPreCheck.setVisibility(View.GONE);
                rlCertificateUpdate.setVisibility(View.GONE);
                rlCertificate.setVisibility(View.VISIBLE);
            }
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

    /*
     * 后台通信
     */
    // 检查文件是否存在
    private void checkImageExist(){
        mRemotePath = "project/" + projectId + "/" + basketinfo.getBasketId();  // 图片上传地址
        new Thread() {
            public void run() {
                try {
                    // 上传文件
                    mFTPClient.openConnect();  // 建立连接
                    mFTPClient.uploadingInit(mRemotePath); // 上传文件初始化
                    if(mFTPClient.listCurrentFileNames()!=null ){
                        List<String>  filenames = mFTPClient.listCurrentFileNames();
                        for (int idx=0; idx<PortionMap.englishPortion.size(); idx++){
                            if (filenames.contains(PortionMap.englishPortion.get(idx) + ".jpg")) {
                                mPortions.get(idx).setState(1);
                                continue;
                            }
                            if (filenames.contains(PortionMap.englishPortion.get(idx) + "_left.jpg") &&
                                    filenames.contains(PortionMap.englishPortion.get(idx) + "_right.jpg")) {
                                mPortions.get(idx).setState(1);
                                continue;
                            }
                            mPortions.get(idx).setState(0);
                        }
                    }
                    mFTPClient.closeConnect();  // 关闭连接
                    mHandler.sendEmptyMessage(UPDATE_IMAGE_STATE_MSG);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    // FTP 初始化
    private void initFTPClient(){
        mFTPClient = new FTPUtil(AppConfig.FILE_SERVER_YBLIU_IP, AppConfig.FILE_SERVER_YBLIU_PORT,
                AppConfig.FILE_SERVER_USERNAME, AppConfig.FILE_SERVER_PASSWORD);
    }

    private void getIntentData(){
        Intent intent = getIntent();
        projectId = intent.getStringExtra("project_id");
        basketinfo = (MgBasketInstallInfo)intent.getSerializableExtra("basket_info");
    }

    /*
     * 生命周期
     */
    @Override
    public void onResume(){
        super.onResume();
        checkImageExist();
    }

    /*
     * 其它函数
     */
    // 初始化部件列表
    private void initPortionList(){
        mPortions = new ArrayList<>();
        Portion electricalBox = new Portion("电柜", R.mipmap.ic_electrical_box, 0);
        mPortions.add(electricalBox);
        Portion camera = new Portion("摄像头", R.mipmap.ic_camera, 0);
        mPortions.add(camera);
        Portion safeRope = new Portion("安全绳", R.mipmap.ic_safe_rope, 0);
        mPortions.add(safeRope);
        Portion cable = new Portion("电缆", R.mipmap.ic_cable, 0);
        mPortions.add(cable);
        Portion elevator = new Portion("提升机", R.mipmap.ic_elevator, 0);
        mPortions.add(elevator);
        Portion safeLock = new Portion("安全锁", R.mipmap.ic_safe_lock, 0);
        mPortions.add(safeLock);
        Portion mainSteel = new Portion("主钢丝", R.mipmap.ic_main_steel, 0);
        mPortions.add(mainSteel);
        Portion sideSteel = new Portion("副钢丝", R.mipmap.ic_side_steel, 0);
        mPortions.add(sideSteel);
        Portion heavyPunch = new Portion("重锤", R.mipmap.ic_heavy_punch, 0);
        mPortions.add(heavyPunch);
        Portion limitPosition = new Portion("上限位器", R.mipmap.ic_limit_position, 0);
        mPortions.add(limitPosition);
        Portion weighingMachine = new Portion("称重器", R.mipmap.ic_weighing_machine, 0);
        mPortions.add(weighingMachine);
        Portion bigArm = new Portion("大臂", R.mipmap.ic_big_arm, 0);
        mPortions.add(bigArm);
        Portion balanceWeight = new Portion("配重", R.mipmap.ic_balance_weight, 0);
        mPortions.add(balanceWeight);
    }

    /*
     * 处理Activity返回结果
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case UPLOAD_PRE_CHECK_RESULT:  // 提交预检结果返回值
                if(resultCode == RESULT_OK) {
                    basketinfo.setStateInPro(2);
                    mHandler.sendEmptyMessage(UPDATE_BASKET_STATE);
                    Intent intent = new Intent();
                    setResult(RESULT_OK, intent);
                }else if(resultCode == RESULT_FIRST_USER){
                    basketinfo.setStateInPro(1);
                    basketinfo.setFlag(0);
                    mHandler.sendEmptyMessage(UPDATE_BASKET_STATE);
                    Intent intent = new Intent();
                    setResult(RESULT_OK, intent);
            }
                break;
            case UPLOAD_CERTIFICATE_IMAGE_RESULT:  // 上传安监证书返回值
                if(resultCode == RESULT_OK) {
                    basketinfo.setStateInPro(21);
                    mHandler.sendEmptyMessage(UPDATE_BASKET_STATE);
                    Intent intent = new Intent();
                    setResult(RESULT_OK, intent);
                }
                break;
            default:
                break;
        }
    }


}
