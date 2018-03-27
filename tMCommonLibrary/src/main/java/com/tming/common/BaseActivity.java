package com.tming.common;

import com.umeng.analytics.MobclickAgent;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

/**
 * <p>基类，用于规范代码</p>
 * <p>Created by zll on 2014-3-3</p>
 */
public abstract class BaseActivity extends FragmentActivity {

    /**
     *  界面退出时，进入的Activity的动画资源ID，为0时没有动画
     */
    protected int finishEnterAnim = R.anim.push_pic_right_in;

    /**
     * 界面退出时，退出的Activity的动画资源ID，为0时没有动画
     */
    protected int finishExitAnim = R.anim.push_pic_right_out;

    /**
     * 初始化布局资源文件
     */
    public abstract int initResource();

    /**
     * 初始化组件
     */
    public abstract void initComponent();

    /**
     * 初始化数据,在此请求网络数据
     */
    public abstract void initData();

    /**
     * 添加监听
     */
    public abstract void addListener();

    /**
     * 初始化权限
     */
    public void initAuthoriy() {}

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppManager.getAppManager().addActivity(this);
        setContentView(initResource());
        initComponent();
        addListener();
        initData();
        initAuthoriy();
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
    
    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(finishEnterAnim, finishExitAnim);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        AppManager.getAppManager().removeActivity(this);
    }
}
