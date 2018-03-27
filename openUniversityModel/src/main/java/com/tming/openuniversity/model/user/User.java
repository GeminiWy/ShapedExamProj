package com.tming.openuniversity.model.user;

import com.tming.openuniversity.model.IJsonInitable;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by zll on 2015/3/5.
 */
public class User implements IJsonInitable {

    public String token;//登录令牌
    public String uid;//用户ID
    public String stu_id;//学生ID - 如果是老师，这个值为空
    public String name;//用户姓名
    public int type;//用户类型（整数）：0 =教师，1=学生

    @Override
    public void initWithJsonObject(JSONObject jsonObj) throws JSONException {
        token = jsonObj.getString("token");
        JSONObject userInfo = jsonObj.getJSONObject("user_info");
        uid = userInfo.getString("uid");
        stu_id = userInfo.getString("stu_id");
        name = userInfo.getString("name");
        type = userInfo.getInt("type");
    }
}
