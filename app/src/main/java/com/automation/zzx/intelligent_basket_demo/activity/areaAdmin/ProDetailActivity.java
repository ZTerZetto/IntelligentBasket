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
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.automation.zzx.intelligent_basket_demo.R;
import com.automation.zzx.intelligent_basket_demo.entity.UserInfo;
import com.automation.zzx.intelligent_basket_demo.utils.http.HttpUtil;
import com.automation.zzx.intelligent_basket_demo.activity.loginRegist.LoginActivity;
import com.automation.zzx.intelligent_basket_demo.entity.ProjectInfo;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Response;

public class ProDetailActivity extends AppCompatActivity {

    // 页面跳转
    private final static int COMMIT_ACTIVITY_RESULT = 1;

    // Handler消息
    private final static int GET_PROJECT_INFO = 100;
    private final static int GET_PROJECT_INFO_FAIL = 101;
    private final static int GET_COMMIT_ACTIVITY = 102;


    private ProjectInfo projectInfo;
    private UserInfo adminAreaUser = new UserInfo(); //区域管理员
    private UserInfo adminRentUser = new UserInfo(); //租方管理员
    private String projectAdminName; //项目负责人名字

    private String mProjectId;

    private TextView txtProName;
    private TextView txtProNumber;
    private TextView txtProState;
    private LinearLayout llProContract;
    private TextView txtProStart;
    private TextView txtProEnd;
    private TextView txtProRent;
    private TextView txtProArea;
    private TextView txtProBuilder;
    private TextView txtProAreaUser;
    private Button btnProCommit;

    public SharedPreferences pref;
    private String token;

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case GET_PROJECT_INFO:
                    projectInfo = JSON.parseObject(msg.obj.toString(), ProjectInfo.class);
                    if(projectInfo!=null){
                        txtProName.setText(projectInfo.getProjectName());
                        txtProNumber.setText("No:"+projectInfo.getProjectId());
                        txtProState.setText(projectInfo.getProjectState());
                        txtProStart.setText(projectInfo.getProjectStart());
                        txtProEnd.setText(projectInfo.getProjectEnd());
                        String rentAdmin = adminRentUser.getUserName()+"("+projectInfo.getAdminRentId()+")";//张三(9999)
                        txtProRent.setText(rentAdmin);
                        String areaAdmin = adminAreaUser.getUserName()+"("+projectInfo.getAdminAreaId()+")";//张三(9999)
                        txtProArea.setText(areaAdmin);
                        txtProBuilder.setText(projectInfo.getProjectBuilders());
                        String proAdmin = projectAdminName+"("+projectInfo.getAdminProjectId()+")";//张三(9999)
                        txtProAreaUser.setText(proAdmin);

                        //“项目验收申请”按键设置可见
                        if(projectInfo.getProjectState().equals("进行中")){
                            btnProCommit.setVisibility(View.VISIBLE);
                        }else{
                            btnProCommit.setVisibility(View.GONE);
                        }
                    }
                    break;
                case GET_PROJECT_INFO_FAIL:
                    Toast.makeText(ProDetailActivity.this, "没有权限访问！", Toast.LENGTH_LONG).show();
                    finish();
                    break;
                case GET_COMMIT_ACTIVITY:
                    if(msg.obj.toString().equals("ready")){
                        btnProCommit.setText("等待验收审核...");
                        btnProCommit.setClickable(false);
                    }
                    break;
                default:
                    break;
            }
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pro_detail);

        // 顶部导航栏
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        TextView titleText = (TextView) findViewById(R.id.toolbar_title);
        toolbar.setTitle(getString(R.string.projectDetail_tile));
        titleText.setText("");
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true); //设置返回键可用

        txtProName = findViewById(R.id.txt_pro_name);
        txtProNumber =  findViewById(R.id.txt_pro_number);
        txtProState =  findViewById(R.id.txt_pro_state);
        btnProCommit = findViewById(R.id.btn_pro_commit);
        llProContract = findViewById(R.id.ll_pro_contract);
        txtProStart = findViewById(R.id.txt_pro_start_time);
        txtProEnd = findViewById(R.id.txt_pro_end_time);
        txtProRent = findViewById(R.id.txt_pro_area_admin);
        txtProArea = findViewById(R.id.txt_pro_rent_admin);
        txtProBuilder = findViewById(R.id.txt_pro_builder);
        txtProAreaUser = findViewById(R.id.txt_pro_area_user);

        pref = PreferenceManager.getDefaultSharedPreferences(this);
        token = pref.getString("loginToken", "");
        if (token == null) {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
        }

        Intent intent = this.getIntent();
        projectInfo = (ProjectInfo)intent.getSerializableExtra("projectDetail");
        if(projectInfo!=null){
            txtProName.setText(projectInfo.getProjectName());
            txtProNumber.setText(projectInfo.getProjectId());
            txtProState.setText(projectInfo.getProjectState());
            txtProStart.setText(projectInfo.getProjectStart());
            txtProEnd.setText(projectInfo.getProjectEnd());
            txtProRent.setText(projectInfo.getAdminRentId());
            txtProArea.setText(projectInfo.getAdminAreaId());
            txtProBuilder.setText(projectInfo.getProjectBuilders());
        } else {
            mProjectId = intent.getStringExtra("projectId");
            if(mProjectId != null){
                initProState();
            } else {
                mProjectId = "001";
                initProState();
            }
        }


        //项目验收提交申请跳转
        btnProCommit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ProDetailActivity.this, ProCommitActivity.class);
                intent.putExtra("projectInfo",projectInfo);
                startActivityForResult(intent, COMMIT_ACTIVITY_RESULT);


            }
        });

        //项目合同书界面跳转
        llProContract.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ProDetailActivity.this,CheckCompactActivity.class);
                startActivity(intent);
            }
        });


    }

    private void initProState( ) {
        HttpUtil.getProjectDetailInfoOkHttpRequest(new okhttp3.Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                //异常情况处理
                Looper.prepare();
                Toast.makeText(ProDetailActivity.this, "网络连接失败！", Toast.LENGTH_LONG).show();
                Looper.loop();
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                // 返回服务器数据
                String responseData = response.body().string();
                try {
                    JSONObject jsonObject = JSON.parseObject(responseData);
                    String isAllowed = jsonObject.getString("isAllowed");
                    Message msg = new Message();
                    if(isAllowed.equals("true")){
                        projectAdminName = jsonObject.getString("projectAdminName");
                        JSONObject projectObj = jsonObject.getJSONObject("projectDetail");
                        if(projectObj != null){
                            JSONObject adminArea = projectObj.getJSONObject("adminAreaUser");
                            adminAreaUser = new UserInfo(adminArea.getString("userId"),adminArea.getString("userName"),adminArea.getString("userPassword"),
                                    adminArea.getString("userPhone"),adminArea.getString("userRole"),adminArea.getString("userPerm"),
                                    adminArea.getString("userImage"),adminArea.getBoolean("checked"));
                            //String userId, String userName, String userPassword, String userPhone, String userRole,
                            //                    String userPerm, String userImage, boolean checked
                            JSONObject adminRent = projectObj.getJSONObject("adminRentUser");
                            adminRentUser = new UserInfo(adminRent.getString("userId"),adminRent.getString("userName"),adminRent.getString("userPassword"),
                                    adminRent.getString("userPhone"),adminRent.getString("userRole"),adminRent.getString("userPerm"),
                                    adminRent.getString("userImage"),adminRent.getBoolean("checked"));
                        }
                        msg.obj = jsonObject.get("projectDetail");
                        msg.what = GET_PROJECT_INFO;
                    } else{
                        msg.what = GET_PROJECT_INFO_FAIL;
                    }
                    handler.sendMessage(msg);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        },token,mProjectId);
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
    /*
     * 活动返回
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        switch (requestCode){
            case COMMIT_ACTIVITY_RESULT:
                if(resultCode == RESULT_OK){
                    String commitResult = data.getStringExtra("commit_result");
                    Message msg = new Message();
                    msg.what = GET_COMMIT_ACTIVITY;
                    msg.obj = commitResult;
                    handler.sendMessage(msg);
                }
                break;
        }
    }


}
