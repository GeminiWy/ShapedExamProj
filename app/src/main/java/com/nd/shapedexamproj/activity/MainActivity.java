package com.nd.shapedexamproj.activity;


import android.app.ActivityGroup;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.nd.shapedexamproj.App;
import com.nd.shapedexamproj.AuthorityManager;
import com.nd.shapedexamproj.R;
import com.nd.shapedexamproj.activity.course.CourseListActivity;
import com.nd.shapedexamproj.activity.course.SelectCourseActivity;
import com.nd.shapedexamproj.activity.major.MajorDetailActivity;
import com.nd.shapedexamproj.im.manager.UserManager;
import com.nd.shapedexamproj.util.Constants;
import com.nd.shapedexamproj.view.CloseableSlideMenu;
import com.nd.shapedexamproj.view.LeftMenuLayout;
import com.tming.common.util.Helper;
import com.tming.common.util.Log;
import com.tming.common.view.slidemenu.SlideMenu;
import com.tming.common.view.slidemenu.SlideMenu.LayoutParams;
import com.tming.common.view.slidemenu.SlideMenu.OnSlideStateChangeListener;
import com.umeng.analytics.MobclickAgent;

/**
 * 双向滑动布局
 *
 * @author zll create in 2014-3-11
 */
public class MainActivity extends ActivityGroup {

    private static final String LOG_TAG = "MainActivity";

    /**
     * 双向滑动菜单布局
     */
    private CloseableSlideMenu mSlideMenu;
    // private LinearLayout layout;
    private LeftMenuLayout leftMenuLayout;
    /* private RightMenuLayout rightMenuLayout; */
    private TextView head_cate;
    private View headView, views;
    //导航栏左侧返回按钮
    private ImageView mHeadLeftButton;

    private boolean isCurrentView = true; // 是否是当前页面,如果否，则按返回键先移除当前页上的那个页面。
    private boolean isFromLeft = false;   // 是否来自于左界面

    View intentView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        IntentFilter filter = new IntentFilter();
        filter.addAction("allcourse");
        filter.addAction("allcourse_noslider");
        filter.addAction("nondegreegoodcourse");
        filter.addAction("coursecategory");
        filter.addAction("backToMain");
        filter.addAction("courselist");
        filter.addAction("professionaldetail");
        filter.addAction("backToShowLeftMenu");
        filter.addAction("openLeftMenu");
        // registerReceiver(mBroadcastReceiver, filter);
        Helper.registLocalReciver(this, mBroadcastReceiver, filter);
        //检查更新
        /*VersionUpdate.checkHasVersionUpdate(Constants.VERSION_CHECK,
                new AutoVersionCheckUpdateAdapter(this));*/
        AuthorityManager au = AuthorityManager.getInstance();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onContentChanged() {
        super.onContentChanged();
        Log.e(LOG_TAG, "onContentChanged");
        mSlideMenu = (CloseableSlideMenu) findViewById(R.id.bidir_sliding_layout);
        mSlideMenu.setPrimaryShadowWidth(2);
        mSlideMenu.setSecondaryShadowWidth(2);

        Intent intent = new Intent(MainActivity.this, MainTabActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        MainTabActivity.sCanCloseSlideMenu = mSlideMenu;
        views = MainActivity.this.getLocalActivityManager()
                .startActivity("mainActivity", intent).getDecorView();
        mSlideMenu.addView(views, new LayoutParams(android.view.ViewGroup.LayoutParams.MATCH_PARENT,android.view.ViewGroup.LayoutParams.MATCH_PARENT, LayoutParams.ROLE_CONTENT));

        //非游客模式
        if (false && !AuthorityManager.getInstance().isInnerAuthority()) { // 关闭左侧抽屉功能
            getLayoutInflater().inflate(R.layout.left_menu_layout_shadow, mSlideMenu, true);//

            //left_menu_layout
			/*
			 * getLayoutInflater().inflate(R.layout.right_menu_layout, mSlideMenu,
			 * true);
			 */

        }

        headView = LayoutInflater.from(MainActivity.this)
                .inflate(R.layout.common_head_layout, null);
        initHeadViewListener(views);

        mSlideMenu.setOnSlideStateChangeListener(new OnSlideStateChangeListener() {

            @Override
            public void onSlideStateChange(int slideState) {
                if (mHeadLeftButton != null) {
                    if (slideState == SlideMenu.STATE_OPEN_LEFT) {
                        mHeadLeftButton.setImageResource(R.drawable.menu_leftbutton_clicked);
                    } else if (slideState == SlideMenu.STATE_CLOSE) {
                        mHeadLeftButton.setImageResource(R.drawable.menu_leftbutton);
                    }
                }
            }

            @Override
            public void onSlideOffsetChange(float offsetPercent) {

            }
        });
    }

    @Override
    protected void onDestroy() {

        Log.d(LOG_TAG,"---------------------Destory!!!-------------------");
        super.onDestroy();
        // unregisterReceiver(mBroadcastReceiver);
        Helper.unregistLocalReciver(App.getAppContext(), mBroadcastReceiver);
		/*
		 * leftMenuLayout.cancelRegisterReceiver();
		 * rightMenuLayout.CancelRegisterReceiver();
		 */
    }

    private void initHeadViewListener(View views) {

        //初始化设定返回按钮
        mHeadLeftButton = (ImageView) views.findViewById(R.id.list_head_left);
        mHeadLeftButton.setVisibility(View.INVISIBLE); // 隐藏左侧抽屉
        View head_right = views.findViewById(R.id.common_main_head_new_lay_select_course_tv);

        if (mHeadLeftButton == null) {
            return;
        }
        head_cate = (TextView) views.findViewById(R.id.list_head_tv);

        mHeadLeftButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                mSlideMenu.open(false, true);
            }
        });
        head_right.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
				/* mSlideMenu.open(true, true); */
                // 跳转到下载界面
//                startActivity(new Intent(MainActivity.this, DownloadManagerActivity.class));
                startActivity(new Intent(MainActivity.this, SelectCourseActivity.class));
            }
        });
    }

    /**
     * 初始化注册广播
     */
    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, final Intent intent) {
            isCurrentView = false;
            if ("coursecategory".equals(intent.getAction())) {
                stepToActivityFromLeftMenu(CourseListActivity.class);
            } else if ("allcourse".equals(intent.getAction())) {
                //左边menu打开的情况进入 CourseListActivity
                stepToActivityFromLeftMenu(CourseListActivity.class);
            } else if ("allcourse_noslider".equals(intent.getAction())) {
                //左边menu没有打开的情况进入 CourseListActivity
                stepToActivity(CourseListActivity.class);
            } else if ("nondegreegoodcourse".equals(intent.getAction())) {
                stepToActivityFromLeftMenu(CourseListActivity.class);
            } else if ("courselist".equals(intent.getAction())) {
                stepToActivityFromLeftMenu(CourseListActivity.class);
            } else if ("professionaldetail".equals(intent.getAction())) {
                stepToActivityFromLeftMenu(MajorDetailActivity.class);
            } else if ("backToMain".equals(intent.getAction())) {
                backToMain();
            } else if ("backToShowLeftMenu".equals(intent.getAction())) {
                backToLeftMenu();
            } else if ("openLeftMenu".equals(intent.getAction())) {
                mSlideMenu.open(false, true);
            }
        }
    };

    /**
     *
     * <p> 从左界面跳转到Activity </p>
     *
     * @param cls 跳转的Activity
     */
    private void stepToActivityFromLeftMenu(Class<?> cls) {
        isFromLeft = true;
        Constants.IS_LEFTMENU_VISIABLE=isFromLeft;
        Intent intent = new Intent(MainActivity.this, cls)
                .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intentView = MainActivity.this.getLocalActivityManager()
                .startActivity(cls.getName(), intent).getDecorView();//cls.getName()
        mSlideMenu.addView(intentView,new LayoutParams(android.view.ViewGroup.LayoutParams.MATCH_PARENT,android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                        LayoutParams.ROLE_CONTENT));
        mSlideMenu.close(true);
    }

    /**
     *
     * <p> 跳转到指定的Activity</P>
     *
     * @param cls 跳转的Activity
     */
    private void stepToActivity(Class<?> cls) {
        isFromLeft = false;
        Constants.IS_LEFTMENU_VISIABLE=isFromLeft;
        Intent intent = new Intent(MainActivity.this, cls)
                .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intentView = MainActivity.this.getLocalActivityManager()
                .startActivity(cls.getName(), intent).getDecorView();
        mSlideMenu.addView(intentView,
                new LayoutParams(android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                        android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                        LayoutParams.ROLE_CONTENT));
        mSlideMenu.close(true);
    }

    /**
     * <p> 返回时显示左侧菜单</p>
     */
    private void backToLeftMenu() {
        Intent course = new Intent(MainActivity.this, MainTabActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        View views = MainActivity.this.getLocalActivityManager().startActivity("mainActivity", course).getDecorView();

        mSlideMenu.addView(views, new LayoutParams(
                android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                android.view.ViewGroup.LayoutParams.MATCH_PARENT, LayoutParams.ROLE_CONTENT));
        mSlideMenu.open(false, true);
        initHeadViewListener(views);
    }

    /**
     * <p> 收起左侧边框，跳转回到主界面</p>
     */
    private void backToMain() {
        Intent course = new Intent(MainActivity.this, MainTabActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        View views = MainActivity.this.getLocalActivityManager()
                .startActivity("mainActivity", course).getDecorView();
        mSlideMenu.addView(views, new LayoutParams(
                android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                android.view.ViewGroup.LayoutParams.MATCH_PARENT, LayoutParams.ROLE_CONTENT));
        mSlideMenu.close(true);
        initHeadViewListener(views);
    }

    private long lastPressBackTime = 0;
    @Override
    public void onBackPressed() {
        Log.d(LOG_TAG, " - onBackPressed - ");
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (!isCurrentView) {
            if (!isFromLeft) {
                backToMain();
            } else {
                backToLeftMenu();
            }
            isFromLeft = false;
            isCurrentView = true;
            return false;
        }
        if (!mSlideMenu.isOpen() && keyCode == KeyEvent.KEYCODE_BACK && isCurrentView) {
            if (AuthorityManager.getInstance().isInnerAuthority()) {
                finish() ;
                return true;
            }
            if (System.currentTimeMillis() - lastPressBackTime < 3000) {
                UserManager.disconnectAccount();
                App.cleanWifiNotification();
                MobclickAgent.onKillProcess(App.getAppContext()) ;//添加友盟统计
                System.exit(0);
                return true;
            }
            Helper.ToastUtil(this, getString(R.string.exit_hint));
            lastPressBackTime = System.currentTimeMillis();
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_MENU && !mSlideMenu.ismIsCloseSlide()) {
            if (mSlideMenu.isOpen()) {
                mSlideMenu.close(true);
            } else {
                mSlideMenu.open(false, true);
            }
            return true;
        } else {
            Log.d(LOG_TAG, " - else - ");
            return super.onKeyDown(keyCode, event);
        }
    }

	/*
	 * private void addheadView(View views){ View head =
	 * views.findViewById(R.id.course_actionbar); if(head == null) return;
	 * ImageView head_left = (ImageView) head.findViewById(R.id.list_head_left);
	 * head_left.setImageResource(R.drawable.actionbar_icon_back);
	 * head.findViewById(R.id.list_head_right).setVisibility(View.GONE);
	 * head_cate = (TextView) head.findViewById(R.id.list_head_tv);
	 * head_cate.setText("经济类课程");
	 *
	 *
	 * head_left.setOnClickListener(new OnClickListener() {
	 *
	 * @Override public void onClick(View v) { Log.d(LOG_TAG, "head_left");
	 * mSlideMenu.open(false, true); } }); }
	 */

}