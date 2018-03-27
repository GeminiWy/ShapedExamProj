package com.nd.shapedexamproj.im.manager;

import com.nd.shapedexamproj.im.resource.IMConstants;

import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.provider.ProviderManager;
import org.jivesoftware.smack.util.StringUtils;
import org.jivesoftware.smackx.PrivateDataManager;
import org.jivesoftware.smackx.packet.DiscoverItems;
/**
 * @Title: SessionManager
 * @Description: Session管理类
 * @author WuYuLong
 * @date 2014-5-7
 * @version V1.0
 */
public final class SessionManager implements ConnectionListener {
    private XMPPConnection connection;
    private PrivateDataManager personalDataManager;

    private String serverAddress;
    private String username;
    private String password;

    private String JID;

    private String userBareAddress;
    private DiscoverItems discoverItems;

    public SessionManager() {
    }

    /**
     * Initializes session.
     *
     * @param connection the XMPPConnection used in this session.
     * @param username   the agents username.
     * @param password   the agents password.
     */
    public void initializeSession(XMPPConnection connection, String username, String password) {
        this.connection = connection;
        this.username = username;
        this.password = password;
        this.userBareAddress = StringUtils.parseBareAddress(connection.getUser());

        // create workgroup session
        personalDataManager = new PrivateDataManager(getConnection());

        // Discover items

        IMManager.configure(ProviderManager.getInstance());
        //ProviderManager.getInstance().addExtensionProvider("event", "http://jabber.org/protocol/disco#info", new Features.Provider());
    }


    /**
     * Returns the XMPPConnection used for this session.
     *
     * @return the XMPPConnection used for this session.
     */
    public XMPPConnection getConnection() {
        return connection;
    }


    /**
     * Returns the PrivateDataManager responsible for handling all private data for individual
     * agents.
     *
     * @return the PrivateDataManager responsible for handling all private data for individual
     *         agents.
     */
    public PrivateDataManager getPersonalDataManager() {
        return personalDataManager;
    }


    /**
     * Returns the host for this connection.
     *
     * @return the connection host.
     */
    public String getServerAddress() {
        return serverAddress;
    }

    /**
     * Set the server address
     *
     * @param address the address of the server.
     */
    public void setServerAddress(String address) {
        this.serverAddress = address;
    }
    
    /**
     * Connect the connection with throws exception.
     *
     * @param ex the Exception that took place.
     */
    public void connection() throws Exception {
    	//设置连接配置
		ConnectionConfiguration config = new ConnectionConfiguration(IMConstants.hostServerName, IMConstants.port, IMConstants.areaServerName);
		config.setReconnectionAllowed(true);
		config.setRosterLoadedAtLogin(true);
		config.setSendPresence(false);
		config.setCompressionEnabled(false);
		config.setDebuggerEnabled(true);
		config.setSASLAuthenticationEnabled(true);
		connection = new XMPPConnection(config);
		
		//请求连接
		connection.connect();
    }

    /**
     * Notify agent the connection was closed due to an exception.
     *
     * @param ex the Exception that took place.
     */
    public void connectionClosedOnError(final Exception ex) {
		if(connection != null && connection.isConnected()) {
			connection.disconnect();
		}
		ex.printStackTrace();
    }

    /**
     * Notify agent that the connection has been closed.
     */
    public void connectionClosed() {
		if(connection != null && connection.isConnected()) {
			connection.disconnect();
		}
    }

    /**
     * Return the username associated with this session.
     *
     * @return the username associated with this session.
     */
    public String getUsername() {
        return StringUtils.unescapeNode(username);
    }

    /**
     * Return the password associated with this session.
     *
     * @return the password assoicated with this session.
     */
    public String getPassword() {
        return password;
    }


    /**
     * Returns the jid of the Spark user.
     *
     * @return the jid of the Spark user.
     */
    public String getJID() {
        return JID;
    }

    /**
     * Sets the jid of the current Spark user.
     *
     * @param jid the jid of the current Spark user.
     */
    public void setJID(String jid) {
        this.JID = jid;
    }

    /**
     * Returns the users bare address. A bare-address is the address without a resource (ex. derek@jivesoftware.com/spark would
     * be derek@jivesoftware.com)
     *
     * @return the users bare address.
     */
    public String getBareAddress() {
        return userBareAddress;
    }


    /**
     * Returns the Discovered Items.
     *
     * @return the discovered items found on startup.
     */
    public DiscoverItems getDiscoveredItems() {
        return discoverItems;
    }

    public void setConnection(XMPPConnection con) {
        this.connection = con;
    }

    public void reconnectingIn(int i) {
    }

    public void reconnectionSuccessful() {
    }

    public void reconnectionFailed(Exception exception) {
    }

}
