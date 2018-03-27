package com.tming.common.util;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.*;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import com.tming.common.CommonApp;

import java.io.*;

/**
 * <p> 对图片操作帮助类</p>
 * <p>Created by yusongying on 2014/10/16</p>
 */
public class BitmapUtil {

    /**
     * <p>从资源中读取图片</p>
     * <strong>请注意内存溢出异常 {@link java.lang.OutOfMemoryError}</strong>
     *
     * @param resId 图片资源ID
     * @return 读取后的图片
     */
    public static Bitmap loadImageFromResource(int resId) {
        return loadImageFromResource(resId, -1, -1);
    }

    /**
     * <p>从资源中读取图片</p>
     * <strong>请注意内存溢出异常 {@link java.lang.OutOfMemoryError}</strong>
     *
     * @param resId 图片资源ID
     * @param reqWidth 指定宽度
     * @param reqHeight 指定高度
     * @return 读取后的图片
     */
    public static Bitmap loadImageFromResource(int resId, int reqWidth, int reqHeight) {
        if (reqWidth > 0 && reqHeight > 0) {
            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeResource(CommonApp.getAppContext().getResources(), resId, options);

            // 计算取样值
            options.inSampleSize = calculateInSampleSize(options, reqWidth,
                    reqHeight);

            // 根据取样值加载图片文件
            options.inJustDecodeBounds = false;
            return BitmapFactory.decodeResource(CommonApp.getAppContext().getResources(), resId, options);
        }
        return BitmapFactory.decodeResource(CommonApp.getAppContext().getResources(), resId);
    }

    /**
     * <p>从文件中读取图片</p>
     * <strong>请注意内存溢出异常 {@link java.lang.OutOfMemoryError}</strong>
     *
     * @param file 图片文件
     * @return 读取后的图片
     */
    public static Bitmap loadImage(File file) {
        return loadImage(file, -1, -1);
    }

    /**
     * <p>以指定大小进行读取图片</p>
     * <strong>请注意内存溢出异常 {@link java.lang.OutOfMemoryError}</strong>
     *
     * @param file 图片文件
     * @param reqWidth 指定宽度
     * @param reqHeight 指定高度
     * @return 读取后的图片
     */
    public static Bitmap loadImage(File file, int reqWidth, int reqHeight) {
        return loadImage(file, reqWidth, reqHeight, false);
    }

    /**
     * <p>以指定大小进行读取图片</p>
     * <strong>请注意内存溢出异常 {@link java.lang.OutOfMemoryError}</strong>
     *
     * @param file 图片文件
     * @param reqWidth 指定宽度
     * @param reqHeight 指定高度
     * @param adjustOritation 是否自动旋转方向
     * @return 读取后的图片
     */
    public static Bitmap loadImage(File file, int reqWidth, int reqHeight, boolean adjustOritation) {
        // 通过 inJustDecodeBounds=true 获取到图片的大小
        Bitmap bitmap = null;
        if (reqWidth > 0 && reqHeight > 0) {
            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(file.getPath(), options);

            // 计算取样值
            options.inSampleSize = calculateInSampleSize(options, reqWidth,reqHeight);

            // 根据取样值加载图片文件
            options.inJustDecodeBounds = false;

            bitmap = BitmapFactory.decodeFile(file.getPath(), options);
        } else {
            bitmap = BitmapFactory.decodeFile(file.getPath());
        }

        if (adjustOritation) {
            try {
                ExifInterface exifInterface = new ExifInterface(file.getPath());
                int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);
                int degree = 0;
                // 计算旋转角度
                switch (orientation) {
                    case ExifInterface.ORIENTATION_ROTATE_90:
                        degree = 90;
                        break;
                    case ExifInterface.ORIENTATION_ROTATE_180:
                        degree = 180;
                        break;
                    case ExifInterface.ORIENTATION_ROTATE_270:
                        degree = 270;
                        break;
                }
                if (degree > 0) {
                    Bitmap rotateBitmap = rotateBitmap(bitmap, degree);
                    return rotateBitmap;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return bitmap;
    }

    /**
     * <p>计算图片按指定宽高加载的取样值</P>
     *
     * @param options 通过inJustDecodeBounds==true 编码，能够获取到outWidth，outHeight的options
     * @param reqWidth 指定的宽度
     * @param reqHeight 指定的高度
     * @return 取样值
     */
    private static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;
        if (height > reqHeight || width > reqWidth) {
            // 计算出实际宽高和目标宽高的比率
            final int heightRatio = Math.round((float) height / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);
            inSampleSize = Math.max(widthRatio, heightRatio);
        }
        return inSampleSize;
    }

    /**
     * 以JPG保存图片到文件
     * @param bitmap 图片Bitmap对象
     * @param file 保存到的文件
     * @throws IOException
     */
    public static void saveBitmap2JPGFile(Bitmap bitmap, File file) throws IOException {
        saveImage2File(bitmap, file, Bitmap.CompressFormat.JPEG);
    }

    /**
     * 以PNG保存图片到文件
     * @param bitmap 图片Bitmap对象
     * @param file 保存到的文件
     * @throws IOException
     */
    public static void saveBitmap2PNGFile(Bitmap bitmap, File file) throws IOException {
        saveImage2File(bitmap, file, Bitmap.CompressFormat.PNG);
    }

    /**
     * 以PNG保存图片到文件
     * @param bitmap 图片Bitmap对象
     * @param file 保存到的文件
     * @throws IOException
     */
    @TargetApi(14)
    public static void saveBitmap2WEBPFile(Bitmap bitmap, File file) throws IOException {
        saveImage2File(bitmap, file, Bitmap.CompressFormat.WEBP);
    }

    private static void saveImage2File(Bitmap bitmap, File file, Bitmap.CompressFormat format) throws IOException {
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file);
            bitmap.compress(format, 100, fos);// 把数据写入文件
            fos.flush();
        } finally {
            try {
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 压缩图片为JPG
     *
     * @param imageFile 图片文件
     * @param reduceQuality 是否减小图片质量
     * @param maxSize 图片文件的最大大小（单位KB）
     * @param maxWH 图片的最大宽高
     * @param adjustOritation 是否自动旋转方向
     * @return 压缩后的图片文件（如果图片已经小于最大值，则会返回原文件）
     * @throws IOException
     */
    private static File compressImageFile(File imageFile, boolean reduceQuality, long maxSize, int maxWH, boolean adjustOritation) throws IOException {
        if (imageFile.length() < maxSize * 1024 && !adjustOritation) {
            return imageFile;
        }
        File compressedFile = FileUtil.createTempFile("image", ".jpg");
        FileUtil.copyFile(imageFile, compressedFile);
        do {
            Bitmap bitmap = loadImage(compressedFile, maxWH, maxWH, adjustOritation);
            if (reduceQuality) {
                compressImageWithLowerQuality(bitmap, compressedFile, 80);
            } else {
                saveBitmap2JPGFile(bitmap, compressedFile);
            }
            bitmap.recycle();
            maxWH = maxWH >> 1;// 大小缩小一倍
        } while (compressedFile.length() > maxSize * 1024);
        return compressedFile;
    }

    /**
     * 压缩图片为JPG
     *
     * @param imageFile 图片文件
     * @param reduceQuality 是否减小图片质量
     * @param maxSize 图片文件的最大大小（单位KB）
     * @return 压缩后的图片文件（如果图片已经小于最大值，则会返回原文件）
     * @throws IOException
     */
    public static File compressImageFile(File imageFile, boolean reduceQuality, long maxSize) throws IOException {
        return compressImageFile(imageFile, reduceQuality, maxSize, 1024, false);
    }

    /**
     * 压缩图片为JPG
     *
     * @param imageFile 图片文件
     * @param reduceQuality 是否减小图片质量
     * @param maxSize 图片文件的最大大小（单位KB）
     * @return 压缩后的图片文件（如果图片已经小于最大值，则会返回原文件）
     * @throws IOException
     */
    public static File compressImageFile(File imageFile, boolean reduceQuality, long maxSize, int maxWH) throws IOException {
        return compressImageFile(imageFile, reduceQuality, maxSize, maxWH, false);
    }

    /**
     * 压缩图片为JPG,并自动修复图片方向
     *
     * @param imageFile 图片文件
     * @param reduceQuality 是否减小图片质量
     * @param maxSize 图片文件的最大大小（单位KB）
     * @return 压缩后的图片文件（如果图片已经小于最大值，则会返回原文件）
     * @throws IOException
     */
    public static File compressImageFileAndFixOritation(File imageFile, boolean reduceQuality, long maxSize, int maxWH) throws IOException {
        return compressImageFile(imageFile, reduceQuality, maxSize, maxWH, true);
    }

    /**
     * 按照指定质量压缩图片到JPG文件中
     *
     * @param bitmap 图片对象
     * @param imageFile 文件
     * @param quality 压缩质量
     * @throws IOException
     */
    public static void compressImageWithLowerQuality(Bitmap bitmap, File imageFile, int quality) throws IOException {
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(imageFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, fos);
            fos.flush();
        } finally {
            try {
                if (fos != null) fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 图片压缩方法：（使用compress的方法） <br>
     *
     * <b>注意</b> bitmap实际并没有被回收，如果你不需要，请手动置空 <br>
     * <b>说明</b> 如果bitmap本身的大小小于maxSize，则不作处理
     *  @param bitmap 要压缩的图片
     * @param maxSize 压缩后的大小，单位kb
     * @return 压缩后的图片
     */
    public static Bitmap imageZoom(Bitmap bitmap, double maxSize) {
        // 将bitmap放至数组中，意在获得bitmap的大小（与实际读取的原文件要大）
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        // 格式、质量、输出流
        bitmap.compress(Bitmap.CompressFormat.PNG, 70, baos);
        byte[] b = baos.toByteArray();
        // 将字节换成KB
        double mid = b.length / 1024;
        // 获取bitmap大小 是允许最大大小的多少倍
        double i = mid / maxSize;
        // 判断bitmap占用空间是否大于允许最大空间 如果大于则压缩 小于则不压缩
        if (i > 1) {
            // 缩放图片 此处用到平方根 将宽带和高度压缩掉对应的平方根倍
            // （保持宽高不变，缩放后也达到了最大占用空间的大小）
            return scaleWithWH(bitmap, bitmap.getWidth() / Math.sqrt(i), bitmap.getHeight() / Math.sqrt(i));
        }
        return bitmap;
    }

    /***
     * 图片的缩放方法,如果参数宽高为0,则不处理<br>
     *
     * <b>注意</b> src实际并没有被回收，如果你不需要，请手动置空
     *
     * @param src
     *            ：源图片资源
     * @param w
     *            ：缩放后宽度
     * @param h
     *            ：缩放后高度
     */
    public static Bitmap scaleWithWH(Bitmap src, double w, double h) {
        if (w == 0 || h == 0 || src == null) {
            return src;
        } else {
            // 记录src的宽高
            int width = src.getWidth();
            int height = src.getHeight();
            // 创建一个matrix容器
            Matrix matrix = new Matrix();
            // 计算缩放比例
            float scaleWidth = (float) (w / width);
            float scaleHeight = (float) (h / height);
            // 开始缩放
            matrix.postScale(scaleWidth, scaleHeight);
            // 创建缩放后的图片
            return Bitmap.createBitmap(src, 0, 0, width, height, matrix, true);
        }
    }

    /**
     * 图片的缩放方法<br>
     *
     * <b>注意</b> src实际并没有被回收，如果你不需要，请手动置空
     *
     * @param src
     *            ：源图片资源
     * @param scaleMatrix
     *            ：缩放规则
     */
    public static Bitmap scaleWithMatrix(Bitmap src, Matrix scaleMatrix) {
        return Bitmap.createBitmap(src, 0, 0, src.getWidth(), src.getHeight(), scaleMatrix, true);
    }

    /**
     * 图片的缩放方法<br>
     *
     * <b>注意</b> src实际并没有被回收，如果你不需要，请手动置空
     *
     * @param src
     *            ：源图片资源
     * @param scaleX
     *            ：横向缩放比例
     * @param scaleY
     *            ：纵向缩放比例
     */
    public static Bitmap scaleWithXY(Bitmap src, float scaleX, float scaleY) {
        Matrix matrix = new Matrix();
        matrix.postScale(scaleX, scaleY);
        return Bitmap.createBitmap(src, 0, 0, src.getWidth(), src.getHeight(), matrix, true);
    }

    /**
     * 图片的缩放方法<br>
     *
     * <b>注意</b> src实际并没有被回收，如果你不需要，请手动置空
     *
     * @param src
     *            ：源图片资源
     * @param scaleXY
     *            ：缩放比例
     */
    public static Bitmap scaleWithXY(Bitmap src, float scaleXY) {
        return scaleWithXY(src, scaleXY, scaleXY);
    }

    /**
     * <p>旋转图片</p>
     * <p>请注意内存溢出异常 {@link java.lang.OutOfMemoryError}</p>
     * @param bm 图片Bitmap对象
     * @param orientationDegree 旋转的角度
     * @return 旋转后的图片
     */
    public static Bitmap rotateBitmap(Bitmap bm, int orientationDegree) {
        Matrix m = new Matrix();
        m.postRotate(orientationDegree);
        return Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), m, true);
    }

    /**
     * <p>将Bitmap转化为灰色图片</p>
     * <p>请注意内存溢出异常 {@link java.lang.OutOfMemoryError}</p>
     * @param bitmap 图片Bitmap对象
     * @return 灰色图片
     */
    public static Bitmap toGrayImage(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        Bitmap grayImg = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(grayImg);
        Paint paint = new Paint();
        ColorMatrix colorMatrix = new ColorMatrix();
        colorMatrix.setSaturation(0);
        ColorMatrixColorFilter colorMatrixFilter = new ColorMatrixColorFilter(colorMatrix);
        paint.setColorFilter(colorMatrixFilter);
        canvas.drawBitmap(bitmap, 0, 0, paint);
        return grayImg;
    }

    /**
     * 回收一个未被回收的Bitmap
     *
     * @param bitmap
     */
    public static void doRecycledIfNot(Bitmap bitmap) {
        if (!bitmap.isRecycled()) {
            bitmap.recycle();
        }
    }

    /**
     * 获取Bitmap的大小
     * @param bitmap
     * @return Bitmap所占用空间的大小
     */
    public static long getBitmapByteCount(Bitmap bitmap) {
        if (Build.VERSION.SDK_INT >= 19) {
            return bitmap.getAllocationByteCount();
        } else {
            return bitmap.getRowBytes() * bitmap.getHeight();
        }
    }

    /**
     * 获取图片文件中图片的尺寸
     * @param file 图片文件
     * @return 图片的尺寸
     */
    public static int[] getBitmapSizeWithFile(File file) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(file.getPath(), options);
        return new int[]{options.outWidth, options.outHeight};
    }
}
