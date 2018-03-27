package com.tming.common.download.service;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import com.tming.common.download.DownloadState;
import com.tming.common.util.Helper;
import com.tming.common.util.Log;
import com.tming.common.util.PhoneUtil;

import java.io.File;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by yusongying on 2015/1/4.
 */
public class DownloadService extends Service {

    private final static String TAG = "DownloadService";
    public final static String INTENT_COMMAND_EXTRA_KEY = "commend";
    public final static String INTENT_URL_STRING_EXTRA_KEY = "download_url_string";
    public final static String INTENT_DOWNLOADED_PATH_EXTRA_KEY = "downloaded_file_path";
    public final static int COMMAND_START = 1;
    public final static int COMMAND_PAUSE = 2;
    public final static int COMMAND_RESUME = 3;
    public final static int COMMAND_CANCEL = 4;

    private RemoteCallbackList<IDownloadServiceCallback> mCallbacks = new RemoteCallbackList<IDownloadServiceCallback>();
    private ReentrantLock mCallbacksLock = new ReentrantLock();
    private Map<String, DownloadTask> mDownloadTasks = Collections.synchronizedMap(new HashMap<String, DownloadTask>());

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate");
    }

    @SuppressLint("NewApi")
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (PhoneUtil.getSDKVersionInt() >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            Log.d(TAG, "onDestroy,mDownloadTasks:" + mDownloadTasks.size() + ",mCallbacks:" + mCallbacks.getRegisteredCallbackCount());
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        int command = intent.getIntExtra(INTENT_COMMAND_EXTRA_KEY, 0);
        String urlString =  intent.getStringExtra(INTENT_URL_STRING_EXTRA_KEY);
        DownloadTask task;
        switch (command) {
            case COMMAND_RESUME:
            case COMMAND_START:
                if (!mDownloadTasks.containsKey(urlString)) {
                    String downloadedPath = intent.getStringExtra(INTENT_DOWNLOADED_PATH_EXTRA_KEY);
                    if (downloadedPath == null) {
                        task = new DownloadTask(this, startId, urlString);
                    } else {
                        task = new DownloadTask(this, startId, urlString, downloadedPath);
                    }
                    mDownloadTasks.put(urlString, task);
                    task.execute();
                    return START_REDELIVER_INTENT;
                }
                return START_NOT_STICKY;
            case COMMAND_PAUSE:
                task = mDownloadTasks.get(urlString);
                if (task != null) {
                    task.cancel(false);// 仅结束下载线程
                    mDownloadTasks.remove(urlString);
                }
                return START_NOT_STICKY;
            case COMMAND_CANCEL:
                task = mDownloadTasks.get(urlString);
                if (task != null) {
                    task.cancel(true);// 结束下载线程，并删除下载文件
                    mDownloadTasks.remove(urlString);
                } else {// 删除临时文件
                    File tempFile = new File(DownloadTask.getDownloadTempFilePath(urlString));
                    if (tempFile != null) {
                        tempFile.delete();
                    }
                }
                return START_NOT_STICKY;
            default:
                return START_NOT_STICKY;
        }
    }

    private IDownloadService.Stub mBinder = new IDownloadService.Stub() {


        @Override
        public int getDownloadState(String urlString) throws RemoteException {
            DownloadTask downloadTask = mDownloadTasks.get(urlString);
            if (downloadTask != null && downloadTask.isExecuting()) {
                return DownloadState.DOWNLOADING;
            }
            File file = new File(DownloadTask.getDownloadedFilePath(urlString));
            if (file.exists()) {
                return DownloadState.DOWNLOADED;
            }
            File tempFile = new File(DownloadTask.getDownloadTempFilePath(urlString));
            if (tempFile.exists()) {
                return DownloadState.PAUSE;
            }
            return DownloadState.UNKNOWN;
        }

        @Override
        public void registDownloadCallback(IDownloadServiceCallback callback) throws RemoteException {
            mCallbacksLock.lock();
            mCallbacks.register(callback);
            mCallbacksLock.unlock();
        }

        @Override
        public void unregistDownloadCallback(IDownloadServiceCallback callback) throws RemoteException {
            mCallbacksLock.lock();
            mCallbacks.unregister(callback);
            mCallbacksLock.unlock();
        }

        @Override
        public List<String> getDownloadingUrlStrings() throws RemoteException {
            return new ArrayList<String>(mDownloadTasks.keySet());
        }
    };

    private void taskExit(String urlString, int startId) {
        mDownloadTasks.remove(urlString);
        stopSelf(startId);
    }

    public void taskCallUpdateProgress(String urlString, long downloadedSize, long contentLength) {
        mCallbacksLock.lock();
        int count = mCallbacks.beginBroadcast();
        for (int i = 0; i < count; i++) {
            try {
                mCallbacks.getBroadcastItem(i).downloadUpdateProgress(urlString, downloadedSize, contentLength);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        mCallbacks.finishBroadcast();
        mCallbacksLock.unlock();
    }

    public void taskCallFinish(String urlString, String downloadedFilePath, int taskStartId) {
        mCallbacksLock.lock();
        taskExit(urlString, taskStartId);
        int count = mCallbacks.beginBroadcast();
        for (int i = 0; i < count; i++) {
            try {
                mCallbacks.getBroadcastItem(i).downloadFinish(urlString, downloadedFilePath);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        mCallbacks.finishBroadcast();
        mCallbacksLock.unlock();
    }

    public void taskCallFail(String urlString, int errorCode, String errorMsg, int taskStartId) {
        mCallbacksLock.lock();
        taskExit(urlString, taskStartId);
        int count = mCallbacks.beginBroadcast();
        for (int i = 0; i < count; i++) {
            try {
                mCallbacks.getBroadcastItem(i).downloadFail(urlString, errorCode, errorMsg);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        mCallbacks.finishBroadcast();
        mCallbacksLock.unlock();
    }

    public void taskCallStateChange(String urlString, int state, int taskStartId) {
        mCallbacksLock.lock();
        if (state != DownloadState.DOWNLOADING) {
            taskExit(urlString, taskStartId);
        }
        int count = mCallbacks.beginBroadcast();
        for (int i = 0; i < count; i++) {
            try {
                mCallbacks.getBroadcastItem(i).downloadStateChange(urlString, state);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        mCallbacks.finishBroadcast();
        mCallbacksLock.unlock();
    }
}
