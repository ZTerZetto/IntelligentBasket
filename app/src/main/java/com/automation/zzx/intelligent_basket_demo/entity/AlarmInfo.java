package com.automation.zzx.intelligent_basket_demo.entity;

public class AlarmInfo {

    private String device_id;
    private String alarm_type;
    private String id;
    private String time;
    private String alarm_detail;

    /*
     * 构造函数
     */
    public AlarmInfo(){ }

    public AlarmInfo(String device_id, String alarm_type, String id, String time, String alarm_detail) {
        this.device_id = device_id;
        this.alarm_type = alarm_type;
        this.id = id;
        this.time = time;
        this.alarm_detail = alarm_detail;
    }

    //getter & setter
    public String getDevice_id() {
        return device_id;
    }

    public void setDevice_id(String device_id) {
        this.device_id = device_id;
    }

    public String getAlarm_type() {
        return alarm_type;
    }

    public void setAlarm_type(String alarm_type) {
        this.alarm_type = alarm_type;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getAlarm_detail() {
        return alarm_detail;
    }

    public void setAlarm_detail(String alarm_detail) {
        this.alarm_detail = alarm_detail;
    }

}
