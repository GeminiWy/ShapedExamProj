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
 * 重新开始形考
 * Created by zll on 2015/3/20.
 */
public class XKRestartWarmupTask extends AsyncTask<Void, Void, Boolean> {
    protected Context context;
    protected String warmupId;

    public XKRestartWarmupTask(Context context,String warmupId) {
        this.context = context;
        this.warmupId = warmupId;
    }

    @Override
    protected Boolean doInBackground(Void... voids) {

        Map<String,Object> params = new HashMap<String, Object>();
        params.put("stu_id", App.getsStudentId());
        params.put("id",warmupId);

        JSONObject jsonObject = PhpApiUtil.getJSONObject(Constants.XK_WARM_UP_RESTART, params);
        int intResult = parseResult(jsonObject);
        boolean isSendSuccess = loadResult(intResult);

        return isSendSuccess;
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
                    Toast.makeText(context, context.getResources().getString(R.string.net_error), Toast.LENGTH_SHORT).show();
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
                        Toast.makeText(context,errorMsg, Toast.LENGTH_SHORT).show();
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
