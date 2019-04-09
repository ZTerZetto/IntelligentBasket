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
import com.automation.zzx.intelligent_basket_demo.utils.HttpUtil;
import com.automation.zzx.intelligent_basket_demo.utils.ToastUtil;
import com.automation.zzx.intelligent_basket_demo.widget.dialog.CommonDialog;
import com.hjq.permissions.OnPermission;
import com.hjq.permissions.Permission;
import com.hjq.permissions.XXPermissions;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import me.nereo.multi_image_selector.MultiImageSelectorActivity;
import okhttp3.Call;
import okhttp3.Response;


public class UploadImageActivity extends AppCompatActivity implements View.OnClickListener {

    private final static  String TAG = "UploadImageActivity";

    private final static int GET_PHOTO_FROM_ALBUM = 1;  // 相册
    public static final int TAKE_PHOTO_FROM_CAMERA= 2;  // 照相机

    // Handler消息
    private final static int GET_UPLOAD_INFO = 100;
    private final static int GET_UPLOAD_WRONG = 101;

    private final static String PROJECT_ID = "project_id";


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
    private String projectId;
    private String token;


    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case GET_UPLOAD_INFO:
                    DialogToast("提示", "您已成功上传安检证书！").show();
                    break;
                case GET_UPLOAD_WRONG:
                    DialogToast("提示", "安检证书上传失败！").show();
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

        //获取基本信息
        pref = PreferenceManager.getDefaultSharedPreferences(this);
        token = pref.getString("loginToken", "");
        //projectId = pref.getString("projectId", "");
        Intent intent = new Intent();
        projectId = intent.getStringExtra("project_id");
        if(projectId==null) projectId = "001";


        // 获取权限
        if(!isHasPermission()) requestPermission();
        // 初始化控件
        initWidgetResource();
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
                    //点击了添加图片按钮，弹出弹窗
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
                onBackPressed();
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
    public void startCameraActivity(){
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
        intent.putExtra(MultiImageSelectorActivity.EXTRA_SELECT_COUNT, 9);// 最大图片选择数量
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
     * 上传照片至服务器
     */
    private void startSendImage(){
        HttpUtil.uploadPicOkHttpRequest(new okhttp3.Callback() {

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
                            msg.what = GET_UPLOAD_INFO;
                            //上传成功操作
                            break;
                        case "1":
                            msg.what = GET_UPLOAD_WRONG;
                            break;
                    }
                    handler.sendEmptyMessage(msg.what);
                } else {
                    int errorCode = response.code();
                    switch (errorCode){
                        case 201:
                            ToastUtil.showToastTips(UploadImageActivity.this, "安检证书已上传，请勿重复提交！");
                            finish();
                            break;
                        case 401:
                            ToastUtil.showToastTips(UploadImageActivity.this, "登陆已过期，请重新登录");
                            startActivity(new Intent(UploadImageActivity.this, LoginActivity.class));
                            finish();
                            break;
                        case 403:
                            break;
                    }
                }
            }

        },mUploadImageUrlList,projectId,token);
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
                            finish();
                        } else {
                            dialog.dismiss();
                        }
                    }
                }).setTitle(mTitle);
    }
}
