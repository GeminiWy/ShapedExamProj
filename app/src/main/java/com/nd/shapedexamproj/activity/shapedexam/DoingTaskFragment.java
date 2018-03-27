package com.nd.shapedexamproj.activity.shapedexam;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import com.nd.shapedexamproj.App;
import com.nd.shapedexamproj.R;
import com.nd.shapedexamproj.model.xkhomework.XKHomework;
import com.nd.shapedexamproj.model.xkhomework.XKSubject;
import com.nd.shapedexamproj.view.xkhomework.XKINextable;
import com.nd.shapedexamproj.view.xkhomework.XKSubjectView;

import java.util.ArrayList;
import java.util.List;

/**
 * 做作业Fragment
 * Created by yusongying on 2015/1/19.
 */
public class DoingTaskFragment extends Fragment implements View.OnClickListener, ViewPager.OnPageChangeListener, XKINextable {

    protected XKHomework homework;
    private List<XKSubjectView> destroyViews = new ArrayList<XKSubjectView>();
    private List<XKSubjectView> showingViews = new ArrayList<XKSubjectView>();
    private ViewPager mViewPager = null;
    private TextView mTitleTv;
    private SubjectAdapter mAdapter = null;
    private int mCurrentShowSubjectType;
    private boolean editable = true;//设置全部题目是否可以编辑
    private boolean isPartEditable;//设置部分题目是否可编辑

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.doing_task_fragment, null);

        mViewPager = (ViewPager) view.findViewById(R.id.doing_homework_frame_content_vp);
        mTitleTv = (TextView) view.findViewById(R.id.doing_homework_frame_title_tv);

        initData();
        return view;
    }

    public void setHomework(XKHomework homework) {
        this.homework = homework;
    }

    public void initData() {
        mAdapter = new SubjectAdapter();
        mViewPager.setAdapter(mAdapter);
        mViewPager.setOnPageChangeListener(this);
        mAdapter.notifyDataSetChanged();
        mCurrentShowSubjectType = -1;

        // 跳转到未做的第一题
        if (homework != null) {
            int index = homework.getFirstUndoSubjectIndex();
            mViewPager.setCurrentItem(index);
            onPageSelected(index);
        }
    }



    @Override
    public void onClick(View v) {

    }

    @Override
    public void onPageScrolled(int i, float v, int i1) {

    }

    @Override
    public void onPageSelected(int i) {
        if (homework == null || homework.getSubjectSize() == 0) {
            return;
        }
        XKSubject showingSubject = homework.getSubjectAtPageIndex(i);
        // 如果题目类型变化
        int subjectType = showingSubject.getType();
        if (mCurrentShowSubjectType != subjectType) {
            mTitleTv.setText(mAdapter.getPageTitle(i));
            mCurrentShowSubjectType = subjectType;
        }

        hideKeyborad();
    }

    private void hideKeyborad() {
        InputMethodManager inputMethodManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(mTitleTv.getWindowToken(), 0);
    }
    @Override
    public void onPageScrollStateChanged(int i) {

    }

    @Override
    public void showNext(XKSubject currentShowSubject) {
        int curItem = mViewPager.getCurrentItem();
        if (curItem < homework.getPageSize() - 1) {
            mViewPager.setCurrentItem(curItem + 1);
        } /*else {
            DoingHomeworkActivity activity = (DoingHomeworkActivity) getActivity();
            activity.showProgress();
        }*/
    }

    /**
     * 新创建一个题目视图
     *
     * @param subject
     * @return
     */
    protected XKSubjectView inflateSubjectView(XKSubject subject) {
        return subject.createShowView(getActivity());
    }

    protected void setSubjectData(XKSubject subject, XKSubjectView subjectView) {
        subjectView.setShowSubject(subject);
        subjectView.setShowNext(DoingTaskFragment.this);
        if (isPartEditable) {
            subjectView.setEditable(subject.isEditable());
        } else {
            subjectView.setEditable(editable);
        }
    }

    private class SubjectAdapter extends PagerAdapter {

        @Override
        public CharSequence getPageTitle(int position) {
            return homework.getSubjectTitle(position);
        }

        @Override
        public int getCount() {
            return homework != null ? homework.getPageSize() : 0;
        }

        @Override
        public boolean isViewFromObject(View view, Object o) {
            return view == o;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            XKSubjectView convertView = null;
            XKSubject subject = homework.getSubjectAtPageIndex(position);
            int destroyViewLen = destroyViews.size();
            int index;
            for (index = 0; index < destroyViewLen; index++) {
                XKSubjectView subjectView = destroyViews.get(index);
                if (subjectView.getShowSubjectType() == subject.getType()) {
                    convertView = subjectView;
                }
            }
            if (convertView != null) {
                destroyViews.remove(convertView);
            }
            if (convertView == null) {
                convertView = inflateSubjectView(subject);
            }

            if (subject.getType() == XKSubject.SUBJECT_TYPE_COMPLETION ||
                    subject.getType() == XKSubject.SUBJECT_TYPE_SHORT_ANSWER) {
                subject.setEditable(true);
            } else {
                subject.setEditable(false);
            }
            setSubjectData(subject, convertView);

            showingViews.add(convertView);
            container.addView(convertView);
            return convertView;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
            showingViews.remove(object);
            destroyViews.add((XKSubjectView) object);
        }
    }

    /**
     * 设置是否可编辑
     *
     * @param editable
     */
    public void setEditable(boolean editable) {
        this.editable = editable;
        for (XKSubjectView subjectView : showingViews) {
            subjectView.setEditable(editable);
        }
    }

    public boolean isPartEditable() {
        return isPartEditable;
    }

    public void setPartEditable(boolean isPartEditable) {
        this.isPartEditable = isPartEditable;
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        destroyViews.clear();

    }


    @Override
    public void onPause() {
        super.onPause();
        destroyViews.clear();
        hideKeyborad();
    }

    /**
     * 重做题目
     * @param subject
     */
    public void redoSubject(XKSubject subject) {
        final int index = homework.getPageIndexOf(subject);
        if (index >= 0) {
            App.getAppHandler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    mViewPager.setCurrentItem(index, false);
                    // 更新标题
                    onPageSelected(index);
                }
            }, 300);
        }
    }
}