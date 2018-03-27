package com.nd.shapedexamproj.model;

import com.google.gson.Gson;

import java.util.List;

/**
 * 
 * 老师任课的课程
 * @version 1.0.0 
 * @author Abay Zhuang <br/>
 *		   Create at 2014-5-21
 */
public class TeacherCourse {
	private List<CourseInfo> list;
	private int total;

	public List<CourseInfo> getList() {
		return list;
	}

	public void setList(List<CourseInfo> list) {
		this.list = list;
	}

	public int getTotal() {
		return total;
	}

	public void setTotal(int total) {
		this.total = total;
	}

     
	public class Result{
		public int flag;
		public Res res;
	}
	
	public class Res{
	    public int code;
		public TeacherCourse data;
	}
	
	
	/**
	 * 
	 *Json 转  TeacherCourse 对象
	 *@param json
	 *@return Result 
	 */
	public static Result getObject(String json) {
		Gson gson = new Gson();
		Result tc = gson.fromJson(json, Result.class);
		return tc;
	}
	
	
	
	/**
	 * 
	 * 任教课程信息
	 * @version 1.0.0 
	 * @author Abay Zhuang <br/>
	 *		   Create at 2014-5-21
	 */
	public class CourseInfo {
		private String name;
		private String course_id;
		private int students;

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getCourse_id() {
			return course_id;
		}

		public void setCourse_id(String course_id) {
			this.course_id = course_id;
		}

		public int getStudents() {
			return students;
		}

		public void setStudents(int students) {
			this.students = students;
		}
	}
}

 