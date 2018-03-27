package com.nd.shapedexamproj.activity.inner;

import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.nd.shapedexamproj.R;
import com.nd.shapedexamproj.activity.BaseActivity;
import com.nd.shapedexamproj.util.UIHelper;

/**
 * 在线报名 步骤3<br/>
 * 在线报名结果
 * 
 * @version 1.0.0
 * @author Abay Zhuang <br/>
 *         Create at 2014-5-23 修改者，修改日期，修改内容。
 */
public class RegistrationResult extends BaseActivity {
	private TextView mTitleTv;
	private Button mNextBtn;
	private ImageView mBackImgV;

	@Override
	public int initResource() {
		return R.layout.inner_registration_result;
	}

	@Override
	public void initComponent() {
		mTitleTv = (TextView) findViewById(R.id.commonheader_title_tv);
		mTitleTv.setText(R.string.online_registration_header);
		mNextBtn = (Button) findViewById(R.id.registration_next_btn);
		mBackImgV = (ImageView) findViewById(R.id.commonheader_left_iv);

	}

	@Override
	public void initData() {
		// TODO Auto-generated method stub

	}

	@Override
	public void addListener() {
		mBackImgV.setOnClickListener(UIHelper.finish(this));
		mNextBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// UIHelper.showOnlineRegisterSecond(RegistrationResult.this);

			}
		});

	}

}
