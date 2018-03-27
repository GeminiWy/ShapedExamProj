package com.nd.shapedexamproj.fragment;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.nd.shapedexamproj.R;
import com.nd.shapedexamproj.model.TeacherClasses;

import java.util.ArrayList;
import java.util.List;

/**
 * 教师负责班级 适配器
 * @version 1.0.0 
 * @author Abay Zhuang <br/>
 *		   Create at 2014-5-21
 */
public class TeacherClassesAdapter extends BaseAdapter {
	private final static String TAG = TeacherClassesAdapter.class.getSimpleName();
	private LayoutInflater inflater;
	public List<TeacherClasses.ClassInfo> mLst = new ArrayList<TeacherClasses.ClassInfo>();

	public TeacherClassesAdapter(Context context, TeacherClasses teacherCourse){
		inflater = LayoutInflater.from(context);
		setTeacherClasses(teacherCourse);
	}
	
	public void setTeacherClasses(TeacherClasses teacherCourse){
		if (teacherCourse != null && teacherCourse.getList() != null) {
			mLst.clear();
			mLst.addAll(teacherCourse.getList());
			Log.e(TAG, "count :" + mLst.size());
			notifyDataSetChanged();
		}
	}
	
	public List<TeacherClasses.ClassInfo> getList(){
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
			convertView = inflater.inflate(R.layout.course_4_teacher_classes_item, null);
			holder = new ViewHolder();
			holder.className = (TextView)convertView.findViewById(R.id.course_classes_name_tv);
			holder.classCount = (TextView)convertView.findViewById(R.id.course_classes_count_tv);
			convertView.setTag(holder);
		}else {
			holder = (ViewHolder)convertView.getTag();
		}
		
		TeacherClasses.ClassInfo info = mLst.get(position);
		if (info != null) {
		   holder.className.setText(info.term_name + " - " + info.getName());
		   holder.classCount.setText(""+info.getStudents());
		}
		
		return convertView;

	}

	static class ViewHolder{
		TextView className;
		TextView classCount;
	}
}
