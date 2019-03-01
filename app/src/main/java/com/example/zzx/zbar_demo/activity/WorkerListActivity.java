package com.example.zzx.zbar_demo.activity;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.example.zzx.zbar_demo.Adapter.WorkerAdapter;
import com.example.zzx.zbar_demo.R;
import com.example.zzx.zbar_demo.VideoPlay.VideoPlayActivity;
import com.example.zzx.zbar_demo.entity.UserInfo;

import java.util.ArrayList;
import java.util.List;

public class WorkerListActivity extends AppCompatActivity {

    private ListView mLv;
    private WorkerAdapter adapter;
    private List<UserInfo> userInfoArrayList = new ArrayList<>();
    private Context mContext = WorkerListActivity.this;

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
        txtSearch = findViewById(R.id.txt_input_search);
        btnSearch = findViewById(R.id.search_button);
        txtResult = findViewById(R.id.txt_search_result);
        initList();

        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text = txtSearch.getText().toString();
                if (text.isEmpty()) {
                    showList(userInfoArrayList);
                } else {
                    List<UserInfo> arrayList = new ArrayList<>();
                    for (int i = 0; i < userInfoArrayList.size(); i++) {
                        UserInfo userInfo = userInfoArrayList.get(i);
                        if (userInfo.getUserId().equals(text)) {
                            arrayList.add(userInfo);
                        } else if (userInfo.getUserName().equals(text)){
                            arrayList.add(userInfo);
                        }
                    }
                    if (arrayList.isEmpty()) {
                        mLv.setVisibility(View.INVISIBLE);
                        txtResult.setVisibility(View.VISIBLE);
                    } else {
                        userInfoArrayList.clear();
                        userInfoArrayList = arrayList;
                        showList(userInfoArrayList);
                    }
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

    private void showList(List<UserInfo> arrayList) {
        adapter = new WorkerAdapter(mContext,R.layout.item_worker,arrayList);
        mLv.setAdapter(adapter);
        mLv.setVisibility(View.VISIBLE);
        txtResult.setVisibility(View.INVISIBLE);
    }

    private void initList() {
        String workerName = "张三三";
        String workerId = "WK239548";
        UserInfo workerInfo;
        for(int i=0; i<10 ;i++){
            workerInfo = new UserInfo(workerId+i,workerName,null,null,null,null);
            userInfoArrayList.add(workerInfo);
        }
    }

}
