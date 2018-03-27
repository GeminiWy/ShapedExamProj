package com.nd.shapedexamproj.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.nd.shapedexamproj.App;
import com.nd.shapedexamproj.model.download.DownloadInfo;
import com.tming.common.util.Log;

import java.util.ArrayList;
import java.util.List;
/**
 * 下载数据库操作
 * @author Linlg
 *
 */
public class VideoDownloadDBOperator {
	public OpenUniversityOpenHelper helper;
	private static VideoDownloadDBOperator operator;
	private String mUserId;
	
	public static VideoDownloadDBOperator getInstance(Context context) {
	    if (operator != null) {
    	    if (operator.mUserId.equals(App.getUserId())) {//上个用户id和当前用户id比较，避免当前用户操作上个用户的数据库。（使用static要注意）
                return operator;
            } else {
                operator = null;
            }
	    }
        synchronized (VideoDownloadDBOperator.class) {
            if (operator == null) {
                operator = new VideoDownloadDBOperator(context);
            }
        }
        return operator;
	}
	
	
	public VideoDownloadDBOperator(Context context){
		helper = new OpenUniversityOpenHelper(context);
		mUserId = App.getUserId();
	}
	
	public void insert(DownloadInfo info){
		synchronized (helper.getDbLock()) {
			SQLiteDatabase db = null;
			ContentValues cv = null;
			try {
				db = helper.getWritableDatabase();
				cv = new ContentValues();
				cv.put("url", info.url);
				cv.put("coursecateid", info.coursecateid);
				cv.put("coursecatename", info.coursecatename);
				cv.put("courseid", info.courseId);
				cv.put("videoname", info.title);
				cv.put("videoid", info.videoId);
				db.insert("videodownload", null, cv);
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				if (db != null) {
					db.close();
				}
			}
		}
	}
	
	public synchronized void deleteByUrl(String url){
		SQLiteDatabase db = null;
		try {
			db = helper.getWritableDatabase();
			db.execSQL("delete from videodownload where url = '?'", new String[]{url});
			Log.e("VideoDownloadDBOperator", "====删除一条下载记录====");
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (db != null)
				db.close();
		}
	}
	
	public synchronized boolean isUrlExist(String url) {
		SQLiteDatabase db = null;
		Cursor cursor = null;
		try {
			db = helper.getWritableDatabase();
			// TODO wy 2018年3月22日17:11:44 ?报错，改成了'?'
			cursor = db.rawQuery("select * from videodownload where url='?'", new String[]{url});
			return cursor.moveToFirst();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if(cursor != null) {
				cursor.close();
			}
			if (db != null) {
				db.close();
			}
		}
		return false;
	}
	
	public synchronized List<DownloadInfo> getListByCourseId(int coursecateId) {
		List<DownloadInfo> downloadInfos = new ArrayList<DownloadInfo>();
		SQLiteDatabase db = null;
		Cursor cursor = null;
		try {
			db = helper.getWritableDatabase();
			cursor = db.rawQuery("select * from videodownload where coursecateid = '?'",
					new String[] { String.valueOf(coursecateId) });
			if (cursor != null) {
				if (cursor.moveToFirst()) {
					do {
						DownloadInfo info = new DownloadInfo();
						info.url = cursor.getString(cursor.getColumnIndex("url"));
						info.coursecateid = cursor.getInt(cursor.getColumnIndex("coursecateid"));
						info.coursecatename = cursor.getString(cursor.getColumnIndex("coursecatename"));
						info.title = cursor.getString(cursor.getColumnIndex("videoname"));
						info.courseId = cursor.getString(cursor.getColumnIndex("courseid"));
						info.videoId = cursor.getString(cursor.getColumnIndex("videoid"));
						downloadInfos.add(info);
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
		return downloadInfos;
	}
	
	public List<DownloadInfo> getDownloads() {
		List<DownloadInfo> downloadInfos = new ArrayList<DownloadInfo>();
		SQLiteDatabase db = null;
		Cursor cursor = null;
		synchronized (helper.getDbLock()) {
			try {
				db = helper.getWritableDatabase();
				cursor = db.rawQuery("select * from videodownload", null);
				if (cursor != null) {
					if (cursor.moveToFirst()) {
						do {
							DownloadInfo info = new DownloadInfo();
							info.url = cursor.getString(cursor.getColumnIndex("url"));
							info.coursecateid = cursor.getInt(cursor.getColumnIndex("coursecateid"));
							info.coursecatename = cursor.getString(cursor.getColumnIndex("coursecatename"));
							info.title = cursor.getString(cursor.getColumnIndex("videoname"));
							info.courseId = cursor.getString(cursor.getColumnIndex("courseid"));
							info.videoId = cursor.getString(cursor.getColumnIndex("videoid"));
							downloadInfos.add(info);
						} while (cursor.moveToNext());
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				if (cursor != null)
					cursor.close();
				if (db != null) db.close();
			}
		}
		return downloadInfos;
	}

}
