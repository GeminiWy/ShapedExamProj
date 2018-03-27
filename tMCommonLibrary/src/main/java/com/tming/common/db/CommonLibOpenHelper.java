package com.tming.common.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
/**
 * 创建数据库
 * @author zll
 * create in 2014-3-19
 */
public class CommonLibOpenHelper extends SQLiteOpenHelper{

	public CommonLibOpenHelper(Context context) {
		super(context, "common_lib.db", null, 1);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		//课件信息表
		db.execSQL("CREATE TABLE [download_info] ([url] TEXT, [thid] INT, [done] INT, [downlength] INT);");
		
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		
	}

}
