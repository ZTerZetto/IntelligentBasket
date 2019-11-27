package com.automation.zzx.intelligent_basket_demo.entity;

public class PositionInfo {

    private String id; // 关联吊篮ID or 楼层ID
    private Float position_X;  // X坐标
    private Float position_Y;  // Y坐标


    public PositionInfo() {
    }

    public PositionInfo(String id, Float position_X, Float position_Y) {
        this.id = id;
        this.position_X = position_X;
        this.position_Y = position_Y;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Float getPosition_X() {
        return position_X;
    }

    public void setPosition_X(Float position_X) {
        this.position_X = position_X;
    }

    public Float getPosition_Y() {
        return position_Y;
    }

    public void setPosition_Y(Float position_Y) {
        this.position_Y = position_Y;
    }
}
