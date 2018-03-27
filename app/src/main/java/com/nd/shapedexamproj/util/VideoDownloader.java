package com.nd.shapedexamproj.util;

import android.widget.Toast;

import com.nd.shapedexamproj.App;
import com.nd.shapedexamproj.model.download.DownloadInfo;
import com.tming.common.util.Log;

import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Observable;

/**
 * 下载
 * 
 * @author Linlg
 * @deprecated 
 * @see {@link com.tming.openuniversity.util.service.FileDownloadService FileDownloadService}
 * 
 */
public class VideoDownloader extends Observable implements Runnable {

    private static final String TAG = VideoDownloader.class.getSimpleName();
    // Max size of download buffer.
    private static final int MAX_BUFFER_SIZE = 1024 * 8;
    // These are the status names.
    public static final String STATUSES[] = { "Downloading", "Paused", "Complete", "Cancelled", "Error" };
    // These are the status codes.
    public static final int DOWNLOADING = 0;
    public static final int PAUSED = 1;
    public static final int COMPLETE = 2;
    public static final int CANCELLED = 3;
    public static final int ERROR = 4;
    // private URL url; // download URL
    private int size; // size of download in bytes
    private int downloaded; // number of bytes downloaded
    private int status; // current status of download
    private int progress;
    // add by linlg for openuniversity project
    private DownloadInfo downloadInfo = new DownloadInfo();

    // Constructor for Download.
    public VideoDownloader(DownloadInfo info) {
        this.downloadInfo = info;
        size = -1;
        downloaded = 0;
        status = DOWNLOADING;
        Log.e("VideoDownloader", TAG + "_" + info.url);
        // Begin the download.
        download();
    }

    // add by linlg for openuniversity project
    public int getCateId() {
        return downloadInfo.coursecateid;
    }

    public String getCateName() {
        return downloadInfo.coursecatename;
    }

    public String getVideoName() {
        return downloadInfo.title;
    }

    public String getCourseId() {
        return downloadInfo.courseId;
    }

    public DownloadInfo getDownloadInfo() {
        return downloadInfo;
    }

    // Get this download's URL.
    public String getUrl() {
        return downloadInfo.url;
    }

    // Get this download's size.
    public int getSize() {
        return size;
    }

    // Get this download's progress.
    public int getProgress() {
        return (int) (((float) downloaded / size) * 100);
    }

    // Get this download's status.
    public int getStatus() {
        return status;
    }

    // Pause this download.
    public void pause() {
        status = PAUSED;
        stateChanged();
    }

    // Resume this download.
    public void resume() {
        status = DOWNLOADING;
        stateChanged();
        download();
    }

    // Cancel this download.
    public void cancel() {
        status = CANCELLED;
        stateChanged();
    }

    // Mark this download as having an error.
    private void error() {
        App.getAppHandler().post(new Runnable() {

            @Override
            public void run() {
                
                Toast.makeText(App.getAppContext(),  "下载发生错误", Toast.LENGTH_SHORT).show();
            }
        });
        /*App.videoDownloaders.remove(getUrl());*///note by zll at 2014/10/09 在出现异常的时候，暂停下载
        /*status = ERROR;*/
        
        //App.videoDownloaders.get(getUrl()).pause();
        status = PAUSED;
        stateChanged();
    }

    // no space for downloading
    private void noSpace() {
        App.getAppHandler().post(new Runnable() {

            @Override
            public void run() {
                Toast.makeText(App.getAppContext(), "存储空间不足", Toast.LENGTH_SHORT).show();
            }
        });
        /*App.videoDownloaders.remove(getUrl());*///note by zll at 2014/10/09 在出现异常的时候，暂停下载
        /*status = ERROR;*/
        
        //App.videoDownloaders.get(getUrl()).pause();
        status = PAUSED;
        stateChanged();
    }

    // Start or resume downloading.
    private void download() {
        Thread thread = new Thread(this);
        thread.start();
    }
    
    public interface DownloadListener {

        public void download(final int progress, final long size);
    }

    private DownloadListener mDownloadListener;

    public void setDownloadListener(DownloadListener downloadListener) {
        mDownloadListener = downloadListener;
    }

    // Download file.
    public void run() {
        RandomAccessFile file = null;
        InputStream stream = null;
        progress = 0;
        SDCardInfo sdCardInfo = SDCardInfo.getSDCardInfo();
        try {
            URL url = new URL(downloadInfo.url);
            // Open connection to URL.
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            // Specify what portion of file to download.
            connection.setRequestProperty("Range", "bytes=" + downloaded + "-");
            // Connect to server.
            connection.connect();
            // Make sure response code is in the 200 range.
            if (connection.getResponseCode() / 100 != 2) {
                Log.e(TAG, "ResponseCode ！= 200");
                error();
            }
            // Check for valid content length.
            int contentLength = connection.getContentLength();
            if (contentLength < 1) {
                Log.e(TAG, "contentLength < 1");
                error();
            }
            
            if (contentLength >= sdCardInfo.free) {
                noSpace();
            }
            /*
             * Set the size for this download if it hasn't been already set.
             */
            if (size == -1) {
                size = contentLength;
                stateChanged();
            }
            // Open file and seek to the end of it.
            // file = new RandomAccessFile(App.getDownload_path() + "test.mp4",
            // "rw");
            file = new RandomAccessFile(Utils.convertUrlToPath(url.toString()), "rw");
            file.seek(downloaded);
            stream = connection.getInputStream();
            while (status == DOWNLOADING) {
                /*
                 * Size buffer according to how much of the file is left to
                 * download.
                 */
                byte buffer[];
                if (size - downloaded > MAX_BUFFER_SIZE) {
                    buffer = new byte[MAX_BUFFER_SIZE];
                } else {
                    buffer = new byte[size - downloaded];
                }
                // Read from server into buffer.
                int read = stream.read(buffer);
                if (read == -1)
                    break;
                // Write buffer to file.
                file.write(buffer, 0, read);
                downloaded += read;
                // stateChanged();
                int tmpProgress = (int) (((float) downloaded / size) * 100);
                Log.e(TAG, "tmpProgress = " + tmpProgress);
                if (tmpProgress > progress && mDownloadListener != null) {
                    progress = tmpProgress;
                    if (mDownloadListener != null)
                        mDownloadListener.download(progress, size);
                }
            }
            /*
             * Change status to complete if this point was reached because
             * downloading has finished.
             */
            if (status == DOWNLOADING) {
                status = COMPLETE;
                //App.videoDownloaders.remove(downloadInfo.url);
                stateChanged();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "=======下载异常捕获=======");
            error();
        } finally {
            // Close file.
            if (file != null) {
                try {
                    file.close();
                } catch (Exception e) {}
            }
            // Close connection to server.
            if (stream != null) {
                try {
                    stream.close();
                } catch (Exception e) {}
            }
        }
    }

    // Notify observers that this download's status has changed.
    private void stateChanged() {
        setChanged();
        notifyObservers();
    }
}
