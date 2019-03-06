package com.example.zzx.zbar_demo.activity.loginRegist;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.ContentUris;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.example.zzx.zbar_demo.R;
import com.example.zzx.zbar_demo.Util.HttpUtil;
import com.example.zzx.zbar_demo.Util.ToastUtil;
import com.example.zzx.zbar_demo.entity.UserInfo;
import com.google.gson.Gson;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Response;

public class RegistWorkerActivity extends AppCompatActivity {

    public static final int TAKE_PHOTO = 1;
    public static final int CHOOSE_PHOTO = 2;
    private ImageView picture;
    private Uri imageUri;
    private Bitmap bitmap;
    private File photo_file;
    private Boolean photo_exist;

    private TextView uploadResult;

    private EditText edt_userName;
    private EditText edt_userPhone;
    private EditText edt_userPwd;
    private EditText edt_userPwd_again;

    private Button takePhoto;
    private Button chooseFromAlbum;
    private Button upLoad ;
    private Button register;

    private UserInfo userinfo;
    private Spinner spinner;
    private List<String> type_list;
    private ArrayAdapter<String> typeAdapter;
    private String workerType;


    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0: {
                    uploadResult.setText("图片上传成功！");
                    break;
                }
                default: {
                    uploadResult.setText("图片上传失败，请重新上传！");
                    break;
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_regist_worker);

        // 顶部导航栏
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        TextView titleText = (TextView) findViewById(R.id.toolbar_title);
        toolbar.setTitle("");
        titleText.setText(getString(R.string.registWorker_title));
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true); //设置返回键可用

        takePhoto = (Button) findViewById(R.id.take_photo);
        chooseFromAlbum = (Button) findViewById(R.id.choose_from_album);
        upLoad = (Button)findViewById(R.id.btn_upload);
        register = findViewById(R.id.btn_regist);

        uploadResult = findViewById(R.id.tv_upload_result);
        picture = (ImageView) findViewById(R.id.picture);
        spinner = (Spinner) findViewById(R.id.spinner_type_choose);

        edt_userName = findViewById(R.id.edt_register_userName);
        edt_userPhone = findViewById(R.id.edt_register_userPhone);
        edt_userPwd = findViewById(R.id.edt_register_pwd);
        edt_userPwd_again = findViewById(R.id.edt_register_pwd_again);

        photo_exist = false;

        initRegister();


        takePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 创建File对象，用于存储拍照后的图片；
                //应用关联缓存目录“/sdcard/Android/data/<package name>/cache”
                photo_file = new File(getExternalCacheDir(), "output_image.jpg");
                try {
                    if (photo_file.exists()) {
                        photo_file.delete();
                    }
                    photo_file.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (Build.VERSION.SDK_INT < 24) {
                    //低于Android 7.0 ，将file转换为uri对象
                    imageUri = Uri.fromFile(photo_file);
                } else {
                    //转换为封装过的uri对象
                    //FileProvider —— 内容提供器
                    imageUri = FileProvider.getUriForFile(RegistWorkerActivity.this, "com.example.cameraalbumtest.fileprovider", photo_file);
                }
                // 启动相机程序
                Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
                intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                startActivityForResult(intent, TAKE_PHOTO);
            }
        });

        chooseFromAlbum.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(RegistWorkerActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(RegistWorkerActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                } else {
                    openAlbum();
                }
            }
        });

        upLoad.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(photo_exist.equals(false)){
                    Toast.makeText(getApplicationContext(),"请上传身份证图片！",Toast.LENGTH_LONG).show();
                }else {
                    uploadPhoto();
                }
            }
        });

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String password = edt_userPwd.getText().toString();
                String password_2 = edt_userPwd_again.getText().toString();
                if (workerType.equals(null)) {
                    Toast.makeText(getApplicationContext(), "请选择您的工种！", Toast.LENGTH_LONG).show();
                } /*else if (photo_exist.equals(false)) {
                    Toast.makeText(getApplicationContext(), "请上传身份证图片！", Toast.LENGTH_LONG).show();
                }*/ else if (!password.equals(password_2)) {
                    Toast.makeText(getApplicationContext(), "两次密码输入不一致！", Toast.LENGTH_LONG).show();
                    edt_userPwd.getText().clear();
                    edt_userPwd_again.getText().clear();
                } /*else if (!isMobileNO(edt_userPhone.getText().toString())) {
                    Toast.makeText(getApplicationContext(), "手机号码格式不正确！", Toast.LENGTH_LONG).show();
                    edt_userPhone.getText().clear();
                } */else {
                    userinfo = new UserInfo(edt_userName.getText().toString(), edt_userPhone.getText().toString(), edt_userPwd.getText().toString(), workerType);
                    sendRegister();
                }
            }
        });
    }

    private void initRegister() {
        initSpinner();
        workerType = null;
        if (type_list == null || type_list.isEmpty()) {
            return;
        }
        //适配器初始化
        spinner.setDropDownVerticalOffset(50); //下拉的纵向偏移
        typeAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item,type_list);
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(typeAdapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String type = (String) spinner.getSelectedItem();
                switch (type){
                    case "木工": workerType = "worker_1";break;
                    case "泥瓦匠": workerType = "worker_2";break;
                    case "油漆工": workerType = "worker_3";break;
                    case "水泥工": workerType = "worker_4";break;
                    default: workerType = "worker";break;
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                workerType = "worker";
            }
        });


    }
    private void initSpinner() {
        type_list = new ArrayList<String>();
        type_list.add("木工");
        type_list.add("泥瓦匠");
        type_list.add("油漆工");
        type_list.add("水泥工");
        type_list.add("其他");

    }

    private void openAlbum() {
        Intent intent = new Intent("android.intent.action.GET_CONTENT");
        intent.setType("image/*");
        startActivityForResult(intent, CHOOSE_PHOTO); // 打开相册
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openAlbum();
                } else {
                    Toast.makeText(this, "You denied the permission", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case TAKE_PHOTO:
                if (resultCode == RESULT_OK) {
                    try {
                        // 将拍摄的照片显示出来
                        bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(imageUri));
                        picture.setImageBitmap(bitmap);
                        photo_exist = true;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                break;
            case CHOOSE_PHOTO:
                if (resultCode == RESULT_OK) {
                    // 判断手机系统版本号
                    if (Build.VERSION.SDK_INT >= 19) {
                        // 4.4及以上系统使用这个方法处理图片
                        handleImageOnKitKat(data);
                    } else {
                        // 4.4以下系统使用这个方法处理图片
                        handleImageBeforeKitKat(data);
                    }
                }
                break;
            default:
                break;
        }
    }

    @TargetApi(19)
    private void handleImageOnKitKat(Intent data) {
        String imagePath = null;
        Uri uri = data.getData();
        Log.d("TAG", "handleImageOnKitKat: uri is " + uri);
        if (DocumentsContract.isDocumentUri(this, uri)) {
            // 如果是document类型的Uri，则通过document id处理
            String docId = DocumentsContract.getDocumentId(uri);
            if ("com.android.providers.media.documents".equals(uri.getAuthority())) {
                String id = docId.split(":")[1]; // 解析出数字格式的id
                String selection = MediaStore.Images.Media._ID + "=" + id;
                imagePath = getImagePath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, selection);
            } else if ("com.android.providers.downloads.documents".equals(uri.getAuthority())) {
                Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(docId));
                imagePath = getImagePath(contentUri, null);
            }
        } else if ("content".equalsIgnoreCase(uri.getScheme())) {
            // 如果是content类型的Uri，则使用普通方式处理
            imagePath = getImagePath(uri, null);
        } else if ("file".equalsIgnoreCase(uri.getScheme())) {
            // 如果是file类型的Uri，直接获取图片路径即可
            imagePath = uri.getPath();
        }
        displayImage(imagePath);
    }

    private void handleImageBeforeKitKat(Intent data) {
        Uri uri = data.getData();
        String imagePath = getImagePath(uri, null);
        displayImage(imagePath);
    }

    private String getImagePath(Uri uri, String selection) {
        String path = null;
        // 通过Uri和selection来获取真实的图片路径
        Cursor cursor = getContentResolver().query(uri, null, selection, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
            }
            cursor.close();
        }
        return path;
    }

    // 根据图片路径显示图片
    private void displayImage(String imagePath) {
        if (imagePath != null) {
            photo_exist = true;
            bitmap = BitmapFactory.decodeFile(imagePath);
            picture.setImageBitmap(bitmap);
            photo_file = new File(imagePath);
        } else {
            Toast.makeText(this, "failed to get image", Toast.LENGTH_SHORT).show();
        }
    }

    //上传照片文件至服务器
    private void uploadPhoto(){
        HttpUtil.uploadPicOkHttpRequest(new okhttp3.Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                //异常情况处理
                Looper.prepare();
                Toast.makeText(RegistWorkerActivity.this, "网络连接失败！", Toast.LENGTH_LONG).show();
                Looper.loop();
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                // 返回服务器数据
                String responseData = response.body().string();
                try {
                    Message message = new Message();
                    JSONObject jsonObject = JSON.parseObject(responseData);
                    String result = jsonObject.getString("error");
                    switch (result){
                        case "0":
                            message.what = 0;break;
                        default:
                            message.what = 1;break;
                    }
                    handler.sendMessage(message);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, photo_file);
    }

    //注册
    private void sendRegister() {
        String json = new Gson().toJson(userinfo);
        HttpUtil.sendRegistOkHttpRequest(new okhttp3.Callback(){
            @Override
            public void onFailure(Call call, IOException e) {
                //异常情况处理
                Looper.prepare();
                Toast.makeText(RegistWorkerActivity.this, "网络连接失败！", Toast.LENGTH_LONG).show();
                Looper.loop();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseData = response.body().string();
                try{
                    JSONObject jsonObject = JSON.parseObject(responseData);

                }catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        },json);
    }

    // 顶部导航栏消息响应
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home: // 返回按钮
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public static boolean isMobileNO(String mobiles) {
        /**
         * 判断字符串是否符合手机号码格式
         * 移动号段: 134,135,136,137,138,139,147,150,151,152,157,158,159,170,178,182,183,184,187,188
         * 联通号段: 130,131,132,145,155,156,170,171,175,176,185,186
         * 电信号段: 133,149,153,170,173,177,180,181,189
         * @param str
         * @return 待检测的字符串
         */
        String telRegex = "^((1[3,5,7,8][0-9])|(15[^4,\\D])|(18[0,5-9]))\\d{8}$";
        if (TextUtils.isEmpty(mobiles)) {
            return false;
        } else {
            return mobiles.matches(telRegex);
        }
    }
}
