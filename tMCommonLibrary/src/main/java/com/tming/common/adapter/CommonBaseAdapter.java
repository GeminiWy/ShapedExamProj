package com.tming.common.adapter;

import java.util.ArrayList;
import java.util.List;

import android.os.Looper;
import com.tming.common.R;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
/**
 * 通用列表适配器
 * @author zll
 *
 * @param <T>
 */
public abstract class CommonBaseAdapter<T> extends BaseAdapter{

	protected Context mContext;
	protected List<T> datas = new ArrayList<T>();
	private List<Integer> selectedItemIndex;
	
	public CommonBaseAdapter(Context context) {
		mContext = context;
	}
	
	public CommonBaseAdapter(Context context, List<T> datas) {
		mContext = context;
		this.datas = datas;
	}
	
	@Override
	public int getCount() {
		return datas.size();
	}

	@Override
	public Object getItem(int position) {
		return datas.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = infateItemView(mContext);
		}
		BaseViewHolder holder = (BaseViewHolder) convertView.getTag();
		T data = datas.get(position);
		setViewHolderData(holder, data);
		
		if (selectedItemIndex != null) {
			if (selectedItemIndex.contains(position)) {
				convertView.setBackgroundColor(mContext.getResources().getColor(R.color.btn_pressed_gray));
			} else {
				convertView.setBackgroundResource(R.drawable.pop_item_selector);
			}
		}
		
		return convertView;
	}

	public abstract void setViewHolderData(BaseViewHolder holder, T data);

	public abstract View infateItemView(Context context);


	/*public abstract String getItemId(T data);*/
	
	
	public class BaseViewHolder {
		
	}
	
	public void addItem(T data) {
        // 判断是否在主线程中调用
        if (Looper.getMainLooper() != Looper.myLooper()) {
            throw new IllegalAccessError("Please call replaceItem method on main thread!");
        }
		datas.add(data);
	}
	
	public void addItemCollection(List<T> datas) {
        // 判断是否在主线程中调用
        if (Looper.getMainLooper() != Looper.myLooper()) {
            throw new IllegalAccessError("Please call replaceItem method on main thread!");
        }
        this.datas.addAll(datas);
	}
	
	public void replaceItem(List<T> oldData, List<T> newData) {
        // 判断是否在主线程中调用
        if (Looper.getMainLooper() != Looper.myLooper()) {
            throw new IllegalAccessError("Please call replaceItem method on main thread!");
        }
		// 如果不存在旧数据
		if (oldData == null || oldData.size() == 0) {
			datas.addAll(newData);
			notifyDataSetChanged();
			return;
		}
		
		// 做替换
		int oldStartIndex = datas.indexOf(oldData.get(0));
		int oldEndIndex = datas.indexOf(oldData.get(oldData.size() - 1));
		if (oldStartIndex != -1 && oldEndIndex != -1) {
            int removeLen = oldData.size();
            for (int i = 0; i < removeLen; i++) {
                datas.remove(oldStartIndex);
            }
            int insertStartIndex = oldStartIndex;
            int addLen = newData.size();
            for (int i = 0; i < addLen; i++) {
                datas.add(insertStartIndex++, newData.get(i));
            }
        }
		super.notifyDataSetChanged();
	}

	@Override
	public void notifyDataSetChanged() {
		super.notifyDataSetChanged();

	}

	public void clear() {
		datas.clear();
		notifyDataSetChanged();
	}
	
	public void setSelected(int index) {
		if (selectedItemIndex == null) {
			selectedItemIndex = new ArrayList<Integer>();
		}
		if (selectedItemIndex.contains(Integer.valueOf(index))) {
			selectedItemIndex.remove(Integer.valueOf(index));
		} else {
			selectedItemIndex.add(Integer.valueOf(index));
		}
	}
	
	
	public List<T> getSelectedItem() {
		ArrayList<T> list = new ArrayList<T>();
		if (selectedItemIndex != null) {
			for (int i = 0; i < selectedItemIndex.size(); i++) {
				int index = selectedItemIndex.get(i);
				list.add(datas.get(index));
			}
		}
		return list;
	}
	
	public List<Integer> getSelectedItemIndex() {
		return selectedItemIndex;
	}
	
	public void clearSelected() {
		if (selectedItemIndex != null) {
			selectedItemIndex.clear();
			notifyDataSetChanged();
		}
	}
	
}
