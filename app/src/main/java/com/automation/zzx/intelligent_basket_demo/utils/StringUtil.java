package com.automation.zzx.intelligent_basket_demo.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by pengchenghu on 2020/6/7.
 * Author Email: 15651851181@163.com
 * Describe:
 */
public class StringUtil {
    public static String replaceBlank(String str) {
        String dest = "";
        if (str != null) {
            Pattern p = Pattern.compile("\\s*|\t|\r|\n");
            Matcher m = p.matcher(str);
            dest = m.replaceAll("");
        }
        return dest;

    }
}
