package com.nd.shapedexamproj.view.homework;

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
import com.nd.shapedexamproj.activity.homework.DoingHomeworkActivity;
import com.nd.shapedexamproj.model.homework.Homework;
import com.nd.shapedexamproj.model.homework.Subject;

import java.util.ArrayList;
import java.util.List;

/**
 * 做作业Fragment
 * Created by yusongying on 2015/1/19.
 */
public class DoingHomeworkFragment extends Fragment implements View.OnClickListener, ViewPager.OnPageChangeListener, INextable {

    protected Homework homework;
    private List<SubjectView> destroyViews = new ArrayList<SubjectView>();
    private List<SubjectView> showingViews = new ArrayList<SubjectView>();
    private ViewPager mViewPager = null;
    private TextView mTitleTv;
    private SubjectAdapter mAdapter = null;
    private TextView mProgressTv;
    private View previousBtn;
    private int mCurrentShowSubjectType;
    private boolean editable = true;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.doing_homework_fragment, null);

        mViewPager = (ViewPager) view.findViewById(R.id.doing_homework_frame_content_vp);
        mProgressTv = (TextView) view.findViewById(R.id.doing_homework_frame_progress_tv);
        mTitleTv = (TextView) view.findViewById(R.id.doing_homework_frame_title_tv);
        previousBtn = view.findViewById(R.id.doing_homework_frame_previous_btn);

        view.findViewById(R.id.doing_homework_frame_next_btn).setOnClickListener(this);
        view.findViewById(R.id.doing_homework_frame_progress_tv).setOnClickListener(this);
        previousBtn.setOnClickListener(this);

        initData();
        return view;
    }

    public void setHomework(Homework homework) {
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
        switch (v.getId()) {
            case R.id.doing_homework_frame_next_btn: {
                int curItemIndex = mViewPager.getCurrentItem();
                if (curItemIndex == homework.getPageSize() - 1) {// 最后一题
                    DoingHomeworkActivity activity = (DoingHomeworkActivity) getActivity();
                    activity.showProgress();
                } else if (curItemIndex < homework.getPageSize()) {
                    mViewPager.setCurrentItem(curItemIndex + 1);
                }
            }
                break;
            case R.id.doing_homework_frame_previous_btn: {
                int curItemIndex = mViewPager.getCurrentItem();
                if (curItemIndex > 0) {
                    mViewPager.setCurrentItem(curItemIndex - 1);
                }
            }
                break;
            case R.id.doing_homework_frame_progress_tv:
                DoingHomeworkActivity activity = (DoingHomeworkActivity) getActivity();
                activity.showProgress();
                break;
        }
    }

    @Override
    public void onPageScrolled(int i, float v, int i1) {

    }

    @Override
    public void onPageSelected(int i) {
        if (homework == null || homework.getSubjectSize() == 0) {
            return;
        }
        if (i == 0) {
            previousBtn.setClickable(false);
        } else {
            previousBtn.setClickable(true);
        }
        Subject showingSubject = homework.getSubjectAtPageIndex(i);
        mProgressTv.setText(String.format("进度:%d/%d", homework.indexOf(showingSubject) + 1, homework.getSubjectSize()));
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
    public void showNext(Subject currentShowSubject) {
        int curItem = mViewPager.getCurrentItem();
        if (curItem < homework.getPageSize() - 1) {
            mViewPager.setCurrentItem(curItem + 1);
        } else {
            DoingHomeworkActivity activity = (DoingHomeworkActivity) getActivity();
            activity.showProgress();
        }
    }

    /**
     * 新创建一个题目视图
     *
     * @param subject
     * @return
     */
    protected SubjectView inflateSubjectView(Subject subject) {
        return subject.createShowView(getActivity());
    }

    protected void setSubjectData(Subject subject, SubjectView subjectView) {
        subjectView.setShowSubject(subject);
        subjectView.setShowNext(DoingHomeworkFragment.this);
        subjectView.setEditable(editable);
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
            SubjectView convertView = null;
            Subject subject = homework.getSubjectAtPageIndex(position);
            int destroyViewLen = destroyViews.size();
            int index;
            for (index = 0; index < destroyViewLen; index++) {
                SubjectView subjectView = destroyViews.get(index);
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
            setSubjectData(subject, convertView);

            showingViews.add(convertView);
            container.addView(convertView);
            return convertView;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
            showingViews.remove(object);
            destroyViews.add((SubjectView) object);
        }
    }

    /**
     * 设置是否可编辑
     *
     * @param editable
     */
    public void setEditable(boolean editable) {
        this.editable = editable;
        for (SubjectView subjectView : showingViews) {
            subjectView.setEditable(editable);
        }
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
    public void redoSubject(Subject subject) {
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