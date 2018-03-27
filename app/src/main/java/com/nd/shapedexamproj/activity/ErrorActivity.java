package com.nd.shapedexamproj.activity;

import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.nd.shapedexamproj.App;
import com.nd.shapedexamproj.R;
import com.nd.shapedexamproj.adapter.ErrorAdapter;
import com.nd.shapedexamproj.model.ErrorQuestion;
import com.nd.shapedexamproj.util.Constants;
import com.tming.common.cache.net.TmingCacheHttp;
import com.tming.common.view.RefreshableListView;
import com.tming.common.view.RefreshableListView.OnRefreshListener;

import java.util.*;

/**
 * 
 * 错题集列表
 * 
 * @author Linlg
 * 
 *         Create on 2014-4-29
 */
public class ErrorActivity extends BaseActivity {
	private int page = 1;
	private int pageSize = 10;
	private TmingCacheHttp cacheHttp;
	private List<ErrorQuestion> errorQuestions = new ArrayList<ErrorQuestion>();
	private ErrorAdapter adapter;
	private RefreshableListView lv;
	private TextView errorQuestionNodataTV ;
	
	@Override
	public int initResource() {
		return R.layout.error_question;
	}

	@Override
	public void initComponent() {
		
		cacheHttp = TmingCacheHttp.getInstance(this);
		findViewById(R.id.commonheader_right_btn).setVisibility(View.GONE);
		findViewById(R.id.error_question_head_layout).findViewById(R.id.commonheader_left_iv)
				.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						finish();
					}
				});
		((TextView) findViewById(R.id.error_question_head_layout).findViewById(
				R.id.commonheader_title_tv)).setText(getResources().getString(R.string.redo_error_question));
		lv = (RefreshableListView) findViewById(R.id.error_question_lv);
		errorQuestionNodataTV = (TextView) findViewById(R.id.error_question_nodata_tv);
		
		// lv.setOnItemClickListener(new OnItemClickListener() {
		//
		// @Override
		// public void onItemClick(AdapterView<?> parent, View view, int
		// position, long id) {
		// Intent it = new Intent(ErrorActivity.this, RedoErrorsActivity.class);
		// System.out.println("errorQuestions.get(position).courseId:" +
		// errorQuestions.get(position).courseId);
		// it.putExtra("courseId", errorQuestions.get(position).courseId);
		// startActivity(it);
		// }
		//
		//
		// });
		adapter = new ErrorAdapter(ErrorActivity.this);
		lv.setAdapter(adapter);
		lv.setOnClickListener(adapter);
		requestData();

	}

	@Override
	public void initData() {

	}

	@Override
	public void addListener() {
		lv.setonRefreshListener(new OnRefreshListener() {
			
			@Override
			public void onRefresh() {
				page = 1;
				requestData();
			}
			
			@Override
			public void onLoadMore() {
				++ page ;
				requestData();
			}
		});
	}

	private void requestData() {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("pageNum", page);
		params.put("pageSize", pageSize);
		params.put("userid", App.getUserId());
		cacheHttp.asyncRequestWithCache(Constants.HOST + "errquestion/errList.html", params,
				new TmingCacheHttp.RequestWithCacheCallBackV2<List<ErrorQuestion>>() {

					@Override
					public List<ErrorQuestion> parseData(String data) throws Exception {
						return ErrorQuestion.JSONPasing(data);
					}

					@Override
					public void cacheDataRespone(List<ErrorQuestion> data) {
						lv.onRefreshComplete();
						setErrorView(data);
					}

					@Override
					public void requestNewDataRespone(List<ErrorQuestion> cacheRespones, List<ErrorQuestion> newRespones) {
						lv.onRefreshComplete();
						//过滤 错误=0
						Iterator<ErrorQuestion> it = newRespones.iterator();
						while (it.hasNext()) {
							ErrorQuestion eq = it.next();
							if (eq.error == 0)
								it.remove();
						}

						adapter.replaceItem(cacheRespones, newRespones);
						adapter.notifyDataSetChanged();
					}

					@Override
					public void exception(Exception exception) {
						lv.onRefreshComplete();
					}

					@Override
					public void onFinishRequest() {
						if (adapter.getCount() == 0) {
							errorQuestionNodataTV.setVisibility(View.VISIBLE);
						} else {
							errorQuestionNodataTV.setVisibility(View.GONE);
						}
					}
				});
	}

	private void setErrorView(List<ErrorQuestion> data) {
		for(int i = 0;i < data.size();i ++){
			ErrorQuestion eQeustion = data.get(i);
			if(!errorQuestions.contains(eQeustion) && eQeustion.error != 0) {
				errorQuestions.add(eQeustion);
			}
		}
		adapter.clear();
		adapter.addItemCollection(errorQuestions);
		adapter.notifyDataSetChanged();
	}

}
