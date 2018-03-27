package com.nd.shapedexamproj.model.xkhomework;

import android.content.Context;
import android.view.View;

import com.nd.shapedexamproj.R;
import com.nd.shapedexamproj.view.xkhomework.XKSubjectView;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Administrator on 2015/1/20.
 */
public class XKJudgmentSubject extends XKSubject {

    public static final String RIGHT_STRING = "对";
    public static final String ERROR_STRING = "错";

    @Override
    public int getType() {
        return SUBJECT_TYPE_JUDGMENT;
    }

    @Override
    public boolean hasDone() {
        if (userAnswer != null && userAnswer.trim().length() > 0) {
            return true;
        }
        return false;
    }

    @Override
    public boolean isRight() {
        if (userAnswer != null && userAnswer.equals(rightAnswer)) {
            return true;
        }
        return false;
    }

    public String getRightAnswer() {
        return rightAnswer;
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
        return (XKSubjectView) View.inflate(context, R.layout.xk_doing_homework_subject_judgment_view, null);
    }

    @Override
    public void initWithJsonObject(JSONObject jsonObj) throws JSONException {
        super.initWithJsonObject(jsonObj);
        if (jsonObj.has("answer")) {
            rightAnswer = jsonObj.getString("answer");
        }
    }

    @Override
    public JSONObject toJsonObject() throws JSONException {
        JSONObject jsonObject = super.toJsonObject();
        jsonObject.put("answer", rightAnswer);
        return jsonObject;
    }
}
