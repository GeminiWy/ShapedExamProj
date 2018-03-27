package com.nd.shapedexamproj.im.model;

import com.nd.shapedexamproj.App;
import com.nd.shapedexamproj.im.manager.IMManager;
import com.nd.shapedexamproj.im.manager.UserManager;
import com.nd.shapedexamproj.im.packet.RoomMemmberQueryIQ;
import com.nd.shapedexamproj.im.resource.IMConstants;

import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.PacketExtension;
import org.jivesoftware.smack.packet.Presence;
/**  
 * ClassName:RoomModel <br/>  
 * description: IM聊天室模块 <br/>  
 * Date: 2014年7月15日下午9:30:00 <br/>
 * @author   WuYuLong  
 * @version    
 * @since    JDK 1.6  
 * @see        
 */
public class RoomModel {
	public static final String JOIN_ROOM_HEAD = "join_room_";
	public static final String ROOM_MEMMBER_HEAD = "room_memmber_";
	public final static String OFFLINE_MSG_HEAD = "offline_msg_head_";
	/**
	 * 发送聊天室登录请求包，并获取返回的聊天室列表
	 */
	public void sendJoinRoomRequest(){
		if(IMManager.getSessionManager().getConnection() != null){
			//连接丢失,重新连接
			if(!IMManager.getSessionManager().getConnection().isConnected()){
				UserManager.userLogin(App.getAppContext(), false);
			}
	    	try{
	    		StringBuffer xmlSB = new StringBuffer("");
	    		xmlSB.append("<x xmlns='http://jabber.org/protocol/muc'> <history maxstanzas='0'/> </x>");
	    		final String xmlStr = xmlSB.toString();
	    		
	    		Presence presence = new Presence(Presence.Type.available);
	    		presence.setFrom(IMConstants.getUserJID());
	    		presence.setTo(IMConstants.roomServerName);
	    		String packetId = JOIN_ROOM_HEAD + presence.getPacketID();
	    		presence.setPacketID(packetId);
	    		presence.addExtension(new PacketExtension() {
					
					@Override
					public String toXML() {
						return xmlStr;
					}
					
					@Override
					public String getNamespace() {
						return null;
					}
					
					@Override
					public String getElementName() {
						return null;
					}
				});
	    		
				IMManager.getSessionManager().getConnection().sendPacket(presence);
				
	    	} catch(Exception e){
	    		e.printStackTrace();
	    	} finally {
	    		
	    	}
		}
	}
	
	/**
	 * 获取聊天室成员列表
	 * @param roomJId
	 */
	public void sendRoomMemmberRequest(String roomJId){
		if(IMManager.getSessionManager().getConnection() != null){
			//连接丢失,重新连接
			if(!IMManager.getSessionManager().getConnection().isConnected()){
				UserManager.userLogin(App.getAppContext(), false);
			}
	    	try{
	    		StringBuffer xmlSB = new StringBuffer("");
	    		xmlSB.append("<query xmlns='http://jabber.org/protocol/muc'><item affiliation='member'/></query>");
	    		
	    		RoomMemmberQueryIQ iq = new RoomMemmberQueryIQ(xmlSB.toString());
	    		iq.setFrom(IMConstants.getUserJID());
	    		iq.setTo(roomJId);
	    		String packetId = ROOM_MEMMBER_HEAD + iq.getPacketID();
	    		iq.setPacketID(packetId);
	    		iq.setType(IQ.Type.GET);
	    		
				IMManager.getSessionManager().getConnection().sendPacket(iq);
				
	    	} catch(Exception e){
	    		e.printStackTrace();
	    	} finally {
	    		
	    	}
		}
	}
}
