package com.tming.common.net;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.tming.common.CommonApp;
import com.tming.common.util.Constants;

/**
 * <p>发送Multipart数据任务，适用于与服务端进行发送文件。</p>
 * <p>Created by yusongying on 2014年8月15日</p>
 * 
 * @see {@link TmingHttp#asyncPostFile(String, java.util.HashMap, MultipartRequestCallBack)}
 * @see {@link TmingHttp#asyncPostFile(String, java.util.HashMap, java.util.HashMap, MultipartRequestCallBack)}
 * @see {@link TmingHttp#asyncSimpleSendFile(String, String, File, MultipartRequestCallBack)}
 */
public class MultipartDataRequestTask implements Runnable {

    /**
     * 边界标识前缀
     */
    private static final String MULTIPART_PREFIX = "--";
    /**
     * 换行符
     */
    private static final String MULTIPART_LINE_END = "\r\n";
    /**
     * 请求地址
     */
    private String mRequestUrlString = null;
    /**
     * 请求头
     */
    private Map<String, String> mHeaders = null;
    /**
     * 请求参数
     */
    private Map<String, Object> mParams = null;
    /**
     * 请求回调
     */
    private MultipartRequestCallBack mCallBack;
    /**
     * 请求连接对象
     */
    private HttpURLConnection mConnection;
    /**
     * 总共的进度
     */
    private long mTotalProgress;
    /**
     * 当前的进度
     */
    private long mCurProgress;
    /**
     * 标识用户是否取消了
     */
    private boolean mIsUserCancelled = false;
    /**
     * 上次往回调接口返回进度的值
     */
    private int mLastPostProgress = 0;
    /**
     * 服务端返回结果
     */
    private TmingResponse response;
    
    /**
     * 构造发送Multipart数据任务
     * 
     * @param urlString 请求地址
     * @param headers 请求头，可为空
     * @param params 请求参数( {@link String}、{@link Integer} 将会调用 {@code toString()} 使用 text/plain 进行编码。 {@link File} 将会以 application/octet-stream 编码)
     * @param callBack 请求结果回调
     */
    public MultipartDataRequestTask(String urlString, Map<String, String> headers, Map<String, Object> params, MultipartRequestCallBack callBack) {
        mRequestUrlString = urlString;
        mHeaders = headers;
        mParams = params;
        mCallBack = callBack;
    }

    /**
     * 取消上传
     */
    public void cancel() {
        mIsUserCancelled = true;
        if (mConnection != null) {
            mConnection.disconnect();
        }
    }
    
    @Override
    public void run() {
        // 边界标识 随机生成
        String boundary = UUID.randomUUID().toString();
        try {
            URL url = new URL(mRequestUrlString);
            mConnection = (HttpURLConnection) url.openConnection();
            mConnection.setReadTimeout(Constants.HTTP_READ_TIMEOUT * 5);
            mConnection.setConnectTimeout(Constants.HTTP_CONNECT_TIMEOUT);
            mConnection.setDoInput(true);
            mConnection.setDoOutput(true);
            // 设置使用默认块大小进行上传，减小虚拟机堆内存的增加
            // mConnection.setChunkedStreamingMode(0);
            mConnection.setUseCaches(false);
            mConnection.setRequestMethod("POST");
            mConnection.setRequestProperty("Charset", "UTF-8");
            mConnection.setRequestProperty("connection", "keep-alive");
            mConnection.addRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
            
            // 设置头部参数
            if (mHeaders != null) {
                Set<String> headerKeys = mHeaders.keySet();
                for (String headerKey : headerKeys) {
                    mConnection.addRequestProperty(headerKey, mHeaders.get(headerKey));
                }
            }
            
            // 输出请求数据
            OutputStream os = mConnection.getOutputStream();
            if (mParams != null) {
                Set<String> paramKeys = mParams.keySet();
                // 计算进度
                for (String paramKey : paramKeys) {
                    Object paramValue = mParams.get(paramKey);
                    if (paramValue instanceof File) {
                        File file = (File) paramValue;
                        mTotalProgress += file.length();
                    } else {
                        mTotalProgress += paramValue.toString().length();
                    }
                }
                
                // 开始发送数据包内容
                for (String paramKey : paramKeys) {
                    Object paramValue = mParams.get(paramKey);
                    if (paramValue instanceof File) {
                        File file = (File) paramValue;
                        writeFileData(os, boundary, paramKey, file);
                    } else {
                        writeTextPlainData(os, boundary, paramKey, paramValue.toString());
                    }
                }
                // 写入结束标识
                os.write((MULTIPART_PREFIX + boundary + MULTIPART_PREFIX + MULTIPART_LINE_END).getBytes("UTF-8"));
                os.flush();
            }
            
            // 获取服务端返回结果
            response = new TmingResponse(mConnection);
            response.asString();
            int responseCode = mConnection.getResponseCode();
            if (responseCode != 200) {
                throw new TmingHttpException(responseCode + "\n" + response.asString(), responseCode);
            }
            if (mCallBack != null) {
                final Object data = mCallBack.parseMultipartDataResponse(response.asString());
                CommonApp.getAppHandler().post(new Runnable() {
                    
                    @Override
                    public void run() {
                        mCallBack.onPostMultipartDataSuccess(data);
                    }
                });
            }
        } catch (final Throwable e) {
            // 如果非用户取消，将异常发送给回调
            if (mCallBack != null && !mIsUserCancelled) {
                CommonApp.getAppHandler().post(new Runnable() {
                    
                    @Override
                    public void run() {
                        mCallBack.onPostMultipartDataException(e);
                    }
                });
            }
        }
    }
    
    /**
     * 写入表单文本类型数据
     *
     * @param os 请求的输出流
     * @param boundary 分割标识串
     * @param name 表单值名称
     * @param value 表单值
     * @throws UnsupportedEncodingException
     * @throws IOException
     */
    private void writeTextPlainData(OutputStream os, String boundary, String name, String value) throws UnsupportedEncodingException, IOException {
        StringBuffer sb = new StringBuffer();
        sb.append(MULTIPART_PREFIX + boundary + "\r\n");
        sb.append("Content-Disposition: form-data; name=\"" + name + "\"" + MULTIPART_LINE_END);
        sb.append(MULTIPART_LINE_END);
        sb.append(value);
        sb.append(MULTIPART_LINE_END);
        
        os.write(sb.toString().getBytes("UTF-8"));
        plusProgress(value.length());
    }
    
    /**
     * 写入文件类型数据
     *
     * @param os 请求输出流
     * @param boundary 分割标识串
     * @param name 文件表单名
     * @param file 文件对象
     * @throws UnsupportedEncodingException
     * @throws IOException
     */
    private void writeFileData(OutputStream os, String boundary, String name, File file) throws UnsupportedEncodingException, IOException {
        StringBuffer sb = new StringBuffer();
        sb.append(MULTIPART_PREFIX + boundary + "\r\n");
        sb.append("Content-Disposition: form-data; name=\"" + name + "\";filename=\"" + file.getName() + "\"" + MULTIPART_LINE_END);
        sb.append("Content-Type: application/octet-stream; charset=utf-8");
        sb.append(MULTIPART_LINE_END);
        sb.append(MULTIPART_LINE_END);
        
        os.write(sb.toString().getBytes("UTF-8"));
        
        InputStream is = null;
        try {
            is = new FileInputStream(file);
            byte[] bytes = new byte[1024];
            int len = 0;
            while ((len = is.read(bytes)) != -1) {
                os.write(bytes, 0, len);
                plusProgress(len);
            }
            os.write(MULTIPART_LINE_END.getBytes());
        } finally {
            if (is != null) {
                is.close();
            }
        }
    }
    
    /**
     * 加上进度，并调用回调
     * 
     * @param progress 新完成的进度
     */
    private void plusProgress(long progress) {
        mCurProgress += progress;
        if (mCallBack != null) {
            // 已最大值为95进行进度换算，剩余5%为接口返回状态
            final int showProgress = (int) (((double) mCurProgress / (double) mTotalProgress) * 95);
            if (showProgress != mLastPostProgress) {
                CommonApp.getAppHandler().post(new Runnable() {
                    
                    @Override
                    public void run() {
                        mCallBack.onPostMultipartDataProgressUpdate(showProgress);
                    }
                });
                mLastPostProgress = showProgress;
            }
        }
    }
    
    /**
     * 获取请求响应，如果请求还未结束将返回null
     * 
     * @return 请求响应
     */
    public TmingResponse getResponse() {
        if (response == null) {
            try {
                response = new TmingResponse(mConnection);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return response;
    }
    
    /**
     * <p> 发送Multipart结构请求回调接口</p>
     * <p>Created by yusongying on 2014年8月14日</p>
     */
    public static interface MultipartRequestCallBack<T> {
        
        /**
         * 进度反馈， 该方法将在主线程中调用
         *
         * @param progress
         */
        public void onPostMultipartDataProgressUpdate(int progress);
        
        /**
         * 解析服务端返回数据，该方法将在子线程中调用，请勿操作UI
         *
         * @param response 返回字符串
         * @return 解析后数据
         */
        public T parseMultipartDataResponse(String response) throws Throwable;
        
        /**
         * 请求成功的回调，该方法将在主线程中调用
         * 
         * @param respones
         */
        public void onPostMultipartDataSuccess(T respones);
        
        /**
         * 请求发生异常的回调，该方法将在主线程中调用
         * 
         * @param throwable 异常
         */
        public void onPostMultipartDataException(Throwable throwable);
    }
}
