package com.nd.shapedexamproj.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.nd.shapedexamproj.App;

/**
 * 创建数据库
 * @author zll
 * create in 2014-3-19
 */
public class OpenUniversityOpenHelper extends SQLiteOpenHelper{

	private Object dbLock = new Object();
	private static int mCurrentVersion = 3;
	
	public OpenUniversityOpenHelper(Context context) {
		super(context, "open_university_" + App.getUserId() + ".db", null, mCurrentVersion);
	}
	
	public Object getDbLock() {
		return dbLock;
	}
	
	@Override
	public void onCreate(SQLiteDatabase db) {
		//课件信息表
		db.execSQL("CREATE TABLE video_info (video_id VARCHAR2(50), course_id VARCHAR2(50), user_id VARCHAR2(50), " +
				"video_name VARCHAR2(100), video_path VARCHAR2(100), video_url VARCHAR2(100),course_name VARCHAR2(100));");
		//课程分类表
		db.execSQL("CREATE TABLE coursecategory(cc_id INTEGER, coursecategoryid, INTEGER, coursecategoryname TEXT);");
		//专业列表
		db.execSQL("CREATE TABLE major(c_id INTEGER, majorid, INTEGER, majorname TEXT);");

		db.execSQL("CREATE TABLE downloadinfo(url TEXT, coursecateid INTEGER, status INTEGER, rate INTEGER, title TEXT," +
				" total INTEGER, coursecatename TEXT, pause INTEGER);");
		
		db.execSQL("CREATE TABLE videodownload(url TEXT, coursecateid INTEGER, coursecatename TEXT, videoname TEXT, courseid TEXT, videoid TEXT);");
        //关注用户表
        db.execSQL("CREATE TABLE usersrelationship(userid TEXT, username TEXT, relatedid TEXT, relatedusername TEXT, type INTEGER, desc TEXT,note TEXT);");
        db.execSQL("CREATE TABLE newfriends(fansId TEXT, type INTEGER, headImg TEXT)");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		if (oldVersion == 1 && newVersion == 2) {
		    //关注用户表
	        db.execSQL("CREATE TABLE usersrelationship(userid TEXT, username TEXT, relatedid TEXT, relatedusername TEXT, type INTEGER, desc TEXT,note TEXT);");
	        db.execSQL("CREATE TABLE newfriends(fansId TEXT, type INTEGER, headImg TEXT)");
		}

	}

}
