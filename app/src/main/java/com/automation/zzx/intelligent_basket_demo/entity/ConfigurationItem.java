package com.automation.zzx.intelligent_basket_demo.entity;

/**
 * Created by pengchenghu on 2019/5/27.
 * Author Email: 15651851181@163.com
 * Describe: 配置清单条目
 */
public class ConfigurationItem {

    private String name;  // 条目名称
    private String number; // 条目数量
    private String unit; // 条目单位

    /*
     * 构造函数
     */
    public ConfigurationItem(){ }
    public ConfigurationItem(String name, String number, String unit){
        this.name = name;
        this.number = number;
        this.unit = unit;
    }

    /*
     * Bean 函数
     */

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getNumber() {
        return number;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getUnit() {
        return unit;
    }
}
