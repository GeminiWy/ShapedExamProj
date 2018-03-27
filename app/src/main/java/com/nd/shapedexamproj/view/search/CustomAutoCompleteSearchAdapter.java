package com.nd.shapedexamproj.view.search;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.*;

import com.nd.shapedexamproj.R;

import java.util.ArrayList;
import java.util.List;

public class CustomAutoCompleteSearchAdapter extends BaseAdapter implements Filterable { 
	private static final String PREFERENCES_HISTORY_DATA = "search_history";
	
	private Context mContext;
	private ArrayFilter mFilter;
	private ArrayList<String> mOriginalValues;// 所有的Item
	private List<String> mObjects;// 过滤后的item
	private final Object mLock = new Object();
	private int mMaxMatch = 5;// 最多显示多少个选项,负数表示全部
	private SharedPreferences mSharedPreferences = null;

	/**
	 * @param mOriginalValues 保存搜索记录的列表
	 * @param maxMatch 保留的最大搜索记录数
	 * */
	public CustomAutoCompleteSearchAdapter(Context context, ArrayList<String> mOriginalValues, int maxMatch, SharedPreferences preferences) {
		this.mContext = context;
		this.mOriginalValues = mOriginalValues;
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
			LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = inflater.inflate(R.layout.list_item_for_custom_search, null);
			
			holder.headerIv = (ImageView) convertView.findViewById(R.id.search_recoder_content_item_head_iv);
			holder.contentTv = (TextView) convertView.findViewById(R.id.search_recoder_content_item_tv);
			holder.delIv = (ImageView) convertView.findViewById(R.id.search_recoder_content_item_del_iv);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		final TextView contentTv = holder.contentTv;
		holder.headerIv.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) { 
				contentTv.setText(mObjects.get(position));
			}
		});
		holder.contentTv.setText(mObjects.get(position));
        holder.delIv.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                String obj = mObjects.remove(position);
                removeHistory(obj);
                mOriginalValues.remove(obj);
                notifyDataSetChanged();
            }
        });
		return convertView;
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
		String history = mSharedPreferences.getString(PREFERENCES_HISTORY_DATA, "");
		String[] msgs = history.split(",");
		history = "";
		for (int i = 0; i < msgs.length; i++) {
			if (msgs[i].equals(removeMsg)) {
				continue;
			} else {
				history = history + msgs[i] + ",";
			}
		}
		mSharedPreferences.edit().putString(PREFERENCES_HISTORY_DATA, history).commit();
	}

	class ViewHolder {
		TextView contentTv; //搜索记录内容文本
		ImageView headerIv; 
		ImageView delIv; //删除图像按钮
	}

	public ArrayList<String> getAllItems() {
		return mOriginalValues;
	}
}
