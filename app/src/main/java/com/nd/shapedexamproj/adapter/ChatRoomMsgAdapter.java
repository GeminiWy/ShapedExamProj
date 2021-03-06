package com.nd.shapedexamproj.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.nd.shapedexamproj.App;
import com.nd.shapedexamproj.R;
import com.nd.shapedexamproj.entity.ChatGroupMsgEntity;
import com.nd.shapedexamproj.entity.ChatMsgEntity;
import com.nd.shapedexamproj.entity.PersonPresenceEntity;
import com.nd.shapedexamproj.model.my.PersonalInfo;
import com.nd.shapedexamproj.util.Constants;
import com.nd.shapedexamproj.util.DateUtils;
import com.nd.shapedexamproj.util.StringUtils;
import com.nd.shapedexamproj.view.InputBottomBar;
import com.tming.common.cache.image.ImageCacheTool;

import java.util.Date;
import java.util.List;

/**
 * 
 * @ClassName: ChatMsgAdapter
 * @Title:
 * @Description:IM聊天消息适配器
 * @Author:XueWenJian
 * @Since:2014-4-29下午10:16:44
 * @Version:1.0
 */
public class ChatRoomMsgAdapter extends BaseAdapter {

	private static final String TAG = ChatRoomMsgAdapter.class.getSimpleName();
	private static final long TIMEINTERVAL = 60 * 1000;
	private static final int FACESIZE = 60;

	// ListView视图的内容由IMsgViewType决定
	public static interface IMsgViewType {
		// 对方发来的信息
		int IMVT_COM_MSG = 0;
		// 自己发出的信息
		int IMVT_TO_MSG = 1;
	}
	private Context context;
	private LayoutInflater inflater;

	private List<ChatGroupMsgEntity> chatMsglist;
	private List<PersonPresenceEntity> oppPersonPresences;
	private List<PersonalInfo> oppPersonalInfos;

	private String selfUserId = App.getUserId();
    private String imgUrl = "";

	public ChatRoomMsgAdapter(Context context, List<ChatGroupMsgEntity> chatMsglist, String selfUserId) {
		this.context = context;
		this.chatMsglist = chatMsglist;
		inflater = LayoutInflater.from(context);
	}

	@Override
	public int getCount() {
		return chatMsglist.size();
	}

	@Override
	public Object getItem(int arg0) {
		return chatMsglist.get(arg0);
	}

	@Override
	public long getItemId(int arg0) {
		return 0;
	}

	public View getView(final int position, View convertView, ViewGroup parent) {

		ChatGroupMsgEntity entity = chatMsglist.get(position);
		final ViewHolder viewHolder = new ViewHolder();
		
		if (selfUserId.equals(entity.getToId())) {
			// 如果是对方发来的消息，则显示的是左气泡
			convertView = inflater.inflate(R.layout.chat_group_left_msg_item, null);
			viewHolder.tvUserNameTime = (TextView) convertView
			.findViewById(R.id.chat_name_tv);
			
		} else {
			// 如果是自己发出的消息，则显示的是右气泡
			convertView = inflater.inflate(R.layout.chat_right_msg_item, null);
			
			
		};
		viewHolder.tvContent = (TextView) convertView
				.findViewById(R.id.chat_chatcontent_tv);
		viewHolder.ivHead = (ImageView) convertView
				.findViewById(R.id.chat_userhead_iv);
		viewHolder.tvMsgTime = (TextView) convertView
				.findViewById(R.id.chat_time_tv);

		viewHolder.ivHead.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {

				
			}
		});
		int faceSize = (int) viewHolder.tvContent.getTextSize();
		viewHolder.tvContent.setText(InputBottomBar.parseFaceByText(context, entity.getText(), faceSize));
		
		if (null != viewHolder.tvUserNameTime) {
		    if (StringUtils.isEmpty(entity.getFromUserName())) {
		        viewHolder.tvUserNameTime.setText(entity.getFromUserId());
		    } else {
		        viewHolder.tvUserNameTime.setText(entity.getFromUserName());
		    }
		}
		
		
		if (selfUserId.equals(entity.getToId())) {
		    imgUrl = entity.getFormUserImgurl();
            if(imgUrl != null && !"".equals(imgUrl)){
                viewHolder.ivHead.setBackgroundResource(R.drawable.transparent_bg);
                ImageCacheTool.asyncLoadImage(viewHolder.ivHead, imgUrl, R.drawable.all_use_icon_photo);
            }
			
		} else {
		    imgUrl = Constants.USER_PHOTO_URL + App.getUserId();
		    ImageCacheTool.asyncLoadImage(viewHolder.ivHead, imgUrl, R.drawable.all_use_icon_photo);
		}
		

		Log.e(TAG, entity.getDate());
		if (!StringUtils.isEmpty(entity.getDate())) {
			Date date = StringUtils.toDate(entity.getDate());
			if (null != date) {
				long msgTime = date.getTime();

				if (0 == position) {
					viewHolder.tvMsgTime.setVisibility(View.VISIBLE);
					viewHolder.tvMsgTime.setText(DateUtils.toToday(msgTime));
				} else {
					ChatGroupMsgEntity lastChatMsgEntity = chatMsglist
							.get(position - 1);
					Date lastMsgdate = StringUtils.toDate(lastChatMsgEntity
							.getDate());
					if (null != lastMsgdate) {
						if (msgTime - lastMsgdate.getTime() > TIMEINTERVAL) {
							viewHolder.tvMsgTime.setVisibility(View.VISIBLE);
							viewHolder.tvMsgTime.setText(DateUtils.toToday(msgTime));
						}
					} else {
						viewHolder.tvMsgTime.setVisibility(View.VISIBLE);
						viewHolder.tvMsgTime.setText(DateUtils.toToday(msgTime));
					}
				}

			}

		}
		
		viewHolder.ivError = (ImageView) convertView.findViewById(R.id.chat_err_iv);
		viewHolder.pbProcess = (ProgressBar) convertView.findViewById(R.id.chat_process_pb);
		if(null != viewHolder.ivError && null != viewHolder.pbProcess)
		{
			if(entity.getState() == ChatMsgEntity.STATE_SENDING)
			{
				viewHolder.ivError.setVisibility(View.GONE);
				viewHolder.pbProcess.setVisibility(View.VISIBLE);
			}
			else if(entity.getState() == ChatMsgEntity.STATE_ERROR)
			{
				viewHolder.ivError.setVisibility(View.VISIBLE);
				viewHolder.pbProcess.setVisibility(View.GONE);
				
				viewHolder.ivError.setTag(position);
			}
			else
			{
				viewHolder.ivError.setVisibility(View.GONE);
				viewHolder.pbProcess.setVisibility(View.GONE);
			}
		}

		return convertView;
	}

	
	/*public void setSelfUserInfo(PersonalInfo selfUserInfo) {
		this.selfUserInfo = selfUserInfo;
	}*/

	public void setOppPersonalInfos(List<PersonalInfo> oppPersonalInfos) {
		this.oppPersonalInfos = oppPersonalInfos;
	}
	public void setOppPersonalPresences(List<PersonPresenceEntity> oppPersonPresences) {
		this.oppPersonPresences = oppPersonPresences;
	}


	static class ViewHolder {
		public ImageView ivHead;
		public TextView tvContent;
		public TextView tvMsgTime;
		public ImageView ivError;
		public ProgressBar pbProcess;
		public TextView tvUserNameTime;
	}

}
