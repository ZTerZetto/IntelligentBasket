package com.automation.zzx.intelligent_basket_demo.fragment.rentAdmin;

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
import com.alibaba.fastjson.JSONObject;
import com.automation.zzx.intelligent_basket_demo.R;
import com.automation.zzx.intelligent_basket_demo.activity.basket.BasketDetailActivity;
import com.automation.zzx.intelligent_basket_demo.activity.common.UploadImageFTPActivity;
import com.automation.zzx.intelligent_basket_demo.activity.loginRegist.LoginActivity;
import com.automation.zzx.intelligent_basket_demo.activity.rentAdmin.RentAdminPrimaryActivity;
import com.automation.zzx.intelligent_basket_demo.adapter.rentAdmin.MgBasketListAdapter;
import com.automation.zzx.intelligent_basket_demo.entity.AppConfig;
import com.automation.zzx.intelligent_basket_demo.entity.MgBasketInfo;
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

import static com.automation.zzx.intelligent_basket_demo.entity.AppConfig.RENT_ADMIN_APPLY_PRE_STOP_BASKETS;
import static com.automation.zzx.intelligent_basket_demo.entity.AppConfig.RENT_ADMIN_MG_ALL_BASKET_INFO;

/**
 * Created by pengchenghu on 2019/3/22.
 * Author Email: 15651851181@163.com
 * Describe: 租方管理员列表管理吊篮
 * Extra: 本页HTTP请求使用BaseOkHttpClient
 * Update: 租方管理员由yu报停->
 */
public class MgBasketListFragment extends Fragment implements View.OnClickListener {

    private final static String TAG = "MgBasketListFragment";
    // intent 消息参数
    public final static String PROJECT_ID = "projectId";  // 上传图片的项目Id
    public final static String UPLOAD_IMAGE_TYPE  = "uploadImageType";
    public final static String DEVICES_LIST = "devicesList";  // 吊篮列表
    public final static String UPLOAD_BASKETS_APPLY_STOP_IMAGE = "basketsApplyStop"; // 预验收

    // Handler消息
    private final static int MG_BASKET_LIST_INFO = 1;  // 吊篮消息列表视图更新显示
    private final static int GET_BASKET_LIST_INFO = 2; // 从后台获取吊篮列表数据

    // 主体
    private SmartRefreshLayout mSmartRefreshLayout; // 下拉刷新
    private RecyclerView basketRv; // 吊篮列表
    private List<MgBasketInfo> mgBasketInfoList;
    private MgBasketListAdapter mgBasketListAdapter;
    private RelativeLayout noBasketListRelativeLayout; // 空空如也
    private TextView noBasketListTextView;

    // 底部合计
    private CheckBox basketAllSelected;  // 全选复选框
    private TextView basketNumber;  // 已选择吊篮个数
    private TextView basketApplyStop; // 吊篮预报停

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
                case MG_BASKET_LIST_INFO:  // 吊篮列表页面更新
                    mgBasketInfoList.clear();
                    mgBasketInfoList.addAll(parseBasketListInfo((String) msg.obj));
                    mgBasketListAdapter.notifyDataSetChanged();
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
        View view = inflater.inflate(R.layout.fragment_rent_mg_basket_list, container, false);

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
        mgBasketInfoList = new ArrayList<>();
        rentAdminGetBasketListInfo();
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        basketRv.setLayoutManager(layoutManager);
        mgBasketListAdapter = new MgBasketListAdapter(getContext(), mgBasketInfoList);
        basketRv.setAdapter(mgBasketListAdapter);
        mgBasketListAdapter.setOnItemClickListener(new MgBasketListAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                // item 点击响应
                Log.i(TAG, "You have clicked the "+ position +" item");
                Intent intent = new Intent(getActivity(), BasketDetailActivity.class);
                intent.putExtra("project_id",projectId);
                intent.putExtra("basket_id", mgBasketInfoList.get(position).getId());
                intent.putExtra("principal_name", mgBasketInfoList.get(position).getPrincipal());
                startActivity(intent);
            }

            @Override
            public void onCheckChanged(View view, int position, boolean checked) {
                // checkbox 状态更换
                Log.i(TAG, "You have changed the "+ position +" item checkbox");
                int basketNumberSelected = mgBasketListAdapter.checkedBasket();
                basketNumber.setText(String.valueOf(basketNumberSelected));
                basketAllSelected.setChecked(basketNumberSelected == mgBasketInfoList.size());
            }
        });
        // 空空如也
        noBasketListRelativeLayout = (RelativeLayout) view.findViewById(R.id.basket_no_avaliable);
        noBasketListTextView = (TextView) view.findViewById(R.id.no_basket_hint);

        // 底部合计
        // 控件初始化
        basketAllSelected = (CheckBox) view.findViewById(R.id.basket_all_checkbox);
        basketAllSelected.setChecked(false);
        basketNumber = (TextView) view.findViewById(R.id.basket_number);
        basketApplyStop = (TextView) view.findViewById(R.id.basket_apply_stop);
        // 消息监听
        // 全选按钮
        basketAllSelected.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Map<Integer,Boolean> isCheck = mgBasketListAdapter.getMap();
                if(!isChecked){  // 规避减一个checkbox导致取消全选的问题
                    if(isCheck.size() != mgBasketListAdapter.checkedBasket())
                        return;
                }
                mgBasketListAdapter.initCheck(isChecked);
                mgBasketListAdapter.notifyDataSetChanged();
            }
        });
        // 报停按钮点击
        basketApplyStop.setOnClickListener(this);

        return view;
    }

    /*
     * 控件点击响应
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.basket_apply_stop:
                Log.i(TAG, "You have clicked the apply_stop button");
                if(Integer.parseInt(basketNumber.getText().toString()) == 0) {  // 尚未选择吊篮
                    ToastUtil.showToastTips(getActivity(), "您尚未选择任何吊篮");
                }else if(!checkBasketsState()){ // 选择的吊篮状态问题
                    ToastUtil.showToastTips(getActivity(), "请勿提交尚未审核通过的吊篮或已申请报停的吊篮！");
                }else{
                    String content = "您申请预报停的吊篮编号为" + getApplyStopBasketList();
                    // 弹窗二次确认
                    new CommonDialog(getActivity(), R.style.dialog, content,
                            new CommonDialog.OnCloseListener() {
                                @Override
                                public void onClick(Dialog dialog, boolean confirm) {
                                    if(confirm){
                                        //rentAdminApplyPreStopBasket();  // 预报停申请
                                        //rentAdminApplyStopBasket();   // 报停申请
                                        Log.i(TAG, "You have clicked the pre stop info button");
                                        Intent intent = new Intent(getActivity(), UploadImageFTPActivity.class);
                                        intent.putExtra(PROJECT_ID, projectId);
                                        intent.putExtra(DEVICES_LIST, getApplyStopBasketList());
                                        intent.putExtra(UPLOAD_IMAGE_TYPE, UPLOAD_BASKETS_APPLY_STOP_IMAGE);
                                        startActivity(intent);
                                        dialog.dismiss();
                                    }else{
                                        dialog.dismiss();
                                    }
                                }
                            }).setTitle("提示").show();
                }
                break;
        }
    }

    /*
     * 网络请求相关
     */
    // 从后台获取吊篮列表数据
    public void rentAdminGetBasketListInfo(){
        BaseOkHttpClient.newBuilder()
                .addHeader("Authorization", token)
                .addParam("userId", userInfo.getUserId())
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
    private List<MgBasketInfo> parseBasketListInfo(String responseDate){
        List<MgBasketInfo> mgBasketInfos = new ArrayList<>();

        JSONObject jsonObject = JSON.parseObject(responseDate);

        if(projectId == null || projectId.equals("")) { // 解析获取项目Id
            ((RentAdminPrimaryActivity) getActivity()).getProjectId(
                    jsonObject.getString("projectId"));
            projectId = ((RentAdminPrimaryActivity) getActivity()).pushProjectId();
        }

        Iterator<String> iterator = jsonObject.keySet().iterator();  // 迭代获取吊篮信息
        while(iterator.hasNext()){
            String key = iterator.next();
            if(!key.contains("Box")) continue;
            String value = jsonObject.getString(key);
            JSONObject basketObj = JSON.parseObject(value);
            MgBasketInfo mgBasketInfo = new MgBasketInfo(null, basketObj.getString("deviceId"),
                    String.valueOf(basketObj.getIntValue("workingState")), basketObj.getString("date"),
                    basketObj.getString("projectId"), basketObj.getString("storageState"));
            mgBasketInfos.add(mgBasketInfo);
        }
        return mgBasketInfos;
    }

    // 预报停申请
    private void rentAdminApplyPreStopBasket(){
        BaseOkHttpClient.newBuilder()
                .addHeader("Authorization", token)
                .addParam("projectId", projectId)
                .addParam("storageList", getApplyPreStopBasketList())
                .post()
                .url(RENT_ADMIN_APPLY_PRE_STOP_BASKETS)
                .build()
                .enqueue(new BaseCallBack() {
                    @Override
                    public void onSuccess(Object o) {
                        Log.i(TAG, "预报停成功" );

                        JSONObject jsonObject = JSON.parseObject(o.toString());
                        if(jsonObject.getString("update").equals("申请成功")) {
                            // 申请成功
                            handler.sendEmptyMessage(GET_BASKET_LIST_INFO);
                            DialogToast("提示", "预报停申请成功，待区域管理员审核!");
                        }else{
                            // 申请失败
                            DialogToast("错误", "未知错误，预报停申请失败！");
                        }
                    }

                    @Override
                    public void onError(int code) {
                        Log.i(TAG, "预报停错误：" + code);
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
                    }

                    @Override
                    public void onFailure(Call call, IOException e) {
                        Log.i(TAG, "预报停失败：" + e.toString());
                    }
                });
    }
    // 获取预报停吊篮列表
    private String getApplyPreStopBasketList(){
        String results = "";

        Map<Integer,Boolean> isCheck = mgBasketListAdapter.getMap();
        for(int i=0; i<isCheck.size(); i++){
            if(isCheck.get(i)){
                results += mgBasketInfoList.get(i).getId() + ",";
            }
        }
        results = results.substring(0, results.length()-1);
        return results;
    }

    // 保亭吊篮状态审核
    private boolean checkBasketsState(){
        Map<Integer,Boolean> isCheck = mgBasketListAdapter.getMap();
        for(int i=0; i<isCheck.size(); i++){
            if(isCheck.get(i)){  // 吊篮选中
                if(!mgBasketInfoList.get(i).getStorageState().equals("3")){ // 吊篮状态
                    return false;
                }
            }
        }
        return true;
    }
    // 报停申请
    private void rentAdminApplyStopBasket(){
        BaseOkHttpClient.newBuilder()
                .addHeader("Authorization", token)
                .addParam("projectId", projectId)
                .addParam("deviceList", getApplyStopBasketList())
                .addParam("managerId", userInfo.getUserId())
                .post()
                .url(AppConfig.RENT_ADMIN_APPLY_STOP_BASKETS)
                .build()
                .enqueue(new BaseCallBack() {
                    @Override
                    public void onSuccess(Object o) {
                        Log.i(TAG, "报停成功" );

                        JSONObject jsonObject = JSON.parseObject(o.toString());
                        if(jsonObject.getString("update").contains("成功")) {
                            // 申请成功
                            handler.sendEmptyMessage(GET_BASKET_LIST_INFO);
                            DialogToast("提示", "报停申请成功，待公司管理员审核!");
                        }else{
                            // 申请失败
                            DialogToast("错误", "未知错误，报停申请失败！");
                        }
                    }

                    @Override
                    public void onError(int code) {
                        Log.i(TAG, "报停错误：" + code);
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
                    }

                    @Override
                    public void onFailure(Call call, IOException e) {
                        Log.i(TAG, "报停失败：" + e.toString());
                    }
                });
    }
    // 报停列表
    private String getApplyStopBasketList(){
        return getApplyPreStopBasketList();
    }

    /*
     * UI 更新相关
     */
    private void updateContentView(){
        if(projectId == null || projectId.equals("")) {
            basketRv.setVisibility(View.GONE);
            noBasketListRelativeLayout.setVisibility(View.VISIBLE);
            noBasketListTextView.setText("您还没有相关的项目");
        }else {
            if (mgBasketInfoList.size() < 1) { // 暂无吊篮
                basketRv.setVisibility(View.GONE);
                noBasketListRelativeLayout.setVisibility(View.VISIBLE);
                noBasketListTextView.setText("您还没有相关的吊篮");
            } else {  // 好多吊篮
                noBasketListRelativeLayout.setVisibility(View.GONE);
                basketRv.setVisibility(View.VISIBLE);
            }
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
        userInfo = ((RentAdminPrimaryActivity) context).pushUserInfo();
        token = ((RentAdminPrimaryActivity) context).pushToken();
        projectId = ((RentAdminPrimaryActivity) context).pushProjectId();
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
    /*
     * 初始化列表
     */
    private void initBaksetList(){
        mgBasketInfoList = new ArrayList<>();

        mgBasketInfoList.add(new MgBasketInfo());
        mgBasketInfoList.add(new MgBasketInfo());
        mgBasketInfoList.add(new MgBasketInfo());
        mgBasketInfoList.add(new MgBasketInfo());
        mgBasketInfoList.add(new MgBasketInfo());
        mgBasketInfoList.add(new MgBasketInfo());
    }

}
