package com.tming.common.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.format.Time;
import android.widget.Toast;
import com.tming.common.R;

import java.io.File;
import java.io.IOException;

/**
 * <p> 调用Action的辅助类 </p>
 * <p>Created by yusongying on 2014/10/28</p>
 */
public class ActionUtil {

    /**
     * 启动安装
     *
     * @param context android上下文
     * @param apkFile apk文件
     */
    public static void startInstallApk(Context context, File apkFile) {
        Intent installIntent = new Intent(Intent.ACTION_VIEW);
        Uri uri = Uri.fromFile(apkFile);
        installIntent.setDataAndType(uri, "application/vnd.android.package-archive");
        context.startActivity(installIntent);
    }

    /**
     * 打开浏览器
     *
     * @param context android上下文
     * @param urlString url地址
     */
    public static void openBrowse(Context context, String urlString) {
        Uri uri = Uri.parse(urlString);
        Intent it = new Intent(Intent.ACTION_VIEW, uri);
        context.startActivity(it);
    }

    /**
     * 多媒体相关的Action调用
     */
    public static class Media {

        /**
         * 打开照相机拍照
         *  @param activity 所在的Activity
         * @param quality 拍照的质量，（0-1）之间
         * @param requestCode 请求代码
         * @return 拍照完成产生的图片文件
         */
        public static File captureImage(Activity activity, int quality, int requestCode) {
            try {
                File captureFile = getMediaStoreFile();
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                Uri uri = Uri.fromFile(captureFile);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
                intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, quality);
                activity.startActivityForResult(Intent.createChooser(intent, activity.getString(R.string.action_choose_camera)), requestCode);
                return captureFile;
            } catch (IOException e) {
                e.printStackTrace();
                Helper.ToastUtil(activity, activity.getString(R.string.open_camera_no_sdcard_hit));
            }
            return null;
        }

        /**
         * 获取拍照存储文件
         *
         * @return 拍照文件
         * @throws IOException
         */
        private static File getMediaStoreFile() throws IOException {
            // 判断sd卡是否存在
            boolean sdCardExist = Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED);
            if (!sdCardExist) {
                throw new IOException("no sdcard.");
            }
            File dir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), "Camera");
            if (!dir.exists() || !dir.isDirectory()) {
                dir.mkdirs();
            }
            Time time = new Time();
            time.setToNow();
            File file = new File(dir, "IMG_" + time.format("%Y%m%d_%H%M%S") + ".jpg");
            return file;
        }

        /**
         * 在相册中查看图片
         * @param context android上下文
         * @param imagePath 图片路径
         */
        public static void viewPictureOnAlbum(Context context, File imagePath) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.addCategory(Intent.CATEGORY_DEFAULT);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            Uri uri = Uri.fromFile(imagePath);
            intent.setDataAndType(uri, "image/*");
            context.startActivity(intent);
        }

    }

}
