package com.nd.shapedexamproj.model.homework;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import com.nd.shapedexamproj.App;
import com.nd.shapedexamproj.R;
import com.nd.shapedexamproj.net.ServerApi;
import com.tming.common.net.TmingHttp;
import com.tming.common.net.TmingHttpException;
import com.tming.common.net.TmingResponse;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * 提交作业或保存作业到服务端
 * Created by zll on 2015/1/26.
 */
public class HandInHomeworkTask extends AsyncTask<Void, Void, Boolean> {

    private String mHomeworkId;
    private Context mContext;
    private boolean isHandIn;//是否交卷,如果是false，则只进行保存作业不提交


    public HandInHomeworkTask(Context mContext,String mHomeworkId,boolean isHandIn) {
        this.mContext = mContext;
        this.mHomeworkId = mHomeworkId;
        this.isHandIn = isHandIn;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected Boolean doInBackground(Void... voids) {
        String strResponse = null;
        // 组装作业
        Homework homework = Homework.readHomework(mHomeworkId);
        homework.readUserAnswer();
        long costTime = homework.getCostTimeMillis() / 1000;
        JSONObject userAnswers = homework.getUserAnswer();

        try {
            TmingResponse sendResponse = TmingHttp.tmingHttpRequest(ServerApi.Homework.sentWork(mHomeworkId,costTime,userAnswers),null);
            strResponse = sendResponse.asString();
        } catch (TmingHttpException e) {
            e.printStackTrace();
        }
        String strResult = parseResult(strResponse);
        boolean isSendSuccess = loadResult(strResult);
        if (isSendSuccess && isHandIn) {
            String submitResult = null;
            String submitResponse = null;
            try {       //交卷
                TmingResponse response = TmingHttp.tmingHttpRequest(ServerApi.Homework.submitWork(mHomeworkId), null);
                submitResponse = response.asString();

            } catch (TmingHttpException e) {
                e.printStackTrace();
            }
            submitResult = parseResult(submitResponse);
            boolean isSubmitSuccess = loadResult(submitResult);
            if (isSubmitSuccess) {
                return true;
            } else {
                return false;
            }
        } else if (isSendSuccess && !isHandIn) {
            return true;
        }

        return false;
    }

    @Override
    protected void onProgressUpdate(Void... values) {
        super.onProgressUpdate(values);
    }

    @Override
    protected void onPostExecute(Boolean aBoolean) {
        if (aBoolean && isHandIn) {
            boolean hasDoneHomework = Homework.checkUserHasDoHomework(mHomeworkId);
            if (hasDoneHomework) {
                Homework.clearUserAnswer(mHomeworkId);
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
        } else if (!aBoolean) {
            App.getAppHandler().post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(mContext, "提交失败，请稍后再试", Toast.LENGTH_SHORT).show();
                }
            });
        }
        super.onPostExecute(aBoolean);
    }

    /**
     * 解析数据
     * @param result
     * @return
     */
    private String parseResult(String result) {
        String flag2 = "";
        if (result == null) {
            App.getAppHandler().post(new Runnable() {
                public void run() {
                    Toast.makeText(mContext,mContext.getResources().getString(R.string.net_error), Toast.LENGTH_SHORT).show();
                }
            });
            return flag2;
        }
        try {
            JSONObject jobj = new JSONObject(result);

            int flag = jobj.getInt("flag");
            if (flag != 1) {
                final String msg = jobj.getString("msg");
                App.getAppHandler().post(new Runnable() {
                    public void run() {
                        Toast.makeText(mContext, msg, Toast.LENGTH_SHORT).show();
                    }
                });
                return null;
            }
            int code = jobj.getJSONObject("res").getInt("code");
            if (code != 1) {
                dealWithCode(code);
                return null;
            }
            JSONObject info_jobj = jobj.getJSONObject("res").getJSONObject("data").getJSONObject("info");
            flag2 = info_jobj.getString("flag");

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return flag2;
    }

    private void dealWithCode(int code) {
        switch(code){
            case -7 :
                App.getAppHandler().post(new Runnable() {

                    @Override
                    public void run() {
                        Toast.makeText(mContext, "获取不到答题信息", Toast.LENGTH_SHORT).show();
                    }
                });
                break ;
            case -8:
                App.getAppHandler().post(new Runnable() {

                    @Override
                    public void run() {
                        Toast.makeText(mContext, "获取不到试卷信息", Toast.LENGTH_SHORT).show();
                    }
                });
                break;
            case -9:
                App.getAppHandler().post(new Runnable() {

                    @Override
                    public void run() {
                        Toast.makeText(mContext, "次数已满或已过期", Toast.LENGTH_SHORT).show();
                    }
                });
                break;
            default:
                break ;
        }
    }

    /**
     * @param str
     */
    private boolean loadResult(String str){
        if(str != null && !str.equals("") && str.equals("y")){//操作成功
            return true;
        } else {	//操作失败
            return false;
        }

    }
}
