package com.nd.shapedexamproj.model;

import com.google.gson.Gson;
import com.nd.shapedexamproj.util.StringUtils;

import java.util.List;

/**
 * 用户基本信息
 * @version 1.0.0 
 * @author Abay Zhuang <br/>
 *		   Create at 2014-5-30
 * 修改者，修改日期，修改内容。
 */
public class User {
	private int age;
	private String birthday;
	private String city;
	private String className;
	private String classid;
	private String company;
	private String constellation;
	private String explanation;
	private String fanscount;
	private String flowercount;
	private String followcount;
	private String hobby;
	private String industryid;
	private String juniormiddleschool;
	private String phone;
	private String phonevisible;
	private String photo;
	private String primaryschool;
	private String profession;
	private String seniormiddleschool;
	private String sex;
	private String specialtyid;
	private String teachingpointid;
	private String timelinecount;
	private String university;
	private String usercardid;
	private String userid;
	private String username;
	private String usertype;
	
	
	public int getAge() {
		return age;
	}


	public void setAge(int age) {
		this.age = age;
	}


	public String getBirthday() {
		return birthday;
	}


	public void setBirthday(String birthday) {
		this.birthday = birthday;
	}


	public String getCity() {
		return city;
	}


	public void setCity(String city) {
		this.city = city;
	}


	public String getClassName() {
		return className;
	}


	public void setClassName(String className) {
		this.className = className;
	}


	public String getClassid() {
		return classid;
	}


	public void setClassid(String classid) {
		this.classid = classid;
	}


	public String getCompany() {
		return company;
	}


	public void setCompany(String company) {
		this.company = company;
	}


	public String getConstellation() {
		return constellation;
	}


	public void setConstellation(String constellation) {
		this.constellation = constellation;
	}


	public String getExplanation() {
		return explanation;
	}


	public void setExplanation(String explanation) {
		this.explanation = explanation;
	}


	public String getFanscount() {
		return fanscount;
	}


	public void setFanscount(String fanscount) {
		this.fanscount = fanscount;
	}


	public String getFlowercount() {
		return flowercount;
	}


	public void setFlowercount(String flowercount) {
		this.flowercount = flowercount;
	}


	public String getFollowcount() {
		return followcount;
	}


	public void setFollowcount(String followcount) {
		this.followcount = followcount;
	}


	public String getHobby() {
		return hobby;
	}


	public void setHobby(String hobby) {
		this.hobby = hobby;
	}


	public String getIndustryid() {
		return industryid;
	}


	public void setIndustryid(String industryid) {
		this.industryid = industryid;
	}


	public String getJuniormiddleschool() {
		return juniormiddleschool;
	}


	public void setJuniormiddleschool(String juniormiddleschool) {
		this.juniormiddleschool = juniormiddleschool;
	}


	public String getPhone() {
		return phone;
	}


	public void setPhone(String phone) {
		this.phone = phone;
	}


	public String getPhonevisible() {
		return phonevisible;
	}


	public void setPhonevisible(String phonevisible) {
		this.phonevisible = phonevisible;
	}


	public String getPhoto() {
		return photo;
	}


	public void setPhoto(String photo) {
		this.photo = photo;
	}


	public String getPrimaryschool() {
		return primaryschool;
	}


	public void setPrimaryschool(String primaryschool) {
		this.primaryschool = primaryschool;
	}


	public String getProfession() {
		return profession;
	}


	public void setProfession(String profession) {
		this.profession = profession;
	}


	public String getSeniormiddleschool() {
		return seniormiddleschool;
	}


	public void setSeniormiddleschool(String seniormiddleschool) {
		this.seniormiddleschool = seniormiddleschool;
	}


	public String getSex() {
		return sex;
	}


	public void setSex(String sex) {
		this.sex = sex;
	}


	public String getSpecialtyid() {
		return specialtyid;
	}


	public void setSpecialtyid(String specialtyid) {
		this.specialtyid = specialtyid;
	}


	public String getTeachingpointid() {
		return teachingpointid;
	}


	public void setTeachingpointid(String teachingpointid) {
		this.teachingpointid = teachingpointid;
	}


	public String getTimelinecount() {
		return timelinecount;
	}


	public void setTimelinecount(String timelinecount) {
		this.timelinecount = timelinecount;
	}


	public String getUniversity() {
		return university;
	}


	public void setUniversity(String university) {
		this.university = university;
	}


	public String getUsercardid() {
		return usercardid;
	}


	public void setUsercardid(String usercardid) {
		this.usercardid = usercardid;
	}


	public String getUserid() {
		return userid;
	}


	public void setUserid(String userid) {
		this.userid = userid;
	}


	public String getUsername() {
		return username;
	}


	public void setUsername(String username) {
		this.username = username;
	}


	public String getUsertype() {
		return usertype;
	}


	public void setUsertype(String usertype) {
		this.usertype = usertype;
	}
	
	@Override
	public boolean equals(Object o) {
		if (o == null)
			return false;
		if (o instanceof User){
			User other = (User)o;
			if (StringUtils.isEmpty(other.getUserid()))
				return false;
			if (StringUtils.isEmpty(this.getUserid()))
				return false;
				
			return this.getUserid().equals(other.getUserid());
		}
		return false;
	}


	public class Result {
		public int flag;
		public User user;
	}
	

	
	
	/**
	 * 
	 * Json 转 Result 对象
	 * 
	 * @param json
	 * @return Result
	 */
	public static Result getObject(String json) {
		Gson gson = new Gson();
		Result tc = gson.fromJson(json, Result.class);
		return tc;
	}
	
	
	public class ResultList{
		public int flag;
		public int total;
		public List<User> list;
	}
	
	
	/**
	 * 
	 * Json 转 ResultList 对象
	 * 
	 * @param json
	 * @return ResultList
	 */
	public static ResultList getUserList(String json) {
		Gson gson = new Gson();
		ResultList tc = gson.fromJson(json, ResultList.class);
		return tc;
	}
	
	
}
