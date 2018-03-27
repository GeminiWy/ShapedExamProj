package com.nd.shapedexamproj.model.course;

import com.google.gson.annotations.SerializedName;

/**
 * 
 * @ClassName: CoachTopicDetail
 * @Title:
 * @Description:课堂-辅导讨论区-帖子详情 实体
 * @Author:XueWenJian
 * @Since:2014年5月22日10:47:35
 * @Version:1.0
 */
public class CoachTopicDetail {

	private String title;
	
	private String content;
	
	@SerializedName("reply_num")
	private int relyNum;
	
	@SerializedName("add_uid")
	private String authorId;
	
	@SerializedName("user_name")
	private String authorName;
	
	@SerializedName("user_avatar")
	private String authorAvatar;
	
	@SerializedName("add_time")
	private long addTimeStamp;
	
	@SerializedName("user_title")
	private String authorTitle;

	public String getAuthorTitle() {
		return authorTitle;
	}

	public void setAuthorTitle(String authorTitle) {
		this.authorTitle = authorTitle;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public int getRelyNum() {
		return relyNum;
	}

	public void setRelyNum(int relyNum) {
		this.relyNum = relyNum;
	}

	public String getAuthorId() {
		return authorId;
	}

	public void setAuthorId(String authorId) {
		this.authorId = authorId;
	}

	public String getAuthorName() {
		return authorName;
	}

	public void setAuthorName(String authorName) {
		this.authorName = authorName;
	}

	public String getAuthorAvatar() {
		return authorAvatar;
	}

	public void setAuthorAvatar(String authorAvatar) {
		this.authorAvatar = authorAvatar;
	}

	public long getAddTimeStamp() {
		return addTimeStamp;
	}

	public void setAddTimeStamp(long addTimeStamp) {
		this.addTimeStamp = addTimeStamp;
	}


	
}
