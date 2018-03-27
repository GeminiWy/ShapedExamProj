/**  
 * Project Name:OpenUniversity  
 * File Name:RoomPresence.java  
 * Package Name:com.tming.openuniversity.im.packet  
 * Date:2014-6-11下午12:50:53  
 * Copyright (c) 2014, XueWenJian All Rights Reserved.  
 *  
*/  

  
package com.nd.shapedexamproj.im.packet;

import org.jivesoftware.smack.packet.PacketExtension;
import org.jivesoftware.smack.packet.Presence;

/**  
 * ClassName:RoomPresence <br/>  
 * description: TODO add description. <br/>  
 * Date:     2014-6-11 下午12:50:53 <br/>  
 * @author   XueWenJian  
 * @version    
 * @since    JDK 1.6  
 * @see        
 */
public class RoomPresence extends Presence {
	private String positionElement;
	 
	public RoomPresence(Type arg0,String element) {
		super(arg0);
		positionElement = element;
		addExtension(new PacketExtension() {
			
			@Override
			public String toXML() {
	
				return positionElement;
			}
			
			@Override
			public String getNamespace() {
				//return "x";
				return null;
			}
			
			@Override
			public String getElementName() {
				//return "http://jabber.org/protocol/muc";
				return null;
			}
		});
	}
}
  
