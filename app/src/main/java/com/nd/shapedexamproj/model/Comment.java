package com.nd.shapedexamproj.model;


import com.nd.shapedexamproj.util.StringUtils;

import java.util.HashMap;
import java.util.Map;


/**
 * 
 *@ClassName: Comment
 *@Title:
 *@Description:提交评论数据
 *@Author:Administrator
 *@Since:2014-5-7下午9:52:09 
 *@Version:1.0
 */
public class Comment {
	private String timelineid ;
	private String userid;
	private String content;
	private String type;
	private String classid;
	private String teachingpointid;
	private String originaltimelineid;
	
	
	public String getTimelineid() {
		return timelineid;
	}
	public void setTimelineid(String timelineid) {
		this.timelineid = timelineid;
	}
	public String getUserid() {
		return userid;
	}
	public void setUserid(String userid) {
		this.userid = userid;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getClassid() {
		return classid;
	}
	public void setClassid(String classid) {
		this.classid = classid;
	}
	public String getTeachingpointid() {
		return teachingpointid;
	}
	public void setTeachingpointid(String teachingpointid) {
		this.teachingpointid = teachingpointid;
	}

	/**
	 * @return the originaltimelineid
	 */
	public String getOriginaltimelineid() {
		return originaltimelineid;
	}
	/**
	 * @param originaltimelineid the originaltimelineid to set
	 */
	public void setOriginaltimelineid(String originaltimelineid) {
		this.originaltimelineid = originaltimelineid;
	}
	
	
	public Map<String, Object> getMapData(){
		Map<String,Object> map = new HashMap<String,Object>();
		map.put("timelineid", timelineid);
		map.put("userid", userid);
		map.put("content", content);
		map.put("type", type);
		if (!StringUtils.isEmpty(classid))
			map.put("classid", classid);
		if (!StringUtils.isEmpty(teachingpointid))
			map.put("teachingpointid", teachingpointid);
		if(!StringUtils.isEmpty(originaltimelineid)){
			map.put("originaltimelineid", originaltimelineid);
		}
		return map;
	}
	
}
