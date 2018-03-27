package com.nd.shapedexamproj.view;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nd.shapedexamproj.App;
import com.nd.shapedexamproj.AuthorityManager;
import com.nd.shapedexamproj.R;
import com.nd.shapedexamproj.model.playground.Timeline;
import com.nd.shapedexamproj.util.CircleImageView;
import com.nd.shapedexamproj.util.Constants;
import com.nd.shapedexamproj.util.TimelinePostTimeAgoUtil;
import com.nd.shapedexamproj.util.TimelineUtil;
import com.nd.shapedexamproj.util.UIHelper;
import com.tming.common.cache.image.ImageCacheTool;
import com.tming.common.net.TmingHttp.RequestCallback;
import com.tming.common.util.Helper;
import com.tming.common.util.Log;
//import u.aly.T;

import java.lang.ref.WeakReference;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * 操场动态列表项布局
 * Created by Administrator on 2014/11/27.
 */
public class TimelineView extends LinearLayout implements View.OnClickListener {

    /**
     * 加载头像图片的大小
     */
    public static final int LOAD_HEAD_IMAGE_WH_DP = 50;

    private final String MAN_SAX_VAL = "0";
    private final String WOMAN_SAX_VAL = "1";// 性别 0—男 1—女
    private final String DEFAULT_AGE_VAL = "0";// 默认年龄
    
    private Context context;
    private ImageView mHeadPortriatIv,mMoreIv;
    private LinearLayout timeline_item_comment_lay,timeline_sexage_lay, timeline_user_industry_lay;
    private TextView mNameTv, postTimeAgoTV, commentCountTV, industryTV,mClassTv;

    private TextView timeline_user_age;// 年龄
    private ImageView timeline_user_sex_icon;// 用户性别图像
    private TimelineContentView timelineContentView;
    private RelativeLayout timelineContentRL;
    private int singleImageHeight;
    private int reqImageWH, contentSmallImageSize;
    
    private String mSameClass,mSameCollege,mTeacher;
    private WeakReference<Timeline> mTimelineRef;
    private RequestCallback deleteCallBack;
    
    public TimelineView(Context context) {
        super(context);
        this.context = context;
    }

    public TimelineView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
    }

    @TargetApi(11)
    public TimelineView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        singleImageHeight = Helper.dip2px(getContext(), 120);
        reqImageWH = Helper.dip2px(getContext(), LOAD_HEAD_IMAGE_WH_DP);
        
        timelineContentRL = (RelativeLayout) findViewById(R.id.timeline_content_rl);
        timelineContentView = (TimelineContentView) findViewById(R.id.timeline_content_lay);
        mSameClass = getResources().getString(R.string.playground_same_class);
        mSameCollege = getResources().getString(R.string.playground_same_college);
        mTeacher = getResources().getString(R.string.teacher);
        
        timeline_item_comment_lay = (LinearLayout) findViewById(R.id.timeline_item_comment_lay);
        mHeadPortriatIv = (CircleImageView) findViewById(R.id.common_timeline_item_view_headportrait_iv);
        mNameTv = (TextView) findViewById(R.id.common_timeline_item_view_name_tv);
        mClassTv = (TextView) findViewById(R.id.common_timeline_item_view_class_tv);
        mMoreIv = (ImageView) findViewById(R.id.common_timeline_item_view_time_iv);
        
        postTimeAgoTV = (TextView) findViewById(R.id.timeline_item_postTimeAgo_TV);
        commentCountTV = (TextView) findViewById(R.id.timeline_item_commentCount_TV);
        
        timeline_sexage_lay = (LinearLayout) findViewById(R.id.common_timeline_item_view_sex_lay);
        timeline_user_industry_lay = (LinearLayout) findViewById(R.id.common_timeline_item_view_industry_lay);
        industryTV = (TextView) findViewById(R.id.common_timeline_item_view_industry_tv);
        timeline_user_age = (TextView) findViewById(R.id.common_timeline_item_view_age_tv);// 用户年龄
        timeline_user_sex_icon = (ImageView) findViewById(R.id.common_timeline_item_view_sex_iv);// 用户性别图像
        
        initClickListener();
    }

    public void initClickListener(){
        mHeadPortriatIv.setOnClickListener(this);
        mNameTv.setOnClickListener(this);
        timelineContentRL.setOnClickListener(this);
        timelineContentView.setOnClickListener(this);
        timeline_item_comment_lay.setOnClickListener(this);
        mMoreIv.setOnClickListener(this);
    }

    public void updateTimelineData(Timeline timeline,int layoutType) {
        if (layoutType == Timeline.LAYOUT_TYPE_SECOND) {
            String userId = String.valueOf(timeline.getUserId());
            if (userId.equals(App.getUserId())) {
                mMoreIv.setVisibility(View.VISIBLE);
            } else {
                mMoreIv.setVisibility(View.GONE);
            }
        } else if (layoutType == Timeline.LAYOUT_TYPE_FIRST){
            mMoreIv.setVisibility(View.GONE);
        }
        mNameTv.setText(timeline.getUsername());
        //----------------设置同学关系----------------------
        if (!AuthorityManager.getInstance().isInnerAuthority()) {
            if (App.sClassId.equals(Long.toString(timeline.getClassId()))) {
                if (App.getUserId().equals(Long.toString(timeline.getUserId()))) {
                    mClassTv.setText("");
                } else {
                    mClassTv.setText(mSameClass);
                }
            } else {
                if (timeline.getUserType() == 0 || timeline.getUserType() == 1) {
                    mClassTv.setText(mTeacher);
                } else {
                    mClassTv.setText(mSameCollege);
                }
            }
        } else {
            if (timeline.getUserType() == 0 || timeline.getUserType() == 1) {
                mClassTv.setText(mTeacher);
            } else {
                mClassTv.setText(mSameCollege);
            }
        }
        //------------------
        String ageVal = timeline.getAge();
        if ("null".equals(ageVal)) {
            ageVal = DEFAULT_AGE_VAL;
        }
        try {
            URL url = new URL(timeline.getPhoto());
            ImageCacheTool.getInstance().asyncLoadImage(mHeadPortriatIv,null, url, reqImageWH, reqImageWH);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        timeline_user_age.setText(ageVal);// 年龄
        String userSexVal = timeline.getSex();
        if (userSexVal.equals(MAN_SAX_VAL)) {
            timeline_user_sex_icon.setImageResource(R.drawable.classroom_icon_male);
        } else if (userSexVal.equals(WOMAN_SAX_VAL)) {
            timeline_user_sex_icon.setImageResource(R.drawable.classroom_icon_female);
        } else {
            timeline_user_sex_icon.setImageResource(R.drawable.classroom_icon_male);
        }
        postTimeAgoTV.setText(TimelinePostTimeAgoUtil.TimelinePostTimeAgo(timeline.getPostTime()));
        commentCountTV.setText(String.valueOf(timeline.getCommentCount()));
        
        timelineContentView.setTimeline(null,timeline);
        updateIndustry(timeline.getIndustryid());
    }

    public void updateIndustry(int industryId) {
        switch (industryId) {
            case 1:
                timeline_user_industry_lay.setBackgroundResource(R.color.industry_it_bg);
                industryTV.setText("IT");
                break;
            case 2:
                timeline_user_industry_lay.setBackgroundResource(R.color.industry_it_bg);
                industryTV.setText("工");
                break;
            case 3:
                timeline_user_industry_lay.setBackgroundResource(R.color.industry_shang_bg);
                industryTV.setText("商");
                break;
            case 4:
                timeline_user_industry_lay.setBackgroundResource(R.color.industry_jin_bg);
                industryTV.setText("金");
                break;
            case 5:
                timeline_user_industry_lay.setBackgroundResource(R.color.industry_jin_bg);
                industryTV.setText("文");
                break;
            case 6:
                timeline_user_industry_lay.setBackgroundResource(R.color.industry_jin_bg);
                industryTV.setText("艺");
                break;
            case 7:
                timeline_user_industry_lay.setBackgroundResource(R.color.industry_yi_bg);
                industryTV.setText("医");
                break;
            case 8:
                timeline_user_industry_lay.setBackgroundResource(R.color.industry_yi_bg);
                industryTV.setText("法");
                break;
            case 9:
                timeline_user_industry_lay.setBackgroundResource(R.color.industry_yi_bg);
                industryTV.setText("教");
                break;
            case 10:
                timeline_user_industry_lay.setBackgroundResource(R.color.industry_yi_bg);
                industryTV.setText("政");
                break;
            case 11:
                timeline_user_industry_lay.setBackgroundResource(R.color.industry_study_bg);
                industryTV.setText("学");
                break;
            case 0:
                timeline_user_industry_lay.setBackgroundResource(R.color.industry_other_bg);
                industryTV.setText("其");
                break;
            default:
                break;
        }
    }
    /**
     * 类的外部入口
     *
     * @param timeline
     * @param layoutType
     */
    public void setTimeline(Timeline timeline,int layoutType) {
        mTimelineRef = new WeakReference<Timeline>(timeline);
        updateTimelineData(timeline,layoutType);
    }
    
    public void setDeleteCallBack(RequestCallback deleteCallBack){
        this.deleteCallBack = deleteCallBack;
    }
    
    @Override
    public void onClick(View v) {
        Timeline timeline = null;
        if (mTimelineRef == null || (timeline = mTimelineRef.get()) == null) {
            return;
        }
        switch (v.getId()) {
            case R.id.common_timeline_item_view_headportrait_iv:// 用户头像点击跳转到他人信息
                if (AuthorityManager.getInstance().isInnerAuthority()) {
                    AuthorityManager.getInstance().showInnerDialog(getContext());
                    return;
                }
                UIHelper.showFriendInfoActivity(getContext(), Long.toString(timeline.getUserId()));
                break;
            case R.id.common_timeline_item_view_name_tv:
                if (AuthorityManager.getInstance().isInnerAuthority()) {
                    AuthorityManager.getInstance().showInnerDialog(getContext());
                    return;
                }
                UIHelper.showFriendInfoActivity(getContext(), Long.toString(timeline.getUserId()));
                break;
            case R.id.common_timeline_item_view_time_iv:
                if (AuthorityManager.getInstance().isInnerAuthority()) {
                    AuthorityManager.getInstance().showInnerDialog(context);
                    break;
                }
                showLongClickDialog(timeline);
                break;
            case R.id.timeline_content_rl:
            case R.id.timeline_content_lay:
                UIHelper.showTimelineActivity(getContext(), timeline);
                break;
            case R.id.timeline_item_comment_lay:
                // 如果评论数大于0跳转到动态详情页面 为0跳转到发布评论界面
                if (timeline.getCommentCount() > 0) {
                    UIHelper.showTimelineActivity(getContext(), timeline);
                } else {
                    if (AuthorityManager.getInstance().isInnerAuthority()) {//游客可以查看动态详情，但是不能评论
                        AuthorityManager.getInstance().showInnerDialog(getContext());
                        return;
                    }
                    UIHelper.showSendCommentActivity(getContext(), timeline, Constants.REPLY_TIMELINE, 1);
                }
                break;
        }
    }
    
    /**
     * 长按Dialog操作
     * @param timeline
     */
    private void showLongClickDialog(final Timeline timeline) {
        Log.e("TimelineView", "====弹窗====");
        String userId = String.valueOf(timeline.getUserId());
        if (!userId.equals(App.getUserId())) {// 如果不是自己的动态则直接返回
            return;
        }
        // 列表长按事件
        Builder builder = new Builder(context);
        View view = LayoutInflater.from(context).inflate(R.layout.playground_list_dialog, null);
        // 初始化Dialog中的控件
        LinearLayout mCommentDialogLL = (LinearLayout) view.findViewById(R.id.playground_list_comment_ll);// 评论
        mCommentDialogLL.setVisibility(View.GONE);
        LinearLayout mPersonInfoDialogLL = (LinearLayout) view.findViewById(R.id.playground_person_info_ll);// 查看个人信息
        TextView mPlaygroundPersonInfoTV = (TextView) view.findViewById(R.id.playground_person_info_tv);
        /* final Timeline timeline= mCommentTimelineList.get(clickNum); */
        
        if (String.valueOf(timeline.getUserId()).equals(App.getUserId())) {// 如果是自己的动态则可以删除
            mPlaygroundPersonInfoTV.setText(getResources().getString(R.string.del_comment));
        } else {
            mPlaygroundPersonInfoTV.setText(getResources().getString(R.string.see_personal_info));
        }
        builder.setView(view);// 设置自定义布局view
        final AlertDialog mDialog = builder.show();
        mPersonInfoDialogLL.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                String userId = String.valueOf(timeline.getUserId());
                if (userId.equals(App.getUserId())) {// 如果是自己的动态则可以删除
                    // TODO 删除动态
                    TimelineUtil.getInstance(context).deleteWeibo(userId, timeline.getTimelineId(),deleteCallBack);
                } else {
                    // 点击查看个人信息
                    Log.e("plaza", "click mPersonInfoDialogLL...");
                    String userIdValue = "";
                    userIdValue = Long.toString(timeline.getUserId());
                    UIHelper.showFriendInfoActivity(context, userIdValue);
                }
                mDialog.dismiss();
            }
        });
    }
    
}
