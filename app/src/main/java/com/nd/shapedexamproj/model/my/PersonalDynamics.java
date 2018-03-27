package com.nd.shapedexamproj.model.my;

public class PersonalDynamics {
	private int type;
	private String content;
	private boolean top;
	private long timelineId;
	private long userId;
	private String postTime;
	private int commentedCount;
	private int commentCount;
	private int transferredCount;
	private int transferCount;
	private int classId;
	private int teachingPointId;
	private int referId;
	private String username;
	private String photo;
	private int gender = -1;
	private int age;
	
	public int getGender() {
		return gender;
	}

	public void setGender(int gender) {
		this.gender = gender;
	}

	public int getAge() {
		return age;
	}

	public void setAge(int age) {
		this.age = age;
	}

	public int getReferId() {
		return referId;
	}

	public void setReferId(int referId) {
		this.referId = referId;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPhoto() {
		return photo;
	}

	public void setPhoto(String photo) {
		this.photo = photo;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public boolean isTop() {
		return top;
	}

	public void setTop(boolean top) {
		this.top = top;
	}

	public long getTimelineId() {
		return timelineId;
	}

	public void setTimelineId(long timelineId) {
		this.timelineId = timelineId;
	}

	public long getUserId() {
		return userId;
	}

	public void setUserId(long userId) {
		this.userId = userId;
	}

	public String getPostTime() {
		return postTime;
	}

	public void setPostTime(String postTime) {
		this.postTime = postTime;
	}

	public int getCommentedCount() {
		return commentedCount;
	}

	public void setCommentedCount(int commentedCount) {
		this.commentedCount = commentedCount;
	}

	public int getCommentCount() {
		return commentCount;
	}

	public void setCommentCount(int commentCount) {
		this.commentCount = commentCount;
	}

	public int getTransferredCount() {
		return transferredCount;
	}

	public void setTransferredCount(int transferredCount) {
		this.transferredCount = transferredCount;
	}

	public int getTransferCount() {
		return transferCount;
	}

	public void setTransferCount(int transferCount) {
		this.transferCount = transferCount;
	}

	public int getClassId() {
		return classId;
	}

	public void setClassId(int classId) {
		this.classId = classId;
	}

	public int getTeachingPointId() {
		return teachingPointId;
	}

	public void setTeachingPointId(int teachingPointId) {
		this.teachingPointId = teachingPointId;
	}

}
