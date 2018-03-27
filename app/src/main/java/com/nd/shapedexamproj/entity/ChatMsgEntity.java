package com.nd.shapedexamproj.entity;

import java.io.Serializable;

/**
 * 
 * @ClassName: ChatMsgEntity
 * @Title:
 * @Description:IM聊天消息实体
 * @Author:XueWenJian
 * @Since:2014-4-29下午10:16:44
 * @Version:1.0
 */
public class ChatMsgEntity implements Serializable{
	
	private static final String TAG = ChatMsgEntity.class.getSimpleName();
	
	public static final int STATE_NORMAL = 0, STATE_SENDING = 1,STATE_ERROR = 2;

	private String id;
	private String tag;

	private String msgId;
	//发送者id,name,imgurl
	
    private String formId;
  
    private String formName;
    
    private String formImgurl;
    
  //接受者id ,name，imgurl
    private String toId;
    
    private String toName;
    
    private String toImgurl;
    
    
    //日期
    private String date;
    //聊天内容
    private String text;
    
    private int state;

	public String getMsgId() {
		return msgId;
	}
	public void setMsgId(String msgId) {
		this.msgId = msgId;
	}
	public String getDate() {
		return date;
	}
	public void setDate(String date) {
		this.date = date;
	}
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}
	
	public String getFormId() {
		return formId;
	}
	public void setFormId(String formId) {
		this.formId = formId;
	}
	public String getFormName() {
		return formName;
	}
	public void setFormName(String formName) {
		this.formName = formName;
	}
	public String getFormImgurl() {
		return formImgurl;
	}
	public void setFormImgurl(String formImgurl) {
		this.formImgurl = formImgurl;
	}
	public String getToId() {
		return toId;
	}
	public void setToId(String toId) {
		this.toId = toId;
	}
	public String getToName() {
		return toName;
	}
	public void setToName(String toName) {
		this.toName = toName;
	}
	public String getToImgurl() {
		return toImgurl;
	}
	public void setToImgurl(String toImgurl) {
		this.toImgurl = toImgurl;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getTag() {
		return tag;
	}
	public void setTag(String tag) {
		this.tag = tag;
	}
	public int getState() {
		return state;
	}
	public void setState(int state) {
		this.state = state;
	}

	@Override
	public boolean equals(Object o) {
		if (o == null) return false;
		if (!(o instanceof ChatMsgEntity)) {
			return false;
		}
		ChatMsgEntity chatMsgEntity = (ChatMsgEntity) o;
		if (id != null && chatMsgEntity.id.equals(id) ) {
			return true;
		}
		return false;
	}
}
