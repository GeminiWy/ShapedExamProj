package com.nd.shapedexamproj.fragment;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;

import com.nd.shapedexamproj.R;
import com.tming.common.cache.image.ImageCacheTool;
import com.tming.common.util.Helper;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class PictureAdapter extends BaseAdapter {
	List<String> mList = new ArrayList<String>();
	LayoutInflater mInflater;
	ImageCacheTool mImageCacheTool;
    /**
     * 指定加载图片的大小
     */
    private int reqImageWH = 0;

	public PictureAdapter(Context context, List<String> list) {
		mInflater = LayoutInflater.from(context);
		mImageCacheTool = ImageCacheTool.getInstance();
		this.mList.clear();
		this.mList.addAll(list);
        reqImageWH = Helper.dip2px(context, 72);
	}

	public int getCount() {
		return mList.size();
	}

	public Object getItem(int arg0) {
		return mList.get(arg0);
	}

	public long getItemId(int arg0) {
		return 0;
	}

	/**
	 * ListView Item设置
	 */
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder = null;
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.timeline_gridview_img2,
					parent, false);
			holder = new ViewHolder();
			holder.image = (ImageView) convertView
					.findViewById(R.id.item_grida_image);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		
		if (mList.size() == 1) {
		    holder.image.setScaleType(ScaleType.FIT_CENTER);
		} else {
		    holder.image.setScaleType(ScaleType.CENTER_CROP);
		}
		
		String imgUrl = mList.get(position);
		try {
			URL url = new URL(imgUrl);
            holder.image.setImageResource(R.drawable.image_loading);//added by ysy 防止图片重复
			mImageCacheTool.asyncLoadImage(holder.image, null, url, reqImageWH, reqImageWH);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}

		// if (position == Bimp.bmp.size()) {
		// holder.image.setImageBitmap(BitmapFactory.decodeResource(
		// getResources(), R.drawable.icon_addpic_unfocused));
		// if (position == CHOOSEABLE_PIC_COUNT) {
		// holder.image.setVisibility(View.GONE);
		// }
		// } else {
		// holder.image.setImageBitmap(Bimp.bmp.get(position));
		// }

		return convertView;
	}

	public class ViewHolder {
		public ImageView image;
	}

}
