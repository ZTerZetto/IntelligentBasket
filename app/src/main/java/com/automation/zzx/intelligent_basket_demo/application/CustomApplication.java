package com.automation.zzx.intelligent_basket_demo.application;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.Application;
import android.app.KeyguardManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.os.Process;
import android.os.Vibrator;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;

import com.automation.zzx.intelligent_basket_demo.R;
import com.automation.zzx.intelligent_basket_demo.activity.worker.WorkerPrimaryActivity;
import com.automation.zzx.intelligent_basket_demo.entity.MessageInfo;
import com.automation.zzx.intelligent_basket_demo.fragment.areaAdmin.AreaAdminMessageFragment;
import com.automation.zzx.intelligent_basket_demo.utils.xiaomi.mipush.MiMessageReceiver;
import com.baidu.mapapi.SDKInitializer;
import com.xiaomi.channel.commonutils.logger.LoggerInterface;
import com.xiaomi.mipush.sdk.Logger;
import com.xiaomi.mipush.sdk.MiPushClient;
import com.xiaomi.mipush.sdk.MiPushMessage;

import org.litepal.LitePal;
import org.litepal.crud.DataSupport;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by pengchenghu on 2019/3/18.
 * Author Email: 15651851181@163.com
 * Describe:
 */
public class CustomApplication extends Application {
    // user your appid the key.
    private static final String APP_ID = "2882303761517987207";
    // user your appid the key.
    private static final String APP_KEY = "5971798771207";

    // 此TAG在adb logcat中检索自己所需要的信息， 只需在命令行终端输入 adb logcat | grep
    // com.xiaomi.mipushdemo
    public static final String TAG = "MiPushApplication";

    public static final int OnReceivePassThroughMessage = 2;
    public static final int onNotificationMessageClicked = 3;

    private static MiHandler mHandler = null;
    private static Activity mainActivity = null;

    // 仅使用震动+提示音
    private static KeyguardManager keyguardManager;
    private static Vibrator vibrator;//震动
    private static MediaPlayer mediaplayer;//提示音
    private static int ID_LED = 19960428; // 呼吸灯

    // 使用Notification未成功
    private static NotificationManager notificationManager;
    private String beforeChannelId;
    private String channelId;
    private int chatCount = 0;

    // 解析消息
    SimpleDateFormat dataFormat = new SimpleDateFormat("yyyy年MM月dd日 HH:mm");//设置日期格式

    // 为了提高推送服务的注册率，官方Demo建议在Application的onCreate中初始化推送服务
    // 你也可以根据需要，在其他地方初始化推送服务
    @Override
    public void onCreate() {
        super.onCreate();

        // 百度地图
        SDKInitializer.initialize(getApplicationContext());
        // 初始化LitePal数据库
        LitePal.initialize(this);

        // 判断用户是否已经打开App，详细见下面方法定
        if (shouldInit()) {
            MiPushClient.registerPush(this, APP_ID, APP_KEY);
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
        Logger.disablePushFileLog(this);

        if (mHandler == null) {
            mHandler = new MiHandler(getApplicationContext());
        }

        // 下面与震动+提示音有关
        keyguardManager = (KeyguardManager) getSystemService(KEYGUARD_SERVICE);
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);  // 震动
        releaseMediaPlayer(); // 铃声(这里先释放内存)
        mediaplayer = new MediaPlayer();
        try {
            mediaplayer.setDataSource(this, RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
            mediaplayer.prepare();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 下面与消息提示相关
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        channelId = "chat";
        //setChannel(true);
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

    public static void setMainActivity(Activity activity) {
        mainActivity = activity;
    }

    public static void releaseMediaPlayer(){
        if(mediaplayer != null){
            mediaplayer.stop();
            mediaplayer.release();
            mediaplayer = null;
        }
    }


    // 通过设置Handler来设置提示文案
    public class MiHandler extends Handler {

        private Context context;

        public MiHandler(Context context) {
            this.context = context;
        }

        @Override
        public void handleMessage(Message msg) {
            if (mainActivity != null) {
                MiPushMessage miMessage = (MiPushMessage) msg.obj;
                switch(msg.what){
                    case OnReceivePassThroughMessage:  // 通知栏消息
                        // 震动和声音提示
                        onMessageNotify();
                        // 消息解析
                        onMessageParse(miMessage);
                        break;
                    case onNotificationMessageClicked:
                        // 停止震动
                        stopMessageNotify();
                        break;
                }
            }
        }
    }

    /*
     * 提示音 + 震动
     */
    public void onMessageNotify(){
        // 黑屏震动
        if(isScreenOff(this)) {
            //vibrator.vibrate(new long[]{100, 500, 100, 500}, 2);
        }

        // 提示音
        mediaplayer.start();

        Notification.Builder builder;
        // 呼吸灯
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            //如果是8以上的系统。需要传一个channelId.
            builder = new Notification.Builder(this, channelId);
        }else{
            builder = new Notification.Builder(this);
        }
//        Notification notification = builder.build();
//        notification.ledARGB = 0xff000000;
//        notification.ledOnMS = 1000;
//        notification.ledOffMS = 1000;
//        notification.flags = Notification.FLAG_SHOW_LIGHTS;
//        notificationManager.notify(ID_LED, notification);
    }
    /*
    * 停止震动
    */
    public void stopMessageNotify(){
        vibrator.cancel();
        //notificationManager.cancel(ID_LED);
    }

    /*
    *  消息解析
    * */
    public void onMessageParse(MiPushMessage message){
        MessageInfo messageInfo = new MessageInfo(dataFormat.format(new Date()),
                message.getTitle(), message.getDescription());
        Map<String, String> keyValuePair = message.getExtra();
        messageInfo.setmType(keyValuePair.get("type"));
        if(messageInfo.getmType()==null || messageInfo.getmType().equals("")) return;
        switch (messageInfo.getmType()){
            case "1":  // 报警消息
                messageInfo.setmWorkerPhone(keyValuePair.get("deviceId"));
                messageInfo.setmWorkerList(keyValuePair.get("worker"));
                break;
            case "2": // 验收申请
                break;
            case "3": // 项目流程
                break;
            case "5": // 配置清单
                messageInfo.setmProjectId(keyValuePair.get("projectId"));  // 项目id
                break;
        }
        messageInfo.save(); // 保存数据库
    }

    /*
     * 锁屏提示 闪光灯 声音 和 震动提示
     * 尚未完成
     */
    public void createNotification(){
        Intent intent = new Intent(this, WorkerPrimaryActivity.class);
        /*
         * 调用PendingIntent的静态放法创建一个 PendingIntent对象用于点击通知之后执行的操作，
         * PendingIntent可以理解为延时的Intent，在这里即为点击通知之后执行的Intent
         * 这里调用getActivity(Context context, int requestCode, Intent intent, int flag)方法
         * 表示这个PendingIntent对象启动的是Activity，类似的还有getService方法、getBroadcast方法
         */
        PendingIntent pi = PendingIntent.getActivity(this, 0, intent, 0);

        NotificationCompat.Builder builder;

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            //如果是8以上的系统。需要传一个channelId.
            builder = new NotificationCompat.Builder(this, channelId);
        }else{
            builder = new NotificationCompat.Builder(this);
        }

        builder.setContentTitle("震惊!某985大学生竟然使用小米推送!") // 创建通知的标题
                .setContentText("小米推送大法好啊!") // 创建通知的内容
                .setSmallIcon(R.mipmap.ic_worker_message) // 创建通知的小图标
                .setLargeIcon(BitmapFactory.decodeResource(getResources(),
                        R.mipmap.ic_launcher)) // 创建通知的大图标
                /*
                 * 首先，无论你是使用自定义视图还是系统提供的视图，上面4的属性一定要设置，不然这个通知显示不出来
                 */
                .setWhen(System.currentTimeMillis()) // 设定通知显示的时间
                .setContentIntent(pi) // 设定点击通知之后启动的内容，这个内容由方法中的参数：PendingIntent对象决定
                .setPriority(NotificationCompat.PRIORITY_MAX) // 设置通知的优先级
                .setAutoCancel(true) // 设置点击通知之后通知是否消失
                //.setSound(Uri.parse("android.resource://" + getPackageName() + "/raw/beep")) // 设置声音
                /*
                    * 设置震动，用一个 long 的数组来表示震动状态，这里表示的是先震动1秒、静止1秒、再震动1秒，这里以毫秒为单位
                    * 如果要设置先震动1秒，然后停止0.5秒，再震动2秒则可设置数组为：long[]{1000, 500, 2000}。
                    * 别忘了在AndroidManifest配置文件中申请震动的权限
                */
                //.setVibrate(new long[]{1000, 500, 2000})
                /*
                    * 设置手机的LED灯为蓝色并且灯亮2秒，熄灭1秒，达到灯闪烁的效果，不过这些效果在模拟器上是看不到的，
                    * 需要将程序安装在真机上才能看到对应效果，如果不想设置这些通知提示效果，
                    * 可以直接设置：setDefaults(Notification.DEFAULT_ALL);
                    * 意味将通知的提示效果设置为系统的默认提示效果
                 */
                //.setLights(Color.BLUE, 2000, 1000)
                .setDefaults(Notification.DEFAULT_ALL)
                .setVisibility(Notification.VISIBILITY_PUBLIC);


        Notification notification = builder.build();
        notificationManager.notify(1 ,notification);
    }


    private void setChannel(boolean checked) {
        //channelId = "chat";//消息通道的id，以后可以通过该id找到该消息通道
        String channelName = "聊天消息" ;//消息通道的name
        int importance = NotificationManager.IMPORTANCE_MAX;//通知的优先级
        // .具体的请自行百度。作用就是优先级的不同。可以导致消息出现的形式不一样。
        // MAX是会震动并且出现在屏幕的上方。设置优先级为low或者min时。来通知时都不会震动，
        // 且不会直接出现在屏幕上方
        createNotificationChannel(checked, channelId, channelName, importance);
        beforeChannelId = channelId;
    }

    @TargetApi(Build.VERSION_CODES.O)
    private void createNotificationChannel(boolean isVibrate, String channelId, String channelName,
                                           int importance) {
        if (!TextUtils.isEmpty(beforeChannelId)) {
            //先删除之前的channelId对应的消息通道.
            //notificationManager.deleteNotificationChannel(beforeChannelId);
        }
        //重新new一个消息通道。
        NotificationChannel channel = new NotificationChannel(channelId, channelName, importance);
        //是否震动
        if (isVibrate) {
            // 设置通知出现时的震动（如果 android 设备支持的话）
            channel.enableVibration(true);
            channel.setVibrationPattern(new long[]{1000, 500, 2000});
        } else {
            // 设置通知出现时不震动
            channel.enableVibration(false);
            channel.setVibrationPattern(new long[]{0});
        }
        notificationManager.createNotificationChannel(channel);
    }

    /*
     * 生命周期
     */

    /*
     * 屏幕相关
     */
    public static boolean isScreenOff(Context context) {
        return keyguardManager.inKeyguardRestrictedInputMode();
    }
}
