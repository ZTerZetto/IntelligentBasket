package com.automation.zzx.intelligent_basket_demo.activity.loginRegist;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Dialog;
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
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.automation.zzx.intelligent_basket_demo.R;

import com.automation.zzx.intelligent_basket_demo.entity.AppConfig;
import com.automation.zzx.intelligent_basket_demo.entity.UserInfo;
import com.automation.zzx.intelligent_basket_demo.utils.ftp.FTPUtil;
import com.automation.zzx.intelligent_basket_demo.utils.http.HttpUtil;
import com.automation.zzx.intelligent_basket_demo.widget.dialog.CommonDialog;
import com.automation.zzx.intelligent_basket_demo.widget.dialog.LoadingDialog;
import com.google.gson.Gson;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.Call;
import okhttp3.Response;

public class RegistWorkerActivity extends AppCompatActivity {

    public static final int TAKE_PHOTO = 1;
    public static final int CHOOSE_PHOTO = 2;

    private ImageView ivIdPicture;
    private Uri imageUri;
    private Bitmap bitmap;
    private File photo_file;
    private Boolean photo_exist;

    private EditText edt_userName;
    private EditText edt_userAge;
    private EditText edt_userLocal;
    private Spinner spinnerGender;
    private EditText edt_userPhone;
    private EditText edt_userPwd;
    private EditText edt_userPwd_again;
    private EditText edt_userIDNum;
    
    private LinearLayout llSpinner;

    private Button takePhoto;
    private Button chooseFromAlbum;
    private Button register;

    private UserInfo userinfo;
    private Spinner spinnerType;
    private List<String> gender_list;
    private List<String> type_list;
    private ArrayAdapter<String> genderAdapter;
    private ArrayAdapter<String> typeAdapter;
    private String genderType;
    private String workerType;

    private ImageView ivAddSkill;
    private ListView lvSkill;

    private CommonDialog mCommonDialog;
    private LoadingDialog mLoadingDialog;

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1: {
                    sendRegister();
                    break;
                }
                case 2: {
                    if (mCommonDialog == null) {
                        mCommonDialog = initDialog(getString(R.string.pic_failNotice));
                    }
                    mLoadingDialog.dismiss();
                    mCommonDialog.show();
                    handler.removeCallbacksAndMessages(null);
                    break;
                }
                case 3: {
                    if (mCommonDialog == null) {
                        mCommonDialog = initDialog(getString(R.string.register_success));
                    }
                    mLoadingDialog.dismiss();
                    mCommonDialog.show();
                    break;
                }
                case 4: {
                    if (mCommonDialog == null) {
                        mCommonDialog = initDialog(getString(R.string.register_exist));
                    }
                    mLoadingDialog.dismiss();
                    mCommonDialog.show();
                    break;
                }
                case 5:{
                    if (mCommonDialog == null) {
                        mCommonDialog = initDialog(getString(R.string.register_back_fail));
                    }
                    mLoadingDialog.dismiss();
                    mCommonDialog.show();
                    break;
                }
                default: {
                    break;
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_regist);

        // 顶部导航栏
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        TextView titleText = (TextView) findViewById(R.id.toolbar_title);
        toolbar.setTitle("");
        titleText.setText(getString(R.string.registWorker_title));
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true); //设置返回键可用


        takePhoto = (Button) findViewById(R.id.take_photo);
        chooseFromAlbum = (Button) findViewById(R.id.choose_from_album);
        register = findViewById(R.id.btn_regist);

        ivIdPicture = (ImageView) findViewById(R.id.iv_idcard_picture);

        //基本信息
        edt_userName = findViewById(R.id.edt_register_userName);
        edt_userAge = findViewById(R.id.edt_register_age);
        edt_userLocal = findViewById(R.id.edt_register_native);
        spinnerGender = findViewById(R.id.spinner_gender);
        //账号信息
        edt_userPhone = findViewById(R.id.edt_register_userPhone);
        edt_userPwd = findViewById(R.id.edt_register_pwd);
        edt_userPwd_again = findViewById(R.id.edt_register_pwd_again);

        //身份证信息
        edt_userIDNum = findViewById(R.id.edt_id_card_num);

        //工种信息
        llSpinner = findViewById(R.id.ll_spinner);
        llSpinner.setVisibility(View.VISIBLE);
        spinnerType = (Spinner) findViewById(R.id.spinner_type_choose);

        //技能资质
        ivAddSkill = findViewById(R.id.iv_add_skill);
        lvSkill = findViewById(R.id.listView_skill);

        photo_exist = false;

        initRegister();
        initLoadingDialog();

        //点击拍摄按钮
        takePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 创建File对象，用于存储拍照后的图片；
                //应用关联缓存目录“/sdcard/Android/data/<package name>/cache”
                if (ContextCompat.checkSelfPermission(RegistWorkerActivity.this,
                        Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(RegistWorkerActivity.this,
                            new String[]{Manifest.permission.CAMERA}, 2);
                }else {
                    openCamera();
                }
            }
        });

        //点击从相册选择按钮
        chooseFromAlbum.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(RegistWorkerActivity.this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(RegistWorkerActivity.this,
                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                } else {
                    openAlbum();
                }
            }
        });

        //点击注册按钮
       register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String password = edt_userPwd.getText().toString();
                String password_2 = edt_userPwd_again.getText().toString();
                if (workerType == null) {
                    Toast.makeText(getApplicationContext(), "请选择您的工种！", Toast.LENGTH_LONG).show();
                } else if (photo_exist.equals(false)) {
                    Toast.makeText(getApplicationContext(), "请上传身份证图片！", Toast.LENGTH_LONG).show();
                } else if (!password.equals(password_2) || password.equals("") || password == null) {
                    Toast.makeText(getApplicationContext(), "两次密码输入不一致！", Toast.LENGTH_LONG).show();
                    edt_userPwd.getText().clear();
                    edt_userPwd_again.getText().clear();
                } else if (!isMobileNO(edt_userPhone.getText().toString())) {
                    Toast.makeText(getApplicationContext(), "手机号码格式不正确！", Toast.LENGTH_LONG).show();
                    edt_userPhone.getText().clear();
                } else if (edt_userName.getText().toString().equals("")
                        || edt_userName.getText().toString().equals(" ") ) {
                    Toast.makeText(getApplicationContext(), "请填写用户名！", Toast.LENGTH_LONG).show();
                    edt_userPhone.getText().clear();
                } else if (!isIdNum(edt_userIDNum.getText().toString())) {
                    Toast.makeText(getApplicationContext(), "身份证号填写有误！", Toast.LENGTH_LONG).show();
                }else {
                    userinfo = new UserInfo(edt_userName.getText().toString(), edt_userPwd.getText().toString(),edt_userPhone.getText().toString(),
                            workerType,genderType,edt_userAge.getText().toString(),edt_userLocal.getText().toString(),edt_userIDNum.getText().toString());
                    mLoadingDialog.show();
                    uploadIdentityCardImage();
                }
            }
        });

       //点击添加技能证书按钮
        ivAddSkill.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "添加技能证书", Toast.LENGTH_LONG).show();
            }
        });

        //点击技能证书中的Item
    }

    private void initRegister() {
        initSpinner();

        //性别选择
        genderType = null;
        if(gender_list == null || gender_list.isEmpty()){
            return;
        }
        spinnerGender.setDropDownVerticalOffset(80); //下拉的纵向偏移
        genderAdapter = new ArrayAdapter<String>(this,R.layout.spinner_simple_item,gender_list);
        genderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerGender.setAdapter(genderAdapter);
        spinnerGender.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                genderType= (String) spinnerGender.getSelectedItem();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                genderType = "";
            }
        });

        //工种选择适配器初始化
        workerType = null;
        if (type_list == null || type_list.isEmpty()) {
            return;
        }
        spinnerType.setDropDownVerticalOffset(50); //下拉的纵向偏移
        typeAdapter = new ArrayAdapter<String>(this,R.layout.spinner_simple_item,type_list);
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerType.setAdapter(typeAdapter);
        spinnerType.setSelection(5, true); // 设置默认值为:其它

        spinnerType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String type = (String) spinnerType.getSelectedItem();
                switch (type){
                    case "涂料": workerType = "worker_1";break;
                    case "幕墙": workerType = "worker_2";break;
                    case "内装": workerType = "worker_3";break;
                    case "土建": workerType = "worker_4";break;
                    case "车辆": workerType = "worker_5";break;
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

        //性别选择
        gender_list = new ArrayList<>();
        gender_list.add("男");
        gender_list.add("女");

        //工种选择
        type_list = new ArrayList<String>();
        type_list.add("涂料");
        type_list.add("幕墙");
        type_list.add("内装");
        type_list.add("土建");
        type_list.add("车辆");
        type_list.add("其他");

        //技能选择
    }

    private void openAlbum() {
        Intent intent = new Intent("android.intent.action.GET_CONTENT");
        intent.setType("image/*");
        startActivityForResult(intent, CHOOSE_PHOTO); // 打开相册
    }

    private void openCamera(){
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
            imageUri = FileProvider.getUriForFile(RegistWorkerActivity.this,
                    "com.automation.zzx.intelligent_basket_demo.fileprovider", photo_file);
        }
        // 启动相机程序
        Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        startActivityForResult(intent, TAKE_PHOTO);
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
            case 2:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //openAlbum();
                    openCamera();
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
                        ivIdPicture.setImageBitmap(bitmap);
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
            ivIdPicture.setImageBitmap(bitmap);
            photo_file = new File(imagePath);
        } else {
            Toast.makeText(this, "failed to get image", Toast.LENGTH_SHORT).show();
        }
    }

    /*
     * 上传身份证图片至服务器
     */
    // FTP直接上传
    private void uploadIdentityCardImage(){
        final FTPUtil mFTPClient = new FTPUtil(AppConfig.FILE_SERVER_YBLIU_IP, AppConfig.FILE_SERVER_YBLIU_PORT,
                AppConfig.FILE_SERVER_USERNAME, AppConfig.FILE_SERVER_PASSWORD);
        final String mRemotePath = "photo";
        new Thread(){
            public void run() {
                try {
                    mFTPClient.openConnect();  // 建立连接
                    mFTPClient.uploadingInit(mRemotePath); // 上传文件初始化
                    String fileName = userinfo.getUserPhone() + ".jpg";
                    mFTPClient.uploadingSingleRenameFile(photo_file, fileName);
                    mFTPClient.closeConnect();  // 关闭连接
                    handler.sendEmptyMessage(1);
                }catch (IOException e){
                    // 上传文件失败
                    e.printStackTrace();
                    handler.sendEmptyMessage(5);
                }
            }
        }.start();
    }
    //上传照片文件至服务器
    private void uploadPhoto(){
        HttpUtil.uploadSinglePicOkHttpRequest(new okhttp3.Callback() {
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
                Message message = new Message();
                try {
                    JSONObject jsonObject = JSON.parseObject(responseData);
                    String result = jsonObject.getString("error");
                    if(result != null){
                        switch (result){
                            case "0":
                                message.what = 1;break;
                            default:
                                message.what = 2;break;
                        }
                    } else {
                        message.what = 5;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                handler.sendMessage(message);
            }
        }, photo_file,userinfo.getUserPhone());
    }

    //注册
    private void sendRegister() {
        handler.removeMessages(1);
        handler.removeMessages(2);
        final String json = new Gson().toJson(userinfo);
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
                    String mMessage = jsonObject.getString("message");
                    Message message = new Message();
                    if(mMessage!=null){
                        if(mMessage.equals("success")){
                            message.what = 3;
                        }else if(mMessage.equals("exist")){
                            message.what = 4;
                        }else{
                            message.what = 5;
                        }
                    } else {
                        message.what = 5;
                    }
                    handler.sendMessage(message);
                }catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        },json);
    }

    /*
     * 提示弹框
     */
    // 提示弹窗
    private CommonDialog initDialog(String mMsg){
        return new CommonDialog(this, R.style.dialog, mMsg,
                new CommonDialog.OnCloseListener() {
                    @Override
                    public void onClick(Dialog dialog, boolean confirm) {
                        if(confirm){
                            Intent intent = new Intent(RegistWorkerActivity.this,LoginActivity.class);
                            startActivity(intent);
                            finish();
                        }else{
                            dialog.dismiss();
                        }
                    }
                }).setTitle("提示");
    }
    // 加载弹窗
    private void initLoadingDialog(){
        mLoadingDialog = new LoadingDialog(RegistWorkerActivity.this, "正在上传...");
        mLoadingDialog.setCancelable(false);
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

    /**
     * 判断身份证格式
     *
     * @param idNum
     * @return
     */
    public static boolean isIdNum(String idNum) {
        // 中国公民身份证格式：长度为15或18位，最后一位可以为字母
        Pattern idNumPattern = Pattern.compile("(\\d{14}[0-9a-zA-Z])|(\\d{17}[0-9a-zA-Z])");
        // 格式验证
        if (!idNumPattern.matcher(idNum).matches()) return false;
        // 合法性验证
        int year = 0;
        int month = 0;
        int day = 0;
        if (idNum.length() == 15) {
            // 提取身份证上的前6位以及出生年月日
            Pattern birthDatePattern = Pattern.compile("\\d{6}(\\d{2})(\\d{2})(\\d{2}).*");
            Matcher birthDateMather = birthDatePattern.matcher(idNum);
            if (birthDateMather.find()) {
                year = Integer.valueOf("19" + birthDateMather.group(1));
                month = Integer.valueOf(birthDateMather.group(2));
                day = Integer.valueOf(birthDateMather.group(3));
            }
        } else if (idNum.length() == 18) {
            // 提取身份证上的前6位以及出生年月日
            Pattern birthDatePattern = Pattern.compile("\\d{6}(\\d{4})(\\d{2})(\\d{2}).*");
            Matcher birthDateMather = birthDatePattern.matcher(idNum);
            if (birthDateMather.find()) {
                year = Integer.valueOf(birthDateMather.group(1));
                month = Integer.valueOf(birthDateMather.group(2));
                day = Integer.valueOf(birthDateMather.group(3));
            }
        }
        // 年份判断，100年前至今
        Calendar cal = Calendar.getInstance();
        // 当前年份
        int currentYear = cal.get(Calendar.YEAR);
        if (year <= currentYear - 100 || year > currentYear)
            return false;
        // 月份判断
        if (month < 1 || month > 12)
            return false;
        //计算月份天数
        int dayCount = 31;
        switch (month) {
            case 1:
            case 3:
            case 5:
            case 7:
            case 8:
            case 10:
            case 12:
                dayCount = 31;
                break;
            case 2:
                // 2月份判断是否为闰年
                if ((year % 4 == 0 && year % 100 != 0) || (year % 400 == 0)) {
                    dayCount = 29;
                    break;
                } else {
                    dayCount = 28;
                    break;
                }
            case 4:
            case 6:
            case 9:
            case 11:
                dayCount = 30;
                break;
        }
        if (day < 1 || day > dayCount)
            return false;
        return true;
    }
}
