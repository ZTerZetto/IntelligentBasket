package com.example.zzx.zbar_demo.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.zzx.zbar_demo.R;
import com.example.zzx.zbar_demo.Adapter.VarSwitchAdapter;
import com.example.zzx.zbar_demo.entity.VarSwitch;
import com.example.zzx.zbar_demo.widget.SmartGridView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by pengchenghu on 2019/2/23.
 * Author Email: 15651851181@163.com
 * Describe: 吊篮参数监视活动
 * limits: 从服务器抓取数据，动态显示数据
 */

public class ParameterActivity extends AppCompatActivity {

    // 控件声明
    private SmartGridView mVarSwitchGv;  // 开关变量网格控件
    private ImageView mMotorLeft;  // 左电机
    private ImageView mMotorRight; // 右电机
    private ImageView mVfd;         // 变频器
    private ImageView mContactorLeft; // 左接触器
    private ImageView mContactorRight; // 右接触器
    private TextView mVfdCurrent;   // 变频器电流
    private TextView mClinometerDegree; // 倾斜仪角度
    private TextView mLocationMsg;  // 位置信息

    // var switch gridview
    private List<VarSwitch> mVarSwitches;  // 开关变量列表
    private VarSwitchAdapter mVarSwitchAdapter;  // 开关变量适配器

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parameter);

        initWidgetResource();
    }

    // 控件初始化
    private void initWidgetResource(){
        // 顶部导航栏
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        TextView titleText = (TextView) findViewById(R.id.toolbar_title);
        toolbar.setTitle("");
        titleText.setText(getString(R.string.parameter_title));
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true); //设置返回键可用

        // GridView:开关变量状态显示
        mVarSwitchGv = (SmartGridView) findViewById(R.id.var_switch_gv);  // 获取资源控件
        initVarSwitchList();    // 初始化列表内容
        mVarSwitchAdapter = new VarSwitchAdapter(ParameterActivity.this,
                R.layout.item_var_switch, mVarSwitches);  // 初始化适配器
        mVarSwitchGv.setAdapter(mVarSwitchAdapter);  // 装载适配器
        mVarSwitchGv.setOnItemClickListener(new AdapterView.OnItemClickListener() {  // 消息响应
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            }
        });

        // 控制输入
        mMotorLeft = (ImageView) findViewById(R.id.motor_left);
        mMotorRight = (ImageView) findViewById(R.id.motor_right);

        // 控制输出
        mVfd = (ImageView) findViewById(R.id.vfd);
        mContactorLeft = (ImageView) findViewById(R.id.contactor_left);
        mContactorRight = (ImageView) findViewById(R.id.contactor_right);

        // 其它数据
        mVfdCurrent = (TextView) findViewById(R.id.vfd_current_tv);
        mClinometerDegree = (TextView) findViewById(R.id.clinometer_degree_tv);
        mLocationMsg = (TextView) findViewById(R.id.location_msg_tv);

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
       * 其它函数
     */
    // 初始化开关列表
    private void initVarSwitchList(){
        mVarSwitches = new ArrayList<>();
        VarSwitch mainRope = new VarSwitch("主绳", R.mipmap.ic_main_rope, true);
        mVarSwitches.add(mainRope);
        VarSwitch viceRope = new VarSwitch("副绳", R.mipmap.ic_vice_rope, true);
        mVarSwitches.add(viceRope);
        VarSwitch cable = new VarSwitch("电缆", R.mipmap.ic_cable, true);
        mVarSwitches.add(cable);
        VarSwitch limit = new VarSwitch("限位器", R.mipmap.ic_limit, true);
        mVarSwitches.add(limit);
        VarSwitch vfd = new VarSwitch("变频器", R.mipmap.ic_vfd, false);
        mVarSwitches.add(vfd);
        VarSwitch plc = new VarSwitch("PLC", R.mipmap.ic_plc, false);
        mVarSwitches.add(plc);
        VarSwitch cloudBox = new VarSwitch("云盒", R.mipmap.ic_cloud_box, false);
        mVarSwitches.add(cloudBox);
        VarSwitch buzzer = new VarSwitch("蜂鸣器", R.mipmap.ic_buzzer, false);
        mVarSwitches.add(buzzer);
    }

}
