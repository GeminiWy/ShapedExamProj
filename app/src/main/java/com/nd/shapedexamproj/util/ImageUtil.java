package com.nd.shapedexamproj.util;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.ImageView;

import com.nd.shapedexamproj.App;
import com.tming.common.cache.image.ImageCacheTool;
import com.tming.common.cache.image.ImageCacheTool.ImageLoadCallBack;
import com.tming.common.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * 
 * @ClassName: ImageUtil
 * @Title:
 * @Description:图片工具类
 * @Author:XueWenJian
 * @Since:2014年5月19日09:03:13
 * @Version:1.0
 */
public class ImageUtil {

	private final static String TAG = "ImageUtil";
    private ImageLoadCallBack mImageLoadCallBack;
	/**
	 * 网络图片加载
	 */
	public static Bitmap returnBitMap(String url) {
		Log.d(TAG, url);
		URL myFileUrl = null;
		Bitmap bitmap = null;
		try {
			myFileUrl = new URL(url);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		try {
			HttpURLConnection conn = (HttpURLConnection) myFileUrl.openConnection();
			conn.setDoInput(true);
			conn.connect();
			InputStream is = conn.getInputStream();
			bitmap = BitmapFactory.decodeStream(is);
			is.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return bitmap;
	}
	
	public static void asyncLoadImage(final ImageView imageView, final String imgUrl)
	{
		asyncLoadImage(imageView, imgUrl, 0, 0);
	}

    public static void asyncLoadImage(final ImageView imageView, final String imgUrl, final int errImgId)
    {
        asyncLoadImage(imageView, imgUrl, 0, errImgId);
    }

    public static void asyncLoadImage(final ImageView imageView, final String imgUrl, final int loadImgId,final int errImgId)
    {
        if (!StringUtils.isEmpty(imgUrl)) {
            try {
                ImageCacheTool.getInstance(App.getAppContext()).asyncLoadImage(new URL(imgUrl), new ImageLoadCallBack() {

                    @Override
                    public void progress(int progress) {
                        if(0 != loadImgId)
                        {
                            imageView.setImageDrawable(App.getAppContext().getResources().getDrawable(loadImgId));
                        }
                    }

                    @Override
                    public void loadResult(Bitmap bitmap) {
                        if(null == bitmap)
                        {
                            Log.e(TAG, "未能加载图片:"+imgUrl);
                            if(0 != errImgId)
                            {
                                //没有获取到图片 设置错误图片
                                imageView.setImageDrawable(App.getAppContext().getResources().getDrawable(errImgId));
                            }

                        }
                        else
                        {
                            if(null != imageView)
                            {
                                imageView.setImageBitmap(bitmap);
                            }
                        }

                    }
                });
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        }
        else
        {
            Log.e(TAG, "img url is null!!");
            if(0 != errImgId)
            {
                imageView.setBackgroundResource(errImgId);
            }
        }
    }

    public static void asyncLoadImage(final ImageView imageView, final String imgUrl, final int errImgId,ImageLoadCallBack callBack)
    {
        asyncLoadImage(imageView, imgUrl, 0, errImgId,callBack);
    }

    public static void asyncLoadImage(final ImageView imageView, final String imgUrl, final int loadImgId,final int errImgId,ImageLoadCallBack callBack)
    {
        if (!StringUtils.isEmpty(imgUrl)) {
            try {
                ImageCacheTool.getInstance(App.getAppContext()).asyncLoadImage(new URL(imgUrl),callBack);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        }
        else
        {
            Log.e(TAG, "img url is null!!");
            if(0 != errImgId)
            {
                imageView.setBackgroundResource(errImgId);
            }
        }
    }
	
}
