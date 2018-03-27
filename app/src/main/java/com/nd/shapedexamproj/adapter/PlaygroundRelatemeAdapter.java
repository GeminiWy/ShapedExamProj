package com.nd.shapedexamproj.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.nd.shapedexamproj.App;
import com.nd.shapedexamproj.R;
import com.nd.shapedexamproj.model.playground.Timeline;
import com.nd.shapedexamproj.model.plaza.RelatemeBaseInfo;
import com.nd.shapedexamproj.net.ServerApi;
import com.nd.shapedexamproj.util.StringUtils;
import com.nd.shapedexamproj.util.TimelinePostTimeAgoUtil;
import com.nd.shapedexamproj.util.UIHelper;
import com.nd.shapedexamproj.view.InputBottomBar;
import com.nd.shapedexamproj.view.TimelineContentView;
import com.tming.common.adapter.ImageBaseAdatapter;
import com.tming.common.net.TmingHttp;
import com.tming.common.net.TmingHttp.RequestCallback;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

public class PlaygroundRelatemeAdapter extends ImageBaseAdatapter<RelatemeBaseInfo> {

    public PlaygroundRelatemeAdapter(Context context) {
        super(context);
        mSameClass = context.getResources().getString(R.string.playground_same_class);
        mSameCollege = context.getResources().getString(R.string.playground_same_college);
        mTeacher = context.getResources().getString(R.string.teacher);
    }

    private final String TAG = "PlaygroundRelatemeAdapter";
    private String mSameClass, mSameCollege,mTeacher;

    @SuppressLint("LongLogTag")
    @Override
    public View infateItemView(Context context) {
        View convertView = View.inflate(context, R.layout.playground_relateme_item, null);
        ViewHolder viewHolder = new ViewHolder();
        viewHolder.imageView = (ImageView) convertView.findViewById(R.id.relateme_user_photo);
        viewHolder.userNameTV = (TextView) convertView.findViewById(R.id.relateme_user_name);
        /*viewHolder.relatemeContentLL = (LinearLayout) convertView.findViewById(R.id.relateme_content_ll);
        viewHolder.relatemeContentTV = (TextView) convertView.findViewById(R.id.relateme_content_tv);
        viewHolder.relatemePictureListGV = (ShowGridViewImageView) convertView.findViewById(R.id.relateme_picture_list_gv);*/
        viewHolder.timelineContentView = (TimelineContentView) convertView.findViewById(R.id.timeline_content_lay);
        
        viewHolder.mClassTv = (TextView) convertView.findViewById(R.id.relateme_class_tv);
        viewHolder.relatemeCommentUserCommentTV = (TextView) convertView
                .findViewById(R.id.relateme_comment_user_comment_tv);
        viewHolder.relatemeTimeTV = (TextView) convertView.findViewById(R.id.relateme_time_tv);
        convertView.setTag(viewHolder);
        Log.i(TAG, "getCount:" + getCount());
        return convertView;
    }

    @Override
    public URL getDataImageUrl(RelatemeBaseInfo data) {
        URL url = null;
        if (data.getCommentTimelineInfo() != null
                && data.getDynamicTimelineInfo() != null) {
            try {
                url = new URL(data.getCommentTimelineInfo().getPhoto());
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        }
        return url;
    }

    @Override
    public long getItemId(RelatemeBaseInfo data) {
        if (data.getCommentTimelineInfo() != null) {
            return Long.valueOf(data.getCommentTimelineInfo().getTimelineId());
        } else {
            return 0;
        }
    }

    @Override
    public void setViewHolderData(BaseViewHolder holder, final RelatemeBaseInfo relatemeBaseInfo) {
        //PersonalInfo personalInfo = relatemeBaseInfo.getPersonalInfo();
        Timeline commentTimelineInfo = relatemeBaseInfo.getCommentTimelineInfo();
        Timeline dynamicTimelineInfo = relatemeBaseInfo.getDynamicTimelineInfo();
        
        if (commentTimelineInfo == null) {
            return;
        }
        
        String user_name = "";
        user_name = commentTimelineInfo.getUsername();
        if (user_name != null && !("".equals(user_name))) {
            ((ViewHolder) holder).userNameTV.setText(user_name);
        } else {
            ((ViewHolder) holder).userNameTV.setText("");
        }
        String commentContent = commentTimelineInfo.getContent();
        final List<String> commentImgUrlLst = UIHelper.parseImages(commentContent);
        if (commentImgUrlLst.size() == 0) {} else {
            commentContent = UIHelper.filterImageContent(commentContent);
        }
        
        if (App.sClassId.equals(Long.toString(commentTimelineInfo.getClassId()))) {
            if (App.getUserId().equals(commentTimelineInfo.getUserId())) {
                ((ViewHolder) holder).mClassTv.setText("");
            } else {
                ((ViewHolder) holder).mClassTv.setText(mSameClass);
            }
        } else {
            if (commentTimelineInfo.getUserType() == 0 || commentTimelineInfo.getUserType() == 1) {
                ((ViewHolder) holder).mClassTv.setText(mTeacher);
            } else {
                ((ViewHolder) holder).mClassTv.setText(mSameCollege);
            }
        }
        if (dynamicTimelineInfo != null) {
            //----------------------------------
            String commentPrifix = "";
            if (dynamicTimelineInfo.getType() == Timeline.TYPE_COMMENT || dynamicTimelineInfo.getType() == Timeline.TYPE_REPLY_COMMENT) {// 若回复的是一条评论
                commentPrifix = "回复我的评论：";
            } else if (dynamicTimelineInfo.getType() == Timeline.TYPE_ORIGINAL) {// 若回复的是一条动态
                commentPrifix = "回复我的动态：";
            }
            ((ViewHolder) holder).relatemeCommentUserCommentTV.setText(InputBottomBar.parseFaceByText(mContext,
                    commentPrifix + commentContent, (int) ((ViewHolder) holder).relatemeCommentUserCommentTV.getTextSize()));
            
            String originalPrifix = "";
            if (dynamicTimelineInfo.getType() == Timeline.TYPE_COMMENT || dynamicTimelineInfo.getType() == Timeline.TYPE_REPLY_COMMENT) {// 若回复的是一条评论
                originalPrifix = "原评论：";
            } else if (dynamicTimelineInfo.getType() == Timeline.TYPE_ORIGINAL) {// 若回复的是一条动态
                originalPrifix = "原动态：";
            }
            ((ViewHolder)holder).timelineContentView.setTimeline(originalPrifix,dynamicTimelineInfo);
            ((ViewHolder)holder).timelineContentView.setOnClickListener(new OnClickListener() {
                
                @SuppressLint("LongLogTag")
                @Override
                public void onClick(View v) {
                    Log.e(TAG, "=======点击原动态=====");
                    requestShowComment(relatemeBaseInfo);
                }
            });
            
            String relatemeTimeValue = "";
            long relatemeTime = relatemeBaseInfo.getOperateTime();
            DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(relatemeTime);
            relatemeTimeValue = formatter.format(calendar.getTime());
            String relatemeTimes = TimelinePostTimeAgoUtil.TimelinePostTimeAgo(relatemeTimeValue);
            ((ViewHolder) holder).relatemeTimeTV.setText(relatemeTimes);// 刷新发布时间
        } else {
            ((ViewHolder)holder).timelineContentView.setTimeline("",null);
        }
    }

    class ViewHolder extends BaseViewHolder {
        
        /*private LinearLayout relatemeContentLL;*/
        private TextView userNameTV, /*relatemeContentTV,*/ mClassTv, relatemeCommentUserCommentTV, relatemeTimeTV;// 用户名称、与我相关内容、评论用户名、评论时间
        /*private ShowGridViewImageView relatemePictureListGV;*/
        private TimelineContentView timelineContentView;
    }
    
    /**
     * 
     * 方法作用：requestShowComment: 查看原动态<br/>
     * 
     * @author xujs
     * @param relatemeBaseInfoes
     * @since JDK 1.6
     */
    private void requestShowComment(RelatemeBaseInfo relatemeBaseInfoes) {
        String timelineidVal = "";
        Timeline dynamicTimelineInfo = relatemeBaseInfoes.getDynamicTimelineInfo();
        Timeline commentTimelineInfo = relatemeBaseInfoes.getCommentTimelineInfo();
        
        if(StringUtils.isEmpty(dynamicTimelineInfo.getOriginalTimelineId()) || "0".equals(dynamicTimelineInfo.getOriginalTimelineId())) {
            timelineidVal = String.valueOf(commentTimelineInfo.getReferId());
        } else {
            timelineidVal = String.valueOf(dynamicTimelineInfo.getOriginalTimelineId());
        }
        requestDynamicInfo(timelineidVal);
        
    }
    
    /**
     * 请求动态信息
     */
    private void requestDynamicInfo(String timelineid) {
        TmingHttp.asyncRequest(ServerApi.Timeline.getTimeline(timelineid), null, new RequestCallback<Timeline>() {

                    @Override
                    public Timeline onReqestSuccess(String respones)
                            throws Exception {
                        return dynamicInfoList(respones);
                    }

                    @Override
                    public void success(Timeline respones) {
                        if(respones == null) {
                            UIHelper.ToastMessage(mContext, "无法获取原动态数据，请重试");
                            return;
                        }
                        UIHelper.showTimelineActivity(mContext, respones);
                        
                    }

                    @Override
                    public void exception(Exception exception) {
                        UIHelper.ToastMessage(mContext, "无法获取原动态数据，请重试");
                        return;
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
        Timeline timelineInfo = null;
        try {
            JSONObject resObj = new JSONObject(result);
            if (!resObj.isNull("flag") && resObj.getInt("flag") == 1) {
                if (!resObj.isNull("data")) {
                    JSONArray dataAry = resObj.getJSONArray("data");
                    if (null != dataAry && dataAry.length() > 0) {
                        for (int i = 0; i < dataAry.length(); i++) {
                            
                            timelineInfo = new Timeline();
                            JSONObject item = dataAry.getJSONObject(i);
                            timelineInfo.initWithJsonObject(item);
                        }
                    } 
                }
            } 

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return timelineInfo;
    }
    
}
