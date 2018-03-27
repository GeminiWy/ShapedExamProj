/**  
 * Project Name:OpenUniversity  
 * File Name:ContactPersonAddActivity.java  
 * Package Name:com.tming.openuniversity.activity.im  
 * Date:2014-6-9下午11:38:17  
 * Copyright (c) 2014, XueWenJian All Rights Reserved.  
 *  
 */

package com.nd.shapedexamproj.activity.im;

import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nd.shapedexamproj.R;
import com.nd.shapedexamproj.activity.BaseActivity;
import com.nd.shapedexamproj.util.UIHelper;

/**
 * <p> ContactPersonAddActivity IM-添加好友</p>
 * <p>Created by xuewenjian  2014/6/9 </p>
 * <p>Modified by xuwenzhuo 2014/11/18</p>
 */
public class ContactPersonAddActivity extends BaseActivity {

	private static final String TAG = "ContactPersonAddActivity";
	private Context mContext;

	private RelativeLayout mHeadRL;
	private ImageView mBackIV;
	private TextView mHeadTitleTV;
	private Button mHeadRightBtn;
	
	private RelativeLayout mSearchRL;

	@Override
	public int initResource() {
		return R.layout.im_contactpersonadd_activity;
	}

	@Override
	public void initComponent() {
		mHeadRL = (RelativeLayout) findViewById(R.id.common_head_layout);
		mBackIV = (ImageView) findViewById(R.id.commonheader_left_iv);
		mHeadTitleTV = (TextView) findViewById(R.id.commonheader_title_tv);
		mHeadRightBtn = (Button) findViewById(R.id.commonheader_right_btn);
		mSearchRL = (RelativeLayout) findViewById(R.id.contactperson_search_rl);
		mHeadTitleTV.setText(R.string.im_personadd_title);
		mHeadRightBtn.setVisibility(View.INVISIBLE);
	}

	@Override
	public void initData() {
		mContext = this;

	}

	@Override
	public void addListener() {
		
		mSearchRL.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
                //进入搜索界面
				UIHelper.showPersonSearchActivity(mContext);
				
			}
		});
		
		mBackIV.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				ContactPersonAddActivity.this.finish();

			}
		});
		
		mHeadTitleTV.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				ContactPersonAddActivity.this.finish();
				
			}
		});

	}

}
