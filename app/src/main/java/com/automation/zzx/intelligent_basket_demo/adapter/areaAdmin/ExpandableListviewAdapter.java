package com.automation.zzx.intelligent_basket_demo.adapter.areaAdmin;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.automation.zzx.intelligent_basket_demo.R;

import java.util.List;


public class ExpandableListviewAdapter extends BaseExpandableListAdapter {

    private Context context;
    private List<String> groups;
    private List<List<String>> childs;


    public ExpandableListviewAdapter(Context context, List<String> groups, List<List<String>> childs){
        this.context=context;
        this.groups=groups;
        this.childs=childs;
    }

    public void refresh( List<String> groups, List<List<String>> childs) {
        this.groups=groups;
        this.childs=childs;
        notifyDataSetChanged();
    }

    @Override
    public int getGroupCount() {
        return groups.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return childs.get(groupPosition).size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return groups.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return childs.get(groupPosition).get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    //分组和子选项是否持有稳定的ID, 就是说底层数据的改变会不会影响到它们
    public boolean hasStableIds() {
        return false;
    }

    @Override
    /**
     *
     * 获取显示指定组的视图对象
     *
     * @param groupPosition 组位置
     * @param isExpanded 该组是展开状态还是伸缩状态，true=展开
     * @param convertView 重用已有的视图对象
     * @param parent 返回的视图对象始终依附于的视图组
     */
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        GroupViewHolder groupViewHolder;
        if (convertView == null){
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.expand_parent_item,parent,false);
            groupViewHolder = new GroupViewHolder();
            groupViewHolder.tvParent = convertView.findViewById(R.id.parent_textview_id);
            groupViewHolder.ivParent = convertView.findViewById(R.id.parent_image);
            groupViewHolder.tvParentDetail = convertView.findViewById(R.id.parent_textview_detail);
            convertView.setTag(groupViewHolder);
        } else {
            groupViewHolder = (GroupViewHolder)convertView.getTag();
        }
        groupViewHolder.tvParent.setText(groups.get(groupPosition));
        groupViewHolder.tvParentDetail.setText("共"+String.valueOf(childs.get(groupPosition).size())+"条记录");
        //展开状态下
        if(isExpanded){
            groupViewHolder.ivParent.setImageDrawable(ContextCompat.getDrawable(context,R.mipmap.icon_dropup));
        } else {
            groupViewHolder.ivParent.setImageDrawable(ContextCompat.getDrawable(context,R.mipmap.icon_dropdown));
        }
        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        ChildViewHolder childViewHolder;
        if (convertView == null){
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.expand_child_item,parent,false);
            childViewHolder = new ChildViewHolder();
            childViewHolder.tvChildren = convertView.findViewById(R.id.child_item);
            //childViewHolder.ivChildren = convertView.findViewById(R.id.child_image);
            convertView.setTag(childViewHolder);
        } else {
            childViewHolder = (ChildViewHolder) convertView.getTag();
        }
        childViewHolder.tvChildren.setText(childs.get(groupPosition).get(childPosition));

        return convertView;
    }

    @Override
    //指定位置上的子元素是否可选中
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    static class GroupViewHolder {
        TextView  tvParent;
        TextView  tvParentDetail;
        ImageView ivParent;
    }

    static class ChildViewHolder {
        TextView  tvChildren;
        //ImageView ivChildren;
    }

}
