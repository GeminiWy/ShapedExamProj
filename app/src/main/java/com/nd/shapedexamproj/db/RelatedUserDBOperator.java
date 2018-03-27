package com.nd.shapedexamproj.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.nd.shapedexamproj.App;
import com.nd.shapedexamproj.entity.RelatedUserEntity;
import com.nd.shapedexamproj.im.model.CommunicationItemInfo;
import com.nd.shapedexamproj.im.model.PersonSearchResult;
import com.tming.common.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>  RelatedUserDBOperator 用户关注关系数据库操作类 </p>
 * <p> Created by xuwenzhuo on 2014/11/23.</p>
 */
public class RelatedUserDBOperator {

    private static RelatedUserDBOperator instance = null;
    private OpenUniversityOpenHelper helper;
    //用户id
    private String mUserId;
    private static final int OP_ERROR=0,OP_SUCCESS=1,OP_INSERT=2,OP_DELETE=3,OP_UPDATE=4,OP_LOCK=-1;


    public RelatedUserDBOperator(Context context) {
        helper = new OpenUniversityOpenHelper(context);
        mUserId = App.getUserId();
    }

    public static RelatedUserDBOperator getInstance(Context context) {

        if (instance != null) {
            if (instance.mUserId.equals(App.getUserId())) {
                return instance;
            } else {
                instance = null;
            }
        }
        synchronized (VideoDownloadDBOperator.class) {
            if (instance == null) {
                instance = new RelatedUserDBOperator(context);
            }
        }
        return instance;
    }

    /**
     * <p>插入用户关系记录</p>
     * @param relatedUserEntity 关系实体对象
     * @return
     */
    public  synchronized int insertRelationShipWithRelatedObject(RelatedUserEntity relatedUserEntity){

        synchronized (helper.getDbLock()){
            SQLiteDatabase db = null;
            ContentValues cv = null;
            try{
                db = helper.getWritableDatabase();
                cv = new ContentValues();
                cv.put("userid", mUserId);
                cv.put("username", mUserId);
                cv.put("relatedid", relatedUserEntity.getmRelatedId());
                cv.put("relatedusername", relatedUserEntity.getmRelatedUserName());
                cv.put("type", relatedUserEntity.getmType());
                db.insert("usersrelationship", null, cv);
                Log.e("插入", "用户关系数据表=====");
            }catch(Exception e){
                e.printStackTrace();
                return OP_ERROR;
            }
            return OP_INSERT;
        }
    }

    /**
     * <p>插入用户关系记录</p>
     * @param communicationItemInfo 信息交互实体对象
     * @return
     */
    public  synchronized int insertRelationShipWithCommuObject(CommunicationItemInfo communicationItemInfo){

        synchronized (helper.getDbLock()){
            SQLiteDatabase db = null;
            ContentValues cv = null;
            try{
                db = helper.getWritableDatabase();
                String relatedId=communicationItemInfo.followId.equals(App.getUserId())?communicationItemInfo.userid:communicationItemInfo.followId;
                cv = new ContentValues();
                cv.put("userid", App.getUserId());
                cv.put("username", "");
                cv.put("relatedid", relatedId);
                cv.put("relatedusername", communicationItemInfo.getName());
                cv.put("type", communicationItemInfo.getSortKey());
                db.insert("usersrelationship", null, cv);
                Log.e("插入", "用户关系数据表=====");
            }catch(Exception e){
                e.printStackTrace();
                return OP_ERROR;
            }
            return OP_INSERT;
        }
    }

    /**
     * <p>保存用户关系到数据库</p>
     * @param personSearchResult 搜索到的用户对象
     * @return
     */
    public  synchronized int insertRelationShipWithSearchPersonObject(PersonSearchResult personSearchResult){

        synchronized (helper.getDbLock()){
            SQLiteDatabase db = null;
            ContentValues cv = null;
            try{
                db = helper.getWritableDatabase();
                String relatedId= personSearchResult.getUserId();
                cv = new ContentValues();
                cv.put("userid", App.getUserId());
                cv.put("username", "");
                cv.put("relatedid", relatedId);
                cv.put("relatedusername", personSearchResult.getUserName());
                cv.put("type", personSearchResult.getIsAdded());
                db.insert("usersrelationship", null, cv);
                Log.e("插入", "用户关系数据表=====");
            }catch(Exception e){
                e.printStackTrace();
                return OP_ERROR;
            }
            return OP_INSERT;
        }
    }

    /**
     * <p>保存用户关系到数据库</p>
     * @param personSearchResult 搜索到的用户对象
     * @return
     */
    public  synchronized int deleteRelationShipWithSearchPersonObject(PersonSearchResult personSearchResult){

        synchronized (helper.getDbLock()) {
            SQLiteDatabase db = null;
            try{
                db = helper.getWritableDatabase();
                db.execSQL("delete from usersrelationship where relatedid = '"+personSearchResult.getUserId()+"'");
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
     * <p>根据用户id 获取关注关系 </p>
     * @param relatedUserid 关联用户id
     * @return
     */
    public synchronized RelatedUserEntity getRelatedByUserId(String relatedUserid){

        SQLiteDatabase db = null;
        RelatedUserEntity relatedUserEntity=null;
        Cursor cursor = null;
        try{
            db = helper.getWritableDatabase();
            cursor = db.rawQuery("select distinct relatedid,relatedusername,type from usersrelationship where relatedid = '?'", new String[]{relatedUserid});
            if(cursor.moveToFirst()){
                relatedUserEntity= new RelatedUserEntity();
                relatedUserEntity.setmUserId(mUserId);
                relatedUserEntity.setmRelatedId( cursor.getString(cursor.getColumnIndex("relatedid")));
                relatedUserEntity.setmRelatedUserName(cursor.getString(cursor.getColumnIndex("relatedusername")));
                relatedUserEntity.setmType(cursor.getInt(cursor.getColumnIndex("type")));
            }
        }catch (Exception e){
            e.printStackTrace();
        } finally{
            if(cursor!=null){
                cursor.close();
            }
            if(db != null) db.close();
        }
        return relatedUserEntity;
    }

    /**
     * <p>根据用户id 查询添加的好友对象</p>
     * @param userid
     * @return
     */
    public synchronized List<RelatedUserEntity> queryRelatedUsersWithId(String userid){
        List<RelatedUserEntity> relatedUserEntities=new ArrayList<RelatedUserEntity>();
        SQLiteDatabase db = null;
        Cursor cursor = null;
        try {
            db = helper.getWritableDatabase();
            cursor = db.rawQuery("select * from usersrelationship where userid = '?'",
                    new String[] { String.valueOf(userid) });
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    do {
                        RelatedUserEntity info = new RelatedUserEntity();
                        info.setmUserId(mUserId);
                        info.setmRelatedId(cursor.getString(cursor.getColumnIndex("relatedid")));
                        info.setmRelatedUserName(cursor.getString(cursor.getColumnIndex("relatedusername")));
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

    /**
     * <p> 根据关注用户的id删除相关记录 </p>
     * @param relatedId
     * @return
     */
    public int deleteRelatedObjectById(String relatedId){

        synchronized (helper.getDbLock()) {
            SQLiteDatabase db = null;
            try{
                db = helper.getWritableDatabase();
                //TODO 这个类中的所有 ? 报错，改成了'?'
                db.execSQL("delete from usersrelationship where relatedid = '?'",new String[]{relatedId});
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
     * <p>删除所有关注记录</p>
     * @return
     */
    public int clearAllRelatedRecords(){

        synchronized (helper.getDbLock()) {
            SQLiteDatabase db = null;
            try{
                db = helper.getWritableDatabase();
                db.execSQL("delete from usersrelationship where relatedid <> ''");
            }catch(Exception e){
                e.printStackTrace();
                return OP_ERROR;
            }finally{
                if (db != null) db.close();
                return OP_SUCCESS;
            }
        }
    }

}
