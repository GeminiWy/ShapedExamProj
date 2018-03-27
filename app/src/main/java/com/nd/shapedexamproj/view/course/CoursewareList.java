package com.nd.shapedexamproj.view.course;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.RemoteException;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.*;

import com.nd.shapedexamproj.App;
import com.nd.shapedexamproj.AuthorityManager;
import com.nd.shapedexamproj.R;
import com.nd.shapedexamproj.activity.course.CourseDetailActivity;
import com.nd.shapedexamproj.adapter.CoursewareListAdapter;
import com.nd.shapedexamproj.db.VideoDownloadDBOperator;
import com.nd.shapedexamproj.model.Video;
import com.nd.shapedexamproj.util.Constants;
import com.nd.shapedexamproj.util.SDCardInfo;
import com.tming.common.cache.net.TmingCacheHttp;
import com.tming.common.download.DownloadServiceUtil;
import com.tming.common.download.DownloadState;
import com.tming.common.download.IDownloadCallback;
import com.tming.common.download.IDownloadServiceApi;
import com.tming.common.util.Helper;
import com.tming.common.util.Log;
import com.tming.common.util.PhoneUtil;
import com.tming.common.view.RefreshableListView;
import com.tming.common.view.RefreshableListView.OnRefreshListener;
import com.tming.common.view.support.pulltorefresh.PullToRefreshBase;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
/**
 * 课程详情页--课件列表
 * @author zll
 * create in 2014-3-6
 */
public class CoursewareList extends RelativeLayout{
	
	private final  int DOWNLOADING = 1;
	private final int DOWNLOADED = 0;
	private final int NOT_DOWNLOADED = 2;
	private final int DOWNLOAD_WAITING = 3;
	
	private final  int PAUSE = 2;
	private final  int START = 0;
	private final  int TIME_OUT = 10;
	private final  int FILE_NOT_FOUND = 11;
	
	private static final String TAG = "CoursewareList";
	
	private Context context ;
	private RefreshableListView listView ;
	private LinearLayout loading_layout;
	private RelativeLayout error_layout;
	private Button error_btn;
	private TextView no_data_tv;
	
	private LayoutInflater inflater;
	private View courseware_view,mListHeadView,mListFootView;
//	private List<Section> sectionList; 
	private Button btn_download ,btn_share ,btn_delete;
	private CoursewareListAdapter adapter ;
	private PopupWindow pop ;
	private View pop_layout = null;
	
	private int screen_height;	//屏幕高度
	
	private int pageNum = 1,pageSize = 30;
	private int itemHeight = 67;  //列表项的高度
	
	private String courseid = "";	//课程id
	private String course_name = "" ;
	private TmingCacheHttp cacheHttp ;
	private VideoDownloadDBOperator operator;
	private SDCardInfo sdCardInfo = null;
	private List<Video> videos = new ArrayList<Video>();
	private boolean focusable;//是否要滑动到正在播放的那一行
	
	private IDownloadServiceApi mServiceApi;
	private ServiceConnection mServiceConnection;
	
	public CoursewareList(Context context,String courseid,String course_name){
		super(context);
		this.context = context;
		this.courseid = courseid;
		this.course_name = course_name;
		initComponent();
		addListener();
		
	}
	
	/**
	 * 请求网络数据
	 */
	private void requestData(){
		String url = Constants.COURSEWARE_LIST ;
		Map<String ,Object> map = new HashMap<String,Object>();
		map.put("courseid", courseid);
		map.put("pageNum", pageNum);
		map.put("pageSize", pageSize);
		map.put("userid", App.getUserId());//App.getUserId()

        cacheHttp = TmingCacheHttp.getInstance();
        cacheHttp.asyncRequestWithCache(url, map, new TmingCacheHttp.RequestWithCacheCallBackV2<List<Video>>() {
            @Override
            public List<Video> parseData(String data) throws Exception {
                return jsonParsing(data);
            }

            @Override
            public void cacheDataRespone(List<Video> data) {
                loadData(data);
                listView.onRefreshComplete();
                updateCenterPlayButtonVisible(data == null || data.size() == 0);
            }

            @Override
            public void requestNewDataRespone(List<Video> cacheRespones, List<Video> newRespones) {
                loading_layout.setVisibility(GONE);
                error_layout.setVisibility(GONE);

                if (newRespones.size() == 0 && adapter.getCount() == 0) {
                    no_data_tv.setText(getResources().getString(R.string.no_courseware));
                } else {
                    no_data_tv.setText("");
                }
                
                try {
                    checkDownloadState(newRespones);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                adapter.replaceItem(cacheRespones, newRespones);
                adapter.notifyDataSetChanged();
                listView.onRefreshComplete();
                updateCenterPlayButtonVisible(newRespones == null || newRespones.size() == 0);
            }

            @Override
            public void exception(Exception exception) {
                loading_layout.setVisibility(View.GONE);
                error_layout.setVisibility(VISIBLE);
            }

            @Override
            public void onFinishRequest() {
                if (focusable) {
                    focusable = false;
                    for (int i = 0;i < adapter.getCount();i ++) {
                        Video video = (Video) adapter.getItem(i);
                        if (video.video_id.equals(adapter.getmCurrentPlayVideoId())) {
                            Log.e(TAG, "当前位置1：" + i);
                            adapter.setmCurrentPlayPosition(i);
                            break;
                        }
                    }
                    Log.e(TAG, "当前位置2：" + adapter.getmCurrentPlayPosition());
                    listView.getRefreshableView().setSelectionFromTop(adapter.getmCurrentPlayPosition(),0);
                }
            }
        });
	}

    /**
     * 发送通知“没有课程”
     */
    public void updateCenterPlayButtonVisible(boolean isHide) {
        Intent intent = new Intent("com.tming.openuniversity.updatePlayButtonVisible");
        intent.putExtra("isHide", isHide);
        getContext().sendBroadcast(intent);
    }

	/**
	 * 解析数据
	 */
	private List<Video> jsonParsing(String result){
		List<Video> vedio_list = new ArrayList<Video>();
		JSONObject jobj;
		int flag = 0;
		try {
			jobj = new JSONObject(result);
			flag = jobj.getInt("flag");
			if(flag != 1){
				App.dealWithFlag(flag);
				return null;
			}
			JSONArray listArr = jobj.getJSONObject("res").getJSONObject("data").getJSONArray("list");
			for(int i=0;i < listArr.length();i ++){
				Video vedio = new Video();
				JSONObject vedio_jobj = listArr.getJSONObject(i);
				
				vedio.video_id = vedio_jobj.getString("chapter_id") ;
				vedio.desc = vedio_jobj.getString("desc");
				vedio.video_name = vedio_jobj.getString("name");
				vedio.percent = vedio_jobj.getDouble("percent");
				if(vedio_jobj.isNull("url")) {
					vedio.video_url = "";//有些没有播放地址
				} else {
					vedio.video_url = vedio_jobj.getString("url");
				}
				vedio.course_id = courseid;
				vedio.course_name = course_name;
				vedio.video_path = Constants.COURSEWARE_PATH + course_name ;
				
				vedio_list.add(vedio);
			}
			
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		return vedio_list;
	}
	/**
	 * 加载数据
	 */
	private void loadData(List<Video> list){
		loading_layout.setVisibility(GONE);
		error_layout.setVisibility(GONE);
		if(list != null){
			
			if (list.size() == 0 && adapter.getCount() == 0) {
			    mListFootView.setVisibility(View.GONE);
				no_data_tv.setText(getResources().getString(R.string.no_courseware));
				return;
			} else {
				no_data_tv.setText("");
			}
			
			try {
                checkDownloadState(list);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
			adapter.addAll(list);
			adapter.notifyDataSetChanged();
		} else {
			if (adapter.getCount() == 0) {
				no_data_tv.setText(getResources().getString(R.string.no_courseware));
			} else {
				no_data_tv.setText("");
			}
		}
		if (adapter.getCount() < pageSize ) {
		    mListFootView.setVisibility(View.GONE);
		} else {
		    mListFootView.setVisibility(View.VISIBLE);
		}
		
	}
	/**
	 * 检查下载状态
	 * @param list
	 * @throws RemoteException 
	 */
	private void checkDownloadState(List<Video> list) throws RemoteException {
	    for (int i = 0;i < list.size(); i++) {
	        Video vedio = list.get(i);
    	    if (operator.isUrlExist(vedio.video_url)) {//查看数据库，如果已下载
                int status = mServiceApi.getDownloadState(vedio.video_url);
    	        switch (status) {
    	            case DownloadState.DOWNLOADED:
    	                File file = new File(mServiceApi.getDownloadedFile(vedio.video_url).getAbsolutePath());
    	                if (!file.exists()) {
    	                    Log.e("delete", "用户"+ App.getUserId() + " 要删除：" + vedio.video_name);
    	                    operator.deleteByUrl(vedio.video_url);
    	                    vedio.downloadState = NOT_DOWNLOADED;
    	                } else {
    	                    if (AuthorityManager.getInstance().isInnerAuthority()) {//游客不开放下载，全部不显示“已下载”
    	                        vedio.downloadState = NOT_DOWNLOADED;
    	                    } else {
    	                        vedio.downloadState = DOWNLOADED;
    	                    }
    	                }
    	                break;
    	            case DownloadState.DOWNLOADING:
    	                vedio.downloadState = DOWNLOADING;
    	                break;
    	            case DownloadState.UNKNOWN:
    	                vedio.downloadState = NOT_DOWNLOADED;
    	                break;
    	            case DownloadState.PAUSE:
    	                vedio.downloadState = DOWNLOADING;
    	                break;
    	        }
    	    } else {
    	        vedio.downloadState = NOT_DOWNLOADED;
    	    }
    	    /*else if (App.videoDownloaders.size() > 0) {
                Set<String> keySet = App.videoDownloaders.keySet();
                if (keySet.contains(vedio.video_url)) {
                    vedio.downloadState = DOWNLOADING;
                } else {
                    vedio.downloadState = NOT_DOWNLOADED;
                }
            } else if (App.videoDownloaders.size() == 0) {
                vedio.downloadState = NOT_DOWNLOADED;
            }*/
	    }
	}
	
	private void initComponent(){
		
		screen_height = ((Activity)context).getWindowManager().getDefaultDisplay().getHeight();
		Log.e("高度", "屏幕" + screen_height);
		
		inflater = LayoutInflater.from(context);
		courseware_view = (View) inflater.inflate(R.layout.courseware_list, this);
		listView = (RefreshableListView) courseware_view.findViewById(R.id.refreshable_listview);
		listView.setMode(PullToRefreshBase.Mode.PULL_FROM_START);
		
		loading_layout = (LinearLayout) courseware_view.findViewById(R.id.loading_layout);
		error_layout = (RelativeLayout) courseware_view.findViewById(R.id.error_layout);
		error_btn = (Button) courseware_view.findViewById(R.id.error_btn);
		no_data_tv = (TextView) courseware_view.findViewById(R.id.no_data_tv);
		// 引入弹出窗配置文件  
		pop_layout = LayoutInflater.from(context).inflate(R.layout.popup_layout,null);
        btn_download = (Button) pop_layout.findViewById(R.id.btn_play);
        btn_download.setText(getResources().getString(R.string.download));
        btn_delete = (Button) pop_layout.findViewById(R.id.btn_delete);
        btn_delete.setVisibility(View.GONE);
        btn_share = (Button) pop_layout.findViewById(R.id.btn_share);
        btn_share.setVisibility(View.GONE);
        
        sdCardInfo = SDCardInfo.getSDCardInfo();
        operator = VideoDownloadDBOperator.getInstance(context);
        
        mListHeadView = LayoutInflater.from(context).inflate(R.layout.common_empty_head, null);
        mListFootView = LayoutInflater.from(context).inflate(R.layout.course_coach_foot_item, null);
        mListFootView.setVisibility(View.INVISIBLE);
        
        listView.addHeaderView(mListHeadView, null, false);
        
        adapter = new CoursewareListAdapter(context,courseid,course_name);
        listView.setAdapter(adapter);
	}
	
	private void registerCallBack() {
		mServiceConnection = DownloadServiceUtil.registDownloadCallback(context,new IDownloadCallback() {

            @Override
            public void downloadServiceConnected(IDownloadServiceApi serviceApi) {
                mServiceApi = serviceApi;
                requestData();
            }

            @Override
            public void downloadServiceDisconnected() {
            }

            @Override
            public void downloadFail(String urlString, int errorCode, String errorMsg) {
                
            }

            @Override
            public void downloadStateChange(String urlString, int state) {
                if (adapter != null) {
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void downloadFinish(String urlString, String downloadedFilePath) {
                
            }

            @Override
            public void downloadUpdateProgress(String urlString, long downloadedSize, long contentLength) {
                
            }
            
        });
	}
	
	public IDownloadServiceApi getServiceApi() {
	    return mServiceApi;
	}
	
	@Override
	protected void onAttachedToWindow() {
	    registerCallBack();
	    super.onAttachedToWindow();
	}
	
    @Override
    protected void onDetachedFromWindow() {
        if (mServiceApi != null) {
			mServiceApi.destroy();
		}
		if (mServiceConnection != null) {
			context.unbindService(mServiceConnection);
		}
        if (pop != null && pop.isShowing()) {
            pop.dismiss();
            pop = null;
        }
		super.onDetachedFromWindow();
    }

	private void addListener(){
		
		error_btn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(PhoneUtil.checkNetworkEnable(context) != PhoneUtil.NETSTATE_ENABLE){
					Toast.makeText(context, getResources().getString(R.string.please_open_network),
							Toast.LENGTH_SHORT).show();
				} else {
					error_layout.setVisibility(GONE);
					loading_layout.setVisibility(VISIBLE);
					pageNum = 1;
	                adapter.clear();
					requestData();
				}
			}
		});
		listView.setonRefreshListener(new OnRefreshListener() {
			
			@Override
			public void onRefresh() {
				pageNum = 1;
				adapter.clear();
				requestData();
			}
			
			@Override
			public void onLoadMore() {
				++ pageNum ;
				requestData();
			}
		});
		
	}
	
	public void playNextVideo(String videoId){
        videos.clear();
        videos.addAll(adapter.getVedioList());
        for (int i = 0; i < videos.size(); i++) {
            if (videos.get(i).video_id.equals(videoId)) {
                if (i + 1 == videos.size()) {
                    Helper.ToastUtil(context, "已经是最后一章");
                    return;
                }
                Video video = videos.get(i + 1);
                adapter.setCurrentPlayVideoId(video.video_id);
                adapter.notifyDataSetChanged();

                CourseDetailActivity.playOnlineVideo(getContext(), video.video_id);
            }
        }
    }
	
	
	/**
	 * 
	 * <p>设置当前播放视频的ID</P>
	 *
	 * @param videoId 视频ID
	 */
    public void setCurrentPlayVideoId(String videoId,boolean focusable) {
        adapter.setCurrentPlayVideoId(videoId);
        adapter.notifyDataSetChanged();
        this.focusable = focusable;
    }


}
