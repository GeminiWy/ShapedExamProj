package com.nd.shapedexamproj.util;

import android.content.Context;
import com.tming.common.net.TmingHttp;
import com.tming.common.net.TmingHttp.RequestCallback;

import java.util.HashMap;
import java.util.Map;
/**
 * 
 * <p>类微博工具类</p>
 * <p>Created by zll on 2014-7-11</p>
 */

public class TimelineUtil {
    
    private static TimelineUtil instance;
    
    public TimelineUtil(Context context) {
    }

    public static TimelineUtil getInstance(Context context) {
        if (instance == null) {
            synchronized (TimelineUtil.class) {
                if (instance == null) {
                    instance = new TimelineUtil(context);
                }
            }
        }
        return instance;
    }
    
    /**
     * 
     * @Title: deleteWeibo
     * @Description: 删除微博
     * @Since: 2014-5-13上午11:32:50
     * @param userid
     * @param timelineId
     */
    public void deleteWeibo(String userid, String timelineId,RequestCallback mDeleteRequestCallback) {
        String url = Constants.WEIBO_DELETE;
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("userid", userid);
        map.put("timelineid", timelineId);
        TmingHttp.asyncRequest(url, map, mDeleteRequestCallback);
    }
    
}
