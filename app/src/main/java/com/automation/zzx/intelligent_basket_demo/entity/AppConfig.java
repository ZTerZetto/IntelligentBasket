package com.automation.zzx.intelligent_basket_demo.entity;

// Created by $USER_NAME on 2018/11/28/028.
public class AppConfig {
    /*
     * 服务器地址
     */
    public final static String BASE_URL_PATH = "http://47.100.1.211";// 后台地址
    //public final static String ANDROID_URL_PATH = "http://47.100.1.211";  // 阿里云
    //public final static String ANDROID_URL_PATH = "http://10.193.1.44:8080";  // 师姐
    public final static String ANDROID_URL_PATH = "http://10.193.6.159:8080"; // 老刘
    public final static String IMAGE_URL = "http://10.193.0.20:21";
    public final static String FILE_SERVER_PATH = "http://10.193.0.20:8089"; // 文件服务器地址
    public final static String COMMUNICATION_SERVER_PATH = "http://47.100.1.211:8081"; // 通讯服务器地址
    public final static String VIDEO_STREAM_PATH = "rtmp://47.96.103.244:1935"; // 流媒体服务器地址

    /* 登陆
    * userId、userPassword
    * Authorization：NULL
    * POST
    * */
    public final static String LOGIN_USER = ANDROID_URL_PATH.concat("/login");

    /* 注册
    * userId、userName、userPassword、userRole、userPhone、userImage;
    * Authorization：NULL
    * POST
    * */
    public final static String REGISTER_USER = ANDROID_URL_PATH.concat("/checkRegister");

    //上传单个文件
    public final static String CREATE_FILE = ANDROID_URL_PATH.concat("/createFtpFile");

    //上传多个文件
    public final static String CREATE_CERT_FILE = ANDROID_URL_PATH.concat("/createCertFile");


    /* 获取当前登录者的用户名和角色
   * Authorization：TOKEN
   * POST
   * */
    public final static String USER_INFO = ANDROID_URL_PATH.concat("/getUserInfo");



    /* 获取不同状态下的项目
    * userFlag: 1代表正在进行中的项目
    *  GET
    * */
    public final static String PRO_LIST = ANDROID_URL_PATH.concat("/projectInfo");

    /* 获取被点击项目的详细信息
    * projectId
    * GET
    * */
    public final static String PRO_DETAIL = ANDROID_URL_PATH.concat("/projectDetailInfo");


    /* 获取项目的吊篮列表
     * projectId
     * GET
     * */
    public final static String GET_DEVICE_LIST = ANDROID_URL_PATH.concat("/getBasketList");

    /* 获取项目的吊篮列表
     * projectId
     * GET
     * */
    public final static String GET_WORKER_LIST = ANDROID_URL_PATH.concat("/getUserList");


    /* 获取吊篮实施参数
     */
    public static final String REAL_TIME_PARAMETER = ANDROID_URL_PATH.concat("/getRealTimeData");


    /* 获取流媒体视频
     */
    // 视频播放器窗口纵横比
    public static final float ASPECT_RATIO = (float)1.7777777777777777;  // 16:9
    // 视频纵横比
    public static final float ASPECT_RATIO_VIDEO = (float)1.333333333333;  // 4:3
    public static final String HANGING_BASKET_VIDEO = COMMUNICATION_SERVER_PATH.concat("/sendToDevice");

    /*
     * 施工人员活动请求
     */
    // 施工人员基本信息
    public static final String WORKER_ALL_INFO = ANDROID_URL_PATH.concat("/androidGetWorker");
    // 施工人员上工
    public static final String WORKER_BEGIN_WORK = ANDROID_URL_PATH.concat("/androidBeginWork");
    // 施工人员下工
    public static final String WORKER_ENG_WORK = ANDROID_URL_PATH.concat("/androidEndWork");

    /*
     * 租方管理员活动请求
     */
    // 租方管理员请求所有吊篮信息
    public static final String RENT_ADMIN_MG_ALL_BASKET_INFO = ANDROID_URL_PATH.concat("/forecastStop");
    // 租方管理员请求吊篮预报停
    public static final String RENT_ADMIN_APPLY_PRE_STOP_BASKETS = ANDROID_URL_PATH.concat("/prepareEnd");
    // 租方管理员请求所有施工人员
    public static final String RENT_ADMIN_GET_ALL_WORKER_INFO = ANDROID_URL_PATH.concat("/getUserList");
    // 租方管理员添加施工人员
    public static final String RENT_ADMIN_ADD_WORKER= ANDROID_URL_PATH.concat("/androidIncreaseWorker");

    /*
     * 区域管理员请求
     */
    // 区域管理员请求所有的项目信息
    public static final String AREA_ADMIN_GET_ALL_PROJECT_INFO = ANDROID_URL_PATH.concat("/getAllProject");
    // 区域管理员请求特定项目的所有吊篮
    public static final String AREA_ADMIN_GET_ALL_BASKET_INFO = ANDROID_URL_PATH.concat("/getBasketList");
    // 区域管理员请求特定项目添加指定吊篮
    public static final String AREA_ADMIN_ADD_BASKET_INTO_PROJECT = ANDROID_URL_PATH.concat("/androidIncreaseBasket");

    /*
    * 常量
    * */
    public static final String INTENT_USER = "Intent_userInfo";
    public static final Integer MAX_SELECT_PIC_NUM = 5;

    /*
     * 公/局域 网切换
     * 默认是局域网
     */
}