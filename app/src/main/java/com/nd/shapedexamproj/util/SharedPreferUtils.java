package com.nd.shapedexamproj.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;


/**
 * 
 * @ClassName: SharedPreferUtils
 * @Title:
 * @Description:获取getSharedPreferences的工具类
 * @Author:XueWenJian
 * @Since:2014年5月15日20:39:44
 * @Version:1.0
 */
public class SharedPreferUtils {

    public static SharedPreferences getSharedPreferences(Context context)
    {
        return getSharedPreferences(context,Constants.SHARE_PREFERENCES_NAME);
    }
    
    public static SharedPreferences getSharedPreferences(Context context,String name)
    {
        SharedPreferences sharedPreferences = context.getSharedPreferences(name, Context.MODE_PRIVATE);

        return sharedPreferences;
    }

    public static Editor getEditor(Context context)
    {
        return getSharedPreferences(context).edit();
    }
    
    public static boolean getBoolean(Context context, final String key, boolean defValue)
    {
    	return getSharedPreferences(context).getBoolean(key, defValue);
    }
    
    /**
     * 获得SharedPreferences中的boolean值，默认值为true
     * @param context
     * @param key
     * @return
     */
    public static boolean getBoolean(Context context, final String key)
    {
    	return getSharedPreferences(context).getBoolean(key, true);
    }
    
    public static String getString(Context context, final String key)
    {
    	return getSharedPreferences(context).getString(key, "0");
    }
    
    public static int getInt(Context context, final String key)
    {
    	return getSharedPreferences(context).getInt(key, 0);
    }
    
    public static int getInt(Context context, final String key, int defval)
    {
    	return getSharedPreferences(context).getInt(key, defval);
    }
    
}
