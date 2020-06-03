package com.automation.zzx.intelligent_basket_demo.fragment.proAdmin;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.automation.zzx.intelligent_basket_demo.R;
import com.automation.zzx.intelligent_basket_demo.activity.areaAdmin.AreaAdminPrimaryActivity;
import com.automation.zzx.intelligent_basket_demo.activity.areaAdmin.AreaAdminProListActivity;
import com.automation.zzx.intelligent_basket_demo.activity.proAdmin.ProAdminPrimaryActivity;
import com.automation.zzx.intelligent_basket_demo.entity.ProjectInfo;
import com.automation.zzx.intelligent_basket_demo.entity.UserInfo;
import com.automation.zzx.intelligent_basket_demo.utils.okhttp.BaseCallBack;
import com.automation.zzx.intelligent_basket_demo.utils.okhttp.BaseOkHttpClient;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;

import static com.automation.zzx.intelligent_basket_demo.entity.AppConfig.PRO_ADMIN_GET_PROINFO;


public class ProAdminFirstFragment extends Fragment implements View.OnClickListener {

    private final static String TAG = "ProAdminFirstFragment";

    private final static String OPERATING = "operatingProjectList";

    private final static int UPDATE_PRO_ADMIN_PROJECT_MSG = 103; // 更新项目管理员的项目详情


    // 控件
    // 顶部导航栏
    private Toolbar mProjectMoreTb;
    private TextView mPorjectTitle;

    //按钮
    private RelativeLayout rlPro1;

    // 个人信息相关
    private UserInfo mUserInfo;
    private String mToken;
    private SharedPreferences mPref;
    private SharedPreferences.Editor editor;

    private List<ProjectInfo> mgProjectInfoList ;


    /*
     * 消息函数
     */
    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch(msg.what) {
                case UPDATE_PRO_ADMIN_PROJECT_MSG: // 更新项目管理员的项目详情
                    parseProjectInfo((String)msg.obj);  // 解析数据
                    break;

                default:
                    break;
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        mgProjectInfoList = new ArrayList<>();

        getUserInfo();
        proAdminGetAllProject();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_pro_admin_project_first,
                container, false);
        // 顶部toolbar
        mProjectMoreTb = (Toolbar) view.findViewById(R.id.project_more_toolbar);
        mProjectMoreTb.setTitle("");
        mPorjectTitle = view.findViewById(R.id.project_title);
        mPorjectTitle.setText("项   目");
        ((AppCompatActivity) getActivity()).setSupportActionBar(mProjectMoreTb);

        // 控件
        rlPro1 = view.findViewById(R.id.rl_project_1);
        rlPro1.setOnClickListener(this);

        return view;
    }

    @Override
    public void onClick(View v) {
        Intent intent;
        switch (v.getId()){
            case R.id.rl_project_1:
                if(mgProjectInfoList.size() == 0){
                    Toast.makeText(getActivity(),"该项目管理员暂无项目",Toast.LENGTH_SHORT).show();
                }else{
                    // item 点击响应
                    intent = new Intent(getActivity(), ProAdminPrimaryActivity.class);
                    intent.putExtra("project_info",mgProjectInfoList.get(0));
                    startActivity(intent);
                    break;
                }

        }
    }

    // 获取用户数据
    private void getUserInfo(){
        // 从本地获取数据
        mPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        mUserInfo = new UserInfo();
        mUserInfo.setUserId(mPref.getString("userId", ""));
        mUserInfo.setUserPhone(mPref.getString("userPhone", ""));
        mUserInfo.setUserRole(mPref.getString("userRole", ""));
        mToken = mPref.getString("loginToken","");
    }

    /*
     * 网络相关
     */
    // 获取区域管理员的项目列表
    private void proAdminGetAllProject(){
        BaseOkHttpClient.newBuilder()
                .addHeader("Authorization", mToken)
                .addParam("userId", mUserInfo.getUserId())
                .get()
                .url(PRO_ADMIN_GET_PROINFO)
                .build()
                .enqueue(new BaseCallBack() {
                    @Override
                    public void onSuccess(Object o) {
                        Log.d(TAG, "成功获取项目管理员的项目信息");
                        Message message = new Message();
                        message.what = UPDATE_PRO_ADMIN_PROJECT_MSG;  // 获取项目信息
                        message.obj = o.toString();
                        mHandler.sendMessage(message);
                    }

                    @Override
                    public void onError(int code) {
                        Log.d(TAG, "获取所有项目信息错误，错误编码："+code);

                    }

                    @Override
                    public void onFailure(Call call, IOException e) {
                        Log.d(TAG, "获取所有项目信息失败");
                    }
                });
    }

    // 解析项目列表
    private void parseProjectInfo(String responseData){
        mgProjectInfoList.clear();
        JSONObject jsonObject = JSON.parseObject(responseData);
        String projectStr = jsonObject.getString("project");
        if(!projectStr.equals("false") && !projectStr.equals("") ){
            ProjectInfo projectInfo = JSON.parseObject(projectStr, ProjectInfo.class);
            mgProjectInfoList.add(projectInfo);
        }
    }

}