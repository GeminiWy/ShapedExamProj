/**  
 * Project Name:OpenUniversity  
 * File Name:IMSingleChatSettingActivity.java  
 * Package Name:com.tming.openuniversity.activity.im  
 * Date:2014-6-13上午1:33:22  
 * Copyright (c) 2014, XueWenJian All Rights Reserved.  
 *  
*/  

  
package com.nd.shapedexamproj.activity.im;

import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.nd.shapedexamproj.App;
import com.nd.shapedexamproj.R;
import com.nd.shapedexamproj.activity.BaseActivity;
import com.nd.shapedexamproj.db.ChatHistoryMsgDao;
import com.nd.shapedexamproj.db.ChatMsgDao;
import com.nd.shapedexamproj.entity.ChatHistoryEntity;
import com.nd.shapedexamproj.util.UIHelper;
import com.tming.common.cache.image.ImageCacheTool;

/**  
 * ClassName:IMSingleChatSettingActivity <br/>  
 * description: IM聊天-单聊设置 <br/>  
 * Date:     2014-6-13 上午1:33:22 <br/>  
 * @author   XueWenJian  
 * @version    
 * @since    JDK 1.6  
 * @see        
 */
public class IMSingleChatSettingActivity extends BaseActivity implements OnClickListener{

	private String mOppId;
	private String mOppName;
	private String mOppImgUrl;

	private ChatMsgDao chatMsgDao;
	private ChatHistoryMsgDao chatHistoryMsgDao;
	
	private ImageView mUserImageIV;
	private TextView mUserNameTV;
	
	@Override
	public int initResource() {
		return R.layout.im_single_chat_setting;
	}

	@Override
	public void initComponent() {
		((TextView)findViewById(R.id.commonheader_title_tv)).setText(getResources().getString(R.string.im_talk_setting));
		findViewById(R.id.commonheader_right_btn).setVisibility(View.GONE);
		mUserImageIV = (ImageView)findViewById(R.id.msg_talk_setting_userimage_iv);
		mUserNameTV = (TextView)findViewById(R.id.msg_talk_setting_username_tv);

	}

	@Override
	public void initData() {
		mOppId = getIntent().getStringExtra("oppId");
		mOppName = getIntent().getStringExtra("oppName");
		mOppImgUrl = getIntent().getStringExtra("oppImgUrl");
		chatMsgDao = ChatMsgDao.getInstance(this);
		chatHistoryMsgDao = new ChatHistoryMsgDao(this);
		
		mUserNameTV.setText(mOppName);
		ImageCacheTool.asyncLoadImage(mUserImageIV, mOppImgUrl, R.drawable.all_use_icon_photo);
	}

	@Override
	public void addListener() {
		findViewById(R.id.commonheader_left_iv).setOnClickListener(this);
		findViewById(R.id.msg_talk_setting_record_rl).setOnClickListener(this);
		findViewById(R.id.msg_talk_setting_notifi_rl).setOnClickListener(this);
		
	}

	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.commonheader_left_iv:
			finish();
			break;
		case R.id.msg_talk_setting_record_rl:
			//删除消息列表
			ChatHistoryEntity chatMsgEntity = new ChatHistoryEntity();
			chatMsgEntity.setOppId(mOppId);
			chatHistoryMsgDao.deleteChatHistotyMsg(chatMsgEntity);
			//删除单聊消息列表
			chatMsgDao.deleteAllChatMsg(getChatTag());
			setResult(RESULT_OK);
			UIHelper.ToastMessage(this, R.string.im_talk_setting_history_clean);
			break;
		case R.id.msg_talk_setting_notifi_rl:
			UIHelper.showFriendInfoActivity(this, mOppId);
			break;
		}
	}
	
	private String getChatTag() {
		String tag =  App.getUserId() +"-"+ mOppId;
		
		return tag;
	}
}
  
