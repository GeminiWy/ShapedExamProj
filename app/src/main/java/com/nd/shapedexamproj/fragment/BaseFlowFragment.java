package com.nd.shapedexamproj.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
/**
 * 
 * 基本功能 定义 ，定义流程模板。
 * @version 1.0.0 
 * @author Abay Zhuang <br/>
 *		   Create at 2014-5-22
 */
public abstract class BaseFlowFragment extends BaseFragment{
	
	/**
	 * 初始化布局资源文件
	 */
	public abstract int initResource();
	/**
	 * 初始化组件
	 */
	public abstract void initComponent(View view);
	/**
	 * 初始化数据,在此请求网络数据
	 */
	public abstract void initData();
	/**
	 * 添加监听
	 */
	public abstract void initListener();
	
	/**
	 *权限设置
	 */
	public void initAuthority(){
		
	}
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		initData();
		initListener();
		initAuthority();
	}
	
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = LayoutInflater.from(getBaseActivity()).inflate(initResource(), container, false);
		initComponent(v);
		return v;
	}
}
