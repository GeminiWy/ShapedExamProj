package com.tming.common.download;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import com.tming.common.CommonApp;
import com.tming.common.download.service.DownloadService;
import com.tming.common.download.service.DownloadTask;
import com.tming.common.download.service.IDownloadService;
import com.tming.common.download.service.IDownloadServiceCallback;

import java.io.File;
import java.util.List;

/**
 * Created by yusongying on 2015/1/5.
 */
public class DownloadServiceApiImpl extends IDownloadServiceCallback.Stub implements IDownloadServiceApi, ServiceConnection {

    private IDownloadCallback mCallback;
    private IDownloadService mServiceInterface;

    public DownloadServiceApiImpl(Context context, IDownloadCallback callback) {
        mCallback = callback;

        Intent intent = new Intent(context, DownloadService.class);
        context.bindService(intent, this, Context.BIND_AUTO_CREATE);
    }

    @Override
    public int getDownloadState(String urlString) throws RemoteException {
        return mServiceInterface.getDownloadState(urlString);
    }

    @Override
    public long getDownloadedSize(String urlString) {
        File tempFile = new File(DownloadTask.getDownloadTempFilePath(urlString));
        return tempFile.length();
    }

    @Override
    public File getDownloadedFile(String urlString) {
        return new File(DownloadTask.getDownloadedFilePath(urlString));
    }

    @Override
    public long getDownloadContentLength(String urlString) {
        return DownloadTask.getDownloadedFileSize(urlString);
    }
    
    @Override
    public List<String> getDownloadingUrlStrings() throws RemoteException {
        return mServiceInterface.getDownloadingUrlStrings();
    }

    @Override
    public void destroy() {
        try {
            mServiceInterface.unregistDownloadCallback(this);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void downloadUpdateProgress(final String urlString, final long downloadedSize, final long contentLength) throws RemoteException {
        CommonApp.getAppHandler().post(new Runnable() {
            @Override
            public void run() {
                mCallback.downloadUpdateProgress(urlString, downloadedSize, contentLength);
            }
        });
    }

    @Override
    public void downloadFinish(final String urlString, final String downloadedFilePath) throws RemoteException {
        CommonApp.getAppHandler().post(new Runnable() {
            @Override
            public void run() {
                mCallback.downloadFinish(urlString, downloadedFilePath);
            }
        });
    }

    @Override
    public void downloadFail(final String urlString, final int errorCode, final String errorMsg) throws RemoteException {
        CommonApp.getAppHandler().post(new Runnable() {
            @Override
            public void run() {
                mCallback.downloadFail(urlString, errorCode, errorMsg);
            }
        });
    }

    @Override
    public void downloadStateChange(final String urlString, final int state) throws RemoteException {
        CommonApp.getAppHandler().post(new Runnable() {
            @Override
            public void run() {
                mCallback.downloadStateChange(urlString, state);
            }
        });
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        mServiceInterface = IDownloadService.Stub.asInterface(service);
        try {
            mServiceInterface.registDownloadCallback(this);
            mCallback.downloadServiceConnected(this);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        mCallback.downloadServiceDisconnected();
        mServiceInterface = null;
    }
}
