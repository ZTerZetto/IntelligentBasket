package com.automation.zzx.intelligent_basket_demo.entity;

import android.support.annotation.Nullable;

import com.baidu.mapapi.model.LatLng;

import java.io.Serializable;

// Created by $USER_NAME on 2019/1/23/023.
public class ProjectInfo implements Serializable {
    private String projectId;
    private String projectName;
    /*
     * 0: 待入库
     * 1：待安装
     * 2：安装审核
     * 3：使用中
     * 4：待报停
     * 5：报停审核
     */
    private String projectState;
    // 开始、结束时间
    private String projectStart;
    private String projectEnd;
    // 电子合同地址
    private String projectContractUrl;
    // 安检证书地址
    private String projectCertUrl;
    // 负责的区域管理员
    private String adminAreaId;
    // 负责的租方管理员ID
    private String adminRentId;
    // 本项目中用到的电柜
    private String boxList;
    // 包含吊篮数量
    private String deviceNum;
    // 本项目中涉及的工人
    private String worker;
    // 工人数量
    private String workerNum;
    // 项目发起者
    private String projectBuilders;
    // 出库照片
    private String storeOut;
    // 报停照片地址
    private String projectEndUrl;
    // 乙方公司名称
    private String companyName;
    // 所属者
    private String owner;
    // 项目所属区域
    private String region;
    // 本项目区域管理员基本信息
    public UserInfo adminAreaUser;
    // 本项目租方管理员基本信息
    public UserInfo adminRentUser;
    // 项目的经纬度信息
    public String coordinate;


    /*
     * 构造函数
     */
    public ProjectInfo() {
        super();
    }

    public ProjectInfo(String projectId, String projectName, String projectState, String projectStart, String projectEnd) {
        this.projectId = projectId;
        this.projectName = projectName;
        this.projectState = projectState;
        this.projectStart = projectStart;
        this.projectEnd = projectEnd;
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

    public ProjectInfo(String projectId, String projectName, String projectState, String projectStart, String projectEnd,
                       String projectContractUrl, String projectCertUrl, String adminAreaId, String adminRentId,
                       String boxList, String deviceNum, String worker, String workerNum, String projectBuilders,
                       String storeOut, String projectEndUrl, String companyName, String owner, String region,
                       UserInfo adminAreaUser, UserInfo adminRentUser) {
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
        this.deviceNum = deviceNum;
        this.worker = worker;
        this.workerNum = workerNum;
        this.projectBuilders = projectBuilders;
        this.storeOut = storeOut;
        this.projectEndUrl = projectEndUrl;
        this.companyName = companyName;
        this.owner = owner;
        this.region = region;
        this.adminAreaUser = adminAreaUser;
        this.adminRentUser = adminRentUser;
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
    public String getDeviceNum() {
        return deviceNum;
    }

    public void setDeviceNum(String deviceNum) {
        this.deviceNum = deviceNum;
    }

    public String getWorkerNum() {
        return workerNum;
    }

    public void setWorkerNum(String workerNum) {
        this.workerNum = workerNum;
    }

    public String getProjectEndUrl() {
        return projectEndUrl;
    }

    public void setProjectEndUrl(String projectEndUrl) {
        this.projectEndUrl = projectEndUrl;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public UserInfo getAdminRentUser() {
        return adminRentUser;
    }

    public void setAdminRentUser(UserInfo adminRentUser) {
        this.adminRentUser = adminRentUser;
    }

    public String getCoordinate() {
        return coordinate;
    }

    public void setCoordinate(String coordinate) {
        this.coordinate = coordinate;
    }
}
