package com.tming.common.net;

import com.tming.common.CommonApp;
import com.tming.common.cache.Cache;
import com.tming.common.cache.net.TmingCacheHttp;
import com.tming.common.util.AESencrypt;
import com.tming.common.util.Helper;
import com.tming.common.util.Log;

import java.io.*;
import java.util.Map;

/**
 * <p> 使用缓存的Http请求</p>
 * @see {@link com.tming.common.cache.net.TmingCacheHttp#asyncRequestWithCache(String, java.util.Map, com.tming.common.cache.net.TmingCacheHttp.RequestWithCacheCallBackV2)}
 * @see {@link com.tming.common.cache.net.TmingCacheHttp#asyncRequestWithCache(String, java.util.Map, java.util.Map, com.tming.common.cache.net.TmingCacheHttp.RequestWithCacheCallBackV2)}
 * <p>Created by yusongying on 2014/10/19</p>
 */
public class TmingHttpRequestWithCacheTask<T> extends TmingHttpRequestTask<T> {

    private static final String TAG = "TmingCacheHttp";
    private static final String CHARSET = "UTF-8";

    /**
     * 缓存文件的AES加密密码
     */
    private static final String AES_PASSWORD = "tming";

    /**
     * 请求回调
     */
    private TmingCacheHttp.RequestWithCacheCallBackV2<T> mRequestCallback;
    /**
     * 缓存相关类
     */
    private Cache mCache;

    public TmingHttpRequestWithCacheTask(Cache cache, String urlString, Map<String, String> headers, Map<String, Object> params, TmingCacheHttp.RequestWithCacheCallBackV2<T> callback) {
        super(urlString, headers, params, null);
        mCache = cache;
        mRequestCallback = callback;
    }

    @Override
    public void run() {
        try {
            mThread = Thread.currentThread();
            // 缓存文件名的MD5加密URL
            File cacheFile = getRequestCacheFile();
            String cacheContentMd5 = null;
            T tmpPreData = null;
            if (cacheFile != null && cacheFile.exists()) {
                try {
                    Log.d(TAG, "Read from sdcard:" + mRequestUrlString);
                    String cache = loadContentFile(cacheFile);
                    if (cache != null) {
                        cacheContentMd5 = Helper.getMD5String(cache);
                        final T data = mRequestCallback.parseData(cache);
                        tmpPreData = data;
                        CommonApp.getAppHandler().post(new Runnable() {

                            @Override
                            public void run() {
                                mRequestCallback.cacheDataRespone(data);
                            }
                        });
                    }
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
            try {
                TmingResponse response = tmingHttpRequest();
                String newContent = response.asString();
                String newMd5 = Helper.getMD5String(newContent);
                final T preData = tmpPreData;
                if (!newMd5.equals(cacheContentMd5)) {
                    final T data = mRequestCallback.parseData(response.asString());
                    CommonApp.getAppHandler().post(new Runnable() {

                        @Override
                        public void run() {
                            mRequestCallback.requestNewDataRespone(preData, data);
                        }
                    });
                    if (cacheFile != null) {
                        saveResponseToSd(cacheFile, newContent);
                    }
                }
            } catch (Throwable e) {
                if(cacheContentMd5 == null) {
                    throw e;
                }
            }
        } catch (final Throwable e) {
            CommonApp.getAppHandler().post(new Runnable() {

                @Override
                public void run() {
                    mRequestCallback.exception(new Exception(e));
                }
            });
        }
        // 请求结束
        CommonApp.getAppHandler().post(new Runnable() {

            @Override
            public void run() {
                mRequestCallback.onFinishRequest();
            }
        });
        mThread = null;
    }

    /**
     * 保存内容到缓存文件中
     */
    protected void saveResponseToSd(File toFile, String content) {
        // 删除需要删除的部分缓存
        if (content == null || content.length() < 1) {
            Log.d(TAG, "Trying to save null or 0 length content");
            return;
        }
        mCache.freeSomeCacheSpace();
        if (!toFile.getParentFile().exists()) {
            toFile.getParentFile().mkdirs();
        }
        OutputStream outStream = null;
        try {
            toFile.createNewFile();
            outStream = new FileOutputStream(toFile);
            // Log.e("encrypt","加密前：" + content);
            String encrypt = null;
            try {
                encrypt = AESencrypt.encrypt(AES_PASSWORD, content);
            } catch (Exception e) {
                e.printStackTrace();
            }
            // Log.e("encrypt","加密后：" + encrypt);
            outStream.write(encrypt.getBytes("utf-8"));
            outStream.flush();
        } catch (FileNotFoundException e) {
            Log.d(TAG, "FileNotFoundException");
        } catch (IOException e) {
            Log.d(TAG, "IOException" + e.getMessage());
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
     * 加载文件内容
     */
    protected String loadContentFile(File file) {
        if (file.exists()) {
            // 存在，则修改文件的最后修改时间
            mCache.updateFileTime(file);
            FileInputStream fis = null;
            try {
                fis = new FileInputStream(file);
                int len = fis.available();
                if (len > 0) {
                    byte[] buf = new byte[len];
                    fis.read(buf);
                    String string = new String(buf, CHARSET);
                    // aes解密
                    String decrypt = AESencrypt.decrypt(AES_PASSWORD, string);
                    // Log.e("encrypt","解密后：" + new String(decrypt));
                    return decrypt;
                }
            } catch (Exception e) {
                e.printStackTrace();
            } catch (OutOfMemoryError e) {
                e.printStackTrace();
            } finally {
                if (fis != null)
                    try {
                        fis.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
            }
        }
        return null;
    }

    /**
     * <p>将请求链接和请求参数转换为缓存文件</P>
     *
     * @return 缓存文件
     */
    protected File getRequestCacheFile() {
        if (null == mRequestUrlString || "".equals(mRequestUrlString.trim())) {
            return null;
        }
        String encodedParams = encodeParameters();
        String cacheFileName = mRequestUrlString;
        if (encodedParams != null) {
            cacheFileName += encodedParams;
        }
        cacheFileName = mCache.convertUrlToFileName(cacheFileName);
        File dir = mCache.getCacheDirectory(Cache.CACHE_TYPE_DATA);
        return new File(dir, cacheFileName);
    }
}
