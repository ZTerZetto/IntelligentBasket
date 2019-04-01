package com.automation.zzx.intelligent_basket_demo.entity;

/**
 * Created by pengchenghu on 2019/3/25.
 * Author Email: 15651851181@163.com
 * Describe: 租方管理员管理工人
 */
public class MgWorkerInfo {

    private String headImg;
    private String id; // 工人id
    private String name;  // 工人姓名
    private String state;  // 工作状态
    private String basketId;  // 吊篮ID
    private String totalTime; // 累计时长
    private String phone; // 电话

    /*
     * 构造函数
     */
    public MgWorkerInfo(){}

    /*
     * Bean 函数
     */

    public String getId(){
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getHeadImg() {
        return headImg;
    }

    public void setHeadImg(String headImg) {
        this.headImg = headImg;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getBasketId() {
        return basketId;
    }

    public void setBasketId(String basketId) {
        this.basketId = basketId;
    }

    public String getTotalTime(){
        return totalTime;
    }

    public void setTotalTime(String totalTime) {
        this.totalTime = totalTime;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
}
