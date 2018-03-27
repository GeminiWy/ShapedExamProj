package com.nd.shapedexamproj.activity.homework;

import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.nd.shapedexamproj.App;
import com.nd.shapedexamproj.R;
import com.nd.shapedexamproj.activity.BaseActivity;
import com.nd.shapedexamproj.model.FinishedHomework;
import com.nd.shapedexamproj.model.homework.Homework;
import com.nd.shapedexamproj.net.ServerApi;
import com.nd.shapedexamproj.util.JsonUtil;
import com.nd.shapedexamproj.util.UIHelper;
import com.tming.common.adapter.CommonBaseAdapter;
import com.tming.common.cache.net.TmingCacheHttp;
import com.tming.common.net.TmingHttpRequestWithCacheTask;
import com.tming.common.view.RefreshableListView;
import org.json.JSONObject;

import java.util.List;

/**
 * 已完成的作业详情
 * Created by yusongying on 2015/1/13.
 */
public class FinishedHomeworkDetailActivity extends BaseActivity implements OnClickListener,RefreshableListView.OnRefreshListener, TmingCacheHttp.RequestWithCacheCallBackV2<List<FinishedHomework>> {

    private RefreshableListView mListView;
    private DetailHomeworkAdapter mAdapter;
    private View errorView, loadingView;
    private String courseId;
    private int pageNo = 1;
    private TextView noDataTv;
    private TmingHttpRequestWithCacheTask mTask;
    
    @Override
    public int initResource() {
        return R.layout.finished_homework_detail_activity;
    }

    @Override
    public void initComponent() {
        mListView = (RefreshableListView) findViewById(R.id.finished_homework_detail_activity_lv);
        mAdapter = new DetailHomeworkAdapter(this);
        mListView.setAdapter(mAdapter);
        noDataTv = (TextView) findViewById(R.id.no_data_tv);
        errorView = findViewById(R.id.finished_homework_detail_activity_error_lay);
        loadingView = findViewById(R.id.finished_homework_detail_activity_loading_lay);
        findViewById(R.id.common_head_right_btn).setVisibility(View.GONE);
        TextView titleTv = (TextView) findViewById(R.id.commonheader_title_tv);
        titleTv.setText("已完成");
    }

    @Override
    protected void onResume() {
        super.onResume();
        onRefresh();
    }

    @Override
    public void initData() {
        courseId = getIntent().getStringExtra("courseId");
    }

    private void requestData() {
        if (mTask == null) {
            String requestUrl = ServerApi.getExamList(App.getUserId(), true, courseId, false, pageNo++, 20);
            mTask = TmingCacheHttp.getInstance().asyncRequestWithCache(requestUrl, null, this);
        }
    }

    @Override
    public void addListener() {
        findViewById(R.id.commonheader_left_iv).setOnClickListener(this);
        findViewById(R.id.error_btn).setOnClickListener(this);
        mListView.setonRefreshListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.error_btn:
                pageNo = 1;
                requestData();
                loadingView.setVisibility(View.VISIBLE);
                errorView.setVisibility(View.GONE);
                break;
            case R.id.commonheader_left_iv:
                finish();
                break;
        }
    }

    @Override
    public List<FinishedHomework> parseData(String data) throws Exception {
        JSONObject jsonObject = new JSONObject(data);
        if (JsonUtil.checkPhpApiALLIsOK(jsonObject)) {
            return JsonUtil.paraseJsonArray(jsonObject.getJSONObject("data").getJSONArray("list"), FinishedHomework.class);
        }
        throw new Exception("api error");
    }

    @Override
    public void cacheDataRespone(List<FinishedHomework> data) {
        if (pageNo == 2) {
            mAdapter.clear();
        }
        mAdapter.addItemCollection(data);
        mAdapter.notifyDataSetChanged();
        loadingView.setVisibility(View.GONE);
        errorView.setVisibility(View.GONE);
        mListView.setVisibility(View.VISIBLE);
        mListView.onRefreshComplete();
    }

    @Override
    public void requestNewDataRespone(List<FinishedHomework> cacheRespones, List<FinishedHomework> newRespones) {
        if (pageNo == 2) {
            mAdapter.clear();
            mAdapter.addItemCollection(newRespones);
        } else {
            mAdapter.replaceItem(cacheRespones, newRespones);
        }
        mAdapter.notifyDataSetChanged();
        loadingView.setVisibility(View.GONE);
        errorView.setVisibility(View.GONE);
        mListView.setVisibility(View.VISIBLE);
        mListView.onRefreshComplete();
    }

    @Override
    public void exception(Exception exception) {
        loadingView.setVisibility(View.GONE);
        errorView.setVisibility(View.VISIBLE);
        mListView.setVisibility(View.GONE);
        noDataTv.setVisibility(View.VISIBLE);
    }

    @Override
    public void onFinishRequest() {
        if (mAdapter.getCount() == 0) {
            noDataTv.setVisibility(View.VISIBLE);
        } else {
            noDataTv.setVisibility(View.GONE);
        }
        mTask = null;
        mListView.onRefreshComplete();
    }

    @Override
    public void onRefresh() {
        pageNo = 1;
        requestData();
    }

    @Override
    public void onLoadMore() {
        requestData();
    }

    private class DetailHomeworkAdapter extends CommonBaseAdapter<FinishedHomework> {

        public DetailHomeworkAdapter(Context context) {
            super(context);
        }

        @Override
        public void setViewHolderData(BaseViewHolder holder, final FinishedHomework data) {
            ViewHolder viewHolder = (ViewHolder) holder;
            viewHolder.titleTv.setText(data.getExameName());
            viewHolder.subTitleTv.setText(
                    String.format("答题次数:%-8s最高得分:%s分", data.getDoTimes() + "/" + data.getMaxDoTimes()
                            , data.getScore() % 1 != 0 ? String.valueOf(data.getScore()) : String.valueOf((int) data.getScore())));

            // 未过期并且存在本地做题数据
            if (data.getTimeEnd() > System.currentTimeMillis() && Homework.checkUserHasDoHomework(data.getExameId()) ) {
                viewHolder.scoreTv.setText("未提交");
                viewHolder.scoreTv.setTextColor(getResources().getColor(R.color.orange));
            } else {
                if (data.isMarked()) {
                    int textColor = 0xFFF99E0C;
                    if (data.getScore() >= 60) {
                        textColor = 0xFF1BBE87;
                    }
                    viewHolder.scoreTv.setTextColor(textColor);
                    viewHolder.scoreTv.setText(String.valueOf(data.getScore()));
                } else {
                    viewHolder.scoreTv.setTextColor(getResources().getColor(R.color.light_black));
                    viewHolder.scoreTv.setText("待批改");
                }
            }
            viewHolder.itemLL.setOnClickListener(new OnClickListener() {
                
                @Override
                public void onClick(View v) {
                    UIHelper.showHomeworkDetailActivity(mContext, data.getExameId(),data.getExameName(),data.getScore());
                }
            });
        }

        @Override
        public View infateItemView(Context context) {
            View view = View.inflate(context, R.layout.finished_homework_detail_item, null);
            ViewHolder viewHolder = new ViewHolder();
            view.setTag(viewHolder);
            
            viewHolder.itemLL = (LinearLayout) view.findViewById(R.id.finished_homework_detail_item_ll);
            viewHolder.titleTv = (TextView) view.findViewById(R.id.finished_homework_detail_item_title_tv);
            viewHolder.subTitleTv = (TextView) view.findViewById(R.id.finished_homework_detail_item_subtitle_tv);
            viewHolder.scoreTv = (TextView) view.findViewById(R.id.finished_homework_detail_item_score_tv);
            
            return view;
        }

        private class ViewHolder extends BaseViewHolder {
            LinearLayout itemLL;
            TextView titleTv;
            TextView subTitleTv;
            TextView scoreTv;
        }
    }
}
