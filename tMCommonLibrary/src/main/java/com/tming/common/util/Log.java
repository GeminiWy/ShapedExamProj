package com.tming.common.util;


/**
 * {@linkplain android.util.Log}
 */
public final class Log {

    public static final boolean LOG = Constants.DEBUG;

    /**
     * {@linkplain android.util.Log#v(String, String)}
     */
    public static int v(String tag, String msg) {
        if (LOG)
            return android.util.Log.v(tag, msg);
        return 0;
    }
    
    /**
     * {@linkplain android.util.Log#v(String, String, Throwable)}
     */
    public static int v(String tag, String msg, Throwable tr) {
        if (LOG) return android.util.Log.v(tag, msg, tr);
        return 0;
    }
    
    /**
     * {@linkplain android.util.Log#d(String, String)}
     */
    public static int d(String tag, String msg) {
        if (LOG) return android.util.Log.d(tag, msg);
        return 0;
    }
    
    /**
     * {@linkplain android.util.Log#d(String, String, Throwable)}
     */
    public static int d(String tag, String msg, Throwable tr) {
        if (LOG) return android.util.Log.d(tag, msg, tr);
        return 0;
    }
    
    /**
     * {@linkplain android.util.Log#i(String, String)}
     */
    public static int i(String tag, String msg) {
        if (LOG) return android.util.Log.i(tag, msg);
        return 0;
    }
    
    /**
     * {@linkplain android.util.Log#i(String, String, Throwable)}
     */
    public static int i(String tag, String msg, Throwable tr) {
        if (LOG) return android.util.Log.i(tag, msg, tr);
        return 0;
    }
    
    /**
     * {@linkplain android.util.Log#w(String, String)}
     */
    public static int w(String tag, String msg) {
        if (LOG) return android.util.Log.w(tag, msg);
        return 0;
    }
    
    /**
     * {@linkplain android.util.Log#w(String, String, Throwable)}
     */
    public static int w(String tag, String msg, Throwable tr) {
        if (LOG) return android.util.Log.w(tag, msg, tr);
        return 0;
    }
    
    /**
     * {@linkplain android.util.Log#w(String, Throwable)}
     */
    public static int w(String tag, Throwable tr) {
        if (LOG) return android.util.Log.w(tag, tr);
        return 0;
    }
    
    /**
     * {@linkplain android.util.Log#e(String, String)}
     */
    public static int e(String tag, String msg) {
        if (LOG) return android.util.Log.e(tag, msg);
        return 0;
    }
    
    /**
     * {@linkplain android.util.Log#e(String, String, Throwable)}
     */
    public static int e(String tag, String msg, Throwable tr) {
        if (LOG) return android.util.Log.e(tag, msg, tr);
        return 0;
    }
    
    /**
     * {@linkplain android.util.Log#wtf(String, String)}
     */
    public static int wtf(String tag, String msg) {
        if (LOG) return android.util.Log.wtf(tag, msg);
        return 0;
    }
    
    /**
     * {@linkplain android.util.Log#wtf(String, Throwable)}
     */
    public static int wtf(String tag, Throwable tr) {
        if (LOG) return android.util.Log.wtf(tag, tr);
        return 0;
    }
    
    /**
     * {@linkplain android.util.Log#e(String, String)}
     */
    public static int wtf(String tag, String msg, Throwable tr) {
        if (LOG) return android.util.Log.wtf(tag, msg, tr);
        return 0;
    }
    
    /**
     * {@linkplain android.util.Log#println(int, String, String)}
     */
    public static int println(int priority, String tag, String msg) {
        if (LOG) return android.util.Log.println(priority, tag, msg);
        return 0;
    }
    
    /**
     * {@linkplain android.util.Log#getStackTraceString(Throwable)}
     */
    public static String getStackTraceString(Throwable tr) {
        return android.util.Log.getStackTraceString(tr);
    }
}
