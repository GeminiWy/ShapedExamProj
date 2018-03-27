package com.tming.common.util;

import java.io.FileInputStream;
import java.lang.reflect.Field;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import android.app.ActivityManager;
import android.content.*;
import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v4.content.LocalBroadcastManager;
import android.telephony.TelephonyManager;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;


public final class Helper {
	private static final String LOG_TAG = "Helper";
	private static final char HEX_DIGITS[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',  
        'a', 'b', 'c', 'd', 'e', 'f' }; 
	/**
	 * 将字符串进行MD5 32位小写加密
	 * @param str
	 * @return
	 */
	public static String getMD5String(String str) {
		StringBuffer stringbuffer;
		MessageDigest messagedigest;
		try {
			messagedigest = MessageDigest.getInstance("MD5");
			messagedigest.update(str.getBytes());
			byte abyte0[] = messagedigest.digest();
			stringbuffer = new StringBuffer();
			for (int i = 0; i < abyte0.length; i++) {
//				if ((abyte0[i] & 0xFF) < 0x10) { 
//					stringbuffer.append("0");
//				} else {
//					stringbuffer.append(Integer.toHexString(abyte0[i] & 0xFF));
//				}
				stringbuffer.append(HEX_DIGITS[(abyte0[i] & 0xf0) >>> 4]);    
				stringbuffer.append(HEX_DIGITS[abyte0[i] & 0x0f]);
			}
			return stringbuffer.toString();
		} catch (NoSuchAlgorithmException e) {
			Log.e(LOG_TAG, "getMD5 error", e);
			e.printStackTrace();
			return "";
		}
	}
	
	public static void printObject(Object object) {
		Class<?> class1 = object.getClass();
		Field[] fields = class1.getDeclaredFields();
		for(Field field : fields) {
			try {
				field.setAccessible(true);
				Log.e(LOG_TAG, field.getName() + "=" + String.valueOf(field.get(object)));
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		}
	}
	
	public static String unitByte(long size) {
    	int mb = 0,kb=0;
    	mb = (int) (size / (1024 * 1024));
    	kb = (int) ((size % (1024 * 1024)) / 1024);
    	return mb==0 ? kb==0 ? size + "B ": kb + "KB " : mb + "MB " + kb + "KB ";
    }
	
	//add by lpf:2013-9-11
	private static long theOldTime = 0;
	public static void ToastUtil(Context context, String textStr) {
		if (theOldTime == 0) {// 解决Toast重复出现问题
			theOldTime = System.currentTimeMillis();
		} else {
			if (System.currentTimeMillis() - theOldTime < 2000) {
				return;
			}
		}
		Toast.makeText(context, textStr, Toast.LENGTH_SHORT).show();
		theOldTime = System.currentTimeMillis();
	}
	
	//add by lpf:2013-9-15 获得imsi号
	public static String getImsi(Context context) {
		TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
		return telephonyManager.getSubscriberId();
	}
	
	/**
	 * 获取imei号
	 * @param context
	 * @return
	 */
	public static String getImei(Context context){
		TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);  
		return tm.getDeviceId();
	}

	/** 
     * 根据手机的分辨率从 dp 的单位 转成为 px(像素) 
     */  
    public static int dip2px(Context context, float dpValue) {  
        final float scale = context.getResources().getDisplayMetrics().density;  
        return (int) (dpValue * scale + 0.5f);  
    } 
    
	 /** 
     * 根据手机的分辨率从 px(像素) 的单位 转成为 dp 
     */  
    public static int px2dip(Context context, float pxValue) {  
        final float scale = context.getResources().getDisplayMetrics().density;  
        return (int) (pxValue / scale + 0.5f);  
    }

	/**
	 * <p>判断当前 Activity 是否是在最前端</p>
	 *
	 * <strong>requires android.permission.GET_TASKS</strong>
	 *
	 * @param activity
	 * @return
	 */
	public static boolean isRunningForeground(Activity activity) {
		ActivityManager activityManager = (ActivityManager) activity.getSystemService(Context.ACTIVITY_SERVICE);
		ComponentName componentName = activityManager.getRunningTasks(1).get(0).topActivity;
		if (activity.getPackageName().equals(componentName.getPackageName()) && componentName.getClassName().equals(activity.getClass().getName())) {
			return true;
		}
		return false;
	}

	/**
	 * 获取展示给用户的时间
	 * @param timestamp
	 * @return
	 */
	public static String getShowTime(long timestamp) {
		if(timestamp < 1) return "unkown";
		long interval = System.currentTimeMillis() - timestamp;
		if(interval < 1000 * 60 * 60) {
			if (interval / (1000 * 60) < 1) {
				return "1 minutes ago";
			}else{
				return interval / (1000 * 60) + " minutes ago";
			}
		} else if(interval < 1000 * 60 * 60 * 24) {
			return interval / (1000 * 60 * 60) + " hours ago";
		} else {
			return interval / (1000 * 60 * 60*24) + " days ago";
		}
	}
	
    /*
     * 判断网络连接是否已开
     *true 已打开  false 未打开
     * */
    public static boolean isConn(Context context){
        boolean bisConnFlag=false;
        ConnectivityManager conManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo network = conManager.getActiveNetworkInfo();
        if(network!=null){
            bisConnFlag=conManager.getActiveNetworkInfo().isAvailable();
        }
        return bisConnFlag;
    }
	
	//add by lpf:2013-10-11设置封推背景
	/*public static void setRecommendBackground(View linearAll){				
		Bitmap bitmap;
		
		try {
			bitmap = BitmapFactory.decodeStream(new FileInputStream(ImageCacheTool.getInstance().getRecommendBackgroundPath()));
			BitmapDrawable bd = new BitmapDrawable(App.getAppContext().getResources(), bitmap); 
			linearAll.setBackgroundDrawable(bd);
		} catch (Exception e) {
			e.printStackTrace();
		} catch (OutOfMemoryError e) {
			e.printStackTrace();
		}
	}*/
	
	public static String fixHtmlText(String text) {
		if(text != null) {
			text = text.trim().replaceAll("\\\\+", "");
			text = text.replace("&amp;", "&");
			text = text.replace("&lt;", "<");
			text = text.replace("&gt;", ">");
			text = text.replace("&quot;", "\"");
		}
		text = Html.fromHtml(text).toString();
		return text;
	}
	
	public static String replaceHtmlText(String text) {
	    if(text != null) {
            text = text.trim().replaceAll("\\\\+", "");
            text = text.replace("&amp;", "&");
            text = text.replace("&lt;", "<");
            text = text.replace("&gt;", ">");
            text = text.replace("&quot;", "\"");
            text = text.replace("&apos;", "\'");
            text = text.replace("&cent;", "￠");
            text = text.replace("&pound;", "£");
            text = text.replace("&yen;", "¥");
            text = text.replace("&euro;", "€");
            text = text.replace("&sect;", "§");
            text = text.replace("&copy;", "©");
            text = text.replace("&reg;", "®");
            text = text.replace("&copy;", "©");
            text = text.replace("&trade;", "™");
            text = text.replace("&times;", "×");
            text = text.replace("&divide;", "÷");
        }
        return text;
	}
	
	/**
	 * 仅在本地应用中发送广播
	 * @param context
	 * @param intent
	 */
	public static void sendLocalBroadCast(Context context, Intent intent) {
		LocalBroadcastManager manager = LocalBroadcastManager.getInstance(context);
		manager.sendBroadcast(intent);
	}
	
	/**
	 * 注册本地应用的广播监听
	 * @param context
	 * @param broadcastReceiver
	 * @param intentFilter
	 */
	public static void registLocalReciver(Context context, BroadcastReceiver broadcastReceiver, IntentFilter intentFilter) {
		LocalBroadcastManager manager = LocalBroadcastManager.getInstance(context);
		manager.registerReceiver(broadcastReceiver, intentFilter);
	}
	
	/**
	 * 注销本地应用的广播监听
	 * @param context
	 * @param receiver
	 */
	public static void unregistLocalReciver(Context context, BroadcastReceiver receiver) {
		LocalBroadcastManager manager = LocalBroadcastManager.getInstance(context);
		manager.unregisterReceiver(receiver);
	}
}
