package com.automation.zzx.intelligent_basket_demo.entity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RepairInfo {
    private String deviceId;

    public RepairInfo(String deviceId, String projectId, String managerId, String dealerId, String reason,
                      String imageStart, String startTime, String imageEnd, String discription, String endTime) {
        this.deviceId = deviceId;
        this.projectId = projectId;
        this.managerId = managerId;
        this.dealerId = dealerId;
        this.reason = reason;
        this.imageStart = imageStart;
        this.startTime = startTime;
        this.imageEnd = imageEnd;
        this.discription = discription;
        this.endTime = endTime;
    }

    private String projectId;
    private String managerId;
    private String dealerId;

    public RepairInfo(String deviceId, String projectId, String managerId, String reason, String imageStart, String startTime) {
        this.deviceId = deviceId;
        this.projectId = projectId;
        this.managerId = managerId;
        this.reason = reason;
        this.imageStart = imageStart;
        this.startTime = startTime;
    }

    private String reason;
    private String imageStart;
    private String startTime;

    public String getImageStart() {
        return imageStart;
    }

    public void setImageStart(String imageStart) {
        this.imageStart = imageStart;
    }

    public String getImageEnd() {
        return imageEnd;
    }

    public void setImageEnd(String imageEnd) {
        this.imageEnd = imageEnd;
    }

    public String getDiscription() {
        return discription;
    }

    public void setDiscription(String discription) {
        this.discription = discription;
    }

    private String imageEnd;
    private String discription;
    private String endTime;


    public RepairInfo(String deviceId, String projectId, String managerId, String startTime) {
        this.deviceId = deviceId;
        this.projectId = projectId;
        this.managerId = managerId;
        this.startTime = startTime;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public String getManagerId() {
        return managerId;
    }

    public void setManagerId(String managerId) {
        this.managerId = managerId;
    }

    public String getDealerId() {
        return dealerId;
    }

    public void setDealerId(String dealerId) {
        this.dealerId = dealerId;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }


    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public List<String> getImageStartList(){
        List<String> list = Arrays.asList(imageStart.split(","));
        return  list;
    }
    public List<String> getImageEndList(){
        List<String> list = Arrays.asList(imageStart.split(","));
        return  list;
    }

}
