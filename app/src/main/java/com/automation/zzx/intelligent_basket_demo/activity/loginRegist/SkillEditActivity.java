package com.automation.zzx.intelligent_basket_demo.activity.loginRegist;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
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
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.automation.zzx.intelligent_basket_demo.R;
import com.automation.zzx.intelligent_basket_demo.activity.basket.BasketRepairActivity;
import com.automation.zzx.intelligent_basket_demo.adapter.areaAdmin.UploadImageLikeWxAdapter;
import com.automation.zzx.intelligent_basket_demo.entity.AppConfig;
import com.automation.zzx.intelligent_basket_demo.utils.ftp.FTPUtil;
import com.automation.zzx.intelligent_basket_demo.utils.http.HttpUtil;
import com.automation.zzx.intelligent_basket_demo.widget.dialog.CommonDialog;
import com.automation.zzx.intelligent_basket_demo.widget.dialog.ProgressAlertDialog;
import com.google.gson.Gson;
import com.hjq.permissions.OnPermission;
import com.hjq.permissions.Permission;
import com.hjq.permissions.XXPermissions;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.nereo.multi_image_selector.MultiImageSelectorActivity;
import okhttp3.Call;
import okhttp3.Response;

import static com.automation.zzx.intelligent_basket_demo.activity.basket.BasketDetailActivity.UPLOAD_BASKET_ID;
import static com.automation.zzx.intelligent_basket_demo.activity.basket.BasketDetailActivity.UPLOAD_BASKET_REPAIR_IMAGE;
import static com.automation.zzx.intelligent_basket_demo.activity.basket.BasketDetailActivity.UPLOAD_IMAGE_TEXT_TYPE;
import static com.automation.zzx.intelligent_basket_demo.activity.basket.BasketDetailActivity.UPLOAD_PROJECT_ID;

public class SkillEditActivity extends AppCompatActivity implements View.OnClickListener {

    private final static  String TAG = "SkillEditActivity";

    private final static int GET_PHOTO_FROM_ALBUM = 1;  // 相册
    public static final int TAKE_PHOTO_FROM_CAMERA= 2;  // 照相机

    // Handler消息
    private final static int GET_UPLOAD_INFO = 100;  // 上传图片成功
    private final static int GET_UPLOAD_WRONG = 101;  // 上传图片失败
    private final static int UPLOAD_PROGRESS_PARAMS = 102; // 更新文件进度条
    private final static int APPLY_REPAIR_BASKET = 103; // 申請報修


    //相册位置
    public static final String CAMERA_PATH = Environment.getExternalStorageDirectory() +
            File.separator + Environment.DIRECTORY_DCIM + File.separator+"Camera"+ File.separator;

    // 控件
    // 顶部导航栏
    private Toolbar mToolbar;  // 顶部导航栏
    private TextView mSendTextView; // 发送 按钮
    private ImageView mSendImageView; // 发送 图标

    private Spinner spinnerSkill;
    private String skillType;
    private List<String> type_list;
    private ArrayAdapter<String> typeAdapter;

    // 图片列表
    private GridView mImageGridView;
    private List<Bitmap> mUploadImageList = new ArrayList<>();
    private ArrayList<String> mUploadImageUrlList = new ArrayList<>();
    private ArrayList<String> tempFileNameList = new ArrayList<>();
    private UploadImageLikeWxAdapter mUploadImageLikeWxAdapter;

    //CAMERA
    private String fileName = "";  // 图片名
    private File photoFile ; // 图片文件
    private Uri photoUrl ; // 图片URL

    //基本信息
    private SharedPreferences pref;
    private SharedPreferences.Editor editor;

    private String token; // 验证token
    private String managerId; // 申请人Id
    private String mProjectId;  // 報修的项目编号
    private String mBasketId; // 報修的吊籃編號
    private String uploadType; // 上传图片类型
    private int maxUploadImageNumer; // 最大上传图片数量
    private String uploadHint; // 上传提示信息
    private String uploadUrl; // 上传地址
    private String reason;
    private Map<String, String> params = new HashMap<String, String>(); // 上传参数
    private EditText edtCommit;

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
                case UPLOAD_PROGRESS_PARAMS:  // 更新文件上傳進度軸參數
                    mProgressDialog.setProgress(msg.arg1);
                    break;
                case APPLY_REPAIR_BASKET:  // 申请報停
                    applyRepair();
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
        mSendTextView = (TextView) findViewById(R.id.toolbar_send_textview);
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
       skillType = null;
        if(type_list == null || type_list.isEmpty()){
            return;
        }
        spinnerSkill.setDropDownVerticalOffset(80); //下拉的纵向偏移
        typeAdapter = new ArrayAdapter<String>(this,R.layout.spinner_simple_item,type_list);
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSkill.setAdapter(typeAdapter);
        spinnerSkill.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                skillType= (String) spinnerSkill.getSelectedItem();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                skillType = "未选择";
            }
        });

    }
    private void initSpinner() {
        //技能选择
        type_list = new ArrayList<String>();
        type_list.add("电焊");
        type_list.add("高空作业");
        type_list.add("吊篮操作");
        type_list.add("其他");
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
            case R.id.toolbar_send_textview:  // 发送监听
                break;
            case R.id.toolbar_send_imageview: // 发送监听
                if(mUploadImageUrlList.size()==0){
                    Toast.makeText(SkillEditActivity.this, "请上传技能证书图片！", Toast.LENGTH_SHORT).show();
                } else {
                    showUploadProgressDialog();
                    startSendImage();
                }
                break;
            case R.id.btn_pro_commit:
                if(mUploadImageUrlList.size()==0){
                    Toast.makeText(SkillEditActivity.this, "请上传技能证书图片！", Toast.LENGTH_SHORT).show();
                } else {
                    showUploadProgressDialog();
                    startSendImage();
                }
                break;

        }
    }
    /*
     * 获取基本信息
     */
    // 用户信息
    private void getBaseInfoFromPred(){
        pref = PreferenceManager.getDefaultSharedPreferences(this);
        managerId = pref.getString("userId", "");
        token = pref.getString("loginToken", "");
    }

    // 设置上传图片的参数、路径等
    private void setUploadParameters(){
        if(uploadType==null || uploadType.equals(""))
            return;
        switch (uploadType){
            case UPLOAD_BASKET_REPAIR_IMAGE: // 预验收申请图片地址
                mRemotePath = "storageRepair";
                mToolbar.setTitle("申请吊篮保修");
                uploadHint = "吊篮故障图片";
                maxUploadImageNumer = 9;
                break;
        }
    }


    // FTP 初始化
    private void initFTPClient(){
        mFTPClient = new FTPUtil(AppConfig.FILE_SERVER_YBLIU_IP, AppConfig.FILE_SERVER_YBLIU_PORT,
                AppConfig.FILE_SERVER_USERNAME, AppConfig.FILE_SERVER_PASSWORD);
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
                    tempFileNameList.clear();
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");// HH:mm:ss
                    //获取当前时间
                    Date date = new Date(System.currentTimeMillis());
                    String time = simpleDateFormat.format(date);
                    for(int i=0; i < mUploadImageUrlList.size(); i++){
                        Message message = new Message();
                        message.what = UPLOAD_PROGRESS_PARAMS;
                        message.arg1 = i;
                        mHandler.sendMessage(message);
                        String tempFileName = "";
                        if(uploadType.equals(UPLOAD_BASKET_REPAIR_IMAGE)) {  // 报修命名 projectId_deviceId_num.jpg
                            tempFileName = mProjectId + "_" + mBasketId + "_" + (i + 1) + "_" + time + ".jpg";
                            tempFileNameList.add(tempFileName);
                        }

                        mFTPClient.uploadingSingleRenameFile(new File(mUploadImageUrlList.get(i)), tempFileName);
                    }
                    mFTPClient.closeConnect();  // 关闭连接

                    // 文件上传成功后操作
                    switch(uploadType){
                        case UPLOAD_BASKET_REPAIR_IMAGE:  // 报修申請
                            mHandler.sendEmptyMessage(APPLY_REPAIR_BASKET);
                            break;
                    }
                } catch (IOException e) {
                    // 上传文件失败
                    e.printStackTrace();
                    mHandler.sendEmptyMessage(GET_UPLOAD_WRONG);
                }
            }
        }.start();
    }


    // 申請報停
    private void applyRepair(){
        String reason = edtCommit.getText().toString();
        if(params != null){
            params.clear();
        }
        params.put("deviceId",mBasketId);
        params.put("managerId",managerId);
        params.put("reason",reason);
        for(int i=1;i<=tempFileNameList.size();i++){
            params.put("pic_"+i,tempFileNameList.get(i-1));
        }
        String json = new Gson().toJson(params);
        HttpUtil.sendRentAdminRepairOkHttpRequest(new okhttp3.Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                //异常情况处理
                Looper.prepare();
                Toast.makeText(SkillEditActivity.this, "网络连接失败！", Toast.LENGTH_SHORT).show();
                Looper.loop();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.code() != 200) {
                    Looper.prepare();
                    Toast.makeText(SkillEditActivity.this, "网络连接超时,请稍后重试！", Toast.LENGTH_SHORT).show();
                    Looper.loop();
                }
                // 返回服务器数据
                String responseData = response.body().string();
                try {
                    Message msg = new Message();
                    JSONObject jsonObject = JSON.parseObject(responseData);
                    String create = jsonObject.getString("create");
                    if(create.equals("success")){
                        msg.what = GET_UPLOAD_INFO;
                    }else{
                        msg.what = GET_UPLOAD_WRONG;
                    }
                    mHandler.sendEmptyMessage(msg.what);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        },json, token);
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
