package com.nd.shapedexamproj.im.manager;

import android.content.Context;

import com.nd.shapedexamproj.App;
import com.nd.shapedexamproj.entity.GroupItemInfo;
import com.nd.shapedexamproj.entity.PersonPresenceEntity;
import com.nd.shapedexamproj.entity.PersonVCardEntity;
import com.nd.shapedexamproj.im.model.LoginModel;
import com.nd.shapedexamproj.im.resource.IMConstants;
import com.nd.shapedexamproj.im.task.LoginAsyncTask;
import com.nd.shapedexamproj.util.Constants;
import com.tming.common.util.Log;
import org.jivesoftware.smack.*;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.util.StringUtils;
import org.jivesoftware.smackx.ServiceDiscoveryManager;
import org.jivesoftware.smackx.muc.*;
import org.jivesoftware.smackx.packet.VCard;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
/**
 * @Title: UserManager
 * @Description: 用户管理类
 * @author WuYuLong
 * @date 2014-5-7
 * @version V1.0
 */
public class UserManager {
	private static final String TAG = UserManager.class.getSimpleName();
	private static LoginModel loginModel = LoginModel.getInstance();


    public UserManager() {
    }
    
    /**
     * IM登录
     * @param context
     * @param isGetOffLineMsg 是否获取离线消息
     * @return
     */
    public static boolean userLogin(Context context, boolean isGetOffLineMsg){
    	Log.e(TAG, "UserManager.userLogin() --- start");
    	Log.e(TAG, "Constants.USER_TYPE = " + App.getUserType());
    	boolean flag = false;
    	//判断非游客登录
    	if(App.getUserType() != Constants.USER_TYPE_INNER){
	    	try {
				//启动IM登录线程
				LoginAsyncTask loginAsyncTask = new LoginAsyncTask(context, loginModel, isGetOffLineMsg);
				loginAsyncTask.execute();
				flag = true;
	    	} catch(Exception e){
	    		flag = false;
	    		e.printStackTrace();
	    	}
    	}
    	return flag;
    }
    
    /**
     * 注销用户
     * @return
     */
    public static boolean disconnectAccount(){
    	boolean flag = false;
    	//判断非游客登录
    	if(App.getUserType() != Constants.USER_TYPE_INNER){
    		if(IMManager.getSessionManager().getConnection() != null && IMManager.getSessionManager().getConnection().isConnected()){
		    	try{
					//将登陆状态改变为“离线”
					Presence presence = new Presence(Presence.Type.unavailable);
					IMManager.getSessionManager().getConnection().sendPacket(presence);
					
					sendGroupOfflinePackage();
					
					IMConstants.setUserJID("");
					IMConstants.setGroupList(new ArrayList<GroupItemInfo>());
					IMConstants.setPersonVCard(new PersonVCardEntity());
					IMConstants.setRoomPersonInfoList(new ArrayList<PersonPresenceEntity>());
					IMConstants.setUserState(IMConstants.STATE_OFFLINE);
					flag = true;
		    	} catch(Exception e){
		    		flag = false;
		    		e.printStackTrace();
		    	} finally {
					//断开连接
					IMManager.getSessionManager().getConnection().disconnect();
		    	}
    		}
    	}
    	return flag;
    }
    /**
     * 
     * <p>发送群离线包</P>
     *
     */
    private static void sendGroupOfflinePackage() {
        Presence presence = new Presence(Presence.Type.unavailable);
        presence.setFrom(IMConstants.getUserJID());
        presence.setTo(IMConstants.roomServerName);
        IMManager.getSessionManager().getConnection().sendPacket(presence);
    }
    
    public String getNickname() {
        // Default to node if nothing.
        String username = IMManager.getSessionManager().getUsername();
        username = StringUtils.unescapeNode(username);

        return username;
    }
    
    /**
     * Return a Collection of all user jids found in the specified room.
     *
     * @param room    the name of the chatroom
     * @param fullJID set to true if you wish to have the full jid with resource, otherwise false
     *                for the bare jid.
     * @return a Collection of jids found in the room.
     */
    public Collection<String> getUserJidsInRoom(String room, boolean fullJID) {
        return new ArrayList<String>();
    }

    /**
     * Checks to see if the Occupant is the owner of the room.
     *
     * @param occupant the occupant of a room.
     * @return true if the user is an owner.
     */
    public boolean isOwner(Occupant occupant) {
        if (occupant != null) {
            String affiliation = occupant.getAffiliation();
            if ("owner".equals(affiliation)) {
                return true;
            }
        }
        return false;
    }
    /**
     * Checks if the Occupant is a Member in this Room<br>
     * <b>admins and owners are also members!!!</b>
     * @param occupant
     * @return true if member, else false
     */
    public boolean isMember(Occupant occupant) {
	if (occupant != null) {
	    String affiliation = occupant.getAffiliation();
	    if ("member".equals(affiliation) || affiliation.equals("owner")
		    || affiliation.equals("admin")) {
		return true;
	    }
	}
	return false;
    }

    /**
     * Checks to see if the Occupant is a moderator.
     *
     * @param occupant the Occupant of a room.
     * @return true if the user is a moderator.
     */
    public boolean isModerator(Occupant occupant) {
        if (occupant != null) {
            String role = occupant.getRole();
            if ("moderator".equals(role)) {
                return true;
            }
        }
        return false;
    }


    /**
     * Checks to see if the user is either an owner or admin of the given room.
     *
     * @param occupant the <code>Occupant</code> to check.
     * @return true if the user is either an owner or admin of the room.
     */
    public boolean isOwnerOrAdmin(Occupant occupant) {
        if (occupant != null) {
            String affiliation = occupant.getAffiliation();
            if ("owner".equals(affiliation) || "admin".equals(affiliation)) {
                return true;
            }
        }
        return false;
    }


    /**
     * Escapes a complete JID by examing the Node itself and escaping
     * when neccessary.
     *
     * @param jid the users JID
     * @return the escaped JID.
     */
    public static String escapeJID(String jid) {
        if (jid == null) {
            return null;
        }

        final StringBuilder builder = new StringBuilder();
        String node = StringUtils.parseName(jid);
        String restOfJID = jid.substring(node.length());
        builder.append(StringUtils.escapeNode(node));
        builder.append(restOfJID);
        return builder.toString();
    }

    /**
     * Unescapes a complete JID by examing the node itself and unescaping when necessary.
     *
     * @param jid the users jid.
     * @return the unescaped JID.
     */
    public static String unescapeJID(String jid) {
        if (jid == null) {
            return null;
        }

        final StringBuilder builder = new StringBuilder();
        String node = StringUtils.parseName(jid);
        String restOfJID = jid.substring(node.length());
        builder.append(StringUtils.unescapeNode(node));
        builder.append(restOfJID);
        return builder.toString();
    }



    /**
     * Returns the correct JID based on the number of resources signed in.
     *
     * @param jid the users jid.
     * @return the valid jid to use.
     */
    public static String getValidJID(String jid) {
        Roster roster = IMManager.getConnection().getRoster();
        Iterator<Presence> presences = roster.getPresences(jid);
        int count = 0;
        Presence p = null;
        if (presences.hasNext()) {
            p = presences.next();
            count++;
        }

        if (count == 1 && p != null) {
            return p.getFrom();
        }
        else {
            return jid;
        }
    }
    
    public String getUserNicknameFromJID(String jid) {
        /*ContactList contactList = IMManager.getWorkspace().getContactList();
        ContactItem item = contactList.getContactItemByJID(jid);
        if (item != null) {
            return item.getDisplayName();
        }*/

        return unescapeJID(jid);
    }
    

    /**
     * 
      * @Title: getEntriesByGroup
      * @Description: 获取某个组里面的所有好友 
      * @param @param groupName
      * @param @return
      * @return List<RosterEntry>    返回类型
      * @throws
     */
    public static List<RosterEntry> getEntriesByGroup(String groupName) {  
    	Log.d(TAG, "getEntriesByGroup:"+groupName);
        if (IMManager.getSessionManager().getConnection() == null)  
            return null;  
        List<RosterEntry> Entrieslist = new ArrayList<RosterEntry>();
        Log.d(TAG, "getEntriesByGroup begin request");
        RosterGroup rosterGroup = IMManager.getSessionManager().getConnection().getRoster().getGroup(  
                groupName);
        Log.d(TAG, "getEntriesByGroup end request");
        if(null != rosterGroup) {
        	Log.d(TAG, "getEntriesByGroup rosterGroup is not null");
        	Collection<RosterEntry> rosterEntry = rosterGroup.getEntries();  
        	Log.d(TAG, "getEntriesByGroup rosterEntry size:"+rosterEntry.size());
            Iterator<RosterEntry> i = rosterEntry.iterator();  
            while (i.hasNext()) {  
                Entrieslist.add(i.next());  
            }  
        }
       
        return Entrieslist;  
    }
    
    /** 
     * 初始化会议室列表 
     */ 
    public static List<HostedRoom> getHostRooms() {  
        if (IMManager.getSessionManager().getConnection() == null)  
            return null;  
        Collection<HostedRoom> hostrooms = null;  
        List<HostedRoom> roominfos = new ArrayList<HostedRoom>();  
        try {  
            new ServiceDiscoveryManager(IMManager.getSessionManager().getConnection());  
            hostrooms = MultiUserChat.getHostedRooms(IMManager.getSessionManager().getConnection(),  
            		IMConstants.roomServerName);  
            for (HostedRoom entry : hostrooms) {  
                roominfos.add(entry);
                Log.i(TAG, "名字：" + entry.getName() + " - ID:" + entry.getJid());  
            }  
            Log.i(TAG, "服务会议数量:" + roominfos.size());  
        } catch (XMPPException e) {  
            e.printStackTrace();  
        }  
        return roominfos;  
    }

    public static MultiUserChat getMultiUserChat(String jid) {
    	 MultiUserChat muc = new MultiUserChat(IMManager.getSessionManager().getConnection(), jid);

    	 try {
    		 Iterator<String> itrs =  muc.getOccupants();
    	        while(itrs.hasNext()){
    	            Log.i(TAG, "occupant: "+itrs.next());
    	        }
    		 
    	       
    	        
    	        muc.getOccupantsCount();
    	        muc.getModerators();
    	        muc.getAdmins();
			Collection<Affiliate> list = muc.getMembers();
			for(Iterator<Affiliate> iter = list.iterator();iter.hasNext();)
			{
				Affiliate affiliate = iter.next();
				Log.i(TAG, "affiliate.getJid():" + affiliate.getJid() + " affiliate.getNick()" + affiliate.getNick());  
				
			}
			
		} catch (XMPPException e) {
			e.printStackTrace();
		}
    	 return muc;
    }
    
    
    
    /** 
     * 查询会议室成员名字 
     *  
     * @param muc 
     */ 
    public static List<String> findMulitUser(String room) {  
    	Log.d(TAG, "findMulitUser room:"+room);
        if (IMManager.getSessionManager().getConnection() == null)  
            return null;
        MultiUserChat muc = new MultiUserChat(IMManager.getSessionManager().getConnection(), room);
        String getRoom = muc.getRoom();
        Log.d(TAG, "findMulitUser getRoom:"+getRoom);
        List<String> listUser = findMulitUser(muc);
        return listUser;  
    }  
    
    /** 
     * 查询会议室成员名字 
     *  
     * @param muc 
     */ 
    public static List<String> findMulitUser(MultiUserChat muc) {  
        if (IMManager.getSessionManager().getConnection() == null)  
            return null;  
        List<String> listUser = new ArrayList<String>();  
        Iterator<String> it = muc.getOccupants();  
        // 遍历出聊天室人员名称  
        while (it.hasNext()) {
        	
            // 聊天室成员名字  
            String name = StringUtils.parseResource(it.next());  
            Log.d(TAG, "findMulitUser :"+name);
            listUser.add(name);  
        }  
        return listUser;  
    }  
    
    /** 
     * 加入会议室 
     *  
     * @param user 昵称 
     * @param password 会议室密码 
     * @param roomsName 会议室名 
     * @param connection  
     */  
    public static void joinMultiUserChat(String user, String password, String roomsName,  
            XMPPConnection connection) {  
        try {  
            // 使用XMPPConnection创建一个MultiUserChat窗口
        	if(connection == null){
        		UserManager.userLogin(null, false);
        	} else {
        		final SessionManager sessionManager = IMManager.getSessionManager();
				//连接丢失,重新连接
				if(!sessionManager.getConnection().isConnected()){
					UserManager.userLogin(null, false);
				}
        	}
            MultiUserChat muc = new MultiUserChat(connection, roomsName);  
            // 聊天室服务将会决定要接受的历史记录数量  
            DiscussionHistory history = new DiscussionHistory();  
            history.setMaxStanzas(0);  
            //history.setSince(new Date());  
            // 用户加入聊天室  
            muc.join(user, password, history, SmackConfiguration.getPacketReplyTimeout());  
            Log.e(TAG, "会议室加入成功........");  
        } catch (XMPPException e) {  
            e.printStackTrace();  
            Log.e(TAG, "会议室加入失败........");  
        }  
    }  
    
    /** 
     * 获取用户VCard信息 
     *  
     * @param connection 
     * @param user 
     * @return 
     * @throws XMPPException 
     */ 
    public static VCard getUserVCard(String user) {  
        if (IMManager.getSessionManager().getConnection() == null)  
            return null;  
        VCard vcard = new VCard();  
        try {  
            vcard.setFrom(user);
            vcard.load(IMManager.getSessionManager().getConnection());
        } catch (XMPPException e) {  
            e.printStackTrace();  
        }  
        return vcard;  
    }  
}



