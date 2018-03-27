package com.tming.common.view.support.pulltorefresh.internal;



import android.util.TypedValue;

import com.tming.common.CommonApp;


public class Utility {

    private Utility() {
        // Forbidden being instantiated.
    }
    
    public static int dip2px(int dipValue) {
        float reSize = CommonApp.getAppContext().getResources().getDisplayMetrics().density;
        return (int) ((dipValue * reSize) + 0.5);
    }

    public static int px2dip(int pxValue) {
        float reSize = CommonApp.getAppContext().getResources().getDisplayMetrics().density;
        return (int) ((pxValue / reSize) + 0.5);
    }

    public static float sp2px(int spValue) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, spValue,
        		CommonApp.getAppContext().getResources().getDisplayMetrics());
    }
 
}

