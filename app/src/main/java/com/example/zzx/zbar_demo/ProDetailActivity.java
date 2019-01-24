package com.example.zzx.zbar_demo;

import android.annotation.SuppressLint;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ProDetailActivity extends AppCompatActivity {

    private TextView txtProNumber;
    private TextView txtProState;
    private LinearLayout llChangeProState;
    private LinearLayout llProContract;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pro_detail);

        txtProNumber =  findViewById(R.id.txt_pro_number);
        txtProState =  findViewById(R.id.txt_user_name);
        llChangeProState = findViewById(R.id.ll_change_pro_state);
        llProContract = findViewById(R.id.ll_pro_contract);

        //项目状态更改跳转
        llChangeProState.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        //项目合同书界面跳转
        llProContract.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

    }
}
