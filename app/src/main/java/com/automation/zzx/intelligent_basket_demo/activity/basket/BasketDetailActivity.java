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
    private EditText mSetBasketIdEv;
    private GridView mFunctionGridView;  // 功能测试

    // function gridview
    private List<Function> mFunctions;  // 功能列表
    private FunctionAdapter mFunctionAdapter;  // 功能适配器

    // others
    private String mBasketId;  // 吊篮id

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
    }

    // 资源句柄初始化及监听
    public void initWidgetResource(){
        // 获取控件句柄
        mSetBasketIdEv = (EditText) findViewById(R.id.basket_id_ev);
        mSetBasketIdEv.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                mBasketId = s.toString();
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mBasketId = s.toString();
            }

            @Override
            public void afterTextChanged(Editable s) {
                mBasketId = s.toString();
            }
        });
        mFunctionGridView = (GridView) findViewById(R.id.function_gridview);

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
                    case 0:  // 参数页面
                        intent = new Intent(BasketDetailActivity.this, BasketParameterActivity.class);
                        intent.putExtra(BASKET_ID, mBasketId);
                        startActivity(intent);
                        break;
                    case 1:  // 图片页面
                        intent = new Intent(BasketDetailActivity.this, BasketPhotoActivity.class);
                        intent.putExtra(BASKET_ID, mBasketId);
                        startActivity(intent);
                        break;
                    case 2:  // 视频页面
                        intent = new Intent(BasketDetailActivity.this, BasketVideoActivity.class);
                        intent.putExtra(BASKET_ID, mBasketId);
                        startActivity(intent);
                        break;
                    case 3:  // 设置页面
                        intent = new Intent(BasketDetailActivity.this, BasketSettleActivity.class);
                        intent.putExtra(BASKET_ID, mBasketId);
                        startActivity(intent);
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
        Function parameter = new Function("参数", R.mipmap.ic_parameter_192);
        //Function parameter = new Function("参数", R.mipmap.ic_parameter);
        mFunctions.add(parameter);
        Function image = new Function("图片", R.mipmap.ic_image_192);
        //Function image = new Function("图片", R.mipmap.ic_image);
        mFunctions.add(image);
        Function video = new Function("监控", R.mipmap.ic_video_192);
        //Function video = new Function("监控", R.mipmap.ic_video);
        mFunctions.add(video);
        Function setting = new Function("设置", R.mipmap.ic_setting_192);
        //Function video = new Function("监控", R.mipmap.ic_video);
        mFunctions.add(setting);
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

}
