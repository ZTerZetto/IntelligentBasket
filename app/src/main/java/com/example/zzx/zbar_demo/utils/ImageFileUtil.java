package com.example.zzx.zbar_demo.utils;

import android.graphics.Bitmap;
import android.os.Environment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by pengchenghu on 2018/12/11.
 * Author Email: 15651851181@163.com
 * Describe:
 */
public class ImageFileUtil {
    public static final String PACKAGE_NAME = "example.zzx.zbar_demo";
    public static final String IMAGE_DIRECTORY_NAME = "Images";

    // byte数组到图片到硬盘上
    public static boolean byte2image(byte[] data){
        if(data.length<3) return false;//判断输入的byte是否为空

        File directory = createDirectory();
        String filename = nameSaveFile();
        File file = new File(directory, filename);

        try{
            FileOutputStream imageOutput = new FileOutputStream(file);//打开输入流
            imageOutput.write(data, 0, data.length);//将byte写入硬盘
            imageOutput.flush();
            imageOutput.close();
        } catch(Exception ex) {
            ex.printStackTrace();
            return false;
        }
        return true;
    }

    // 将Bitmap 文件写入手机卡,位置：外部存储/PACKAGE_NAME/Image/
    public static boolean writeBitmapToDisk(Bitmap bitmap){
        boolean result = true;

        File directory = createDirectory();
        String filename = nameSaveFile();
        File file = new File(directory, filename);

        try{
            if(file.exists())
                file.delete();
            file.createNewFile();
            FileOutputStream fos = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();
        }catch (IOException e){
            e.printStackTrace();
            return false;
        }

        return result;
    }

    // 图片位置
    private static File createDirectory(){
        String externalStorage = Environment.getExternalStoragePublicDirectory("").toString();
        String saveDir = externalStorage + "/" + PACKAGE_NAME + "/" + IMAGE_DIRECTORY_NAME;
        File directory = new File(saveDir);
        if(!directory.exists())
            directory.mkdirs();
        return directory;
    }

    // 文件命名
    private static String nameSaveFile(){
        long l = System.currentTimeMillis();
        Date date = new Date(); //new日期对象
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
        String filename = dateFormat.format(date)+".jpg";
        return filename;
    }

}
