package com.tming.common;

import com.umeng.analytics.MobclickAgent;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;


public class BaseFragmentActivity extends FragmentActivity {
    
    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        AppManager.getAppManager().addActivity(this);
    }
    
    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.push_pic_right_in, R.anim.push_pic_right_out);
        AppManager.getAppManager().removeActivity(this);
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);//友盟统计时长
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);//友盟统计时长
    }
}
