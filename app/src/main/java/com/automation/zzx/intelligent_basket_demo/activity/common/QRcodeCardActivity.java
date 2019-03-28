package com.automation.zzx.intelligent_basket_demo.activity.common;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import com.automation.zzx.intelligent_basket_demo.R;
import com.automation.zzx.intelligent_basket_demo.widget.zxing.encode.EncodeMessage;

/**
 * Created by pengchenghu on 2019/3/28.
 * Author Email: 15651851181@163.com
 * Describe: 个人信息二维码
 */

public class QRcodeCardActivity extends AppCompatActivity {

    private final static int GENERATE_QRCODE_MSG = 1; // 生成二维码

    // 控件
    private ImageView mQRcodeImageView;

    // 二维码
    private Bitmap mUserQRcodeBitmap;

    // 用户信息
    private String userId = "null";  // 用户ID

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case GENERATE_QRCODE_MSG:
                    mUserQRcodeBitmap = EncodeMessage.createQRImage(QRcodeCardActivity.this, userId,
                            BitmapFactory.decodeResource(getResources(), R.mipmap.ic_default_user_head));
                    mQRcodeImageView.setImageBitmap(mUserQRcodeBitmap);
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qrcode_card);

        initWidgetResource();

    }

    /*
     * 控件初始化
     */
    private void initWidgetResource() {
        // 顶部导航栏
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        TextView titleText = (TextView) findViewById(R.id.toolbar_title);
        toolbar.setTitle("二维码名片");
        titleText.setText("");
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true); //设置返回键可用

        // 二维码
        mQRcodeImageView = (ImageView) findViewById(R.id.user_qrcode_imageview);
        mHandler.sendEmptyMessage(GENERATE_QRCODE_MSG);
    }

    /*
     * 消息监听
     */
    // 顶部导航栏消息响应
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home: // 返回按钮
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
