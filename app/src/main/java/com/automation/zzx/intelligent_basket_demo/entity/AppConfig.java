package com.automation.zzx.intelligent_basket_demo.entity;

public class AppConfig {
    /*
     * 服务器地址
     */
    public final static String BASE_URL_PATH = "http://47.100.1.211";// 后台地址
    //public final static String ANDROID_URL_PATH = "http://47.100.1.211";  // 阿里云
    //public final static String ANDROID_URL_PATH = "http://10.193.1.44:8080";  // 师姐
    public final static String ANDROID_URL_PATH = "http://10.193.6.159:8080"; // 老刘
    public final static String FILE_SERVER_YBLIU_IP = "47.100.1.211";  // 刘跃博FTP文件服务器
    public final static int FILE_SERVER_YBLIU_PORT = 21;  // 刘跃博FTP文件服务器的端口
    public final static String FILE_SERVER_YBLIU_PATH = "http://47.100.1.211:8082"; // 刘跃博HTTP文件服务器地址
    //public final static String COMMUNICATION_SERVER_PATH = "http://47.100.1.211:8081"; // 通讯服务器地址
    public final static String COMMUNICATION_SERVER_PATH = "http://10.193.6.159:8081"; // 通讯服务器地址-老刘暂时
    public final static String VIDEO_STREAM_PATH = "rtmp://47.96.103.244:1935"; // 流媒体服务器地址

    /*
     * 账户和密码
     */
    public final static String FILE_SERVER_USERNAME = "root";
    public final static String FILE_SERVER_PASSWORD = "nishipig2/";

    /* 登陆
    * userId、userPassword
    * Authorization：NULL
    * POST
    * */
    public final static String LOGIN_USER = ANDROID_URL_PATH.concat("/login");

    /* 判断是否是项目管理员
     * userId
     * Authorization：token
     * POST
     * */
    public final static String JUDGE_PROADMIN = ANDROID_URL_PATH.concat("/judgeProAdmin");

    /* 注册
    */
    public final static String REGISTER_USER = ANDROID_URL_PATH.concat("/checkRegister");

    //上传单个文件
    public final static String CREATE_IDENTITY_CARD_IMAGE = ANDROID_URL_PATH.concat("/createImageFile");

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
    //视频推流地址+电柜参数设置
    public static final String HANGING_BASKET_VIDEO = COMMUNICATION_SERVER_PATH.concat("/sendToDevice");
    //电柜参数获取
    public static final String HANGING_BASKET_PARAM = COMMUNICATION_SERVER_PATH.concat("/getSetUpData");

    /*
     * 施工人员活动请求
     */
    // 施工人员基本信息
    public static final String WORKER_ALL_INFO = ANDROID_URL_PATH.concat("/androidGetUserInfo");
    // 施工人员上工
    public static final String WORKER_BEGIN_WORK = ANDROID_URL_PATH.concat("/androidBeginWork");
    // 施工人员下工
    public static final String WORKER_ENG_WORK = ANDROID_URL_PATH.concat("/androidEndWork");

    /*
     * 租方管理员活动请求
     */
    // 施工人员基本信息
    public static final String RENT_ADMIN_ALL_INFO = ANDROID_URL_PATH.concat("/androidGetUserInfo");
    // 租方管理员请求所有吊篮信息
    public static final String RENT_ADMIN_MG_ALL_BASKET_INFO = ANDROID_URL_PATH.concat("/forecastStop");
    // 租方管理员请求预报停
    public static final String RENT_ADMIN_APPLY_PRE_STOP_BASKETS = ANDROID_URL_PATH.concat("/prepareEnd");
    // 租方管理员请求报停
    public static final String RENT_ADMIN_APPLY_STOP_BASKETS = ANDROID_URL_PATH.concat("/storageEnd");
    // 租方管理员请求所有施工人员
    public static final String RENT_ADMIN_GET_ALL_WORKER_INFO = ANDROID_URL_PATH.concat("/getUserList");
    // 租方管理员添加施工人员
    public static final String RENT_ADMIN_ADD_WORKER= ANDROID_URL_PATH.concat("/androidIncreaseWorker");
    // 租方管理员报修某个吊篮
    public static final String RENT_ADMIN_REPARI_BASKET= ANDROID_URL_PATH.concat("/createRepairBox");

    /*
     * 区域管理员请求
     */
    // 施工人员基本信息
    public static final String AREA_ADMIN_ALL_INFO = ANDROID_URL_PATH.concat("/androidGetUserInfo");
    // 区域管理员请求所有的项目信息
    public static final String AREA_ADMIN_GET_ALL_PROJECT_INFO = ANDROID_URL_PATH.concat("/getAllProject");
    // 区域管理员请求特定项目的所有吊篮
    public static final String AREA_ADMIN_GET_ALL_BASKET_INFO = ANDROID_URL_PATH.concat("/getBasketList");
    // 区域管理员请求特定项目添加指定吊篮
    public static final String AREA_ADMIN_ADD_BASKET_INTO_PROJECT = ANDROID_URL_PATH.concat("/androidIncreaseBasket");
    // 区域管理员上传安监证书
    public static final String AREA_ADMIN_CREATE_CERT_FILE = ANDROID_URL_PATH.concat("/createCertFile");
    // 区域管理员上传项目预安装验收启动图片
    public static final String AREA_ADMIN_CREATE_PREINSTALL_FILE = ANDROID_URL_PATH.concat("/createProjectFile");
    // 区域管理员请求预验收申请
    public static final String AREA_ADMIN_APPLY_INSTALL = ANDROID_URL_PATH.concat("/installApply");
    // 区域管理员请求项目开始
    public static final String AREA_ADMIN_BEGIN_PROJECT = ANDROID_URL_PATH.concat("/beginProject");
    // 区域管理员请求吊篮预报停(丢弃)
    public static final String AREA_ADMIN_PREPARE_STOP_DEVICE = ANDROID_URL_PATH.concat("/storageControl");
    // 区域管理员上传预报停信息
    public static final String AREA_ADMIN_SEND_PRE_STOP_INFO = ANDROID_URL_PATH.concat("/createPreStop");

    /*
     * 项目负责人请求
     */
    // 项目负责人基本信息
    public static final String PRO_ADMIN_ALL_INFO = ANDROID_URL_PATH.concat("/androidGetUserInfo");
    // 项目负责人请求项目信息
    public static final String PRO_ADMIN_GET_PROINFO = ANDROID_URL_PATH.concat("/getProjectByProAdmin");



    /*
     * 巡检人员请求
     */
    // 巡检人员获取项目信息
    public static final String INSPECTION_PERSON_GET_PRO_INFO = ANDROID_URL_PATH.concat("/projectDetailInfo");
    // 巡检人员获取项目吊篮列表
    public static final String INSPECTION_PERSON_GET_BASKET_LIST_INFO = ANDROID_URL_PATH.concat("/getBasketList");
    // 巡检人员将吊篮出库
    public static final String INSPECTION_PERSON_OUT_STORAGE = ANDROID_URL_PATH.concat("/androidIncreaseBasket");
    // 巡检人员将吊篮入库
    public static final String INSPECTION_PERSON_IN_STORAGE = ANDROID_URL_PATH.concat("/storageIn");

    /*
    * 常量
    * */
    public static final String INTENT_USER = "Intent_userInfo";
    public static final Integer MAX_SELECT_PIC_NUM = 5;
}