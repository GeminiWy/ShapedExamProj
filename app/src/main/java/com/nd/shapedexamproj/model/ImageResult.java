package com.nd.shapedexamproj.model;

import com.google.gson.Gson;

import java.util.List;
/**
 * 上传图片 回馈
 * @version 1.0.0 
 * @author Abay Zhuang <br/>
 *		   Create at 2014-6-23
 * 修改者，修改日期，修改内容。
 */
public class ImageResult {
	public int flag;
	public Res res;
	
	public class Res{
		public int code;
		public Data data;
	}
	
	public class Data{
		public List<Userpic> info;
	}
	
	public class Userpic{
		public String userpic;
	}
	
	public static ImageResult parseJson(String jsondata){
		Gson gson = new Gson();
		return gson.fromJson(jsondata, ImageResult.class);
	}
}
