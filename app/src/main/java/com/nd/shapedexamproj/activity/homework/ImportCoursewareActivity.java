package com.nd.shapedexamproj.activity.homework;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.*;
import android.widget.CompoundButton.OnCheckedChangeListener;

import com.nd.shapedexamproj.R;
import com.nd.shapedexamproj.activity.BaseActivity;
import com.nd.shapedexamproj.model.Video;

import java.util.ArrayList;
import java.util.List;

/**
 * 导入课件
 * @author zll
 * create in 2014-3-5
 */
public class ImportCoursewareActivity extends BaseActivity {
	
	private Button common_head_right_btn ;
	private ImageView commonheader_left_iv;
	private TextView commonheader_title_tv;
	private CheckBox import_courseware_ck;	//全选
	
	private ListView import_courseware_list ;	//列表
	private List<Video> coursewareList ;
	
	@Override
	public int initResource() {
		return R.layout.import_courseware;
	}

	@Override
	public void initData() {
		coursewareList = new ArrayList<Video>();
//		coursewareList.add(new Vedio("1", "知识概要"));
//		coursewareList.add(new Vedio("2", "理论与实践"));
		import_courseware_list.setAdapter(new LocalCoursewareListAdapter(this));
		
	}

	@Override
	public void initComponent() {
		//头部按钮
//		common_head_left_btn = (Button) findViewById(R.id.common_head_left_btn);
//		common_head_left_btn.setText(getResources().getString(R.string.import_ppt));
		commonheader_left_iv = (ImageView) findViewById(R.id.commonheader_left_iv);
		commonheader_title_tv = (TextView) findViewById(R.id.commonheader_title_tv);
		commonheader_title_tv.setText(getResources().getString(R.string.import_ppt));
		common_head_right_btn = (Button) findViewById(R.id.common_head_right_btn);
		common_head_right_btn.setText(getResources().getString(R.string.save));
		//全选复选框
		import_courseware_ck = (CheckBox) findViewById(R.id.import_courseware_ck);
		//本地课件列表
		import_courseware_list = (ListView) findViewById(R.id.import_courseware_list);
		
		
	}

	@Override
	public void addListener() {
		commonheader_left_iv.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
			}
		});
		common_head_right_btn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
			}
		});
		import_courseware_ck.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				
			}
		});
	}
	/**
	 * 本地课件列表
	 */
	private class LocalCoursewareListAdapter extends BaseAdapter{
		
		private Context context;
		
		public LocalCoursewareListAdapter(Context context){
			this.context = context;
		}
		
		@Override
		public int getCount() {
			return coursewareList.size();
		}

		@Override
		public Object getItem(int position) {
			return coursewareList.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder = null;
			if (convertView == null){
				holder = new ViewHolder();
				convertView = LayoutInflater.from(context).inflate(R.layout.import_courseware_item, null);
				holder.import_courseware_item_tv = (TextView) convertView.findViewById(R.id.import_courseware_item_tv);
				
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			holder.import_courseware_item_tv.setText(coursewareList.get(position).video_name);
			
			return convertView;
		}
		
	}
	
	private class ViewHolder{
		private TextView import_courseware_item_tv;
	}
	
}
