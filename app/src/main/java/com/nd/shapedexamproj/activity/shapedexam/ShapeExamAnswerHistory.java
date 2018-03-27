package com.nd.shapedexamproj.activity.shapedexam;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nd.shapedexamproj.App;
import com.nd.shapedexamproj.R;
import com.nd.shapedexamproj.activity.BaseActivity;
import com.nd.shapedexamproj.util.Constants;
import com.nd.shapedexamproj.util.PhpApiUtil;
import com.nd.shapedexamproj.util.XKUIHelper;
import com.tming.common.adapter.CommonBaseAdapter;
import com.tming.common.cache.net.TmingCacheHttp;
import com.tming.common.view.RefreshableListView;
import com.tming.common.view.support.pulltorefresh.PullToRefreshBase;
import com.tming.openuniversity.model.exam.AnswerHistoryList;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 答题历史记录列表
 * Created by zll on 2015/3/2.
 */
public class ShapeExamAnswerHistory extends BaseActivity {

    private RefreshableListView mListView;
    private View loadingView;
    private TextView noDataTv;
    private TaskListAdapter adapter;
    private int mRefreshState ;
    private String mTaskId,mTaskName;//任务id,任务名称
    private int page = 1, size = 10;
    @Override
    public int initResource() {
        return R.layout.xk_task_list;
    }

    @Override
    public void initComponent() {
        mListView = (RefreshableListView) findViewById(R.id.refreshable_listview);
        mListView.setDividerHeight(0);
        noDataTv = (TextView) findViewById(R.id.no_data_tv);
        mTaskId = getIntent().getStringExtra("taskId");
        mTaskName = getIntent().getStringExtra("taskName");
        findViewById(R.id.common_head_right_btn).setVisibility(View.GONE);
        TextView title = (TextView) findViewById(R.id.commonheader_title_tv);
        title.setText(mTaskName);
        loadingView = findViewById(R.id.loading_layout);
        mRefreshState = Constants.PULL_DOWN_TO_REFRESH;
    }

    @Override
    public void initData() {
        adapter = new TaskListAdapter(this);
        mListView.setAdapter(adapter);
    }

    @Override
    public void addListener() {
        mListView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2<ListView>() {
            @Override
            public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
                page = 1;
                mRefreshState = Constants.PULL_DOWN_TO_REFRESH;
                getTaskList();
            }

            @Override
            public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
                ++ page;
                mRefreshState = Constants.PULL_UP_TO_REFRESH;
                getTaskList();
            }
        });
        findViewById(R.id.commonheader_left_iv).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        getTaskList();
    }

    private void getTaskList() {
        Map<String,Object> params = new HashMap<String,Object>();
        params.put("stu_id", App.getsStudentId());
        params.put("task_id",mTaskId);//课程id
        params.put("page",page);
        params.put("size",size);
        PhpApiUtil.sendData(Constants.XK_ANSWER_LIST, params, new TmingCacheHttp.RequestWithCacheCallBackV2<List<AnswerHistoryList>>() {

            @Override
            public List<AnswerHistoryList> parseData(String data) throws Exception {
                JSONObject dataObject = new JSONObject(data);
                AnswerHistoryList taskListInfo = new AnswerHistoryList();
                taskListInfo.initWithJsonObject(dataObject);
                return taskListInfo.answerHistoryList;
            }

            @Override
            public void cacheDataRespone(List<AnswerHistoryList> data) {
                if (mRefreshState == Constants.PULL_DOWN_TO_REFRESH) {
                    adapter.clear();
                }
                adapter.addItemCollection(data);
            }

            @Override
            public void requestNewDataRespone(List<AnswerHistoryList> cacheRespones, List<AnswerHistoryList> newRespones) {
                if (mRefreshState == Constants.PULL_DOWN_TO_REFRESH) {
                    adapter.clear();
                }
                adapter.replaceItem(cacheRespones, newRespones);
            }

            @Override
            public void exception(Exception e) {
                e.printStackTrace();
            }

            @Override
            public void onFinishRequest() {
                adapter.notifyDataSetChanged();
                mListView.onRefreshComplete();
                loadingView.setVisibility(View.GONE);
                if (adapter.getCount() == 0) {
                    noDataTv.setText(getResources().getString(R.string.xk_no_record));
                } else {
                    noDataTv.setText("");
                }
            }
        });
    }

    protected class TaskListAdapter extends CommonBaseAdapter<AnswerHistoryList> {
        private Context context;

        public TaskListAdapter(Context context) {
            super(context);
            this.context = context;
        }

        public TaskListAdapter(Context context, List<AnswerHistoryList> datas) {
            super(context, datas);
            this.context = context;
        }

        @Override
        public void setViewHolderData(BaseViewHolder holder, final AnswerHistoryList data) {
            TaskItemHolder taskItemHolder = (TaskItemHolder) holder;
            taskItemHolder.nameTv.setText("答题记录" + data.doTime);
            String finishTimeStr;
            if (data.start == 0) {
                finishTimeStr = "未提交";
            } else {
                finishTimeStr = new SimpleDateFormat("yyyy-MM-dd").format(new Date(data.start * 1000));
            }
            taskItemHolder.infoTv.setText("答题时间:" + finishTimeStr);

            if (data.status == 1) {
                taskItemHolder.scoreTv.setTextColor(getResources().getColor(R.color.orange));
                taskItemHolder.scoreTv.setText("继续任务");
            } else if (data.status == 6) {
                taskItemHolder.scoreTv.setTextColor(getResources().getColor(R.color.out_of_date_txt_bg));
                taskItemHolder.scoreTv.setText("已经过期");
            } else if (data.status != 1 && data.status != 6) {
                if (data.score >= 60) {
                    taskItemHolder.scoreTv.setTextColor(getResources().getColor(R.color.title_green));
                } else {
                    taskItemHolder.scoreTv.setTextColor(getResources().getColor(R.color.orange));
                }
                taskItemHolder.scoreTv.setText("" + data.score);
            }

            taskItemHolder.taskItemRl.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (data.status != 6) {//TODO
                        XKUIHelper.showAnswerRecordDetail(context,mTaskId,data.recordId, "答题记录" + data.doTime);
                    }
                }
            });
        }

        @Override
        public View infateItemView(Context context) {
            View convertView = LayoutInflater.from(context).inflate(R.layout.xk_task_list_item,null);
            TaskItemHolder holder = new TaskItemHolder();

            holder.taskItemRl = (RelativeLayout) convertView.findViewById(R.id.xk_task_list_item_rl);
            holder.nameTv = (TextView) convertView.findViewById(R.id.xk_task_list_item_name);
            holder.infoTv = (TextView) convertView.findViewById(R.id.xk_task_list_item_info);
            holder.scoreTv = (TextView) convertView.findViewById(R.id.xk_task_list_item_finishtime);
            convertView.setTag(holder);
            return convertView;
        }

        class TaskItemHolder extends BaseViewHolder {
            public TextView nameTv,infoTv,scoreTv;
            public RelativeLayout taskItemRl;
        }
    }
}
