package com.nd.shapedexamproj.activity.course;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.nd.shapedexamproj.R;
import com.nd.shapedexamproj.view.course.SelectCourseListFragment;
import com.tming.common.BaseFragmentActivity;

/**
 * 选课界面
 * Created by yusongying on 2015/3/3.
 */
public class SelectCourseActivity extends BaseFragmentActivity implements View.OnClickListener,ViewPager.OnPageChangeListener {

    private ViewGroup[] mTabs = new ViewGroup[2];
    private ViewPager mViewPage;

    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        setContentView(R.layout.select_course_activity);
        initComponent();
    }

    private void selectTab(int position) {
        for (int i = 0; i < mTabs.length; i++) {
            if (i == position) {
                mTabs[i].setSelected(true);
                mTabs[i].setClickable(false);
                mTabs[i].getChildAt(1).setVisibility(View.VISIBLE);
            } else {
                mTabs[i].setSelected(false);
                mTabs[i].getChildAt(1).setVisibility(View.INVISIBLE);
                mTabs[i].setClickable(true);
            }
        }
    }

    public void initComponent() {
        mTabs[0] = (ViewGroup) findViewById(R.id.select_course_activity_tab_major_course_lay);
        mTabs[1] = (ViewGroup) findViewById(R.id.select_course_activity_tab_other_course_lay);
        selectTab(0);

        mTabs[0].setOnClickListener(this);
        mTabs[1].setOnClickListener(this);
        findViewById(R.id.commonheader_left_iv).setOnClickListener(this);
        findViewById(R.id.commonheader_right_ll).setVisibility(View.GONE);
        TextView titleTv = (TextView) findViewById(R.id.commonheader_title_tv);
        titleTv.setText("选课");

        mViewPage = (ViewPager) findViewById(R.id.select_course_activity_vp);
        mViewPage.setOnPageChangeListener(this);
        mViewPage.setAdapter(new FragmentStatePagerAdapter(getSupportFragmentManager()) {
            @Override
            public Fragment getItem(int i) {
                return SelectCourseListFragment.newInstance(i);
            }

            @Override
            public int getCount() {
                return 2;
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.select_course_activity_tab_major_course_lay:
                selectTab(0);
                mViewPage.setCurrentItem(0);
                break;
            case R.id.select_course_activity_tab_other_course_lay:
                selectTab(1);
                mViewPage.setCurrentItem(1);
                break;
            case R.id.commonheader_left_iv:
                finish();
                break;
        }
    }

    @Override
    public void onPageScrolled(int i, float v, int i1) {

    }

    @Override
    public void onPageSelected(int i) {
        selectTab(i);
    }

    @Override
    public void onPageScrollStateChanged(int i) {
    }
}
