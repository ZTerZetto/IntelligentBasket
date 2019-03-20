package com.example.zzx.zbar_demo.utils.xiaomi.mipush;

import android.app.Activity;

import com.example.zzx.zbar_demo.application.CustomApplication;
import com.xiaomi.mipush.sdk.MiPushClient;

/**
 * Created by pengchenghu on 2019/3/19.
 * Author Email: 15651851181@163.com
 * Describe:
 */
public class MiPushUtil {

    /*
     * 小米推送初始化
     */
    public static void initMiPush(Activity activity, String alias, String topic){
        CustomApplication.setMainActivity(activity);

        // 设置别名
        if(alias != null){
            MiPushClient.setAlias(activity, alias, null);
        }

        // 订阅消息
        if(topic != null){
            MiPushClient.subscribe(activity, topic, null);
        }

    }
}
