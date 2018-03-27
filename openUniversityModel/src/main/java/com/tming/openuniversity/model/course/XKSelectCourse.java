package com.tming.openuniversity.model.course;

import com.tming.openuniversity.model.IJsonInitable;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * 选课课程类
 * Created by yusongying on 2015/3/18.
 */
public class XKSelectCourse implements IJsonInitable {

    private String id;
    private String name;
    private String cover;
    private String notice;
    private String scheme;
    private int shortestDays;
    private int suggestDays;
    private boolean isSelected;

    @Override
    public void initWithJsonObject(JSONObject jsonObj) throws JSONException {
        id = jsonObj.getString("id");
        name = jsonObj.getString("name");
        cover = jsonObj.getString("cover");
        notice = jsonObj.getString("notice");
        scheme = jsonObj.getString("scheme");
        shortestDays = jsonObj.getInt("shortest_days");
        suggestDays = jsonObj.getInt("suggest_days");
        isSelected = jsonObj.getInt("is_select") == 1;
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

    public String getCover() {
        return cover;
    }

    public void setCover(String cover) {
        this.cover = cover;
    }

    public String getNotice() {
        return notice;
    }

    public void setNotice(String notice) {
        this.notice = notice;
    }

    public String getScheme() {
        return scheme;
    }

    public void setScheme(String scheme) {
        this.scheme = scheme;
    }

    public int getShortestDays() {
        return shortestDays;
    }

    public void setShortestDays(int shortestDays) {
        this.shortestDays = shortestDays;
    }

    public int getSuggestDays() {
        return suggestDays;
    }

    public void setSuggestDays(int suggestDays) {
        this.suggestDays = suggestDays;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean isSelected) {
        this.isSelected = isSelected;
    }
}
