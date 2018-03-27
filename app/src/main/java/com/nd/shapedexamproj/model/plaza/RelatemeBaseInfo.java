package com.nd.shapedexamproj.model.plaza;


import com.nd.shapedexamproj.model.my.PersonalInfo;
import com.nd.shapedexamproj.model.playground.Timeline;

/**
 * 与我相关信息列表
 * 
 * @author Administrator
 * 
 */
public class RelatemeBaseInfo {
	private int userId;// 用户ID
	private int targetUserId;
	private int targetId;// 与我相关信息ID
	private int operateType;
	private Long operateTime;
	private Timeline commentTimelineInfo;// 评论对象
	private Timeline dynamicTimelineInfo;// 动态对象
	private PersonalInfo personalInfo;// 个人信息

	public int getUserId() {
		return userId;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}

	public int getTargetUserId() {
		return targetUserId;
	}

	public void setTargetUserId(int targetUserId) {
		this.targetUserId = targetUserId;
	}

	public int getTargetId() {
		return targetId;
	}

	public void setTargetId(int targetId) {
		this.targetId = targetId;
	}

	public int getOperateType() {
		return operateType;
	}

	public void setOperateType(int operateType) {
		this.operateType = operateType;
	}

	public Long getOperateTime() {
		return operateTime;
	}

	public void setOperateTime(Long operateTime) {
		this.operateTime = operateTime;
	}

    public Timeline getCommentTimelineInfo() {
        return commentTimelineInfo;
    }
    
    public void setCommentTimelineInfo(Timeline commentTimelineInfo) {
        this.commentTimelineInfo = commentTimelineInfo;
    }
    
    public Timeline getDynamicTimelineInfo() {
        return dynamicTimelineInfo;
    }

    public void setDynamicTimelineInfo(Timeline dynamicTimelineInfo) {
        this.dynamicTimelineInfo = dynamicTimelineInfo;
    }

    public PersonalInfo getPersonalInfo() {
		return personalInfo;
	}

	public void setPersonalInfo(PersonalInfo personalInfo) {
		this.personalInfo = personalInfo;
	}
	
	
	

}
