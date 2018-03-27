/**  
 * Project Name:OpenUniversity  
 * File Name:SendListener.java  
 * Package Name:com.tming.openuniversity.im.model.listener  
 * Date:2014-6-10下午9:02:04  
 * Copyright (c) 2014, XueWenJian All Rights Reserved.  
 *  
*/  

  
package com.nd.shapedexamproj.im.model.listener;

import android.util.Log;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;

/**  
 * ClassName:SendListener <br/>  
 * description: TODO add description. <br/>  
 * Date:     2014-6-10 下午9:02:04 <br/>  
 * @author   XueWenJian  
 * @version    
 * @since    JDK 1.6  
 * @see        
 */
public class SendListener implements PacketListener{
	
	private static final String TAG = SendListener.class.getSimpleName();
	
	@Override
	public void processPacket(final Packet packet) {
        new Runnable() {
            public void run() {
                handleIncomingPacket(packet);
            }
        };
	}
	
	/**
	 * 解析消息
	 * @param packet
	 */
	private void handleIncomingPacket(Packet packet) {
        if (packet instanceof Message) {
        	Log.e(TAG, "handleIncomingPacket begin") ;
            final Message message = (Message)packet;
            Log.e(TAG, "ReceiveListener接收到消息 message.getBody():"+message.getBody()) ;
        }
	}
}
  
