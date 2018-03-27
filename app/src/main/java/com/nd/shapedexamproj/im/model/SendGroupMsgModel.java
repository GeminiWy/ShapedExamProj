package com.nd.shapedexamproj.im.model;

import android.util.Log;

import com.nd.shapedexamproj.App;
import com.nd.shapedexamproj.im.manager.IMManager;
import com.nd.shapedexamproj.im.manager.UserManager;
import com.nd.shapedexamproj.im.resource.IMConstants;

import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.PacketExtension;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.util.StringUtils;
/**
 * @Title: SendModel
 * @Description: IM发送消息模块
 * @author WuYuLong
 * @date 2014-5-7
 * @version V1.0
 */
public class SendGroupMsgModel {
	private static String threadID;
	private static final String TAG = SendGroupMsgModel.class.getSimpleName();
	
	private static final String MSG_END = "@conference.example1.com";
	
	/**
	 * 发送消息
	 * @param text
	 */
	public boolean sendMessage(String text, String from, String to) {
		boolean flag = false;
		try {
			final Message message = new Message();
	
			//单一线程控制
			if (threadID == null) {
				threadID = StringUtils.randomString(6);
			}
			
			//拼装消息体
			message.setThread(threadID);
			message.setBody(text);
			message.setType(Message.Type.groupchat);
			message.setFrom(from + "@" + IMConstants.areaServerName + "/" + IMConstants.resource);
			message.setTo(to);
			
			flag = sendMessage(message);
		} catch (Exception ex) {
			ex.printStackTrace();
			flag = false;
		}
		return flag;
	}
	
	/**
	 * 联网请求
	 * @param message
	 */
	public boolean sendMessage(Message message) {
		boolean flag = false;
		try {
			
			Log.e(TAG, "send message:" +message.toXML());
			Packet packet = message;

			Log.e(TAG, "send packet:" +packet.toXML());
			IMManager.sendPacket(packet);
			flag = true;
		} catch (Exception ex) {
			ex.printStackTrace();
			flag = false;
		}
		return flag;
	}
	/**
	 * 
	 * <p>获取离线消息</P>
	 *
	 */
	public void getOfflineMsg(String grpId) {
	    if(IMManager.getSessionManager().getConnection() != null){
            //连接丢失,重新连接
            if(!IMManager.getSessionManager().getConnection().isConnected()){
                UserManager.userLogin(App.getAppContext(), false);
            }
            try{
                StringBuffer xmlSB = new StringBuffer("");
                xmlSB.append("<x xmlns='http://jabber.org/protocol/muc#offline'><history maxstanzas='1'/></x>");
                final String xmlStr = xmlSB.toString();
                
                Presence presence = new Presence(Presence.Type.available);
                presence.setFrom(IMConstants.getUserJID());
                presence.setTo(grpId);
                String packetId = RoomModel.OFFLINE_MSG_HEAD + presence.getPacketID();
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
	
	
}
