package com.example.zzx.zbar_demo.utils;

import android.net.Uri;

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

import static com.example.zzx.zbar_demo.entity.AppConfig.REAL_TIME_PARAMETER;

// Created by $USER_NAME on 2018/11/28/028.
public class HttpUtil {
    public static void uploadPicOkHttpRequest(okhttp3.Callback callback, File file) {
        OkHttpClient client = new OkHttpClient();
        RequestBody fileBody = RequestBody.create(MediaType.parse("image/jpg"), file);
        RequestBody builder = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("fileName", file.getName())
                .addFormDataPart("file", file.getName(), fileBody)
                .build();
        final Request request = new Request.Builder()
                .url(AppConfig.CREATE_FILE)
                .post(builder)
                .build();
        client.newCall(request).enqueue(callback);
    }

    public static void sendLoginOkHttpRequest(okhttp3.Callback callback, String json) {
        OkHttpClient client = new OkHttpClient();
        RequestBody requestBody = FormBody.create(MediaType.parse("application/json; charset=utf-8"), json);
        final Request request = new Request.Builder()
                .url(AppConfig.LOGIN_USER)
                .addHeader("Authorization", "null")
                .post(requestBody)
                .build();
        client.newCall(request).enqueue(callback);
    }

    public static void sendRegistOkHttpRequest(okhttp3.Callback callback, String json) {
        OkHttpClient client = new OkHttpClient();
        RequestBody requestBody = FormBody.create(MediaType.parse("application/json; charset=utf-8"), json);
        final Request request = new Request.Builder()
                .url(AppConfig.REGISTER_USER)
                .addHeader("Authorization", "null")


                .post(requestBody)
                .build();
        client.newCall(request).enqueue(callback);
    }


    public static void getUserInfoOkHttpRequest(okhttp3.Callback callback, String token) {
        OkHttpClient client = new OkHttpClient();

        FormBody builder = new FormBody.Builder().build();
        final Request request = new Request.Builder()
                .url(AppConfig.USER_INFO)
                .addHeader("Authorization", token)
                .post(builder)
                .build();
        client.newCall(request).enqueue(callback);
    }

    public static void getProjectInfoOkHttpRequest(okhttp3.Callback callback, String token, String userFlag) {
        OkHttpClient client = new OkHttpClient();
        final Request request = new Request.Builder()
                .url(AppConfig.PROINFO + "?userFlag=" + userFlag)
                .addHeader("Authorization", token)
                .get()
                .build();
        client.newCall(request).enqueue(callback);
    }

    public static void getProjectDetailInfoOkHttpRequest(okhttp3.Callback callback, String token, String projectId) {
        OkHttpClient client = new OkHttpClient();
        final Request request = new Request.Builder()
                .url(AppConfig.PRO_DETAIL + "?projectId=" + projectId)
                .addHeader("Authorization", token)
                .get()
                .build();
        client.newCall(request).enqueue(callback);
    }


    public static void sendLogin2OkHttpRequest(okhttp3.Callback callback, UserInfo userInfo) {
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

    /*
     * 设备参数请求
     */
    public static void getDeviceParameterOkHttpRequest(okhttp3.Callback callback,
                                                       String token, String deviceId) {
        OkHttpClient client = new OkHttpClient();

        StringBuilder tempParams = new StringBuilder();
        //对参数进行URLEncoder
        tempParams.append(String.format("%s=%s", "deviceId", Uri.encode(deviceId), "utf-8"));
        String requestUrl = String.format("%s?%s", REAL_TIME_PARAMETER, tempParams.toString());
        final Request request = new Request.Builder()
                .url(requestUrl)
                .addHeader("Authorization", token)
                .get()
                .build();
        client.newCall(request).enqueue(callback);
    }

    /*
     * 施工人员请求开始
     */
    /*
     * 施工人员全部信息
     * post
     * header:token
     * key: userId
     */
    public static void getWorkerAllInfoOkHttpRequest(okhttp3.Callback callback,
                                                       String token, String userId){
        OkHttpClient client = new OkHttpClient();
        FormBody builder = new FormBody.Builder()
                .add("userId", userId)
                .build();
        final Request request = new Request.Builder()
                .url(AppConfig.WORKER_ALL_INFO)
                .addHeader("Authorization", token)
                .post(builder)
                .build();
        client.newCall(request).enqueue(callback);
    }
    /*
     * 施工人员上下工请求
     * token
     * url
     * userid basketid projectId
     */
    public static void workerBeginOrEndWorkOkHttpRequest(okhttp3.Callback callback,
                                                         String url, String token, String userId,
                                                         String basketId, String projectId){
        OkHttpClient client = new OkHttpClient();
        FormBody builder = new FormBody.Builder()
                .add("userId", userId)
                .add("boxId", basketId)
                .add("projectId", projectId)
                .build();
        final Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", token)
                .post(builder)
                .build();
        client.newCall(request).enqueue(callback);
    }
    /*
     * 施工人员请求结束
     */

}
