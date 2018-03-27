package com.nd.shapedexamproj.model;

import com.google.gson.Gson;

import java.util.List;


/**
 * 在修的课程
 * @version 1.0.0 
 * @author Abay Zhuang <br/>
 *		   Create at 2014-6-18
 * 修改者，修改日期，修改内容。
 */
public class Coursing {
	String course_credit;//: "20.0",
	String name;//: "数学",
	String times;//: "50",
	String course_id;//: "5329484152149cf5"

	// ,
	//String course_percent;//: 0
	
	public String getCourse_credit() {
		return course_credit;
	}
	public void setCourse_credit(String course_credit) {
		this.course_credit = course_credit;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getTimes() {
		return times;
	}
	public void setTimes(String times) {
		this.times = times;
	}
	public String getCourse_id() {
		return course_id;
	}
	public void setCourse_id(String course_id) {
		this.course_id = course_id;
	}
//	public String getCourse_percent() {
//		return course_percent;
//	}
//	public void setCourse_percent(String course_percent) {
//		this.course_percent = course_percent;
//	}

	
	@Override
	public boolean equals(Object o) {
		if (o == null)
			return false;
		
		if (o == this)	
			return true;
		
		if(!(o instanceof Coursing)){
			return false ;
		}
		
		Coursing other = (Coursing) o;
		return other.course_id.equals(this.course_id);
		
	}
	/**
	 * 
	 * @version 1.0.0 
	 * @author Abay Zhuang <br/>
	 *		   Create at 2014-6-18
	 */
	public class Result{
		public int flag;
		public Res res;
	}
	
	public class Res{
		public int code;
		public Data data;
	}
	
	public class Data {
		public int total;
		public List<Coursing> list;
	}
	
	public static Result parseJson(String json){
		Gson gson = new Gson();
		Result tc = gson.fromJson(json, Result.class);
		return tc;
	}
	
}
