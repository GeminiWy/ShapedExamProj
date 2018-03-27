package com.nd.shapedexamproj.model.course;

import com.google.gson.annotations.SerializedName;

/**
 * 
 * @ClassName: CoachTopic
 * @Title:
 * @Description:课堂-辅导讨论区-帖子信息 实体
 * @Author:XueWenJian
 * @Since:2014年5月21日10:45:19
 * @Version:1.0
 */
public class CoachTopic implements Comparable<CoachTopic>{

	
	@SerializedName("topic_id")
	private String topicId;
	
	@SerializedName("ttl_reply")
	private int relyNum;
	
	@SerializedName("add_uid")
	private String authorId;
	
	@SerializedName("user_name")
	private String authorName;
	
	@SerializedName("add_time")
	private long addTimeStamp;
	
	@SerializedName("title")
	private String title;

	public String getTopicId() {
		return topicId;
	}

	public void setTopicId(String topicId) {
		this.topicId = topicId;
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

	public long getAddTimeStamp() {
		return addTimeStamp;
	}

	public void setAddTimeStamp(long addTimeStamp) {
		this.addTimeStamp = addTimeStamp;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getAuthorName() {
		return authorName;
	}

	public void setAuthorName(String authorName) {
		this.authorName = authorName;
	}

	@Override
	public int compareTo(CoachTopic another) {	//根据时间排序
		return -(int)(addTimeStamp - another.addTimeStamp) ;
	}
	
	
}
