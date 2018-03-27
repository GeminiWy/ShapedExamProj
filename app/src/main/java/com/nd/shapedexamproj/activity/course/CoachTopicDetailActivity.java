package com.nd.shapedexamproj.activity.course;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.nd.shapedexamproj.App;
import com.nd.shapedexamproj.R;
import com.nd.shapedexamproj.activity.BaseActivity;
import com.nd.shapedexamproj.adapter.CoachTopicDetailAdapter;
import com.nd.shapedexamproj.model.course.CoachTopicDetail;
import com.nd.shapedexamproj.model.course.CoachTopicReply;
import com.nd.shapedexamproj.util.Constants;
import com.nd.shapedexamproj.util.JsonParseObject;
import com.nd.shapedexamproj.util.UIHelper;
import com.nd.shapedexamproj.view.CircularImage;
import com.nd.shapedexamproj.view.InputBottomBar;
import com.tming.common.cache.image.ImageCacheTool;
import com.tming.common.cache.image.ImageCacheTool.ImageLoadCallBack;
import com.tming.common.net.TmingHttp;
import com.tming.common.util.Log;
import com.tming.common.util.PhoneUtil;
import com.tming.common.view.RefreshableListView;
import com.tming.common.view.RefreshableListView.OnRefreshListener;
import org.json.JSONException;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 
 * @ClassName: CoachTopicDetailActivity
 * @Title:
 * @Description:课堂-辅导讨论区-帖子详情
 * @Author:XueWenJian
 * @Since:2014年5月22日10:16:54
 * @Version:1.0
 */
public class CoachTopicDetailActivity extends BaseActivity {

	private final static String TAG = "CoachTopicDetailActivity";
	public final static int LIST_REFRESH = 1, LIST_LOAD_MORE = 2;
	public static final  int HEAD_POSITION_SHIFT = 2;//加入HeaderView产生的偏差值
	
	private Context mContext;
	
	private View mListHeadView, mLoadinglayout;
	private TextView mTopicTitleTV;
	private TextView mTopicTimeTV;
	private TextView mTopicReplyNumTV;
	private CircularImage mTopicAuthorAvatarIV;
	private TextView mTopicAuthorNameTV;
	private TextView mTopicContentTV;
	private TextView mTopicAuthorType;
	private RelativeLayout mHeadRL;
	private ImageView mBackIV;
	private TextView mHeadTitleTV;
	private Button mHeadRightBtn;
	private TextView mHeadMoreBtn;
	private TextView mCourseTitleTV;
	private RefreshableListView mContentLV;
	//TODO 测试手机上出现崩溃BUG，屏蔽PopUpView的实例化
/*	private CoachTopicPopUpView mTopicPopUpView;
	private CoachTopicReplyPopUpView mReplyPopUpView;*/
	
	private String mCourseId;
	private String mTopicId;
	
	private CoachTopicDetailAdapter mCoachTopicDetailAdapter;
	private CoachTopicDetail mCoachTopicDetail;
	private List<CoachTopicReply> mCoachTopicDetails;
	private int mPage = 1;
	private int mPageSize = 10;
	
	@Override
	public int initResource() {
		return R.layout.course_coachdisc_topicdetail_activity;
	}

	@Override
	public void initComponent() {
		mContext = this;

		mLoadinglayout = (LinearLayout) findViewById(R.id.loading_layout);
		mHeadRL = (RelativeLayout) findViewById(R.id.coach_topicdetail_head_layout);
		mBackIV = (ImageView) findViewById(R.id.commonheader_left_iv);
		mHeadTitleTV = (TextView) findViewById(R.id.commonheader_title_tv);
		mHeadRightBtn = (Button) findViewById(R.id.commonheader_right_btn);
		mHeadMoreBtn = (TextView) findViewById(R.id.commonheader_more_tv);

		mContentLV = (RefreshableListView) findViewById(R.id.coach_topicdetail_content_lv);

		mHeadTitleTV.setText(R.string.coach_topicdetail_title);
		mHeadRightBtn.setVisibility(View.VISIBLE);
		mContentLV.setFootVisible(true);
		
		mListHeadView = getLayoutInflater().inflate(R.layout.course_coachdisc_topicdetail_top_item, null);
		mListHeadView.setVisibility(View.INVISIBLE);
		
		mTopicTitleTV = (TextView) mListHeadView.findViewById(R.id.coachdisc_topic_title_tv);
		mTopicTimeTV = (TextView) mListHeadView.findViewById(R.id.coachdisc_topic_time_tv);
		mTopicReplyNumTV = (TextView) mListHeadView.findViewById(R.id.coachdisc_topic_replynum_tv);
		mTopicAuthorAvatarIV = (CircularImage) mListHeadView.findViewById(R.id.coach_topicdetail_userimage_iv);
		mTopicAuthorNameTV = (TextView) mListHeadView.findViewById(R.id.coach_topicdetail_username_tv);
		mTopicContentTV = (TextView) mListHeadView.findViewById(R.id.coach_topicdetail_content_tv);
		mTopicAuthorType = (TextView) mListHeadView.findViewById(R.id.coach_topicdetail_type_tv);
		
		mContentLV.addHeaderView(mListHeadView,null, false);
		mCourseTitleTV = (TextView) findViewById(R.id.coachdisc_title_tv);
		mHeadRightBtn.setText(R.string.coach_reply_topic);
		
		/*mReplyPopUpView = new CoachTopicReplyPopUpView(CoachTopicDetailActivity.this);
		mTopicPopUpView = new CoachTopicPopUpView(CoachTopicDetailActivity.this);*/
	}

	@Override
	public void initData() {
		mCourseId = getIntent().getStringExtra("courseId");
		mTopicId = getIntent().getStringExtra("topicId");
		
		mCoachTopicDetail = new CoachTopicDetail();
		mCoachTopicDetails = new ArrayList<CoachTopicReply>();
		mCoachTopicDetailAdapter = new CoachTopicDetailAdapter(mContext, mCoachTopicDetail, mCoachTopicDetails);
		mContentLV.setAdapter(mCoachTopicDetailAdapter);
		refreshList();
	}


	public void initTopicAuthoriy() {
		//判断身份
		if(mCoachTopicDetail.getAuthorId().equals(App.getUserId()))
		{
			//发帖人
			mHeadMoreBtn.setVisibility(View.VISIBLE);

		}
		else
		{
			//隐藏 置顶等功能
			mHeadMoreBtn.setVisibility(View.GONE);
		}

		super.initAuthoriy();
	}

	private void getCoachTopicDetails(final int refreshType) {
		if (PhoneUtil.checkNetworkEnable(mContext) == PhoneUtil.NETSTATE_DISABLE) {
			Toast.makeText(mContext, getResources().getString(R.string.please_open_network), Toast.LENGTH_SHORT).show();
			return ;
		}
		mLoadinglayout.setVisibility(View.VISIBLE);
		
		String url = Constants.DISCUSSION_GET_TOPICDETAIL;
		Log.d(TAG, url);
		Log.d(TAG, "topic_id:"+mTopicId+";pageNum"+mPage+"pageSize:"+mPageSize);
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("topic_id", mTopicId);
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
									mCoachTopicDetails.clear();
									mCoachTopicDetailAdapter.notifyDataSetChanged();
								}

								if (!jsonParseObject.getResJs().isNull("list")) {
									Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd").create();
									mCoachTopicDetail = gson.fromJson(jsonParseObject.getResJs().getJSONObject("list").getJSONObject("topic_info").toString(),
											CoachTopicDetail.class);
									List<CoachTopicReply> result = gson.fromJson(jsonParseObject.getResJs().getJSONObject("list").getJSONArray("reply_list").toString(),
											new TypeToken<ArrayList<CoachTopicReply>>() {
											}.getType());

									if (result.size() > 0) {
										mCoachTopicDetails.addAll(result);
										mPage++;
									}
								}

							} catch (JsonSyntaxException e) {
								e.printStackTrace();
								jsonParseObject.setFlag(0);
							} catch (JSONException e) {
								e.printStackTrace();
								jsonParseObject.setFlag(0);
							}

						}

						int result = jsonParseObject.getFlag();

						// UI表现
						if (Constants.SUCCESS_MSG == result) {

							refreshHeadView();
							mLoadinglayout.setVisibility(View.GONE);
							mCoachTopicDetailAdapter.setmCoachTopicDetail(mCoachTopicDetail);
							mCoachTopicDetailAdapter.notifyDataSetChanged();
							//TODO 关闭修置顶/取消置顶，修改主帖，删除主帖功能，等待接口
							//initTopicAuthoriy();
							Log.d(TAG, "mCoachDiscTopics size:" + mCoachTopicDetails.size());
						} else {
							Toast.makeText(mContext,
									R.string.api_error, Toast.LENGTH_SHORT)
									.show();
							return;
						}

						mListHeadView.setVisibility(View.VISIBLE);
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
	
	@Override
	public void addListener() {
		
		mBackIV.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				CoachTopicDetailActivity.this.finish();
				
			}
		});
		mHeadRightBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				
				UIHelper.showCoachReplyTopicActivty(CoachTopicDetailActivity.this, mCourseId, mTopicId);
			}
		});
		mHeadMoreBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				//mTopicPopUpView.showPopupWindow(mHeadMoreBtn);
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
				
				nextPage();
			}
		});
		
		/*mTopicPopUpView.setTopicTopListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Toast.makeText(mContext, R.string.coach_topic_top, Toast.LENGTH_SHORT).show();
				
			}
		});
		
		mTopicPopUpView.setTopicUpdateListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Toast.makeText(mContext, R.string.coach_topic_update, Toast.LENGTH_SHORT).show();
				
			}
		});
		mTopicPopUpView.setTopicDelListener(new OnClickListener() {
	
		@Override
		public void onClick(View v) {
			Toast.makeText(mContext, R.string.coach_topic_delete, Toast.LENGTH_SHORT).show();
			
		}
});*/
	}
	
	private void refreshList()
	{
		mPage = 1;
		mCoachTopicDetails.clear();
		mListHeadView.setVisibility(View.INVISIBLE);
		mCoachTopicDetailAdapter.notifyDataSetChanged();
		getCoachTopicDetails(LIST_REFRESH);
	}
	
	private void nextPage()
	{
		getCoachTopicDetails(LIST_LOAD_MORE);
	}

	private void refreshHeadView()
	{
		mTopicTitleTV.setText(mCoachTopicDetail.getTitle());
    	mTopicReplyNumTV.setText(mCoachTopicDetail.getRelyNum()+"");
		mTopicAuthorNameTV.setText(mCoachTopicDetail.getAuthorName());
		mTopicContentTV.setText(InputBottomBar.parseFaceByText(mContext, mCoachTopicDetail.getContent(),(int)mTopicContentTV.getTextSize()));
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    	String date = sdf.format(new Date(mCoachTopicDetail.getAddTimeStamp()*1000));
    	mTopicTimeTV.setText(date);
    	mTopicAuthorType.setText(mCoachTopicDetail.getAuthorTitle());
		
		try {
			ImageCacheTool.getInstance(mContext).asyncLoadImage(new URL(mCoachTopicDetail.getAuthorAvatar()), new ImageLoadCallBack() {
				
				@Override
				public void progress(int progress) {
					
				}
				
				@Override
				public void loadResult(Bitmap bitmap) {
					if(null == bitmap)
					{
						mTopicAuthorAvatarIV.setBackgroundResource(R.drawable.all_use_icon_bg);
					}
					else
					{
						mTopicAuthorAvatarIV.setImageBitmap(bitmap);
					}
				}
			});
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		
		mListHeadView.setVisibility(View.VISIBLE);
	}
	
	public void onReplyMoreClick(View view)
	{
		final Integer position = (Integer)view.getTag();
		CoachTopicReply coachTopicReply = null;
	    if (position < mCoachTopicDetails.size()) {
	        coachTopicReply = mCoachTopicDetails.get(position);
	        //判断身份 
	    	if(null != mCoachTopicDetail && mCoachTopicDetail.getAuthorId().equals(App.getUserId()))
			{
	    		
	    	}
	    	else
	    	{
	    		//是否是回复人
	            if(coachTopicReply.getAuthorId().equals(App.getUserId()))
	            {
	            	//隐藏删除回复
	            	//mReplyPopUpView.setReplyDelVisiable(View.GONE);
	            }
	            else
	            {
	            	
	            }
	    	}
	    	
	    	/*mReplyPopUpView.setReplyDelListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					Toast.makeText(mContext, "delete:"+ position, Toast.LENGTH_SHORT).show();
					
				}
			});
	    	
	    	mReplyPopUpView.setReplyUpdateListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					Toast.makeText(mContext, "update:"+ position, Toast.LENGTH_SHORT).show();
					
				}
			});
	    	
	    	mReplyPopUpView.showPopupWindow(view);*/
	    }
		
		
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
