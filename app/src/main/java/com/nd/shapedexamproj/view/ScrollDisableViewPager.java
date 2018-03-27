package com.nd.shapedexamproj.view;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * <p>不可通过左右滑动切换页面的viewpager</p>
 * <p>Created by zll on 2014-11-19</p>
 */
public class ScrollDisableViewPager extends ViewPager{

    public ScrollDisableViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    
    public ScrollDisableViewPager(Context context) {
        super(context);
    }
    
    @Override
    public boolean onInterceptTouchEvent(MotionEvent arg0) {
        return false;
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent arg0) {
        return false;
    }
    
}
