package com.nd.shapedexamproj.im.resource;

import com.nd.shapedexamproj.entity.GroupItemInfo;
import com.nd.shapedexamproj.entity.PersonPresenceEntity;
import com.nd.shapedexamproj.entity.PersonVCardEntity;
import com.nd.shapedexamproj.util.Constants;
import com.nd.shapedexamproj.util.StringUtils;
import com.tming.common.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * @Title: IMConstants
 * @Description: IM常量
 * @author WuYuLong
 * @date 2014-5-7
 * @version V1.0
 */
public class IMConstants {
	
	private static final String TAG = "IMConstants";
	
	public static final String IM_STATE_ACTION = "com.tming.openiversity.im.login";
	
	public static final String ACTION_SINGLE_RECEIVER_NAME = "IM_MSG_SINGLE_RECEIVER";//系统第一次接收的广播（单聊）
	public static final String ACTION_SINGLE_RECEIVER_SECOND = "IM_MSG_SINGLE_RECEIVER_SECOND";//系统接收第一次的广播后，发送的广播（单聊）
	public static final String ACTION_ROOM_RECEIVER_NAME = "IM_MSG_ROOM_RECEIVER";//系统第一次接收的广播（群聊）
	public static final String ACTION_ROOM_RECEIVER_SECOND = "IM_MSG_ROOM_RECEIVER_SECOND";//系统接收第一次的广播后，发送的广播（群聊）
	
	//群成员列表
	public static final String ACTION_ROOM_MEMMBER_LIST = "action_room_memmber_list";
	//群列表
	public static final String ACTION_ROOM_LIST = "IM_ROOM_LIST";
	//群离线消息
	public static final String ACTION_OFFLINE_ROOM_MSG = "action_offline_room_msg";
	//群成员信息
	public static final String ACTION_ROOM_USERINFO_NAME = "IM_MSG_ROOM_USERINFO";
	//命名空间
	public final static String SHARED_GROUP_NAMESPACE = "http://www.jivesoftware.org/protocol/sharedgroup";
	public final static String ROOM_PRESENCE_NAMESPACE = "http://jabber.org/protocol/muc";
	
	//public final static String hostServerName = "qq.tming.tmc";//内网虚拟域名
	public final static String hostServerName = Constants.IS_OUTER_NET ? "im.service.fjou.cn" :"192.168.181.46";//old address "192.168.208.37";//外网218.66.49.115
	
	public final static String areaServerName = "example1.com";
	public final static String roomServerName = "conference.example1.com";
	
	public final static int port = 5222;
	public final static int timeOut = 10;
	public final static String resource = "kd_1.0_android";
	
	public final static String passWord_t0= "123456";
	
	//登录状态
	public final static int STATE_OFFLINE = -1;
	public final static int STATE_LINKING = 0;
	public final static int STATE_ONLINE = 1;
	public final static int STATE_HIDDENLINE = 2;
	
	/*
	 * xxx@example1.com
	 */
	private static String userJId = "";
	private static PersonVCardEntity personVCardEntity;
	
	//聊天室信息
	private static List<GroupItemInfo> mGroupList = new ArrayList<GroupItemInfo>();
	//聊天室内 用户信息
	private static List<PersonPresenceEntity> mPersonPresenceList = new ArrayList<PersonPresenceEntity>();
	public static int imLoginState = 0;
	
	/**
	 * 聊天类型 私聊-0.群聊-1
	 */
	public static final int CHAT_TYPE_SINGLE = 0, CHAT_TYPE_GROUP = 1;
	
	public static int getUserState()
	{
		return imLoginState;
	}
	
	public static void setUserState(int loginState)
	{
		imLoginState = loginState;
	}
	
	public static String getUserIdByJID(String Jid)
	{
		Log.i(TAG, "Jid:"+Jid);
		String JIDarray[]= Jid.split("@");
		String UserId = "";
		if(JIDarray.length>0 && !StringUtils.isEmpty( JIDarray[0])) {
			UserId = JIDarray[0];
		}
		Log.i(TAG, "UserId"+UserId);
		return UserId;
	}
	

	public static void setUserJID(String jid) {
		userJId = jid;
	}
	
	public static String getUserJID() {
		return userJId;
	}
	
	public static void setGroupList(List<GroupItemInfo> groupList) {
		mGroupList = groupList;
	}
	
	public static List<GroupItemInfo> getGroupList() {
		return mGroupList;
	}
	
	public static void setRoomPersonInfoList(List<PersonPresenceEntity> personPresenceList) {
		mPersonPresenceList = personPresenceList;
	}
	
	public static List<PersonPresenceEntity> getRoomPersonInfoList() {
		return mPersonPresenceList;
	}
	
	public static void setPersonVCard(PersonVCardEntity personVCard) {
		personVCardEntity = personVCard;
	}
	
	public static PersonVCardEntity getPersonVCard() {
		return personVCardEntity;
	}
	
	
	public static GroupItemInfo findGroupItemInfo(String roomJId){
		GroupItemInfo groupItemInfo = null;
		if(null != IMConstants.getGroupList()) {
			List<GroupItemInfo> entities = IMConstants.getGroupList();
			for(int index=0; index<entities.size(); index++) {
				GroupItemInfo entity = entities.get(index);
				Log.i(TAG, entity.getName());
				if (entity.getJid().equals(roomJId)) {
					groupItemInfo = entity;
					
					
					break;
				}
			}
		}
		return groupItemInfo;
	}
	
	/**
	 * 
	  * @Title: getLoginId
	  * @Description: 获得IM的登录ID
	  * @param @return
	  * @return String    返回类型
	  * @throws
	 */
	public static String getLoginId() {
		String userId = "";
		if (!StringUtils.isEmpty(userJId)) {
			userId = getUserIdByJID(userJId);
		}
		
		return userId;
		
	}
}
