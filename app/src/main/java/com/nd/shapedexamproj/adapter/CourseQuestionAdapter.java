package com.nd.shapedexamproj.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.nd.shapedexamproj.R;
import com.nd.shapedexamproj.model.course.CourseQuestion;

import java.util.ArrayList;
import java.util.List;

/**
 * 课程答疑适配器。
 * @author Xiezz
 * @date 2014-06-19
 * */
public class CourseQuestionAdapter extends BaseAdapter{
	
	private Context mContext;
	private List<CourseQuestion> mListItems = new ArrayList<CourseQuestion>();

	public CourseQuestionAdapter(Context context, List<CourseQuestion> data) { 
		this.mContext = context; 
		this.mListItems = data;
	}

	@Override
	public int getCount() {  
		return mListItems.size(); 
	}

	@Override
	public Object getItem(int position) { 
		return null;
	}

	@Override
	public long getItemId(int position) { 
		return 0;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		ViewHolder holder = null;
		if (convertView == null) {
			holder = new ViewHolder();
			convertView = LayoutInflater.from(mContext).inflate(R.layout.course_answer_list_item, null);
			holder.questionTv = (TextView) convertView.findViewById(R.id.course_answer_list_item_question_tv);
			holder.dividerLine = (View) convertView.findViewById(R.id.course_answer_divider_line);
			/*holder.answerTv = (TextView) convertView.findViewById(R.id.course_answer_list_item_answer_tv);
			holder.course_answer_list_item_photo_iv = (CircularImage) convertView.findViewById(R.id.course_answer_list_item_photo_iv);
			holder.course_answer_list_item_username_tv = (TextView) convertView.findViewById(R.id.course_answer_list_item_username_tv);*/
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		
		holder.questionTv.setText(mListItems.get(position).getTitle());
		if (position == getCount() - 1) {
		    holder.dividerLine.setVisibility(View.GONE);
		} else {
		    holder.dividerLine.setVisibility(View.VISIBLE); 
		}
		/*holder.answerTv.setText(mListItems.get(position).getQuestion());
		holder.course_answer_list_item_username_tv.setText(mListItems.get(position).getAskerName());
		ImageUtil.asyncLoadImage(holder.course_answer_list_item_photo_iv, mListItems.get(position).getAskerAvatar(), R.drawable.all_use_icon_photo);
		holder.course_answer_list_item_username_tv.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) { 
			    if (AuthorityManager.getInstance()
                        .isInnerAuthority()) {
                    AuthorityManager.getInstance().showInnerDialog(mContext);
                    return;
                }
				UIHelper.showFriendInfoActivity(mContext, mListItems.get(position).getAskerStu());
			}
		});
		holder.course_answer_list_item_photo_iv.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) { 
			    if (AuthorityManager.getInstance()
                        .isInnerAuthority()) {
                    AuthorityManager.getInstance().showInnerDialog(mContext);
                    return;
                }
				UIHelper.showFriendInfoActivity(mContext, mListItems.get(position).getAskerStu());
			}
		});*/
		return convertView;
	}
	
	private class ViewHolder {
		private TextView questionTv;
		private View dividerLine;
		/*private CircularImage course_answer_list_item_photo_iv;
		private TextView course_answer_list_item_username_tv;*/
	}
}
