package com.nd.shapedexamproj.activity.course;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.FragmentManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
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
 * @ClassName: CoachReplyTopicActivty
 * @Title:
 * @Description:课堂-辅导讨论区-帖子详情-回复帖子
 * @Author:XueWenJian
 * @Since:2014年5月23日10:39:41
 * @Version:1.0
 */
public class CoachReplyTopicActivty extends BaseActivity {

	private final static String TAG = "CoachReplyTopicActivty";
	
	private static final int INPUT_MAX_LENGTH = 300;
	private static final int INPUT_MIN_LENGHT = 6;
	
	private Context mContext;
	private LinearLayout mLoadinglayout ;
	private RelativeLayout mHeadRL;
	private ImageView mBackIV;
	private TextView mHeadTitleTV;
	private Button mHeadRightBtn;
	private InputBottomBar mInputBottomBar;
	private EditText mReplayTopicContentET;
	private FragmentManager fragmentManager;
	
	private String mCourseId;
	private String mTopicId;
	private InputMethodManager imm;
	
	@Override
	public int initResource() {
		return R.layout.course_coachdisc_replaytopic_activity;
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
		mReplayTopicContentET = (EditText) findViewById(R.id.coachdisc_replaytopic_content_et);
		
		mHeadTitleTV.setText(R.string.coach_replay_topic_title);
		mHeadRightBtn.setVisibility(View.VISIBLE);
		mHeadRightBtn.setText(R.string.coach_replay_topic_send);
		
		View view = ((ViewGroup)findViewById(android.R.id.content)).getChildAt(0);
		mInputBottomBar = new InputBottomBar(mContext,fragmentManager, view, mReplayTopicContentET, INPUT_MAX_LENGTH);
		sendClickable(false);
		
		imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
		imm.showSoftInput(mReplayTopicContentET, InputMethodManager.SHOW_FORCED);
		
	}

	@Override
	public void initData() {
		mCourseId = getIntent().getStringExtra("courseId");
		mTopicId = getIntent().getStringExtra("topicId");
	}

	@Override
	public void addListener() {
		
		mReplayTopicContentET.addTextChangedListener(new TextWatcher() {
			private  CharSequence mTemp;
            private int mSelectionStart;
            private int mSelectionEnd;
            int mMinNum = INPUT_MIN_LENGHT;
            
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
               
                mSelectionStart = mReplayTopicContentET.getSelectionStart();
                mSelectionEnd = mReplayTopicContentET.getSelectionEnd();
                Log.i(TAG,""+mSelectionStart+" "+mSelectionEnd);

                if(mTemp.length() >= mMinNum)
                {
                	sendClickable(true);
                }
                else
                {
                	sendClickable(false);
                }
                
			}
		});
		
		mReplayTopicContentET.setOnFocusChangeListener(new OnFocusChangeListener() {
			
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
		mReplayTopicContentET.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) { 
				if(mInputBottomBar.hasShowFace()){  
					mInputBottomBar.hideFace();
				}				
			}
		});
		
		mBackIV.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				CoachReplyTopicActivty.this.finish();
				
			}
		});
		mHeadRightBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
			    if (PhoneUtil.checkNetworkEnable(mContext) == PhoneUtil.NETSTATE_DISABLE) {
			        Toast.makeText(mContext, getResources().getString(R.string.please_open_network),
			                Toast.LENGTH_SHORT).show();
			        return ;
			    }
				//发送
				String content = mReplayTopicContentET.getText().toString();
				Log.d(TAG, "content:"+content);
				if(!StringUtils.isEmpty(content))
				{
					if(content.length() >= INPUT_MIN_LENGHT) {
						mInputBottomBar.getInputMethodManager().hideSoftInputFromWindow(CoachReplyTopicActivty.this.getCurrentFocus().getWindowToken()
								,InputMethodManager.HIDE_NOT_ALWAYS);
						mHeadRightBtn.setClickable(false);
						sendReplay(content);
					} else {
						Toast.makeText(CoachReplyTopicActivty.this, getResources().getString(R.string.input_at_least_6_charector,INPUT_MIN_LENGHT), 
	                			Toast.LENGTH_SHORT).show();
					}
				}
				
			}
		});
	}
	
	private void sendReplay(String content)
	{
		mLoadinglayout.setVisibility(View.VISIBLE);
		
		String url = Constants.DISCUSSION_REPLAY_TOPIC;
		Log.d(TAG, url);
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("topic_id", mTopicId);
		params.put("courseid", mCourseId);
		params.put("userid", App.getUserId());
		params.put("title", getResources().getString(R.string.coach_replay_topic_default_title));
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
							//刷新课程详情页，辅导讨论区
							Intent intent = new Intent();
							intent.setAction("kd.coursecoach.refresh");
							mContext.sendBroadcast(intent);

							CoachReplyTopicActivty.this.finish();
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
						Toast.makeText(mContext,
								getResources().getString(R.string.net_error),
								Toast.LENGTH_SHORT).show();
						exception.printStackTrace();
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
