package com.nd.shapedexamproj.im.model;

import android.util.Log;

import com.nd.shapedexamproj.im.manager.IMManager;
import com.nd.shapedexamproj.im.resource.IMConstants;

import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.util.StringUtils;
/**
 * @Title: SendModel
 * @Description: IM发送消息模块
 * @author WuYuLong
 * @date 2014-5-7
 * @version V1.0
 */
public class SendModel {
	private static String threadID;
	private static final String TAG = SendModel.class.getSimpleName();
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
			message.setType(Message.Type.chat);
			message.setFrom(from + "@" + IMConstants.areaServerName + "/" + IMConstants.resource);
			message.setTo(to + "@" + IMConstants.areaServerName);
			
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
			IMManager.getConnection().sendPacket(packet);
			flag = true;
		} catch (Exception ex) {
			ex.printStackTrace();
			flag = false;
		}
		return flag;
	}
}
