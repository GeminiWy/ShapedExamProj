package com.nd.shapedexamproj.db;


import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.tming.common.util.Log;

/**
 * 
 * @ClassName: MyDbOpenHelper
 * @Title:
 * @Description:数据存储
 * @Author:XueWenJian
 * @Since:2014年5月7日09:34:42
 * @Version:1.0
 */
public class ChatMsgDBOpenHelper extends SQLiteOpenHelper{

    private Object dbLock = new Object();
	private static final int DATABASE_VERSION = 4;
	private static final String TAG = "ChatMsgDBOpenHelper";
	
	private static final String DATABASE_NAME = "open_university_chat_msg.db";
	//-------------------------------------------------------------------------------------------
	public static final String CHAT_TABLE_NAME = "t_chatmsg";
	public static final String CHAT_FIELD_ID = "id";
	public static final String CHAT_FIELD_MSG_ID = "msgId";
	public static final String CHAT_FIELD_FID = "formId";
	public static final String CHAT_FIELD_FNAME = "formName";
	public static final String CHAT_FIELD_FIMG = "formImgurl";
	public static final String CHAT_FIELD_TID = "toId";
	public static final String CHAT_FIELD_TNAME = "toName";
	public static final String CHAT_FIELD_TIMG = "toImgurl";
	public static final String CHAT_FIELD_DATE = "date";
	public static final String CHAT_FIELD_MSG = "msg";
	public static final String CHAT_FIELD_STATE = "state";
	public static final String CHAT_FIELD_TAG = "tag";
	
	private static final String CHAT_TABLE_CREATE = "CREATE TABLE "+CHAT_TABLE_NAME +
			"("+CHAT_FIELD_ID+" INTEGER PRIMARY KEY," +
			""+CHAT_FIELD_FID+" TEXT,"+CHAT_FIELD_FNAME+" TEXT,"+CHAT_FIELD_FIMG+" TEXT" +
			","+CHAT_FIELD_TID+" TEXT,"+CHAT_FIELD_TNAME+" TEXT,"+CHAT_FIELD_TIMG+" TEXT" +
			","+CHAT_FIELD_DATE+" TEXT,"+CHAT_FIELD_MSG+" TEXT,"+CHAT_FIELD_STATE+" TEXT" +
			","+CHAT_FIELD_TAG+ " TEXT," + CHAT_FIELD_MSG_ID + " TEXT)" ;
	//-------------------------------------------------------------------------------------------
	public static final String CHAT_ROOM_TABLE_NAME = "t_chat_room_msg";
	public static final String CHAT_ROOM_FIELD_ID = "id";
	
	public static final String CHAT_ROOM_FIELD_ROOMID = "roomId";
	public static final String CHAT_ROOM_FIELD_FID = "formuserId";
	public static final String CHAT_ROOM_FIELD_NAME = "fromusername";
	public static final String CHAT_ROOM_FIELD_USER_IMG = "fromuserimg";//用户头像
	public static final String CHAT_ROOM_FIELD_TID = "toId";
	
	public static final String CHAT_ROOM_FIELD_DATE = "date";
	public static final String CHAT_ROOM_FIELD_MSG = "msg";
	public static final String CHAT_ROOM_FIELD_STATE = "state";
	public static final String CHAT_ROOM_FIELD_TAG = "tag";
	
	private static final String CHAT_ROOM_TABLE_CREATE = "CREATE TABLE "+CHAT_ROOM_TABLE_NAME +
			"("+CHAT_ROOM_FIELD_ID+" INTEGER PRIMARY KEY," +
			""+CHAT_ROOM_FIELD_ROOMID+" TEXT,"+CHAT_ROOM_FIELD_FID+" TEXT,"+CHAT_ROOM_FIELD_TID+" TEXT" +
			","+CHAT_ROOM_FIELD_DATE+" TEXT,"+CHAT_ROOM_FIELD_MSG+" TEXT,"+CHAT_ROOM_FIELD_STATE+" TEXT" +
			","+CHAT_ROOM_FIELD_TAG+" TEXT," + CHAT_ROOM_FIELD_NAME + " TEXT," + CHAT_ROOM_FIELD_USER_IMG+ " TEXT)";
	//-------------------------------------------------------------------------------------------
	public static final String CHAT_HISTORY_TABLE_NAME = "t_chat_history_msg";
	public static final String CHAT_HISTORY_FIELD_ID = "id";
	public static final String CHAT_HISTORY_FIELD_UID = "userId";
	public static final String CHAT_HISTORY_FIELD_NAME = "name";
	public static final String CHAT_HISTORY_FIELD_OPPID = "oppId";
	public static final String CHAT_HISTORY_FIELD_UPDATE_TIME = "updateTimeStamp";
	public static final String CHAT_HISTORY_FIELD_TOP_TIME = "topTimeStamp";
	public static final String CHAT_HISTORY_FIELD_TOP_TYPE = "chatType";
	public static final String CHAT_HISTORY_FIELD_TAG = "tag";

	private static final String CHAT_HISTORY_TABLE_CREATE = "CREATE TABLE "+CHAT_HISTORY_TABLE_NAME +
			"("+CHAT_HISTORY_FIELD_ID+" INTEGER PRIMARY KEY," +
			""+CHAT_HISTORY_FIELD_UID+" TEXT ,"+CHAT_HISTORY_FIELD_NAME+" TEXT,"+CHAT_HISTORY_FIELD_OPPID+" TEXT" +
			","+CHAT_HISTORY_FIELD_UPDATE_TIME+" NUMBER,"+CHAT_HISTORY_FIELD_TOP_TIME+" NUMBER,"
			+CHAT_HISTORY_FIELD_TOP_TYPE+" TEXT" +
			","+CHAT_HISTORY_FIELD_TAG+" TEXT UNIQUE " +
			")" ;
	//-------------------------------------------------------------------------------------------
	public static final String CHAT_USERINFO_TABLE_NAME = "t_chat_user_info";//本地缓存用户信息
	
	private static final String CHAT_USERINFO_TABLE_CREATE = "CREATE TABLE " + CHAT_USERINFO_TABLE_NAME + "(" +
	        CHAT_ROOM_FIELD_FID+" TEXT PRIMARY KEY," + CHAT_ROOM_FIELD_NAME + " TEXT," + CHAT_ROOM_FIELD_USER_IMG+ " TEXT)";
	//-------------------------------------------------------------------------------------------
	
	public Object getDbLock() {
        return dbLock;
    }
	
	public ChatMsgDBOpenHelper(Context context){
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(CHAT_TABLE_CREATE);
		db.execSQL(CHAT_ROOM_TABLE_CREATE);
		db.execSQL(CHAT_HISTORY_TABLE_CREATE);
		db.execSQL(CHAT_USERINFO_TABLE_CREATE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		if(oldVersion == 1) {
		    String sql = "ALTER TABLE " + CHAT_ROOM_TABLE_NAME + " ADD COLUMN " + CHAT_ROOM_FIELD_NAME + " TEXT ";
		    Log.e(TAG, sql);
            db.execSQL(sql);
		}
		if (oldVersion >= 1 && oldVersion < 3) {
		    String sql = "ALTER TABLE " + CHAT_ROOM_TABLE_NAME + " ADD COLUMN " + CHAT_ROOM_FIELD_USER_IMG + " TEXT ";
            Log.e(TAG, sql);
            db.execSQL(sql);
            db.execSQL(CHAT_USERINFO_TABLE_CREATE);
		}
		if (oldVersion == 3) {
			db.execSQL("drop table " + CHAT_TABLE_NAME);
			db.execSQL(CHAT_TABLE_CREATE);
		}
	}
	
}
