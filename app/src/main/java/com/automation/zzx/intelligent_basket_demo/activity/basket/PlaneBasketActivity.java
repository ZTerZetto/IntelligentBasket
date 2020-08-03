package com.automation.zzx.intelligent_basket_demo.activity.basket;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.automation.zzx.intelligent_basket_demo.R;
import com.automation.zzx.intelligent_basket_demo.activity.loginRegist.LoginActivity;
import com.automation.zzx.intelligent_basket_demo.adapter.basket.BasketPlaneAdapter;
import com.automation.zzx.intelligent_basket_demo.entity.AppConfig;
import com.automation.zzx.intelligent_basket_demo.entity.PositionInfo;
import com.automation.zzx.intelligent_basket_demo.entity.UserInfo;
import com.automation.zzx.intelligent_basket_demo.utils.ToastUtil;
import com.automation.zzx.intelligent_basket_demo.utils.ftp.FTPUtil;
import com.automation.zzx.intelligent_basket_demo.utils.okhttp.BaseCallBack;
import com.automation.zzx.intelligent_basket_demo.utils.okhttp.BaseOkHttpClient;
import com.automation.zzx.intelligent_basket_demo.widget.image.SmartImageView;
import com.github.chrisbanes.photoview.PhotoView;
import com.scwang.smartrefresh.header.BezierCircleHeader;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.constant.SpinnerStyle;
import com.scwang.smartrefresh.layout.footer.BallPulseFooter;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;

import static java.lang.Math.sqrt;

public class PlaneBasketActivity extends AppCompatActivity implements View.OnClickListener {
    private final static int PULL_DOWN = 102;  // 下拉刷新
    private final static int UPDATE_IMAGE = 103; // 更新图片
    private final static int NO_MORE_IMAGE = 104; // 暂未上传平面图
    private final static int UPDATE_FAILED = 105; // 更新失败
    private final static int UPDATE_SUCESS = 106; // 更新成功

    private final static int SHOW_LOCATION_FAIL = 110;//无坐标信息时
    private final static int SHOW_LOCATION = 111; //显示坐标信息

    private double DISTANCE; // 点击范围阈值
    private final static double COMPENSATION_X = 0; // 水平位置偏移量30
    private final static double COMPENSATION_Y = 0; // 竖直位置偏移量34

    private List<String> urls  = new ArrayList<>(); // bitmap 位图
    private List<Bitmap> bitmaps  = new ArrayList<>(); // 文件Url
    private PositionInfo positionInfoA= new PositionInfo("A","A",-75686.174,32863.739);
    private PositionInfo positionInfoB= new PositionInfo("B","B",45847.787,126467.042);
    private List<PositionInfo> infoList = new ArrayList<>();
    private Map<String, PositionInfo> positionMap = new HashMap<>();

    //项目信息
    private String projectId;
    private String projectState;

    // 控件声明
    private RefreshLayout mSmartRefreshLayout;
    private SmartImageView mSmartIv;
    private TextView tvTitle;
    private ListView lvBasket;
    private BasketPlaneAdapter basketPlaneAdapter;

    private String buildId;

    //选择弹窗
    private AlertDialog mSelectProjectDialog;  // 选择角色弹窗
    private int currentSelected = 0; // 当前角色位置
    private int tmpSelected = 0; // 临时角色位置
    private String[] mBuildString ;  // 疑似点击楼号列表

    //屏幕适配
    private float dst_2[] = new float[2];

    // 用户登录信息相关
    private UserInfo mUserInfo;
    private String mProjectId;
    private String mToken;
    private SharedPreferences mPref;

    //FTP相关
    private FTPUtil mFTPClient;

    // 消息Handler
    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case PULL_DOWN:  // 下拉刷新
                    //displayWorkPhoto(1, mFileNameList.get(0));
                    break;
                case SHOW_LOCATION:  // 更新图片
                    //更新适配器
                    basketPlaneAdapter.notifyDataSetChanged();
                    break;

                case SHOW_LOCATION_FAIL:
                    //隐藏适配器
                    lvBasket.setVisibility(View.GONE);
                    tvTitle.setVisibility(View.GONE);

                case NO_MORE_IMAGE: // 尚无更多的图片
                    mSmartRefreshLayout.finishRefresh(500); // 刷新动画结束
                    mSmartRefreshLayout.finishLoadMore(500); // 加载动画结束
                    break;
            }
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plane_basket);

        //初始化坐标点信息
        //initPosition();

        // 初始化控件
        initWidgetResource();

        // 初始化FTP连接
        initFTPClient();

        // 初始化用户信息
        getUserInfo();

        Intent intent = getIntent();
        buildId = intent.getStringExtra("build_id");
        mProjectId = intent.getStringExtra("project_id");
        //获取intent数据
        if(buildId != null && mProjectId != null){
            String root_url = AppConfig.FILE_SERVER_YBLIU_PATH + "project" + File.separator +
                    mProjectId + File.separator + "plane_graph_"+ buildId +".jpg";
            mSmartIv.setImageUrl(root_url, R.mipmap.ic_empty);
            getPlaneFigureWithInfo();
        }

        // 获取相关图片
        //displayPlanePhoto(0, "");

    }

   /* private void initPosition(){
        PositionInfo positionInfo1= new PositionInfo("1","20191112",-30053.379,63739.285);
        infoList.add(positionInfo1);
        PositionInfo positionInfo2= new PositionInfo("2","20191112",-30053.379,43249.445);
        infoList.add(positionInfo2);
        PositionInfo positionInfo3= new PositionInfo("3","20191112",-208.307,65478.735);
        infoList.add(positionInfo3);
        PositionInfo positionInfo4= new PositionInfo("4","20191112",-129.566,73575.676);
        infoList.add(positionInfo4);
        PositionInfo positionInfo5= new PositionInfo("5","20191112",27000.872,75108.255);
        infoList.add(positionInfo5);
        PositionInfo positionInfo6= new PositionInfo("6","20191112",37196.703,84486.968);
        infoList.add(positionInfo6);
        PositionInfo positionInfo7= new PositionInfo("7","20191112",37407.298,105384.505);
        infoList.add(positionInfo7);
        PositionInfo positionInfo8= new PositionInfo("8","20191112",26941.406,115620.630);
        infoList.add(positionInfo8);
        PositionInfo positionInfo9= new PositionInfo("9","20191112",3027.207,115108.735);
        infoList.add(positionInfo9);
        PositionInfo positionInfo10= new PositionInfo("10","20191112",-21230.465,115518.278);
        infoList.add(positionInfo10);
        PositionInfo positionInfo11= new PositionInfo("11","20191112",-58505.545,115657.204);
        infoList.add(positionInfo11);
        PositionInfo positionInfo12= new PositionInfo("12","20191112",-67864.128,106087.881);
        infoList.add(positionInfo12);
        PositionInfo positionInfo13= new PositionInfo("13","20191112",-67930.382,84659.387);
        infoList.add(positionInfo13);
        PositionInfo positionInfo14= new PositionInfo("14","20191112",-57878.567,75146.626);
        infoList.add(positionInfo14);
        PositionInfo positionInfo15= new PositionInfo("15","20191112",-25181.852,89459.565);
        infoList.add(positionInfo15);
        PositionInfo positionInfo16= new PositionInfo("16","20191112",7611.330,75545.110);
        infoList.add(positionInfo16);

        for(int i = 0; i < infoList.size();i++){
            positionMap.put(infoList.get(i).getId(),infoList.get(i));
        }
    }*/

    private void getPlaneFigureWithInfo() {

        //从后台获取平面图信息
        BaseOkHttpClient.newBuilder()
                .addHeader("Authorization", mToken)
                .addParam("projectId", mProjectId)
                .addParam("buildingNum", buildId)
                .addParam("type", 2) //请求某栋楼的平面图信息
                .get()
                .url(AppConfig.GET_PLANE_GRAPH_INFO)
                .build()
                .enqueue(new BaseCallBack() {
                    @Override
                    public void onSuccess(Object o) {
                        String response = o.toString();
                        JSONObject jsonObject = JSON.parseObject(response);
                        Boolean isLogin = jsonObject.getBoolean("isLogin");
                        if(isLogin){
                            JSONArray data = jsonObject.getJSONArray("planeGraphInfo");
                            parseGraghInfo(data);
                        }
                    }

                    @Override
                    public void onError(int code) {
                        switch(code){
                            case 401:
                                ToastUtil.showToastTips(PlaneBasketActivity.this, "登陆已过期，请重新登录");
                                startActivity(new Intent(PlaneBasketActivity.this, LoginActivity.class));
                                PlaneBasketActivity.this.finish();
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

    private void parseGraghInfo(JSONArray data){
        infoList.clear();
        if(data.size() != 0){
            for(int index=0; index <data.size();index++) {
                JSONObject basketObj = (JSONObject)data.get(index);
                String buildId = basketObj.getString("device_id");
                if(buildId.equals("A")){
                    //分隔符得到XY坐标
                    String location =  basketObj.getString("location");
                    String[] locations = location.split(",");
                    Double location_x = Double.valueOf(locations[0]);
                    double location_y = Double.valueOf(locations[1]);
                    projectId = basketObj.getString("project_id");
                    positionInfoA = new PositionInfo(buildId, "", location_x,location_y);
                }else if(buildId.equals("B")){
                    //分隔符得到XY坐标
                    String location =  basketObj.getString("location");
                    String[] locations = location.split(",");
                    Double location_x = Double.valueOf(locations[0]);
                    double location_y = Double.valueOf(locations[1]);
                    projectId = basketObj.getString("project_id");
                    positionInfoB = new PositionInfo(buildId, "", location_x,location_y);
                }else{

                    //分隔符得到XY坐标
                    String location =  basketObj.getString("location");
                    String basketId =  basketObj.getString("location_id");
                    String[] locations = location.split(",");
                    Double location_x = Double.valueOf(locations[0]);
                    double location_y = Double.valueOf(locations[1]);
                    projectId = basketObj.getString("project_id");
                    projectState = basketObj.getString("project_state");
                    PositionInfo positionInfo = new PositionInfo(basketId, buildId, location_x,location_y);
                    positionInfo.setBasketState(basketObj.getString("basket_state"));
                    infoList.add(positionInfo);
                }
            }
            for(int i = 0; i < infoList.size();i++){
                positionMap.put(infoList.get(i).getId(),infoList.get(i));
            }
            mHandler.sendEmptyMessage(SHOW_LOCATION);
        }else{
            mHandler.sendEmptyMessage(SHOW_LOCATION_FAIL);
        }
    }

    /*
     * 控件初始化
     */
    private void initWidgetResource() {
        // 上拉、下拉刷新
        mSmartRefreshLayout = (SmartRefreshLayout) findViewById(R.id.smart_refresh_layout);
        mSmartRefreshLayout.setRefreshHeader(  //设置 Header 为 贝塞尔雷达 样式
                new BezierCircleHeader(this));
        mSmartRefreshLayout.setRefreshFooter(  //设置 Footer 为 球脉冲 样式
                new BallPulseFooter(this).setSpinnerStyle(SpinnerStyle.Scale));
        mSmartRefreshLayout.setPrimaryColorsId(R.color.smart_loading_background_color);
        mSmartRefreshLayout.setOnRefreshListener(new OnRefreshListener() { // 添加下拉刷新监听
            @Override
            public void onRefresh(RefreshLayout refreshlayout) {
                if(urls.size() > 0)
                    mHandler.sendEmptyMessage(PULL_DOWN);
            }
        });

        // 顶部导航栏
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        TextView titleText = (TextView) findViewById(R.id.toolbar_title);
        toolbar.setTitle("吊篮平面图");

        titleText.setText("");
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true); //设置返回键可用

        mSmartIv = findViewById(R.id.general_layout);
        mSmartIv.setOnTouchListener(onTouchListener);
        lvBasket = findViewById(R.id.lv_basket);
        tvTitle = findViewById(R.id.tv_title);
        basketPlaneAdapter = new BasketPlaneAdapter(this,R.layout.item_position_list,infoList);
        lvBasket.setAdapter(basketPlaneAdapter);

        //消息响应
        lvBasket.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(PlaneBasketActivity.this, infoList.get(position).getId() + "号吊篮",
                        Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(PlaneBasketActivity.this, BasketDetailActivity.class);
                intent.putExtra("basket_id", infoList.get(position).getId());
                intent.putExtra("project_state",projectState);
                intent.putExtra("project_id",projectId);
                intent.putExtra("basket_state", infoList.get(position).getBasketState());
                startActivity(intent);
            }
        });
    }

    // FTP初始化
    private void initFTPClient(){
        mFTPClient = new FTPUtil(AppConfig.FILE_SERVER_YBLIU_IP, AppConfig.FILE_SERVER_YBLIU_PORT,
                AppConfig.FILE_SERVER_USERNAME, AppConfig.FILE_SERVER_PASSWORD);
    }

    /*
     * 解析用户信息
     */
    // 获取用户数据
    private void getUserInfo(){
        // 从本地获取数据
        mPref = PreferenceManager.getDefaultSharedPreferences(this);
        mUserInfo = new UserInfo();
        mUserInfo.setUserId(mPref.getString("userId", ""));
        mUserInfo.setUserPhone(mPref.getString("userPhone", ""));
        mUserInfo.setUserRole(mPref.getString("userRole", ""));
        mToken = mPref.getString("loginToken","");
        mProjectId = mPref.getString("projectId","");
    }

    // 获取要显示图片的url
    private void displayPlanePhoto(final int direction, final String filename){
        /*new Thread(){
            public void run(){
                try{
                    mFTPClient.openConnect();  // 建立连接
                    mFTPClient.downloadingInit(REMOTE_WORK_PHOTO_PATH + "/" + mBasketId);  // 切换工作环境
                    List<String> newFileNames = mFTPClient.getDownloadFileName(direction, filename);
                    if(newFileNames.size() == 0){  // 没有更多的图片
                        mHandler.sendEmptyMessage(NO_MORE_IMAGE);
                    }else {   // 图片更新
                        Message msg = new Message();  // 通知页面更新
                        msg.what = UPDATE_IMAGE;
                        msg.arg1 = direction;
                        msg.obj = newFileNames;
                        mHandler.sendMessage(msg);
                    }
                    mFTPClient.closeConnect();  // 关闭连接
                    mHandler.sendEmptyMessage(UPDATE_SUCESS);
                }catch(IOException e){
                    e.printStackTrace();
                    mHandler.sendEmptyMessage(UPDATE_FAILED);
                }
            }
        }.start();*/
    }

    /*
     * 点击事件
     * */
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

    //事件监听方法
    private View.OnTouchListener onTouchListener = new View.OnTouchListener() {

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    //转换控件边界
                    //Matrix matrix = new Matrix();
                    //Matrix inverse = mSmartIv.getImageMatrix();
                    Matrix matrix = new Matrix(mSmartIv.getImageMatrix());
                    Matrix inverse = new Matrix();
                    matrix.invert(inverse);
                    inverse.mapPoints(dst_2, new float[]{mSmartIv.getWidth(), mSmartIv.getHeight()});
                    DISTANCE = 0.05*(dst_2[0]+dst_2[1]);

                    List<PositionInfo> positionInfos = areaJudge(v, event);
                    float x = event.getX();
                    float y = event.getY();
                    // 目标点的坐标
                    float dst[] = new float[2];
                    // 获取到ImageView的matrix
                    Matrix imageMatrix = mSmartIv.getImageMatrix();
                    // 创建一个逆矩阵
                    Matrix inverseMatrix = new Matrix();

                    // 求逆，逆矩阵被赋值
                    imageMatrix.invert(inverseMatrix);
                    // 通过逆矩阵映射得到目标点 dst 的值
                    inverseMatrix.mapPoints(dst, new float[]{x, y});

                    Toast.makeText(PlaneBasketActivity.this, "("+dst[0]+","+dst[1]+")",
                            Toast.LENGTH_SHORT).show();
                    if(positionInfos != null) {
                        if(positionInfos.size() == 1) {
                            Toast.makeText(PlaneBasketActivity.this, positionInfos.get(0).getId() + "号吊篮",
                                    Toast.LENGTH_SHORT).show();
                            /*Intent intent = new Intent(PlaneBasketActivity.this, PlaneBasketActivity.class);
                            intent.putExtra("build_id", positionInfos.get(0).getId());
                            startActivity(intent);*/
                        } else if(positionInfos.size() > 1) {
                            mBuildString =  new String[positionInfos.size()];
                            for(int i=0;i<positionInfos.size();i++){
                                mBuildString[i] = positionInfos.get(i).getId();
                            }
                            showSelectDialog();

                        }

                    }
                    break;
                default:
                    break;
            }
            return false;
        }
    };

    private  List<PositionInfo> areaJudge(View v, MotionEvent event){
        List<PositionInfo> positionInfos = new ArrayList<>();
        float x = event.getX();
        float y = event.getY();
        // 目标点的坐标
        float dst[] = new float[2];
        // 获取到ImageView的matrix
        Matrix imageMatrix = mSmartIv.getImageMatrix();
        // 创建一个逆矩阵
        Matrix inverseMatrix = new Matrix();
        // 求逆，逆矩阵被赋值
        imageMatrix.invert(inverseMatrix);
        // 通过逆矩阵映射得到目标点 dst 的值
        inverseMatrix.mapPoints(dst, new float[]{x, y});
        // 判断dstX, dstY在Bitmap上的位置即可
        for(int i=0;i<infoList.size();i++){
            double distance_x = dst[0]-parseToScreen(infoList.get(i)).getPosition_X();
            double distance_y = dst[1]-parseToScreen(infoList.get(i)).getPosition_Y();
            double distance = sqrt(distance_x*distance_x+distance_y*distance_y);
            if(distance < DISTANCE) positionInfos.add(infoList.get(i));
        }
        return positionInfos;
    }

    //坐标转换至空间像素
    private PositionInfo parseToScreen(PositionInfo positionInfo){
        PositionInfo mPosition;
        Double x = (positionInfo.getPosition_X()-positionInfoA.getPosition_X())
                /(positionInfoB.getPosition_X()-positionInfoA.getPosition_X());
        Double y = (positionInfo.getPosition_Y()-positionInfoB.getPosition_Y())
                /(positionInfoA.getPosition_Y()-positionInfoB.getPosition_Y());
        mPosition = new PositionInfo(positionInfo.getId(),positionInfo.getItemId(),
                (mSmartIv.getRight()-mSmartIv.getLeft())*x+COMPENSATION_X,
                (mSmartIv.getBottom()-mSmartIv.getTop())*y+COMPENSATION_Y);
        return mPosition;
    }


    // 弹出选择框
    public void showSelectDialog(){
        final AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
        alertBuilder.setTitle("选择吊篮编号");
        alertBuilder.setSingleChoiceItems(mBuildString, currentSelected, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int position) {
                tmpSelected = position;
            }
        });

        alertBuilder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                currentSelected = tmpSelected;
               /* //跳转至所选择的楼号主界面
                Toast.makeText(PlaneBasketActivity.this, mBuildString[currentSelected] + "号吊篮",
                        Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(PlaneBasketActivity.this, PlaneBasketActivity.class);
                intent.putExtra("build_id", mBuildString[currentSelected]);
                startActivity(intent);*/
                mSelectProjectDialog.dismiss();
            }
        });

        alertBuilder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                mSelectProjectDialog.dismiss();
            }
        });

        mSelectProjectDialog = alertBuilder.create();
        mSelectProjectDialog.show();
    }
}
