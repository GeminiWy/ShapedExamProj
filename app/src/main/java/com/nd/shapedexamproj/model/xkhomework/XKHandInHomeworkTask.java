package com.nd.shapedexamproj.model.xkhomework;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import com.nd.shapedexamproj.App;
import com.nd.shapedexamproj.R;
import com.nd.shapedexamproj.util.Constants;
import com.nd.shapedexamproj.util.PhpApiUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * 提交作业或保存作业到服务端
 * Created by zll on 2015/1/26.
 */
public class XKHandInHomeworkTask extends AsyncTask<Void, Void, Boolean> {

    protected String mHomeworkId;
    protected Context mContext;
    protected boolean isHandIn;//是否交卷,如果是false，则只进行保存作业不提交
    protected int type;//1=保存答案, 2=提交答案

    public XKHandInHomeworkTask(Context mContext, String mHomeworkId, boolean isHandIn) {
        this.mContext = mContext;
        this.mHomeworkId = mHomeworkId;
        this.isHandIn = isHandIn;
        if (isHandIn) {
            type = 2;
        } else {
            type = 1;
        }
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected Boolean doInBackground(Void... voids) {
        String strResponse = null;
        // 组装作业
        XKHomework homework = XKHomework.readHomework(mHomeworkId);
        homework.readUserAnswer();
        long costTime = homework.getCostTimeMillis() / 1000;
        JSONObject userAnswers = homework.getUserAnswer();

        Map<String,Object> params = new HashMap<String, Object>();
        params.put("stu_id", App.getsStudentId());
        params.put("answer_id",mHomeworkId);
        params.put("type",type);
        params.put("answers",userAnswers);

        JSONObject jsonObject = PhpApiUtil.getJSONObject(Constants.XK_SAVE_SUBJECTS,params);
        int intResult = parseResult(jsonObject);
        boolean isSendSuccess = loadResult(intResult);

        return isSendSuccess;
    }

    @Override
    protected void onProgressUpdate(Void... values) {
        super.onProgressUpdate(values);
    }

    @Override
    protected void onPostExecute(Boolean aBoolean) {
        if (aBoolean && isHandIn) {
            boolean hasDoneHomework = XKHomework.checkUserHasDoHomework(mHomeworkId);
            if (hasDoneHomework) {
                XKHomework.clearUserAnswer(mHomeworkId);
            }
            App.getAppHandler().post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(mContext, "交卷成功", Toast.LENGTH_SHORT).show();
                }
            });
        } else if (aBoolean && !isHandIn) {
            App.getAppHandler().post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(mContext, "保存成功", Toast.LENGTH_SHORT).show();
                }
            });
        }
        super.onPostExecute(aBoolean);
    }

    /**
     * 解析数据
     * @param jobj
     * @return
     */
    protected int parseResult(JSONObject jobj) {
        if (jobj == null) {
            App.getAppHandler().post(new Runnable() {
                public void run() {
                    Toast.makeText(mContext,mContext.getResources().getString(R.string.net_error), Toast.LENGTH_SHORT).show();
                }
            });
            return 0;
        }
        try {
            int code = jobj.getInt("_c");
            if (code != 0) {
                final String errorMsg = jobj.getString("_m");
                App.getAppHandler().post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(mContext,errorMsg, Toast.LENGTH_SHORT).show();
                    }
                });
                return 0;
            }
            return jobj.getInt("result");

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * @param result
     */
    protected boolean loadResult(int result){
        if(result == 1){//操作成功
            return true;
        } else {	//操作失败
            return false;
        }

    }
}
