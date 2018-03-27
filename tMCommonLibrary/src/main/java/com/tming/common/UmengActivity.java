package com.tming.common;

import com.umeng.analytics.MobclickAgent;

import android.app.Activity;

public class UmengActivity extends Activity {
	
	@Override
	protected void onPause() {
		super.onPause();
		MobclickAgent.onResume(this);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		MobclickAgent.onPause(this);
	}
}
