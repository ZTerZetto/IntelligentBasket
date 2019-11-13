package com.automation.zzx.intelligent_basket_demo.entity;

/**
 * Created by pengchenghu on 2019/2/23.
 * Author Email: 15651851181@163.com
 * Describe: 测试功能类
 */
public class Function{
    private String name;  // 功能名称
    private int imageId;  // 功能示意图
    private boolean viewState; //显示状态

    public Function(String name, int imageId, boolean viewState){
        this.name = name;
        this.imageId = imageId;
        this.viewState = viewState;
    }

    public String getName(){
        return name;
    }

    public int getImageId(){
        return imageId;
    }

    public void setViewState(boolean viewState)
    {
        this.viewState = viewState;
    }

    public boolean getViewState()
    {
        return viewState;
    }
}
