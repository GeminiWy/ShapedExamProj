package com.tming.common.net;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.json.JSONObject;

import com.tming.common.CommonApp;
import com.tming.common.net.MultipartDataRequestTask.MultipartRequestCallBack;
import com.tming.common.util.Constants;
import com.tming.common.util.Log;
import com.tming.common.util.PhoneUtil;


/**
 * <p>Http请求</p>
 */
public class TmingHttp {

    public static final Executor S_EXECUTOR = Executors.newFixedThreadPool(3);

    /**
     * 
     * Http请求回调
     * 
     * @param <T>
     *            返回值
     */
    public static interface RequestCallback<T> {

        /**
         * 请求成功的回调，可以在该方法中解析数据, 该方法将在子线程中调用,请勿操作ui。
         */
        public T onReqestSuccess(String respones) throws Exception;

        /**
         * 请求成功的回调，该方法将在主线程中调用
         * 
         * @param respones
         */
        public void success(T respones);

        /**
         * 请求发生异常的回调，该方法将在主线程中调用
         * 
         * @param exception
         */
        public void exception(Exception exception);
    }

    /**
     * 异步请求网络连接
     *
     * @param url 请求地址
     * @param callback 回调
     *
     * @param <T> 返回到主线程的数据类型
     */
    public static <T> TmingHttpRequestTask asyncRequest(String url, RequestCallback<T> callback) {
        TmingHttpRequestTask requestTask = new TmingHttpRequestTask(url, null, null, callback);
        S_EXECUTOR.execute(requestTask);
        return requestTask;
    }

    /**
     * 异步请求网络连接
     * 
     * @param url 请求地址
     * @param params 当该参数为空时为get请求，否则为post
     * @param callback 回调
     * 
     * @param <T> 返回到主线程的数据类型
     */
    public static <T> TmingHttpRequestTask asyncRequest(String url, Map<String, Object> params, RequestCallback<T> callback) {
        TmingHttpRequestTask requestTask = new TmingHttpRequestTask(url, null, params, callback);
        S_EXECUTOR.execute(requestTask);
        return requestTask;
    }

    /**
     * 异步请求网络连接
     *
     * @param url 请求地址
     * @param header 请求头部参数
     * @param params 当该参数为空时为get请求，否则为post
     * @param callback 回调
     *
     * @param <T> 返回到主线程的数据类型
     */
    public static <T> TmingHttpRequestTask asyncRequest(String url, Map<String, String> header, Map<String, Object> params, RequestCallback<T> callback) {
        TmingHttpRequestTask requestTask = new TmingHttpRequestTask(url, header, params, callback);
        S_EXECUTOR.execute(requestTask);
        return requestTask;
    }

    /**
     * 获取jSONObject对象
     */
    static public JSONObject getJSONObject(String url, Map<String, Object> map) throws TmingHttpException {
        TmingHttpRequestTask requestTask = new TmingHttpRequestTask(url, null, map, null);
        return requestTask.tmingHttpRequest().asJSONObject();
    }

    /**
     * 发送Http请求  
     * @see {@link #asyncRequest(String, Map, RequestCallback)}
     * @hide
     * @param url 链接地址
     * @param postParams post参数
     * @return {@link TmingResponse}
     * @exception TmingHttpException
     */
    static public TmingResponse tmingHttpRequest(String url, Map<String, Object> postParams) throws TmingHttpException {
        TmingHttpRequestTask requestTask = new TmingHttpRequestTask(url, null, postParams, null);
        return requestTask.tmingHttpRequest();
    }

    /**
     * <p>MutipartForm 数据上传, 可调用返回值中的{@link MultipartDataRequestTask#cancel()} 取消请求</P>
     *
     * @param urlString 请求地址
     * @param headers 请求头,可以为NULL
     * @param params 参数( {@link String}、{@link Integer} 将会调用 {@code toString()} 使用 text/plain 进行编码。{@link File} 将会以 application/octet-stream 编码)
     * @param callBack 请求结果回调
     * @return 发送数据请求任务
     */
    public static <T> MultipartDataRequestTask asyncPostFile(String urlString, HashMap<String, String> headers, HashMap<String, Object> params, MultipartRequestCallBack<T> callBack) {
        MultipartDataRequestTask task = new MultipartDataRequestTask(urlString, headers, params, callBack);
        S_EXECUTOR.execute(task);
        return task;
    } 
    
    /**
     * <p>MutipartForm 数据上传, 可调用返回值中的{@link MultipartDataRequestTask#cancel()} 取消请求</P>
     *
     * @param urlString 请求地址
     * @param params 参数( {@link String}、{@link Integer} 将会调用 {@code toString()} 使用 text/plain 进行编码。{@link File} 将会以 application/octet-stream 编码)
     * @param callBack 请求结果回调
     * @return 发送数据请求任务
     */
    public static <T> MultipartDataRequestTask asyncPostFile(String urlString, HashMap<String, Object> params, MultipartRequestCallBack<T> callBack) {
        MultipartDataRequestTask task = new MultipartDataRequestTask(urlString, null, params, callBack);
        S_EXECUTOR.execute(task);
        return task;
    }
    
    /**
     * <p>MutipartForm 数据上传, 可调用返回值中的{@link MultipartDataRequestTask#cancel()} 取消请求</P>
     *
     * @param urlString 请求地址
     * @param postKey 服务端接收文件的表单的名称
     * @param file 发送的文件对象
     * @param callBack 请求结果回调
     * @return 发送数据请求任务
     */
    public static <T> MultipartDataRequestTask asyncSimpleSendFile(String urlString, String postKey, File file, MultipartRequestCallBack<T> callBack) {
        HashMap<String, Object> params = new HashMap<String, Object>();
        params.put(postKey, file);
        MultipartDataRequestTask task = new MultipartDataRequestTask(urlString, null, params, callBack);
        S_EXECUTOR.execute(task);
        return task;
    }
    
    /**
     * <p>同步的 MutipartForm 数据上传</P>
     *
     * @param urlString 请求地址
     * @param postKey 服务端接收文件的表单的名称
     * @param file 发送的文件对象
     * @return 发送数据请求任务
     */
    public static TmingResponse syncSendFile(String urlString, String postKey, File file) {
        HashMap<String, Object> params = new HashMap<String, Object>();
        params.put(postKey, file);
        MultipartDataRequestTask task = new MultipartDataRequestTask(urlString, null, params, null);
        task.run();
        return task.getResponse();
    }

    /**
     * <p>同步的 MutipartForm 数据上传</P>
     *
     * @param urlString 请求地址
     * @param params 请求参数( {@link String}、{@link Integer} 将会调用 {@code toString()} 使用 text/plain 进行编码。 {@link File} 将会以 application/octet-stream 编码)
     * @return 发送数据请求任务
     */
    public static TmingResponse syncSendFile(String urlString, HashMap<String, Object> params) {
        MultipartDataRequestTask task = new MultipartDataRequestTask(urlString, null, params, null);
        task.run();
        return task.getResponse();
    }
}