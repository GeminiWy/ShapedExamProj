package com.nd.shapedexamproj.db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.nd.shapedexamproj.im.model.CommunicationItemInfo;
import com.nd.shapedexamproj.model.my.PersonalInfo;
import com.tming.common.util.Log;

import java.util.List;

/**
 * 
 * <p>群聊用户信息表，做本地缓存使用</p>
 * <p>Created by zll on 2014-10-17</p>
 */
public class ChatUserInfoDao {
    
    
private static final String TAG = ChatUserInfoDao.class.getSimpleName();
    
    private ChatMsgDBOpenHelper helper;
    private Context mContext;
    private SQLiteDatabase db;

    public ChatUserInfoDao(Context context) {
        mContext = context;
        helper = new ChatMsgDBOpenHelper(mContext);
    }
    
    private static ChatUserInfoDao instance = null;
    public static ChatUserInfoDao getInstance(Context context) {
        if (instance == null) {
            synchronized (ChatUserInfoDao.class) {
                instance = new ChatUserInfoDao(context);
            }
        }
        return instance;
    }
    
    /**
     * <p>保存用户信息</P>
     * @param result
     */
    public void saveUserInfo(List<CommunicationItemInfo> result) {
        for (int i = 0;i < result.size();i ++) {
            CommunicationItemInfo itemInfo = result.get(i);
            String photoUrl = itemInfo.getUserPhotoUrl();
            String toUserId = photoUrl.substring(photoUrl.lastIndexOf("uid="));
            Log.e(TAG, "要存的用户id：" + toUserId + ",名称：" + itemInfo.getName());
            // 获取用户信息  如果本地数据库存在该用户信息，则不请求网络
            PersonalInfo mPersonalInfo = getChatUserInfo(toUserId);
            if (mPersonalInfo == null) {
                mPersonalInfo = new PersonalInfo();
                
                mPersonalInfo.setUserId(toUserId);
                mPersonalInfo.setUserName(itemInfo.getName());
                mPersonalInfo.setPhotoUrl(itemInfo.getUserPhotoUrl());
                inSertChatUserInfo(mPersonalInfo);
            }
        }
    }
    
    public void inSertChatUserInfo(PersonalInfo personalInfo) {

        synchronized (helper.getDbLock()) {
            try {
                db = helper.getWritableDatabase();
                db.execSQL("insert into " + ChatMsgDBOpenHelper.CHAT_USERINFO_TABLE_NAME + "("
                        + ChatMsgDBOpenHelper.CHAT_ROOM_FIELD_FID + ", "
                        + ChatMsgDBOpenHelper.CHAT_ROOM_FIELD_NAME + ", "
                        + ChatMsgDBOpenHelper.CHAT_ROOM_FIELD_USER_IMG + ")"
                        + " values('?','?','?')"
    
                , new Object[] { personalInfo.getUserId(), personalInfo.getUserName(), personalInfo.getPhotoUrl()});
                Log.e(TAG, "-----insert:" + personalInfo.getUserName());
                
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
    
    public PersonalInfo getChatUserInfo(String fromUserId) {
        PersonalInfo personalInfo = null;
        synchronized (helper.getDbLock()) {
            Cursor cursor = null;
            try{
                String sql= "select * from " + ChatMsgDBOpenHelper.CHAT_USERINFO_TABLE_NAME +
                        " where "+ ChatMsgDBOpenHelper.CHAT_ROOM_FIELD_FID +"=?; ";
                db = helper.getWritableDatabase();
                
                
                cursor = db.rawQuery(sql,new String[]{fromUserId});
                Log.e(TAG, sql);
                
                while(cursor.moveToNext()) {
                    personalInfo = creatChatMsgEntity(cursor);
                }
                
            } catch(Exception e) {
                e.printStackTrace();
            } finally {
                if (cursor != null) {
                    cursor = null;
                }
            }
        }
        return personalInfo;
    }
    
    private PersonalInfo creatChatMsgEntity(Cursor cursor) {
        
        PersonalInfo personalInfo = new PersonalInfo();
        
        personalInfo.setUserId(cursor.getString(cursor.getColumnIndex(ChatMsgDBOpenHelper.CHAT_ROOM_FIELD_FID)));
        personalInfo.setUserName(cursor.getString(cursor.getColumnIndex(ChatMsgDBOpenHelper.CHAT_ROOM_FIELD_NAME)));
        personalInfo.setPhotoUrl(cursor.getString(cursor.getColumnIndex(ChatMsgDBOpenHelper.CHAT_ROOM_FIELD_USER_IMG)));
        return personalInfo;
    }
    
}
