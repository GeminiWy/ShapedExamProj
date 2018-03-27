package com.nd.shapedexamproj.view.course;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.nd.shapedexamproj.R;
import com.nd.shapedexamproj.adapter.CoachDiscListAdapter;
import com.nd.shapedexamproj.model.course.CoachTopic;
import com.nd.shapedexamproj.util.Constants;
import com.nd.shapedexamproj.util.JsonParseObject;
import com.nd.shapedexamproj.util.UIHelper;
import com.tming.common.net.TmingHttp;
import com.tming.common.util.Log;
import com.tming.common.view.support.pulltorefresh.PullToRefreshBase;
import com.tming.common.view.support.pulltorefresh.PullToRefreshBase.OnRefreshListener2;
import com.tming.common.view.support.pulltorefresh.PullToRefreshListView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
/**
 * 课程详情页--辅导讨论区
 * @author xuewenjian
 *
 */
public class CourseCoach extends RelativeLayout{

	private final static String TAG = "CourseCoach";
	private static final int HEAD_SIZE = 1;
	
	private Context mContext;
	
	private View mCourseCoach;
	private PullToRefreshListView mContentPLV;
	private ListView mContentLV;
//	private LinearLayout mLoadinglayout;
	private View mListFootView;
	
	private String mCourseId;
	private String mCourseName;
	private CoachDiscListAdapter mCoachDiscListAdapter;
	private List<CoachTopic> mCoachDiscTopics;
	private int mPage = 1;
	private int mPageSize = 10;//只请求三条数据
	
	public CourseCoach(Context context, String courseId, String courseName) {
		super(context);
		this.mCourseId = courseId;
		this.mCourseName = courseName;
		this.mContext = context;
		
		initView();
		requestData();
		addListener();
	}

	private void initView()
	{
		mCourseCoach = LayoutInflater.from(mContext)
				.inflate(R.layout.course_coach_view, this);
		/*mLoadinglayout = (LinearLayout) mCourseCoach.findViewById(R.id.loading_layout);*/
		mContentPLV = (PullToRefreshListView)mCourseCoach.findViewById(R.id.coachdisc_content_lv);
		mContentLV = mContentPLV.getRefreshableView();
		mCoachDiscTopics = new ArrayList<CoachTopic>();
		mCoachDiscListAdapter = new CoachDiscListAdapter(mContext, mCoachDiscTopics);
		mListFootView = LayoutInflater.from(mContext).inflate(R.layout.course_coach_foot_item, null);
		mContentLV.addFooterView(mListFootView);
		mContentLV.setAdapter(mCoachDiscListAdapter);
		
		IntentFilter filter = new IntentFilter();
		filter.addAction("kd.coursecoach.refresh");
		filter.addAction("kd.coursecoach.unregister");
		mContext.registerReceiver(receiver, filter);
	}
	
	public void requestData()
	{
		getCoachTopics();
	}
	
	private BroadcastReceiver receiver = new BroadcastReceiver() {
        
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("kd.coursecoach.refresh")) {
                Log.e(TAG, "==刷新列表==");
                mContentPLV.setRefreshing(false);
            } else if (intent.getAction().equals("kd.coursedetail.unregister")) {
                mContext.unregisterReceiver(receiver);
            }
        }
    };
	
	
	public void addListener() {
		
		mContentPLV.setOnRefreshListener(new OnRefreshListener2<ListView>() {

			@Override
			public void onPullDownToRefresh(
					PullToRefreshBase<ListView> refreshView) {
				requestData();
			}

			@Override
			public void onPullUpToRefresh(
					PullToRefreshBase<ListView> refreshView) {
			}
		});
		
		mContentLV.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) 
			{
				int currPosition = position - HEAD_SIZE;
				if(mCoachDiscTopics.size() > currPosition)
				{
					CoachTopic coachTopic = mCoachDiscTopics.get(currPosition);
					UIHelper.showCoachTopicDetailActivity(mContext, mCourseId, coachTopic.getTopicId());
				}
				else
				{
					UIHelper.showCoachDiscListActivity(mContext, mCourseId, mCourseName);
				}
				
			}

		});
	}
	
	private void getCoachTopics() {
		/*mLoadinglayout.setVisibility(View.VISIBLE);*/
		
		String url = Constants.DISCUSSION_GET_TOPICLIST;
		Log.d(TAG, url);
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("courseid", mCourseId);
		params.put("pageNum", mPage);
		params.put("pageSize", mPageSize);

		TmingHttp.asyncRequest(url, params,
				new TmingHttp.RequestCallback<Integer>() {

					@Override
					public Integer onReqestSuccess(String respones)
							throws Exception {
						// 数据组装
						Log.i(TAG, respones);
						JsonParseObject jsonParseObject = JsonParseObject.parseJson(respones);
						Log.d(TAG, jsonParseObject.getResJs().toString());
						if (Constants.SUCCESS_MSG == jsonParseObject.getFlag()) {
							try {
								mCoachDiscTopics.clear();
								if (!jsonParseObject.getResJs().isNull("list")) {
									Gson gson = new GsonBuilder().setDateFormat(
											"yyyy-MM-dd").create();

									List<CoachTopic> result = gson.fromJson(jsonParseObject.getResJs().getJSONArray("list").toString(),
											new TypeToken<ArrayList<CoachTopic>>() {
											}.getType());

									if (result.size() > 0) {
										mCoachDiscTopics.addAll(result);
									}
								}

							} catch (JsonSyntaxException e) {
								e.printStackTrace();
								jsonParseObject.setFlag(0);
							}

						}

						return jsonParseObject.getFlag();

					}

					@Override
					public void success(Integer result) {
						/*mLoadinglayout.setVisibility(View.GONE);*/
						// UI表现
						if (Constants.SUCCESS_MSG == result) {

							mCoachDiscListAdapter.notifyDataSetChanged();
							Log.d(TAG, "mCoachDiscTopics size:" + mCoachDiscTopics.size());
						} else {
							Toast.makeText(mContext,
									R.string.api_error, Toast.LENGTH_SHORT)
									.show();
							return;
						}
						mContentPLV.onRefreshComplete();
					}

					@Override
					public void exception(Exception exception) {
						/*mLoadinglayout.setVisibility(View.GONE);*/
						exception.printStackTrace();
						mContentPLV.onRefreshComplete();
					}

				});
	}

}
