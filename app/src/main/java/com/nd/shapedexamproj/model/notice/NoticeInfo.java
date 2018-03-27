package com.nd.shapedexamproj.model.notice;
/**
 * @Title: NoticeInfo
 * @Description: 系统公告实体类
 * @author WuYuLong
 * @date 2014-5-7
 * @version V1.0
 */
public class NoticeInfo {
	String noticeId;
	String title;
	String time;
	String content;
	
	public String getNoticeId() {
		return noticeId;
	}
	public void setNoticeId(String noticeId) {
		this.noticeId = noticeId;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getTime() {
		return time;
	}
	public void setTime(String time) {
		this.time = time;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
}
