package com.example.zzx.zbar_demo.zbar;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.zzx.zbar_demo.R;

public class QRScanActivity extends AppCompatActivity implements View.OnClickListener{

    private Button btn_scan;
    private TextView tv_scanResult;
    private static final int REQUEST_CODE_SCAN = 0x0000;// 扫描二维码
    private static final int REQUEST_CAMERA_PERMISSION = 0x001;//二维码权限申请

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qrscan);
        btn_scan = (Button) findViewById(R.id.btn_scan);
        tv_scanResult = (TextView) findViewById(R.id.tv_scanResult);
        btn_scan.setOnClickListener(this);

    }
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_scan:
                //动态权限申请
                if (ContextCompat.checkSelfPermission(QRScanActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(QRScanActivity.this, new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
                } else {
                    goScan();
                }
                break;
            default:
                break;
        }
    }

    /**
     * 跳转到扫码界面扫码
     */
    private void goScan() {
        Intent intent = new Intent(QRScanActivity.this, CaptureActivity.class);
        startActivityForResult(intent, REQUEST_CODE_SCAN);
    }

    //动态权限申请提示
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_CAMERA_PERMISSION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    goScan();
                } else {
                    Toast.makeText(this, "你拒绝了权限申请，可能无法打开相机扫码哟！", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_CODE_SCAN:// 二维码
                // 扫描二维码回传
                if (resultCode == RESULT_OK) {
                    if (data != null) {
                        //获取扫描结果
                        Bundle bundle = data.getExtras();
                        String result = bundle.getString(CaptureActivity.EXTRA_STRING);
                        tv_scanResult.setText("扫描结果：" + result);
                    }
                }
                break;
            default:
                break;
        }
    }

}
