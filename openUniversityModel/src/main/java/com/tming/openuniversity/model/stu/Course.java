package com.tming.openuniversity.model.stu;

import com.tming.openuniversity.model.IJsonInitable;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zll on 2015/3/12.
 */
public class Course implements IJsonInitable {

    public String courseId;//课程ID
    public String courseName;//课程名称
    public String cover;//课程封面
    public String time;
    public int type ;//类型（整数） 1=专业课程 2=其他课程

    public int exam_complete;//形考任务完成的数量（整数）
    public int exam_total;//形考任务总数据量（整数）

    public int warmup_complete;//热身考试完成的数量（整数）
    public int warmup_total;//热身考试总数据量（整数）

    public String percent;//完成百分比 默认0.00
    public String playRecord;//最后的观看节点，如果没有的话，不返回这个字段
    public int total;//总条数

    public List<Course> courses = new ArrayList<Course>();

    @Override
    public void initWithJsonObject(JSONObject jsonObj) throws JSONException {
        JSONArray listArr = jsonObj.getJSONArray("list");
        total = jsonObj.getInt("total");
        for (int i = 0; i < listArr.length(); i++) {
            JSONObject courseObj = listArr.getJSONObject(i);
            Course course = new Course();
            course.courseId = courseObj.getString("id");
            course.courseName = courseObj.getString("name");
            course.cover = courseObj.getString("cover");
            if (courseObj.has("type")) {
                course.type = courseObj.getInt("type");
            }
            course.exam_complete = courseObj.getJSONObject("exam_info").getInt("complete");
            course.exam_total = courseObj.getJSONObject("exam_info").getInt("total");

            course.warmup_complete = courseObj.getJSONObject("warmup_info").getInt("complete");
            course.warmup_total = courseObj.getJSONObject("warmup_info").getInt("total");

            course.percent = courseObj.getString("percent");
            if (courseObj.has("play_recorded")) {
                course.playRecord = courseObj.getString("play_recorded");//TODO 没返回
            }

            courses.add(course);
        }
    }
}
