package com.nd.shapedexamproj.model.blacklist;

public class Blacklist {

	private int blackid;
	private int blackuserid;
	private int userid;
	private String userName;
	private String userImgAddr;
	
	
	
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	public String getUserImgAddr() {
		return userImgAddr;
	}
	public void setUserImgAddr(String userImgAddr) {
		this.userImgAddr = userImgAddr;
	}
	public int getBlackid() {
		return blackid;
	}
	public void setBlackid(int blackid) {
		this.blackid = blackid;
	}
	public int getBlackuserid() {
		return blackuserid;
	}
	public void setBlackuserid(int blackuserid) {
		this.blackuserid = blackuserid;
	}
	public int getUserid() {
		return userid;
	}
	public void setUserid(int userid) {
		this.userid = userid;
	}
	
	/*
	public Blacklist(int blackid, int blackuserid, int userid) {
		super();
		this.blackid = blackid;
		this.blackuserid = blackuserid;
		this.userid = userid;
	}
	*/
	
	
}
