package com.tming.openuniversity.model;

import org.json.JSONException;
import org.json.JSONObject;

public interface IJsonInitable {
    public void initWithJsonObject(JSONObject jsonObj) throws JSONException;
}
