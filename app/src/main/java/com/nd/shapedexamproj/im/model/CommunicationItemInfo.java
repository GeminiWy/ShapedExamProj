package com.nd.shapedexamproj.im.model;

import com.nd.shapedexamproj.App;
import com.nd.shapedexamproj.im.manager.plugin.GB2Alpha;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.LinkedList;
import java.util.List;

/**
 * 通讯录列表项
 * @author zll
 *
 */
public class CommunicationItemInfo {
    /**
     * 如果是粉丝列表，表示他人；如果是关注列表，表示自己
     */
	public String userid = "";
	/**
	 * 如果是粉丝列表，表示自己；如果是关注列表，表示他人
	 */
	public String followId = "";
	public String name = "";
	public String content = "";
	public String userPhotoUrl = "";
	public String sortKey = "";
	public int isFollowing = 0; //是否已加关注。0：未关注，1：已关注,2:互相关注
	
	public String getUserid() {
		return userid;
	}
	public String getName() {
		return name;
	}
	public String getContent() {
		return content;
	}
	public String getUserPhotoUrl() {
		return userPhotoUrl;
	}
	public String getSortKey() {
		return sortKey;
	}
	public void setUserid(String userid) {
		this.userid = userid;
	}
	public void setName(String name) {
		this.name = name;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public void setUserPhotoUrl(String userPhotoUrl) {
		this.userPhotoUrl = userPhotoUrl;
	}
	public void setSortKey(String sortKey) {
		this.sortKey = sortKey;
	}
	
	@Override
	public boolean equals(Object o) {
		if (!(o instanceof CommunicationItemInfo)) {
			return false ;
		}
		CommunicationItemInfo info = (CommunicationItemInfo) o;
		if (info.userid.equals(this.userid)) {
			return true ;
		} else {
			return false ;
		}
	}
	
	//关注或粉丝列表
	public static List<CommunicationItemInfo> jsonParsing(String result){
	    
		List<CommunicationItemInfo> list = new LinkedList<CommunicationItemInfo>();
		try {
			JSONObject obj = new JSONObject(result);
			int flag = obj.getInt("flag");
			if(flag == 1 && !obj.isNull("data")){
				JSONArray dataArr = obj.getJSONArray("data");
				GB2Alpha alphaUtil = new GB2Alpha();
				for (int i = 0;i < dataArr.length();i ++) {
					CommunicationItemInfo user = new CommunicationItemInfo();
					JSONObject dataObj =  dataArr.getJSONObject(i);
					user.setUserid(dataObj.getString("userId"));
                    //判定是否是自己记录，如果是，剔除
                    if(dataObj.getString("followId").equals(dataObj.getString("userId")) && user.getUserid().equals(App.getUserId())){
                        continue;
                    }
					user.name = dataObj.getString("followUserName");
					user.followId = dataObj.getString("followId");
					user.userPhotoUrl = dataObj.getString("followUserIcon");
					if (!dataObj.isNull("explanation")) {//个人说明
					    user.content = dataObj.getString("explanation");
					}
					user.sortKey = alphaUtil.String2Alpha(user.name);
					list.add(user);
				}
				
			}
			
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		return list;
	}
	
}
