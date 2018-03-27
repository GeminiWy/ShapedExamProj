package com.tming.common.download;

import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import com.tming.common.download.service.DownloadService;

/**
 * Created by yusongying on 2015/1/4.
 */
public class DownloadServiceUtil {

    /**
     * 开始下载
     * @param urlString 下载地址
     */
    public static void startDownload(Context context, String urlString) {
        Intent intent = new Intent(context, DownloadService.class);
        intent.putExtra(DownloadService.INTENT_COMMAND_EXTRA_KEY, DownloadService.COMMAND_START);
        intent.putExtra(DownloadService.INTENT_URL_STRING_EXTRA_KEY, urlString);
        context.startService(intent);
    }

    /**
     * 开始下载
     * @param context Android上下文
     * @param urlString 下载地址
     * @param saveToFilePath 保存到文件的路径
     */
    public static void startDownload(Context context, String urlString, String saveToFilePath) {
        Intent intent = new Intent(context, DownloadService.class);
        intent.putExtra(DownloadService.INTENT_COMMAND_EXTRA_KEY, DownloadService.COMMAND_START);
        intent.putExtra(DownloadService.INTENT_URL_STRING_EXTRA_KEY, urlString);
        intent.putExtra(DownloadService.INTENT_DOWNLOADED_PATH_EXTRA_KEY, saveToFilePath);
        context.startService(intent);
    }

    /**
     * 暂停下载
     * @param urlString 下载地址
     */
    public static void pauseDownload(Context context, String urlString) {
        Intent intent = new Intent(context, DownloadService.class);
        intent.putExtra(DownloadService.INTENT_COMMAND_EXTRA_KEY, DownloadService.COMMAND_PAUSE);
        intent.putExtra(DownloadService.INTENT_URL_STRING_EXTRA_KEY, urlString);
        context.startService(intent);
    }

    /**
     * 恢复下载
     * @param urlString 下载地址
     */
    public static void resumeDownload(Context context, String urlString) {
        Intent intent = new Intent(context, DownloadService.class);
        intent.putExtra(DownloadService.INTENT_COMMAND_EXTRA_KEY, DownloadService.COMMAND_RESUME);
        intent.putExtra(DownloadService.INTENT_URL_STRING_EXTRA_KEY, urlString);
        context.startService(intent);
    }

    /**
     * 取消下载
     * @param urlString 下载地址
     */
    public static void cancelDownload(Context context, String urlString) {
        Intent intent = new Intent(context, DownloadService.class);
        intent.putExtra(DownloadService.INTENT_COMMAND_EXTRA_KEY, DownloadService.COMMAND_CANCEL);
        intent.putExtra(DownloadService.INTENT_URL_STRING_EXTRA_KEY, urlString);
        context.startService(intent);
    }

    /**
     * 注册下载监听
     * <p><strong>需要在退出界面时调用 {@link android.app.Activity#unbindService(ServiceConnection)}</strong></p>
     * @param downloadCallback 回调接口
     * @return 服务连接
     */
    public static ServiceConnection registDownloadCallback(Context context, IDownloadCallback downloadCallback) {
        return new DownloadServiceApiImpl(context, downloadCallback);
    }
}
