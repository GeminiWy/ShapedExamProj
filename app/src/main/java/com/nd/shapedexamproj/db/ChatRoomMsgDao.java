package com.nd.shapedexamproj.db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.nd.shapedexamproj.entity.ChatGroupMsgEntity;
import com.tming.common.util.Log;

import java.util.ArrayList;
import java.util.List;

public class ChatRoomMsgDao {

	private static final String TAG = ChatRoomMsgDao.class.getSimpleName();
	
	private ChatMsgDBOpenHelper helper;
	private Context mContext;
	private SQLiteDatabase db;

	public ChatRoomMsgDao(Context context) {
		mContext = context;
		helper = new ChatMsgDBOpenHelper(mContext);
	}
	
	private static ChatRoomMsgDao instance = null;
    public static ChatRoomMsgDao getInstance(Context context) {
        if (instance == null) {
            synchronized (ChatRoomMsgDao.class) {
                instance = new ChatRoomMsgDao(context);
            }
        }
        return instance;
    }

	public void insertChatMsg(ChatGroupMsgEntity chatMsgEntity) {
	    synchronized (helper.getDbLock()) {
	        Cursor cursor = null;
    		try {
    			db = helper.getWritableDatabase();
    			db.execSQL("insert into " + ChatMsgDBOpenHelper.CHAT_ROOM_TABLE_NAME + "("
    					+ ChatMsgDBOpenHelper.CHAT_ROOM_FIELD_ID + ", "
    					+ ChatMsgDBOpenHelper.CHAT_ROOM_FIELD_ROOMID + ", "
    					+ ChatMsgDBOpenHelper.CHAT_ROOM_FIELD_FID + ","
    					+ ChatMsgDBOpenHelper.CHAT_ROOM_FIELD_TID + ", "
    					+ ChatMsgDBOpenHelper.CHAT_ROOM_FIELD_DATE + ", "
    					+ ChatMsgDBOpenHelper.CHAT_ROOM_FIELD_MSG + ","
    					+ ChatMsgDBOpenHelper.CHAT_ROOM_FIELD_STATE + ", "
    					+ ChatMsgDBOpenHelper.CHAT_ROOM_FIELD_TAG + ")"
						//TODO ?改成'?'
    					+ " values('?','?','?','?','?','?','?','?')"
    
    			, new Object[] { chatMsgEntity.getId(), chatMsgEntity.getGroupId(),
    					chatMsgEntity.getFromUserId(), chatMsgEntity.getToId(),
    					chatMsgEntity.getDate(), chatMsgEntity.getText(),
    					chatMsgEntity.getState(), chatMsgEntity.getTag() });
    
    			cursor = db.rawQuery("select last_insert_rowid() from "+ChatMsgDBOpenHelper.CHAT_ROOM_TABLE_NAME,null);          
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

	public void updateChatMsgState(ChatGroupMsgEntity chatMsgEntity) {
	    synchronized (helper.getDbLock()) {
    		String sql = "update " + ChatMsgDBOpenHelper.CHAT_TABLE_NAME +
    						" set " + ChatMsgDBOpenHelper.CHAT_FIELD_STATE + " = ? " +
    						" where "+ ChatMsgDBOpenHelper.CHAT_FIELD_ID + " = ? ;";
    		try {
    			db = helper.getWritableDatabase();
    			db.execSQL(sql, new Object[] {chatMsgEntity.getState(), chatMsgEntity.getId()});
    		} catch (Exception e) {
    			Log.i(TAG, e.getMessage());
    			e.printStackTrace();
    
    		} finally {
    		    if (db != null) {
                    db.close();
                }
    		}
	    }
	}
	
	public void updateChatRoomMsgUserName(ChatGroupMsgEntity chatMsgEntity) {
	    synchronized (helper.getDbLock()) {
            String sql = "update " + ChatMsgDBOpenHelper.CHAT_ROOM_TABLE_NAME +
                            " set " + ChatMsgDBOpenHelper.CHAT_ROOM_FIELD_NAME + " = ?, " + 
                            ChatMsgDBOpenHelper.CHAT_ROOM_FIELD_USER_IMG + " = ? " +
                            " where "+ ChatMsgDBOpenHelper.CHAT_ROOM_FIELD_FID + " = ? ;";
            Log.e(TAG, "updateChatRoomMsgUserName: " + sql);
            try {
                db = helper.getWritableDatabase();
                db.execSQL(sql, new Object[] {chatMsgEntity.getFromUserName(), chatMsgEntity.getFormUserImgurl(), chatMsgEntity.getFromUserId()});
            } catch (Exception e) {
                Log.i(TAG, e.getMessage());
                e.printStackTrace();
    
            } finally {
                if (db != null) {
                    db.close();
                }
            }
	    }
    }
	
	public ChatGroupMsgEntity getLastChatRoomMsg(String tag) {
		Log.i(TAG, "getLastChatRoomMsg tag :"+tag);
		ChatGroupMsgEntity chatMsgEntity = null;
    		
		synchronized (helper.getDbLock()) {
		    chatMsgEntity = new ChatGroupMsgEntity();
    		String sql= "select * from " + ChatMsgDBOpenHelper.CHAT_ROOM_TABLE_NAME +
    				" where "+ ChatMsgDBOpenHelper.CHAT_ROOM_FIELD_TAG +"=? " +
    						"order by " + ChatMsgDBOpenHelper.CHAT_ROOM_FIELD_ID +" DESC"+
    						";";
    		
    		db = helper.getReadableDatabase();
    		Cursor cursor = null;
    		cursor = db.rawQuery(sql,new String[]{tag});
    		Log.e(TAG, sql);
    
    		if(cursor.moveToNext()){
    			chatMsgEntity = creatChatRoomMsgEntity(cursor);
    
    		}
    		
    		cursor.close();
    		if (db != null) {
                db.close();
            }
    		Log.i(TAG, "getLastChatMsg chatMsgEntity :"+chatMsgEntity.getText());
	    }
		return chatMsgEntity;
	}
	
	
	
	public List<ChatGroupMsgEntity> getChatMsgByTag(String tag,int page, int pageSize) {
		List<ChatGroupMsgEntity> chatMsgEntities = null;
		synchronized (helper.getDbLock()) {
		    chatMsgEntities = new ArrayList<ChatGroupMsgEntity>();
    		String sql= "select * from " + ChatMsgDBOpenHelper.CHAT_ROOM_TABLE_NAME +
    				" where "+ ChatMsgDBOpenHelper.CHAT_ROOM_FIELD_TAG +"=? " +
    						"order by " + ChatMsgDBOpenHelper.CHAT_ROOM_FIELD_ID +" DESC"+
    						" limit ? offset ?;";
    		
    		db = helper.getReadableDatabase();
    		Cursor cursor = null;
    		cursor = db.rawQuery(sql,new String[]{tag, ""+pageSize, ""+(page*pageSize)});
    
    		Log.e(TAG, sql);
    		Log.e(TAG, "tag:"+ tag+";page:"+page+";pageSize:"+pageSize);
    		
    		while (cursor.moveToNext()) {
    			ChatGroupMsgEntity chatMsgEntity = new ChatGroupMsgEntity();
    			
    			chatMsgEntity = creatChatRoomMsgEntity(cursor);
    			
    			chatMsgEntities.add(chatMsgEntity);
    		}
    		
    		cursor.close();
    		if (db != null) {
                db.close();
            }
		}
		return chatMsgEntities;
	}
	
	private ChatGroupMsgEntity creatChatRoomMsgEntity(Cursor cursor) {
		
		ChatGroupMsgEntity chatMsgEntity = new ChatGroupMsgEntity();
		
		chatMsgEntity.setId(cursor.getString(cursor.getColumnIndex(ChatMsgDBOpenHelper.CHAT_ROOM_FIELD_ID)));
		chatMsgEntity.setGroupId(cursor.getString(cursor.getColumnIndex(ChatMsgDBOpenHelper.CHAT_ROOM_FIELD_ID)));
		chatMsgEntity.setFromUserId(cursor.getString(cursor.getColumnIndex(ChatMsgDBOpenHelper.CHAT_ROOM_FIELD_FID)));
		chatMsgEntity.setFromUserName(cursor.getString(cursor.getColumnIndex(ChatMsgDBOpenHelper.CHAT_ROOM_FIELD_NAME)));
		chatMsgEntity.setFormUserImgurl(cursor.getString(cursor.getColumnIndex(ChatMsgDBOpenHelper.CHAT_ROOM_FIELD_USER_IMG)));
		chatMsgEntity.setToId(cursor.getString(cursor.getColumnIndex(ChatMsgDBOpenHelper.CHAT_ROOM_FIELD_TID)));
			
		chatMsgEntity.setDate(cursor.getString(cursor.getColumnIndex(ChatMsgDBOpenHelper.CHAT_ROOM_FIELD_DATE)));
		chatMsgEntity.setText(cursor.getString(cursor.getColumnIndex(ChatMsgDBOpenHelper.CHAT_ROOM_FIELD_MSG)));
		chatMsgEntity.setState(cursor.getInt(cursor.getColumnIndex(ChatMsgDBOpenHelper.CHAT_ROOM_FIELD_STATE)));
		chatMsgEntity.setTag(cursor.getString(cursor.getColumnIndex(ChatMsgDBOpenHelper.CHAT_ROOM_FIELD_TAG)));
			
		return chatMsgEntity;
	}
	
	
	public void deleteAllChatRoomMsg(String tag) {
		String sql = "delete from " + ChatMsgDBOpenHelper.CHAT_ROOM_TABLE_NAME +
				" where "+ ChatMsgDBOpenHelper.CHAT_ROOM_FIELD_TAG + " = ? ;";
		try {
			db = helper.getWritableDatabase();
			db.execSQL(sql, new Object[] {tag});
		} catch (Exception e) {
			Log.i(TAG, e.getMessage());
			e.printStackTrace();
		
		} finally {
		    if (db != null) {
                db.close();
            }
		}
	}
	
}
