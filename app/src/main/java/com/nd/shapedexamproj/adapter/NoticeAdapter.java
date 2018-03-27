package com.nd.shapedexamproj.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.nd.shapedexamproj.R;
import com.nd.shapedexamproj.model.notice.NoticeInfo;
import com.nd.shapedexamproj.util.DateUtils;
import com.tming.common.util.Log;

import java.util.List;

/**
 * @Title: NoticeAdapter
 * @Description: 系统公告适配器
 * @author WuYuLong
 * @date 2014-5-7
 * @version V1.0
 */
public class NoticeAdapter extends BaseAdapter {
	private static final String TAG = "NoticeAdapter";
	private Context context;
	private List<NoticeInfo> noticeList;
	
	public NoticeAdapter(Context context, List<NoticeInfo> noticeList){
		this.noticeList = noticeList;
		this.context = context;
	}

	@Override
	public int getCount() {
		return noticeList.size();
	}

	@Override
	public Object getItem(int arg0) {
		return noticeList.get(arg0);
	}

	@Override
	public long getItemId(int arg0) {
		return arg0;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup arg2) {
		ViewHolder holder = null;
		if (convertView == null) {
			holder = new ViewHolder();
			LayoutInflater inflater = LayoutInflater.from(context);
			convertView = inflater.inflate(R.layout.notice_list_item, null);
			
			holder.notice_list_item_title_tv = (TextView)convertView.findViewById(R.id.notice_list_item_title_tv);
			holder.notice_list_item_time_tv = (TextView)convertView.findViewById(R.id.notice_list_item_time_tv);
			holder.notice_list_item_content_tv = (TextView)convertView.findViewById(R.id.notice_list_item_content_tv);
			
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		
		if (noticeList.size() == 0) {
			return convertView ;
		}
		
		Log.e(TAG, noticeList.get(position).getTitle());
		if(TextUtils.isEmpty(noticeList.get(position).getTitle())){
			holder.notice_list_item_title_tv.setText("");
		}else{
			holder.notice_list_item_title_tv.setText(noticeList.get(position).getTitle());
		}
		
		String time = noticeList.get(position).getTime();
		time = DateUtils.getDate(time, "yyyy-MM-dd HH:mm:ss");
		holder.notice_list_item_time_tv.setText(time);
		
		String content = noticeList.get(position).getContent();
		content = subContent(content);
		holder.notice_list_item_content_tv.setText(content);

		return convertView;
	}
	
	public void replaceItem(List<NoticeInfo> oldData, List<NoticeInfo> newData) {
		// 如果不存在旧数据
		if (oldData == null || oldData.size() == 0) {
			noticeList.addAll(newData);
			notifyDataSetChanged();
			return;
		}
		
		// 做替换
		int oldStartIndex = noticeList.indexOf(oldData.get(0));
		int oldEndIndex = noticeList.indexOf(oldData.get(oldData.size() - 1));
		if (oldStartIndex != -1 && oldEndIndex != -1) {
			for (int i = oldStartIndex; i <= oldEndIndex; i++) {
				noticeList.remove(oldStartIndex);
			}
			int insertStartIndex = oldStartIndex; 
			for (int i = 0; i < newData.size(); i++) {
				noticeList.add(insertStartIndex++, newData.get(i));
			}
		}
		super.notifyDataSetChanged();
	}
	
	private final class ViewHolder {
		private TextView notice_list_item_title_tv;
		private TextView notice_list_item_time_tv;
		private TextView notice_list_item_content_tv;
	}
	
	/**
	 * 切割公告内容
	 * @param content
	 * @return
	 */
	private static String subContent(String content){
		int limitLen = 11;
		if(!"".equals(content)){
			int size = content.length();
			if(size > limitLen){
				content = content.substring(0, limitLen) + "...";
			}
		} else {
			content = "";
		}
		return content;
	}
}
