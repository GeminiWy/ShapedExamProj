/**  
 * Project Name:TMCommonLibrary  
 * File Name:HeadLoadingLayout.java  
 * Package Name:com.tming.common.view.support.pulltorefresh.internal  
 * Date:2014-6-6上午11:12:29  
 * Copyright (c) 2014, XueWenJian All Rights Reserved.  
 *  
 */

package com.tming.common.view.support.pulltorefresh.internal;

import com.tming.common.R;
import com.tming.common.view.support.pulltorefresh.PullToRefreshBase.Mode;
import com.tming.common.view.support.pulltorefresh.PullToRefreshBase.Orientation;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView.ScaleType;

/**
 * ClassName:HeadLoadingLayout <br/>
 * description: 头部加载表现的具体实现 <br/>
 * Date: 2014-6-6 上午11:12:29 <br/>
 * 
 * @author XueWenJian
 * @version
 * @since JDK 1.6
 * @see
 */
public class HeadLoadingLayout extends LoadingLayout {

	// 下拉时箭头的动画效果
	private RotateAnimation animation;
	private RotateAnimation reverseAnimation;
	private final Matrix mHeaderImageMatrix;

	private float mRotationPivotX, mRotationPivotY;

	private final boolean mRotateDrawableWhilePulling;

	public HeadLoadingLayout(Context context, Mode mode,
			Orientation scrollDirection, TypedArray attrs) {
		super(context, mode, scrollDirection, attrs);

		mRotateDrawableWhilePulling = attrs.getBoolean(
				R.styleable.PullToRefresh_ptrRotateDrawableWhilePulling, true);

		mHeaderImage.setScaleType(ScaleType.MATRIX);
		mHeaderImageMatrix = new Matrix();
		mHeaderImage.setImageMatrix(mHeaderImageMatrix);

		// 箭头向上旋转的动作
		animation = new RotateAnimation(0, -180,
				RotateAnimation.RELATIVE_TO_SELF, 0.5f,
				RotateAnimation.RELATIVE_TO_SELF, 0.5f);
		animation.setInterpolator(new LinearInterpolator());
		animation.setDuration(200);
		animation.setFillAfter(true);
		// 箭头向下旋转的动作
		reverseAnimation = new RotateAnimation(-180, 0,
				RotateAnimation.RELATIVE_TO_SELF, 0.5f,
				RotateAnimation.RELATIVE_TO_SELF, 0.5f);
		reverseAnimation.setInterpolator(new LinearInterpolator());
		reverseAnimation.setDuration(200);
		reverseAnimation.setFillAfter(true);
	}

	protected void onPullImpl(float scaleOfLayout) {

	}

	@Override
	protected void refreshingImpl() {
		mHeaderImage.clearAnimation();
		mHeaderImage.setVisibility(View.INVISIBLE);
		mHeaderProgress.setVisibility(View.VISIBLE);
	}

	@Override
	protected void resetImpl() {
		mHeaderImage.clearAnimation();
		resetImageRotation();
		mHeaderImage.setVisibility(View.VISIBLE);
		mHeaderProgress.setVisibility(View.INVISIBLE);
	}

	private void resetImageRotation() {
		if (null != mHeaderImageMatrix) {
			mHeaderImageMatrix.reset();
			mHeaderImage.setImageMatrix(mHeaderImageMatrix);
		}
	}

	@Override
	protected void pullToRefreshImpl() {
		// NO-OP
		mHeaderImage.clearAnimation();
		mHeaderImage.startAnimation(animation);
	}

	@Override
	protected void releaseToRefreshImpl() {
		// NO-OP
		mHeaderImage.clearAnimation();
		mHeaderImage.startAnimation(reverseAnimation);
	}

	@Override
	protected int getDefaultDrawableResId() {
		return R.drawable.arrow;
	}

	@Override
	public void onLoadingDrawableSet(Drawable imageDrawable) {
		if (null != imageDrawable) {
			mRotationPivotX = Math
					.round(imageDrawable.getIntrinsicWidth() / 2f);
			mRotationPivotY = Math
					.round(imageDrawable.getIntrinsicHeight() / 2f);
		}
	}

}
