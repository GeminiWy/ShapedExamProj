package com.nd.shapedexamproj.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.nd.shapedexamproj.R;
import com.nd.shapedexamproj.model.course.CoachTopic;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * 
 * @ClassName: CoachDiscListAdapter
 * @Title:
 * @Description:课堂-辅导讨论区-帖子列表 适配器
 * @Author:XueWenJian
 * @Since:2014年5月21日10:45:19
 * @Version:1.0
 */
public class CoachDiscListAdapter extends BaseAdapter {

	private final static String TAG = "CoachDiscListAdapter";
	
	private Context mContext;
	private List<CoachTopic> mCoachDiscTopics;
	
	public CoachDiscListAdapter(Context context, List<CoachTopic> coachDiscTopics)
	{
		mContext = context;
		mCoachDiscTopics = coachDiscTopics;
	}
	
	@Override
	public int getCount() {
		return mCoachDiscTopics.size();
	}

	@Override
	public Object getItem(int arg0) {
		return mCoachDiscTopics.get(arg0);
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
	            convertView = View.inflate(mContext, R.layout.course_coachdisc_topic_item, null);

	            convertView.setTag(viewHolder);
	        } else {
	        	viewHolder = (ViewHolder) convertView.getTag();
	        }
	        
	        viewHolder.coachdisc_topic_author_tv = (TextView)convertView.findViewById(R.id.coachdisc_topic_author_tv);
	        viewHolder.coachdisc_topic_time_tv = (TextView)convertView.findViewById(R.id.coachdisc_topic_time_tv);
	        viewHolder.coachdisc_topic_title_tv = (TextView)convertView.findViewById(R.id.coachdisc_topic_title_tv);
	        viewHolder.coachdisc_topic_replynum_tv = (TextView)convertView.findViewById(R.id.coachdisc_topic_replynum_tv);
	        
	        
	        CoachTopic coachTopic = null;
	        if (position < mCoachDiscTopics.size()) {
	        	coachTopic = mCoachDiscTopics.get(position);
	        	viewHolder.init(coachTopic);
	        }

	        return convertView;

	    }


	    @Override
		public boolean isEnabled(int position) {
			return super.isEnabled(position);
		}


		class ViewHolder {
			
	        TextView coachdisc_topic_title_tv;
	        TextView coachdisc_topic_time_tv;
	        TextView coachdisc_topic_author_tv;
	        TextView coachdisc_topic_replynum_tv;

	        public void init(CoachTopic coachTopic) {
	        	coachdisc_topic_title_tv.setText(coachTopic.getTitle());
	        	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
	        	String date = sdf.format(new Date(coachTopic.getAddTimeStamp()*1000));
	        	coachdisc_topic_time_tv.setText(date);
	        	coachdisc_topic_author_tv.setText(mContext.getText(R.string.coach_by_author)+coachTopic.getAuthorName());
	        	coachdisc_topic_replynum_tv.setText("" + coachTopic.getRelyNum());
	        }
	    }

}
