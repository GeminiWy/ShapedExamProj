package com.nd.shapedexamproj.model.course;

import com.google.gson.annotations.SerializedName;

/**
 * 
 * @ClassName: CourseQuestion
 * @Title:
 * @Description:课程答疑实体
 * @Author:XueWenJian
 * @Since:2014年6月3日19:35:08
 * @Version:1.0
 */
public class CourseQuestion {

	@SerializedName("ask_id")
	private String askId;
	
	@SerializedName("ask_time")
	private String askTime;

	private String title;

	private String question;
	
	@SerializedName("join_course_id")
	private String courseId;
	
	private String answer;
	
	@SerializedName("an_tech")
	private String answerUserId;
	
	@SerializedName("an_name")
	private String answerName;
	
	@SerializedName("an_avatar")
	private String answerAvatar;
	
	@SerializedName("an_time")
	private String answerTime;
	
	@SerializedName("add_name")
	private String askerName;
	
	@SerializedName("add_avatar")
	private String askerAvatar;
	
	@SerializedName("add_stu")
	private String askerStu; //提问用户ID

	public String getAskId() {
		return askId;
	}

	public void setAskId(String askId) {
		this.askId = askId;
	}


	public String getAskTime() {
		return askTime;
	}

	public void setAskTime(String askTime) {
		this.askTime = askTime;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getQuestion() {
		return question;
	}

	public void setQuestion(String question) {
		this.question = question;
	}

	public String getCourseId() {
		return courseId;
	}

	public void setCourseId(String courseId) {
		this.courseId = courseId;
	}

	public String getAnswer() {
		return answer;
	}

	public void setAnswer(String answer) {
		this.answer = answer;
	}

	public String getAnswerUserId() {
		return answerUserId;
	}

	public void setAnswerUserId(String answerUserId) {
		this.answerUserId = answerUserId;
	}

	public String getAnswerTime() {
		return answerTime;
	}

	public void setAnswerTime(String answerTime) {
		this.answerTime = answerTime;
	}

	public String getAskerName() {
		return askerName;
	}

	public void setAskerName(String askerName) {
		this.askerName = askerName;
	}

	public String getAskerAvatar() {
		return askerAvatar;
	}

	public void setAskerAvatar(String askerAvatar) {
		this.askerAvatar = askerAvatar;
	}

	public String getAnswerName() {
		return answerName;
	}

	public void setAnswerName(String answerName) {
		this.answerName = answerName;
	}

	public String getAnswerAvatar() {
		return answerAvatar;
	}

	public void setAnswerAvatar(String answerAvatar) {
		this.answerAvatar = answerAvatar;
	}

	public String getAskerStu() {
		return askerStu;
	}

	public void setAskerStu(String askerStu) {
		this.askerStu = askerStu;
	} 
}
