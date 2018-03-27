package com.nd.shapedexamproj.activity.shapedexam;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.*;

import com.nd.shapedexamproj.App;
import com.nd.shapedexamproj.R;
import com.nd.shapedexamproj.activity.BaseActivity;
import com.nd.shapedexamproj.model.QuestionHolder;
import com.nd.shapedexamproj.model.xkhomework.XKHandInWarmupTask;
import com.nd.shapedexamproj.model.xkhomework.XKRestartWarmupTask;
import com.nd.shapedexamproj.model.xkhomework.XKWarmup;
import com.nd.shapedexamproj.util.Constants;
import com.nd.shapedexamproj.util.PhpApiUtil;
import com.nd.shapedexamproj.util.SimpleAlertDialog;
import com.nd.shapedexamproj.util.XKUIHelper;
import com.nd.shapedexamproj.view.CircularImage;
import com.nd.shapedexamproj.view.RoundProgressBar;
import com.tming.common.cache.net.TmingCacheHttp;
import com.tming.common.util.PhoneUtil;
import com.tming.openuniversity.model.exam.WarmupDetail;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 形考热身考试
 * Created by zll on 2015/3/19.
 */
public class WarmupExamActivity extends BaseActivity implements View.OnClickListener{

    private static final String TAG = "WarmupExamActivity";

    private ImageView commonheaderLeftIv;
    private TextView commonheader_title_tv;

    private LinearLayout mloadingLayout;

    private LinearLayout homework_detail_type_ll,twoBtnLL;
    private RelativeLayout homework_detail_all_rl,homework_detail_undone_rl, homework_detail_times_rl;
    private TextView homework_detail_all_tv, homework_detail_undone_tv1,homework_detail_undone_tv2, homework_detail_times_tv;
    //private TextView deadline_tv; //截止时间

    private RelativeLayout mCurrScoreImgRL;  //中间圆形得分布局
    private CircularImage objectScoreImg,SubjectScoreImg,totalScoreImg ;
    private RoundProgressBar objectScoreRp,subjectScoreRp,totalScoreRp ;
    private TextView mCurrentScoreTv,objectScoreTv,subjectScoreTv,totalScoreTv;

    private Button mStartTestBtn,warmupLeftBtn,warmupRightBtn;

    private boolean isCanTest = true ;		//是否可以答题
    private int pageNum = 1,pageSize = 100 ;
    private int total_num ;			//总题数
    /**
     * 0是未提交，1是提交。
     */
    private String warmupName ;
    private String warmupId;
    private String kmId ;//课程id
    private int mStatus;//1=第一次做, 2=已经提交, 3=还没有提交
    private List<QuestionHolder> question_type ;	//题型列表
    private TmingCacheHttp cacheHttp ;

    @Override
    public int initResource() {
        return R.layout.xk_answer_record_detail;
    }

    @Override
    public void initComponent() {
        kmId = getIntent().getStringExtra("kmId");
        warmupName = getIntent().getStringExtra("warmupName");

        question_type = new ArrayList<QuestionHolder>();
        cacheHttp = TmingCacheHttp.getInstance();

        findViewById(R.id.common_head_right_btn).setVisibility(View.GONE);

        commonheaderLeftIv = (ImageView) findViewById(R.id.commonheader_left_iv);
        commonheader_title_tv = (TextView) findViewById(R.id.commonheader_title_tv);
        commonheader_title_tv.setText(warmupName == null ? "" : warmupName);
        mloadingLayout = (LinearLayout) findViewById(R.id.loading_layout);

        homework_detail_all_tv = (TextView) findViewById(R.id.homework_detail_all_tv);
        homework_detail_undone_tv1 = (TextView) findViewById(R.id.homework_detail_undone_tv1);
        homework_detail_undone_tv2 = (TextView) findViewById(R.id.homework_detail_undone_tv2);
        homework_detail_times_tv = (TextView) findViewById(R.id.homework_detail_times_tv);

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
        twoBtnLL = (LinearLayout) findViewById(R.id.homework_detail_two_btn_ll);

        //开始做题
        mStartTestBtn = (Button) findViewById(R.id.start_test_btn);
        warmupLeftBtn = (Button) findViewById(R.id.warmup_left_btn);
        warmupRightBtn = (Button) findViewById(R.id.warmup_right_btn);
    }

    @Override
    public void initData() {

    }

    @Override
    public void addListener() {
        mStartTestBtn.setOnClickListener(this);
        warmupLeftBtn.setOnClickListener(this);
        warmupRightBtn.setOnClickListener(this);
        commonheaderLeftIv.setOnClickListener(this);
    }

    @Override
    protected void onResume() {
        //根据数据库判断作业状态
        getWarmupDetail();
        super.onResume();
    }

    /**
     * 获取作业详情
     */
    private void getWarmupDetail(){
        String module = Constants.XK_WARM_UP_INFO ;
        Map<String,Object> map = new HashMap<String,Object>();
        map.put("stu_id", App.getsStudentId());
        map.put("km_id", kmId);
        PhpApiUtil.sendData(module, map, new TmingCacheHttp.RequestWithCacheCallBackV2<WarmupDetail>() {

            @Override
            public WarmupDetail parseData(String data) throws Exception {
                JSONObject jsonObject = new JSONObject(data);
                WarmupDetail warmupDetail = new WarmupDetail();
                warmupDetail.initWithJsonObject(jsonObject);
                return warmupDetail;
            }

            @Override
            public void cacheDataRespone(WarmupDetail data) {
                loadHomeworkDetail(data);
            }

            @Override
            public void requestNewDataRespone(WarmupDetail cacheRespones, WarmupDetail newRespones) {
                loadHomeworkDetail(newRespones);
            }

            @Override
            public void exception(Exception exception) {
                exception.printStackTrace();
                mloadingLayout.setVisibility(View.GONE);
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
    private void loadHomeworkDetail(WarmupDetail homework){
        mloadingLayout.setVisibility(View.GONE);

        warmupId = homework.warmupId;
        mStatus = homework.status;
        homework_detail_all_tv.setText(""+homework.totalNum);

        if (homework.allowTimes != -1) {
            homework_detail_times_tv.setText(""+homework.times + "/" + homework.allowTimes );
        } else {
            homework_detail_times_tv.setText("不限");
        }
        int subTotalScore = 0, objTotalScore = 0;
        if(homework.subjectsList.size() > 0 && homework.subjectsList != null){

            homework_detail_type_ll.removeAllViews();
            for(int i = 0;i < homework.subjectsList.size();i ++) {
                String num = "";

                WarmupDetail.SubjectHolder holder = homework.subjectsList.get(i);
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
        setButton(homework);
        setData(homework);

    }

    private void setButton(WarmupDetail data) {
        if (data.status == 1) {
            twoBtnLL.setVisibility(View.GONE);
            mStartTestBtn.setText(getResources().getString(R.string.xk_start_test));
            setBtnEnabled(true);
        } else if (data.status == 2) {
            twoBtnLL.setVisibility(View.VISIBLE);
            warmupLeftBtn.setText(getResources().getString(R.string.xk_check_detail));
            if (data.times < data.allowTimes) {
                warmupRightBtn.setText(getResources().getString(R.string.xk_redo_test));
                warmupRightBtn.setVisibility(View.VISIBLE);
            } else {
                warmupRightBtn.setVisibility(View.GONE);
            }
            setBtnEnabled(false);
        } else if (data.status == 3) {
            twoBtnLL.setVisibility(View.VISIBLE);
            warmupLeftBtn.setText(getResources().getString(R.string.xk_commit_test));
            warmupRightBtn.setText(getResources().getString(R.string.xk_continue_test));
            setBtnEnabled(false);
        }
    }

    /**
     * <p>设置数据</P>
     * @param homework
     */
    private void setData(WarmupDetail homework) {
        String strObjScore = homework.objectiveScore == -1 ? "?" : String.valueOf(homework.objectiveScore);
        float intObjScore = homework.objectiveScore == -1 ? 0 : homework.objectiveScore;

        String strSbjScore = homework.subjectiveScore == -1 ? "?" : String.valueOf(homework.subjectiveScore);
        float intSbjScore = homework.subjectiveScore == -1 ? 0 : homework.subjectiveScore;

        String strTotalScore = homework.totalScore == -1 ? "?" : String.valueOf(homework.totalScore);
        float intTotalScore = homework.totalScore == -1 ? 0 : homework.totalScore;

        if (homework.status == 1) {//已做未提交
            mCurrScoreImgRL.setVisibility(View.GONE);
        } else {
            mCurrScoreImgRL.setVisibility(View.VISIBLE);
        }

        if (homework.status == 3) {
            XKWarmup warmup = XKWarmup.readHomework(warmupId);
            if (warmup != null) {
                warmup.readUserAnswer();
                homework_detail_undone_tv2.setText("" + warmup.getSubjectSizeOfNotDone());
            }
        } else {
            homework_detail_undone_tv2.setText("" + homework.undoNum);
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

        subjectScoreTv.setText(strSbjScore);
        totalScoreTv.setText(strTotalScore);

        subjectScoreRp.setProgress((int) intSbjScore);
        totalScoreRp.setProgress((int) intTotalScore);
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

    public void showHandInHomeworkDialog() {
        // 组装作业
        XKWarmup homework = XKWarmup.readHomework(warmupId);
        if (homework == null) {
            Toast.makeText(WarmupExamActivity.this,"暂无答题记录",Toast.LENGTH_SHORT).show();
            return;
        } else {
            homework.readUserAnswer();
        }
        SimpleAlertDialog dialog = new SimpleAlertDialog(this) {
            @Override
            public void onPositiveButtonClick() {
                handInHomeworkAction(true);
            }
        };
        dialog.setContent(homework.getDoingProfileString(true));
        dialog.setTitle("确定交卷吗?");
        dialog.setPositiveButton("交卷");
        dialog.setNegativeButton("暂不");
        dialog.show();
    }

    public void handInHomeworkAction(boolean isHandIn) {
        if (PhoneUtil.checkNetworkEnable() == PhoneUtil.NETSTATE_DISABLE) {
            finish();
            return;
        }
        XKHandInWarmupTask task = new XKHandInWarmupTask(WarmupExamActivity.this, warmupId, isHandIn) {

            @Override
            protected void onPostExecute(Boolean aBoolean) {
                super.onPostExecute(aBoolean);
                if (aBoolean) {
                    finish();
                }
            }

        };
        task.execute();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.start_test_btn:
                //根据服务端状态判断是否可编辑
                XKUIHelper.showDoingWarmupActicity(this, warmupName, warmupId, mStatus,true);
                break;
            case R.id.warmup_left_btn:
                if (mStatus == 2) {//已经提交--查看详情
                    XKUIHelper.showDoingWarmupActicity(this, warmupName, warmupId, mStatus,false);
                } else if (mStatus == 3) {//还未提交--提交考试
                    showHandInHomeworkDialog();
                }
                break;
            case R.id.warmup_right_btn:
                if (mStatus == 3) {
                    XKUIHelper.showDoingWarmupActicity(this, warmupName, warmupId, mStatus, true);
                } else if (mStatus == 2) {
                    restartWarmuoDialog();
                }
                break;
            case R.id.commonheader_left_iv:
                finish();
                break;
            default:
                break;
        }
    }

    private void restartWarmuoDialog() {
        SimpleAlertDialog dialog = new SimpleAlertDialog(this) {
            @Override
            public void onPositiveButtonClick() {
                restartWarmupTest();
            }
        };
        dialog.setTitle("确定重新考试?");
        dialog.setPositiveButton("确定");
        dialog.setNegativeButton("暂不");
        dialog.show();
    }

    private void restartWarmupTest() {
        XKRestartWarmupTask restartWarmupTask = new XKRestartWarmupTask(this,warmupId){
            @Override
            protected void onPreExecute() {
                mloadingLayout.setVisibility(View.VISIBLE);
            }

            @Override
            protected void onPostExecute(Boolean aBoolean) {
                mloadingLayout.setVisibility(View.GONE);
                if (aBoolean) {
                    //TODO 刪除用戶答題記錄
                    boolean hasDoneHomework = XKWarmup.checkUserHasDoHomework(warmupId);
                    if (hasDoneHomework) {
                        XKWarmup.clearUserAnswer(warmupId);
                    }
                    XKUIHelper.showDoingWarmupActicity(WarmupExamActivity.this, warmupName, warmupId, mStatus, true);
                } else {
                    App.getAppHandler().post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(WarmupExamActivity.this,"暂时无法重做",Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        };
        restartWarmupTask.execute();
    }

}
