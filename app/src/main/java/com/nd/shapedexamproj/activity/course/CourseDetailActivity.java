package com.nd.shapedexamproj.activity.course;

import android.content.*;
import android.content.SharedPreferences.Editor;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Color;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.TranslateAnimation;
import android.widget.*;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.SeekBar.OnSeekBarChangeListener;

import com.nd.shapedexamproj.App;
import com.nd.shapedexamproj.AuthorityManager;
import com.nd.shapedexamproj.R;
import com.nd.shapedexamproj.activity.BaseActivity;
import com.nd.shapedexamproj.db.VideoDownloadDBOperator;
import com.nd.shapedexamproj.entity.LearningCourseInfoEntity;
import com.nd.shapedexamproj.fragment.StudentCourseFragment;
import com.nd.shapedexamproj.model.CourseDetail;
import com.nd.shapedexamproj.model.KnowledgeInfo;
import com.nd.shapedexamproj.model.Video;
import com.nd.shapedexamproj.model.download.DownloadInfo;
import com.nd.shapedexamproj.net.ServerApi;
import com.nd.shapedexamproj.util.Constants;
import com.nd.shapedexamproj.util.UIHelper;
import com.nd.shapedexamproj.view.course.CourseDetailLayout;
import com.nd.shapedexamproj.view.course.CoursewareList;
import com.nd.shapedexamproj.view.video.MenuDownloadLayout;
import com.nd.shapedexamproj.view.video.MenuListView;
import com.nd.shapedexamproj.view.video.MenuNoteView;
import com.nd.shapedexamproj.view.video.MenuQuestionView;
import com.tming.common.cache.net.TmingCacheHttp;
import com.tming.common.cache.net.TmingCacheHttp.RequestWithCacheCallBackV2Adapter;
import com.tming.common.download.DownloadServiceUtil;
import com.tming.common.download.DownloadState;
import com.tming.common.download.IDownloadCallback;
import com.tming.common.download.IDownloadServiceApi;
import com.tming.common.net.TmingHttp;
import com.tming.common.net.TmingHttp.RequestCallback;
import com.tming.common.net.TmingHttpRequestTask;
import com.tming.common.util.Helper;
import com.tming.common.util.Log;
import com.tming.common.util.PhoneUtil;
import com.tming.common.util.ScreenObserver;
import com.tming.common.util.ScreenObserver.ScreenStateListener;
import io.vov.vitamio.MediaPlayer;
import io.vov.vitamio.MediaPlayer.OnBufferingUpdateListener;
import io.vov.vitamio.MediaPlayer.OnCompletionListener;
import io.vov.vitamio.MediaPlayer.OnInfoListener;
import io.vov.vitamio.MediaPlayer.OnPreparedListener;
import io.vov.vitamio.utils.StringUtils;
import io.vov.vitamio.widget.VideoView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.*;

/**
 * 
 * 视频课程模块
 * 
 * @author Linlg
 * 
 *         Create on 2014-3-22
 */
public class CourseDetailActivity extends BaseActivity {

    private final int MESSAGE_VITAMIO = 0; // Vitamio消息
    private final int MESSAGE_TOUCH = 1; // 视频控制栏消息
    private final int MESSAGE_ONLINE = 2; // 在线人数消息
    private final long TOUCH_TIME = 5000; // 设置视频控制栏隐藏时间
    private final long SET_TIME = 30000; // 设置快进、快退时间
    private final int VIDEO_NET = 1; // 网络视频
    private final int VIDEO_LOCAL = 2; // 本地视频
    private final int VIDEO_SMALL = 1; // 半屏
    private final int VIDEO_ZOOM = 2; // 全屏
    private final int BUTTON_PLAY = 1; // 播放
    private final int BUTTON_ZOOM = 2; // 全屏
    private final int BUTTON_SOUND = 3; // 声音
    private final int BUTTON_PRE = 4; // 快退
    private final int BUTTON_NEXT = 5; // 快进
    private final int BUTTON_MENU = 6; // 菜单
    private final int BUTTON_BACK = 7; // 返回
    private final int BUTTON_MENU_LIST = 8; // 菜单目录
    private final int BUTTON_MENU_DISCUSS = 9; // 菜单讨论
    private final int BUTTON_MENU_ASK = 10; // 菜单提问
    private final int BUTTON_MENU_NOTE = 11; // 菜单笔记
    private final int BUTTON_MENU_DOWNLOAD = 12; // 菜单下载
    private final int BUTTON_MENU_CLOSED = 13; // 菜单关闭
    private final int BUTTON_CENTER_PLAY = 14; // 中间播放按钮
    private final int BUTTON_HEAD_BACK = 15; // 头部返回按钮
    private final int BUTTON_HEAD_HOMEWORK = 17;//做作业
    private final int FULL_SCREEN_MASK = 16;//全屏遮罩
    private long videoDuration; // 视频长度
    private int menuMargin; // 菜单视图左边间距
    private boolean isSeeking = false; // seekbar是否拖曳
    private boolean isZoom = false; // 是否全屏
    private boolean isFirstZoom = true;
    private boolean isScreenLock = false;
    private boolean isDetail = true;
    private String videoUrl = ""; // 视频播放地址
    private List<KnowledgeInfo> knowledges = new ArrayList<KnowledgeInfo>();
    private VideoView videoView;
    private RelativeLayout videoLayout, topBarLayout, centerLayout, headerLayout, menuLayout, listLayout, /*
                                                                                                           * discussLayout
                                                                                                           */
    noteLayout, questionLayout, downloadLayout, closeLayout;
    private LinearLayout headRightBtn,menuDetailLayout, /*loadingLayout*/ sknowledgesLayout, zknowledgesLayout;
    private View sbarView, zbarView, finishedView,fullMask;
    private ImageButton splayBtn, szoomBtn, zplayBtn;
    private Button zmenuBtn, zsoundBtn, zpreBtn, znextBtn, topBackBtn, topSpeakBtn ;
    private SeekBar sSeekbar, zSeekbar;
    private TextView /* sshowTimeTv, */zcurrentTimeTV, ztotalTimeTv, zdownloadSpeedTv, topBufferTv, topTitleTv, tipsTv,
            loadingTv, scoursenameTv/* , peopleTv */, currentRateTv, common_head_left_tv;
    private ImageView listIv, /* discussIv, */noteIv, questionIv, downloadIv, common_head_left_iv,centerPlayBtn;
    private TextView listTv, /* discussTv, */noteTv, questionTv, downloadTv;
    private ProgressBar centerplayProgress;
    private Handler handler;
    private TmingCacheHttp cacheHttp;
    private ScreenObserver mScreenObserver;
    private View menuListView ;
    private MenuDownloadLayout menuDownloadView;
    private MenuQuestionView menuQuestionView ;
    private MenuNoteView noteView;
    private SharedPreferences spf = null; 
    private Editor editor;
    // ----------------------------非视频模块------------------------------
    private String videoId = "";
    private String course_id = "";   // 课程id
    private String user_id = "0";    // 用户id
    private String course_name = ""; // 课程名称
    private String videoName = "";   // 课程名称
    private int pageNum = 1,pageSize = 30;
    private List<String> progress_list = null; // 作业完成进度
    /**
     * 在修课程3，播放历史2、本地调用1、网络0
     */
    private int local = 0;
    private VideoDownloadDBOperator operator;
    private long currentPosition = 0;
    private boolean isFirst = true;      // 是否第一次加载
    private boolean isComeBack = false;  // 是否返回
    private boolean isVideoPlay = false; // 视频是否播放
    private boolean isFromList = false;  // 是否列表播放
    private boolean isLock = false;
    private CourseDetailLayout mBottomLayout;
    /**
     * 当前正在播放的课件详情
     */
    private CourseDetail courseDetail = null;
    private TmingHttpRequestTask mRequestCourseDetailTask;
    private long playHistory = 0;

    private ServiceConnection mServiceConnection;

    @Override
    public int initResource() {
        return R.layout.videoview;
    }

    @Override
    public void initComponent() {
        cacheHttp = TmingCacheHttp.getInstance();
        spf = getSharedPreferences(Constants.SHARE_PREFERENCES_NAME, Context.MODE_PRIVATE);
        editor = spf.edit();
        
        onScreenStatusListener();
        local = getIntent().getIntExtra("local", 0);
        initViews();
        initHandler();
        operator = new VideoDownloadDBOperator(this);
        
        registerCallBack();
    }

    /**
     * 播放历史>课件详情
     */
    private void playFromHistoryPage() {
        setVideoState(BUFFERING);
        videoUrl = getIntent().getStringExtra("video_url");
        videoName = getIntent().getStringExtra("video_name");
        videoId = getIntent().getStringExtra("video_id");
        checkVitamio();
    }
    
    /**
     * 下载管理>课件详情
     */
    private void playFromDownloadPage() {
        setVideoState(PLAYING);
        videoId = getIntent().getStringExtra("video_id");
        videoUrl = getIntent().getStringExtra("video_url");
        videoName = getIntent().getStringExtra("video_name");
        checkVitamio();
    }
    /**
     * <p>从在修课程进入</P>
     */
    private void playFromLearningPage() {
        videoId = getIntent().getStringExtra("video_id");
    }
    
    private void registerCallBack() {
        mServiceConnection = DownloadServiceUtil.registDownloadCallback(this, new IDownloadCallback() {

            @Override
            public void downloadServiceConnected(IDownloadServiceApi serviceApi) {
                mServiceApi = serviceApi;
                if (local == 1) {
                    playFromDownloadPage();
                } else if (local == 2) {
                    playFromHistoryPage();
                } else if (local == 3) {
                    playFromLearningPage();
                }
                initCourseDetailLayout();

                if (videoId.equals("")) {//获取课件列表
                    requestCoursewareList();
                } else {
                    requestVideoDetail();
                }
            }

            @Override
            public void downloadServiceDisconnected() {
                
            }

            @Override
            public void downloadUpdateProgress(String urlString, long downloadedSize, long contentLength) {
                
            }

            @Override
            public void downloadFinish(String urlString, String downloadedFilePath) {
                
            }

            @Override
            public void downloadFail(String urlString, int errorCode, String errorMsg) {
                
            }

            @Override
            public void downloadStateChange(String urlString, int state) {
                
            }
            
        });
    }
    
    @Override
    public void initData() {}

    @Override
    public void addListener() {}

    /**
     * 初始化视频Vitamio
     */
    private void checkVitamio() {
        handler.sendEmptyMessage(MESSAGE_VITAMIO);
    }

    /**
     * 初始化Handle
     */
    private void initHandler() {
        handler = new Handler() {

            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if (msg.what == MESSAGE_TOUCH) {
                    sbarView.setVisibility(View.GONE);
                    /*headerLayout.setVisibility(View.GONE);*/
                    zbarView.setVisibility(View.GONE);
                    topBarLayout.setVisibility(View.GONE);
                } else if (msg.what == MESSAGE_VITAMIO) {
                    if (videoUrl == null || videoUrl.equals("")) {
                        Toast.makeText(CourseDetailActivity.this, getResources().getString(R.string.null_path),
                                Toast.LENGTH_SHORT).show();
                        return;
                    }
                    
                    if (operator.isUrlExist(videoUrl)) {//判断播放本地还是播放网络视频
                        int status = DownloadState.UNKNOWN;
                        try {
                            if (mServiceApi == null) {
                                mServiceApi = mBottomLayout.getCouseWareListView().getServiceApi();
                            }
                            status = mServiceApi.getDownloadState(videoUrl);
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                        if (status == DownloadState.DOWNLOADING || status == DownloadState.PAUSE) {
                            playVideo(VIDEO_NET);
                        } else if (status == DownloadState.DOWNLOADED) {
                            playVideo(VIDEO_LOCAL);
                        }
                    } else {
                        playVideo(VIDEO_NET);
                    }
                }
            }
        };
    }

    private void initCourseDetailLayout() {
        Intent intent = new Intent();
        intent.setAction(Constants.COURSE_DETAIL_ACTION);
        intent.putExtra(Constants.COURSE_ID, course_id);
        intent.putExtra(Constants.COURSE_NAME, course_name);
        intent.putExtra("time", "1"); // 表示第一次
        sendBroadcast(intent);
        Log.e("CourseDetailActivity","===========发送广播到详情页布局==========");
    }

    private void focusCurrentVideo() {
        Intent intent = new Intent();
        intent.setAction(Constants.COURSE_DETAIL_FOCUS_CURR_VIDEO);
        sendBroadcast(intent);
    }

    /**
     * 锁屏监听
     */
    private void onScreenStatusListener() {
        mScreenObserver = new ScreenObserver(this);
        mScreenObserver.requestScreenStateUpdate(new ScreenStateListener() {

            @Override
            public void onScreenOn() {
                if (noteView != null) {
                    noteView.resume();
                }
            }

            @Override
            public void onScreenOff() {
                if (!isPause()) {
                    videoView.pause();
                    zplayBtn.setImageResource(R.drawable.mediacontroller_play);
                    splayBtn.setImageResource(R.drawable.mediacontroller_play);
                }
                if (noteView != null) {//暂停语音笔记播放
                    noteView.pause();
                }
            }

            @Override
            public void onUserPresent() {
            }
        });
    }

    public long getVideoCurrent() {
        return videoView.getCurrentPosition();
    }

    /**
     * 初始化视图
     */
    private void initViews() {
        user_id = App.getUserId();
        course_id = getIntent().getStringExtra("course_id");
        course_name = getIntent().getStringExtra("course_name");
        // 注册广播
        IntentFilter selectFilter = new IntentFilter();
        selectFilter.addAction("com.kaida.video");
        selectFilter.addAction("play.local.video");
        selectFilter.addAction("com.tming.videoview.play.resume");
        selectFilter.addAction("com.tming.videoview.play.pause");
        selectFilter.addAction("com.tming.kd.seekto");
        selectFilter.addAction("com.tming.videoview.list.setVideo");
        selectFilter.addAction("com.tming.openuniversity.updatePlayButtonVisible");
        registerReceiver(receiver, selectFilter);
        // 返回按钮
        common_head_left_iv = (ImageView) findViewById(R.id.common_head_left_iv);
        common_head_left_iv.setOnClickListener(new OnclickListenerImpl(BUTTON_HEAD_BACK));
        common_head_left_tv = (TextView) findViewById(R.id.common_head_left_tv);
        common_head_left_tv.setText(course_name);
        headRightBtn = (LinearLayout) findViewById(R.id.videoview_do_homework_ll);
        headRightBtn.setOnClickListener(new OnclickListenerImpl(BUTTON_HEAD_HOMEWORK));
        videoView = (io.vov.vitamio.widget.VideoView) findViewById(R.id.videoview);
        videoLayout = (RelativeLayout) findViewById(R.id.videoview_rl);
        centerLayout = (RelativeLayout) findViewById(R.id.videoview_centerplay_rl);
        /*loadingLayout = (LinearLayout) findViewById(R.id.videoview_loading_ll);*/
        loadingTv = (TextView) findViewById(R.id.videoview_loadingrate_tv);
        tipsTv = (TextView) findViewById(R.id.videoview_tips_tv);
        centerPlayBtn = (ImageView) findViewById(R.id.videoview_centerplay_btn);
        centerplayProgress = (ProgressBar) findViewById(R.id.videoview_centerplay_progress);
        
        headerLayout = (RelativeLayout) findViewById(R.id.videoview_head_rl);
        scoursenameTv = (TextView) findViewById(R.id.videoview_coursename_tv);
        menuLayout = (RelativeLayout) findViewById(R.id.videoview_right_rl);
        listLayout = (RelativeLayout) findViewById(R.id.videoview_menulist_rl);
        // discussLayout = (RelativeLayout)
        // findViewById(R.id.videoview_menudiscuss_rl);
        noteLayout = (RelativeLayout) findViewById(R.id.videoview_menunote_rl);
        questionLayout = (RelativeLayout) findViewById(R.id.videoview_menuask_rl);
        downloadLayout = (RelativeLayout) findViewById(R.id.videoview_menudownload_rl);
        closeLayout = (RelativeLayout) findViewById(R.id.videoview_menuclosed_rl);
        listIv = (ImageView) findViewById(R.id.videoview_menulist_iv);
        listTv = (TextView) findViewById(R.id.videoview_menulist_tv);
        // discussIv = (ImageView) findViewById(R.id.videoview_menudiscuss_iv);
        // discussTv = (TextView) findViewById(R.id.videoview_menudiscuss_tv);
        noteIv = (ImageView) findViewById(R.id.videoview_menunote_iv);
        noteTv = (TextView) findViewById(R.id.videoview_menunote_tv);
        questionIv = (ImageView) findViewById(R.id.videoview_menuask_iv);
        questionTv = (TextView) findViewById(R.id.videoview_menuask_tv);
        downloadIv = (ImageView) findViewById(R.id.videoview_menudownload_iv);
        downloadTv = (TextView) findViewById(R.id.videoview_menudownload_tv);
        menuDetailLayout = (LinearLayout) findViewById(R.id.videoview_menudetail_ll);
        /*currentRateTv = (TextView) findViewById(R.id.videoview_current_rate_tv);*/
        finishedView = findViewById(R.id.videoview_finished_layout);
        // downloadBtn = (Button) findViewById(R.id.videoview_menudownload_btn);
        // closedBtn = (Button) findViewById(R.id.videoview_menuclosed_btn);
        // menuDetailLayout = (LinearLayout)
        // findViewById(R.id.videoview_menudetail_ll);
        // 半屏控制栏视图
        sbarView = (View) findViewById(R.id.videoview_minbar);
        splayBtn = (ImageButton) sbarView.findViewById(R.id.videoviewbar_play_imgbtn);
        szoomBtn = (ImageButton) sbarView.findViewById(R.id.videoviewbar_zoom_imgbtn);
        sSeekbar = (SeekBar) sbarView.findViewById(R.id.videoviewbar_seek_sb);
        // sshowTimeTv = (TextView)
        // sbarView.findViewById(R.id.videoviewbar_showtime_tv);
        sknowledgesLayout = (LinearLayout) sbarView.findViewById(R.id.videoviewbar_knowledges_ll);
        // 全屏控制栏视图
        zbarView = (View) findViewById(R.id.videoview_zoombar);
        zplayBtn = (ImageButton) zbarView.findViewById(R.id.videoviewzoombar_play_imgbtn);
        zSeekbar = (SeekBar) zbarView.findViewById(R.id.videoviewzoombar_seek_sb);
        zcurrentTimeTV = (TextView) zbarView.findViewById(R.id.videoviewzoombar_currenttime_tv);
        ztotalTimeTv = (TextView) zbarView.findViewById(R.id.videoviewzoombar_totaltime_tv);
        zdownloadSpeedTv = (TextView) zbarView.findViewById(R.id.videoviewzoombar_downloadspeed_tv);
        zmenuBtn = (Button) zbarView.findViewById(R.id.videoviewzoombar_menu_btn);
        zsoundBtn = (Button) zbarView.findViewById(R.id.videoviewzoombar_sound_btn);
        zpreBtn = (Button) zbarView.findViewById(R.id.videoviewzoombar_pre_btn);
        znextBtn = (Button) zbarView.findViewById(R.id.videoviewzoombar_next_btn);
        zknowledgesLayout = (LinearLayout) zbarView.findViewById(R.id.videoviewzoombar_knowledges_ll);
        
        fullMask = findViewById(R.id.videoview_full_mask);
        
        topBarLayout = (RelativeLayout) findViewById(R.id.videoview_topbar_rl);
        topBackBtn = (Button) findViewById(R.id.videoview_topbarback_btn);
        // topSpeakBtn = (Button) findViewById(R.id.videoview_topbarspeak_btn);
        topTitleTv = (TextView) findViewById(R.id.videoview_topbartitle_btn);
        topBufferTv = (TextView) findViewById(R.id.videoview_topbarbuffer_tv);
        mBottomLayout = (CourseDetailLayout) findViewById(R.id.videoview_coursedetail_rv);
        mBottomLayout.setActivity(this);
        initAuthority();
        // peopleTv = (TextView) findViewById(R.id.videoview_people_tv);
        //requestData();TODO 暂时屏蔽做作业
    }
    
    private void initAuthority () {
        if (AuthorityManager.getInstance().isInnerAuthority()) {
            headRightBtn.setVisibility(View.GONE);
        } else {
            requestLearningCourses(1,100);
        }
    }

    /**
     * 获取用户正在学习的课程列表
     *
     * @param pageNum 页号
     * @param PageSize 每页记录数
     */
    private void requestLearningCourses(int pageNum,int PageSize) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("userid", App.getUserId());
        params.put("pageNum", pageNum);
        params.put("pageSize", PageSize);
        cacheHttp.asyncRequestWithCache(Constants.HOST + "student/courses.html", params, requestCoursingCallBack);
    }

    /**
     * mTmingCacheHttp 请求回调函数
     */
    private RequestWithCacheCallBackV2Adapter<List<LearningCourseInfoEntity>> requestCoursingCallBack =
            new RequestWithCacheCallBackV2Adapter<List<LearningCourseInfoEntity>>() {
        private boolean isCoursing;//是否在修课程
        @Override
        public List<LearningCourseInfoEntity> parseData(String data) throws Exception {
            return coursingJSONParsing(data);
        }

        @Override
        public void cacheDataRespone(List<LearningCourseInfoEntity> data) {
            for (LearningCourseInfoEntity entity: data) {
                if (entity.mCourseId.equals(course_id)) {
                    isCoursing = true;
                    break;
                }
            }
        }

        @Override
        public void requestNewDataRespone(List<LearningCourseInfoEntity> cacheRespones, List<LearningCourseInfoEntity> newRespones) {
            for (LearningCourseInfoEntity entity: newRespones) {
                if (entity.mCourseId.equals(course_id)) {
                    isCoursing = true;
                    break;
                }
            }
        }

        @Override
        public void exception(Exception exception) {
            super.exception(exception);
        }

        @Override
        public void onFinishRequest() {
            if (isCoursing) {
                headRightBtn.setVisibility(View.VISIBLE);
            } else {
                headRightBtn.setVisibility(View.GONE);
            }
        }
    };

    /**
     * 字符串封装为JSON数组
     *
     * @param result JSON字符串
     * @return List<LearningCourseInfoEntify> 在修课程列表
     * @throws JSONException JSON转换异常
     */
    private List<LearningCourseInfoEntity> coursingJSONParsing(String result)
            throws JSONException {
        List<LearningCourseInfoEntity> coursings = new ArrayList<LearningCourseInfoEntity>();
        JSONObject object = new JSONObject(result);
        JSONObject dataObj = object.getJSONObject("res").getJSONObject("data");
        if (!dataObj.isNull("list")) {
            JSONArray list = dataObj.getJSONArray("list");
            for (int i = 0; i < list.length(); i++) {
                LearningCourseInfoEntity coursing = new LearningCourseInfoEntity();
                coursing.mCourseId = list.getJSONObject(i)
                        .getString("course_id");
                coursing.mCredit = list.getJSONObject(i).getString(
                        "course_credit");
                coursing.mName = list.getJSONObject(i).getString("name");
                coursing.mTimes = list.getJSONObject(i).getString("times");
                coursing.mPhoto=list.getJSONObject(i).getString("cover");
                coursing.mPercent = list.getJSONObject(i).getDouble(
                        "course_percent");
                if (!list.getJSONObject(i).isNull("play_recorded")){
                    coursing.setPlayRecord( list.getJSONObject(i).getJSONObject("play_recorded"));
                } else{
                    coursing.setPlayRecord(null);
                }
                coursings.add(coursing);
            }
        }
        return coursings;
    }
     /**
     * 注册监听事件
     */
    private void registerListener() {
        videoLayout.setOnTouchListener(onTouchListenerImpl);
        listLayout.setOnClickListener(new OnclickListenerImpl(BUTTON_MENU_LIST));
        // discussLayout.setOnClickListener(new OnclickListenerImpl(
        // BUTTON_MENU_DISCUSS));
        noteLayout.setOnClickListener(new OnclickListenerImpl(BUTTON_MENU_NOTE));
        questionLayout.setOnClickListener(new OnclickListenerImpl(BUTTON_MENU_ASK));
        downloadLayout.setOnClickListener(new OnclickListenerImpl(BUTTON_MENU_DOWNLOAD));
        closeLayout.setOnClickListener(new OnclickListenerImpl(BUTTON_MENU_CLOSED));
        // 半屏事件监听
        sSeekbar.setOnSeekBarChangeListener(onSeekBarChangeListenerImpl);
        splayBtn.setOnClickListener(new OnclickListenerImpl(BUTTON_PLAY));
        szoomBtn.setOnClickListener(new OnclickListenerImpl(BUTTON_ZOOM));
        // 全屏事件监听
        zSeekbar.setOnSeekBarChangeListener(onSeekBarChangeListenerImpl);
        zplayBtn.setOnClickListener(new OnclickListenerImpl(BUTTON_PLAY));
        zsoundBtn.setOnClickListener(new OnclickListenerImpl(BUTTON_SOUND));
        if (AuthorityManager.getInstance().isStudentAuthority()) {
            zmenuBtn.setOnClickListener(new OnclickListenerImpl(BUTTON_MENU));
        } else {
            zmenuBtn.setVisibility(View.GONE);
        }
        zpreBtn.setOnClickListener(new OnclickListenerImpl(BUTTON_PRE));
        znextBtn.setOnClickListener(new OnclickListenerImpl(BUTTON_NEXT));
        topBackBtn.setOnClickListener(new OnclickListenerImpl(BUTTON_BACK));
        fullMask.setOnClickListener(new OnclickListenerImpl(FULL_SCREEN_MASK));
    }

    /**
     * 视频视图触屏事件
     */
    OnTouchListener onTouchListenerImpl = new OnTouchListener() {

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            
            if (!spf.getBoolean("VideoViewMaskHasShown", false) && isZoom) {//显示遮罩层
                //videoView.pause();
                //显示用户引导操作遮罩
                //fullMask.setVisibility(View.VISIBLE);
                editor.putBoolean("VideoViewMaskHasShown", true);
                editor.commit();
            }
            showVideoBar();
            return true;
        }
    };
    /**
     * 视频缓冲更新
     */
    OnBufferingUpdateListener onBufferingUpdateListenerImpl = new OnBufferingUpdateListener() {

        @Override
        public void onBufferingUpdate(MediaPlayer mp, int percent) {
            if (percent <= 100) {
                topBufferTv.setText(percent + "%");
                loadingTv.setText(percent + "%");
            }
        }
    };
    /**
     * 视频加载过程
     */
    OnInfoListener onInfoListenerImpl = new OnInfoListener() {

        @Override
        public boolean onInfo(MediaPlayer mp, int what, int extra) {
            Log.e("CourseDetailActivity", "onInfo");
            switch (what) {
                case MediaPlayer.MEDIA_INFO_BUFFERING_START:
                    System.out.println("MEDIA_INFO_BUFFERING_START");
                    videoView.pause();
                    if (!isZoom) {
                        centerLayout.setVisibility(View.VISIBLE);
                    } else {
                        topBufferTv.setVisibility(View.VISIBLE);
                    }
                    break;
                case MediaPlayer.MEDIA_INFO_BUFFERING_END:
                    topBufferTv.setText("0%");
                    loadingTv.setText("0%");
                    centerLayout.setVisibility(View.GONE);
                    topBufferTv.setVisibility(View.GONE);
                    if (isPause()) {
                        videoView.start();
                    }
                    break;
                case MediaPlayer.MEDIA_INFO_DOWNLOAD_RATE_CHANGED:
                    if (isZoom) {
                        zdownloadSpeedTv.setText(extra + "kb/s");
                    }
                    break;
            }
            return true;
        }
    };
    /**
     * 视频预处理完成调用
     */
    OnPreparedListener onPreparedListenerImpl = new OnPreparedListener() {

        @Override
        public void onPrepared(MediaPlayer mp) {
            Log.e("CourseDetailActivity", "onPreparedListenerImpl");
            if (finishedView.isShown()) {
                finishedView.setVisibility(View.GONE);
            }
            videoDuration = videoView.getDuration();
            ztotalTimeTv.setText(StringUtils.generateTime(videoDuration));
            if (isComeBack) {
                isComeBack = false;
                videoView.seekTo(currentPosition);
            } else {
                videoView.seekTo(playHistory);
            }
            /*currentRateTv.setVisibility(View.GONE);*/
            centerLayout.setVisibility(View.GONE);
            zplayBtn.setImageResource(R.drawable.mediacontroller_pause);
            splayBtn.setImageResource(R.drawable.mediacontroller_pause);
            showVideoBar();
            requestKnowledges();
            registerListener();
            setProgress();
            /*if (local != 0) {
                isZoom = true;
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            }*/
        }
    };
    /**
     * 视频播放完成调用
     */
    OnCompletionListener onCompletionListenerImpl = new OnCompletionListener() {

        @Override
        public void onCompletion(MediaPlayer mp) {
            if (isZoom) {
                getFinishedView();
            }
        }
    };
    private CoursewareList finishedCoursewareView = null;
    /**
     * 播放完成视图
     */
    private void getFinishedView() {
        finishedView.setVisibility(View.VISIBLE);
        TextView tv = (TextView) findViewById(R.id.video_finished_title_tv);
        tv.setText(videoName);
        RelativeLayout layout = (RelativeLayout) finishedView.findViewById(R.id.video_finished_courseware_rl);
        if (finishedCoursewareView == null) {
            finishedCoursewareView = new CoursewareList(this, course_id, course_name);
            layout.addView(finishedCoursewareView);
        }
        findViewById(R.id.video_finished_back_btn).setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                finishedView.setVisibility(View.GONE);
            }
        });
        findViewById(R.id.video_finished_replay_btn).setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                videoView.seekTo(0);
                if (isPause()) {
                    videoView.start();
                    splayBtn.setImageResource(R.drawable.mediacontroller_pause);
                    zplayBtn.setImageResource(R.drawable.mediacontroller_pause);
                }
                setProgress();
                finishedView.setVisibility(View.GONE);
            }
        });
        findViewById(R.id.video_finished_continue_play_iv).setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                finishedCoursewareView.playNextVideo(videoId);
                finishedView.setVisibility(View.GONE);
            }
        });

    }

    /**
     * seekbar事件处理
     */
    OnSeekBarChangeListener onSeekBarChangeListenerImpl = new OnSeekBarChangeListener() {

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            isSeeking = false;
            tipsTv.setVisibility(View.GONE);
            videoView.seekTo(videoDuration * seekBar.getProgress() / 1000);
            if (isPause()) {
                videoView.start();
                splayBtn.setImageResource(R.drawable.mediacontroller_pause);
                zplayBtn.setImageResource(R.drawable.mediacontroller_pause);
            }
            if (finishedView.isShown()) {
                finishedView.setVisibility(View.GONE);
            }
            setProgress();
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            videoView.pause();
            showVideoBar();
            isSeeking = true;
        }

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (isSeeking) {
                videoView.pause();
                showVideoBar();
                tipsTv.setVisibility(View.VISIBLE);
                if (knowledges.size() != 0) {
                    for (int i = 0; i < knowledges.size(); i++) {
                        if (videoDuration * progress / 1000 - knowledges.get(i).time * 1000 < 30000
                                && videoDuration * progress / 1000 - knowledges.get(i).time * 1000 > 0) {
                            tipsTv.setText(knowledges.get(i).knowledge);
                            break;
                        } else {
                            tipsTv.setText(StringUtils.generateTime(videoDuration * progress / 1000));
                        }
                    }
                } else {
                    tipsTv.setText(StringUtils.generateTime(videoDuration * progress / 1000));
                }
            } else {
                tipsTv.setVisibility(View.GONE);
            }
        }
    };

    /**
     * 是否暂停
     */
    private boolean isPause() {
        return videoView.isPlaying() ? false : true;
    }

    // private void requestOnlinePeople() {
    // Map<String, Object> map = new HashMap<String, Object>();
    // map.put("coursewareid", course_id);
    // cacheHttp.asyncRequestWithCache(Constants.HOST
    // + "courseware/statis.html", map,
    // new RequestWithCacheCallBack<Integer>() {
    //
    // @Override
    // public Integer onPreRequestCache(String cache)
    // throws Exception {
    // return onlineJSONParsing(cache);
    // }
    //
    // @Override
    // public void onPreRequestSuccess(Integer data) {
    // peopleTv.setText("当前" + data + "人在线");
    // peopleTv.setVisibility(View.VISIBLE);
    // handler.sendEmptyMessageDelayed(MESSAGE_ONLINE,
    // TOUCH_TIME);
    // }
    //
    // @Override
    // public Integer onReqestSuccess(String respones)
    // throws Exception {
    // return onlineJSONParsing(respones);
    // }
    //
    // @Override
    // public void success(Integer cacheRespones,
    // Integer newRespones) {
    // peopleTv.setText("当前" + newRespones + "人在线");
    // peopleTv.setVisibility(View.VISIBLE);
    // handler.sendEmptyMessageDelayed(MESSAGE_ONLINE,
    // TOUCH_TIME);
    // }
    //
    // @Override
    // public void exception(Exception exception) {
    //
    // }
    // });
    // }
    // private int onlineJSONParsing(String result) {
    // int online = 0;
    // try {
    // JSONObject object = new JSONObject(result);
    // online = object.getJSONObject("res").getJSONObject("data")
    // .getInt("online");
    // } catch (JSONException e) {
    // e.printStackTrace();
    // }
    // return online;
    //
    // }
    /**
     * 设置视图知识锚点
     */
    private void setKnowledgesView(SeekBar seekBar, LinearLayout layout) {
        int[] margins = new int[knowledges.size()];
        layout.removeAllViews();
        for (int i = 0; i < knowledges.size(); i++) {
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(1, Helper.dip2px(this, 10));
            ImageView knowledgeIv = new ImageView(this);
            knowledgeIv.setBackgroundDrawable(getResources().getDrawable(R.color.video_point));
            int margin = (int) (seekBar.getWidth() * knowledges.get(i).time * 1000 / videoDuration);
            margins[i] = margin;
            if (i > 0) {
                params.leftMargin = margins[i] - margins[i - 1] - 1;
            } else {
                params.leftMargin = margin;
            }
            layout.addView(knowledgeIv, params);
        }
    }
    
    private void requestCoursewareList() {
        String url = Constants.COURSEWARE_LIST ; 
        Map<String ,Object> map = new HashMap<String,Object>();
        map.put("courseid", course_id);
        map.put("pageNum", pageNum);
        map.put("pageSize", pageSize);
        map.put("userid", App.getUserId());

        TmingHttp.asyncRequest(url, map, new RequestCallback<List<Video>>() {

            @Override
            public List<Video> onReqestSuccess(String respones) throws Exception {
                return ParseCoursewareList(respones);
            }

            @Override
            public void success(List<Video> respones) {
                if (respones != null && respones.size() > 0) {
                    Video video = respones.get(0);
                    videoId = video.video_id;
                    requestVideoDetail();
                }
            }

            @Override
            public void exception(Exception exception) {
                
            }
        });
    
    }
    
    /**
     * 解析课件列表数据
     */
    private List<Video> ParseCoursewareList(String result){
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
                vedio.course_id = course_id;
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
     * 请求课件详情接口
     */
    private void requestVideoDetail() {
        TmingHttp.asyncRequest(ServerApi.getCoursewareDetailUrl(videoId), null, new RequestCallback<CourseDetail>() {

            @Override
            public CourseDetail onReqestSuccess(String respones) throws Exception {
                return CourseDetail.JSONPasring(respones);
            }

            @Override
            public void success(CourseDetail respones) {
                if (respones == null) {
                    Log.e("CourseDetailActivity", getResources().getString(R.string.course_get_url_failed));
                    return;
                }
                handleCoursewareDetail(respones);
            }

            @Override
            public void exception(Exception exception) {
                exception.printStackTrace();
            }
        });
    }
    
    private void handleCoursewareDetail(CourseDetail respones) {
        courseDetail = respones;
        videoUrl = respones.url;
        videoName = respones.name;
        if (isFromList) {
            playHistory = 1000 * courseDetail.last_point;
            /*if (playHistory == 0) {
                currentRateTv.setText(getResources().getString(R.string.ready_to_play));
            } else {
                currentRateTv.setText(getString(R.string.current_play_rate1)
                        + StringUtils.generateTime(playHistory) + getString(R.string.current_play_rate2));
            }*/
            return;
        }

        String proportion = null;
        playHistory = 1000 * courseDetail.last_point;
        Log.e("CourseDetailActivity", "" + playHistory);
        centerPlayBtn.setOnClickListener(new OnclickListenerImpl(BUTTON_CENTER_PLAY, videoUrl));
        String mVideoName = videoName == null ? "" : videoName;
        if (!mVideoName.equals("")) {
            scoursenameTv.setText(getResources().getString(R.string.click_play) + "《" + mVideoName + "》");
        } else {
            scoursenameTv.setText("");
        }

        /*
        if (playHistory == 0) {
            currentRateTv.setText(getResources().getString(R.string.ready_to_play));
        } else {
            currentRateTv.setText(getString(R.string.current_play_rate1)
                    + StringUtils.generateTime(playHistory) + getString(R.string.current_play_rate2));
        }
        */

        if (progress_list != null && progress_list.size() != 0) {
            //做作业按钮上的信息
            proportion = progress_list.get(0) + "/" + progress_list.get(1);
        }
        // -------------------------------发送广播到详情页布局---------------------------------------
        Intent intent = new Intent();
        intent.setAction(Constants.COURSE_DETAIL_ACTION);
        intent.putExtra("proportion", proportion); // 已完成的作业比例
        intent.putExtra("time", "2"); // 表示第二次
        sendBroadcast(intent);
        Log.e("CourseDetailActivity","===========发送广播到详情页布局==========");
        // -----------------------------------------------------------------------
    }
    
    /**
     * 请求知识点接口
     */
    private void requestKnowledges() {
        Map<String, Object> postParams = new HashMap<String, Object>();
        postParams.put("coursewareid", videoId);
        cacheHttp.asyncRequestWithCache(Constants.HOST + "courseware/knowledges.html", postParams, knowledgeCallback);
    }
    
    private RequestWithCacheCallBackV2Adapter<List<KnowledgeInfo>> knowledgeCallback = new RequestWithCacheCallBackV2Adapter<List<KnowledgeInfo>>() {
        
        @Override
        public List<KnowledgeInfo> parseData(String data) throws Exception {
            return knowledgesJSONParsing(data);
        }

        @Override
        public void cacheDataRespone(List<KnowledgeInfo> data) {
            knowledges = data;
            setKnowledgesView(sSeekbar, sknowledgesLayout);
        }

        @Override
        public void requestNewDataRespone(List<KnowledgeInfo> cacheRespones, List<KnowledgeInfo> newRespones) {
            knowledges = newRespones;
            setKnowledgesView(sSeekbar, sknowledgesLayout);
        }
    };

    /**
     * 知识点数据解析
     */
    private List<KnowledgeInfo> knowledgesJSONParsing(String result) {
        List<KnowledgeInfo> knowledgeInfos = new ArrayList<KnowledgeInfo>();
        try {
            JSONObject obj = new JSONObject(result);
            if (!obj.getJSONObject("res").isNull("data")) {
                JSONArray list = obj.getJSONObject("res").getJSONObject("data").getJSONArray("list");
                for (int i = 0; i < list.length(); i++) {
                    JSONObject item = list.getJSONObject(i);
                    KnowledgeInfo info = new KnowledgeInfo();
                    info.knowledge = item.getString("title");
                    info.time = item.getInt("knowledges_time");
                    info.imageUrl = item.getString("image");
                    knowledgeInfos.add(info);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Collections.sort(knowledgeInfos, new Comparator<KnowledgeInfo>() {

            @Override
            public int compare(KnowledgeInfo lhs, KnowledgeInfo rhs) {
                return lhs.time.compareTo(rhs.time);
            }
        });
        return knowledgeInfos;
    }
    
    private IDownloadServiceApi mServiceApi;
    /**
     * 播放视频(本地、网络)
     */
    private void playVideo(int which) {
        isVideoPlay = true;
        videoView.setOnCompletionListener(onCompletionListenerImpl);
        switch (which) {
            case VIDEO_NET:
                videoView.setVideoURI(Uri.parse(videoUrl));
                videoView.setOnInfoListener(onInfoListenerImpl);
                videoView.setOnBufferingUpdateListener(onBufferingUpdateListenerImpl);
                break;
            case VIDEO_LOCAL:
                if (mServiceApi == null) {
                    mServiceApi = mBottomLayout.getCouseWareListView().getServiceApi();
                }
                String localVideoPath = mServiceApi.getDownloadedFile(videoUrl).getAbsolutePath();
                videoView.setVideoPath(localVideoPath);
                zdownloadSpeedTv.setVisibility(View.GONE);
                break;
        }
        videoView.setOnPreparedListener(onPreparedListenerImpl);
    }

    /**
     * 横竖屏切换执行
     */
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE && isZoom) { // 横屏
            setVideoScreen(VIDEO_ZOOM);
        } else if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT && !isZoom) { // 竖屏
            setVideoScreen(VIDEO_SMALL);
        } else {
            return;
        }
        if (videoView != null) {
            videoView.setVideoLayout(VideoView.VIDEO_LAYOUT_SCALE, 0);
        }
    }

    /**
     * 切换屏幕操作
     */
    private void setVideoScreen(int which) {
        LayoutParams params = (LayoutParams) videoLayout.getLayoutParams();
        switch (which) {
            case VIDEO_SMALL:
                topBarLayout.setVisibility(View.GONE);
                headerLayout.setVisibility(View.VISIBLE);
                fullMask.setVisibility(View.GONE);
                params.height = Helper.dip2px(CourseDetailActivity.this, 210);
                videoLayout.setLayoutParams(params);
                zbarView.setVisibility(View.GONE);
                menuLayout.setVisibility(View.GONE);
                topTitleTv.setText(course_name);
                isZoom = false;
                break;
            case VIDEO_ZOOM:
                // showOnlinePeople();
                topTitleTv.setText(videoName);
                headerLayout.setVisibility(View.GONE);
                params.height = LayoutParams.FILL_PARENT;
                videoLayout.setLayoutParams(params);
                sbarView.setVisibility(View.GONE);
                isZoom = true;
                break;
        }
    }

    /**
     * 显示视频控制栏
     */
    private void showVideoBar() {
        if (menuLayout.isShown()) {
            clearStatus();
            isDetail = true;
            LayoutParams params = new LayoutParams(menuLayout.getLayoutParams());
            if (menuMargin == 0) {
                menuMargin = menuLayout.getLeft();
            }
            params.setMargins(menuMargin, 0, 0, 0);
            menuLayout.setLayoutParams(params);
            menuLayout.setVisibility(View.GONE);
            showVideoBar();
            return;
        }
        topBarLayout.setVisibility(View.VISIBLE);
        if (isZoom) {
            zbarView.setVisibility(View.VISIBLE);
            if (zSeekbar.getWidth() != 0 && isFirstZoom) {
                setKnowledgesView(zSeekbar, zknowledgesLayout);
                isFirstZoom = false;
            }
        } else {
            topBarLayout.setVisibility(View.GONE);
            sbarView.setVisibility(View.VISIBLE);
            /*headerLayout.setVisibility(View.VISIBLE);*/
        }
        handler.removeMessages(MESSAGE_TOUCH);
        handler.sendEmptyMessageDelayed(MESSAGE_TOUCH, TOUCH_TIME);
    }

    /**
     * 设置播放进度
     */
    private void setProgress() {
        new Thread() {

            public void run() {
                super.run();
                while (true) {
                    if (!isPause()) {
                        handler.post(refreshUI);
                        for (int i = 0; i < knowledges.size(); i++) {
                            if (videoView.getCurrentPosition() < knowledges.get(i).time * 1000) {
                                if (i > 0) {
                                    Intent it = new Intent("com.tming.kd.knowledge");
                                    it.putExtra("setKnowledge", i - 1);
                                    CourseDetailActivity.this.sendBroadcast(it);
                                    break;
                                }
                            }
                        }
                        if (knowledges.size() != 0 && videoView.getCurrentPosition() > knowledges.get(knowledges.size() - 1).time * 1000) {
                            Intent it = new Intent("com.tming.kd.knowledge");
                            it.putExtra("setKnowledge", knowledges.size() - 1);
                            CourseDetailActivity.this.sendBroadcast(it);
                        }
                    }
                    try {
                        sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            };
        }.start();
    }

    /**
     * 播放进度刷新UI线程
     */
    Runnable refreshUI = new Runnable() {

        @Override
        public void run() {
            sSeekbar.setProgress((int) (1000 * videoView.getCurrentPosition() / videoDuration));
            zSeekbar.setProgress((int) (1000 * videoView.getCurrentPosition() / videoDuration));
            zcurrentTimeTV.setText(StringUtils.generateTime(videoView.getCurrentPosition()));
            if (videoView.isPlaying()) {
                zplayBtn.setImageResource(R.drawable.mediacontroller_pause);
                splayBtn.setImageResource(R.drawable.mediacontroller_pause);
            }
        }
    };

    /**
     * 展开菜单
     */
    private void showMenu() {
        zbarView.setVisibility(View.GONE);
        topBarLayout.setVisibility(View.GONE);
        menuLayout.setVisibility(View.VISIBLE);
    }

    /**
     * 菜单视图动画特效
     */
    private void menuViewAnimation() {
        if (!isDetail) {
            return;
        }
        TranslateAnimation animation = new TranslateAnimation(0, -Helper.dip2px(this, 190), 0, 0);
        animation.setDuration(500);// 设置动画持续时间
        animation.setFillAfter(true);// 动画执行完后是否停留在执行完的状态
        animation.setAnimationListener(new AnimationListener() {

            @Override
            public void onAnimationStart(Animation animation) {}

            @Override
            public void onAnimationRepeat(Animation animation) {}

            @Override
            public void onAnimationEnd(Animation animation) {
                menuMargin = menuLayout.getLeft();
                menuLayout.clearAnimation();
                // menuDetailLayout.setVisibility(View.VISIBLE);
                // menuDetailLayout.addView(new
                // MenuDownloadView(CourseDetailActivity.this));
                // menuDetailLayout.addView(new
                // MenuListView(CourseDetailActivity.this, knowledges));
                LayoutParams params = new LayoutParams(menuLayout.getLayoutParams());
                params.setMargins(menuMargin - Helper.dip2px(CourseDetailActivity.this, 190), 0, 0, 0);
                menuLayout.setLayoutParams(params);
                isDetail = false;
            }
        });
        menuLayout.startAnimation(animation);
    }

    /**
     * 菜单视图动画特效
     */
    private void menuViewAnimation(int timeInterval) {
        if (!isDetail) {
            return;
        }
        TranslateAnimation animation = new TranslateAnimation(0, -Helper.dip2px(this, 190), 0, 0);
        animation.setDuration(timeInterval);// 设置动画持续时间
        animation.setFillAfter(true);// 动画执行完后是否停留在执行完的状态
        animation.setAnimationListener(new AnimationListener() {

            @Override
            public void onAnimationStart(Animation animation) {}

            @Override
            public void onAnimationRepeat(Animation animation) {}

            @Override
            public void onAnimationEnd(Animation animation) {
                menuMargin = menuLayout.getLeft();
                menuLayout.clearAnimation();
                // menuDetailLayout.setVisibility(View.VISIBLE);
                // menuDetailLayout.addView(new
                // MenuDownloadView(CourseDetailActivity.this));
                // menuDetailLayout.addView(new
                // MenuListView(CourseDetailActivity.this, knowledges));
                LayoutParams params = new LayoutParams(menuLayout.getLayoutParams());
                params.setMargins(menuMargin - Helper.dip2px(CourseDetailActivity.this, 190), 0, 0, 0);
                menuLayout.setLayoutParams(params);
                isDetail = false;
            }
        });
        menuLayout.startAnimation(animation);
    }

    /**
     * 显示在线人数
     */
    // private void showOnlinePeople() {
    // requestOnlinePeople();
    // }
    /**
     * 按钮事件
     */
    private class OnclickListenerImpl implements OnClickListener {

        private int which;
        private String url;

        public OnclickListenerImpl(int which) {
            this.which = which;
        }

        public OnclickListenerImpl(int which, String url) {
            this.which = which;
            this.url = url;
        }

        @Override
        public void onClick(View v) {
            long currentTime = videoView.getCurrentPosition();
            switch (which) {
                case BUTTON_PLAY:
                    showVideoBar();
                    if (isPause()) {
                        zplayBtn.setImageResource(R.drawable.mediacontroller_pause);
                        splayBtn.setImageResource(R.drawable.mediacontroller_pause);
                        videoView.start();
                    } else {
                        zplayBtn.setImageResource(R.drawable.mediacontroller_play);
                        splayBtn.setImageResource(R.drawable.mediacontroller_play);
                        videoView.pause();
                    }
                    break;
                case BUTTON_ZOOM:
                    isZoom = true;
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                    break;
                case BUTTON_SOUND:
                    showVideoBar();
                    AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
                    int currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, currentVolume, AudioManager.FLAG_SHOW_UI);
                    break;
                case BUTTON_PRE:
                    showVideoBar();
                    if (currentTime - SET_TIME >= 0) {
                        videoView.seekTo(currentTime - SET_TIME);
                    } else {
                        videoView.seekTo(0);
                    }
                    if (isPause()) {
                        videoView.start();
                        splayBtn.setImageResource(R.drawable.mediacontroller_pause);
                        zplayBtn.setImageResource(R.drawable.mediacontroller_pause);
                    }
                    break;
                case BUTTON_NEXT:
                    showVideoBar();
                    if (currentTime + SET_TIME <= videoDuration) {
                        videoView.seekTo(currentTime + SET_TIME);
                    } else {
                        videoView.seekTo(videoDuration);
                    }
                    if (isPause()) {
                        videoView.start();
                        splayBtn.setImageResource(R.drawable.mediacontroller_pause);
                        zplayBtn.setImageResource(R.drawable.mediacontroller_pause);
                    }
                    break;
                case BUTTON_MENU:
                    showMenu();
                    break;
                case BUTTON_BACK:
                    isZoom = false;
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                    if (finishedView.isShown()) {
                        finishedView.setVisibility(View.GONE);
                    }
                    break;
                case BUTTON_MENU_LIST:
                    clearStatus();
                    menuListView = new MenuListView(CourseDetailActivity.this, knowledges, course_id, videoId);
                    menuDetailLayout.removeAllViews();
                    menuDetailLayout.addView(menuListView);
                    listIv.setImageResource(R.drawable.classroom_icon_kj_catalogue_on);
                    listTv.setTextColor(0xFF23bd8b);
                    menuViewAnimation();
                    break;
                // case BUTTON_MENU_DISCUSS:
                // clearStatus();
                // discussIv
                // .setImageResource(R.drawable.classroom_icon_kj_discuss_on);
                // discussTv.setTextColor(Color.parseColor("#23bd8b"));
                // menuViewAnimation();
                // break;
                case BUTTON_MENU_ASK:
                    clearStatus();
                    if (menuQuestionView == null) {
                        menuQuestionView = new MenuQuestionView(CourseDetailActivity.this, course_id, course_name);
                    }
                    menuDetailLayout.removeAllViews();
                    menuDetailLayout.addView(menuQuestionView);
                    menuQuestionView.refresh();
                    questionIv.setImageResource(R.drawable.classroom_icon_kj_question_on);
                    questionTv.setTextColor(Color.parseColor("#23bd8b"));
                    menuViewAnimation();
                    break;
                case BUTTON_MENU_NOTE:
                    clearStatus();
                    menuDetailLayout.removeAllViews();
                    if (noteView == null) {
                        noteView = new MenuNoteView(CourseDetailActivity.this, course_id, videoId,
                                CourseDetailActivity.this);
                    }
                    menuDetailLayout.removeAllViews();
                    menuDetailLayout.addView(noteView);
                    noteIv.setImageResource(R.drawable.classroom_icon_kj_note_on);
                    noteTv.setTextColor(0xFF23bd8b);
                    menuViewAnimation();
                    break;
                case BUTTON_MENU_DOWNLOAD:
                    if (menuDownloadView == null) {
                        DownloadInfo downloadInfo = new DownloadInfo();
                        downloadInfo.title = videoName;
                        downloadInfo.url = videoUrl;
                        downloadInfo.coursecateid = course_id.hashCode();
                        downloadInfo.coursecatename = course_name;
                        downloadInfo.courseId = course_id;
                        downloadInfo.videoId = videoId;
                        menuDownloadView = new MenuDownloadLayout(CourseDetailActivity.this, downloadInfo);
                    }
                    menuDetailLayout.removeAllViews();
                    menuDetailLayout.addView(menuDownloadView);
                    clearStatus();
                    downloadIv.setImageResource(R.drawable.classroom_icon_kj_download_on);
                    downloadTv.setTextColor(Color.parseColor("#23bd8b"));
                    menuViewAnimation();
                    break;
                case BUTTON_MENU_CLOSED:
                    clearStatus();
                    isDetail = true;
                    LayoutParams params = new LayoutParams(menuLayout.getLayoutParams());
                    if (menuMargin == 0) {
                        menuMargin = menuLayout.getLeft();
                    }
                    params.setMargins(menuMargin, 0, 0, 0);
                    menuLayout.setLayoutParams(params);
                    menuLayout.setVisibility(View.GONE);
                    showVideoBar();
                    break;
                case BUTTON_CENTER_PLAY:
                    if (url == null || url.equals("")) {
                        Toast.makeText(CourseDetailActivity.this, getResources().getString(R.string.null_path),
                                Toast.LENGTH_SHORT).show();
                        return;
                    }
                    focusCurrentVideo();
                    setVideoState(BUFFERING);
                    checkVitamio();
                    break;
                case BUTTON_HEAD_BACK:
                    Log.e("back", "返回");
                    noteView = null;
                    finish();
                    break;
                case BUTTON_HEAD_HOMEWORK:
                    UIHelper.showHomeworkListActivity(CourseDetailActivity.this, course_id);
                    break;
                case FULL_SCREEN_MASK:
                    fullMask.setVisibility(View.GONE);
                    videoView.start();
                    break;
            }
        }
    }

    private void clearStatus() {
        listIv.setImageResource(R.drawable.classroom_icon_kj_catalogue);
        listTv.setTextColor(0xFF888888);
        noteIv.setImageResource(R.drawable.classroom_icon_kj_note);
        noteTv.setTextColor(0xFF888888);
        questionIv.setImageResource(R.drawable.classroom_icon_kj_question);
        questionTv.setTextColor(0xFF888888);
        downloadIv.setImageResource(R.drawable.classroom_icon_kj_download);
        downloadTv.setTextColor(0xFF888888);
    }

    @Override
    protected void onDestroy() {
        unBindService();
        if (videoView != null && videoView.isPlaying()) {
            videoView.stopPlayback();
        }
        mBottomLayout.onActivityDestroy();
        unregisterReceiver(receiver);
        mScreenObserver.stopScreenStateUpdate();
        if (noteView != null) {
            noteView.onDestroy();
        }
        super.onDestroy();
    }
    
    private void unBindService() {
        if (mServiceApi != null) {
            mServiceApi.destroy();
        }
        if (mServiceConnection != null) {
            unbindService(mServiceConnection);
        }
    }
    
    /**
     * 显示在做作业按钮上的数字
     */
    private void requestData() {
        String url = Constants.COURSE_DETAIL_URL;
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("userid", user_id);
        map.put("courseid", course_id);
        TmingHttp.asyncRequest(url, map, new RequestCallback<Video>() {

            @Override
            public Video onReqestSuccess(String respones) throws Exception {
                return jsonParsing(respones);
            }

            @Override
            public void success(Video respones) {
                loadData(respones);
            }

            @Override
            public void exception(Exception exception) {
                exception.printStackTrace();
            }
            
        });
    }

    /**
     * 解析数据（目前只有做作业）
     * 
     * @param result
     * @return
     */
    private Video jsonParsing(String result) {
        Video video = new Video();
        int flag = 0;
        try {
            progress_list = new ArrayList<String>();
            JSONObject jobj = new JSONObject(result);
            flag = jobj.getInt("flag");
            if (flag != 1) {
                App.dealWithFlag(flag);
                return null;
            }
            JSONObject infoObj = jobj.getJSONObject("res").getJSONObject("data").getJSONObject("info");
            if (infoObj != null && !infoObj.isNull("is_link")) {
                int is_link = infoObj.getInt("is_link");
                // 是否为在修课程
                Intent intent = new Intent();
                intent.setAction(Constants.COURSE_DETAIL_ACTION);
                intent.putExtra("is_link", is_link); // 是否选修
                sendBroadcast(intent);
            }
            JSONObject dataObj = jobj.getJSONObject("homeworkprogress").getJSONObject("data");
            JSONArray examArr = dataObj.getJSONArray("exams");
            progress_list.add(examArr.getString(0)); // 已做
            progress_list.add(examArr.getString(1)); // 全部
            if (jobj.isNull("show_courseware")) {
                return null;
            }
            JSONObject coursewareObj = jobj.getJSONObject("show_courseware"); // 小屏上的课件信息
            video.course_id = course_id;
            video.course_name = course_name;
            if (coursewareObj.isNull("coursewareid")) {
                video.video_id = "";
            } else {
                video.video_id = coursewareObj.getString("coursewareid");
            }
            video.desc = coursewareObj.getString("desc");
            video.video_name = coursewareObj.getString("name");
            if (coursewareObj.isNull("url")) {
                video.video_url = "";
            } else {
                video.video_url = coursewareObj.getString("url");
            }
            Log.e("video_url", "video_url=" + video.video_url);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return video;
    }

    private Video first_video;

    private void loadData(Video video) {
        if (video == null) {
            return;
        }
        videoId = video.video_id;
        if (local == 0) {
            videoUrl = video.video_url;
            videoName = video.video_name;
        }
    }

    /**
     * 播放进度同步接口
     * 
     * @param rate
     */
    private void sumbitPlayPosition(long rate) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("userid", App.getUserId());
        params.put("fs_id", courseDetail.fs_id);
        params.put("stat_id", courseDetail.stat_id);
        params.put("last_point", rate / 1000);
        params.put("stay_time", rate);

        TmingHttp.asyncRequest(Constants.HOST + "/courseware/recordPosition.html", params, submitPlayPositionCallback);
    }
    
    private RequestCallback<JSONObject> submitPlayPositionCallback = new RequestCallback<JSONObject>() {
        
        @Override
        public void success(JSONObject respones) {
            // 发送广播刷新播放历史记录页面
            Intent intent = new Intent("com.tming.sumbitPlayPosition");
            Helper.sendLocalBroadCast(CourseDetailActivity.this, intent);
        }
        
        @Override
        public JSONObject onReqestSuccess(String respones) throws Exception {
            return new JSONObject(respones);
        }
        
        @Override
        public void exception(Exception exception) {
        }
    };

    /**
     * 请求课件详情接口
     */
    private void requestVideoDetail(final String videoId) {
        if (mRequestCourseDetailTask != null) {
            mRequestCourseDetailTask.cancel();
        }
        mRequestCourseDetailTask = TmingHttp.asyncRequest(ServerApi.getCoursewareDetailUrl(videoId), new RequestCallback<CourseDetail>() {

            @Override
            public CourseDetail onReqestSuccess(String respones) throws Exception {
                return CourseDetail.JSONPasring(respones);
            }

            @Override
            public void success(CourseDetail recCourseDetail) {
                if (recCourseDetail == null) {
                    Log.e("CourseDetailActivity", getResources().getString(R.string.course_get_url_failed));
                    return;
                }
                mBottomLayout.getCouseWareListView().setCurrentPlayVideoId(videoId,false);
                if (finishedCoursewareView != null) {
                    finishedCoursewareView.setCurrentPlayVideoId(videoId,false);
                }
                noteView = null;
                menuDownloadView = null;
                menuListView = null;
                Log.e("CourseDetailActivity", "" + videoName);
                local = 0;
                Log.e("CourseDetailActivity" , "接收的视频路径  == " + videoUrl);
                if (courseDetail != null && isVideoPlay) {
                    sumbitPlayPosition(videoView.getCurrentPosition());
                }
                if (videoView.isPlaying() || videoView != null) {
                    videoView.stopPlayback();
                }
                
                if (sbarView.isShown()) {
                    sbarView.setVisibility(View.GONE);
                }
                handleCoursewareDetail(recCourseDetail);
                topTitleTv.setText(videoName == null ? "" : videoName);
                isFirstZoom = true;
                isVideoPlay = false;
                checkVitamio();
            }

            @Override
            public void exception(Exception exception) {
                Helper.ToastUtil(CourseDetailActivity.this, getResources().getString(R.string.course_get_url_failed));
            }

        });
    }
    /**
     * <p>播放在线视频</P>
     * @param context
     * @param videoId
     */
    public static void playOnlineVideo(Context context, String videoId) {
        Intent intent = new Intent(Constants.VIDEO_ACTION);
        intent.putExtra("video_id", videoId);
        context.sendBroadcast(intent);
    }
    /**
     * <p>播放本地课件</P>
     * @param videoId
     * @param videoUrl
     * @param videoName
     */
    public static void playLocalVideo(String videoId,String videoUrl,String videoName) {
        Intent intent = new Intent("play.local.video");
        intent.putExtra("video_id", videoId);
        intent.putExtra("video_url", videoUrl);
        intent.putExtra("video_name", videoName);
        App.getAppContext().sendBroadcast(intent);
    }
    
    private final int PREPARING = 0;
    private final int BUFFERING = 1;
    private final int PLAYING = 2;
    /**
     * 设置视频状态表现
     * @param state
     */
    private void setVideoState(int state) {
        switch (state) {
            case PREPARING:
                scoursenameTv.setVisibility(View.VISIBLE);
                centerPlayBtn.setVisibility(View.VISIBLE);
                centerplayProgress.setVisibility(View.GONE);
                break;
            case BUFFERING:
                scoursenameTv.setVisibility(View.GONE);
                centerPlayBtn.setVisibility(View.GONE);
                centerplayProgress.setVisibility(View.VISIBLE);
                break;
            case PLAYING:
                scoursenameTv.setVisibility(View.GONE);
                centerPlayBtn.setVisibility(View.GONE);
                centerplayProgress.setVisibility(View.GONE);
                break;
            
        }
    }
    
    /**
     * 往小屏幕添加视频的广播
     */
    private BroadcastReceiver receiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Constants.VIDEO_ACTION)) {
                String tmpVideoId = intent.getStringExtra("video_id"); // 课件ID
                if (videoId.equals(tmpVideoId)) {
                    //已经完成加载，直接播放
                    setVideoState(BUFFERING);
                    checkVitamio();
                    return;
                } else {
                    videoId = tmpVideoId;
                }
                setVideoState(BUFFERING);
                requestVideoDetail(videoId);
            } else if (intent.getAction().equals("com.tming.videoview.play.pause")) {
                zplayBtn.setImageResource(R.drawable.mediacontroller_play);
                splayBtn.setImageResource(R.drawable.mediacontroller_play);
                videoView.pause();
            } else if (intent.getAction().equals("com.tming.videoview.play.resume")) {
                /*if (mScreenObserver.isScreenOn()) {
                    splayBtn.setImageResource(R.drawable.mediacontroller_pause);
                    zplayBtn.setImageResource(R.drawable.mediacontroller_pause);
                    videoView.start();
                }*/
            } else if (intent.getAction().equals("com.tming.videoview.note")) {
                Intent currentIt = new Intent();
                currentIt.setAction("com.tming.videoview.note.callback");
                currentIt.putExtra("videoTime", videoView.getDuration());
                Helper.sendLocalBroadCast(CourseDetailActivity.this, currentIt);
            } else if (intent.getAction().equals("com.tming.kd.seekto")) {
                Log.e("seekto", "seekto:" + intent.getIntExtra("seekto", 0));
                if (isPause()) {
                    splayBtn.setImageResource(R.drawable.mediacontroller_pause);
                    zplayBtn.setImageResource(R.drawable.mediacontroller_pause);
                }
                videoView.seekTo(intent.getIntExtra("seekto", 0) * 1000 + 15000);
            } else if (intent.getAction().equals("com.tming.videoview.list.setVideo")) {
                if (videoView.isPlaying()) {
                    videoView.stopPlayback();
                }
                menuDetailLayout.removeAllViews();
                menuListView = null;
                noteView = null;
                menuDownloadView = null;
                clearStatus();
                isDetail = true;
                LayoutParams params = new LayoutParams(menuLayout.getLayoutParams());
                if (menuMargin == 0) {
                    menuMargin = menuLayout.getLeft();
                }
                params.setMargins(menuMargin, 0, 0, 0);
                menuLayout.setLayoutParams(params);
                menuLayout.setVisibility(View.GONE);
                videoUrl = intent.getStringExtra("video_url");
                videoId = intent.getStringExtra("video_id");
                checkVitamio();
            } else if ("com.tming.openuniversity.updatePlayButtonVisible".equals(intent.getAction())) {
                boolean isHide = intent.getBooleanExtra("isHide", false);
                /*centerPlayBtn.setVisibility(isHide ? View.GONE : View.VISIBLE);*/
            } else if ("play.local.video".equals(intent.getAction())) {
                setVideoState(BUFFERING);
                videoId = intent.getStringExtra("video_id");
                videoUrl = intent.getStringExtra("video_url");
                videoName = intent.getStringExtra("video_name");
                if (PhoneUtil.checkNetworkEnable() == PhoneUtil.NETSTATE_ENABLE) {
                    requestVideoDetail(videoId);
                } else {
                    playVideo(VIDEO_LOCAL);
                }
                mBottomLayout.getCouseWareListView().setCurrentPlayVideoId(videoId,false);
            }
        }
    };

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && !isFinishing()) {
            if (isZoom) {
                isZoom = false;
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                if (finishedView.isShown()) {
                    finishedView.setVisibility(View.GONE);
                }
            } else {
                noteView = null;
                finish();
            }
        }
        return false;
    };

    @Override
    protected void onPause() {
        super.onPause();
        Log.e("CourseDetailActivity", "onPause");
        if (videoView != null) {
            currentPosition = videoView.getCurrentPosition();
        }
        if (courseDetail != null && isVideoPlay) {
            sumbitPlayPosition(currentPosition);
        }
        //发送广播 StudentCourseFragment 中接收，完成课程信息的刷新
        Intent intent=new Intent();
        intent.setAction(StudentCourseFragment.COURSEINFO_CHANGE);
        CourseDetailActivity.this.sendBroadcast(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isFirst) {
            isFirst = false;
            return;
        }
        if (videoView != null && isVideoPlay && !isLock) {
            isLock = false;
            loadingTv.setVisibility(View.VISIBLE);
            // loadingLayout.setVisibility(View.VISIBLE);
            Log.e("CourseDetailActivity", "onResume");
            isComeBack = true;
        }
    }
    
    
    public String getVideoId() {
        return videoId;
    }
}
