package com.nd.shapedexamproj.model;

import com.nd.shapedexamproj.App;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * 题目对象
 * @author zll
 * create in 2014-3-27
 */
public class Question {
	
	public String q_id ;
	/**
	 * 用户id
	 */
	public String user_id;
	/**
	 * 作业id
	 */
	public String homework_id ;
	/**
	 * 套题子项的父id
	 */
	public String parent_id;
	
	/**
	 * 题型，	'0'=>'单项选择题',
			'1'=>'多项选择题',
			'2'=>'判断题',
			'4'=>'填空题',
			'5'=>'简答题',
			'g'=>'套题'
	 */
	public String q_type ;
	/**
	 * 题目
	 */
	public String q_title ;
	/**
	 * 选项列表
	 */
	public List<QuestionChoice> choiceList = new LinkedList<QuestionChoice>();//TODO 暂时没用
	public List<String> choice_list = new LinkedList<String>();
	/*public Map<String,String> choiceMap = new HashMap<String,String>();*/
	/**
	 * 字符串型的选项列表
	 */
	public String choices = "";
	/**
	 * 答案
	 */
	public String q_answer ;
	/**
	 * 用户的回答
	 */
	public String q_reply ="";
	/**
	 * 分值
	 */
	public String q_value ;
	/**
	 * 是否已回答过。1：已回答，0：未回答
	 */
	public int is_replied ;
	public Question(String q_id, int is_replied) {
		super();
		this.q_id = q_id;
		this.is_replied = is_replied;
	}
	
	@Override
	public boolean equals(Object o) {
		if (!(o instanceof Question)) {
			return false;
		}

		Question question = (Question) o;
		if (this.q_id.equals(question.q_id))
			return true;
		else
			return false;

	}
	
	public Question(){}
	
	/**
	 * 解析题目列表
	 * @param result
	 * @return
	 */
	public static List<Question> parseQuestionList(String result,String homework_id){
		List<Question> list = new LinkedList<Question>();
		try {
			JSONObject jobj = new JSONObject(result);
			int flag = jobj.getInt("flag");
			if (flag != 1) {
				App.dealWithFlag(flag);
				return null;
			}
			JSONArray listArr = jobj.getJSONObject("res").getJSONObject("data").getJSONArray("list");
			for (int i = 0;i < listArr.length();i ++) {
				JSONObject listObj = listArr.getJSONObject(i);
				/*String q_type = listObj.getString("work_type");//TODO 暂时先屏蔽套题
				if (q_type.equals("g")) {
					continue ;
				}*/
				Question question = getQuestionFromJsonobject (listObj, homework_id, null);
				if (question != null ) {
					if (!question.q_type.equals("g")) {  //套题不作为单独的题目插入数据库
						list.add(question);
					}
				}
				
				if (!listObj.isNull("subs")) {
					String parent_id = listObj.getString("question_id");
					JSONArray subArr = listObj.getJSONArray("subs");
					for (int j = 0;j <subArr.length();j ++) {
						JSONObject subObj = subArr.getJSONObject(j);
						Question subQuestion = getQuestionFromJsonobject (subObj, homework_id, parent_id);
						if (subQuestion != null) {
							list.add(subQuestion);
						}
						
					}
				}
				
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return list;
	}
	
	private static Question getQuestionFromJsonobject (JSONObject listObj, String homework_id, String parent_id) {
		
		Question question = null;
		try {
			question = new Question();
			question.q_type = listObj.getString("work_type");
			question.q_id = listObj.getString("question_id");
			question.q_title = listObj.getString("work_name");
			question.q_value = listObj.getString("sub_score");
			question.homework_id = homework_id;
			if (parent_id != null) {
				question.parent_id = parent_id;
			}
			question.user_id = App.getUserId();
			if (!listObj.isNull("choice")) {
				JSONArray choiceArr = listObj.getJSONArray("choice");
				for (int j = 0;j < choiceArr.length();j ++) {
					JSONObject choiceObj = choiceArr.getJSONObject(j);	//TODO 接口写法可以再优化
					Iterator<String> itr = choiceObj.keys();
					
					while (itr.hasNext()) {
						QuestionChoice choice = new QuestionChoice();
						String key = itr.next();
						choice.number = key;
						choice.title = choiceObj.getString(key);
						
						question.choiceList.add(choice);
						question.choice_list.add(key + "." + choiceObj.getString(key));
					}
				}
				
			} else {
				if(question.q_type.equals("2")){		//手动插入判断题选项
					question.choice_list.add(0,"对");
					question.choice_list.add(1, "错");
				}
			}
			
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		return question;
	}
	
}
