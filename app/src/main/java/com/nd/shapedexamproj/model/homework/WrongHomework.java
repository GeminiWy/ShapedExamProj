package com.nd.shapedexamproj.model.homework;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

/**
 * 错题集
 * Created by yusongying on 2015/2/4.
 */
public class WrongHomework extends Homework {

    /**
     * 错题解析
     */
    private HashMap<String, String> subjectAnalysis;


    @Override
    public void initWithJsonObject(JSONObject jsonObj) throws JSONException {
        subjectAnalysis = new HashMap<String, String>();
        JSONArray jsonArray = jsonObj.getJSONArray("list");
        int len = jsonArray.length();
        for (int i = 0; i < len; i++) {
            JSONObject subjectJsonObj = jsonArray.getJSONObject(i);
            String serviceType =subjectJsonObj.getString("work_type");
            Subject subject = Subject.newSubjectWithWorkType(serviceType);
            subject.initWithJsonObject(subjectJsonObj);
            subjects.add(subject);

            String subId = subject.getSubjectId();
            String analysis = subjectJsonObj.getString("analy");
            subjectAnalysis.put(subId, analysis);
        }
        hasSort = false;
    }

    /**
     * 获取题目解析
     * @param questionId
     * @return
     */
    public String getSubjectAnalysis(String questionId) {
        return subjectAnalysis.get(questionId);
    }
}
