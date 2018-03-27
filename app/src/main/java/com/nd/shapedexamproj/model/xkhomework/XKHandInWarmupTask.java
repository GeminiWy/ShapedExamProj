package com.nd.shapedexamproj.model.xkhomework;

import android.content.Context;

import com.nd.shapedexamproj.App;
import com.nd.shapedexamproj.util.Constants;
import com.nd.shapedexamproj.util.PhpApiUtil;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * 保存或提交热身考试
 * Created by zll on 2015/3/20.
 */
public class XKHandInWarmupTask extends XKHandInHomeworkTask {

    public XKHandInWarmupTask(Context mContext, String mHomeworkId, boolean isHandIn) {
        super(mContext, mHomeworkId, isHandIn);
    }

    @Override
    protected Boolean doInBackground(Void... voids) {

        // 组装作业
        XKWarmup homework = XKWarmup.readHomework(mHomeworkId);
        homework.readUserAnswer();
        long costTime = homework.getCostTimeMillis() / 1000;
        JSONObject userAnswers = homework.getUserAnswer();

        Map<String,Object> params = new HashMap<String, Object>();
        params.put("stu_id", App.getsStudentId());
        params.put("id",mHomeworkId);
        params.put("type",type);
        params.put("answers",userAnswers);

        JSONObject jsonObject = PhpApiUtil.getJSONObject(Constants.XK_WARM_UP_SAVE_SUBJECTS, params);
        int intResult = parseResult(jsonObject);
        boolean isSendSuccess = loadResult(intResult);

        return isSendSuccess;
    }
}
