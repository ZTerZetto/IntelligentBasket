package com.example.zzx.zbar_demo.utils.xiaomi.mipush;

import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.os.Process;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.example.zzx.zbar_demo.activity.WelcomeActivity;
import com.xiaomi.channel.commonutils.logger.LoggerInterface;
import com.xiaomi.mipush.sdk.Logger;
import com.xiaomi.mipush.sdk.MiPushClient;

import java.util.List;

/**
 * Created by pengchenghu on 2019/3/18.
 * Author Email: 15651851181@163.com
 * Describe:
 */
public class MiPushApplication extends Application {
    // user your appid the key.
    private static final String APP_ID = "1000270";
    // user your appid the key.
    private static final String APP_KEY = "670100056270";

    // 此TAG在adb logcat中检索自己所需要的信息， 只需在命令行终端输入 adb logcat | grep
    // com.xiaomi.mipushdemo
    public static final String TAG = "utils.xiaomi.mipush";

    private static MiHandler mHandler = null;
    private static WelcomeActivity mainActivity = null;

    // 为了提高推送服务的注册率，官方Demo建议在Application的onCreate中初始化推送服务
    // 你也可以根据需要，在其他地方初始化推送服务

    @Override
    public void onCreate() {
        super.onCreate();

        // 判断用户是否已经打开App，详细见下面方法定
        if (shouldInit()) {
            // 注册推送服务
            // 注册成功后会向DemoMessageReceiver发送广播
            // 可以从DemoMessageReceiver的onCommandResult方法中MiPushCommandMessage对象参数中获取注册信息
            MiPushClient.registerPush(this, APP_ID, APP_KEY);
            // 参数说明
            // context：Android平台上app的上下文，建议传入当前app的application context
            // appID：在开发者网站上注册时生成的，MiPush推送服务颁发给app的唯一认证标识
            // appKey:在开发者网站上注册时生成的，与appID相对应，用于验证appID是否合法
        }

        // 下面是与测试相关的日志设置
        LoggerInterface newLogger = new LoggerInterface() {

            @Override
            public void setTag(String tag) {
                // ignore
            }

            @Override
            public void log(String content, Throwable t) {
                Log.d(TAG, content, t);
            }

            @Override
            public void log(String content) {
                Log.d(TAG, content);
            }
        };
        Logger.setLogger(this, newLogger);
        if (mHandler == null) {
            mHandler = new MiHandler(getApplicationContext());
        }
    }

    // 通过判断手机里的所有进程是否有这个App的进程
    // 从而判断该App是否有打开
    private boolean shouldInit() {
        // 通过ActivityManager我们可以获得系统里正在运行的activities
        // 包括进程(Process)等、应用程序/包、服务(Service)、任务(Task)信息。
        ActivityManager am = ((ActivityManager) getSystemService(Context.ACTIVITY_SERVICE));
        List<ActivityManager.RunningAppProcessInfo> processInfos = am.getRunningAppProcesses();
        String mainProcessName = getPackageName();

        // 获取本App的唯一标识
        int myPid = Process.myPid();
        for (ActivityManager.RunningAppProcessInfo info : processInfos) {
            // 利用一个增强for循环取出手机里的所有进程
            if (info.pid == myPid && mainProcessName.equals(info.processName)) {
                // 通过比较进程的唯一标识和包名判断进程里是否存在该App
                return true;
            }
        }
        return false;
    }

    public static void reInitPush(Context ctx) {
        MiPushClient.registerPush(ctx.getApplicationContext(), APP_ID, APP_KEY);
    }

    public static MiHandler getHandler() {
        return mHandler;
    }

    public static void setMainActivity(WelcomeActivity activity) {
        mainActivity = activity;
    }


    // 通过设置Handler来设置提示文案
    public static class MiHandler extends Handler {

        private Context context;

        public MiHandler(Context context) {
            this.context = context;
        }

        @Override
        public void handleMessage(Message msg) {
            String s = (String) msg.obj;
            if (mainActivity != null) {
                //mWelcomeActivity.refreshLogInfo();
            }
            if (!TextUtils.isEmpty(s)) {
                Toast.makeText(context, s, Toast.LENGTH_LONG).show();
            }
        }
    }
}
