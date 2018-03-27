package com.nd.shapedexamproj.activity.shapedexam;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.nd.shapedexamproj.App;
import com.nd.shapedexamproj.R;
import com.nd.shapedexamproj.activity.BaseActivity;
import com.nd.shapedexamproj.util.Constants;
import com.nd.shapedexamproj.util.PhpApiUtil;
import com.nd.shapedexamproj.util.StringUtils;
import com.nd.shapedexamproj.util.XKUIHelper;
import com.tming.common.adapter.CommonBaseAdapter;
import com.tming.common.cache.net.TmingCacheHttp;
import com.tming.common.net.TmingHttp;
import com.tming.common.view.RefreshableListView;
import com.tming.common.view.support.pulltorefresh.PullToRefreshBase;
import com.tming.openuniversity.model.exam.TaskListInfo;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 形考任务列表
 * Created by zll on 2015/3/2.
 */
public class ShapeExamTaskList extends BaseActivity implements View.OnClickListener{

    private RefreshableListView mListView;
    private View loadingView;
    private TextView noDataTv;
    private TaskListAdapter adapter;
    private List<TaskListInfo> dataList;
    private int mRefreshState;
    private long currentTime = 0;
    private String mCourseId,mStepId,mCourseName;//课程id，学期id
    private int page = 1, size = 10;
    @Override
    public int initResource() {
        return R.layout.xk_task_list;
    }

    @Override
    public void initComponent() {
        findViewById(R.id.commonheader_center_tv).setVisibility(View.GONE);
        mListView = (RefreshableListView) findViewById(R.id.refreshable_listview);
        mListView.setDividerHeight(0);
        loadingView = findViewById(R.id.loading_layout);
        noDataTv = (TextView) findViewById(R.id.no_data_tv);
        mCourseId = getIntent().getStringExtra("courseId");
        mStepId = getIntent().getStringExtra("stepId");
        mCourseName = getIntent().getStringExtra("courseName");
        TextView title = (TextView) findViewById(R.id.commonheader_title_tv);
        title.setText(mCourseName);
        findViewById(R.id.common_head_right_btn).setVisibility(View.GONE);
        findViewById(R.id.commonheader_left_iv).setOnClickListener(this);
        mRefreshState = Constants.PULL_DOWN_TO_REFRESH;
    }

    @Override
    public void initData() {
        dataList = new ArrayList<TaskListInfo>();
        adapter = new TaskListAdapter(this,dataList);
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


    }

    @Override
    protected void onResume() {
        super.onResume();
        requestCurrentTime();
    }

    private void requestCurrentTime() {
        TmingHttp.asyncRequest(Constants.CURRENT_TIME_IN_MILLIS, new TmingHttp.RequestCallback<Long>() {

            @Override
            public Long onReqestSuccess(String respones) throws Exception {
                JSONObject timeObj = new JSONObject(respones);
                long time = timeObj.getLong("data");
                return time == 0 ? System.currentTimeMillis() : time;
            }

            @Override
            public void success(Long respones) {
                currentTime = respones;
                getTaskList();
            }

            @Override
            public void exception(Exception exception) {
                currentTime = System.currentTimeMillis();
                getTaskList();
            }

        });
    }

    private void getTaskList() {
        Map<String,Object> params = new HashMap<String,Object>();
        params.put("stu_id", App.getsStudentId());
        params.put("km_id",mCourseId);//课程id
        if (!StringUtils.isEmpty(mStepId)) {
            params.put("step_id", mStepId);//学期id
        }
        params.put("page",page);
        params.put("size",size);
        PhpApiUtil.sendData(Constants.XK_TASK_LIST, params, new TmingCacheHttp.RequestWithCacheCallBackV2<List<TaskListInfo>>() {

            @Override
            public List<TaskListInfo> parseData(String data) throws Exception {
                JSONObject dataObject = new JSONObject(data);
                TaskListInfo taskListInfo = new TaskListInfo();
                taskListInfo.initWithJsonObject(dataObject);
                return taskListInfo.taskList;
            }

            @Override
            public void cacheDataRespone(List<TaskListInfo> data) {
                if (mRefreshState == Constants.PULL_DOWN_TO_REFRESH) {
                    adapter.clear();
                }
                adapter.addItemCollection(data);
            }

            @Override
            public void requestNewDataRespone(List<TaskListInfo> cacheRespones, List<TaskListInfo> newRespones) {
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
                    noDataTv.setText(getResources().getString(R.string.xk_no_task));
                } else {
                    noDataTv.setText("");
                }
            }
        });
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.commonheader_left_iv:
                finish();
                break;
            default:
                break;
        }
    }

    protected class TaskListAdapter extends CommonBaseAdapter<TaskListInfo> {
        private Context context;
        public TaskListAdapter(Context context, List<TaskListInfo> datas) {
            super(context, datas);
            this.context = context;
        }

        @Override
        public void setViewHolderData(BaseViewHolder holder, final TaskListInfo data) {
            TaskItemHolder taskItemHolder = (TaskItemHolder) holder;
            taskItemHolder.nameTv.setText(data.taskName);
            taskItemHolder.infoTv.setText("权重:" + data.weight + ".0%  答题次数:" + data.times + "/" + data.allowTimes);
            if (data.isStart == 1) {
                long timeLag = getTimeLag(data.finishTime * 1000);
                if (timeLag > 3 * 24 * 60 * 60 * 1000) {
                    taskItemHolder.timeTv.setTextColor(getResources().getColor(R.color.title_green));
                } else if (timeLag >= 0 && timeLag <= 3 * 24 * 60 * 60 * 1000) {
                    taskItemHolder.timeTv.setTextColor(getResources().getColor(R.color.orange));
                } else {
                    taskItemHolder.timeTv.setTextColor(getResources().getColor(R.color.out_of_date_txt_bg));
                }
                String finishTimeStr = new SimpleDateFormat("yyyy-MM-dd").format(new Date(data.finishTime * 1000));
                taskItemHolder.timeTv.setText(finishTimeStr);
            } else {
                taskItemHolder.timeTv.setTextColor(getResources().getColor(R.color.out_of_date_txt_bg));
                taskItemHolder.timeTv.setText(getResources().getString(R.string.xk_not_start));
            }
            taskItemHolder.taskItemRl.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                if (data.isStart == 1) {
                    XKUIHelper.showTaskDetailInfo(context, data.taskId, data.taskName);
                } else {
                    Toast.makeText(context,"任务未开始",Toast.LENGTH_SHORT).show();
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
            holder.timeTv = (TextView) convertView.findViewById(R.id.xk_task_list_item_finishtime);
            convertView.setTag(holder);
            return convertView;
        }

        class TaskItemHolder extends BaseViewHolder {
            public TextView nameTv,infoTv,timeTv;
            public RelativeLayout taskItemRl;
        }
    }

    private long getTimeLag(long finishTime) {
        long timeLag = 0;  //时间差（毫秒）
        timeLag = finishTime - currentTime ; //接口中的时间单位为秒
        return timeLag;
    }
}
