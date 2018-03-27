package com.nd.shapedexamproj.im.manager;

import com.nd.shapedexamproj.im.packet.RoomMemmberQueryIQ;
import com.nd.shapedexamproj.im.packet.RoomOffilineMsgExtension;
import com.nd.shapedexamproj.im.packet.RoomPacketExtension;
import com.nd.shapedexamproj.im.resource.IMConstants;
import com.tming.common.util.Log;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.provider.PrivacyProvider;
import org.jivesoftware.smack.provider.ProviderManager;
import org.jivesoftware.smackx.GroupChatInvitation;
import org.jivesoftware.smackx.MessageEventManager;
import org.jivesoftware.smackx.PrivateDataManager;
import org.jivesoftware.smackx.ServiceDiscoveryManager;
import org.jivesoftware.smackx.packet.*;
import org.jivesoftware.smackx.provider.*;
import org.jivesoftware.smackx.search.UserSearch;

/**
 * @Title: IMManager
 * @Description: IM管理类
 * @author WuYuLong
 * @date 2014-5-7
 * @version V1.0
 */
public final class IMManager {
	
	private static final String TAG = "IMManager";
	
	private static SessionManager sessionManager;
	private static MessageEventManager messageEventManager;
	private static UserManager userManager;

	private IMManager() {
		// Do not allow initialization
	}

	/**
	 * Gets the {@link SessionManager} instance.
	 * 
	 * @return the SessionManager instance.
	 */
	public static SessionManager getSessionManager() {
		if (sessionManager == null) {
			sessionManager = new SessionManager();
		}
		return sessionManager;
	}

	/**
	 * Gets the {@link XMPPConnection} instance.
	 * 
	 * @return the {@link XMPPConnection} associated with this session.
	 */
	public static XMPPConnection getConnection() {
		if (null != sessionManager) {
			return sessionManager.getConnection();
		}
		return null;
	}

	/**
	 * Returns the <code>UserManager</code> for LiveAssistant. The UserManager
	 * keeps track of all users in current chats.
	 * 
	 * @return the <code>UserManager</code> for LiveAssistant.
	 */
	public static UserManager getUserManager() {
		if (userManager == null) {
			userManager = new UserManager();
		}
		return userManager;
	}

	/**
	 * Returns the <code>MessageEventManager</code> used in Spark. The
	 * MessageEventManager is responsible for XMPP specific operations such as
	 * notifying users that you have received their message or inform a users
	 * that you are typing a message to them.
	 * 
	 * @return the MessageEventManager used in Spark.
	 */
	public static MessageEventManager getMessageEventManager() {
		if (messageEventManager == null) {
			messageEventManager = new MessageEventManager(getConnection());
		}
		return messageEventManager;
	}

	/**
	 * Adds a feature that can be discovered through Disco.
	 * 
	 * @param namespace
	 *            the namespace of the feature.
	 */
	public static void addFeature(String namespace) {
		// Obtain the ServiceDiscoveryManager associated with my XMPPConnection
		ServiceDiscoveryManager discoManager = ServiceDiscoveryManager
				.getInstanceFor(getConnection());

		// Register that a new feature is supported by this XMPP entity
		discoManager.addFeature(namespace);
	}

	/**
	 * Removes a feature that can be discovered through Disco.
	 * 
	 * @param namespace
	 *            the namespace to remove.
	 */
	public static void removeFeature(String namespace) {
		// Obtain the ServiceDiscoveryManager associated with my XMPPConnection
		ServiceDiscoveryManager discoManager = ServiceDiscoveryManager
				.getInstanceFor(getConnection());

		// Register that a new feature is supported by this XMPP entity
		discoManager.removeFeature(namespace);
	}

	/**
	 * 发送消息
	 * @param
	 */
	public static boolean sendPresenceMessage(Presence presence) {
		boolean flag = false;
		try {
			Packet packet = presence;
			
			Log.e(TAG, "send packet:" +presence.toXML());
			sendPacket(presence);
		} catch (Exception ex) {
			ex.printStackTrace();
			flag = false;
		}
		return flag;
	}
	
	/**
	 * 发送请求包
	 * @param packet
	 */
	public static void sendPacket(Packet packet) {
		if(IMManager.getConnection() != null ) {
			IMManager.getConnection().sendPacket(packet);	
		}
	}
	
	
	
	public static void configure(ProviderManager pm) {

		// Private Data Storage
		pm.addIQProvider("query", "jabber:iq:private",
				new PrivateDataManager.PrivateDataIQProvider());

		// Time
		try {
			pm.addIQProvider("query", "jabber:iq:time",
					Class.forName("org.jivesoftware.smackx.packet.Time"));
		} catch (ClassNotFoundException e) {
			Log.w("TestClient",
					"Can't load class for org.jivesoftware.smackx.packet.Time");
		}

		// Roster Exchange
		pm.addExtensionProvider("x", "jabber:x:roster",
				new RosterExchangeProvider());

		// Message Events
		pm.addExtensionProvider("x", "jabber:x:event",
				new MessageEventProvider());

		// Chat State
		pm.addExtensionProvider("active",
				"http://jabber.org/protocol/chatstates",
				new ChatStateExtension.Provider());
		pm.addExtensionProvider("composing",
				"http://jabber.org/protocol/chatstates",
				new ChatStateExtension.Provider());
		pm.addExtensionProvider("paused",
				"http://jabber.org/protocol/chatstates",
				new ChatStateExtension.Provider());
		pm.addExtensionProvider("inactive",
				"http://jabber.org/protocol/chatstates",
				new ChatStateExtension.Provider());
		pm.addExtensionProvider("gone",
				"http://jabber.org/protocol/chatstates",
				new ChatStateExtension.Provider());

		// XHTML
		pm.addExtensionProvider("html", "http://jabber.org/protocol/xhtml-im",
				new XHTMLExtensionProvider());

		// Group Chat Invitations
		pm.addExtensionProvider("x", "jabber:x:conference",
				new GroupChatInvitation.Provider());

		// Service Discovery # Items
		pm.addIQProvider("query", "http://jabber.org/protocol/disco#items",
				new DiscoverItemsProvider());

		// Service Discovery # Info
		pm.addIQProvider("query", "http://jabber.org/protocol/disco#info",
				new DiscoverInfoProvider());

		// Data Forms
		pm.addExtensionProvider("x", "jabber:x:data", new DataFormProvider());

		// Room Online Events
		pm.addExtensionProvider("x", IMConstants.ROOM_PRESENCE_NAMESPACE,
				new RoomPacketExtension.Provider());
		
		//群离线消息
		pm.addExtensionProvider("x", "http://jabber.org/protocol/muc#offline", 
                new RoomOffilineMsgExtension.Provider());
		
		// Room Memmber
		pm.addIQProvider("query", IMConstants.ROOM_PRESENCE_NAMESPACE,
				new RoomMemmberQueryIQ.Provider());
		
		// MUC User
		pm.addExtensionProvider("x", "http://jabber.org/protocol/muc#user",
				new MUCUserProvider());

		// MUC Admin
		pm.addIQProvider("query", "http://jabber.org/protocol/muc#admin",
				new MUCAdminProvider());

		// MUC Owner
		pm.addIQProvider("query", "http://jabber.org/protocol/muc#owner",
				new MUCOwnerProvider());
		
		// Delayed Delivery
		pm.addExtensionProvider("x", "jabber:x:delay",
				new DelayInformationProvider());

		// Version
		try {
			pm.addIQProvider("query", "jabber:iq:version",
					Class.forName("org.jivesoftware.smackx.packet.Version"));
		} catch (ClassNotFoundException e) {
			// Not sure what's happening here.
		}

		// VCard
		pm.addIQProvider("vCard", "vcard-temp", new VCardProvider());

		// Offline Message Requests
		pm.addIQProvider("offline", "http://jabber.org/protocol/offline",
				new OfflineMessageRequest.Provider());

		// Offline Message Indicator
		pm.addExtensionProvider("offline",
				"http://jabber.org/protocol/offline",
				new OfflineMessageInfo.Provider());

		// Last Activity
		pm.addIQProvider("query", "jabber:iq:last", new LastActivity.Provider());

		// User Search
		pm.addIQProvider("query", "jabber:iq:search", new UserSearch.Provider());

		// SharedGroupsInfo
		pm.addIQProvider("sharedgroup",
				"http://www.jivesoftware.org/protocol/sharedgroup",
				new SharedGroupsInfo.Provider());

		// JEP-33: Extended Stanza Addressing
		pm.addExtensionProvider("addresses",
				"http://jabber.org/protocol/address",
				new MultipleAddressesProvider());

		// FileTransfer
		pm.addIQProvider("si", "http://jabber.org/protocol/si",
				new StreamInitiationProvider());

		pm.addIQProvider("query", "http://jabber.org/protocol/bytestreams",
				new BytestreamsProvider());

		// Privacy
		pm.addIQProvider("query", "jabber:iq:privacy", new PrivacyProvider());
		pm.addIQProvider("command", "http://jabber.org/protocol/commands",
				new AdHocCommandDataProvider());
		pm.addExtensionProvider("malformed-action",
				"http://jabber.org/protocol/commands",
				new AdHocCommandDataProvider.MalformedActionError());
		pm.addExtensionProvider("bad-locale",
				"http://jabber.org/protocol/commands",
				new AdHocCommandDataProvider.BadLocaleError());
		pm.addExtensionProvider("bad-payload",
				"http://jabber.org/protocol/commands",
				new AdHocCommandDataProvider.BadPayloadError());
		pm.addExtensionProvider("bad-sessionid",
				"http://jabber.org/protocol/commands",
				new AdHocCommandDataProvider.BadSessionIDError());
		pm.addExtensionProvider("session-expired",
				"http://jabber.org/protocol/commands",
				new AdHocCommandDataProvider.SessionExpiredError());
		
		pm.addIQProvider("sharedgroup", "http://www.jivesoftware.org/protocol/sharedgroup",
				new SharedGroupsInfo.Provider());
	}

}
