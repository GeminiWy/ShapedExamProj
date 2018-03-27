package com.nd.shapedexamproj.util;

import com.tming.common.cache.net.TmingCacheHttp;
import com.tming.common.net.TmingHttp;
import com.tming.common.net.TmingHttpException;
import com.tming.common.net.TmingResponse;
import com.tming.common.util.Helper;
import org.json.JSONObject;

import java.util.Map;

/**
 * php接口工具类--开大形考
 * @author zll
 * @date 2015.02.27
 */
public class PhpApiUtil {

	private static String TAG = PhpApiUtil.class.getSimpleName();

	private static final String XK_APPID = "2000";
	private static final String PRIVATE_KEY = "f4b387c6a2dff4a46cbdee5490286da8";
	
	/**
	 * 生成接口地址
	 * @param module	请求接口-例如 [user.login]
	 * @return
	 */
	public static String getApiUrl(String module) {
		long time = System.currentTimeMillis() / 1000;
		String sig = Helper.getMD5String(XK_APPID + PRIVATE_KEY + time);
		String apiUrl = Constants.XK_PHP_API + "?_c=" + module + "&_sig=" + sig + "&_v=" + Constants.XK_PHP_API_V + "&_appid=" +
				XK_APPID + "&_time=" + time;
		return apiUrl;
	}
	
	/**
	 * 执行请求接口操作
	 * @param module 请求接口
	 * @param data	业务请求参数
	 * @return
	 */
	public static void sendData(String module, Map<String,Object> data,TmingHttp.RequestCallback callback) {
		String apiUrl = getApiUrl(module);
		TmingHttp.asyncRequest(apiUrl, data,callback);
	}
	/**
	 * 执行带缓存的请求接口操作
	 * @param module 请求接口
	 * @param data	业务请求参数
	 * @return
	 */
	public static void sendData(String module,Map<String,Object> data,TmingCacheHttp.RequestWithCacheCallBackV2 cacheCallBack) {
		String apiUrl = getApiUrl(module);
		TmingCacheHttp.getInstance().asyncRequestWithCache(apiUrl,data,cacheCallBack);
	}

	/**
	 * 获取jSONObject对象
	 */
	public static JSONObject getJSONObject(String module, Map<String, Object> map) {
		JSONObject jobj = null;
		TmingHttp tmingHttp = new TmingHttp();
		try {
			String apiUrl = getApiUrl(module);
			TmingResponse response = tmingHttp.tmingHttpRequest(apiUrl, map);
			jobj = response.asJSONObject();
			return jobj;
		} catch (TmingHttpException e) {
		} catch (OutOfMemoryError e) {

		}
		return null;
	}

}
