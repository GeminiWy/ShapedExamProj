package com.nd.shapedexamproj.activity.course;

import android.content.Context;
import android.support.v4.app.FragmentManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;

import com.nd.shapedexamproj.App;
import com.nd.shapedexamproj.R;
import com.nd.shapedexamproj.activity.BaseActivity;
import com.nd.shapedexamproj.util.Constants;
import com.nd.shapedexamproj.util.JsonParseObject;
import com.nd.shapedexamproj.util.StringUtils;
import com.nd.shapedexamproj.view.InputBottomBar;
import com.tming.common.net.TmingHttp;
import com.tming.common.util.Log;
import com.tming.common.util.PhoneUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * 
 * @ClassName: CourseNewQuestionActivity
 * @Title:
 * @Description:课堂-答疑-发起提问/我的-我的提问-发起提问
 * @Author:XueWenJian
 * @Since:2014年5月22日10:16:54
 * @Version:1.0
 */
public class CourseNewQuestionActivity extends BaseActivity {

	private final static String TAG = "CourseNewQuestionActivity";
	
	//取问题的标题长度
	private final static int QUESTION_TITLE_SIZE = 15;
	private static final int INPUT_MIN_LENGHT = 6;
	
	private Context mContext;
	
	private LinearLayout mLoadinglayout ;
	private RelativeLayout mHeadRL;
	private ImageView mBackIV;
	private TextView mHeadTitleTV;
	private Button mHeadRightBtn;
	private InputBottomBar mInputBottomBar;
	private EditText mQuestionContentET;
	private TextView mQuestionLinkTV;
	private FragmentManager fragmentManager;
	
	private String mCourseId;
	private String mCourseName;
	private String mChapterId;
	private String mChapterName;
	private InputMethodManager imm;
	
	@Override
	public int initResource() {
		return R.layout.course_new_question_activity;
	}

	@Override
	public void initComponent() {
		mContext = this;
		
		mLoadinglayout = (LinearLayout) findViewById(R.id.loading_layout);
		mHeadRL = (RelativeLayout) findViewById(R.id.common_head_layout);
		mBackIV = (ImageView) findViewById(R.id.commonheader_left_iv);
		mHeadTitleTV = (TextView) findViewById(R.id.commonheader_title_tv);
		mHeadRightBtn = (Button) findViewById(R.id.commonheader_right_btn);
		
		fragmentManager = getSupportFragmentManager();
		mQuestionContentET = (EditText) findViewById(R.id.course_qusertion_content_et);
		mQuestionLinkTV = (TextView) findViewById(R.id.course_question_link_tv);
		
		mHeadTitleTV.setText(R.string.course_new_question_title);
		mHeadRightBtn.setVisibility(View.VISIBLE);
		mHeadRightBtn.setText(R.string.feedback_commit);//modified by Caiyx on 20140818
		sendClickable(false);

		View view = ((ViewGroup)findViewById(android.R.id.content)).getChildAt(0);
		mInputBottomBar = new InputBottomBar(mContext,fragmentManager, view, mQuestionContentET,INPUT_MAX_LENGTH_CONTENT);
		mInputBottomBar.setVisiable(View.GONE);
	}

	@Override
	public void initData() {
		mCourseId = getIntent().getStringExtra("courseId");
		mCourseName = getIntent().getStringExtra("courseName");
		mChapterId = getIntent().getStringExtra("chapterId");
		mChapterName = getIntent().getStringExtra("chapterName");
		Log.d(TAG, "chapter_id:"+mChapterId+";courseid:"+mCourseId);
		
		mQuestionLinkTV.setText("《" + mCourseName + "》");
		
		if(!StringUtils.isEmpty(mChapterName))
		{
			mQuestionLinkTV.append("-");
			mQuestionLinkTV.append(mChapterName);
		}
	}

	@Override
	public void initAuthoriy() {
		if (App.getUserType() != Constants.USER_TYPE_STUDENT) {
			mHeadRightBtn.setVisibility(View.INVISIBLE);
		}
		
		super.initAuthoriy();
	}
	

	private static final int INPUT_MAX_LENGTH_CONTENT = 300;
	
	@Override
	public void addListener() {
		
		mQuestionContentET.addTextChangedListener(new TextWatcher() {
			private  CharSequence mTemp;
            private int mSelectionStart;
            private int mSelectionEnd;
            int mMinNum = INPUT_MIN_LENGHT;
 
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				//S：变化后的所有字符；start：字符起始的位置；before: 变化之前的总字节数；count:变化后的字节数
			}
			
			@Override
			public void beforeTextChanged(CharSequence charSequence, int start, int count,
					int after) {
				//s:变化前的所有字符； start:字符开始的位置； count:变化前的总字节数；after:变化后的字节数
				mTemp = charSequence;
			}
			
			@Override
			public void afterTextChanged(Editable editable) {
				//s:变化后的所有字符
               
                mSelectionStart = mQuestionContentET.getSelectionStart();
                mSelectionEnd = mQuestionContentET.getSelectionEnd();
                Log.i("TAG",""+mSelectionStart+" "+mSelectionEnd);
                
				if (mTemp.length() >= mMinNum) {
				    sendClickable(true);
				} else {
				    sendClickable(false);

				}
                
			}
		});
		
		mBackIV.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				setResult(RESULT_OK);
				CourseNewQuestionActivity.this.finish();
				
			}
		});
		mHeadRightBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				if (PhoneUtil.checkNetworkEnable(mContext) == PhoneUtil.NETSTATE_DISABLE) {
					Toast.makeText(mContext, getResources().getString(R.string.please_open_network), Toast.LENGTH_SHORT).show();
					return ;
				}
				//发送
				String allInput = mQuestionContentET.getText().toString();
				String title = "";
				String content = allInput;
				if(QUESTION_TITLE_SIZE < allInput.length())
				{
					title = allInput.subSequence(0, QUESTION_TITLE_SIZE).toString();
					title += "...";
				}
				else
				{
					title = allInput;
				}
				
				
				if(!StringUtils.isEmpty(content))
				{
					if(content.length() >= INPUT_MIN_LENGHT){
						mInputBottomBar.getInputMethodManager().hideSoftInputFromWindow(CourseNewQuestionActivity.this.getCurrentFocus().getWindowToken()
								,InputMethodManager.HIDE_NOT_ALWAYS);
						mHeadRightBtn.setClickable(false);
						sendNewQuestion(title, content);
					}
					else{
						Toast.makeText(mContext, getResources().getString(R.string.input_at_least_6_charector,INPUT_MIN_LENGHT), Toast.LENGTH_LONG).show();
					}
				}
				
			}
		});
		
	}

	private void sendNewQuestion(String title,String content)
	{
		Log.d(TAG, "title:"+title+";content:"+content);
		Log.d(TAG, "userid:"+App.getUserId());
		
		mLoadinglayout.setVisibility(View.VISIBLE);
		
		String url = Constants.COURSE_NEW_QUESTION;
		Log.d(TAG, url);
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("chapter_id", mChapterId);
		params.put("courseid", mCourseId);
		params.put("userid", App.getUserId());
		params.put("title", title);
		params.put("question", content);

		TmingHttp.asyncRequest(url, params,
				new TmingHttp.RequestCallback<Integer>() {

					@Override
					public Integer onReqestSuccess(String respones)
							throws Exception {
						// 数据组装
						Log.i(TAG, respones);
						JsonParseObject jsonParseObject = JsonParseObject.parseJson(respones);
						Log.d(TAG, jsonParseObject.getResJs().toString());


						return jsonParseObject.getFlag();

					}

					@Override
					public void success(Integer result) {
						// UI表现
						if (Constants.SUCCESS_MSG == result) {

							mLoadinglayout.setVisibility(View.GONE);
							setResult(RESULT_OK);
							CourseNewQuestionActivity.this.finish();
							Toast.makeText(mContext, R.string.commit_question_success, Toast.LENGTH_SHORT).show();//modified by Caiyx on 20140818
						} else {
							Toast.makeText(mContext,
									R.string.api_error, Toast.LENGTH_SHORT)
									.show();
							return;
						}
						mHeadRightBtn.setClickable(true);
					}

					@Override
					public void exception(Exception exception) {
						Toast.makeText(mContext,
								getResources().getString(R.string.net_error),
								Toast.LENGTH_SHORT).show();
						exception.printStackTrace();
						mLoadinglayout.setVisibility(View.GONE);
						mHeadRightBtn.setClickable(true);
					}

				});
	}
	
	private void sendClickable(boolean clickable)
	{
		/*mHeadRightBtn.setClickable(clickable);*/
		if(clickable)
		{
			mHeadRightBtn.setBackgroundResource(R.drawable.null_foreground_button_pressed);
		}
		else
		{
			mHeadRightBtn.setBackgroundColor(getResources().getColor(R.color.light_black));
		}
	}
}
