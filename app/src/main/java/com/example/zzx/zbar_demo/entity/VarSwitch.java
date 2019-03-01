package com.example.zzx.zbar_demo.entity;

/**
 * Created by pengchenghu on 2019/2/24.
 * Author Email: 15651851181@163.com
 * Describe: 开关变量类
 */
public class VarSwitch {
    private String name;  // 功能名称
    private int imageId;  // 功能示意图
    private boolean state; // 开关状态

    public VarSwitch(String name, int imageId, boolean state){
        this.name = name;
        this.imageId = imageId;
        this.state = state;
    }

    public String getName(){
        return name;
    }

    public int getImageId(){
        return imageId;
    }

    public boolean getState(){
        return state;
    }

    public void setState(boolean state){
        this.state = state;
    }
}
