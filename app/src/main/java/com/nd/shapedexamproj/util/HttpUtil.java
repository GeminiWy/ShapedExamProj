package com.nd.shapedexamproj.util;

import android.os.Handler;
import com.tming.common.util.Log;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * 
 *@ClassName: HttpUtil
 *@Title:
 *@Description:get请求带参数，上传文件  from 方式
 *@Author:Abay Zhuang
 *@Since:2014-5-8下午11:40:24 
 *@Version:1.0
 */
public class HttpUtil {

	private static final String LOG_TAG = HttpUtil.class.getSimpleName();

	private static final int OK = 200;// OK: Success!
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

	public static final Executor S_EXECUTOR = Executors.newFixedThreadPool(3);
	private static Handler sHandler;

	public static void init(Handler handler) {
		sHandler = handler;
	}

	/**
	 * 
	 * Http请求回调
	 * 
	 * @param <T>
	 *            返回值
	 */
	public static interface RequestCallback<T> {

		/**
		 * 请求成功的回调，可以在该方法中解析数据, 该方法将在线程中调用,请勿操作ui。
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
     * 
     * Http请求回调
     * 
     * @param <T>
     *            返回值
     */
    public static interface RequestCallbackV2<T> {

        /**
         * 请求成功的回调，可以在该方法中解析数据, 该方法将在线程中调用,请勿操作ui。
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
        /**
         * <p>抛出错误</P>
         */
        public void error();
    }

    /**
     * <p>异步请求网络连接</p>
     * @param url 请求地址
     * @param params
     * @param callback 回调
     * @param <T> 返回到主线程的数据类型
     */
	public static <T> void asyncRequestGet(final String url,
			final Map<String, Object> params, final RequestCallback<T> callback) {
		S_EXECUTOR.execute(new Runnable() {

			@Override
			public void run() {
				try {
					String sUrl = url + "?" + encodeParameters(params);
					Log.e("HttpUtil", "Url :" + sUrl);
					// 创建DefaultHttpClient对象
					HttpClient httpclient = new DefaultHttpClient();
					// 创建一个HttpGet对象
					HttpGet get = new HttpGet(sUrl);
					// 获取HttpResponse对象
					HttpResponse response = httpclient.execute(get);

					if (callback != null) {
						final T data = callback.onReqestSuccess(EntityUtils
								.toString(response.getEntity()));
						sHandler.post(new Runnable() {

							@Override
							public void run() {
								callback.success(data);
							}
						});
					}
				} catch (final Exception e) {
					e.printStackTrace();
					sHandler.post(new Runnable() {

						@Override
						public void run() {
							callback.exception(e);
						}
					});
				}
			}

		});
	}

	/**
	 * Build up post params encode to String Object
	 * 
	 * @param httpParams
	 * @return String
	 */
	public static String encodeParameters(Map<String, Object> httpParams) {
		if (httpParams == null)
			return "";
		StringBuffer buf = new StringBuffer();

		Set<String> keys = httpParams.keySet();
		for (String key : keys) {
			try {
				buf.append(key)
						.append("=")
						.append(URLEncoder.encode(
								String.valueOf(httpParams.get(key)), "UTF-8"))
						.append("&");
			} catch (UnsupportedEncodingException neverHappen) {
			}
		}
		return buf.length() > 0 ? buf.substring(0, buf.length() - 1) : "";
	}

	/**
	 * 
	 * @Title: asyncUploadFile
	 * @Author: Abay Zhuang
	 * @Since: 2014-5-8下午7:49:22
	 * @param file
	 * @param RequestURL
	 * @param callback
	 */
	public static <T> void asyncUploadFile(final File file,
			final String RequestURL, final RequestCallbackV2<T> callback) {
		S_EXECUTOR.execute(new Runnable() {

			@Override
			public void run() {
				try {
					String result = uploadFile(file, RequestURL);
					if (callback != null) {
						final T data = callback.onReqestSuccess(result);
						sHandler.post(new Runnable() {

							@Override
							public void run() {
								callback.success(data);
							}
						});
					}
				} catch (final Throwable e) {
				    e.printStackTrace();
					if (e instanceof OutOfMemoryError) {
    					sHandler.post(new Runnable() {
    
    						@Override
    						public void run() {
    							callback.error();
    						}
    					});
					} else if (e instanceof Exception) {
					    sHandler.post(new Runnable() {
					        
                            @Override
                            public void run() {
                                callback.exception((Exception)e);
                            }
                        });
					}
				} 
			}
		});
	}

//	public static String uploadFile(File file, String RequestURL)
//			throws ClientProtocolException, IOException {
//		// 创建DefaultHttpClient对象
//		HttpClient httpclient = new DefaultHttpClient();
//		 httpclient.getParams().setParameter(  
//	                CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);  
//	        HttpPost httppost = new HttpPost(RequestURL);  
//	          
//	        MultipartEntity entity = new MultipartEntity();  
//	          
//	        
//	        FileBody fileBody = new FileBody(file);  
//	        // <input type="file"
//			// name="userfile" /> 对应的
//	        entity.addPart("imgfile", fileBody);  
//	              
//	        httppost.setEntity(entity);  
//	        HttpResponse response = httpclient.execute(httppost);
//		
//		
//		// HttpPost httppost = new HttpPost(RequestURL);
//		// MultipartEntityBuilder builder = MultipartEntityBuilder.create();
//		// builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
//		// FileBody fb = new FileBody(file);
//		// builder.addPart("imgfile", fb);
//		// HttpEntity yourEntity = builder.build();
//		//
//		// httppost.setEntity(yourEntity);
//		//
//		// HttpResponse response = httpclient.execute(httppost);
//		// System.out.println("executing request " + httppost.getRequestLine());
//		return EntityUtils.toString(response.getEntity());
//	}

	/**
	 * 图片上传
	 * 表单式的上传数据
	 * @throws IOException
	 */
	public static String uploadFile(File file, String RequestURL)
			throws IOException {
		int TIME_OUT = 120 * 1000; // 超时时间
		String CHARSET = "utf-8"; // 设置编码
		String BOUNDARY = UUID.randomUUID().toString(); // 边界标识 随机生成
		String PREFIX = "--", LINE_END = "\r\n";
		String CONTENT_TYPE = "multipart/form-data"; // 内容类型
		String formName = "imgfile";

		URL url = new URL(RequestURL);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setReadTimeout(TIME_OUT);
		conn.setConnectTimeout(TIME_OUT);
		conn.setDoInput(true); // 允许输入流
		conn.setDoOutput(true); // 允许输出流
		conn.setUseCaches(false); // 不允许使用缓存
		conn.setRequestMethod("POST"); // 请求方式
		conn.setRequestProperty("Charset", CHARSET); // 设置编码
		conn.setRequestProperty("connection", "keep-alive");
		conn.setRequestProperty("Content-Type", CONTENT_TYPE + ";boundary="
				+ BOUNDARY);
		if (file != null) {
			/**
			 * 当文件不为空，把文件包装并且上传
			 */
			OutputStream outputSteam = conn.getOutputStream();

			DataOutputStream dos = new DataOutputStream(outputSteam);
			StringBuffer sb = new StringBuffer();
			sb.append(PREFIX);
			sb.append(BOUNDARY);
			sb.append(LINE_END);
			/**
			 * 这里重点注意： name里面的值为服务器端需要key 只有这个key 才可以得到对应的文件
			 * filename是文件的名字，包含后缀名的 比如:abc.png
			 */

			sb.append("Content-Disposition: form-data; name=\"" + formName
					+ "\"; filename=\"" + file.getName() + "\"" + LINE_END);
			sb.append("Content-Type: application/octet-stream; charset="
					+ CHARSET + LINE_END);
			sb.append(LINE_END);
			Log.e("HttpUtil", sb.toString());
			dos.write(sb.toString().getBytes());
			InputStream is = new FileInputStream(file);
			byte[] bytes = new byte[1024];
			int len = 0;
			while ((len = is.read(bytes)) != -1) {
				dos.write(bytes, 0, len);
			}
			is.close();
			dos.write(LINE_END.getBytes());
			byte[] end_data = (PREFIX + BOUNDARY + PREFIX + LINE_END)
					.getBytes();
			dos.write(end_data);
			dos.flush();
			/**
			 * 获取响应码 200=成功 当响应成功，获取响应的流
			 */
			int res = conn.getResponseCode();
			if (res == 200) {
				InputStream input = conn.getInputStream();
				StringBuffer sb1 = new StringBuffer();
				int ss = 0;
				while ((ss = input.read()) != -1) {
					sb1.append((char) ss);
				}
				String result = sb1.toString();
				System.out.println("UPLOAD:" + result);
				return result;
			}
		}
		return "";
	}
	/**
     * <p>获取文件大小</P>
     *
     * @param urlStr
     * @return
     */
    public static int getContentLength(String urlStr) {
        int contentLength = 0;
        try {
            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            contentLength = conn.getContentLength();
            Log.e(LOG_TAG, "文件大小为：" + contentLength);
            conn.disconnect();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return contentLength;
    }
}
