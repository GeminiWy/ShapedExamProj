package com.tming.common.view;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;

import com.tming.common.view.support.pulltorefresh.PullToRefreshBase;
import com.tming.common.view.support.pulltorefresh.PullToRefreshBase.AnimationStyle;
import com.tming.common.view.support.pulltorefresh.PullToRefreshBase.Mode;
import com.tming.common.view.support.pulltorefresh.PullToRefreshBase.OnRefreshListener2;
import com.tming.common.view.support.pulltorefresh.PullToRefreshListView;

/**
 * 
 * @ClassName: RefreshableListView
 * @Description: 上拉刷新，下拉加载 ListView
 * @author XueWenJian
 * @date 2014-6-5 下午2:03:42
 * 
 */
public class RefreshableListView extends PullToRefreshListView implements
		OnRefreshListener2<ListView> {

	private static final String TAG = "TMCommonLibrary_RefreshableListView";
	
	private ListView listView;
	private OnRefreshListener onRefreshListener;

	public RefreshableListView(Context context) {
		super(context);
		super.setMode(Mode.BOTH);
		Log.i(TAG, "RefreshableListView(Context context)");
		init();
	}

	public RefreshableListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		super.setMode(Mode.BOTH);
		Log.i(TAG, "RefreshableListView(Context context, AttributeSet attrs)");
		init();
	}

	public RefreshableListView(Context context, Mode mode) {
		super(context, mode);
		Log.i(TAG, "RefreshableListView(Context context, Mode mode)");
		init();
	}

	public RefreshableListView(Context context, Mode mode, AnimationStyle style) {
		super(context, mode, style);
		Log.i(TAG, "RefreshableListView(Context context, Mode mode, AnimationStyle style)");
		init();
	}

	/**
	 * 
	 * @Title: init
	 * @Description: 初始化
	 * @param
	 * @return void 返回类型
	 * @throws
	 */
	private void init() {
		super.setOnRefreshListener(this);
		Log.i(TAG, "getMode:"+super.getMode());
	}

	/**
	 * 
	 * @Title: setFootVisible
	 * @Description: 设置加载更多是否可见
	 * @param @param visibility
	 * @return void 返回类型
	 * @throws
	 */
	public void setFootVisible(boolean visibility) {
		if(!visibility)
		{
			super.setMode(Mode.PULL_FROM_START);
		}
		
	}

	/**
	 * 
	 * @Title: setonRefreshListener
	 * @Description: 设置可下拉和上拉刷新监听
	 * @param @param refreshListener
	 * @return void 返回类型
	 * @throws
	 */
	public void setonRefreshListener(OnRefreshListener refreshListener) {
		onRefreshListener = refreshListener;
	}

	/**
	 * 
	 * @ClassName: OnRefreshListener
	 * @Description: 上拉刷新下拉加载监听接口
	 * @author XueWenJian
	 * @date 2014-6-5 下午2:09:33
	 * 
	 */
	public interface OnRefreshListener {
		public void onRefresh();

		/**
		 * 加载更多
		 */
		public void onLoadMore();
	}

	/**
	 * 
	 * @Title: onRefreshComplete
	 * @Description: @deprecated 完成刷新后的动作(不建议使用) 建议使用onRefreshComplete()
	 * @param @param index
	 * @return void 返回类型
	 * @throws
	 */
	public void onRefreshComplete(int index) {
		onRefreshComplete();
	}

	@Override
	public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {

		if (null != onRefreshListener) {
			onRefreshListener.onRefresh();
		}

	}

	@Override
	public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
		if (null != onRefreshListener) {
			onRefreshListener.onLoadMore();
		}
	}
	
	/**
	 * 
	  * @Title: getListView
	  * @Description: 获得原始ListView
	  * @param @return 
	  * @return ListView    返回类型
	  * @throws
	 */
	public ListView getListView()
	{
		return super.getRefreshableView();
	}

	public void setDividerHeight(int height) {
		super.getRefreshableView().setDividerHeight(height);
	}

	public void addHeaderView(View view, Object data, boolean isSelectable) {
		super.getRefreshableView().addHeaderView(view, data, isSelectable);
	}

	public void addFooterView(View view) {
		super.getRefreshableView().addFooterView(view, null, true);
	}

	public void addFooterView(View view, Object data, boolean isSelectable) {
		super.getRefreshableView().addFooterView(view, data, isSelectable);
	}

	public void smoothScrollToPosition(int position) {
		super.getRefreshableView().smoothScrollToPosition(position);
	}

	public void setOnItemLongClickListener(OnItemLongClickListener listener) {
		super.getRefreshableView().setOnItemLongClickListener(listener);
	}

	public void setOnItemClickListener(OnItemClickListener listener) {
		super.getRefreshableView().setOnItemClickListener(listener);
	}

}