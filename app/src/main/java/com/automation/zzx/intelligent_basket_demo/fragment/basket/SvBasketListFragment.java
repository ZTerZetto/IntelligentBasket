package com.automation.zzx.intelligent_basket_demo.fragment.basket;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.automation.zzx.intelligent_basket_demo.activity.basket.BasketDetailActivity;
import com.automation.zzx.intelligent_basket_demo.activity.basketSupervisor.SupervisorBasketActivity;
import com.automation.zzx.intelligent_basket_demo.activity.loginRegist.LoginActivity;
import com.automation.zzx.intelligent_basket_demo.adapter.SvBasketListAdapter;
import com.automation.zzx.intelligent_basket_demo.adapter.rentAdmin.MgBasketListAdapter;
import com.automation.zzx.intelligent_basket_demo.entity.MgBasketStatement;
import com.automation.zzx.intelligent_basket_demo.utils.ToastUtil;
import com.automation.zzx.intelligent_basket_demo.utils.okhttp.BaseCallBack;
import com.automation.zzx.intelligent_basket_demo.utils.okhttp.BaseOkHttpClient;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.RelativeLayout;
import android.widget.TextView;
import com.automation.zzx.intelligent_basket_demo.R;


import com.automation.zzx.intelligent_basket_demo.entity.MgBasketInfo;
import com.automation.zzx.intelligent_basket_demo.entity.UserInfo;
import com.scwang.smartrefresh.header.BezierCircleHeader;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import okhttp3.Call;

import static com.automation.zzx.intelligent_basket_demo.entity.AppConfig.RENT_ADMIN_MG_ALL_BASKET_INFO;

public class SvBasketListFragment extends Fragment{

    private final static String TAG = "SvBasketListFragment";

    // Handler消息
    private final static int MG_BASKET_LIST_INFO = 1;  // 吊篮消息列表视图更新显示
    private final static int GET_BASKET_LIST_INFO = 2; // 从后台获取吊篮列表数据

    // 主体
    private SmartRefreshLayout mSmartRefreshLayout; // 下拉刷新
    private RecyclerView basketRv; // 吊篮列表
    private List<MgBasketStatement> mgBasketStatementList;
    private SvBasketListAdapter svBasketListAdapter;
    private RelativeLayout noBasketListRelativeLayout; // 空空如也
    private TextView noBasketListTextView;


    // 本地存储
    public SharedPreferences pref;
    private UserInfo userInfo; // 个人信息
    private String token; //
    private String projectId;


    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            switch(msg.what) {
                case MG_BASKET_LIST_INFO:  // 吊篮列表页面更新
                    mgBasketStatementList.clear();
                    mgBasketStatementList.addAll(parseBasketListInfo((String) msg.obj));
                    svBasketListAdapter.notifyDataSetChanged();
                    updateContentView();
                    break;
                case GET_BASKET_LIST_INFO: // 获取吊篮列表信息
                    if(projectId == null || projectId.equals("")) {  // 无项目
                        basketRv.setVisibility(View.GONE);
                        noBasketListRelativeLayout.setVisibility(View.VISIBLE);
                        noBasketListTextView.setText("您还没有相关的项目");
                        mSmartRefreshLayout.finishRefresh(500, false); // 刷新失败
                    }else {  // 获取吊篮列表
                        rentAdminGetBasketListInfo();
                    }
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sv_basket_list, container, false);

        // 下拉刷新
        mSmartRefreshLayout = (SmartRefreshLayout) view.findViewById(R.id.smart_refresh_layout);
        mSmartRefreshLayout.setRefreshHeader(  //设置 Header 为 贝塞尔雷达 样式
                new BezierCircleHeader(getActivity()));
        mSmartRefreshLayout.setPrimaryColorsId(R.color.smart_loading_background_color);
        mSmartRefreshLayout.setOnRefreshListener(new OnRefreshListener() { // 添加下拉刷新监听
            @Override
            public void onRefresh(RefreshLayout refreshlayout) {
                handler.sendEmptyMessage(GET_BASKET_LIST_INFO);
            }
        });
        // 初始化吊篮列表
        basketRv = (RecyclerView) view.findViewById(R.id.basket_recycler_view);
        mgBasketStatementList = new ArrayList<>();
        rentAdminGetBasketListInfo();
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        basketRv.setLayoutManager(layoutManager);

        svBasketListAdapter = new SvBasketListAdapter(getContext(), mgBasketStatementList);
        basketRv.setAdapter(svBasketListAdapter);
        svBasketListAdapter.setOnItemClickListener(new SvBasketListAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                // item 点击响应
                Log.i(TAG, "You have clicked the "+ position +" item");
                Intent intent = new Intent(getActivity(), BasketDetailActivity.class);
                intent.putExtra("project_id",projectId);
                intent.putExtra("basket_id", mgBasketStatementList.get(position).getBasketId());
                intent.putExtra("principal_name"," ");
                startActivity(intent);
            }
        });
        // 空空如也
        noBasketListRelativeLayout = (RelativeLayout) view.findViewById(R.id.basket_no_avaliable);
        noBasketListTextView = (TextView) view.findViewById(R.id.no_basket_hint);

        return view;
    }

    /*
     * 网络请求相关
     */
    // 从后台获取吊篮列表数据
    public void rentAdminGetBasketListInfo(){
        BaseOkHttpClient.newBuilder()
                .addHeader("Authorization", token)
                .addParam("projectId", userInfo.getUserPhone())//项目ID即为用户手机号
                .get()
                .url(RENT_ADMIN_MG_ALL_BASKET_INFO)
                .build()
                .enqueue(new BaseCallBack() {
                    @Override
                    public void onSuccess(Object o) {
                        Log.i(TAG, "成功" );
                        String responseData = o.toString();
                        Message message = new Message();
                        message.what = MG_BASKET_LIST_INFO;
                        message.obj = responseData;
                        handler.sendMessage(message);

                        mSmartRefreshLayout.finishRefresh(500); // 刷新成功
                    }

                    @Override
                    public void onError(int code) {
                        Log.i(TAG, "错误：" + code);
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
                        Log.i(TAG, "失败：" + e.toString());

                        mSmartRefreshLayout.finishRefresh(500, false); // 刷新失败
                    }
                });
    }

    // 解析吊篮列表数据
    private List<MgBasketStatement> parseBasketListInfo(String responseDate){

        List<MgBasketStatement> mBasketStatements = new ArrayList<>();
        JSONObject jsonObject = JSON.parseObject(responseDate);
        Iterator<String> iterator = jsonObject.keySet().iterator();  // 迭代获取吊篮信息
        while(iterator.hasNext()) {
            String key = iterator.next();
            if(!key.contains("storage")) continue;
            String value = jsonObject.getString(key);
            if(value==null || value.equals("")) continue;
            JSONObject basketObj = JSON.parseObject(value);
            String deviceId = basketObj.getString("deviceId");
            if(deviceId==null || deviceId.equals("")) continue;
            mBasketStatements.add(new MgBasketStatement(basketObj.getString("deviceId"),
                    null, basketObj.getString("storageState")));
        }
        return mBasketStatements;
    }

    /*
     * UI 更新相关
     */
    private void updateContentView(){
            if (mgBasketStatementList.size() < 1) { // 暂无吊篮
                basketRv.setVisibility(View.GONE);
                noBasketListRelativeLayout.setVisibility(View.VISIBLE);
                noBasketListTextView.setText("您还没有相关的吊篮");
            } else {  // 好多吊篮
                noBasketListRelativeLayout.setVisibility(View.GONE);
                basketRv.setVisibility(View.VISIBLE);
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
        userInfo = ((SupervisorBasketActivity) context).pushUserInfo();
        token = ((SupervisorBasketActivity) context).pushToken();
        projectId = ((SupervisorBasketActivity) context).pushProjectId();
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

}
