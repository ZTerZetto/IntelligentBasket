package com.automation.zzx.intelligent_basket_demo.fragment.rentAdmin;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.automation.zzx.intelligent_basket_demo.R;
import com.automation.zzx.intelligent_basket_demo.adapter.rentAdmin.MgRentMessageAdapter;
import com.automation.zzx.intelligent_basket_demo.entity.MessageInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by pengchenghu on 2019/3/27.
 * Author Email: 15651851181@163.com
 * Describe:租方管理员消息页面
 */
public class RentAdminMessageFragment extends Fragment {
    private View mView;
    private MgRentMessageAdapter mgRentMessageAdapter;
    private List<MessageInfo> mMessageInfoList = new ArrayList<>();
    private RecyclerView recyclerView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (mView == null) {
            mView = inflater.inflate(R.layout.fragment_rent_admin_message, container, false);
            // 顶部导航栏
            Toolbar toolbar = (Toolbar) mView.findViewById(R.id.toolbar);
            TextView titleText = (TextView) mView.findViewById(R.id.toolbar_title);
            toolbar.setTitle("");
            titleText.setText(getString(R.string.info_title));
            ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
            //隐藏返回箭头
            ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(false);

            initMessageInfoList();//初始化消息列表信息
            recyclerView = mView.findViewById(R.id.rv_message);
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
            recyclerView.setLayoutManager(linearLayoutManager);
            mgRentMessageAdapter = new MgRentMessageAdapter(mMessageInfoList);
            recyclerView.setAdapter(mgRentMessageAdapter);
        }
        return mView;
    }

    /**
     * 获取消息列表信息
     * */
    private void initMessageInfoList(){
        for(int i = 0; i < 5;i++){
            MessageInfo messageInfo = new MessageInfo("2019年3月31日 14:00","报警消息","张三违规操作");
            mMessageInfoList.add(messageInfo);
        }
        MessageInfo messageInfo = new MessageInfo("今天 15:21","吊篮预报停申请","吊篮预报停申请通过");
        mMessageInfoList.add(messageInfo);
    }

}
