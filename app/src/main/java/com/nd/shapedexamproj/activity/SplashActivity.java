package com.nd.shapedexamproj.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Handler;

import com.nd.shapedexamproj.R;
import com.nd.shapedexamproj.util.Constants;
import com.nd.shapedexamproj.util.SharedPreferUtils;
import com.tming.common.util.Log;

/**
 * 
 * @ClassName: SplashActivity
 * @Title:
 * @Description:启动页
 * @Author:XueWenJian
 * @Since:2014年5月15日21:00:10
 * @Version:1.0
 */
public class SplashActivity extends BaseActivity {

	private final static String TAG = "SplashActivity";

	private Context mContext;

	private Intent mIntent;

	@Override
	public int initResource() {

		return R.layout.splash_activity;
	}

	@Override
	public void initComponent() {

	}

	@Override
	public void initData() {
		mContext = this;
		Log.e(TAG, "initData");

		String guideSign = Constants.IS_FIRST_LAUNCH_APP ;
		if (SharedPreferUtils.getBoolean(mContext, guideSign)) {
			Editor editor = SharedPreferUtils.getEditor(mContext);
			editor.putBoolean(guideSign, false);
			editor.commit();
			mIntent = new Intent(getApplication(), GuideActivity.class);
		} else
			mIntent = new Intent(getApplication(), HomeActivity.class);
		Handler handler = new Handler();
		handler.postDelayed(new splashhandler(), 2000);
	}

	@Override
	public void addListener() {

	}

	class splashhandler implements Runnable {

		public void run() {
			startActivity(mIntent);
			SplashActivity.this.finish();
		}

	}

	private int getAppVersionCode() {
		try {
			PackageInfo pi = mContext.getPackageManager().getPackageInfo(
					mContext.getPackageName(), 0);
			return pi.versionCode;
		} catch (PackageManager.NameNotFoundException e) {
			e.printStackTrace();
			return 0;
		}
	}

}
