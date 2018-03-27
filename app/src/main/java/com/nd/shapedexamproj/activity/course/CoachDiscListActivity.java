package com.nd.shapedexamproj.activity.course;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.*;
import android.widget.AdapterView.OnItemClickListener;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.nd.shapedexamproj.App;
import com.nd.shapedexamproj.R;
import com.nd.shapedexamproj.activity.BaseActivity;
import com.nd.shapedexamproj.adapter.CoachDiscListAdapter;
import com.nd.shapedexamproj.model.course.CoachTopic;
import com.nd.shapedexamproj.util.Constants;
import com.nd.shapedexamproj.util.JsonParseObject;
import com.nd.shapedexamproj.util.StringUtils;
import com.nd.shapedexamproj.util.UIHelper;
import com.tming.common.net.TmingHttp;
import com.tming.common.util.Log;
import com.tming.common.view.RefreshableListView;
import com.tming.common.view.RefreshableListView.OnRefreshListener;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.*;

/**
 * 
 * @ClassName: CourseCoachActivity
 * @Title:
 * @Description:课堂-辅导讨论区
 * @Author:XueWenJian
 * @Since:2014年5月21日10:45:19
 * @Version:1.0
 */
public class CoachDiscListActivity extends BaseActivity {

	private final static String TAG = "CoachDiscListActivity";
	public final static int LIST_REFRESH = 1, LIST_LOAD_MORE = 2;
	public final static int HEAD_POSITION_SHIFT = 2;//加入HeaderView产生的偏差值

	private Context mContext;
	private LinearLayout mLoadinglayout ;
	private View mListHeadView;
	private RelativeLayout mHeadRL;
	private ImageView mBackIV;
	private TextView mHeadTitleTV;
	private Button mHeadRightBtn;
	private TextView mCourseTitleTV;
	private RefreshableListView mContentLV;

	private String mCourseId;
	private String mCourseName;
	private CoachDiscListAdapter mCoachDiscListAdapter;
	private List<CoachTopic> mCoachDiscTopics;
	private int mPage = 1;
	private int mPageSize = 10;
	private String mCoacherId;   //课程责任老师ID

	@Override
	public int initResource() {
		return R.layout.course_coachdisc_list_activity;
	}

	@Override
	public void initComponent() {
		mContext = this;

		mLoadinglayout = (LinearLayout) findViewById(R.id.loading_layout);
		mHeadRL = (RelativeLayout) findViewById(R.id.coachdisc_head_layout);
		mBackIV = (ImageView) findViewById(R.id.commonheader_left_iv);
		mHeadTitleTV = (TextView) findViewById(R.id.commonheader_title_tv);
		mHeadRightBtn = (Button) findViewById(R.id.commonheader_right_btn);
		mContentLV = (RefreshableListView) findViewById(R.id.coachdisc_content_lv);

		mHeadTitleTV.setText(R.string.coach_title);
		mHeadRightBtn.setVisibility(View.VISIBLE);
		mContentLV.setFootVisible(true);
		
		
		mListHeadView = getLayoutInflater().inflate(R.layout.course_coachdisc_topic_top_item, null);
		mContentLV.addHeaderView(mListHeadView,null, false);
		mCourseTitleTV = (TextView) findViewById(R.id.coachdisc_title_tv);
		mHeadRightBtn.setText(R.string.coach_new_topic);
		
		mHeadRightBtn.setVisibility(View.INVISIBLE);
	}

	@Override
	public void initData() {
		mCourseId = getIntent().getStringExtra("courseId");
		mCourseName = getIntent().getStringExtra("courseName");

		mCourseTitleTV.setText(mCourseName);
		
		mCoachDiscTopics = new ArrayList<CoachTopic>();
		mCoachDiscListAdapter = new CoachDiscListAdapter(mContext,
				mCoachDiscTopics);
		
		mContentLV.setAdapter(mCoachDiscListAdapter);
		/*mContentLV.setRefreshing(true);*/
		getCourseInfo();
		getCoachTopics(LIST_REFRESH);
	}


	private void getCourseInfo() {
		String url = Constants.COURSE_DESC;
		Log.d(TAG, url);
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("courseid", mCourseId);

		TmingHttp.asyncRequest(url, params,
				new TmingHttp.RequestCallback<String>() {

					@Override
					public String onReqestSuccess(String respones)
							throws Exception {

						return respones;
					}

					@Override
					public void success(String respones) {
						JsonParseObject jsonParseObject = JsonParseObject.parseJson(respones);
						if (jsonParseObject.getFlag() == Constants.SUCCESS_MSG) {
							if (!jsonParseObject.getResJs().isNull("info")) {
								try {
									JSONObject jsonInfo = jsonParseObject.getResJs().getJSONObject("info");
									if (!jsonInfo.isNull("coacher")) {
										Iterator<String> iter = jsonInfo.getJSONObject("coacher").keys();
										if (iter.hasNext()) {
											String coacherId = iter.next();
											mCoacherId = coacherId;
										}
									}

								} catch (JSONException e) {
									e.printStackTrace();
								}
							}

						}

						if (!StringUtils.isEmpty(mCoacherId) && App.getUserId().equals(mCoacherId)) {
							mHeadRightBtn.setVisibility(View.VISIBLE);
						} else {
							mHeadRightBtn.setVisibility(View.INVISIBLE);
						}

					}

					@Override
					public void exception(Exception exception) {
						Toast.makeText(mContext,
								getResources().getString(R.string.net_error),
								Toast.LENGTH_SHORT).show();
						exception.printStackTrace();
					}
				});
	}
	
	private void getCoachTopics(final int refreshType) {
		/*mLoadinglayout.setVisibility(View.VISIBLE);*/
		
		String url = Constants.DISCUSSION_GET_TOPICLIST;
		Log.d(TAG, url);
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("courseid", mCourseId);
		params.put("pageNum", mPage);
		params.put("pageSize", mPageSize);

		TmingHttp.asyncRequest(url, params,
				new TmingHttp.RequestCallback<String>() {

					@Override
					public String onReqestSuccess(String respones)
							throws Exception {

						return respones;
					}

					@Override
					public void success(String respones) {
						mContentLV.onRefreshComplete();
						// 数据组装
						Log.i(TAG, respones);
						JsonParseObject jsonParseObject = JsonParseObject.parseJson(respones);
						Log.d(TAG, jsonParseObject.getResJs().toString());
						if (Constants.SUCCESS_MSG == jsonParseObject.getFlag()) {
							try {

								if (LIST_REFRESH == refreshType) {
									mCoachDiscTopics.clear();
									mCoachDiscListAdapter.notifyDataSetChanged();
								}

								if (!jsonParseObject.getResJs().isNull("list")) {
									Gson gson = new GsonBuilder().setDateFormat(
											"yyyy-MM-dd").create();

									List<CoachTopic> result = gson.fromJson(jsonParseObject.getResJs().getJSONArray("list").toString(),
											new TypeToken<ArrayList<CoachTopic>>() {
											}.getType());

									if (result.size() > 0) {
										mCoachDiscTopics.addAll(result);
										//Collections.sort(mCoachDiscTopics);//对列表进行排序
										mPage++;
									}
								}

							} catch (JsonSyntaxException e) {
								e.printStackTrace();

							} catch (JSONException e) {
								e.printStackTrace();
								jsonParseObject.setFlag(0);
							}

						}

						Integer result = jsonParseObject.getFlag();


						// UI表现
						if (Constants.SUCCESS_MSG == result) {
							
							/*mLoadinglayout.setVisibility(View.GONE);*/
							mCoachDiscListAdapter.notifyDataSetChanged();
							Log.d(TAG, "mCoachDiscTopics size:" + mCoachDiscTopics.size());
						} else {
							Log.e(TAG, getResources().getString(R.string.api_error));
							return;
						}

					}

					@Override
					public void exception(Exception exception) {
						mContentLV.onRefreshComplete();
						exception.printStackTrace();
					}

				});
	}

	@Override
	public void addListener() {
		mContentLV.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) 
			{
				int currPosition = position - HEAD_POSITION_SHIFT;
				CoachTopic coachTopic = mCoachDiscTopics.get(currPosition);
				UIHelper.showCoachTopicDetailActivity(mContext, mCourseId, coachTopic.getTopicId());
			}

		});
		
		mContentLV.setonRefreshListener(new OnRefreshListener() {

			@Override
			public void onRefresh() {
				Log.e(TAG, "onRefresh");
				refreshList();

			}
			
			@Override
			public void onLoadMore() {
				Log.e(TAG, "onLoadMore");
				getCoachTopics(LIST_LOAD_MORE);

			}
		});
		
		mBackIV.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				CoachDiscListActivity.this.finish();
				
			}
		});
		mHeadRightBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				UIHelper.showCoachNewTopicActivity(CoachDiscListActivity.this, mCourseId);
			}
		});

	}
	
	private void refreshList()
	{
		mPage = 1;
		mCoachDiscTopics.clear();
		mCoachDiscListAdapter.notifyDataSetChanged();
		getCoachTopics(LIST_REFRESH);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		Log.d(TAG, "onActivityResult; requestCode:"+ requestCode+"; resultCode:"+resultCode);
		
		if(RESULT_OK == resultCode)
		{
			mContentLV.setRefreshing(true);
			/*refreshList();*/
		}
	}

}
