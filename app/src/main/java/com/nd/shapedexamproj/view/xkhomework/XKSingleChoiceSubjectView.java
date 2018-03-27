package com.nd.shapedexamproj.view.xkhomework;

import android.content.Context;
import android.util.AttributeSet;

import com.nd.shapedexamproj.App;
import com.nd.shapedexamproj.R;
import com.nd.shapedexamproj.model.xkhomework.XKSingleChoiceSubject;
import com.nd.shapedexamproj.model.xkhomework.XKSubject;
import com.nd.shapedexamproj.view.AutoImageLoadTextView;
import com.nd.shapedexamproj.view.homework.OptionsView;

import java.util.*;

/**
 * 单选题视图
 * Created by yusongying on 2015/1/19.
 */
public class XKSingleChoiceSubjectView extends XKSubjectView implements OptionsView.OnChoiceChangeListener {

    protected OptionsView mOptionsView;
    /*protected List<String> choiceData;*/

    public XKSingleChoiceSubjectView(Context context) {
        super(context);
    }

    public XKSingleChoiceSubjectView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }


    protected void onInit() {
        titleView = (AutoImageLoadTextView) findViewById(R.id.doing_homework_subject_single_choice_title_tv);
        mOptionsView = (OptionsView) findViewById(R.id.doing_homework_subject_single_choice_options_view);
        mOptionsView.setChoiceListener(this);
    }

    @Override
    public int getShowSubjectType() {
        return XKSubject.SUBJECT_TYPE_SINGLE_CHOICE;
    }

    @Override
    public void setShowSubject(XKSubject subject) {
        super.setShowSubject(subject);
        XKSingleChoiceSubject singleChoiceSubject = (XKSingleChoiceSubject) subject;
        Set<String> keys = singleChoiceSubject.getChoices().keySet();
        List<String> keysList = new ArrayList<String>(keys);
        Collections.sort(keysList);

        int selectedChoiceIndex = -1;
        int index = 0;
        for (String key : keysList) {
            // 设置用户答题数据
            if (singleChoiceSubject.getUserAnswerData() != null && singleChoiceSubject.getUserAnswerData().equals(key)) {
                selectedChoiceIndex = index;
                break;
            }
            index++;
        }
        mOptionsView.setShowChoiceData(singleChoiceSubject.getChoices());
        mOptionsView.setSelectedChoice(selectedChoiceIndex);
    }

    @Override
    public void setEditable(boolean editable) {
        mOptionsView.setEnabled(editable);
    }


    protected String getChoiceKey(Map<String, String> choices, int choiceIndex) {
        Set<String> keys = choices.keySet();

        String[] keyAry = new String[keys.size()];
        keys.toArray(keyAry);
        Arrays.sort(keyAry);

        return keyAry[choiceIndex];
    }

    @Override
    public void onOptionsViewChoiceChange(OptionsView view, String choice, int index) {
        XKSingleChoiceSubject singleChoiceSubject = (XKSingleChoiceSubject) subject;
        singleChoiceSubject.setUserAnswerData(getChoiceKey(singleChoiceSubject.getChoices(), index));

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
