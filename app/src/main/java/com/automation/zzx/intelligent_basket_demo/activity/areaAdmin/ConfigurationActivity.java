package com.automation.zzx.intelligent_basket_demo.activity.areaAdmin;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.automation.zzx.intelligent_basket_demo.R;
import com.automation.zzx.intelligent_basket_demo.utils.http.HttpUtil;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Response;


public class ConfigurationActivity extends AppCompatActivity implements View.OnClickListener {

    public final static String PROJECT_ID = "projectId";  // 上传图片的项目Id

    // Handler消息
    private final static int GET_UPLOAD_INFO = 100;   // 上传配置清单成功
    private final static int GET_UPLOAD_WRONG = 101;  // 上传配置清单失败

    // 控件
    // 顶部导航栏
    private Toolbar mToolbar;  // 顶部导航栏
    private TextView mSendTextView; // 发送 标题
    private ImageView mSendImageView; // 发送 图标
    private TextView txtProjectId; // 项目编号
    private EditText txtSixMetersNum; // 六米吊篮数目
    private Button btSendConfig; //提交配置清单按钮

    // 上传参数
    private Map<String, String> params = new HashMap<String, String>();

    //基本信息
    private SharedPreferences pref;
    private String token; // 验证token
    private String mProjectId;  // 報修的项目编号

    //上传结果
    private String uploadResult = " ";

    @SuppressLint("HandlerLeak")
    public Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case GET_UPLOAD_INFO:  // 上传图片成功
                    Toast.makeText(ConfigurationActivity.this, "提交配置清单成功！", Toast.LENGTH_SHORT).show();
                    uploadResult = "success";
                    finish();
                    break;
                case GET_UPLOAD_WRONG: // 上传图片失败
                    Toast.makeText(ConfigurationActivity.this, "提交配置清单失败,请稍后重试！", Toast.LENGTH_SHORT).show();
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configuration);

        initWidgetResource();  // 初始化控件
        //获取基本信息
        getBaseInfoFromPred();
        getProjectInfoFromIntent();

    }



    private void initWidgetResource(){
        // 顶部导航栏
        mToolbar = (Toolbar) findViewById(R.id.upload_toolbar);
        mToolbar.setTitle("配置吊篮清单");
        setSupportActionBar(mToolbar);
        getSupportActionBar().setHomeButtonEnabled(true); //设置返回键可用
        mSendTextView = (TextView) findViewById(R.id.toolbar_send_textview);
        mSendTextView.setOnClickListener(this);
        mSendImageView = (ImageView) findViewById(R.id.toolbar_send_configuration);
        mSendImageView.setOnClickListener(this);

        //获取资源控件
        mSendImageView = findViewById(R.id.toolbar_send_configuration);
        mSendImageView.setOnClickListener(this);

        txtProjectId  = findViewById(R.id.txt_project_id);
        txtSixMetersNum  = findViewById(R.id.txt_sixMeter_num);

        btSendConfig = findViewById(R.id.bt_send_configuration);
        btSendConfig.setOnClickListener(this);
    }

    /*
     * 获取基本信息
     */
    // 用户信息
    private void getBaseInfoFromPred(){
        pref = PreferenceManager.getDefaultSharedPreferences(this);
        token = pref.getString("loginToken", "");
    }

    // 项目信息
    private void getProjectInfoFromIntent(){
        Intent intent = getIntent();
        mProjectId = intent.getStringExtra(PROJECT_ID);

        txtProjectId.setText(mProjectId);

    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.toolbar_send_configuration:
                httpSubmitParam();
                break;
            case R.id.bt_send_configuration:
                httpSubmitParam();
                break;
            default:
                break;
        }
    }

    //提交修改申请
    private void httpSubmitParam(){
        if(params != null){
            params.clear();
        }
        params.put("projectId",mProjectId);
        params.put("sixMetersNum",txtSixMetersNum.getText().toString());
        String json = new Gson().toJson(params);

        HttpUtil.setConfigurationOkHttpRequest(new okhttp3.Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                //异常情况处理
                Looper.prepare();
                Toast.makeText(ConfigurationActivity.this, "网络连接失败！", Toast.LENGTH_SHORT).show();
                Looper.loop();
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if(response.code() != 200){
                    Looper.prepare();
                    Toast.makeText(ConfigurationActivity.this, "网络连接超时,请稍后重试！", Toast.LENGTH_SHORT).show();
                    Looper.loop();
                }
                // 返回服务器数据
                String responseData = response.body().string();
                try {
                    Message msg = new Message();
                    JSONObject jsonObject = JSON.parseObject(responseData);
                    String result = jsonObject.getString("push");
                    if(result.equals("success")){
                        msg.what = GET_UPLOAD_INFO;
                        mHandler.sendEmptyMessage(msg.what);
                    } else {
                        msg.what = GET_UPLOAD_WRONG;
                        mHandler.sendEmptyMessage(msg.what);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        },token,json);
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

    @Override
    public void onBackPressed(){
        super.onBackPressed();
        if(uploadResult.equals("success")) {
            Intent intent = new Intent();
            setResult(RESULT_OK, intent);
        }else {
            Intent intent = new Intent();
            setResult(RESULT_CANCELED, intent);
        }
    }
}
