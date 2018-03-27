/**  
 * Project Name:OpenUniversity  
 * File Name:IMSingleChatSettingActivity.java  
 * Package Name:com.tming.openuniversity.activity.im  
 * Date:2014-6-13上午1:33:22  
 * Copyright (c) 2014, XueWenJian All Rights Reserved.  
 *  
*/  

  
package com.nd.shapedexamproj.activity.im;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.nd.shapedexamproj.R;
import com.nd.shapedexamproj.activity.BaseActivity;
import com.nd.shapedexamproj.db.ChatHistoryMsgDao;
import com.nd.shapedexamproj.db.ChatRoomMsgDao;
import com.nd.shapedexamproj.entity.ChatHistoryEntity;
import com.nd.shapedexamproj.entity.PersonPresenceEntity;
import com.nd.shapedexamproj.im.model.RoomModel;
import com.nd.shapedexamproj.im.resource.IMConstants;
import com.nd.shapedexamproj.util.UIHelper;

import java.util.List;

/**  
 * ClassName:IMSingleChatSettingActivity <br/>  
 * description: IM聊天-聊天室设置 <br/>  
 * Date:     2014-6-13 上午1:33:22 <br/>  
 * @author   XueWenJian  
 * @version    
 * @since    JDK 1.6  
 * @see        
 */
public class IMRoomChatSettingActivity extends BaseActivity implements OnClickListener{

	private String mOppId;
	
	private String mRoomName;
	private int mNumber;
	
	private TextView mRoomNameTV;
	private TextView mNumTV;
	
	private ChatRoomMsgDao chatMsgDao;
	private ChatHistoryMsgDao chatHistoryMsgDao;
	private int memmberNum = 0;
	
	private BroadcastReceiver receiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(IMConstants.ACTION_ROOM_MEMMBER_LIST)) {//TODO 获取群成员列表
                List<PersonPresenceEntity> personList = (List<PersonPresenceEntity>) intent.getSerializableExtra("group_memmbers");
                if (personList != null) {
                    memmberNum = personList.size();
                    mNumTV.setText("" + memmberNum);
                }
            }
        }
	    
	};
	
	@Override
	public int initResource() {
		return R.layout.im_room_chat_setting;
	}

	@Override
	public void initComponent() {
	    
	    
		((TextView)findViewById(R.id.commonheader_title_tv)).setText(getResources().getString(R.string.im_talk_setting));
		findViewById(R.id.commonheader_right_btn).setVisibility(View.GONE);
		mRoomNameTV = (TextView)findViewById(R.id.msg_talk_setting_roomname_tv);
		mNumTV = (TextView)findViewById(R.id.msg_talk_setting_num_tv);
		
		IntentFilter filter = new IntentFilter ();
		filter.addAction(IMConstants.ACTION_ROOM_MEMMBER_LIST);
		registerReceiver(receiver, filter);
		
	}

	@Override
	public void initData() {
		mOppId = getIntent().getStringExtra("oppId");
		mRoomName = getIntent().getStringExtra("roomName");
		/*mNumber = getIntent().getIntExtra("number", 0);*///废弃
		chatMsgDao = ChatRoomMsgDao.getInstance(this);
		chatHistoryMsgDao = new ChatHistoryMsgDao(this);
		
		RoomModel roomModel = new RoomModel();
        roomModel.sendRoomMemmberRequest(mOppId);
		
		mRoomNameTV.setText(mRoomName);
		mNumTV.setText("" + memmberNum);
	}

	@Override
	public void addListener() {
		findViewById(R.id.commonheader_left_iv).setOnClickListener(this);
		findViewById(R.id.msg_talk_setting_record_rl).setOnClickListener(this);
		
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
			//删除群聊消息列表
			chatMsgDao.deleteAllChatRoomMsg(getChatTag());
			setResult(RESULT_OK);
			UIHelper.ToastMessage(this, R.string.im_talk_setting_history_clean);
			break;
		}
	}
	
	private String getChatTag() {
		String tag =  IMConstants.getLoginId() +"-"+ mOppId;
		
		return tag;
	}
	
	@Override
	protected void onDestroy() {
	    unregisterReceiver(receiver);
	    super.onDestroy();
	}
}
  
