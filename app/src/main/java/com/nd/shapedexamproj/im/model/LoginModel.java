package com.nd.shapedexamproj.im.model;

import com.nd.shapedexamproj.entity.PersonVCardEntity;
import com.nd.shapedexamproj.im.manager.IMManager;
import com.nd.shapedexamproj.im.manager.SessionManager;
import com.nd.shapedexamproj.im.manager.UserManager;
import com.nd.shapedexamproj.im.model.filter.ReceiveFilter;
import com.nd.shapedexamproj.im.model.filter.SendFilter;
import com.nd.shapedexamproj.im.model.listener.IMConnectionListener;
import com.nd.shapedexamproj.im.model.listener.ReceiveListener;
import com.nd.shapedexamproj.im.model.listener.SendListener;
import com.nd.shapedexamproj.im.resource.IMConstants;
import com.tming.common.util.Log;

import org.jivesoftware.smack.SmackConfiguration;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smackx.packet.VCard;


/**
 * @Title: LoginModel
 * @Description: IM登录模块
 * @author WuYuLong
 * @date 2014-5-7
 * @version V1.0
 */
public class LoginModel {
	private final String TAG = "LoginModel";
	private static LoginModel loginModel = null;
	public static final int LOGIN_STATE_ERROR = 0, LOGIN_STATE_SUCCESS = 1, LOGIN_STATE_PWD_ERROR = 2;
	private int imLoginState = LOGIN_STATE_ERROR;

	// 单例模式
	public static LoginModel getInstance() {
		if (loginModel == null) {
			loginModel = new LoginModel();
		}
		return loginModel;
	}

	/**
	 * 带离线消息的IM登录
	 * @param userId
	 * @param pwd
	 * @param isGetOffLineMsg
	 * @return
	 */
	public int login(String userId, String pwd, boolean isGetOffLineMsg) {
		final SessionManager sessionManager = IMManager.getSessionManager();
		XMPPConnection.DEBUG_ENABLED = false;
		SmackConfiguration.setPacketReplyTimeout(IMConstants.timeOut * 1000);

		// 获取链接
		try {
			// 请求连接
			sessionManager.connection();
			// 配置消息包监听
			sessionManager.getConnection().addPacketListener(new ReceiveListener(), new ReceiveFilter());
			//sessionManager.getConnection().addPacketListener(new ReceiveListener(), new PacketTypeFilter(Presence.class));
			// 执行登录
			sessionManager.getConnection().login(userId, pwd, IMConstants.resource);
			
			//判断获取离线消息
			if(isGetOffLineMsg){
				//getOfflineMessage(sessionManager.getConnection());
	            /*for (Message offlineMessage : ReceiveFilter.offlineMessages) {
	            	Log.e(TAG, offlineMessage.getBody());
	            }*/
			}
			
			// 发送登录状态
			Presence presence = new Presence(Presence.Type.available);
			sessionManager.getConnection().sendPacket(presence);
			
			sessionManager.setServerAddress(sessionManager.getConnection().getServiceName());
			sessionManager.initializeSession(sessionManager.getConnection(),userId, pwd);
			sessionManager.setJID(sessionManager.getConnection().getUser());
			//添加消息过滤器用于监听
			sessionManager.getConnection().addPacketWriterListener(new SendListener(), new SendFilter());
			sessionManager.getConnection().addConnectionListener(new IMConnectionListener());

			imLoginState = LOGIN_STATE_SUCCESS;
			VCard vUserCard= UserManager.getUserVCard(IMConstants.getUserJID());
			//Log.d(TAG, "getNickName:" + vUserCard.getNickName());
			PersonVCardEntity personVCardEntity = new PersonVCardEntity();
			personVCardEntity.setvCard(vUserCard);
			IMConstants.setPersonVCard(personVCardEntity);
			
		} /*catch (SaslException e) {
			Log.e(TAG, "SaslException" + e.getMessage());
			imLoginState = LOGIN_STATE_ERROR;
			// 异常断开连接
			sessionManager.connectionClosed();
		}*/catch (XMPPException e) {
			Log.e(TAG, "XMPPException" + e.getMessage());
			imLoginState = LOGIN_STATE_ERROR;
			// 异常断开连接
			sessionManager.connectionClosed();
		} catch (Exception e) {
			Log.e(TAG, "Exception" + e.getMessage());
			imLoginState = LOGIN_STATE_ERROR;
			// 异常断开连接
			sessionManager.connectionClosed();
		} finally {
		    IMConstants.setUserJID(userId + "@" + IMConstants.areaServerName + "/" + IMConstants.resource);
		}

		return imLoginState;
	}
}
