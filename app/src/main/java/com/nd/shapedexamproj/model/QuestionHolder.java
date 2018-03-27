package com.nd.shapedexamproj.model;

import java.io.Serializable;


/**
 * 作业详情的作业题型
 * @author zll
 * create in 2014-4-11
 */
public class QuestionHolder implements Serializable{
	/**
	 * 0:单项选择题，1：多项选择题，2:判断题，4：填空题，5：简答题，g:套题
	 */
	public String work_type;
	public int work_num; 
	/**
	 * 题型总分
	 */
	public int total_score = 0;  
	
}
