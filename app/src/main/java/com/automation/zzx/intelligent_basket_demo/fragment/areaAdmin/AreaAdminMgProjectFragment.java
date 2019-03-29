package com.automation.zzx.intelligent_basket_demo.fragment.areaAdmin;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.menu.MenuBuilder;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.automation.zzx.intelligent_basket_demo.R;
import com.automation.zzx.intelligent_basket_demo.activity.ProDetailActivity;
import com.automation.zzx.intelligent_basket_demo.activity.areaAdmin.AreaAdminPrimaryActivity;
import com.automation.zzx.intelligent_basket_demo.activity.areaAdmin.UploadImageActivity;
import com.automation.zzx.intelligent_basket_demo.adapter.areaAdmin.MgBasketStatementAdapter;
import com.automation.zzx.intelligent_basket_demo.adapter.areaAdmin.MgStateAdapter;
import com.automation.zzx.intelligent_basket_demo.entity.MgBasketStatement;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by pengchenghu on 2019/3/27.
 * Author Email: 15651851181@163.com
 * Describe:租方管理员项目
 */
public class AreaAdminMgProjectFragment extends Fragment {

    private final static String TAG = "AreaAdminMgProject";
    // Handler 消息类型
    private final static int UPDATE_BASKET_STATEMENT_MSG = 1;  // 更新吊篮状态筛选栏
    private final static int UPDATE_PROJECT_AND_BASKET_MSG = 2;

    // 控件
    // 顶部导航栏
    private Toolbar mProjectMoreTb;
    private TextView mProjectTitleTv; // 项目名称
    private AlertDialog mSelectProjectDialog;  // 切换项目弹窗
    private List<String> mProjectList;  // 项目列表（网络请求）
    private int currentSelectedProject = 0; // 当前项目号
    private int tmpSelectedProject = 0; // 临时项目号
    // 吊篮状态选择栏
    private GridView mBasketStateGv; // 吊篮状态
    private List<String> mStateLists; // 状态名称
    private MgStateAdapter mgStateAdapter; //适配器
    private int pre_selectedPosition = 0;
    // 主体内容部分
    private RelativeLayout mListRelativeLayout;  // 有吊篮
    private RelativeLayout mBlankRelativeLayout; // 无吊篮
    private RecyclerView mBasketListRecyclerView;
    private List<MgBasketStatement> mgBasketStatementList;  // 显示列表（网络请求）
    private List<List<MgBasketStatement>> mgBasketStatementClassifiedList;  // 分类的吊篮数据（网络请求）
    private MgBasketStatementAdapter mgBasketStatementAdapter;

    // 上下左右滑动监听
    private static enum State{ VISIBLE,ANIMATIONING,INVISIBLE,}
    private State state = State.INVISIBLE;
    protected static final float FLIP_DISTANCE = 150;
    private GestureDetector mGestureDetector;
    private AreaAdminPrimaryActivity.MyOnTouchListener myOnTouchListener;
    private SVCGestureListener mGestureListener = new SVCGestureListener();

    /*
     * 消息函数
     */
    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch(msg.what) {
                case UPDATE_BASKET_STATEMENT_MSG:  // 更新列表
                    mgBasketStatementList.clear();
                    mgBasketStatementList.addAll(mgBasketStatementClassifiedList.get(pre_selectedPosition));
                    if(mgBasketStatementList.size() == 0){  // 显示无操作吊篮
                        mListRelativeLayout.setVisibility(View.GONE);
                        mBlankRelativeLayout.setVisibility(View.VISIBLE);
                    }else {                                   // 显示可操作吊篮列表
                        mBlankRelativeLayout.setVisibility(View.GONE);
                        mListRelativeLayout.setVisibility(View.VISIBLE);
                        mgBasketStatementAdapter.notifyDataSetChanged();
                    }
                    break;
                case UPDATE_PROJECT_AND_BASKET_MSG:  // 更换项目，重新获取吊篮列表
                    // 更新项目名称
                    mProjectTitleTv.setText(mProjectList.get(currentSelectedProject));
                    mgBasketStatementList.clear();
                    mgBasketStatementClassifiedList.clear();
                    if(currentSelectedProject == 0){  // 初始化列表
                        initMgBasketStatementList();
                        initMgBasketStatementClassifiedList();
                        parseMgBasketStatementList(mgBasketStatementList);
                    }else{  // 其它列表为空
                        initMgBasketStatementClassifiedList();
                    }
                    pre_selectedPosition = 0;
                    mgStateAdapter.setSelectedPosition(pre_selectedPosition);
                    sendEmptyMessage(UPDATE_BASKET_STATEMENT_MSG);
                    break;
                default:
                    break;
            }
        }
    };

    /*
     * 生命周期函数
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_area_admin_manage_project,
                container, false);

        // 顶部toolbar
        mProjectMoreTb = (Toolbar) view.findViewById(R.id.project_more_toolbar);
        mProjectTitleTv = (TextView) view.findViewById(R.id.project_title);
        mProjectMoreTb.setTitle("项目");
        ((AppCompatActivity) getActivity()).setSupportActionBar(mProjectMoreTb);
        initProjectList();        // 初始化项目列表

        // 状态选择栏初
        // 初始化
        mBasketStateGv = (GridView) view.findViewById(R.id.mg_basket_state);
        initStateList();
        mgStateAdapter = new MgStateAdapter(getContext(), R.layout.item_basket_state_switch, mStateLists);
        mgStateAdapter.setSelectedPosition(pre_selectedPosition);
        mBasketStateGv.setAdapter(mgStateAdapter);
        // 消息响应
        mBasketStateGv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                pre_selectedPosition = position;
                mgStateAdapter.setSelectedPosition(pre_selectedPosition);
                mHandler.sendEmptyMessage(UPDATE_BASKET_STATEMENT_MSG);  // 更新列表
            }
        });

        // 主体内容部分
        mListRelativeLayout = (RelativeLayout) view.findViewById(R.id.basket_avaliable);
        // 吊篮列表
        mBasketListRecyclerView = (RecyclerView) view.findViewById(R.id.basket_recycler_view);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        mBasketListRecyclerView.setLayoutManager(layoutManager);
        mgBasketStatementList = new ArrayList<>();
        mgBasketStatementClassifiedList = new ArrayList<>();
        initMgBasketStatementList();
        initMgBasketStatementClassifiedList();
        parseMgBasketStatementList(mgBasketStatementList);
        mgBasketStatementAdapter = new MgBasketStatementAdapter(getContext(), mgBasketStatementList);
        mBasketListRecyclerView.setAdapter(mgBasketStatementAdapter);
        // 列表监听
        mgBasketStatementAdapter.setOnItemClickListener(new MgBasketStatementAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                // 点击item响应
                Log.i(TAG, "You have clicked the "+position+" item");
            }

            @Override
            public void onPreAssAndAceptClick(View view, int position) {
                // 点击预安装
                Log.i(TAG, "You have clicked the "+ position+" item's PreAssAndAcept");
                startActivity(new Intent(getActivity(), UploadImageActivity.class));
            }

            @Override
            public void onPreApplyStopClick(View view, int position) {
                // 点击预报停
                Log.i(TAG, "You have clicked the "+ position+" item's PreApplyStop");
                startActivity(new Intent(getActivity(), UploadImageActivity.class));

            }
        });
        // 无吊篮提示信息
        mBlankRelativeLayout = (RelativeLayout) view.findViewById(R.id.basket_no_avaliable);

        // 设置手势监听
        setGestureListener();

        return view;
    }

    /*
     * 重构函数
     */
    // 溢出栏消息响应
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.current_project:
                Log.i(TAG, "You have clicked the project more");
                startActivity(new Intent(getActivity(), ProDetailActivity.class));
                break;
            case R.id.switch_project:
                Log.i(TAG, "You have clicked the switch project");
                showSingleAlertDialog();
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }
    // 初始化溢出栏弹窗
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        //这里设置另外的menu
        menu.clear();
        inflater.inflate(R.menu.area_admin_project_more, menu);

        //通过反射让menu的图标可见
        if (menu != null) {
            if (menu.getClass() == MenuBuilder.class) {
                try {
                    Method m = menu.getClass().getDeclaredMethod("setOptionalIconsVisible", Boolean.TYPE);
                    m.setAccessible(true);
                    m.invoke(menu, true);
                } catch (Exception e) {

                }
            }
        }

        //这一行不能忘，否则看不到图标
        //拿到ActionBar后，可以进行设置
        ((AppCompatActivity) getActivity()).getSupportActionBar();
        super.onCreateOptionsMenu(menu, inflater);
    }

    /*
     * 初始化项目列表
     */
    private void initProjectList(){
        mProjectList = new ArrayList<>();
        mProjectList.add("南京凤凰国际大厦");
        mProjectList.add("万达国际大厦");
        mProjectList.add("德基广场");
        mProjectList.add("瑞都广场");
    }
    /*
     * 初始化状态筛选栏
     */
    private void initStateList(){
        mStateLists = new ArrayList<>();
        mStateLists.add("全部");
        mStateLists.add("待分配");
        mStateLists.add("待安装");
        mStateLists.add("待审核");
        mStateLists.add("使用中");
        mStateLists.add("待报停");
    }

    /*
     * 初始化吊篮
     */
    private void initMgBasketStatementList(){
        mgBasketStatementList.add(new MgBasketStatement("10000001", null, "1"));
        mgBasketStatementList.add(new MgBasketStatement("10000002", null, "2"));
        mgBasketStatementList.add(new MgBasketStatement("10000003", null, "3"));
        mgBasketStatementList.add(new MgBasketStatement("10000004", null, "4"));
        mgBasketStatementList.add(new MgBasketStatement("10000005", null, "5"));
        mgBasketStatementList.add(new MgBasketStatement("10000006", null, "1"));
        mgBasketStatementList.add(new MgBasketStatement("10000007", null, "2"));
        mgBasketStatementList.add(new MgBasketStatement("10000008", null, "3"));
        mgBasketStatementList.add(new MgBasketStatement("10000009", null, "4"));
        mgBasketStatementList.add(new MgBasketStatement("100000010", null, "5"));
        mgBasketStatementList.add(new MgBasketStatement("10000011", null, "5"));
        mgBasketStatementList.add(new MgBasketStatement("10000012", null, "4"));
        mgBasketStatementList.add(new MgBasketStatement("10000013", null, "3"));
        mgBasketStatementList.add(new MgBasketStatement("10000014", null, "2"));
        mgBasketStatementList.add(new MgBasketStatement("10000015", null, "1"));
        mgBasketStatementList.add(new MgBasketStatement("10000016", null, "1"));

    }

    /*
     * 解析列表数据
     */
    private void initMgBasketStatementClassifiedList(){
        mgBasketStatementClassifiedList = new ArrayList<>();
        for(int i=0; i<6;i++){
            mgBasketStatementClassifiedList.add(new ArrayList<MgBasketStatement>());
        }
    }
    private void parseMgBasketStatementList(List<MgBasketStatement> mgBasketStatements){

        for(int i=0; i<mgBasketStatements.size(); i++){
            MgBasketStatement mgBasketStatement = mgBasketStatements.get(i);
            mgBasketStatementClassifiedList.get(Integer.valueOf(mgBasketStatement.getBasketStatement())).
                    add(mgBasketStatement);
        }
        mgBasketStatementClassifiedList.get(0).addAll(mgBasketStatements);
    }

    /*
     * 设置手势监听
     */
    private void setGestureListener(){
        mGestureDetector = new GestureDetector(getActivity(), mGestureListener);
        mGestureDetector.setIsLongpressEnabled(true);
        mGestureDetector.setOnDoubleTapListener(mGestureListener);

        myOnTouchListener = new AreaAdminPrimaryActivity.MyOnTouchListener() {

            @Override
            public boolean onTouch(MotionEvent ev) {
                return mGestureDetector.onTouchEvent(ev);
            }
        };
        ((AreaAdminPrimaryActivity)getActivity()).registerMyOnTouchListener(myOnTouchListener);
    }

    /*
     * 滑动、触摸监听类
     */
    public class SVCGestureListener implements GestureDetector.OnGestureListener,
            GestureDetector.OnDoubleTapListener {

        @Override
        public boolean onDown(MotionEvent e) {
            return false;
        }

        @Override
        public void onShowPress(MotionEvent e) {

        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            return false;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2,float distanceX, float distanceY) {
            return false;
        }

        @Override
        public void onLongPress(MotionEvent e) {
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,float velocityY) {
            if(Math.abs(e1.getX()-e2.getX()) > Math.abs(e1.getY()-e2.getY())) {
                // 水平滑动的距离大于竖直滑动的距离
                if (e1.getX() - e2.getX() > FLIP_DISTANCE) {
                    Log.i(TAG, "向左滑...");
                    int tmp_position = pre_selectedPosition + 1;
                    pre_selectedPosition = (tmp_position < mStateLists.size()) ? tmp_position : (mStateLists.size() - 1);
                    mgStateAdapter.setSelectedPosition(pre_selectedPosition);
                    mHandler.sendEmptyMessage(UPDATE_BASKET_STATEMENT_MSG);  // 更新列表
                    return true;
                }
                if (e2.getX() - e1.getX() > FLIP_DISTANCE) {
                    Log.i(TAG, "向右滑...");
                    int tmp_position = pre_selectedPosition - 1;
                    pre_selectedPosition = (tmp_position > 0) ? tmp_position : 0;
                    mgStateAdapter.setSelectedPosition(pre_selectedPosition);
                    mHandler.sendEmptyMessage(UPDATE_BASKET_STATEMENT_MSG);  // 更新列表
                    return true;
                }
            }else {
                if (e1.getY() - e2.getY() > FLIP_DISTANCE) {
                    Log.i(TAG, "向上滑...");
                    return true;
                }
                if (e2.getY() - e1.getY() > FLIP_DISTANCE) {
                    Log.i(TAG, "向下滑...");
                    return true;
                }
            }

            Log.d(TAG, e2.getX() + " " + e2.getY());

            return false;
        }
        //点击的时候执行
        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            //          toolbar.changeVisibility();
            if(state == State.VISIBLE) {

            } else {

            }
            return true;
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            return false;
        }

        @Override
        public boolean onDoubleTapEvent(MotionEvent e) {
            return false;
        }
    }

    /*
     * UI 更新类
     */
    /*
     * 弹出项目单选框
     */
    public void showSingleAlertDialog(){
        final AlertDialog.Builder alertBuilder = new AlertDialog.Builder(getContext());
        alertBuilder.setTitle("这是单选框");
        alertBuilder.setSingleChoiceItems(listToArray(mProjectList), currentSelectedProject, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int position) {
                tmpSelectedProject = position;
            }
        });

        alertBuilder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                currentSelectedProject = tmpSelectedProject;
                mHandler.sendEmptyMessage(UPDATE_PROJECT_AND_BASKET_MSG);
                mSelectProjectDialog.dismiss();
            }
        });

        alertBuilder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                mSelectProjectDialog.dismiss();
            }
        });

        mSelectProjectDialog = alertBuilder.create();
        mSelectProjectDialog.show();
    }

    /*
     * 生命周期函数
     */
    // 销毁
    @Override
    public void onDestroy() {
        super.onDestroy();
        ((AreaAdminPrimaryActivity) getActivity()).unregisterMyOnTouchListener(myOnTouchListener);
    }

    /*
     * 工具类
     */
    private String[] listToArray(List<String> list ){
        String[] strings = new String[list.size()];
        list.toArray(strings);
        return strings;
    }

}
