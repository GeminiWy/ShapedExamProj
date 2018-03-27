/*
 * Copyright (c) 2010. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package com.tming.common.util;

import android.view.MotionEvent;

public class EclairMotionEvent extends WrapMotionEvent {

   protected EclairMotionEvent(MotionEvent event) {
      super(event);
   }

   public float getX(int pointerIndex) {
      return event.getX(pointerIndex);
   }

   public float getY(int pointerIndex) {
      return event.getY(pointerIndex);
   }

   public int getPointerCount() {
      return event.getPointerCount();
   }

   public int getPointerId(int pointerIndex) {
      return event.getPointerId(pointerIndex);
   }
}