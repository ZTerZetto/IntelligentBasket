package com.example.zzx.zbar_demo.activity.loginRegist;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.zzx.zbar_demo.R;

public class RegisterPreActivity extends AppCompatActivity implements View.OnClickListener{

    private Button btnWorker;
    private Button btnRent;
    private Button btnRegion;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_pre);

        btnWorker = findViewById(R.id.btn_regist_worker);
        btnRent = findViewById(R.id.btn_regist_rent_manager);
        btnRegion = findViewById(R.id.btn_regist_region_manager);

        btnWorker.setOnClickListener(this);
        btnRent.setOnClickListener(this);
        btnRegion.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        Intent intent;
        switch (v.getId()){
            case R.id.btn_regist_worker:
                intent = new Intent(this,RegistWorkerActivity.class);
                startActivityForResult(intent,1);
                break;
            case R.id.btn_regist_rent_manager:
                intent = new Intent(this,RegistRentManActivity.class);
                startActivityForResult(intent,1);
                break;
            case R.id.btn_regist_region_manager:
                intent = new Intent(this,RegistRentManActivity.class);
                startActivityForResult(intent,1);
                break;
                default:
        }
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
