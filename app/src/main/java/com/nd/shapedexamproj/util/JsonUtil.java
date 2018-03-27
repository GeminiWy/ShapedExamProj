package com.nd.shapedexamproj.util;

import com.tming.openuniversity.model.IJsonInitable;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yusongying on 2014/11/27.
 */
public class JsonUtil {

    /**
     * 解析json数组
     * @param jsonArray json数组
     * @param cls 实现 {@link com.tming.openuniversity.util.IJsonInitable} 接口的实体类
     * @return
     * @throws JSONException
     */
    public static <T> List<T> paraseJsonArray(JSONArray jsonArray, Class<T> cls) throws JSONException {
        List<T> datas = new ArrayList<T>();
        try {
            int len = jsonArray.length();
            for (int i = 0; i < len; i++) {
                IJsonInitable newInstance = (IJsonInitable) cls.newInstance();
                newInstance.initWithJsonObject(jsonArray.getJSONObject(i));
                datas.add((T) newInstance);
            }
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return datas;
    }

    /**
     * 检查java端接口返回状态码是否正确
     * @param jsonObject json数据
     * @return
     * @throws JSONException
     */
    public static boolean checkResultIsOK(JSONObject jsonObject) throws JSONException {
        int flag = jsonObject.getInt("flag");
        return flag == 1;
    }

    /**
     * 检查php端接口状态码是否都正确
     * @param jsonObject
     * @return
     * @throws JSONException
     */
    public static boolean checkPhpApiALLIsOK(JSONObject jsonObject) throws JSONException {
        if (jsonObject != null) {
            int phpFlag = jsonObject.getInt("code");
            return phpFlag == 1;
        }
        return false;
    }

    /**
     * 检查形考php端接口状态码是否都正确
     * @param jsonObject
     * @return
     * @throws JSONException
     */
    public static boolean checkXKPhpApiALLIsOK(JSONObject jsonObject) throws JSONException {
        if (jsonObject != null) {
            int phpFlag = jsonObject.getInt("_c");
            return phpFlag == 0;
        }
        return false;
    }
}
