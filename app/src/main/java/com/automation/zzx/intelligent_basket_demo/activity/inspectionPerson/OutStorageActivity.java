package com.automation.zzx.intelligent_basket_demo.activity.inspectionPerson;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;


import com.automation.zzx.intelligent_basket_demo.R;
import com.automation.zzx.intelligent_basket_demo.widget.searchview.ICallBack;
import com.automation.zzx.intelligent_basket_demo.widget.searchview.SearchView;
import com.automation.zzx.intelligent_basket_demo.widget.searchview.bCallBack;

/**
 * Created by pengchenghu on 2019/5/14.
 * Author Email: 15651851181@163.com
 * Describe: 巡检人员扫码出库界面
 * limits:
 */

public class OutStorageActivity extends AppCompatActivity {

    private final static String TAG = "OutStorageActivity";

    // 搜索框
    private SearchView searchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_out_storage);

        initWidget();
    }

    /*
     * 页面初始化
     */
    // 初始化控件
    private void initWidget(){
        // 搜索框
        searchView = (SearchView) findViewById(R.id.search_view);  // 绑定视图
        // 参数 = 搜索框输入的内容
        searchView.setOnClickSearch(new ICallBack() {
            @Override
            public void SearchAciton(String string) {

            }
        });
        // 设置点击返回按键后的操作（通过回调接口）
        searchView.setOnClickBack(new bCallBack() {
            @Override
            public void BackAciton() {
                finish();
            }
        });

    }
}
