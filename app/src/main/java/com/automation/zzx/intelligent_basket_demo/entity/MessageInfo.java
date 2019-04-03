package com.automation.zzx.intelligent_basket_demo.entity;


/**
 * Describe: 报警消息
 */

public class MessageInfo {

    private String mTime;
    private String mTitle;
    private String mContent;

    /**
     *  构造函数
     */
    public MessageInfo() {
    }

    public MessageInfo(String mTime, String mTitle, String mContent) {
        this.mTime = mTime;
        this.mTitle = mTitle;
        this.mContent = mContent;
    }


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

    public String getmContent() {
        return mContent;
    }

    public void setmContent(String mContent) {
        this.mContent = mContent;
    }

}

