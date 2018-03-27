package com.tming.openuniversity.model.exam;

import com.tming.openuniversity.model.IJsonInitable;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 答题历史记录
 * Created by zll on 2015/3/2.
 */
public class AnswerHistoryDetail implements IJsonInitable {

    public String recordId;//答题记录id
    public String taskId;//任务ID
    public String taskName;//任务名称
    public int status;//状态：1=继续答题，2=已提交，3=正在评阅，4=已经退回，5=已经评阅, 6=已经过期
    public long startTime;//任务开始时间戳 - 1423792753
    public long finishTime;//任务结束时间戳
    public int allowTimes;//允许答题次数
    public int times;//现在第几次答题
    public int totalNum;//总的题目数
    public int undoNum;//未答的题目数
    public int errorNum;//做错的题目数
    public float objectiveScore;//客观题得分
    public float subjectiveScore;//主观题得分
    public float totalScore;//成绩,客观题加主观题
    public String errorMsg;

    public List<SubjectHolder> subjectsList = new ArrayList<SubjectHolder>();
    public class SubjectHolder implements Serializable {

        public String subjectName;//题型名称 - 例如单项选择题
        public int subjectNums;//总题数
        public String subjectValue ;//题型总分
    }


    @Override
    public void initWithJsonObject(JSONObject jsonObj) throws JSONException {
        JSONObject infoObj = jsonObj.getJSONObject("info");
        recordId = infoObj.getString("id");
        taskId = infoObj.getString("task_id");
        status = infoObj.getInt("status");
        JSONObject baseObj = infoObj.getJSONObject("base");
        allowTimes = baseObj.getInt("allow_times");
        times = baseObj.getInt("times");
        totalNum = baseObj.getInt("total_num");
        if (baseObj.has("undo_num")) {
            undoNum = baseObj.getInt("undo_num");
        }
        if (baseObj.has("error_num")) {
            errorNum = baseObj.getInt("error_num");
        }
        finishTime = baseObj.getLong("finish");
        if (infoObj.has("score")) {
            JSONObject scoreObj = infoObj.getJSONObject("score");
            String obScore = scoreObj.getString("objective");
            if (!obScore.equals("") && obScore != null) {
                objectiveScore = Float.parseFloat(obScore);
            }
            String sbScore = scoreObj.getString("subjectivity");
            if (!sbScore.equals("") && sbScore != null) {
                subjectiveScore = Float.parseFloat(sbScore);
            }
            String ttScore = scoreObj.getString("total");
            if (!ttScore.equals("") && ttScore != null) {
                totalScore = Float.parseFloat(ttScore);
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
