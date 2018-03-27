package com.nd.shapedexamproj.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import com.nd.shapedexamproj.R;
import com.tming.common.view.slidemenu.SlideMenu;

public class CloseableSlideMenu extends SlideMenu {
	
	private static final String TAG = "CanCloseSlideMenu";
	
	/**
	 * false表示没有关闭
	 */
	private boolean mIsCloseSlide = false;

	public CloseableSlideMenu(Context context, AttributeSet attrs) {
		this(context, attrs, R.attr.slideMenuStyle);
	}

	public CloseableSlideMenu(Context context) {
		this(context, null);
	}
	
	public CloseableSlideMenu(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}
	
	public void setIsCloseSlide(boolean isClosed) {
		mIsCloseSlide = isClosed;
	}
	
	public boolean ismIsCloseSlide() {
		return mIsCloseSlide;
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent event) {
		if (mIsCloseSlide) {
			return false;
		}
		return super.onInterceptTouchEvent(event);
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (mIsCloseSlide) {
			return false;
		}
		return super.onTouchEvent(event);
	}
}
