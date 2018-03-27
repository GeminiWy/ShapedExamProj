package com.nd.shapedexamproj.view;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;

import java.util.Hashtable;

/**
 * 自定义可实现自动换行布局
 * @author zll
 * create in 2014-3-4
 */
public class PredictLayout extends LinearLayout {
	int mLeft, mRight, mTop, mBottom;
	private final static int MARGIN = -10;
	Hashtable map = new Hashtable();
	
	private Context context;
	
	public PredictLayout(Context context) {
		super(context);
		this.context = context;
	}

	public PredictLayout(Context context, int horizontalSpacing,
			int verticalSpacing) {
		super(context);
		this.context = context;
	}

	public PredictLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.context = context;
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

		int mWidth = MeasureSpec.getSize(widthMeasureSpec);
		int mCount = getChildCount();
		//mX和mY用来控制换行
		int mX = 0;
		int mY = 0;
		mLeft = 0;
		mRight = 0;
		mTop = 0;
		mBottom = 0;

		int j = 0;

		View lastview = null;
		for (int i = 0; i < mCount; i++) {
			final View child = getChildAt(i);

			child.measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED);
			// 此处增加onlayout中的换行判断，用于计算所需的高度
			int childw = child.getMeasuredWidth();
			int childh = child.getMeasuredHeight();
			mX += childw+MARGIN; // 将每次子控件宽度进行统计叠加，如果大于设定的高度则需要换行，高度即Top坐标也需重新设置

			Position position = new Position();
			mLeft = getPosition(i - j, i);
			mRight = mLeft + child.getMeasuredWidth();
			
			if (mX >= mWidth) {
				mX = childw;
				mY += childh;
				j = i;
				mLeft = 0;
				mRight = mLeft + child.getMeasuredWidth();
				mTop = mY + 10;		//数字10控制每行间距
				// PS：如果发现高度还是有问题就得自己再细调了
			}
			mBottom = mTop + child.getMeasuredHeight() + 15;//数字15用来控制布局最底部边距
			mY = mTop; // 每次的高度必须记录 否则控件会叠加到一起
			position.left = mLeft;
			position.top = mTop + 3;
			position.right = mRight;
			position.bottom = mBottom;
			map.put(child, position);
		}
		setMeasuredDimension(mWidth, mBottom);
	}

	@Override
	protected LayoutParams generateDefaultLayoutParams() {
		return new LayoutParams(1, 1); // default of 1px spacing
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {

		int count = getChildCount();
		for (int i = 0; i < count; i++) {
			View child = getChildAt(i);
			Position pos = (Position) map.get(child);
			if (pos != null) {
				child.layout(pos.left, pos.top, pos.right, pos.bottom); //指派child所在的位置
			} else {
				Log.i("MyLayout", "error");
			}
		}
	}

	private class Position {
		int left, top, right, bottom;
	}

	public int getPosition(int IndexInRow, int childIndex) {
		if (IndexInRow > 0) {
			return getPosition(IndexInRow - 1, childIndex - 1)
					+ getChildAt(childIndex - 1).getMeasuredWidth() + MARGIN;
		}
		return getPaddingLeft();
	}
}