package com.tming.common.net;

import android.os.Handler;
import com.tming.common.CommonApp;
import com.tming.common.cache.Cache;
import com.tming.common.cache.net.TmingCacheHttp;

import java.io.File;
import java.util.Map;

/**
 * <p> 仅在离线状态下使用缓存的请求</p>
 * @see {@link com.tming.common.cache.net.TmingCacheHttp#asyncReqeustWithOfflineCache(String, java.util.Map, com.tming.common.cache.net.TmingCacheHttp.RequestWithOfflineCacheCallBack)}
 * <p>Created by yusongying on 2014/10/19</p>
 */
public class TmingHttpRequestWithOfflineCacheTask<T> extends TmingHttpRequestWithCacheTask {

    private TmingCacheHttp.RequestWithOfflineCacheCallBack mCallback;

    public TmingHttpRequestWithOfflineCacheTask(Cache cache, String urlString, Map<String, String> headers, Map<String, Object> params, TmingCacheHttp.RequestWithOfflineCacheCallBack<T> callback) {
        super(cache, urlString, headers, params, null);
        this.mCallback = callback;
    }

    @Override
    public void run() {
        mThread = Thread.currentThread();
        Handler handler = CommonApp.getAppHandler();
        try {
            File cacheFile = getRequestCacheFile();
            String content = null;
            try {
                TmingResponse response = tmingHttpRequest();
                content = response.asString();
                saveResponseToSd(cacheFile, content);
            } catch (TmingHttpException e) {
                e.printStackTrace();
            }
            if (content == null && cacheFile != null && cacheFile.exists()) {
                content = loadContentFile(cacheFile);
            }
            final T data = (T) mCallback.parseData(content);
            handler.post(new Runnable() {

                @Override
                public void run() {
                    mCallback.requestRespone(data);
                }
            });
        } catch (final Exception e) {
            e.printStackTrace();
            handler.post(new Runnable() {

                @Override
                public void run() {
                    mCallback.exception(e);
                }
            });
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
            handler.post(new Runnable() {

                @Override
                public void run() {
                    mCallback.exception(new TmingHttpException("OutOfMemoryError"));
                }
            });
        }
        // 请求结束
        handler.post(new Runnable() {

            @Override
            public void run() {
                mCallback.onFinishRequest();
            }
        });
        mThread = null;
    }
}
