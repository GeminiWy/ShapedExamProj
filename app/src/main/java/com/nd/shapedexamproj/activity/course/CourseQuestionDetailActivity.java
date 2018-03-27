package com.nd.shapedexamproj.activity.course;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nd.shapedexamproj.App;
import com.nd.shapedexamproj.R;
import com.nd.shapedexamproj.activity.BaseActivity;
import com.nd.shapedexamproj.model.my.MyQuestion;
import com.nd.shapedexamproj.util.Constants;
import com.nd.shapedexamproj.util.ImageUtil;
import com.nd.shapedexamproj.util.StringUtils;
import com.nd.shapedexamproj.util.UIHelper;
import com.nd.shapedexamproj.view.CircularImage;
import com.tming.common.util.Log;

/**
 * 
 * @ClassName: MyQuestionDetailActivity
 * @Title:
 * @Description:我的-我的提问-提问详情
 * @Author:XueWenJian
 * @Since:2014年5月30日11:33:32
 * @Version:1.0
 */
public class CourseQuestionDetailActivity  extends BaseActivity {

	private final static String TAG = "CourseQuestionDetailActivity";
	
	private Context mContext;
	
	private RelativeLayout mHeadRL;
	private ImageView mBackIV;
	private TextView mHeadTitleTV;
	private Button mHeadRightBtn;
	
	private ImageView mQuestionStateIV;
	private TextView mQuestionTitleTV;
	private TextView mQuestionContentTV;
	private RelativeLayout courseQuestionLinkRl ;
	private TextView mQuestionLinkTV;
	private RelativeLayout mCourseAnswerRL;
	private CircularImage mAnswerUserImageIV;
	private TextView mAnswerUserNameTV;
	private TextView mAnswerUserTypeTV;
	private TextView mAnswerUserContentTV;
	private TextView mQuestionIgnoreTV;
	
	private MyQuestion mMyQuestion;
	
	@Override
	public int initResource() {
		return R.layout.course_question_detail_activity;
	}

	@Override
	public void initComponent() {
		
		mHeadRL = (RelativeLayout) findViewById(R.id.common_head_layout);
		mBackIV = (ImageView) findViewById(R.id.commonheader_left_iv);
		mHeadTitleTV = (TextView) findViewById(R.id.commonheader_title_tv);
		mHeadRightBtn = (Button) findViewById(R.id.commonheader_right_btn);
		
		mQuestionStateIV = (ImageView)findViewById(R.id.course_question_state_iv);
		mQuestionTitleTV = (TextView)findViewById(R.id.course_question_title_tv);
		mQuestionContentTV = (TextView)findViewById(R.id.course_question_content_tv);
		
		courseQuestionLinkRl = (RelativeLayout) findViewById(R.id.course_question_link_rl);
		mQuestionLinkTV = (TextView)findViewById(R.id.course_question_link_tv);
		mCourseAnswerRL = (RelativeLayout)findViewById(R.id.course_answer_rl);
		mAnswerUserImageIV = (CircularImage)findViewById(R.id.course_answer_userimage_iv);
		mAnswerUserNameTV = (TextView)findViewById(R.id.course_answer_username_tv);
		mAnswerUserTypeTV = (TextView)findViewById(R.id.course_answer_user_type_tv);
		mAnswerUserContentTV = (TextView)findViewById(R.id.course_answer_content_tv);
		mQuestionIgnoreTV = (TextView)findViewById(R.id.course_question_ignore_tv);
		
		mHeadTitleTV.setText(R.string.course_question_title);
		mHeadRightBtn.setVisibility(View.VISIBLE);
		mHeadRightBtn.setText(R.string.text_new_question);
	}

	@Override
	public void initData() {
		mContext = this;
		
		Intent intent = this.getIntent(); 
		mMyQuestion = (MyQuestion)intent.getSerializableExtra("question");
		
		if(null != mMyQuestion)
		{
			mQuestionTitleTV.setText(mMyQuestion.getTitle());
			mQuestionContentTV.setText(mMyQuestion.getQuestion());
			
			mQuestionLinkTV.setText("《" + mMyQuestion.getCourseName() + "》");
			
			if(!StringUtils.isEmpty(mMyQuestion.getChapterName()))
			{
				mQuestionLinkTV.append(mMyQuestion.getChapterName());
			}
			
			if(mMyQuestion.getAnswer_status() == mMyQuestion.QUESTION_STATE_ANSWERED || !StringUtils.isEmpty(mMyQuestion.getAnswer()))
			{
				mCourseAnswerRL.setVisibility(View.VISIBLE);
				mQuestionIgnoreTV.setVisibility(View.GONE);
				mQuestionStateIV.setBackgroundResource(R.drawable.classroom_icon_kj_ask_2);
				
				mAnswerUserNameTV.setText(mMyQuestion.getAnswerName());
				mAnswerUserTypeTV.setText(R.string.coach_teacher);
				/*String content = Helper.fixHtmlText(mMyQuestion.getAnswer());*/
				mAnswerUserContentTV.setText(mMyQuestion.getAnswer());
				
				ImageUtil.asyncLoadImage(mAnswerUserImageIV, mMyQuestion.getAnswerAvatar(), R.drawable.all_use_icon_photo);

			}
			else
			{
				mQuestionStateIV.setBackgroundResource(R.drawable.classroom_icon_question);
				mCourseAnswerRL.setVisibility(View.GONE);
				mQuestionIgnoreTV.setVisibility(View.GONE);
			}
			
			if(mMyQuestion.getIs_ignore() == MyQuestion.QUESTION_IGNORE_YES)
			{
				mQuestionStateIV.setBackgroundResource(R.drawable.classroom_icon_kj_ask_3);
				mQuestionIgnoreTV.setVisibility(View.VISIBLE);
				mCourseAnswerRL.setVisibility(View.GONE);
			}

		}

	}

	@Override
	public void initAuthoriy() {
		if (App.getUserType() != Constants.USER_TYPE_STUDENT) {
			mHeadRightBtn.setVisibility(View.INVISIBLE);
		}
		
		super.initAuthoriy();
	}
	
	@Override
	public void addListener() {
		mBackIV.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				CourseQuestionDetailActivity.this.finish();
				
			}
		});
		mHeadRightBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {

				UIHelper.showCourseNewQuestionActivity(CourseQuestionDetailActivity.this, mMyQuestion.getCourse_id(), mMyQuestion.getCourseName(), mMyQuestion.getChapter_id(), mMyQuestion.getChapterName());
			}
		});
		courseQuestionLinkRl.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				UIHelper.showCourseDetail(mContext, mMyQuestion.course_id, mMyQuestion.courseName);
			}
		});
		
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		Log.d(TAG, "onActivityResult; requestCode:"+ requestCode+"; resultCode:"+resultCode);
		
		if(RESULT_OK == resultCode)
		{
			setResult(RESULT_OK);
		}
	}

}
