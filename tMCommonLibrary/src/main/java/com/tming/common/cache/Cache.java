package com.tming.common.cache;

import java.io.File;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import android.os.*;
import com.tming.common.CommonApp;
import com.tming.common.thread.TMExcutors;
import com.tming.common.util.Helper;
import com.tming.common.util.Log;

import android.content.Context;

/**
 * 缓存管理类
 */
public class Cache {

    private static final String TAG = "Cache";
    protected static final int MB = 1024 * 1024;
    protected static final int LOW_FREE_SPACE_LIMIT = 5; //SD卡最低大小，单位MB
    protected static final int SDCARD_CACHE_SIZE = 256; //缓存空间中，已缓存的空间上限，单位MB
    protected static final int INNER_CACHER_SIZE = 16;
    //protected static final long MTIMEDIFF = 3600 * 24 * 3; //保存时间，保存三天
    protected static final String WHOLESALE_CONV = ".cache"; //缓存图片名称的后缀
    protected static final String ALL_PACKAGE_CACHE_DIR = "data/";
    protected static final String DIR_IMAGE_CACHE = "image";
    protected static final String DIR_DATA_CACHE = "data";
    public static final int CACHE_TYPE_IMAGE = 1;
    public static final int CACHE_TYPE_DATA = 2;
    public static final int CACHE_TYPE_NOT_CLEAR = 3;
    public static final int DELETE_FILE = 123;


    /**
     * 标识是否有线程正在回收文件
     */
    private volatile boolean isRemoving = false;

    /**
     * 移除线程的同步锁
     */
    private Object mRemoveLock = new Object();

    /**
     * 上下文
     */
    protected Context mContext;

    /**
     * 缓存目录
     */
    protected String mCacheDir;


    protected Handler mHandler = null;


    public Cache(Context context) {
        mContext = context;
        mCacheDir = ALL_PACKAGE_CACHE_DIR + context.getPackageName();
        mHandler = new Handler();
    }

    /**
     * 计算SD卡的剩余空间
     *
     * @return SD卡剩余空间（单位M）
     */
    public int freeSpaceOnSd() {
        StatFs stat = new StatFs(Environment.getExternalStorageDirectory().getPath());
        double sdFreeMB = 0;
        if (Build.VERSION.SDK_INT >= 18) {
            sdFreeMB = stat.getAvailableBytes() / MB;
        } else {
            sdFreeMB = ((long) stat.getAvailableBlocks() * stat.getBlockSize()) / MB;
        }
        return (int) sdFreeMB;
    }

    /**
     * 内部存储环境大小
     * @return
     */
    public int freeSpaceOnInner() {
        StatFs stat = new StatFs(Environment.getRootDirectory().getPath());
        double sdFreeMB;
        if (Build.VERSION.SDK_INT >= 18) {
            sdFreeMB = stat.getAvailableBytes() / MB;
        } else {
            sdFreeMB = ((long) stat.getAvailableBlocks() * stat.getBlockSize()) / MB;
        }
        return (int) sdFreeMB;
    }

    /**
     * 判断SD卡是否存在
     * @return true 存在，否则不存在
     */
    public boolean isSDCardExist() {
        return Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED);
    }

    /**
     * 取SD卡路径不带/
     *
     * @return
     */
    private String getSDPath() {
        File sdDir = null;
        boolean sdCardExist = Environment.getExternalStorageState().equals(
                android.os.Environment.MEDIA_MOUNTED); // 判断SD卡是否存在
        if (sdCardExist) {
            sdDir = Environment.getExternalStorageDirectory();// 获取根目录
        }
        if (sdDir != null) {
            return sdDir.toString();
        } else {
            return null;
        }
    }

    /**
     * 获得缓存目录
     *
     * @param type 文件类型
     * @return 缓存的目录路径
     */
    public File getCacheDirectory(int type) {
        //目录不存在，则创建目录
        String cacheDirName = null;
        switch (type) {
            case CACHE_TYPE_DATA:
                cacheDirName = DIR_DATA_CACHE;
                break;
            case CACHE_TYPE_IMAGE:
                cacheDirName = DIR_IMAGE_CACHE;
                break;
            default:
                cacheDirName = "";
        }
        File dirFile = null;
        if (isSDCardExist()) {
            String dirPath = getSDPath() + "/" + mCacheDir + "/" + cacheDirName;
            dirFile = new File(dirPath);
        } else {
            dirFile = new File(CommonApp.getAppContext().getCacheDir(), cacheDirName);
        }
        if (dirFile.exists() && !dirFile.isDirectory()) {
            dirFile.delete();
            dirFile.mkdirs();
            Log.e(TAG, "delete file:" + dirFile.getPath() + ", create dir");
        } else if (!dirFile.exists()) {
            Log.e(TAG, "create dir:" + dirFile.getPath());
            dirFile.mkdirs();
        }
        return dirFile;
    }

    /**
     * 清除缓存<br/>
     * <p>启动时会有一个线程进行清除，每删除一个文件会往 obsHandler 中发送消息<br/>
     * 消息 what = {@link com.tming.common.cache.Cache#DELETE_FILE},包含 file 和 fileSize 信息，分别为文件路径和文件大小</p>
     */
    public void clearAllCache(final Handler obsHandler) {
        new Thread() {

            @Override
            public void run() {
                super.run();
                boolean sdCardExist = Environment.getExternalStorageState().equals(
                        android.os.Environment.MEDIA_MOUNTED); // 判断SD卡是否存在
                if (sdCardExist) {
                    deleteDir(getCacheDirectory(CACHE_TYPE_IMAGE), obsHandler);
                    deleteDir(getCacheDirectory(CACHE_TYPE_DATA), obsHandler);
                }
                deleteDir(CommonApp.getAppContext().getCacheDir(), obsHandler);
            }

        }.start();
    }

    private void deleteDir(File dir, Handler obsHandler) {
        File[] files = dir.listFiles();
        if (files == null) {
            return;
        }
        for (File file : files) {
            if (file == null) continue;
            if (file.isDirectory()) {
                deleteDir(file, obsHandler);
            }
            if (obsHandler != null) {
                Message message = obsHandler.obtainMessage();
                message.what = DELETE_FILE;
                Bundle bundle = new Bundle();
                bundle.putString("file", file.getPath());
                bundle.putLong("fileSize", file.length());//add by lpf:2013-9-9添加文件大小
                message.setData(bundle);
                obsHandler.sendMessage(message);
            }
            file.delete();
        }
    }

    /**
     * 将url转成文件名
     *
     * @param url
     * @return
     */
    public String convertUrlToFileName(String url) {
        return Helper.getMD5String(url) + WHOLESALE_CONV;
    }

    /**
     * 计算存储目录下的文件大小，当文件总大小大于规定的CACHE_SIZE或者SD卡剩余空间小于FREE_SD_SPACE_NEEDED_TO_CACHE的规定
     * 那么删除40%最近没有被使用的文件
     */
    public void freeSomeCacheSpace() {
        if (!isRemoving) {
            synchronized (mRemoveLock) {
                isRemoving = true;
                long dirSize = getCachedSize();
                if (isSDCardExist() && (SDCARD_CACHE_SIZE * MB < dirSize || LOW_FREE_SPACE_LIMIT > freeSpaceOnSd())) {
                    autoDeleteSomeCacheFile();
                } else if (INNER_CACHER_SIZE * MB < dirSize || LOW_FREE_SPACE_LIMIT > freeSpaceOnInner()) {// 清除内部缓存
                    autoDeleteSomeCacheFile();
                }
                isRemoving = false;
            }
        }
    }

    /**
     * 自动清除40%缓存文件
     */
    private void autoDeleteSomeCacheFile() {
        List<File> files = new ArrayList<File>();
        recurGetCacheFiles(getCacheDirectory(CACHE_TYPE_IMAGE), files);
        recurGetCacheFiles(getCacheDirectory(CACHE_TYPE_DATA), files);
        int fileCount = files.size();
        int removeFactor = (int) ((0.4 * fileCount) + 1);
        try {
            Collections.sort(files, new FileLastModifSort());
        } catch (IllegalArgumentException e) {
            Log.d(TAG, e.getMessage());
        }
        comparatorFiles.clear();
        Log.i(TAG, "Clear 40% cache files for new cache.");
        for (int i = 0; i < removeFactor && fileCount > i; i++) {
            files.get(i).delete();
        }
    }

    /**
     * 获取缓存的大小
     *
     * @return 缓存大小（单位B）
     */
    public long getCachedSize() {
        return recurSort(getCacheDirectory(CACHE_TYPE_DATA))
                + recurSort(getCacheDirectory(CACHE_TYPE_IMAGE));
    }

    /**
     * 统计文件夹大小
     *
     * @param dir
     * @return
     */
    private long recurSort(File dir) {
        long size = 0;
        File[] files = dir.listFiles();
        if (files == null) return 0;
        for (File file : files) {
            if (!file.isDirectory()) {
                size += file.length();
            } else {
                size += recurSort(file);
            }
        }
        return size;
    }

    /**
     * 获取所以缓存的文件
     *
     * @param dir
     * @param container
     */
    private void recurGetCacheFiles(File dir, List<File> container) {
        File[] files = dir.listFiles();
        for (File file : files) {
            if (!file.isDirectory()) {
                container.add(file);
            } else {
                recurGetCacheFiles(file, container);
            }
        }
    }

    /**
     * 修改文件的最后修改时间
     *
     * @param file
     */
    public void updateFileTime(File file) {
        long newModifiedTime = System.currentTimeMillis();
        file.setLastModified(newModifiedTime);
    }

    /**
     * 文件和文件的最后修改时间的键值对
     * 为了防止文件修改的多线程操作，导致文件修改时间排序时发生异常。
     * added by yusongying on 2012-3-23
     */
    private static HashMap<File, Long> comparatorFiles = new HashMap<File, Long>();

    /**
     * 根据文件的最后修改时间进行排序
     */
    class FileLastModifSort implements Comparator<File> {

        private Long getLastModifTime(File f) {
            if (f == null) {
                return (long) 0;
            }
            if (comparatorFiles.containsKey(f)) {
                return comparatorFiles.get(f);
            } else {
                comparatorFiles.put(f, f.lastModified());
                return f.lastModified();
            }
        }

        public int compare(File arg0, File arg1) {
            long arg0Time = getLastModifTime(arg0);
            long arg1Time = getLastModifTime(arg1);
            if (arg0Time > arg1Time) {
                return 1;
            } else if (arg0Time == arg1Time) {
                return 0;
            } else {
                return -1;
            }
        }
    }
}

