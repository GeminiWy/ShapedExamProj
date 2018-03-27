package com.nd.shapedexamproj.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Scroller;

import com.nd.shapedexamproj.R;

/**
 * 
 * 视差特效
 * 
 * @author Linlg
 * 
 *         Create on 2014-5-15
 */
public class CustomScrollLayout extends LinearLayout {
	private RelativeLayout changeIv;
	private RelativeLayout detailLayout;
	private int left, top;
	private float startX, startY;
	private float currentX, currentY;
	private int rootWidth, rootHeight;
	private int changeIvHeight;
	private Scroller scroller;
	private TouchTool touchTool;
	/**
	 * 最大移动距离，实际情况可以变化
	 */
	private static int MAX_MOVE = 200;

	public CustomScrollLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	protected void onFinishInflate() {
		super.onFinishInflate();
		changeIv = (RelativeLayout) findViewById(R.id.my_cover_iv);
		/*detailLayout = (RelativeLayout) findViewById(R.id.my_detail_rl);*/
		setLongClickable(true);
		scroller = new Scroller(getContext(), new AccelerateDecelerateInterpolator());
	}

	public boolean onTouchEvent(MotionEvent event) {
		int action = event.getAction();
		if (!scroller.isFinished()) {
			return super.onTouchEvent(event);
		}
		currentX = event.getX();
		currentY = event.getY();
		switch (action) {
		case MotionEvent.ACTION_DOWN:
			left = detailLayout.getLeft();
			top = detailLayout.getTop();

			rootWidth = getWidth();
			rootHeight = getHeight();

			changeIvHeight = changeIv.getHeight();

			startX = currentX;
			startY = currentY;
			// TouchTool 是滑动工具，是下面的imageview移动的时候，计算能够移动的距离
			touchTool = new TouchTool(detailLayout.getLeft(), detailLayout.getTop(),
					detailLayout.getLeft(), detailLayout.getTop() + MAX_MOVE);
			break;
		case MotionEvent.ACTION_MOVE:
			if (touchTool != null) {

				int l = touchTool.getScrollX(currentX - startX);
				int t = touchTool.getScrollY(currentY - startY);
				if (t >= top && t <= detailLayout.getTop() + MAX_MOVE) {
					// 滑动时候，重新定位上面的布局和下面的布局
					detailLayout.layout(left, t, left + detailLayout.getWidth(),
							t + detailLayout.getHeight());
					changeIv.layout(0, 0, changeIv.getWidth(), t);
				}
			}
			break;
		case MotionEvent.ACTION_UP:
			// 抬起手的时候，慢慢的换到原先位置
			scroller.startScroll(detailLayout.getLeft(), detailLayout.getTop(),
					0 - detailLayout.getLeft(), changeIvHeight - detailLayout.getTop(), 200);
			invalidate();
			break;
		}
		return super.onTouchEvent(event);
	}

	public void computeScroll() {
		if (scroller.computeScrollOffset()) {
			int x = scroller.getCurrX();
			int y = scroller.getCurrY();
			changeIv.layout(0, 0, x + changeIv.getWidth(), y);
			detailLayout.layout(x, y, x + detailLayout.getWidth(), y + detailLayout.getHeight());
			invalidate();
		}
	}

}
