package com.nd.shapedexamproj.util;

import android.view.View;
import android.view.animation.*;
/**
 * <p>  AnimationUtil 动画类 </p>
 * <p> Created by xuwenzhuo on 2014/11/14.</p>
 */
public class AnimationUtil {


    /**
     * <p>  视图旋转 ，顺时针</p>
     * @param view
     * @param currAngle
     */
    public static void positive(View view,float currAngle) {
        Animation anim = new RotateAnimation(currAngle, currAngle + 180, Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f);
        /** 匀速插值器 */
        LinearInterpolator lir = new LinearInterpolator();
        anim.setInterpolator(lir);
        anim.setDuration(1000);
        /** 动画完成后不恢复原状 */
        anim.setFillAfter(true);
        currAngle += 180;
        if (currAngle > 360) {
            currAngle = currAngle - 360;
        }
        view.startAnimation(anim);
    }

    /**
     * <p>视图旋转，逆时针</p>
     * @param view
     * @param currAngle
     */
    public static void negative(View view,float currAngle) {
        Animation anim = new RotateAnimation(currAngle, currAngle - 180, Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f);
        /** 匀速插值器 */
        LinearInterpolator lir = new LinearInterpolator();
        anim.setInterpolator(lir);
        anim.setDuration(1000);
        /** 动画完成后不恢复原状 */
        anim.setFillAfter(true);
        currAngle -= 180;
        if (currAngle < -360) {
            currAngle = currAngle + 360;
        }
        view.startAnimation(anim);
    }

    /**
     * <p> 淡出效果 </p>
     * @param view 动画视图
     * @param dura 持续时间
     */
    public static void alpha(View view,int dura) {
        Animation anim = new AlphaAnimation(1.0f, 0.0f);
        anim.setDuration(dura);
        anim.setFillAfter(true);
        view.startAnimation(anim);
    }

    /**
     * <p> 视图移动 </p>
     * @param view 动画视图
     */
    public static void translate(View view) {
        Animation anim = new TranslateAnimation(200, 0, 300, 0);
        anim.setDuration(2000);
        anim.setFillAfter(true);
        OvershootInterpolator overshoot = new OvershootInterpolator();
        anim.setInterpolator(overshoot);
        view.startAnimation(anim);
    }

    /**
     * <p></p>
     * @param view
     */
    public static void sclae(View view) {
        Animation anim = new ScaleAnimation(2.0f, 1.0f, 2.0f, 1.0f,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f);
        anim.setDuration(2000);
        anim.setFillAfter(true);
        BounceInterpolator bounce = new BounceInterpolator();
        anim.setInterpolator(bounce);
        view.startAnimation(anim);
    }
}
