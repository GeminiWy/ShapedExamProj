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
public class ChatGroupMsgEntity implements Serializable{
	
	private static final String TAG = ChatGroupMsgEntity.class.getSimpleName();
	
	public static final int STATE_NORMAL = 0, STATE_SENDING = 1,STATE_ERROR = 2;

	private String id;
	private String tag;
	
	//ROOM jid 1@conference.example1.com
	private String groupId;
	
	private String fromUserId;
	/**
	 * 消息来源用户的头像
	 */
	private String formUserImgurl;
	private String toId;
	
    //日期
    private String date;
    //聊天内容
    private String text;
    
    private int state;
	
	
	private String groupName;
/*	//发送者id,name,imgurl
	
    private String formId;
  
    private String formName;
    
    private String formImgurl;
    
  //接受者id ,name，imgurl
    private String toId;
    
    private String toName;
    
    private String toImgurl;*/
    
    private String fromUserName;
    
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ChatGroupMsgEntity)) {
            return false;
        }
        ChatGroupMsgEntity  entity = (ChatGroupMsgEntity) o;
        if (entity.fromUserId.equals(this.fromUserId) && entity.text.equals(this.text) && entity.date.equals(this.date)) {
            return true;
        } else {
            return false;
        }
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
	
	/*public String getFormId() {
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
	}*/
	public String getToId() {
		return toId;
	}
	public void setToId(String toId) {
		this.toId = toId;
	}
	/*public String getToName() {
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
	}*/
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
	public String getGroupName() {
		return groupName;
	}
	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}
	public String getFromUserName() {
		return fromUserName;
	}
	public void setFromUserName(String fromUserName) {
		this.fromUserName = fromUserName;
	}
	public String getGroupId() {
		return groupId;
	}
	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}
	public String getFromUserId() {
		return fromUserId;
	}
	public void setFromUserId(String fromUserId) {
		this.fromUserId = fromUserId;
	}

    public String getFormUserImgurl() {
        return formUserImgurl;
    }

    public void setFormUserImgurl(String formUserImgurl) {
        this.formUserImgurl = formUserImgurl;
    }
    
}
