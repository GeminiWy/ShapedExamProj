package com.nd.shapedexamproj.model.homework;

import android.content.Context;
import android.view.View;

import com.nd.shapedexamproj.R;
import com.nd.shapedexamproj.view.homework.SubjectView;

/**
 * Created by Administrator on 2015/1/22.
 */
public class ShortAnswerSubject extends Subject {

    private String userAnswer;

    @Override
    public int getType() {
        return SUBJECT_TYPE_SHORT_ANSWER;
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
    public SubjectView createShowView(Context context) {
        return (SubjectView) View.inflate(context, R.layout.doing_homework_subject_short_answer_view, null);
    }
}
