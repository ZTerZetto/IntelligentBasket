package com.automation.zzx.intelligent_basket_demo.entity;

import android.support.annotation.Nullable;

import java.io.Serializable;

// Created by $USER_NAME on 2018/11/28/028.
public class UserInfo implements Serializable {

    public UserInfo() {
        super();
    }

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
    public UserInfo(String userId,String userRole,String userName) {
        this.userId = userId;
        this.userRole = userRole;
        this.userPhone = userName;
    }

    public UserInfo(String userId, String userName, String userPassword, String userPhone, String userRole, String userPerm, String userImage, boolean checked) {
        this.userId = userId;
        this.userName = userName;
        this.userPassword = userPassword;
        this.userPhone = userPhone;
        this.userRole = userRole;
        this.userPerm = userPerm;
        this.userImage = userImage;
        this.checked = checked;
    }

    private String userId;
    private String userName;
    private String userPassword;
    private String userPhone;
    private String userRole; //用户角色

    private String userPerm; //用户权限
    private String userImage;
    private boolean checked;


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

    public String getUserPerm() {
        return userPerm;
    }

    public void setUserPerm(String userPerm) {
        this.userPerm = userPerm;
    }

    public String getUserImage() {
        return userImage;
    }

    public void setUserImage(String userImage) {
        this.userImage = userImage;
    }

    public boolean getChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        checked = checked;
    }

}
