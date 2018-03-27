package com.nd.shapedexamproj.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.tming.common.util.Log;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.List;

/**
 * 
 * @ClassName: JsonParseObject
 * @Title:
 * @Description:Json数据转换类
 * @Author:XueWenJian
 * @Since:2014年5月21日16:23:59
 * @Version:1.0
 */
public class JsonParseObject<T> {

    private final static String TAG = "JsonParseObject";
    private int flag = 0;
    private JSONObject resJs;
    private String errMsg;
    private JSONArray resListJs;
    private T resourse;
    private List<T> resourseList;

    public int getFlag() {
        return flag;
    }

    public void setFlag(int flag) {
        this.flag = flag;
    }

    public JSONObject getResJs() {
        if (null != resJs) {
            return resJs;
        } else {
            return new JSONObject();
        }
    }

    public void setResJs(JSONObject resJs) {
        this.resJs = resJs;
    }

    public JSONArray getResListJs() {
        return resListJs;
    }

    public void setResListJs(JSONArray resListJs) {
        this.resListJs = resListJs;
    }

    public static JsonParseObject parseJson(String respones) {
        Log.i(TAG, respones);
        JsonParseObject jsonParse = new JsonParseObject();
        try {
            JSONObject responesJs = new JSONObject(respones);
            jsonParse.flag = responesJs.getInt("flag");
            if (Constants.SUCCESS_MSG == jsonParse.flag) {
                if (responesJs.isNull("res")) {
                    JSONObject resJs = new JSONObject();
                    resJs.put("res", "");
                    jsonParse.setResJs(resJs);
                } else {
                    jsonParse.setResJs(responesJs.getJSONObject("res"));
                }
            } else {
                jsonParse.errMsg = responesJs.optString("msg");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonParse;
    }

    /*public static JsonParseObject parseJson(String respones) {
        Log.i(TAG, respones);
        JsonParseObject jsonParse = new JsonParseObject();
        try {
            JSONObject responesJs = new JSONObject(respones);
            jsonParse.flag = responesJs.getInt("flag");
            if (Constants.SUCCESS_MSG == jsonParse.flag) {
                JSONObject resJs = responesJs.getJSONObject("res");
                jsonParse.code = resJs.getInt("code");
                jsonParse.dataJs = resJs.optJSONObject("data");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonParse;
    }*/

    /**
     * 
     * @Title: getParseObject
     * @Description: 将json数据转化为实体
     * @param @param respones json数据
     * @param @param entityClass 实体的类型 e.g UserLoginEntity.class
     * @param @return
     * @return JsonParseObject<T> 返回类型
     * @throws
     */
    public static <T> JsonParseObject<T> getParseObject(String respones, Class<T> entityClass) {
        Log.i(TAG, respones);
        JsonParseObject<T> jsonParse = new JsonParseObject<T>();
        try {
            JSONObject responesJs = new JSONObject(respones);
            jsonParse.flag = responesJs.getInt("flag");
            if (Constants.SUCCESS_MSG == jsonParse.flag) {
                if (responesJs.isNull("res")) {
                    JSONObject resJs = new JSONObject();
                    resJs.put("res", "");
                    jsonParse.setResJs(resJs);
                } else {
                    jsonParse.setResJs(responesJs.getJSONObject("res"));
                }
            } else {
                jsonParse.errMsg = responesJs.optString("msg");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Gson gson = new GsonBuilder().create();
        T entity = gson.fromJson(jsonParse.getResJs().toString(), entityClass);
        jsonParse.resourse = entity;
        return jsonParse;
    }

    /**
     * 
     * @Title: getParseObjectList
     * @Description: 将json数据转化为 List数据
     * @param @param respones json数据
     * @param @param typenew e.g TypeToken<List<SignRecodeEntity>>()
     *        {}.getType()
     * @param @return
     * @return JsonParseObject<E> 返回类型
     * @throws
     */
    public static <E> JsonParseObject<E> getParseObjectList(String respones, Type type) {
        Log.i(TAG, respones);
        JsonParseObject<E> jsonParse = new JsonParseObject<E>();
        try {
            JSONObject responesJs = new JSONObject(respones);
            jsonParse.flag = responesJs.getInt("flag");
            if (Constants.SUCCESS_MSG == jsonParse.flag) {
                if (responesJs.isNull("res")) {
                    jsonParse.setResListJs(new JSONArray());
                } else {
                    jsonParse.setResListJs(responesJs.getJSONArray("res"));
                }
            } else {
                jsonParse.errMsg = responesJs.optString("msg");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Gson gson = new GsonBuilder().create();
        List<E> entityList = gson.fromJson(jsonParse.getResListJs().toString(), type);
        jsonParse.resourseList = entityList;
        return jsonParse;
    }

    public static JsonParseObject parseJsonList(String respones) {
        Log.i(TAG, respones);
        JsonParseObject jsonParse = new JsonParseObject();
        try {
            JSONObject responesJs = new JSONObject(respones);
            jsonParse.flag = responesJs.getInt("flag");
            if (Constants.SUCCESS_MSG == jsonParse.flag) {
                if (responesJs.isNull("res")) {
                    jsonParse.setResListJs(new JSONArray());
                } else {
                    jsonParse.setResListJs(responesJs.getJSONArray("res"));
                }
            } else {
                jsonParse.errMsg = responesJs.optString("msg");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonParse;
    }

    /**
     * 
     * @Title: getErrMsg
     * @Description: 返回错误信息
     * @param @return
     * @return String 返回类型
     * @throws
     */
    public String getErrMsg() {
        return errMsg;
    }

    /**
     * 
     * @Title: isSuccess
     * @Description: 消息是否成功
     * @param @return
     * @return boolean 返回类型
     * @throws
     */
    public boolean isSuccess() {
        return flag == Constants.SUCCESS_MSG;
    }

    public T getResourse() {
        return resourse;
    }

    public void setResourse(T resourse) {
        this.resourse = resourse;
    }

    public List<T> getResourseList() {
        return resourseList;
    }

    public void setResourseList(List<T> resourseList) {
        this.resourseList = resourseList;
    }
}
