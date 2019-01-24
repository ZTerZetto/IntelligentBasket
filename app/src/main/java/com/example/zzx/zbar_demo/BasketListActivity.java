package com.example.zzx.zbar_demo;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.example.zzx.zbar_demo.Adapter.BasketAdapter;
import com.example.zzx.zbar_demo.VideoPlay.VideoPlayActivity;
import com.example.zzx.zbar_demo.entity.BasketInfo;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.List;

public class BasketListActivity extends AppCompatActivity {

    private ListView mLv;
    private BasketAdapter adapter;
    private List<BasketInfo> basketInfoArrayList = new ArrayList<>();
    private Context mContext = BasketListActivity.this;

    private TextView txtSearch;
    private Button btnSearch;
    private TextView txtResult;

    public static final int HTTP_SUCCESS = 1;


/*    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            switch(msg.what) {
                case HTTP_SUCCESS:
                    Log.d("search", "success");
                    String jso = msg.obj.toString();
                    basketInfoArrayList = JSON.parseArray(jso, BasketInfo.class);

                    if(null != basketInfoArrayList) {
                        adapter = new BasketAdapter(mContext,R.layout.item_basket,basketInfoArrayList);
                        mLv.setAdapter(adapter);
                        mLv.setVisibility(View.VISIBLE);
                    }
                    break;
                default:
                    break;
            }
        }
    };*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_basket_list);

        mLv = findViewById(R.id.list_view);
        txtSearch = findViewById(R.id.txt_input_search);
        btnSearch = findViewById(R.id.search_button);
        txtResult = findViewById(R.id.txt_search_result);

        initList();


        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text = txtSearch.getText().toString();
                List<BasketInfo> arrayList = new ArrayList<>();
                for (int i=0;i<basketInfoArrayList.size();i++){
                    BasketInfo basketInfo = basketInfoArrayList.get(i);
                    if(basketInfo.getBasketId().equals(text)){
                        arrayList.add(basketInfo);
                    }
                }
                if(arrayList.isEmpty()) {
                    mLv.setVisibility(View.INVISIBLE);
                    txtResult.setVisibility(View.VISIBLE);
                } else {
                    basketInfoArrayList.clear();
                    basketInfoArrayList = arrayList;
                    showList(basketInfoArrayList);
                }
            }
        });

        //listview点击事件
        mLv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                //跳转至缩略图显示
                Intent intent = new Intent(mContext, VideoPlayActivity.class);
                //intent.putExtra("basket_id", basketInfoArrayList.get(i).getBasketId());
                startActivity(intent);
            }
        });

    }

    private void showList(List<BasketInfo> arrayList) {
        adapter = new BasketAdapter(mContext,R.layout.item_basket,arrayList);
        mLv.setAdapter(adapter);
        mLv.setVisibility(View.VISIBLE);
        txtResult.setVisibility(View.INVISIBLE);
    }

    private void initList() {
        String basketId = "111111";
        String workerId = "WK239548";
        BasketInfo basketInfo;
        for(int i=0; i<5 ;i++){
            basketInfo = new BasketInfo(basketId, "WORKING", workerId+i);
            basketInfoArrayList.add(basketInfo);
            basketInfo = new BasketInfo(basketId, "RESTING", workerId+i);
            basketInfoArrayList.add(basketInfo);
        }
        showList(basketInfoArrayList);
    }
}
