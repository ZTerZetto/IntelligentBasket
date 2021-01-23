package com.automation.zzx.intelligent_basket_demo.widget.dialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.automation.zzx.intelligent_basket_demo.R;
import com.shehuan.niv.NiceImageView;

/**
 * Created by pengchenghu on 2019/3/18.
 * Author Email: 15651851181@163.com
 * Describe:
 */
public class PictureDialog extends Dialog implements View.OnClickListener{
    private TextView contentTxt;
    private TextView titleTxt;
    private TextView submitTxt;
    private TextView cancelTxt;
    private ImageView imageView;

    private Context mContext;
    private String content;
    private OnCloseListener listener;
    private int imageType; // 提示类型：0-提示，1-成功，2-失败
    private String positiveName;
    private String negativeName;
    private String title;

    public PictureDialog(Context context) {
        super(context);
        this.mContext = context;
    }

    public PictureDialog(Context context, int themeResId, String content) {
        super(context, themeResId);
        this.mContext = context;
        this.content = content;
    }

    public PictureDialog(Context context, int themeResId, String content, OnCloseListener listener) {
        super(context, themeResId);
        this.mContext = context;
        this.content = content;
        this.listener = listener;
    }

    protected PictureDialog(Context context, boolean cancelable, OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
        this.mContext = context;
    }

    public PictureDialog(Context context, int themeResId, String content, int imageType, PictureDialog.OnCloseListener listener) {
        super(context, themeResId);
        this.mContext = context;
        this.content = content;
        this.imageType = imageType;
        this.listener = listener;
    }


    public PictureDialog setTitle(String title){
        this.title = title;
        return this;
    }

    public PictureDialog setPositiveButton(String name){
        this.positiveName = name;
        return this;
    }

    public PictureDialog setNegativeButton(String name){
        this.negativeName = name;
        return this;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_picture);
        setCanceledOnTouchOutside(false);
        initView();
    }

    private void initView(){
        contentTxt = (TextView)findViewById(R.id.content);
        titleTxt = (TextView)findViewById(R.id.title);
        imageView =  (ImageView) findViewById(R.id.icon);
        submitTxt = (TextView)findViewById(R.id.submit);
        submitTxt.setOnClickListener(this);
        cancelTxt = (TextView)findViewById(R.id.cancel);
        cancelTxt.setOnClickListener(this);

        contentTxt.setText(content);
        if(!TextUtils.isEmpty(positiveName)){
            submitTxt.setText(positiveName);
        }

        if(!TextUtils.isEmpty(negativeName)){
            cancelTxt.setText(negativeName);
        }

        if(!TextUtils.isEmpty(title)){
            titleTxt.setText(title);
        }

        switch (imageType) {
            case 0:
//                imageView.setImageResource(R.mipmap.dialog_tip);
                imageView.setImageResource(R.mipmap.dialog_tip);
                titleTxt.setText("提示");
                break;
            case 1:
//                imageView.setImageResource(R.mipmap.dialog_sucess);
                imageView.setImageResource(R.mipmap.dialog_sucess);
                titleTxt.setText("操作成功");
                break;
            case 2:
//                imageView.setImageResource(R.mipmap.dialog_fail);
                imageView.setImageResource(R.mipmap.dialog_fail);
                titleTxt.setText("操作失败！");
                break;
            default:
                imageView.setVisibility(View.GONE);
                break;
        }


    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.cancel:
                if(listener != null){
                    listener.onClick(this, false);
                }
                this.dismiss();
                break;
            case R.id.submit:
                if(listener != null){
                    listener.onClick(this, true);
                }
                break;
        }
    }

    public interface OnCloseListener{
        void onClick(Dialog dialog, boolean confirm);
    }
}
