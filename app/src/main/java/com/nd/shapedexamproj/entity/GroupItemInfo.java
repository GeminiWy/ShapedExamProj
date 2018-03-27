/**  
 * Project Name:OpenUniversity  
 * File Name:GroupItemInfo.java  
 * Package Name:com.tming.openuniversity.entity  
 * Date:2014-6-11下午4:12:39  
 * Copyright (c) 2014, XueWenJian All Rights Reserved.  
 *  
*/  

  
package com.nd.shapedexamproj.entity;

import android.os.Parcel;
import android.os.Parcelable;

/**  
 * ClassName:GroupItemInfo <br/>  
 * description: GroupItemInfo <br/>  
 * Date:     2014-6-11 下午4:12:39 <br/>  
 * @author   XueWenJian  
 * @version    
 * @since    JDK 1.6  
 * @see        
 */
public class GroupItemInfo implements Parcelable{
	String name = "";
	String jid = "";
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getJid() {
		return jid;
	}

	public void setJid(String jid) {
		this.jid = jid;
	}

    @Override
    public int describeContents() {
        return 0;
    }
    
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof GroupItemInfo)) {
            return false;
        }
        GroupItemInfo itemInfo = (GroupItemInfo) o;
        if (itemInfo.jid.equals(this.jid) && itemInfo.name.equals(this.name)) {
            return true;
        } else {
            return false;
        }
        
    }
    
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(jid);
        dest.writeString(name);
    }
    
    private GroupItemInfo readFromParcel(Parcel source){  
        this.jid = source.readString();  
        this.name = source.readString();  
        return this;  
    }  
    
    public static final Creator<GroupItemInfo> CREATOR = new Creator<GroupItemInfo>() {

        @Override
        public GroupItemInfo createFromParcel(Parcel source) {
            
            return new GroupItemInfo().readFromParcel(source);
        }

        @Override
        public GroupItemInfo[] newArray(int size) {
            
            return new GroupItemInfo[size];
        }
        
    };
}
  
