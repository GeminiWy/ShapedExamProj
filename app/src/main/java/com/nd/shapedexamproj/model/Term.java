package com.nd.shapedexamproj.model;

import com.nd.shapedexamproj.App;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.LinkedList;
import java.util.List;

/**
 * 学期
 * @author zll
 *
 */
public class Term {
	
	public static final int ACTTERM_NO =  1, ACTTERM_YES =  1;
	
	public String name ;
	public String in_year ;//年份
	public int season ;//季度
	public int is_act ;//是不是当前学期，0：不是，1：是
	public String id ;
	
	public static List<Term> ParseTermList (String result){
		List<Term> list = new LinkedList<Term>();
		int flag = 0;
		try {
			JSONObject jobj = new JSONObject(result);
		
			flag = jobj.getInt("flag");
			if(flag != 1){
				App.dealWithFlag(flag);
				return null;
			}
			int total =  jobj.getJSONObject("res").getJSONObject("data").getInt("total");
			JSONArray listArr = jobj.getJSONObject("res").getJSONObject("data").getJSONArray("list");
			for(int i = 0;i < listArr.length();i ++){
				JSONObject listObj = listArr.getJSONObject(i);
				Term term = new Term();
				term.name = listObj.getString("name");
				term.in_year = listObj.getString("in_year");
				term.season = listObj.getInt("season");
				term.is_act = listObj.getInt("is_act");
				term.id = listObj.getString("id");
				
				list.add(term);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return list;
		
	}
	
	/**
	 * 
	  * @Title: isActionTerm
	  * @Description: 是否是当前学期
	  * @param @return    
	  * @return booelan    返回类型
	  * @throws
	 */
	public boolean isActionTerm()
	{
		boolean result = false;
		if(ACTTERM_YES == is_act)
		{
			result = true;
		}
		else
		{
			result = false;
		}
		return result;
	}
	

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getIn_year() {
		return in_year;
	}

	public void setIn_year(String in_year) {
		this.in_year = in_year;
	}

	
	public int getSeason() {
		return season;
	}

	public void setSeason(int season) {
		this.season = season;
	}

	public int getIs_act() {
		return is_act;
	}

	public void setIs_act(int is_act) {
		this.is_act = is_act;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
	
	
	
	
}
