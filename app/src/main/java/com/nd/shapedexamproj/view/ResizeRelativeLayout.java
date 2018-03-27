package com.nd.shapedexamproj.view;


import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

/**
 * 
 * <p>监听键盘弹出使用的布局</p>
 * <p>Created by zll on 2014-7-25</p>
 */
public class ResizeRelativeLayout extends RelativeLayout{ 
    private static int count = 0; 
    private static String TAG = "ResizeLayout";
    
    public ResizeRelativeLayout(Context context, AttributeSet attrs) { 
        super(context, attrs); 
    } 
     
    private OnResizeListener mListener; 
    
    public interface OnResizeListener { 
        void OnResize(int w, int h, int oldw, int oldh); 
    } 
     
    public void setOnResizeListener(OnResizeListener l) { 
        mListener = l; 
    } 
     
    @Override 
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {     
        super.onSizeChanged(w, h, oldw, oldh); 
         
        if (mListener != null) { 
            mListener.OnResize(w, h, oldw, oldh); 
        } 
    } 
}