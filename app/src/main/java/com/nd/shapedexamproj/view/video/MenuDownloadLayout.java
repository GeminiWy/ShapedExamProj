package com.nd.shapedexamproj.view.video;

import android.content.Context;
import android.content.ServiceConnection;
import android.os.Environment;
import android.os.Handler;
import android.os.RemoteException;
import android.text.format.Formatter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.*;

import com.nd.shapedexamproj.App;
import com.nd.shapedexamproj.R;
import com.nd.shapedexamproj.db.VideoDownloadDBOperator;
import com.nd.shapedexamproj.model.download.DownloadInfo;
import com.nd.shapedexamproj.util.SDCardInfo;
import com.nd.shapedexamproj.util.Utils;
import com.tming.common.download.DownloadServiceUtil;
import com.tming.common.download.DownloadState;
import com.tming.common.download.IDownloadCallback;
import com.tming.common.download.IDownloadServiceApi;
import com.tming.common.util.Helper;
import com.tming.common.util.Log;
import com.tming.common.util.PhoneUtil;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * 视频菜单-下载
 * 
 * @author Linlg
 * 
 */
public class MenuDownloadLayout extends RelativeLayout implements OnClickListener {

    private View downloadView;
    private ProgressBar downloadPb, storePb;
    private TextView storeTv, downloadRateTv, downloadedTv, sizeTv;
    private Context mContext;
    private IDownloadServiceApi mServiceApi;
    private ServiceConnection mServiceConnection;
    private DownloadInfo downloadInfo;
    private Handler mhHandler = new Handler();
    private VideoDownloadDBOperator operator;
    private Button downloadBtn, cancelBtn;
    private RelativeLayout downloadLayout;
    private LinearLayout storeLayout;
    private int fileSize = 0;
    SDCardInfo sdCardInfo = null;
    private Handler handler = new Handler();

    public MenuDownloadLayout(Context context, DownloadInfo info) {
        super(context);
        this.mContext = context;
        this.downloadInfo = info;
        operator = new VideoDownloadDBOperator(context);
        initView();
        thread.start();
    }

    private void initView() {
        downloadView = LayoutInflater.from(mContext).inflate(R.layout.videoview_menu_download, this);
        downloadPb = (ProgressBar) downloadView.findViewById(R.id.videoviewmenudownload_download_pb);
        downloadRateTv = (TextView) downloadView.findViewById(R.id.videoviewmenudownload_downloadrate_tv);
        downloadLayout = (RelativeLayout) downloadView.findViewById(R.id.videoviewmenudownload_download_rl);
        storeLayout = (LinearLayout) downloadView.findViewById(R.id.videoviewmenudownload_store_ll);
        storePb = (ProgressBar) downloadView.findViewById(R.id.videoviewmenudownload_store_pb);
        storeTv = (TextView) downloadView.findViewById(R.id.videoviewmenudownload_store_tv);
        downloadedTv = (TextView) downloadView.findViewById(R.id.videoview_menu_downloaded_tv);
        sizeTv = (TextView) downloadView.findViewById(R.id.videoviewmenudownload_size_tv);
        downloadBtn = (Button) downloadView.findViewById(R.id.videoviewmenudownload_download_btn);
        cancelBtn = (Button) downloadView.findViewById(R.id.videoviewmenudownload_canceldownload_btn);
        downloadBtn.setOnClickListener(this);
        cancelBtn.setOnClickListener(this);
        sdCardInfo = SDCardInfo.getSDCardInfo();
        setStoreInfo();
        
    }
    
    private void registerDownloadListener() {
        mServiceConnection = DownloadServiceUtil.registDownloadCallback(mContext,new IDownloadCallback() {

            @Override
            public void downloadServiceConnected(IDownloadServiceApi serviceApi) {//初始状态
                mServiceApi = serviceApi;
                downloadBtn.setVisibility(View.VISIBLE);
                if (operator.isUrlExist(downloadInfo.url)) {
                    int status;
                    try {
                        status = mServiceApi.getDownloadState(downloadInfo.url);
                        if (status == DownloadState.DOWNLOADED) {
                            cancelBtn.setVisibility(View.GONE);
                            downloadLayout.setVisibility(View.GONE);
                            storeLayout.setVisibility(View.GONE);
                            downloadBtn.setVisibility(View.GONE);
                            downloadedTv.setVisibility(View.VISIBLE);
                            
                        } else if (status == DownloadState.DOWNLOADING || status == DownloadState.PAUSE) {
                            cancelBtn.setVisibility(View.VISIBLE);
                            downloadLayout.setVisibility(View.VISIBLE);
                            storeLayout.setVisibility(View.GONE);
                            downloadBtn.setVisibility(View.VISIBLE);
                            downloadedTv.setVisibility(View.GONE);
                            if (status == DownloadState.DOWNLOADING) {
                                downloadBtn.setText(getResources().getString(R.string.video_menu_download_pause));
                            } else {
                                downloadBtn.setText(R.string.video_menu_download_resume);
                            }
                            long downloadedSize = mServiceApi.getDownloadedSize(downloadInfo.url);
                            long contentLength = mServiceApi.getDownloadContentLength(downloadInfo.url);
                            
                            float progress = (float) downloadedSize / contentLength;
                            String txt = Utils.FormetFileSize((int) (contentLength * progress)) + "/" + Utils.FormetFileSize(contentLength);
                            downloadPb.setProgress((int) (progress * 100));
                            downloadRateTv.setText(txt);
                        } 
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
                
            }

            @Override
            public void downloadServiceDisconnected() {
            }
            
            @Override
            public void downloadFail(String urlString, int errorCode, String errorMsg) {
                
            }

            @Override
            public void downloadStateChange(String urlString, int state) {
            }

            @Override
            public void downloadFinish(String urlString, String downloadedFilePath) {
                if (operator.isUrlExist(downloadInfo.url)) {
                    cancelBtn.setVisibility(View.GONE);
                    downloadLayout.setVisibility(View.GONE);
                    storeLayout.setVisibility(View.GONE);
                    downloadBtn.setVisibility(View.GONE);
                    downloadedTv.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void downloadUpdateProgress(String urlString, long downloadedSize, long contentLength) {
                Log.e("MenuDownloadLayout", "=====downloadUpdateProgress====");
                float progress = (float) downloadedSize / contentLength;
                String txt = Utils.FormetFileSize((int) (contentLength * progress)) + "/" + Utils.FormetFileSize(contentLength);
                downloadPb.setProgress((int) (progress * 100));
                downloadRateTv.setText(txt);
            }
        });
    }
    
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.videoviewmenudownload_download_btn:
                if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                    Toast.makeText(mContext, getResources().getString(R.string.download_no_sdcard), Toast.LENGTH_SHORT)
                            .show();
                    return;
                }
                if (sdCardInfo.free < fileSize) {
                    Toast.makeText(mContext, getResources().getString(R.string.download_memory_shortage), Toast.LENGTH_SHORT).show();
                    return;
                }
                if (PhoneUtil.checkNetworkEnable() != PhoneUtil.NETSTATE_ENABLE) {
                    Helper.ToastUtil(getContext(), getResources().getString(R.string.net_error_tip));
                    return;
                }
                cancelBtn.setVisibility(View.VISIBLE);
                storeLayout.setVisibility(View.GONE);
                downloadLayout.setVisibility(View.VISIBLE);
                
                int status = DownloadState.UNKNOWN;
                try {
                    status = mServiceApi.getDownloadState(downloadInfo.url);
                } catch (RemoteException e1) {
                    e1.printStackTrace();
                }
                if (status == DownloadState.DOWNLOADING ) {
                    ((Button) v).setText(getResources().getString(R.string.video_menu_download_resume));
                    DownloadServiceUtil.pauseDownload(mContext,downloadInfo.url);
                
                } else if (status == DownloadState.PAUSE) {
                    ((Button) v).setText(getResources().getString(R.string.video_menu_download_pause));
                    DownloadServiceUtil.resumeDownload(mContext,downloadInfo.url);
                    
                } else if (status == DownloadState.UNKNOWN) {//未下载
                    ((Button) v).setText(getResources().getString(R.string.video_menu_download_pause));
                    
                    DownloadServiceUtil.startDownload(mContext,downloadInfo.url);
                    if (!operator.isUrlExist(downloadInfo.url)) {
                        operator.insert(downloadInfo);
                    }
                }
                
                break;
            case R.id.videoviewmenudownload_canceldownload_btn:
                DownloadServiceUtil.cancelDownload(mContext,downloadInfo.url);
                downloadBtn.setText(getResources().getString(R.string.video_menu_download_ok));
                cancelBtn.setVisibility(View.GONE);
                downloadLayout.setVisibility(View.GONE);
                storeLayout.setVisibility(View.VISIBLE);
                break;
        }
    }

    private Thread thread = new Thread() {

        public void run() {
            super.run();
            fileSize = getContentLength();
            handler.post(new Runnable() {

                @Override
                public void run() {
                    sizeTv.setText(Utils.FormetFileSize(fileSize));
                }
            });
        };
    };

    private void setStoreInfo() {
        // long totalMemory = PhoneUtil.getTotalMemory(mContext);
        // long availMemory = PhoneUtil.getAvailMemory(mContext);
        // SDCardInfo sdCardInfo = SDCardInfo.getSDCardInfo();
        if (sdCardInfo == null) {
            storeTv.setText(getResources().getString(R.string.settings_no_sd_card));
            storePb.setProgress(0);
            downloadBtn.setVisibility(View.GONE);
            return;
        }
        storeTv.setText("已用" + Formatter.formatFileSize(mContext, /*
                                                                   * totalMemory
                                                                   * -
                                                                   * availMemory
                                                                   * +
                                                                   */sdCardInfo.total - sdCardInfo.free) + "/剩余"
                + Formatter.formatFileSize(mContext, /* availMemory + */sdCardInfo.free));
        // storePb.setProgress((int) (100 * (totalMemory - availMemory) /
        // totalMemory));
        storePb.setProgress((int) (100 * (sdCardInfo.total - sdCardInfo.free) / sdCardInfo.total));
    }

    private int getContentLength() {
        int contentLength = 0;
        try {
            URL url = new URL(downloadInfo.url);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            contentLength = conn.getContentLength();
            conn.disconnect();
        } catch (MalformedURLException e) {
            App.getAppHandler().post(new Runnable() {
                
                @Override
                public void run() {
                    Toast.makeText(mContext, getResources().getString(R.string.download_useless_url), Toast.LENGTH_SHORT).show();
                }
            });
            
            e.printStackTrace();
        } catch (IOException e) {
            App.getAppHandler().post(new Runnable() {
                
                @Override
                public void run() {
                    Toast.makeText(mContext, getResources().getString(R.string.download_useless_url), Toast.LENGTH_SHORT).show();
                }
            });
            e.printStackTrace();
        }
        return contentLength;
    }
    @Override
    protected void onAttachedToWindow() {
        registerDownloadListener();
        super.onAttachedToWindow();
    }
    
    @Override
    protected void onDetachedFromWindow() {
        Log.e("MenuDownloadLayout", "===onDetachedFromWindow===");
        if (mServiceApi != null) {
            mServiceApi.destroy();
        }
        try {
            if (mServiceConnection != null) {
                mContext.unbindService(mServiceConnection);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onDetachedFromWindow();
    }
    
}
