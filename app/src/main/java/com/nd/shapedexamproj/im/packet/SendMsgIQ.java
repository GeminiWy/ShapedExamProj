/**  
 * Project Name:OpenUniversity  
 * File Name:SendMsgIQ.java  
 * Package Name:com.tming.openuniversity.im.packet  
 * Date:2014-6-11上午11:26:16  
 * Copyright (c) 2014, XueWenJian All Rights Reserved.  
 *  
 */

package com.nd.shapedexamproj.im.packet;

import org.jivesoftware.smack.packet.IQ;

/**
 * ClassName:SendMsgIQ <br/>
 * description: SendMsgIQ <br/>
 * Date: 2014-6-11 上午11:26:16 <br/>
 * 
 * @author XueWenJian
 * @version
 * @since JDK 1.6
 * @see
 */
public class SendMsgIQ extends IQ {

	private String positionElement;

	// constructor
	public SendMsgIQ(String element) {

		setPositionElement(element);
		toXML();
	}

	// get and set
	public String getPositionElement() {
		return positionElement;
	}

	public void setPositionElement(String positionElement) {
		this.positionElement = positionElement;
	}

	// get and set end
	@Override
	public String getChildElementXML() {
		// TODO Auto-generated method stub
		return getPositionElement();
	}

}
