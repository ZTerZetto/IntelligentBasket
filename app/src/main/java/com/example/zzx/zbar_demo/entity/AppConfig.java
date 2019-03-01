package com.example.zzx.zbar_demo.entity;

// Created by $USER_NAME on 2018/11/28/028.
public class AppConfig {
    public final static String BASE_URL_PATH = "http://47.100.1.211";

    /* 登陆
    * userId、userPassword
    * Authorization：NULL
    * POST
    * */
    public final static String LOGIN_USER = BASE_URL_PATH.concat("/webLogin");

    /* 登出
    * userId
    * Authorization：NULL
    * GET
    * */
    public final static String LOGOUT_USER = BASE_URL_PATH.concat("/quitLoad");


    /* 获取当前登录者的用户名和角色
   * Authorization：TOKEN
   * POST
   * */
    public final static String USER_INFO = BASE_URL_PATH.concat("/getUserInfo");

    /* 注册
    * userId、userName、userPassword、userRole、userPhone、userImage;
    * POST
    * */
    public final static String REGISTER = BASE_URL_PATH.concat("/checkRegister");

    //上传文件
    public final static String CREAT_FILE = BASE_URL_PATH.concat("/createFile");


    /* 获取当前登录者的用户名和角色


    * 带上Authorization头部
    *  post
    * */
    public final static String USERINFO = BASE_URL_PATH.concat("/getUserInfo");

    /* 获取不同状态下的项目
    * userFlag: 1代表正在进行中的项目
    *  GET
    * */
    public final static String PROINFO = BASE_URL_PATH.concat("/projectInfo");

    /* 获取被点击项目的详细信息
    * projectId
    * GET
    * */
    public final static String PRO_DETAIL = BASE_URL_PATH.concat("/projectDetailInfo");


    // 视频播放器纵横比
    public static final float ASPECT_RATIO = (float)1.7777777777777777;  // 16:9




}