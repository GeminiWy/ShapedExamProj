package com.nd.shapedexamproj.model.homework;

import android.content.Context;
import android.view.View;

import com.nd.shapedexamproj.R;
import com.nd.shapedexamproj.view.homework.SubjectView;

/**
 * Created by Administrator on 2015/1/20.
 */
public class MultiChoiceSubject extends SingleChoiceSubject {

    @Override
    public int getType() {
        return SUBJECT_TYPE_MULTI_CHOICE;
    }

    @Override
    public SubjectView createShowView(Context context) {
        return (SubjectView) View.inflate(context, R.layout.doing_homework_subject_multi_choice_view, null);
    }

    @Override
    public boolean isRight() {
        if (userAnswer != null && userAnswer.length() > 0) {
            if (userAnswer.length() == rightAnswer.length()) {
                int len = userAnswer.length();
                // 对比每个字符是否相同
                for (int i = 0; i < len; i++) {
                    if (rightAnswer.indexOf(userAnswer.charAt(i)) < 0) {
                        return false;
                    }
                }
                return true;
            }
        }
        return false;
    }
}
