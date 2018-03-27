package com.nd.shapedexamproj.fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.*;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import android.widget.AdapterView.OnItemLongClickListener;

import com.nd.shapedexamproj.App;
import com.nd.shapedexamproj.AuthorityManager;
import com.nd.shapedexamproj.R;
import com.nd.shapedexamproj.activity.plaza.PlaygroundSendCommentActivity;
import com.nd.shapedexamproj.activity.plaza.PlaygroundSendTrendsActivity;
import com.nd.shapedexamproj.adapter.TimelineAdapter;
import com.nd.shapedexamproj.db.ChatUserInfoDao;
import com.nd.shapedexamproj.model.my.PersonalInfo;
import com.nd.shapedexamproj.model.playground.CommentLastTime;
import com.nd.shapedexamproj.model.playground.Timeline;
import com.nd.shapedexamproj.net.ServerApi;
import com.nd.shapedexamproj.util.Constants;
import com.nd.shapedexamproj.util.JsonUtil;
import com.nd.shapedexamproj.util.SharedPreferUtils;
import com.nd.shapedexamproj.util.StringUtils;
import com.nd.shapedexamproj.util.TimelineUtil;
import com.nd.shapedexamproj.util.UIHelper;
import com.nd.shapedexamproj.view.LinkView;
import com.tming.common.cache.image.ImageCacheTool;
import com.tming.common.cache.net.TmingCacheHttp;
import com.tming.common.net.TmingHttp;
import com.tming.common.net.TmingHttp.RequestCallback;
import com.tming.common.util.Log;
import com.tming.common.view.RefreshableListView;
import com.tming.common.view.RefreshableListView.OnRefreshListener;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @项目名称: OpenUniversity
 * @版本号 V1.1.0
 * @所在包: com.tming.openuniversity.fragment
 * @文件名: PlaygroundFragment
 * @文件描述: 操场 Fragment ,实现用户操场相关功能
 * @创建人: Linlg
 * @创建时间: 2014-4-21
 * @修改记录:
 *     1、Modified by xuwenzhuo On 2014/12/1
 *       修改：根据交互需求修改功能
 *
 * @Copyright: 2014 Tming All rights reserved.
 */

@SuppressLint("ValidFragment")
public class PlaygroundFragment extends BaseFragment {

    public static final int LOAD_HEAD_IMAGE_WH_DP = 50;  // 加载头像图片的大小
    public static final int CLASS_TYPE = 0, SCHOOL_TYPE = 1, COLLEGE_TYPE = 2;
    private static final  int LOAD_TYPE_REFRESH = 1;
    private static final  int LOAD_TYPE_MORE = 2;
    private static final int GET_USER_INFO = 10;     // 1表示获取用户信息
    private static final long INTERVAL_TIME = 30000;// 间隔30s

    private static final String TAG = "PlaygroundFragment";
    private static final String MAN_SAX_VAL = "0", WOMAN_SAX_VAL = "1";// 性别 0—男 1—女
    private static final String DEFAULT_AGE_VAL = "0";// 默认年龄
    private static final int PAGE_SIZE = 10;     // 每页记录数，不传默认10条记录
    private static final String CLASS_TYPE_VALUE = "0", SCHOOL_TYPE_VALUE = "1", COLLEGE_TYPE_VALUE = "2";  // 修改传递类型,列表类型，0-我的班级，1-我的教学点，2-开放大学

    private int mPageno = 1;// 第几页，不传默认第1页
    private int mLoadType = -1;
    private String mUserID = App.getUserId();// "1100000034";
    private String mClassTypeIdValue, mSchoolTypeIdValue, mCollegeTypeIdValue = "2";
    private String mLastOperateTime = "0";//记录最新的时间
    private String mTypeValue = COLLEGE_TYPE_VALUE;
    
    private View mPlaygroundView;
    private LinearLayout mSearchBarLL;
    private EditText mSearchEDT;
    private ImageView mMsgIV, mPostTimelineIV;
    private RefreshableListView mTimelineRLV;
    private TmingCacheHttp mCacheHttp;
    private ImageCacheTool mImageCacheTool;
    private ChatUserInfoDao chatUserInfoDao;
    private TimelineAdapter mImageItemAdapter;
    private TextView mAnnouncementTypeTV, mPlaygroundPersonInfoTV;// 类型
    private View mAnnouncementTypeView;// 类型视图
    private TextView mAnnouncementClassTV, mAnnouncementSchoolTV, mAnnouncementCollegeTV;// 我的班级、我的教学点、开发大学控件
    private LinearLayout mAnnouncementClassLL, mAnnouncementSchoolLL, mAnnouncementCollegeLL;
    private PopupWindow mAnnouncementPopupWindow;
    
    private RelativeLayout mAnnouncementTypeRL, mAnnouncementRL;// 泡泡参考物
    private RelativeLayout mPlaygroundHeadRL;// 头部搜索框
    private LinearLayout mCommentDialogLL, mPersonInfoDialogLL;// 弹窗中评论、查看个人信息
    private AlertDialog mDialog;
    private InputMethodManager mInputMM;// 软键盘管理类
    private View mLoadingView;
    private final int mRefreshListType = 1;// 用于判别是否发送刷新列表广播0-不发送 1-发送
    private boolean mOpenPopuwindow = false;
    private SharedPreferences mUserInfoSp;

    // 定义广播(用于接收大厅列表数据改变)
    private BroadcastReceiver mRefreshListBroadcastReceiver = new BroadcastReceiver() {

        public void onReceive(Context context, Intent intent) {
            String refreshListAction = intent.getAction();
            if (refreshListAction.equals(Constants.SEND_COMMENT_SUCCESS_REFRESH_LIST_ACTION)
                    || refreshListAction.equals(Constants.REFRESH_LIST)) {
                //refreshPlaygroundList();// 刷新列表
            }
        };
    };

    private boolean mStopFlag = false;// 停止线程
    private Thread mMyThread;
    Handler mHandler = new Handler();
    //刷新与我相关的计时器
    Runnable mRunnable = new Runnable() {

        @Override
        public void run() {
            while (!mStopFlag) {
                try {
                    // 要做的事 间隔30s请求一次
                    requestCommentNewInfo();
                    Thread.sleep(INTERVAL_TIME);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    };

    public PlaygroundFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mPlaygroundView = inflater.inflate(R.layout.playground, container, false);
        mCacheHttp = TmingCacheHttp.getInstance();
        mImageCacheTool = ImageCacheTool.getInstance();
        chatUserInfoDao = ChatUserInfoDao.getInstance(App.getAppContext());
        mAnnouncementTypeView = inflater.inflate(R.layout.playground_announcement_type_item, container, false);
        if (!StringUtils.isEmpty(App.sClassId)) {
            mClassTypeIdValue = App.sClassId;
        } else {
            mClassTypeIdValue = "" + SharedPreferUtils.getString(App.getAppContext(), "classid");
        }
        initView();
        initAnnouncementTypePopupWindowView();
        addListener();
        registerRefreshListBoradcastReceiver();// 注册刷新列表广播
        // 获取用户信息
        // requestUserInfo();
        if (App.sTeachingPointId != null && !("".equals(App.sTeachingPointId))) {
            mSchoolTypeIdValue = App.sTeachingPointId;
        } else {
            mSchoolTypeIdValue = "" + SharedPreferUtils.getString(App.getAppContext(), "teachingpointid");
        }
        //requestPlayground(mUserID, mTypeValue, mSchoolTypeIdValue, mPageno, PAGE_SIZE);
        // 开启刷新与我相关信息提示线程
        mMyThread = new Thread(mRunnable);
        mMyThread.start();
        // requestCommentNewInfo();
        /*initAuthority();*///TODO 暂时不需隐藏
        return mPlaygroundView;
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshPlaygroundList();
    }

    @Override
    public void onDestroy() {
        if (mRefreshListBroadcastReceiver != null) {
            getActivity().unregisterReceiver(mRefreshListBroadcastReceiver);
        }
        // 销毁定时器
        if (mHandler != null) {
            mHandler.removeCallbacks(mRunnable);
        }
        if (mMyThread != null) {
            mStopFlag = true;
        }
        super.onDestroy();
    }

    /**
     * 初始化视图
     */
    private void initView() {
        Log.e("plaza", "inner ?????");
        mInputMM = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        mSearchEDT = (EditText) mPlaygroundView.findViewById(R.id.playground_search_et);
        mSearchBarLL = (LinearLayout) mPlaygroundView.findViewById(R.id.playground_search_hint_layout);
        mMsgIV = (ImageView) mPlaygroundView.findViewById(R.id.msg_iv);
        
        mPlaygroundHeadRL = (RelativeLayout) mPlaygroundView.findViewById(R.id.playground_head_rl);
        mAnnouncementTypeRL = (RelativeLayout) mPlaygroundView.findViewById(R.id.announcement_type_rl);
        mAnnouncementRL = (RelativeLayout) mPlaygroundView.findViewById(R.id.announcement_operate_rl);
        mAnnouncementTypeTV = (TextView) mPlaygroundView.findViewById(R.id.announcement_type_tv);
        mPostTimelineIV = (ImageView) mPlaygroundView.findViewById(R.id.posttimeline_iv);
        mTimelineRLV = (RefreshableListView) mPlaygroundView.findViewById(R.id.playground_timeline_list);
        mTimelineRLV.setFootVisible(true);
        mImageItemAdapter = new TimelineAdapter(getActivity());
        
        mTimelineRLV.setAdapter(mImageItemAdapter);
        mLoadingView = mPlaygroundView.findViewById(R.id.loading_layout);

        // 隐藏搜索功能
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) mTimelineRLV.getLayoutParams();
        layoutParams.setMargins(0, 0, 0, 0);
        mTimelineRLV.setLayoutParams(layoutParams);
        mPlaygroundHeadRL.setVisibility(View.GONE);
        // mTimelineRLV.setOnScrollListener(new HideSearchScrollListener());
        // 隐藏搜索功能 end.
    }

    private class HideSearchScrollListener implements AbsListView.OnScrollListener {

        int lastFirstVisibleItem;
        boolean isSearchHeadVisiable = true;
        long lastVisiableChangeTime = 0;
        boolean isNeedRestListViewMargin;
        TranslateAnimation hideAnimation;

        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {
        }

        @Override
        public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
            if (firstVisibleItem == 0 && isNeedRestListViewMargin) {
                isNeedRestListViewMargin = false;
                FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) mTimelineRLV.getLayoutParams();
                layoutParams.setMargins(0, getResources().getDimensionPixelSize(R.dimen._72px), 0, 0);
                mTimelineRLV.setLayoutParams(layoutParams);

                if (hideAnimation != null && !hideAnimation.hasEnded()) {
                    hideAnimation.cancel();
                }

                if (!isSearchHeadVisiable) {
                    TranslateAnimation anim = new TranslateAnimation(0, 0,  -getResources().getDimensionPixelSize(R.dimen._72px), 0);
                    anim.setFillAfter(true);
                    anim.setDuration(400);
                    mPlaygroundHeadRL.startAnimation(anim);
                }
            }
            if (System.currentTimeMillis() - lastVisiableChangeTime < 500) {
                return;
            }
            if (firstVisibleItem > 1 && firstVisibleItem > lastFirstVisibleItem && isSearchHeadVisiable) {// 隐藏
                Log.d("tag", "---隐藏--");
                isSearchHeadVisiable = false;
                hideAnimation = new TranslateAnimation(0, 0, 0, -getResources().getDimensionPixelSize(R.dimen._72px));
                hideAnimation.setFillAfter(true);
                hideAnimation.setDuration(400);
                mPlaygroundHeadRL.startAnimation(hideAnimation);
                FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) mTimelineRLV.getLayoutParams();
                layoutParams.setMargins(0, 0, 0, 0);
                mTimelineRLV.setLayoutParams(layoutParams);
                lastVisiableChangeTime = System.currentTimeMillis();
                isNeedRestListViewMargin = true;
            } else if ((firstVisibleItem < lastFirstVisibleItem || firstVisibleItem == 0) && !isSearchHeadVisiable) {// 出现
                isSearchHeadVisiable = true;
                Log.d("tag", "---显示--");
                lastVisiableChangeTime = System.currentTimeMillis();
                TranslateAnimation anim = new TranslateAnimation(0, 0,  -getResources().getDimensionPixelSize(R.dimen._72px), 0);
                anim.setFillAfter(true);
                anim.setDuration(400);
                mPlaygroundHeadRL.startAnimation(anim);
            }
            lastFirstVisibleItem = firstVisibleItem;
        }
    }

    /**
     * 权限设置，判别用户是否为游客对部分功能进行屏蔽
     */
    void initAuthority() {
        if (AuthorityManager.getInstance().isInnerAuthority()) {
            /*mAnnouncementTypeRL.setVisibility(View.GONE);*///TODO 隐藏
            mAnnouncementRL.setVisibility(View.GONE);
        } else {
            /*mAnnouncementTypeRL.setVisibility(View.VISIBLE);*///TODO 隐藏
            mAnnouncementRL.setVisibility(View.VISIBLE);
        }
    }

    /**
     * 注册刷新列表的广播
     */
    private void registerRefreshListBoradcastReceiver() {
        IntentFilter myIntentFilter = new IntentFilter();
        myIntentFilter.addAction(Constants.SEND_COMMENT_SUCCESS_REFRESH_LIST_ACTION);
        myIntentFilter.addAction(Constants.REFRESH_LIST);
        if (mRefreshListBroadcastReceiver != null) {
            getActivity().registerReceiver(mRefreshListBroadcastReceiver, myIntentFilter);
            Log.d(TAG, "mRefreshListBroadcastReceiver NOT NULL");
        } else {
            Log.d(TAG, "mRefreshListBroadcastReceiver IS NULL");
        }
    }
    
    private Animation mInAnim, mOutAnim;

    private void addListener() {
        mInAnim = AnimationUtils.loadAnimation(getActivity(), R.anim.slide_in_from_top);
        mOutAnim = AnimationUtils.loadAnimation(getActivity(), R.anim.slide_out_to_top);
        
        // 控制搜索框上的提示语显示/隐藏
        mSearchEDT.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                UIHelper.showSearchModelActivity(getActivity(),2);
            }
        });

        mSearchEDT.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View arg0, boolean arg1) {
                if (arg1) {
                    UIHelper.showSearchModelActivity(getActivity(),2);
                } else {
                    InputMethodManager in = (InputMethodManager) getActivity().getSystemService(
                            Context.INPUT_METHOD_SERVICE);
                    in.hideSoftInputFromWindow(arg0.getApplicationWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                }
            }
        });

        mSearchBarLL.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                UIHelper.showSearchModelActivity(getActivity(),2);
            }
        });

        //与我相关数量提醒按钮监听
        mMsgIV.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (AuthorityManager.getInstance().isInnerAuthority()) {
                    AuthorityManager.getInstance().showInnerDialog(getActivity());
                    return;
                }
                // 获取消息
                Log.e("plaza", "get msg...");
                mMsgIV.setImageResource(R.drawable.playground_icon_mail);
                UIHelper.showRelativeActivity(getActivity(), mLastOperateTime);
            }
        });

        //发布动态按钮监听
        mPostTimelineIV.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (AuthorityManager.getInstance().isInnerAuthority()) {
                    AuthorityManager.getInstance().showInnerDialog(getActivity());
                    return;
                }
                // 发布动态
                Log.e("plaza", "post timeline...");
                Intent sendTrendsIntent = new Intent();
                sendTrendsIntent.putExtra("send_type", 1);// 1表示在操场界面跳转到发布动态界面
                sendTrendsIntent.putExtra("classid", mClassTypeIdValue);
                sendTrendsIntent.putExtra("teachingpointid", mSchoolTypeIdValue);
                // 发动态
                sendTrendsIntent.setClass(App.getAppContext(), PlaygroundSendTrendsActivity.class);
                startActivity(sendTrendsIntent);
            }
        });

        mAnnouncementTypeRL.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                // 选择类型
                Log.e("plaza", "click mAnnouncementTypeTV...");
                if (mAnnouncementPopupWindow != null) {
                    if (mAnnouncementPopupWindow.isShowing()) {
                        mAnnouncementPopupWindow.dismiss();
                    } else {
                        showAnnouncementTypePopupWindow();
                    }
                } else {
                    showAnnouncementTypePopupWindow();
                }
            }
        });

        //动态列表长按事件监听
        mTimelineRLV.setOnItemLongClickListener(new OnItemLongClickListener() {

            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                if (AuthorityManager.getInstance().isInnerAuthority()) {
                    AuthorityManager.getInstance().showInnerDialog(getActivity());
                    return false;
                }
                showLongClickDialog(position - 1);
                if (view instanceof LinearLayout || view instanceof GridView || view instanceof TextView
                        || view instanceof LinkView) {
                    return true;
                }
                return false;
            }
        });

        mTimelineRLV.setonRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh() {
                requestCommentNewInfo();
                refreshPlaygroundList();
            }
            @Override
            public void onLoadMore() {
                Log.e(TAG, "-----------onLoadMore------------");
                mLoadType = LOAD_TYPE_MORE;
                if (mTypeValue == CLASS_TYPE_VALUE) {
                    requestPlayground(mUserID, mTypeValue, mClassTypeIdValue, ++mPageno, PAGE_SIZE);
                } else if (mTypeValue == SCHOOL_TYPE_VALUE) {
                    requestPlayground(mUserID, mTypeValue, mSchoolTypeIdValue, ++mPageno, PAGE_SIZE);
                } else if (mTypeValue == COLLEGE_TYPE_VALUE) {
                    requestPlayground(mUserID, mTypeValue, mCollegeTypeIdValue, ++mPageno, PAGE_SIZE);
                }
            }
        });
    }

    /**
     * 操场数据请求操作
     * @param useridValue： 用户id
     * @param typeValue：类型
     * @param typeidValue：
     * @param pageNo：请求页数
     * @param pageSize：每页记录条数
     */
    private void requestPlayground(String useridValue, String typeValue, String typeidValue, int pageNo,
            int pageSize) {
        // 服务接口请求数据
        mCacheHttp.asyncRequestWithCache(ServerApi.Timeline.getNewTimelineListUrl(useridValue, typeValue, typeidValue, pageNo, pageSize), null, new TmingCacheHttp.RequestWithCacheCallBackV2Adapter<List<Timeline>>() {
            @Override
            public List<Timeline> parseData(String data) throws Exception {
                JSONObject jsonObj = new JSONObject(data);
                if (JsonUtil.checkResultIsOK(jsonObj)) {
                    return JsonUtil.paraseJsonArray(jsonObj.getJSONArray("data"), Timeline.class);
                }
                throw new Exception("Api 异常");
            }

            @Override
            public void cacheDataRespone(List<Timeline> data) {
                mTimelineRLV.onRefreshComplete();
                mLoadingView.setVisibility(View.GONE);
                if (mLoadType == LOAD_TYPE_REFRESH) {
                    mImageItemAdapter.clear();
                }
                mImageItemAdapter.addItemCollection(data);
                mImageItemAdapter.notifyDataSetChanged();
            }

            @Override
            public void requestNewDataRespone(List<Timeline> cacheRespones, List<Timeline> newRespones) {
                mTimelineRLV.onRefreshComplete();
                mLoadingView.setVisibility(View.GONE);
                mImageItemAdapter.replaceItem(cacheRespones, newRespones);
                saveUserInfo(newRespones);
            }

            @Override
            public void exception(Exception exception) {
                if (getActivity() != null) {
                    UIHelper.ToastMessage(getActivity(), "网络连接异常");
                    mLoadingView.setVisibility(View.GONE);
                    mTimelineRLV.onRefreshComplete();
                    exception.printStackTrace();
                }
            }
        });
    }
    /**
     * <p>保存用户信息</P>
     * @param newRespones
     */
    private void saveUserInfo(List<Timeline> newRespones) {
        for (int i = 0;i < newRespones.size();i ++) {
            Timeline timeline = newRespones.get(i);
            // 获取用户信息  如果本地数据库存在该用户信息，则不请求网络
            PersonalInfo mPersonalInfo = chatUserInfoDao.getChatUserInfo(String.valueOf(timeline.getUserId()));
            if (mPersonalInfo == null) {
                mPersonalInfo = new PersonalInfo();
                mPersonalInfo.setUserId(String.valueOf(timeline.getUserId()));
                mPersonalInfo.setUserName(timeline.getUsername());
                mPersonalInfo.setPhotoUrl(timeline.getPhoto());
                chatUserInfoDao.inSertChatUserInfo(mPersonalInfo);
            }
        }
    }
    
    /**
     * 获取是否有新的与我相关新的信息
     */
    private void requestCommentNewInfo() {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("userid", mUserID);
        map.put("lasttime", getLastUpdataTime());
        TmingHttp.asyncRequest(Constants.MICROBLOG_HOST + "timeline/lastestrelateme.do", map,
                new TmingHttp.RequestCallback<List<CommentLastTime>>() {

                    @Override
                    public List<CommentLastTime> onReqestSuccess(String respones) throws Exception {
                        List<CommentLastTime> commentLastTimeList = CommentLastTime.commentLastTimeJSONPasing(respones);
                        return commentLastTimeList;
                    }

                    @Override
                    public void success(List<CommentLastTime> respones) {
                        if (respones == null) {
                            return;
                        }
                        showLastTimeList(respones);
                    }

                    @Override
                    public void exception(Exception exception) {
                        if (getActivity() != null) {
                            exception.printStackTrace();
                            mLoadingView.setVisibility(View.GONE);
                        }
                    }
                });
    }

    private List<CommentLastTime> lastTimeParse(String result) {
        Log.e("result:", result);
        List<CommentLastTime> commentLastTimeList = new ArrayList<CommentLastTime>();
        commentLastTimeList.clear();
        try {
            JSONObject resObj = new JSONObject(result);
            JSONArray dataAry = resObj.getJSONArray("data");
            if (null != dataAry && dataAry.length() > 0) {
                for (int i = 0; i < dataAry.length(); i++) {
                    CommentLastTime commentLastTime = new CommentLastTime();
                    JSONObject item = dataAry.getJSONObject(i);
                    commentLastTime.setTargetId(item.getInt("targetId"));
                    commentLastTime.setOperateTime(item.getString("operateTime"));
                    commentLastTime.setUserId(item.getInt("userId"));
                    commentLastTime.setTargetUserId(item.getInt("targetUserId"));
                    commentLastTime.setOperateType(item.getInt("operateType"));
                    commentLastTimeList.add(commentLastTime);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.e("commentLastTimeList", "" + commentLastTimeList);
        return commentLastTimeList;
    }

    private void showLastTimeList(List<CommentLastTime> mCommentLastTimeList) {
        if (mCommentLastTimeList != null && mCommentLastTimeList.size() > 0) {
            CommentLastTime commentLastTime = mCommentLastTimeList.get(0);
            /*saveLastUpdataTime(commentLastTime.getOperateTime());*///等清除完上次的“与我相关”后，再记录新的时间
            mLastOperateTime = commentLastTime.getOperateTime();//记录最新的时间
            mMsgIV.setImageResource(R.drawable.playground_icon_mail_new);
        } else {
            mMsgIV.setImageResource(R.drawable.playground_icon_mail);
        }
    }

    /**
     * 初始化泡泡弹窗中的控件
     */
    private void initAnnouncementTypePopupWindowView() {
        mAnnouncementClassLL = (LinearLayout) mAnnouncementTypeView.findViewById(R.id.announcement_class_ll);
        mAnnouncementSchoolLL = (LinearLayout) mAnnouncementTypeView.findViewById(R.id.announcement_school_ll);
        mAnnouncementCollegeLL = (LinearLayout) mAnnouncementTypeView.findViewById(R.id.announcement_college_ll);
        mAnnouncementClassTV = (TextView) mAnnouncementTypeView.findViewById(R.id.announcement_class_tv);
        mAnnouncementSchoolTV = (TextView) mAnnouncementTypeView.findViewById(R.id.announcement_school_tv);
        mAnnouncementCollegeTV = (TextView) mAnnouncementTypeView.findViewById(R.id.announcement_college_tv);
    }

    /**
     * 展示选择类型的泡泡窗口
     */
    private void showAnnouncementTypePopupWindow() {
        if (App.getUserType() == Constants.USER_TYPE_STUDENT) {
            if (mClassTypeIdValue.equals("0")) {
                mAnnouncementClassLL.setVisibility(View.GONE);
            } else {
                mAnnouncementClassLL.setVisibility(View.VISIBLE);
            }
        } else if (App.getUserType() == Constants.USER_TYPE_TEACHER) { // 有负责班级的时候才有class选项
            if (App.sTeacherClassesNum > 0 && (App.getUserType() == 0 || App.getUserType() == 1)) {//用户类型为老师且负责的班级数不为0显示班级选项   modified by Caiyx on 20140821
                mAnnouncementClassLL.setVisibility(View.VISIBLE);
            } else {
                mAnnouncementClassLL.setVisibility(View.GONE);
            }
        }
        mAnnouncementPopupWindow = new PopupWindow(mAnnouncementTypeView);
        mAnnouncementPopupWindow.setWidth(LayoutParams.WRAP_CONTENT);
        mAnnouncementPopupWindow.setHeight(LayoutParams.WRAP_CONTENT);
        // 这三行代码是点击空白处隐藏泡泡窗口
        mAnnouncementPopupWindow.setTouchable(true);
        mAnnouncementPopupWindow.setOutsideTouchable(true);
        mAnnouncementPopupWindow.setFocusable(true);
        mAnnouncementPopupWindow.setBackgroundDrawable(new BitmapDrawable(getResources(), (Bitmap) null));
        mAnnouncementPopupWindow.showAsDropDown(mAnnouncementTypeRL, 8, 0);// 需要指定Gravity，默认情况是center.
        initAnnouncementTypeListener();
    }

    /**
     * 为类型添加监听器
     */
    private void initAnnouncementTypeListener() {
        mAnnouncementClassLL.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // 点击我的班级
                Log.e("plaza", "initAnnouncementTypeListener class...");
                PersonalInfo.updateUserInfo(App.getUserId(), new RequestCallback<PersonalInfo>() {

                    @Override
                    public void success(PersonalInfo respones) {
                        mLoadingView.setVisibility(View.GONE);
                        mAnnouncementTypeTV.setText("我的班级");
                        mPageno = 1;
                        mTypeValue = CLASS_TYPE_VALUE;
                        if (respones != null) {// 获取班级id
                            mClassTypeIdValue = String.valueOf(respones.getClassid());
                        }
                        mImageItemAdapter.clear();
                        mImageItemAdapter.notifyDataSetChanged();
                        requestPlayground(mUserID, CLASS_TYPE_VALUE, mClassTypeIdValue, mPageno, PAGE_SIZE);
                        if (mAnnouncementPopupWindow != null) {
                            mAnnouncementPopupWindow.dismiss();
                        }
                        mLoadingView.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public PersonalInfo onReqestSuccess(String respones) throws Exception {
                        return PersonalInfo.personalSimpleInfoJSONPasing(respones);
                    }

                    @Override
                    public void exception(Exception exception) {}
                });
            }
        });
        mAnnouncementSchoolLL.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // 点击我的教学点
                PersonalInfo.updateUserInfo(App.getUserId(), new RequestCallback<PersonalInfo>() {

                    @Override
                    public PersonalInfo onReqestSuccess(String respones) throws Exception {
                        return PersonalInfo.personalSimpleInfoJSONPasing(respones);
                    }

                    @Override
                    public void success(PersonalInfo respones) {
                        mLoadingView.setVisibility(View.GONE);
                        mAnnouncementTypeTV.setText("我的教学点");
                        mPageno = 1;
                        mTypeValue = SCHOOL_TYPE_VALUE;
                        if (respones != null) {// 获取教学点id
                            mSchoolTypeIdValue = String.valueOf(respones.teachingpointid);
                        }
                        mImageItemAdapter.clear();
                        mImageItemAdapter.notifyDataSetChanged();
                        requestPlayground(mUserID, SCHOOL_TYPE_VALUE, mSchoolTypeIdValue, mPageno, PAGE_SIZE);
                        if (mAnnouncementPopupWindow != null) {
                            mAnnouncementPopupWindow.dismiss();
                        }
                        mLoadingView.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void exception(Exception exception) {}
                });
            }
        });
        mAnnouncementCollegeLL.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // 点击开放大学
                mLoadingView.setVisibility(View.GONE);
                mAnnouncementTypeTV.setText("开放大学");
                mPageno = 1;
                mTypeValue = COLLEGE_TYPE_VALUE;
                mImageItemAdapter.clear();
                mImageItemAdapter.notifyDataSetChanged();
                requestPlayground(mUserID, COLLEGE_TYPE_VALUE, mCollegeTypeIdValue, mPageno, PAGE_SIZE);
                if (mAnnouncementPopupWindow != null) {
                    mAnnouncementPopupWindow.dismiss();
                }
                mLoadingView.setVisibility(View.VISIBLE);
            }
        });
    }

    /**
     * 长按Dialog操作
     * @param clickNum
     */
    private void showLongClickDialog(final int clickNum) {
        
        final Timeline timeline = (Timeline) mImageItemAdapter.getItem(clickNum);
        String userId = String.valueOf(timeline.getUserId());
        if (!userId.equals(App.getUserId())) {// 如果不是自己的动态则直接返回
            return;
        }
        // 列表长按事件
        Builder builder = new Builder(getActivity());
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.playground_list_dialog, null);
        // 初始化Dialog中的控件
        mCommentDialogLL = (LinearLayout) view.findViewById(R.id.playground_list_comment_ll);// 评论
        mCommentDialogLL.setVisibility(View.GONE);
        mPersonInfoDialogLL = (LinearLayout) view.findViewById(R.id.playground_person_info_ll);// 查看个人信息
        mPlaygroundPersonInfoTV = (TextView) view.findViewById(R.id.playground_person_info_tv);
        /* final Timeline timeline= mCommentTimelineList.get(clickNum); */
        
        if (String.valueOf(timeline.getUserId()).equals(App.getUserId())) {// 如果是自己的动态则可以删除
            mPlaygroundPersonInfoTV.setText(getResources().getString(R.string.del_comment));
        } else {
            mPlaygroundPersonInfoTV.setText(getResources().getString(R.string.see_personal_info));
        }
        builder.setView(view);// 设置自定义布局view
        mDialog = builder.show();
        mCommentDialogLL.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // 点击评论
                Log.e("plaza", "click mCommentDialogLL...");
                String timeline_id = "";
                String class_id = "";
                String teachingpoint_id = "";
                timeline_id = timeline.getTimelineId();
                class_id = Long.toString(timeline.getClassId());
                teachingpoint_id = Long.toString(timeline.getTeachingPointId());
                // 发评论
                Intent sendCommentIntent = new Intent();
                sendCommentIntent.setClass(App.getAppContext(), PlaygroundSendCommentActivity.class);
                sendCommentIntent.putExtra("timelineid", timeline_id);
                sendCommentIntent.putExtra("classid", class_id);
                sendCommentIntent.putExtra("teachingpointid", teachingpoint_id);
                sendCommentIntent.putExtra("originaltimelineid", timeline_id);
                sendCommentIntent.putExtra("refreshlisttype", mRefreshListType);
                startActivity(sendCommentIntent);
                mDialog.dismiss();
            }
        });
        mPersonInfoDialogLL.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                String userId = String.valueOf(timeline.getUserId());
                if (userId.equals(App.getUserId())) {// 如果是自己的动态则可以删除
                    // TODO 删除动态
                    TimelineUtil.getInstance(getActivity()).deleteWeibo(userId, timeline.getTimelineId(),
                            mDeleteRequestCallback);
                } else {
                    // 点击查看个人信息
                    Log.e("plaza", "click mPersonInfoDialogLL...");
                    String userIdValue = "";
                    userIdValue = Long.toString(timeline.getUserId());
                    UIHelper.showFriendInfoActivity(getActivity(), userIdValue);
                }
                mDialog.dismiss();
            }
        });
    }

    /**
     * 异步删除操作回调
     */
    protected RequestCallback<String> mDeleteRequestCallback = new RequestCallback<String>() {

        @Override
        public String onReqestSuccess(String respones) throws Exception {
            return parseDelResult(respones);
        }

        @Override
        public void success(String respones) {
            if (getActivity() == null) {
                return;
            }
            if (respones.equals("1")) {
                // 刷新列表
                refreshPlaygroundList();
            } else {
                Toast.makeText(getActivity(), getResources().getString(R.string.del_failed), Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public void exception(Exception exception) {}
    };

    private String parseDelResult(String respones) {
        String flag = "";
        try {
            JSONObject jobj = new JSONObject(respones);
            flag = jobj.getString("flag");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return flag;
    }
    
    /*/**
     * 显示匿名用户提示框 by Abay Zhuang
     *//*
     * private void showInnerDialog() { AlertDialog.Builder builder = new
     * Builder(context); View view = LayoutInflater.from(context).inflate(
     * R.layout.inner_playground_dialog_layout, null); // 初始化Dialog中的控件 Button
     * negative = (Button) view.findViewById(R.id.negative_button);// 评论 Button
     * positive = (Button) view.findViewById(R.id.positive_button);// 查看个人信息
     * builder.setView(view);// 设置自定义布局view mDialog = builder.show();
     * negative.setOnClickListener(new OnClickListener() {
     * 
     * @Override public void onClick(View v) { mDialog.dismiss(); } });
     * 
     * positive.setOnClickListener(new OnClickListener() {
     * 
     * @Override public void onClick(View v) { UIHelper.showLogin(context);
     * mDialog.dismiss(); } });
     * 
     * }
     */

    /**
     * 获取最近一次更新时间
     * @return:更新时间字符串
     */
    private String getLastUpdataTime() {
        String lastTimeValue = "";
        SharedPreferences preferences = getActivity().getSharedPreferences(Constants.PREFERENCES_NAME, Activity.MODE_PRIVATE);
        lastTimeValue = preferences.getString(App.getUserId(), "0");
        return lastTimeValue;
    }

    /**
     * 刷新列表
     */
    public void refreshPlaygroundList() {
        mPageno = 1;
        mLoadType = LOAD_TYPE_REFRESH;
        Log.e(TAG, "-----------onRefresh------------");
        if (mTypeValue == CLASS_TYPE_VALUE) {
            requestPlayground(mUserID, mTypeValue, mClassTypeIdValue, mPageno, PAGE_SIZE);
        } else if (mTypeValue == SCHOOL_TYPE_VALUE) {
            requestPlayground(mUserID, mTypeValue, mSchoolTypeIdValue, mPageno, PAGE_SIZE);
        } else if (mTypeValue == COLLEGE_TYPE_VALUE) {
            requestPlayground(mUserID, mTypeValue, mCollegeTypeIdValue, mPageno, PAGE_SIZE);
        }
    }
    
    public void scrollToFirstItem(){
        mTimelineRLV.smoothScrollToPosition(0);
    }
    
}
