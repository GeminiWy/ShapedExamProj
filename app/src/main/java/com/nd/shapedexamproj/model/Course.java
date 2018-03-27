package com.nd.shapedexamproj.model;

import com.google.gson.annotations.SerializedName;

import java.util.HashMap;
import java.util.Map;

/**
 * 课程信息
 * 
 * @author zll create in 2014-2-26
 */
public class Course {
	public String course_id = "0";
	@SerializedName("name")
	public String course_name = "";
	/**
	 * 学分
	 */
	public String credit = "";
	/**
	 * 课时
	 */
	public String lessons = "";
	/**
	 * 在修学员数
	 */
	public String users = "";
	/**
	 * 责任老师
	 */
	public String coacher = "";
	/**
	 * 责任老师id
	 */
	public String coacherId = "";
	
	public Map<String, String> coacherMap = new HashMap<String, String> (); //老师id和名称
	/**
	 * 课程讲师
	 */
	public String lecturer = "";
	/**
	 * 辅导老师
	 */
	public String counselor = "";
	/**
	 * 辅导老师id
	 */
	public String counselorId = "" ;
	public Map<String, String> counselorMap = new HashMap<String, String> ();//老师id和名称
	/**
	 * 课程介绍
	 */
	public String introduction = "";
	
	/**
	 * 得分
	 */
	public float score ;
	/**
	 * 是不是免修。1：是；0：不是
	 */
	public int is_exemption;
	
	public Course() {
	}

	public Course(String course_id, String course_name) {
		super();
		this.course_id = course_id;
		this.course_name = course_name;
	}
	
	@Override
	public boolean equals(Object o) {
		if(!(o instanceof Course))  return false;
		
		Course course = (Course) o;
		if(course.course_id.equals(this.course_id)){
			return true ;
		} else {
			return false ;
		}
	}
	
}
