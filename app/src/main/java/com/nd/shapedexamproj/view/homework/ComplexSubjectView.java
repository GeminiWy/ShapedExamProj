package com.nd.shapedexamproj.view.homework;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.ScrollView;

import com.nd.shapedexamproj.R;
import com.nd.shapedexamproj.model.homework.ComplexSubject;
import com.nd.shapedexamproj.model.homework.Subject;
import com.nd.shapedexamproj.view.AutoImageLoadTextView;

import java.util.ArrayList;
import java.util.List;

/**
 * 综合题
 * Created by yusongying on 2015/1/22.
 */
public class ComplexSubjectView extends SubjectView implements INextable {

    private ViewGroup mSubContentLay;
    private List<SubjectView> mSubContentViews = new ArrayList<SubjectView>();
    private ScrollView mScrollView;

    public ComplexSubjectView(Context context) {
        super(context);
    }

    public ComplexSubjectView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onInit() {
        mScrollView = (ScrollView) findViewById(R.id.doing_homework_subject_complex_view_sv);
        titleView = (AutoImageLoadTextView) findViewById(R.id.doing_homework_subject_complex_view_title_tv);
        mSubContentLay = (ViewGroup) findViewById(R.id.doing_homework_subject_complex_view_sub_content_lay);
    }

    @Override
    public int getShowSubjectType() {
        return Subject.SUBJECT_TYPE_COMPLEX;
    }

    @Override
    public void setShowSubject(Subject subject) {
        super.setShowSubject(subject);
        ComplexSubject complexSubject = (ComplexSubject) subject;
        mSubContentLay.removeAllViews();
        mSubContentViews.clear();
        List<Subject> subjects = complexSubject.getSubjects();
        for (Subject sub : subjects) {
            SubjectView view = sub.createShowView(getContext());
            view.removeScrollView();
            view.setShowSubject(sub);
            view.showCompexSubjectType();
            view.setShowNext(this);
            mSubContentLay.addView(view);
            mSubContentViews.add(view);
        }
    }

    @Override
    public void setEditable(boolean editable) {
        for (SubjectView subjectView : mSubContentViews) {
            subjectView.setEditable(editable);
        }
    }

    @Override
    public void showNext(Subject currentShowSubject) {
        ComplexSubject complexSubject = (ComplexSubject) subject;
        List<Subject> list = complexSubject.getSubjects();
        if (list != null) {
            int index = list.indexOf(currentShowSubject);
            if (index  > -1 && index < list.size() - 1) {
                SubjectView view = mSubContentViews.get(index);
                mScrollView.smoothScrollBy(0, view.getHeight());
            } else if (index == list.size() - 1) {
                INextable nextable = nextWeakReference.get();
                if (nextable != null) {
                    nextable.showNext(subject);
                }
            }
        }
    }
}
