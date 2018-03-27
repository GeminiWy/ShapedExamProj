package com.nd.shapedexamproj.activity.course;

import android.content.Intent;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.*;
import android.widget.AdapterView.OnItemClickListener;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.nd.shapedexamproj.App;
import com.nd.shapedexamproj.AuthorityManager;
import com.nd.shapedexamproj.R;
import com.nd.shapedexamproj.activity.BaseActivity;
import com.nd.shapedexamproj.adapter.CourseQuestionAdapter;
import com.nd.shapedexamproj.model.course.CourseQuestion;
import com.nd.shapedexamproj.model.my.MyQuestion;
import com.nd.shapedexamproj.net.ServerApi;
import com.nd.shapedexamproj.util.Constants;
import com.nd.shapedexamproj.util.JsonParseObject;
import com.nd.shapedexamproj.util.UIHelper;
import com.tming.common.net.TmingHttp;
import com.tming.common.util.Log;
import com.tming.common.util.PhoneUtil;
import com.tming.common.view.RefreshableListView;
import com.tming.common.view.RefreshableListView.OnRefreshListener;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * 课程答疑列表
 * @author Xiezz
 * 
 * */
public class CourseQuestionListActivity extends BaseActivity {
	private static final String TAG = "CourseQuestionListActivity"; 
	private final static int LOAD_TYPE_REFRESH = 1;
	private final static int LOAD_TYPE_MORE = 2; 
	
	private int mLoadType = -1;
	  
	private int pageno = 1;// 第几页，不传默认第一页 
	private String courseId;
	private String courseName;
	
	private RelativeLayout mHeadRL;
	private ImageView mBackIV;
	private TextView mHeadTitleTV;
	private Button mHeadRightBtn; 
	
	private TextView mCourseTitleTV; 
	
	private RefreshableListView mCourseQuestionListLv;
	private View loadingView;
	 
	
	private CourseQuestionAdapter mCourseQuestionAdapter;
	 
	private List<CourseQuestion> questions = new ArrayList<CourseQuestion>();
	

	@Override
	public int initResource() { 
		return R.layout.course_question_list_activity;
	}

	@Override
	public void initComponent() { 
		// 设置头部透明色 
		
		mHeadRL = (RelativeLayout) findViewById(R.id.course_question_head_layout);
		mBackIV = (ImageView) findViewById(R.id.commonheader_left_iv);
		mHeadTitleTV = (TextView) findViewById(R.id.commonheader_title_tv);
		mHeadRightBtn = (Button) findViewById(R.id.commonheader_right_btn); 
		mCourseTitleTV = (TextView) findViewById(R.id.course_question_introduce_tv);

		mCourseQuestionListLv = (RefreshableListView) findViewById(R.id.course_question_list_lv);
		 
		mCourseQuestionAdapter = new CourseQuestionAdapter(CourseQuestionListActivity.this, questions);
		mCourseQuestionListLv.setAdapter(mCourseQuestionAdapter);
  
		loadingView = findViewById(R.id.loading_layout);		
	}

	@Override
	public void initData() { 
		courseId = getIntent().getStringExtra("courseId");
		courseName = getIntent().getStringExtra("courseName");
		mHeadTitleTV.setText(R.string.course_answers);
		
		mHeadRightBtn.setText(R.string.raise_question);
		if(TextUtils.isEmpty(courseName)){
			courseName = "";
		}
		mCourseTitleTV.setText(courseName); 		
		
		mCourseQuestionListLv.setVisibility(View.VISIBLE); 		
		loadingView.setVisibility(View.VISIBLE);   
		/*mCourseQuestionListLv.setRefreshing(true);*/
		refreshListView();	
	}

	@Override
	public void addListener() { 
		mBackIV.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View arg0) { 
					finish();
				}
			}); 
		
		mHeadRightBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) { 
				UIHelper.showCourseNewQuestionActivity(CourseQuestionListActivity.this, courseId, courseName, "", "");
			}
		});
		
		mCourseQuestionListLv.setonRefreshListener(new OnRefreshListener() {

			@Override
			public void onRefresh() {  
				if(PhoneUtil.checkNetworkEnable(CourseQuestionListActivity.this) != PhoneUtil.NETSTATE_ENABLE){  
					loadingView.setVisibility(View.GONE);				
					mCourseQuestionListLv.onRefreshComplete();  
					mCourseQuestionListLv.setVisibility(View.GONE);
					Toast.makeText(CourseQuestionListActivity.this, getResources().getString(R.string.please_open_network), Toast.LENGTH_SHORT).show(); 
				} else{
					mCourseQuestionListLv.setVisibility(View.VISIBLE);
					
					refreshListView();
					Log.e(TAG, "-----------onRefresh------------");					 
				}				
			}

			@Override
			public void onLoadMore() { 
				if(PhoneUtil.checkNetworkEnable(CourseQuestionListActivity.this) != PhoneUtil.NETSTATE_ENABLE){  
					loadingView.setVisibility(View.GONE);
					mCourseQuestionListLv.onRefreshComplete();  
					mCourseQuestionListLv.setVisibility(View.GONE);
					Toast.makeText(CourseQuestionListActivity.this, getResources().getString(R.string.please_open_network), Toast.LENGTH_SHORT).show(); 
				} else{
					++pageno; 
					requestCourseQuestionData(LOAD_TYPE_MORE);
				}
			}
		}); 
		
		mCourseQuestionListLv.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) 
			{
				int currPosition = position - 1;			 
				CourseQuestion courseQuestion = questions.get(currPosition);
				MyQuestion myQuestion = new MyQuestion();
				myQuestion.setCourse_id(courseId);
				myQuestion.setCourseName(courseName); 
				myQuestion.setAsk_id(courseQuestion.getAskId());
				myQuestion.setQuestion(courseQuestion.getQuestion());
				myQuestion.setTitle(courseQuestion.getTitle());
				myQuestion.setAdd_time(courseQuestion.getAskTime());
				myQuestion.setAnswer(courseQuestion.getAnswer());
				myQuestion.setAn_time(courseQuestion.getAnswerTime());
				myQuestion.setAnswerAvatar(courseQuestion.getAnswerAvatar());
				myQuestion.setAnswerName(courseQuestion.getAnswerName());
				UIHelper.showCourseQuestionDetailActivty(CourseQuestionListActivity.this, myQuestion);
			}

		}); 
	}
	 
	@Override
	public void initAuthoriy() {
		if (!AuthorityManager.getInstance().isStudentAuthority()){
			mHeadRightBtn.setVisibility(View.GONE);
		}   
		super.initAuthoriy();
	}
 
	/**
	 * 请求查看全部答疑数据接口
	 * */
	private void requestCourseQuestionData(final int refreshType) {
		TmingHttp.asyncRequest(ServerApi.getQuestionList(10, pageno, courseId, App.getUserId()),
				new HashMap<String, Object>(),
				new TmingHttp.RequestCallback<String>() {

					@Override
					public String onReqestSuccess(String respones) throws Exception {
						// 数据组装
						Log.i(TAG, respones);
						return respones;
					}

					@Override
					public void success(String respones) {
						loadingView.setVisibility(View.GONE);
						mCourseQuestionListLv.onRefreshComplete();

						// 数据组装
						Log.i(TAG, respones);
						JsonParseObject jsonParseObject = JsonParseObject.parseJson(respones);
						Log.d(TAG, jsonParseObject.getResJs().toString());
						if (Constants.SUCCESS_MSG == jsonParseObject.getFlag()) {
							try {

								if (LOAD_TYPE_REFRESH == refreshType) {
									questions.clear();
									mCourseQuestionAdapter.notifyDataSetChanged();
								}

								if (!jsonParseObject.getResJs().isNull("list")) {
									Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd").create();

									List<CourseQuestion> result = gson.fromJson(jsonParseObject.getResJs().getJSONArray("list").toString(),
											new TypeToken<ArrayList<CourseQuestion>>() {
											}.getType());

									if (result.size() > 0) {
										questions.addAll(result);
										pageno++;
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
							mCourseQuestionAdapter.notifyDataSetChanged();
						} else {
							Toast.makeText(CourseQuestionListActivity.this, R.string.api_error, Toast.LENGTH_SHORT).show();
						}
					}

					@Override
					public void exception(Exception exception) {
						Toast.makeText(CourseQuestionListActivity.this, getResources().getString(R.string.net_error), Toast.LENGTH_SHORT).show();
						exception.printStackTrace();
					}

				});
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		Log.d(TAG, "onActivityResult; requestCode:"+ requestCode+"; resultCode:"+resultCode);
		
		if(RESULT_OK == resultCode)
		{
			mCourseQuestionListLv.setRefreshing(true);
			/*refreshListView();*/
		}
	}
	
	private void refreshListView() { 
		pageno = 1;
		if(questions.size() > 0){
			questions.clear();  
		}
		requestCourseQuestionData(LOAD_TYPE_REFRESH);
	} 
}
