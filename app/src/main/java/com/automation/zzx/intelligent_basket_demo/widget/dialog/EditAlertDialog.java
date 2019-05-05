package com.automation.zzx.intelligent_basket_demo.widget.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.automation.zzx.intelligent_basket_demo.R;

/**
 * Created by pengchenghu on 2019/3/18.
 * Author Email: 15651851181@163.com
 * Describe:
 */
public class EditAlertDialog extends Dialog implements View.OnClickListener{
    private EditText contentTxt;
    private TextView titleTxt;
    private TextView submitTxt;
    private TextView cancelTxt;

    private Context mContext;
    private String content;
    private OnCloseListener listener;
    private String positiveName;
    private String negativeName;
    private String title;

    public EditAlertDialog(Context context) {
        super(context);
        this.mContext = context;
    }

    public EditAlertDialog(Context context, int themeResId, String content) {
        super(context, themeResId);
        this.mContext = context;
        this.content = content;
    }

    public EditAlertDialog(Context context, int themeResId, String content, OnCloseListener listener) {
        super(context, themeResId);
        this.mContext = context;
        this.content = content;
        this.listener = listener;
    }

    protected EditAlertDialog(Context context, boolean cancelable, OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
        this.mContext = context;
    }

    public EditAlertDialog setTitle(String title){
        this.title = title;
        return this;
    }

    public EditAlertDialog setPositiveButton(String name){
        this.positiveName = name;
        return this;
    }

    public EditAlertDialog setNegativeButton(String name){
        this.negativeName = name;
        return this;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_edit_alert);
        setCanceledOnTouchOutside(false);
        initView();
    }

    private void initView(){
        contentTxt = (EditText) findViewById(R.id.content);
        titleTxt = (TextView)findViewById(R.id.title);
        submitTxt = (TextView)findViewById(R.id.submit);
        submitTxt.setOnClickListener(this);
        cancelTxt = (TextView)findViewById(R.id.cancel);
        cancelTxt.setOnClickListener(this);

        contentTxt.setText(content);
        contentTxt.setSelection(content.length());//光标移至文字末尾

        if(!TextUtils.isEmpty(positiveName)){
            submitTxt.setText(positiveName);
        }

        if(!TextUtils.isEmpty(negativeName)){
            cancelTxt.setText(negativeName);
        }

        if(!TextUtils.isEmpty(title)){
            titleTxt.setText(title);
        }

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.cancel:
                if(listener != null){
                    listener.onClick(this, false,null);
                }
                this.dismiss();
                break;
            case R.id.submit:
                if(listener != null){

                    listener.onClick(this, true, contentTxt.getText().toString());
                }
                break;
        }
    }

    public interface OnCloseListener{
        void onClick(Dialog dialog, boolean confirm, String result);
    }
}
