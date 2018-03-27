package com.nd.shapedexamproj.adapter;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import com.nd.shapedexamproj.R;
import com.nd.shapedexamproj.util.Constants;

import java.util.ArrayList;
import java.util.List;

public class CustomLoginOptionsAdapter extends BaseAdapter implements Filterable{
	
	/**
	 * 删除选中的用户名数据
	 * */
	private static final int MSG_REMOVE_SELECT_ITEM_DATA = 52;  	
	private static final int MSG_GET_SELECT_ITEM_DATA = 51; 
 
    private Activity activity = null; 
	private Handler handler;
	private ArrayFilter mFilter;
	private ArrayList<String> mOriginalValues;// 所有的Item
	private List<String> mObjects;// 过滤后的item
	private final Object mLock = new Object();
	private int mMaxMatch = 3;// 最多显示多少个选项,负数表示全部
	private SharedPreferences mSharedPreferences = null;
	
    
	/**
	 * 自定义构造方法
	 * @param activity
	 * @param handler
	 * @param
	 */
    public CustomLoginOptionsAdapter(Activity activity,Handler handler, int maxMatch, ArrayList<String> data, SharedPreferences preferences){
    	this.activity = activity;
    	this.handler = handler;
    	this.mObjects = data;
    	this.mMaxMatch = maxMatch;
    	this.mSharedPreferences = preferences;
    }
	
	@Override
	public Filter getFilter() { 
		if (mFilter == null) {
			mFilter = new ArrayFilter();
		}
		return mFilter;
	}

	private class ArrayFilter extends Filter {

		@Override
		protected FilterResults performFiltering(CharSequence prefix) { 
			FilterResults results = new FilterResults(); 

			if (prefix == null || prefix.length() == 0) {
				synchronized (mLock) {
					Log.i("tag", "mOriginalValues.size=" + mOriginalValues.size());
					ArrayList<String> list = new ArrayList<String>(mOriginalValues);
					results.values = list;
					results.count = list.size();
					return results;
				}
			} else {
//				String prefixString = prefix.toString().toLowerCase();
				final int count = mOriginalValues.size();
//				final ArrayList<String> newValues = new ArrayList<String>(count); 
				//返回所有
				results.values = mOriginalValues;
				results.count = mOriginalValues.size();  
			}

			return results;
		}

		@Override
		protected void publishResults(CharSequence constraint, FilterResults results) { 
			mObjects = (List<String>) results.values;
			if (results.count > 0) {
				notifyDataSetChanged();
			} else {
				notifyDataSetInvalidated();
			}
		}
	}

	
	@Override
	public int getCount() {
		if(mObjects != null){
			return mObjects.size();
		}
		return 0;
	}

	@Override
	public Object getItem(int position) {
		if(mObjects != null){
			return mObjects.get(position);
		}
		return null;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		ViewHolder holder = null; 
        if (convertView == null) { 
            holder = new ViewHolder(); 
            //下拉项布局
            convertView = LayoutInflater.from(activity).inflate(R.layout.list_item_for_custom_login, null);
			
			holder.contentTv = (TextView) convertView.findViewById(R.id.login_recoder_content_item_tv);
			holder.delIv = (ImageView) convertView.findViewById(R.id.login_recoder_content_item_del_iv);
            
            convertView.setTag(holder); 
        } else { 
            holder = (ViewHolder) convertView.getTag(); 
        } 
        
        holder.contentTv.setText(mObjects.get(position));
        
        //为下拉框选项文字部分设置事件，最终效果是点击将其文字填充到文本框
        holder.contentTv.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Message msg = new Message();
				Bundle data = new Bundle();
				//设置选中索引
				data.putInt("selIndex", position);
				msg.setData(data);
				msg.what = MSG_GET_SELECT_ITEM_DATA;
				//发出消息
				handler.sendMessage(msg);
			}
		}); 
        
        //为下拉框选项删除图标部分设置事件，最终效果是点击将该选项删除
        holder.delIv.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Message msg = new Message();
				Bundle data = new Bundle();
				//设置删除索引
				data.putInt("delIndex", position);
				msg.setData(data);
				msg.what = MSG_REMOVE_SELECT_ITEM_DATA;
				//发出消息
				handler.sendMessage(msg); 
//				String obj = mObjects.remove(position);
//				removeHistory(obj);
//				if (mOriginalValues != null && mOriginalValues.size() > 0) {
//					mOriginalValues.remove(obj);
//				}
//				notifyDataSetChanged();
			}
		});
        
        return convertView; 
	}

	class ViewHolder { 
		TextView contentTv; //登录用户 
		ImageView delIv; //删除图像按钮
	} 
	
	/**
	 * 
	 * @param removeMsg    要移除的字符串
	 */
	private void removeHistory(String removeMsg) {
		/**
		 * 在hisroy中要移除removemsg的话，不能直接移除，因为如果要用indexof找removeMsg的话，
		 * 很可能是在别的字符串中间，如要移除abc，有个aabcc； 如果要用removeMsg +
		 * ","这种方法的话，很有可能遇到如要删除，abc+","最后删除了aabc+","； 如果要用"," + removeMsg +
		 * ","的话，那么很有可能要删除的就在第一个位置，那么就无法找到。 所以经过如下方法，是最可靠的方法，虽然可能有点繁琐。
		 */
		String history = mSharedPreferences.getString(Constants.SHARE_PREFERENCES_USERNAME_DATA, "");
		String[] msgs = history.split(",");
		history = "";
		for (int i = 0; i < msgs.length; i++) {
			if (msgs[i].equals(removeMsg)) {
				continue;
			} else {
				history = history + msgs[i] + ",";
			}
		}
		mSharedPreferences.edit().putString(Constants.SHARE_PREFERENCES_USERNAME_DATA, history).commit();
	}
}




 