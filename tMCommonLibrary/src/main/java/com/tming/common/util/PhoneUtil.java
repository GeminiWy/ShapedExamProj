package com.tming.common.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

import com.tming.common.CommonApp;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.MemoryInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Environment;
import android.os.Build.VERSION;
import android.provider.Settings;
import android.provider.Settings.Secure;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.text.format.Formatter;
import android.view.Window;
import android.view.WindowManager;

public final class PhoneUtil {
	public static final int NETWORKTYPE_INVALID = 1;
	public static final int NETWORKTYPE_2G = 2;
	public static final int NETWORKTYPE_3G = 3;
	public static final int NETWORKTYPE_WIFI = 4;
	public static final int NETWORKTYPE_WAP = 5;

	public static final int NETSTATE_ENABLE = 1;
	public static final int NETSTATE_DISABLE = 2;
	public static final int NETSTATE_UNKOWN = 3;

	private static final String LOG_TAG = "PhoneUtil";

	/**
	 * 检查权限
	 * 
	 * @param context
	 * @param permission
	 * @return
	 * 
	 * @author yusongying on 2013-7-19
	 */
	public static boolean checkPermissions(Context context, String permission) {
		PackageManager packagemanager = context.getPackageManager();
		return packagemanager.checkPermission(permission,
				context.getPackageName()) == PackageManager.PERMISSION_GRANTED;
	}

	public static String getPackageName(Context context) {
		return context.getPackageName();
	}

	/**
	 * 手机宽高
	 */
	@SuppressWarnings("deprecation")
	public static int[] getDisplayWidthHeight(Activity act) {
		WindowManager wm = (WindowManager) act.getSystemService(Context.WINDOW_SERVICE);
		int height = wm.getDefaultDisplay().getHeight() - act.getWindow().findViewById(Window.ID_ANDROID_CONTENT).getTop();
		int width = wm.getDefaultDisplay().getWidth();
		return new int[] { width, height };
	}

	/**
	 * 获取手机wifi的mac地址
	 * 
	 * @param context
	 * @return
	 * 
	 * @author yusongying on 2013-7-19
	 */
	public static String getWifiMacAddress(Context context) {
		WifiInfo wifiinfo;
		WifiManager wifimanager = (WifiManager) context
				.getSystemService(Context.WIFI_SERVICE);
		try {
			if (!checkPermissions(context, "android.permission.ACCESS_WIFI_STATE")) {
				Log.e(LOG_TAG, "Could not get mac address.no permission android.permission.ACCESS_WIFI_STATE");
				return null;
			}
			wifiinfo = wifimanager.getConnectionInfo();
			return wifiinfo.getMacAddress();
		} catch (Exception exception) {
			Log.e(LOG_TAG, (new StringBuilder()).append("Could not get mac address.").append(exception.toString()).toString());
		}
		return null;
	}

	public static String getIMEI(Context context) {
		TelephonyManager telephonymanager = (TelephonyManager) context
				.getSystemService(Context.TELEPHONY_SERVICE);
		String imei = "";
		try {
			if (checkPermissions(context, "android.permission.READ_PHONE_STATE"))
				imei = telephonymanager.getDeviceId();
		} catch (Exception exception) {
			Log.e(LOG_TAG, "No IMEI.", exception);
		}
		return imei;
	}

	public static String getAndroidId(Context context) {
		return Secure.getString(context.getContentResolver(), "android_id");
	}

	/**
	 * 获取手机的唯一标识
	 * 
	 * @param context
	 * @return
	 * 
	 * @author yusongying on 2013-7-19
	 */
	/*
	 * public static String getPhoneUUID(Context context) { TelephonyManager
	 * telephonymanager = (TelephonyManager) context .getSystemService("phone");
	 * String uuid = ""; try { if (checkPermissions(context,
	 * "android.permission.READ_PHONE_STATE")) uuid = "deviceId:" +
	 * telephonymanager.getDeviceId(); } catch (Exception exception) {
	 * Log.e(LOG_TAG, "No IMEI.", exception); } if (TextUtils.isEmpty(uuid)) {
	 * Log.e(LOG_TAG, "No IMEI."); uuid = "wifiMacAddress:" +
	 * getWifiMacAddress(context); if (TextUtils.isEmpty(uuid)) { Log.e(LOG_TAG,
	 * "Failed to take mac as IMEI. Try to use Secure.ANDROID_ID instead.");
	 * uuid = "android_id:" + Secure.getString(context.getContentResolver(),
	 * "android_id"); Log.e(LOG_TAG, (new StringBuilder())
	 * .append("getDeviceId: Secure.ANDROID_ID: ") .append(uuid).toString());
	 * return uuid; } } return uuid; }
	 */

	/**
	 * 
	 * 当前的网络状况
	 * 
	 * @author yusongying on 2013-7-23
	 */
	/*
	 * public static int getCurrentNetWorkType(Context context) {
	 * ConnectivityManager manager = (ConnectivityManager) context
	 * .getSystemService(Context.CONNECTIVITY_SERVICE); NetworkInfo networkInfo
	 * = manager.getActiveNetworkInfo(); int mNetWorkType = 0; if (networkInfo
	 * != null && networkInfo.isConnected()) { String type =
	 * networkInfo.getTypeName(); if (type.equalsIgnoreCase("WIFI")) {
	 * mNetWorkType = NETWORKTYPE_WIFI; } else if
	 * (type.equalsIgnoreCase("MOBILE")) { String proxyHost =
	 * android.net.Proxy.getDefaultHost(); mNetWorkType =
	 * TextUtils.isEmpty(proxyHost) ? (isFastMobileNetwork(context) ?
	 * NETWORKTYPE_3G : NETWORKTYPE_2G) : NETWORKTYPE_WAP; } } else {
	 * mNetWorkType = NETWORKTYPE_INVALID; } return mNetWorkType; }
	 */

	private static boolean isFastMobileNetwork(Context context) {
		TelephonyManager telephonyManager = (TelephonyManager) context
				.getSystemService(Context.TELEPHONY_SERVICE);
		switch (telephonyManager.getNetworkType()) {
		case TelephonyManager.NETWORK_TYPE_1xRTT:
			return false; // ~ 50-100 kbps
		case TelephonyManager.NETWORK_TYPE_CDMA:
			return false; // ~ 14-64 kbps
		case TelephonyManager.NETWORK_TYPE_EDGE:
			return false; // ~ 50-100 kbps
		case TelephonyManager.NETWORK_TYPE_EVDO_0:
			return true; // ~ 400-1000 kbps
		case TelephonyManager.NETWORK_TYPE_EVDO_A:
			return true; // ~ 600-1400 kbps
		case TelephonyManager.NETWORK_TYPE_GPRS:
			return false; // ~ 100 kbps
		case TelephonyManager.NETWORK_TYPE_HSDPA:
			return true; // ~ 2-14 Mbps
		case TelephonyManager.NETWORK_TYPE_HSPA:
			return true; // ~ 700-1700 kbps
		case TelephonyManager.NETWORK_TYPE_HSUPA:
			return true; // ~ 1-23 Mbps
		case TelephonyManager.NETWORK_TYPE_UMTS:
			return true; // ~ 400-7000 kbps
		case 14:
			return true; // ~ 1-2 Mbps
		case 12:
			return true; // ~ 5 Mbps
		case 15:
			return true; // ~ 10-20 Mbps
		case TelephonyManager.NETWORK_TYPE_IDEN:
			return false; // ~25 kbps
		case 13:
			return true; // ~ 10+ Mbps
		case TelephonyManager.NETWORK_TYPE_UNKNOWN:
			return false;
		default:
			return false;
		}
	}

	/**
	 * 
	 * 设备ID
	 * 
	 * @author yusongying on 2013-7-23
	 */
	public static String getDeviceId(Context context) {
		if (checkPermissions(context, "android.permission.READ_PHONE_STATE")) {
			TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
			return telephonyManager.getDeviceId();
		} else {
			Log.e(LOG_TAG, "Could not get mac address.no permission android.permission.READ_PHONE_STATE");
			return "";
		}
	}

	/**
	 * @deprecated use {@link #checkNetworkEnable()} instead.
	 * <p>检查网络状态是否可用</P>
	 *
	 * @param cx Android上下文
	 * @return {@link #NETSTATE_ENABLE} 网络可用、{@link #NETSTATE_DISABLE} 网络不可用、{@link #NETSTATE_UNKOWN} 网络状态未知
	 */
	public static int checkNetworkEnable(Context cx) {
		return CommonApp.sNetWorkState;
	}

    /**
     * <p>检查网络状态是否可用</P>
     *
     * @return {@link #NETSTATE_ENABLE} 网络可用、{@link #NETSTATE_DISABLE} 网络不可用、{@link #NETSTATE_UNKOWN} 网络状态未知
     */
    public static int checkNetworkEnable() {
        return CommonApp.sNetWorkState;
    }

	/**
	 * 获得当前屏幕亮度值 0--255
	 * 
	 */
	public static int getScreenBrightness(Context context) {
		int screenBrightness = 255;
		try {
			screenBrightness = Settings.System.getInt(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return screenBrightness;
	}

	/**
     * @deprecated use {@link #isWifiNetwork()} instead.
	 * 判断网络类型是否为wifi
	 * 
	 * @return
	 */
	public static boolean isWifiNetwork(Context context) {
		return CommonApp.sNetWorkIsWifi;
	}

    /**
     * 判断网络类型是否为wifi
     *
     * @return
     */
    public static boolean isWifiNetwork() {
        return CommonApp.sNetWorkIsWifi;
    }

	/**
	 * 保存当前的屏幕亮度值，并使之生效
	 */
	public static void setScreenBrightness(Activity activity, int paramInt) {
		if (paramInt < 10) {// 防止黑屏
			paramInt = 10;
		}
		Window localWindow = activity.getWindow();
		WindowManager.LayoutParams localLayoutParams = localWindow
				.getAttributes();
		float f = paramInt / 255.0F;
		localLayoutParams.screenBrightness = f;
		localWindow.setAttributes(localLayoutParams);
	}

	/**
	 * 第一行是CPU的型号，第二行是CPU的频率
	 * 
	 * @author yusongying on 2013-7-23
	 */
	public static String[] getCpuInfo() {
		String str1 = "/proc/cpuinfo";
		String str2 = "";
		String[] cpuInfo = { "", "" };
		String[] arrayOfString;
		BufferedReader localBufferedReader = null;
		try {
			FileReader fr = new FileReader(str1);
			localBufferedReader = new BufferedReader(fr, 8192);
			str2 = localBufferedReader.readLine();
			arrayOfString = str2.split("\\s+");
			for (int i = 2; i < arrayOfString.length; i++) {
				cpuInfo[0] = cpuInfo[0] + arrayOfString[i] + " ";
			}
			str2 = localBufferedReader.readLine();
			arrayOfString = str2.split("\\s+");
			cpuInfo[1] += arrayOfString[2];
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (localBufferedReader != null)
					localBufferedReader.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return cpuInfo;
	}

	/**
	 * 判断是否有所需的目录
	 * 
	 * @author Caiyx
	 * @return
	 */
	public static boolean hasExistedDir() {
		File f = new File(Environment.getExternalStorageDirectory()
				+ "/gamereader");
		if (f.exists()) {
			f = new File(Environment.getExternalStorageDirectory()
					+ "/gamereader/softwares");
			if (f.exists()) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 取SD卡路径不带/
	 * 
	 * @return
	 */
	public static String getSDPath() {
		File sdDir = null;
		boolean sdCardExist = Environment.getExternalStorageState().equals(
				android.os.Environment.MEDIA_MOUNTED); // 判断SD卡是否存在
		if (sdCardExist) {
			sdDir = Environment.getExternalStorageDirectory();// 获取根目录
		}
		if (sdDir != null) {
			return sdDir.toString();
		} else {
			return null;
		}
	}

	/**
	 * 启动App
	 * 
	 * @param context
	 * @param packageName
	 *            应用包名
	 * @return
	 * 
	 * @author yusongying on 2013-12-10
	 */
	public static Intent getStartAppIntent(Context context, String packageName) {
		try {
			PackageManager pManager = context.getPackageManager();
			PackageInfo pi = pManager.getPackageInfo(packageName, 0);
			Intent resolveIntent = new Intent(Intent.ACTION_MAIN, null);
			resolveIntent.setPackage(pi.packageName);
			List<ResolveInfo> apps = pManager.queryIntentActivities(
					resolveIntent, 0);
			ResolveInfo ri = apps.get(0);
			if (ri != null) {
				packageName = ri.activityInfo.packageName;
				String className = ri.activityInfo.name;
				Intent intent = new Intent(Intent.ACTION_MAIN);
				ComponentName cn = new ComponentName(packageName, className);
				intent.setComponent(cn);
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				return intent;
			}
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}

	private static int sVersionInt = -1;

	/**
	 * 获取SKD版本号
	 * 
	 * @return
	 * 
	 * @author yusongying on 2013-12-10
	 */
	public static int getSDKVersionInt() {
		if (sVersionInt == -1) {
			Class<? extends Object> versionCls = Build.VERSION.class;
			try {
				sVersionInt = versionCls.getField("SDK_INT").getInt(
						new Build.VERSION());
				Log.d(LOG_TAG, "sVersionInt = " + sVersionInt);
				return sVersionInt;
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (NoSuchFieldException e) {
				e.printStackTrace();
			}
		}
		return sVersionInt;
	}

	/**
	 * 获取系统内存大小
	 */
	public static long getTotalMemory(Context context) {
		String str1 = "/proc/meminfo";// 系统内存信息文件
		String str2;
		String[] arrayOfString;
		long initial_memory = 0;

		try {
			FileReader localFileReader = new FileReader(str1);
			BufferedReader localBufferedReader = new BufferedReader(
					localFileReader, 8192);
			str2 = localBufferedReader.readLine();// 读取meminfo第一行，系统总内存大小

			arrayOfString = str2.split("\\s+");
			for (String num : arrayOfString) {
				Log.i(str2, num + "\t");
			}

			initial_memory = Integer.valueOf(arrayOfString[1]).intValue() * 1024;// 获得系统总内存，单位是KB，乘以1024转换为Byte
			localBufferedReader.close();

		} catch (IOException e) {
		}
//		return Formatter.formatFileSize(context, initial_memory);// Byte转换为KB或者MB，内存大小规格化
		return initial_memory;
	}

	/**
	 * 获取系统可用内存
	 */
	public static long getAvailMemory(Context context) {// 获取android当前可用内存大小

		ActivityManager am = (ActivityManager) context
				.getSystemService(Context.ACTIVITY_SERVICE);
		MemoryInfo mi = new MemoryInfo();
		am.getMemoryInfo(mi);
		// mi.availMem; 当前系统的可用内存

//		return Formatter.formatFileSize(context, mi.availMem);// 将获取的内存大小规格化
		return mi.availMem;
	}

}
