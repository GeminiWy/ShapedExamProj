package com.tming.common;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;

import android.os.Looper;
import com.tming.common.cache.image.ImageCacheTool;
import com.tming.common.cache.net.TmingCacheHttp;
import com.tming.common.exception.CrashHandler;
import com.tming.common.util.Constants;
import com.tming.common.util.Log;
import com.tming.common.util.PhoneUtil;
import com.tming.common.util.VersionUpdate;

/**
 * <p>应用Application对象，可以提供全局的Context和主线程的Handler、添加网络状态监听、崩溃处理等</p>
 * <p><strong>如果没有工程中的Application对象没有继承该类，需要主动调用 {@link #initCommonApp(Context)}、{@link #initNetworkListener(Application)}</strong>
 * ，如果没有调用初始化，{@link ImageCacheTool}、{@link TmingCacheHttp}、{@link VersionUpdate} 都有可能崩溃
 * </p>
 * <p>Created by yusongying</p>
 */
public class CommonApp extends Application {

    private final static String LOG_TAG = "CommonApp";
    /**
     * 全局Context
     */
    private static Context sAppContext;
    /**
     * 全局Handler
     */
    private static Handler sAppHandler;
    /**
     * 单例
     */
    private static CommonApp sInstance;
    /**
     * 网络状态
     */
    public static int sNetWorkState = PhoneUtil.NETSTATE_UNKOWN;
    /**
     * 标识是否为wifi
     */
    public static boolean sNetWorkIsWifi = false;

    /**
     * 单例获取方法
     *
     * @return {@link CommonApp} 实例，有可能会是空
     */
    public static CommonApp getInstance() {
        return sInstance;
    }
    
    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(LOG_TAG, "CommonApp.onCreate()");
        initCommonApp(getApplicationContext());
        iniCrashHandler(getApplicationContext());
        initNetworkListener(this);
    }
    
    @Override
    public void onTerminate() {
        super.onTerminate();
        unregisterReceiver(netChangeBroadcastReceiver);
    }
    
    /**
     * 初始化 全局Context和Handler，以及ImageCacheTool、TmingCacheHttp
     *
     * @param appContext 全局Context
     */
    public static void initCommonApp(Context appContext) {
        if (sAppContext == null) {
            sAppContext = appContext;
            sAppHandler = new Handler();
        }
        if (sAppHandler == null) {
            // 判断是否在主线程中调用
            if (Looper.getMainLooper() != Looper.myLooper()) {
                throw new IllegalAccessError("Please call addItem method on main thread!");
            }
            sAppHandler = new Handler();
        }
        ImageCacheTool.getInstance();
        TmingCacheHttp.getInstance();
    }
    
    /**
     * 初始化网络状态监听 
     *
     * @param app {@link Application}
     */
    public static void initNetworkListener(Application app) {
        // 初始化判断网络状态
        onNetStateChange();
        // 网络变化监听
        IntentFilter netIntentFilter = new IntentFilter();
        netIntentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        app.registerReceiver(netChangeBroadcastReceiver, netIntentFilter);
    }
    
    /**
     * 初始化崩溃处理，并发送日志到默认服务器
     * 
     * @param appContext 全局安卓上下文
     */
    public static void iniCrashHandler(Context appContext) {
        CrashHandler crashHandler = CrashHandler.getInstance();
        // 注册crashHandler
        crashHandler.init(appContext);
        if (!Constants.DEBUG) {
            // 发送报告到默认服务端
            crashHandler.startSendCrashReportToDefault();
        }
    }
    
    /**
     * 网络变化广播接收器
     */
    private static BroadcastReceiver netChangeBroadcastReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
                onNetStateChange();
            }
        }
    };

    /**
     * 网络状态变化 
     */
    public static void onNetStateChange() {
        ConnectivityManager connectivityManager = (ConnectivityManager) sAppContext
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = connectivityManager.getActiveNetworkInfo();
        if (info != null && info.isAvailable()) {
            String name = info.getTypeName();
            sNetWorkState = PhoneUtil.NETSTATE_ENABLE;
            sNetWorkIsWifi = info.getType() == ConnectivityManager.TYPE_WIFI;
            Log.d(LOG_TAG, "onNetStateChange：" + name);
        } else {
            sNetWorkIsWifi = false;
            sNetWorkState = PhoneUtil.NETSTATE_DISABLE;
            Log.d(LOG_TAG, "onNetStateChange: NETSTATE_DISABLE");
        }
    }
    
    /**
     * 获取应用Context
     * 
     * @return {@link Context}
     */
    public static Context getAppContext() {
        return sAppContext;
    }

    /**
     * 获取全局Handler对象 
     *
     * @return {@linkplain Handler}
     */
    public static Handler getAppHandler() {
        return sAppHandler;
    }
}
