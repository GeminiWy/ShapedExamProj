package com.nd.shapedexamproj.model;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


public class ErrorQuestion {
	public String courseId;
	public String courseName;
	public int error;
	
	@Override
	public boolean equals(Object o) {
		if (!(o instanceof ErrorQuestion)){
			return false ;
		}
		ErrorQuestion eQuestion = (ErrorQuestion) o;
		if (eQuestion.courseId.equals(this.courseId)){
			return true ;
		} else {
			return false ;
		}
	}
	
	public static List<ErrorQuestion> JSONPasing(String result) {
		List<ErrorQuestion> errorQuestions = new ArrayList<ErrorQuestion>();
		try {
			JSONObject jsonObject = new JSONObject(result);
			
			JSONObject dataObj = jsonObject.getJSONObject("res").getJSONObject("data");
			if(!dataObj.isNull("list")){
			
				JSONArray list = dataObj.getJSONArray("list");
				for (int i = 0; i < list.length(); i++) {
					ErrorQuestion question = new ErrorQuestion();
					question.courseId = list.getJSONObject(i).getString("course_id");
					question.courseName = list.getJSONObject(i).getString("course_name");
					question.error = list.getJSONObject(i).getInt("err_num");
					errorQuestions.add(question);
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return errorQuestions;
	}
}
