/*
 * Copyright (c) 2010. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package com.tming.common.view;

import com.tming.common.util.WrapMotionEvent;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.ViewPager.LayoutParams;
import android.util.AttributeSet;
import android.util.FloatMath;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

public class TouchImageView extends ImageView {
	private final String TAG = "TouchImageView";
	private Matrix matrix = new Matrix();
	private Matrix savedMatrix = new Matrix();
	private Bitmap image;
	static final int NONE = 0;
	static final int DRAG = 1;
	static final int ZOOM = 2;
	
	int mode = NONE;

	// Remember some things for zooming
	private PointF start = new PointF();
	private PointF mid = new PointF();
	private float oldDist = 1f;
	public float sacleSum;
	private long lastTouchDownTime = -1;

	private OnClickListener listener;
	
	public TouchImageView(Context context) {
		super(context);
		init();
	}

	public TouchImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	private void init() {
		setClickable(true);
		matrix.setTranslate(1f, 1f);
		setImageMatrix(matrix);
		setScaleType(ScaleType.MATRIX);
		setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent rawEvent) {
				WrapMotionEvent event = WrapMotionEvent.wrap(rawEvent);

				// Dump touch event to log
				// dumpEvent(event);

				// Handle touch events here...
				switch (event.getAction() & MotionEvent.ACTION_MASK) {
				case MotionEvent.ACTION_UP:
					float xDiff = event.getX() - start.x;
					// int yDiff = (int) Math.abs(event.getY() - start.y);
					// Tool.log(TAG,
					// "System.currentTimeMillis() - lastTouchDownTime:" +
					// (System.currentTimeMillis() - lastTouchDownTime));
					// Tool.log(TAG, "xDiff: " + xDiff);
					if (System.currentTimeMillis() - lastTouchDownTime < 150) {
						performClick();
						// } else if(CustomGallery.MOVABLE_FLAG && activity !=
						// null && xDiff > 200) {
						// activity.showPreAlbum();
						// } else if(CustomGallery.MOVABLE_FLAG && activity !=
						// null && xDiff < -200) {
						// activity.showNextAlbum();
					}
					// CustomGallery.MOVABLE_FLAG = (sacleSum == 0 ? true :
					// false);
					break;
				case MotionEvent.ACTION_DOWN:
					lastTouchDownTime = System.currentTimeMillis();
					savedMatrix.set(matrix);
					start.set(event.getX(), event.getY());
					mode = DRAG;
					return false;
				case MotionEvent.ACTION_POINTER_DOWN:
					oldDist = spacing(event);
					if (oldDist > 10f) {
						savedMatrix.set(matrix);
						midPoint(mid, event);
						mode = ZOOM;
					}
					break;
				case MotionEvent.ACTION_POINTER_UP:
					mode = NONE;
					break;
				case MotionEvent.ACTION_MOVE:
					if (mode == ZOOM) {
						float newDist = spacing(event);
						if (newDist > 10f) {
							matrix.set(savedMatrix);
							float scale = newDist / oldDist;
							sacleSum += scale;
							matrix.postScale(scale, scale, mid.x, mid.y);
							// CustomGallery.MOVABLE_FLAG = (sacleSum == 0 ?
							// true : false);
						}
						setImageMatrix(matrix);
					} else if (mode == DRAG && sacleSum != 0) {
						matrix.set(savedMatrix);
						matrix.postTranslate(event.getX() - start.x,
								event.getY() - start.y);
						setImageMatrix(matrix);
					}
					break;
				}
				return true; // indicate event was handled
			}
		});
		setClickable(true);
		setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if(sacleSum != 0) {
					sacleSum = 0;
					centerImage();
				} else {
					if(listener != null) listener.onClick(TouchImageView.this);
				}
			}
		});
	}
	
	public void setTouchImageViewClickListener(OnClickListener listener) {
		this.listener = listener;
	}
	
	public void zoomInCenter() {
		dumpZoom(true);
	}

	public void zoomOutCenter() {
		dumpZoom(false);
	}

	public void recycle() {
		if (image != null) {
			image.recycle();
			image = null;
		}
	}
	
	/**
	 * Zoom Controls Button listener relaization method
	 * 
	 * @param zoomOut
	 */
	// TODO: Canel fix sX,sY...value
	public void dumpZoom(Boolean zoomOut) {
		int i = 0;
		// int zoomDist = 100;
		float sX = 330f;
		float sY = 600f;
		float tX = 150f;
		float tY = 150f;

		oldDist = spacingCenter(sX, sY, tX, tY);
		mid = new PointF((sX + tX) / 2, (sY + tY) / 2);
		start.set(sX, sY);

		while (i < 3) {
			float diffValue = -5f;
			if (zoomOut)
				diffValue = 5f;
			sX -= diffValue;
			sY -= diffValue;
			tX += diffValue;
			tY += diffValue;

			float newDist = spacingCenter(sX, sY, tX, tY);
			float scale = newDist / oldDist;
			matrix.postScale(scale, scale, mid.x, mid.y);
			sacleSum += scale;
			i++;
			setImageMatrix(matrix);
		}
	}

	public float spacingCenter(float sX, float sY, float tX, float tY) {
		return (float) Math.sqrt((sX - tX) * (sX - tX) + (sY - tY) * (sY - tY));
	}

	@Override
	public void setImageBitmap(Bitmap bm) {
		this.image = bm;
		centerImage();
		super.setImageBitmap(bm);
	}

	@Override
	public void onSizeChanged(int displayWidth, int displayHeight, int s, int d) {
		centerImage();
	}

	public void centerImage() {
		Bitmap _image = image;
		if (_image == null) {
			return;
		}
		int width = super.getWidth();
		int height = super.getHeight();

		float scale;
		if ((height / _image.getHeight()) >= (width / _image.getWidth())) {
			scale = (float) width / (float) _image.getWidth();
		} else {
			scale = (float) height / (float) _image.getHeight();
		}

		matrix.reset();
		savedMatrix.reset();
		setImageMatrix(matrix);
		matrix.postScale(scale, scale);
		setImageMatrix(matrix);

		float redundantYSpace = (float) height
				- (scale * (float) _image.getHeight());
		float redundantXSpace = (float) width
				- (scale * (float) _image.getWidth());

		redundantYSpace /= (float) 2;
		redundantXSpace /= (float) 2;
		savedMatrix.set(matrix);
		matrix.postTranslate(redundantXSpace, redundantYSpace);

		setImageMatrix(matrix);
	}

	/** Show an event in the LogCat view, for debugging */
	private void dumpEvent(WrapMotionEvent event) {
		// ...
		String names[] = { "DOWN", "UP", "MOVE", "CANCEL", "OUTSIDE",
				"POINTER_DOWN", "POINTER_UP", "7?", "8?", "9?" };
		StringBuilder sb = new StringBuilder();
		int action = event.getAction();
		int actionCode = action & MotionEvent.ACTION_MASK;
		sb.append("event ACTION_").append(names[actionCode]);
		if (actionCode == MotionEvent.ACTION_POINTER_DOWN
				|| actionCode == MotionEvent.ACTION_POINTER_UP) {
			sb.append("(pid ").append(
					action >> MotionEvent.ACTION_POINTER_ID_SHIFT);
			sb.append(")");
		}
		sb.append("[");
		for (int i = 0; i < event.getPointerCount(); i++) {
			sb.append("#").append(i);
			sb.append("(pid ").append(event.getPointerId(i));
			sb.append(")=").append((int) event.getX(i));
			sb.append(",").append((int) event.getY(i));
			if (i + 1 < event.getPointerCount())
				sb.append(";");
		}
		sb.append("]mid-x:" + mid.x + "|mid-y" + mid.y);
		System.out.println(sb.toString());
	}

	/** Determine the space between the first two fingers */
	private float spacing(WrapMotionEvent event) {
		// ...
		float x = event.getX(0) - event.getX(1);
		float y = event.getY(0) - event.getY(1);
		return (float) Math.sqrt(x * x + y * y);
	}

	/** Calculate the mid point of the first two fingers */
	private void midPoint(PointF point, WrapMotionEvent event) {
		// ...
		float x = event.getX(0) + event.getX(1);
		float y = event.getY(0) + event.getY(1);
		point.set(x / 2, y / 2);
	}
	
}