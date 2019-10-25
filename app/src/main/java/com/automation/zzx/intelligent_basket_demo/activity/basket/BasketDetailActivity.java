package com.automation.zzx.intelligent_basket_demo.activity.basket;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import com.automation.zzx.intelligent_basket_demo.R;
import com.automation.zzx.intelligent_basket_demo.adapter.basket.FunctionAdapter;
import com.automation.zzx.intelligent_basket_demo.entity.Function;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by pengchenghu on 2019/2/23.
 * Author Email: 15651851181@163.com
 * Describe: 选择测试功能
 */

public class BasketDetailActivity extends AppCompatActivity implements View.OnClickListener {

    // 页面跳转消息
    public final static String BASKET_ID = "basket_id";

    // 控件声明
    //private EditText mSetBasketIdEv;
    private GridView mFunctionGridView;  // 功能测试

    private TextView txtBasketId;  //吊篮编号
    //private TextView txtWorkerName;  //负责人姓名

    // function gridview
    private List<Function> mFunctions;  // 功能列表
    private FunctionAdapter mFunctionAdapter;  // 功能适配器

    // others
    private String mProjectId;  //項目ID
    private String mBasketId;  // 吊篮id
    //private String mPrincipal; //吊篮负责人姓名

    public final static String UPLOAD_PROJECT_ID  = "project_id"; // 項目ID
    public final static String UPLOAD_BASKET_ID = "basket_id"; // 報修操作
    public final static String UPLOAD_IMAGE_TEXT_TYPE  = "uploadImageTextType"; // 上传图片的类型
    public final static String UPLOAD_BASKET_REPAIR_IMAGE = "basketRepair"; // 報修操作

    //返回結果
    public final static int UPLOAD_BASKET_REPAIR_RESULT = 101; //報修返回



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

        getBaseInfoFromPred();
        initWidgetResource();
    }

    // 項目和吊籃信息获取
    public void getBaseInfoFromPred() {
        Intent intent = getIntent();
        mProjectId = intent.getStringExtra("project_id");
        mBasketId = intent.getStringExtra("basket_id");
        //mPrincipal = intent.getStringExtra("principal_name");
    }
    // 资源句柄初始化及监听
    public void initWidgetResource(){
        // 获取控件句柄
        mFunctionGridView = (GridView) findViewById(R.id.function_gridview);

        //初始化控件并显示内容
        txtBasketId = (TextView) findViewById(R.id.basket_id);
        txtBasketId.setText(mBasketId);
        //txtWorkerName = (TextView) findViewById(R.id.basket_worker_id);
       // txtWorkerName.setText(mPrincipal);

        // 初始化功能列表
        initFunctionList();
        mFunctionAdapter = new FunctionAdapter(BasketDetailActivity.this,
                R.layout.item_function, mFunctions); // 初始化适配器
        mFunctionGridView.setAdapter(mFunctionAdapter); // 装载适配器
        mFunctionGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() { // GridView 监听
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // do something
                Intent intent;
                switch(position){
                    case 0:  // 实时参数
                        intent = new Intent(BasketDetailActivity.this, BasketParameterActivity.class);
                        intent.putExtra(BASKET_ID, mBasketId);
                        startActivity(intent);
                        break;
                    case 1:  // 工况图片
                        intent = new Intent(BasketDetailActivity.this, BasketPhotoActivity.class);
                        intent.putExtra(BASKET_ID, mBasketId);
                        startActivity(intent);
                        break;
                    case 2:  // 监控视频
                        intent = new Intent(BasketDetailActivity.this, BasketVideoActivity.class);
                        intent.putExtra(BASKET_ID, mBasketId);
                        startActivity(intent);
                        break;
                    case 3:  // 参数设置
                        intent = new Intent(BasketDetailActivity.this, BasketSettleActivity.class);
                        intent.putExtra(BASKET_ID, mBasketId);
                        startActivity(intent);
                        break;
                    case 4:  // 设备绑定
                        intent = new Intent(BasketDetailActivity.this, BasketDeviceActivity.class);
                        intent.putExtra(UPLOAD_BASKET_ID, mBasketId);
                        startActivity(intent);
                        break;
                    case 5:  // 历史图片
                        intent = new Intent(BasketDetailActivity.this, BasketHistoryInfoActivity.class);
                        intent.putExtra(UPLOAD_BASKET_ID, mBasketId);
                        startActivity(intent);
                        break;

                    case 6:  // 申请报修
                        intent = new Intent(BasketDetailActivity.this, BasketRepairActivity.class);
                        intent.putExtra(UPLOAD_PROJECT_ID, mProjectId);
                        intent.putExtra(UPLOAD_BASKET_ID, mBasketId);
                        intent.putExtra(UPLOAD_IMAGE_TEXT_TYPE, UPLOAD_BASKET_REPAIR_IMAGE);
                        startActivityForResult(intent, UPLOAD_BASKET_REPAIR_RESULT);
                        break;
                    default:break;
                }
            }
        });
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
        mFunctions = new ArrayList<>();
        Function parameter = new Function("实时参数", R.mipmap.ic_parameter_192);
        //Function parameter = new Function("参数", R.mipmap.ic_parameter);
        mFunctions.add(parameter);
        Function image = new Function("工况图片", R.mipmap.ic_image_192);
        //Function image = new Function("图片", R.mipmap.ic_image);
        mFunctions.add(image);
        Function video = new Function("监控视频", R.mipmap.ic_video_192);
        //Function video = new Function("监控", R.mipmap.ic_video);
        mFunctions.add(video);
        Function setting = new Function("参数设置", R.mipmap.ic_setting_192);
        //Function video = new Function("监控", R.mipmap.ic_video);
        mFunctions.add(setting);
        Function equipment = new Function("设备绑定", R.mipmap.ic_device_192);
        //Function video = new Function("监控", R.mipmap.ic_video);
        mFunctions.add(equipment);
        Function history = new Function("相关信息", R.mipmap.ic_history_info_192);
        //Function video = new Function("监控", R.mipmap.ic_video);
        mFunctions.add(history);
        Function repair = new Function("申请报修", R.mipmap.ic_repair_192);
        //Function video = new Function("监控", R.mipmap.ic_video);
        mFunctions.add(repair);
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
