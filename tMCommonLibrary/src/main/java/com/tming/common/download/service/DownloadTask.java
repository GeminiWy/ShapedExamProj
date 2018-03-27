package com.tming.common.download.service;

import android.content.Context;
import android.content.SharedPreferences;
import com.tming.common.CommonApp;
import com.tming.common.download.DownloadState;
import com.tming.common.thread.TMExcutors;
import com.tming.common.util.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 下载任务，由下载服务 {@link com.tming.common.download.service.DownloadService} 调用
 *
 * Created by yusongying on 2015/1/4.
 */
public class DownloadTask implements Runnable {
    private static final String TAG = "DownloadTask";
    private static final String SHARE_PREFERENCES_NAME_DOWNLOADING_INFO = "donwloading_info";
    private static final int SHARE_PREFERENCES_MODE_DOWNLOADING_INFO = Context.MODE_PRIVATE | Context.MODE_MULTI_PROCESS;
    private static final String SHARE_PREFERENCES_PREFIX_DOWNLOADED_PATH = "downloaded_path_";
    private static final String SHARE_PREFERENCES_PREFIX_DOWNLOADED_SIZE = "dwonloaded_size_";

    public static ThreadPoolExecutor excutors = TMExcutors.newLinkQueueCachedThreadPool("DownloadTask");

    private int mServiceStartId;
    private String mDownloadedFilePath;
    private String mTempFilePath;
    private String mUrlString;
    private boolean isStopFlag;
    private boolean isNeedDeleteTempFile;
    private float lastSendProgress;
    private DownloadService mService;
    private Thread mThread;

    public static String getDownloadedFilePath(String urlString) {
        String urlStringMd5String = Helper.getMD5String(urlString);
        // 自定义的下载地址
        SharedPreferences sharedPreferences = CommonApp.getAppContext().getSharedPreferences(SHARE_PREFERENCES_NAME_DOWNLOADING_INFO, SHARE_PREFERENCES_MODE_DOWNLOADING_INFO);
        String downloadedFilePath = sharedPreferences.getString(SHARE_PREFERENCES_PREFIX_DOWNLOADED_PATH + urlStringMd5String, null);
        if (downloadedFilePath != null) {
            return downloadedFilePath;
        }
        // 默认的下载地址
        String sdPath = PhoneUtil.getSDPath();
        if (sdPath != null) {
            return sdPath + "/data/" + CommonApp.getAppContext().getPackageName() + "/download/" + urlStringMd5String;
        }
        return null;
    }

    public static String getDownloadTempFilePath(String urlString) {
        String sdPath = PhoneUtil.getSDPath();
        if (sdPath != null) {
            return sdPath + "/data/" + CommonApp.getAppContext().getPackageName() + "/download/" + Helper.getMD5String(urlString) + ".temp";
        }
        return null;
    }

    public static long getDownloadedFileSize(String urlString) {
        File file = new File(getDownloadedFilePath(urlString));
        if (file.exists()) {
            return file.length();
        }
        String urlStringMD5String = Helper.getMD5String(urlString);
        SharedPreferences sharedPreferences = CommonApp.getAppContext().getSharedPreferences(SHARE_PREFERENCES_NAME_DOWNLOADING_INFO, SHARE_PREFERENCES_MODE_DOWNLOADING_INFO);
        long fileSize = sharedPreferences.getLong(SHARE_PREFERENCES_PREFIX_DOWNLOADED_SIZE + urlStringMD5String, 0);
        return fileSize;
    }

    public DownloadTask(DownloadService service, int serviceStartId, String urlString) {
        mUrlString = urlString;
        mService = service;
        mServiceStartId = serviceStartId;
    }

    public DownloadTask(DownloadService service, int serviceStartId, String urlString, String downloadedPath) {
        mUrlString = urlString;
        mService = service;
        mServiceStartId = serviceStartId;
        mDownloadedFilePath = downloadedPath;
    }

    private void init() {
        if (mDownloadedFilePath == null) {
            mDownloadedFilePath = getDownloadedFilePath(mUrlString);
        } else {
            // 保存自定义下载路径
            String urlStringMd5String = Helper.getMD5String(mUrlString);
            SharedPreferences sharedPreferences = CommonApp.getAppContext().getSharedPreferences(SHARE_PREFERENCES_NAME_DOWNLOADING_INFO, SHARE_PREFERENCES_MODE_DOWNLOADING_INFO);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(SHARE_PREFERENCES_PREFIX_DOWNLOADED_PATH + urlStringMd5String, mDownloadedFilePath);
            editor.commit();
        }
        mTempFilePath = getDownloadTempFilePath(mUrlString);
    }

    public void cancel(boolean isDeleteFile) {
        isStopFlag = true;
        isNeedDeleteTempFile = isDeleteFile;
        if (mThread != null) {
            mThread.interrupt();
        }
    }

    public void execute() {
        excutors.execute(this);
    }

    public boolean isExecuting() {
        return mThread != null;
    }

    public int getServiceStartId() {
        return mServiceStartId;
    }

    public String getDownloadUrlString() {
        return mUrlString;
    }

    @Override
    public void run() {
        mThread = Thread.currentThread();
        InputStream is = null;
        FileOutputStream fos = null;
        File tempFile = null;
        try {
            mService.taskCallStateChange(mUrlString, DownloadState.DOWNLOADING, mServiceStartId);

            // 检查SD卡
            init();
            // 判断是否已经下载完成
            File downloadedFile = new File(mDownloadedFilePath);
            Log.d(TAG, "donwloadedPath = " + mDownloadedFilePath);
            if (downloadedFile.exists()) {
                mService.taskCallFinish(mUrlString, mDownloadedFilePath, mServiceStartId);
                mService.taskCallStateChange(mUrlString, DownloadState.DOWNLOADED, mServiceStartId);
                return;
            }

            if (mTempFilePath == null) {
                mService.taskCallFail(mUrlString, DownloadState.DOWNLOAD_ERROR_NO_SDCARD, "no found sdcard!", mServiceStartId);
                mService.taskCallStateChange(mUrlString, DownloadState.PAUSE, mServiceStartId);
                return;
            }
            // 检查Wifi和网络状态
            boolean hasNetwork = PhoneUtil.checkNetworkEnable() == PhoneUtil.NETSTATE_ENABLE;
            if (!hasNetwork) {// 无网络
                mService.taskCallFail(mUrlString, DownloadState.DOWNLOAD_ERROR_NO_NETWORK, "no network!", mServiceStartId);
                mService.taskCallStateChange(mUrlString, DownloadState.PAUSE, mServiceStartId);
                return;
            } else if (hasNetwork && (!PhoneUtil.isWifiNetwork() && isNetworkFlowControlOpen())) {// 有网络但非wifi
                mService.taskCallFail(mUrlString, DownloadState.DOWNLOAD_ERROR_NOT_WIFI, "has network control and not wifi.", mServiceStartId);
                mService.taskCallStateChange(mUrlString, DownloadState.PAUSE, mServiceStartId);
                return;
            }

            // 检查网络是否需要登录
            if (checkIsNetworkRequiresLogin()) {
                Log.d(TAG, "Network requires browser login!");
                mService.taskCallFail(mUrlString, DownloadState.DOWNLOAD_ERROR_NETWORK_REQUIRES_LOGIN, "Network requires browser login!", mServiceStartId);
                mService.taskCallStateChange(mUrlString, DownloadState.PAUSE, mServiceStartId);
                return;
            }

            tempFile = new File(mTempFilePath);
            long downloadedSize = 0;
            FileUtil.makeParentDirIfNeed(tempFile);

            if (isStopFlag) {
                throw new RuntimeException("user canceled download task.");
            }
            if (tempFile.exists()) {
                downloadedSize = tempFile.length();
            }


            HttpURLConnection connection = makeConnection(downloadedSize);

            if (connection.getResponseCode() == 416) {// 超出界限
                // 重新下载
                tempFile.delete();
                downloadedSize = 0;
                connection = makeConnection(0);
            }

            // 检查返回状态码
            if (connection.getResponseCode() / 200 != 1) {
                mService.taskCallFail(mUrlString, connection.getResponseCode(), "Http response code isn't OK.", mServiceStartId);
                mService.taskCallStateChange(mUrlString, DownloadState.PAUSE, mServiceStartId);
                return;
            }

            is = connection.getInputStream();

            // 获取文件大小
            long contentLength;
            if (downloadedSize > 0) {
                contentLength = readRemoteFileSize();
            } else {
                contentLength = connection.getContentLength();
                saveRemoteFileSize(contentLength);
            }

            fos = new FileOutputStream(tempFile, true);
            byte[] buf = new byte[1024];
            int len = 0;
            while ((len = is.read(buf)) != -1) {
                // 计算进度
                downloadedSize += len;
                float progress = (float) downloadedSize / contentLength;
                if (progress - lastSendProgress > .01f || progress == 1) {
                    mService.taskCallUpdateProgress(mUrlString, downloadedSize, contentLength);
                    lastSendProgress = progress;
                }

                // 写入文件
                fos.write(buf, 0, len);

                // 判断是否停止
                if (isStopFlag) {
                    throw new RuntimeException("user canceled download task.");
                }
            }
            fos.flush();
            fos.close();
            fos = null;

            FileUtil.makeParentDirIfNeed(downloadedFile);
            if (tempFile.renameTo(downloadedFile)) {// 相同目录使用重命名
                Log.d(TAG, "rename:" + tempFile.getName() + " to " + downloadedFile.getName());
                removeDownloadInfoInSharedPreferences();
                mService.taskCallStateChange(mUrlString, DownloadState.DOWNLOADED, mServiceStartId);
                mService.taskCallFinish(mUrlString, mDownloadedFilePath, mServiceStartId);
            } else {
                mService.taskCallStateChange(mUrlString, DownloadState.PAUSE, mServiceStartId);
                mService.taskCallFail(mUrlString, DownloadState.DOWNLOAD_ERROR_RENAME_FAIL, "rename to fail:" + mDownloadedFilePath, mServiceStartId);
            }
        } catch (Throwable e) {
            if (fos != null) {
                try {
                    fos.flush();
                    fos.close();
                    fos = null;
                } catch (IOException e2) {
                    e2.printStackTrace();
                }
            }
            if (!isStopFlag) {
                e.printStackTrace();
                mService.taskCallFail(mUrlString, DownloadState.DOWNLOAD_ERROR_UNKOWN, e.getMessage(), mServiceStartId);
                mService.taskCallStateChange(mUrlString, DownloadState.PAUSE, mServiceStartId);
            } else {
                if (isNeedDeleteTempFile) {
                    // 删除文件
                    if (tempFile.exists()) {
                        tempFile.delete();
                    }
                    removeDownloadInfoInSharedPreferences();
                    mService.taskCallStateChange(mUrlString, DownloadState.UNKNOWN, mServiceStartId);
                } else {
                    mService.taskCallStateChange(mUrlString, DownloadState.PAUSE, mServiceStartId);
                }
            }
        } finally {
            try {
                if (fos != null) fos.close();
                if (is != null) is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            mThread = null;
        }
    }

    /**
     * 连接
     *
     * @param downloadedSize 开始下载的位置
     * @return
     * @throws IOException
     */
    private HttpURLConnection makeConnection(long downloadedSize) throws IOException {
        URL url = new URL(mUrlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        if (downloadedSize > 0) {
            connection.addRequestProperty("Range", "bytes=" + downloadedSize + "-");
        }
        connection.setInstanceFollowRedirects(false);
        connection.setReadTimeout(Constants.HTTP_READ_TIMEOUT * 5);
        connection.setConnectTimeout(Constants.HTTP_CONNECT_TIMEOUT);
        return connection;
    }

    /**
     * 检验网络是否需要登录
     *
     * @return
     */
    private boolean checkIsNetworkRequiresLogin() {
        // 获取接口返回数据，如果不是时间毫秒，就说明网络被拦截
        HttpURLConnection httpURLConnection = null;
        try {
            URL url = new URL("http://api.service.fjou.cn/api/open/getTimeMillis.html");
            httpURLConnection = (HttpURLConnection) url.openConnection();
            byte[] buf = new byte[16];
            InputStream is = httpURLConnection.getInputStream();
            int len;
            int readLen = 0;
            while ((len = is.read(buf, readLen, buf.length - readLen)) != -1) {
                readLen += len;
                if (readLen >= 13) {
                    break;
                }
            }
            return !new String(buf, 0, readLen).matches("[0-9]+");
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (httpURLConnection != null) {
                httpURLConnection.disconnect();
            }
        }
        return false;
    }

    /**
     * 移除SharedPreferences中的下载信息
     */
    private void removeDownloadInfoInSharedPreferences() {
        String urlStringMd5String = Helper.getMD5String(mUrlString);
        SharedPreferences sharedPreferences = CommonApp.getAppContext().getSharedPreferences(SHARE_PREFERENCES_NAME_DOWNLOADING_INFO, SHARE_PREFERENCES_MODE_DOWNLOADING_INFO);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(SHARE_PREFERENCES_PREFIX_DOWNLOADED_SIZE + urlStringMd5String);
        /*editor.remove(SHARE_PREFERENCES_PREFIX_DOWNLOADED_PATH + urlStringMd5String);*/
        editor.commit();
    }

    private long readRemoteFileSize() {
        String urlStringMD5String = Helper.getMD5String(mUrlString);
        SharedPreferences sharedPreferences = CommonApp.getAppContext().getSharedPreferences(SHARE_PREFERENCES_NAME_DOWNLOADING_INFO, SHARE_PREFERENCES_MODE_DOWNLOADING_INFO);
        long fileSize = sharedPreferences.getLong(SHARE_PREFERENCES_PREFIX_DOWNLOADED_SIZE + urlStringMD5String, 0);
        return fileSize;
    }

    /**
     * 保存远程文件大小
     * @param fileSize
     */
    private void saveRemoteFileSize(long fileSize) {
        SharedPreferences sharedPreferences = CommonApp.getAppContext().getSharedPreferences(SHARE_PREFERENCES_NAME_DOWNLOADING_INFO, SHARE_PREFERENCES_MODE_DOWNLOADING_INFO);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putLong(SHARE_PREFERENCES_PREFIX_DOWNLOADED_SIZE + Helper.getMD5String(mUrlString), fileSize);
        editor.commit();
    }

    /**
     *
     * <p>判断用户是否开启了流量控制</P>
     *
     * @return true 开启了，否则没有
     */
    private boolean isNetworkFlowControlOpen() {
        SharedPreferences spf = CommonApp.getAppContext().getSharedPreferences(Constants.SHARE_PREFERENCES_NAME, Context.MODE_PRIVATE | Context.MODE_MULTI_PROCESS);
        return spf.getBoolean(Constants.SHARE_PREFERENCES_IS_FLOW_CONTROL, false);
    }
}
