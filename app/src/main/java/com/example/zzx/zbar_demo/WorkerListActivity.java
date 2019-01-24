package com.example.zzx.zbar_demo;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.os.Handler;
import android.os.Message;
import com.alibaba.fastjson.JSON;
import com.example.zzx.zbar_demo.Adapter.BasketAdapter;
import com.example.zzx.zbar_demo.Adapter.WorkerAdapter;
import com.example.zzx.zbar_demo.entity.BasketInfo;
import com.example.zzx.zbar_demo.entity.UserInfo;

import java.util.ArrayList;
import java.util.List;

public class WorkerListActivity extends AppCompatActivity {

    private ListView mLv;
    private WorkerAdapter adapter;
    private List<UserInfo> userInfoArrayList = new ArrayList<>();
    private Context mContext = WorkerListActivity.this;
    public static final int HTTP_SUCCESS = 1;


/*    @SuppressLint("HandlerLeak")
        private Handler handler = new Handler() {
            public void handleMessage(Message msg) {
                switch(msg.what) {
                    case HTTP_SUCCESS:
                        Log.d("search", "success");
                        String jso = msg.obj.toString();
                        userInfoArrayList = JSON.parseArray(jso, UserInfo.class);

                        if(null != userInfoArrayList) {
                            adapter = new WorkerAdapter(mContext,R.layout.item_worker,userInfoArrayList);
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
        setContentView(R.layout.activity_worker_list);
        mLv = findViewById(R.id.list_view);

        initList();

        adapter = new WorkerAdapter(mContext,R.layout.item_worker,userInfoArrayList);
        mLv.setAdapter(adapter);
        mLv.setVisibility(View.VISIBLE);
    }

    private void initList() {
        String workerName = "张三三";
        String workerId = "WK239548";
        UserInfo workerInfo;
        for(int i=0; i<5 ;i++){
            workerInfo = new UserInfo(workerId+i,workerName);
            userInfoArrayList.add(workerInfo);
            workerInfo = new UserInfo(workerId+i,workerName);
            userInfoArrayList.add(workerInfo);
        }
    }

}
