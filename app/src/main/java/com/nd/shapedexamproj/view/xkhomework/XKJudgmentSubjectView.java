package com.nd.shapedexamproj.view.xkhomework;

import android.content.Context;
import android.util.AttributeSet;

import com.nd.shapedexamproj.App;
import com.nd.shapedexamproj.R;
import com.nd.shapedexamproj.model.homework.JudgmentSubject;
import com.nd.shapedexamproj.model.xkhomework.XKSubject;
import com.nd.shapedexamproj.view.AutoImageLoadTextView;
import com.nd.shapedexamproj.view.homework.OptionsView;

import java.util.ArrayList;

/**
 * Created by Administrator on 2015/1/20.
 */
public class XKJudgmentSubjectView extends XKSubjectView implements OptionsView.OnChoiceChangeListener {

    private OptionsView mOptionsView;

    public XKJudgmentSubjectView(Context context) {
        super(context);
    }

    public XKJudgmentSubjectView(Context context, AttributeSet attrs) {
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
        return XKSubject.SUBJECT_TYPE_JUDGMENT;
    }

    @Override
    public void setShowSubject(XKSubject subject) {
        super.setShowSubject(subject);
        String userAnswer = subject.getUserAnswerData();
        if (JudgmentSubject.ERROR_STRING.equals(userAnswer)) {
            mOptionsView.setSelectedChoice(1);
        } else if (JudgmentSubject.RIGHT_STRING.equals(userAnswer)) {
            mOptionsView.setSelectedChoice(0);
        } else {
            mOptionsView.setSelectedChoice(-1);
        }
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
            final XKINextable nextable = nextWeakReference.get();
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
