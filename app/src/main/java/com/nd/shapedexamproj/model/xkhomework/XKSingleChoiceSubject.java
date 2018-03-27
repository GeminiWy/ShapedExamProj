package com.nd.shapedexamproj.model.xkhomework;

import android.content.Context;
import android.view.View;

import com.nd.shapedexamproj.R;
import com.nd.shapedexamproj.view.xkhomework.XKSingleChoiceSubjectView;
import com.nd.shapedexamproj.view.xkhomework.XKSubjectView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by 单选题 on 2015/1/19.
 */
public class XKSingleChoiceSubject extends XKSubject {

    protected  Map<String, String> choices;

    public Map<String, String> getChoices() {
        return choices;
    }

    @Override
    public int getType() {
        return SUBJECT_TYPE_SINGLE_CHOICE;
    }

    @Override
    public boolean hasDone() {
        return userAnswer != null && userAnswer.trim().length() > 0;
    }

    @Override
    public boolean isRight() {
        if (userAnswer != null && userAnswer.length() > 0 && userAnswer.equals(rightAnswer)) {
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
        this.userAnswer = data;
    }

    @Override
    public XKSubjectView createShowView(Context context) {
        return (XKSingleChoiceSubjectView) View.inflate(context, R.layout.xk_doing_homework_subject_single_choice_view, null);
    }

    @Override
    public void initWithJsonObject(JSONObject jsonObj) throws JSONException {
        super.initWithJsonObject(jsonObj);
        JSONArray choiceJsonArray = jsonObj.getJSONArray("choice");
        int len = choiceJsonArray.length();
        choices = new HashMap<String, String>();
        for (int i = 0; i < len; i++) {
            JSONObject choiceJsonObj = choiceJsonArray.getJSONObject(i);
            String key = (String) choiceJsonObj.keys().next();
            choices.put(key, choiceJsonObj.getString(key));
        }
    }

    @Override
    public JSONObject toJsonObject() throws JSONException {
        JSONObject jsonObject = super.toJsonObject();
        jsonObject.put("answer", rightAnswer);
        Set<String> keys = choices.keySet();
        JSONArray jsonArray = new JSONArray();
        for (String key : keys) {
            JSONObject obj = new JSONObject();
            obj.put(key, choices.get(key));
            jsonArray.put(obj);
        }
        jsonObject.put("choice", jsonArray);
        return jsonObject;
    }
}
