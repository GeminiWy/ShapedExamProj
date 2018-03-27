package com.nd.shapedexamproj.model.playground;

import com.tming.openuniversity.model.IJsonInitable;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

/**
 * 微博图片信息
 * Created by yusongying on 2014/11/27.
 */
public class TimelineImageInfo implements Serializable, IJsonInitable {
    private String urlString;
    private int width;
    private int height;

    public String getUrlString() {
        return urlString;
    }

    public void setUrlString(String urlString) {
        this.urlString = urlString;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    @Override
    public void initWithJsonObject(JSONObject jsonObj) throws JSONException {
        urlString = jsonObj.getString("url");
        width = jsonObj.getInt("width");
        height = jsonObj.getInt("height");
    }
}
