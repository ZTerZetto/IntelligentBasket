package com.automation.zzx.intelligent_basket_demo.activity.areaAdmin;

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
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.automation.zzx.intelligent_basket_demo.R;
import com.automation.zzx.intelligent_basket_demo.activity.loginRegist.LoginActivity;
import com.automation.zzx.intelligent_basket_demo.adapter.areaAdmin.UploadImageLikeWxAdapter;
import com.automation.zzx.intelligent_basket_demo.utils.ToastUtil;
import com.automation.zzx.intelligent_basket_demo.utils.http.ProgressRequestBody;
import com.automation.zzx.intelligent_basket_demo.utils.http.ProgressRequestListener;
import com.automation.zzx.intelligent_basket_demo.utils.okhttp.BaseCallBack;
import com.automation.zzx.intelligent_basket_demo.utils.okhttp.BaseOkHttpClient;
import com.automation.zzx.intelligent_basket_demo.widget.dialog.CommonDialog;
import com.automation.zzx.intelligent_basket_demo.widget.dialog.ProgressAlertDialog;
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
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static com.automation.zzx.intelligent_basket_demo.entity.AppConfig.AREA_ADMIN_BEGIN_PROJECT;
import static com.automation.zzx.intelligent_basket_demo.entity.AppConfig.AREA_ADMIN_CREATE_CERT_FILE;
import static com.automation.zzx.intelligent_basket_demo.entity.AppConfig.AREA_ADMIN_CREATE_PRESTOP_FILE;
import static com.automation.zzx.intelligent_basket_demo.fragment.areaAdmin.AreaAdminMgProjectFragment.PROJECT_ID;
import static com.automation.zzx.intelligent_basket_demo.fragment.areaAdmin.AreaAdminMgProjectFragment.UPLOAD_BASKETS_PRESTOP_IMAGE;
import static com.automation.zzx.intelligent_basket_demo.fragment.areaAdmin.AreaAdminMgProjectFragment.UPLOAD_CERTIFICATE_IMAGE;
import static com.automation.zzx.intelligent_basket_demo.fragment.areaAdmin.AreaAdminMgProjectFragment.UPLOAD_IMAGE_TYPE;


public class UploadImageActivity extends AppCompatActivity implements View.OnClickListener {

    private final static  String TAG = "UploadImageActivity";

    private final static int GET_PHOTO_FROM_ALBUM = 1;  // 相册
    public static final int TAKE_PHOTO_FROM_CAMERA= 2;  // 照相机

    // Handler消息
    private final static int GET_UPLOAD_INFO = 100;  // 上传图片成功
    private final static int GET_UPLOAD_WRONG = 101;  // 上传图片失败
    private final static int APPLY_BEGIN_PROJECT = 102; // 申请开始项目
    public final static int UPLOAD_PROGRESS_PARAMS = 103; // 上传文件进度


    //相册位置
    public static final String CAMERA_PATH= Environment.getExternalStorageDirectory() +
            File.separator + Environment.DIRECTORY_DCIM + File.separator+"Camera"+ File.separator;

    // 控件
    // 顶部导航栏
    private Toolbar mToolbar;  // 顶部导航栏
    private TextView mSendTextView; // 发送 按钮
    private ImageView mSendImageView; // 发送 图标

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
    private String managerId; // 申请人Id
    private String projectId;  // 上传的项目编号
    private String uploadType; // 上传图片类型
    private int maxUploadImageNumer; // 最大上传图片数量
    private String uploadHint; // 上传提示信息
    private String uploadUrl; // 上传地址
    private Map<String, String> params = new HashMap<String, String>(); // 上传参数

    // 弹窗
    private ProgressAlertDialog mProgressDialog; // 文件加载进度

    @SuppressLint("HandlerLeak")
    public Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case GET_UPLOAD_INFO:  // 上传图片成功
                    DialogToast("提示", "您已成功上传" + uploadHint).show();
                    mProgressDialog.dismiss();
                    break;
                case GET_UPLOAD_WRONG: // 上传图片失败
                    DialogToast("提示", uploadHint + "上传失败！").show();
                    mProgressDialog.dismiss();
                    break;
                case APPLY_BEGIN_PROJECT:  // 申请开始
                    beginProject();
                    break;
                case UPLOAD_PROGRESS_PARAMS:  // 更新参数
                    mProgressDialog.setProgress(msg.arg1);
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_image);

        // 获取权限
        if(!isHasPermission()) requestPermission();
        // 初始化控件
        initWidgetResource();

        //获取基本信息
        getBaseInfoFromPred();
        getProjectInfoFromIntent();
        setUploadParameters();
    }

    /*
     * 控件初始化
     */
    private void initWidgetResource() {
        // 顶部导航栏
        mToolbar = (Toolbar) findViewById(R.id.upload_toolbar);
        mToolbar.setTitle("");
        setSupportActionBar(mToolbar);
        getSupportActionBar().setHomeButtonEnabled(true); //设置返回键可用
        mSendTextView = (TextView) findViewById(R.id.toolbar_send_textview);
        mSendTextView.setOnClickListener(this);
        mSendImageView = (ImageView) findViewById(R.id.toolbar_send_imageview);
        mSendImageView.setOnClickListener(this);

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
    // 按钮点击响应
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.toolbar_send_textview:  // 发送监听
                break;
            case R.id.toolbar_send_imageview: // 发送监听
                showUploadProgressDialog();
                startSendImage();
                break;
        }
    }

    //悬浮弹窗监听
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

    /*
     * 活动返回监听
     */
    //页面返回数据监听
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        switch (requestCode){
            case TAKE_PHOTO_FROM_CAMERA:        //拍摄
                if(resultCode == RESULT_CANCELED){
                    Toast.makeText(UploadImageActivity.this, "取消了拍照", Toast.LENGTH_SHORT).show();
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

    /*
     * 跳转照相机页面
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

    /*
     * 跳转至相册选取页面
     */
    public void startMultiImageSelectorActivity(){
        Intent intent = new Intent(UploadImageActivity.this, MultiImageSelectorActivity.class);
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
    public void uploadPicWithProgressOkHttpRequest(okhttp3.Callback callback, final ArrayList<String> fileList,
                                                          Map<String, String> params , String token, String URL) {
        OkHttpClient client = new OkHttpClient();
        MultipartBody.Builder MultipartBodyBuilder = new MultipartBody.Builder()
                .setType(MultipartBody.FORM);
        // 添加参数清单
        for(Map.Entry<String, String> entry : params.entrySet()){
            MultipartBodyBuilder.addFormDataPart(entry.getKey(), entry.getValue());
        }
        // 添加文件清单
        for(int i = 0; i < fileList.size();i++){
            final int index = i;
            File file = new File(fileList.get(i));
            RequestBody fileBody = RequestBody.create(MediaType.parse("image/jpg"),file);
            MultipartBodyBuilder.addFormDataPart("file", file.getName(),
                    new ProgressRequestBody(fileBody, new ProgressRequestListener() {
                        @Override
                        public void onRequestProgress(long bytesWritten, long contentLength, boolean done) {
                            Log.i(TAG, "file:"+index+" bytesWritten:"+bytesWritten+
                                    " contentLength:"+contentLength);
                            Message message = new Message();
                            message.what = UPLOAD_PROGRESS_PARAMS;
                            message.arg1 = index;
                            //message.obj = (float) bytesWritten / contentLength;
                            handler.sendMessage(message);
                        }
                    }));
        }
        MultipartBody builder = MultipartBodyBuilder.build();
        final Request request = new Request.Builder()
                .url(URL)
                .addHeader("Authorization",token)
                .post(builder)
                .build();
        client.newCall(request).enqueue(callback);
    }
    private void startSendImage(){
        uploadPicWithProgressOkHttpRequest(new okhttp3.Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
            //异常情况处理
                Looper.prepare();
                Toast.makeText(UploadImageActivity.this, "网络连接失败！", Toast.LENGTH_LONG).show();
                Looper.loop();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                // 返回服务器数据
                if (response.isSuccessful()) {
                    String responseData = response.body().string();
                    JSONObject jsonObject = JSON.parseObject(responseData);
                    String error = jsonObject.getString("error");
                    Message msg = new Message();
                    switch (error){
                        case "0":
                            if(uploadType.equals(UPLOAD_BASKETS_PRESTOP_IMAGE))
                                handler.sendEmptyMessage(APPLY_BEGIN_PROJECT);  // 提出开始项目申请
                            else
                                handler.sendEmptyMessage(GET_UPLOAD_INFO);
                            break;
                        case "1":
                            handler.sendEmptyMessage(GET_UPLOAD_WRONG);
                            break;
                    }
                } else {
                    int errorCode = response.code();
                    switch (errorCode){
                        case 201:
                            ToastUtil.showToastTips(UploadImageActivity.this,
                                    uploadHint + "已上传，请勿重复提交！");
                            finish();
                            break;
                        case 401:
                            ToastUtil.showToastTips(UploadImageActivity.this,
                                    "登陆已过期，请重新登录");
                            startActivity(new Intent(UploadImageActivity.this,
                                    LoginActivity.class));
                            finish();
                            break;
                        case 403:
                            break;
                    }
                }
            }

        }, mUploadImageUrlList, params, token, uploadUrl);
    }

    // 开始项目
    private void beginProject(){
        BaseOkHttpClient.newBuilder()
                .addHeader("Authorization", token)
                .addParam("projectId", projectId)
                .addParam("picNum", mUploadImageUrlList.size())
                .addParam("managerId", managerId)
                .post()
                .url(AREA_ADMIN_BEGIN_PROJECT)
                .build()
                .enqueue(new BaseCallBack() {
                    @Override
                    public void onSuccess(Object o) {
                        Log.d(TAG, "成功提交开始项目申请");
                        handler.sendEmptyMessage(GET_UPLOAD_INFO);
                    }

                    @Override
                    public void onError(int code) {
                        Log.d(TAG, "提交开始项目申请错误，错误编码："+code);
                    }

                    @Override
                    public void onFailure(Call call, IOException e) {
                        Log.d(TAG, "提交开始项目申请失败");
                    }
                });
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
    // 项目/图片信息
    private void getProjectInfoFromIntent(){
        Intent intent = getIntent();
        projectId = intent.getStringExtra(PROJECT_ID);
        uploadType = intent.getStringExtra(UPLOAD_IMAGE_TYPE);
    }
    // 设置上传图片的参数、路径等
    private void setUploadParameters(){
        if(uploadType==null || uploadType.equals(""))
            return;
        switch (uploadType){
            case UPLOAD_CERTIFICATE_IMAGE: // 安监证书地址
                uploadUrl = AREA_ADMIN_CREATE_CERT_FILE;
                mToolbar.setTitle("上传安监证书");
                uploadHint = "安监证书";
                maxUploadImageNumer = 1;
                break;
            case UPLOAD_BASKETS_PRESTOP_IMAGE: // 预验收申请图片地址
                uploadUrl = AREA_ADMIN_CREATE_PRESTOP_FILE;
                mToolbar.setTitle("上传安装预验收图片");
                uploadHint = "安装预验收图片";
                maxUploadImageNumer = 9;
                break;
            default: // 默认预验收申请地址
                uploadUrl = AREA_ADMIN_CREATE_PRESTOP_FILE;
                mToolbar.setTitle("上传安装预验收图片");
                uploadHint = "安装预验收图片";
                maxUploadImageNumer = 9;
                break;
        }
        params.clear();
        params.put("projectId", projectId);
    }

    /*
     * 申请权限
     */
    private void requestPermission() {
        XXPermissions.with(UploadImageActivity.this)
                .constantRequest() //可设置被拒绝后继续申请，直到用户授权或者永久拒绝
                .permission(Permission.Group.STORAGE) //支持请求6.0悬浮窗权限8.0请求安装权限
                .permission(Permission.CAMERA)
                .request(new OnPermission() {
                    @Override
                    public void hasPermission(List<String> granted, boolean isAll) {
                        if (isAll) {
                            onResume();

                        }else {
                            Toast.makeText(UploadImageActivity.this,
                                    "必须同意所有的权限才能使用本程序", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void noPermission(List<String> denied, boolean quick) {
                        if(quick) {
                            Toast.makeText(UploadImageActivity.this, "被永久拒绝授权，请手动授予权限",
                                    Toast.LENGTH_SHORT).show();
                            // 如果是被永久拒绝就跳转到应用权限系统设置页面
                            XXPermissions.gotoPermissionSettings(UploadImageActivity.this);
                        }else {
                            Toast.makeText(UploadImageActivity.this, "获取权限失败",
                                    Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    }
                });
    }
    // 是否有权限：摄像头、拨打电话
    private boolean isHasPermission() {
        if (XXPermissions.isHasPermission(UploadImageActivity.this, Permission.Group.STORAGE)
                && XXPermissions.isHasPermission(UploadImageActivity.this, Permission.CAMERA))
            return true;
        return false;
    }

    /*
     * 弹窗
     */
    /*
     * 提示弹框
     */
    private CommonDialog DialogToast(String mTitle, String mMsg){
        return new CommonDialog(UploadImageActivity.this, R.style.dialog, mMsg,
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


    @Override
    public void onBackPressed(){
        Log.i(TAG, "onBackPressed");
        if(mUploadImageUrlList.size() > 0){
            Intent intent = new Intent();
            setResult(RESULT_OK, intent);
        }
        finish();
    }
}
