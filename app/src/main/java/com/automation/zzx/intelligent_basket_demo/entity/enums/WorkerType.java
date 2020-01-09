package com.automation.zzx.intelligent_basket_demo.entity.enums;

public enum WorkerType {
    ELECTRIC("curtain_electricWorker","电焊工","幕墙类"),
    STONE("curtain_stoneWorker","石材工","幕墙类"),
    GLASSPLATE("curtain_glassPlate","玻璃铝板工","幕墙类"),
    GLUEWORKER("curtain_glueWorker","打胶工","幕墙类"),
    PAINTER("coating_painter","粉刷工","涂料类"),
    REALSTONE("coating_realStone","真石漆类","涂料类"),
    OTHERS("others_others","其他类","其他类");



    // 定义示例变量
    private String detailtype;    //具体类型
    private String chineseDetail; //具体类型中文
    private String chineseType; //工种类型中文


    //构造函数,这里只能用private修饰
    //注意，这里只能同时存在一种构造函数，因为目前是有参，所以我们得把枚举类，也就是上面的ONE,TWO也改成有参。
    private WorkerType(String detailtype,String chineseDetail,String chineseType){
        this.detailtype = detailtype;
        this.chineseDetail = chineseDetail;
        this.chineseType = chineseType;
    }

    public String getDetailtype() {
        return detailtype;
    }

    public void setDetailtype(String detailtype) {
        this.detailtype = detailtype;
    }

    public String getChineseDetail() {
        return chineseDetail;
    }

    public void setChineseDetail(String chineseDetail) {
        this.chineseDetail = chineseDetail;
    }

    public String getChineseType() {
        return chineseType;
    }

    public void setChineseType(String chineseType) {
        this.chineseType = chineseType;
    }

    public String typeToChineseDetail(String type){
        String chinese = "";
        switch(type){
            case "curtain_electricWorker":
                chinese =  ELECTRIC.getChineseDetail();
                break;
            case "curtain_stoneWorker":
                chinese = STONE.getChineseDetail();
                break;
            case "curtain_glassPlate":
                chinese = GLASSPLATE.getChineseDetail();
                break;
            case "curtain_glueWorker":
                chinese = GLUEWORKER.getChineseDetail();
                break;
            case "coating_painter":
                chinese = PAINTER.getChineseDetail();
                break;
            case "coating_realStone":
                chinese = REALSTONE.getChineseDetail();
                break;
            case "others_others":
                chinese = OTHERS.getChineseDetail();
                break;
        }
        return chinese;
    }

    public String typeToChinese(String type){
        String chinese = "";
        switch(type){
            case "curtain_electricWorker":
            case "curtain_stoneWorker":
            case "curtain_glassPlate":
            case "curtain_glueWorker":
                chinese = ELECTRIC.getChineseType();
                break;
            case "coating_painter":
            case "coating_realStone":
                chinese = PAINTER.getChineseType();
                break;
            case "others_others":
                chinese = OTHERS.getChineseType();
                break;
        }
        return chinese;
    }

    public static WorkerType getByDetailtype(String type){

        for (WorkerType workerType : values()) {
            if(workerType.getDetailtype().equals(type)){
                return workerType;
            }
        }
        return null;
    }

    public static WorkerType getByChineseDetail(String type){

        for (WorkerType workerType : values()) {
            if(workerType.getChineseDetail().equals(type)){
                return workerType;
            }
        }
        return null;
    }

}
