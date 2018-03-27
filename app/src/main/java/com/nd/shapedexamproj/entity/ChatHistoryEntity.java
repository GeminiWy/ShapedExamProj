/**  
 * Project Name:OpenUniversity  
 * File Name:ChatHistoryEntity.java  
 * Package Name:com.tming.openuniversity.entity  
 * Date:2014-6-12下午8:55:20  
 * Copyright (c) 2014, XueWenJian All Rights Reserved.  
 *  
*/  

  
package com.nd.shapedexamproj.entity;

/**  
 * ClassName:ChatHistoryEntity <br/>  
 * description: 聊天记录（最近联系的人） <br/>  
 * Date:     2014-6-12 下午8:55:20 <br/>  
 * @author   XueWenJian  
 * @version    
 * @since    JDK 1.6  
 * @see        
 */
public class ChatHistoryEntity {

	private static final String TAG = "ChatHistoryEntity";
	
	/**
	 *	0-单聊 1-聊天室
	 */
	public static int CHAT_TYPE_SINGLE = 0, CHAT_TYPE_ROOM = 1 ;
	
	private String id;
	
	private String userId;
	
	private String name;
	
	private int chatType;
	
	private String oppId;
	/**
	 * 更新的时间戳（最新消息到达的时间戳）
	 */
	private long updateTimeStamp;
	
	/**
	 * 置顶的时间戳
	 */
	private long topTimeStamp;
	
	
	private String tag;
	
	/**
	 * 聊天内容，不存入最近联系人表
	 */
	private String content;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getChatType() {
		return chatType;
	}

	public void setChatType(int chatType) {
		this.chatType = chatType;
	}


	public long getUpdateTimeStamp() {
		return updateTimeStamp;
	}

	public void setUpdateTimeStamp(long updateTimeStamp) {
		this.updateTimeStamp = updateTimeStamp;
	}

	public long getTopTimeStamp() {
		return topTimeStamp;
	}

	public void setTopTimeStamp(long topTimeStamp) {
		this.topTimeStamp = topTimeStamp;
	}

	public String getOppId() {
		return oppId;
	}

	public void setOppId(String oppId) {
		this.oppId = oppId;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getTag() {
		return tag;
	}

	public void setTag(String tag) {
		this.tag = tag;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}
	
	
	
}
  
