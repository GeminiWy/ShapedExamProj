package com.nd.shapedexamproj.model;

import com.tming.openuniversity.model.IJsonInitable;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Administrator on 2015/1/13.
 */
public class HomeworkSumary implements IJsonInitable {

    private String courseName;
    private String courseId;
    private int doneCount;//已完成
    private int passCount;//及格数
    private int toBeMarkedCount;//待批改

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public String getCourseId() {
        return courseId;
    }

    public void setCourseId(String courseId) {
        this.courseId = courseId;
    }

    public int getDoneCount() {
        return doneCount;
    }

    public void setDoneCount(int doneCount) {
        this.doneCount = doneCount;
    }

    public int getPassCount() {
        return passCount;
    }

    public void setPassCount(int passCount) {
        this.passCount = passCount;
    }

    public int getToBeMarkedCount() {
        return toBeMarkedCount;
    }

    public void setToBeMarkedCount(int toBeMarkedCount) {
        this.toBeMarkedCount = toBeMarkedCount;
    }

    @Override
    public void initWithJsonObject(JSONObject jsonObj) throws JSONException {
        courseId = jsonObj.getString("course_id");
        courseName = jsonObj.getString("course_name");

        JSONObject statJson = jsonObj.getJSONObject("stat");
        passCount = statJson.getInt("passNum");
        doneCount = statJson.getInt("doneNum");
        toBeMarkedCount = statJson.getInt("waitMarkNum");
    }
}
