package com.nd.shapedexamproj.view;


import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import com.tming.common.util.Helper;


/**
 * 
 *@Description: 用户表情Adapter类
 *@Author:Abay Zhuang
 *@Since:2014-5-13下午8:11:22 
 *@Version:1.0
 */
public class GridViewFaceAdapter extends BaseAdapter {
	// 定义Context
	private Context mContext;
	// 定义整型数组 即图片源
	private int[] mImageIds ;
	//当前是第几页，从0开始
	private int currentPageNum;
	
	public int[] getImageIds() {
		return mImageIds;
	}

	public GridViewFaceAdapter(Context c, int[] mImageIds,int currentPageNum) {
		mContext = c;
		this.mImageIds = mImageIds;
		this.currentPageNum = currentPageNum;
	}

	// 获取图片的个数
	public int getCount() {
		return mImageIds.length;
	}

	// 获取图片在库中的位置
	public Object getItem(int position) {
		return position;
	}

	// 获取图片ID
	public long getItemId(int position) {
		return mImageIds[position];
	}
	
	public View getView(int position, View convertView, ViewGroup parent) {
		ImageView imageView;
		if (convertView == null) {
			imageView = new ImageView(mContext);
			int width = Helper.dip2px(mContext , 47);
			int height = Helper.dip2px(mContext , 47);
			int PADDING = Helper.dip2px(mContext , 8);
			// 设置图片n×n显示
			imageView.setLayoutParams(new GridView.LayoutParams(width, height));
			imageView.setPadding(PADDING, PADDING, PADDING, PADDING);
			// 设置显示比例类型
			imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
			convertView = imageView;
		} else {
			imageView = (ImageView) convertView;
		}
		imageView.setImageResource(mImageIds[position]);
		String faceCode = InputBottomBar.getFaceCode(position,currentPageNum);
		imageView.setTag("[" + faceCode + "]");
		return imageView;
	}
}