package com.automation.zzx.intelligent_basket_demo.entity;

// Created by $USER_NAME on 2018/12/12/012.
public class BasketInfo {
    String basketId;
    String state;
    String workerId;

    public BasketInfo(String basketId, String state, String workerId) {
        this.basketId = basketId;
        this.state= state;
        this.workerId = workerId;
    }
    public BasketInfo() {
        super();
    }
    public BasketInfo(String basketId) {
        this.basketId = basketId;
    }

    public String getBasketId() {
        return basketId;
    }

    public void setBasketId(String basketId) {
        this.basketId = basketId;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getWorkerId() {
        return workerId;
    }

    public void setWorkerId(String workerId) {
        this.workerId = workerId;
    }

}
