package com.example.zzx.zbar_demo.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.zzx.zbar_demo.R;
import com.example.zzx.zbar_demo.Util.ToastUtil;
import com.example.zzx.zbar_demo.Util.okhttp.BaseCallBack;
import com.example.zzx.zbar_demo.Util.okhttp.BaseOkHttpClient;
import com.example.zzx.zbar_demo.entity.AppConfig;
import com.example.zzx.zbar_demo.widget.CustomMediaController;
import com.pili.pldroid.player.AVOptions;
import com.pili.pldroid.player.PLOnBufferingUpdateListener;
import com.pili.pldroid.player.PLOnCompletionListener;
import com.pili.pldroid.player.PLOnErrorListener;
import com.pili.pldroid.player.PLOnInfoListener;
import com.pili.pldroid.player.PLOnVideoSizeChangedListener;
import com.pili.pldroid.player.widget.PLVideoTextureView;

import java.io.IOException;

import okhttp3.Call;

import static com.example.zzx.zbar_demo.entity.AppConfig.ASPECT_RATIO;
import static com.example.zzx.zbar_demo.entity.AppConfig.ASPECT_RATIO_VIDEO;
import static com.example.zzx.zbar_demo.entity.AppConfig.HANGING_BASKET_VIDEO;
import static com.example.zzx.zbar_demo.entity.AppConfig.VIDEO_STREAM_PATH;


/**
 * Created by pengchenghu on 2019/2/23.
 * Author Email: 15651851181@163.com
 * Describe: 吊篮视频监视:PLDroidPlayer
 * limits: 从服务器抓取数据，动态显示数据
 */

public class VideoMonitor2Activity extends AppCompatActivity {
    private static final String TAG = "VideoMonitor2Activity";

    // 消息标志位
    private static final int OPEN_VIDEO_SUCESS = 1;
    private static final int OPEN_VIDEO_FAILED = 2;

    // 控件声明
    private RelativeLayout mVideoViewRelativelayout;
    private PLVideoTextureView mVideoView;
    private View mLoadingView;
    private TextView mStateInfoTv;

    private Toast mToast = null;
    private int mDisplayAspectRatio = PLVideoTextureView.ASPECT_RATIO_FIT_PARENT; //default
    //private String mVideoUrls;
    private String mVideoUrls = "rtmp://47.96.103.244:1935/rtmplive/hangingbasket_002";
    private boolean mIsBuffering = true;
    private CustomMediaController mMediaController;

    // 屏幕适配
    private int mScreenWidth;
    private int mScreenHeight;

    // 吊篮相关
    private String mBasketId;

    // 处理线程信息
    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case OPEN_VIDEO_SUCESS:
                    mVideoView.setVideoPath(mVideoUrls); // 设置播放地址
                    break;
                case OPEN_VIDEO_FAILED:
                    ToastUtil.showToastTips(VideoMonitor2Activity.this, "流媒体服务器无响应");
                    finish();
                    break;
                default:break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_monitor2);

        Intent intent = getIntent();
        //mBasketId = intent.getStringExtra(HANGING_BASKET_ID);  // 获取吊篮id
        if(mBasketId==null || mBasketId.equals("")){
            //ToastUtil.showToastTips(VideoMonitor2Activity.this, "没有吊篮ID");
            //this.finish();
            mBasketId = "1";
        }

        getScreenSize();
        initWidget();
        //openDeviceVideo();  // 发送视频流请求
    }

    private void initWidget(){
        // 设置播放器大小
        mVideoViewRelativelayout = (RelativeLayout) findViewById(R.id.video_view_rl);
        ViewGroup.LayoutParams lp = mVideoViewRelativelayout.getLayoutParams();
        lp.width = ViewGroup.LayoutParams.MATCH_PARENT;
        lp.height = (int)(mScreenWidth / ASPECT_RATIO);
        mVideoViewRelativelayout.setLayoutParams(lp);

        // 播放器参数初始化
        mVideoView = (PLVideoTextureView) findViewById(R.id.pl_video_view);
        mLoadingView = (View) findViewById(R.id.loading_ly);  // 设置缓冲提示器
        mVideoView.setBufferingIndicator(mLoadingView);

        //View coverView = findViewById(R.id.cover_image_view);  // 设置黑屏覆盖
        //mVideoView.setCoverView(coverView);

        mStateInfoTv = (TextView) findViewById(R.id.state_info_tv);  // 缓冲信息

        // 视频流基本参数
        AVOptions options = new AVOptions();
        options.setInteger(AVOptions.KEY_PREPARE_TIMEOUT, 10 * 1000); // timeout=10s
        options.setInteger(AVOptions.KEY_LIVE_STREAMING, 1); // 直播优化
        options.setInteger(AVOptions.KEY_FAST_OPEN, 1); // 快开模式
        options.setInteger(AVOptions.KEY_MEDIACODEC, AVOptions.MEDIA_CODEC_HW_DECODE); // 硬解
        options.setInteger(AVOptions.KEY_LOG_LEVEL, 2); // 设置 SDK 的 log 等级
        options.setInteger(AVOptions.KEY_CACHE_BUFFER_DURATION, 500); // 缓存大小500ms
        mVideoView.setAVOptions(options);

        // 控制栏初始化
        mMediaController = new CustomMediaController(this, mBasketId);
        mMediaController.setAnchorView(mVideoViewRelativelayout);  // 设定控制栏位置
        mVideoView.setMediaController(mMediaController);  // 绑定控制栏和播放器
        //mMediaController.setFileName(mBasketId);  // 设置播放页面信息
        mMediaController.setPlVideoTextureView(mVideoView);  // 设置控制栏控制的视频控件
        mMediaController.setVideoSize(mVideoViewRelativelayout, mScreenWidth,
                mVideoView, ASPECT_RATIO_VIDEO); // 设置播放器尺寸

        // 视频播放器监听
        mVideoView.setOnInfoListener(mOnInfoListener);
        mVideoView.setOnVideoSizeChangedListener(mOnVideoSizeChangedListener);
        mVideoView.setOnBufferingUpdateListener(mOnBufferingUpdateListener);
        mVideoView.setOnCompletionListener(mOnCompletionListener);
        mVideoView.setOnErrorListener(mOnErrorListener);

        mVideoView.setLooping(false);  // 不循环

        mVideoView.setVideoPath(mVideoUrls);  // 设置播放地址
    }

    // 正在运行
    @Override
    protected void onResume() {
        super.onResume();
        mVideoView.start();  // 开始播放
    }

    // 离开页面
    @Override
    protected void onPause() {
        super.onPause();
        mVideoView.pause();  // 暂停播放
        mToast = null;
    }

    // 销毁页面
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mVideoView.stopPlayback();  // 释放资源
    }

    // 重写默认按键功能
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode){
            case KeyEvent.KEYCODE_BACK:
                if(getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
                    finish();  // 竖屏-》销毁活动
                else if(getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE)
                    mMediaController.doOriginScreen(this); // 横屏-》竖屏
                break;
        }
        return true;
    }

    /*
     * 监听函数
     */
    private PLOnInfoListener mOnInfoListener = new PLOnInfoListener() {
        @Override
        public void onInfo(int what, int extra) {
            Log.i(TAG, "OnInfo, what = " + what + ", extra = " + extra);
            switch (what) {
                case PLOnInfoListener.MEDIA_INFO_BUFFERING_START:
                    Log.w(TAG, "media info buffering start ");
                    mLoadingView.setVisibility(View.VISIBLE);
                    break;
                case PLOnInfoListener.MEDIA_INFO_BUFFERING_END:
                    Log.w(TAG, "media info buffering end ");
                    mLoadingView.setVisibility(View.GONE);
                    break;
                case PLOnInfoListener.MEDIA_INFO_VIDEO_RENDERING_START:
                    ToastUtil.showToastTips(VideoMonitor2Activity.this,
                            "First video render time: " + extra + "ms");
                    break;
                case PLOnInfoListener.MEDIA_INFO_AUDIO_RENDERING_START:
                    Log.i(TAG, "First audio render time: " + extra + "ms");
                    break;
                case PLOnInfoListener.MEDIA_INFO_VIDEO_FRAME_RENDERING:
                    Log.i(TAG, "video frame rendering, ts = " + extra);
                    break;
                case PLOnInfoListener.MEDIA_INFO_AUDIO_FRAME_RENDERING:
                    Log.i(TAG, "audio frame rendering, ts = " + extra);
                    break;
                case PLOnInfoListener.MEDIA_INFO_VIDEO_GOP_TIME:
                    Log.i(TAG, "Gop Time: " + extra);
                    break;
                case PLOnInfoListener.MEDIA_INFO_SWITCHING_SW_DECODE:
                    Log.i(TAG, "Hardware decoding failure, switching software decoding!");
                    break;
                case PLOnInfoListener.MEDIA_INFO_METADATA:
                    Log.i(TAG, mVideoView.getMetadata().toString());
                    break;
                case PLOnInfoListener.MEDIA_INFO_VIDEO_BITRATE:
                case PLOnInfoListener.MEDIA_INFO_VIDEO_FPS:
                    updateStatInfo();
                    break;
                case PLOnInfoListener.MEDIA_INFO_CONNECTED:
                    Log.i(TAG, "Connected !");
                    break;
                case PLOnInfoListener.MEDIA_INFO_VIDEO_ROTATION_CHANGED:
                    Log.i(TAG, "Rotation changed: " + extra);
                    break;
                default:
                    break;
            }
        }
    };

    private PLOnErrorListener mOnErrorListener = new PLOnErrorListener() {
        @Override
        public boolean onError(int errorCode) {
            Log.e(TAG, "Error happened, errorCode = " + errorCode);
            switch (errorCode) {
                case PLOnErrorListener.ERROR_CODE_IO_ERROR:
                    /**
                     * SDK will do reconnecting automatically
                     */
                    ToastUtil.showToastTips(VideoMonitor2Activity.this, "IO Error !");
                    return false;
                case PLOnErrorListener.ERROR_CODE_OPEN_FAILED:
                    ToastUtil.showToastTips(VideoMonitor2Activity.this, "failed to open player !");
                    break;
                case PLOnErrorListener.ERROR_CODE_SEEK_FAILED:
                    ToastUtil.showToastTips(VideoMonitor2Activity.this, "failed to seek !");
                    return true;
                default:
                    ToastUtil.showToastTips(VideoMonitor2Activity.this, "unknown error !");
                    break;
            }
            finish();
            return true;
        }
    };

    private PLOnCompletionListener mOnCompletionListener = new PLOnCompletionListener() {
        @Override
        public void onCompletion() {
            Log.i(TAG, "Play Completed !");
            ToastUtil.showToastTips(VideoMonitor2Activity.this, "Play Completed !");
            finish();
        }
    };

    private PLOnBufferingUpdateListener mOnBufferingUpdateListener = new PLOnBufferingUpdateListener() {
        @Override
        public void onBufferingUpdate(int precent) {
            Log.w(TAG, "onBufferingUpdate: " + precent);
        }
    };

    private PLOnVideoSizeChangedListener mOnVideoSizeChangedListener = new PLOnVideoSizeChangedListener() {
        @Override
        public void onVideoSizeChanged(int width, int height) {
            Log.i(TAG, "onVideoSizeChanged: width = " + width + ", height = " + height);

        }
    };

    /*
     * 网络相关
     */
    // 管理视频流
    // flag->true:打开， false:关闭
    private void openDeviceVideo(){
        mVideoUrls = VIDEO_STREAM_PATH + "/rtmplive/" + mBasketId;
        String command = "/server.command?command=start_rtmp_stream&pipe=0&url=".concat(mVideoUrls);
        BaseOkHttpClient.newBuilder()
                .addParam("deviceId",  Integer.parseInt(mBasketId))
                .addParam("http_str", command)
                .post()
                .json()
                .url(HANGING_BASKET_VIDEO)
                .build()
                .enqueue(new BaseCallBack() {
                    @Override
                    public void onSuccess(Object o) {
                        // 成功
                        Log.i(TAG, "成功");
                        mHandler.sendEmptyMessage(OPEN_VIDEO_SUCESS);
                    }

                    @Override
                    public void onError(int code) {
                        Log.i(TAG, "错误编码：" + code);
                        mHandler.sendEmptyMessage(OPEN_VIDEO_FAILED);
                    }

                    @Override
                    public void onFailure(Call call, IOException e) {
                        Log.i(TAG, "失败：" + e.toString());
                    }
                });
    }
    // 关闭视频流
    private void closeDeviceVideo(){

    }

    // 更新状态
    private void updateStatInfo() {
        long bitrate = mVideoView.getVideoBitrate() / 1024;
        final String stat = bitrate + "kbps";
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mStateInfoTv.setText(stat);
            }
        });
    }

    // 获取屏幕的宽高度
    private void getScreenSize(){
        DisplayMetrics dm2 = getResources().getDisplayMetrics();
        mScreenHeight = dm2.heightPixels;
        mScreenWidth = dm2.widthPixels;
    }

}
