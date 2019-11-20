package com.automation.zzx.intelligent_basket_demo.entity;

public class StopRecord {
    private String device_id;
    private String project_id;
    private String id;
    private String time;

    /*
     * 构造函数
     */
    public StopRecord(){ }

    public StopRecord(String device_id, String project_id, String id, String time) {
        this.device_id = device_id;
        this.project_id = project_id;
        this.id = id;
        this.time = time;
    }

    //getter & setter
    public String getDevice_id() {
        return device_id;
    }

    public void setDevice_id(String device_id) {
        this.device_id = device_id;
    }

    public String getProject_id() {
        return project_id;
    }

    public void setProject_id(String project_id) {
        this.project_id = project_id;
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

}
