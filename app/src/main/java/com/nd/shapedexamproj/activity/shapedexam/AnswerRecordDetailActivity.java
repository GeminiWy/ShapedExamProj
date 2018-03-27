package com.nd.shapedexamproj.activity.shapedexam;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.*;

import com.nd.shapedexamproj.App;
import com.nd.shapedexamproj.R;
import com.nd.shapedexamproj.activity.BaseActivity;
import com.nd.shapedexamproj.model.QuestionHolder;
import com.nd.shapedexamproj.util.Constants;
import com.nd.shapedexamproj.util.PhpApiUtil;
import com.nd.shapedexamproj.util.XKUIHelper;
import com.nd.shapedexamproj.view.CircularImage;
import com.nd.shapedexamproj.view.RoundProgressBar;
import com.tming.common.cache.net.TmingCacheHttp;
import com.tming.common.net.TmingHttp;
import com.tming.openuniversity.model.exam.AnswerHistoryDetail;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 答题记录详情页
 * Created by zll on 2015/3/3.
 */
public class AnswerRecordDetailActivity extends BaseActivity implements View.OnClickListener{

    private static final String TAG = "AnswerRecordDetailActivity";

    private ImageView commonheader_left_iv;
    private TextView commonheader_title_tv,commonheaderCenterTv;

    private LinearLayout loading_layout;

    private LinearLayout homework_detail_type_ll;
    private RelativeLayout homework_detail_all_rl,homework_detail_undone_rl, homework_detail_times_rl;
    private TextView homework_detail_all_tv, homework_detail_undone_tv1,homework_detail_undone_tv2, homework_detail_times_tv;
    private TextView deadline_tv; //截止时间

    private RelativeLayout mCurrScoreImgRL;  //中间圆形得分布局
    private CircularImage objectScoreImg,SubjectScoreImg,totalScoreImg ;
    private RoundProgressBar objectScoreRp,subjectScoreRp,totalScoreRp ;
    private TextView mCurrentScoreTv,objectScoreTv,subjectScoreTv,totalScoreTv;

    private Button mStartTestBtn;

    /*private boolean isUndoneRLClickable ;*/	//是否可以跳到错题重做
    private boolean isCanTest = true ;		//是否可以答题
    private int pageNum = 1,pageSize = 100 ;
    private int total_num ;			//总题数
    //0 未答题；1做了部分但未提交；2提交了但不及格；3提交了并及格；4提交了并满分；5 提交了未批改
    /**
     * 0是未提交，1是提交。
     */
    private long currentTime = 0;
    private String mAnswerRecordName = "";
    private String mAnswerRecordId ;
    private String mTaskId ;
    private int mStatus;
    private List<QuestionHolder> question_type ;	//题型列表
    private TmingCacheHttp cacheHttp ;

    private String doWrong ;
    private String notDone ;
    @Override
    public int initResource() {
        return R.layout.xk_answer_record_detail;
    }

    @Override
    public void initComponent() {
        mTaskId = getIntent().getStringExtra("taskId");
        mAnswerRecordId = getIntent().getStringExtra("answerRecordId");
        mAnswerRecordName = getIntent().getStringExtra("answerRecordName");

        doWrong = getResources().getString(R.string.do_wrong);
        notDone = getResources().getString(R.string.not_done);

        question_type = new ArrayList<QuestionHolder>();
        cacheHttp = TmingCacheHttp.getInstance();

        findViewById(R.id.common_head_right_btn).setVisibility(View.GONE);

        commonheader_left_iv = (ImageView) findViewById(R.id.commonheader_left_iv);
        commonheader_title_tv = (TextView) findViewById(R.id.commonheader_title_tv);
        commonheader_title_tv.setText(mAnswerRecordName == null ? "" : mAnswerRecordName);
        commonheaderCenterTv = (TextView) findViewById(R.id.commonheader_center_tv);
        loading_layout = (LinearLayout) findViewById(R.id.loading_layout);

        homework_detail_all_tv = (TextView) findViewById(R.id.homework_detail_all_tv);
        homework_detail_undone_tv1 = (TextView) findViewById(R.id.homework_detail_undone_tv1);
        homework_detail_undone_tv2 = (TextView) findViewById(R.id.homework_detail_undone_tv2);
        homework_detail_times_tv = (TextView) findViewById(R.id.homework_detail_times_tv);
        //截止时间
        deadline_tv = (TextView) findViewById(R.id.deadline_tv);

        homework_detail_all_rl = (RelativeLayout) findViewById(R.id.homework_detail_all_rl);
        homework_detail_undone_rl = (RelativeLayout) findViewById(R.id.homework_detail_undone_rl);
        //-------------------中间得分布局------------------------
        mCurrScoreImgRL = (RelativeLayout) findViewById(R.id.homework_score_rl);
        //圆形图片
        objectScoreImg = (CircularImage)findViewById(R.id.homework_score_objective_img);
        objectScoreImg.setImageDrawable(getResources().getDrawable(R.drawable.white));

        SubjectScoreImg = (CircularImage)findViewById(R.id.homework_score_sbjective_img);
        SubjectScoreImg.setImageDrawable(getResources().getDrawable(R.drawable.white));

        totalScoreImg = (CircularImage)findViewById(R.id.homework_score_total_img);
        totalScoreImg.setImageDrawable(getResources().getDrawable(R.drawable.white));

        //圆形进度条
        objectScoreRp = (RoundProgressBar) findViewById(R.id.homework_score_objective_rp);
        objectScoreRp.setProgress(0);

        subjectScoreRp = (RoundProgressBar) findViewById(R.id.homework_score_sbjective_rp);
        subjectScoreRp.setProgress(0);

        totalScoreRp = (RoundProgressBar) findViewById(R.id.homework_score_total_rp);
        totalScoreRp.setProgress(0);

        //得分
        mCurrentScoreTv = (TextView) findViewById(R.id.current_score_tv);
        objectScoreTv = (TextView) findViewById(R.id.homework_score_objective_tv);
        subjectScoreTv = (TextView) findViewById(R.id.homework_score_sbjective_tv);
        totalScoreTv = (TextView) findViewById(R.id.homework_score_total_tv);
        //---------------------中间得分布局---------------------
        //题型
        homework_detail_type_ll = (LinearLayout) findViewById(R.id.homework_detail_type_ll);

        //开始做题
        mStartTestBtn = (Button) findViewById(R.id.start_test_btn);

    }

    @Override
    public void initData() {

    }

    @Override
    public void addListener() {
        commonheader_left_iv.setOnClickListener(this);
        mStartTestBtn.setOnClickListener(this);
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
        String module = Constants.XK_ANSWER_INFO ;
        Map<String,Object> map = new HashMap<String,Object>();
        map.put("stu_id", App.getsStudentId());
        map.put("answer_id", mAnswerRecordId);
        PhpApiUtil.sendData(module, map, new TmingCacheHttp.RequestWithCacheCallBackV2<AnswerHistoryDetail>() {

            @Override
            public AnswerHistoryDetail parseData(String data) throws Exception {
                JSONObject jsonObject = new JSONObject(data);
                AnswerHistoryDetail taskDetailInfo = new AnswerHistoryDetail();
                taskDetailInfo.initWithJsonObject(jsonObject);
                return taskDetailInfo;
            }

            @Override
            public void cacheDataRespone(AnswerHistoryDetail data) {
                loadHomeworkDetail(data);
            }

            @Override
            public void requestNewDataRespone(AnswerHistoryDetail cacheRespones, AnswerHistoryDetail newRespones) {
                loadHomeworkDetail(newRespones);
            }

            @Override
            public void exception(Exception exception) {
                exception.printStackTrace();
                loading_layout.setVisibility(View.GONE);
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
    private void loadHomeworkDetail(AnswerHistoryDetail homework){
        loading_layout.setVisibility(View.GONE);

        mStatus = homework.status;
        homework_detail_all_tv.setText(""+homework.totalNum);

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
        int subTotalScore = 0, objTotalScore = 0;
        if(homework.subjectsList.size() > 0 && homework.subjectsList != null){

            homework_detail_type_ll.removeAllViews();
            for(int i = 0;i < homework.subjectsList.size();i ++) {
                String num = "";

                AnswerHistoryDetail.SubjectHolder holder = homework.subjectsList.get(i);
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
                //计算主观和客观题分数
                if (holder.subjectName.equals("单项选择题")) {
                    objTotalScore += Integer.valueOf(holder.subjectValue);
                } else if (holder.subjectName.equals("多项选择题")) {
                    objTotalScore += Integer.valueOf(holder.subjectValue);
                } else if (holder.subjectName.equals("判断题")) {
                    objTotalScore += Integer.valueOf(holder.subjectValue);
                } else if (holder.subjectName.equals("填空题")) {
                    subTotalScore += Integer.valueOf(holder.subjectValue);
                } else if (holder.subjectName.equals("简答题")) {
                    subTotalScore += Integer.valueOf(holder.subjectValue);
                }
            }
        }
        objectScoreRp.setMax(objTotalScore == 0 ? 150 : objTotalScore);
        subjectScoreRp.setMax(subTotalScore == 0 ? 150 : subTotalScore);
        totalScoreRp.setMax((objTotalScore + subTotalScore) == 0 ? 150 : objTotalScore + subTotalScore);
        setTitle(homework);
        setData(homework);

    }

    private void setTitle(AnswerHistoryDetail data) {
        if (data.status == 1) {
            commonheaderCenterTv.setText("正在做");
            mStartTestBtn.setText("继续答题");
        } else if (data.status == 2) {
            commonheaderCenterTv.setText("已提交");
            mStartTestBtn.setText("查看详情");
        } else if (data.status == 3) {
            commonheaderCenterTv.setText("正在评阅");
            mStartTestBtn.setText("查看详情");
        } else if (data.status == 4) {
            commonheaderCenterTv.setText("已退回");
            mStartTestBtn.setText("查看详情");
        } else if (data.status == 5) {
            commonheaderCenterTv.setText("已评阅");
            mStartTestBtn.setText("查看详情");
        } else if (data.status == 6) {
            commonheaderCenterTv.setText("已过期");
            mStartTestBtn.setText("查看详情");
        }
    }

    /**
     * <p>设置数据</P>
     * @param homework
     */
    private void setData(AnswerHistoryDetail homework) {
        String strObjScore = homework.objectiveScore == -1 ? "?" : String.valueOf(homework.objectiveScore);
        float intObjScore = homework.objectiveScore == -1 ? 0 : homework.objectiveScore;

        if (homework.status == 1) {//继续答题
            mCurrScoreImgRL.setVisibility(View.GONE);
            homework_detail_undone_tv1.setText(getResources().getString(R.string.not_done));
            homework_detail_undone_tv2.setText("" + homework.undoNum);
            homework_detail_undone_rl.setBackgroundColor(getResources().getColor(R.color.undone_test));
        } else {
            homework_detail_undone_tv1.setText(getResources().getString(R.string.do_wrong));
            homework_detail_undone_tv2.setText("" + homework.errorNum);
            homework_detail_undone_rl.setBackgroundColor(getResources().getColor(R.color.wrong_answer));
            mCurrScoreImgRL.setVisibility(View.VISIBLE);
        }

        if (homework.totalScore >= 0 ) {
            if (homework.totalScore >= 60 ) {
                totalScoreTv.setTextColor(getResources().getColor(R.color.title_green));
                totalScoreRp.setCricleProgressColor(getResources().getColor(R.color.title_green));
            } else {
                totalScoreTv.setTextColor(getResources().getColor(R.color.red_txt));
                totalScoreRp.setCricleProgressColor(getResources().getColor(R.color.red_txt));
            }
        }

        if (intObjScore > 0 && intObjScore >= 60) {
            objectScoreTv.setTextColor(getResources().getColor(R.color.title_green));
            objectScoreRp.setCricleProgressColor(getResources().getColor(R.color.title_green));
        } else {
            objectScoreTv.setTextColor(getResources().getColor(R.color.red_txt));
            objectScoreRp.setCricleProgressColor(getResources().getColor(R.color.red_txt));
        }

        objectScoreTv.setText(strObjScore);
        objectScoreRp.setProgress((int) intObjScore);

        subjectScoreTv.setText(String.valueOf(homework.subjectiveScore));
        totalScoreTv.setText(String.valueOf(homework.totalScore));

        subjectScoreRp.setProgress((int) homework.subjectiveScore);
        totalScoreRp.setProgress((int) homework.totalScore);
        if (homework.status == 6) {//已过期
            setBtnEnabled(false);
        } else {
            setBtnEnabled(true);
        }

    }

    /**
     * 设置按钮不可用
     */
    private void setBtnEnabled (boolean isEnabled){
        mStartTestBtn.setEnabled(isEnabled);
        if (!isEnabled) {
            mStartTestBtn.setBackgroundColor(getResources().getColor(R.color.border_color));
            mStartTestBtn.setTextColor(getResources().getColor(R.color.light_black));
        } else {
            mStartTestBtn.setBackgroundDrawable(getResources().getDrawable(R.drawable.common_btn_selector));
            mStartTestBtn.setTextColor(getResources().getColor(R.color.white));
        }
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

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.start_test_btn:
                //根据服务端状态判断是否可编辑
                XKUIHelper.showDoingTaskActivity(this,mAnswerRecordName,mAnswerRecordId,mTaskId,mStatus);
                break;
            case R.id.commonheader_left_iv:
                finish();
                break;
            default:
                break;
        }

    }
}
