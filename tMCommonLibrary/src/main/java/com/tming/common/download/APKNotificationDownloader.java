package com.tming.common.download;

import java.io.File;
import java.util.HashMap;

import android.support.v4.app.NotificationCompat;
import com.tming.common.R;
import com.tming.common.util.Helper;
import com.tming.common.util.Log;
import com.tming.common.util.PhoneUtil;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.widget.RemoteViews;

public class APKNotificationDownloader extends BaseDownload {
	
	private final static String LOG_TAG = "APKNotificationDownloader";
	
	private static final HashMap<String, APKNotificationDownloader> downloaders = new HashMap<String, APKNotificationDownloader>();
	
	private static NotificationManager notificationManager;
	private static int sNotificationIdStartInt = 100;
	
	private Notification updateNtf;
	private String fileName;
	private long lastUpdateNtfTime;
	protected int notificationId;
	
	/**
	 * 开始下载文件，将根据downloadUrl进行唯一性判断
	 * @param context		上下文
	 * @param downloadUrl	下载地址
	 * @param saveToFile	保存文件
	 * @return	若存在相同的地址正在下载则返回false,否则true
	 *
	 * @author yusongying on 2013-12-9
	 */
	public static boolean startDownload(Context context, String downloadUrl, File saveToFile) {
		if(notificationManager == null) {
			notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		}
		if(!downloaders.containsKey(downloadUrl)) {
			synchronized (APKNotificationDownloader.class) {
				if(!downloaders.containsKey(downloadUrl)) {
					APKNotificationDownloader downloader = new APKNotificationDownloader(context, downloadUrl, saveToFile);
					downloader.start();
					Log.d(LOG_TAG, "start download:" + downloadUrl + " notificationId:" + downloader.notificationId);
					return true;
				}
			}
		}
		return false;
	}
	
	
	private APKNotificationDownloader(Context context, String downloadUrl, File saveToFile) {
		super(context, downloadUrl, saveToFile);
		notificationId = sNotificationIdStartInt++;
		downloaders.put(downloadUrl, this);
	}

	@Override
	protected void onPreDownloadFile() {
		super.onPreDownloadFile();
		fileName = downloadUrl.substring(downloadUrl.lastIndexOf("/") + 1, downloadUrl.length());
		RemoteViews contentView = new RemoteViews(context.getPackageName(), R.layout.notification);
		contentView.setTextViewText(R.id.noti_filename, fileName);
		contentView.setTextViewText(R.id.noti_downpercent, "0%");
		contentView.setImageViewResource(R.id.noti_icon, context.getApplicationInfo().icon);
		contentView.setProgressBar(R.id.np, 100, 0, false);
		
		Intent it = PhoneUtil.getStartAppIntent(context, context.getPackageName());
		PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, it, 0);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        builder.setSmallIcon(R.drawable.download);
        builder.setContentTitle("A download in progress");
        builder.setContent(contentView);
        //builder.setContentIntent(pendingIntent);
        updateNtf = builder.build();
		// 设置通知栏显示内容
		updateNtf.flags = Notification.FLAG_NO_CLEAR;
		notificationManager.notify(notificationId, updateNtf);
		Log.d(LOG_TAG, "onPreDownloadFile:" + notificationId);
	}
	
	@Override
	protected void publishProgressOnMainThread(int progress) {
		super.publishProgressOnMainThread(progress);
		// 为了防止频繁的通知导致应用吃紧
		// 每1秒钟更新一次  modified by yusongyin on 2012-4-10
		if (System.currentTimeMillis() - lastUpdateNtfTime > 1000) {
			lastUpdateNtfTime = System.currentTimeMillis();
			updateNtf.contentView.setTextViewText(R.id.noti_downpercent, String.valueOf(progress) + "%");
			updateNtf.contentView.setProgressBar(R.id.np, 100, progress, false);
			notificationManager.notify(notificationId, updateNtf);
			Log.d(LOG_TAG, "publishProgressOnMainThread:" + notificationId);
		}
	}
	
	@Override
	protected void publishExceptionOnMainThread(Exception e) {
		super.publishExceptionOnMainThread(e);
		updateNtf.contentView.setViewVisibility(R.id.noti_complete_install_tip, View.VISIBLE);
		updateNtf.contentView.setTextViewText(R.id.noti_complete_install_tip, context.getResources().getString(R.string.download_failed));
		updateNtf.contentView.setViewVisibility(R.id.np, View.GONE);
		updateNtf.contentView.setViewVisibility(R.id.noti_downpercent, View.GONE);
		updateNtf.flags = Notification.FLAG_AUTO_CANCEL;
		notificationManager.cancel(notificationId);
		Log.d(LOG_TAG, "publishExceptionOnMainThread:" + notificationId);
		notificationManager.notify(notificationId, updateNtf);
		
		downloaders.remove(downloadUrl);
		Helper.ToastUtil(context, context.getResources().getString(R.string.download) + fileName + " " + context.getResources().getString(R.string.failed));
	}
	
	@Override
	protected void publishResultOnMainThread(File saveToFile) {
		super.publishResultOnMainThread(saveToFile);
		Intent installIntent = new Intent(Intent.ACTION_VIEW);
		Uri uri = Uri.fromFile(saveToFile);
		installIntent.setDataAndType(uri, "application/vnd.android.package-archive");
		context.startActivity(installIntent);
		Log.d(LOG_TAG, "publishResultOnMainThread:" + notificationId);
		notificationManager.cancel(notificationId);
		
		downloaders.remove(downloadUrl);
		Helper.ToastUtil(context, context.getResources().getString(R.string.download) + fileName + context.getResources().getString(R.string.success));
	}
}
