package com.nd.shapedexamproj.view.xkhomework;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.widget.EditText;

import com.nd.shapedexamproj.R;
import com.nd.shapedexamproj.model.xkhomework.XKSubject;
import com.nd.shapedexamproj.view.AutoImageLoadTextView;

/**
 * 简答题
 * Created by yusongying on 2015/1/22.
 */
public class XKShortAnswerSubjectView extends XKSubjectView implements TextWatcher {

    protected EditText mInputEt;

    public XKShortAnswerSubjectView(Context context) {
        super(context);
    }

    public XKShortAnswerSubjectView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onInit() {
        titleView = (AutoImageLoadTextView) findViewById(R.id.doing_homework_subject_short_answer_title_tv);
        mInputEt = (EditText) findViewById(R.id.doing_homework_subject_short_answer_input_et);
        mInputEt.addTextChangedListener(this);
    }

    @Override
    public int getShowSubjectType() {
        return XKSubject.SUBJECT_TYPE_SHORT_ANSWER;
    }

    @Override
    public void setShowSubject(XKSubject subject) {
        super.setShowSubject(subject);
        mInputEt.setText(subject.getUserAnswerData());
    }

    @Override
    public void setEditable(boolean editable) {
        if (mInputEt != null) {
            mInputEt.setEnabled(editable);
        }
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        subject.setUserAnswerData(s.toString());
    }
}
