/**  
 * Project Name:OpenUniversity  
 * File Name:PersonVCardEntity.java  
 * Package Name:com.tming.openuniversity.entity  
 * Date:2014-6-12下午3:28:56  
 * Copyright (c) 2014, XueWenJian All Rights Reserved.  
 *  
 */

package com.nd.shapedexamproj.entity;

import com.nd.shapedexamproj.im.resource.IMConstants;
import com.nd.shapedexamproj.util.StringUtils;

import org.jivesoftware.smackx.packet.VCard;

/**
 * ClassName:PersonVCardEntity <br/>
 * description: IM用户数据 <br/>
 * Date: 2014-6-12 下午3:28:56 <br/>
 * 
 * @author XueWenJian
 * @version
 * @since JDK 1.6
 * @see
 */
public class PersonVCardEntity {

	private VCard vCard;

	public VCard getvCard() {
		return vCard;
	}

	public void setvCard(VCard vCard) {
		this.vCard = vCard;
	}

	public String getNickName() {
		String nickName = vCard.getNickName();
		if(StringUtils.isEmpty(nickName)) {
			//当昵称不存在是使用登录Id(userid)补充
			nickName = IMConstants.getLoginId();
		}
		return nickName;
	}
}
