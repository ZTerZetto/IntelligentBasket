package com.automation.zzx.intelligent_basket_demo.entity;

public class PositionInfo {

    private String id; // 所属楼层内吊篮编号 or 总平面图里的楼号
    private String itemId; // 关联吊篮ID or 该楼所包含的所有吊篮ID
    private Double position_X;  // X坐标
    private Double position_Y;  // Y坐标


    public PositionInfo() {
    }

    public PositionInfo(String id, String basketId, Double position_X, Double position_Y) {
        this.id = id;
        this.itemId = basketId;
        this.position_X = position_X;
        this.position_Y = position_Y;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    public Double getPosition_X() {
        return position_X;
    }

    public void setPosition_X(Double position_X) {
        this.position_X = position_X;
    }

    public Double getPosition_Y() {
        return position_Y;
    }

    public void setPosition_Y(Double position_Y) {
        this.position_Y = position_Y;
    }
}
