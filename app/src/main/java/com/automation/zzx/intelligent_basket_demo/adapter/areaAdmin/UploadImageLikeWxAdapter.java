package com.automation.zzx.intelligent_basket_demo.adapter.areaAdmin;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.automation.zzx.intelligent_basket_demo.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by pengchenghu on 2019/3/28.
 * Author Email: 15651851181@163.com
 * Describe:
 */
public class UploadImageLikeWxAdapter extends BaseAdapter {

    private Context context;
    private List<Bitmap> bitmapList = new ArrayList<>();

    /*
     * 构造函数
     */
    public UploadImageLikeWxAdapter(Context context, List<Bitmap> bitmapList){
        this.bitmapList = bitmapList;
        this.context = context;
    }

    @Override
    public int getCount() {
        return bitmapList.size();
    }

    @Override
    public Object getItem(int position) {
        return bitmapList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View gridview_item = View.inflate(context, R.layout.item_upload_image, null);

        ImageView iv_icon = (ImageView) gridview_item.findViewById(R.id.gridview_image);
        int widthPix = ((Activity) context).getResources().getDisplayMetrics().widthPixels;
        widthPix = widthPix / 3 - 20;
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(widthPix, widthPix);
        iv_icon.setLayoutParams(params);
        iv_icon.setImageBitmap(bitmapList.get(position));

        return gridview_item;
    }
}
