package com.nd.shapedexamproj.activity.inner;

import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.nd.shapedexamproj.R;
import com.nd.shapedexamproj.activity.BaseActivity;
import com.nd.shapedexamproj.model.TeachPoint;
import com.nd.shapedexamproj.util.UIHelper;

/**
 * 在线报名 步骤2<br/>
 * 确认用户选择的专业<br/>
 * 输入用户姓名和电话
 * 
 * @version 1.0.0
 * @author Abay Zhuang <br/>
 *         Create at 2014-5-23 <br/>
 */
public class RegistrationSecond extends BaseActivity {

	private static final String TAG = RegistrationSecond.class.getSimpleName();
	public static final String ARG_TEACH_POINT = TAG + ".teachpoint";
	public static final String ARG_SPECIALTY = TAG + ".specialty";
	private TextView mTitleTv;
	private Button mNextBtn;
	private ImageView mBackImgV;
	private TeachPoint mTeachPoint;

	@Override
	public int initResource() {
		return R.layout.inner_registration_second;
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
		if (getIntent() != null) {
			mTeachPoint = (TeachPoint)getIntent().getParcelableExtra(ARG_TEACH_POINT);
			
		}
	}

	@Override
	public void addListener() {
		mBackImgV.setOnClickListener(UIHelper.finish(this));
		mNextBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				UIHelper.showOnlineRegisterThird(RegistrationSecond.this);
			}
		});
	}

}
