package com.tming.common.download;

/**
 * Created by Administrator on 2015/1/4.
 */
public interface DownloadState {

    /**
     * 下载完成
     */
    public static final int DOWNLOADED = 1;
    /**
     * 下载中
     */
    public static final int DOWNLOADING = 2;

    /**
     * 暂停
     */
    public static final int PAUSE = 3;

    /**
     * 未知状态
	 */
    public static final int UNKNOWN = 4;


    /**
     * 未知错误
     */
    public static final int DOWNLOAD_ERROR_UNKOWN = 100;

    /**
     * 没有SD卡
     */
    public static final int DOWNLOAD_ERROR_NO_SDCARD = 101;

    /**
     * 无网络
     */
    public static final int DOWNLOAD_ERROR_NO_NETWORK = 102;

    /**
     * 非WiFi网络
     */
    public static final int DOWNLOAD_ERROR_NOT_WIFI = 103;

    /**
     * 将临时文件名修改为下载文件路径失败
     */
    public static final int DOWNLOAD_ERROR_RENAME_FAIL = 104;

    /**
     * 网络需要到浏览器登录
     */
    public static final int DOWNLOAD_ERROR_NETWORK_REQUIRES_LOGIN = 105;


}
