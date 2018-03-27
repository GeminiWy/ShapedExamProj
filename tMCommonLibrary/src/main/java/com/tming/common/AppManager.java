package com.tming.common;

import java.lang.ref.WeakReference;
import java.util.Stack;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;

/**
 * <p>
 * 应用程序Activity管理类：用于Activity管理和应用程序退出
 * </p>
 * <p>
 * Created by Abay Zhuang on 2014-5-19
 * </p>
 */
public class AppManager {

    /**
     * 保存Activity的stack
     */
    private static Stack<WeakReference<Activity>> activityStack = new Stack<WeakReference<Activity>>();
    
    /**
     * 单例
     */
    private static AppManager instance;

    private AppManager() {}

    /**
     * 单一实例
     */
    public static AppManager getAppManager() {
        if (instance == null) {
            instance = new AppManager();
        }
        return instance;
    }

    /**
     * 添加Activity到堆栈
     */
    public void addActivity(Activity activity) {
        if (activity != null)
            activityStack.add(new WeakReference<Activity>(activity));
    }

    /**
     * 获取当前Activity（堆栈中最后一个压入的）
     */
    public WeakReference<Activity> currentActivity() {
        return activityStack.lastElement();
    }

    /**
     * 结束指定类名的Activity
     */
    public void finishActivity(Class<?> cls) {
        for (WeakReference<Activity> activityWeakReference : activityStack) {
            Activity activity = activityWeakReference.get();
            if (activity != null && activity.getClass().equals(cls)) {
                activityStack.remove(activityWeakReference);
                activity.finish();
            }
        }
    }

    /**
     * 结束所有Activity
     */
    public void finishAllActivity() {
        while (!activityStack.isEmpty()) {
            WeakReference<Activity> activityWeakReference = activityStack.pop();
            Activity activity = null;
            if (activityWeakReference != null && (activity = activityWeakReference.get()) != null) {
                activity.finish();
            }
        }
    }

    /**
     * 退出应用程序
     */
    public void AppExit(Context context) {
        try {
            finishAllActivity();
            ActivityManager activityMgr = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            activityMgr.restartPackage(context.getPackageName());
            System.exit(0);
        } catch (Exception e) {}
    }

    public void removeActivity(Activity act) {
        for (WeakReference<Activity> activityWeakReference : activityStack) {
            Activity activity = activityWeakReference.get();
            if (activity != null && activity == act) {
                activityStack.remove(activityWeakReference);
                act.finish();
                break;
            }
        }
    }
}