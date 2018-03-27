package com.nd.shapedexamproj.db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.nd.shapedexamproj.entity.ChatHistoryEntity;
import com.nd.shapedexamproj.entity.ChatMsgEntity;
import com.tming.common.util.Log;

import java.util.ArrayList;
import java.util.List;

public class ChatHistoryMsgDao {

	private static final String TAG = ChatHistoryMsgDao.class.getSimpleName();
	
	private ChatMsgDBOpenHelper helper;
	private Context mContext;
	private SQLiteDatabase db;

	public ChatHistoryMsgDao(Context context) {
		mContext = context;
		helper = new ChatMsgDBOpenHelper(mContext);
	}

	
	public void replaceChatHistotyMsg(ChatHistoryEntity chatMsgEntity) {
		Log.e(TAG, "replaceChatHistotyMsg ID:"+chatMsgEntity.getUserId()); 
		try {
			db = helper.getWritableDatabase();
			db.execSQL("REPLACE into " + ChatMsgDBOpenHelper.CHAT_HISTORY_TABLE_NAME + "("
					+ ChatMsgDBOpenHelper.CHAT_HISTORY_FIELD_UID + ","
					+ ChatMsgDBOpenHelper.CHAT_HISTORY_FIELD_NAME + ", "
					+ ChatMsgDBOpenHelper.CHAT_HISTORY_FIELD_OPPID + ", "
					+ ChatMsgDBOpenHelper.CHAT_HISTORY_FIELD_UPDATE_TIME + ","
					+ ChatMsgDBOpenHelper.CHAT_HISTORY_FIELD_TOP_TIME + ", "
					+ ChatMsgDBOpenHelper.CHAT_HISTORY_FIELD_TOP_TYPE + ", "
					+ ChatMsgDBOpenHelper.CHAT_HISTORY_FIELD_TAG + ")"
					//TODO 此类中?报错
					+ " values('?','?','?','?','?','?','?')"
				, new Object[] { chatMsgEntity.getUserId(),
					chatMsgEntity.getName(), chatMsgEntity.getOppId(),
					chatMsgEntity.getUpdateTimeStamp(), chatMsgEntity.getTopTimeStamp(),
					chatMsgEntity.getChatType(), chatMsgEntity.getTag() });
					
					
		} catch (Exception e) {
			Log.i("e", e.getMessage());
			e.printStackTrace();

		} finally {
			db.close();
		}
	}
	

	public void insertChatHistotyMsg(ChatHistoryEntity chatMsgEntity) {
		Log.e(TAG, "insertChatHistotyMsg ID:"+chatMsgEntity.getUserId()); 
		try {
			db = helper.getWritableDatabase();
			db.execSQL("insert into " + ChatMsgDBOpenHelper.CHAT_HISTORY_TABLE_NAME + "("
					+ ChatMsgDBOpenHelper.CHAT_HISTORY_FIELD_ID + ", "
					+ ChatMsgDBOpenHelper.CHAT_HISTORY_FIELD_UID + ","
					+ ChatMsgDBOpenHelper.CHAT_HISTORY_FIELD_NAME + ", "
					+ ChatMsgDBOpenHelper.CHAT_HISTORY_FIELD_OPPID + ", "
					+ ChatMsgDBOpenHelper.CHAT_HISTORY_FIELD_UPDATE_TIME + ","
					+ ChatMsgDBOpenHelper.CHAT_HISTORY_FIELD_TOP_TIME + ", "
					+ ChatMsgDBOpenHelper.CHAT_HISTORY_FIELD_TOP_TYPE + ", "
					+ ChatMsgDBOpenHelper.CHAT_HISTORY_FIELD_TAG + ")"
					+ " values('?','?','?','?','?','?','?','?')"

			, new Object[] { chatMsgEntity.getId(), chatMsgEntity.getUserId(),
					chatMsgEntity.getName(), chatMsgEntity.getOppId(),
					chatMsgEntity.getUpdateTimeStamp(), chatMsgEntity.getTopTimeStamp(),
					chatMsgEntity.getChatType(), chatMsgEntity.getTag() });

			Cursor cursor = db.rawQuery("select last_insert_rowid() from "+ChatMsgDBOpenHelper.CHAT_HISTORY_TABLE_NAME,null);          
			int strid = 0;    
			if(cursor.moveToFirst())  
			   strid = cursor.getInt(0);  
			Log.e(TAG, "get ID:"+strid); 
			chatMsgEntity.setId(""+strid);
			
		} catch (Exception e) {
			Log.i("e", e.getMessage());
			e.printStackTrace();

		} finally {
			db.close();
		}

	}
	
	public void deleteChatHistotyMsg(ChatHistoryEntity chatHistoryEntity) {
		String sql = "delete from " + ChatMsgDBOpenHelper.CHAT_HISTORY_TABLE_NAME +
				" where "+ ChatMsgDBOpenHelper.CHAT_HISTORY_FIELD_OPPID + " = ? ;";
		try {
			db = helper.getWritableDatabase();
			db.execSQL(sql, new Object[] {chatHistoryEntity.getOppId()});
		} catch (Exception e) {
			Log.i("e", e.getMessage());
			e.printStackTrace();
		
		} finally {
			db.close();
		}
	}

	public void updateChatMsgState(ChatMsgEntity chatMsgEntity)
	{
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
			db.close();
		}
	}
	
	
	public List<ChatHistoryEntity> getChatHistoryMsgByUserId(String userId,int page, int pageSize) {
		List<ChatHistoryEntity> chatMsgEntities = new ArrayList<ChatHistoryEntity>();
		
		String sql= "select * from " + ChatMsgDBOpenHelper.CHAT_HISTORY_TABLE_NAME +
				" where "+ ChatMsgDBOpenHelper.CHAT_HISTORY_FIELD_UID +"=? " +
						"order by " + ChatMsgDBOpenHelper.CHAT_HISTORY_FIELD_UPDATE_TIME +" DESC"+
						" limit ? offset ?;";
		
		db = helper.getReadableDatabase();
		Cursor cursor = null;
		cursor = db.rawQuery(sql,new String[]{userId, ""+pageSize, ""+(page*pageSize)});

		Log.e(TAG, sql);
		Log.e(TAG, "userId:"+ userId+";page:"+page+";pageSize:"+pageSize);
		
		while(cursor.moveToNext()){
			ChatHistoryEntity chatMsgEntity = new ChatHistoryEntity();
			
			chatMsgEntity.setId(cursor.getString(cursor.getColumnIndex(ChatMsgDBOpenHelper.CHAT_HISTORY_FIELD_ID)));
			chatMsgEntity.setUserId(cursor.getString(cursor.getColumnIndex(ChatMsgDBOpenHelper.CHAT_HISTORY_FIELD_UID)));
			chatMsgEntity.setName(cursor.getString(cursor.getColumnIndex(ChatMsgDBOpenHelper.CHAT_HISTORY_FIELD_NAME)));
			chatMsgEntity.setOppId(cursor.getString(cursor.getColumnIndex(ChatMsgDBOpenHelper.CHAT_HISTORY_FIELD_OPPID)));
			
			chatMsgEntity.setUpdateTimeStamp(cursor.getLong(cursor.getColumnIndex(ChatMsgDBOpenHelper.CHAT_HISTORY_FIELD_UPDATE_TIME)));
			chatMsgEntity.setTopTimeStamp(cursor.getLong(cursor.getColumnIndex(ChatMsgDBOpenHelper.CHAT_HISTORY_FIELD_TOP_TIME)));
			chatMsgEntity.setChatType(cursor.getInt(cursor.getColumnIndex(ChatMsgDBOpenHelper.CHAT_HISTORY_FIELD_TOP_TYPE)));
			
			chatMsgEntity.setTag(cursor.getString(cursor.getColumnIndex(ChatMsgDBOpenHelper.CHAT_HISTORY_FIELD_TAG)));
			
			chatMsgEntities.add(chatMsgEntity);
		}
		
		cursor.close();
		db.close();
		
		return chatMsgEntities;
	}
}
