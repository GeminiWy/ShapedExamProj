package com.tming.common.util;

import java.util.HashMap;

import org.json.JSONException;

import com.tming.common.CommonApp;
import com.tming.common.net.TmingHttp;
import com.tming.common.net.TmingResponse;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;

/**
 * 版本更新检查
 * @author yusongying on 2013-11-28
 *
 */
public class VersionUpdate extends Thread {
	
	private String mVersionInfoUrl = null;
	private VersionCheckUpadteAdapterInterface mCallback;
	private Context mContext;
	
	private VersionUpdate(String versionInfoUrl, VersionCheckUpadteAdapterInterface callback) {
		mContext = CommonApp.getAppContext();
		mCallback = callback;
		mVersionInfoUrl = versionInfoUrl;
	}
	
	@Override
	public void run() {
		super.run();
		try {
			TmingResponse response = TmingHttp.tmingHttpRequest(mVersionInfoUrl, null);
			final VersionInfo versionInfo = mCallback.checkUpdateJsonParse(response.asString());
			PackageInfo packageInfo = mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0);
			int currentVersionCode = packageInfo.versionCode;
			if(currentVersionCode < versionInfo.versionCode) {
				CommonApp.getAppHandler().post(new Runnable() {
					
					@Override
					public void run() {
						mCallback.checkUpadteHasNewVersion(versionInfo);
					}
				});
			} else {
				CommonApp.getAppHandler().post(new Runnable() {
					
					@Override
					public void run() {
						mCallback.checkUpadteVersionIsNewest();
					}
				});
			}
		} catch (final Exception e) {
			CommonApp.getAppHandler().post(new Runnable() {
				
				@Override
				public void run() {
					mCallback.checkUpdateError(e);
				}
			});
		}
		
	}
	
	/**
	 * 版本更新检差
	 * @param versionInfoUrl
	 * @param callback
	 *
	 * @author yusongying on 2013-11-28
	 */
	public static void checkHasVersionUpdate(String versionInfoUrl, VersionCheckUpadteAdapterInterface callback) {
		if (PhoneUtil.checkNetworkEnable() == PhoneUtil.NETSTATE_DISABLE) {//如果是离线状态
			callback.checkUpdateNetworkUnavailable();
			return;
		}
		callback.checkUpdateBefore();
		new VersionUpdate(versionInfoUrl, callback).start();
	}
	
	/**
	 * 版本信息
	 */
	public static class VersionInfo {
		/**
		 * 版本号
		 */
		public int versionCode;
		
		/**
		 * 版本名称
		 */
		public String versionName;
		
		/**
		 * 新版本升级描述
		 */
		public String newVersionDesc;
		
		/**
		 * 新版本下载地址
		 */
		public String newVersionDownloadUrl;
		
		/**
		 * 是否为强制升级
		 */
		public boolean isForcedUpdate;
	}
	
	/**
	 *	版本检测适配 器
	 */
	public static interface VersionCheckUpadteAdapterInterface {
		
		/**
		 * 启动检查前
		 */
		public void checkUpdateBefore();
		
		/**
		 * json数据解析，将在子线程中调用
		 * @param jsonString 	json字符串
		 * @return				版本信息
		 */
		public VersionInfo checkUpdateJsonParse(String jsonString) throws Exception;
		
		/**
		 * 检测到新版本，将在主线程中调用
		 * @param info 版本信息
		 *
		 */
		public void checkUpadteHasNewVersion(VersionInfo info);
		
		/**
		 * 当前已经为最新的安装包，将在主线程中调用
		 */
		public void checkUpadteVersionIsNewest();
		
		/**
		 * 网络不可用，将在主线程中调用
		 */
		public void checkUpdateNetworkUnavailable();
		
		/**
		 * 发生错误，将在主线程中调用
		 */
		public void checkUpdateError(Exception e);
	}
	
}
