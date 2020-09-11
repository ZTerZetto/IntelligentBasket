package com.automation.zzx.intelligent_basket_demo.utils.xiaomi.mipush;

import android.app.Activity;

import com.automation.zzx.intelligent_basket_demo.application.CustomApplication;
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

    /*
     * 去除别名设置
     */
    public static void clearAlias(Activity activity, String alias){
//        CustomApplication.setMainActivity(activity);

        MiPushClient.unsetAlias(activity, alias, null);
    }
}
