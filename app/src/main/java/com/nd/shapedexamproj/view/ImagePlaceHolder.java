package com.nd.shapedexamproj.view;


import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.nd.shapedexamproj.R;

/**
 * 图片占位符
 * @author zll
 * create in 2014-2-25
 */
public class ImagePlaceHolder extends RelativeLayout{
	private ImageView holder_img;
	
	public ImagePlaceHolder(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		holder_img = (ImageView) findViewById(R.id.holder_img);
	}
	
	/**
	 * 设置占位符图像
	 */
	public void setHolderImage(Drawable drawable){
		holder_img.setImageDrawable(drawable);
	}
}
