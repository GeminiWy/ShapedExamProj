package com.nd.shapedexamproj.view;

/**
 * 
 * 视差特效
 * 
 * @author Linlg
 * 
 *         Create on 2014-5-15
 */
public class TouchTool {
	private int startX, startY;
	private int endX, endY;

	public TouchTool(int startX, int startY, int endX, int endY) {
		super();
		this.startX = startX;
		this.startY = startY;
		this.endX = endX;
		this.endY = endY;
	}

	public int getScrollX(float dx) {
		int xx = (int) (startX + dx / 2.5F);
		return xx;
	}

	public int getScrollY(float dy) {
		int yy = (int) (startY + dy / 2.5F);
		return yy;
	}
}