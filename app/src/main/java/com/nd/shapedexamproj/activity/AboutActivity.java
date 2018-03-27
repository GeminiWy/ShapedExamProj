package com.nd.shapedexamproj.activity;

import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.nd.shapedexamproj.R;

/**
 * 关于界面
 * @author zll
 * create in 2014-4-10
 */
public class AboutActivity extends BaseActivity{
	
	private ImageView commonheader_left_iv;
	private TextView commonheader_title_tv;
//	private Button common_head_left_btn;
	
	@Override
	public int initResource() {
		return R.layout.about_activity;
	}

	@Override
	public void initComponent() {
		findViewById(R.id.common_head_right_btn).setVisibility(View.GONE);
		commonheader_left_iv = (ImageView) findViewById(R.id.commonheader_left_iv);
		commonheader_title_tv = (TextView) findViewById(R.id.commonheader_title_tv);
		commonheader_title_tv.setText(getResources().getString(R.string.about));
//		common_head_left_btn = (Button) findViewById(R.id.common_head_left_btn);
//		common_head_left_btn.setText(getResources().getString(R.string.about));
	}

	@Override
	public void initData() {
		
	}

	@Override
	public void addListener() {
		commonheader_left_iv.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				finish();
			}
		});
	}

}
