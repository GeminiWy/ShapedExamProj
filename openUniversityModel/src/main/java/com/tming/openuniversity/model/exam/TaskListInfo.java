package com.tming.openuniversity.model.exam;

import com.tming.openuniversity.model.IJsonInitable;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zll on 2015/3/3.
 */
public class TaskListInfo implements IJsonInitable {
    public String taskId;//任务ID
    public String taskName;//任务名称
    public long startTime;//任务开始时间戳 - 1423792753
    public long finishTime;//任务结束时间戳
    public int allowTimes;//允许答题次数
    public int times;//现在第几次答题
    public int weight;//权重 - 25
    public int total;//总条数 整数
    public int isStart = 1;//是否可以开始，1：可以，0：不可以

    public List<TaskListInfo> taskList = new ArrayList<TaskListInfo>();

    @Override
    public void initWithJsonObject(JSONObject jsonObj) throws JSONException {
        if (jsonObj == null) return;
        this.total = jsonObj.getInt("total");
        JSONArray jsonArray = jsonObj.getJSONArray("list");
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject listObj = jsonArray.getJSONObject(i);
            TaskListInfo listInfo = new TaskListInfo();
            listInfo.taskId = listObj.getString("id");
            listInfo.taskName = listObj.getString("name");
            listInfo.startTime = listObj.getLong("start");
            listInfo.finishTime = listObj.getLong("finish");
            listInfo.allowTimes = listObj.getInt("allow_times");
            listInfo.times = listObj.getInt("times");
            listInfo.weight = listObj.getInt("weight");//TODO
            listInfo.isStart = listObj.getInt("is_start");

            taskList.add(listInfo);
        }
    }
}
