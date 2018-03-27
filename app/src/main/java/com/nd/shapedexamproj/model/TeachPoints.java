package com.nd.shapedexamproj.model;

import com.google.gson.Gson;

import java.util.List;


/**
 * 教学点List
 * @version 1.0.0
 * @author Abay Zhuang
 *         Create at 2014-5-27 修改者，修改日期，修改内容。
 */
public class TeachPoints {
	private List<TeachPoint> list;
	private int total;

	public List<TeachPoint> getList() {
		return list;
	}

	public void setList(List<TeachPoint> list) {
		this.list = list;
	}

	public int getTotal() {
		return total;
	}

	public void setTotal(int total) {
		this.total = total;
	}

	public class Result {
		public int flag;
		public Res res;
	}

	public class Res {
		public int code;
		public TeachPoints data;
	}

	/**
	 * 
	 * Json 转 TeacherCourse 对象
	 * 
	 * @param json
	 * @return Result
	 */
	public static Result getObject(String json) {
		Gson gson = new Gson();
		Result tc = gson.fromJson(json, Result.class);
		return tc;
	}



}
