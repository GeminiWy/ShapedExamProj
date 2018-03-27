package com.tming.common.download.service;
import com.tming.common.download.service.IDownloadServiceCallback;

interface IDownloadService {

	int getDownloadState(String urlString);
	void registDownloadCallback(IDownloadServiceCallback callback);
	void unregistDownloadCallback(IDownloadServiceCallback callback);
	List<String> getDownloadingUrlStrings();

}