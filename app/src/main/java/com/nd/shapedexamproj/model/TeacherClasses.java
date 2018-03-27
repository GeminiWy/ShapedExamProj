package com.nd.shapedexamproj.model;

import com.google.gson.Gson;

import java.util.List;

/**
 * 
 * 老师负责班级类
 * @version 1.0.0 
 * @author Abay Zhuang <br/>
 *		   Create at 2014-5-21
 */
public class TeacherClasses {
	private List<ClassInfo> list;
	private int total;

	public List<ClassInfo> getList() {
		return list;
	}

	public void setList(List<ClassInfo> list) {
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
		public TeacherClasses data;
	}
	
	/**
	 * 
	 *Json 转  TeacherCourse 对象
	 *@param json
	 *@return
	 */
	public static Result getObject(String json) {
		Gson gson = new Gson();
		Result tc = gson.fromJson(json, Result.class);
		return tc;
	}
	
	
	public class ClassInfo {
	    public String term_name;
		private String name;
		private String cls_id;
		private int students;

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}


		public int getStudents() {
			return students;
		}

		public void setStudents(int students) {
			this.students = students;
		}

		public String getCls_id() {
			return cls_id;
		}

		public void setCls_id(String cls_id) {
			this.cls_id = cls_id;
		}
	}
}

 