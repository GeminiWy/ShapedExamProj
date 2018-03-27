package com.nd.shapedexamproj.model.course;

import com.google.gson.annotations.SerializedName;

/**
 * 
 * @ClassName: CoachTopicReply
 * @Title:
 * @Description:课堂-辅导讨论区-帖子详情-回复
 * @Author:XueWenJian
 * @Since:2014年5月28日21:59:21
 * @Version:1.0
 */
public class CoachTopicReply {
	
	private String id;
	
	private String title;
	
	private String content;
	
	@SerializedName("add_uid")
	private String authorId;
	
	@SerializedName("user_name")
	private String authorName;
	
	@SerializedName("user_avatar")
	private String authorAvatar;
	
	@SerializedName("add_time")
	private long addTimeStamp;
	
	@SerializedName("mdf_time")
	private long updateTimeStamp;
	
	@SerializedName("user_type")
	private int authorType;
	
	@SerializedName("user_title")
	private String authorTitle;
	
	@SerializedName("user_industryid")
	private int industryId;

	private int sex;

	private int age;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
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

	public String getAuthorId() {
		return authorId;
	}

	public void setAuthorId(String authorId) {
		this.authorId = authorId;
	}

	public long getAddTimeStamp() {
		return addTimeStamp;
	}

	public void setAddTimeStamp(long addTimeStamp) {
		this.addTimeStamp = addTimeStamp;
	}

	public long getUpdateTimeStamp() {
		return updateTimeStamp;
	}

	public void setUpdateTimeStamp(long updateTimeStamp) {
		this.updateTimeStamp = updateTimeStamp;
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

	public int getSex() {
		return sex;
	}

	public void setSex(int sex) {
		this.sex = sex;
	}

    public int getAuthorType() {
		return authorType;
	}

	public void setAuthorType(int authorType) {
		this.authorType = authorType;
	}

	public int getIndustryId() {
		return industryId;
	}

	public void setIndustryId(int industryId) {
		this.industryId = industryId;
	}

	public int getAge() {
		return age;
	}

	public void setAge(int age) {
		this.age = age;
	}

	public String getAuthorTitle() {
		return authorTitle;
	}

	public void setAuthorTitle(String authorTitle) {
		this.authorTitle = authorTitle;
	}
	
	
}
