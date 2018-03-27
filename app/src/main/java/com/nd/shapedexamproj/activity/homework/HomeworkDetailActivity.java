package com.nd.shapedexamproj.activity.homework;

import android.content.Intent;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.*;

import com.nd.shapedexamproj.App;
import com.nd.shapedexamproj.R;
import com.nd.shapedexamproj.activity.BaseActivity;
import com.nd.shapedexamproj.model.Homework;
import com.nd.shapedexamproj.model.QuestionHolder;
import com.nd.shapedexamproj.model.homework.HandInHomeworkTask;
import com.nd.shapedexamproj.util.Constants;
import com.nd.shapedexamproj.util.SimpleAlertDialog;
import com.nd.shapedexamproj.util.UIHelper;
import com.nd.shapedexamproj.view.CircularImage;
import com.nd.shapedexamproj.view.RoundProgressBar;
import com.tming.common.cache.net.TmingCacheHttp;
import com.tming.common.cache.net.TmingCacheHttp.RequestWithCacheCallBackV2;
import com.tming.common.net.TmingHttp;
import com.tming.common.util.Log;
import com.tming.common.util.PhoneUtil;
import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.*;
/**
 * 作业详情页
 * @author zll
 * create in 2014-3-26
 */
public class HomeworkDetailActivity extends BaseActivity {
	private static final String TAG = "HomeworkDetailActivity";
	
	private ImageView commonheader_left_iv;
	private TextView commonheader_title_tv;
	
	private LinearLayout loading_layout;
	
	private LinearLayout homework_detail_two_btn_ll ,homework_detail_type_ll;
	private RelativeLayout homework_detail_all_rl,homework_detail_undone_rl, homework_detail_times_rl;
	private TextView homework_detail_all_tv, homework_detail_undone_tv1,homework_detail_undone_tv2, homework_detail_times_tv;
	private TextView deadline_tv; //截止时间
	
	private RelativeLayout mCurrScoreImgRL,mHighestScoreImgRL;  //中间圆形得分布局
	private CircularImage objectScoreImg,SubjectScoreImg,totalScoreImg,highestScoreImg ;
	private RoundProgressBar objectScoreRp,subjectScoreRp,totalScoreRp,highestScoreRp ;
	private TextView mCurrentScoreTv,objectScoreTv,subjectScoreTv,totalScoreTv,highestScoreTv;
	
	private Button commit_test_btn, continue_test_btn, start_test_btn;
	
	/*private boolean isUndoneRLClickable ;*/	//是否可以跳到错题重做
	private boolean isCanTest = true ;		//是否可以答题
	private int pageNum = 1,pageSize = 100 ;
	private int total_num ;			//总题数
	private float mHighestScore;//最高分，-1表示未做
	//0 未答题；1做了部分但未提交；2提交了但不及格；3提交了并及格；4提交了并满分；5 提交了未批改
	/**
	 * 0是未提交，1是提交。
	 */
	private int homework_state ;
	private long currentTime = 0;
	private String homework_name = "";
	private String mHomeworkId = "-1";
	private List<QuestionHolder> question_type ;	//题型列表
	private TmingCacheHttp cacheHttp ;

	private String doWrong ;
	private String notDone ;
	
	@Override
	public int initResource() {
		return R.layout.homework_detail;
	}
	@Override
	public void initComponent() {
		mHomeworkId = getIntent().getStringExtra("homework_id");
		homework_name = getIntent().getStringExtra("homework_name");
		mHighestScore = getIntent().getFloatExtra("highestScore", 0);
		
		doWrong = getResources().getString(R.string.do_wrong);
		notDone = getResources().getString(R.string.not_done);
		
		question_type = new ArrayList<QuestionHolder>();
		cacheHttp = TmingCacheHttp.getInstance();

		findViewById(R.id.common_head_right_btn).setVisibility(View.GONE);
		
		commonheader_left_iv = (ImageView) findViewById(R.id.commonheader_left_iv);
		commonheader_title_tv = (TextView) findViewById(R.id.commonheader_title_tv);
		commonheader_title_tv.setText(homework_name == null ? "" : homework_name);
		
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
		mHighestScoreImgRL = (RelativeLayout) findViewById(R.id.homework_score_highest_rl);
		mCurrScoreImgRL = (RelativeLayout) findViewById(R.id.homework_score_rl);
		//圆形图片
		objectScoreImg = (CircularImage)findViewById(R.id.homework_score_objective_img);
		objectScoreImg.setImageDrawable(getResources().getDrawable(R.drawable.white));
		
		SubjectScoreImg = (CircularImage)findViewById(R.id.homework_score_sbjective_img);
		SubjectScoreImg.setImageDrawable(getResources().getDrawable(R.drawable.white));
		
		totalScoreImg = (CircularImage)findViewById(R.id.homework_score_total_img);
		totalScoreImg.setImageDrawable(getResources().getDrawable(R.drawable.white));
		
		highestScoreImg = (CircularImage) findViewById(R.id.homework_score_highest_img);
		highestScoreImg.setImageDrawable(getResources().getDrawable(R.drawable.white));
		//圆形进度条
		objectScoreRp = (RoundProgressBar) findViewById(R.id.homework_score_objective_rp);
		objectScoreRp.setProgress(0);
		objectScoreRp.postInvalidate();
		
		subjectScoreRp = (RoundProgressBar) findViewById(R.id.homework_score_sbjective_rp);
		subjectScoreRp.setProgress(0);
		subjectScoreRp.postInvalidate();
        
		totalScoreRp = (RoundProgressBar) findViewById(R.id.homework_score_total_rp);
		totalScoreRp.setProgress(0);
		totalScoreRp.postInvalidate();
		
		highestScoreRp = (RoundProgressBar) findViewById(R.id.homework_score_highest_rp);
		highestScoreRp.setProgress(0);
		highestScoreRp.postInvalidate();
		//得分
		mCurrentScoreTv = (TextView) findViewById(R.id.current_score_tv);
		objectScoreTv = (TextView) findViewById(R.id.homework_score_objective_tv);
		subjectScoreTv = (TextView) findViewById(R.id.homework_score_sbjective_tv);
		totalScoreTv = (TextView) findViewById(R.id.homework_score_total_tv);
		highestScoreTv = (TextView) findViewById(R.id.homework_score_highest_tv);
		//---------------------中间得分布局---------------------
		//题型
		homework_detail_type_ll = (LinearLayout) findViewById(R.id.homework_detail_type_ll);
		
		//开始做题
		start_test_btn = (Button) findViewById(R.id.start_test_btn);
		commit_test_btn = (Button) findViewById(R.id.commit_test_btn);
		continue_test_btn = (Button) findViewById(R.id.continue_test_btn);
		//两个按钮的层
		homework_detail_two_btn_ll = (LinearLayout) findViewById(R.id.homework_detail_two_btn_ll);
		
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
		String url = Constants.HOMEWORK_DETAIL ;
		Map<String,Object> map = new HashMap<String,Object>();
		map.put("userid", App.getUserId());
		map.put("workid", mHomeworkId);
		cacheHttp.asyncRequestWithCache(url, map, new RequestWithCacheCallBackV2<Homework>() {

            @Override
            public Homework parseData(String data) throws Exception {
                return parseHomeworkDetail(data);
            }

            @Override
            public void cacheDataRespone(Homework data) {
                loadHomeworkDetail(data);
            }

            @Override
            public void requestNewDataRespone(Homework cacheRespones, Homework newRespones) {
                loadHomeworkDetail(newRespones);
            }

            @Override
            public void exception(Exception exception) {
                loading_layout.setVisibility(View.GONE);
                Toast.makeText(HomeworkDetailActivity.this, getResources().getString(R.string.net_error), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFinishRequest() {
                
            }
        });
		
	}
	
	/**
	 * 获取作业详情
	 */
	private Homework parseHomeworkDetail(String result){
		Homework homework = new Homework ();
//		Map <String,QuestionHolder> question_type = new HashMap<String,QuestionHolder>();
		List <QuestionHolder> question_type = new ArrayList<QuestionHolder>();
		try {
			JSONObject jobj = new JSONObject(result);
			int flag = jobj.getInt("flag");
			if(flag != 1){
				App.dealWithFlag(flag);
				return null;
			} 
			JSONObject list_obj = jobj.getJSONObject("res").getJSONObject("data").getJSONObject("list");
			
			homework.work_state = list_obj.getInt("work_state");
			homework_state = list_obj.getInt("work_state");
			Log.e("homework_state", ""+homework_state);
			homework.total_num = list_obj.getInt("total_num");
			homework.undo_num = list_obj.getInt("nodo_num");
			homework.answer_num = list_obj.getInt("answer_num");
			homework.answer_total = list_obj.getInt("answer_total");
			homework.submit_time = list_obj.getLong("submit_time");
			homework.finish_time = list_obj.getLong("close_time");
			homework.homework_score = list_obj.getInt("work_score");
			homework.user_score = list_obj.getInt("user_score");
			homework.error_num = list_obj.getInt("error_num");
			homework.obj_score = list_obj.getDouble("obj_score");
			homework.sbj_score = list_obj.getDouble("sbj_score");
			homework.is_marked = list_obj.getInt("is_marked");
			
			JSONArray question_arr = list_obj.getJSONArray("question");
			for (int i = 0;i < question_arr.length(); i ++) {
				QuestionHolder holder = new QuestionHolder();
				JSONObject question_obj = question_arr.getJSONObject(i);
				holder.work_num = question_obj.getInt("work_num");
				if (holder.work_num == 0) {
					continue;
				}
				holder.work_type = question_obj.getString("work_type");
				holder.total_score = question_obj.getInt("total_score");
				question_type.add( holder);
			}
			homework.question_type.addAll(question_type);
			
		} catch (Exception e) {
			App.getAppHandler().post(new Runnable(){

				@Override
				public void run() {
					loading_layout.setVisibility(View.GONE);
					Toast.makeText(HomeworkDetailActivity.this, "接口数据异常", Toast.LENGTH_SHORT).show();
				}
				
			});
			
			e.printStackTrace();
		}
		
		return homework ;
	}
	/**
	 * 加载作业详情数据
	 * @param homework
	 */
	private void loadHomeworkDetail(Homework homework){
		loading_layout.setVisibility(View.GONE);
		
		homework_detail_all_tv.setText(""+homework.total_num);
		total_num = homework.total_num ;

		if (homework.answer_total >= 100) {
			homework_detail_times_tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 25);
		} else {
			homework_detail_times_tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 28);
		}

		if (homework.answer_total != -1) {
			homework_detail_times_tv.setText(""+homework.answer_num + "/" + homework.answer_total );
		} else {
			homework_detail_times_tv.setText("不限");
		}
		SimpleDateFormat spf = new SimpleDateFormat("yyyy年MM月dd日 HH：mm");
		String finishTime = spf.format(new Date(homework.finish_time * 1000));
		long timeLag = getTimeLag(homework.finish_time * 1000);

		if(timeLag >= 0 && timeLag <= 259200000) {    //距离当前日期≤3天的，标为橙色
			deadline_tv.setTextColor(getResources().getColor(R.color.orange));
		} else if (timeLag > 259200000){
			deadline_tv.setTextColor(getResources().getColor(R.color.title_green));
		} else {
			deadline_tv.setTextColor(getResources().getColor(R.color.out_of_date_txt_bg));
		}
		deadline_tv.setText(getResources().getString(R.string.deadline) + finishTime);
		if(homework.question_type.size() > 0 && homework.question_type != null){

			homework_detail_type_ll.removeAllViews();
			for(int i = 0;i < homework.question_type.size();i ++){
				String num = "";
				question_type.clear();
				question_type.addAll(homework.question_type);
				
				QuestionHolder holder = homework.question_type.get(i);
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
				
				if(holder.work_type.equals("0")){
					proTypeTv.setText(num + "单项选择题(" + holder.work_num + "题，总计" + holder.total_score + "分)");
				} else if (holder.work_type.equals("1")){
					proTypeTv.setText(num + "多项选择题(" + holder.work_num + "题，总计" + holder.total_score + "分)");
				} else if (holder.work_type.equals("2")){
					proTypeTv.setText(num + "判断题(" + holder.work_num + "题，总计" + holder.total_score + "分)");
				} else if (holder.work_type.equals("4")){
					proTypeTv.setText(num + "填空题(" + holder.work_num + "题，总计" + holder.total_score + "分)");
				} else if (holder.work_type.equals("5")){
					proTypeTv.setText(num + "简答题(" + holder.work_num + "题，总计" + holder.total_score + "分)");
				} else if (holder.work_type.equals("g")){
					proTypeTv.setText(num + "套题(" + holder.work_num + "题，总计" + holder.total_score + "分)");
				}
				
				homework_detail_type_ll.addView(view);
			}
		}
		setData(homework);
		
		if (homework.answer_num == 0) {	//未交过卷（又分为已答过题和未答过题）
		    Log.e(TAG, "===未交过卷===");
			stateNotCommitted(homework);
		
		} else if(homework.answer_num >= 1) {	//至少交过一次卷（又分为已批改和未批改）
		    Log.e(TAG, "===至少交过一次卷===");
			stateHasCommitted(homework);
		}
		
	}
	/**
	 * <p>设置数据</P>
	 * @param homework
	 */
	private void setData(Homework homework) {
		String strObjScore = homework.obj_score == -1 ? "?" : String.valueOf((int)homework.obj_score);
		int intObjScore = homework.obj_score == -1 ? 0 : (int)homework.obj_score;
		final com.nd.shapedexamproj.model.homework.Homework modelHomework = com.nd.shapedexamproj.model.homework.Homework.readHomework(mHomeworkId);
		boolean hasDone = com.nd.shapedexamproj.model.homework.Homework.checkUserHasDoHomework(mHomeworkId);

		if (hasDone && modelHomework != null) {//已做未提交，本地计算客观题分数
			modelHomework.readUserAnswer();
			intObjScore = (int) modelHomework.getObjectiveScore();
			strObjScore = "" + intObjScore;
		}

		objectScoreTv.setText(strObjScore);
		objectScoreRp.setProgress(intObjScore);

		if (homework.user_score >= 60) {
			highestScoreTv.setTextColor(getResources().getColor(R.color.title_green));
			highestScoreRp.setCricleProgressColor(getResources().getColor(R.color.title_green));
		} else {
			highestScoreTv.setTextColor(getResources().getColor(R.color.red_txt));
			highestScoreRp.setCricleProgressColor(getResources().getColor(R.color.red_txt));
		}

		int highestScore = homework.user_score == -1 ? 0 : homework.user_score;
        highestScoreTv.setText("" + highestScore);
        highestScoreRp.setProgress(homework.user_score);
        highestScoreRp.postInvalidate();

		if (!hasDone) {//如果重新做未提交，则不改变颜色
			if (homework.user_score >= 60 ) {
				totalScoreTv.setTextColor(getResources().getColor(R.color.title_green));
				totalScoreRp.setCricleProgressColor(getResources().getColor(R.color.title_green));
			} else if (homework.user_score >= 0 && homework.user_score < 60) {
				totalScoreTv.setTextColor(getResources().getColor(R.color.red_txt));
				totalScoreRp.setCricleProgressColor(getResources().getColor(R.color.red_txt));
			}
		} else {
			totalScoreTv.setTextColor(getResources().getColor(R.color.red_txt));
			totalScoreRp.setCricleProgressColor(getResources().getColor(R.color.red_txt));
		}

		if (hasDone) {
			subjectScoreTv.setText("?");
			totalScoreTv.setText("?");

			subjectScoreRp.setProgress(0);
			totalScoreRp.setProgress(0);
		} else {
			if (homework.is_marked == 0) {//未批改
				subjectScoreTv.setText(getResources().getString(R.string.is_to_correct));
				totalScoreTv.setText("?");
			} else {
				subjectScoreTv.setText(String.valueOf((int)homework.sbj_score));
				totalScoreTv.setText(String.valueOf(homework.user_score));

				subjectScoreRp.setProgress((int)homework.sbj_score);
				totalScoreRp.setProgress(homework.user_score);
			}
		}

	}
	
	/**
	 * 未交卷状态
	 */
	private void stateNotCommitted(Homework homework) {
		mHighestScoreImgRL.setVisibility(View.GONE);
		homework_detail_undone_tv1.setText(notDone);

		int unDoneNum = homework.undo_num;
		final com.nd.shapedexamproj.model.homework.Homework modelHomework = com.nd.shapedexamproj.model.homework.Homework.readHomework(mHomeworkId);
		if (modelHomework != null) {
			modelHomework.readUserAnswer();
			unDoneNum = modelHomework.getSubjectSizeOfNotDone();
		}
		homework_detail_undone_tv2.setText("" + unDoneNum);//显示未做题目
		
		boolean hasDone = com.nd.shapedexamproj.model.homework.Homework.checkUserHasDoHomework(mHomeworkId);
		if (hasDone) {						//已答过题，未提交
			homework_detail_two_btn_ll.setVisibility(View.VISIBLE);
			start_test_btn.setVisibility(View.GONE);
			
			mCurrScoreImgRL.setVisibility(View.VISIBLE);
		} else {							//从未答过题
			homework_detail_two_btn_ll.setVisibility(View.GONE);
			start_test_btn.setVisibility(View.VISIBLE);
			
			mCurrScoreImgRL.setVisibility(View.GONE);
		}
	}
	
	/**
	 * 已完成的状态
	 * @param homework
	 */
	private void stateHasCommitted(Homework homework) {
	    mCurrScoreImgRL.setVisibility(View.VISIBLE);
		/*homework_detail_undone_tv2.setTextSize(TypedValue.COMPLEX_UNIT_SP, 34);*/
		homework_detail_undone_tv2.setText("" + homework.error_num);//显示做错的题目

		boolean hasDone = com.nd.shapedexamproj.model.homework.Homework.checkUserHasDoHomework(mHomeworkId);
		if (hasDone) {						//重新答过题，未提交
			if (homework.answer_num < homework.answer_total || homework.answer_total == -1) {	//答题次数未满
				homework_detail_two_btn_ll.setVisibility(View.VISIBLE);
				start_test_btn.setVisibility(View.GONE);
			} else {
				homework_detail_two_btn_ll.setVisibility(View.GONE);
				start_test_btn.setVisibility(View.VISIBLE);
			}
		} else {							//未重新答题
			homework_detail_two_btn_ll.setVisibility(View.GONE);
			start_test_btn.setVisibility(View.VISIBLE);
		}

		homework_detail_undone_tv1.setText(doWrong);
		homework_detail_undone_rl.setBackgroundColor(getResources().getColor(R.color.red_bg));

		if (homework.is_marked == 0) {
			mHighestScoreImgRL.setVisibility(View.VISIBLE);

			String startTimeStr = new SimpleDateFormat("yyyy年MM月dd日HH:mm").format(new Date(homework.submit_time * 1000));
			String commitTime = getResources().getString(R.string.committed_at);
			String replaceStr = commitTime.replace("{0}", startTimeStr);
			if (!hasDone) {
				mCurrentScoreTv.setText(replaceStr);
			} else {
				mCurrentScoreTv.setText(getResources().getString(R.string.current_score));
			}
		} else if (homework.is_marked == 1) {
			if (!hasDone) {
				mCurrentScoreTv.setText(getResources().getString(R.string.highest_score));
				mHighestScoreImgRL.setVisibility(View.GONE);
			} else {
				mHighestScoreImgRL.setVisibility(View.VISIBLE);
			}
		}

		long timeLag = getTimeLag(homework.finish_time * 1000);
		if (timeLag < 0) {
			start_test_btn.setText(getResources().getString(R.string.is_out_of_date));
			setBtnEnabled(false);
		} else {
			if (homework.user_score == 100) {
				start_test_btn.setText(getResources().getString(R.string.fucking_genius));
				setBtnEnabled(false);
			} else {
				if (homework.answer_num < homework.answer_total || homework.answer_total == -1) {//还可继续答题
					setBtnEnabled(true);
					start_test_btn.setText(getResources().getString(R.string.redo_homework));
				} else {//答题次数已用完
					setBtnEnabled(false);
					start_test_btn.setText(getResources().getString(R.string.no_more_chance));
				}
			}
		}
		
	}
	/**
	 * 设置按钮不可用
	 */
	private void setBtnEnabled (boolean isEnabled){
		start_test_btn.setEnabled(isEnabled);
		if (!isEnabled) {
			start_test_btn.setBackgroundColor(getResources().getColor(R.color.border_color));
			start_test_btn.setTextColor(getResources().getColor(R.color.light_black));
		} else {
			start_test_btn.setBackgroundDrawable(getResources().getDrawable(R.drawable.common_btn_selector));
			start_test_btn.setTextColor(getResources().getColor(R.color.white));
		}
	}
	
	@Override
	public void addListener() {
		commonheader_left_iv.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				Intent intent = new Intent();
				intent.setAction("homework.detail.finish");
				intent.putExtra("homework_id", mHomeworkId);
				intent.putExtra("homework_state", homework_state); 		
				Log.e("homework_state", "发送："+homework_state);
				sendBroadcast(intent);
				finish();
			}
		});
		
		start_test_btn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				UIHelper.showDoingHomeworkActivity(HomeworkDetailActivity.this, mHomeworkId, false);
			}
		});
		commit_test_btn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				showHandInHomeworkDialog();
			}
		});
		continue_test_btn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// 跳转到最后一题
				/*continueTest();*/
				UIHelper.showDoingHomeworkActivity(HomeworkDetailActivity.this, mHomeworkId,true);
			}
		});
		
		homework_detail_undone_rl.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				String text = homework_detail_undone_tv1.getText().toString();

				if (text.equals(doWrong)) {
					//错题重做
					UIHelper.showErrorActivity(HomeworkDetailActivity.this);
				} else if (text.equals(notDone)) {
					UIHelper.showDoingHomeworkActivity(HomeworkDetailActivity.this, mHomeworkId, true);
				}
			}
		});
		homework_detail_all_rl.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				/*UIHelper.showDoingHomeworkActivity(HomeworkDetailActivity.this, mHomeworkId, true);*///屏蔽
			}
		});
		
	}

	/**
	 * 交卷对话框
	 */
	public void showHandInHomeworkDialog() {
		final com.nd.shapedexamproj.model.homework.Homework homework = com.nd.shapedexamproj.model.homework.Homework.readHomework(mHomeworkId);
		homework.readUserAnswer();
		SimpleAlertDialog dialog = new SimpleAlertDialog(this) {
			@Override
			public void onPositiveButtonClick() {
				if (PhoneUtil.checkNetworkEnable() == PhoneUtil.NETSTATE_DISABLE) {
					Toast.makeText(HomeworkDetailActivity.this,getResources().getString(R.string.net_error),Toast.LENGTH_SHORT).show();
					return;
				}
				HandInHomeworkTask task = new HandInHomeworkTask(HomeworkDetailActivity.this, mHomeworkId, true){
					@Override
					protected void onPostExecute(Boolean aBoolean) {
						super.onPostExecute(aBoolean);
						if (aBoolean) {
							requestCurrentTime();
						}
					}
				};
				task.execute();
			}
		};
		dialog.setContent(homework.getDoingProfileString(true));
		dialog.setTitle("确定交卷?");
		dialog.setPositiveButton("交卷");
		dialog.setNegativeButton("暂不");
		dialog.show();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		
		if(keyCode == KeyEvent.KEYCODE_BACK){
			Log.e(TAG, "onKeyDown 作业详情页============");
			Intent intent = new Intent();
			intent.setAction("homework.detail.finish");
			intent.putExtra("homework_id", mHomeworkId);
			intent.putExtra("homework_state", homework_state); 		
			Log.e(TAG, "homework_state"+"发送：" + homework_state);
			sendBroadcast(intent);
			finish();
		}
		
		return true;
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
