package com.nd.shapedexamproj.adapter;


import android.content.Context;
import android.view.LayoutInflater;
import android.widget.BaseAdapter;

import java.util.List;

public abstract class MyBaseAdapter  extends BaseAdapter{

	public LayoutInflater inflater;
	public  int itemCounts;
	public Context context;
	protected List<?> data;
	
	
	public MyBaseAdapter(Context context,List<?> data)
	{
		this.context = context;
		inflater = LayoutInflater.from(context);
		this.data = data;
		itemCounts = data.size();
	}
	
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		if(data!=null && data.size()>0){
			return data.size();
		}else{
			return itemCounts;
		}
		
	}
	
	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return data.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}
	
}
