package com.automation.zzx.intelligent_basket_demo.entity;

/**
 * Created by pengchenghu on 2019/3/18.
 * Author Email: 15651851181@163.com
 * Describe: 施工人员工单
 */
public class WorkerOrder {

    private String date; // 日期
    private String start_time; // 开始时间
    private String stop_time; // 结束时间
    private String timing_length; // 时长（min）
    private String project_name; // 项目名称

    /*
     * 构造函数
     */
    public WorkerOrder(){}

    public WorkerOrder(String date, String start_time, String stop_time, String timing_length, String project_name){
        this.date = date;
        this.start_time = start_time;
        this.stop_time = stop_time;
        this.timing_length =timing_length;
        this.project_name = project_name;
    }

    /*
     * Bean 函数
     */

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getStart_time() {
        return start_time;
    }

    public void setStart_time(String start_time) {
        this.start_time = start_time;
    }

    public String getStop_time() {
        return stop_time;
    }

    public void setStop_time(String stop_time) {
        this.stop_time = stop_time;
    }

    public String getTiming_length() {
        return timing_length;
    }

    public void setTiming_length(String timing_length) {
        this.timing_length = timing_length;
    }

    public String getProject_name() {
        return project_name;
    }

    public void setProject_name(String project_name) {
        this.project_name = project_name;
    }

}
