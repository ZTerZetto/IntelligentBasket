package com.automation.zzx.intelligent_basket_demo.entity.enums;

public enum CardType {
    WELDCUT("weld_cut","焊切割操作证",1),
    HIGHWORK("high_work","高空作业证",2),
    BASKETOPERATE("curtain_glassPlate","吊篮操作证",3);

    // 定义示例变量
    private String english; //具体类型
    private String chinese; //具体类型中文
    private int type; //工种类型中文


    //构造函数,这里只能用private修饰
    //注意，这里只能同时存在一种构造函数，因为目前是有参，所以我们得把枚举类，也就是上面的ONE,TWO也改成有参。
    private CardType(String english,String chinese,int type){
        this.english = english;
        this.chinese = chinese;
        this.type = type;
    }

    public String getEnglish() {
        return english;
    }

    public void setEnglish(String english) {
        this.english = english;
    }

    public String getChinese() {
        return chinese;
    }

    public void setChinese(String chinese) {
        this.chinese = chinese;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public static CardType getByType(int type){

        for (CardType cardType : values()) {
            if(cardType.getType() == type){
                return cardType;
            }
        }
        return null;
    }

    public static CardType getByChinese(String chinese){

        for (CardType cardType : values()) {
            if(cardType.getChinese().equals(chinese)){
                return cardType;
            }
        }
        return null;
    }

}
