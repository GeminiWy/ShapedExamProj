package com.nd.shapedexamproj.activity.my;

import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.*;

import com.nd.shapedexamproj.App;
import com.nd.shapedexamproj.AuthorityManager;
import com.nd.shapedexamproj.R;
import com.nd.shapedexamproj.activity.BaseActivity;
import com.nd.shapedexamproj.adapter.MyQuestionAdapter;
import com.nd.shapedexamproj.model.my.MyQuestion;
import com.nd.shapedexamproj.util.Constants;
import com.nd.shapedexamproj.util.StringUtils;
import com.tming.common.cache.net.TmingCacheHttp;
import com.tming.common.net.TmingHttp;
import com.tming.common.net.TmingHttp.RequestCallback;
import com.tming.common.view.RefreshableListView;
import com.tming.common.view.RefreshableListView.OnRefreshListener;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
/**
 * 我的提问
 * @author zll
 * create in 2014-4-20
 */
public class MyQuestionsActivity extends BaseActivity {
	private static final String TAG = "MyQuestionsActivity"; 
	
	private Button commonHeadRightBtn ;
	private ImageView commonheaderLeftIv;
	private TextView commonheaderTitleTv;
	
	private RefreshableListView homeworkList;
	private LinearLayout loadingLayout;
	private RelativeLayout errorLayout;
	private Button errorBtn ;
	//没有数据的页面
	private RelativeLayout myquestionitemRl;
	
	private MyQuestionAdapter adapter ;
	
	TmingCacheHttp cacheHttp ;
	private boolean refresh = false;
	
	private String courseid = "0";
	private int pageNum = 1;
	private int pageSize = 10;
	/**
	 * 判断是否已经加载过数据,true表示是已经加载过数据，false表示还没有加载过数据。
	 * */
	private boolean hasLoadedData = false;
	
	@Override
	public int initResource() {
		return R.layout.my_questions_activity;
	}

	@Override
	public void initComponent() {
		
		courseid = getIntent().getStringExtra(Constants.COURSE_ID);
		commonheaderLeftIv = (ImageView) findViewById(R.id.commonheader_left_iv);
		commonheaderTitleTv = (TextView) findViewById(R.id.commonheader_title_tv);
//		common_head_left_btn = (Button) findViewById(R.id.common_head_left_btn);
		if (AuthorityManager.getInstance().isTeacherAuthority()) {
			commonheaderTitleTv.setText(getResources().getString(R.string.question_reply));
		} else {
			commonheaderTitleTv.setText(getResources().getString(R.string.my_question));
		}

		commonHeadRightBtn = (Button) findViewById(R.id.common_head_right_btn);
		commonHeadRightBtn.setVisibility(View.GONE);
		homeworkList = (RefreshableListView) findViewById(R.id.refreshable_listview);
		loadingLayout = (LinearLayout) findViewById(R.id.loading_layout);
		errorLayout = (RelativeLayout) findViewById(R.id.error_layout);
		errorBtn = (Button) findViewById(R.id.error_btn);
		//没有提问过的界面
		myquestionitemRl = (RelativeLayout) findViewById(R.id.myquestionitem_rl);
		cacheHttp = TmingCacheHttp.getInstance(this);
		adapter = new MyQuestionAdapter(this, MyQuestionAdapter.MY);
		homeworkList.setAdapter(adapter);
		
	}

	@Override
	public void initData() {
		hasLoadedData = false;
		requestData();
	}

	@Override
	public void addListener() {
		commonheaderLeftIv.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				finish();
			}
		});
		
		homeworkList.setOnItemClickListener(adapter);
		homeworkList.setonRefreshListener(new OnRefreshListener() {
			
			@Override
			public void onRefresh() {
				refresh = true ;
				pageNum = 1;
				adapter.clear();
				requestData();
			}
			
			@Override
			public void onLoadMore() {
				refresh = true ;
				++ pageNum;
				requestData();
			}
		});
		errorBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				pageNum = 1;
				requestData();
			}
		});
		
	}
	
	private void requestData(){
		Map<String,Object> map = new HashMap<String, Object>();
		if(!StringUtils.isEmpty(courseid) && !courseid.equals("0")){
			map.put("courseid", courseid);
		}
		map.put("userid", App.getUserId());
		map.put("pageNum", pageNum);
		map.put("pageSize", pageSize);
		String urlStr = Constants.MY_QUESTIONS;
		TmingHttp.asyncRequest(urlStr, map, new RequestCallback<List<MyQuestion>>() {

            @Override
            public List<MyQuestion> onReqestSuccess(String respones) throws Exception {
                return MyQuestion.jsonParsing(respones);
            }

            @Override
            public void success(List<MyQuestion> respones) {
                loadData(respones); 
                homeworkList.onRefreshComplete();
            }

            @Override
            public void exception(Exception exception) {
                loadingLayout.setVisibility(View.GONE);
                errorLayout.setVisibility(View.VISIBLE);
                Toast.makeText(MyQuestionsActivity.this, "网络异常", Toast.LENGTH_SHORT).show();
            }

			/*@Override
			public List<MyQuestion> onPreRequestCache(String cache)
					throws Exception { 
				return MyQuestion.jsonParsing(cache);
			}

			@Override
			public void onPreRequestSuccess(List<MyQuestion> data) { 
				loadData(data); 
				homeworkList.onRefreshComplete();
			}

			@Override
			public List<MyQuestion> onReqestSuccess(String respones)
					throws Exception { 
				return MyQuestion.jsonParsing(respones);
			}

			@Override
			public void success(List<MyQuestion> cacheRespones,
					List<MyQuestion> newRespones) { 
				loadData(cacheRespones,newRespones);
				homeworkList.onRefreshComplete();
			}

			@Override
			public void exception(Exception exception) { 
				loadingLayout.setVisibility(View.GONE);
				errorLayout.setVisibility(View.VISIBLE);
				Toast.makeText(MyQuestionsActivity.this, "网络异常", Toast.LENGTH_SHORT).show();
			}*/
			
		});
		
	}

	private void loadData(List<MyQuestion> data ){
		loadingLayout.setVisibility(View.GONE);
		errorLayout.setVisibility(View.GONE);
		if(data == null){
			Log.e(TAG, "loadData data == null");
			return ;
		}
		if(data.size() == 0 && adapter.getCount() == 0){		//没有提问列表
			myquestionitemRl.setVisibility(View.VISIBLE);
			return;
		}else{
			myquestionitemRl.setVisibility(View.GONE);
		}
		hasLoadedData = true;
		adapter.addItemCollection(data);
		adapter.notifyDataSetChanged();
	}
	
	private void loadData(List<MyQuestion> cacheRespones,List<MyQuestion> newRespones){
		loadingLayout.setVisibility(View.GONE);
		errorLayout.setVisibility(View.GONE);
		if (newRespones == null) {
			return;
		}
		
		if(newRespones.size() == 0 && adapter.getCount() == 0){		//没有提问列表
			myquestionitemRl.setVisibility(View.VISIBLE);
			Log.e(TAG, "loadData more 11");
			return;
		}else{
			myquestionitemRl.setVisibility(View.GONE);
		}
		hasLoadedData = true;
		Log.e(TAG, "loadData more 12");
		
		adapter.replaceItem(cacheRespones, newRespones);
		adapter.notifyDataSetChanged();
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		Log.d(TAG, "onActivityResult; requestCode:"+ requestCode+"; resultCode:"+resultCode);
		
		if(RESULT_OK == resultCode)
		{
			homeworkList.setRefreshing(true);
			/*refresh = true ;
			pageNum = 1;
			requestData();*/
		}
	}
	
}
