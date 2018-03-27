package com.nd.shapedexamproj.adapter;

import android.content.*;
import android.os.Environment;
import android.os.RemoteException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.*;

import com.nd.shapedexamproj.App;
import com.nd.shapedexamproj.AuthorityManager;
import com.nd.shapedexamproj.R;
import com.nd.shapedexamproj.activity.course.CourseDetailActivity;
import com.nd.shapedexamproj.db.VideoDownloadDBOperator;
import com.nd.shapedexamproj.model.Video;
import com.nd.shapedexamproj.model.download.DownloadInfo;
import com.nd.shapedexamproj.util.StringUtils;
import com.nd.shapedexamproj.view.RoundProgressBar;
import com.tming.common.download.DownloadServiceUtil;
import com.tming.common.download.DownloadState;
import com.tming.common.download.IDownloadCallback;
import com.tming.common.download.IDownloadServiceApi;
import com.tming.common.util.Helper;
import com.tming.common.util.PhoneUtil;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
/**
 * 课程详情--课件列表
 * Created by zll on 2014-10-31
 */
public class CoursewareListAdapter extends BaseAdapter{
    
    private final String TAG = "CoursewareListAdapter";
    public final String UNBIND_SERVICE = "kd.coursedetail.unregister";
    private final int DOWNLOADING = 1;
    private final int DOWNLOADED = 0;
    private final int NOT_DOWNLOADED = 2;
    private final int DOWNLOAD_WAITING = 3;
    private final int PAUSED = 4;
    ViewHolder holder = null;
    private VideoDownloadDBOperator operator;
    private String mCurrentPlayVideoId;
    private int mCurrentPlayPosition;//当前视频的位置
    
    private IDownloadServiceApi mServiceApi;
    private ServiceConnection mServiceConnection;
    
    public String getmCurrentPlayVideoId() {
        return mCurrentPlayVideoId;
    }
    
    public int getmCurrentPlayPosition() {
        return mCurrentPlayPosition;
    }
    
    public void setmCurrentPlayPosition(int mCurrentPlayPosition) {
        this.mCurrentPlayPosition = mCurrentPlayPosition;
    }

    private int lightDark,white,titleGreen;
    private String courseid = "";   //课程id
    private String course_name = "" ;
    
    private Context context;
    
    private List<Video> vedioList = new ArrayList<Video>();
    private List<Video> cacheList = new LinkedList<Video>();

    private boolean mIsPassenger=false;//是否为游客

    public CoursewareListAdapter(Context context,String courseid,String course_name) {
        this.context = context;
        this.courseid = courseid;
        this.course_name = course_name;
        
        IntentFilter filter = new IntentFilter();
        filter.addAction(UNBIND_SERVICE);
        context.registerReceiver(receiver, filter); 
        
        registerCallBack();
        lightDark = context.getResources().getColor(R.color.light_dark);
        white = context.getResources().getColor(R.color.white);
        titleGreen = context.getResources().getColor(R.color.title_green);
        operator = VideoDownloadDBOperator.getInstance(context);
        //判定是否为游客
        mIsPassenger= App.getUserId().equals("0")?true:false;
    }
    
    private BroadcastReceiver receiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(UNBIND_SERVICE)) {
                if (mServiceApi != null) {
                    mServiceApi.destroy();
                }
                if (mServiceConnection != null) {
                    context.unbindService(mServiceConnection);
                }
                context.unregisterReceiver(receiver);
            }
        }
        
    };
    
    private void registerCallBack() {
        mServiceConnection = DownloadServiceUtil.registDownloadCallback(context,new IDownloadCallback() {
            
            @Override
            public void downloadStateChange(String urlString, int status) {

                if(status == DownloadState.DOWNLOADED) {
                    findVideoByUrl(urlString).downloadState = DOWNLOADED;
                } else if(status == DownloadState.DOWNLOADING) {
                    findVideoByUrl(urlString).downloadState = DOWNLOADING;
                } else if(status == DownloadState.PAUSE) {
                    findVideoByUrl(urlString).downloadState = PAUSED;
                } else if(status == DownloadState.UNKNOWN) {
                    findVideoByUrl(urlString).downloadState = NOT_DOWNLOADED;
                }
                notifyDataSetChanged();
            }
            
            @Override
            public void downloadServiceDisconnected() {
            }
            
            @Override
            public void downloadServiceConnected(IDownloadServiceApi serviceApi) {
                mServiceApi = serviceApi;
            }
            
            @Override
            public void downloadFail(String urlString, int errorCode, String errorMsg) {
                if (operator.isUrlExist(urlString)) {
                    operator.deleteByUrl(urlString);
                }
                Toast.makeText(context,"课件下载失败",Toast.LENGTH_SHORT).show();
            }

            @Override
            public void downloadFinish(String urlString, String downloadedFilePath) {
                
            }

            @Override
            public void downloadUpdateProgress(String urlString, long downloadedSize, long contentLength) {
                
            }
        });
    }
    
    public void addAll(List<Video> list){
        cacheList.addAll(list);
    }
    
    public void clear(){
        vedioList.clear();
    }
    
    public List<Video> getVedioList() {
        return vedioList;
    }

    public void setCurrentPlayVideoId(String mCurrentPlayVideoId) {
        this.mCurrentPlayVideoId = mCurrentPlayVideoId;
    }
    
    @Override
    public void notifyDataSetChanged() {
        vedioList.addAll(cacheList);
        cacheList.clear();
        super.notifyDataSetChanged();
    }
    
    @Override
    public int getCount() {
        return vedioList.size();
    }

    @Override
    public Object getItem(int position) {
        return vedioList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }
    
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        
        if(convertView == null){
            holder = new ViewHolder();
            convertView = LayoutInflater.from(context).inflate(R.layout.course_detail_courseware_item, null);
            holder.item_tv = (TextView) convertView.findViewById(R.id.courseware_list_item_tv);
            holder.item_layout = (RelativeLayout) convertView.findViewById(R.id.courseware_list_item);
            holder.img_layout = (RelativeLayout) convertView.findViewById(R.id.courseware_list_item_img_lay);
            holder.courseware_list_item_waiting_rl = (RelativeLayout) convertView.findViewById(R.id.courseware_list_item_waiting_rl);
            holder.courseware_list_item_img = (ImageView) convertView.findViewById(R.id.courseware_list_item_img);
            holder.playState = (RoundProgressBar) convertView.findViewById(R.id.courseware_list_item_play_state);
            holder.courseware_list_item_right_tv = (TextView) convertView.findViewById(R.id.courseware_list_item_right_tv);
            holder.bottomDivider = (View) convertView.findViewById(R.id.courseware_bottom_divider);
            holder.topDivider = (View) convertView.findViewById(R.id.courseware_top_divider);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        if (position == 0) {
            holder.topDivider.setVisibility(View.VISIBLE);
            holder.bottomDivider.setVisibility(View.GONE);
        } else if (position == getCount() - 1) {
            holder.topDivider.setVisibility(View.GONE);
            holder.bottomDivider.setVisibility(View.VISIBLE);
        } else {
            holder.topDivider.setVisibility(View.GONE);
            holder.bottomDivider.setVisibility(View.GONE);
        }
        
        final Video video = vedioList.get(position);
        
        // 如果是当前播放的视频ID，选中效果
        if (video.video_id.equals(mCurrentPlayVideoId)) {
            holder.item_tv.setTextColor(titleGreen);
        } else {
            holder.item_tv.setTextColor(context.getResources().getColor(R.color.black));
        }
        
        holder.item_tv.setText(video.video_name);
        holder.item_layout.setOnClickListener(new myLayoutClickListener(position,video));
        
        if (video.percent > 0 && video.percent <= 1) {
            holder.playState.setCricleProgressColor(titleGreen);
            holder.playState.setProgress((int)(video.percent * 100));
        } else if (video.percent == 0) {
            holder.playState.setCricleProgressColor(white);
            holder.playState.setProgress(0);
        }
        
        setHolderStyle(holder, video.downloadState);
        holder.courseware_list_item_img.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    downloadCourseware(holder,video);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        });
        
        return convertView;
    }
    
    public void replaceItem(List<Video> oldData, List<Video> newData) {
        // 如果不存在旧数据
        if (oldData == null || oldData.size() == 0) {
            vedioList.addAll(newData);
            notifyDataSetChanged();
            return;
        }
        
        // 做替换
        int oldStartIndex = vedioList.indexOf(oldData.get(0));
        int oldEndIndex = vedioList.indexOf(oldData.get(oldData.size() - 1));
        if (oldStartIndex != -1 && oldEndIndex != -1) {
            for (int i = oldStartIndex; i <= oldEndIndex; i++) {
                vedioList.remove(oldStartIndex);
            }
            int insertStartIndex = oldStartIndex; 
            for (int i = 0; i < newData.size(); i++) {
                vedioList.add(insertStartIndex++, newData.get(i));
            }
        }
        super.notifyDataSetChanged();
    }
    
    private void setHolderStyle(ViewHolder holder,int style) {

        if(mIsPassenger){
            //游客模式下，全部隐藏
            holder.courseware_list_item_img.setVisibility(View.GONE);
        } else{
            if (style == NOT_DOWNLOADED || style == PAUSED) {
                holder.courseware_list_item_img.setVisibility(View.VISIBLE);
                holder.courseware_list_item_right_tv.setVisibility(View.GONE);
                holder.courseware_list_item_waiting_rl.setVisibility(View.GONE);
            } else if (style == DOWNLOADED) {
                holder.courseware_list_item_img.setVisibility(View.GONE);
                holder.courseware_list_item_right_tv.setVisibility(View.VISIBLE);
                holder.courseware_list_item_right_tv.setText(context.getResources().getString(R.string.already_download));
                holder.courseware_list_item_waiting_rl.setVisibility(View.GONE);
            } else if (style == DOWNLOAD_WAITING) {
                holder.courseware_list_item_waiting_rl.setVisibility(View.VISIBLE);
                holder.courseware_list_item_img.setVisibility(View.GONE);
                holder.courseware_list_item_right_tv.setVisibility(View.GONE);
            } else if (style == DOWNLOADING) {
                holder.courseware_list_item_img.setVisibility(View.GONE);
                holder.courseware_list_item_right_tv.setVisibility(View.VISIBLE);
                holder.courseware_list_item_right_tv.setText(context.getResources().getString(R.string.courseware_downloading));
                holder.courseware_list_item_waiting_rl.setVisibility(View.GONE);
            }
        }
    }
    
    private void downloadCourseware(final ViewHolder holder,final Video video) throws RemoteException {
    
        if (AuthorityManager.getInstance().isInnerAuthority()) {//游客不开放下载
            AuthorityManager.getInstance().showInnerDialog(context);
            return;
        }
        if (PhoneUtil.checkNetworkEnable() != PhoneUtil.NETSTATE_ENABLE) {
            Helper.ToastUtil(context, context.getResources().getString(R.string.net_error_tip));
            return;
        }
        if (StringUtils.isEmpty(video.video_url)) {
            Helper.ToastUtil(context, context.getResources().getString(R.string.download_useless_url));
            return;
        }
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            Toast.makeText(context, context.getResources().getString(R.string.download_no_sdcard), Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (operator.isUrlExist(video.video_url)) {
            Toast.makeText(context, context.getResources().getString(R.string.video_exist), Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (mServiceApi.getDownloadingUrlStrings().contains(video.video_url)) {
            Toast.makeText(context, context.getResources().getString(R.string.downloading), Toast.LENGTH_SHORT).show();
            return;
        }
        DownloadInfo downloadInfo = new DownloadInfo();
        downloadInfo.title = video.video_name;
        downloadInfo.url = video.video_url;
        downloadInfo.coursecateid = courseid.hashCode();
        downloadInfo.coursecatename = course_name;
        downloadInfo.courseId = courseid;
        downloadInfo.videoId = video.video_id;
        
        DownloadServiceUtil.startDownload(context,video.video_url);
        if (!operator.isUrlExist(downloadInfo.url)) {
            operator.insert(downloadInfo);
        }
        Toast.makeText(context, "正在下载中，可以到下载页查看进度", Toast.LENGTH_SHORT).show();
    
    }
    
    /**
     * <p>根据url查找对象</P>
     *
     * @param remoteUrl
     * @return
     */
    private Video findVideoByUrl(String remoteUrl) {
        for (Video video : vedioList) {
            if (video.video_url.equals(remoteUrl)) {
                return video;
            }
        }
        return null;
    }
    
    
    /**
     * 列表项点击监听( 在小屏上播放视频)
     */
    private class myLayoutClickListener implements OnClickListener{
        private int position ;
        private Video video ;
        
        public myLayoutClickListener(int position,Video video){
            this.position = position;
            this.video = video;
        }
        
        @Override
        public void onClick(View v) {
            if (operator.isUrlExist(video.video_url)) {
                int status = DownloadState.UNKNOWN;
                try {
                    status = mServiceApi.getDownloadState(video.video_url);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                if (status == DownloadState.DOWNLOADING || status == DownloadState.PAUSE) {
                    CourseDetailActivity.playOnlineVideo(context, video.video_id);
                } else if (status == DownloadState.DOWNLOADED) {
                    CourseDetailActivity.playLocalVideo(video.video_id, video.video_url, video.video_name);
                }
                
            } else {
                CourseDetailActivity.playOnlineVideo(context, video.video_id);
            }
        }
    }
    
    private class ViewHolder {
        private TextView item_tv, courseware_list_item_right_tv;
        private ImageView courseware_list_item_img;
        private RoundProgressBar playState;
        private View bottomDivider,topDivider;
        private RelativeLayout item_layout,img_layout;
        private RelativeLayout courseware_list_item_waiting_rl;
    }
}