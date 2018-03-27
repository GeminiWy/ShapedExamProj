/**  
 * Project Name:OpenUniversity  
 * File Name:RoomLoginMsgAsyncTask.java  
 * Package Name:com.tming.openuniversity.im.task  
 * Date:2014-6-11下午4:05:18  
 * Copyright (c) 2014, XueWenJian All Rights Reserved.  
 *  
*/  

  
package com.nd.shapedexamproj.im.task;

import android.content.Context;
import android.os.AsyncTask;

import com.nd.shapedexamproj.entity.GroupItemInfo;
import com.nd.shapedexamproj.im.manager.IMManager;
import com.nd.shapedexamproj.im.manager.SessionManager;
import com.nd.shapedexamproj.im.model.RosterModel;
import com.nd.shapedexamproj.im.packet.RoomPresence;
import com.nd.shapedexamproj.im.resource.IMConstants;
import com.tming.common.util.Log;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smackx.muc.HostedRoom;

import java.util.ArrayList;
import java.util.List;

/**  
 * ClassName:RoomLoginMsgAsyncTask <br/>  
 * description: 登录所有聊天室 <br/>  
 * Date:     2014-6-11 下午4:05:18 <br/>  
 * @author   XueWenJian  
 * @version    
 * @param <GroupItemInfo>
 * @since    JDK 1.6  
 * @see        
 */
public class RoomLoginMsgAsyncTask extends AsyncTask<Void, Void, List<GroupItemInfo>>{
	private final String TAG = "RoomLoginMsgAsyncTask";
	private RosterModel rosterModel = new RosterModel();
	private List<GroupItemInfo> groupList;
	private Context context;
	@Override
	protected List<GroupItemInfo> doInBackground(Void... arg0) {
	    final SessionManager sessionManager = IMManager.getSessionManager();
		groupList = new ArrayList<GroupItemInfo>();
		GroupItemInfo groupItemInfo = new GroupItemInfo();

		try{
			//获取所有聊天室 
			// 列表非空判断
		    List<HostedRoom> rosterList = rosterModel.getHostRooms(context, IMConstants.roomServerName);
		   /* Presence presence = new Presence(Presence.Type.available);
		    presence.setFrom("");
		    presence.setTo("");
		    sessionManager.getConnection().sendPacket(presence);*/
		    
			if(rosterList != null){
				int size = rosterList.size();
				for (int i = 0; i < size; i++) {
					groupItemInfo = new GroupItemInfo();
					groupItemInfo.setName(rosterList.get(i).getName());
					groupItemInfo.setJid(rosterList.get(i).getJid());
					groupList.add(groupItemInfo);
				}
				
				//到所有的聊天室上线
				for(int index=0; index<groupList.size(); index++) {
					groupItemInfo = groupList.get(index);
					RoomPresence presence = new RoomPresence(Presence.Type.available, "<x xmlns='http://jabber.org/protocol/muc'><history maxchars='0'/></x>");
					presence.setFrom(IMConstants.getUserJID());
					presence.setTo(groupItemInfo.getJid() + "/" + IMConstants.getLoginId());
					
					IMManager.sendPresenceMessage(presence);
				}
			}
		} catch (Exception e){
			groupList = null;
			Log.e(TAG, e.getMessage());
		}
		
		return groupList;
	}
	
	@Override
	protected void onPostExecute(List<GroupItemInfo> result) {
		IMConstants.setGroupList(result);
	}
}
  
