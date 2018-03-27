package com.nd.shapedexamproj.activity;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;

import com.nd.shapedexamproj.App;
import com.nd.shapedexamproj.R;
import com.nd.shapedexamproj.util.UIHelper;

/**
 * 主界面--课堂
 */
public class HomeActivity extends Activity{
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(initResource());
        initComponent(savedInstanceState);
        initData();
        addListener();
    }
    
	public int initResource() {
		return R.layout.home_activity;
	}

	public void initData() {
		
	}
	
	public void initComponent(Bundle savedInstanceState) {
		String userId = App.getUserId();
		if (!TextUtils.isEmpty(userId)) {
			UIHelper.showMain(this);
		} else {
			// 跳转登录界面
			UIHelper.showLogin(this);
		}
		
		finish();
	}

	public void addListener() {
		
	}
	
}
