package com.example.zzx.zbar_demo.entity;

import android.support.annotation.Nullable;

import java.io.File;

// Created by $USER_NAME on 2018/11/28/028.
public class UserInfo {

    public UserInfo(String userName, @Nullable String userPhone,@Nullable String userPassword, @Nullable String userRole) {
        this.userName = userName;
        this.userPhone = userPhone;
        this.userPassword = userPassword;
        this.userRole = userRole;
        this.userPhone = userPhone;
    }
    public UserInfo(String userId,String userName, @Nullable String userPhone,@Nullable String userPassword, @Nullable String userRole) {
        this.userId = userId;
        this.userName = userName;
        this.userPhone = userPhone;
        this.userPassword = userPassword;
        this.userRole = userRole;
    }


    public UserInfo(String userPhone, String userPassword) {
        this.userPhone = userPhone;
        this.userPassword = userPassword;
    }

    String userId;
    String userName;
    String userPhone;
    String userPassword;
    String userRole;

    //String workerType;
    File file;


    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserPassword() {
        return userPassword;
    }

    public void setUserPassword(String userPassword) {
        this.userPassword = userPassword;
    }

    public String getUserRole() {
        return userRole;
    }

    public void setUserRole(String userRole) {
        this.userRole = userRole;
    }

/*
    public String getWorkerType() {
        return workerType;
    }

    public void setWorkerType(String userType) {
        this.workerType = workerType;
    }
*/

    public String getUserPhone() {
        return userPhone;
    }

    public void setUserPhone(String userPhone) {
        this.userPhone = userPhone;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

}
