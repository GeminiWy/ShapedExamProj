package com.nd.shapedexamproj.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.nd.shapedexamproj.R;
import com.nd.shapedexamproj.model.course.CoachTopicDetail;
import com.nd.shapedexamproj.model.course.CoachTopicReply;
import com.nd.shapedexamproj.model.my.IndustryInfo;
import com.nd.shapedexamproj.model.my.PersonalInfo;
import com.nd.shapedexamproj.util.ImageUtil;
import com.nd.shapedexamproj.util.IndustryUtil;
import com.nd.shapedexamproj.view.CircularImage;
import com.nd.shapedexamproj.view.InputBottomBar;
import com.nd.shapedexamproj.view.LinkView;

import java.util.List;

/**
 * 
 * @ClassName: CoachTopicDetailAdapter
 * @Title:
 * @Description:课堂-辅导讨论区-帖子详情 适配器
 * @Author:XueWenJian
 * @Since:2014年5月22日10:50:17
 * @Version:1.0
 */
public class CoachTopicDetailAdapter extends BaseAdapter{


	private final static String TAG = "CoachDiscListAdapter";
	
	private Context mContext;
	private CoachTopicDetail mCoachTopicDetail;
	private List<CoachTopicReply> mCoachTopicReplys;
	
	public CoachTopicDetailAdapter(Context context, CoachTopicDetail coachTopicDetail, List<CoachTopicReply> coachTopicReplys)
	{
		mContext = context;
		mCoachTopicDetail = coachTopicDetail;
		mCoachTopicReplys = coachTopicReplys;
	}
	
	@Override
	public int getCount() {
		return mCoachTopicReplys.size();
	}

	@Override
	public Object getItem(int arg0) {
		
		return mCoachTopicReplys.get(arg0);
	}

	@Override
	public long getItemId(int arg0) {
		return 0;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		ViewHolder viewHolder = null;
        if (null == convertView) {
        	viewHolder = new ViewHolder();
            convertView = View.inflate(mContext, R.layout.course_coachdisc_topicdetail_item, null);

            convertView.setTag(viewHolder);
        } else {
        	viewHolder = (ViewHolder) convertView.getTag();
        }
        
        viewHolder.coach_topicdetail_userimage_iv = (CircularImage)convertView.findViewById(R.id.coach_topicdetail_userimage_iv);
        viewHolder.coach_topicdetail_username_tv = (TextView)convertView.findViewById(R.id.coach_topicdetail_username_tv);
        viewHolder.coach_topicdetail_type_tv = (TextView)convertView.findViewById(R.id.coach_topicdetail_type_tv);
        viewHolder.coach_topicdetail_content_tv = (LinkView)convertView.findViewById(R.id.coach_topicdetail_content_tv);
        viewHolder.coach_topicdetail_more_iv = (ImageView)convertView.findViewById(R.id.coach_topicdetail_more_iv);
        viewHolder.coach_topicdetail_usergender_iv = (ImageView)convertView.findViewById(R.id.coach_topicdetail_usergender_iv);
        viewHolder.coach_topicdetail_userage_tv = (TextView)convertView.findViewById(R.id.coach_topicdetail_userage_tv);
        viewHolder.coach_topicdetail_stuinfo_ll = (LinearLayout)convertView.findViewById(R.id.coach_topicdetail_stuinfo_ll);
        viewHolder.coach_topicdetail_otherinfo_ll = (LinearLayout)convertView.findViewById(R.id.coach_topicdetail_otherinfo_ll);
        viewHolder.coach_topicdetail_userclassify_tv = (TextView)convertView.findViewById(R.id.coach_topicdetail_userclassify_tv);
        
        CoachTopicReply coachTopicReply = null;
        if (position < mCoachTopicReplys.size()) {
        	coachTopicReply = mCoachTopicReplys.get(position);
        	viewHolder.init(mCoachTopicDetail, coachTopicReply);

        	//TODO 关闭修改回复，删除回复功能，等待接口
        	/*//判断身份 
        	if(null != mCoachTopicDetail && mCoachTopicDetail.getAuthorId().equals(Constants.USER_ID))
    		{
        		//是否是 发帖人
        		viewHolder.coach_topicdetail_more_iv.setVisibility(View.VISIBLE);
        	}
        	else
        	{
        		//是否是回复人
                if(coachTopicReply.getAuthorId().equals(Constants.USER_ID))
                {
                	viewHolder.coach_topicdetail_more_iv.setVisibility(View.VISIBLE);
                }
                else
                {
                	viewHolder.coach_topicdetail_more_iv.setVisibility(View.GONE);
                }
        	}*/
        	
        	
        	viewHolder.coach_topicdetail_more_iv.setTag(position);
        	
        }

        return convertView;
	}
	
	class ViewHolder {
		CircularImage coach_topicdetail_userimage_iv;
		TextView coach_topicdetail_username_tv;
		TextView coach_topicdetail_type_tv;
		LinkView coach_topicdetail_content_tv;
		ImageView coach_topicdetail_more_iv;
		LinearLayout coach_topicdetail_stuinfo_ll;
		ImageView coach_topicdetail_usergender_iv;
		TextView coach_topicdetail_userage_tv;
		LinearLayout coach_topicdetail_otherinfo_ll;
		TextView coach_topicdetail_userclassify_tv;

        public void init(CoachTopicDetail coachTopicDetail, CoachTopicReply coachTopicReply) {
        	coach_topicdetail_username_tv.setText(coachTopicReply.getAuthorName());
        	//coach_topicdetail_type_tv.setText(coachTopicDetail.getTitle());
        	coach_topicdetail_content_tv.setText(InputBottomBar.parseFaceByText(mContext, coachTopicReply.getContent(),(int)coach_topicdetail_content_tv.getTextSize()));

			ImageUtil.asyncLoadImage(coach_topicdetail_userimage_iv, coachTopicReply.getAuthorAvatar(), R.drawable.all_use_icon_bg);

        	//判断回复人的身份
        	if(coachTopicReply.getAuthorType() == PersonalInfo.USER_TYPE_STUDENT)
        	{
        		coach_topicdetail_stuinfo_ll.setVisibility(View.VISIBLE);
        		coach_topicdetail_otherinfo_ll.setVisibility(View.GONE);
        		IndustryInfo industryInfo = IndustryUtil.getIndustryInfo(mContext, coachTopicReply.getIndustryId());
        		coach_topicdetail_userclassify_tv.setTextColor(industryInfo.getColor());
        		coach_topicdetail_userclassify_tv.setText(industryInfo.getAbbreviationIndustry());
        		if(coachTopicReply.getSex() == PersonalInfo.SEX_FEMALE)
                {
                    coach_topicdetail_usergender_iv.setBackgroundResource(R.drawable.classroom_icon_female);
                }
                else
                {
                    coach_topicdetail_usergender_iv.setBackgroundResource(R.drawable.classroom_icon_male);
                }
        	}
        	else
        	{
        		coach_topicdetail_stuinfo_ll.setVisibility(View.GONE);
        		coach_topicdetail_otherinfo_ll.setVisibility(View.VISIBLE);
        	}
        	
        	coach_topicdetail_userage_tv.setText(""+coachTopicReply.getAge());
        	
        }
        
        
    }

	public void setmCoachTopicDetail(CoachTopicDetail mCoachTopicDetail) {
		this.mCoachTopicDetail = mCoachTopicDetail;
	}

}
