package com.nd.shapedexamproj.model.homework;

import android.content.Context;
import android.view.View;

import com.nd.shapedexamproj.R;
import com.nd.shapedexamproj.view.homework.SubjectView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * 综合题
 * Created by yusongying on 2015/1/22.
 */
public class ComplexSubject extends Subject {

    private List<Subject> subjects;

    public ComplexSubject() {
        subjects = new ArrayList<Subject>();
    }

    public List<Subject> getSubjects() {
        return subjects;
    }

    @Override
    public int getType() {
        return SUBJECT_TYPE_COMPLEX;
    }

    @Override
    public boolean hasDone() {
        return true;
    }

    @Override
    public boolean isRight() {
        return false;
    }

    @Override
    public String getUserAnswerData() {
        JSONObject jsonObject = new JSONObject();
        for (Subject subject : subjects) {
            if (subject.hasDone()) {
                try {
                    jsonObject.put(subject.getSubjectId(), subject.getUserAnswerData());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
        return jsonObject.toString();
    }

    @Override
    public void setUserAnswerData(String data) {
        try {
            JSONObject jsonObject = new JSONObject(data);
            for (Subject subject : subjects) {
                if (jsonObject.has(subject.getSubjectId())) {
                    subject.setUserAnswerData(jsonObject.getString(subject.getSubjectId()));
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public SubjectView createShowView(Context context) {
        return (SubjectView) View.inflate(context, R.layout.doing_homework_subject_complex_view, null);
    }

    @Override
    public void initWithJsonObject(JSONObject jsonObj) throws JSONException {
        super.initWithJsonObject(jsonObj);
        subjects = new ArrayList<Subject>();
        if (jsonObj.has("subs")) {
            JSONArray jsonArray = jsonObj.getJSONArray("subs");
            int len = jsonArray.length();
            for (int i = 0; i < len; i++) {
                JSONObject subjectJsonObj = jsonArray.getJSONObject(i);
                String workType = subjectJsonObj.getString("work_type");
                Subject subject = Subject.newSubjectWithWorkType(workType);
                subject.initWithJsonObject(subjectJsonObj);
                subjects.add(subject);
            }
        }
    }

    @Override
    public JSONObject toJsonObject() throws JSONException {
        JSONObject jsonObject = super.toJsonObject();
        JSONArray jsonArray = new JSONArray();
        for (Subject subject : subjects) {
            JSONObject jsonObj = subject.toJsonObject();
            jsonObj.put("work_type", getWorkType(subject.getType()));
            jsonArray.put(jsonObj);
        }
        jsonObject.put("subs", jsonArray);
        return jsonObject;
    }
}
