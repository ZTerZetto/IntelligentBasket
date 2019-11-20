package com.automation.zzx.intelligent_basket_demo.fragment.areaAdmin;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.automation.zzx.intelligent_basket_demo.R;
import com.automation.zzx.intelligent_basket_demo.activity.areaAdmin.AreaAdminProListActivity;
import com.automation.zzx.intelligent_basket_demo.entity.UserInfo;
import com.automation.zzx.intelligent_basket_demo.utils.ToastUtil;



public class AreaAdminFirstFragment extends Fragment implements View.OnClickListener {

    private final static String TAG = "AreaAdminFirstFragment";

    private final static String OPERATING = "operatingProjectList";
    private final static String INSTALLING = "installingProjectList";
    private final static String ENDING = "endProjectList";


    // 控件
    // 顶部导航栏
    private Toolbar mProjectMoreTb;
    private TextView mPorjectTitle;

    //按钮
    private ImageView ivPro1;
    private ImageView ivPro2;
    private ImageView ivPro3;

    // 个人信息相关
    private UserInfo mUserInfo;
    private String mToken;
    private SharedPreferences mPref;
    private SharedPreferences.Editor editor;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_area_admin_project_first,
                container, false);
        // 顶部toolbar
        mProjectMoreTb = (Toolbar) view.findViewById(R.id.project_more_toolbar);
        mProjectMoreTb.setTitle("");
        mPorjectTitle = view.findViewById(R.id.project_title);
        mPorjectTitle.setText("项   目");
        ((AppCompatActivity) getActivity()).setSupportActionBar(mProjectMoreTb);

        // 控件
        ivPro1 = view.findViewById(R.id.iv_project_1);
        ivPro1.setOnClickListener(this);
        ivPro2 = view.findViewById(R.id.iv_project_2);
        ivPro2.setOnClickListener(this);
        ivPro3 = view.findViewById(R.id.iv_project_3);
        ivPro3.setOnClickListener(this);

        return view;
    }
    @Override
    public void onClick(View v) {
        Intent intent;
        switch (v.getId()){
            case R.id.iv_project_1:
                intent = new Intent(getContext(), AreaAdminProListActivity.class);
                intent.putExtra("project_type",OPERATING);
                startActivity(intent);
                break;
            case R.id.iv_project_2:
                intent = new Intent(getContext(), AreaAdminProListActivity.class);
                intent.putExtra("project_type",INSTALLING);
                startActivity(intent);
                break;
            case R.id.iv_project_3:
                intent = new Intent(getContext(), AreaAdminProListActivity.class);
                intent.putExtra("project_type",ENDING);
                startActivity(intent);
                break;
        }
    }

}