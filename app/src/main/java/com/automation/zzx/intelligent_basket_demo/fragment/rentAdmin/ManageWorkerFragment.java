package com.automation.zzx.intelligent_basket_demo.fragment.rentAdmin;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
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
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.automation.zzx.intelligent_basket_demo.R;
import com.automation.zzx.intelligent_basket_demo.activity.loginRegist.LoginActivity;
import com.automation.zzx.intelligent_basket_demo.activity.rentAdmin.RentAdminPrimaryActivity;
import com.automation.zzx.intelligent_basket_demo.activity.worker.WorkerHomePageActivity;
import com.automation.zzx.intelligent_basket_demo.activity.worker.WorkerPrimaryActivity;
import com.automation.zzx.intelligent_basket_demo.adapter.rentAdmin.MgWorkerListAdapter;
import com.automation.zzx.intelligent_basket_demo.entity.MgWorkerInfo;
import com.automation.zzx.intelligent_basket_demo.entity.enums.WorkerType;
import com.automation.zzx.intelligent_basket_demo.utils.ToastUtil;
import com.automation.zzx.intelligent_basket_demo.utils.okhttp.BaseCallBack;
import com.automation.zzx.intelligent_basket_demo.utils.okhttp.BaseOkHttpClient;
import com.automation.zzx.intelligent_basket_demo.widget.dialog.CommonDialog;
import com.automation.zzx.intelligent_basket_demo.widget.zxing.activity.CaptureActivity;
import com.hjq.permissions.OnPermission;
import com.hjq.permissions.Permission;
import com.hjq.permissions.XXPermissions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import okhttp3.Call;

import static android.app.Activity.RESULT_OK;
import static com.automation.zzx.intelligent_basket_demo.entity.AppConfig.RENT_ADMIN_ADD_WORKER;
import static com.automation.zzx.intelligent_basket_demo.entity.AppConfig.RENT_ADMIN_DELETE_WORKER;
import static com.automation.zzx.intelligent_basket_demo.entity.AppConfig.RENT_ADMIN_GET_ALL_WORKER_INFO;
import static com.automation.zzx.intelligent_basket_demo.widget.zxing.activity.CaptureActivity.QR_CODE_RESULT;

/**
 * Created by pengchenghu on 2019/3/22.
 * Author Email: 15651851181@163.com
 * Describe: 租方管理员管理工人
 * Extra: 本页HTTP请求使用BaseOkHttpClient
 */
public class ManageWorkerFragment extends Fragment implements View.OnClickListener {

    private final static String TAG = "ManageWorkerFragment";

    // mHandler 消息列表
    private final static int MG_WORKER_LIST_MSG = 1; // 更新施工人员列表->视图更新
    private final static int UPDATE_WORKER_LIST_MSG = 2; // 更新施工人员列表->网络获取


    // 页面跳转
    private final static int CAPTURE_ACTIVITY_RESULT = 1;

    // 工人列表
    private RecyclerView workerRv;
    private List<MgWorkerInfo> mgWorkerInfoList;
    private MgWorkerListAdapter mgWorkerListAdapter;
    private RelativeLayout noWorkerListRelativeLayout;
    private TextView noWorkerListTextView;

    // 底部统计
    private CheckBox workerAllCheckBox;
    private TextView workerCheckedNumberTv;
    private TextView workerDeleteTv;

    //悬浮按钮
    private ImageView workerAddIv;

    private SharedPreferences pref;
    private String token;
    private String projectId;

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            switch(msg.what) {
                case MG_WORKER_LIST_MSG:
                    mgWorkerInfoList.clear();
                    mgWorkerInfoList.addAll(parseWorkerListInfo((String) msg.obj));
                    mgWorkerListAdapter.notifyDataSetChanged();
                    updateContentView();
                    break;
                case UPDATE_WORKER_LIST_MSG:
                    if(projectId == null || projectId.equals("")) {  // 无项目
                        workerRv.setVisibility(View.GONE);
                        noWorkerListRelativeLayout.setVisibility(View.VISIBLE);
                        noWorkerListTextView.setText("您还没有相关的项目");
                    }else {  // 获取工人列表
                        rentAdminGetWorkerInfo();
                    }
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        /*
         * 权限申请
         */
        if(!isHasPermission()) requestPermission();

        /*
         * 控件初始化
         */
        View view = inflater.inflate(R.layout.fragment_rent_mg_worker, container, false);
        // 列表初始化
        workerRv = (RecyclerView) view.findViewById(R.id.worker_recycler_view);
        mgWorkerInfoList = new ArrayList<>();
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        workerRv.setLayoutManager(layoutManager);
        mgWorkerListAdapter = new MgWorkerListAdapter(getContext(), mgWorkerInfoList, true);
        workerRv.setAdapter(mgWorkerListAdapter);
        mgWorkerListAdapter.setOnItemClickListener(new MgWorkerListAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                // 点击Item响应
                Log.i(TAG, "You have clicked the "+ position +" item");
                Intent intent = new Intent(getActivity(), WorkerHomePageActivity.class);
                intent.putExtra("worker_id", mgWorkerInfoList.get(position).getId());
                startActivity(intent);
            }

            @Override
            public void onCheckChanged(View view, int position, boolean checked) {
                // 点击复选框响应
                Log.i(TAG, "You have changed the "+ position +" item checkbox");
                int basketNumberSelected = mgWorkerListAdapter.checkedBasket();
                workerCheckedNumberTv.setText(String.valueOf(basketNumberSelected));
                workerAllCheckBox.setChecked(basketNumberSelected == mgWorkerInfoList.size());
            }

            @Override
            public void onPhoneCallClick(View view, int position) {
                Log.i(TAG, "You have clicked warning button");
                // 检查权限
                if(!isHasPermission()) requestPermission();
                // 点击拨号响应
                Intent intent;
                intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:"+ mgWorkerInfoList.get(position).getPhone()));
                startActivity(intent);
            }
        });
        noWorkerListRelativeLayout = (RelativeLayout)  view.findViewById(R.id.worker_no_avaliable);
        noWorkerListTextView = (TextView) view.findViewById(R.id.no_worker_hint);

        // 底部合计
        // 控件初始化
        workerAllCheckBox = (CheckBox) view.findViewById(R.id.worker_all_checkbox);
        workerAllCheckBox.setChecked(false);
        workerCheckedNumberTv = (TextView) view.findViewById(R.id.worker_number);
        workerDeleteTv = (TextView) view.findViewById(R.id.worker_apply_delete);
        // 消息监听
        workerAllCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Map<Integer,Boolean> isCheck = mgWorkerListAdapter.getMap();
                if(!isChecked){  // 规避减一个checkbox导致取消全选的问题
                    if(isCheck.size() != mgWorkerListAdapter.checkedBasket())
                        return;
                }
                mgWorkerListAdapter.initCheck(isChecked);
                mgWorkerListAdapter.notifyDataSetChanged();
            }
        });
        workerDeleteTv.setOnClickListener(this);  // 删除工人

        // 悬浮窗
        workerAddIv = (ImageView) view.findViewById(R.id.worker_add_image_view);
        workerAddIv.setOnClickListener(this);  // 添加工人

        handler.sendEmptyMessage(UPDATE_WORKER_LIST_MSG);  // 获取工人列表信息

        return view;
    }

    /*
     * 控件点击消息响应
     */
    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.worker_apply_delete:
                Log.i(TAG, "You have clicked the apply_stop button");
                if(Integer.parseInt(workerCheckedNumberTv.getText().toString()) == 0) {
                    ToastUtil.showToastTips(getActivity(), "您尚未选择任何施工人员!");
                    break;
                }else {
                    String content = "您想删除的工人为" + getDeleteWorkerList();
                    // 弹窗二次确认
                    new CommonDialog(getActivity(), R.style.dialog, content,
                            new CommonDialog.OnCloseListener() {
                                @Override
                                public void onClick(Dialog dialog, boolean confirm) {
                                    if(confirm){
                                        deleteWorker(getDeleteWorkerList());
                                        dialog.dismiss();
                                    }else{
                                        dialog.dismiss();
                                    }
                                }
                            }).setTitle("提示").show();
                }
                break;
            case R.id.worker_add_image_view:
                Log.i(TAG, "You have clicked the add_worker button");
                if(projectId==null || projectId.equals("")){
                    DialogToast("提示", "您尚未参与任何项目");
                    break;
                }
                startActivityForResult(new Intent(getActivity(), CaptureActivity.class), CAPTURE_ACTIVITY_RESULT);
                break;

        }
    }

    /*
    * 处理Activity返回结果
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
            case CAPTURE_ACTIVITY_RESULT:  // 扫描工人二维码名片返回结果
                if(resultCode == RESULT_OK){
                    String userInfo = data.getStringExtra(QR_CODE_RESULT);
                    Log.i(TAG, "QR_Content: "+ userInfo);
                    int colon = userInfo.indexOf(":");
                    String workerId = userInfo.substring(colon+1);
                    if(isWorker(userInfo)){  // 是工人Id
                        if(isWorkerInProject(workerId))  // 已存在于项目中
                            DialogToast("提示", "施工人员已经在本项目中").show();
                        else  // 待添加
                            rentAdminAddWorkerIntoProject(workerId);
                    }else // 非施工人员Id
                        DialogToast("错误", "非施工人员，无法添加至项目").show();
                }
                break;
            default:
                break;
        }
    }

    /*
     * 网络相关
     */
    // 从后台获取项目所有工作人员
    private void rentAdminGetWorkerInfo(){
        BaseOkHttpClient.newBuilder()
                .addHeader("Authorization", token)
                .addParam("projectId", projectId)
                .get()
                .url(RENT_ADMIN_GET_ALL_WORKER_INFO)
                .build()
                .enqueue(new BaseCallBack() {
                    @Override
                    public void onSuccess(Object o) {
                        Log.i(TAG, "成功：" + o.toString());
                        String responseData = o.toString();
                        Message message = new Message();
                        message.what = MG_WORKER_LIST_MSG;
                        message.obj = responseData;
                        handler.sendMessage(message);
                    }

                    @Override
                    public void onError(int code) {
                        Log.i(TAG, "错误：" + code);
                    }

                    @Override
                    public void onFailure(Call call, IOException e) {
                        Log.i(TAG, "失败：" + e.toString());

                    }
                });
    }
    // 解析工作人员消息返回
    private List<MgWorkerInfo> parseWorkerListInfo(String responseDate){
        List<MgWorkerInfo> mgWorkerInfos = new ArrayList<>();

        JSONObject jsonObject = JSON.parseObject(responseDate);
        String userListStr = jsonObject.getString("userList");
        if(userListStr==null) return mgWorkerInfos;
        JSONArray userList= JSON.parseArray(userListStr);
        Iterator<Object> iterator = userList.iterator();  // 迭代获取工人信息
        while(iterator.hasNext()) {
            JSONObject workerInfoJsonObject = (JSONObject) iterator.next();
            if(workerInfoJsonObject==null || workerInfoJsonObject.equals("")) continue;
            if(isWorker(workerInfoJsonObject.getString("userRole"))) {
                MgWorkerInfo mgWorkerInfo = new MgWorkerInfo();
                mgWorkerInfo.setId(workerInfoJsonObject.getString("userId"));
                mgWorkerInfo.setName(workerInfoJsonObject.getString("userName"));
                mgWorkerInfo.setPhone(workerInfoJsonObject.getString("userPhone"));
                mgWorkerInfos.add(mgWorkerInfo);
            }
        }
        return  mgWorkerInfos;
    }
    // 添加施工人员
    private void rentAdminAddWorkerIntoProject(String workerId){
        BaseOkHttpClient.newBuilder()
                .addHeader("Authorization", token)
                .addParam("projectId", projectId)
                .addParam("userId", workerId)
                .post()
                .url(RENT_ADMIN_ADD_WORKER)
                .build()
                .enqueue(new BaseCallBack() {
                    @Override
                    public void onSuccess(Object o) {
                        String response = o.toString();
                        JSONObject jsonObject = JSON.parseObject(response);
                        String increase = jsonObject.getString("increase");
                        if(increase.contains("成功")) {
                            Log.i(TAG, "添加施工人员成功");
                            DialogToast("提示", "您已成功添加该施工人员").show();
                            handler.sendEmptyMessage(UPDATE_WORKER_LIST_MSG);  // 更新本地列表
                        }else if(increase.contains("存在项目中")){
                            Log.i(TAG, "工人员已存在其它项目中");
                            DialogToast("提示", "施工人员已存在其它项目中").show();
                        }
                    }

                    @Override
                    public void onError(int code) {
                        Log.i(TAG, "添加施工人员错误：" + code);
                        switch(code){
                            case 401:
                                ToastUtil.showToastTips(getActivity(), "登陆已过期，请重新登录");
                                startActivity(new Intent(getActivity(), LoginActivity.class));
                                getActivity().finish();
                                break;
                            case 403:
                                break;
                        }
                    }

                    @Override
                    public void onFailure(Call call, IOException e) {

                    }
                });
    }

    //删除施工人员
    private void deleteWorker(String workerStr){
        BaseOkHttpClient.newBuilder()
                .addHeader("Authorization", token)
                .addParam("userId", workerStr)
                .post()
                .url(RENT_ADMIN_DELETE_WORKER)
                .build()
                .enqueue(new BaseCallBack() {
                    @Override
                    public void onSuccess(Object o) {
                        String response = o.toString();
                        JSONObject jsonObject = JSON.parseObject(response);
                        String increase = jsonObject.getString("delete");
                        if(increase.contains("success")) {
                            Log.i(TAG, "删除施工人员成功");
                            DialogToast("提示", "您已成功删除施工人员").show();
                            handler.sendEmptyMessage(UPDATE_WORKER_LIST_MSG);  // 更新本地列表
                        }else if(increase.contains("fail")){
                            Log.i(TAG, "删除施工人员失败");
                            DialogToast("提示", "删除施工人员失败，请稍后重试").show();
                        }
                    }

                    @Override
                    public void onError(int code) {
                        Log.i(TAG, "删除施工人员错误：" + code);
                        switch(code){
                            case 401:
                                ToastUtil.showToastTips(getActivity(), "登陆已过期，请重新登录");
                                startActivity(new Intent(getActivity(), LoginActivity.class));
                                getActivity().finish();
                                break;
                            case 403:
                                break;
                        }
                    }

                    @Override
                    public void onFailure(Call call, IOException e) {

                    }
                });
    }

    // 获取预报停吊篮列表
    private String getDeleteWorkerList(){
        String results = "";

        Map<Integer,Boolean> isCheck = mgWorkerListAdapter.getMap();
        for(int i=0; i<isCheck.size(); i++){
            if(isCheck.get(i)){
                results += mgWorkerInfoList.get(i).getId() + ",";
            }
        }
        results = results.substring(0, results.length()-1);
        return results;
    }

    /*
     * UI 更新相关
     */
    private void updateContentView(){
        if(projectId == null || projectId.equals("")) {
            workerRv.setVisibility(View.GONE);
            noWorkerListRelativeLayout.setVisibility(View.VISIBLE);
            noWorkerListTextView.setText("您还没有相关的项目");
        }else {
            if (mgWorkerInfoList.size() < 1) { // 暂无吊篮
                workerRv.setVisibility(View.GONE);
                noWorkerListRelativeLayout.setVisibility(View.VISIBLE);
                noWorkerListTextView.setText("尚未添加工人至项目");
            } else {  // 好多吊篮
                noWorkerListRelativeLayout.setVisibility(View.GONE);
                workerRv.setVisibility(View.VISIBLE);
            }
        }
    }

    /*
     * 生命周期函数
     */
    /*
     * 登录相关
     */
    protected void onAttachToContext(Context context) {
        //do something
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
        用xxpermissions申请权限
     */
    // 申请权限
    private void requestPermission() {
        XXPermissions.with(getActivity())
                .constantRequest() //可设置被拒绝后继续申请，直到用户授权或者永久拒绝
                .permission(Permission.CAMERA) //支持请求6.0悬浮窗权限8.0请求安装权限
                .permission(Permission.CALL_PHONE) //支持请求6.0悬浮窗权限8.0请求安装权限
                .request(new OnPermission() {
                    @Override
                    public void hasPermission(List<String> granted, boolean isAll) {
                        if (isAll) {
                            onResume();
                        }else {
                            Toast.makeText(getActivity(),
                                    "必须同意所有的权限才能使用本程序", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void noPermission(List<String> denied, boolean quick) {
                        if(quick) {
                            Toast.makeText(getActivity(), "被永久拒绝授权，请手动授予权限",
                                    Toast.LENGTH_SHORT).show();
                            // 如果是被永久拒绝就跳转到应用权限系统设置页面
                            XXPermissions.gotoPermissionSettings(getActivity());
                        }else {
                            Toast.makeText(getActivity(), "获取权限失败",
                                    Toast.LENGTH_SHORT).show();
                            getActivity().finish();
                        }
                    }
                });
    }

    // 是否有权限：摄像头、拨打电话
    private boolean isHasPermission() {
        if (XXPermissions.isHasPermission(getActivity(), Permission.CAMERA)
                && XXPermissions.isHasPermission(getActivity(), Permission.CALL_PHONE))
            return true;
        return false;
    }

    /*
     * 业务逻辑相关
     */
    // 判断扫描到的施工人员是否在项目中
    private boolean isWorkerInProject(String workerId){
        for(int i=0; i<mgWorkerInfoList.size(); i++){
            if(mgWorkerInfoList.get(i).getId().equals(workerId)){
                return true; // 改施工人员已经在项目中
            }
        }
        return false; // 施工人员不在项目中
    }


    /*
     * 弹窗
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
    * 判断是否为施工人员
    * */
    private boolean isWorker(String userRole){
       if( userRole.contains("worker")||userRole.contains("curtain_electricWorker")||
               userRole.contains("curtain_stoneWorker")||userRole.contains("curtain_glassPlate")||
               userRole.contains("curtain_glueWorker")||userRole.contains("coating_painter")||
               userRole.contains("coating_realStone")||userRole.contains("others_others"))
           return true;
       else return false;
    }


    /*
     * 列表初始化
     */
    private void initWorkerInfoList(){
        for(int i=0; i<8; i++){
            mgWorkerInfoList.add(new MgWorkerInfo());
        }
    }

}
