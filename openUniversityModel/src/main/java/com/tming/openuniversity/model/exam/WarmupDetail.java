package com.tming.openuniversity.model.exam;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * 热身考试详情页
 * Created by zll on 2015/3/19.
 */
public class WarmupDetail extends AnswerHistoryDetail {
    public String warmupId;//热身考试ID
    @Override
    public void initWithJsonObject(JSONObject jsonObj) throws JSONException {
        JSONObject infoObj = jsonObj.getJSONObject("info");
        warmupId = infoObj.getString("id");
        status = infoObj.getInt("status");//状态（整数）：1=第一次做, 2=已经提交, 3=还没有提交
        JSONObject baseObj = infoObj.getJSONObject("base");
        allowTimes = baseObj.getInt("allow_times");
        times = baseObj.getInt("times");
        totalNum = baseObj.getInt("total_num");
        undoNum = baseObj.getInt("undo_num");
        if (infoObj.has("score")) {
            JSONObject scoreObj = infoObj.getJSONObject("score");
            String obScore = scoreObj.getString("objective");
            if (!obScore.equals("") && obScore != null) {
                objectiveScore = Float.parseFloat(obScore);
            } else {
                objectiveScore = -1;
            }
            String sbScore = scoreObj.getString("subjectivity");
            if (!sbScore.equals("") && sbScore != null) {
                subjectiveScore = Float.parseFloat(sbScore);
            } else {
                subjectiveScore = -1;
            }
            String ttScore = scoreObj.getString("total");
            if (!ttScore.equals("") && ttScore != null) {
                totalScore = Float.parseFloat(ttScore);
            } else {
                totalScore = -1;
            }
        }

        JSONArray overViewArray = infoObj.getJSONArray("overview");
        for (int i = 0; i < overViewArray.length(); i++) {
            JSONObject overViewObj = overViewArray.getJSONObject(i);
            SubjectHolder holder = new SubjectHolder();
            holder.subjectName = overViewObj.getString("name");
            JSONObject sbjObj = overViewObj.getJSONObject("info");
            holder.subjectNums = sbjObj.getInt("nums");
            holder.subjectValue = sbjObj.getString("scores");
            subjectsList.add(holder);
        }
    }
}
