package com.tming.openuniversity.model.exam;

import com.tming.openuniversity.model.IJsonInitable;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 任务详情
 * Created by zll on 2015/3/2.
 */
public class TaskDetailInfo implements IJsonInitable {
    public String taskId;//任务ID
    public String taskName;//任务名称
    public long startTime;//任务开始时间戳 - 1423792753
    public long finishTime;//任务结束时间戳
    public int allowTimes;//允许答题次数
    public int times;//现在第几次答题
    public int score;//成绩,客观题加主观题
    public int subjectNum;//题目总数
    public int canStart;//是否可以开始任务。1=可以，2=不可以
    public int status;//答题状态（整数） - 0=未开始，1=继续答题，2=已提交，3=正在评阅，4=已经退回，5=已经评阅, 6=已经过期
    public float weight;//权重-0.25
    public String errorMsg;
    public List<SubjectHolder> holderList = new ArrayList<SubjectHolder>();//题型列表

    /**
     * 作业详情的作业题型
     * @author zll
     */
    public class SubjectHolder implements Serializable {

        public String subjectName;//题型名称 - 例如单项选择题
        public int subjectNums;//总题数
        public int subjectValue = 0;//题型总分
    }

    @Override
    public void initWithJsonObject(JSONObject jsonObj) throws JSONException {
        try {
            int flag = jsonObj.getInt("_c");
            if (flag != 0) {
                this.errorMsg = jsonObj.getString("_m");
                return;
            }

            JSONObject infoObj = jsonObj.getJSONObject("info");

            this.taskId = infoObj.getString("id");
            this.taskName = infoObj.getString("name");
            this.startTime = infoObj.getLong("start");
            this.finishTime = infoObj.getLong("finish");
            this.allowTimes = infoObj.getInt("allow_times");
            this.times = infoObj.getInt("times");
            this.score = infoObj.getInt("score");
            this.subjectNum = infoObj.getInt("subject_num");
            this.canStart = infoObj.getInt("can_start");
            this.status = infoObj.getInt("status");
            JSONArray overView = infoObj.getJSONArray("overview");
            for (int i = 0; i < overView.length(); i++) {
                JSONObject overViewObj = overView.getJSONObject(i);
                SubjectHolder holder = new SubjectHolder();

                holder.subjectName = overViewObj.getString("name");
                JSONObject overViewinfoObj = overViewObj.getJSONObject("info");
                holder.subjectNums = overViewinfoObj.getInt("nums");
                holder.subjectValue = overViewinfoObj.getInt("scores");

                holderList.add(holder);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
