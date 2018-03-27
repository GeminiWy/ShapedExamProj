package com.nd.shapedexamproj.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.nd.shapedexamproj.App;
import com.nd.shapedexamproj.entity.RelatedUserEntity;
import com.nd.shapedexamproj.util.Constants;
import com.tming.common.util.Log;

import java.util.ArrayList;
import java.util.List;


public class NewFriendsDBOperator {
    
    private static final int OP_ERROR=0,OP_SUCCESS=1;
    public OpenUniversityOpenHelper helper;
    private static NewFriendsDBOperator operator;
    private String mUserId;
    
    public static NewFriendsDBOperator getInstance(Context context) {
        if (operator != null) {
            if (operator.mUserId.equals(App.getUserId())) {//上个用户id和当前用户id比较，避免当前用户操作上个用户的数据库。（使用static要注意）
                return operator;
            } else {
                operator = null;
            }
        }
        synchronized (NewFriendsDBOperator.class) {
            if (operator == null) {
                operator = new NewFriendsDBOperator(context);
            }
        }
        return operator;
    }
    
    
    public NewFriendsDBOperator(Context context){
        helper = new OpenUniversityOpenHelper(context);
        mUserId = App.getUserId();
    }
    
    /**
     * <p> 根据关注用户的id删除相关记录 </p>
     * @param type
     * @return
     */
    public int deleteNewFriendsByType(Integer type){

        synchronized (helper.getDbLock()) {
            SQLiteDatabase db = null;
            try{
                db = helper.getWritableDatabase();
                db.execSQL("delete from newfriends where type = '?'",new Integer[]{type});
            }catch(Exception e){
                e.printStackTrace();
                return OP_ERROR;
            }finally{
                if (db != null) db.close();
                return OP_SUCCESS;
            }
        }
    }
    /**
     * <p>插入用户关系记录</p>
     * @param entity 关系实体对象
     * @return
     */
    public synchronized int insertNewfriendsId(RelatedUserEntity entity){

        synchronized (helper.getDbLock()){
            SQLiteDatabase db = null;
            ContentValues cv = null;
            try{
                db = helper.getWritableDatabase();
                cv = new ContentValues();
                String fansId = "";
                if (App.getUserId().equals(entity.getmUserId())) {
                    fansId = entity.getmRelatedId();
                } else {
                    fansId = entity.getmUserId();
                }
                cv.put("fansId", fansId);
                cv.put("headImg", entity.getmRelatedImg());
                cv.put("type", Constants.FANS);
                
                db.insert("newfriends", null, cv);
                Log.e("插入", "粉丝ID：" + fansId);
            }catch(Exception e){
                e.printStackTrace();
                return OP_ERROR;
            }
            return OP_SUCCESS;
        }
    }
    
    /**
     * <p>根据用户关系类型 查询好友id</p>
     * @param
     * @return
     */
    public synchronized List<RelatedUserEntity> queryNewFriendsWithType(Integer type){
        List<RelatedUserEntity> relatedUserEntities=new ArrayList<RelatedUserEntity>();
        SQLiteDatabase db = null;
        Cursor cursor = null;
        try {
            db = helper.getWritableDatabase();
            //TODO 这个类中多个?报错，改成'?'
            cursor = db.rawQuery("select * from newfriends where type = '?'",
                    new String[] { String.valueOf(type) });
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    do {
                        RelatedUserEntity info = new RelatedUserEntity();
                        info.setmRelatedId(cursor.getString(cursor.getColumnIndex("fansId")));
                        info.setmRelatedImg(cursor.getString(cursor.getColumnIndex("headImg")));
                        relatedUserEntities.add(info);
                    } while (cursor.moveToNext());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            if (db != null) {
                db.close();
            }
        }
        return relatedUserEntities;
    }
}
