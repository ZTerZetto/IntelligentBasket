package com.automation.zzx.intelligent_basket_demo.entity;

import org.litepal.crud.DataSupport;

/**
 * Describe: 推送消息类
 */

public class MessageInfo extends DataSupport {

    private String mTime;  // 时间
    private String mTitle; // 消息标题
    private String mDescription; // 消息内容

    private String mType; // 消息类型-》1：报警消息 2.验收申请类 3.项目流程类

    private String mProjectId;  // 项目编号
    private String mProjectName; // 项目名称

    private String mWorkerPhone; // 施工人员联系方式
    private String mRentAdminPhone; // 租方管理员联系方式

    /**
     *  构造函数
     */
    public MessageInfo() {
    }

    public MessageInfo(String mTime, String mTitle, String mDescription) {
        this.mTime = mTime;
        this.mTitle = mTitle;
        this.mDescription = mDescription;
    }


    /*
     * Bean 函数
     */
    public String getmTime() {
        return mTime;
    }

    public void setmTime(String mTime) {
        this.mTime = mTime;
    }

    public String getmTitle() {
        return mTitle;
    }

    public void setmTitle(String mTitle) {
        this.mTitle = mTitle;
    }

    public String getmDescription() {
        return mDescription;
    }

    public void setmDescription(String mDescription) {
        this.mDescription = mDescription;
    }

    public String getmType() {
        return mType;
    }

    public void setmType(String mType) {
        this.mType = mType;
    }

    public String getmProjectId() {
        return mProjectId;
    }

    public void setmProjectId(String mProjectId) {
        this.mProjectId = mProjectId;
    }

    public String getmProjectName() {
        return mProjectName;
    }

    public void setmProjectName(String mProjectName) {
        this.mProjectName = mProjectName;
    }

    public String getmWorkerPhone() {
        return mWorkerPhone;
    }

    public void setmWorkerPhone(String mWorkerPhone) {
        this.mWorkerPhone = mWorkerPhone;
    }

    public String getmRentAdminPhone() {
        return mRentAdminPhone;
    }

    public void setmRentAdminPhone(String mRentAdminPhone) {
        this.mRentAdminPhone = mRentAdminPhone;
    }
}

