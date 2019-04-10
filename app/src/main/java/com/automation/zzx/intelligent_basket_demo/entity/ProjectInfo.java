package com.automation.zzx.intelligent_basket_demo.entity;

import android.support.annotation.Nullable;

import java.io.Serializable;

// Created by $USER_NAME on 2019/1/23/023.
public class ProjectInfo implements Serializable {
    public String projectId;
    public String projectName;
    // 当前状态，0：立项； 1：进行中； 2：已结束
    public String projectState;
    // 开始、结束时间
    public String projectStart;
    public String projectEnd;
    // 电子合同地址
    public String projectContractUrl;
    // 安检证书地址
    public String projectCertUrl;
    // 负责的区域管理员
    public String adminAreaId;
    // 负责的租方管理员ID
    public String adminRentId;
    // 本项目中用到的电柜
    public String boxList;
    // 本项目中涉及的工人
    private String worker;
    // 项目发起者
    private String projectBuilders;
    // 出库照片
    private String storeOut;
    // 本项目区域管理员基本信息
    public UserInfo adminAreaUser;

    /*
     * 构造函数
     */
    public ProjectInfo(String projectId, String projectName, String projectState, String projectStart, String projectEnd) {
        this.projectId = projectId;
        this.projectName = projectName;
        this.projectState = projectState;
        this.projectStart = projectStart;
        this.projectEnd = projectEnd;
    }

    public ProjectInfo() {
        super();
    }

    public ProjectInfo(String projectId, String projectName, String projectState, String projectStart, String projectEnd,
                       @Nullable String projectContractUrl, @Nullable String projectCertUrl, @Nullable String adminAreaId, @Nullable String adminRentId,
                       @Nullable String boxList, @Nullable String projectBuilders, @Nullable UserInfo adminAreaUser) {
        this.projectId = projectId;
        this.projectName = projectName;
        this.projectState = projectState;
        this.projectStart = projectStart;
        this.projectEnd = projectEnd;

        this.projectContractUrl = projectContractUrl;
        this.projectCertUrl = projectCertUrl;
        this.adminAreaId = adminAreaId;
        this.adminRentId = adminRentId;
        this.boxList = boxList;
        this.projectBuilders = projectBuilders;
        this.adminAreaUser = adminAreaUser;
    }

    /*
     * Bean 函数
     */
    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getProjectState() {
        return projectState;
    }

    public void setProjectState(String projectState) {
        this.projectState = projectState;
    }

    public String getProjectStart() {
        return projectStart;
    }

    public void setProjectStart(String projectStart) {
        this.projectStart = projectStart;
    }

    public String getProjectEnd() {
        return projectEnd;
    }

    public void setProjectEnd(String projectEnd) {
        this.projectEnd = projectEnd;
    }

    public String getProjectContractUrl() {
        return projectContractUrl;
    }

    public void setProjectContractUrl(String projectContractUrl) {
        this.projectContractUrl = projectContractUrl;
    }

    public String getProjectCertUrl() {
        return projectCertUrl;
    }

    public void setProjectCertUrl(String projectCertUrl) {
        this.projectCertUrl = projectCertUrl;
    }

    public String getAdminAreaId() {
        return adminAreaId;
    }

    public void setAdminAreaId(String adminAreaId) {
        this.adminAreaId = adminAreaId;
    }

    public String getAdminRentId() {
        return adminRentId;
    }

    public void setAdminRentId(String adminRentId) {
        this.adminRentId = adminRentId;
    }

    public String getBoxList() {
        return boxList;
    }

    public void setBoxList(String boxList) {
        this.boxList = boxList;
    }

    public String getWorker() {
        return worker;
    }

    public void setWorker(String worker) {
        this.worker = worker;
    }

    public String getProjectBuilders() {
        return projectBuilders;
    }

    public void setProjectBuilders(String projectBuilders) {
        this.projectBuilders = projectBuilders;
    }

    public String getStoreOut() {
        return storeOut;
    }

    public void setStoreOut(String storeOut) {
        this.storeOut = storeOut;
    }

    public UserInfo getAdminAreaUser() {
        return adminAreaUser;
    }

    public void setAdminAreaUser(UserInfo adminAreaUser) {
        this.adminAreaUser = adminAreaUser;
    }

}
