package com.nd.shapedexamproj.activity.course;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.nd.shapedexamproj.R;
import com.nd.shapedexamproj.model.User;
import com.nd.shapedexamproj.util.Constants;
import com.tming.common.cache.image.ImageCacheTool;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Vector;
/**
 * 联系人适配器
 * @version 1.0.0 
 * @author Abay Zhuang <br/>
 *		   Create at 2014-6-13
 * 修改者，修改日期，修改内容。
 */
public class ContactAdapter extends BaseAdapter {
	private ImageCacheTool imageCacheTool;
	private List<User> mUserLst = new Vector<User>(); //线程安全
	private Context mContext;

	public ContactAdapter(Context context, List<User> mUserLst){
		mContext = context;
		imageCacheTool = ImageCacheTool.getInstance(context);;
		addUsers(mUserLst);
	}
	
	public void addUsers(List<User> userLst){
		if (userLst == null)
			return ;
		mUserLst.addAll(userLst);
	}
	
	public void clear(){
		mUserLst.clear();
	}
	

	@Override
	public int getCount() {
		if (mUserLst != null)
			return mUserLst.size();
		return 0;
	}

	@Override
	public Object getItem(int arg0) {
		if (arg0 < mUserLst.size())
			return mUserLst.get(arg0);
		return null;
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
			LayoutInflater inflater = LayoutInflater
					.from(mContext);
			convertView = inflater
					.inflate(R.layout.msg_fragment_item, null);

			holder.msg_fragment_item_photo = (ImageView) convertView
					.findViewById(R.id.msg_fragment_item_img_photo);
			holder.msg_fragment_item_name = (TextView) convertView
					.findViewById(R.id.msg_fragment_item_name);
			holder.msg_fragment_item_content = (TextView) convertView
					.findViewById(R.id.msg_fragment_item_content);
			holder.msg_fragment_item_time = (TextView) convertView
					.findViewById(R.id.msg_fragment_item_time);

			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		User user = mUserLst.get(position);		
		String faceURL = user.getPhoto();
		holder.msg_fragment_item_photo
		.setImageResource(R.drawable.msg_friend_icon);
		if (!com.nd.shapedexamproj.util.StringUtils.isEmpty(faceURL)) {
			try {
				URL url = new URL(faceURL);
				imageCacheTool.asyncLoadImage(holder.msg_fragment_item_photo, null, url);
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}
		}

		// 查看好友基本信息
//		holder.msg_fragment_item_photo
//				.setOnClickListener(new OnClickListener() {
//					@Override
//					public void onClick(View view) {
//						UIHelper.showFriendInfoActivity(mContext,
//								mUserLst.get(position).getUserid());
//					}
//				});

		// 设置内容文字
		holder.msg_fragment_item_name.setText(user.getUsername());
		if (user.getUsertype().equals(Constants.USER_TYPE_TEACHER))
			holder.msg_fragment_item_content.setText("教师");
		else 
			holder.msg_fragment_item_content.setText("");
		holder.msg_fragment_item_time.setText("");


		return convertView;
	}

	private static final class ViewHolder {
		private ImageView msg_fragment_item_photo;
		private TextView msg_fragment_item_name;
		private TextView msg_fragment_item_content;
		private TextView msg_fragment_item_time;
	}
}
