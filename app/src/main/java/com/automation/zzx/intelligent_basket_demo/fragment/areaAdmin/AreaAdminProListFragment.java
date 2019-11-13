package com.automation.zzx.intelligent_basket_demo.fragment.areaAdmin;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.automation.zzx.intelligent_basket_demo.R;
import com.automation.zzx.intelligent_basket_demo.activity.areaAdmin.AreaAdminPrimaryActivity;
import com.automation.zzx.intelligent_basket_demo.activity.areaAdmin.AreaAdminProListActivity;
import com.automation.zzx.intelligent_basket_demo.activity.loginRegist.LoginActivity;
import com.automation.zzx.intelligent_basket_demo.activity.rentAdmin.RentAdminPrimaryActivity;
import com.automation.zzx.intelligent_basket_demo.adapter.ProjectAdapter;
import com.automation.zzx.intelligent_basket_demo.entity.ProjectInfo;
import com.automation.zzx.intelligent_basket_demo.entity.UserInfo;
import com.automation.zzx.intelligent_basket_demo.utils.ToastUtil;
import com.automation.zzx.intelligent_basket_demo.utils.okhttp.BaseCallBack;
import com.automation.zzx.intelligent_basket_demo.utils.okhttp.BaseOkHttpClient;
import com.automation.zzx.intelligent_basket_demo.widget.dialog.CommonDialog;
import com.scwang.smartrefresh.header.BezierCircleHeader;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import okhttp3.Call;

import static com.automation.zzx.intelligent_basket_demo.entity.AppConfig.AREA_ADMIN_GET_ALL_PROJECT_INFO;

public class AreaAdminProListFragment extends Fragment implements View.OnClickListener {
    private final static String TAG = "AreaAdminProListFragment";
    // 消息参数
    private final static int OPERATING = 1;
    private final static int INSTALLING = 2;
    private final static int ENDING = 3;

    // Handler消息
    private final static int MG_PROJECT_LIST_INFO = 1;  // 项目列表视图更新显示
    private final static int GET_PROJECT_LIST_INFO = 2; // 从后台获取吊篮列表数据

    // 主体
    private SmartRefreshLayout mSmartRefreshLayout; // 下拉刷新
    private RecyclerView projectRv; // 项目列表
    private List<ProjectInfo> mgProjectInfoList ;
    private ProjectAdapter mgProjectListAdapter;
    private RelativeLayout noProjectListRelativeLayout; // 空空如也
    private TextView noProjectListTextView;

    //项目
    private String projectType;

    // 本地存储
    public SharedPreferences pref;
    private UserInfo userInfo; // 个人信息
    private String token; //
    private String projectId;
    private boolean getProjectId = false;

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            switch(msg.what) {
                case GET_PROJECT_LIST_INFO:
                    //处理收到的项目列表信息
                    mgProjectInfoList.clear();
                    parseProjectListInfo((String)msg.obj);  // 解析数据
                    handler.sendEmptyMessage(MG_PROJECT_LIST_INFO);
                    break;
                case MG_PROJECT_LIST_INFO:
                    updateProjectContentView(); // 更新项目信息及控件显示
                    break;

            }
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_area_mg_project_list, container, false);

        // 下拉刷新
        mSmartRefreshLayout = (SmartRefreshLayout) view.findViewById(R.id.smart_refresh_layout);
        mSmartRefreshLayout.setRefreshHeader(  //设置 Header 为 贝塞尔雷达 样式
                new BezierCircleHeader(getActivity()));
        mSmartRefreshLayout.setPrimaryColorsId(R.color.smart_loading_background_color);
        mSmartRefreshLayout.setOnRefreshListener(new OnRefreshListener() { // 添加下拉刷新监听
            @Override
            public void onRefresh(RefreshLayout refreshlayout) {
                handler.sendEmptyMessage(GET_PROJECT_LIST_INFO);
            }
        });

        // 初始化吊篮列表
        projectRv= (RecyclerView) view.findViewById(R.id.project_recycler_view);
        mgProjectInfoList = new ArrayList<>();
        rentAdminGetProjectListInfo();

        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        projectRv.setLayoutManager(layoutManager);
        mgProjectListAdapter = new ProjectAdapter(getContext(), mgProjectInfoList);
        projectRv.setAdapter(mgProjectListAdapter);
        mgProjectListAdapter.setOnItemClickListener(new ProjectAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                // item 点击响应
                Intent intent = new Intent(getActivity(), AreaAdminPrimaryActivity.class);
                intent.putExtra("project_info",mgProjectInfoList.get(position));
                startActivity(intent);
            }
        });


        // 空空如也
        noProjectListRelativeLayout = (RelativeLayout) view.findViewById(R.id.project_no_avaliable);
        noProjectListTextView = (TextView) view.findViewById(R.id.no_project_hint);


        // 消息监听

        return view;
    }

    /*
     * 控件点击响应
     */
    @Override
    public void onClick(View v) {

    }
    /*
     * UI 更新类
     */
    // 主体页面显示逻辑控制
    public void updateProjectContentView(){
        if(mgProjectInfoList.size() == 0){  // 显示无项目操作
            projectRv.setVisibility(View.GONE);
            noProjectListRelativeLayout.setVisibility(View.VISIBLE);
            noProjectListTextView.setText("您还没有相关的项目");

        }else{                               // 显示项目列表
            projectRv.setVisibility(View.VISIBLE);
            mgProjectListAdapter.notifyDataSetChanged();
            noProjectListRelativeLayout.setVisibility(View.GONE);
            noProjectListTextView.setVisibility(View.GONE);
        }
    }

    /*
     * 网络请求相关
     */
    // 从后台获取吊篮列表数据
    public void rentAdminGetProjectListInfo(){
        BaseOkHttpClient.newBuilder()
                .addHeader("Authorization", token)
                .addParam("userId", userInfo.getUserId())
                .get()
                .url(AREA_ADMIN_GET_ALL_PROJECT_INFO)
                .build()
                .enqueue(new BaseCallBack() {
                    @Override
                    public void onSuccess(Object o) {
                        String responseData = o.toString();
                        Message message = new Message();
                        message.what = GET_PROJECT_LIST_INFO;
                        message.obj = responseData;
                        handler.sendMessage(message);

                        mSmartRefreshLayout.finishRefresh(500); // 刷新成功
                    }

                    @Override
                    public void onError(int code) {
                        switch (code){
                            case 401: // 未授权
                                ToastUtil.showToastTips(getActivity(), "登录已过期，请重新登陆");
                                startActivity(new Intent(getActivity(), LoginActivity.class));
                                getActivity().finish();
                                break;
                            case 403: // 禁止
                                break;
                            case 404: // 404
                                break;
                        }

                        mSmartRefreshLayout.finishRefresh(500, false); // 刷新失败
                    }

                    @Override
                    public void onFailure(Call call, IOException e) {
                        mSmartRefreshLayout.finishRefresh(500, false); // 刷新失败
                    }
                });
    }
    // 解析项目列表数据
    private void parseProjectListInfo(String responseData){
        JSONObject jsonObject = JSON.parseObject(responseData);
        JSONObject jsonObjectList = jsonObject.getJSONObject("projectList");
        String projectListStr = jsonObjectList.getString(projectType);
       /* String projectListStr = jsonObjectList.getString("operatingProjectList");*/
        JSONArray projectList= JSON.parseArray(projectListStr);
        Iterator<Object> iterator = projectList.iterator();  // 迭代获取项目信息
        while(iterator.hasNext()) {
            JSONObject projectInfoJsonObject = (JSONObject) iterator.next();
            mgProjectInfoList.add(projectInfoJsonObject.toJavaObject(ProjectInfo.class));
        }
    }


    /*
     *  生命周期函数
     */
    /*
     * 登录相关
     */
    protected void onAttachToContext(Context context) {
        //do something
        userInfo = ((AreaAdminProListActivity) context).pushUserInfo();
        token = ((AreaAdminProListActivity) context).pushToken();
        projectType = ((AreaAdminProListActivity) context).pushType();
    }
    @TargetApi(23)
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        onAttachToContext(context);
    }
    @SuppressWarnings("deprecation")
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            onAttachToContext(activity);
        }
    }

    /*
     * 权限申请
     */


    /*
     * 提示弹框
     */
    private CommonDialog DialogToast(String mTitle, String mMsg){
        return new CommonDialog(getActivity(), R.style.dialog, mMsg,
                new CommonDialog.OnCloseListener() {
                    @Override
                    public void onClick(Dialog dialog, boolean confirm) {
                        if(confirm){
                            dialog.dismiss();
                        }else{
                            dialog.dismiss();
                        }
                    }
                }).setTitle(mTitle);
    }

}
