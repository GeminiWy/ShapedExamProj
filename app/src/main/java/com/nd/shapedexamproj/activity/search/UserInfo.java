package com.nd.shapedexamproj.activity.search;


import com.nd.shapedexamproj.util.StringUtils;

/**
 * @Description: 用户搜索三个资源类
 * @author xiezz
 * @date 2014-05-29 
 * */
public class UserInfo { 
	private String userId; //用户ID 
	private String userName;// 用户名  
	private String avatar;// 用户头像  
	private int type; //身份，老师或是学生
	private String cash; //每学分学费
	private String sex;//: "0",
	private String age; // "32"
	 
	 
	public String getAvatar() {
		return avatar;
	}

	public void setAvatar(String avatar) {
		this.avatar = avatar;
	}
 

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public String getCash() {
		return cash;
	}

	public void setCash(String cash) {
		this.cash = cash;
	}

	public String getUserId() {
		return userId;
	}

    /**
     * <p>获取用户detail信息，根据基础信息封装</p>
     * @return
     */
    public String getUserDetailMsg(){
        String msg="";
        msg+=(type==2)?"学生 ":"教师 ";
        msg+= StringUtils.isEmpty(sex)?"":"0".equals(sex)?"男 ":"女 ";
        msg+=StringUtils.isEmpty(age)?"":"年龄: "+age;
        return msg;
    }

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getSex() {
		return sex;
	}

	public void setSex(String sex) {
		this.sex = sex;
	}

	public String getAge() {
		return age;
	}

	public void setAge(String age) {
		this.age = age;
	} 
}
