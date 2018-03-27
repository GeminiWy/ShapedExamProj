package com.tming.common.cache.image;

import java.io.*;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.Executor;


import com.tming.common.thread.TMExcutors;
import com.tming.common.util.BitmapUtil;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.view.View;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.ProgressBar;

import com.tming.common.CommonApp;
import com.tming.common.R;
import com.tming.common.cache.Cache;
import com.tming.common.util.Constants;
import com.tming.common.util.Log;
import com.tming.common.util.PhoneUtil;

/**
 * <p>异步加载图片并进行缓存</p>
 * <ul>
 *     <li>对图片进行内存软引用，加快图片读取速度</li>
 *     <li>清除图片缓存</li>
 *     <li>实现控制非WiFi环境是否加载图片</li>
 *     <li>根据指定大小加载图片</li>
 *     <li>加载本地相册文件图片并支持自动旋转</li>
 * </ul>
 */
 public class ImageCacheTool extends Cache {
    
    /**
     * Debug log Tag
     */
    private static final String TAG = "ImageCacheTool";
    /**
     * 不使用缓存策略
     */
    private static final int POLICY_NO_USE_CACHE = 0;
    /**
     * 使用文件缓存
     */
    private static final int POLICY_USE_FILE_CACHE = 1;

    /**
     * 使用内存缓存
     */
    private static final int POLICY_USE_MEMERORY_CACHE = 1 << 3;

    /**
     * 缓存不可清除
     */
    private static final int POLICY_CAN_NOT_CLEAR = 1 << 1;
    /**
     * 不返回Bitmap对象，仅下载图片到sd卡
     */
    private static final int POLICY_NOT_DISPLAY = 1 << 2;

    /**
     * 加载本地图片自动旋转方向
     */
    private static final int POLICY_LOCAL_FILE_ADJUST_ORITATION = 1 << 4;

    /**
     * 单例变量
     */
    private static ImageCacheTool instance = new ImageCacheTool(CommonApp.getAppContext());
    /**
     * 缓存服务器 If-Modified-Since 持久化
     */
    private SharedPreferences mCachePreferences;
    /**
     * 写入 cachePreferences 线程同步锁
     */
    private Object mCachePreferencesLock = new Object();
    /**
     * 正在加载的图片地址，用于防止多个任务加载相同图片
     */
    private Vector<String> mLoadingImageUrls = new Vector<String>();
    
    /**
     * 图片内存缓存，在指定内存范围大小内进行强引用对象图片
     */
    private HashMap<String, WeakReference<Bitmap>> mMemoryCache = new HashMap<String, WeakReference<Bitmap>>();

    /**
     * 图片加载线程池
     */
    protected static Executor executor = TMExcutors.newStackQueueCachedThreadPool("Image-loader");
    
    /**
     * @see #getInstance()
     */
    @Deprecated
    public static synchronized ImageCacheTool getInstance(Context context) {
        return instance;
    }

    /**
     * 获取缓存实例
     *
     * @return 缓存实例
     */
    public static ImageCacheTool getInstance() {
        return instance;
    }

    private Map<ImageView, ImageViewLoadImageCallback> mImageViewCallbacks;
    
    /**
     * 构造方法
     * 
     * @param context Android上下文
     */
    private ImageCacheTool(Context context) {
        super(context);
        mCachePreferences = context.getSharedPreferences("image_cache", Context.MODE_PRIVATE);
        mImageViewCallbacks = Collections.synchronizedMap(new WeakHashMap<ImageView, ImageViewLoadImageCallback>());
    }

    /**
     * 将图片保存在SD卡中
     *
     * @param bm    Bitmap图片对象
     * @param saveFile 保存的文件
     * @param lastModifedTime 服务器lastModifedTime
     */
    public void saveBitmapToSd(Bitmap bm, File saveFile, long lastModifedTime) {
        // 删除需要删除的部分缓存
        if (bm == null || bm.isRecycled()) {
            Log.d(TAG, "Trying to save null or recycled bitmap");
            return;
        }
        freeSomeCacheSpace();
        OutputStream outStream = null;
        try {
            if (saveFile != null && !saveFile.getParentFile().exists()) {
                saveFile.getParentFile().mkdirs();
            }
            saveFile.createNewFile();
            outStream = new FileOutputStream(saveFile);
            bm.compress(Bitmap.CompressFormat.PNG, 100, outStream);
            outStream.flush();
            saveImageModifiedSince(saveFile.getName(), lastModifedTime);
        } catch (FileNotFoundException e) {
            Log.e(TAG, "saveBitmapToSd-FileNotFoundException:" + saveFile.getPath());
        } catch (IOException e) {
            Log.e(TAG, "saveBitmapToSd-IOException:" + saveFile.getPath());
        } finally {
            try {
                if (null != outStream) {
                    outStream.close();
                }
            } catch (IOException e) {
                Log.d(TAG, "IOException" + e.getMessage());
            }
        }
    }

    /**
     * 判断url地址的图片内容是否存在,可被清除和不可被清除图片存放不同路径
     * 
     * @param url 图片地址
     * @param canClear 是否可被清除
     * @return
     */
    public boolean isFileExist(String url, boolean canClear) {
        File file = getImageCacheFile(url, canClear);
        return file != null ? file.exists() : false;
    }
    
    /**
     * <p>获取缓存图片文件</P>
     * 
     * @param url 图片地址
     * @param canClear 文件是否属于可被清除
     * @return 缓存文件
     */
    public File getImageCacheFile(String url, boolean canClear) {
        if (null == url || "".equals(url.trim())) {
            return null;
        }
        String filename = convertUrlToFileName(url);
        File dir = getCacheDirectory(canClear ? CACHE_TYPE_IMAGE : CACHE_TYPE_NOT_CLEAR);
        return new File(dir, filename);
    }

    /**
     * @deprecated Use {@link #getImageCacheFile(String, boolean)} instead.
     * <p>获取缓存图片文件</P>
     * @param url 图片地址
     * @param canClear 文件是否属于可被清除
     * @param reqWidth 指定宽
     * @param reqHeight 指定高
     * @return
     */
    public File getImageCacheFile(String url, boolean canClear, int reqWidth, int reqHeight) {
        File saveFile = getImageCacheFile(url, canClear);
        return saveFile;
    }

    /**
     * 从缓存文件中加载图片
     * 
     * @param url 图片服务端地址
     * @param reqWidth
     * @return 存在返回Bitmap，不存在返回null
     */
    private Bitmap loadImageFile(URL url, File file, int reqWidth, int reqHeight, boolean adjustOritation) {
        if (file != null && file.exists()) {
            // 检查服务器上的文件最后修改时间
            if (reqWidth < 1 && checkCanDownloadImage() && checkImageIsModified(url, file.getName())) {
                return null;
            }
            // 存在，则修改文件的最后修改时间
            updateFileTime(file);
            return BitmapUtil.loadImage(file, reqWidth, reqHeight, adjustOritation);
        }
        return null;
    }

    /**
     * 移除缓存
     * 
     * @param imageUrlString 图片地址
     */
    public void removeCache(String imageUrlString) {
        removeMemCache(imageUrlString, -1, -1);
        removeDiskCache(imageUrlString, -1, -1);
    }
    
    /**
     * 移除缓存
     * 
     * @param imageUrlString 图片地址
     * @param reqWidth 指定加载图片的宽度
     * @param reqHeight 指定加载图片的高度
     */
    public void removeCache(String imageUrlString, int reqWidth, int reqHeight) {
        removeMemCache(imageUrlString, reqWidth, reqHeight);
        removeDiskCache(imageUrlString, reqWidth, reqHeight);
    }

    /**
     * 
     * 移除内存cache
     * 
     * @param urlString 图片地址
     * @param reqWidth 指定加载图片的宽度
     * @param reqHeight 指定加载图片的高度
     */
    private void removeMemCache(String urlString, int reqWidth, int reqHeight) {
        try {
            String memeryCacheKey = new URL(urlString).toString();
            // 根据不同尺寸进行缓存图片
            if (reqWidth > 0 && reqHeight > 0) {
                memeryCacheKey = reqWidth + "_" + reqHeight + "_" + memeryCacheKey;
            }
            Log.d(TAG, "remove memory cache:" + memeryCacheKey + ":" + mMemoryCache.get(memeryCacheKey));
            mMemoryCache.remove(memeryCacheKey);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * 移除硬盘图片缓存
     * 
     * @param urlString 图片地址
     * @param reqWidth 指定加载图片的宽度
     * @param reqHeight 指定加载图片的高度
     */
    private void removeDiskCache(String urlString, int reqWidth, int reqHeight) {
        try {
            urlString = new URL(urlString).toString();
            File file = getImageCacheFile(urlString, true);
            if (file != null && file.exists()) {
                file.delete();
                Log.d(TAG, "delete disk cache:" + file.getPath());
            } else {
                Log.e(TAG, "no found disk cache:" + urlString);
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    /**
     * 唤醒加载相同图片的线程
     * 
     * @param url 图片地址
     */
    private void wakeUpLoadSameImgThread(String url) {
        synchronized (url) {
            url.notifyAll();
            mLoadingImageUrls.remove(url);
        }
    }

    /**
     * 拷贝文件方法
     * @param orgFile 原始文件
     * @param newFile 新文件
     */
    private boolean copyFile(File orgFile, File newFile) {
        FileInputStream fis = null;
        FileOutputStream fos = null;
        try {
            fis = new FileInputStream(orgFile);
            fos = new FileOutputStream(newFile);
            byte[] buf = new byte[1024];
            int len = -1;
            while ((len = fis.read(buf)) != -1) {
                fos.write(buf, 0, len);
            }
            fos.flush();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (fos != null) {
                    fos.close();
                }
                if (fis != null) {
                    fis.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    /**
     *
     * <p>加载图片核心方法</P>
     *
     * @param url 图片地址
     * @param callBack 回调类
     * @param cachePolicy 缓存策略
     * @param reqWidth 指定加载图片的宽度
     * @param reqHeight 指定加载图片的高度
     * @return
     */
    private Bitmap loadImage(URL url, ImageLoadCallBack callBack, int cachePolicy, int reqWidth, int reqHeight) {
        // 图片软引用
        if (url == null) {
            if (callBack != null)
                callBack.loadResult(null);
            return null;
        }
        // 内存缓存的key
        String memeryCacheKey = url.toString();
        String sameLoadingLock = memeryCacheKey;
        // 根据不同尺寸进行缓存图片
        if (reqWidth > 0 && reqHeight > 0) {
            memeryCacheKey = reqWidth + "_" + reqHeight + "_" + memeryCacheKey;
        }
        // 如果有相同的图片正在加载，就暂停加载
        waitSameUrlLoadTask(sameLoadingLock);

        if (mMemoryCache.get(memeryCacheKey) != null) {
            Bitmap bitmap = mMemoryCache.get(memeryCacheKey).get();
            if (bitmap != null) {
                if (callBack != null)
                    callBack.loadResult(bitmap);
                return bitmap;
            }
        }
        // 添加到正在加载的列表中
        mLoadingImageUrls.add(sameLoadingLock);
        boolean canClear = (cachePolicy & POLICY_CAN_NOT_CLEAR) == 0;
        boolean isNeedDisplay = (cachePolicy & POLICY_NOT_DISPLAY) == 0;
        File saveFile = null;
        // 判断是否为本地文件
        if ("file".equals(url.getProtocol())) {
            saveFile = new File(url.getFile());
        } else {
            saveFile = getImageCacheFile(url.toString(), canClear);
        }
        if (saveFile != null && saveFile.exists()) {
            try {
                if (!isNeedDisplay) {
                    return null;
                }
                Bitmap record = loadImageFile(url, saveFile, reqWidth, reqHeight, (cachePolicy & POLICY_LOCAL_FILE_ADJUST_ORITATION) > 0);
                if (record != null) {
                    if (callBack != null)
                        callBack.loadResult(record);
                    if ((cachePolicy & POLICY_USE_MEMERORY_CACHE)  > 0) {
                        mMemoryCache.put(memeryCacheKey, new WeakReference<Bitmap>(record));
                    }
                    return record;
                }
            } catch (OutOfMemoryError e) {
                Log.d(TAG, "loadImageFile oom:" + url);
                System.gc();
                if (callBack != null)
                    callBack.loadResult(null);
                return null;
            } finally {
                wakeUpLoadSameImgThread(sameLoadingLock);
            }
        }
        // 若流量有控制，当前又无wifi连接就返回null
        if (!checkCanDownloadImage()) {
            wakeUpLoadSameImgThread(sameLoadingLock);
            if (callBack != null) {
                callBack.loadResult(null);
            }
            return null;
        }
        InputStream is = null;
        HttpURLConnection conn = null;
        Bitmap image = null;
        try {
            URL fixedUrl = fixUrl(url);
            Log.d(TAG, "start Load Image:" + fixedUrl.toString());
            conn = (HttpURLConnection) fixedUrl.openConnection();
            conn.setDoInput(true);
            conn.setConnectTimeout(Constants.HTTP_CONNECT_TIMEOUT);
            conn.setReadTimeout(Constants.HTTP_READ_TIMEOUT);
            conn.setInstanceFollowRedirects(true);
            conn.connect();
            int responeCode = conn.getResponseCode();
            if (responeCode == 200) {
                int contentLength = conn.getContentLength();
                is = conn.getInputStream();
                // 不需要返回图片显示的仅做保存
                if (!isNeedDisplay && saveFile != null) {
                    Log.d(TAG, "no display save:" + url.toString());
                    saveImageInputStreamToFile(is, saveFile, conn.getLastModified(), contentLength, callBack);
                } else if (reqWidth > 0 && reqHeight > 0) {
                    // 按照指定宽高加载图片
                    saveImageInputStreamToFile(is, saveFile, conn.getLastModified(), contentLength, callBack);
                    image = loadImageFile(fixedUrl, saveFile, reqWidth, reqHeight, false);
                } else {
                    image = decodeStream2Image(is, contentLength, callBack);
                    if (image != null && saveFile != null && (cachePolicy & POLICY_USE_FILE_CACHE) > 0) {
                        saveBitmapToSd(image, saveFile, conn.getLastModified());
                    }
                }
            }
            if (image != null && (cachePolicy & POLICY_USE_MEMERORY_CACHE) > 0) {
                mMemoryCache.put(memeryCacheKey, new WeakReference<Bitmap>(image));
            }
            return image;
        } catch (Throwable e) {
            Log.d(TAG, "Load network image occur error:" + url, e);
        } finally {
            try {
                if (is != null)
                    is.close();
                if (conn != null)
                    conn.disconnect();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (callBack != null)
                callBack.loadResult(image);
            // 唤醒加载相同图片的线程
            wakeUpLoadSameImgThread(sameLoadingLock);
        }
        return null;
    }
    
    /**
     * <p>实现将图片加载回调在主线程调用</P>
     *
     * @param url 图片地址
     * @param imageLoadCallBack 回调类
     * @param policy 缓存策略
     * @param reqWidth 指定加载图片的宽度
     * @param reqHeight 指定加载图片的高度
     */
    private void asyncLoadImage(final URL url, final int policy, final ImageLoadCallBack imageLoadCallBack, final int reqWidth, final int reqHeight) {
        executor.execute(new Runnable() {
            
            private final ImageLoadCallBack callBack = new ImageLoadCallBack() {

                @Override
                public void progress(final int progress) {
                    mHandler.post(new Runnable() {

                        @Override
                        public void run() {
                            imageLoadCallBack.progress(progress);
                        }
                    });
                }

                @Override
                public void loadResult(final Bitmap result) {
                    mHandler.post(new Runnable() {

                        @Override
                        public void run() {
                            imageLoadCallBack.loadResult(result);
                        }
                    });
                }
            };
            
            @Override
            public void run() {
                loadImage(url, callBack, policy, reqWidth, reqHeight);
            }
        });
    }
    
    /**
     * 异步加载图片,到ImageView上
     * 
     * @param imageView 图片显示的控件
     * @param bar 图片加载的进度条
     * @param imageUrl 图片地址
     * @param errImgId 图片加载发生错误，显示的图片资源
     * @param policy 缓存策略
     * @param reqWidth 指定加载图片的宽度
     * @param reqHeight 指定加载图片的高度
     */
    private void asyncLoadImage(ImageView imageView, ProgressBar bar, URL imageUrl, final int errImgId, int policy, int reqWidth, int reqHeight) {
        // 判断内存中的图片，存在直接设置返回
        String memeryCacheKey = imageUrl.toString();
        if (reqWidth > 0 && reqHeight > 0) {
            memeryCacheKey = reqWidth + "_" + reqHeight + "_" + memeryCacheKey;
        }
        WeakReference<Bitmap> bitmapSoftReference = mMemoryCache.get(memeryCacheKey);
        Bitmap bitmap;
        if (bitmapSoftReference != null && (bitmap = bitmapSoftReference.get()) != null) {
            imageView.setImageBitmap(bitmap);
            if (bar != null) {
                bar.setVisibility(View.INVISIBLE);
            }
            return;
        }

        // 将UI组件封装成弱引用
        final WeakReference<ImageView> weakReferenceImageView = new WeakReference<ImageView>(imageView);
        WeakReference<ProgressBar> tempVar = null;
        if (bar != null) {
            tempVar = new WeakReference<ProgressBar>(bar);
        }
        final WeakReference<ProgressBar> weakReferenceBar = tempVar;

        // 开始加载图片
        ImageViewLoadImageCallback callback = new ImageViewLoadImageCallback(weakReferenceImageView, weakReferenceBar, errImgId);
        asyncLoadImage(imageUrl, policy,callback, reqWidth, reqHeight);
        mImageViewCallbacks.put(imageView, callback);
    }

    /**
     * 取消加载图片
     *
     * <P>这个方法并非真的取消了图片加载线程，只是取消了加载的回调</P>
     * @see #asyncLoadImage(android.widget.ImageView, android.widget.ProgressBar, java.net.URL, int, int)
     * @see #asyncLoadImage(android.widget.ImageView, android.widget.ProgressBar, java.net.URL)
     * @see #asyncLoadImage(android.widget.ImageView, String)
     * @see #asyncLoadImage(android.widget.ImageView, String, int)
     * @see #asyncLoadImage(java.net.URL, android.widget.ImageView, int, int, int)
     * @param imageView 显示用的ImageView
     */
    public void cancelLoadImage(ImageView imageView) {
        mImageViewCallbacks.remove(imageView);
    }

    /**
     * ImageView 异步加载回调类
     */
    private class ImageViewLoadImageCallback implements ImageLoadCallBack {

        private WeakReference<ProgressBar> mProgressBarWeakReference;
        private WeakReference<ImageView> mImageViewWeakReference;
        private int mLoadFailResourceId;

        public ImageViewLoadImageCallback(WeakReference<ImageView> imageViewWeakReference, WeakReference<ProgressBar> progressBarWeakReference, int loadFailResourceId) {
            mImageViewWeakReference = imageViewWeakReference;
            mProgressBarWeakReference = progressBarWeakReference;
            mLoadFailResourceId = loadFailResourceId;
        }

        @Override
        public void progress(int progress) {
            if (mProgressBarWeakReference != null && !checkHasCanceled()) {
                ProgressBar bar = mProgressBarWeakReference.get();
                if (bar != null)
                    bar.setProgress(progress);
            }
        }

        /**
         * 判断当前ImageView的加载任务是否被替换成另一个加载任务，或者是取消了
         * @return true 取消了，否则没有
         */
        public boolean checkHasCanceled() {
            ImageView imageView = mImageViewWeakReference.get();
            if (imageView == null) {
                return true;
            }
            return mImageViewCallbacks.get(imageView) != this;
        }

        @Override
        public void loadResult(Bitmap bitmap) {
            ImageView imageView = mImageViewWeakReference.get();
            boolean isCanceled = checkHasCanceled();
            if (!isCanceled) {
                if (bitmap != null) {
                    imageView.setImageBitmap(bitmap);
                } else {// edit by lpf:2013-9-13图片加载失败的话使用别的默认图片
                    imageView.setImageResource(mLoadFailResourceId != 0 ? mLoadFailResourceId : R.drawable.loaded_fail);
                    imageView.setScaleType(ScaleType.CENTER_INSIDE);
                }
            }
            if (mProgressBarWeakReference != null && !isCanceled) {
                ProgressBar bar = mProgressBarWeakReference.get();
                if (bar != null) {
                    bar.setVisibility(View.INVISIBLE);
                }
            }
        }
    }
    
    /**
     * <p>阻塞相同Url图片的加载</P>
     * @param memeryCacheKey 内存中的缓存key 
     */
    private void waitSameUrlLoadTask(String memeryCacheKey) {
        if (mLoadingImageUrls.contains(memeryCacheKey)) {
            try {
                int position = mLoadingImageUrls.indexOf(memeryCacheKey);
                String lastCacheKey = mLoadingImageUrls.get(position);
                synchronized (lastCacheKey) {
                    try {
                        lastCacheKey.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
    
    /**
     * 
     * <p>修正URL中的&amp为&和URL中的Path进行Uri.encode</P>
     *
     * @param url
     * @return
     * @throws MalformedURLException
     */
    private URL fixUrl(URL url) throws MalformedURLException {
        String filePath = url.getPath();
        String[] filePathSplites = filePath.split("/");
        String encodeUrl = url.getProtocol() + "://" + url.getHost();
        if (url.getPort() != -1 && url.getPort() != 80) {
            encodeUrl += ":" + url.getPort();
        }
        for (String splitePath : filePathSplites) {
            if (splitePath.length() > 0) {
                encodeUrl += "/" + Uri.encode(splitePath, "UTF-8");
            }
        }
        String queryString = url.getQuery();
        if (queryString != null) {
            queryString = queryString.replace("&amp;", "&");
            encodeUrl += "?" + queryString;
        }
        return new URL(encodeUrl);
    }
    
    /**
     * 
     * <p>将输入流编码成图片</P>
     *
     * @param is 输入流
     * @param contentLength 图片字节长度
     * @param callBack 回调
     * @return 图片
     * @throws IOException
     */
    private Bitmap decodeStream2Image(InputStream is, int contentLength, ImageLoadCallBack callBack) throws IOException {
        ByteArrayOutputStream byteOS = new ByteArrayOutputStream();
        byte[] buf = new byte[1024];
        int len = -1;
        while ((len = is.read(buf)) != -1) {
            byteOS.write(buf, 0, len);
            if (callBack != null && contentLength > 0) {
                int progress = (int) (((float) (byteOS.size()) / (float) contentLength) * 100);
                callBack.progress(progress);
            }
        }
        return BitmapFactory.decodeByteArray(byteOS.toByteArray(), 0, byteOS.size());
    }
    
    /**
     * 
     * <p>保存输入流到磁盘缓存</P>
     *
     * @param is 输入流
     * @param saveFile 保存的文件路径
     * @param lastModifedTime 服务器lastModifedTime
     * @param contentLength 图片字节长度
     */
    private void saveImageInputStreamToFile(InputStream is, File saveFile, long lastModifedTime, long contentLength, ImageLoadCallBack callBack) {
        freeSomeCacheSpace();
        if (saveFile == null) {
            return;
        }
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(saveFile);
            int len;
            long wirtedLen = 0;
            byte[] buf = new byte[1024];
            while ((len = is.read(buf)) != -1) {
                fos.write(buf, 0, len);
                wirtedLen += len;
                if (callBack != null) {
                    int progress = (int) (((float) wirtedLen / (float) contentLength) * 100);
                    callBack.progress(progress);
                }
            }
            fos.flush();
            saveImageModifiedSince(saveFile.getName(), lastModifedTime);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (fos != null) fos.close();
                if (is != null) is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 检查是否可以下载图片，包括网络状态和图片下载控制
     * 
     * @return true 可以下载，否则不可以
     */
    private boolean checkCanDownloadImage() {
        boolean hasNetwork = PhoneUtil.checkNetworkEnable() == PhoneUtil.NETSTATE_ENABLE;
        if (!hasNetwork || (isNetworkFlowControlOpen() && !PhoneUtil.isWifiNetwork())) {
            return false;
        }
        return true;
    }

    /**
     *
     * <p>保存服务器的If-Modified-Since</P>
     *
     * @param fileName 本地保存的文件名
     * @param lastModifedTime Modified-Since时间
     */
    private void saveImageModifiedSince(String fileName, long lastModifedTime) {
        synchronized (mCachePreferencesLock) {
            Editor editor = mCachePreferences.edit();
            editor.putLong(fileName, lastModifedTime);
            editor.commit();
        }
    }
    
    /**
     *
     * <p>
     * 根据Http If-Modified-Since 协议， 判读服务器上的图片文件是否已经改变
     * </P>
     *
     * @param url 图片地址
     * @param fileName 文件名
     * @return true 已经发生改变，否则为未修改，Http异常默认为false
     */
    private boolean checkImageIsModified(URL url, String fileName) {
        HttpURLConnection connection = null;
        long lastModified;
        synchronized (mCachePreferencesLock) {
            lastModified = mCachePreferences.getLong(fileName, 0);
        }
        try {
            if (lastModified != 0) {
                connection = (HttpURLConnection) url.openConnection();
                connection.setConnectTimeout(1000);
                connection.setReadTimeout(3000);
                connection.setIfModifiedSince(lastModified);
                connection.connect();
                int code = connection.getResponseCode();
                // 如果已经被修改服务端返回200状态，否则默认为未修改
                if (code == 200) {
                    return true;
                } else {
                    return false;
                }
            } else {
                return true;
            }
        } catch (Throwable e) {
            e.printStackTrace();
        } finally {
            if (connection != null)
                connection.disconnect();
        }
        return false;
    }

    /**
     * 带缓存的图片加载，缓存不可调用 clearAllCache 清除
     * 
     * @param url 图片地址
     * @param callBack 该回调采用下载线程
     * @return
     */
    public Bitmap loadImageWithCanNotClearCache(URL url, ImageLoadCallBack callBack) {
        return loadImage(url, callBack, POLICY_USE_FILE_CACHE | POLICY_USE_MEMERORY_CACHE | POLICY_CAN_NOT_CLEAR, -1, -1);
    }
    
    /**
     * 带缓存的图片加载，缓存不可调用 clearAllCache 清除
     * 
     * @param url 图片地址
     * @param callBack 该回调采用下载线程
     * @param reqWidth 指定加载图片的宽度
     * @param reqHeight 指定加载图片的高度
     * @return
     */
    public Bitmap loadImageWithCanNotClearCache(URL url, ImageLoadCallBack callBack, int reqWidth, int reqHeight) {
        return loadImage(url, callBack, POLICY_USE_FILE_CACHE | POLICY_USE_MEMERORY_CACHE | POLICY_CAN_NOT_CLEAR, reqWidth, reqHeight);
    }

    /**
     * 带缓存的图片加载，缓存可调用 clearAllCache 清除
     * 
     * @param url 图片地址
     * @param callBack 该回调采用下载线程
     * @return Bitmap对象
     */
    public Bitmap loadImgByURLHasCache(URL url, ImageLoadCallBack callBack) {
        return loadImage(url, callBack, POLICY_USE_FILE_CACHE | POLICY_USE_MEMERORY_CACHE, -1, -1);
    }
    
    /**
     * 带缓存的图片加载，缓存可调用 clearAllCache 清除
     * 
     * @param url 图片地址
     * @param callBack 该回调采用下载线程
     * @param reqWidth 指定加载图片的宽度
     * @param reqHeight 指定加载图片的高度
     * @return Bitmap对象
     */
    public Bitmap loadImgByURLHasCache(URL url, ImageLoadCallBack callBack, int reqWidth, int reqHeight) {
        return loadImage(url, callBack, POLICY_USE_FILE_CACHE | POLICY_USE_MEMERORY_CACHE, reqWidth, reqHeight);
    }

    /**
     * 异步加载图片
     * 
     * @param imageUrl 图片地址
     * @param imageLoadCallBack 该回调采用主线程进行回调
     */
    public void asyncLoadImage(URL imageUrl, ImageLoadCallBack imageLoadCallBack) {
        asyncLoadImage(imageUrl, POLICY_USE_FILE_CACHE | POLICY_USE_MEMERORY_CACHE, imageLoadCallBack, -1, -1);
    }
    
    /**
     * 异步加载图片
     * 
     * @param imageUrl 图片地址
     * @param imageLoadCallBack 该回调采用主线程进行回调
     * @param reqWidth 指定加载图片的宽度
     * @param reqHeight 指定加载图片的高度
     */
    public void asyncLoadImage(URL imageUrl, ImageLoadCallBack imageLoadCallBack, int reqWidth, int reqHeight) {
        asyncLoadImage(imageUrl, POLICY_USE_FILE_CACHE | POLICY_USE_MEMERORY_CACHE, imageLoadCallBack, reqWidth, reqHeight);
    }

    /**
     * 异步加载图片
     *
     * @param imageUrl 图片地址
     * @param imageLoadCallBack 该回调采用主线程进行回调
     * @param reqWidth 指定加载图片的宽度
     * @param reqHeight 指定加载图片的高度
     */
    public void asyncLoadAdjustOritationImage(URL imageUrl, ImageLoadCallBack imageLoadCallBack, int reqWidth, int reqHeight) {
        asyncLoadImage(imageUrl, POLICY_USE_FILE_CACHE | POLICY_USE_MEMERORY_CACHE | POLICY_LOCAL_FILE_ADJUST_ORITATION, imageLoadCallBack, reqWidth, reqHeight);
    }

    /**
     * 异步加载大图，不使用内存缓存
     *
     * @param imageUrl 图片地址
     * @param imageLoadCallBack 该回调采用主线程进行回调
     * @param reqWidth 指定加载图片的宽度
     * @param reqHeight 指定加载图片的高度
     */
    public void asyncLoadBigImage(URL imageUrl, ImageLoadCallBack imageLoadCallBack, int reqWidth, int reqHeight) {
        asyncLoadImage(imageUrl, POLICY_USE_FILE_CACHE, imageLoadCallBack, reqWidth, reqHeight);
    }
    
    /**
     * 异步加载图片
     *
     * @param imageView 图片显示控件
     * @param imageUrl 图片地址
     */
    public void asyncLoadImage(URL imageUrl, ImageView imageView) {
        asyncLoadImage(imageView, null, imageUrl, 0, POLICY_USE_FILE_CACHE | POLICY_USE_MEMERORY_CACHE, -1, -1);
    }

    /**
     * 同步加载图片
     * @param imageUrl
     * @return
     */
    public Bitmap loadImage(URL imageUrl) {
        return loadImage(imageUrl, null, POLICY_USE_FILE_CACHE | POLICY_USE_MEMERORY_CACHE, -1, -1);
    }
    
    /**
     * 异步加载图片
     *
     * @param imageView 图片显示控件
     * @param imageUrl 图片地址
     * @param reqWidth 指定加载图片的宽度
     * @param reqHeight 指定加载图片的高度
     */
    public void asyncLoadImage(URL imageUrl, ImageView imageView, int reqWidth, int reqHeight) {
        asyncLoadImage(imageView, null, imageUrl, 0, POLICY_USE_FILE_CACHE | POLICY_USE_MEMERORY_CACHE, reqWidth, reqHeight);
    }
    
    /**
     * 异步加载图片
     *
     * @param imageView 图片显示控件
     * @param imageUrl 图片的url地址
     * @param errImgId 图片加载失败时显示的图片
     */
    public void asyncLoadImage(URL imageUrl, ImageView imageView, int errImgId) {
        asyncLoadImage(imageView, null, imageUrl, errImgId, POLICY_USE_FILE_CACHE | POLICY_USE_MEMERORY_CACHE, -1, -1);
    }
    
    /**
     * 异步加载图片
     *
     * @param imageView 图片显示控件
     * @param imageUrl 图片的url地址
     * @param errImgId 图片加载失败时显示的图片
     * @param reqWidth 指定加载图片的宽度
     * @param reqHeight 指定加载图片的高度
     */
    public void asyncLoadImage(URL imageUrl, ImageView imageView, int errImgId, int reqWidth, int reqHeight) {
        asyncLoadImage(imageView, null, imageUrl, errImgId, POLICY_USE_FILE_CACHE | POLICY_USE_MEMERORY_CACHE, reqWidth, reqHeight);
    }

    /**
     * 异步加载图片
     *
     * @param imageView 图片显示控件
     * @param imageUrl 图片的url地址
     * @param errImgId 图片加载失败时显示的图片
     * @param reqWidth 指定加载图片的宽度
     * @param reqHeight 指定加载图片的高度
     */
    public void asyncLoadAdjustOritationImage(URL imageUrl, ImageView imageView, int errImgId, int reqWidth, int reqHeight) {
        asyncLoadImage(imageView, null, imageUrl, errImgId, POLICY_USE_FILE_CACHE | POLICY_USE_MEMERORY_CACHE | POLICY_LOCAL_FILE_ADJUST_ORITATION, reqWidth, reqHeight);
    }

    /**
     * 异步加载图片
     *
     * @param imageView 图片显示控件
     * @param imageUrl 图片的url地址
     * @param errImgId 图片加载失败时显示的图片
     * @param reqWidth 指定加载图片的宽度
     * @param reqHeight 指定加载图片的高度
     */
    public void asyncLoadBigImage(URL imageUrl, ImageView imageView, int errImgId, int reqWidth, int reqHeight) {
        asyncLoadImage(imageView, null, imageUrl, errImgId, POLICY_USE_FILE_CACHE, reqWidth, reqHeight);
    }

    /**
     * 异步加载图片
     *
     * @param imageView 图片显示控件
     * @param imageUrl 图片的url地址
     * @param errImgId 图片加载失败时显示的图片
     * @param reqWidth 指定加载图片的宽度
     * @param reqHeight 指定加载图片的高度
     */
    public void asyncLoadAdjustOritationBigImage(URL imageUrl, ImageView imageView, int errImgId, int reqWidth, int reqHeight) {
        asyncLoadImage(imageView, null, imageUrl, errImgId, POLICY_USE_FILE_CACHE | POLICY_LOCAL_FILE_ADJUST_ORITATION, reqWidth, reqHeight);
    }
    
    /**
     * 异步加载图片,到ImageView上
     * 
     * @param imageView 图片显示的控件
     * @param bar       图片加载的进度条
     * @param imageUrl  图片地址
     */
    public void asyncLoadImage(ImageView imageView, ProgressBar bar, URL imageUrl) {
        asyncLoadImage(imageView, bar, imageUrl, -1, -1);
    }
    
    /**
     * 异步加载图片,到ImageView上
     * 
     * @param imageView 图片显示的控件
     * @param bar       图片加载的进度条
     * @param imageUrl  图片地址
     * @param reqWidth 指定加载图片的宽度
     * @param reqHeight 指定加载图片的高度
     */
    public void asyncLoadImage(ImageView imageView, ProgressBar bar, URL imageUrl, int reqWidth, int reqHeight) {
        asyncLoadImage(imageView, bar, imageUrl, 0, POLICY_USE_FILE_CACHE | POLICY_USE_MEMERORY_CACHE, reqWidth, reqHeight);
    }

    /**
     * 异步加载图片,缓存不可调用 clearAllCache 清除
     * 
     * @param imageUrl  图片地址
     * @param imageLoadCallBack 该回调采用主线程进行回调
     */
    public void asyncLoadImageCanNotClear(URL imageUrl, ImageLoadCallBack imageLoadCallBack) {
        asyncLoadImage(imageUrl, POLICY_USE_FILE_CACHE | POLICY_USE_MEMERORY_CACHE | POLICY_CAN_NOT_CLEAR, imageLoadCallBack, -1, -1);
    }
    
    /**
     * 异步加载图片,缓存不可调用 clearAllCache 清除
     * 
     * @param imageUrl  图片地址
     * @param imageLoadCallBack 该回调采用主线程进行回调
     * @param reqWidth 指定加载图片的宽度
     * @param reqHeight 指定加载图片的高度
     */
    public void asyncLoadImageCanNotClear(URL imageUrl, ImageLoadCallBack imageLoadCallBack, int reqWidth, int reqHeight) {
        asyncLoadImage(imageUrl, POLICY_USE_FILE_CACHE | POLICY_USE_MEMERORY_CACHE | POLICY_CAN_NOT_CLEAR, imageLoadCallBack, reqWidth, reqWidth);
    }

    /**
     * 异步加载图片, 缓存不可调用 {@link #clearAllCache(android.os.Handler)} 清除
     * 
     * @param imageView 图片显示的控件
     * @param bar       图片加载的进度条
     * @param imageUrl  图片地址
     */
    public void asyncLoadImageCanNotClear(ImageView imageView, ProgressBar bar, URL imageUrl) {
        asyncLoadImage(imageView, bar, imageUrl, 0, POLICY_USE_FILE_CACHE | POLICY_USE_MEMERORY_CACHE | POLICY_CAN_NOT_CLEAR, -1, -1);
    }
    
    /**
     * 异步加载图片, 缓存不可调用 {@link #clearAllCache(android.os.Handler)} 清除
     * 
     * @param imageView 图片显示的控件
     * @param bar       图片加载的进度条
     * @param imageUrl  图片地址
     * @param reqWidth 指定加载图片的宽度
     * @param reqHeight 指定加载图片的高度
     */
    public void asyncLoadImageCanNotClear(ImageView imageView, ProgressBar bar, URL imageUrl, int reqWidth, int reqHeight) {
        asyncLoadImage(imageView, bar, imageUrl, 0, POLICY_USE_FILE_CACHE | POLICY_USE_MEMERORY_CACHE | POLICY_CAN_NOT_CLEAR, reqWidth, reqHeight);
    }

    /**
     * 图片加载回调
     * @see {@link ImageLoadCallBackAdapter} 
     */
    public static interface ImageLoadCallBack {

        /**
         * 图片加载成功
         */
        public void loadResult(Bitmap bitmap);

        /**
         * 进度反馈
         */
        public void progress(int progress);
    }

    /**
     * <p> 图片加载回调适配器 ,实现{@link ImageLoadCallBack} 接口</p>
     */
    public static class ImageLoadCallBackAdapter implements ImageLoadCallBack {

        @Override
        public void loadResult(Bitmap bitmap) {}

        @Override
        public void progress(int progress) {}
    }

    /**
     *
     * <p>判断用户是否开启了流量控制</P>
     *
     * @return true 开启了，否则没有
     */
    public boolean isNetworkFlowControlOpen() {
        SharedPreferences spf = mContext.getSharedPreferences(Constants.SHARE_PREFERENCES_NAME, Context.MODE_PRIVATE);
        return spf.getBoolean(Constants.SHARE_PREFERENCES_IS_FLOW_CONTROL, false);
    }

    /**
     * 设置是否开启了流量控制， 如果开启了在非wifi环境下不进行加载图片。
     *
     * @param isOpen 是否开启流量控制
     */
    public void setNetworkFlowControl(boolean isOpen) {
        SharedPreferences spf = mContext.getSharedPreferences(Constants.SHARE_PREFERENCES_NAME, Context.MODE_PRIVATE);
        Editor editor = spf.edit();
        editor.putBoolean(Constants.SHARE_PREFERENCES_IS_FLOW_CONTROL, isOpen);
        editor.commit();
    }
    
    /**
     * 不在界面显示的下载到缓存
     *
     * @param urlString 图片地址
     */
    public void loadImageNoDisplay(String urlString) {
        try {
            loadImage(new URL(urlString), null, POLICY_USE_FILE_CACHE | POLICY_USE_MEMERORY_CACHE | POLICY_NOT_DISPLAY, -1, -1);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    /**
     * 
     * <p>异步加载图片</P>
     *
     * @param imageView 图片显示控件
     * @param imgUrl    图片地址
     */
    public static void asyncLoadImage(ImageView imageView, String imgUrl) {
        asyncLoadImage(imageView, imgUrl, 0, 0);
    }

    /**
     * 
     * <p>异步加载图片</P>
     *
     * @param imageView     图片显示控件
     * @param imgUrl        图片的url地址
     * @param errImgId      图片加载失败时显示的图片
     */
    public static void asyncLoadImage(final ImageView imageView, final String imgUrl, final int errImgId) {
        asyncLoadImage(imageView, imgUrl, 0, errImgId);
    }

    /**
     * 
     * <p>异步加载图片</P>
     *
     * @param imageView     图片显示控件
     * @param imgUrlString        图片的url地址
     * @param loadImgId     正在加载图片时显示的图片
     * @param errImgId      图片加载失败时显示的图片
     */
    public static void asyncLoadImage(final ImageView imageView, final String imgUrlString, final int loadImgId,
            final int errImgId) {
        try {
            URL url = new URL(imgUrlString);
            imageView.setImageResource(loadImgId);
            getInstance().asyncLoadImage(imageView, null, url, errImgId, POLICY_USE_FILE_CACHE | POLICY_USE_MEMERORY_CACHE, -1, -1);
        } catch (MalformedURLException e) {
            e.printStackTrace();
            if (errImgId != 0) {
                imageView.setImageResource(errImgId);
            }
        }
    }
}