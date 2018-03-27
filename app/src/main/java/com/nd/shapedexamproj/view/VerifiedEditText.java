package com.nd.shapedexamproj.view;


import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nd.shapedexamproj.R;

/**
 * 可验证提示的文本框
 * @author zll
 * create in 2014-2-25
 */
public class VerifiedEditText extends RelativeLayout{
	private EditText verified_et;
	private TextView verified_tv;
	private Drawable et_drawable,normal_et_bg;
	
	public VerifiedEditText(Context context, AttributeSet attrs) {
		super(context, attrs);
		
	}
	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		verified_et = (EditText) findViewById(R.id.verified_et);
		verified_tv = (TextView) findViewById(R.id.verified_tv);
		
		et_drawable = getResources().getDrawable(R.drawable.verified_error_et_bg);
		normal_et_bg = getResources().getDrawable(R.drawable.normal_et_bg);
	}
	
	/**
	 * 错误提示
	 */
	public void showErrorAlert(){
		verified_et.setBackgroundDrawable(et_drawable);
		verified_tv.setVisibility(View.VISIBLE);
	}
	/**
	 * 取消错误提示
	 */
	public void hideErrorAlert(){
		verified_et.setBackgroundDrawable(normal_et_bg);
		verified_tv.setVisibility(View.GONE);
	}
}
