package com.tming.common.cache.net;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import java.util.Set;

import android.content.Context;

import com.tming.common.CommonApp;
import com.tming.common.cache.Cache;
import com.tming.common.net.*;
import com.tming.common.util.AESencrypt;
import com.tming.common.util.Helper;
import com.tming.common.util.Log;

/**
 * <p>带缓存 Http 请求</p>
 */
public class TmingCacheHttp extends Cache {

    private static TmingCacheHttp instance = new TmingCacheHttp(CommonApp.getAppContext());

    /**
     * @deprecated Use {@link #getInstance()} instead.
     * <p>获取单例实例 </P>
     * 
     * @param context android 上下文
     * @return 单例实例
     */
    public static TmingCacheHttp getInstance(Context context) {
        return instance;
    }

    /**
     * 
     * <p>获取单例实例</P>
     *
     * @return 单例实例
     */
    public static TmingCacheHttp getInstance() {
        return instance;
    }

    /**
     * 构造函数
     * @param context android 上下文
     */
    public TmingCacheHttp(Context context) {
        super(context);
    }

    /**
     * @deprecated Use {@link TmingCacheHttp.RequestWithCacheCallBackV2} instead.
     */
    public static interface RequestWithCacheCallBack<T> {

        /**
         * 上次请求的缓存，线程调用
         * 
         * @param cache
         * @return
         */
        public abstract T onPreRequestCache(String cache) throws Exception;

        /**
         * 上次请求的缓存数据，UI线程调用
         * 
         * @param data
         */
        public abstract void onPreRequestSuccess(T data);

        /**
         * 请求成功的回调，可以在该方法中解析数据, 该方法将在线程中调用,请勿操作ui。
         */
        public T onReqestSuccess(String respones) throws Exception;

        /**
         * 请求成功的回调，该方法将在主线程中调用
         * 
         * @param cacheRespones 缓存数据
         * @param newRespones 返回的新数据
         */
        public void success(T cacheRespones, T newRespones);

        /**
         * 请求发生异常的回调，该方法将在主线程中调用
         * 
         * @param exception
         */
        public void exception(Exception exception);
    }

    /**
     * <p>
     * 接口请求回调
     * </p>
     * @see {@link TmingCacheHttp.RequestWithCacheCallBackV2Adapter}
     */
    public static interface RequestWithCacheCallBackV2<T> {

        /**
         * 解析数据 该方法将在线程中调用,请勿操作ui。
         * 
         * @param data
         *            服务端返回的数据
         * @return
         */
        public abstract T parseData(String data) throws Exception;

        /**
         * 上次请求的缓存数据，UI线程调用
         * 
         * @param data 缓存数据解析结果
         */
        public abstract void cacheDataRespone(T data);

        /**
         * 请求成功的回调，UI线程调用 第一请求或者是服务端数据更新后将会被调用
         * 
         * @param cacheRespones 缓存的数据
         * @param newRespones 服务端新数据
         */
        public void requestNewDataRespone(T cacheRespones, T newRespones);

        /**
         * 请求发生异常的回调，该方法将在主线程中调用
         * 
         * @param exception
         */
        public void exception(Exception exception);

        /**
         * 请求结束回调，UI线程调用
         */
        public void onFinishRequest();
    }
    
    /**
     * <p>离线缓存请求回调</p>
     * @see {@link RequestWithOfflineCacheCallBackAdapter}
     */
    public static interface RequestWithOfflineCacheCallBack<T> {

        /**
         * 解析数据 该方法将在线程中调用,请勿操作ui。
         * 
         * @param data 未解析的数据
         * @return
         */
        public abstract T parseData(String data) throws Exception;

        /**
         * 请求成功的回调，UI线程调用，在离线状态下将返回缓存数据
         * 
         * @param respone 用户解析的数据
         */
        public void requestRespone(T respone);

        /**
         * 请求发生异常的回调，该方法将在主线程中调用
         * 
         * @param exception
         */
        public void exception(Exception exception);

        /**
         * 请求结束回调，UI线程调用
         */
        public void onFinishRequest();
    }

    /**
     * <p>请求回调 {@link TmingCacheHttp.RequestWithCacheCallBackV2} 适配类</p>
     * <p>Created by yusongying on 2014年7月16日</p>
     */
    public static abstract class RequestWithCacheCallBackV2Adapter<T> implements RequestWithCacheCallBackV2<T> {

        @Override
        public T parseData(String data) throws Exception {
            return null;
        }

        @Override
        public void cacheDataRespone(T data) {}

        @Override
        public void requestNewDataRespone(T cacheRespones, T newRespones) {}

        @Override
        public void exception(Exception exception) {
            exception.printStackTrace();
        }

        @Override
        public void onFinishRequest() {}
    }

    /**
     * <p>请求回调 {@link TmingCacheHttp.RequestWithOfflineCacheCallBack} 适配类</p>
     * <p>Created by yusongying on 2014年7月26日</p>
     * @see {@link RequestWithOfflineCacheCallBack}
     */
    public static abstract class RequestWithOfflineCacheCallBackAdapter<T> implements RequestWithOfflineCacheCallBack<T> {
        
        @Override
        public void exception(Exception arg0) {}
        
        @Override
        public void onFinishRequest() {}
    }

    /**
     * 异步请求网络连接,如果数据无变化则不调用新的请求结果给回调
     * 
     * @deprecated Use {@link #asyncRequestWithCache(String, Map, RequestWithCacheCallBackV2)} instead.
     * @param url 请求地址
     * @param postParams 当该参数为空时为get请求，否则为post
     * @param callBack 回调
     * @return 数据请求任务，可以进行取消
     */
    public <T> TmingHttpRequestWithCacheTask asyncRequestWithCache(String url, Map<String, Object> postParams, final RequestWithCacheCallBack<T> callBack) {
        TmingHttpRequestWithCacheTask task = new TmingHttpRequestWithCacheTask(this, url, null, postParams, new RequestWithCacheCallBackV2<T>(){

            @Override
            public T parseData(String data) throws Exception {
                return callBack.onPreRequestCache(data);
            }

            @Override
            public void cacheDataRespone(T data) {
                callBack.onPreRequestSuccess(data);
            }

            @Override
            public void requestNewDataRespone(T cacheRespones, T newRespones) {
                callBack.success(cacheRespones, newRespones);
            }

            @Override
            public void exception(Exception exception) {
                callBack.exception(exception);
            }

            @Override
            public void onFinishRequest() {}
        });
        TmingHttp.S_EXECUTOR.execute(task);
        return task;
    }

    /**
     * 异步请求网络连接,如果数据无变化则不调用新的请求结果给回调
     *
     * @param urlString 请求地址
     * @param postParams 当该参数为空时为get请求，否则为post
     * @param callBack 回调
     */
    public <T> TmingHttpRequestWithCacheTask asyncRequestWithCache(String urlString, Map<String, Object> postParams, RequestWithCacheCallBackV2<T> callBack) {
        TmingHttpRequestWithCacheTask task = new TmingHttpRequestWithCacheTask(this, urlString, null, postParams, callBack);
        TmingHttp.S_EXECUTOR.execute(task);
        return task;
    }

    /**
     * 异步请求网络连接,如果数据无变化则不调用新的请求结果给回调
     *
     * @param urlString 请求地址
     * @param header Http请求头
     * @param postParams 当该参数为空时为get请求，否则为post
     * @param callBack 回调
     */
    public <T> TmingHttpRequestWithCacheTask asyncRequestWithCache(String urlString, Map<String, String> header, Map<String, Object> postParams, RequestWithCacheCallBackV2<T> callBack) {
        TmingHttpRequestWithCacheTask task = new TmingHttpRequestWithCacheTask(this, urlString, header, postParams, callBack);
        TmingHttp.S_EXECUTOR.execute(task);
        return task;
    }
    
    /**
     * <p>异步Http请求，在离线状态或请求失败的状态下返回缓存数据</P>
     *  @param urlString url地址
     * @param postParams post请求参数，如果是 null 将使用GET请求，否则使用 POST
     * @param callBack 请求结果回调
     */
    public <T> TmingHttpRequestWithOfflineCacheTask asyncReqeustWithOfflineCache(String urlString, Map<String, Object> postParams, RequestWithOfflineCacheCallBack<T> callBack) {
        TmingHttpRequestWithOfflineCacheTask task = new TmingHttpRequestWithOfflineCacheTask(this, urlString, null, postParams, callBack);
        TmingHttp.S_EXECUTOR.execute(task);
        return task;
    }

    /**
     * <p>异步Http请求，在离线状态或请求失败的状态下返回缓存数据</P>
     * @param urlString url地址
     * @param header Http请求头
     * @param postParams post请求参数，如果是 null 将使用GET请求，否则使用 POST
     * @param callBack 请求结果回调
     */
    public <T> TmingHttpRequestWithOfflineCacheTask asyncReqeustWithOfflineCache(String urlString, Map<String, String> header, Map<String, Object> postParams, RequestWithOfflineCacheCallBack<T> callBack) {
        TmingHttpRequestWithOfflineCacheTask task = new TmingHttpRequestWithOfflineCacheTask(this, urlString, header, postParams, callBack);
        TmingHttp.S_EXECUTOR.execute(task);
        return task;
    }
}
