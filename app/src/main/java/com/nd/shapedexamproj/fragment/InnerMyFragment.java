package com.nd.shapedexamproj.fragment;

import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;

import com.nd.shapedexamproj.App;
import com.nd.shapedexamproj.R;
import com.nd.shapedexamproj.activity.SettingActivity;
import com.nd.shapedexamproj.util.UIHelper;

/**
 * 
 * 基本功能 定义 ，定义流程模板。
 * @version 1.0.0 
 * @author Abay Zhuang <br/>
 *		   Create at 2014-5-22
 */
public class InnerMyFragment extends BaseFlowFragment{
	private Button mSignUpBtn;
	private ImageView mySettingBtn;

	@Override
	public int initResource() {
		return R.layout.my_inner;
	}

	@Override
	public void initComponent(View view) {
		mSignUpBtn = (Button) view.findViewById(R.id.my_login_btn);
		mySettingBtn = (ImageView) view.findViewById(R.id.my_setting_btn);
	}

	@Override
	public void initData() {
		
	}

	@Override
	public void initListener() {
		mSignUpBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				UIHelper.showLogin(getBaseActivity());
			}
		});
		mySettingBtn.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                Intent settingIntent = new Intent(App.getAppContext(),SettingActivity.class);
                startActivity(settingIntent);
            }
        });
		
	}
	
	
}
