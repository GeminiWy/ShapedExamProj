package com.tming.common.adapter;

import android.app.Activity;
import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import com.tming.common.R;
import com.tming.common.cache.image.ImageCacheTool;
import com.tming.common.util.PhoneUtil;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * <p> 适用于查看大图的ViewPage使用适配器</p>
 * <P>该适配器仅会对加载当前查看的图片进行加载大图，其余使用小图50*50进行加载或者不加载。</P>
 * <p>需要设置 {@link android.support.v4.view.ViewPager#setOnPageChangeListener(android.support.v4.view.ViewPager.OnPageChangeListener)} </p>
 * <p>Created by yusongying on 2014/10/30</p>
 */
public abstract class ImagePagerAdapter extends PagerAdapter implements ViewPager.OnPageChangeListener {

    /**
     * 对应位置的显示视图
     */
    protected HashMap<Integer, View> showingViewHashMap = new HashMap<Integer, View>();

    /**
     * android上线文
     */
    protected Context mContext;

    /**
     * 所在的ViewPage
     */
    protected ViewPager mViewPager;

    /**
     * 窗口大小，会根据窗口大小来加载图片
     */
    protected int[] windowSize;

    /**
     * 加载中使用的图片
     */
    protected int loadingImageResourceId = R.drawable.image_loading;

    /**
     * 加载错误使用的图片
     */
    protected int failLoadImageResourceId = R.drawable.loaded_fail;

    /**
     * 显示图片的ImageView ID
     */
    protected int showImageViewId = R.id.image_view_page_image_view_id;

    /**
     * 已经被移除的视图
     */
    private ArrayList<View> mRemovedViews = new ArrayList<View>();

    public ImagePagerAdapter(Activity activity, ViewPager pager) {
        mContext = pager.getContext();
        mViewPager = pager;
        windowSize = PhoneUtil.getDisplayWidthHeight(activity);
        windowSize[0] = Math.min(windowSize[0], 1024);
        windowSize[1] = Math.min(windowSize[1], 1024);
    }

    @Override
    public boolean isViewFromObject(View view, Object o) {
        return view == o;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        View convertView = null;
        if (mRemovedViews.size() > 0) {
            convertView = mRemovedViews.get(0);
            mRemovedViews.remove(0);
        } else {
            convertView = infateItemView(mContext, container);
        }
        setViewData(convertView, position);

        showingViewHashMap.put(position, convertView);
        container.addView(convertView);

        if (mViewPager.getCurrentItem() == position) {
            onPageSelected(position);
        }

        return convertView;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        View view = (View) object;
        container.removeView(view);
        showingViewHashMap.remove(position);
        mRemovedViews.add(view);

        // 回收图片资源
        ImageView imageView = (ImageView) view.findViewById(showImageViewId);
        if (imageView != null) {
            imageView.setImageResource(loadingImageResourceId);
        }
    }

    @Override
    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }

    @Override
    public void onPageScrolled(int i, float v, int i2) {}

    @Override
    public void onPageSelected(int position) {
        if (position - 1 >= 0) {
            loadImage(position - 1, false);
        }
        if (position + 1 < getCount()) {
            loadImage(position + 1, false);
        }
        loadImage(position, true);
        System.gc();
    }

    protected void loadImage(int index, boolean isLoadBigImage) {
        View view = showingViewHashMap.get(index);
        if (view == null) {
            return;
        }
        ImageView imageView = (ImageView) view.findViewById(showImageViewId);
        if (imageView != null) {
            URL url = getItemImageURL(index);
            if (isLoadBigImage) {
                ImageCacheTool.getInstance().asyncLoadBigImage(url, imageView, failLoadImageResourceId, windowSize[0], windowSize[1]);
            } else {
                imageView.setImageResource(loadingImageResourceId);
                ImageCacheTool.getInstance().asyncLoadImage(url, imageView, failLoadImageResourceId, 50, 50);
            }
        } else {
            throw new RuntimeException("请设置图片显示控件ID，imageShowId");
        }
    }

    @Override
    public void onPageScrollStateChanged(int i) {}

    /**
     * 获取要显示图片的URL地址
     * @param index 显示数据的索引
     * @return 图片URL地址
     */
    public abstract URL getItemImageURL(int index);

    /**
     * <p/>创建一个新列表显示视图</P>
     *
     * @param context Android上下文
     * @return 显示视图
     */
    public View infateItemView(Context context, ViewGroup container) {
        ImageView imageView = new ImageView(context);
        imageView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        imageView.setBackgroundColor(0xFF000000);
        imageView.setImageResource(loadingImageResourceId);
        imageView.setId(showImageViewId);
        return imageView;
    }

    /**
     * <p/>将数据显示到控件上</P>
     *
     * @param view
     * @param index
     */
    public void setViewData(View view, int index) {};
}
