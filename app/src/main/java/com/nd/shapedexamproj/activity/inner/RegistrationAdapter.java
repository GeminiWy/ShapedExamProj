package com.nd.shapedexamproj.activity.inner;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.nd.shapedexamproj.R;
import com.nd.shapedexamproj.model.TeachPoint;
import com.nd.shapedexamproj.model.TeachPoints;

import java.util.ArrayList;
import java.util.List;

/**
 * 教师负责班级 适配器
 * @version 1.0.0 
 * @author Abay Zhuang <br/>
 *		   Create at 2014-5-21
 */
public class RegistrationAdapter extends BaseAdapter {
	private Context mContext;
	private final static String TAG = RegistrationAdapter.class.getSimpleName();
	private LayoutInflater inflater;
	public List<TeachPoint> mLst = new ArrayList<TeachPoint>();
	public int mSelected = 0;

	public RegistrationAdapter(Context context, TeachPoints teachpoints){
		this.mContext = context;
		inflater = LayoutInflater.from(context);
		setTeachPoints(teachpoints);
	}
	
	public void setTeachPoints(TeachPoints teachpoints){
		if (teachpoints != null && teachpoints.getList() != null) {
			mLst.clear();
			mLst.addAll(teachpoints.getList());
		//	mLst.add(location, object)
			Log.e(TAG, "count :" + mLst.size());
			notifyDataSetChanged();
		}
	}
	
	public void setSelected(int selected){
		mSelected = selected;
		notifyDataSetChanged();
	}
	
	
	public List<TeachPoint> getList(){
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
			convertView = inflater.inflate(R.layout.inner_registration_teach_point_item, null);
			holder = new ViewHolder();
			holder.name = (TextView)convertView.findViewById(R.id.registration_teachpoint_name_tv);
			holder.phone = (TextView)convertView.findViewById(R.id.registration_teachpoint_phone_number_tv);
			holder.address = (TextView)convertView.findViewById(R.id.registration_teachpoint_address_tv);
			holder.choose = (ImageView)convertView.findViewById(R.id.registration_teachpoint_choose_iv);
			convertView.setTag(holder);
		}else {
			holder = (ViewHolder)convertView.getTag();
		}
		
		TeachPoint teachPoint = mLst.get(position);
		if (teachPoint != null) {
			
			holder.name.setText(teachPoint.getName());
			//String phone = String.format(mContext.getString(R.string.online_registration_first_teach_point_item_phone), teachPoint.getPhone());
			holder.phone.setText("电话："+teachPoint.getPhone());
			//String address = String.format(mContext.getString(R.string.online_registration_first_teach_point_item_address), teachPoint.getAddress());
			holder.address.setText("地址："+teachPoint.getAddress());
			//holder.choose.setOnClickListener(mChooseClickListener);
			//holder.choose.setTag(holder.choose);
			
		}	
		
		if (position == mSelected){
			holder.choose.setImageResource(R.drawable.online_doubledot);
		}else {
			holder.choose.setImageResource(R.drawable.online_doubledots);
		}
		
		return convertView;

	}
	
	
	private OnClickListener mChooseClickListener = new OnClickListener(){

		@Override
		public void onClick(View v) {
			ImageView choose = (ImageView)v.getTag();
			choose.setImageResource(R.drawable.online_doubledots);
			
		}
	};

	static class ViewHolder{
		TextView name;
		TextView address;
		TextView phone;
		ImageView choose;
	}
}
