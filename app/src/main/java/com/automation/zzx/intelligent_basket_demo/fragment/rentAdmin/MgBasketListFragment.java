package com.automation.zzx.intelligent_basket_demo.fragment.rentAdmin;

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
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.automation.zzx.intelligent_basket_demo.R;
import com.automation.zzx.intelligent_basket_demo.activity.basket.BasketDetailActivity;
import com.automation.zzx.intelligent_basket_demo.activity.loginRegist.LoginActivity;
import com.automation.zzx.intelligent_basket_demo.activity.rentAdmin.RentAdminPrimaryActivity;
import com.automation.zzx.intelligent_basket_demo.adapter.rentAdmin.MgBasketListAdapter;
import com.automation.zzx.intelligent_basket_demo.entity.MgBasketInfo;
import com.automation.zzx.intelligent_basket_demo.entity.UserInfo;
import com.automation.zzx.intelligent_basket_demo.utils.HttpUtil;
import com.automation.zzx.intelligent_basket_demo.utils.ToastUtil;
import com.automation.zzx.intelligent_basket_demo.utils.okhttp.BaseCallBack;
import com.automation.zzx.intelligent_basket_demo.utils.okhttp.BaseOkHttpClient;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import static com.automation.zzx.intelligent_basket_demo.entity.AppConfig.RENT_ADMIN_MG_ALL_BASKET_INFO;

/**
 * Created by pengchenghu on 2019/3/22.
 * Author Email: 15651851181@163.com
 * Describe: 租方管理员列表管理吊篮
 * Extra: 本页HTTP请求使用BaseOkHttpClient
 */
public class MgBasketListFragment extends Fragment implements View.OnClickListener {

    private final static String TAG = "MgBasketListFragment";
    private final static int MG_BASKET_LIST_INFO = 1;

    // 吊篮列表
    private RecyclerView basketRv; // 吊篮列表
    private List<MgBasketInfo> mgBasketInfoList;
    private MgBasketListAdapter mgBasketListAdapter;

    // 底部合计
    private CheckBox basketAllSelected;  // 全选复选框
    private TextView basketNumber;  // 已选择吊篮个数
    private TextView basketApplyStop; // 吊篮预报停

    // 本地存储
    private String projectId;
    public SharedPreferences pref;
    // 个人信息
    private UserInfo userInfo; // 个人信息
    private String token; //

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            switch(msg.what) {
                case MG_BASKET_LIST_INFO:  // 吊篮列表更新
                    mgBasketInfoList.clear();
                    mgBasketInfoList.addAll(parseBasketListInfo((String) msg.obj));
                    mgBasketListAdapter.notifyDataSetChanged();
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_rent_mg_basket_list, container, false);

        // 初始化吊篮列表
        basketRv = (RecyclerView) view.findViewById(R.id.basket_recycler_view);
        //initBaksetList();
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
                //intent.putExtra("basket_id", mgBasketInfoList.get(position).getId());
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
        // 预报停按钮点击
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
                if(Integer.parseInt(basketNumber.getText().toString()) == 0) {
                    ToastUtil.showToastTips(getActivity(), "您尚未选择任何吊篮");
                    break;
                }
                break;
        }
    }

    /*
     * 网络请求相关
     */
    //从后台获取吊篮列表数据
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
                    }

                    @Override
                    public void onFailure(Call call, IOException e) {
                        Log.i(TAG, "失败：" + e.toString());
                    }
                });
    }

    // 解析吊篮列表数据
    private List<MgBasketInfo> parseBasketListInfo(String responseDate){
        List<MgBasketInfo> mgBasketInfos = new ArrayList<>();

        JSONObject jsonObject = JSON.parseObject(responseDate);

        ((RentAdminPrimaryActivity) getActivity()).getProjectId(  // 解析获取项目Id
                jsonObject.getString("projectId"));

        Iterator<String> iterator = jsonObject.keySet().iterator();  // 迭代获取吊篮信息
        while(iterator.hasNext()){
            String key = iterator.next();
            if(!key.contains("Box")) continue;
            String value = jsonObject.getString(key);
            JSONObject basketObj = JSON.parseObject(value);
            MgBasketInfo mgBasketInfo = new MgBasketInfo(null, basketObj.getString("deviceId"),
                    String.valueOf(basketObj.getIntValue("workingState")), basketObj.getString("date"),
                    basketObj.getString("managerId"));
            mgBasketInfos.add(mgBasketInfo);
        }
        return mgBasketInfos;
    }

    /*
     * 生命周期函数
     */
    /*
     * 登录相关
     */
    protected void onAttachToContext(Context context) {
        //do something
        userInfo = ((RentAdminPrimaryActivity) context).pushUserInfo();
        token = ((RentAdminPrimaryActivity) context).pushToken();
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
