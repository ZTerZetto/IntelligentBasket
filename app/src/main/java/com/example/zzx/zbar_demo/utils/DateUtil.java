package com.example.zzx.zbar_demo.utils;


import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by pengchenghu on 2019/3/18.
 * Author Email: 15651851181@163.com
 * Describe:
 */
public class DateUtil {
    /*
     * 给定日期字符串输出
     */
    public static Calendar parseDateString(String dateStr){
        try {
            Date date1 = new SimpleDateFormat("yyyy-MM-dd").parse(dateStr);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date1);
            return calendar;
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    /*
     * 给定month_in_year，输出月份
     */
    public static String getMonth(int month_in_year){
        //month_in_year = month_in_year + 1;

        if(month_in_year < 10){
            return "0" + String.valueOf(month_in_year);
        }else{
            return String.valueOf(month_in_year);
        }
    }

    /*
     * 给定day_in_week，输出星期
     */
    public static String getWeekDay(int day_in_week){
        String result = null;
        switch(day_in_week){
            case 1:
                result = "周日";
                break;
            case 2:
                result = "周一";
                break;
            case 3:
                result = "周二";
                break;
            case 4:
                result = "周三";
                break;
            case 5:
                result = "周四";
                break;
            case 6:
                result = "周五";
                break;
            case 7:
                result = "周六";
                break;
        }
        return result;
    }
}
