package com.automation.zzx.intelligent_basket_demo.activity.worker;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.automation.zzx.intelligent_basket_demo.R;
import com.automation.zzx.intelligent_basket_demo.activity.loginRegist.LoginActivity;
import com.automation.zzx.intelligent_basket_demo.adapter.areaAdmin.UploadImageLikeWxAdapter;
import com.automation.zzx.intelligent_basket_demo.entity.AppConfig;
import com.automation.zzx.intelligent_basket_demo.entity.enums.CardType;
import com.automation.zzx.intelligent_basket_demo.utils.ToastUtil;
import com.automation.zzx.intelligent_basket_demo.utils.ftp.FTPUtil;
import com.automation.zzx.intelligent_basket_demo.utils.okhttp.BaseCallBack;
import com.automation.zzx.intelligent_basket_demo.utils.okhttp.BaseOkHttpClient;
import com.automation.zzx.intelligent_basket_demo.widget.dialog.CommonDialog;
import com.automation.zzx.intelligent_basket_demo.widget.dialog.ProgressAlertDialog;
import com.heynchy.compress.CompressImage;
import com.heynchy.compress.compressinterface.CompressLubanListener;
import com.hjq.permissions.OnPermission;
import com.hjq.permissions.Permission;
import com.hjq.permissions.XXPermissions;

import org.apache.commons.net.ftp.FTPFile;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import me.nereo.multi_image_selector.MultiImageSelectorActivity;
import okhttp3.Call;

public class SkillEditActivity extends AppCompatActivity implements View.OnClickListener {

    private final static  String TAG = "SkillEditActivity";

    private final static int GET_PHOTO_FROM_ALBUM = 1;  // 相册
    public static final int TAKE_PHOTO_FROM_CAMERA= 2;  // 照相机

    // Handler消息
    private final static int GET_UPLOAD_INFO = 100;  // 上传图片成功
    private final static int GET_UPLOAD_WRONG = 101;  // 上传图片失败
    private final static int UPLOAD_PROGRESS_PARAMS = 102; // 更新文件进度条
    private final static int UPDATE_CAPACITY_IMAGE = 107;  // 更新资质证书


    //相册位置
    public static final String CAMERA_PATH = Environment.getExternalStorageDirectory() +
            File.separator + Environment.DIRECTORY_DCIM + File.separator+"Camera"+ File.separator;

    // 控件
    // 顶部导航栏
    private Toolbar mToolbar;  // 顶部导航栏
    private TextView mSendTextView; // 发送 按钮
    private ImageView mSendImageView; // 发送 图标

    private Spinner spinnerSkill;
    private CardType cardType = CardType.WELDCUT;
    private List<String> type_list;
    private ArrayAdapter<String> typeAdapter;

    // 图片列表
    private GridView mImageGridView;
    private List<Bitmap> mUploadImageList = new ArrayList<>();
    private ArrayList<String> mUploadImageUrlList = new ArrayList<>();
    private UploadImageLikeWxAdapter mUploadImageLikeWxAdapter;

    //CAMERA
    private String fileName = "";  // 图片名
    private File photoFile ; // 图片文件
    private Uri photoUrl ; // 图片URL

    //基本信息
    private SharedPreferences pref;
    private SharedPreferences.Editor editor;

    private String token; // 验证token
    private String workerId; // 工人Id
    private int maxUploadImageNumer; // 最大上传图片数量
    private String uploadHint; // 上传提示信息


    // FTP 文件服务器
    private FTPUtil mFTPClient;
    private String mRemotePath;

    // 弹窗
    private ProgressAlertDialog mProgressDialog; // 文件加载进度

    @SuppressLint("HandlerLeak")
    public Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            Intent intent = new Intent();
            switch (msg.what) {
                case GET_UPLOAD_INFO:  // 上传图片成功
                    DialogToast("提示", "您已成功上传" + uploadHint).show();
                    setResult(RESULT_OK, intent);
                    mProgressDialog.dismiss();
                    break;
                case GET_UPLOAD_WRONG: // 上传图片失败
                    DialogToast("提示", uploadHint + "上传失败！").show();
                    setResult(RESULT_CANCELED, intent);
                    mProgressDialog.dismiss();
                    break;
                case UPLOAD_PROGRESS_PARAMS:  // 更新文件上传进度轴参数
                    mProgressDialog.setProgress(msg.arg1);
                    break;
                case UPDATE_CAPACITY_IMAGE:   // 工人更新资质证书
                    workerUpdateCapacityImage();
                    break;
                default:
                    break;
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_skill_edit);

        // 获取权限
        if(!isHasPermission()) requestPermission();

        // 初始化控件
        initWidgetResource();

        //获取基本信息
        getBaseInfoFromPred();
        setUploadParameters();

        // 初始化文件服务器
        initFTPClient();
    }

    /*
     * 控件初始化
     */
    private void initWidgetResource() {
        // 顶部导航栏
        mToolbar = (Toolbar) findViewById(R.id.upload_toolbar);
        mToolbar.setTitle("技能资质");
        setSupportActionBar(mToolbar);
        getSupportActionBar().setHomeButtonEnabled(true); //设置返回键可用
        mSendTextView = (TextView) findViewById(R.id.btn_commit);
        mSendTextView.setOnClickListener(this);
        mSendImageView = (ImageView) findViewById(R.id.toolbar_send_imageview);
        mSendImageView.setOnClickListener(this);

        //其余控件
        spinnerSkill = findViewById(R.id.spinner_skill);

        // 图片选择框
        mImageGridView = (GridView) findViewById(R.id.release_gridview_image);
        addPulseImageBitmap();
        mUploadImageLikeWxAdapter = new UploadImageLikeWxAdapter(this, mUploadImageList);
        mImageGridView.setAdapter(mUploadImageLikeWxAdapter);
        mImageGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(position == mUploadImageList.size()-1){
                    // 点击加号图片
                    showAddImageWayMenu(view);
                }else{
                    // 点击其它图片


                }
            }
        });

        initSpinner();

        //技能选择
        if(type_list == null || type_list.isEmpty()){
            return;
        }
        spinnerSkill.setDropDownVerticalOffset(80); //下拉的纵向偏移
        typeAdapter = new ArrayAdapter<String>(this,R.layout.spinner_left_item,type_list);
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSkill.setAdapter(typeAdapter);
        spinnerSkill.setSelection(0, true); // 设置默认值为:焊切割操作证
        spinnerSkill.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String type = (String) spinnerSkill.getSelectedItem();
                cardType = CardType.getByChinese(type);
                setUploadParameters();//更新地址信息
                /*switch (type){
                    case "焊切割操作证":
                        skillType = "1";
                        break;
                    case "高空作业证":
                        skillType = "2";
                        break;
                    case "吊篮操作证":
                        skillType = "3";
                        break;
                }*/
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

    }
    private void initSpinner() {
        //技能选择
        type_list = new ArrayList<String>();
        type_list.add("焊切割操作证");
        type_list.add("高空作业证");
        type_list.add("吊篮操作证");
    }


    /*
     * 消息监听
     */
    // 顶部导航栏消息响应
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home: // 返回按钮
                if(mUploadImageUrlList.size() > 0){
                    Intent intent = new Intent();
                    setResult(RESULT_OK, intent);
                }
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_commit:  // 发送监听
            case R.id.toolbar_send_imageview: // 发送监听
                if(spinnerSkill == null){
                    Toast.makeText(SkillEditActivity.this, "未选择技能类型！", Toast.LENGTH_SHORT).show();
                } else if(mUploadImageUrlList.size()==0){
                    Toast.makeText(SkillEditActivity.this, "未上传技能证书图片！", Toast.LENGTH_SHORT).show();
                } else {
                    showUploadProgressDialog();
                    startSendImage();
                }
                break;
        }
    }



    /*
     * 活动返回监听
     */
    //页面返回数据监听
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        switch (requestCode){
            case TAKE_PHOTO_FROM_CAMERA:        //拍摄
                if(resultCode == RESULT_CANCELED){
                    Toast.makeText(SkillEditActivity.this, "取消了拍照", Toast.LENGTH_SHORT).show();
                    return;
                }
                String photoFilePath = CAMERA_PATH + "IMAGE_"+ fileName +".jpg";
                String compressFilePath = CAMERA_PATH + "IMAGE_"+ fileName +"_compress.jpg";
                compressImage(photoFilePath, compressFilePath);  // 压缩图片
                Bitmap  photo = BitmapFactory.decodeFile(CAMERA_PATH + "IMAGE_"+ fileName +".jpg");
                if(mUploadImageList.size() >= 1) {
                    mUploadImageList.remove(mUploadImageList.size() - 1); //移除最后一张图片
                    mUploadImageList.add(photo);
                    mUploadImageUrlList.add(CAMERA_PATH+ "IMAGE_"+fileName+".jpg");
                }
                if(mUploadImageList.size() < 9) // 图片数量小于9，增加+图片
                    addPulseImageBitmap();
                mUploadImageLikeWxAdapter.notifyDataSetChanged();
                try {
                    MediaStore.Images.Media.insertImage(getContentResolver(), photoFile.getAbsolutePath(),
                            photoFile.getName(), null);//图片插入到系统图库
                }catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                intent.setData(Uri.parse("file://" + photoFile.getAbsolutePath()));
                sendBroadcast(intent);
                break;
            case GET_PHOTO_FROM_ALBUM:         //相册
                if(resultCode == RESULT_OK){
                    mUploadImageList.clear();
                    mUploadImageUrlList = data.getStringArrayListExtra(MultiImageSelectorActivity.EXTRA_RESULT);
                    for(int i = 0; i < mUploadImageUrlList.size(); i++){
                        Log.d("ReleaseHouseActivity", mUploadImageUrlList.get(i));
                        String imageFilePath = mUploadImageUrlList.get(i);
                        String saveImageFilePath = imageFilePath.replace(".", "_compress.");
                        if(!(new File(saveImageFilePath)).exists()){  // 压缩文件不存在就压缩文件
                            compressImage(imageFilePath, saveImageFilePath);
                        }
                        Bitmap bitmap = BitmapFactory.decodeFile(mUploadImageUrlList.get(i));
                        mUploadImageList.add(bitmap);
                    }
                    if(mUploadImageList.size() < 9)  // 图片数量小于9，增加+图片
                        addPulseImageBitmap();
                    mUploadImageLikeWxAdapter.notifyDataSetChanged();
                }
                break;
            default:break;
        }
    }


    // 是否有权限：摄像头、拨打电话
    private boolean isHasPermission() {
        if (XXPermissions.isHasPermission(SkillEditActivity.this, Permission.Group.STORAGE)
                && XXPermissions.isHasPermission(SkillEditActivity.this, Permission.CAMERA))
            return true;
        return false;
    }


    /*
     * 跳转照相机和相册页面
     */
    //跳转至cameraactivity
    public void startCameraActivity() {
        Intent intent =  new Intent(MediaStore.ACTION_IMAGE_CAPTURE);   //跳转至拍照页面
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
        Date date = new Date(System.currentTimeMillis());
        fileName = format.format(date);
        photoFile = new File(CAMERA_PATH, "IMAGE_"+fileName+".jpg");
        Log.i(TAG,getPackageName() + ".fileprovider");
        photoUrl = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", photoFile);
        Log.i(TAG,photoUrl.toString());
        //拍照后的保存路径
        intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUrl);
        startActivityForResult(intent, TAKE_PHOTO_FROM_CAMERA);
    }

    // 相册
    public void startMultiImageSelectorActivity(){
        Intent intent = new Intent(this, MultiImageSelectorActivity.class);
        intent.putExtra(MultiImageSelectorActivity.EXTRA_SHOW_CAMERA, false);// 是否显示调用相机拍照
        // 设置模式 (支持 单选/MultiImageSelectorActivity.MODE_SINGLE 或者 多选/MultiImageSelectorActivity.MODE_MULTI)
        intent.putExtra(MultiImageSelectorActivity.EXTRA_SELECT_COUNT, maxUploadImageNumer);// 最大图片选择数量
        // 默认选择图片,回填选项(支持String ArrayList)
        intent.putExtra(MultiImageSelectorActivity.EXTRA_SELECT_MODE, MultiImageSelectorActivity.MODE_MULTI);
        intent.putStringArrayListExtra(MultiImageSelectorActivity.EXTRA_DEFAULT_SELECTED_LIST, mUploadImageUrlList);
        startActivityForResult(intent, GET_PHOTO_FROM_ALBUM);
    }
    /*
     * 在网格布局末尾添上+图片
     */
    private void addPulseImageBitmap(){
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_add_upload_image);
        mUploadImageList.add(bitmap);
    }


    /*
     * 后台通信
     */
    // 上传图片
    private void startSendImage(){
        new Thread() {
            public void run() {
                try {
                    // 上传文件
                    mFTPClient.openConnect();  // 建立连接
                    mFTPClient.uploadingInit(mRemotePath); // 上传文件初始化
                    int startIndex = 0; // 图片命名起始编号
                    for(int i=0; i < mUploadImageUrlList.size(); i++){
                        Message message = new Message();
                        message.what = UPLOAD_PROGRESS_PARAMS;
                        message.arg1 = i;
                        mHandler.sendMessage(message);
                        String tempFileName = "";
                        if(i==0) startIndex = getCapacityIndex();
                        tempFileName = workerId + "_" + cardType.getType() + "_" + (i+1+startIndex) + ".jpg";
                        String originFilePath = mUploadImageUrlList.get(i);
                        File originalFile = new File(originFilePath);  // 原图
                        String compressFilePath = originFilePath.replace(".", "_compress.");
                        File compressFile = new File(compressFilePath);  // 压缩图
                        if(!compressFile.exists()){  // 默认选择压缩图上传
                            mFTPClient.uploadingSingleRenameFile(originalFile, tempFileName);  // 上传图片
                        }else{
                            mFTPClient.uploadingSingleRenameFile(compressFile, tempFileName);  // 上传图片
                            myDeleteFile(compressFile); // 删除压缩图
                        }
                    }
                    mFTPClient.closeConnect();  // 关闭连接

                    // 文件上传成功后操作
                    mHandler.sendEmptyMessage(UPDATE_CAPACITY_IMAGE);

                } catch (IOException e) {
                    // 上传文件失败
                    e.printStackTrace();
                    mHandler.sendEmptyMessage(GET_UPLOAD_WRONG);
                }
            }
        }.start();
    }

    // FTP 初始化
    private void initFTPClient(){
        mFTPClient = new FTPUtil(AppConfig.FILE_SERVER_YBLIU_IP, AppConfig.FILE_SERVER_YBLIU_PORT,
                AppConfig.FILE_SERVER_USERNAME, AppConfig.FILE_SERVER_PASSWORD);
    }


    // 获取当前资质证书
    private int getCapacityIndex(){
        try {
            List<FTPFile> files = mFTPClient.listCurrentFiles();
            if(files.size()==0) return 0;
            String lastFileName = files.get(files.size()-1).getName();
            return Integer.parseInt(lastFileName.substring(lastFileName.indexOf('_')+1, lastFileName.indexOf('.')));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return 0;
    }

    // 工人更新资质证书
    private void workerUpdateCapacityImage(){
        BaseOkHttpClient.newBuilder()
                .addHeader("Authorization", token)
                .addParam("userId", workerId)
                .addParam("type", cardType.getType())
                .addParam("picNum", mUploadImageUrlList.size())
                .post()
                .url(AppConfig.WORKER_UPDATE_CAPACITY_IMAGE)
                .build()
                .enqueue(new BaseCallBack() {
                    @Override
                    public void onSuccess(Object o) {
                        Log.i(TAG, "更新资质证书成功" );
                        JSONObject jsonObject = JSON.parseObject(o.toString());
                        if(jsonObject.getString("update").equals("success")) {
                            // 申请成功
                            mHandler.sendEmptyMessage(GET_UPLOAD_INFO);
                        }else{
                            // 申请失败
                            mHandler.sendEmptyMessage(GET_UPLOAD_WRONG);
                        }
                    }

                    @Override
                    public void onError(int code) {
                        Log.i(TAG, "更新资质证书错误：" + code);
                        switch (code){
                            case 401: // 未授权
                                ToastUtil.showToastTips(SkillEditActivity.this, "登录已过期，请重新登陆");
                                startActivity(new Intent(SkillEditActivity.this, LoginActivity.class));
                                finish();
                                break;
                            case 403: // 禁止
                                break;
                            case 404: // 404
                                break;
                        }
                    }

                    @Override
                    public void onFailure(Call call, IOException e) {
                        Log.i(TAG, "更新资质证书失败：" + e.toString());
                        mHandler.sendEmptyMessage(GET_UPLOAD_WRONG);
                    }
                });
    }


    /*
     * 获取基本信息
     */
    // 用户信息
    private void getBaseInfoFromPred(){
        pref = PreferenceManager.getDefaultSharedPreferences(this);
        workerId = pref.getString("userId", "");
        token = pref.getString("loginToken", "");
    }

    // 设置上传图片的参数、路径等
    private void setUploadParameters(){
        mRemotePath = "userImage/" + workerId + "/" + cardType.getEnglish();
        //mRemotePath = "userImage/" + workerId ;
        uploadHint = "资质证书";
        maxUploadImageNumer = 9;
    }

    /*
     * 申请权限
     */
    private void requestPermission() {
        XXPermissions.with(SkillEditActivity.this)
                .constantRequest() //可设置被拒绝后继续申请，直到用户授权或者永久拒绝
                .permission(Permission.Group.STORAGE) //支持请求6.0悬浮窗权限8.0请求安装权限
                .permission(Permission.CAMERA)
                .request(new OnPermission() {
                    @Override
                    public void hasPermission(List<String> granted, boolean isAll) {
                        if (isAll) {
                            onResume();

                        }else {
                            Toast.makeText(SkillEditActivity.this,
                                    "必须同意所有的权限才能使用本程序", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void noPermission(List<String> denied, boolean quick) {
                        if(quick) {
                            Toast.makeText(SkillEditActivity.this, "被永久拒绝授权，请手动授予权限",
                                    Toast.LENGTH_SHORT).show();
                            // 如果是被永久拒绝就跳转到应用权限系统设置页面
                            XXPermissions.gotoPermissionSettings(SkillEditActivity.this);
                        }else {
                            Toast.makeText(SkillEditActivity.this, "获取权限失败",
                                    Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    }
                });
    }

    /*
     * 工具类
     */
    // 进行Luban算法压缩图片
    private void compressImage(final String filePath, final String savePath){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                CompressImage.getInstance().imageLubrnCompress(filePath, savePath, new CompressLubanListener() {
                    @Override
                    public void onCompressLubanSuccessed(String imgPath, Bitmap bitmap) {
                        /**
                         * 返回值: imgPath----压缩后图片的绝对路径
                         *        bitmap----返回的图片
                         */
                        Log.i(TAG, "Compress Success:" + imgPath);
                    }

                    @Override
                    public void onCompressLubanFailed(String imgPath, String msg) {
                        /**
                         * 返回值: imgPath----原图片的绝对路径
                         *        msg----返回的错误信息
                         */
                        Log.i(TAG, "Compress Failed:"+ imgPath + " " + msg);
                    }

                });
            }
        });
    }

    // 删除压缩的图片
    private void myDeleteFile (File file){
        String path = file.getPath();
        // 删除系统缩略图
        getContentResolver().delete(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, MediaStore.Images.Media.DATA + "=?", new String[]{path});
        // 删除手机中图片
        file.delete();
    }

    /*
     * 弹窗
     */
    /*
     * 提示弹框
     */
    private CommonDialog DialogToast(String mTitle, String mMsg){
        return new CommonDialog(SkillEditActivity.this, R.style.dialog, mMsg,
                new CommonDialog.OnCloseListener() {
                    @Override
                    public void onClick(Dialog dialog, boolean confirm) {
                        if (confirm) {
                            dialog.dismiss();
                            if(mUploadImageUrlList.size() > 0){
                                Intent intent = new Intent();
                                setResult(RESULT_OK, intent);
                            }
                            finish();
                        } else {
                            dialog.dismiss();
                        }
                    }
                }).setTitle(mTitle);
    }
    /*
     * 上传弹窗
     */
    private void showUploadProgressDialog(){
        mProgressDialog = new ProgressAlertDialog(this);
        mProgressDialog.setMessage("正在上传，请稍后...");
        mProgressDialog.setMax(mUploadImageUrlList.size());
        mProgressDialog.setProgress(0);
        mProgressDialog.show();
    }

    // 悬浮弹窗监听
    public void showAddImageWayMenu(View v){
        final PopupMenu popupMenu = new PopupMenu(this,v);
        popupMenu.getMenuInflater().inflate(R.menu.menu_upload_image_type,popupMenu.getMenu());

        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch(item.getItemId()){
                    case R.id.camera:
                        if(isHasPermission()){  // 有权限
                            startCameraActivity();
                        }else{      // 没有权限那么申请权限
                            requestPermission();
                        }
                        break;
                    case R.id.photo:
                        if(isHasPermission()){  // 有权限
                            startMultiImageSelectorActivity();
                        }else{      // 没有权限那么申请权限
                            requestPermission();
                        }
                    default:break;
                }
                return false;
            }
        });
        popupMenu.show();
    }

    @Override
    public void onBackPressed(){
        Log.i(TAG, "onBackPressed");
        if(mUploadImageUrlList.size() > 0){
            Intent intent = new Intent();
            setResult(RESULT_OK, intent);
        }

    }
}
