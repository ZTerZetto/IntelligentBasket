package com.automation.zzx.intelligent_basket_demo.entity;

public class AppConfig {
    /*
     * 服务器地址
     */
    public final static String ANDROID_URL_PATH = "http://39.98.115.183";  // 阿里云
//    public final static String ANDROID_URL_PATH = "http://10.193.118.189:8080"; // 老刘
    public final static String FILE_SERVER_YBLIU_IP = "39.99.158.73";  // 刘跃博FTP文件服务器
    public final static int FILE_SERVER_YBLIU_PORT = 21;  // 刘跃博FTP文件服务器的端口
    public final static String FILE_SERVER_YBLIU_PATH = "http://39.99.158.73:8082/var/ftp/smartNacelle/"; // 刘跃博HTTP文件服务器地址
    //public final static String FILE_SERVER_YBLIU_PATH_SMART = "http://47.100.1.211:8082/smartNacelle/"; // 刘跃博HTTP文件服务器地址_安装队伍相关
    public final static String COMMUNICATION_SERVER_PATH = "http://39.98.115.183:8081"; // 通讯服务器地址
//    public final static String COMMUNICATION_SERVER_PATH = "http://10.193.7.58:8081"; // 通讯服务器地址-老刘暂时
    public final static String VIDEO_STREAM_PATH = "rtmp://47.96.103.244:1935"; // 流媒体服务器地址

    /*
     * 账户和密码
     */
    public final static String FILE_SERVER_USERNAME = "root";
    public final static String FILE_SERVER_PASSWORD = "nishipig2/";

    /*
     * 萤石云：appkey
     */
    public final static String EZUIKit_APPKEY = "6747c45b0baf43868d88e34748c742e7";
    public final static String EZUIKit_SECRET = "3b3d8db1a048dd9e197711b37ecb6c42";
    public final static String EZUIKit_AccessToken = "at.dq2en1fi9mpkqqzrdaqjhk392c7es89x-2h6a9c2x9j-1ry029z-abzmupcyx";

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


    //修改密码
    public final static String UPDATE_PASSWORD = ANDROID_URL_PATH.concat("/updatePassword");

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


    /*
     * 吊篮请求
     */
    // 获取吊篮实施参数
    public static final String REAL_TIME_PARAMETER = ANDROID_URL_PATH.concat("/getRealTimeData");
    // 获取吊篮设备序列号
    public static final String GET_ELECTRICBOX_CONFIG = ANDROID_URL_PATH.concat("/getElectricBoxConfig");

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
    // 打开/关闭吊篮设备
    public static final String HANGING_BASKET_OPERATION = COMMUNICATION_SERVER_PATH.concat("/sendToDevice");
    /* 萤石云
     */
    public static final String GET_UIKIT_ACCESS_TOKEN = "https://open.ys7.com/api/lapp/token/get";
    public static final String GET_UIKIT_VIDEO_URL = "https://open.ys7.com/api/lapp/live/video/list";
    //获取现场编号
    public static final String GET_SITENO = ANDROID_URL_PATH.concat("/getSiteNo");



    /*
     * 施工人员活动请求
     */
    // 施工人员基本信息
    public static final String WORKER_ALL_INFO = ANDROID_URL_PATH.concat("/androidGetUserInfo");
    // 施工人员上工
    public static final String WORKER_BEGIN_WORK = ANDROID_URL_PATH.concat("/androidBeginWork");
    // 施工人员下工
    public static final String WORKER_END_WORK = ANDROID_URL_PATH.concat("/androidEndWork");
    // 施工人员更新资质证书
    public static final String WORKER_UPDATE_CAPACITY_IMAGE = ANDROID_URL_PATH.concat("/updateQualifications");
    // 施工人员资质证书URL
    public static final String WORKER_GET_CAPACITY_IMAGE = ANDROID_URL_PATH.concat("/getQualifications");
    // 施工人员查看历史上工记录
    public static final String WORKER_GET_WORE_TIME = ANDROID_URL_PATH.concat("/getWorkerTime");

    /*
     * 租方管理员活动请求
     */
    // 施工人员基本信息
    public static final String RENT_ADMIN_ALL_INFO = ANDROID_URL_PATH.concat("/androidGetUserInfo");
    // 租方管理员请求所有可预报停的吊篮信息
    public static final String RENT_ADMIN_MG_ALL_PRE_STOP_BASKET_INFO = ANDROID_URL_PATH.concat("/forecastStop");
    // 租方管理员请求预报停
    public static final String RENT_ADMIN_APPLY_PRE_STOP_BASKETS = ANDROID_URL_PATH.concat("/prepareEnd");
    // 租方管理员请求报停
    public static final String RENT_ADMIN_APPLY_STOP_BASKETS = ANDROID_URL_PATH.concat("/storageEnd");
    // 租方管理员请求所有施工人员
    public static final String RENT_ADMIN_GET_ALL_WORKER_INFO = ANDROID_URL_PATH.concat("/getUserList");
    // 租方管理员添加施工人员
    public static final String RENT_ADMIN_ADD_WORKER= ANDROID_URL_PATH.concat("/androidIncreaseWorker");
    // 租方管理员删除施工人员
    public static final String RENT_ADMIN_DELETE_WORKER= ANDROID_URL_PATH.concat("/deleteUser");
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
    public static final String AREA_ADMIN_GET_ALL_BASKET_INFO = ANDROID_URL_PATH.concat("/getBasketListByAdmin");
    // 区域管理员请求特定项目添加指定吊篮
    public static final String AREA_ADMIN_ADD_BASKET_INTO_PROJECT = ANDROID_URL_PATH.concat("/androidIncreaseBasket");
    // 区域管理员上传安监证书
    public static final String AREA_ADMIN_CREATE_CERT_FILE = ANDROID_URL_PATH.concat("/createCertFile");
    // 区域管理员上传配置清单
    public static final String AREA_ADMIN_CONFIGURATION = ANDROID_URL_PATH.concat("/pushConfigurationList");
    // 区域管理员上传项目预安装验收启动图片
    public static final String AREA_ADMIN_CREATE_PREINSTALL_FILE = ANDROID_URL_PATH.concat("/createProjectFile");
    // 区域管理员请求安装预检申请
    public static final String AREA_ADMIN_APPLY_INSTALL = ANDROID_URL_PATH.concat("/installApply");
    // 区域管理员请求预验收申请
    public static final String AREA_ADMIN_CHECK_INSTALL = ANDROID_URL_PATH.concat("/installCheck");
    // 区域管理员请求项目开始
    public static final String AREA_ADMIN_BEGIN_PROJECT = ANDROID_URL_PATH.concat("/beginProject");
    // 区域管理员请求吊篮预报停(丢弃)
    public static final String AREA_ADMIN_PREPARE_STOP_DEVICE = ANDROID_URL_PATH.concat("/storageControl");
    // 区域管理员上传预报停信息
    public static final String AREA_ADMIN_SEND_PRE_STOP_INFO = ANDROID_URL_PATH.concat("/createPreStop");
    // 区域管理员请求报修信息（未结束）
    public static final String AREA_ADMIN_GET_REPAIR_INFO = ANDROID_URL_PATH.concat("/getRepairBox");
    // 区域管理员请求报修信息（已经结束）
    public static final String AREA_ADMIN_GET_REPAIR_END_INFO = ANDROID_URL_PATH.concat("/getRepairEndBox");
    // 请求报警记录（全项目记录or单吊篮记录）
    public static final String GET_ALARM_INFO = ANDROID_URL_PATH.concat("/getAlarmInfo");
    // 请求报停记录
    public static final String GET_STOP_RECORD = ANDROID_URL_PATH.concat("/getElectricStopInfo");
    // 请求操作人员（根据吊篮ID）
    public static final String GET_WORKERS_BY_BASKET = ANDROID_URL_PATH.concat("/getWorker");
    // 请求操作人员（根据吊篮ID）
    public static final String CREATE_INSTALL_INFO = ANDROID_URL_PATH.concat("/createInstallInfo");

    // 区域管理员根据项目获取获取安装队伍信息
    public static final String GET_PROJECT_BY_PROJECTID = ANDROID_URL_PATH.concat("/getProjectInstallInfoByProjectId");

    //获取工程方案（平面图坐标信息）
    public static final String GET_PLANE_GRAPH_INFO = ANDROID_URL_PATH.concat("/getPlaneGraphInfo");

    /*
     * 项目管理员请求
     */
    // 项目管理员基本信息
    public static final String PRO_ADMIN_ALL_INFO = ANDROID_URL_PATH.concat("/androidGetUserInfo");
    // 项目管理员请求项目信息
    public static final String PRO_ADMIN_GET_PROINFO = ANDROID_URL_PATH.concat("/getProjectByProAdmin");
    // 项目管理员请求单个报修信息
    public static final String PRO_ADMIN_GET_REPAIR_SINGLE = ANDROID_URL_PATH.concat("/getRepairBoxOne");
    // 项目管理员请求单个修复信息
    public static final String PRO_ADMIN_GET_REPAIR_END_SINGLE = ANDROID_URL_PATH.concat("/createRepairEndBox");


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
    // 巡检人员上传异常吊篮信息
    public static final String INSPECTION_PERSON_EXCEPTION_REPORT = ANDROID_URL_PATH.concat("/createExceptionBox");
    // 巡检人员查看项目清单
    public static final String INSPECTION_PERSON_CHECK_CONFIGURATION_LIST = ANDROID_URL_PATH.concat("/getConfigurationList");
    // 巡检人员请求所有吊篮信息
    public static final String RENT_ADMIN_MG_ALL_BASKET_INFO = ANDROID_URL_PATH.concat("/getBasketList");

    /*
    * 常量
    * */
    public static final String INTENT_USER = "Intent_userInfo";
    public static final Integer MAX_SELECT_PIC_NUM = 5;
}