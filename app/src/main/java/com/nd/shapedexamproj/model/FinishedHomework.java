package com.nd.shapedexamproj.model;

import com.tming.openuniversity.model.IJsonInitable;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * 已完成的作业信息
 * Created by yusongying on 2015/1/13.
 */
public class FinishedHomework implements IJsonInitable {

    private String exameId;
    private String exameName;
    private int doTimes;
    private int maxDoTimes;
    private float score;
    private boolean isMarked;
    private long timeEnd;

    public String getExameId() {
        return exameId;
    }

    public void setExameId(String exameId) {
        this.exameId = exameId;
    }

    public String getExameName() {
        return exameName;
    }

    public void setExameName(String exameName) {
        this.exameName = exameName;
    }

    public int getDoTimes() {
        return doTimes;
    }

    public void setDoTimes(int doTimes) {
        this.doTimes = doTimes;
    }

    public int getMaxDoTimes() {
        return maxDoTimes;
    }

    public void setMaxDoTimes(int maxDoTimes) {
        this.maxDoTimes = maxDoTimes;
    }

    public float getScore() {
        return score;
    }

    public void setScore(float score) {
        this.score = score;
    }

    public boolean isMarked() {
        return isMarked;
    }

    public void setMarked(boolean isMarked) {
        this.isMarked = isMarked;
    }

    public long getTimeEnd() {
        return timeEnd;
    }

    public void setTimeEnd(long timeEnd) {
        this.timeEnd = timeEnd;
    }

    @Override
    public void initWithJsonObject(JSONObject jsonObj) throws JSONException {
        exameId = jsonObj.getString("exam_id");
        exameName = jsonObj.getString("exam_name");
        score = Float.parseFloat(jsonObj.getString("score"));
        if (score == -1) {
            score = 0;
        }
        doTimes = Integer.parseInt(jsonObj.getString("do_times"));
        maxDoTimes = Integer.parseInt(jsonObj.getString("max_times"));
        isMarked = "1".equals(jsonObj.getString("is_marked"));
        timeEnd = Long.parseLong(jsonObj.getString("time_end")) * 1000;
    }
}
