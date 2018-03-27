package com.nd.shapedexamproj.fragment;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.nd.shapedexamproj.R;
import com.nd.shapedexamproj.model.TeacherCourse;

import java.util.ArrayList;
import java.util.List;


/**
 * 教师任课 适配器
 * @version 1.0.0 
 * @author Abay Zhuang <br/>
 *		   Create at 2014-5-21
 */
public class TeacherCourseAdapter extends BaseAdapter {
	private final static String TAG = TeacherCourseAdapter.class.getSimpleName();
	private LayoutInflater inflater;
	public List<TeacherCourse.CourseInfo> mLst = new ArrayList<TeacherCourse.CourseInfo>();

	public TeacherCourseAdapter(Context context, TeacherCourse teacherCourse){
		inflater = LayoutInflater.from(context);
		setTeacherCourse(teacherCourse);
	}
	
	public void setTeacherCourse(TeacherCourse teacherCourse){
		if (teacherCourse != null && teacherCourse.getList() != null) {
			mLst.clear();
			mLst.addAll(teacherCourse.getList());
			Log.e(TAG, "count :" + mLst.size());
			notifyDataSetChanged();
		}
	}
	
	public List<TeacherCourse.CourseInfo> getList(){
		return mLst;
	}
	
	@Override
	public int getCount() {
		return mLst.size();
	}

	@Override
	public Object getItem(int position) {
		if (position < mLst.size())
			return mLst.get(position);
		return null;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.course_4_teacher_item, null);
			holder = new ViewHolder();
			holder.courseName = (TextView)convertView.findViewById(R.id.course_teach_name_tv);
			holder.courseCount = (TextView)convertView.findViewById(R.id.course_teach_count_tv);
			convertView.setTag(holder);
		}else {
			holder = (ViewHolder)convertView.getTag();
		}
		
		TeacherCourse.CourseInfo info = mLst.get(position);
		if (info != null) {
		   if (info.getName() != null)
			   holder.courseName.setText(info.getName());
		   holder.courseCount.setText(""+info.getStudents());
		}
		
		return convertView;

	}

	static class ViewHolder{
		TextView courseName;
		TextView courseCount;
	}
}
