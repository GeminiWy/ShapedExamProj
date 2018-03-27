package com.tming.common.view;

import com.tming.common.R;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
/**
 * 正在加载的布局
 * @author zll
 * create in 2014-2-25
 */
public class LoadingView extends LinearLayout{
	private  ProgressBar pg;
	private TextView loading_tv;
	
	public LoadingView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		pg = (ProgressBar) findViewById(R.id.loading_progress);
		loading_tv = (TextView) findViewById(R.id.loading_tv);
	}
	/**
	 * 设置滚动条图片资源
	 * @param resid
	 */
	public void setProgressDrawable(Drawable d){
		pg.setIndeterminateDrawable(d);
	}
	
	/**
	 * 设置文本颜色
	 * @param color
	 */
	public void setTextColor(int color){
		loading_tv.setTextColor(color);
	}
	
}
