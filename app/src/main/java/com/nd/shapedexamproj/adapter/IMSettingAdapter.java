package com.nd.shapedexamproj.adapter;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.nd.shapedexamproj.R;
import com.tming.common.adapter.ImageBaseAdatapter;
import com.tming.common.util.Helper;

import java.net.MalformedURLException;
import java.net.URL;
/**
 * 聊天设置头像适配器（暂时没用）
 * @author Linlg
 *
 */
public class IMSettingAdapter extends ImageBaseAdatapter<String> {
	
	private int width;
	public IMSettingAdapter(Context context) {
		super(context);
		width = Helper.dip2px(context, 75);
	}

	@Override
	public View infateItemView(Context context) {
		View itemView = View.inflate(context, R.layout.msg_talk_setting_photo_item, null);
		BaseViewHolder holder = new BaseViewHolder();
		holder.imageView = (ImageView)itemView.findViewById(R.id.item_image);
		holder.imageView.setLayoutParams(new LinearLayout.LayoutParams(width, width));
		itemView.setTag(holder);
		return itemView;
	}

	@Override
	public URL getDataImageUrl(String data) {
		try {
			return new URL(data);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public long getItemId(String data) {
		return data.hashCode();
	}

	@Override
	public void setViewHolderData(
			com.tming.common.adapter.ImageBaseAdatapter.BaseViewHolder arg0,
			String arg1) {
		
	}


}
