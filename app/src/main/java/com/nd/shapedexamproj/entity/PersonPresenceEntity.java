/**  
 * Project Name:OpenUniversity  
 * File Name:PersonPresenceEntity.java  
 * Package Name:com.tming.openuniversity.entity  
 * Date:2014-6-12上午9:34:56  
 * Copyright (c) 2014, XueWenJian All Rights Reserved.  
 *  
*/  

  
package com.nd.shapedexamproj.entity;

import java.io.Serializable;

/**  
 * ClassName:PersonPresenceEntity <br/>  
 * description: IM-用户信息 <br/>  
 * Date:     2014-6-12 上午9:34:56 <br/>  
 * @author   XueWenJian  
 * @version    
 * @since    JDK 1.6  
 * @see        
 */
public class PersonPresenceEntity implements Serializable{

	private static final long serialVersionUID = -7928247415658338158L;

	/*<presence to="boy@example1.com/openuniversity_1.0" from="1@conference.example1.com/1000887947">
		<x xmlns="http://jabber.org/protocol/muc#user">
			<item affiliation="member" role="none"><reason><
				/reason>
				<actor jid="1000887947@example1.com"/>
			</item><status code="102"/>
		</x>
	   </presence>*/
	
	private String userJId;//1000887947@example1.com
	
	private String userId;//1000887947
	
	private String userNickName;//1000887947
	
	private String from;//1@conference.example1.com/1000887947
	
	private String fromRoomJid;//1@conference.example1.com

	public String getUserJId() {
		return userJId;
	}

	public void setUserJId(String userJId) {
		this.userJId = userJId;
	}

	public String getUserNickName() {
		return userNickName;
	}

	public void setUserNickName(String userNickName) {
		this.userNickName = userNickName;
	}

	public String getFrom() {
		return from;
	}

	public void setFrom(String from) {
		this.from = from;
	}

	public String getFromRoomJid() {
		return fromRoomJid;
	}

	public void setFromRoomJid(String fromRoomJid) {
		this.fromRoomJid = fromRoomJid;
	}

	
	

}
  
