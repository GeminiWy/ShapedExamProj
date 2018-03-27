package com.nd.shapedexamproj.view.homework;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.EditText;

import com.nd.shapedexamproj.R;
import com.nd.shapedexamproj.model.homework.Subject;
import com.nd.shapedexamproj.view.AutoImageLoadTextView;

/**
 * 完型填空
 * Created by Administrator on 2015/1/22.
 */
public class CompletionSubjectView extends ShortAnswerSubjectView {

    public CompletionSubjectView(Context context) {
        super(context);
    }

    public CompletionSubjectView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public int getShowSubjectType() {
        return Subject.SUBJECT_TYPE_COMPLETION;
    }

    @Override
    protected void onInit() {
        titleView = (AutoImageLoadTextView) findViewById(R.id.doing_homework_subject_completion_title_tv);
        mInputEt = (EditText) findViewById(R.id.doing_homework_subject_completion_input_et);

        mInputEt.addTextChangedListener(this);
    }
}
