package com.nd.shapedexamproj.model.my;

import com.google.gson.annotations.SerializedName;
import com.nd.shapedexamproj.App;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


/**
 * 我的提问
 * @author zll
 * create in 2014-4-22
 */
public class MyQuestion implements Serializable{

	/**
	 * 提问的状态(0=未回复，1=已回复)
	 */
	public static final int QUESTION_STATE_EMPTY = 0, QUESTION_STATE_ANSWERED = 1;
	/**
	 * 是否被忽略(0否，1是)
	 */
	public static final int QUESTION_IGNORE_NO = 0, QUESTION_IGNORE_YES = 1;
	/**
	 * 
	 */
	private static final long serialVersionUID = 1322363433289941709L;

	public String ask_id = ""; 
	
	public String course_id;
	
	public String courseName;
	/**
	 * 课件章节ID
	 */
	public String chapter_id;
	
	private String chapterName;
	/**
	 * 提问的标题
	 */
	public String title;
	/**
	 * 提问的问题
	 */
	public String question;
	/**
	 * 提问的时间
	 */
	public String add_time;
	/**
	 * 提问的状态(0=未回复，1=已回复)
	 */
	public int answer_status;
	/**
	 * 是否被忽略(0否，1是)
	 */
	public int is_ignore;
	/**
	 * 提问的答案
	 */
	public String answer;
	/**
	 * 回答时间
	 */
	public String an_time;
	
	@SerializedName("an_name")
	private String answerName;
	
	@SerializedName("an_avatar")
	private String answerAvatar;
	
    public String getAsk_id() {
        return ask_id;
    }

    
    public void setAsk_id(String ask_id) {
        this.ask_id = ask_id;
    }

    public String getCourse_id() {
		return course_id;
	}

	public void setCourse_id(String course_id) {
		this.course_id = course_id;
	}

	public String getChapter_id() {
		return chapter_id;
	}

	public void setChapter_id(String chapter_id) {
		this.chapter_id = chapter_id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getQuestion() {
		return question;
	}

	public void setQuestion(String question) {
		this.question = question;
	}

	public String getAdd_time() {
		return add_time;
	}

	public void setAdd_time(String add_time) {
		this.add_time = add_time;
	}



	public int getAnswer_status() {
		return answer_status;
	}

	public void setAnswer_status(int answer_status) {
		this.answer_status = answer_status;
	}

	public int getIs_ignore() {
		return is_ignore;
	}

	public void setIs_ignore(int is_ignore) {
		this.is_ignore = is_ignore;
	}

	public String getAnswer() {
		return answer;
	}

	public void setAnswer(String answer) {
		this.answer = answer;
	}

	public String getAn_time() {
		return an_time;
	}

	public void setAn_time(String an_time) {
		this.an_time = an_time;
	}
	
	@Override
	public boolean equals(Object o) {
		if(!(o instanceof MyQuestion)){
			return false;
		}
		MyQuestion myQuestion = (MyQuestion) o;
		if(myQuestion.ask_id.equals(this.ask_id)){	
			return true ;
		} else {
			return false ;
		}
	}
	
	
	
	public String getCourseName() {
		return courseName;
	}

	public void setCourseName(String courseName) {
		this.courseName = courseName;
	}

	public String getChapterName() {
		return chapterName;
	}

	public void setChapterName(String chapterName) {
		this.chapterName = chapterName;
	}

	
	
	public String getAnswerName() {
		return answerName;
	}

	public void setAnswerName(String answerName) {
		this.answerName = answerName;
	}

	public String getAnswerAvatar() {
		return answerAvatar;
	}

	public void setAnswerAvatar(String answerAvatar) {
		this.answerAvatar = answerAvatar;
	}

	/**
	 * 加载数据
	 * @param result
	 */
	public static List<MyQuestion> jsonParsing(String result){
		List<MyQuestion> list = new ArrayList<MyQuestion>();
		try {
			JSONObject jobj = new JSONObject(result);
			int flag = jobj.getInt("flag");
			if(flag != 1){
				App.dealWithFlag(flag);
				return null;
			}
			int total = jobj.getJSONObject("res").getJSONObject("data").getInt("total");
			if(total == 0){		//没有提问列表
				
				return list;
			}
			JSONArray listArray = jobj.getJSONObject("res").getJSONObject("data").getJSONArray("list");
			for (int i = 0; i < listArray.length(); i++) {
				
				MyQuestion question = new MyQuestion();
				JSONObject listObject = listArray.getJSONObject(i);
				question.ask_id = listObject.getString("ask_id");
				question.course_id = listObject.getString("course_id");
				question.chapter_id = listObject.getString("chapter_id");
				question.title = listObject.getString("title");
				question.question = listObject.getString("question");
				question.add_time = listObject.getString("add_time");
				question.answer_status = listObject.getInt("answer_status");
				question.is_ignore = listObject.getInt("is_ignore");
				question.answer = listObject.getString("answer");
				question.an_time = listObject.getString("an_time");
				question.courseName = listObject.getString("course_name");
				question.chapterName = listObject.getString("chapter_name");
				question.answerName = listObject.getString("an_name");
				question.answerAvatar = listObject.getString("an_avatar");
				
				list.add(question);
			}
			
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		return list;
	}
	
}
