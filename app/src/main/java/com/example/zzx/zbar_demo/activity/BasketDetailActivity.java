package com.example.zzx.zbar_demo.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.GridView;

import com.example.zzx.zbar_demo.R;
import com.example.zzx.zbar_demo.Adapter.FunctionAdapter;
import com.example.zzx.zbar_demo.entity.Function;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by pengchenghu on 2019/2/23.
 * Author Email: 15651851181@163.com
 * Describe: 选择测试功能
 */

public class BasketDetailActivity extends AppCompatActivity {
    // 控件声明
    private EditText mBasketIdEditView;  // 吊篮Id
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

        initWidgetResource();
    }

    // 资源句柄初始化及监听
    public void initWidgetResource(){
        // 获取控件句柄
        mBasketIdEditView = (EditText) findViewById(R.id.hangingbasket_id);
        mFunctionGridView = (GridView) findViewById(R.id.function_gridview);

        // 初始化功能列表
        initFunctionList();

        // 初始化适配器
        mFunctionAdapter = new FunctionAdapter(BasketDetailActivity.this,
                R.layout.item_function, mFunctions);

        // 装载适配器
        mFunctionGridView.setAdapter(mFunctionAdapter);

        // editview 监听
        mBasketIdEditView.addTextChangedListener(new TextWatcher() {  // editview 文本发生改变
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                mBasketId = s.toString();
            }
        });
        // GridView 监听
        mFunctionGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // do something
                Intent intent;
                switch(position){
                    case 0:  // 参数页面
                        intent = new Intent(BasketDetailActivity.this, ParameterActivity.class);
                        startActivity(intent);
                        break;
                    case 1:  // 图片页面
                        intent = new Intent(BasketDetailActivity.this, WorkPhotosActivity.class);
                        startActivity(intent);
                        break;
                    case 2:  // 视频页面
                        intent = new Intent(BasketDetailActivity.this, VideoMonitor2Activity.class);
                        startActivity(intent);
                        break;
                    default:break;
                }
            }
        });

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
    }
}
