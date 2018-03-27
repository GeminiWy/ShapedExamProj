package com.nd.shapedexamproj.view.homework;

import android.content.Context;
import android.util.AttributeSet;

import com.nd.shapedexamproj.App;
import com.nd.shapedexamproj.R;
import com.nd.shapedexamproj.model.homework.JudgmentSubject;
import com.nd.shapedexamproj.model.homework.Subject;
import com.nd.shapedexamproj.view.AutoImageLoadTextView;

import java.util.ArrayList;

/**
 * Created by Administrator on 2015/1/20.
 */
public class JudgmentSubjectView extends SubjectView implements OptionsView.OnChoiceChangeListener {

    private OptionsView mOptionsView;

    public JudgmentSubjectView(Context context) {
        super(context);
    }

    public JudgmentSubjectView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }


    @Override
    protected void onInit() {
        titleView = (AutoImageLoadTextView) findViewById(R.id.doing_homework_subject_judgment_view_title_tv);
        mOptionsView = (OptionsView) findViewById(R.id.doing_homework_subject_judgment_view_options_view);

        ArrayList<String> choice = new ArrayList<String>();
        choice.add(JudgmentSubject.RIGHT_STRING);
        choice.add(JudgmentSubject.ERROR_STRING);
        mOptionsView.setShowChoiceData(choice);
        mOptionsView.setChoiceListener(this);
    }

    @Override
    public int getShowSubjectType() {
        return Subject.SUBJECT_TYPE_JUDGMENT;
    }

    @Override
    public void setShowSubject(Subject subject) {
        super.setShowSubject(subject);
        String userAnswer = subject.getUserAnswerData();
        mOptionsView.setSelectedChoice(userAnswer.equals(JudgmentSubject.RIGHT_STRING) ? 0 : 1);
    }

    @Override
    public void setEditable(boolean editable) {
        mOptionsView.setEnabled(editable);
    }

    @Override
    public void onOptionsViewChoiceChange(OptionsView view, String choice, int index) {
        subject.setUserAnswerData(choice);

        // 显示下一题
        if (nextWeakReference != null) {
            final INextable nextable = nextWeakReference.get();
            if (nextable != null) {
                App.getAppHandler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        nextable.showNext(subject);
                    }
                }, 300);
            }
        }
    }
}
