package com.nd.shapedexamproj.activity.shapedexam;


import android.view.LayoutInflater;
import android.view.View;
import android.widget.*;

import com.nd.shapedexamproj.App;
import com.nd.shapedexamproj.R;
import com.nd.shapedexamproj.activity.BaseActivity;
import com.nd.shapedexamproj.util.Constants;
import com.nd.shapedexamproj.util.PhpApiUtil;
import com.nd.shapedexamproj.util.XKUIHelper;
import com.nd.shapedexamproj.view.CircularImage;
import com.nd.shapedexamproj.view.RoundProgressBar;
import com.tming.common.cache.net.TmingCacheHttp;
import com.tming.common.net.TmingHttp;
import com.tming.openuniversity.model.exam.TaskDetailInfo;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 形考任务详情页
 * Created by Administrator on 2015/3/2.
 */
public class ShapeExamDetailActivity extends BaseActivity {
    private static final String TAG = "ShapeExamDetailActivity";

    private ImageView commonheader_left_iv;
    private TextView commonheader_title_tv,commonheaderCenterTv;
    private Button commonheaderRightBtn;

    private LinearLayout loading_layout;

    private LinearLayout homework_detail_type_ll;
    private RelativeLayout homework_detail_all_rl,bottomBtnLayout;
    private TextView homework_detail_all_tv, homework_detail_times_tv;
    private TextView deadline_tv; //截止时间

    private RelativeLayout mCurrScoreImgRL,mHighestScoreImgRL;  //中间圆形得分布局
    private CircularImage highestScoreImg ;
    private RoundProgressBar highestScoreRp ;
    private TextView mCurrentScoreTv,highestScoreTv;

    private Button start_test_btn;

    /*private boolean isUndoneRLClickable ;*/	//是否可以跳到错题重做
    private boolean isCanTest = true ;		//是否可以答题
    private int pageNum = 1,pageSize = 100 ;
    private int total_num ;			//总题数
    //0 未答题；1做了部分但未提交；2提交了但不及格；3提交了并及格；4提交了并满分；5 提交了未批改
    /**
     * 0是未提交，1是提交。
     */
    private int mStatus ;
    private long currentTime = 0;
    private String mTaskName = "";
    private String mTaskid = "-1";
    private List<TaskDetailInfo.SubjectHolder> holderList ;	//题型列表

    @Override
    public int initResource() {
        return R.layout.xk_task_detail;
    }

    @Override
    public void initComponent() {
        mTaskid = getIntent().getStringExtra("taskId");
        mTaskName = getIntent().getStringExtra("taskName");

        holderList = new ArrayList<TaskDetailInfo.SubjectHolder>();

        commonheaderRightBtn = (Button) findViewById(R.id.common_head_right_btn);
        commonheaderRightBtn.setText("历史记录");
        commonheaderCenterTv = (TextView) findViewById(R.id.commonheader_center_tv);
        commonheader_left_iv = (ImageView) findViewById(R.id.commonheader_left_iv);
        commonheader_title_tv = (TextView) findViewById(R.id.commonheader_title_tv);
        commonheader_title_tv.setText(mTaskName == null ? "" : mTaskName);

        loading_layout = (LinearLayout) findViewById(R.id.loading_layout);

        homework_detail_all_tv = (TextView) findViewById(R.id.homework_detail_all_tv);
        homework_detail_times_tv = (TextView) findViewById(R.id.homework_detail_times_tv);
        //截止时间
        deadline_tv = (TextView) findViewById(R.id.deadline_tv);

        homework_detail_all_rl = (RelativeLayout) findViewById(R.id.homework_detail_all_rl);
        //-------------------中间得分布局------------------------
        mHighestScoreImgRL = (RelativeLayout) findViewById(R.id.homework_score_highest_rl);
        mCurrScoreImgRL = (RelativeLayout) findViewById(R.id.homework_score_rl);
        //圆形图片
        highestScoreImg = (CircularImage) findViewById(R.id.homework_score_highest_img);
        highestScoreImg.setImageDrawable(getResources().getDrawable(R.drawable.white));
        //圆形进度条
        highestScoreRp = (RoundProgressBar) findViewById(R.id.homework_score_highest_rp);
        highestScoreRp.setProgress(0);
        highestScoreRp.postInvalidate();
        //得分
        mCurrentScoreTv = (TextView) findViewById(R.id.current_score_tv);
        highestScoreTv = (TextView) findViewById(R.id.homework_score_highest_tv);
        //---------------------中间得分布局---------------------
        //题型
        homework_detail_type_ll = (LinearLayout) findViewById(R.id.homework_detail_type_ll);

        //开始做题
        start_test_btn = (Button) findViewById(R.id.start_test_btn);
        bottomBtnLayout = (RelativeLayout) findViewById(R.id.bottom_btn_layout);
    }

    @Override
    public void initData() {

    }

    @Override
    protected void onResume() {
        //根据数据库判断作业状态
        requestCurrentTime();
        super.onResume();
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
                getHomeworkDetail();
            }

            @Override
            public void exception(Exception exception) {
                currentTime = System.currentTimeMillis();
                getHomeworkDetail();
            }

        });
    }

    /**
     * 获取作业详情
     */
    private void getHomeworkDetail(){
        String module = Constants.XK_TASK_INFO ;
        Map<String,Object> map = new HashMap<String,Object>();
        map.put("stu_id", App.getsStudentId());
        map.put("task_id", mTaskid);
        PhpApiUtil.sendData(module, map, new TmingCacheHttp.RequestWithCacheCallBackV2<TaskDetailInfo>() {

            @Override
            public TaskDetailInfo parseData(String data) throws Exception {
                JSONObject jsonObject = new JSONObject(data);
                TaskDetailInfo taskDetailInfo = new TaskDetailInfo();
                taskDetailInfo.initWithJsonObject(jsonObject);
                return taskDetailInfo;
            }

            @Override
            public void cacheDataRespone(TaskDetailInfo data) {
                loadHomeworkDetail(data);
            }

            @Override
            public void requestNewDataRespone(TaskDetailInfo cacheRespones, TaskDetailInfo newRespones) {
                loadHomeworkDetail(newRespones);
            }

            @Override
            public void exception(Exception exception) {
                loading_layout.setVisibility(View.GONE);
                Toast.makeText(ShapeExamDetailActivity.this, getResources().getString(R.string.net_error), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFinishRequest() {

            }
        });

    }

    /**
     * 加载作业详情数据
     * @param homework
     */
    private void loadHomeworkDetail(TaskDetailInfo homework){
        loading_layout.setVisibility(View.GONE);

        mStatus = homework.status;
        homework_detail_all_tv.setText(""+homework.subjectNum);

        if (homework.allowTimes != -1) {
            homework_detail_times_tv.setText(""+homework.times + "/" + homework.allowTimes );
        } else {
            homework_detail_times_tv.setText("不限");
        }
        SimpleDateFormat spf = new SimpleDateFormat("yyyy年MM月dd日 HH：mm");
        String finishTime = spf.format(new Date(homework.finishTime * 1000));
        long timeLag = getTimeLag(homework.finishTime * 1000);

        if(timeLag >= 0 && timeLag <= 259200000) {    //距离当前日期≤3天的，标为橙色
            deadline_tv.setTextColor(getResources().getColor(R.color.orange));
        } else if (timeLag > 259200000){
            deadline_tv.setTextColor(getResources().getColor(R.color.title_green));
        } else {
            deadline_tv.setTextColor(getResources().getColor(R.color.out_of_date_txt_bg));
        }
        deadline_tv.setText(getResources().getString(R.string.deadline) + finishTime);
        int maxScore = 0;
        if(homework.holderList.size() > 0 && homework.holderList != null){

            homework_detail_type_ll.removeAllViews();
            for(int i = 0;i < homework.holderList.size();i ++){
                String num = "";
                holderList.clear();
                holderList.addAll(homework.holderList);

                TaskDetailInfo.SubjectHolder holder = homework.holderList.get(i);
                View view = LayoutInflater.from(this).inflate(R.layout.homework_type_item,null) ;
                TextView proTypeTv = (TextView) view.findViewById(R.id.pro_type);

                if(i == 0){
                    num = "一、";
                } else if(i == 1){
                    num = "二、";
                } else if(i == 2){
                    num = "三、";
                } else if(i == 3){
                    num = "四、";
                } else if(i == 4){
                    num = "五、";
                } else if(i == 5){
                    num = "六、";
                }
                proTypeTv.setText(num + holder.subjectName + "(" + holder.subjectNums + "题，总计" + holder.subjectValue + "分)");
                homework_detail_type_ll.addView(view);
                //计算总分
                maxScore += Integer.valueOf(holder.subjectValue);
            }
        }
        highestScoreRp.setMax(maxScore == 0 ? 150 : maxScore);
        setTitle(homework);
        setData(homework);

    }
    private void setTitle(TaskDetailInfo data) {
        if (data.status == 1) {
            commonheaderCenterTv.setText("正在做");
            setBtnEnabled(false);
        } else if (data.status == 2) {
            commonheaderCenterTv.setText("已提交");
            setBtnEnabled(false);
        } else if (data.status == 3) {
            commonheaderCenterTv.setText("正在评阅");
            setBtnEnabled(false);
        } else if (data.status == 4) {
            commonheaderCenterTv.setText("已退回");
            setBtnEnabled(data.canStart == 1);
        } else if (data.status == 5) {
            commonheaderCenterTv.setText("已评阅");
            setBtnEnabled(data.canStart == 1);
        } else if (data.status == 6) {
            commonheaderCenterTv.setText("已过期");
            setBtnEnabled(false);
        } else if (data.status == 0) {
            commonheaderCenterTv.setText("");
            setBtnEnabled(data.canStart == 1);
        }

    }

    /**
     * <p>设置数据</P>
     * @param task
     */
    private void setData(TaskDetailInfo task) {
        if (task.score >= 60) {
            highestScoreTv.setTextColor(getResources().getColor(R.color.title_green));
            highestScoreRp.setCricleProgressColor(getResources().getColor(R.color.title_green));
        } else {
            highestScoreTv.setTextColor(getResources().getColor(R.color.red_txt));
            highestScoreRp.setCricleProgressColor(getResources().getColor(R.color.red_txt));
        }

        highestScoreTv.setText("" + task.score);
        highestScoreRp.setProgress(task.score);
        highestScoreRp.postInvalidate();

    }

    /**
     * 设置按钮不可用
     */
    private void setBtnEnabled (boolean isEnabled){
        start_test_btn.setEnabled(isEnabled);
        if (!isEnabled) {
            bottomBtnLayout.setVisibility(View.GONE);
        } else {
            bottomBtnLayout.setVisibility(View.VISIBLE);
        }
    }


    @Override
    public void addListener() {
        start_test_btn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {//这里进去都是从新开始一个新的答题记录
                XKUIHelper.showDoingTaskActivity(ShapeExamDetailActivity.this,mTaskName,null,mTaskid,0);
            }
        });
        homework_detail_all_rl.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
				/*UIHelper.showDoingHomeworkActivity(HomeworkDetailActivity.this, mTaskid, true);*///屏蔽
            }
        });
        findViewById(R.id.common_head_right_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                XKUIHelper.showAnswerList(ShapeExamDetailActivity.this, mTaskid, mTaskName);
            }
        });
        commonheader_left_iv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    /**
     * 获取作业时间节点与当前时间的时间差（服务器时间）
     * @return
     */
    private long getTimeLag(long finishTime) {
        long timeLag = 0;  //时间差（毫秒）
        timeLag = finishTime - currentTime ; //接口中的时间单位为秒
        return timeLag;
    }
}
