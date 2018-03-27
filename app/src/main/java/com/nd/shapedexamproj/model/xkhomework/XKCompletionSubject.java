package com.nd.shapedexamproj.model.xkhomework;

import android.content.Context;
import android.view.View;

import com.nd.shapedexamproj.R;
import com.nd.shapedexamproj.view.xkhomework.XKSubjectView;

/**
 * Created by Administrator on 2015/1/22.
 */
public class XKCompletionSubject extends XKSubject {

    @Override
    public int getType() {
        return SUBJECT_TYPE_COMPLETION;
    }

    @Override
    public boolean hasDone() {
        return userAnswer != null && userAnswer.trim().length() > 0;
    }

    @Override
    public boolean isRight() {
        return false;
    }

    @Override
    public String getUserAnswerData() {
        return userAnswer;
    }

    @Override
    public void setUserAnswerData(String data) {
        userAnswer = data;
    }

    @Override
    public XKSubjectView createShowView(Context context) {
       return (XKSubjectView) View.inflate(context, R.layout.xk_doing_homework_subject_completion_view, null);
    }
}
