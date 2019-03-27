package com.example.zzx.zbar_demo.entity;

/**
 * Created by pengchenghu on 2019/3/27.
 * Author Email: 15651851181@163.com
 * Describe: 区域管理员更新操作吊篮出/入库状态
 */
public class MgBasketStatement {

    private String basketId; // 吊篮ID
    private String basketIndexImage; // 首页数据
    /*
     * 0: 在仓库中
     * 1：待分配
     * 2：待安装
     * 3：待审核
     * 4：使用中
     * 5：待报停
     */
    private String basketStatement; // 吊篮出/入状态

    /*
     * 构造函数
     */
    public MgBasketStatement(){}

    public MgBasketStatement(String basketId, String basketIndexImage, String basketStatement){
        this.basketId = basketId;
        this.basketIndexImage = basketIndexImage;
        this.basketStatement = basketStatement;
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
}
