/**
 * 
 */
package com.nd.shapedexamproj.util;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.*;
import android.graphics.PorterDuff.Mode;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ImageView;

import com.nd.shapedexamproj.R;

/**
 * @author Caiyx
 * 2014-5-12
 */
public class CircleImageView extends android.support.v7.widget.AppCompatImageView {
	
	private Context context;
	
	public CircleImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.context = context;
		init();
	}

	public CircleImageView(Context context) {
		super(context);
		init();
	}

	private final RectF roundRect = new RectF();
	private float rect_adius = 45;
	private final Paint maskPaint = new Paint();
	private final Paint zonePaint = new Paint();

	private void init() {
		maskPaint.setAntiAlias(true);
		maskPaint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
		//
		zonePaint.setAntiAlias(true);
		zonePaint.setColor(Color.WHITE);
		//
		float density = getResources().getDisplayMetrics().density;
		rect_adius = rect_adius * density;
	}

	public void setRectAdius(float adius) {
		rect_adius = adius;
		invalidate();
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right,
			int bottom) {
		super.onLayout(changed, left, top, right, bottom);
		int w = getWidth();
		int h = getHeight();
		roundRect.set(0, 0, w, h);
	}

	@Override
	public void draw(Canvas canvas) {
		try{
			canvas.saveLayer(roundRect, zonePaint, Canvas.ALL_SAVE_FLAG);
			canvas.drawRoundRect(roundRect, rect_adius, rect_adius, zonePaint);
			canvas.saveLayer(roundRect, maskPaint, Canvas.ALL_SAVE_FLAG);
			super.draw(canvas);
			canvas.restore();
		}catch (Exception e) {
			Log.e("CircleImageView", "Exception e");
			Bitmap defaultBitmap =  BitmapFactory.decodeResource(context.getResources(), R.drawable.all_use_icon_photo);
			canvas.saveLayer(roundRect, zonePaint, Canvas.ALL_SAVE_FLAG);
			canvas.drawRoundRect(roundRect, rect_adius, rect_adius, zonePaint);
			canvas.saveLayer(roundRect, maskPaint, Canvas.ALL_SAVE_FLAG);
			canvas.drawBitmap(defaultBitmap, 0, 0, null); 
			canvas.restore();
		}
	}
	
	// 从资源中获取Bitmap
	public static Bitmap getBitmapFromResources(Activity act, int resId) {
		Resources res = act.getResources();
		return BitmapFactory.decodeResource(res, resId);
	}


}
