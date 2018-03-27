package com.nd.shapedexamproj.activity.homework;

import android.content.Context;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.nd.shapedexamproj.App;
import com.nd.shapedexamproj.R;
import com.nd.shapedexamproj.activity.BaseActivity;
import com.nd.shapedexamproj.model.HomeworkSumary;
import com.nd.shapedexamproj.net.ServerApi;
import com.nd.shapedexamproj.util.JsonUtil;
import com.nd.shapedexamproj.util.UIHelper;
import com.tming.common.adapter.CommonBaseAdapter;
import com.tming.common.cache.net.TmingCacheHttp;
import com.tming.common.view.support.pulltorefresh.PullToRefreshBase;
import com.tming.common.view.support.pulltorefresh.PullToRefreshListView;
import org.json.JSONObject;

import java.util.List;

/**
 * 已完成作业概览页面
 * <P>created by yusongying on 20150113</P>
 */
public class FinishedHomeworkActivity extends BaseActivity implements AdapterView.OnItemClickListener, PullToRefreshBase.OnRefreshListener2<ListView>, View.OnClickListener, TmingCacheHttp.RequestWithCacheCallBackV2<List<HomeworkSumary>> {

	private PullToRefreshListView mListView;
	private FinishHomeworkSumaryAdapter mAdapter;
	private View loadingView;
	private View errorView;
	private View noDataTv;

	@Override
	public int initResource() {
		return R.layout.finished_homework_sumary_activity;
	}

	@Override
	public void initComponent() {
		mListView = (PullToRefreshListView) findViewById(R.id.finished_homework_sumary_activity_lv);
		mAdapter = new FinishHomeworkSumaryAdapter(this);
		mListView.setAdapter(mAdapter);
		errorView = findViewById(R.id.finished_homework_sumary_activity_error_lay);
		loadingView = findViewById(R.id.finished_homework_sumary_activity_loading_lay);
		noDataTv = findViewById(R.id.no_data_tv);
		findViewById(R.id.common_head_right_btn).setVisibility(View.GONE);
		TextView titleTv = (TextView) findViewById(R.id.commonheader_title_tv);
		titleTv.setText("已完成");
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		HomeworkSumary homeworkSumary = (HomeworkSumary) mAdapter.getItem(position - 1);
		UIHelper.showFinishedHomework(this, homeworkSumary.getCourseId());
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.error_btn:
				initData();
				errorView.setVisibility(View.GONE);
				loadingView.setVisibility(View.VISIBLE);
				break;
			case R.id.commonheader_left_iv:
				finish();
				break;
		}
	}

	@Override
	public List<HomeworkSumary> parseData(String data) throws Exception {
		JSONObject jsonObject = new JSONObject(data);
		if (JsonUtil.checkPhpApiALLIsOK(jsonObject)) {
			return JsonUtil.paraseJsonArray(jsonObject.getJSONArray("data"), HomeworkSumary.class);
		}
		throw new Exception("解析错误!");
	}

	@Override
	public void cacheDataRespone(List<HomeworkSumary> data) {
		mAdapter.clear();
		mAdapter.addItemCollection(data);
		mAdapter.notifyDataSetChanged();
		mListView.onRefreshComplete();
	}

	@Override
	public void requestNewDataRespone(List<HomeworkSumary> cacheRespones, List<HomeworkSumary> newRespones) {
		mAdapter.clear();
		mAdapter.addItemCollection(newRespones);
		mAdapter.notifyDataSetChanged();
		mListView.onRefreshComplete();
	}

	@Override
	public void exception(Exception exception) {
		mListView.setVisibility(View.GONE);
		errorView.setVisibility(View.VISIBLE);

	}

	@Override
	public void onFinishRequest() {
		loadingView.setVisibility(View.GONE);
		if (mAdapter.getCount() == 0) {
			noDataTv.setVisibility(View.VISIBLE);
		} else {
			noDataTv.setVisibility(View.GONE);
		}
	}

	@Override
	public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
		initData();
		loadingView.setVisibility(View.VISIBLE);
	}

	@Override
	public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {

	}


	private class FinishHomeworkSumaryAdapter extends CommonBaseAdapter<HomeworkSumary> {

		public FinishHomeworkSumaryAdapter(Context context) {
			super(context);
		}

		@Override
		public void setViewHolderData(BaseViewHolder holder, HomeworkSumary data) {
			ViewHolder viewHolder = (ViewHolder) holder;
			viewHolder.courseNameTv.setText(data.getCourseName());
			viewHolder.finishedCountTv.setText(String.valueOf(data.getDoneCount()));
			viewHolder.passCountTv.setText(String.valueOf(data.getPassCount()));
			viewHolder.toBeMarkedCountTv.setText(String.valueOf(data.getToBeMarkedCount()));
		}

		@Override
		public View infateItemView(Context context) {
			View view = View.inflate(context, R.layout.finished_homework_sumary_item, null);
			ViewHolder viewHolder = new ViewHolder();
			View finishedLay = view.findViewById(R.id.finished_homework_sumary_item_finished_lay);
			View failLay = view.findViewById(R.id.finished_homework_sumary_item_pass_lay);
			View toBeMarkedLay = view.findViewById(R.id.finished_homework_sumary_item_to_be_marked_lay);

			viewHolder.courseNameTv = (TextView) view.findViewById(R.id.finished_homework_sumary_item_title_tv);
			viewHolder.finishedCountTv = initSumaryItemOne(finishedLay, "已完成");
			viewHolder.passCountTv = initSumaryItemOne(failLay, "及格");
			viewHolder.toBeMarkedCountTv = initSumaryItemOne(toBeMarkedLay, "待批改");

			view.setTag(viewHolder);
			return view;
		}

		private TextView initSumaryItemOne(View lay, String text) {
			TextView titleTv = (TextView) lay.findViewById(R.id.finished_homework_sumary_item_one_title_tv);
			TextView countTv = (TextView) lay.findViewById(R.id.finished_homework_sumary_item_one_count_tv);
			titleTv.setText(text);
			return countTv;
		}

		class ViewHolder extends BaseViewHolder {
			TextView courseNameTv;
			TextView finishedCountTv;
			TextView passCountTv;
			TextView toBeMarkedCountTv;
		}
	};

	@Override
	public void initData() {
		TmingCacheHttp.getInstance().asyncRequestWithCache(ServerApi.getStudentCourseInfo(App.getUserId(), "all"), null, this);
	}

	@Override
	public void addListener() {
		mListView.setOnItemClickListener(this);
		mListView.setOnRefreshListener(this);
		findViewById(R.id.commonheader_left_iv).setOnClickListener(this);
		findViewById(R.id.error_btn).setOnClickListener(this);
	}
}
