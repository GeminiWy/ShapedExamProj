package com.nd.shapedexamproj.fragment;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.nd.shapedexamproj.R;
import com.nd.shapedexamproj.photoview.PhotoViewAttacher;
import com.tming.common.cache.image.ImageCacheTool;
import com.tming.common.util.BitmapUtil;
import com.tming.common.util.PhoneUtil;

import java.lang.ref.WeakReference;
import java.net.MalformedURLException;
import java.net.URL;
/**
 * 
 * <p>图片预览</p>
 * <p>Created by zll on 2014-8-26</p>
 */
public class ImageDetailFragment extends BaseFragment {
    
    private int imagePathTypeVal;
    private String mImageUrl;
    
    private ImageView mImageView;
    private ProgressBar progressBar;
    private PhotoViewAttacher mAttacher;
    private ImageCacheTool mImageCacheTool;
    //网络图片—1,本地图片—0
    int[] windowWH;

    private WeakReference<Bitmap> mSmallImageWeakReference;
    private WeakReference<Bitmap> mBigImageWeakReference;
    private Callback mImageLoadCallback;

    public static ImageDetailFragment newInstance(String imageUrl) {
        ImageDetailFragment imgFragment = new ImageDetailFragment();
        Bundle args = new Bundle();
        args.putString("url", imageUrl);
        imgFragment.setArguments(args);
        return imgFragment;
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mImageUrl = getArguments() != null ? getArguments().getString("url") : null;
        windowWH = PhoneUtil.getDisplayWidthHeight(getActivity());
        windowWH[0] = Math.min(windowWH[0], 1024);
        windowWH[1] = Math.min(windowWH[1], 1024);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.image_detail_fragment, container, false);
        mImageView = (ImageView) v.findViewById(R.id.image);
        mImageCacheTool = ImageCacheTool.getInstance();
        mAttacher = new PhotoViewAttacher(mImageView);
        mAttacher.setOnPhotoTapListener(new PhotoViewAttacher.OnPhotoTapListener() {

            @Override
            public void onPhotoTap(View arg0, float arg1, float arg2) {
                getActivity().finish();
            }
        });
        progressBar = (ProgressBar) v.findViewById(R.id.loading);
        return v;
    }

    public void recycleBigImage() {
        mImageView.setImageResource(R.drawable.image_loading);
        Bitmap bitmap = null;
        if (mBigImageWeakReference != null && (bitmap = mBigImageWeakReference.get()) != null) {
            BitmapUtil.doRecycledIfNot(bitmap);
            mBigImageWeakReference = null;
        }
    }

    public void startLoadImage(boolean isLoadBigImage) {
        try {
            if (!isLoadBigImage) {
                recycleBigImage();
            }
            System.gc();

            if (mImageLoadCallback != null) {// 释放回调
                mImageLoadCallback.weakReferenceAttacher = null;
                mImageLoadCallback.weakReferenceImageView = null;
                mImageLoadCallback.weakReferenceProgressBar = null;
            }

            Bitmap bitmap = null;
            if (!isLoadBigImage && mSmallImageWeakReference != null && (bitmap = mSmallImageWeakReference.get()) != null) {
                mImageView.setImageBitmap(bitmap);
                mAttacher.update();
                return;
            }

            URL url = new URL(mImageUrl);
            mImageLoadCallback = new Callback(mImageView, progressBar, mAttacher, isLoadBigImage);
            if (isLoadBigImage) {
                mImageCacheTool.asyncLoadBigImage(url, mImageLoadCallback, windowWH[0], windowWH[1]);
            } else {
                mImageCacheTool.asyncLoadImage(url, mImageLoadCallback, 50, 50);
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    private class Callback implements ImageCacheTool.ImageLoadCallBack {

        private WeakReference<ImageView> weakReferenceImageView;
        private WeakReference<ProgressBar> weakReferenceProgressBar;
        private WeakReference<PhotoViewAttacher> weakReferenceAttacher;
        private boolean isLoadBigImage;

        public Callback(ImageView imageView, ProgressBar progressBar1, PhotoViewAttacher attacher, boolean isBigImage) {
            weakReferenceImageView = new WeakReference<ImageView>(imageView);
            weakReferenceProgressBar = new WeakReference<ProgressBar>(progressBar1);
            weakReferenceAttacher = new WeakReference<PhotoViewAttacher>(attacher);
            isLoadBigImage = isBigImage;
        }

        @Override
        public void progress(int progress) {}

        @Override
        public void loadResult(Bitmap bitmap) {
            ProgressBar progressBar1 = null;
            if (weakReferenceProgressBar != null && (progressBar1 = weakReferenceProgressBar.get()) != null) {
                progressBar1.setVisibility(View.GONE);
            }
            ImageView imageView = null;
            if (weakReferenceImageView != null && (imageView = weakReferenceImageView.get()) != null) {
                if (bitmap != null) {
                    if (!isLoadBigImage) {
                        mSmallImageWeakReference = new WeakReference<Bitmap>(bitmap);
                    } else {
                        mBigImageWeakReference = new WeakReference<Bitmap>(bitmap);
                    }
                    imageView.setImageBitmap(bitmap);
                    imageView.setVisibility(View.VISIBLE);
                } else {
                    if (!isLoadBigImage) {
                        imageView.setImageResource( R.drawable.loaded_fail);
                        // 加载失败不能改变imagevi的scaleType，否则会导致崩溃
                        // imageView.setScaleType(ScaleType.CENTER_INSIDE);
                    }
                    // 加载大图失败，开始加载小图
                    if (mSmallImageWeakReference == null || mSmallImageWeakReference.get() == null) {
                        startLoadImage(false);
                    }
                }
            }
            PhotoViewAttacher attacher = null;
            if (weakReferenceAttacher != null && (attacher = weakReferenceAttacher.get()) != null) {
                attacher.update();
            }
        }
    }
}
