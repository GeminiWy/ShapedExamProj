package com.nd.shapedexamproj.activity.course;


import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.nd.shapedexamproj.R;
import com.nd.shapedexamproj.activity.BaseActivity;
import com.nd.shapedexamproj.model.User;
import com.nd.shapedexamproj.util.Constants;
import com.nd.shapedexamproj.util.UIHelper;
import com.tming.common.net.TmingHttp;
import com.tming.common.util.Log;
import com.tming.common.view.RefreshableListView;
import com.tming.common.view.RefreshableListView.OnRefreshListener;

import java.util.HashMap;

/**
 * 班级联系人
 * 
 * @version 1.0.0
 * @author Abay Zhuang <br/>
 *         Create at 2014-6-16 修改者，修改日期，修改内容。
 */
public class ClassContactActivity extends BaseActivity {

	private static final String TAG = ClassContactActivity.class.getSimpleName();
	public static final String ARG_CLASS_ID = TAG + ".classid";
	private String mClassId = "";
	private View mLoadingView;   // 网络指示标
	private TextView mHeaderTv, noDataTv;
	private ImageView mCommonHeaderLeftBtn;
	private ImageView mCommonHeaderRightBtn;
	private int pageNum;
	private RefreshableListView mListView;
	private ContactAdapter mContactAdapter;

	@Override
	public int initResource() {
		return R.layout.class_contact;
	}

	@Override
	public void initComponent() {
		// 头部按钮
		mCommonHeaderLeftBtn = (ImageView) findViewById(R.id.list_head_left);
		mCommonHeaderRightBtn = (ImageView) findViewById(R.id.list_head_right);
		mCommonHeaderRightBtn.setVisibility(View.INVISIBLE);

		mHeaderTv = (TextView) findViewById(R.id.list_head_tv);
		mHeaderTv.setText("班级通讯录");
		noDataTv = (TextView) findViewById(R.id.no_data_tv);
		// 设置列表
		mListView = (RefreshableListView) findViewById(R.id.contact_lstv);
		// 课程列表下拉刷新 added by WuYuLong
		mListView.setonRefreshListener(new OnRefreshListener() {			
			@Override
			public void onRefresh() {
				userReflash();
			}
			
			@Override
			public void onLoadMore() {
				++pageNum;
				requestUserData();
			}
		});
		// 网络指示标
		mLoadingView = findViewById(R.id.loading_layout);

	}

	@Override
	public void initData() {
		if (getIntent() != null) {
			mClassId = getIntent().getStringExtra(ARG_CLASS_ID);
		} else {
			mClassId = "1";
		}
		// RosterAsyncTask asyncTask = new RosterAsyncTask();
		mContactAdapter = new ContactAdapter(this, null);
		// asyncTask.execute();
		mListView.setAdapter(mContactAdapter);
		mLoadingView.setVisibility(View.VISIBLE);
		userReflash();

	}
	
	/**
	 * 首次加载网络数据 或 刷新
	 */
	private void userReflash(){
		pageNum = 1;
		mContactAdapter.clear();
		requestUserData();
	}

	/**
	 * 获取用户列表数据
	 */
	private void requestUserData() {
		String url = Constants.GET_USER_LIST_URL;
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("classid", mClassId);
		map.put("pageNum", pageNum);
		map.put("pageSize", Constants.PAGESIZE);
		TmingHttp.asyncRequest(url, map, mRequestCallback);
	}

	/**
	 * 回调内部类
	 */
	private TmingHttp.RequestCallback<User.ResultList> mRequestCallback = new TmingHttp.RequestCallback<User.ResultList>() {

		@Override
		public User.ResultList onReqestSuccess(String respones) throws Exception {
			return User.getUserList(respones);
		}

		@Override
		public void success(User.ResultList respones) {
			if (respones.flag == 1) {
			    if (respones.list.size() == 0 && mContactAdapter.getCount() == 0) {
			        noDataTv.setVisibility(View.VISIBLE);
			    } else {
			        noDataTv.setVisibility(View.GONE);
			        mContactAdapter.addUsers(respones.list);
			    }
			} else {
			    noDataTv.setVisibility(View.VISIBLE);
				UIHelper.ToastMessage(ClassContactActivity.this, "同班级错误");
			}
			updateView();
		}

		@Override
		public void exception(Exception exception) {
			Log.e(TAG, "exception : " + exception);
			updateView();
		}

	};

	private void updateView() {
		mLoadingView.setVisibility(View.GONE);
		mContactAdapter.notifyDataSetChanged();
		mListView.onRefreshComplete();
	}

	@Override
	public void addListener() {
		mCommonHeaderLeftBtn.setOnClickListener(UIHelper.finish(this));
		mListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				User user = (User) mContactAdapter.getItem(position - 1);
				UIHelper.showFriendInfoActivity(ClassContactActivity.this,
						user.getUserid());

			}
		});
	}

}
