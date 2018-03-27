package com.tming.common.view;


import com.tming.common.R;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.PathEffect;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.Xfermode;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.widget.ProgressBar;

public class CircleProgress extends ProgressBar {
	
	private final int TYPE_IMAGE = 0;
	private final int TYPE_IMAGE_1 = 1;
	
	int progress = 0;
	int centX, centY, radius;
	RectF rectF2 = new RectF();
	Rect rectF = new Rect();
	PaintFlagsDrawFilter paintFlagsDrawFilter = null;
	static Bitmap[] bitmaps = new Bitmap[6];
	static Bitmap bgBitmap[] = new Bitmap[2];
	Paint paint = new Paint();
	private final Object lock = new Object();
	int type = 0;
	public CircleProgress(Context context, AttributeSet attrs) {
		super(context, attrs);
		if(bitmaps[0] == null) {
			synchronized (lock) {
				if(bitmaps[0] == null) {
					bitmaps[0] = BitmapFactory.decodeResource(getResources(), R.drawable.circle_1);
					bitmaps[1] = BitmapFactory.decodeResource(getResources(), R.drawable.circle_2);
					bitmaps[2] = BitmapFactory.decodeResource(getResources(), R.drawable.circle_3);
					bitmaps[3] = BitmapFactory.decodeResource(getResources(), R.drawable.circle_4);
					bitmaps[4] = BitmapFactory.decodeResource(getResources(), R.drawable.circle_5);
					bitmaps[5] = BitmapFactory.decodeResource(getResources(), R.drawable.circle_6);
					
					bgBitmap[0] = BitmapFactory.decodeResource(getResources(), R.drawable.img_bg);
					bgBitmap[1] = BitmapFactory.decodeResource(getResources(), R.drawable.black_loading);
				}
			}
		}
		TypedArray a =
	            context.obtainStyledAttributes(attrs, R.styleable.CircelProgress, 0, 0);
		type = a.getInt(R.styleable.CircelProgress_type, 0);
		a.recycle();
		rectF.right = bitmaps[0].getWidth();
		rectF.bottom = bitmaps[0].getHeight();
	}
	
	@Override
	protected synchronized void onDraw(Canvas canvas) {
		Bitmap bitmap = type == 0 ? bgBitmap[0] : bgBitmap[1];
		canvas.drawBitmap(bitmap, centX - bitmap.getWidth() / 2, (float) centY - (float) bitmap.getHeight() / 2, paint);
		int i = progress / 20;
		if(i > 5) {
			i = 5;
		} else if(i < 0) {
			i = 0;
		}
		canvas.drawBitmap(bitmaps[i], rectF, rectF2, paint);
	}
	
	@Override
	protected void onLayout(boolean changed, int left, int top, int right,
			int bottom) {
		super.onLayout(changed, left, top, right, bottom);
		if(changed) {
			centX = (right - left) / 2;
			centY = (bottom - top) / 2;
			if(centX > centY) {
				radius = centY;
			} else {
				radius = centX;
			}
//			margin = (int) (radius * 0.2);
//			rectF.left = centX - radius + margin;
//			rectF.top = centY - radius + margin;
//			rectF.right = centX + radius - margin;
//			rectF.bottom = centY + radius - margin;
//			strokeWidth = (float) (radius * 0.5);
			
			rectF2.left = centX - rectF.right / 2;
			rectF2.top = centY - rectF.right / 2;
			rectF2.right = centX + rectF.right / 2;
			rectF2.bottom = centY + rectF.right / 2;
		}
	}
    
    int max;
    
    @Override
    public synchronized void setMax(int max) {
    	this.max = max;
    	super.setMax(max);
    }
    
    @Override
    public synchronized void setProgress(int progress) {
    	this.progress = (int) ((double) (100 * progress) / (double) max);
//    	int time = 0;
//    	if(_angel - angel > 2) {
//    		int count = _angel - angel;
//    		int preWhat = _angel - angel / (count < 10 ? count : 10);
//    		int preTime = count > 10 ? 1000 / count : 1000;
//    		for(int i = 0; i < count || i < 10; i++) {
//    			handler.sendEmptyMessageDelayed(angel + preWhat,  time + preTime * i);
//    		}
//    	} else {
//    		angel = _angel;
//    	}
    	invalidate();
    	super.setProgress(progress);
    }
    
    public void resetProgress() {
    	progress = 0;
    	invalidate();
    }
    
}
