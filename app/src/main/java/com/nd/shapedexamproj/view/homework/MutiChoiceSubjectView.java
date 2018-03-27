package com.nd.shapedexamproj.view.homework;

import android.content.Context;
import android.util.AttributeSet;

import com.nd.shapedexamproj.R;
import com.nd.shapedexamproj.model.homework.MultiChoiceSubject;
import com.nd.shapedexamproj.model.homework.Subject;
import com.nd.shapedexamproj.view.AutoImageLoadTextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * 简答题视图
 * Created by yusongying on 2015/1/20.
 */
public class MutiChoiceSubjectView extends SingleChoiceSubjectView implements OptionsView.OnMultiChoiceChangeListener {

    public MutiChoiceSubjectView(Context context) {
        super(context);
    }

    public MutiChoiceSubjectView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onInit() {
        titleView = (AutoImageLoadTextView) findViewById(R.id.doing_homework_subject_multi_choice_title_tv);
        mOptionsView = (OptionsView) findViewById(R.id.doing_homework_subject_multi_choice_options_view);
        mOptionsView.setMultiChoiceListener(this);
        mOptionsView.setMultiSelect(true);
    }

    @Override
    public int getShowSubjectType() {
        return Subject.SUBJECT_TYPE_MULTI_CHOICE;
    }


    @Override
    public void setShowSubject(Subject subject) {
        super.setShowSubject(subject);
        MultiChoiceSubject multiChoiceSubject = (MultiChoiceSubject) subject;
        String userAnswer = subject.getUserAnswerData();
        if (userAnswer != null) {
            List<Integer> selectedChoice = new ArrayList<Integer>();

            Set<String> keys = multiChoiceSubject.getChoices().keySet();
            String[] keyAry = new String[keys.size()];
            keys.toArray(keyAry);
            Arrays.sort(keyAry);

            for (int i = 0; i < keyAry.length; i++) {
                if (userAnswer.indexOf(keyAry[i]) > -1) {
                    selectedChoice.add(i);
                }
            }
            if (selectedChoice.size() > 0) {
                int[] selectedChoiceAry = new int[selectedChoice.size()];
                for (int i = 0; i < selectedChoiceAry.length; i++) {
                    selectedChoiceAry[i] = selectedChoice.get(i);
                }
                mOptionsView.setSelectedChoice(selectedChoiceAry);
            } else {
                mOptionsView.setSelectedChoice(null);
            }
        }
    }

    @Override
    public void onOptionsViewChoiceChange(OptionsView view, int[] indexs) {
        if (indexs != null) {
            Arrays.sort(indexs);
            MultiChoiceSubject multiChoiceSubject = (MultiChoiceSubject) subject;
            StringBuffer userAnswer = new StringBuffer();
            for (int choice : indexs) {
                userAnswer.append(getChoiceKey(multiChoiceSubject.getChoices(), choice));
            }
            subject.setUserAnswerData(userAnswer.toString());
        } else {
            subject.setUserAnswerData(null);
        }
    }
}
