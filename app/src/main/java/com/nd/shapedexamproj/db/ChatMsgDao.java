package com.nd.shapedexamproj.db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.nd.shapedexamproj.entity.ChatMsgEntity;
import com.tming.common.util.Log;

import java.util.ArrayList;
import java.util.List;

public class ChatMsgDao {

	private static final String TAG = ChatMsgDao.class.getSimpleName();
	
	private ChatMsgDBOpenHelper helper;
	private Context mContext;
	private SQLiteDatabase db;
	
	private static ChatMsgDao instance = null;
    public static ChatMsgDao getInstance(Context context) {
        if (instance == null) {
            synchronized (ChatMsgDao.class) {
                instance = new ChatMsgDao(context);
            }
        }
        return instance;
    }
	
	public ChatMsgDao(Context context) {
		mContext = context;
		helper = new ChatMsgDBOpenHelper(mContext);
	}


	public void insertChatMsg(ChatMsgEntity chatMsgEntity) {
	    synchronized (helper.getDbLock()) {
	        Cursor cursor = null;
    		try {
    			db = helper.getWritableDatabase();
    			db.execSQL("insert into " + ChatMsgDBOpenHelper.CHAT_TABLE_NAME + "("
    					+ ChatMsgDBOpenHelper.CHAT_FIELD_ID + ", "
						+ ChatMsgDBOpenHelper.CHAT_FIELD_MSG_ID + ", "
    					+ ChatMsgDBOpenHelper.CHAT_FIELD_FID + ", "
    					+ ChatMsgDBOpenHelper.CHAT_FIELD_FNAME + ","
    					+ ChatMsgDBOpenHelper.CHAT_FIELD_FIMG + ", "
    					+ ChatMsgDBOpenHelper.CHAT_FIELD_TID + ", "
    					+ ChatMsgDBOpenHelper.CHAT_FIELD_TNAME + ","
    					+ ChatMsgDBOpenHelper.CHAT_FIELD_TIMG + ", "
    					+ ChatMsgDBOpenHelper.CHAT_FIELD_DATE + ", "
    					+ ChatMsgDBOpenHelper.CHAT_FIELD_MSG + ","
    					+ ChatMsgDBOpenHelper.CHAT_FIELD_STATE + ","
    					+ ChatMsgDBOpenHelper.CHAT_FIELD_TAG + ")"
						//TODO ?报错
    					+ " values('?','?','?','?','?','?','?','?','?','?','?','?')"
    
    			, new Object[] { chatMsgEntity.getId(), chatMsgEntity.getMsgId(),chatMsgEntity.getFormId(),
    					chatMsgEntity.getFormName(), chatMsgEntity.getFormImgurl(),
    					chatMsgEntity.getToId(), chatMsgEntity.getToName(),
    					chatMsgEntity.getToImgurl(), chatMsgEntity.getDate(),
    					chatMsgEntity.getText(), chatMsgEntity.getState(), chatMsgEntity.getTag() });
    			Log.e(TAG, "-----insert:" + chatMsgEntity.getText());
    			cursor = db.rawQuery("select last_insert_rowid() from "+ChatMsgDBOpenHelper.CHAT_TABLE_NAME,null);          
    			int strid = 0;    
    			if(cursor.moveToFirst())  
    			   strid = cursor.getInt(0);  
    			Log.e(TAG, "get ID:"+strid); 
    			chatMsgEntity.setId(""+strid);
    			
    		} catch (Exception e) {
    			Log.i("e", e.getMessage());
    			e.printStackTrace();
    
    		} finally {
    		    cursor.close();
    		    if (db != null) {
                    db.close();
                }
    		}
	    }
	}

	public void updateChatMsgState(ChatMsgEntity chatMsgEntity) { 
	    synchronized (helper.getDbLock()) {
    		String sql = "update " + ChatMsgDBOpenHelper.CHAT_TABLE_NAME +
    						" set " + ChatMsgDBOpenHelper.CHAT_FIELD_STATE + " = ? " +
    						" where "+ ChatMsgDBOpenHelper.CHAT_FIELD_ID + " = ? ;";
    		try {
    			db = helper.getWritableDatabase();
    			db.execSQL(sql, new Object[] {chatMsgEntity.getState(), chatMsgEntity.getId()});
    		} catch (Exception e) {
    			Log.i("e", e.getMessage());
    			e.printStackTrace();
    
    		} finally {
    		    if (db != null) {
    		        db.close();
    		    }
    		}
	    }
	}
	
	public ChatMsgEntity getLastChatMsg(String tag) {
		Log.i(TAG, "getLastChatMsg tag :"+tag);
		
		ChatMsgEntity chatMsgEntity = new ChatMsgEntity();
		synchronized (helper.getDbLock()) {
    		List<ChatMsgEntity> chatMsgEntities = new ArrayList<ChatMsgEntity>();
    		
    		String sql= "select * from " + ChatMsgDBOpenHelper.CHAT_TABLE_NAME +
    				" where "+ ChatMsgDBOpenHelper.CHAT_FIELD_TAG +"=? " +
    						"order by " + ChatMsgDBOpenHelper.CHAT_FIELD_ID +" DESC"+
    						";";
    		
    		db = helper.getReadableDatabase();
    		Cursor cursor = null;
    		cursor = db.rawQuery(sql,new String[]{tag});
    		Log.e(TAG, sql);
    
    		if(cursor.moveToNext()){
    			chatMsgEntity = creatChatMsgEntity(cursor);
    
    			chatMsgEntities.add(chatMsgEntity);
    		}
    		
    		cursor.close();
    		if (db != null) {
                db.close();
            }
    		Log.i(TAG, "getLastChatMsg chatMsgEntity :"+chatMsgEntity.getText());
	    }
		return chatMsgEntity;
	}
	
	public List<ChatMsgEntity> getChatMsgByTag(String tag,int page, int pageSize) {
		List<ChatMsgEntity> chatMsgEntities = new ArrayList<ChatMsgEntity>();
		synchronized (helper.getDbLock()) {
    		String sql= "select * from " + ChatMsgDBOpenHelper.CHAT_TABLE_NAME +
    				" where "+ ChatMsgDBOpenHelper.CHAT_FIELD_TAG +"=? " +
    						"order by " + ChatMsgDBOpenHelper.CHAT_FIELD_ID +" DESC"+
    						" limit ? offset ?;";
    		
    		db = helper.getReadableDatabase();
    		Cursor cursor = null;
    		cursor = db.rawQuery(sql,new String[]{tag, ""+pageSize, ""+(page*pageSize)});
    
    		Log.e(TAG, sql);
    		Log.e(TAG, "tag:"+ tag+";page:"+page+";pageSize:"+pageSize);
    		
    		while(cursor.moveToNext()){
    			ChatMsgEntity chatMsgEntity = new ChatMsgEntity();
    			
    			chatMsgEntity = creatChatMsgEntity(cursor);
    			
    			
    			chatMsgEntities.add(chatMsgEntity);
    		}
    		
    		cursor.close();
    		if (db != null) {
                db.close();
            }
		}
		return chatMsgEntities;
	}
	
	private ChatMsgEntity creatChatMsgEntity(Cursor cursor) {
		
		ChatMsgEntity chatMsgEntity = new ChatMsgEntity();
		
		chatMsgEntity.setId(cursor.getString(cursor.getColumnIndex(ChatMsgDBOpenHelper.CHAT_FIELD_ID)));
		chatMsgEntity.setMsgId(cursor.getString(cursor.getColumnIndex(ChatMsgDBOpenHelper.CHAT_FIELD_MSG_ID)));
		chatMsgEntity.setFormId(cursor.getString(cursor.getColumnIndex(ChatMsgDBOpenHelper.CHAT_FIELD_FID)));
		chatMsgEntity.setFormName(cursor.getString(cursor.getColumnIndex(ChatMsgDBOpenHelper.CHAT_FIELD_FNAME)));
		chatMsgEntity.setFormImgurl(cursor.getString(cursor.getColumnIndex(ChatMsgDBOpenHelper.CHAT_FIELD_FIMG)));
		
		chatMsgEntity.setToId(cursor.getString(cursor.getColumnIndex(ChatMsgDBOpenHelper.CHAT_FIELD_TID)));
		chatMsgEntity.setToName(cursor.getString(cursor.getColumnIndex(ChatMsgDBOpenHelper.CHAT_FIELD_TNAME)));
		chatMsgEntity.setToImgurl(cursor.getString(cursor.getColumnIndex(ChatMsgDBOpenHelper.CHAT_FIELD_TIMG)));
		
		chatMsgEntity.setDate(cursor.getString(cursor.getColumnIndex(ChatMsgDBOpenHelper.CHAT_FIELD_DATE)));
		chatMsgEntity.setText(cursor.getString(cursor.getColumnIndex(ChatMsgDBOpenHelper.CHAT_FIELD_MSG)));
		chatMsgEntity.setState(cursor.getInt(cursor.getColumnIndex(ChatMsgDBOpenHelper.CHAT_FIELD_STATE)));
		chatMsgEntity.setTag(cursor.getString(cursor.getColumnIndex(ChatMsgDBOpenHelper.CHAT_FIELD_TAG)));
		
		return chatMsgEntity;
	}
	
	
	public void deleteAllChatMsg(String tag) {
	    synchronized (helper.getDbLock()) {
    		String sql = "delete from " + ChatMsgDBOpenHelper.CHAT_TABLE_NAME +
    				" where "+ ChatMsgDBOpenHelper.CHAT_FIELD_TAG + " = ? ;";
    		try {
    			db = helper.getWritableDatabase();
    			db.execSQL(sql, new Object[] {tag});
    		} catch (Exception e) {
    			Log.i("e", e.getMessage());
    			e.printStackTrace();
    		
    		} finally {
    			db.close();
    		}
	    }
	}
}
