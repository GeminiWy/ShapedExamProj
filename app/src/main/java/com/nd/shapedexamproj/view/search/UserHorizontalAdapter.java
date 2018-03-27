package com.nd.shapedexamproj.view.search;


import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.nd.shapedexamproj.AuthorityManager;
import com.nd.shapedexamproj.R;
import com.nd.shapedexamproj.activity.search.UserInfo;
import com.nd.shapedexamproj.util.CircleImageView;
import com.nd.shapedexamproj.util.UIHelper;
import com.tming.common.cache.image.ImageCacheTool;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

/**
 * <p> 全局搜索 相关用户水平列表适配器。 </p>
 * <p>Created by xiezz on 2014-06-03</p>
 * <p>Modified by xuwenzhuo on 2014/12/04</p>
 * */

 public class UserHorizontalAdapter extends BaseAdapter {

	private static final String TAG = "UserHorizontalAdapter";
	private Context mContext;  
	private List<UserInfo> mListItems; //数据集合

	private ImageCacheTool imageCacheTool = null;
	private int defaultImageResourceId = R.drawable.all_use_icon_photo;
	/**
	 * 最多有多少项
	 * */
	private int mCountItems;  

	/**
	 * @param count 用来控制是否要限制显示子项个数,传入的值必须小于图像的个数，否则自动限制
	 * */
	public UserHorizontalAdapter(Context context, List<UserInfo> data, int count){
		this.mContext = context;
		this.mListItems = data; 
		this.mCountItems = count;
		imageCacheTool = ImageCacheTool.getInstance();
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

    public List<UserInfo> getmListItems() {
        return mListItems;
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

		final ViewHolder holder;
		if(convertView == null){
			holder = new ViewHolder();
			convertView = LayoutInflater.from(mContext).inflate(R.layout.search_user_horizontal_list_item, null);
			holder.photoIv = (CircleImageView)convertView.findViewById(R.id.user_photo_iv);
			holder.usernameTv = (TextView)convertView.findViewById(R.id.user_name_tv);
            holder.userDetailTv=(TextView)convertView.findViewById(R.id.user_detail_tv);
			convertView.setTag(holder);
		}else{
			holder = (ViewHolder)convertView.getTag();
		}

        //设定用户名称
		if(TextUtils.isEmpty(mListItems.get(position).getUserName())){
			holder.usernameTv.setText(""); 		
		}else {
			holder.usernameTv.setText(mListItems.get(position).getUserName()); 
		}

        //设定用户基本消息
        if(TextUtils.isEmpty(mListItems.get(position).getUserDetailMsg())){
            holder.userDetailTv.setText("");
        }else {
            holder.userDetailTv.setText(mListItems.get(position).getUserDetailMsg());
        }
		
		/**
		 * 设置图片
		 * */
		if(!TextUtils.isEmpty(mListItems.get(position).getAvatar())){			
			try {
				URL url = new URL(mListItems.get(position).getAvatar());
				imageCacheTool.asyncLoadImage(holder.photoIv, null, url);
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} 
		}else{
			holder.photoIv.setImageResource(defaultImageResourceId);
		}
        final String uId=mListItems.get(position).getUserId();
		convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (AuthorityManager.getInstance().isInnerAuthority()) {
                    AuthorityManager.getInstance().showInnerDialog(mContext);
                    return;
                }
                UIHelper.showFriendInfoActivity(mContext,uId,false);
            }
        });
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
		private TextView usernameTv;
        private TextView userDetailTv;
		//private ImageView photoIv;
        private CircleImageView photoIv;
	}  
	
	 
}