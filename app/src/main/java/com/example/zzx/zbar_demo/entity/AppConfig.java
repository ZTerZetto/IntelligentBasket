package com.example.zzx.zbar_demo.entity;

// Created by $USER_NAME on 2018/11/28/028.
public class AppConfig {
    /*
     * 服务器地址
     */
    public final static String BASE_URL_PATH = "http://47.100.1.211";// 后台地址
    public final static String ANDROID_URL_PATH = "http://10.193.2.79:8080";// ANDROID后台地址
    public final static String IMAGE_URL = "http://10.193.0.20:21";
    public final static String FILE_SERVER_PATH = "http://10.193.0.20:8089"; // 文件服务器地址
    public final static String VIDEO_STREAM_PATH = "rtmp://47.96.103.244:1935"; // 流媒体服务器地址

    /* 登陆
    * userId、userPassword
    * Authorization：NULL
    * POST
    * */
    public final static String LOGIN_USER = ANDROID_URL_PATH.concat("/androidLogin");

    /* 注册
    * userId、userName、userPassword、userRole、userPhone、userImage;
    * Authorization：NULL
    * POST
    * */
    public final static String REGISTER_USER = ANDROID_URL_PATH.concat("/checkRegister");

    //上传文件
    public final static String CREATE_FILE = FILE_SERVER_PATH.concat("/createFtpFile");


    /* 获取当前登录者的用户名和角色
   * Authorization：TOKEN
   * POST
   * */
    public final static String USER_INFO = ANDROID_URL_PATH.concat("/getUserInfo");



    /* 获取不同状态下的项目
    * userFlag: 1代表正在进行中的项目
    *  GET
    * */
    public final static String PROINFO = ANDROID_URL_PATH.concat("/projectInfo");

    /* 获取被点击项目的详细信息
    * projectId
    * GET
    * */
    public final static String PRO_DETAIL = ANDROID_URL_PATH.concat("/projectDetailInfo");


    /* 获取吊篮实施参数
     */
    public static final String REAL_TIME_PARAMETER = BASE_URL_PATH.concat("/getRealTimeData");

    /* 获取流媒体视频
     */
    // 视频播放器窗口纵横比
    public static final float ASPECT_RATIO = (float)1.7777777777777777;  // 16:9
    // 视频纵横比
    public static final float ASPECT_RATIO_VIDEO = (float)1.333333333333;  // 4:3
    public static final String HANGING_BASKET_VIDEO = VIDEO_STREAM_PATH.concat("/sendToDevice");
}