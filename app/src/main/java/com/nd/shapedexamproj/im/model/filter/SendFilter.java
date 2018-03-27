/**  
 * Project Name:OpenUniversity  
 * File Name:SendFilter.java  
 * Package Name:com.tming.openuniversity.im.model.filter  
 * Date:2014-6-10下午9:06:04  
 * Copyright (c) 2014, XueWenJian All Rights Reserved.  
 *  
*/  

  
package com.nd.shapedexamproj.im.model.filter;

import android.util.Log;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.packet.Packet;

/**  
 * ClassName:SendFilter <br/>  
 * description: TODO add description. <br/>  
 * Date:     2014-6-10 下午9:06:04 <br/>  
 * @author   XueWenJian  
 * @version    
 * @since    JDK 1.6  
 * @see        
 */
public class SendFilter implements PacketFilter {
	
	private static final String TAG = SendFilter.class.getSimpleName();
	
    @Override
    public boolean accept(Packet packet) {
    	Log.e(TAG, "send packet:" +packet.toXML());
    	Log.e(TAG, "send Xmlns:" +packet.getXmlns());
    	
       
        return true;
    }
}

