package com.example.zzx.zbar_demo.entity;

import java.io.File;

// Created by $USER_NAME on 2018/12/12/012.
public class BasketInfo {
    public BasketInfo(String basketId, String state, String workerId) {
        this.basketId = basketId;
        this.state= state;
        this.workerId = workerId;
    }


    String basketId;

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

    String state;
    String workerId;



}
