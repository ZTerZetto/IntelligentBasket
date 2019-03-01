package com.example.zzx.zbar_demo.entity;

/**
 * Created by pengchenghu on 2019/2/23.
 * Author Email: 15651851181@163.com
 * Describe: 测试功能类
 */
public class Function{
    private String name;  // 功能名称
    private int imageId;  // 功能示意图

    public Function(String name, int imageId){
        this.name = name;
        this.imageId = imageId;
    }

    public String getName(){
        return name;
    }

    public int getImageId(){
        return imageId;
    }
}
