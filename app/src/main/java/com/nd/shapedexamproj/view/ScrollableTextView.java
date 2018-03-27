package com.nd.shapedexamproj.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;
/**
 * 文字可自动滚动的TextView
 * @author zll
 * create in 2014-2-24
 */
public class ScrollableTextView extends TextView{

	public ScrollableTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
		
	}
	@Override
	public boolean isFocused() {
		return true;
	}
}
