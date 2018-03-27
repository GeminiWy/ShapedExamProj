package com.tming.common.view;

import com.tming.common.R;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * 加载错误页
 * @author zll
 * create in 2014-2-25
 */
public class ErrorView extends RelativeLayout{
	private ImageView error_icon;
	private TextView error_tv;
	private Button error_btn ;
	
	public ErrorView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initComponent();
	}
	
	private void initComponent(){
		error_icon = (ImageView) findViewById(R.id.error_icon);
		error_tv = (TextView) findViewById(R.id.error_tv);
		error_btn = (Button) findViewById(R.id.error_btn);
		
	}
	/**
	 * 设置错误图片
	 */
	public void setErrorIcon(Drawable drawable){
		error_icon.setImageDrawable(drawable);
	}
	/**
	 * 设置按钮背景样式
	 */
	public void setButtonBackground(Drawable d){
		error_btn.setBackgroundDrawable(d);
	}
	/**
	 * 设置文字
	 */
	public void setText(String text){
		error_tv.setText(text);
	}
	/**
	 * 设置文字颜色
	 */
	public void setTextColor(int color){
		error_tv.setTextColor(color);
	}
}
