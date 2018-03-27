/**  
 * Project Name:OpenUniversity  
 * File Name:RoomUserInfoBroadcastReceiver.java  
 * Package Name:com.tming.openuniversity.im.model.listener  
 * Date:2014-6-12上午11:02:09  
 * Copyright (c) 2014, XueWenJian All Rights Reserved.  
 *  
 */

package com.nd.shapedexamproj.im.model.listener;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.nd.shapedexamproj.entity.PersonPresenceEntity;
import com.nd.shapedexamproj.im.resource.IMConstants;
import com.tming.common.util.Log;

/**
 * ClassName:RoomUserInfoBroadcastReceiver <br/>
 * description:处理收到IM- 群成员信息 广播 <br/>
 * Date: 2014-6-12 上午11:02:09 <br/>
 * 
 * @author XueWenJian
 * @version
 * @since JDK 1.6
 * @see
 */
public class RoomUserInfoBroadcastReceiver extends BroadcastReceiver {

	private static final String TAG = "RoomUserInfoBroadcastReceiver";

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.e(IMConstants.ACTION_ROOM_USERINFO_NAME, "Receive");
		Log.d(TAG, "RoomUserInfoBroadcastReceiver IN");
		PersonPresenceEntity entity = (PersonPresenceEntity) intent.getExtras()
				.getSerializable("GroupUserInfo");
		Log.d(TAG, entity.getUserJId());
		IMConstants.getRoomPersonInfoList().add(entity);

	}
}
