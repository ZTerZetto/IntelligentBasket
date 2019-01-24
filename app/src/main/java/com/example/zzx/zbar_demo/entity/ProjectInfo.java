package com.example.zzx.zbar_demo.entity;

import android.support.annotation.Nullable;

import java.security.PrivilegedExceptionAction;

// Created by $USER_NAME on 2019/1/23/023.
public class ProjectInfo {
    public String projectId;
    public String projectName;
    public String projectState;
    public String projectStart;
    public String projectEnd;


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



    public String projectContractUrl;
    public String projectCertUrl;
    public String adminAreaId;
    public String adminRentId;
    public String boxList;
    public String projectBuilders;
    public String adminAreaUser;


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

    public String getProjectBuilders() {
        return projectBuilders;
    }

    public void setProjectBuilders(String projectBuilders) {
        this.projectBuilders = projectBuilders;
    }

    public String getAdminAreaUser() {
        return adminAreaUser;
    }

    public void setAdminAreaUser(String adminAreaUser) {
        this.adminAreaUser = adminAreaUser;
    }

    public ProjectInfo(String projectId, String projectName, String projectState, String projectStart, String projectEnd,
                       @Nullable String projectContractUrl, @Nullable String projectCertUrl, @Nullable String adminAreaId, @Nullable String adminRentId,
                       @Nullable String boxList, @Nullable String projectBuilders, @Nullable String adminAreaUser) {
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

}
