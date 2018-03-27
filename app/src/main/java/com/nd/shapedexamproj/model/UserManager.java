package com.nd.shapedexamproj.model;

import java.util.List;
import java.util.Vector;

/**
 * 
 * 实现的主要功能。
 * 
 * @version 1.0.0
 * @author Abay Zhuang
 *         Create at 2014-5-30 修改者，修改日期，修改内容。
 */
public class UserManager {

	/**
	 * 单例模式
	 */
	private static UserManager mInstance;

	public static UserManager getInstance() {
		if (mInstance == null)
			mInstance = new UserManager();
		return mInstance;
	}

	private UserManager() {

	}

	// 线程安全
	private List<User> mUserLst = new Vector<User>();

	public void add(User user) {
		if (!mUserLst.contains(user))
			mUserLst.add(user);
	}

	public void addAll(List<User> users) {
		if (users == null || users.size() == 0)
			return;
		for (User user : users) {
			add(user);
		}
	}

	public void remove(User user) {
		if (mUserLst.contains(user))
			mUserLst.remove(user);
	}

	public void clear (){
		mUserLst.clear();
	}
	
}
