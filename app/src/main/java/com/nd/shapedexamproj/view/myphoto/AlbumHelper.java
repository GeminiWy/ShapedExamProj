package com.nd.shapedexamproj.view.myphoto;

import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore.Images.Media;

import java.util.ArrayList;

/**
 * 相册帮助类
 */
public class AlbumHelper {

	private static final String TAG = "AlbumHelper";

    /**
     * 获取所有的图片文件路径（对ID进行倒序）
     *
     * @param context
     * @return
     */
    public static ArrayList<String> getAlbumImages(Context context) {
        String columns[] = new String[]{ Media._ID, Media.DATA };
        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(Media.EXTERNAL_CONTENT_URI, columns, null, null, Media._ID + " DESC");
            ArrayList<String> images = new ArrayList<String>();
            if (cursor != null && cursor.moveToFirst()) {
                int photoPathIndex = cursor.getColumnIndexOrThrow(Media.DATA);
                do {
                    images.add(cursor.getString(photoPathIndex));
                } while (cursor.moveToNext());
            }
            return images;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    /**
     * The orientation for the image expressed as degrees. Only degrees 0, 90, 180, 270 will work.
     *
     * @param context
     * @param capturePath
     * @return 0, 90, 180, 270
     */
    public static int getImageOrientation(Context context, String capturePath) {
        String columns[] = new String[]{ Media._ID, Media.DATA, Media.ORIENTATION};
        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(Media.EXTERNAL_CONTENT_URI, columns, Media.DATA + "= ?", new String[]{capturePath}, null);
            if (cursor != null && cursor.moveToFirst()) {
                int orientationIndex = cursor.getColumnIndexOrThrow(Media.ORIENTATION);
                return cursor.getInt(orientationIndex);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return 0;
    }

}
