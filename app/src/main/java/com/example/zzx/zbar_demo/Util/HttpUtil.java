package com.example.zzx.zbar_demo.Util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.example.zzx.zbar_demo.entity.AppConfig;
import com.example.zzx.zbar_demo.entity.UserInfo;
import java.io.File;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

// Created by $USER_NAME on 2018/11/28/028.
public class HttpUtil {
    public static void uploadPicOkHttpRequest(okhttp3.Callback callback, File file){
        OkHttpClient client = new OkHttpClient();
        RequestBody fileBody=RequestBody.create(MediaType.parse("image/jpg"),file);
        RequestBody builder = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("fileName",file.getName())
                .addFormDataPart("file",file.getName(),fileBody)
                .build();
        final Request request = new Request.Builder()
                .url(AppConfig.CREATE_FILE)
                .post(builder)
                .build();
        client.newCall(request).enqueue(callback);
    }

    public static void sendLoginOkHttpRequest(okhttp3.Callback callback,String json){
        OkHttpClient client = new OkHttpClient();
        RequestBody requestBody = FormBody.create(MediaType.parse("application/json; charset=utf-8"), json);
        final Request request = new Request.Builder()
                .url(AppConfig.LOGIN_USER)
                .addHeader("Authorization","null")
                .post(requestBody)
                .build();
        client.newCall(request).enqueue(callback);
    }

    public static void sendRegistOkHttpRequest(okhttp3.Callback callback,String json){
        OkHttpClient client = new OkHttpClient();
        RequestBody requestBody = FormBody.create(MediaType.parse("application/json; charset=utf-8"), json);
        final Request request = new Request.Builder()
                .url(AppConfig.REGISTER_USER)
                .addHeader("Authorization","null")


                .post(requestBody)
                .build();
        client.newCall(request).enqueue(callback);
    }


    public static void getUserInfoOkHttpRequest(okhttp3.Callback callback,String token){
        OkHttpClient client = new OkHttpClient();

        FormBody builder = new FormBody.Builder().build();
        final Request request = new Request.Builder()
                .url(AppConfig.USER_INFO)
                .addHeader("Authorization",token)
                .post(builder)
                .build();
        client.newCall(request).enqueue(callback);
    }

    public static void getProjectInfoOkHttpRequest(okhttp3.Callback callback,String token,String userFlag){
        OkHttpClient client = new OkHttpClient();
        final Request request = new Request.Builder()
                .url(AppConfig.PROINFO+"?userFlag="+userFlag)
                .addHeader("Authorization",token)
                .get()
                .build();
        client.newCall(request).enqueue(callback);
    }

    public static void getProjectDetailInfoOkHttpRequest(okhttp3.Callback callback,String token,String projectId){
        OkHttpClient client = new OkHttpClient();
        final Request request = new Request.Builder()
                .url(AppConfig.PRO_DETAIL+"?projectId=" + projectId)
                .addHeader("Authorization",token)
                .get()
                .build();
        client.newCall(request).enqueue(callback);
    }



    public static void sendLogin2OkHttpRequest(okhttp3.Callback callback, UserInfo userInfo){
        OkHttpClient client = new OkHttpClient();
        JSONObject jsonObject = JSON.parseObject(userInfo.toString());
        FormBody builder = new FormBody.Builder()
               /* .add(jsonObject)*/
                .add("userId", userInfo.getUserId())
                .add("userPassword", userInfo.getUserPassword())
                .build();
        final Request request = new Request.Builder()
                .url(AppConfig.LOGIN_USER)
                .post(builder)
                .build();
        client.newCall(request).enqueue(callback);
    }

}
