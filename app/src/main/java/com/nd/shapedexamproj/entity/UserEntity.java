package com.nd.shapedexamproj.entity;

public class UserEntity {

	private String userId; 
	private String userName;
	private int imgResId;
	private String imgUrl;
	
	private String pwd;
	
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	public String getImgUrl() {
		return imgUrl;
	}
	public void setImgUrl(String imgUrl) {
		this.imgUrl = imgUrl;
	}
	public int getImgResId() {
		return imgResId;
	}
	public void setImgResId(int imgResId) {
		this.imgResId = imgResId;
	}
	public String getPwd() {
		return pwd;
	}
	public void setPwd(String pwd) {
		this.pwd = pwd;
	}

	
	
	
	
}
