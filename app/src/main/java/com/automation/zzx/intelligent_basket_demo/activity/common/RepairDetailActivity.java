package com.automation.zzx.intelligent_basket_demo.activity.common;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.automation.zzx.intelligent_basket_demo.R;
import com.automation.zzx.intelligent_basket_demo.entity.MessageInfo;
import com.automation.zzx.intelligent_basket_demo.fragment.proAdmin.ProAdminMessageFragment;
import com.automation.zzx.intelligent_basket_demo.fragment.rentAdmin.RentAdminMessageFragment;

import org.w3c.dom.Text;

/**
 * Created by pengchenghu on 2019/2/23.
 * Author Email: 15651851181@163.com
 * Describe: 吊篮报修详情
 * limits:
 */

public class RepairDetailActivity extends AppCompatActivity implements View.OnClickListener {

    private final static String TAG = "RepairDetailActivity";

    // 控件
    private TextView mProjectIdTv; // 项目ID
    private TextView mProjectNameTv; // 项目名称
    private TextView mBasketIdTv; // 吊篮编号
    private TextView mRepairDescribeTv; // 报修文字描述
    private GridView mRepairDescribeGv; // 报修图片描述
    private TextView mRepairDateTv; // 报修时间
    private LinearLayout mMaintainDescribeLy; //维修
    private TextView mMaintainDescribeTv; // 维修文字描述
    private GridView mMaintainDescribeGv; // 维修图片描述
    private TextView mMaintainDateTv; // 维修时间
    private Button mRepairBtn;  // 结束报修按钮

    // 页面传递信息
    private MessageInfo mRepairMessageInfo;

    // 后台请求必要信息
    private String mToken;
    private SharedPreferences mPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_repair_detail);

        //mRepairMessageInfo = (MessageInfo) getIntent().getExtras().get(ProAdminMessageFragment.REPAIR_MESSAGE_MSG);
        getUserToken();
        initWidgetResource();
        initDisplayView();  // 页面加载
    }

    /*
     * 页面初始化
     */
    // 控件初始化
    private void initWidgetResource(){
        // 顶部导航栏
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        TextView titleText = (TextView) findViewById(R.id.toolbar_title);
        toolbar.setTitle("");
        titleText.setText("报修详情");
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true); //设置返回键可用

        // 获取控件资源
        mProjectIdTv = (TextView) findViewById(R.id.project_id_textview);
        mProjectNameTv = (TextView) findViewById(R.id.project_name_textview);
        mBasketIdTv = (TextView) findViewById(R.id.basket_id_textview);
        mRepairDescribeTv = (TextView) findViewById(R.id.repair_describe_textview);
        mRepairDescribeGv = (GridView) findViewById(R.id.repair_describe_gridview);
        mRepairDateTv = (TextView) findViewById(R.id.repair_date_gridview);
        mMaintainDescribeLy = (LinearLayout) findViewById(R.id.maintain_describe_layout);
        mMaintainDescribeTv = (TextView) findViewById(R.id.maintain_describe_textview);
        mMaintainDescribeGv = (GridView) findViewById(R.id.maintain_describe_gridview);
        mMaintainDateTv = (TextView) findViewById(R.id.maintain_date_textview);
        mRepairBtn = (Button) findViewById(R.id.repair_over_button);
        mRepairBtn.setOnClickListener(this);
    }

    /*
     * 消息响应
     */
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
    // 控件点击响应
    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.repair_over_button:  // 结束报修按钮

                break;
        }
    }

    /*
     * 网络加载
     */
    //

    /*
     * 页面更新
     */
    // 加载页面数据
    private void initDisplayView(){
        mProjectIdTv.setText(mRepairMessageInfo.getmProjectId());
        mProjectNameTv.setText(mRepairMessageInfo.getmProjectName());
        mBasketIdTv.setText(mRepairMessageInfo.getmBasketId());
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
