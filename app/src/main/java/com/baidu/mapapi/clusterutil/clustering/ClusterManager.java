/*
 * Copyright (C) 2015 Baidu, Inc. All Rights Reserved.
 */

package com.baidu.mapapi.clusterutil.clustering;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;

import com.baidu.mapapi.clusterutil.MarkerManager;
import com.baidu.mapapi.clusterutil.clustering.algo.Algorithm;
import com.baidu.mapapi.clusterutil.clustering.algo.NonHierarchicalDistanceBasedAlgorithm;
import com.baidu.mapapi.clusterutil.clustering.algo.PreCachingAlgorithmDecorator;
import com.baidu.mapapi.clusterutil.clustering.view.ClusterRenderer;
import com.baidu.mapapi.clusterutil.clustering.view.DefaultClusterRenderer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.Marker;

import java.util.Collection;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Groups many items on a map based on zoom level.
 * <p/>
 * ClusterManager should be added to the map
 * <li>
 */
public class ClusterManager<T extends ClusterItem> implements
        BaiduMap.OnMapStatusChangeListener, BaiduMap.OnMarkerClickListener {
    private final MarkerManager mMarkerManager;
    private final MarkerManager.Collection mMarkers;
    private final MarkerManager.Collection mClusterMarkers;

    private Algorithm<T> mAlgorithm;  // 负责计算是否达到聚合条件
    private final ReadWriteLock mAlgorithmLock = new ReentrantReadWriteLock();  // 读写锁类
    private ClusterRenderer<T> mRenderer;  // 负责处理聚合操作，如marker的显示以及它们的动画

    private BaiduMap mMap;  // 地图主题
    private MapStatus mPreviousCameraPosition;  // 地图状态
    private ClusterTask mClusterTask;  // 聚合线程
    private final ReadWriteLock mClusterTaskLock = new ReentrantReadWriteLock();

    // 各类点击事件监听
    private OnClusterItemClickListener<T> mOnClusterItemClickListener;
    private OnClusterInfoWindowClickListener<T> mOnClusterInfoWindowClickListener;
    private OnClusterItemInfoWindowClickListener<T> mOnClusterItemInfoWindowClickListener;
    private OnClusterClickListener<T> mOnClusterClickListener;

    // 构造函数
    public ClusterManager(Context context, BaiduMap map) {
        this(context, map, new MarkerManager(map));
    }

    public ClusterManager(Context context, BaiduMap map, MarkerManager markerManager) {
        mMap = map;
        mMarkerManager = markerManager;
        mClusterMarkers = markerManager.newCollection();
        mMarkers = markerManager.newCollection();
        mRenderer = new DefaultClusterRenderer<T>(context, map, this);
        mAlgorithm = new PreCachingAlgorithmDecorator<T>(new NonHierarchicalDistanceBasedAlgorithm<T>());
        mClusterTask = new ClusterTask();
        mRenderer.onAdd();
    }

    public MarkerManager.Collection getMarkerCollection() {
        return mMarkers;
    }

    public MarkerManager.Collection getClusterMarkerCollection() {
        return mClusterMarkers;
    }

    public MarkerManager getMarkerManager() {
        return mMarkerManager;
    }

    public void setRenderer(ClusterRenderer<T> view) {
        mRenderer.setOnClusterClickListener(null);
        mRenderer.setOnClusterItemClickListener(null);
        mClusterMarkers.clear();
        mMarkers.clear();
        mRenderer.onRemove();
        mRenderer = view;
        mRenderer.onAdd();
        mRenderer.setOnClusterClickListener(mOnClusterClickListener);
        mRenderer.setOnClusterInfoWindowClickListener(mOnClusterInfoWindowClickListener);
        mRenderer.setOnClusterItemClickListener(mOnClusterItemClickListener);
        mRenderer.setOnClusterItemInfoWindowClickListener(mOnClusterItemInfoWindowClickListener);
        cluster();
    }

    public void setAlgorithm(Algorithm<T> algorithm) {
        mAlgorithmLock.writeLock().lock();
        try {
            if (mAlgorithm != null) {
                algorithm.addItems(mAlgorithm.getItems());
            }
            mAlgorithm = new PreCachingAlgorithmDecorator<T>(algorithm);
        } finally {
            mAlgorithmLock.writeLock().unlock();
        }
        cluster();
    }

    /*
     * ClusterItem 增删改查
     */
    public void clearItems() {
        mAlgorithmLock.writeLock().lock();
        try {
            mAlgorithm.clearItems();
        } finally {
            mAlgorithmLock.writeLock().unlock();
        }
    }

    public void addItems(Collection<T> items) {
        mAlgorithmLock.writeLock().lock();
        try {
            mAlgorithm.addItems(items);  // 将ClusterItem添加到Algorithm对象中
        } finally {
            mAlgorithmLock.writeLock().unlock();
        }

    }

    public void addItem(T myItem) {
        mAlgorithmLock.writeLock().lock();
        try {
            mAlgorithm.addItem(myItem);
        } finally {
            mAlgorithmLock.writeLock().unlock();
        }
    }

    public void removeItem(T item) {
        mAlgorithmLock.writeLock().lock();
        try {
            mAlgorithm.removeItem(item);
        } finally {
            mAlgorithmLock.writeLock().unlock();
        }
    }

    /**
     * Force a re-cluster. You may want to call this after adding new item(s).
     */
    public void cluster() {
        mClusterTaskLock.writeLock().lock();  // 停止之前的操作
        try {
            // Attempt to cancel the in-flight request.
            mClusterTask.cancel(true);
            mClusterTask = new ClusterTask();  // 开启新的ClusterTask任务线程，执行聚合

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
                mClusterTask.execute(mMap.getMapStatus().zoom);
            } else {
                mClusterTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, mMap.getMapStatus().zoom);
            }
        } finally {
            mClusterTaskLock.writeLock().unlock();
        }
    }


    /*
     * 监听地图状态，如缩放比例
     */
    @Override
    public void onMapStatusChangeStart(MapStatus mapStatus) {
        // 地图状态改变开始
    }

    @Override
    public void onMapStatusChangeStart(MapStatus status, int reason) {
        // 地图状态改变开始
    }

    @Override
    public void onMapStatusChange(MapStatus mapStatus) {
        // 地图状态改变
        if (mRenderer instanceof BaiduMap.OnMapStatusChangeListener) {
            ((BaiduMap.OnMapStatusChangeListener) mRenderer).onMapStatusChange(mapStatus);
        }

        // Don't re-compute clusters if the map has just been panned/tilted/rotated.
        MapStatus position = mMap.getMapStatus();
        if (mPreviousCameraPosition != null && mPreviousCameraPosition.zoom == position.zoom) {
            return;
        }
        mPreviousCameraPosition = mMap.getMapStatus();

        cluster();  // 聚合实现
    }

    @Override
    public void onMapStatusChangeFinish(MapStatus mapStatus) {
        // 地图状态改变结束
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        return getMarkerManager().onMarkerClick(marker);
    }

    /**
     * Runs the clustering algorithm in a background thread, then re-paints when results come back.
     * 另开线程运行聚合算法，然后当结果返回时将重绘地图
     */
    private class ClusterTask extends AsyncTask<Float, Void, Set<? extends Cluster<T>>> {
        @Override
        protected Set<? extends Cluster<T>> doInBackground(Float... zoom) {
            // 计算可以聚合的cluster集合
            mAlgorithmLock.readLock().lock();
            try {
                return mAlgorithm.getClusters(zoom[0]);
            } finally {
                mAlgorithmLock.readLock().unlock();
            }
        }

        @Override
        protected void onPostExecute(Set<? extends Cluster<T>> clusters) {
            // 处理后台线程返回的结果
            mRenderer.onClustersChanged(clusters);
        }
    }

    /*
     * 各类点击事件响应实现
     */
    /**
     * Sets a callback that's invoked when a Cluster is tapped. Note: For this listener to function,
     * the ClusterManager must be added as a click listener to the map.
     * 聚合类被点击
     */
    public void setOnClusterClickListener(OnClusterClickListener<T> listener) {
        mOnClusterClickListener = listener;
        mRenderer.setOnClusterClickListener(listener);
    }

    /**
     * Sets a callback that's invoked when a Cluster is tapped. Note: For this listener to function,
     * the ClusterManager must be added as a info window click listener to the map.
     * 聚合类信息窗口被点击
     */
    public void setOnClusterInfoWindowClickListener(OnClusterInfoWindowClickListener<T> listener) {
        mOnClusterInfoWindowClickListener = listener;
        mRenderer.setOnClusterInfoWindowClickListener(listener);
    }

    /**
     * Sets a callback that's invoked when an individual ClusterItem is tapped. Note: For this
     * listener to function, the ClusterManager must be added as a click listener to the map.
     * 独立的聚合Item被点击
     */
    public void setOnClusterItemClickListener(OnClusterItemClickListener<T> listener) {
        mOnClusterItemClickListener = listener;
        mRenderer.setOnClusterItemClickListener(listener);
    }

    /**
     * Sets a callback that's invoked when an individual ClusterItem's Info Window is tapped. Note: For this
     * listener to function, the ClusterManager must be added as a info window click listener to the map.
     * 聚合Item信息窗口被点击
     */
    public void setOnClusterItemInfoWindowClickListener(OnClusterItemInfoWindowClickListener<T> listener) {
        mOnClusterItemInfoWindowClickListener = listener;
        mRenderer.setOnClusterItemInfoWindowClickListener(listener);
    }

    /**
     * Called when a Cluster is clicked.
     */
    public interface OnClusterClickListener<T extends ClusterItem> {
        public boolean onClusterClick(Cluster<T> cluster);
    }

    /**
     * Called when a Cluster's Info Window is clicked.
     */
    public interface OnClusterInfoWindowClickListener<T extends ClusterItem> {
        public void onClusterInfoWindowClick(Cluster<T> cluster);
    }

    /**
     * Called when an individual ClusterItem is clicked.
     */
    public interface OnClusterItemClickListener<T extends ClusterItem> {
        public boolean onClusterItemClick(T item);
    }

    /**
     * Called when an individual ClusterItem's Info Window is clicked.
     */
    public interface OnClusterItemInfoWindowClickListener<T extends ClusterItem> {
        public void onClusterItemInfoWindowClick(T item);
    }
}
