package com.nd.shapedexamproj.activity.plaza;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;

import com.nd.shapedexamproj.App;
import com.nd.shapedexamproj.R;
import com.nd.shapedexamproj.activity.BaseActivity;
import com.nd.shapedexamproj.model.Comment;
import com.nd.shapedexamproj.util.Constants;
import com.nd.shapedexamproj.util.JsonParseObject;
import com.nd.shapedexamproj.util.StringUtils;
import com.nd.shapedexamproj.util.UIHelper;
import com.nd.shapedexamproj.view.InputBottomBar;
import com.tming.common.net.TmingHttp;
import com.tming.common.util.Helper;
import com.tming.common.util.Log;
import com.tming.common.util.PhoneUtil;

/**
 * 
 * 类名: PlaygroundSendCommentActivity <br/>
 * 功能: TODO 发评论<br/>
 * 描述: <br/>
 * 时间: 2014年5月25日 下午8:16:52 <br/>
 * 
 * @author xujs
 * @version
 * @since JDK 1.6
 */
public class PlaygroundSendCommentActivity extends BaseActivity {

	private final String TAG = "PlaygroundSendCommentActivity";
	private static final int INPUT_MAX_LENGTH = 140;
	
	private AlertDialog dialog;
	private Context mContext;
	private Button commonHeadRightBtn;// 公用导航左边、右边按钮
	private ImageView commonheader_left_iv;
	private TextView commonheader_title_tv;
	private EditText sendContextET;// 写评论
	//private LinearLayout sendPictureListLL;// 添加图片控件
	private GridView sendPictureListGV;// 图片
	private LinearLayout sendFaceIconLL;
	private InputBottomBar mInputBottomBar;// 表情
	private int sendType;// 评论—0    回复评论—1  转发-2 ；默认为0
	private String userNameVal = "";//  回复评论用户名称
	private String timeline_id = "";//消息ID
	private String class_id = "";//班级ID
	private String teachingpoint_id = "";//教学点ID
	private String originaltimeline_id = "";//原始动态ID
	private int refreshlist_type = 0;//用于判别是否发送刷新列表广播0-不发送   1-发送
	private View loadingView;
	private ImageView tweetPubFootbarFace;// 表情图标
	
	private FragmentManager fragmentManager;
	private InputMethodManager imm;// 软键盘管理类
//	private GridView mGridView;
	private ViewPager tweetPubViewpager;
	private RelativeLayout tweetPubViewpagerRl;
	//private int type;// 1评论  2转发    3回复评论的评论
	
	/**
	 * 表示从哪里跳转尽量的 
	 * */
	private String mComeForm = "";

	@Override
	public int initResource() {
		return R.layout.playground_send_common_stat;
	}

	@Override
	public void initComponent() {
		mContext = this;
		// 软键盘管理类
		imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
		fragmentManager = getSupportFragmentManager();
		
		tweetPubFootbarFace = (ImageView) findViewById(R.id.tweet_pub_footbar_face);
		tweetPubViewpager = (ViewPager) findViewById(R.id.tweet_pub_viewpager);
		tweetPubViewpagerRl = (RelativeLayout) findViewById(R.id.tweet_pub_viewpager_rl);
		if(getIntent() != null){
			sendType = getIntent().getIntExtra("send_type", Constants.REPLY_TIMELINE);
			if(sendType == Constants.REPLY_COMMENT){
				userNameVal = getIntent().getStringExtra("userName");
			}
			timeline_id = getIntent().getStringExtra("timelineid");
			class_id = getIntent().getStringExtra("classid");
			teachingpoint_id = getIntent().getStringExtra("teachingpointid");
			originaltimeline_id = getIntent().getStringExtra("originaltimelineid");
			refreshlist_type = getIntent().getIntExtra("refreshlisttype", 0);
			mComeForm = getIntent().getStringExtra("comeformrefresh");
		}
		
//		commonHeadLeftBtn = (Button) findViewById(R.id.common_head_left_btn);
		commonHeadRightBtn = (Button) findViewById(R.id.common_head_right_btn);
		commonheader_left_iv = (ImageView) findViewById(R.id.commonheader_left_iv);
		commonheader_title_tv = (TextView) findViewById(R.id.commonheader_title_tv);

		sendContextET = (EditText) findViewById(R.id.send_context_et);

        ScrollView scrollView = (ScrollView) findViewById(R.id.send_context_sc);
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) scrollView.getLayoutParams();
        params.height = Helper.dip2px(this, 200);
        scrollView.setLayoutParams(params);
		
		//sendPictureListLL = (LinearLayout) findViewById(R.id.send_picture_list_ll);
		sendPictureListGV = (GridView) findViewById(R.id.send_picture_list_gv);
		sendFaceIconLL = (LinearLayout) findViewById(R.id.send_icon_ll);
		loadingView = findViewById(R.id.loading_layout);
		loadingView.setVisibility(View.GONE);
		
		showIMM();
	}

	@Override
	public void initData() {
		if(sendType == Constants.REPLY_COMMENT){
//			commonHeadLeftBtn.setText(getResources().getString(
//					R.string.playground_reply_comment_but));
			commonheader_title_tv.setText(getResources().getString(
					R.string.playground_reply_comment_but));
		} else if(sendType == Constants.REPLY_TIMELINE){
//			commonHeadLeftBtn.setText(getResources().getString(
//					R.string.playground_comment_but));
			commonheader_title_tv.setText(getResources().getString(
					R.string.playground_comment_but));
		}
		
		commonHeadRightBtn.setText(getResources().getString(
				R.string.playground_send_but));
		if(sendType == Constants.REPLY_COMMENT){
			sendContextET.setHint("回复" + userNameVal + ":");
		}else if(sendType == Constants.REPLY_TIMELINE){
			sendContextET.setHint(getResources().getString(
					R.string.playground_comment_et));
		}
		
		sendPictureListGV.setVisibility(View.VISIBLE);
		mInputBottomBar = new InputBottomBar(mContext,fragmentManager, sendFaceIconLL,sendContextET,INPUT_MAX_LENGTH);
	}

	@Override
	public void addListener() {
		commonheader_left_iv.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
			    if (sendContextET.getText().toString().trim().equals("")) {
    				// 点击返回按钮
    			    // 隐藏软键盘
    	            imm.hideSoftInputFromWindow(sendContextET.getWindowToken(), 0);
    				PlaygroundSendCommentActivity.this.finish();
			    } else {
                    showFinishDialog();
			    }
			}
		});
		commonHeadRightBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
			    if (PhoneUtil.checkNetworkEnable(mContext) == PhoneUtil.NETSTATE_DISABLE) {
			        Toast.makeText(mContext, getResources().getString(R.string.please_open_network), Toast.LENGTH_SHORT).show();
			        return;
			    }
			    
			    commonHeadRightBtn.setClickable(false);
				// 点击发送按钮
				loadingView.setVisibility(View.VISIBLE);
				String content = sendContextET.getText().toString();
				if (StringUtils.isEmpty(content)) {
					UIHelper.ToastMessage(PlaygroundSendCommentActivity.this, getResources().getString(R.string.coach_input_empty));
					loadingView.setVisibility(View.GONE);
					commonHeadRightBtn.setClickable(true);
					return;
				} 

				if(getCurrentFocus()!=null && getCurrentFocus().getWindowToken()!=null){
					// 隐藏表情选择框
					mInputBottomBar.getInputMethodManager()
							.hideSoftInputFromWindow(
									PlaygroundSendCommentActivity.this
											.getCurrentFocus().getWindowToken(),0);
				}
				

				pubComment(timeline_id, content, sendType);
			}
		});
		
		// 编辑器点击事件
		sendContextET.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				// 显示软键盘
				showIMM();
			}
		});

	}
	
	private void showIMM() {
		tweetPubFootbarFace.setTag(1);
		showOrHideIMM();
	}
	
	private void showOrHideIMM() {
		if (tweetPubFootbarFace.getTag() == null) {
			// 隐藏软键盘
			imm.hideSoftInputFromWindow(sendContextET.getWindowToken(), 0);
			// 显示表情
			showFace();
		} else {
			// 显示软键盘
			imm.showSoftInput(sendContextET, InputMethodManager.SHOW_FORCED);
			// 隐藏表情
			hideFace();
		}
	}
	
	private void showFace() {
		tweetPubFootbarFace.setImageResource(R.drawable.widget_bar_keyboard);
		tweetPubFootbarFace.setTag(1);
		tweetPubViewpagerRl.setVisibility(View.VISIBLE);
	}
	
	private void hideFace() {
		tweetPubFootbarFace.setImageResource(R.drawable.widget_bar_face);
		tweetPubFootbarFace.setTag(null);
		tweetPubViewpagerRl.setVisibility(View.GONE);
	}
	
	private void showFinishDialog() {
        Builder builder = new Builder(mContext);
        View view = LayoutInflater.from(mContext).inflate(
                R.layout.playground_list_dialog, null);
        // 初始化Dialog中的控件
        LinearLayout finishDialogLL = (LinearLayout) view
                .findViewById(R.id.playground_list_comment_ll);// 退出
        LinearLayout cancelDialogLL = (LinearLayout) view
                .findViewById(R.id.playground_person_info_ll);// 取消
        TextView quitDialogTV = (TextView) view
                .findViewById(R.id.playground_list_comment_tv);
        TextView continueDialogTV = (TextView) view
                .findViewById(R.id.playground_person_info_tv);
        quitDialogTV.setText(getResources().getString(
                R.string.playground_quit));
        continueDialogTV.setText(getResources().getString(R.string.playground_continue_edit));

        builder.setView(view);// 设置自定义布局view
        dialog = builder.show();
        finishDialogLL.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                imm.hideSoftInputFromWindow(sendContextET.getWindowToken(), 0);
                finish();
            }
        });

        cancelDialogLL.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
    }

	TmingHttp.RequestCallback<JsonParseObject<Object>> mRequestCallback = new TmingHttp.RequestCallback<JsonParseObject<Object>>() {

		@Override
		public void exception(Exception exception) {
		    commonHeadRightBtn.setClickable(true);
			loadingView.setVisibility(View.GONE);
			UIHelper.ToastMessage(mContext, getResources().getString(R.string.net_error_tip));
		}

		@Override
		public JsonParseObject<Object> onReqestSuccess(String respones) throws Exception {
			return JsonParseObject.parseJson(respones);
		}

		@Override
		public void success(JsonParseObject<Object> respones) {
			commonHeadRightBtn.setClickable(true);

			if (respones.getFlag() != 1) {
				loadingView.setVisibility(View.GONE);
				UIHelper.ToastMessage(mContext, respones.getErrMsg());
			} else {
				UIHelper.ToastMessage(mContext, getResources().getString(R.string.coach_send_success));
				if(refreshlist_type == 1){ 
					Intent mIntent = new Intent(Constants.SEND_COMMENT_SUCCESS_REFRESH_LIST_ACTION);
		            //发送广播
		            sendBroadcast(mIntent);  
				}else{
					setResult(Constants.SEND_COMMENT_SUCCESS_BACK_CEOD);
				}
				
				finish();
			}
		}
	};
 	
	/**
	 * 发布信息
	 * @param content
	 * @param typeVal "1" 评论  "2" 转发 3回复评论的评论
	 */
	private void pubComment(String timelineid, String content, int typeVal) {
		Comment comment = new Comment();
		if(TextUtils.isEmpty(timelineid)){
			timelineid = originaltimeline_id;
		}
		comment.setTimelineid(timelineid);
		comment.setUserid(App.getUserId());
		if(typeVal == Constants.REPLY_COMMENT) {
			Log.e(TAG, "pubComment 内容：= " + "@" + userNameVal + ":" +content); 
		    comment.setContent("@" + userNameVal + ":" +content);
		} else {
		    comment.setContent(content); 
		}
		comment.setType(String.valueOf(typeVal));
		comment.setClassid(class_id);
		comment.setTeachingpointid(teachingpoint_id);
		comment.setOriginaltimelineid(originaltimeline_id);
		String url = Constants.COMMENT_PUBLISH;
		TmingHttp.asyncRequest(url, comment.getMapData(), mRequestCallback);

	}

//	private void send() {
//
//	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
//			if (mInputBottomBar.getmHolder().mFace.getTag() == null) {
//				return false;
//			} else {
//				mInputBottomBar.hideFace();
//				return true;				
//			}
			if (mInputBottomBar.getmHolder().mFace.getTag() != null) { 
				mInputBottomBar.hideFace();
				return true;				
			} else {
			    if (!sendContextET.getText().toString().trim().equals("")) {
			        showFinishDialog();
			        return true;
			    }
			}
		}
		return super.onKeyDown(keyCode, event);
	}
}
