package com.baidu.mapapi.clusterutil.clustering.algo;

import com.baidu.mapapi.clusterutil.clustering.Cluster;
import com.baidu.mapapi.clusterutil.clustering.ClusterItem;
import com.baidu.mapapi.clusterutil.projection.Point;
import com.baidu.mapapi.clusterutil.projection.SphericalMercatorProjection;
import com.baidu.mapapi.clusterutil.quadtree.PointQuadTree;
import com.baidu.mapapi.model.LatLng;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Created by pengchenghu on 2019/1/23.
 * Author Email: 15651851181@163.com
 * Describe: 基于行政区域划分的聚类算法
 */
/*
 * 基于行政区域划分聚类算法
 * 高级算法
 * 1. 按照添加的顺序遍历条目（候选集）
 * 2. 根据不同的zoom，生成不同的行政划分等级
 * 3. 创建以项为中心的集群
 * 4. 添加到集群中一样的行政区域归属
 * 5. 从候选集列表中删除这些项
 */
public class AdministrativeDivisionBasedAlgorithm <T extends ClusterItem> implements Algorithm<T>{

    /**
     * Any modifications should be synchronized on mQuadTree.
     */
    private final Collection<QuadItem<T>> mItems = new ArrayList<QuadItem<T>>();

    /**
     * Any modifications should be synchronized on mQuadTree.
     */
    private final PointQuadTree<QuadItem<T>> mQuadTree = new PointQuadTree<QuadItem<T>>(0, 1, 0, 1);

    private static final SphericalMercatorProjection PROJECTION = new SphericalMercatorProjection(1);

    @Override
    public void addItem(T item) {
        // 把自定义的MyItem对象全部转换成Item对象，然后保存到QuadTree树中，这是一种四叉树
        final QuadItem<T> quadItem = new QuadItem<T>(item);
        synchronized (mQuadTree) {
            mItems.add(quadItem);
            mQuadTree.add(quadItem);
        }
    }

    @Override
    public void addItems(Collection<T> items) {
        for (T item : items) {
            addItem(item);
        }
    }

    @Override
    public void clearItems() {
        synchronized (mQuadTree) {
            mItems.clear();
            mQuadTree.clear();
        }
    }

    @Override
    public void removeItem(T item) {
        // TODO: delegate QuadItem#hashCode and QuadItem#equals to its item.
        throw new UnsupportedOperationException("AdministrativeDivisionBasedAlgorithm.remove not implemented");
    }

    /**
     *  cluster算法核心
     * @param zoom map的级别
     * @return
     */
    @Override
    public Set<? extends Cluster<T>> getClusters(double zoom) {

        return null;
    }

    @Override
    public Collection<T> getItems() {
        final List<T> items = new ArrayList<T>();
        synchronized (mQuadTree) {
            for (QuadItem<T> quadItem : mItems) {
                items.add(quadItem.mClusterItem);
            }
        }
        return items;
    }

    private static class QuadItem<T extends ClusterItem> implements PointQuadTree.Item, Cluster<T> {
        private final T mClusterItem;
        private final Point mPoint;
        private final LatLng mPosition;
        private Set<T> singletonSet;

        private QuadItem(T item) {
            mClusterItem = item;
            mPosition = item.getPosition();
            mPoint = PROJECTION.toPoint(mPosition);
            singletonSet = Collections.singleton(mClusterItem);
        }

        @Override
        public Point getPoint() {
            return mPoint;
        }

        @Override
        public LatLng getPosition() {
            return mPosition;
        }

        @Override
        public Set<T> getItems() {
            return singletonSet;
        }

        @Override
        public int getSize() {
            return 1;
        }
    }
}
