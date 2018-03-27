package com.tming.common.net;

import com.tming.common.CommonApp;
import com.tming.common.util.Constants;
import com.tming.common.util.Log;
import com.tming.common.util.PhoneUtil;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Map;
import java.util.Set;

/**
 * @hide
 * <p>Http请求任务</p>
 * @see {@link com.tming.common.net.TmingHttp#asyncRequest(String, java.util.Map, com.tming.common.net.TmingHttp.RequestCallback)}
 * @see {@link com.tming.common.net.TmingHttp#tmingHttpRequest(String, java.util.Map)}
 * <p>Created by yusongying on 2014/10/19</p>
 */
public class TmingHttpRequestTask<T> implements Runnable {

    /**
     * Success!
     */
    private static final int OK = 200;
    private static final int NOT_MODIFIED = 304;// Not Modified:
    private static final int BAD_REQUEST = 400;// Bad Request:
    private static final int NOT_AUTHORIZED = 401;// Not Authorized:
    private static final int FORBIDDEN = 403;// Forbidden:
    private static final int NOT_FOUND = 404;// Not Found:
    private static final int NOT_ACCEPTABLE = 406;// Not Acceptable:
    private static final int INTERNAL_SERVER_ERROR = 500;// Internal Server
    // Error:
    private static final int BAD_GATEWAY = 502;// Bad Gateway
    private static final int SERVICE_UNAVAILABLE = 503;// Service Unavailable:

    private static final String TAG = "TmingHttpRequestTask";

    /**
     * 请求地址
     */
    protected String mRequestUrlString = null;
    /**
     * 请求头
     */
    private Map<String, String> mHeaders = null;
    /**
     * 请求参数
     */
    protected Map<String, Object> mParams = null;

    /**
     * 完成编码的请求参数
     */
    protected String mEncodedParamsString = null;
    /**
     * 请求回调
     */
    private TmingHttp.RequestCallback<T> mRequestCallback;

    /**
     * 请求Http线程
     */
    protected Thread mThread;

    /**
     * 标识用户是否取消了
     */
    private boolean mIsUserCancelled = false;

    public TmingHttpRequestTask(String urlString, Map<String, String> headers, Map<String, Object> params, TmingHttp.RequestCallback<T> callback) {
        mRequestUrlString = urlString;
        mHeaders = headers;
        mParams = params;
        mRequestCallback = callback;
    }

    /**
     * 取消Http请求
     */
    public void cancel() {
        mIsUserCancelled = true;
        if (mThread != null) {
            mThread.interrupt();
        }
    }

    @Override
    public void run() {
        mThread = Thread.currentThread();
        try {
            TmingResponse response = tmingHttpRequest();
            if (mRequestCallback != null) {
                final T data = mRequestCallback.onReqestSuccess(response.asString());
                CommonApp.getAppHandler().post(new Runnable() {

                    @Override
                    public void run() {
                        mRequestCallback.success(data);
                    }
                });
            }
        } catch (final Throwable e) {
            // 如果非用户取消，将异常发送给回调
            if (mRequestCallback != null && !mIsUserCancelled) {
                CommonApp.getAppHandler().post(new Runnable() {
                    @Override
                    public void run() {
                        mRequestCallback.exception(new Exception(e));
                    }
                });
            }
        }
        mThread = null;
    }

    /**
     * 发送Http请求
     * @hide
     * @return {@link TmingResponse}
     * @exception TmingHttpException
     */
    public TmingResponse tmingHttpRequest() throws TmingHttpException {
        String encodedParamString = encodeParameters();
        if (Constants.DEBUG) {
            Log.d(TAG, (encodedParamString != null ? "POST" : "GET") + ":" + mRequestUrlString + (encodedParamString != null ? ("?" + encodedParamString) : ""));
        }
        if (PhoneUtil.checkNetworkEnable() != PhoneUtil.NETSTATE_ENABLE) {
            throw new TmingHttpException("network is disable", 600);
        }
        int responseCode = -1;
        OutputStream osw = null;
        try {
            if (mIsUserCancelled) {
                throw new TmingHttpException("user is cancelled.");
            }
            HttpURLConnection connection = getConnection(mRequestUrlString);
            connection.setDoInput(true);

            // 设置头部参数
            if (mHeaders != null) {
                Set<String> headerKeys = mHeaders.keySet();
                for (String headerKey : headerKeys) {
                    connection.addRequestProperty(headerKey, mHeaders.get(headerKey));
                }
            }

            // 设置请求参数
            if (encodedParamString != null) {
                connection.setDoOutput(true);
                byte[] bytes = encodedParamString.getBytes("UTF-8");
                osw = connection.getOutputStream();
                osw.write(bytes);
                osw.flush();
                osw.close();
            }

            TmingResponse res = new TmingResponse(connection);
            res.asString();
            responseCode = connection.getResponseCode();
            if (responseCode != OK) {
                throw new TmingHttpException(getCause(responseCode) + "\n" + res.asString(), responseCode);
            }
            return res;
        } catch (IOException ioe) {
            throw new TmingHttpException(ioe);
        }
    }

    /**
     * 设置请求超时和读取超时
     * @param url 链接地址
     * @return {@link java.net.HttpURLConnection}
     */
    private HttpURLConnection getConnection(String url) throws IOException {
        HttpURLConnection con = (HttpURLConnection) new URL(url).openConnection();
        if (Constants.HTTP_CONNECT_TIMEOUT > 0) {
            con.setConnectTimeout(Constants.HTTP_CONNECT_TIMEOUT);
        }
        if (Constants.HTTP_READ_TIMEOUT > 0) {
            con.setReadTimeout(Constants.HTTP_READ_TIMEOUT);
        }
        return con;
    }

    /**
     * 将请求参数拼接，并对Value进行URLEncoder
     * @return 拼接完成的参数字符串
     */
    protected String encodeParameters() {
        if (mEncodedParamsString == null) {
            if (mParams == null || mParams.size() == 0) return null;
            StringBuffer buf = new StringBuffer();
            Set<String> keys = mParams.keySet();
            for (String key : keys) {
                try {
                    buf.append(key).append("=").append(URLEncoder.encode(String.valueOf(mParams.get(key)), "UTF-8"))
                            .append("&");
                } catch (java.io.UnsupportedEncodingException neverHappen) {}
            }
            mEncodedParamsString = buf.substring(0, buf.length() - 1);
        }
        return mEncodedParamsString;
    }

    /**
     * Http的状态码(respone code)对应的描述
     * @param statusCode 状态码
     * @return 描述
     */
    private String getCause(int statusCode) {
        String cause = null;
        switch (statusCode) {
            case NOT_MODIFIED:
                break;
            case BAD_REQUEST:
                cause = "The request was invalid.  An accompanying error message will explain why. This is the status code will be returned during rate limiting.";
                break;
            case NOT_AUTHORIZED:
                cause = "Authentication credentials were missing or incorrect.";
                break;
            case FORBIDDEN:
                cause = "The request is understood, but it has been refused.  An accompanying error message will explain why.";
                break;
            case NOT_FOUND:
                cause = "The URI requested is invalid or the resource requested, such as a user, does not exists.";
                break;
            case NOT_ACCEPTABLE:
                cause = "Returned by the Search API when an invalid format is specified in the request.";
                break;
            case INTERNAL_SERVER_ERROR:
                cause = "Something is broken.  Please post to the group so the Tming  team can investigate.";
                break;
            case BAD_GATEWAY:
                cause = "Tming is down or being upgraded.";
                break;
            case SERVICE_UNAVAILABLE:
                cause = "Service Unavailable: The Tming servers are up, but overloaded with requests. Try again later. The search and trend methods use this to indicate when you are being rate limited.";
                break;
            default:
                cause = "";
        }
        return statusCode + ":" + cause;
    }

}
