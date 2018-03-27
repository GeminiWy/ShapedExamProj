package com.nd.shapedexamproj.activity.my;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.nd.shapedexamproj.R;
import com.nd.shapedexamproj.activity.BaseActivity;
import com.nd.shapedexamproj.adapter.TimelineAdapter;
import com.nd.shapedexamproj.model.my.PersonalInfo;
import com.nd.shapedexamproj.model.playground.Timeline;
import com.nd.shapedexamproj.net.ServerApi;
import com.nd.shapedexamproj.util.Constants;
import com.nd.shapedexamproj.util.JsonUtil;
import com.tming.common.cache.image.ImageCacheTool;
import com.tming.common.cache.net.TmingCacheHttp;
import com.tming.common.cache.net.TmingCacheHttp.RequestWithCacheCallBack;
import com.tming.common.net.TmingHttp.RequestCallback;
import com.tming.common.util.Log;
import com.tming.common.view.RefreshableListView;
import com.tming.common.view.RefreshableListView.OnRefreshListener;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PersonalDynamicsActivity extends BaseActivity implements OnClickListener {
	
	private final static String TAG = "PersonalDynamicsActivity";
	private final static int LOAD_TYPE_INIT = 2;
	private final static int LOAD_TYPE_REFRESH = 1;
	private final static int LOAD_TYPE_MORE = 2;
	private int mLoadType = LOAD_TYPE_INIT;

	private boolean isRequest = false;;

	private TmingCacheHttp cacheHttp;
	private ImageCacheTool imageCacheTool;

	private ImageView goBack;
	private Button rightBtn;
	private RefreshableListView personalDynamicsListView;
	private View loadingView;
	private TextView noDataTipTv;
	private Bitmap userImageBitmap = null;
	private TimelineAdapter timelineAdapter;

	/*private List<Timeline> personalDynamicsList = new ArrayList<Timeline>();*/
	private int pageno = 1, pageSize = 20, age, gender = -1, industryId = -1;
	private String userId;
	/**
	 * 他人的用户名
	 * */
	private String userName = "";
	
	private BroadcastReceiver receiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Constants.REFRESH_LIST) || 
                    intent.getAction().equals(Constants.SEND_COMMENT_SUCCESS_REFRESH_LIST_ACTION)) {
                refresh();
            }
        }
	    
	};
	
	protected void onDestroy() {
	    if (receiver != null) {
	        unregisterReceiver(receiver);
	    }
	    super.onDestroy();
	};
	
	@Override
	public int initResource() {
		return R.layout.personal_dynamics;
	}

	@Override
	public void initComponent() {
		userId = getIntent().getStringExtra("userid");
		userName = getIntent().getStringExtra("otherUserName");
		Log.d(TAG, "userid : " + userId);
		goBack = (ImageView) findViewById(R.id.commonheader_left_iv);
		rightBtn = (Button) findViewById(R.id.commonheader_right_btn);
		rightBtn.setVisibility(View.GONE);
		if(TextUtils.isEmpty(userName)){
			((TextView) findViewById(R.id.commonheader_title_tv)).setText(getResources().getString(R.string.personal_dynamics));
		}else{ 
			((TextView) findViewById(R.id.commonheader_title_tv)).setText(String.format(getString(R.string.other_personal_dynamics), userName));
		}

		loadingView = findViewById(R.id.loading_layout);

		//pullToRefreshView = (PullToRefreshView) findViewById(R.id.pullToRefreshView);

		personalDynamicsListView = (RefreshableListView) findViewById(R.id.refreshable_listview);
		personalDynamicsListView.setonRefreshListener(new OnRefreshListener() {
			
			@Override
			public void onRefresh() {
			    refresh();
			}
			
			@Override
			public void onLoadMore() {
				Log.e(TAG,"-----------onLoadMore------------");
				mLoadType = LOAD_TYPE_MORE;
				requestMyDynamicsList(userId, ++pageno, pageSize);
			}
		});
		
		noDataTipTv = (TextView) findViewById(R.id.no_data_tv);
		/*
		 * //lv.setDividerHeight(Utils.dip2px(this, 8));
		 * //重设RefreshableListView的左右边距，这里设为8dp，原xml设置10dp,感觉看起来不大协调
		 * RelativeLayout.LayoutParams lp = new
		 * RelativeLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
		 * LinearLayout.LayoutParams.MATCH_PARENT); // , 1是可选写的
		 * lp.setMargins(Utils.dip2px(this, 8), 0, Utils.dip2px(this, 8), 0);
		 * lv.setLayoutParams(lp);
		 */

		imageCacheTool = ImageCacheTool.getInstance();
		
		IntentFilter filter = new IntentFilter ();
		filter.addAction(Constants.REFRESH_LIST);
		filter.addAction(Constants.SEND_COMMENT_SUCCESS_REFRESH_LIST_ACTION);
		registerReceiver(receiver, filter);
		
		timelineAdapter = new TimelineAdapter(this,mDeleteRequestCallback,Timeline.LAYOUT_TYPE_SECOND);
		personalDynamicsListView.setAdapter(timelineAdapter);
	}
	
	/**
     * 异步删除操作回调
     */
    protected RequestCallback<String> mDeleteRequestCallback = new RequestCallback<String>() {

        @Override
        public String onReqestSuccess(String respones) throws Exception {
            return parseDelResult(respones);
        }

        @Override
        public void success(String respones) {
            if (respones.equals("1")) {
                // 刷新列表
                refresh();
            } else {
                Toast.makeText(PersonalDynamicsActivity.this, getResources().getString(R.string.del_failed), Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public void exception(Exception exception) {}
    };
    
    private String parseDelResult(String respones) {
        String flag = "";
        try {
            JSONObject jobj = new JSONObject(respones);
            flag = jobj.getString("flag");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return flag;
    }
	
	/**
	 * 
	 * <p>下拉刷新</P>
	 *
	 */
	private void refresh () {
	    pageno = 1;
        mLoadType = LOAD_TYPE_REFRESH;
		timelineAdapter.clear();
        Log.e(TAG, "-----------onRefresh------------");
        requestMyDynamicsList(userId, pageno, pageSize);
	}
	
	@Override
	public void initData() {
		cacheHttp = TmingCacheHttp.getInstance();
		mLoadType = LOAD_TYPE_REFRESH;
		requestPersonal();
	}

	@Override
	public void addListener() {
		goBack.setOnClickListener(this);
	}
	
	/**
	 * 设置个人动态没有数据的提示。
	 * */
	private void setNotDynamicsListDataTip(){
		if(timelineAdapter.getCount() == 0){
			if(TextUtils.isEmpty(userName)){
				noDataTipTv.setText(getResources().getString(R.string.personal_dynamics_no_date_tip));
			}else{ 
				noDataTipTv.setText(getResources().getString(R.string.personal_dynamics_other_no_date_tip));
			}
		} else {
		    noDataTipTv.setText("");
		}
	}

	/**
	 * 获取个人动态列表
	 * 
	 * @param userId
	 * @param pageno
	 * @param pageSize
	 */
	private void requestMyDynamicsList(String userId, int pageno, int pageSize) {
		cacheHttp.asyncRequestWithCache(ServerApi.Timeline.getMyTimelineListUrl(userId, pageno, pageSize), null, new TmingCacheHttp.RequestWithCacheCallBackV2Adapter<List<Timeline>>() {

			@Override
			public List<Timeline> parseData(String data) throws Exception {
				JSONObject jsonObject = new JSONObject(data);
				if (JsonUtil.checkResultIsOK(jsonObject)) {
					return JsonUtil.paraseJsonArray(jsonObject.getJSONArray("data"), Timeline.class);
				}
				throw new Exception("Api 发生错误!");
			}

			@Override
			public void cacheDataRespone(List<Timeline> data) {
				if (mLoadType == LOAD_TYPE_REFRESH) {
					timelineAdapter.clear();
				}
				loadingView.setVisibility(View.GONE);
				timelineAdapter.addItemCollection(data);

				personalDynamicsListView.onRefreshComplete();
				Log.e(TAG,"personalDynamicsAdapter.notifyDataSetChanged()");
				timelineAdapter.notifyDataSetChanged();
				setNotDynamicsListDataTip();
			}

			@Override
			public void requestNewDataRespone(List<Timeline> cacheRespones, List<Timeline> newRespones) {
				cacheDataRespone(newRespones);
			}
		});
	}



	/**
	 * 个人信息网络请求
	 */
	@SuppressWarnings("deprecation")
    private void requestPersonal() {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("userid", userId);

		cacheHttp.asyncRequestWithCache(Constants.HOST + "user/getUser.html",
				map, new RequestWithCacheCallBack<PersonalInfo>() {

					@Override
					public PersonalInfo onPreRequestCache(String cache)
							throws Exception {
						PersonalInfo data = personalInfoJSONPasing(cache);
						age = data.age;
						gender = data.sex;
						if (!isRequest) {
							requestMyDynamicsList(userId, pageno, pageSize);
							isRequest = true;
						}

						return data;
					}

					@Override
					public void onPreRequestSuccess(PersonalInfo data) {

					}

					@Override
					public PersonalInfo onReqestSuccess(String respones)
							throws Exception {
						PersonalInfo data = personalInfoJSONPasing(respones);
						age = data.age;
						gender = data.sex;
						if (!isRequest) {
							requestMyDynamicsList(userId, pageno, pageSize);
							isRequest = true;
						}

						return data;
					}

					@Override
					public void success(PersonalInfo cacheRespones,
							PersonalInfo newRespones) {

					}

					@Override
					public void exception(Exception exception) {
						exception.printStackTrace();
					}
				});
	}

	/**
	 * 个人信息Json解析
	 */
	private PersonalInfo personalInfoJSONPasing(String result) {
		PersonalInfo personalInfo = new PersonalInfo();
		try {
			// System.out.println("个人信息："+result);
			JSONObject personal = new JSONObject(result);
			JSONObject user = personal.getJSONObject("user");
			personalInfo.sex = user.getInt("sex");
			personalInfo.age = user.getInt("age");
			personalInfo.setUserName(user.getString("username"));
			// 注释掉的这些字段，本模块用不到
			// personalInfo.phone = user.getString("phone");
			// personalInfo.city = user.getString("city");
			// personalInfo.hobby = Base64.decode(user.getString("hobby"));
			// personalInfo.juniormiddleSchool =
			// Base64.decode(user.getString("juniormiddleschool"));
			// personalInfo.seniormiddleSchool =
			// Base64.decode(user.getString("seniormiddleschool"));
			// personalInfo.primarySchool =
			// Base64.decode(user.getString("primaryschool"));
			// personalInfo.university =
			// Base64.decode(user.getString("university"));
			// personalInfo.star = user.getString("constellation");
			// personalInfo.profession =
			// Base64.decode(user.getString("profession"));
			// personalInfo.explanation =
			// Base64.decode(user.getString("explanation"));
			personalInfo.photoUrl = user.getString("photo");
			// personalInfo.industry =
			// IndustryUtil.getIndustry(user.getInt("industryid")).split("-")[1];
			industryId = user.getInt("industryid");
			// personalInfo.company = Base64.decode(user.getString("company"));
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return personalInfo;
	}
	

	@Override
	public void onClick(View v) { 
		if (v == goBack) {
			finish();
		}
	}

}
