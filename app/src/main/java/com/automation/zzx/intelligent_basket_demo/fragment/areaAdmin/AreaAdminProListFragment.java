package com.automation.zzx.intelligent_basket_demo.fragment.areaAdmin;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.automation.zzx.intelligent_basket_demo.R;
import com.automation.zzx.intelligent_basket_demo.activity.areaAdmin.ProjectOperatingActivity;
import com.automation.zzx.intelligent_basket_demo.activity.areaAdmin.AreaAdminProListActivity;
import com.automation.zzx.intelligent_basket_demo.activity.areaAdmin.ProjectFinishedActivity;
import com.automation.zzx.intelligent_basket_demo.activity.areaAdmin.ProjectInstallActivity;
import com.automation.zzx.intelligent_basket_demo.activity.loginRegist.LoginActivity;
import com.automation.zzx.intelligent_basket_demo.adapter.ProjectAdapter;
import com.automation.zzx.intelligent_basket_demo.entity.ProjectInfo;
import com.automation.zzx.intelligent_basket_demo.entity.UserInfo;
import com.automation.zzx.intelligent_basket_demo.utils.ToastUtil;
import com.automation.zzx.intelligent_basket_demo.utils.okhttp.BaseCallBack;
import com.automation.zzx.intelligent_basket_demo.utils.okhttp.BaseOkHttpClient;
import com.scwang.smartrefresh.header.BezierCircleHeader;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import okhttp3.Call;

import static com.automation.zzx.intelligent_basket_demo.entity.AppConfig.AREA_ADMIN_GET_ALL_PROJECT_INFO;

public class AreaAdminProListFragment extends Fragment implements View.OnClickListener {
    private final static String TAG = "AreaAdminProListFragment";
    // 消息参数
    private final static String OPERATING = "operatingProjectList";
    private final static String INSTALLING = "installingProjectList";
    private final static String ENDING = "endProjectList";

    // Handler消息
    private final static int MG_PROJECT_LIST_INFO = 1;  // 项目列表视图更新显示
    private final static int GET_PROJECT_LIST_INFO = 2; // 从后台获取吊篮列表数据

    // 主体
    //搜索框控件
    private SearchView mSearchView;
    private AutoCompleteTextView mAutoCompleteTextView;//搜索输入框
    private ImageView mDeleteButton;//搜索框中的删除按钮

    private SmartRefreshLayout mSmartRefreshLayout; // 下拉刷新
    private RecyclerView projectRv; // 项目列表
    private List<ProjectInfo> mgProjectInfoList ;
    private List<ProjectInfo> showProjectList;
    private ProjectAdapter showProjectAdapter;
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

        //搜索框
        mSearchView=view.findViewById(R.id.view_search);
        mAutoCompleteTextView=mSearchView.findViewById(R.id.search_src_text);
        mDeleteButton=mSearchView.findViewById(R.id.search_close_btn);
        mDeleteButton.setOnClickListener(this);
        mSearchView.setIconifiedByDefault(false);//设置搜索图标是否显示在搜索框内
        mAutoCompleteTextView.clearFocus(); //默认失去焦点
        mSearchView.setImeOptions(3);//设置输入法搜索选项字段，1:回车2:前往3:搜索4:发送5:下一項6:完成
//      mSearchView.setInputType(1);//设置输入类型
//      mSearchView.setMaxWidth(200);//设置最大宽度
        mSearchView.setQueryHint("输入项目名称或项目ID");//设置查询提示字符串
        mSearchView.setSubmitButtonEnabled(true);//设置是否显示搜索框展开时的提交按钮
        mAutoCompleteTextView.setTextColor(Color.GRAY);
        //设置SearchView下划线透明
        setUnderLinetransparent(mSearchView);

        // 下拉刷新
        mSmartRefreshLayout = (SmartRefreshLayout) view.findViewById(R.id.smart_refresh_layout);
        mSmartRefreshLayout.setRefreshHeader(  //设置 Header 为 贝塞尔雷达 样式
                new BezierCircleHeader(getActivity()));
        mSmartRefreshLayout.setPrimaryColorsId(R.color.smart_loading_background_color);
        mSmartRefreshLayout.setOnRefreshListener(new OnRefreshListener() { // 添加下拉刷新监听
            @Override
            public void onRefresh(RefreshLayout refreshlayout) {
                rentAdminGetProjectListInfo();
                mSearchView.setQuery("",false);
            }
        });

        // 初始化吊篮列表
        projectRv= (RecyclerView) view.findViewById(R.id.project_recycler_view);
        mgProjectInfoList = new ArrayList<>();
        showProjectList = new ArrayList<>();
        rentAdminGetProjectListInfo();

        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        projectRv.setLayoutManager(layoutManager);
        showProjectAdapter = new ProjectAdapter(getContext(), showProjectList);
        projectRv.setAdapter(showProjectAdapter);
        showProjectAdapter.setOnItemClickListener(new ProjectAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Intent intent;
                switch (projectType){
                    case OPERATING:
                        // item 点击响应
                         intent = new Intent(getActivity(), ProjectOperatingActivity.class);
                        intent.putExtra("project_info",showProjectList.get(position));
                        startActivity(intent);
                        break;
                    case INSTALLING:
                        // item 点击响应
                        intent = new Intent(getActivity(), ProjectInstallActivity.class);
                        intent.putExtra("project_info",showProjectList.get(position));
                        startActivity(intent);
                        break;
                    case ENDING:
                        // item 点击响应
                        intent = new Intent(getActivity(), ProjectFinishedActivity.class);
                        intent.putExtra("project_info",showProjectList.get(position));
                        startActivity(intent);
                        break;
                }
            }
        });


        // 空空如也
        noProjectListRelativeLayout = (RelativeLayout) view.findViewById(R.id.project_no_avaliable);
        noProjectListTextView = (TextView) view.findViewById(R.id.no_project_hint);

        setListener();

        return view;
    }


    private void setListener(){

        // 设置搜索文本监听
        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            //当点击搜索按钮时触发该方法
            @Override
            public boolean onQueryTextSubmit(String query) {
                showProjectList.clear();
                if (query == null  || query.equals("")) {
                    showProjectList.addAll(mgProjectInfoList);
                }else{
                    for (int i = 0; i < mgProjectInfoList.size(); i++) {
                        ProjectInfo projectInfo = mgProjectInfoList.get(i);
                        if (projectInfo.getProjectName().contains(query)) {
                            showProjectList.add(projectInfo);
                        } else if (projectInfo.getProjectId().contains(query)) {
                            showProjectList.add(projectInfo);
                        }
                    }
                    handler.sendEmptyMessage(MG_PROJECT_LIST_INFO);
                }
                return true;
            }

            //当搜索内容改变时触发该方法
            @Override
            public boolean onQueryTextChange(String newText) {
                showProjectList.clear();
                if (newText == null || newText.equals("")) {
                    showProjectList.addAll(0,mgProjectInfoList);
                }else{
                    for (int i = 0; i < mgProjectInfoList.size(); i++) {
                        ProjectInfo projectInfo = mgProjectInfoList.get(i);
                        if (projectInfo.getProjectName().contains(newText)) {
                            showProjectList.add(projectInfo);
                        } else if (projectInfo.getProjectId().contains(newText)) {
                            showProjectList.add(projectInfo);
                        }
                    }
                }
                handler.sendEmptyMessage(MG_PROJECT_LIST_INFO);
                return true;
            }
        });
    }

    /**设置SearchView下划线透明**/
    private void setUnderLinetransparent(SearchView searchView){
        try {
            Class<?> argClass = searchView.getClass();
            Field ownField = argClass.getDeclaredField("mSearchPlate");
            ownField.setAccessible(true);
            View mView = (View) ownField.get(searchView);
            mView.setBackgroundColor(Color.TRANSPARENT);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    /*
     * 控件点击响应
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.search_close_btn:
                mAutoCompleteTextView.setText("");
                break;
            default:
                break;
        }
    }

    /*
     * UI 更新类
     */
    // 主体页面显示逻辑控制
    public void updateProjectContentView(){
        if(mgProjectInfoList.size() == 0 ){  // 显示无项目操作
            projectRv.setVisibility(View.GONE);
            noProjectListRelativeLayout.setVisibility(View.VISIBLE);
            noProjectListTextView.setText("您还没有相关的项目");
        }else if(showProjectList.size() == 0 ){ // 显示未搜索
            projectRv.setVisibility(View.GONE);
            noProjectListRelativeLayout.setVisibility(View.VISIBLE);
            noProjectListTextView.setText("未搜索出相关项目");
        }else{                                          // 显示项目列表
            projectRv.setVisibility(View.VISIBLE);
            showProjectAdapter.notifyDataSetChanged();
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
        JSONArray projectList= JSON.parseArray(projectListStr);
        Iterator<Object> iterator = projectList.iterator();  // 迭代获取项目信息
        while(iterator.hasNext()) {
            JSONObject projectInfoJsonObject = (JSONObject) iterator.next();
            mgProjectInfoList.add(projectInfoJsonObject.toJavaObject(ProjectInfo.class));
        }
        showProjectList.clear();
        showProjectList.addAll(mgProjectInfoList);
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


}
