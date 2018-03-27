package com.tming.common.download;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import android.content.Context;

import com.tming.common.CommonApp;
import com.tming.common.util.Constants;

/**
 * 下载文件基础类
 * @author yusongying on 2013-11-29
 *
 */
public class BaseDownload extends Thread {
	
	/**
	 * 下载地址
	 */
	protected String downloadUrl;
	
	/**
	 * 保存文件
	 */
	protected File saveToFile;
	
	protected Context context;
	private boolean isStopFlag = false;
    private HttpURLConnection mConnection = null;
	
	public BaseDownload(Context context, String downloadUrl, File saveToFile) {
		this.context = context;
		this.downloadUrl = downloadUrl;
		this.saveToFile = saveToFile;
	}
	
	@Override
	public void run() {
		super.run();
		InputStream is = null;
		FileOutputStream fos = null;
		try {
			publishPreDownload();
            if (isStopFlag) {
                throw new RuntimeException("user canceled download task.");
            }
			URL url = new URL(downloadUrl);
            mConnection = (HttpURLConnection) url.openConnection();
            mConnection.setInstanceFollowRedirects(true);
            mConnection.setReadTimeout(Constants.HTTP_READ_TIMEOUT * 5);
            mConnection.setConnectTimeout(Constants.HTTP_CONNECT_TIMEOUT);
			is = mConnection.getInputStream();
			int contentLength = mConnection.getContentLength();
			File parentFile = saveToFile.getParentFile();
			if(!parentFile.exists()) {
				parentFile.mkdirs();
			}
			fos = new FileOutputStream(saveToFile);
			byte[] buf = new byte[1024];
			int len = 0;
			int curSize = 0;
			while((len = is.read(buf)) != -1 && !isStopFlag) {
				curSize += len;
				publishProgress((int) (curSize * 100f / contentLength));
				fos.write(buf, 0, len);
			}
			if(!isStopFlag) {
				fos.flush();
				publishResult();
			}
		} catch (Exception e) {
			if(!isStopFlag) {
                publishException(e);
            } else {
                onCanceled();
            }
		} finally {
			try {
				if(fos != null) fos.close();
				if(is != null) is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	private void publishPreDownload() {
		CommonApp.getAppHandler().post(new Runnable() {
			
			@Override
			public void run() {
				onPreDownloadFile();
			}
		});
	}
	
	private void publishProgress(final int progress) {
		CommonApp.getAppHandler().post(new Runnable() {
			
			@Override
			public void run() {
				publishProgressOnMainThread(progress);
			}
		});
	}
	
	private void publishResult() {
		CommonApp.getAppHandler().post(new Runnable() {
			
			@Override
			public void run() {
				publishResultOnMainThread(saveToFile);
			}
		});
	}
	
	private void publishException(final Exception e) {
		CommonApp.getAppHandler().post(new Runnable() {
			
			@Override
			public void run() {
				publishExceptionOnMainThread(e);
			}
		});
	}

    private void publicCanceled() {
        CommonApp.getAppHandler().post(new Runnable() {

            @Override
            public void run() {
                onCanceled();
            }
        });
    }
	
	/**
	 * 停止下载
	 */
	public void stopDownload() {
		isStopFlag = true;
        if (mConnection != null) {
			mConnection.disconnect();
        }
	}
	
	/**
	 * 开始下载前，方法将在主线程中被调用
	 */
	protected void onPreDownloadFile() {
	}
	
	/**
	 * 进度反馈，方法将在主线程中被调用
	 * @param progress	进度
	 */
	protected void publishProgressOnMainThread(int progress) {
	}
	
	/**
	 * 下载成功反馈 ，方法将在主线程中被调用
	 * @param saveToFile 	保存文件
	 */
	protected void publishResultOnMainThread(File saveToFile) {
	}
	
	/**
	 * 下载发生异常，方法将在主线程中被调用 
	 * @param e
	 */
	protected void publishExceptionOnMainThread(Exception e) {
	}

    /**
     * 下载被取消
     */
    protected void onCanceled() {
    }
}
