package com.nd.shapedexamproj.activity.plaza;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentManager;
import android.text.TextUtils;
import android.view.*;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.widget.*;

import com.nd.shapedexamproj.App;
import com.nd.shapedexamproj.AuthorityManager;
import com.nd.shapedexamproj.R;
import com.nd.shapedexamproj.activity.BaseActivity;
import com.nd.shapedexamproj.model.Comment;
import com.nd.shapedexamproj.model.playground.Timeline;
import com.nd.shapedexamproj.net.ServerApi;
import com.nd.shapedexamproj.util.Constants;
import com.nd.shapedexamproj.util.ImageUtil;
import com.nd.shapedexamproj.util.JsonParseObject;
import com.nd.shapedexamproj.util.StringUtils;
import com.nd.shapedexamproj.util.TimelinePostTimeAgoUtil;
import com.nd.shapedexamproj.util.TimelineUtil;
import com.nd.shapedexamproj.util.UIHelper;
import com.nd.shapedexamproj.view.ChatBottomBar;
import com.nd.shapedexamproj.view.InputBottomBar;
import com.nd.shapedexamproj.view.ResizeRelativeLayout;
import com.nd.shapedexamproj.view.TimelineContentView;
import com.tming.common.adapter.ImageBaseAdatapter;
import com.tming.common.net.TmingHttp;
import com.tming.common.net.TmingHttp.RequestCallback;
import com.tming.common.util.Log;
import com.tming.common.util.PhoneUtil;
import com.tming.common.view.RefreshableListView;
import com.tming.common.view.support.pulltorefresh.PullToRefreshBase;
import com.tming.common.view.support.pulltorefresh.PullToRefreshBase.OnRefreshListener2;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 
 * <p> 动态详情,版本2 </p>
 * <p>Created by zll on 2014-12-18</p>
 * 
 * @see  TimelineActivityV2
 */
public class TimelineActivityV2 extends BaseActivity implements OnClickListener, ChatBottomBar.OnSendBtnClickListener {

    private final String TAG = "TimelineActivity";
    private final int MAN_SAX_VAL = 0, WOMAN_SAX_VAL = 1;// 性别 0—男 1—女
    //private final int REPLY_TIMELINE = 1, REPOST = 2, REPLY_COMMENT = 3;// 分别是：回复动态，转发，回复评论
    private static final int BIGGER = 1; 
    private static final int SMALLER = 2; 
    private static final int MSG_RESIZE = 1;
    
    private static final int INPUT_MAX_LENGTH = 140;
    private final String DEFAULT_AGE_VAL = "0";// 默认年龄
    public Context mContext;
    private Timeline timelineDynamicInfo;
    private String timeline_id = "";
    private ImageView mBackIV;
    private TextView mHeadTitleTV;
    private Button commonheaderRightBtn;
    private ResizeRelativeLayout mRootView ;
    private final static int LOAD_TYPE_REFRESH = 1;// 表示刷新
    private final static int LOAD_TYPE_MORE = 2;// 表示加载更多
    private int mLoadType = -1;// 操作类型
    private View loadingView;// 等待加载
    private View topView;
    private List<Timeline> commentInfoLists = new ArrayList<Timeline>();// 与我相关评论信息列表
    private boolean isShowCommentLineIV = false;
    private int page_no = 1;// 页码 不写默认第一页
    private final int page_size = 10;// 每页展示的条数 不写默认为20条
    private RefreshableListView commentList_LV;
    private CommentInfoListAdapter commentInfoListAdapter;
    private int count = 0;
    private LinearLayout replayCommentLL, delCommentLL;// 弹窗回复评论、删除评论
    private TextView replayCommentTV, delCommentTV;// 弹窗回复评论、删除评论文字控件
    private AlertDialog dialog;
    private RequestCallback<JsonParseObject<Object>> delCommentRequestCallback;
    private boolean isRefreshList = false;// 是否刷新大厅列表
    private TimelineComparator timelineComparator;// 用于按照时间排序
    private RelativeLayout comment_load_info_ll;
    private TextView comment_not_info_tv;
    private TextView timelineCommentcountTV;// 评论总数
    private long timelineCommentcount = 0;// 评论总数值
    private ChatBottomBar mInputBottomBar;// 表情
    private FragmentManager fragmentManager;
    private LinearLayout chatEditLL;
    private String mSameClass, mSameCollege,mTeacher;

    @Override
    public int initResource() {
        return R.layout.timeline_detail_v2;
    }

    @Override
    public void initComponent() {
        mContext = this;
        // 获取上个界面传递的用户信息
        timelineDynamicInfo = (Timeline) getIntent().getSerializableExtra("user_timeline");
        timeline_id = timelineDynamicInfo.getTimelineId();
        mBackIV = (ImageView) findViewById(R.id.commonheader_left_iv);
        mHeadTitleTV = (TextView) findViewById(R.id.commonheader_title_tv);
        commonheaderRightBtn = (Button) findViewById(R.id.commonheader_right_btn);
        mRootView = (ResizeRelativeLayout) findViewById(R.id.timeline_item_lay);
        // 获取下拉刷新，上拉加载控件
        
        commentList_LV = (RefreshableListView) findViewById(R.id.comment_list_LV);
        commentInfoListAdapter = new CommentInfoListAdapter(mContext);
        // 获取到评论控件
        topView = View.inflate(this, R.layout.timeline_detail_item, null);
        loadingView = topView.findViewById(R.id.loading_layout);
        comment_not_info_tv = (TextView) topView.findViewById(R.id.comment_not_info_tv);
        comment_load_info_ll = (RelativeLayout) topView.findViewById(R.id.comment_load_info_ll);
        timelineCommentcountTV = (TextView) topView.findViewById(R.id.timeline_commentcount_tv);// 评论总数
        comment_not_info_tv.setVisibility(View.GONE);
        comment_load_info_ll.setVisibility(View.VISIBLE);
        
        /*commentList_LV.addHeaderView(topView, null, false);*/
        commentList_LV.setAdapter(commentInfoListAdapter);
        
        mSameClass = getResources().getString(R.string.playground_same_class);
        mSameCollege = getResources().getString(R.string.playground_same_college);
        mTeacher = getResources().getString(R.string.teacher);
        // -------------------------------------------
        chatEditLL = (LinearLayout) findViewById(R.id.chat_edit_ll);
        fragmentManager = getSupportFragmentManager();
        mInputBottomBar = new ChatBottomBar(mContext, fragmentManager, chatEditLL, INPUT_MAX_LENGTH);
    }

    @Override
    public void initData() {
        mHeadTitleTV.setText(R.string.timeline_title);
        commonheaderRightBtn.setVisibility(View.GONE);
        if (timelineDynamicInfo == null) {
            requestDynamicInfo();// 展示动态信息
        } else {
            showDynamicInfoList();
        }
        requestCommentInfo(page_no + "", page_size + "");// 获取评论信息
    }
    
    private Handler mHandler = new Handler () {
        public void handleMessage(Message msg) {
            switch (msg.what) { 
                  case MSG_RESIZE:  
                      /*commentList_LV.smoothScrollToPosition(commentInfoListAdapter.getCount());*/
                      /*timelineScrollView.getRefreshableView().fullScroll(ScrollView.FOCUS_DOWN);*/
                      break; 
       
                  default: 
                      break; 
                  } 
                  super.handleMessage(msg); 
        };  
      };
    
    @Override
    public void addListener() {
        mRootView.setOnResizeListener(new ResizeRelativeLayout.OnResizeListener() {
            
            @Override
            public void OnResize(int w, int h, int oldw, int oldh) {
                int change = BIGGER; 
                if (h < oldh) { 
                    change = SMALLER; 
                } 
                                 
                Message msg = new Message(); 
                msg.what = 1; 
                msg.arg1 = change; 
                mHandler.sendMessage(msg); 
            }
        });
        
        mBackIV.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // 点击了返回按钮
                if (isRefreshList) {
                    // 是否发送广播刷新大厅列表(如果动态正文界面数据发生改变 删除评论 添加评论等刷新大厅列表 或者不刷新)
                    Intent mIntent = new Intent(Constants.SEND_COMMENT_SUCCESS_REFRESH_LIST_ACTION);
                    // 发送广播
                    sendBroadcast(mIntent);
                }
                TimelineActivityV2.this.finish();
            }
        });
        commentList_LV.setOnRefreshListener(new OnRefreshListener2<ListView>() {

            @Override
            public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
                mLoadType = LOAD_TYPE_REFRESH;
                page_no = 1;
                commentInfoLists.clear();
                if (timelineDynamicInfo == null) {
                    requestDynamicInfo();// 展示动态信息
                } else {
                    showDynamicInfoList();
                }
                requestCommentInfo(page_no + "", page_size + "");
            }

            @Override
            public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
                mLoadType = LOAD_TYPE_MORE;
                requestCommentInfo((++page_no) + "", page_size + "");
            }
        });
        
        commentList_LV.getRefreshableView().setOnTouchListener(new OnTouchListener() {
            
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                mInputBottomBar.hideFaceAndKeyboard();
                return false;
            }
        });
        mInputBottomBar.setOnSendBtnClickListener(this);
    }
    
    @Override
    public void sendMsg() {
        
        if (AuthorityManager.getInstance().isInnerAuthority()) {
            AuthorityManager.getInstance().showInnerDialog(mContext);
            return;
        }
        
        if (!mInputBottomBar.getText().startsWith("@")) {
            mInputBottomBar.setTag(null);
        }
        Timeline timeline = (Timeline) mInputBottomBar.getTag();
        if (timeline != null) {
            publishCommentInCurrentPage(Constants.REPLY_COMMENT, timeline);
        } else {
            publishCommentInCurrentPage(Constants.REPLY_TIMELINE, timelineDynamicInfo);
        }
    }
    
    /**
     * <p>
     * 在当前页发表评论
     * </P>
     * 
     * @param replyType
     *            回复类型，分为回复动态和回复评论
     */
    private void publishCommentInCurrentPage(int replyType, Timeline timeline) {
        if (PhoneUtil.checkNetworkEnable(mContext) == PhoneUtil.NETSTATE_DISABLE) {
            Toast.makeText(mContext, getResources().getString(R.string.please_open_network), Toast.LENGTH_SHORT).show();
            return;
        }
        // 点击发送按钮
        loadingView.setVisibility(View.VISIBLE);
        String content = mInputBottomBar.getText();
        if (StringUtils.isEmpty(content)) {
            UIHelper.ToastMessage(TimelineActivityV2.this, getResources().getString(R.string.coach_input_empty));
            loadingView.setVisibility(View.GONE);
            return;
        }
        if (getCurrentFocus() != null && getCurrentFocus().getWindowToken() != null) {
            // 隐藏表情选择框
            mInputBottomBar.getInputMethodManager().hideSoftInputFromWindow(
                    TimelineActivityV2.this.getCurrentFocus().getWindowToken(), 0);
        }
        pubComment(replyType, timeline);
    }

    /**
     * 发布信息
     * 
     * @param
     * @param
     *            "1" 评论 "2" 转发 3回复评论的评论
     */
    private void pubComment(int replyType, Timeline timeline) {
        if (timeline == null) {
            return;
        }
        Comment comment = new Comment();
        if (TextUtils.isEmpty(timeline_id)) {
            timeline_id = timeline.getOriginalTimelineId();
        }
        comment.setTimelineid(timeline.getTimelineId());
        comment.setUserid(App.getUserId());
        if (replyType == Constants.REPLY_COMMENT) {
            Log.e(TAG, "pubComment 内容：= " + mInputBottomBar.getText());
            comment.setContent(mInputBottomBar.getText());
        } else if (replyType == Constants.REPLY_TIMELINE) {
            comment.setContent(mInputBottomBar.getText());
        }
        comment.setType(String.valueOf(replyType));
        comment.setClassid(String.valueOf(timeline.getClassId()));
        comment.setTeachingpointid(String.valueOf(timeline.getTeachingPointId()));
        comment.setOriginaltimelineid(timeline_id);
        String url = Constants.COMMENT_PUBLISH;
        TmingHttp.asyncRequest(url, comment.getMapData(), mRequestCallback);
    }

    RequestCallback<JsonParseObject<Object>> mRequestCallback = new RequestCallback<JsonParseObject<Object>>() {

        @Override
        public void exception(Exception exception) {
            loadingView.setVisibility(View.GONE);
            mInputBottomBar.setText("");
            mInputBottomBar.setTag(null);
            UIHelper.ToastMessage(mContext, getResources().getString(R.string.net_error_tip));
        }

        @Override
        public JsonParseObject<Object> onReqestSuccess(String respones) throws Exception {
            return JsonParseObject.parseJson(respones);
        }

        @Override
        public void success(JsonParseObject<Object> respones) {
            Log.e(TAG, "发送广播");
            if (respones.getFlag() != 1) {
                loadingView.setVisibility(View.GONE);
                UIHelper.ToastMessage(mContext, respones.getErrMsg());
            } else {
                refreshCommentList();
                UIHelper.ToastMessage(mContext, getResources().getString(R.string.coach_send_success));
            }
            mInputBottomBar.setText("");
            mInputBottomBar.setTag(null);

        }
    };

    /**
     * <p>
     * 刷新评论列表
     * </P>
     */
    private void refreshCommentList() {
        mLoadType = LOAD_TYPE_REFRESH;
        page_no = 1;
        loadingView.setVisibility(View.VISIBLE);
        commentInfoLists.clear();
        commentInfoListAdapter.clear();
        requestCommentInfo(page_no + "", page_size + "");
        isRefreshList = true;
        Log.d(TAG, "成功刷新评论列表");
    }

    /**
     * @deprecated <p>
     *             跳转到另一个页面发表评论，see {@link }
     *             </P>
     */
    private void publishComment() {
        if (timelineDynamicInfo != null) {
            Intent sendCommentIntent = new Intent(TimelineActivityV2.this, PlaygroundSendCommentActivity.class);
            sendCommentIntent.putExtra("timelineid", timelineDynamicInfo.getTimelineId() + "");
            sendCommentIntent.putExtra("classid", Long.toString(timelineDynamicInfo.getClassId()));
            sendCommentIntent.putExtra("teachingpointid", Long.toString(timelineDynamicInfo.getTeachingPointId()));
            sendCommentIntent.putExtra("originaltimelineid", timelineDynamicInfo.getTimelineId() + "");
            TimelineActivityV2.this.startActivityForResult(sendCommentIntent, Constants.SEND_COMMENT_SUCCESS_CEOD);
        } else {
            UIHelper.ToastMessage(mContext, "动态正文无法加载,请重试...");
        }
    }

    /**
     * 请求动态信息
     */
    private void requestDynamicInfo() {
        TmingHttp.asyncRequest(ServerApi.Timeline.getTimeline(timeline_id), null,
                new RequestCallback<Timeline>() {

                    @Override
                    public Timeline onReqestSuccess(String respones) throws Exception {
                        return dynamicInfoList(respones);
                    }

                    @Override
                    public void success(Timeline respones) {
                        showDynamicInfoList();
                    }

                    @Override
                    public void exception(Exception exception) {
                    }
                });
    }

    /**
     * 解析返回数据
     * 
     * @param result
     * @return
     */
    private Timeline dynamicInfoList(String result) {
        Log.e("result:", result);
        try {
            JSONObject resObj = new JSONObject(result);
            if (!resObj.isNull("flag") && resObj.getInt("flag") == 1) {
                if (!resObj.isNull("data")) {
                    JSONArray dataAry = resObj.getJSONArray("data");
                    if (null != dataAry && dataAry.length() > 0) {
                        for (int i = 0; i < dataAry.length(); i++) {
                            Timeline timelineInfo = new Timeline();
                            
                            JSONObject item = dataAry.getJSONObject(i);
                            timelineInfo.initWithJsonObject(item);
                            
                            timelineDynamicInfo = timelineInfo;
                        }
                    } else {
                        closeRefreshAndLoadMore();
                    }
                }
            } else {
                closeRefreshAndLoadMore();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.e("timelineDynamicInfo", "" + timelineDynamicInfo);
        return timelineDynamicInfo;
    }

    private void showDynamicInfoList() {
        // 发送获取用户信息请求
        RelativeLayout timelineDetailLay = (RelativeLayout) topView.findViewById(R.id.timeline_detail_lay);
        LinearLayout timelineDetailUserInfoLay = (LinearLayout) topView.findViewById(R.id.timeline_detail_userinfo_lay);
        ImageView userPhotoIV = (ImageView) topView.findViewById(R.id.timeline_user_photo);// 用户图片
        ImageView timelineSexageIV = (ImageView) topView.findViewById(R.id.timeline_sexage_iv);// 性别图片
        TextView userNameTV = (TextView) topView.findViewById(R.id.timeline_user_name);// 用户姓名
        TextView userClassTv = (TextView) topView.findViewById(R.id.timeline_class_tv);
        TextView timelineUserAgeTV = (TextView) topView.findViewById(R.id.timeline_user_age);// 用户年龄
        TextView timelineUserTypeTV = (TextView) topView.findViewById(R.id.timeline_user_type);// 用户类型
        LinearLayout timelineSexageLayLL = (LinearLayout) topView.findViewById(R.id.timeline_sexage_lay);// 用户性别图片和年龄背景
        LinearLayout timelineUserTypesLL = (LinearLayout) topView.findViewById(R.id.timeline_user_type_ll);
        TextView timelineTimeTV = (TextView) topView.findViewById(R.id.timeline_time_tv);// 发布时间
        TimelineContentView timelineContentView = (TimelineContentView) topView.findViewById(R.id.timeline_content_lay);
        
        TextView timelineFlowerTV = (TextView) topView.findViewById(R.id.timeline_flower_tv);// 鲜花总数
        final ImageView timelineCommentLineIV = (ImageView) topView.findViewById(R.id.timeline_comment_line_iv);
        final ImageView timelineFlowerLineIV = (ImageView) topView.findViewById(R.id.timeline_flower_line_iv);
        RelativeLayout timelineCommentFlowerRL = (RelativeLayout) topView.findViewById(R.id.timeline_comment_flower_rl);
        // 点击用户基本信息外层控件获取用户信息
        userPhotoIV.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View veiw) {
                // TODO 点击头像
                if (AuthorityManager.getInstance().isInnerAuthority()) {
                    AuthorityManager.getInstance().showInnerDialog(mContext);
                    return;
                }// 游客不能查看他人信息，modified by Caiyx on 20140909
                String userIdValue = "";
                userIdValue = Long.toString(timelineDynamicInfo.getUserId());
                UIHelper.showFriendInfoActivity(mContext, userIdValue);
            }
        });
        timelineContentView.setOnLongClickListener(new OnLongClickListener() {

            @Override
            public boolean onLongClick(View v) {
                // 评论，删除
                if (AuthorityManager.getInstance().isInnerAuthority()) {
                    AuthorityManager.getInstance().showInnerDialog(mContext);
                    return false;
                }// 游客不能进行评论，modified by Caiyx on 20140909
                showTimelineLongClickDialog(timelineDynamicInfo);
                return true;
            }
        });
        // 刷新用户头像
        ImageUtil.asyncLoadImage(userPhotoIV, timelineDynamicInfo.getPhoto(), R.drawable.all_use_icon_bg);
        int userSexVal = Integer.valueOf(timelineDynamicInfo.getSex());
        if (userSexVal == MAN_SAX_VAL) {
            timelineSexageIV.setImageResource(R.drawable.classroom_icon_male);
        } else if (userSexVal == (WOMAN_SAX_VAL)) {
            timelineSexageIV.setImageResource(R.drawable.classroom_icon_female);
        } else {
            timelineSexageIV.setImageResource(R.drawable.classroom_icon_male);
        }
        String ageVal = timelineDynamicInfo.getAge() + "";
        if ("null".equals(ageVal)) {
            ageVal = DEFAULT_AGE_VAL;
        }
        timelineUserAgeTV.setText(ageVal);// 年龄
        userNameTV.setText(timelineDynamicInfo.getUsername());// 刷新用户姓名
        //-------------设置同学关系-------------------
        if (!AuthorityManager.getInstance().isInnerAuthority()) {
            if (App.sClassId.equals(Long.toString(timelineDynamicInfo.getClassId()))) {
                if (App.getUserId().equals(Long.toString(timelineDynamicInfo.getUserId()))) {
                    userClassTv.setText("");
                } else {
                    userClassTv.setText(mSameClass);
                }
            } else {
                if (timelineDynamicInfo.getUserType() == 0 || timelineDynamicInfo.getUserType() == 1) {
                    userClassTv.setText(mTeacher);
                } else {
                    userClassTv.setText(mSameCollege);
                }
            }
        } else {
            userClassTv.setText("");
        }

        //刷新发布时间
        timelineTimeTV.setText(TimelinePostTimeAgoUtil.TimelinePostTimeAgo(timelineDynamicInfo.getPostTime()));
        timelineContentView.setTimeline(null,timelineDynamicInfo);
        
        int industryId = 1;
        industryId = Integer.valueOf(timelineDynamicInfo.getIndustryid());
        switch (industryId) {
            case 1:
                timelineUserTypesLL.setBackgroundResource(R.color.industry_it_bg);
                timelineUserTypeTV.setText("IT");
                break;
            case 2:
                timelineUserTypesLL.setBackgroundResource(R.color.industry_it_bg);
                timelineUserTypeTV.setText("工");
                break;
            case 3:
                timelineUserTypesLL.setBackgroundResource(R.color.industry_shang_bg);
                timelineUserTypeTV.setText("商");
                break;
            case 4:
                timelineUserTypesLL.setBackgroundResource(R.color.industry_jin_bg);
                timelineUserTypeTV.setText("金");
                break;
            case 5:
                timelineUserTypesLL.setBackgroundResource(R.color.industry_jin_bg);
                timelineUserTypeTV.setText("文");
                break;
            case 6:
                timelineUserTypesLL.setBackgroundResource(R.color.industry_jin_bg);
                timelineUserTypeTV.setText("艺");
                break;
            case 7:
                timelineUserTypesLL.setBackgroundResource(R.color.industry_yi_bg);
                timelineUserTypeTV.setText("医");
                break;
            case 8:
                timelineUserTypesLL.setBackgroundResource(R.color.industry_yi_bg);
                timelineUserTypeTV.setText("法");
                break;
            case 9:
                timelineUserTypesLL.setBackgroundResource(R.color.industry_yi_bg);
                timelineUserTypeTV.setText("教");
                break;
            case 10:
                timelineUserTypesLL.setBackgroundResource(R.color.industry_yi_bg);
                timelineUserTypeTV.setText("政");
                break;
            case 11:
                timelineUserTypesLL.setBackgroundResource(R.color.industry_study_bg);
                timelineUserTypeTV.setText("学");
                break;
            case 0:
                timelineUserTypesLL.setBackgroundResource(R.color.industry_other_bg);
                timelineUserTypeTV.setText("其");
                break;
            default:
                break;
        }
        timelineCommentcount = timelineDynamicInfo.getCommentCount();
        String flowerCountVal = timelineDynamicInfo.getFlowerCount();
        if ("null".equals(flowerCountVal)) {
            flowerCountVal = "0";
        }
        timelineFlowerTV.setText("鲜花" + flowerCountVal);
        if (!isShowCommentLineIV) {
            timelineCommentLineIV.setVisibility(View.VISIBLE);
            timelineFlowerLineIV.setVisibility(View.GONE);
        }
        timelineCommentcountTV.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                timelineCommentLineIV.setVisibility(View.VISIBLE);
                timelineFlowerLineIV.setVisibility(View.GONE);
            }
        });
        timelineFlowerTV.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                timelineCommentLineIV.setVisibility(View.GONE);
                timelineFlowerLineIV.setVisibility(View.VISIBLE);
            }
        });
        isShowCommentLineIV = true;
    }

    private void showImage(int position, List<String> imgUrlLst) {
        String urls[] = new String[imgUrlLst.size()];
        for (int i = 0; i < imgUrlLst.size(); i++) {
            urls[i] = imgUrlLst.get(i);
        }
        UIHelper.showImageBrower(this, position, urls);
    }

    /**
     * 获取评论列表信息
     */
    private void requestCommentInfo(String pageno, String pageSize) {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("timelineid", timeline_id);
        map.put("pageno", pageno);
        map.put("pagesize", pageSize);
        TmingHttp.asyncRequest(Constants.MICROBLOG_HOST + "timeline/commentlist.do", map,
                new RequestCallback<List<Timeline>>() {

                    @Override
                    public List<Timeline> onReqestSuccess(String respones) throws Exception {
                        return commentInfoList(respones);
                    }

                    @Override
                    public void success(List<Timeline> respones) {
                        if (respones == null) {
                            closeRefreshAndLoadMore();
                            return;
                        }
                        if (respones.size() <= 0) {
                            if (commentInfoListAdapter.getCount() <= 1) {
                                comment_not_info_tv.setVisibility(View.VISIBLE);
                                comment_load_info_ll.setVisibility(View.VISIBLE);
                            }
                            closeRefreshAndLoadMore();
                            return;
                        }
                        if (mLoadType == LOAD_TYPE_REFRESH) {
                            commentInfoListAdapter.clear();
                        }
                        for (int i = 0; i < respones.size(); i++) {
                            commentInfoLists.add(respones.get(i));
                        }
                        timelineCommentcount = respones.get(0).getTotalSize();
                        timelineCommentcountTV.setText("" + timelineCommentcount + "评论");
                        comment_not_info_tv.setVisibility(View.GONE);
                        comment_load_info_ll.setVisibility(View.GONE);
                        showCommentInfoLists();
                    }

                    @Override
                    public void exception(Exception exception) {
                        closeRefreshAndLoadMore();
                        if (commentInfoListAdapter.getCount() <= 1 ) {
                            comment_not_info_tv.setVisibility(View.VISIBLE);
                            comment_load_info_ll.setVisibility(View.VISIBLE);
                        } else {
                            comment_not_info_tv.setVisibility(View.GONE);
                            comment_load_info_ll.setVisibility(View.GONE);
                        }
                        
                        UIHelper.ToastMessage(mContext, "网络异常，请重试");
                        return;
                    }
                });
    }

    /**
     * 解析评论返回的数据
     * 
     * @return
     */
    private List<Timeline> commentInfoList(String result) {
        List<Timeline> saveCommentInfoList = new ArrayList<Timeline>();// 临时保存我的评论列表
        try {
            JSONObject resObj = new JSONObject(result);
            if (!resObj.isNull("flag") && resObj.getInt("flag") == 1) {
                if (!resObj.isNull("data")) {
                    JSONArray dataAry = resObj.getJSONArray("data");
                    if (null != dataAry && dataAry.length() > 0) {
                        for (int i = 0; i < dataAry.length(); i++) {
                            Timeline timelineInfo = new Timeline();
                            timelineInfo.setTotalSize(resObj.getInt("totalSize"));
                            JSONObject item = dataAry.getJSONObject(i);
                            timelineInfo.setType(item.getInt("type"));
                            timelineInfo.setContent(item.getString("content"));
                            timelineInfo.setTop(item.getBoolean("top"));
                            timelineInfo.setTimelineId(item.getString("timelineId"));
                            timelineInfo.setUserId(item.getInt("userId"));
                            timelineInfo.setFlowerCount(item.getString("flowerCount"));
                            timelineInfo.setPhoto(item.getString("photo"));
                            timelineInfo.setCommentedCount(item.getInt("commentedCount"));
                            timelineInfo.setCommentCount(item.getInt("commentCount"));
                            timelineInfo.setTransferredCount(item.getInt("transferredCount"));
                            timelineInfo.setTransferCount(item.getInt("transferCount"));
                            timelineInfo.setPostTime(item.getString("postTime"));
                            timelineInfo.setReferId(item.getString("referId"));
                            timelineInfo.setUsername(item.getString("username"));
                            timelineInfo.setOriginalTimelineId(item.getString("originalTimelineId"));
                            timelineInfo.setClassId(item.getInt("classId"));
                            timelineInfo.setTeachingPointId(item.getInt("teachingPointId"));
                            timelineInfo.setIndustryid(item.getInt("industryid"));
                            timelineInfo.setSex(item.getString("sex"));
                            timelineInfo.setAge(item.getString("age"));
                            saveCommentInfoList.add(timelineInfo);
                        }
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return saveCommentInfoList;
    }
    
    /**
     * 展示评论列表
     */
    private void showCommentInfoLists() {
        // 按发布时间进行排序
        if (timelineComparator == null) {
            timelineComparator = new TimelineComparator();
        }
        Collections.sort(commentInfoLists, timelineComparator);
        commentInfoListAdapter.clear();
        commentInfoListAdapter.addItemCollection(commentInfoLists);
        commentInfoListAdapter.notifyDataSetChanged();
        count = 0;
        closeRefreshAndLoadMore();
    }

    class CommentInfoListAdapter extends ImageBaseAdatapter<Timeline> {

        int commentPersonColor = getResources().getColor(R.color.playground_comment_person_color);

        public CommentInfoListAdapter(Context context) {
            super(context);
        }

        @Override
        public View infateItemView(Context context) {
            View convertView = View.inflate(context, R.layout.timeline_detail_comment_item, null);
            ItemViewHolder itemViewHolder = new ItemViewHolder();
            itemViewHolder.timelineCommentListRL = (RelativeLayout) convertView
                    .findViewById(R.id.timeline_comment_list_rl);
            itemViewHolder.imageView = (ImageView) convertView.findViewById(R.id.timeline_comment_user_photo);
            itemViewHolder.timelineCommentUserNameTV = (TextView) convertView
                    .findViewById(R.id.timeline_comment_user_name);
            itemViewHolder.timelineCommentToUseredTV = (TextView) convertView
                    .findViewById(R.id.timeline_comment_to_usered);
            itemViewHolder.timelineCommentTimeTV = (TextView) convertView.findViewById(R.id.timeline_comment_time_tv);// 评论时间
            convertView.setTag(itemViewHolder);
            return convertView;
        }

        @Override
        public URL getDataImageUrl(Timeline data) {
            URL url = null;
            try {
                url = new URL(data.getPhoto());
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            return url;
        }
        
        @Override
        public long getItemId(int position) {
            return 0;
        }
        
        
        @Override
        public int getCount() {
            return super.getCount() + 1;
        }
        
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (position == 0) {
                return topView;
            } else {
                if (!(convertView.getTag() instanceof ItemViewHolder)) {
                    convertView = null;
                }
                return super.getView(position - 1, convertView, parent);
            }
        }

        @Override
        public void setViewHolderData(BaseViewHolder holder, final Timeline timelineInfo) {
            String userName = timelineInfo.getUsername();
            if (userName != null && !("".equals(userName))) {
                userName = timelineInfo.getUsername();
            } else {
                userName = "";
            }
            ((ItemViewHolder) holder).timelineCommentUserNameTV.setText(userName);
            String sendCommentTimeValue = "";
            String sendCommentTime = TimelinePostTimeAgoUtil.TimelinePostTimeAgo(timelineInfo.getPostTime());
            if (!sendCommentTime.contains("-")) {
                sendCommentTimeValue = sendCommentTime;
            } else {
                sendCommentTimeValue = timelineInfo.getPostTime().substring(5, 16).trim();
            }
            ((ItemViewHolder) holder).timelineCommentTimeTV.setText(sendCommentTimeValue);// 刷新发布时间
            // 加载评论内容
            String contentImage = timelineInfo.getContent();
            List<String> imgUrlLst = UIHelper.parseImages(contentImage);
            contentImage = UIHelper.filterImageContent(contentImage);
            
            String commentUserName = timelineInfo.getCommentUserName();
            if (commentUserName != null) {//拼接字符串
                contentImage = "@" + timelineInfo.getCommentUserName() + ":" + contentImage;
            }
            
            ((ItemViewHolder) holder).timelineCommentToUseredTV.setText(InputBottomBar.parseFaceByText(mContext,
                    contentImage, (int) ((ItemViewHolder) holder).timelineCommentToUseredTV.getTextSize(),
                    commentPersonColor));// 刷新发布内容
            // 添加点击事件
            ((ItemViewHolder) holder).imageView.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View arg0) {
                    if (AuthorityManager.getInstance().isInnerAuthority()) {
                        AuthorityManager.getInstance().showInnerDialog(mContext);
                        return;
                    }// 游客不能查看他人资料，modified by Caiyx on 20140909
                    String userIdValue = "";
                    userIdValue = Long.toString(timelineInfo.getUserId());
                    UIHelper.showFriendInfoActivity(mContext, userIdValue);
                }
            });
            ((ItemViewHolder) holder).timelineCommentUserNameTV.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View arg0) {
                    if (AuthorityManager.getInstance().isInnerAuthority()) {
                        AuthorityManager.getInstance().showInnerDialog(mContext);
                        return;
                    }// 游客不能查看他人资料，modified by Caiyx on 20140909
                    String userIdValue = "";
                    userIdValue = Long.toString(timelineInfo.getUserId());
                    UIHelper.showFriendInfoActivity(mContext, userIdValue);
                }
            });
            ((ItemViewHolder) holder).timelineCommentListRL.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View arg0) {
                    if (AuthorityManager.getInstance().isInnerAuthority()) {
                        AuthorityManager.getInstance().showInnerDialog(mContext);
                        return;
                    }// 游客不能进行评论，modified by Caiyx on 20140909
                     // 点击回复评论 删除评论
                    /* showTimelineReplyCommentClick(timelineInfo); */
                    relateToComment(timelineInfo);
                }
            });
            ((ItemViewHolder) holder).timelineCommentListRL.setOnLongClickListener(new OnLongClickListener() {

                @Override
                public boolean onLongClick(View v) {
                    showTimelineReplyCommentClick(timelineInfo);
                    return false;
                }
            });
        }

        private class ItemViewHolder extends BaseViewHolder {

            private TextView timelineCommentUserNameTV, timelineCommentToUseredTV, timelineCommentTimeTV;
            private RelativeLayout timelineCommentListRL;
        }
    }
    /**
     * <p>关联评论</P>
     * @param timelineInfo
     */
    private void relateToComment(Timeline timelineInfo) {
        mInputBottomBar.setTag(timelineInfo);
        mInputBottomBar.setText("@" + timelineInfo.getUsername() + ":");
        mInputBottomBar.getEditText().setSelection(mInputBottomBar.getEditText().getText().length());
    }
    
    /**
     * 
     * <p>
     * 动态正文长按事件
     * </P>
     * 
     */
    public void showTimelineLongClickDialog(final Timeline timeline) {
        String dynamicUserId = String.valueOf(timelineDynamicInfo.getUserId());
        String conmentUserId = String.valueOf(timeline.getUserId());
        if (!dynamicUserId.equals(App.getUserId())) {// 对非自己的动态取消弹窗
            return;
        }
        // 列表长按事件
        Builder builder = new Builder(mContext);
        View view = LayoutInflater.from(mContext).inflate(R.layout.playground_list_dialog, null);
        // 初始化Dialog中的控件
        LinearLayout replayContentLL = (LinearLayout) view.findViewById(R.id.playground_list_comment_ll);// 评论
        replayContentLL.setVisibility(View.GONE);// TODO 暂时隐藏
        LinearLayout delContentLL = (LinearLayout) view.findViewById(R.id.playground_person_info_ll);// 查看个人信息
        if (dynamicUserId.equals(App.getUserId()) || conmentUserId.equals(App.getUserId())) {// 只能删除自己动态的评论或自己发表的评论
            delContentLL.setVisibility(View.VISIBLE);
        } else {
            delContentLL.setVisibility(View.GONE);
        }
        TextView replayCommentTV = (TextView) view.findViewById(R.id.playground_list_comment_tv);
        TextView delCommentTV = (TextView) view.findViewById(R.id.playground_person_info_tv);
        replayCommentTV.setText(getResources().getString(R.string.playground_comment_but));
        delCommentTV.setText(getResources().getString(R.string.del_comment));
        builder.setView(view);// 设置自定义布局view
        dialog = builder.show();
        replayContentLL.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (AuthorityManager.getInstance().isInnerAuthority()) {
                    AuthorityManager.getInstance().showInnerDialog(mContext);
                    return;
                }// 游客不能进行评论，modified by Caiyx on 20140909
                publishComment();
                dialog.dismiss();
            }
        });
        delContentLL.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (AuthorityManager.getInstance().isInnerAuthority()) {
                    AuthorityManager.getInstance().showInnerDialog(mContext);
                    return;
                }// 游客不能删除评论，modified by Caiyx on 20140909
                Log.e("plaza", "click personInfoDialogLL...");
                loadingView.setVisibility(View.VISIBLE);
                TimelineUtil.getInstance(mContext).deleteWeibo(String.valueOf(timeline.getUserId()),
                        timeline.getTimelineId(), mDeleteRequestCallback);
                dialog.dismiss();
            }
        });
    }

    /**
     * 删除评论
     */
    public static final int DEL_COMMENT = 0;
    /**
     * 删除动态
     */
    public static final int DEL_TIMELINE = 3;
    /**
     * 动态回复
     */
    public static final int TIMELINE_REPLY = 1;
    /**
     * 评论回复
     */
    public static final int COMMENT_REPLY = 2;
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
            if (respones.equals("1")) {
                loadingView.setVisibility(View.GONE);
                // 发广播
                sendBroadcast();
                finish();
            } else {
                Toast.makeText(mContext, getResources().getString(R.string.del_failed), Toast.LENGTH_SHORT).show();
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

    private void sendBroadcast() {
        Intent intent = new Intent();
        intent.setAction(Constants.REFRESH_LIST);
        sendBroadcast(intent);
    }

    /**
     *             <p>
     *             点击评论列表，弹出的对话框
     *             </P>
     * @param timelineInfoes
     */
    private void showTimelineReplyCommentClick(final Timeline timelineInfoes) {
        String dynamicUserId = String.valueOf(timelineDynamicInfo.getUserId());
        String conmentUserId = String.valueOf(timelineInfoes.getUserId());
        if (!dynamicUserId.equals(App.getUserId())) {// 若是别人的动态或是别人的评论，取消弹框
            return;
        }
        // 列表长按事件
        Builder builder = new Builder(mContext);
        View view = LayoutInflater.from(mContext).inflate(R.layout.playground_list_dialog, null);
        // 初始化Dialog中的控件
        replayCommentLL = (LinearLayout) view.findViewById(R.id.playground_list_comment_ll);// 评论
        replayCommentLL.setVisibility(View.GONE);// TODO 暂时不需要此入口
        delCommentLL = (LinearLayout) view.findViewById(R.id.playground_person_info_ll);// 查看个人信息
        if (conmentUserId.equals(App.getUserId())) {// 自己不能再评论自己的评论
            replayCommentLL.setVisibility(View.GONE);
        }
        if (dynamicUserId.equals(App.getUserId())) {// 只能删除自己动态的评论
            delCommentLL.setVisibility(View.VISIBLE);
        } else {
            delCommentLL.setVisibility(View.GONE);
        }
        replayCommentTV = (TextView) view.findViewById(R.id.playground_list_comment_tv);
        delCommentTV = (TextView) view.findViewById(R.id.playground_person_info_tv);
        replayCommentTV.setText(getResources().getString(R.string.replay_comment));
        delCommentTV.setText(getResources().getString(R.string.del_comment));
        builder.setView(view);// 设置自定义布局view
        dialog = builder.show();
        replayCommentLL.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (AuthorityManager.getInstance().isInnerAuthority()) {
                    AuthorityManager.getInstance().showInnerDialog(mContext);
                    return;
                }// 游客不能进行评论，modified by Caiyx on 20140909
                 // 点击回复评论
                Log.e("plaza", "click commentDialogLL...");
                String timeline_id_val = "";
                String class_id = "";
                String teachingpoint_id = "";
                String userNameVal = "";
                timeline_id_val = timelineInfoes.getTimelineId();
                class_id = Long.toString(timelineInfoes.getClassId());
                teachingpoint_id = Long.toString(timelineInfoes.getTeachingPointId());
                userNameVal = timelineInfoes.getUsername();
                // 发评论
                Intent sendCommentIntent = new Intent();
                sendCommentIntent.setClass(App.getAppContext(), PlaygroundSendCommentActivity.class);
                sendCommentIntent.putExtra("send_type", Constants.REPLY_COMMENT);
                sendCommentIntent.putExtra("userName", userNameVal);
                sendCommentIntent.putExtra("timelineid", timeline_id_val);
                Log.e(TAG, "replayCommentLL timeline_id:=" + timeline_id_val);
                sendCommentIntent.putExtra("classid", class_id);
                sendCommentIntent.putExtra("teachingpointid", teachingpoint_id);
                sendCommentIntent.putExtra("originaltimelineid", timeline_id);
                TimelineActivityV2.this.startActivityForResult(sendCommentIntent, Constants.SEND_COMMENT_SUCCESS_CEOD);
                dialog.dismiss();
            }
        });
        delCommentLL.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (AuthorityManager.getInstance().isInnerAuthority()) {
                    AuthorityManager.getInstance().showInnerDialog(mContext);
                    return;
                }// 游客不能删除评论，modified by Caiyx on 20140909
                 // 点击删除评论
                Log.e("plaza", "click personInfoDialogLL...");
                loadingView.setVisibility(View.VISIBLE);
                requestClearCommentList(timelineInfoes.getTimelineId());
                dialog.dismiss();
            }
        });
    }

    /**
     * 删除评论
     */
    private void requestClearCommentList(String timelineId) {
        delCommentJsonResult(timelineId);
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("commentid", timelineId);
        TmingHttp.asyncRequest(Constants.MICROBLOG_HOST + "timeline/deletecomment.do", map, delCommentRequestCallback);
        Log.d("requestClearCommentList", "requestClearCommentList   mmmmmmmm");
    }

    private void delCommentJsonResult(final String timelineId) {
        delCommentRequestCallback = new RequestCallback<JsonParseObject<Object>>() {

            @Override
            public void exception(Exception exception) {
                loadingView.setVisibility(View.GONE);
            }

            @Override
            public JsonParseObject<Object> onReqestSuccess(String respones) throws Exception {
                return JsonParseObject.parseJson(respones);
            }

            @Override
            public void success(JsonParseObject<Object> respones) {
                if (respones.getFlag() != 1) {
                    UIHelper.ToastMessage(mContext, respones.getErrMsg());
                } else {
                    loadingView.setVisibility(View.GONE);
                    commentInfoListAdapter.clear();
                    int delNum = -1;
                    for (int i = 0; i < commentInfoLists.size(); i++) {
                        if (commentInfoLists.get(i).getTimelineId().equals(timelineId)) {
                            delNum = i;
                            break;
                        }
                    }
                    isRefreshList = true;
                    commentInfoLists.remove(delNum);
                    commentInfoListAdapter.clear();
                    commentInfoListAdapter.addItemCollection(commentInfoLists);
                    /* requestDynamicInfo(); */
                    timelineCommentcountTV.setText("" + (commentInfoListAdapter.getCount() - 1) + "评论");
                    commentInfoListAdapter.notifyDataSetChanged();
                    UIHelper.ToastMessage(mContext, getResources().getString(R.string.coach_send_success));
                }
            }
        };
    }

    private String getTime() {
        return new SimpleDateFormat("MM-dd HH:mm", Locale.CHINA).format(new Date());
    }

    /**
     * 上拉加载更多 下拉刷新结束后关闭加载图标
     */
    private void closeRefreshAndLoadMore() {
        loadingView.setVisibility(View.GONE);
        // 关闭加载图标
        commentList_LV.onRefreshComplete();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        switch (resultCode) {
            case Constants.SEND_COMMENT_SUCCESS_BACK_CEOD:
                refreshCommentList();
                break;
            default:
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
         if(keyCode == KeyEvent.KEYCODE_BACK){
             if (mInputBottomBar.hasShowFace()) {
                 mInputBottomBar.hideFace();
                 return true;
             }
             // 点击了返回按钮
             if (isRefreshList) {
                // 是否发送广播刷新大厅列表(如果动态正文界面数据发生改变 删除评论 添加评论等刷新大厅列表 或者不刷新)
                Intent mIntent = new Intent(Constants.SEND_COMMENT_SUCCESS_REFRESH_LIST_ACTION);
                // 发送广播
                sendBroadcast(mIntent);
             }
         }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onClick(View view) {
        Log.d("aaa", commentInfoLists.get(view.getId()).getContent());
    }

    /**
     * 时间转为毫秒
     * 
     * @return
     */
    public long timeConvertMillisecond(String dateVal) {
        long millisecondVal = 0L;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            millisecondVal = simpleDateFormat.parse(dateVal).getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return millisecondVal;
    }

    class TimelineComparator implements Comparator<Timeline> {

        @Override
        public int compare(Timeline object1, Timeline object2) {
            if (timeConvertMillisecond(object1.getPostTime()) < timeConvertMillisecond(object2.getPostTime())) {
                return 1;
            } else {
                return -1;
            }
        }
    }
}
