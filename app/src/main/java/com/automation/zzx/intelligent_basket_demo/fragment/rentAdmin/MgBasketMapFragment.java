package com.automation.zzx.intelligent_basket_demo.fragment.rentAdmin;

import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.clusterutil.clustering.Cluster;
import com.baidu.mapapi.clusterutil.clustering.ClusterItem;
import com.baidu.mapapi.clusterutil.clustering.ClusterManager;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.InfoWindow;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.model.LatLngBounds;
import com.automation.zzx.intelligent_basket_demo.R;
import com.automation.zzx.intelligent_basket_demo.activity.basket.BasketDetailActivity;
import com.hjq.permissions.OnPermission;
import com.hjq.permissions.Permission;
import com.hjq.permissions.XXPermissions;

import java.util.ArrayList;
import java.util.List;

import static android.content.Context.SENSOR_SERVICE;

/**
 * Created by pengchenghu on 2019/3/22.
 * Author Email: 15651851181@163.com
 * Describe: 租方管理员地图管理吊篮
 */
public class MgBasketMapFragment extends Fragment implements SensorEventListener, BaiduMap.OnMapLoadedCallback{

    final static private String TAG = "MapViewFragment";

    // 视图相关
    private View mView;

    // 定位相关
    LocationClient mLocClient;  // 位置获取管理
    public MgBasketMapFragment.MyLocationListenner myListener = new MgBasketMapFragment.MyLocationListenner();  // 位置信息监听
    private MyLocationConfiguration.LocationMode mCurrentMode;  // 获取位置模式配置
    BitmapDescriptor mCurrentMarker;  // 当前的标记
    private static final int accuracyCircleFillColor = 0xAAFFFF88;
    private static final int accuracyCircleStrokeColor = 0xAA00FF00;
    private SensorManager mSensorManager;  // 传感器管理
    private Double lastX = 0.0;
    private int mCurrentDirection = 0;
    private double mCurrentLat = 0.0;  // 当前经纬度
    private double mCurrentLon = 0.0;
    private float mCurrentAccracy;

    // 地图相关
    MapView mMapView;  // 控件
    BaiduMap mBaiduMap; // 百度地图
    MapStatus ms;  // 地图状态

    // UI相关
    Button requestLocButton;
    boolean isFirstLoc = true; // 是否首次定位
    private MyLocationData locData; // 位置数据
    private float direction;  // 方向

    // 点聚合相关
    private ClusterManager<MgBasketMapFragment.MyItem> mClusterManager;

    // 地图气泡相关
    private View mPopupView;
    private TextView mText_id;
    private TextView mText_text;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if(!isHasPermission()) requestPermission();  // 检查权限
        if (mView == null) {
            initWidge(inflater, container);
            //initOnTouchEvent();
        }
        return mView;
    }


    // 初始化控件
    public void initWidge(LayoutInflater inflater, ViewGroup container){
        mView = inflater.inflate(R.layout.fragment_rent_mg_basket_map, container, false);

        requestLocButton = (Button) mView.findViewById(R.id.button1);
        mSensorManager = (SensorManager) getActivity().getSystemService(SENSOR_SERVICE);//获取传感器管理服务
        mCurrentMode = MyLocationConfiguration.LocationMode.NORMAL;  // 当前地图指针模式
        requestLocButton.setText("普通");
        requestLocButton.setOnClickListener(btnClickListener);  // 地图模式更改消息响应
        mCurrentMarker = null;

        // 地图初始化
        mMapView = (MapView) mView.findViewById(R.id.bmapView);
        ms = new MapStatus.Builder().target(new LatLng(39.914935, 116.403119)).zoom(8).build();
        mBaiduMap = mMapView.getMap();
        mBaiduMap.setOnMapLoadedCallback(this);  // 地图加载完成返回函数
        mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(ms)); // 地图位置和比例尺
        mBaiduMap.setMyLocationConfiguration(new MyLocationConfiguration(mCurrentMode,
                true, null));  // 位置图标：传入null，恢复默认图标

        // 定位初始化
        mBaiduMap.setMyLocationEnabled(true); // 开启定位图层
        mLocClient = new LocationClient(getActivity());
        mLocClient.registerLocationListener(myListener);  // 注册监听
        LocationClientOption option = new LocationClientOption();
        option.setOpenGps(true); // 打开gps
        option.setCoorType("bd09ll"); // 设置坐标类型
        option.setScanSpan(1000);  // 扫描间隔
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy); // 设置定位模式
        //option.setNeedDeviceDirect(true);  // 设置返回结果包含手机方向
        mLocClient.setLocOption(option);
        mLocClient.start();  // 定位开始

        // 点聚合功能初始化
        mClusterManager = new ClusterManager<MgBasketMapFragment.MyItem>(getActivity(), mBaiduMap);
        addMarkers(); // 添加marker点
        mBaiduMap.setOnMapStatusChangeListener(mClusterManager); // 设置地图监听，当地图状态发生改变时，进行点聚合运算
        mBaiduMap.setOnMarkerClickListener(mClusterManager); // 设置maker点击时的响应

        // 地图气泡初始化
        mPopupView = inflater.inflate(R.layout.bmap_popu_marker, null);
        mText_id = (TextView) mPopupView.findViewById(R.id.marker_id);
        mText_text = (TextView) mPopupView.findViewById(R.id.marker_text);

        /*
         * 消息响应
         */
        // 点击聚合点响应实现
        mClusterManager.setOnClusterClickListener(new ClusterManager.OnClusterClickListener<MgBasketMapFragment.MyItem>() {
            @Override
            public boolean onClusterClick(Cluster<MgBasketMapFragment.MyItem> cluster) {
                List<MgBasketMapFragment.MyItem> items = (List<MgBasketMapFragment.MyItem>) cluster.getItems();  // 获取当前类所有的marker
                LatLngBounds.Builder builder2 = new LatLngBounds.Builder();
                int i=0;
                for(MgBasketMapFragment.MyItem myItem : items){
                    builder2 = builder2.include(myItem.getPosition());
                }
                LatLngBounds latlngBounds = builder2.build();
                MapStatusUpdate u = MapStatusUpdateFactory.newLatLngBounds(latlngBounds,mMapView.getWidth(),mMapView.getHeight());
                mBaiduMap.animateMapStatus(u);
                ms = mBaiduMap.getMapStatus();
                return false;
            }
        });
        // 点击单个item 响应
        mClusterManager.setOnClusterItemClickListener(new ClusterManager.OnClusterItemClickListener<MgBasketMapFragment.MyItem>() {
            @Override
            public boolean onClusterItemClick(MgBasketMapFragment.MyItem item) {
                if(item != null && item.getExtraInfo().get("address")!=null){
                    String address = String.valueOf(item.getExtraInfo().get("address"));
                    // 点击弹出气泡窗口，显示一些信息。点击窗口可以跳转到详情页面
                    mText_id.setText("编号：xxx");
                    mText_text.setText("名称：" + address);

                    // 定义用于显示该InfoWindow的坐标点
                    LatLng pt = new LatLng(item.getPosition().latitude, item.getPosition().longitude);
                    //创建InfoWindow , 传入 view， 地理坐标， y 轴偏移量
                    InfoWindow mInfoWindow = new InfoWindow(mPopupView, pt, -50);
                    //显示InfoWindow
                    mBaiduMap.showInfoWindow(mInfoWindow);
                }

                return false;
            }
        });
        // 点击气泡响应
        mPopupView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 点击气泡进入详情页面：待做
                Log.d(TAG, "点击气泡");
                mBaiduMap.hideInfoWindow();
                Intent intent = new Intent(getActivity(), BasketDetailActivity.class);
                startActivity(intent);
            }
        });
        // 点击地图响应
        mBaiduMap.setOnMapClickListener(new BaiduMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                // 隐藏infowindow
                mBaiduMap.hideInfoWindow();
            }

            @Override
            public boolean onMapPoiClick(MapPoi mapPoi) {
                return false;
            }
        });
    }

    /*
        消息响应
     */
    View.OnClickListener btnClickListener = new View.OnClickListener() {
        public void onClick(View v) {
            switch (mCurrentMode) {
                case NORMAL:
                    requestLocButton.setText("跟随");
                    mCurrentMode = MyLocationConfiguration.LocationMode.FOLLOWING;
                    mBaiduMap.setMyLocationConfiguration(new MyLocationConfiguration(
                            mCurrentMode, true, mCurrentMarker));
                    ms = new MapStatus.Builder().overlook(0).build();
                    mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(ms));
                    break;
                case COMPASS:
                    requestLocButton.setText("普通");
                    mCurrentMode = MyLocationConfiguration.LocationMode.NORMAL;
                    mBaiduMap.setMyLocationConfiguration(new MyLocationConfiguration(
                            mCurrentMode, true, mCurrentMarker));
                    ms = new MapStatus.Builder().overlook(0).build();
                    mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(ms));
                    break;
                case FOLLOWING:
                    requestLocButton.setText("罗盘");
                    mCurrentMode = MyLocationConfiguration.LocationMode.COMPASS;
                    mBaiduMap.setMyLocationConfiguration(new MyLocationConfiguration(
                            mCurrentMode, true, mCurrentMarker));
                    break;
                default:
                    break;
            }
        }
    };

    /*
        传感器监听重构函数
     */
    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        double x = sensorEvent.values[SensorManager.DATA_X];
        if (Math.abs(x - lastX) > 1.0) {
            mCurrentDirection = (int) x;
            locData = new MyLocationData.Builder()
                    .accuracy(mCurrentAccracy)
                    // 此处设置开发者获取到的方向信息，顺时针0-360
                    .direction(mCurrentDirection).latitude(mCurrentLat)
                    .longitude(mCurrentLon).build();
            mBaiduMap.setMyLocationData(locData);
        }
        lastX = x;

    }
    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    /*
        地图加载监听
     */
    @Override
    public void onMapLoaded() {
        // TODO Auto-generated method stub
        ms = new MapStatus.Builder().zoom(18.0f).build();  // 地图加载完成时，调用
        mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(ms));
    }

    /*
        定位监听SDK
     */
    public class MyLocationListenner extends BDAbstractLocationListener {

        @Override
        public void onReceiveLocation(BDLocation location) {
            // map view 销毁后不在处理新接收的位置
            if (location == null || mMapView == null) {
                return;
            }
            mCurrentLat = location.getLatitude();  // 获取经纬度
            mCurrentLon = location.getLongitude();
            mCurrentAccracy = location.getRadius();
            locData = new MyLocationData.Builder()
                    .accuracy(location.getRadius())
                    // 此处设置开发者获取到的方向信息，顺时针0-360
                    .direction(mCurrentDirection).latitude(location.getLatitude())
                    .longitude(location.getLongitude()).build();
            mBaiduMap.setMyLocationData(locData);
            if (isFirstLoc) {  // 是否首次定位
                isFirstLoc = false;
                LatLng ll = new LatLng(location.getLatitude(),
                        location.getLongitude());
                ms = new MapStatus.Builder().target(ll).zoom(18.0f).build();
                mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(ms));;
            }
            if(mCurrentLat<3.85 || mCurrentLat>53.55 // 定位不在中国境内，重新定位
                    || mCurrentLon<73.55 || mCurrentLon>135.08 )
                isFirstLoc = true;
        }

        public void onReceivePoi(BDLocation poiLocation) {
        }
    }
    /*
     * 生命周期函数
     */
    @Override
    public void onPause() {
        mMapView.onPause();
        super.onPause();
    }

    @Override
    public void onResume() {
        mMapView.onResume();
        super.onResume();
        //为系统的方向传感器注册监听器
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION),
                SensorManager.SENSOR_DELAY_UI);
    }

    @Override
    public void onStop() {
        //取消注册传感器监听
        mSensorManager.unregisterListener(this);
        super.onStop();
    }

    @Override
    public void onDestroy() {
        // 退出时销毁定位
        mLocClient.stop();
        // 关闭定位图层
        mBaiduMap.setMyLocationEnabled(false);
        mMapView.onDestroy();
        mMapView = null;
        super.onDestroy();
    }

    /*
        用xxpermissions申请权限
     */
    // 申请权限
    public void requestPermission() {
        XXPermissions.with(getActivity())
                .constantRequest() //可设置被拒绝后继续申请，直到用户授权或者永久拒绝
                .permission(Permission.READ_PHONE_STATE) //支持请求6.0悬浮窗权限8.0请求安装权限
                .permission(Permission.Group.STORAGE, Permission.Group.LOCATION) //不指定权限则自动获取清单中的危险权限
                .request(new OnPermission() {
                    @Override
                    public void hasPermission(List<String> granted, boolean isAll) {
                        if (isAll) {
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

    // 是否有这个权限
    public boolean isHasPermission() {
        if (XXPermissions.isHasPermission(getActivity(), Permission.Group.STORAGE) &&
                XXPermissions.isHasPermission(getActivity(), Permission.Group.LOCATION) &&
                XXPermissions.isHasPermission(getActivity(), Permission.READ_PHONE_STATE)) {
            return true;
        }else {
            return false;
        }
    }

    // 跳转到设置界面
    public void gotoPermissionSettings(View view) {
        XXPermissions.gotoPermissionSettings(getActivity());
    }

    /**
     * 每个Marker点，包含Marker点坐标以及图标
     */
    public class MyItem implements ClusterItem {
        private final LatLng mPosition;
        private Bundle buns; // 额外信息


        public MyItem(LatLng latLng) {
            mPosition = latLng;
        }

        public MyItem(LatLng latLng, Bundle bun) {
            mPosition = latLng;
            buns = bun;
        }

        @Override
        public LatLng getPosition() {
            // 返回marker坐标
            return mPosition;
        }

        @Override
        public Bundle getExtraInfo(){
            return buns;
        }

        @Override
        public BitmapDescriptor getBitmapDescriptor() {
            // 返回marker在地图上的图标
            return BitmapDescriptorFactory
                    .fromResource(R.drawable.ic_baidu_gcoding);
        }
    }

    // 添加初始点聚合点
    public void addMarkers() {
        // 添加Marker点
        LatLng l1A = new LatLng(32.061148, 118.800284); // 东南大学四牌楼校区
        LatLng l1B = new LatLng(32.061494, 118.796678);
        LatLng l1C = new LatLng(32.059660, 118.800120);
        Bundle bundle1_0 = new Bundle();
        bundle1_0.putString("address","东南大学四牌楼校区");
        LatLng l1D = new LatLng(31.893974, 118.826480); // 东南大学九龙湖校区
        LatLng l1E = new LatLng(31.887431, 118.826227);
        Bundle bundle1_1 = new Bundle();
        bundle1_1.putString("address","东南大学九龙湖校区");
        LatLng l1F = new LatLng(32.080885, 118.782467); // 东南大学丁家桥校区
        LatLng l1G = new LatLng(32.078562, 118.782731);
        Bundle bundle1_2 = new Bundle();
        bundle1_2.putString("address","东南大学丁家桥校区");

        LatLng l2A = new LatLng(32.061430, 118.786007); // 南京大学鼓楼校区
        Bundle bundle2_1 = new Bundle();
        bundle2_1.putString("address","南京大学鼓楼校区");
        LatLng l2B = new LatLng(32.125421, 118.964891); // 南京大学仙林校区
        Bundle bundle2_2 = new Bundle();
        bundle2_2.putString("address","南京大学仙林校区");

        LatLng l3A = new LatLng(32.041196, 118.826737); // 南航明故宫校区
        Bundle bundle3_1 = new Bundle();
        bundle3_1.putString("address","南航明故宫校区");
        LatLng l3B = new LatLng(31.944766, 118.798812); // 南航将军路校区
        Bundle bundle3_2 = new Bundle();
        bundle3_2.putString("address","南航将军路校区");

        LatLng l4A = new LatLng(32.031716, 118.863613); // 南京理工大学明孝陵
        Bundle bundle4_1 = new Bundle();
        bundle4_1.putString("address","南京理工大学明孝陵");
        LatLng l4B = new LatLng(32.132273, 118.940764); // 南京理工大学紫金学院
        Bundle bundle4_2 = new Bundle();
        bundle4_2.putString("address","南京理工大学紫金学院");

        List<MgBasketMapFragment.MyItem> items = new ArrayList<MgBasketMapFragment.MyItem>();
        items.add(new MgBasketMapFragment.MyItem(l1A, bundle1_0));
        items.add(new MgBasketMapFragment.MyItem(l1B, bundle1_0));
        items.add(new MgBasketMapFragment.MyItem(l1C, bundle1_0));
        items.add(new MgBasketMapFragment.MyItem(l1D, bundle1_1));
        items.add(new MgBasketMapFragment.MyItem(l1E, bundle1_1));
        items.add(new MgBasketMapFragment.MyItem(l1F, bundle1_2));
        items.add(new MgBasketMapFragment.MyItem(l1G, bundle1_2));
        items.add(new MgBasketMapFragment.MyItem(l2A, bundle2_1));
        items.add(new MgBasketMapFragment.MyItem(l2B, bundle2_2));
        items.add(new MgBasketMapFragment.MyItem(l3A, bundle3_1));
        items.add(new MgBasketMapFragment.MyItem(l3B, bundle3_2));
        items.add(new MgBasketMapFragment.MyItem(l4A, bundle4_1));
        items.add(new MgBasketMapFragment.MyItem(l4B, bundle4_2));

        mClusterManager.addItems(items);

    }

}
