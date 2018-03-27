package com.nd.shapedexamproj.model;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

/**
 * 作业项
 * @author zll
 * create in 2014-2-25
 */
public class Homework implements  Comparable<Homework>{
	/**
	 * 作业id
	 */
    @SerializedName("exam_id")
	public String homework_id = "0";
	/**
	 * 作业名称
	 */
    @SerializedName("exam_name")
	public String homework_name = "";
	/**
	 * 作业总分
	 */
	public int homework_score ;
	/**
	 * 用户得分,总分(如果为-1，则是待批改)
	 */
	@SerializedName("score")
	public int user_score;
	/**
	 * 开始时间
	 */
	@SerializedName("time_begin")
	public long time_begin ;
	
	/**
	 * 作业截止日期
	 */
	@SerializedName("time_end")
	public long finish_time ;
	/**
	 * 答题耗时(秒)
	 */
	@SerializedName("cost_time")
	public int cost_time;
	
	/**
	 * 交卷时间
	 */
	@SerializedName("time")
	public long submit_time;
	/**
	 * 课程名
	 */
	@SerializedName("course_name")
	public String course_name;
	/**
	 * 课程id
	 */
	@SerializedName("course_id")
	public String course_id = "0";
	/**
	 * 是否过期（按课程分类的作业列表使用），0：未过期，1，已过期
	 */
	@SerializedName("is_expired")
	public int is_expired ;
	/**
	 * 用于显示待提交提示<br/>
	 * 0是未提交，1是已提交。
	 */
	@SerializedName("work_state")
	public int work_state = 0;
	/**
	 * 1为已过期，0为未过期
	 */
	public int isOutofDate = 0;
	/**
	 * 是否已做过，0：未做，1：已做
	 */
	public int has_done = 0;
	/**
	 * 是否可以被点击（是否可以进入做题），1：可以做，0：不能做
	 */
	public int isEnabled = 1 ;
	
	/**
	 * 总的题目数
	 */
	public int total_num ;  
	/**
	 * 未做题目
	 */
	public int undo_num ;
	/**
	 * 已答次数
	 */
	@SerializedName("do_times")
	public int answer_num ;
	/**
	 * 答题的限制次数,-1为不限
	 */
	@SerializedName("max_times")
	public int answer_total ;
	/**
	 * 错题数
	 */
	public int error_num ;
	/**
	 * 客观题得分（-1：没有客观题）
	 */
	public double obj_score;
	/**
	 * 主观题得分（-1：未批改）
	 */
	public double sbj_score;
	/**
	 * 是否已批改，1表示已批改，0未批改
	 */
	public int is_marked = 0;
	/**
	 * 题型列表
	 */
//	public Map <String,QuestionHolder> question_style = new HashMap<String,QuestionHolder>();
	
	public List <QuestionHolder> question_type = new ArrayList<QuestionHolder>();

	@Override
	public int compareTo(Homework another) {//从小到大
		return -(int)(another.finish_time - finish_time) ;
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof Homework)) {
			return false;
		}

		Homework homework = (Homework) o;
		if (this.homework_id.equals(homework.homework_id))
			return true;
		else
			return false;

	}
	
}
