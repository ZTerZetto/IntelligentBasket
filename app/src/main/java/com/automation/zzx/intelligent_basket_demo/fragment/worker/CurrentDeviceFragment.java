package com.automation.zzx.intelligent_basket_demo.fragment.worker;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.automation.zzx.intelligent_basket_demo.R;
import com.automation.zzx.intelligent_basket_demo.activity.loginRegist.LoginActivity;
import com.automation.zzx.intelligent_basket_demo.activity.worker.BlueToothControlActivity;
import com.automation.zzx.intelligent_basket_demo.entity.AppConfig;
import com.automation.zzx.intelligent_basket_demo.entity.UserInfo;
import com.automation.zzx.intelligent_basket_demo.utils.ToastUtil;
import com.automation.zzx.intelligent_basket_demo.utils.okhttp.BaseCallBack;
import com.automation.zzx.intelligent_basket_demo.utils.okhttp.BaseOkHttpClient;
import com.automation.zzx.intelligent_basket_demo.widget.dialog.CommonDialog;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;

import okhttp3.Call;


public class CurrentDeviceFragment extends Fragment {

    private final static String TAG = "CurrentDeviceFragment";

    //Handle消息
    public final static int GET_WORK_INFO = 1;  // 获取最新施工信息
    public final static int SHOW_WORK_INFO = 2;  // 视图更新显示
    public final static int STOP_WORK = 3; // 进行下工操作
    //private final static int GET_PROJECT_LIST_INFO = 2; // 进行下工操作

    //控件
    private LinearLayout llOrdinary;
    private TextView tvWorkerName;
    private TextView tvBasketId;
    private RelativeLayout rlCount;
    private TextView tvCount;
    //public Chronometer chronometer;
    private TextView tvStartTime;
    private Button btnEnd;
    // 空空如也
    private RelativeLayout noRepairListRelativeLayout;
    private TextView noRepairListTextView;

    //主体信息
    private UserInfo workerInfo;
    private String mToken;
    private String deviceId;
    private Timestamp startTime;
    private String strStartTime;
    private String countTimeHour;
    private String countTimeMinute;
    //private SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private BlueToothControlActivity blueToothControlActivity;
    private CommonDialog mCommonDialog;

    @SuppressLint("HandlerLeak")
    public Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            switch(msg.what) {
                case GET_WORK_INFO:
                    getWorkerWorkInfo();
                    break;
                case SHOW_WORK_INFO:
                    getTimeDiff();
                    reUpdateContentView(); // 更新施工信息及控件显示
                    break;
                case STOP_WORK:
                    noDeviceContentView(); // 更新施工信息及控件显示
                    break;
            }
        }
    };


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.fragment_current_device, container, false);

        //上机状态，展示信息
        llOrdinary = view.findViewById(R.id.ll_ordinary);
        tvWorkerName = view.findViewById(R.id.tv_worker_name);
        tvBasketId = view.findViewById(R.id.tv_basket_id);
        tvCount = view.findViewById(R.id.tv_counting_time);
        tvStartTime = view.findViewById(R.id.tv_start_time);

        //刷新上机时间
        rlCount = view.findViewById(R.id.rl_count_time);
        rlCount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handler.sendEmptyMessage(SHOW_WORK_INFO);
            }
        });

        //结束上机
        btnEnd = view.findViewById(R.id.btn_end_work);
        btnEnd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mCommonDialog == null) {
                    String mMsg = "您正在操作设备编号为"+ deviceId + "的吊篮,是否需要下机？";
                    mCommonDialog = initDialog(mMsg);
                }
                mCommonDialog.show();
            }
        });

        //未上机状态，页面展示
        noRepairListRelativeLayout = (RelativeLayout) view.findViewById(R.id.basket_no_avaliable);
        noRepairListTextView = (TextView) view.findViewById(R.id.no_basket_hint);

        getWorkerWorkInfo();
        return view;

    }


    /*
     * 后台通信相关
     */

    // 获取实时施工信息
    public void getWorkerWorkInfo(){
        BaseOkHttpClient.newBuilder()
                .addHeader("Authorization", mToken)
                .addParam("userId", workerInfo.getUserId())
                .get()
                .url(AppConfig.GET_WORKER_INFO)
                .build()
                .enqueue(new BaseCallBack() {
                    @Override
                    public void onSuccess(Object o) {
                        Log.d(TAG, "成功获取当前施工信息");
                        JSONObject jsonObject = JSON.parseObject(o.toString());
                        boolean isAllowed = jsonObject.getBoolean("isLogin");
                        if (isAllowed){
                            if(jsonObject.get("workInfo") == null){
                                handler.sendEmptyMessage(STOP_WORK);
                            } else {
                                JSONObject workInfo = jsonObject.getJSONObject("workInfo");
                                deviceId = workInfo.getString("deviceId");
                                strStartTime = workInfo.getString("timeStart").substring(0,10)+" "
                                        +workInfo.getString("timeStart").substring(11,19);
                                startTime =  Timestamp.valueOf(strStartTime);
                                handler.sendEmptyMessage(SHOW_WORK_INFO);
                            }
                        }
                    }

                    @Override
                    public void onError(int code) {
                        Log.d(TAG, "获取施工信息错误，错误编码："+code);
                        switch (code) {
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
                        Log.d(TAG, "获取施工信息失败");
                    }
                });
    }

    private void getTimeDiff(){
        long hour = 0;
        if(startTime != null) {
            long nd = 1000  * 60;
//            long nh = 1000 * 60 * 60;
            Date date = startTime;
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            cal.add(Calendar.HOUR_OF_DAY, 0);
            date = cal.getTime();
            Date now = new Date();
            long diff = now.getTime() - date.getTime();
            hour = diff / nd;
        }
        countTimeHour = String.valueOf(hour / 60 );
        countTimeMinute = String.valueOf(hour % 60);
    }


    /*
     * UI 更新类
     */
    // 主体页面显示逻辑控制
    public void reUpdateContentView(){
        if(deviceId.equals("")||deviceId.isEmpty()){  // 显示无信息
            llOrdinary.setVisibility(View.GONE);
            noRepairListRelativeLayout.setVisibility(View.VISIBLE);
            noRepairListTextView.setVisibility(View.VISIBLE);
        } else {                                          // 显示有信息
            llOrdinary.setVisibility(View.VISIBLE);
            noRepairListRelativeLayout.setVisibility(View.GONE);
            noRepairListTextView.setVisibility(View.GONE);
            tvWorkerName.setText(workerInfo.getUserName());
            tvBasketId.setText(deviceId);
            tvStartTime.setText(strStartTime);
            tvCount.setText(countTimeHour + ":"+countTimeMinute);
            blueToothControlActivity.operatingState = BlueToothControlActivity.WORKING;// 上工中
            blueToothControlActivity.curBasketId = deviceId;
        }
    }

    public void noDeviceContentView(){
        llOrdinary.setVisibility(View.GONE);
        noRepairListRelativeLayout.setVisibility(View.VISIBLE);
        noRepairListTextView.setVisibility(View.VISIBLE);
    }

    /*
     * 提示弹框
     */
    private CommonDialog initDialog(String mMsg){
        return new CommonDialog(getContext(), R.style.dialog, mMsg,
                new CommonDialog.OnCloseListener() {
                    @Override
                    public void onClick(Dialog dialog, boolean confirm) {
                        if(confirm){
                            dialog.dismiss();
                            blueToothControlActivity.curBasketId = deviceId;
                            blueToothControlActivity.mHandler.sendEmptyMessage(BlueToothControlActivity.DISCONNECT_BLE_DEVICE);  // 下工
                        }else{
                            dialog.dismiss();
                        }
                    }
                }).setTitle("提示");
    }

    /*
     *  生命周期函数
     */
    protected void onAttachToContext(Context context) {
        //do something
        workerInfo = ((BlueToothControlActivity) context).pushUserInfo();
        mToken = ((BlueToothControlActivity) context).pushToken();
    }
    @TargetApi(23)
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        onAttachToContext(context);
        blueToothControlActivity = (BlueToothControlActivity) getActivity();
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
     * 蓝牙权限申请
     */


}
