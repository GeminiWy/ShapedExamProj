package com.nd.shapedexamproj.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.nd.shapedexamproj.R;
import com.nd.shapedexamproj.model.note.NoteInfo;
import com.nd.shapedexamproj.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author Linlg
 * 
 */
public class NoteAdapter extends BaseAdapter {
	private Context mContext;
	private List<NoteInfo> mDatas = new ArrayList<NoteInfo>();

	public NoteAdapter(Context context) {
		this.mContext = context;
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

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ItemViewHolder holder = null;
		if (convertView == null) {
			convertView = LayoutInflater.from(mContext).inflate(
					R.layout.videoview_menu_note_item, null);
			holder = new ItemViewHolder();
			holder.timeTv = (TextView) convertView
					.findViewById(R.id.videoview_menu_note_item_tv);
			convertView.setTag(holder);
		} else {
			holder = (ItemViewHolder) convertView.getTag();
		}
		holder.timeTv.setText("" + StringUtils.generateTime(mDatas.get(position).noteTime));
		return convertView;
	}

	private List<NoteInfo> mDatasAddCache = new ArrayList<NoteInfo>();

	public void addItem(NoteInfo data) {
		mDatasAddCache.add(data);
	}

	class ItemViewHolder {
		private TextView timeTv;
	}

	public void addItemCollection(List<NoteInfo> datas) {
		mDatasAddCache.addAll(datas);
	}

	public void replaceItem(List<NoteInfo> oldData, List<NoteInfo> newData) {
		// 如果不存在旧数据
		if (oldData == null || oldData.size() == 0) {
			mDatasAddCache.addAll(newData);
			notifyDataSetChanged();
			return;
		}

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
		super.notifyDataSetChanged();
	}

	@Override
	public void notifyDataSetChanged() {
		if (isCleared) {
			mDatas.clear();
			isCleared = false;
		}
		mDatas.addAll(mDatasAddCache);
		mDatasAddCache.clear();
		super.notifyDataSetChanged();
	}

	boolean isCleared = false;

	public void clear() {
		mDatasAddCache.clear();
		isCleared = true;
	}

}
