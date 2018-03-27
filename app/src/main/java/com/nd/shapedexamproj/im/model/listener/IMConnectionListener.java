/**  
 * Project Name:OpenUniversity  
 * File Name:IMConnectionListener.java  
 * Package Name:com.tming.openuniversity.im.model.listener  
 * Date:2014-6-10上午3:19:23  
 * Copyright (c) 2014, XueWenJian All Rights Reserved.  
 *  
 */

package com.nd.shapedexamproj.im.model.listener;

import com.nd.shapedexamproj.im.model.RoomModel;
import com.tming.common.util.Log;
import org.jivesoftware.smack.ConnectionListener;

/**
 * ClassName:IMConnectionListener <br/>
 * description: 连接监听类 <br/>
 * Date: 2014-6-10 上午3:19:23 <br/>
 * 
 * @author XueWenJian
 * @version
 * @since JDK 1.6
 * @see
 */
public class IMConnectionListener implements ConnectionListener {

	private static final String TAG = "IMConnectionListener";

	@Override
	public void connectionClosed() {
		Log.i(TAG, "连接关闭");
	}

	@Override
	public void connectionClosedOnError(Exception e) {
		Log.i(TAG, "连接关闭异常");
		// 判斷為帳號已被登錄
		boolean error = e.getMessage().equals("stream:error (conflict)");
		if (!error) {
			/*
			 * // 關閉連接
			 * IMManager.getSessionManager().getConnection().disconnect(); //
			 * 重连服务器 tExit = new Timer(); tExit.schedule(new timetask(),
			 * logintime);
			 */
		}
	}

	@Override
	public void reconnectingIn(int arg0) {
	    Log.i(TAG, "重新连接");
	    //登录聊天室，并获取群列表
        RoomModel roomModel = new RoomModel();
        roomModel.sendJoinRoomRequest();

	}

	@Override
	public void reconnectionFailed(Exception arg0) {
	    
	}

	@Override
	public void reconnectionSuccessful() {
		Log.i(TAG, "IM reconnectionSuccessful");
		

	}
}
