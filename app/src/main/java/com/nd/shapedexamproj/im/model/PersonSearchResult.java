/**  
 * Project Name:OpenUniversity  
 * File Name:PersonSearchResultEntity.java  
 * Package Name:com.tming.openuniversity.model.im  
 * Date:2014-6-10上午12:55:15  
 * Copyright (c) 2014, XueWenJian All Rights Reserved.  
 *  
*/  

  
package com.nd.shapedexamproj.im.model;

import com.google.gson.annotations.SerializedName;

/**  
 * ClassName:PersonSearchResultEntity <br/>  
 * description: im-搜索结果实体 <br/>  
 * Date:     2014-6-10 上午12:55:15 <br/>  
 * @author   XueWenJian  
 * @version    
 * @since    JDK 1.6  
 * @see        
 */
public class PersonSearchResult {

	@SerializedName("user_id")
	private String userId;
	
	@SerializedName("user_name")
	private String userName;
	
	@SerializedName("avatar")
	private String imgUrl;
	
	/**
	 * 是否已添加，0：未添加，1：已添加
	 */
	private int isAdded; 
	
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

    public int getIsAdded() {
        return isAdded;
    }

    public void setIsAdded(int isAdded) {
        this.isAdded = isAdded;
    }
	
	
}
  
