package com.automation.zzx.intelligent_basket_demo.activity.basket;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.automation.zzx.intelligent_basket_demo.R;
import com.automation.zzx.intelligent_basket_demo.entity.AppConfig;
import com.automation.zzx.intelligent_basket_demo.entity.MgBasketStatement;
import com.automation.zzx.intelligent_basket_demo.utils.http.HttpUtil;
import com.ezvizuikit.open.EZUIError;
import com.ezvizuikit.open.EZUIKit;
import com.ezvizuikit.open.EZUIPlayer;
import com.hjq.permissions.OnPermission;
import com.hjq.permissions.Permission;
import com.hjq.permissions.XXPermissions;

import java.io.IOException;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by pengchenghu on 2019/10/08.
 * Author Email: 15651851181@163.com
 * Describe: 萤石云视频播放组件
 */

public class BasketEZVideoActivity extends AppCompatActivity {

    private static final String TAG = "BasketEZVideoActivity";
    public static final int SET_ACCESS_TOKEN_MSG = 100;
    public static final int SET_VIDEO_URL_MSG = 101;

    // 控件声明
    private EZUIPlayer mPlayer;
    private MyEZCallback callback = new MyEZCallback();

    // 文件缓存
    public SharedPreferences mSharedPref;
    private SharedPreferences.Editor mEditor;

    // 登录视频查看消息
    private long mExpireTime;  // Token 有效时间戳
    private String mAccessToken;  // 设备Token
    private String mVideoUrl;  // 设备播放地址
    private String mVideoUrlHd;  // 设备播放地址
    private String mDeviceSerial = "D44041017";  // 设备序列号

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SET_ACCESS_TOKEN_MSG:
                    long expireTime = (long)(msg.arg1 * 100000) + (long)(msg.arg2);
                    String accessToken = msg.obj.toString();
                    mExpireTime = expireTime;
                    mAccessToken = accessToken;
                    mEditor.putLong("expireTime", expireTime);
                    mEditor.putString("accessToken", accessToken);
                    mEditor.commit();
//                    setEzVideoUrl();  // 设置播放地
                    mPlayer.setUrl("ezopen://open.ys7.com/D44041017/1.hd.live");
                    break;
                case SET_VIDEO_URL_MSG:
                    mPlayer.setUrl(mVideoUrl);
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_basket_ezvideo);
        mSharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        mEditor = mSharedPref.edit();
        if (!isHasPermission()) requestPermission();
        initWidget();
        getSharedPrefInfo();
    }

    /*
     * 初始化控件
     */
    private void initWidget(){
        mPlayer = (EZUIPlayer) findViewById(R.id.player_ui);  // 获取EZUIPlayer实例
        EZUIKit.initWithAppKey(this.getApplication(), AppConfig.EZUIKit_APPKEY);  // 初始化EZUIKit
        mPlayer.setCallBack(callback);  // 设置播放回调callback
    }


    /*
     * 播放相关函数
     */
    // 获取AccessToken
    private void getEzAccessToken(){
        HttpUtil.getEZAccessToken(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.i(TAG, "失败：" + e.toString());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseData = response.body().string();
                JSONObject jsonObject = JSON.parseObject(responseData);  // string 转 jsonobject
                String code = jsonObject.getString("code");
                if (code.equals("200")){
                    String data = jsonObject.getString("data");
                    JSONObject dataJsonObject = JSON.parseObject(data);  // string 转 jsonobject
                    String accessToken = dataJsonObject.getString("accessToken");
                    long expireTime = dataJsonObject.getLong("expireTime");
                    Message msg = new Message();
                    msg.obj = accessToken;
                    expireTime = (long) expireTime / 1000;
                    msg.arg1 = (int) expireTime / 100000;
                    msg.arg2 = (int) expireTime % 100000;
                    msg.what = SET_ACCESS_TOKEN_MSG;
                    mHandler.sendMessage(msg);
                }else{

                }
            }
        }, AppConfig.EZUIKit_APPKEY, AppConfig.EZUIKit_SECRET);
    }
    // 获取用户所有的直播地址
    private void setEzVideoUrl(){
        HttpUtil.getEZVideoUrlList(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.i(TAG, "失败：" + e.toString());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseData = response.body().string();
                JSONObject jsonObject = JSON.parseObject(responseData);  // string 转 jsonobject
                String code = jsonObject.getString("code");
                if (code.equals("200")){
                    String data = jsonObject.getString("data");
                    JSONArray urls = JSON.parseArray(data);
                    for(int i=0; i<urls.size(); i++){
                        JSONObject row = urls.getJSONObject(i);
                        String deviceSeries = row.getString("deviceSerial");
                        if (deviceSeries.equals(mDeviceSerial)){
                            mVideoUrl = row.getString("rtmp");
                            mVideoUrlHd = row.getString("rtmpHd");
                            mHandler.sendEmptyMessage(SET_VIDEO_URL_MSG);
                        }else{
                            continue;
                        }
                    }
                }else{

                }
            }
        }, mAccessToken);
    }

    /*
     * 本地资源
     */
    // 获取本地expireTime 和 accessToken
    public void getSharedPrefInfo(){
        mExpireTime = mSharedPref.getLong("expireTime", 0);
        mAccessToken = mSharedPref.getString("accessToken", "");

        if (mExpireTime==0 || mAccessToken.equals("")){
            getEzAccessToken();
        }else{
            if (System.currentTimeMillis() >= mExpireTime*1000) {  // 时间超过7天，重新获取
                getEzAccessToken();
            }else{
//                setEzVideoUrl();
                mPlayer.setUrl("ezopen://open.ys7.com/D44041017/1.hd.live");
            }
        }
    }

    /*
     * 活动生命周期类
     */
    @Override
    protected void onStop() {
        super.onStop();
        mPlayer.stopPlay();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mPlayer.releasePlayer();
    }

    /*
     * 回调类
     */
    public class MyEZCallback implements EZUIPlayer.EZUIPlayerCallBack{

        @Override
        public void onPlaySuccess() {
            Log.d(TAG, "onPlaySuccess");
        }

        @Override
        public void onPlayFail(EZUIError ezuiError) {
            Log.d(TAG, "onPlayFail:"+ezuiError.getErrorString());
        }

        @Override
        public void onVideoSizeChange(int i, int i1) {
            Log.d(TAG, "onVideoSizeChange");
        }

        @Override
        public void onPrepared() {
            Log.d(TAG, "onPrepared");
            mPlayer.startPlay();
        }

        @Override
        public void onPlayTime(Calendar calendar) {
        }

        @Override
        public void onPlayFinish() {
            Log.d(TAG, "onPlayFinish");
        }
    }

    /*
     *
     */
    /*
     * 用xxpermissions申请权限
     */
    // 申请权限
    private void requestPermission() {
        XXPermissions.with(BasketEZVideoActivity.this)
                .constantRequest() //可设置被拒绝后继续申请，直到用户授权或者永久拒绝
                .permission(Permission.Group.STORAGE)//支持请求6.0悬浮窗权限8.0请求安装权限
                .request(new OnPermission() {
                    @Override
                    public void hasPermission(List<String> granted, boolean isAll) {
                        if (isAll) {
                            onResume();
                        }else {
                            Toast.makeText(BasketEZVideoActivity.this,
                                    "必须同意所有的权限才能使用本程序", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void noPermission(List<String> denied, boolean quick) {
                        if(quick) {
                            Toast.makeText(BasketEZVideoActivity.this, "被永久拒绝授权，请手动授予权限",
                                    Toast.LENGTH_SHORT).show();
                            // 如果是被永久拒绝就跳转到应用权限系统设置页面
                            XXPermissions.gotoPermissionSettings(BasketEZVideoActivity.this);
                        }else {
                            Toast.makeText(BasketEZVideoActivity.this, "获取权限失败", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    }
                });
    }

    // 是否有权限：摄像头
    private boolean isHasPermission() {
        if (XXPermissions.isHasPermission(this, Permission.Group.STORAGE))
            return true;
        return false;
    }

}
