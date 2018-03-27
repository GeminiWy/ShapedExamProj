package com.tming.common.download;

/**
 * Created by Administrator on 2015/1/4.
 */
public interface IDownloadCallback {

    /**
     * 服务连接成功
     * @param serviceApi 服务接口
     */
    void downloadServiceConnected(IDownloadServiceApi serviceApi);

    /**
     * 服务断开连接
     */
    void downloadServiceDisconnected();

    /**
     * 下载进度回调
     * @param urlString 下载地址
     * @param downloadedSize 已经下载的大小
     * @param contentLength 下载文件的总大小
     */
    void downloadUpdateProgress(String urlString, long downloadedSize, long contentLength);

    /**
     * 下载完成回调
     * @param urlString 下载地址
     */
    void downloadFinish(String urlString, String downloadedFilePath);

    /**
     * 下载失败回调
     * @param urlString 下载地址
     * @param errorCode 错误代码
     * @param errorMsg 错误信息
     */
    void downloadFail(String urlString, int errorCode, String errorMsg);

    /**
     * 下载状态回调
     * @param urlString 下载地址
     * @param state 下载状态
     */
    void downloadStateChange(String urlString, int state);

}
