package com.example.zzx.zbar_demo.fragment.areaAdmin;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
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

import com.example.zzx.zbar_demo.R;
import com.example.zzx.zbar_demo.activity.ProDetailActivity;
import com.example.zzx.zbar_demo.activity.areaAdmin.AreaAdminPrimaryActivity;
import com.example.zzx.zbar_demo.adapter.areaAdmin.MgBasketStatementAdapter;
import com.example.zzx.zbar_demo.adapter.areaAdmin.MgStateAdapter;
import com.example.zzx.zbar_demo.entity.MgBasketStatement;

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
    private final static int UPDATE_RECYCLER_VIEW_MSG = 1;

    // 控件
    // 顶部导航栏
    private Toolbar mProjectMoreTb;
    // 吊篮状态选择栏
    private GridView mBasketStateGv; // 吊篮状态
    private List<String> mStateLists; // 状态名称
    private MgStateAdapter mgStateAdapter; //适配器
    private int pre_selectedPosition = 0;
    // 主体内容部分
    private RelativeLayout mBlankRelativeLayout;
    private RecyclerView mBasketListRecyclerView;
    private List<MgBasketStatement> mgBasketStatementList;  // 所有数据
    private List<List<MgBasketStatement>> mgBasketStatementClassifiedList;  // 分类数据
    private MgBasketStatementAdapter mgBasketStatementAdapter;

    // 上下左右滑动监听
    private static enum State{ VISIBLE,ANIMATIONING,INVISIBLE,}
    private State state = State.INVISIBLE;
    protected static final float FLIP_DISTANCE = 50;
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
                case UPDATE_RECYCLER_VIEW_MSG:  // 更新列表
                    mgBasketStatementList.clear();
                    mgBasketStatementList.addAll(mgBasketStatementClassifiedList.get(pre_selectedPosition));
                    mgBasketStatementAdapter.notifyDataSetChanged();
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
        mProjectMoreTb.setTitle("项目");
        ((AppCompatActivity) getActivity()).setSupportActionBar(mProjectMoreTb);

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
                mHandler.sendEmptyMessage(UPDATE_RECYCLER_VIEW_MSG);  // 更新列表
            }
        });

        // 主体内容部分
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

            }

            @Override
            public void onPreApplyStopClick(View view, int position) {
                // 点击预报停
                Log.i(TAG, "You have clicked the "+ position+" item's PreApplyStop");

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
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.current_project:
                Log.i(TAG, "You have clicked the project more");
                startActivity(new Intent(getActivity(), ProDetailActivity.class));
                break;
            case R.id.switch_project:
                Log.i(TAG, "You have clicked the switch project");
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }
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
            if (e1.getX() - e2.getX() > FLIP_DISTANCE) {
                Log.i(TAG, "向左滑...");
                int tmp_position = pre_selectedPosition + 1;
                pre_selectedPosition = (tmp_position < mStateLists.size()) ? tmp_position : (mStateLists.size()-1);
                mgStateAdapter.setSelectedPosition(pre_selectedPosition);
                mHandler.sendEmptyMessage(UPDATE_RECYCLER_VIEW_MSG);  // 更新列表
                return true;
            }
            if (e2.getX() - e1.getX() > FLIP_DISTANCE) {
                Log.i(TAG, "向右滑...");
                int tmp_position = pre_selectedPosition - 1;
                pre_selectedPosition = (tmp_position > 0) ? tmp_position : 0;
                mgStateAdapter.setSelectedPosition(pre_selectedPosition);
                mHandler.sendEmptyMessage(UPDATE_RECYCLER_VIEW_MSG);  // 更新列表
                return true;
            }
            if (e1.getY() - e2.getY() > FLIP_DISTANCE) {
                Log.i(TAG, "向上滑...");
                return true;
            }
            if (e2.getY() - e1.getY() > FLIP_DISTANCE) {
                Log.i(TAG, "向下滑...");
                return true;
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
     * 生命周期函数
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        ((AreaAdminPrimaryActivity) getActivity()).unregisterMyOnTouchListener(myOnTouchListener);
    }

}
