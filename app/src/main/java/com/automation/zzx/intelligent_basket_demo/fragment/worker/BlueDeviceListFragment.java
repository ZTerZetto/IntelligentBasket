package com.automation.zzx.intelligent_basket_demo.fragment.worker;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.automation.zzx.intelligent_basket_demo.R;
import com.automation.zzx.intelligent_basket_demo.activity.worker.BlueToothControlActivity;
import com.automation.zzx.intelligent_basket_demo.widget.dialog.CommonDialog;
import com.inuker.bluetooth.library.search.SearchRequest;
import com.inuker.bluetooth.library.search.SearchResult;
import com.inuker.bluetooth.library.search.response.SearchResponse;

import java.lang.reflect.Field;


public class BlueDeviceListFragment extends Fragment implements View.OnClickListener{

    // Handler消息
    private final static int UPDATE_DEVICE_LIST = 1;  // 设备列表视图更新显示


    //页面控件
    //搜索框控件
    private SearchView mSearchView;
    private AutoCompleteTextView mAutoCompleteTextView;//搜索输入框
    private ImageView mDeleteButton;//搜索框中的删除按钮
    //设备列表控件
    private TextView pairedTitle;
    private ListView pairedListView;
    private ArrayAdapter<String> mPairedDevicesArrayAdapter; //蓝牙搜索到的设备列表
    private ArrayAdapter<String> mSearchDevicesArrayAdapter; //可展示的设备列表
    private Button scanButton;
    private CommonDialog mCommonDialog;

    private RelativeLayout noDeviceListRelativeLayout; // 空空如也
    private TextView noDeviceListTextView;



    private String TAG = "BlueDeviceListFragment";

    private BlueToothControlActivity blueToothControlActivity;


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        blueToothControlActivity = (BlueToothControlActivity) getActivity();
    }


    @SuppressLint("HandlerLeak")
    public Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            switch(msg.what) {
                case UPDATE_DEVICE_LIST:
                    updateProjectContentView(); // 更新设备展示
                    break;

            }
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_blue_device_list, container, false);

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
        mSearchView.setQueryHint("输入吊篮设备编号");//设置查询提示字符串
        mSearchView.setSubmitButtonEnabled(true);//设置是否显示搜索框展开时的提交按钮
        mAutoCompleteTextView.setTextColor(Color.GRAY);
        //设置SearchView下划线透明
        setUnderLinetransparent(mSearchView);

        //蓝牙设备列表
        pairedTitle = (TextView) view.findViewById(R.id.title_paired_devices);
        pairedTitle.setText(R.string.be_scanning);
        pairedTitle.setVisibility(View.VISIBLE);

        pairedListView = (ListView) view.findViewById(R.id.paired_devices); // 1. 获取控件
        mPairedDevicesArrayAdapter = new ArrayAdapter<String>(getContext(), R.layout.device_name);
        mSearchDevicesArrayAdapter = new ArrayAdapter<String>(getContext(), R.layout.device_name); // 2. 初始化适配器
        pairedListView.setAdapter(mSearchDevicesArrayAdapter); // 3. 装载适配器
        pairedListView.setOnItemClickListener(mDeviceClickListener); // 4. 设置监听

        scanButton = (Button) view.findViewById(R.id.button_scan);
        scanButton.setOnClickListener(this);

        // 空空如也
        noDeviceListRelativeLayout = (RelativeLayout) view.findViewById(R.id.basket_no_avaliable);
        noDeviceListTextView = (TextView) view.findViewById(R.id.no_basket_hint);
        setListener();

        return view;

    }


    // The on-click listener for button.
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.button_scan:   // 按钮监听
                Log.i(TAG, "Click on Scan Button");
                mSearchDevicesArrayAdapter.clear();
                mPairedDevicesArrayAdapter.clear();
                doDiscovery();

                break;
        }
    }


    // The on-click listener for all devices in the ListViews
    private AdapterView.OnItemClickListener mDeviceClickListener = new AdapterView.OnItemClickListener() {
        public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {
            // Cancel discovery because it's costly and we're about to connect
            blueToothControlActivity.mBluetoothClient.stopSearch();

            // Get the device MAC address, which is the last 17 chars in the View
            String info = ((TextView) v).getText().toString();
            String address = info.substring(info.length() - 17); //TODO 获取蓝牙address
            String basketId = info.substring(3, info.length() - 18); // TODO 截取吊篮id

            if (blueToothControlActivity.curBasketId != null && blueToothControlActivity.curBasketId.equals(basketId)){
                //弹出确认弹窗
                String mMsg = "此吊篮为您正在操作的吊篮！";
                mCommonDialog = initDialog(mMsg);
                mCommonDialog.show();
            } else {
                if(blueToothControlActivity.operatingState == BlueToothControlActivity.WORKING){
                    //当已有连接吊篮时，弹出确认弹窗
                    String mMsg = "您正在操作设备编号为"+ blueToothControlActivity.curBasketId +
                            "的吊篮"+'\n'+ "是否确认要更换成"+basketId +"的吊篮？";
                    mCommonDialog = initDialog(basketId,address,mMsg);
                    mCommonDialog.show();
                } else {
                    //弹出确认弹窗
                    String mMsg = "是否请求连接设备编号为"+ basketId +"的吊篮？";
                    mCommonDialog = initDialog(basketId,address,mMsg);
                    mCommonDialog.show();
                }
            }
        }
    };

    // 向activity传递新蓝牙数据，并请求连接
    private void connectBle(final String newBasketId,final String address){
        blueToothControlActivity.newMacAddress = address;
        blueToothControlActivity.newBasketId = newBasketId;
        Log.i(TAG, "Bluetooth 配对 BlueToothControlActivity.macAddress");

        if(blueToothControlActivity.operatingState == BlueToothControlActivity.WORKING){
            blueToothControlActivity.operatingState = BlueToothControlActivity.OPENING_AFTER_CLOSING;
        } else {
            blueToothControlActivity.operatingState = BlueToothControlActivity.AVAILABLE_STATE;
        }
        blueToothControlActivity.mHandler.sendEmptyMessage(BlueToothControlActivity.CONNECT_NEW_BLE_DEVICE);  // 蓝牙连接
    }


    // discovery device
    private void doDiscovery(){
        Log.i(TAG, "doDiscovery");
        if (blueToothControlActivity.mBluetoothClient.isBleSupported()){
            Log.i(TAG, "支持BLE");
        }
        if(!blueToothControlActivity.mBluetoothClient.isBluetoothOpened()){
            blueToothControlActivity.mBluetoothClient.openBluetooth();
        }

//      setTitle(R.string.scanning);
        SearchRequest request = new SearchRequest.Builder()
                .searchBluetoothLeDevice(1000, 3)      // 先扫BLE设备3次，每次3s
                .searchBluetoothClassicDevice(2000) // 再扫经典蓝牙5s
                .searchBluetoothLeDevice(1000)      // 扫BLE设备1次，每次2s
                .build();

        blueToothControlActivity.mBluetoothClient.search(request, new SearchResponse() {
            @Override
            public void onSearchStarted() {
                Log.i(TAG, "Bluetooth 开始扫描");
            }

            @Override
            public void onDeviceFounded(SearchResult device) {
                // Beacon beacon = new Beacon(device.scanRecord);
                if(device.getName().contains("BAS")) {
                    if (mPairedDevicesArrayAdapter.getCount() == 0) {
                        mPairedDevicesArrayAdapter.add(device.getName() + "\n" + device.getAddress());
                        mSearchDevicesArrayAdapter.add(device.getName() + "\n" + device.getAddress());
                        //mPairedDevicesArrayAdapter.notifyDataSetChanged();
                    }
                    for (int i = 0; i < mPairedDevicesArrayAdapter.getCount(); i++) {
                        if (mPairedDevicesArrayAdapter.getItem(i).contains(device.getAddress())) {
                            break;
                        }
                        if (i == mPairedDevicesArrayAdapter.getCount() - 1) {
                            Log.i(TAG, "Find new BlueTooth " + device.getName() + " Mac: " + device.getAddress());
                            mPairedDevicesArrayAdapter.add(device.getName() + "\n" + device.getAddress());
                            mSearchDevicesArrayAdapter.add(device.getName() + "\n" + device.getAddress());
                            //mPairedDevicesArrayAdapter.notifyDataSetChanged();
                        }
                    }
                }
                //mSearchDevicesArrayAdapter = mPairedDevicesArrayAdapter;
            }

            @Override
            public void onSearchStopped() {
                Log.i(TAG, "Bluetooth 停止扫描");
                pairedTitle.setText(R.string.title_paired_devices);
                copyAdapter(mSearchDevicesArrayAdapter,mPairedDevicesArrayAdapter);
                handler.sendEmptyMessage(UPDATE_DEVICE_LIST);
            }

            @Override
            public void onSearchCanceled() {

            }
        });
    }

    /*
     * 提示弹框
     */
    private CommonDialog initDialog(final String newBasketId,final String address,String mMsg){
        return new CommonDialog(getContext(), R.style.dialog, mMsg,
                new CommonDialog.OnCloseListener() {
                    @Override
                    public void onClick(Dialog dialog, boolean confirm) {
                        if(confirm){
                            dialog.dismiss();
                            connectBle(newBasketId,address);
                        }else{
                            dialog.dismiss();
                        }
                    }
                }).setTitle("提示");
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
                        }else{
                            dialog.dismiss();
                        }
                    }
                }).setTitle("提示");
    }


    //搜索框相关
    private void setListener(){
        // 设置搜索文本监听
        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            //当点击搜索按钮时触发该方法
            @Override
            public boolean onQueryTextSubmit(String query) {
                mSearchDevicesArrayAdapter.clear();
                if (query == null  || query.equals("")) {
                    copyAdapter(mSearchDevicesArrayAdapter,mPairedDevicesArrayAdapter);
                }else{
                    for (int i = 0; i < mPairedDevicesArrayAdapter.getCount(); i++) {
                        String blueDevice= mPairedDevicesArrayAdapter.getItem(i);
                        if (blueDevice.contains(query)) {
                            mSearchDevicesArrayAdapter.add(blueDevice);
                        }
                    }
                    handler.sendEmptyMessage(UPDATE_DEVICE_LIST);
                }
                return true;
            }

            //当搜索内容改变时触发该方法
            @Override
            public boolean onQueryTextChange(String newText) {
                mSearchDevicesArrayAdapter.clear();
                if (newText == null || newText.equals("")) {
                    copyAdapter(mSearchDevicesArrayAdapter,mPairedDevicesArrayAdapter);
                }else{
                    for (int i = 0; i < mPairedDevicesArrayAdapter.getCount(); i++) {
                        String blueDevice= mPairedDevicesArrayAdapter.getItem(i);
                        if (blueDevice.contains(newText)) {
                            mSearchDevicesArrayAdapter.add(blueDevice);
                        }
                    }
                }
                handler.sendEmptyMessage(UPDATE_DEVICE_LIST);
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
     * UI 更新类
     */
    // 主体页面显示逻辑控制
    public void updateProjectContentView(){
        if(mPairedDevicesArrayAdapter.getCount() == 0 ){  // 显示无项目操作
            Log.i(TAG, "显示无项目操作");
            pairedListView.setVisibility(View.GONE);
            noDeviceListRelativeLayout.setVisibility(View.VISIBLE);
            noDeviceListTextView.setText("附近暂无可连接蓝牙设备");
        }else if(mSearchDevicesArrayAdapter.getCount() == 0 ){ // 显示未搜索
            Log.i(TAG, "显示未搜索");
            pairedListView.setVisibility(View.GONE);
            noDeviceListRelativeLayout.setVisibility(View.VISIBLE);
            noDeviceListTextView.setText("未搜索出相关设备");
        }else{                                          // 显示项目列表
            Log.i(TAG, "显示项目列表");
            pairedListView.setVisibility(View.VISIBLE);
            mSearchDevicesArrayAdapter.notifyDataSetChanged();
            noDeviceListRelativeLayout.setVisibility(View.GONE);
            noDeviceListTextView.setVisibility(View.GONE);
        }
    }


    // adapter赋值
    private void copyAdapter(ArrayAdapter<String> adapter1,ArrayAdapter<String> adapter2){
        for (int i = 0; i < adapter2.getCount(); i++) {
            String blueDevice= adapter2.getItem(i);
            adapter1.add(blueDevice);
        }
    }



    //生命周期相关
    @Override
    public void onResume() {
        Log.i(TAG, "onResume");
        super.onResume();
        doDiscovery();
    }

    @Override
    public void onPause() {
        super.onPause();
        blueToothControlActivity.mBluetoothClient.stopSearch();  // 离开页面时，关闭扫描
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

}
