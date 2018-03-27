package com.tming.openuniversity.model.stu;

import com.tming.openuniversity.model.IJsonInitable;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * Created by zll on 2015/3/12.
 */
public class Student implements IJsonInitable {

    public String studentId;//学生id
    public String studentName;//用户姓名
    public String studentNum;//学号
    public String idNum;//身份证
    public String jxd;//教学点名称
    public String pro;//专业名称
    public String avatar;//头像
    public int avatarStatus;//头像审核状态（整数） - 1=审核通过，2=审核不通过，3=待审核，
    public String cls_name;//班级名称

    @Override
    public void initWithJsonObject(JSONObject jsonObj) throws JSONException {
        JSONObject userInfo = jsonObj.getJSONObject("user_info");
        studentId = userInfo.getString("stu_id");
        studentName = userInfo.getString("name");
        studentNum = userInfo.getString("stunum");
        idNum = userInfo.getString("idnum");
        jxd = userInfo.getString("jxd");
        pro = userInfo.getString("pro");
        avatar = userInfo.getString("avatar");
        avatarStatus = userInfo.getInt("avatar_status");
        cls_name = userInfo.getString("cls_name");
    }
}
