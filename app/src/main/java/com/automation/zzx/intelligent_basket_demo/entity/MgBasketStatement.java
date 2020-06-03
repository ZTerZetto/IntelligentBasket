package com.automation.zzx.intelligent_basket_demo.entity;

import java.io.Serializable;

/**
 * Created by pengchenghu on 2019/3/27.
 * Author Email: 15651851181@163.com
 * Describe: 区域管理员更新操作吊篮出/入库状态
 */
public class MgBasketStatement  implements Serializable {

    private String basketId; // 吊篮ID
    private String basketIndexImage; // 首页数据
    /*
     * 0: 草稿
     * 1：待分配安装队伍/待安装
     * 11：安装进行中
     * 12：安装已完成--待上传安检证书
     * 2：吊篮安装验收
     * //21：安检证书验收
     * 3：使用中
     * 4：已结束
     */
    private String basketStatement; // 吊篮出/入状态



    /*
     * 0: 未运行
     * 1：运行中
     */
    private String workStatement; // 吊篮工作状态

    /*
     * 构造函数
     */
    public MgBasketStatement(){}


    public MgBasketStatement(String basketId, String basketIndexImage, String basketStatement,String workStatement ){
        this.basketId = basketId;
        this.basketIndexImage = basketIndexImage;
        this.basketStatement = basketStatement;
        this.workStatement = workStatement;
    }

    /*
     * Bean 函数
     */

    public String getBasketId() {
        return basketId;
    }

    public void setBasketId(String basketId) {
        this.basketId = basketId;
    }

    public String getBasketIndexImage() {
        return basketIndexImage;
    }

    public void setBasketIndexImage(String basketIndexImage) {
        this.basketIndexImage = basketIndexImage;
    }

    public String getBasketStatement() {
        return basketStatement;
    }

    public void setBasketStatement(String basketStatement) {
        this.basketStatement = basketStatement;
    }

    public String getWorkStatement() {
        return workStatement;
    }

    public void setWorkStatement(String workStatement) {
        this.workStatement = workStatement;
    }

}
