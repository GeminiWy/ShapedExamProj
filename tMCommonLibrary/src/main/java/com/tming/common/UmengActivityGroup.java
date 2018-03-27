package com.tming.common;

import com.umeng.analytics.MobclickAgent;

import android.app.ActivityGroup;

public class UmengActivityGroup extends ActivityGroup {
	
	@Override
	protected void onPause() {
		super.onPause();
		MobclickAgent.onPause(this);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		MobclickAgent.onResume(this);
	}
	
	protected void exitApp() {
		MobclickAgent.onKillProcess(this);
		System.exit(0);
	}
	
}
