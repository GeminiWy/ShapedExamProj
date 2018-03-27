package com.nd.shapedexamproj.view.homework;

import android.view.View;
import android.view.ViewGroup;

import com.nd.shapedexamproj.R;
import com.nd.shapedexamproj.model.homework.JudgmentSubject;
import com.nd.shapedexamproj.model.homework.MultiChoiceSubject;
import com.nd.shapedexamproj.model.homework.SingleChoiceSubject;
import com.nd.shapedexamproj.model.homework.Subject;
import com.nd.shapedexamproj.model.homework.WrongHomework;
import com.nd.shapedexamproj.view.AutoImageLoadTextView;
import com.tming.common.util.Log;

/**
 * 错题集
 * Created by yusongying on 2015/2/4.
 */
public class DoingWrongHomeworkFragment extends DoingHomeworkFragment {

    private static final String TAG = "DoingWrongHomeworkFragment";

    @Override
    protected SubjectView inflateSubjectView(Subject subject) {
        SubjectView subjectView = super.inflateSubjectView(subject);
        View view = View.inflate(getActivity(), R.layout.doing_wrong_homework_subject_analysis_view, null);
        view.findViewById(R.id.doing_wrong_homework_subject_analysis_view_show_answer_btn).setOnClickListener(this);

        ViewGroup contentView = (ViewGroup) subjectView.findViewById(R.id.doing_homework_subject_content_lay);
        if (contentView != null) {
            contentView.addView(view);
        } else {
            Log.e(TAG, "inflateSubjectView no found:doing_homework_subject_content_lay");
        }
       return subjectView;
    }

    @Override
    protected void setSubjectData(Subject subject, SubjectView subjectView) {
        subjectView.setShowSubject(subject);
        subjectView.setEditable(true);
        AutoImageLoadTextView textView = (AutoImageLoadTextView) subjectView.findViewById(R.id.doing_wrong_homework_subject_analysis_view_analysis_tv);
        if (textView == null) {
            Log.e(TAG, "no found:doing_wrong_homework_subject_analysis_view_analysis_tv");
            return;
        }
        textView.setVisibility(View.GONE);
        subjectView.findViewById(R.id.doing_wrong_homework_subject_analysis_view_show_answer_btn).setVisibility(View.VISIBLE);

        String rightAnswer = null;
        if (subject instanceof SingleChoiceSubject) {
            rightAnswer = ((SingleChoiceSubject) subject).getRightAnswer();
        } else if (subject instanceof MultiChoiceSubject) {
            rightAnswer = ((MultiChoiceSubject) subject).getRightAnswer();
        } else if (subject instanceof JudgmentSubject) {
            rightAnswer = ((JudgmentSubject) subject).getRightAnswer();
        }
        WrongHomework wrongHomework = (WrongHomework) homework;
        textView.setText("<strong>正确答案:</strong>" + rightAnswer + "<br/><strong>答案解析:</strong>"
                + wrongHomework.getSubjectAnalysis(subject.getSubjectId()) + "");
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        if (v.getId() == R.id.doing_wrong_homework_subject_analysis_view_show_answer_btn) {
            ViewGroup vg = (ViewGroup) v.getParent();
            vg.findViewById(R.id.doing_wrong_homework_subject_analysis_view_analysis_tv).setVisibility(View.VISIBLE);
            v.setVisibility(View.GONE);
        }
    }
}
