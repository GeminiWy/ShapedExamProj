package com.nd.shapedexamproj.model.xkhomework;

import android.content.Context;
import android.view.View;

import com.nd.shapedexamproj.R;
import com.nd.shapedexamproj.view.xkhomework.XKSubjectView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Administrator on 2015/1/20.
 */
public class XKMultiChoiceSubject extends XKSingleChoiceSubject {

    @Override
    public int getType() {
        return SUBJECT_TYPE_MULTI_CHOICE;
    }

    @Override
    public XKSubjectView createShowView(Context context) {
        return (XKSubjectView) View.inflate(context, R.layout.xk_doing_homework_subject_multi_choice_view, null);
    }

    @Override
    public boolean isRight() {
        if (userAnswer != null && userAnswer.length() > 0 && rightAnswer != null) {
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

    @Override
    public void initWithJsonObject(JSONObject jsonObj) throws JSONException {
        super.initWithJsonObject(jsonObj);
        if (jsonObj.has("stu_answer")) {
            try {
                JSONArray array = jsonObj.getJSONArray("stu_answer");
                userAnswer = array.getString(0);
            } catch (JSONException e) {
            }
        }
    }
}
