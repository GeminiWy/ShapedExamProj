package com.nd.shapedexamproj.adapter;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.nd.shapedexamproj.R;
import com.nd.shapedexamproj.activity.course.CourseQuestionDetailActivity;
import com.nd.shapedexamproj.model.my.MyQuestion;
import com.tming.common.util.Log;

import java.util.*;

/**
 * “我的提问”适配器
 * 
 * @author Linlg
 * 
 */
public class MyQuestionAdapter extends BaseAdapter implements OnItemClickListener{

	private final static String TAG = "MyQuestionAdapter";
	
	private Activity mContext;
	private List<MyQuestion> mDatas = new ArrayList<MyQuestion>();
	public static final int VIDEO = 1;// 视频内布局
	public static final int MY = 0;// 我的模块布局

	private int flag = VIDEO;

	public MyQuestionAdapter(Activity context, int flag) {
		this.mContext = context;
		this.flag = flag;
	}

	@Override
	public int getCount() {
		return mDatas.size();
	}

	@Override
	public Object getItem(int position) {
		return mDatas.get(position);
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@SuppressWarnings("null")
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ItemViewHolder holder = null;

		if (convertView == null) {
			holder = new ItemViewHolder();
			if (flag == VIDEO) {
				convertView = LayoutInflater.from(mContext).inflate(
						R.layout.videoview_question_item, null);

				holder.descTv = (TextView) convertView
						.findViewById(R.id.video_question_item_desc_tv);
				holder.timeTv = (TextView) convertView
						.findViewById(R.id.video_question_item_date_tv);
				holder.statusIv = (ImageView) convertView
						.findViewById(R.id.video_question_item_icon_iv);
			} else if (flag == MY) {
				convertView = LayoutInflater.from(mContext).inflate(
						R.layout.my_question_item, null);
				holder.statusIv = (ImageView) convertView
						.findViewById(R.id.myquestion_left_img);
				holder.descTv = (TextView) convertView
						.findViewById(R.id.myquestion_item_tv);
			}

			convertView.setTag(holder);
		} else {
			holder = (ItemViewHolder) convertView.getTag();
		}

		MyQuestion info = mDatas.get(position);

		holder.descTv.setText(info.title);
		if (flag == VIDEO) {
			holder.timeTv.setText(getDateStr(Long.parseLong(info.add_time) * 1000));
		}
		if (info.getAnswer_status() == MyQuestion.QUESTION_STATE_ANSWERED) {
			holder.statusIv
			.setImageResource(R.drawable.classroom_icon_kj_ask_2);
		} else {
			holder.statusIv.setImageResource(R.drawable.my_ask1);
		}
		
		if (info.getIs_ignore() == MyQuestion.QUESTION_IGNORE_YES) {
			holder.statusIv.setImageResource(R.drawable.my_ask3);
		}

		return convertView;
	}

	class ItemViewHolder {
		private TextView descTv, timeTv;
		private ImageView statusIv;
	}

	private List<MyQuestion> mDatasAddCache = new ArrayList<MyQuestion>();

	public void addItem(MyQuestion data) {
		mDatasAddCache.add(data);
	}

	public void addItemCollection(List<MyQuestion> datas) {
		mDatasAddCache.addAll(datas);
	}

	public void replaceItem(List<MyQuestion> oldData, List<MyQuestion> newData) {
		// 如果不存在旧数据
		if (oldData == null || oldData.size() == 0) {
			mDatasAddCache.addAll(newData);
			notifyDataSetChanged();
			return;
		}

		{
			for(MyQuestion entity: mDatas) {
				Log.e(TAG, entity.getTitle());
			}
		}
		Log.e(TAG, "=====================");
		// 做替换
		int oldStartIndex = mDatas.indexOf(oldData.get(0));
		int oldEndIndex = mDatas.indexOf(oldData.get(oldData.size() - 1));
		if (oldStartIndex != -1 && oldEndIndex != -1) {
			for (int i = oldStartIndex; i <= oldEndIndex; i++) {
				mDatas.remove(oldStartIndex);
			}
			int insertStartIndex = oldStartIndex;
			for (int i = 0; i < newData.size(); i++) {
				mDatas.add(insertStartIndex++, newData.get(i));
			}
		}
		
		{
			for(MyQuestion entity: mDatas) {
				Log.e(TAG, entity.getTitle());
			}
		}
		Log.e(TAG, "+++++++++++++++++++++");
		super.notifyDataSetChanged();
	}

	@Override
	public void notifyDataSetChanged() {
		/*if (isCleared) {
			mDatas.clear();
			isCleared = false;
		}
		mDatas.addAll(mDatasAddCache);
		mDatasAddCache.clear();*/
		for(int i = 0;i < mDatasAddCache.size();i ++){
			MyQuestion myQuestion = mDatasAddCache.get(i);
			if(!mDatas.contains(myQuestion)){
				mDatas.add(myQuestion);
			}
		}
		mDatasAddCache.clear();
		
		super.notifyDataSetChanged();
	}

	boolean isCleared = false;

	public void clear() {
		mDatasAddCache.clear();
		mDatas.clear();
		isCleared = true;
	}

	private static String getDateStr(long millis) {
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(millis);
		Formatter ft = new Formatter(Locale.CHINA);
		return ft.format("%1$tY-%1$tm-%1$td %1$tT", cal).toString();
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		Log.d(TAG, "position:"+position);
		MyQuestion myQuestion = mDatas.get(position-1);
		Intent intent = new Intent(mContext, CourseQuestionDetailActivity.class);
		Bundle bundle = new Bundle();
		bundle.putSerializable("question", myQuestion);
		intent.putExtras(bundle);
		mContext.startActivityForResult(intent, 0);

	} 
}
