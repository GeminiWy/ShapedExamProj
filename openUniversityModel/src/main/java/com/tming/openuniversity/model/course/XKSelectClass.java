package com.tming.openuniversity.model.course;

import com.tming.openuniversity.model.IJsonInitable;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * 选课班级
 * Created by yusongying on 2015/3/19.
 */
public class XKSelectClass implements IJsonInitable {

    private String id;
    private String name;
    private boolean isSys;
    private String dxTeach;
    private String pyTeach;


    @Override
    public void initWithJsonObject(JSONObject jsonObj) throws JSONException {
        id = jsonObj.getString("id");
        name = jsonObj.getString("name");
        isSys = jsonObj.getInt("is_sys") == 1;
        dxTeach = jsonObj.getString("dx_teach");
        pyTeach = jsonObj.getString("py_tech");
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isSys() {
        return isSys;
    }

    public void setSys(boolean isSys) {
        this.isSys = isSys;
    }

    public String getDxTeach() {
        return dxTeach;
    }

    public void setDxTeach(String dxTeach) {
        this.dxTeach = dxTeach;
    }

    public String getPyTeach() {
        return pyTeach;
    }

    public void setPyTeach(String pyTeach) {
        this.pyTeach = pyTeach;
    }
}
