package com.automation.zzx.intelligent_basket_demo.entity;

import java.io.Serializable;

public class MgBasketInstallInfo implements Serializable {
    private String basketId; // 吊篮ID
    private String userId; // 安装负责人ID
    private String projectId; // 所属项目ID

    private String startTime; // 安装开始时间
    private String endTime; // 安装结束时间

    private int userState; // 安装人员分配标志位
    private int deviceState; // 吊篮配置信息标志位
    private int picFlag; // 图片上传标志位
    private int flag; // 整体安装状态标志位

    /*
     * 2or21：安检证书已上传
     */
    private int stateInPro; //项目中吊篮状态


    public MgBasketInstallInfo( ) {
    }

    public MgBasketInstallInfo(String basketId, String projectId) {
        this.basketId = basketId;
        this.projectId = projectId;
    }

    public MgBasketInstallInfo(String basketId, String userId, String projectId, String startTime, int userState, int deviceState, int flag) {
        this.basketId = basketId;
        this.userId = userId;
        this.projectId = projectId;
        this.startTime = startTime;
        this.userState = userState;
        this.deviceState = deviceState;
        this.flag = flag;
    }

    public String getBasketId() {
        return basketId;
    }

    public void setBasketId(String basketId) {
        this.basketId = basketId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
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

    public int isUserState() {
        return userState;
    }

    public void setUserState(int userState) {
        this.userState = userState;
    }

    public int isDeviceState() {
        return deviceState;
    }

    public void setDeviceState(int deviceState) {
        this.deviceState = deviceState;
    }

    public int isFlag() {
        return flag;
    }

    public void setFlag(int flag) {
        this.flag = flag;
    }
    public int getFlag( ) {
        return flag;
    }

    public int getPicFlag() {
        return picFlag;
    }

    public void setPicFlag(int picFlag) {
        this.picFlag = picFlag;
    }

    public int getStateInPro() {
        return stateInPro;
    }

    public void setStateInPro(int stateInPro) {
        this.stateInPro = stateInPro;
    }
}
