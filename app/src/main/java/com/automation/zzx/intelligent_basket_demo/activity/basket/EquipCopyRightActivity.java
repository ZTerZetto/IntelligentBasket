package com.automation.zzx.intelligent_basket_demo.activity.basket;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import com.automation.zzx.intelligent_basket_demo.R;
import com.automation.zzx.intelligent_basket_demo.entity.UserInfo;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EquipCopyRightActivity extends AppCompatActivity {

    // intent 消息参数
    public final static String BASKET_ID = "basket_id";  // 项目Id

    // Handler消息
    private final static int MG_ALARM_LIST_INFO = 1;
    private final static int UPDATE_LIST_INFO = 2;

    List<Map<String,String>> maps = new ArrayList<>();
    private List<String> copyrights = new ArrayList<>();
    private List<String> titles = new ArrayList<>();
    private SimpleAdapter adapter1;
    private TextView txtBasketId;
    private ListView lvItem;

    // 用户登录信息相关
    private UserInfo mUserInfo;
    private String mToken;
    private SharedPreferences mPref;

    // 吊篮信息
    private String mBasketId;

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            switch(msg.what) {
                case MG_ALARM_LIST_INFO: // 获取报停记录

                    break;
                case UPDATE_LIST_INFO:

                default:
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_equip_copy_right);
        //获取用户数据
        getUserInfo();
        //获取版权数据
        getCopyrightInfoList();
        //初始化控件
        initView();

    }

    // 获取用户数据
    private void getUserInfo(){
        // 从本地获取数据
        mPref = PreferenceManager.getDefaultSharedPreferences(this);
        mUserInfo = new UserInfo();
        mUserInfo.setUserId(mPref.getString("userId", ""));
        mUserInfo.setUserPhone(mPref.getString("userPhone", ""));
        mUserInfo.setUserRole(mPref.getString("userRole", ""));
        mToken = mPref.getString("loginToken","");

        //获取当前项目ID
        Intent intent = getIntent();
        mBasketId = intent.getStringExtra(BASKET_ID);
    }

    private void initView() {

        // 顶部导航栏
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        TextView titleText = (TextView) findViewById(R.id.toolbar_title);
        toolbar.setTitle("设备产权");
        titleText.setText("");
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true); //设置返回键可用

        txtBasketId = findViewById(R.id.txt_basket_id);
        txtBasketId.setText(mBasketId);

        lvItem = findViewById(R.id.copyright_list);
        adapter1 = new SimpleAdapter(EquipCopyRightActivity.this,maps,R.layout.item_simple_list_2,
                new String[]{"title","content"},new int[]{R.id.text1,R.id.text2});
/*        {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView text1 = (TextView) view.findViewById(android.R.id.text1);
                TextView text2 = (TextView) view.findViewById(android.R.id.text2);
                text1.setTextColor(Color.DKGRAY);
                text2.setTextColor(Color.DKGRAY);
                return view;
            }
        };*/
        lvItem.setAdapter(adapter1);
    }

    private void getCopyrightInfoList(){
        initTitle();
        for (int i = 0;i < 13;i++) {
            Map<String, String> listItem = new HashMap<>();
            listItem.put("title", titles.get(i));//这里图片数组
            listItem.put("content", copyrights.get(i));
            maps.add(listItem);
        }
    }

    private void initTitle() {
        //TODO 联网获取设备产权
        titles.add("电柜");
        titles.add("摄像头");
        titles.add("安全绳");
        titles.add("电缆");
        titles.add("提升机");
        titles.add("安全锁");
        titles.add("主钢丝");
        titles.add("副钢丝");
        titles.add("重锤");
        titles.add("上限位器");
        titles.add("称重器");
        titles.add("大臂");
        titles.add("配重");

        for (int i = 0;i < 13;i++){
            copyrights.add("1234567");
        }
    }

    // 顶部导航栏消息响应
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home: // 返回按钮
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
