package com.nd.shapedexamproj.activity.downloadmanage;

import android.content.*;
import android.content.res.Resources.NotFoundException;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.RemoteException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.*;

import com.nd.shapedexamproj.App;
import com.nd.shapedexamproj.R;
import com.nd.shapedexamproj.activity.BaseActivity;
import com.nd.shapedexamproj.db.VideoDownloadDBOperator;
import com.nd.shapedexamproj.model.download.DownloadInfo;
import com.nd.shapedexamproj.util.UIHelper;
import com.nd.shapedexamproj.util.Utils;
import com.tming.common.download.DownloadServiceUtil;
import com.tming.common.download.DownloadState;
import com.tming.common.download.IDownloadCallback;
import com.tming.common.download.IDownloadServiceApi;
import com.tming.common.util.Helper;
import com.tming.common.util.Log;

import java.io.File;
import java.util.*;
/**
 * <p>下载管理类</p>
 * <p>Created by Linlg</p>
 *
 * <p>修改没有数据用户提示，改为图片和文字提示</p>
 * <p>Modified by xuwenzhuo 2014/11/12</p>
 */
public class DownloadManagerActivity extends BaseActivity implements OnClickListener,Observer {
	
	private final static String TAG = DownloadManagerActivity.class.getSimpleName();
    private final static String SIGN = "@_@"; // 特殊字符分割课程名称和课程ID
    private final static int DOWNLOADING = 1; // 下载任务
	private final static int DOWNLOADED = 2; // 已完成任务
	private ListView taskLV;
	private List<Object> tasks = new ArrayList<Object>();
	private Map<String,DownloadingView> mConvertViewMap = new HashMap<String,DownloadingView>();
	private List<DownloadInfo> downloadInfos = null;
	private VideoDownloadDBOperator operator;
	private TaskAdapter adapter;
	private RelativeLayout popLayout;
	private LinearLayout menuLayout;
	private TextView menuPlayTv, menuDelTv, menuShareTv;
	// no_data_tv;
	//private Button no_data_btn;
    private View mNoDataView;
    private int screenHeight = 0;
    private boolean isFirstStart = true;
	private IDownloadServiceApi mServiceApi;
	private ServiceConnection mServiceConnection;
    
    
    private BroadcastReceiver netChangeBroadcastReceiver = new BroadcastReceiver() {
        
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
                ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(
                        Context.CONNECTIVITY_SERVICE);
                NetworkInfo info = connectivityManager.getActiveNetworkInfo();
                if (info != null && mServiceApi != null) {
                    //如果网络可用且为wifi链接，就自动下载
                    if (info.isAvailable() && info.getType() == ConnectivityManager.TYPE_WIFI) {
                        List<String> urlList;
                        try {
                            urlList = mServiceApi.getDownloadingUrlStrings();
                            for (String url : urlList) {
                                int status = mServiceApi.getDownloadState(url);
                                if (status == DownloadState.PAUSE) {
                                    Log.e(TAG, "==继续下载==");
                                    DownloadServiceUtil.resumeDownload(DownloadManagerActivity.this,url);
                                }
                            }
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                    } 
                }
            }
    }};
	
	@Override
	public int initResource() {
		return R.layout.download;
	}

	@Override
	public void initComponent() {
		operator = new VideoDownloadDBOperator(this);
		taskLV = (ListView) findViewById(R.id.download_task_lv);
		((TextView) findViewById(R.id.commonheader_title_tv)).setText(R.string.download_text);
		findViewById(R.id.commonheader_right_btn).setVisibility(View.GONE);
		menuLayout = (LinearLayout) findViewById(R.id.download_pop_ll);
		menuPlayTv = (TextView) findViewById(R.id.download_play_tv);
		menuDelTv = (TextView) findViewById(R.id.download_del_tv);

        //menuShareTv = (TextView) findViewById(R.id.download_share_tv);
		popLayout = (RelativeLayout) findViewById(R.id.download_pop_rl);
		popLayout.setOnClickListener(this);
		findViewById(R.id.commonheader_left_iv).setOnClickListener(this);
		screenHeight = getWindowManager().getDefaultDisplay().getHeight();

        mNoDataView=(View) findViewById(R.id.no_data_view);
        mNoDataView.setVisibility(View.GONE);
        //no_data_tv = (TextView) findViewById(R.id.no_data_tv);
        //no_data_btn = (Button) findViewById(R.id.no_data_btn);
		// 网络变化监听
	    IntentFilter netIntentFilter = new IntentFilter();
	    netIntentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
	    registerReceiver(netChangeBroadcastReceiver, netIntentFilter);
	    registerCallBack();
	}
	
	private void registerCallBack() {
		mServiceConnection = DownloadServiceUtil.registDownloadCallback(DownloadManagerActivity.this, new IDownloadCallback() {
            
            @Override
            public void downloadStateChange(String urlString, int status) {
                switch (status) {
                    case DownloadState.PAUSE:
                        Log.e(TAG, "===暂停===");
                        DownloadServiceUtil.pauseDownload(DownloadManagerActivity.this, urlString);
                        DownloadingView downloadingView = findConvertViewByUrl(urlString);
                        if (downloadingView != null) {
                            downloadingView.playBtn.setText(getResources().getString(R.string.downloadmanage_download_resume));
                        }
                        break;
                    default :
                        break;
                }
            }
            
            @Override
            public void downloadServiceDisconnected() {
                
            }
            
            @Override
            public void downloadServiceConnected(IDownloadServiceApi serviceApi) {
                mServiceApi = serviceApi;
                refreshData();
            }
            
            @Override
            public void downloadFail(String urlString, int errorCode, String errorMsg) {
            }

            @Override
            public void downloadFinish(String urlString, String downloadedFilePath) {
                Log.e(TAG, "downloadFinish, 刷新界面");
                tasks.clear();
                refreshData();
                adapter.notifyDataSetChanged();
                return;
            }

            @Override
            public void downloadUpdateProgress(String urlString, long downloadedSize, long contentLength) {
                DownloadingView downloadingView = findConvertViewByUrl(urlString);
                float progress = (float) downloadedSize / contentLength;
                if (downloadingView != null) {
                    Log.e(TAG, "downloadUpdateProgress progress:" + progress  + "  fileSize: " + contentLength);
                    String txt = Utils.FormetFileSize((int) (contentLength * progress)) + "/" + Utils.FormetFileSize(contentLength);
                    Log.e(TAG, "downloadUpdateProgress  txt: " + txt);
                    downloadingView.rateTv.setText(txt);
                    downloadingView.pb.setProgress((int) (progress * 100));
                    
                }
            }

        });
    }
	
	@Override
	protected void onResume() {
	    super.onResume();
	    if (!isFirstStart) {
	        tasks.clear();
	        refreshData();
            adapter.notifyDataSetChanged();
	    }
	    isFirstStart = false;
	}
	@Override
	protected void onDestroy() {
	    if (mServiceApi != null) {
	        mServiceApi.destroy();
	    }
		if (mServiceConnection != null) {
			unbindService(mServiceConnection);
		}
	    unregisterReceiver(netChangeBroadcastReceiver);
		super.onDestroy();
	}
	@Override
	public void initData() {
	    
	}
	/**
	 * <p>刷新列表</P>
	 */
	private void refreshData() {
        downloadInfos = operator.getDownloads();
        
        if (downloadInfos.size() == 0) {

            /*
            if (AuthorityManager.getInstance().isStudentAuthority()) {
                no_data_tv.setText(getResources().getString(R.string.download_no_data));
                no_data_btn.setVisibility(View.VISIBLE);
            } else if (AuthorityManager.getInstance().isTeacherAuthority()) {
                no_data_tv.setText(getResources().getString(R.string.download_no_data_teacher));
                no_data_btn.setVisibility(View.GONE);
            }
            */
            mNoDataView.setVisibility(View.VISIBLE);
            return;
        }
        List<DownloadInfo> downloadedList = new ArrayList<DownloadInfo>();
        List<DownloadInfo> downloadingList = new ArrayList<DownloadInfo>();
        //调用服务获取下载状态
        for (DownloadInfo downloadInfo : downloadInfos) {
            int downloadStatus;
            try {
                downloadStatus = mServiceApi.getDownloadState(downloadInfo.url);
                switch(downloadStatus) {
                    case DownloadState.UNKNOWN:
                        Log.e("delete", "用户"+ App.getUserId() + " 要删除：" + downloadInfo.title);
                        operator.deleteByUrl(downloadInfo.url);
                        downloadInfos = operator.getDownloads();
                        break;
                    case DownloadState.DOWNLOADED:
                        downloadedList.add(downloadInfo);
                        
                        break;
                    case DownloadState.DOWNLOADING:
                        downloadingList.add(downloadInfo);
                        
                        break;
                    case DownloadState.PAUSE:
                        downloadingList.add(downloadInfo);
                        break;
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        
        // 下载任务
        if (downloadingList.size() != 0) {
            Set<Integer> downloadingSet = new HashSet<Integer>();
            for (int i = 0;i < downloadingList.size();i ++) {
                downloadingSet.add(downloadingList.get(i).coursecateid);
            }
            
            tasks.add(DOWNLOADING); // Integer
            Iterator<Integer> it = downloadingSet.iterator();
            while (it.hasNext()) {
                int index = it.next();
                boolean flag = true;
                for (int i = 0; i < downloadingList.size(); i++) {
                    DownloadInfo downloadInfo = downloadingList.get(i);
                    if (flag && downloadInfo.coursecateid == index) {
                        tasks.add(downloadInfo.coursecatename + SIGN + downloadInfo.courseId); // String
                        flag = false;
                    }
                    if (downloadInfo.coursecateid == index) {
                        tasks.add(downloadInfo); // DownloadInfo
                    }
                }
            }
        }
        // 已完成任务
        if (downloadedList.size() != 0) {
            Set<Integer> downloadedSet = new HashSet<Integer>();
            for (int i = 0; i < downloadedList.size(); i++) {
                downloadedSet.add(downloadedList.get(i).coursecateid);
            }
            tasks.add(DOWNLOADED); // Integer
            Iterator<Integer> it = downloadedSet.iterator();
            while (it.hasNext()) {
                int index = it.next();
                boolean flag = true;
                for (int i = 0; i < downloadedList.size(); i++) {
                    DownloadInfo downloadInfo = downloadedList.get(i);
                    if (flag && downloadInfo.coursecateid == index) {
                        tasks.add(downloadInfo.coursecatename + SIGN + downloadInfo.courseId); // String
                        flag = false;
                    }
                    if (downloadInfo.coursecateid == index) {
                        tasks.add(downloadInfo); // DownloadInfo
                    }
                }
            }
        }
        
        adapter = new TaskAdapter();
        taskLV.setAdapter(adapter);
	}
	
	@Override
	public void addListener() {
	    /*
        no_data_btn.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                Intent intent2 = new Intent();
                intent2.setClass(App.getAppContext(), LocalCoursewareActivity.class);
                startActivity(intent2);
                finish();
            }
        });
        */
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.commonheader_left_iv:
			finish();
			break;
		case R.id.download_pop_rl:
			if (popLayout.isShown()) {
				popLayout.setVisibility(View.GONE);
			}
			break;
		}
	}
	
	private class TaskAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return tasks.size();
		}

		@Override
		public Object getItem(int position) {
			return null;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@SuppressWarnings("deprecation")
        @Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			Object obj = tasks.get(position);
			if (obj == null) {
				return convertView;
			}
			
			if (obj instanceof Integer) { // 下载任务/已完成任务
				convertView = LayoutInflater.from(DownloadManagerActivity.this).inflate(R.layout.downloading_cate, null);
				TextView tv = (TextView) convertView.findViewById(R.id.downloadingitemcate_tv);
				if ((Integer) obj == DOWNLOADING) {
					tv.setText("正在下载");
				} else {
					tv.setText("已下载");
				}
			} else if (obj instanceof String) {
				final String item = (String) obj;
				convertView = LayoutInflater.from(DownloadManagerActivity.this).inflate(R.layout.downloading_title, null);
				TextView tv = (TextView) (TextView) convertView.findViewById(R.id.downloadingitemtitle_tv);
				tv.setText(item.split(SIGN)[0]);
				convertView.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View v) {
						UIHelper.showCourseDetail(DownloadManagerActivity.this, item.split(SIGN)[1], item.split(SIGN)[0]);
					}
				});
			} else if (obj instanceof DownloadInfo) {
			    final DownloadInfo downloadInfo = (DownloadInfo) obj;
			    int status = DownloadState.UNKNOWN;
                try {
                    status = mServiceApi.getDownloadState(downloadInfo.url);
                } catch (RemoteException e1) {
                    e1.printStackTrace();
                }
			    if (status == DownloadState.DOWNLOADING || status == DownloadState.PAUSE) {
			        final DownloadingView downloadingView = new DownloadingView();
			        convertView = LayoutInflater.from(DownloadManagerActivity.this).inflate(R.layout.downloading_item, null);
			        downloadingView.itemLayout = (LinearLayout) convertView.findViewById(R.id.downloadingitem_ll);
			        downloadingView.cancelBtn = (Button) convertView.findViewById(R.id.downloadingitem_cancel_btn);
			        downloadingView.pb = (ProgressBar) convertView.findViewById(R.id.downloadingitem_download_pb);
			        downloadingView.rateTv = (TextView) convertView.findViewById(R.id.downloadingitem_progress_tv);
			        downloadingView.playBtn = (Button) convertView.findViewById(R.id.downloadingitem_pause_btn);
	                downloadingView.titleTv = (TextView) convertView.findViewById(R.id.downloadingitem_title_tv);
	                downloadingView.titleTv.setText(downloadInfo.title);
	                
	                long fileSize = mServiceApi.getDownloadContentLength(downloadInfo.url);
	                long downloadedSize = mServiceApi.getDownloadedSize(downloadInfo.url);
	                float progress = (float) downloadedSize / fileSize;
	                
	                downloadingView.pb.setProgress((int)(progress * 100));
	                String txt = Utils.FormetFileSize((int) (fileSize * progress)) + "/" + Utils.FormetFileSize(fileSize);
	                downloadingView.rateTv.setText(txt);
	                switch (status) {
	                case DownloadState.DOWNLOADING:
	                    downloadingView.playBtn.setText(getResources().getString(R.string.downloadmanage_download_pause));
	                    break;
	                case DownloadState.PAUSE:
	                    downloadingView.playBtn.setText(getResources().getString(R.string.downloadmanage_download_resume));
	                    break;
	                }
	                /*videoDownloader.addObserver(DownloadManagerActivity.this);//add by zll at 20141009;添加观察者模式*/
	                downloadingView.playBtn.setOnClickListener(new OnClickListener() {
	                    
	                    @Override
	                    public void onClick(View v) {
	                        try {
                                if (mServiceApi.getDownloadState(downloadInfo.url) == DownloadState.PAUSE) {
                                    downloadingView.playBtn.setText(getResources().getString(R.string.downloadmanage_download_pause));
                                    DownloadServiceUtil.resumeDownload(DownloadManagerActivity.this, downloadInfo.url);
                                } else {
                                    downloadingView.playBtn.setText(getResources().getString(R.string.downloadmanage_download_resume));
                                    DownloadServiceUtil.pauseDownload(DownloadManagerActivity.this, downloadInfo.url);
                                }
                            } catch (RemoteException e) {
                                e.printStackTrace();
                            } catch (NotFoundException e) {
                                e.printStackTrace();
                            }
	                    }
	                });
	                downloadingView.itemLayout.setOnClickListener(new OnClickListener() {

	                    @Override
	                    public void onClick(View v) {
	                        if (downloadingView.cancelBtn.isShown()) {
	                            downloadingView.cancelBtn.setVisibility(View.GONE);
	                            return;
	                        }
	                        downloadingView.cancelBtn.setVisibility(View.VISIBLE);
	                    }
	                });
	                downloadingView.cancelBtn.setOnClickListener(new OnClickListener() {
	                    
	                    @Override
	                    public void onClick(View v) {
	                        //App.videoDownloaders.remove(url);
	                        //videoDownloader.cancel();
	                        DownloadServiceUtil.cancelDownload(DownloadManagerActivity.this, downloadInfo.url);
	                        if (operator.isUrlExist(downloadInfo.url)) {
	                            operator.deleteByUrl(downloadInfo.url);
	                        }
	                        tasks.clear();
	                        refreshData();
	                        adapter.notifyDataSetChanged();
	                        
	                    }
	                });
	                mConvertViewMap.put(downloadInfo.url, downloadingView);
	                
			    } else {
			    
    				convertView = LayoutInflater.from(DownloadManagerActivity.this).inflate(R.layout.download_finish, null);
    				//final DownloadInfo downloadInfo = (DownloadInfo) obj;
    				TextView tv = (TextView) convertView.findViewById(R.id.downloadfinish_title_tv);
    				final LinearLayout pop = (LinearLayout) convertView.findViewById(R.id.download_finish_menu_ll);
    				tv.setText(downloadInfo.title);
    				convertView.setOnClickListener(new OnClickListener() {
    					
    					@Override
    					public void onClick(View v) {
    						if (popLayout.isShown()) {
    							popLayout.setVisibility(View.GONE);
    							return;
    						}
    						
    						menuPlayTv.setOnClickListener(new MenuOnclickListener(position));
    						menuDelTv.setOnClickListener(new MenuOnclickListener(position));
    						int location[] = new int[2];
    						pop.getLocationInWindow(location);
    						RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(menuLayout.getLayoutParams());
    						Log.e("DownloadManagerActivity:", "" + location[1] + "_" + screenHeight);
    						if (location[1] + Helper.dip2px(DownloadManagerActivity.this, 90) >= screenHeight) {
    							params.topMargin = location[1] - Helper.dip2px(DownloadManagerActivity.this, 100);
    						} else {
    							params.topMargin = location[1];
    						}
    						params.rightMargin = Helper.dip2px(DownloadManagerActivity.this, 10);
    						params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
    						menuLayout.setLayoutParams(params);
    						popLayout.setVisibility(View.VISIBLE);
    					}
    				});
			    }
			}
			return convertView;
		}
		
	}
	
	private class MenuOnclickListener implements OnClickListener {
		private int postion;

		public MenuOnclickListener(int position) {
			this.postion = position;
		}

		@Override
		public void onClick(View v) {
			popLayout.setVisibility(View.GONE);
			switch (v.getId()) {
			case R.id.download_play_tv:
				DownloadInfo downloadInfo = (DownloadInfo) tasks.get(postion);
				UIHelper.showCourseDetail(DownloadManagerActivity.this,downloadInfo);
				break;
			case R.id.download_del_tv:
				operator.deleteByUrl(((DownloadInfo) tasks.get(postion)).url);
				try {
    				File file = new File(mServiceApi.getDownloadedFile(((DownloadInfo) tasks.get(postion)).url).getAbsolutePath());
    				file.delete();
				} catch(Exception e) {
				    e.printStackTrace();
				}
				App.getAppHandler().post(new Runnable() {

					@Override
					public void run() {
						tasks.clear();
						refreshData();
						adapter.notifyDataSetChanged();
					}
				});
				break;
			}
		}

	}
	
	private class DownloadingView{
	    LinearLayout itemLayout ;
        Button cancelBtn ;
        ProgressBar pb ;
        TextView rateTv,titleTv;
        Button playBtn ;
	}
	
	private DownloadingView findConvertViewByUrl(String targetUrl) {
	    return mConvertViewMap.get(targetUrl);
	}
	
    @Override
    public void update(Observable observable, Object data) {
        App.getAppHandler().post(new Runnable() {

            @Override
            public void run() {
                adapter.notifyDataSetChanged();
            }
            
        });
        
    }
	
	
}
