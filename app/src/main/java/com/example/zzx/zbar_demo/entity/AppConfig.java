package com.example.zzx.zbar_demo.entity;

// Created by $USER_NAME on 2018/11/28/028.
public class AppConfig {
    public final static String BASE_URL_PATH = "http://10.193.2.79:8080";
    //用户登入
    public final static String LOGIN_USER = BASE_URL_PATH.concat("/webLogin");

    //上传文件
    public final static String CREAT_FILE = BASE_URL_PATH.concat("/createFile");


}