package com.nd.shapedexamproj.activity.course;

import android.content.Context;
import android.support.v4.app.FragmentManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.widget.*;

import com.nd.shapedexamproj.App;
import com.nd.shapedexamproj.R;
import com.nd.shapedexamproj.activity.BaseActivity;
import com.nd.shapedexamproj.util.Constants;
import com.nd.shapedexamproj.util.JsonParseObject;
import com.nd.shapedexamproj.util.StringUtils;
import com.nd.shapedexamproj.util.Utils;
import com.nd.shapedexamproj.view.InputBottomBar;
import com.tming.common.net.TmingHttp;
import com.tming.common.util.Log;
import java.util.HashMap;
import java.util.Map;

/**
 * 
 * @ClassName: CoachNewTopicActivity
 * @Title:
 * @Description:课堂-辅导讨论区-新提问
 * @Author:XueWenJian
 * @Since:2014年5月21日10:45:19
 * @Version:1.0
 */
public class CoachNewTopicActivity extends BaseActivity {

	private final static String TAG = "CoachDiscListActivity";
	
	private static final int SEND_SUCCESS_FLAG = 2;
	
	private static final int INPUT_MAX_LENGTH_TITLE = 30;
	private static final int INPUT_MAX_LENGTH_CONTENT = 300;
	
	private Context mContext;
	private LinearLayout mLoadinglayout ;
	private RelativeLayout mHeadRL;
	private ImageView mBackIV;
	private TextView mHeadTitleTV;
	private Button mHeadRightBtn;
	private InputBottomBar mInputBottomBar;
	private EditText mNewTopicTitleET;
	private EditText mNewTopicContentET;
	private FragmentManager fragmentManager;
	
	private boolean mSendTitleFlag;//判断标题输入是否符合要求
	private boolean mSendContentFlag;//判断内容输入是否符合要求
	
	private String mCourseId;
	
	@Override
	public int initResource() {
		return R.layout.course_coachdisc_newtopic_activity;
	}

	@Override
	public void initComponent() {
		mContext = this;

		mLoadinglayout = (LinearLayout) findViewById(R.id.loading_layout);
		mHeadRL = (RelativeLayout) findViewById(R.id.coachdisc_head_layout);
		mBackIV = (ImageView) findViewById(R.id.commonheader_left_iv);
		mHeadTitleTV = (TextView) findViewById(R.id.commonheader_title_tv);
		mHeadRightBtn = (Button) findViewById(R.id.commonheader_right_btn);
		
		fragmentManager = getSupportFragmentManager();
		mNewTopicTitleET = (EditText) findViewById(R.id.coachdisc_newtopic_title_et);
		mNewTopicContentET = (EditText) findViewById(R.id.coachdisc_newtopic_content_et);
		
		mHeadTitleTV.setText(R.string.coach_new_topic);
		mHeadRightBtn.setVisibility(View.VISIBLE);
		mHeadRightBtn.setText(R.string.coach_new_topic_send);
		
		View view = ((ViewGroup)findViewById(android.R.id.content)).getChildAt(0);
		mInputBottomBar = new InputBottomBar(mContext, fragmentManager, view, mNewTopicContentET,INPUT_MAX_LENGTH_CONTENT);
		mInputBottomBar.setVisiable(View.INVISIBLE);
		
		mSendTitleFlag = false;
		mSendContentFlag = false;
		sendClickable();
	}

	@Override
	public void initData() {
		mCourseId = getIntent().getStringExtra("courseId");
		
		
	}

	@Override
	public void addListener() {
		mBackIV.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				CoachNewTopicActivity.this.finish();
				
			}
		});
		mHeadRightBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				if (mNewTopicTitleET.getText().toString().length() < 6) {
					
					Toast.makeText(CoachNewTopicActivity.this, getResources().
							getString(R.string.input_at_least_6_title), Toast.LENGTH_SHORT).show();
					return ;
				} 
				/*if (mNewTopicContentET.getText().toString().length() < 6) {
					
					Toast.makeText(CoachNewTopicActivity.this, getResources().
							getString(R.string.input_at_least_5_content), Toast.LENGTH_SHORT).show();
					return ;
				}*/ 
				mHeadRightBtn.setClickable(false);
				sendNewTopic();	
			}
		});
		
	    String errTitleMaxLength = getResources().getString(R.string.coach_title_max_length);
	    errTitleMaxLength = String.format(errTitleMaxLength, INPUT_MAX_LENGTH_TITLE);
		
		Utils.addEditViewMaxLengthListener(mContext, mNewTopicTitleET, INPUT_MAX_LENGTH_TITLE, errTitleMaxLength);
		mNewTopicTitleET.addTextChangedListener(new TextWatcher() {
			private  CharSequence mTemp;
            private int mSelectionStart;
            private int mSelectionEnd;
            int mMinNum = 6;
            
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				//S：变化后的所有字符；start：字符起始的位置；before: 变化之前的总字节数；count:变化后的字节数
				mTemp = s;
			}
			
			@Override
			public void beforeTextChanged(CharSequence charSequence, int start, int count,
					int after) {
				//s:变化前的所有字符； start:字符开始的位置； count:变化前的总字节数；after:变化后的字节数
				
			}
			
			@Override
			public void afterTextChanged(Editable editable) {
				//s:变化后的所有字符
               
                mSelectionStart = mNewTopicTitleET.getSelectionStart();
                mSelectionEnd = mNewTopicTitleET.getSelectionEnd();
                Log.i("TAG",""+mSelectionStart+" "+mSelectionEnd);
               
                
                if(mTemp.length() >= mMinNum)
                {
                	mSendTitleFlag = true;
                	sendClickable();
                }
                else
                {
                	mSendTitleFlag = false;
                	sendClickable();
                }
			}
		});

		mNewTopicContentET.addTextChangedListener(new TextWatcher() {
			private  CharSequence mTemp;
            private int mSelectionStart;
            private int mSelectionEnd;
            int mMinNum = 0;
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				//S：变化后的所有字符；start：字符起始的位置；before: 变化之前的总字节数；count:变化后的字节数
				mTemp = s;
			}
			
			@Override
			public void beforeTextChanged(CharSequence charSequence, int start, int count,
					int after) {
				//s:变化前的所有字符； start:字符开始的位置； count:变化前的总字节数；after:变化后的字节数
				
			}
			
			@Override
			public void afterTextChanged(Editable editable) {
				//s:变化后的所有字符
               
                mSelectionStart = mNewTopicContentET.getSelectionStart();
                mSelectionEnd = mNewTopicContentET.getSelectionEnd();
                Log.i("TAG",""+mSelectionStart+" "+mSelectionEnd);
                
                
               if(mTemp.length() >= mMinNum)
                {
                	mSendContentFlag = true;
                	sendClickable();
                }
                else
                {
                	mSendContentFlag = false;
                	sendClickable();
                }
                
			}
		});
		  
		mNewTopicContentET.setOnFocusChangeListener(new OnFocusChangeListener() {
			
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if(hasFocus)
				{ 
					mInputBottomBar.setVisiable(View.VISIBLE);	 
					if(mInputBottomBar.hasShowFace()){  
						mInputBottomBar.hideFace();
					}
				}
				else
				{
					mInputBottomBar.setVisiable(View.INVISIBLE);
				}
				
			}
		});
		
		mNewTopicContentET.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) { 
				if(mInputBottomBar.hasShowFace()){  
					mInputBottomBar.hideFace();
				}				
			}
		});
		
	}
	private void sendClickable()
	{

		if (true == mSendTitleFlag && true == mSendContentFlag) {
			mHeadRightBtn.setClickable(true);
			mHeadRightBtn.setBackgroundResource(R.drawable.null_foreground_button_pressed);
				
		} else {
			/*mHeadRightBtn.setClickable(false);*/
			mHeadRightBtn.setBackgroundColor(getResources().getColor(R.color.light_black));
			
		}
		
	}
	
	private void sendNewTopic()
	{
		mLoadinglayout.setVisibility(View.VISIBLE);

		String title = mNewTopicTitleET.getText().toString();
		String content = mNewTopicContentET.getText().toString();
		if(StringUtils.isEmpty(content))
		{
			//内容可以为空，当内容为空时，帖子内容=帖子标题
			content = title;
		}
		
		String url = Constants.DISCUSSION_NEW_TOPIC;
		Log.d(TAG, url);
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("courseid", mCourseId);
		params.put("userid", App.getUserId());
		params.put("title", title);
		params.put("content", content);

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
							CoachNewTopicActivity.this.finish();
							Toast.makeText(mContext, R.string.coach_send_success, Toast.LENGTH_SHORT).show();

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
						mLoadinglayout.setVisibility(View.GONE);
						Toast.makeText(mContext,
								getResources().getString(R.string.net_error),
								Toast.LENGTH_SHORT).show();
						exception.printStackTrace();
						mHeadRightBtn.setClickable(true);
					}

				});
	}
}
