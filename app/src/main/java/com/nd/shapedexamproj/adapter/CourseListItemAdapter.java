package com.nd.shapedexamproj.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nd.shapedexamproj.R;
import com.nd.shapedexamproj.model.Course;
import com.nd.shapedexamproj.util.UIHelper;
import com.tming.common.adapter.CommonBaseAdapter;
/**
 * 课程列表适配器
 * @author zll
 *
 */
public class CourseListItemAdapter extends CommonBaseAdapter<Course> implements OnItemClickListener{
	int positionOffset;
	
	public CourseListItemAdapter(Context context,int positionOffset) {
		super(context);
		this.positionOffset = positionOffset ;
	}

	@Override
	public void setViewHolderData(BaseViewHolder holder,
			final Course data) {
		ViewHolder vHolder = (ViewHolder)holder ;
		vHolder.courseCategoryNameTV.setText(data.course_name);
		vHolder.courseCategoryLayout.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				UIHelper.showCourseDetail(mContext, data.course_id, data.course_name);
			}
		});
	}

	@Override
	public View infateItemView(Context context) {
		ViewHolder viewHolder = new ViewHolder();
		View view = LayoutInflater.from(context).inflate(
				R.layout.course_profressional_item, null);

		viewHolder.courseCategoryLayout = (RelativeLayout) view
				.findViewById(R.id.course_category_lay);
		viewHolder.courseCategoryNameTV = (TextView) view
				.findViewById(R.id.course_categoryname);

		view.setTag(viewHolder);
		
		return view;
	}

	
	private final class ViewHolder extends BaseViewHolder {
		private RelativeLayout courseCategoryLayout;
		private TextView courseCategoryNameTV;
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
	}

	
}
