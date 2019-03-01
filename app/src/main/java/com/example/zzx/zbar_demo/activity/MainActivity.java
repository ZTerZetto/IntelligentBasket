package com.example.zzx.zbar_demo.activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.zzx.zbar_demo.PdfRead.PDFStartActivity;
import com.example.zzx.zbar_demo.R;
import com.example.zzx.zbar_demo.zbar.QRScanActivity;
import com.example.zzx.zbar_demo.VideoPlay.VideoPlayActivity;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Button btn_scan;
    private Button btn_video;
    private Button btn_login;
    private Button btn_pdf;
    private Button btn_lbs;
    private Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btn_scan = (Button) findViewById(R.id.btn_scan);
        btn_video =(Button) findViewById(R.id.btn_video);
        btn_login = (Button) findViewById(R.id.btn_login);
        btn_pdf = (Button) findViewById(R.id.btn_pdf);
        btn_lbs = (Button) findViewById(R.id.btn_lbs);

        btn_video.setOnClickListener(this);
        btn_scan.setOnClickListener(this);
        btn_login.setOnClickListener(this);
        btn_pdf.setOnClickListener(this);
        btn_lbs.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_login:
                //登录activity
                intent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(intent);
                break;

            case R.id.btn_scan:
                //二维码扫描demo
                intent = new Intent(MainActivity.this, QRScanActivity.class);
                startActivity(intent);
                break;

            case R.id.btn_video:
                //视频播放demo
                intent = new Intent(MainActivity.this, VideoPlayActivity.class);
                startActivity(intent);
                break;

            case R.id.btn_pdf:
                //PDF阅读demo
                intent = new Intent(MainActivity.this,PDFStartActivity.class);
                startActivity(intent);
                break;

            case R.id.btn_lbs:
                //百度地图显示demo
                intent = new Intent(MainActivity.this, LBSActivity.class);
                startActivity(intent);
                break;

            default:
                break;
        }
    }
}
