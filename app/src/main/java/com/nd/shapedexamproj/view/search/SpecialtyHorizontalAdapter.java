package com.nd.shapedexamproj.view.search;


import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.nd.shapedexamproj.R;
import com.nd.shapedexamproj.activity.search.SpecialtyInfo;

import java.util.List;

/**
 * 全局搜索 相关专业水平列表适配器。
 * @author xiezz
 * @date 2014-06-04
 * */
public class SpecialtyHorizontalAdapter extends BaseAdapter{ 
	private static final String TAG = "SpecialtyHorizontalAdapter";
	private Context mContext;  
	private List<SpecialtyInfo> mListItems; //数据集合
	/**
	 * 最多有多少项
	 * */
	private int mCountItems;  
	private int selectIndex = -1; 

	/**
	 * @param count 用来控制是否要限制显示子项个数,传入的值必须小于图像的个数，否则自动限制
	 * */
	public SpecialtyHorizontalAdapter(Context context, List<SpecialtyInfo> data, int count){
		this.mContext = context;
		this.mListItems = data; 
		this.mCountItems = count; 
	}
	
	@Override
	public int getCount() {	  
		if(mListItems != null){
			if((mCountItems > 0) && (mListItems.size() > mCountItems)){			
				return mCountItems; 
			}
			return mListItems.size();
		} 
		return 0;
		 
	}
	
	@Override
	public Object getItem(int position) {
		return position;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		ViewHolder holder;
		if(convertView == null){
			holder = new ViewHolder();
			convertView = LayoutInflater.from(mContext).inflate(R.layout.search_specialty_horizontal_list_item, null);
			holder.speicaltyTv = (TextView)convertView.findViewById(R.id.speicalty_tv);
			convertView.setTag(holder);
		}else{
			holder=(ViewHolder)convertView.getTag();
		}
		
//		if(mListItems.size() > 2){
//		    //取控件textView当前的布局参数  
//			LinearLayout.LayoutParams linearParams =(LinearLayout.LayoutParams) holder.speicaltyTv.getLayoutParams(); 
//			linearParams.height = (int)mContext.getResources().getDimension(R.dimen._46px); 			 
//			linearParams.width = (int)mContext.getResources().getDimension(R.dimen._100px);// 控件的宽强制设成30   
//			  
//			holder.speicaltyTv.setLayoutParams(linearParams); 
//		}
		
		if(position == selectIndex){
			convertView.setSelected(true);
		}else{
			convertView.setSelected(false);
		}
		
		if(!TextUtils.isEmpty(mListItems.get(position).getProName())){ 
			holder.speicaltyTv.setText(mListItems.get(position).getProName());
		} else{
			holder.speicaltyTv.setText("");
		}

		return convertView;
	}

	 /**
     * 
     * <p>清除适配器中的数据</P>
     *
     */
    public void clear() {
    	mListItems.clear();
        notifyDataSetChanged();
    }
    
	private static class ViewHolder {
		private TextView speicaltyTv; 
	}
	 
	public void setSelectIndex(int i){
		selectIndex = i;
	}
}