package com.automation.zzx.intelligent_basket_demo.activity.basket;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.automation.zzx.intelligent_basket_demo.R;
import com.automation.zzx.intelligent_basket_demo.entity.UserInfo;
import com.automation.zzx.intelligent_basket_demo.widget.dialog.CommonDialog;
import com.lcw.library.stickerview.Sticker;
import com.lcw.library.stickerview.StickerLayout;

public class BasketStickerActivity extends AppCompatActivity {

    private final static String TAG = "BasketStickerActivity";

   //控件布局
    private ImageView mStickerLayout;
    private CommonDialog mCommonDialog;
    private TextView editStickerTv;
    private ImageView addStickerIv;
    private ImageView saveStickerIv;


    //页面状态：查看 or 编辑
    private int UiState = VIEWSTATE;
    private final static int VIEWSTATE = 101;
    private final static int EDITSTATE = 102;

    // 用户登录信息相关
    private UserInfo mUserInfo;
    private String mProjectId;
    private String mToken;
    private SharedPreferences mPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_basket_sticker);

        // 初始化控件
        initWidgetResource();
        // 初始化用户信息
        getUserInfo();

        //用户点击“编辑”按钮时
        findViewById(R.id.toolbar_editBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*
                * 1、判断是否具有编辑权限
                * 2、更改头部UI，隐藏编辑按钮，展示新增及保存按钮
                * 3、更改吊篮UI，展示编号字符
                * */
                String userRole = mUserInfo.getUserRole();
                //if(userRole.equals("areaAdmin") || userRole.equals("projectAdmin")){
                    UiState = EDITSTATE;
                    turnToEditState();
                //} else {
                //    Toast.makeText(BasketStickerActivity.this,"当前角色暂无编辑权限！",Toast.LENGTH_LONG).show();
               // }
            }
        });

/*        //用户点击“新增”按钮时
        findViewById(R.id.toolbar_addSticker).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Sticker sticker = new Sticker(BasketStickerActivity.this,
                        BitmapFactory.decodeResource(BasketStickerActivity.this.getResources(), R.mipmap.ic_launcher_round));
                mStickerLayout.addSticker(sticker);
            }
        });*/

        //用户点击“保存”按钮时
        findViewById(R.id.toolbar_updateChange).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mCommonDialog == null) {
                    mCommonDialog = initDialog("是否点击确认保存图片");
                }
                mCommonDialog.show();
            }
        });


    }

    /*
     * 控件初始化
     */
    private void initWidgetResource() {
        // 顶部导航栏
        Toolbar toolbar = (Toolbar) findViewById(R.id.upload_toolbar);
        toolbar.setTitle(getString(R.string.basketSticker_title));
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true); //设置返回键可用

        mStickerLayout = findViewById(R.id.sl_sticker_layout);
        editStickerTv = findViewById(R.id.toolbar_editBtn);
        addStickerIv = findViewById(R.id.toolbar_addSticker);
        saveStickerIv = findViewById(R.id.toolbar_updateChange);

        mStickerLayout.setOnTouchListener(onTouchListener);

    }


    /*
     * 解析用户信息
     */
    // 获取用户数据
    private void getUserInfo(){
        // 从本地获取数据
        mPref = PreferenceManager.getDefaultSharedPreferences(this);
        mUserInfo = new UserInfo();
        mUserInfo.setUserId(mPref.getString("userId", ""));
        mUserInfo.setUserPhone(mPref.getString("userPhone", ""));
        mUserInfo.setUserRole(mPref.getString("userRole", ""));
        mToken = mPref.getString("loginToken","");
        mProjectId = mPref.getString("projectId","");
    }

    /*
    * 由查看状态切换为编辑状态的UI
    * */
    private void turnToEditState(){
        //更改头部UI，隐藏编辑按钮，展示新增及保存按钮
        editStickerTv.setVisibility(View.GONE);
        addStickerIv.setVisibility(View.VISIBLE);
        saveStickerIv.setVisibility(View.VISIBLE);
        //更改吊篮图标状态，可操作图标且展示编号字符

    }


    /*
     * 由编辑状态切换为查看状态的UI
     * */
    private void turnToViewState(){
        //更改头部UI，展示编辑按钮，隐藏新增及保存按钮
        editStickerTv.setVisibility(View.VISIBLE);
        addStickerIv.setVisibility(View.GONE);
        saveStickerIv.setVisibility(View.GONE);
        //更改吊篮图标状态，可操作图标且展示编号字符

    }

    /*
     * 提示弹框
     */
    private CommonDialog initDialog(final String mMsg){
        return new CommonDialog(this, R.style.dialog, mMsg,
                new CommonDialog.OnCloseListener() {
                    @Override
                    public void onClick(Dialog dialog, boolean confirm) {
                        if(confirm){
                            turnToViewState();
                            Toast.makeText(BasketStickerActivity.this, "点击确认", Toast.LENGTH_SHORT).show();
                        }else{
                            dialog.dismiss();
                        }
                    }
                }).setTitle("提示");
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


    //事件监听方法
    private View.OnTouchListener onTouchListener = new View.OnTouchListener() {

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    float x = event.getX();
                    float y = event.getY();
                    Toast.makeText(BasketStickerActivity.this,"("+x+","+y+")",Toast.LENGTH_LONG).show();
                    break;
                default:
                    break;
            }
            return false;
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }

}
