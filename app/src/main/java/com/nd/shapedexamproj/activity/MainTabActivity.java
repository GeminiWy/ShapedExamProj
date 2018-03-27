package com.nd.shapedexamproj.activity;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.*;

import com.nd.shapedexamproj.App;
import com.nd.shapedexamproj.AuthorityManager;
import com.nd.shapedexamproj.R;
import com.nd.shapedexamproj.fragment.InnerMyFragment;
import com.nd.shapedexamproj.fragment.MoreFragment;
import com.nd.shapedexamproj.fragment.MsgFragment;
import com.nd.shapedexamproj.fragment.MyFragment;
import com.nd.shapedexamproj.fragment.PlaygroundFragment;
import com.nd.shapedexamproj.fragment.XKCourseFragment;
import com.nd.shapedexamproj.util.AnimationUtil;
import com.nd.shapedexamproj.view.CloseableSlideMenu;
import com.tming.common.BaseFragmentActivity;
import com.tming.common.util.Helper;
import com.tming.common.util.Log;

/**
 * 项目的主Activity，所有的Fragment都嵌入在这里。
 *
 * @author zll create in 2014-3-30
 */
public class MainTabActivity extends BaseFragmentActivity {
    private static final String TAG = MainTabActivity.class.getSimpleName();
    public static final  String TAB_CHANGE="TABCHANGE";
    public static CloseableSlideMenu sCanCloseSlideMenu;

    private FrameLayout main_tab_frame;
    private RelativeLayout main_tab_classroom_lay;
    private LinearLayout main_tab_msg_lay, main_tab_playground_lay, main_tab_user_lay,main_tab_more_lay;
    private ImageView main_tab_classroom_img, main_tab_msg_img, main_tab_playground_img,main_tab_user_img, main_tab_more_img;
    private TextView main_tab_classroom_tv, main_tab_msg_tv, main_tab_playground_tv,main_tab_user_tv, main_tab_more_tv;
    private Fragment courseFragment, msgFragment, myFragment, moreFragment;
    private PlaygroundFragment playGroundFragment;
    // int bottom_text_color = getResources().getColor(R.color.black);
    // int bottom_text_color_on = getResources().getColor(R.color.title_green);

    //用户导航界面
    private View mNavigationCoverView;
    /**
     * 用于对Fragment进行管理
     */
    private FragmentManager fragmentManager;
    private BroadcastReceiver mTabChangeReciver=new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //跳转tab 界面
            if (TAB_CHANGE.equals(intent.getAction())) {
                setTabSelection(intent.getIntExtra("tabIndex",0));
            }
        }
    };

    // int bottom_text_color = getResources().getColor(R.color.black);
    // int bottom_text_color_on = getResources().getColor(R.color.title_green);

//	/**
//	 * 用于对Fragment进行管理
//	 */
//	private FragmentManager fragmentManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.e(TAG, "onCreate xxx");

        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.maintab_activity);
        // 初始化布局元素
        initViews();

        fragmentManager = getSupportFragmentManager();

        // 第一次启动时选中第0个tab
        //CoursingManager.getInstance();
        setTabSelection(0);
        //注册tab跳转广播监听
        IntentFilter filter = new IntentFilter();
        filter.addAction(TAB_CHANGE);
        Helper.registLocalReciver(this, mTabChangeReciver, filter);
        mNavigationCoverView.setVisibility(View.GONE);
        mNavigationCoverView.setOnClickListener(new CoverClickListener());
        /*initVLC();*/
    }
    
    /*private void initVLC() {
        if (!LibVlcUtil.hasCompatibleCPU(this)) 
        {
            Log.i(TAG, "current cpu cannot support vlc;"+LibVlcUtil.getErrorMsg());
        }
        try {
            LibVLC vlc= Util.getLibVlcInstance();
            vlc.setNetworkCaching(10);
            vlc.init(this);
            
        } catch (LibVlcException e) {
            e.printStackTrace();
        }
    }*/
    
    /**
     *
     *@Title:  initAuthoriy
     *@Description: 初始化权限
     *@Since: 2014-5-19下午5:14:02
     */
    public void initAuthoriy(){
        Log.e(TAG, "initAuthoriy :" + App.getUserType());
//	    if (Constants.USER_TYPE == Constants.USER_TYPE_INNER){
//	    	main_tab_user_lay.setVisibility(View.GONE);
//	    }else {
//	    	main_tab_user_lay.setVisibility(View.VISIBLE);
//	    }

    }

    @Override
    protected void onResume() {
        super.onResume();
        initAuthoriy();
    }

    @Override
    public void finish() {
        super.finish();
        //AppManager.getAppManager().addActivity(this);
        //		Constants.activity_list.remove(this);

        //AppManager.getAppManager().removeActivity(this);
    }

    @Override
    protected void onDestroy() {
        Log.d("MainTabActivity","-------------------Destory!!!--------------------");
        Helper.unregistLocalReciver(App.getAppContext(),mTabChangeReciver);
        super.onDestroy();
    }

    private void initViews() {
        main_tab_classroom_lay = (RelativeLayout) findViewById(R.id.main_tab_classroom_lay);
        main_tab_msg_lay = (LinearLayout) findViewById(R.id.main_tab_msg_lay);
        main_tab_playground_lay = (LinearLayout) findViewById(R.id.main_tab_playground_lay);
        main_tab_user_lay = (LinearLayout) findViewById(R.id.main_tab_user_lay);
        main_tab_more_lay = (LinearLayout) findViewById(R.id.main_tab_more_lay);

        main_tab_classroom_img = (ImageView) findViewById(R.id.main_tab_classroom_img);
        main_tab_classroom_tv = (TextView) findViewById(R.id.main_tab_classroom_tv);

        main_tab_msg_img = (ImageView) findViewById(R.id.main_tab_msg_img);
        main_tab_msg_tv = (TextView) findViewById(R.id.main_tab_msg_tv);

        main_tab_playground_img = (ImageView) findViewById(R.id.main_tab_playground_img);
        main_tab_playground_tv = (TextView) findViewById(R.id.main_tab_playground_tv);

        main_tab_user_img = (ImageView) findViewById(R.id.main_tab_user_img);
        main_tab_user_tv = (TextView) findViewById(R.id.main_tab_user_tv);


        main_tab_more_img = (ImageView) findViewById(R.id.main_tab_more_img);
        main_tab_more_tv = (TextView) findViewById(R.id.main_tab_more_tv);

        mNavigationCoverView=(View) findViewById(R.id.navigation_view);
        mNavigationCoverView.setVisibility(View.GONE);


        // 添加监听
        main_tab_classroom_lay.setOnClickListener(new TabClickListener());
        main_tab_msg_lay.setOnClickListener(new TabClickListener());
        main_tab_playground_lay.setOnClickListener(new TabClickListener());
        main_tab_user_lay.setOnClickListener(new TabClickListener());
        main_tab_more_lay.setOnClickListener(new TabClickListener());
    }

    /**
     * <p>导航按钮点击操作</p>
     */
    private class CoverClickListener implements OnClickListener{

        @Override
        public void onClick(final View view) {

            if (mNavigationCoverView.getVisibility()!= View.GONE){
                AnimationUtil.alpha(view,1000);
            }
            //mNavigationCoverView.setVisibility(View.GONE);
            //移除遮罩
            ((ViewGroup)view.getParent()).removeView(view);
        }
    }

    private class TabClickListener implements OnClickListener {

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.main_tab_classroom_lay:
                    Log.e("mainTab", "classroom");
                    setTabSelection(0);
                    break;
                case R.id.main_tab_msg_lay:
                    setTabSelection(1);
                    break;
                case R.id.main_tab_playground_lay:
                    setTabSelection(2);
                    break;
                case R.id.main_tab_user_lay:
                    setTabSelection(3);
                    break;
                case R.id.main_tab_more_lay:
                    setTabSelection(4);
                    break;
            }
        }
    }

    /**
     * 根据传入的index参数来设置选中的tab页。
     *
     * @param index
     *            每个tab页对应的下标。0表示课堂，1表示消息，2表示操场，2表示动态，3表示设置。
     */
    private void setTabSelection(int index) {
        // 设置侧滑菜单只有在第一个选项卡才能滑动
        sCanCloseSlideMenu.setIsCloseSlide(index != 0);
        // 每次选中之前先清楚掉上次的选中状态
        clearSelection();
        // 开启一个Fragment事务
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        // 先隐藏掉所有的Fragment，以防止有多个Fragment显示在界面上的情况
        hideFragments(transaction);
        switch (index) {
            case 0:
                // 当点击了课堂tab时，改变控件的图片和文字颜色
                main_tab_classroom_img.setImageResource(R.drawable.navbar_icon_classroom_on);
                main_tab_classroom_tv.setTextColor(Color.parseColor("#23bd8b"));
                Log.e(TAG, "courseFragment = " + courseFragment);
                if (courseFragment == null) {
                    // 如果CourseFragment为空，则创建一个并添加到界面上
                    /*if (AuthorityManager.getInstance().isStudentAuthority()){//TODO 形考只有一个选课界面
                        //courseFragment = new CourseFragment();
                        //修改学生的课堂界面
                        courseFragment = new XKCourseFragment();
                    } else if (AuthorityManager.getInstance().isInnerAuthority()){
                        courseFragment = new InnerCourseFragment();
                    } else {
                        courseFragment = new TeacherCourseFragment();
                    }*/
                    courseFragment = new XKCourseFragment();
                    transaction.add(R.id.main_tab_frame, courseFragment);
                } else {
                    // 如果CourseFragment不为空，则直接将它显示出来
                    transaction.show(courseFragment);
                }
                break;
            case 1:
                // 当点击了消息tab时，改变控件的图片和文字颜色
                main_tab_msg_img.setImageResource(R.drawable.navbar_icon_message_on);
                main_tab_msg_tv.setTextColor(Color.parseColor("#23bd8b"));
                if (msgFragment == null) {
                    // 如果MsgFragment为空，则创建一个并添加到界面上
                    msgFragment = new MsgFragment();
                    transaction.add(R.id.main_tab_frame, msgFragment);
                } else {
                    // 如果MsgFragment不为空，则直接将它显示出来
                    transaction.show(msgFragment);
                    if (!msgFragment.isHidden()) {
                        //点击tab切换，消息通讯录刷新，用于用户信息更新操作
                        ((MsgFragment)msgFragment).refreshContactPage();
                    }
                }
                
                break;
            case 2:
                // 当点击了动态tab时，改变控件的图片和文字颜色
                main_tab_playground_img.setImageResource(R.drawable.navbar_icon_playground_on);
                main_tab_playground_tv.setTextColor(Color.parseColor("#23bd8b"));

                if (playGroundFragment == null) { //
                    // 如果NewsFragment为空，则创建一个并添加到界面上
                    playGroundFragment = new PlaygroundFragment();
                    transaction.add(R.id.main_tab_frame, playGroundFragment);
                } else { // 如果NewsFragment不为空，则直
                // 接将它显示出来
                    transaction.show(playGroundFragment);
                    if (!playGroundFragment.isHidden()) {
                        playGroundFragment.scrollToFirstItem();
                    }
                }
                break;
            case 3:
                // 当点击了“我的”tab时，改变控件的图片和文字颜色
                main_tab_user_img.setImageResource(R.drawable.navbar_icon_user_on);
                main_tab_user_tv.setTextColor(Color.parseColor("#23bd8b"));
                if (myFragment == null) {
                    // 如果SettingFragment为空，则创建一个并添加到界面上
                    if (AuthorityManager.getInstance().isInnerAuthority()) {
                        myFragment = new InnerMyFragment();
                    } else {
                        myFragment = new MyFragment();
                    }
                    transaction.add(R.id.main_tab_frame, myFragment);
                } else {
                    // 如果SettingFragment不为空，则直接将它显示出来
                    transaction.show(myFragment);
                    if (!myFragment.isHidden() && !AuthorityManager.getInstance().isInnerAuthority()) {
                        ((MyFragment) myFragment).refreshMyFragment();
                    }
                }
                break;
            default:
                // 当点击了“更多”tab时，改变控件的图片和文字颜色
                main_tab_more_img.setImageResource(R.drawable.navbar_icon_more_on);
                main_tab_more_tv.setTextColor(Color.parseColor("#23bd8b"));
                if (moreFragment == null) {
                    // 如果SettingFragment为空，则创建一个并添加到界面上
                    moreFragment = new MoreFragment();
                    transaction.add(R.id.main_tab_frame, moreFragment);
                } else {
                    // 如果SettingFragment不为空，则直接将它显示出来
                    transaction.show(moreFragment);
                }
                break;
        }
        transaction.commitAllowingStateLoss();
    }

    /**
     * 清除掉所有的选中状态。
     */
    private void clearSelection() {
        main_tab_classroom_img.setImageResource(R.drawable.navbar_icon_classroom);
        main_tab_classroom_tv.setTextColor(Color.parseColor("#888888"));

        main_tab_msg_img.setImageResource(R.drawable.navbar_icon_message);
        main_tab_msg_tv.setTextColor(Color.parseColor("#888888"));

        main_tab_playground_img.setImageResource(R.drawable.navbar_icon_playground);
        main_tab_playground_tv.setTextColor(Color.parseColor("#888888"));

        main_tab_user_img.setImageResource(R.drawable.navbar_icon_user);
        main_tab_user_tv.setTextColor(Color.parseColor("#888888"));

        main_tab_more_img.setImageResource(R.drawable.navbar_icon_more);
        main_tab_more_tv.setTextColor(Color.parseColor("#888888"));
    }

    /**
     * 将所有的Fragment都置为隐藏状态。
     *
     * @param transaction
     *            用于对Fragment执行操作的事务
     */
    private void hideFragments(FragmentTransaction transaction) {
        if (courseFragment != null) {
            transaction.hide(courseFragment);
        }
        if (msgFragment != null) {
            transaction.hide(msgFragment);
        }
        if (playGroundFragment != null) {
            transaction.hide(playGroundFragment);
        }
        if (myFragment != null) {
            transaction.hide(myFragment);
        }
        if (moreFragment != null) {
            transaction.hide(moreFragment);
        }
    }


}
