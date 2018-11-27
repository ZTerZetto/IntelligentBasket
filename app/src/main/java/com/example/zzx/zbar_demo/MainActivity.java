package com.example.zzx.zbar_demo;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.zzx.zbar_demo.QRCode.QRScanActivity;
import com.example.zzx.zbar_demo.VideoPlay.VideoPlayActivity;

import io.vov.vitamio.provider.MediaStore;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Button btn_scan;
    private Button btn_video;
    private Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btn_scan = (Button) findViewById(R.id.btn_scan);
        btn_video =(Button) findViewById(R.id.btn_video);

        btn_video.setOnClickListener(this);
        btn_scan.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
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
            default:
                break;
        }
    }
}
