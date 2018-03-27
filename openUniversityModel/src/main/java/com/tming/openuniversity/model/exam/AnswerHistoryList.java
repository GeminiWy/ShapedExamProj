package com.tming.openuniversity.model.exam;

import com.tming.openuniversity.model.IJsonInitable;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * 答题历史列表项
 * Created by zll on 2015/3/3.
 */
public class AnswerHistoryList implements IJsonInitable {
    public String recordId;//答题记录ID
    public String doTime;//第几次答题
    public long start;//答题时间戳
    public int status;//答题状态（整数） - 1=继续答题，2=已提交，3=正在评阅，4=已经退回，5=已经评阅, 6=已经过期
    public float score;//得分 - 还没有评阅默认为空
    public int total;//总条数

    public List<AnswerHistoryList> answerHistoryList = new ArrayList<AnswerHistoryList>();
    @Override
    public void initWithJsonObject(JSONObject jsonObj) throws JSONException {
        JSONArray jsonArray = jsonObj.getJSONArray("list");
        this.total = jsonObj.getInt("total");
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject listObj = jsonArray.getJSONObject(i);
            AnswerHistoryList answer = new AnswerHistoryList();
            answer.recordId = listObj.getString("id");
            answer.doTime = listObj.getString("dotime");
            answer.start = listObj.getLong("start");
            answer.status = listObj.getInt("status");
            answer.score = listObj.getInt("score");

            answerHistoryList.add(answer);
        }
    }
}
