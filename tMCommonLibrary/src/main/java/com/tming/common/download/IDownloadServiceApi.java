package com.tming.common.download;

import android.os.RemoteException;

import java.io.File;
import java.util.List;

/**
 * Created by yusongying on 2015/1/4.
 */
public interface IDownloadServiceApi {
    /**
     * 获取下载状态 {@link com.tming.common.download.DownloadState}
     * @param urlString 文件URL地址
     * @return
     */
    public int getDownloadState(String urlString) throws RemoteException;

    /**
     * 获取已经下载的大小
     * @param urlString 文件URL地址
     * @return
     */
    public long getDownloadedSize(String urlString);

    /**
     * 获取下载完成的文件
     * @param urlString 文件URL地址
     * @return
     */
    public File getDownloadedFile(String urlString);

    /**
     * 获取url地址文件内容大小
     * @param urlString 文件URL地址
     * @return
     */
    public long getDownloadContentLength(String urlString);

    /**
     * 获取下载中的文件URL列表
     *
     * @return 下载中的文件URL地址
     * @throws RemoteException
     */
    public List<String> getDownloadingUrlStrings() throws RemoteException;

    /**
     * 销毁
     */
    public void destroy();
}
