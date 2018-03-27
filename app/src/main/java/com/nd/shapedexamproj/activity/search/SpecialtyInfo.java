package com.nd.shapedexamproj.activity.search;

/**
 * @Description: 专业搜索三个资源类
 * @author xiezz
 * @date 2014-06-09 
 * */
public class SpecialtyInfo { 
	private String proId; //专业ID
	private String proName; //专业名称
	private int years; //几年制 
	private String cash;// 用户头像  
	private String termId; // 年级专业ID
	 
	public String getProId() {
		return proId;
	}

	public void setProId(String proId) {
		this.proId = proId;
	}

	public String getProName() {
		return proName;
	}

	public void setProName(String proName) {
		this.proName = proName;
	}

	public int getYears() {
		return years;
	}

	public void setYears(int years) {
		this.years = years;
	}

	public String getCash() {
		return cash;
	}

	public void setCash(String cash) {
		this.cash = cash;
	}

	public String getTermId() {
		return termId;
	}

	public void setTermId(String termId) {
		this.termId = termId;
	} 
}
