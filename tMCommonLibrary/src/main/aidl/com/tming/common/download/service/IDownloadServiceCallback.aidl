package com.tming.common.download.service;

interface IDownloadServiceCallback {

	void downloadUpdateProgress(String urlString, long downloadedSize, long contentLength);
	void downloadFinish(String urlString, String downloadedFilePath);
	void downloadFail(String urlString, int errorCode, String errorMsg);
	void downloadStateChange(String urlString, int state);

}