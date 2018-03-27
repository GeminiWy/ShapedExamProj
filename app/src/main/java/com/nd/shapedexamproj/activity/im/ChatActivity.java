package com.nd.shapedexamproj.activity.im;

import android.content.*;
import android.content.SharedPreferences.Editor;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import android.widget.AdapterView.OnItemClickListener;

import com.nd.shapedexamproj.App;
import com.nd.shapedexamproj.AuthorityManager;
import com.nd.shapedexamproj.R;
import com.nd.shapedexamproj.activity.BaseActivity;
import com.nd.shapedexamproj.adapter.ChatMsgAdapter;
import com.nd.shapedexamproj.db.ChatHistoryMsgDao;
import com.nd.shapedexamproj.db.ChatMsgDao;
import com.nd.shapedexamproj.db.ChatUserInfoDao;
import com.nd.shapedexamproj.entity.ChatHistoryEntity;
import com.nd.shapedexamproj.entity.ChatMsgEntity;
import com.nd.shapedexamproj.im.manager.IMManager;
import com.nd.shapedexamproj.im.model.SendModel;
import com.nd.shapedexamproj.im.resource.IMConstants;
import com.nd.shapedexamproj.im.task.SendChatMsgAsyncTask;
import com.nd.shapedexamproj.model.my.PersonalInfo;
import com.nd.shapedexamproj.util.Constants;
import com.nd.shapedexamproj.util.DateUtils;
import com.nd.shapedexamproj.util.SharedPreferUtils;
import com.nd.shapedexamproj.util.StringUtils;
import com.nd.shapedexamproj.util.UIHelper;
import com.nd.shapedexamproj.util.Utils;
import com.nd.shapedexamproj.view.InputBottomBar;
import com.nd.shapedexamproj.view.ResizeRelativeLayout;
import com.tming.common.net.TmingHttp;
import com.tming.common.net.TmingHttp.RequestCallback;
import com.tming.common.util.Log;
import com.tming.common.view.RefreshableListView;
import com.tming.common.view.RefreshableListView.OnRefreshListener;

import java.util.*;

/**
 * 
 * @ClassName: ChatActivity
 * @Title:
 * @Description:IM聊天界面-私聊界面
 * @Author:XueWenJian
 * @Since:2014-4-29下午10:16:44
 * @Version:1.0
 */
public class ChatActivity extends BaseActivity implements SendChatMsgAsyncTask.OnSendMsgListener {

	private static final String TAG = ChatActivity.class.getSimpleName();
	private static final int GET_USER_SELF = 0, GET_USER_OPP = 1;
	private static boolean IS_EMPTY_LOADING = false;
	private static final int INPUT_MAX_LENGTH = 300;
	
	private static final int BIGGER = 1; 
    private static final int SMALLER = 2; 
    private static final int MSG_RESIZE = 1;
	
	public final static int LIST_REFRESH = 1, LIST_LOAD_MORE = 2;
	private Context mContext;
	private SharedPreferences spf = null;
	private Editor editor;
	
	private ResizeRelativeLayout chatRootView ;
	private RefreshableListView chatContentsListView;
	private EditText chatInputEditText;
	private Button chatSendImgBtn;
	private InputMethodManager inputmmgr;// 软键盘管理类
	private LinearLayout chat_edit_ll;
	private InputBottomBar mInputBottomBar;// 表情选择器
	private ImageView tweetPubFootbarFace;// 表情按钮
//	private GridView mGridView;
	private ViewPager tweetPubViewpager;
	private RelativeLayout tweetPubViewpagerRl;
	
	private FragmentManager fragmentManager;
	//private View actionbarView;
	private TextView chatActionBarTitile;
	private ImageView chatActionBarLeftBtn;
	private Button chatActionBarRightBtn;
	private TextView chatActionBarRightTV;

	private ChatMsgAdapter chatMsgAdapter;
	private List<ChatMsgEntity> chatMsglist;
	private PersonalInfo oppUserInfo;
	private PersonalInfo selfUserInfo;
	private int page = 0;
	private int pageSize = 20;
	private ChatMsgDao chatMsgDao;
	private SendModel sendModel;
	private ChatHistoryMsgDao chatHistoryMsgDao;
	private ChatUserInfoDao chatUserInfoDao;
	
	private String toUserId, toUserName;

	@Override
	public int initResource() {
		return R.layout.chat_activity;
	}

	@Override
	public void initComponent() {
		mContext = this;
		
		spf = SharedPreferUtils.getSharedPreferences(App.getAppContext());
		editor = spf.edit();
		editor.putBoolean(Constants.IS_PERSONALCHATACTIVITY_OPEN, true);
		editor.commit();
		
		if (AuthorityManager.getInstance().isInnerAuthority()) {
            finish();
        }
		
		chatRootView = (ResizeRelativeLayout) findViewById(R.id.chat_root_rl);
		chatContentsListView = (RefreshableListView) findViewById(R.id.chat_content_lv);
		chatInputEditText = (EditText) findViewById(R.id.chat_text_et);
		chatSendImgBtn = (Button) findViewById(R.id.chat_send_imgbtn);
		// 软键盘管理类
		inputmmgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		fragmentManager = getSupportFragmentManager();
		
		chatActionBarTitile = (TextView) findViewById(R.id.commonheader_title_tv);
		chatActionBarLeftBtn = (ImageView)findViewById(R.id.commonheader_left_iv);
		chatActionBarRightTV = (TextView)findViewById(R.id.commonheader_more_tv);
		chatActionBarRightBtn = (Button)findViewById(R.id.commonheader_right_btn);

		chatContentsListView.setFootVisible(false);

		/*IntentFilter intentFilter = new IntentFilter(IMConstants.ACTION_SINGLE_RECEIVER_NAME);
		intentFilter.setPriority(2147483647);
		registerReceiver(myBroadcastReceiver, intentFilter);*/
		
		chatActionBarRightBtn.setVisibility(View.GONE);
		chatActionBarRightTV.setVisibility(View.VISIBLE);
		chatActionBarRightTV.setBackgroundResource(R.drawable.chat_userinfo_icon);
		
		//表情软键盘
		tweetPubFootbarFace = (ImageView) findViewById(R.id.tweet_pub_footbar_face);
		tweetPubViewpager = (ViewPager) findViewById(R.id.tweet_pub_viewpager);
		tweetPubViewpagerRl = (RelativeLayout) findViewById(R.id.tweet_pub_viewpager_rl);
		chat_edit_ll = (LinearLayout) findViewById(R.id.chat_edit_ll);
		mInputBottomBar = new InputBottomBar(mContext,fragmentManager, chat_edit_ll, INPUT_MAX_LENGTH);
		
		IntentFilter intentFilter = new IntentFilter(IMConstants.ACTION_SINGLE_RECEIVER_SECOND);
        intentFilter.setPriority(2147483647);
        registerReceiver(myBroadcastReceiver, intentFilter);
	}

	@Override
	public void initData() {
		toUserId = getIntent().getStringExtra("toUserid");
		toUserName = getIntent().getStringExtra("toUserName");
		
		if (!StringUtils.isEmpty(toUserName)) {
		    chatActionBarTitile.setText(toUserName);
		}
		sendModel = new SendModel();

		chatMsgDao = ChatMsgDao.getInstance(mContext);
		chatHistoryMsgDao = new ChatHistoryMsgDao(mContext);
		chatUserInfoDao = ChatUserInfoDao.getInstance(App.getAppContext());
		
		chatMsglist = new ArrayList<ChatMsgEntity>();
		chatMsgAdapter = new ChatMsgAdapter(mContext, chatMsglist);
		chatContentsListView.setAdapter(chatMsgAdapter);
		
		initUserData();
		loadHistoryChatMsg(0);
		
		//文本框内容为空时，发布按钮为不可用状态
		if("".equals(chatInputEditText.getText().toString())){
			sendClickable(false);
		}
	}
	
    /**
	 * 获取用户信息
	 */
	private void getUserInfo(String userid, final int flag) {
		addRefreshFlag();
		// 获取用户信息  如果本地数据库存在该用户信息，则不请求网络
        PersonalInfo mPersonalInfo = chatUserInfoDao.getChatUserInfo(userid);
        if (mPersonalInfo != null) {
            oppUserInfo = mPersonalInfo;
            if (StringUtils.isEmpty(toUserName)) {
                chatActionBarTitile.setText(mPersonalInfo.getUserName());
            }
            chatMsgAdapter.setOppUserInfo(mPersonalInfo);
            reduceRefreshFlag();
            return;
        }
		
		String url = Constants.GET_USER_URL;
		Map<String ,Object> params = new HashMap<String,Object>();
		params.put("userid", userid);
		
		Log.i(TAG, url);
		
		TmingHttp.asyncRequest(url, params, new RequestCallback<Integer>() {

			@Override
			public Integer onReqestSuccess(String respones) throws Exception {
				PersonalInfo mPersonalInfo = null;
				int result = Utils.jsonParsing(respones);
				mPersonalInfo = PersonalInfo.personalInfoJSONPasing(respones);
				
				if(flag == GET_USER_OPP){
					oppUserInfo = mPersonalInfo;
				} else {
					result = -1;
				}
				return result;
			}

			@Override
			public void success(Integer respones) {
				if(respones != 1){
					return;
				} else {
					if(oppUserInfo != null && oppUserInfo.getUserName() != null){
					    if (StringUtils.isEmpty(toUserName)) {
					        chatActionBarTitile.setText(oppUserInfo.getUserName());
					    }
						chatMsgAdapter.setOppUserInfo(oppUserInfo);
						reduceRefreshFlag();
					} 
					
				}
			}

			@Override
			public void exception(Exception exception) {
				exception.printStackTrace();
			}
			
		});
	}
	
	private int refreshFlag;

	private void addRefreshFlag() {
		refreshFlag++;
	}

	private void reduceRefreshFlag() {
		refreshFlag--;
		if (refreshFlag <= 0) {
			chatMsgAdapter.notifyDataSetChanged();
		}
	}
	
	private void initUserData() {
		//获取用户信息
		/*getUserInfo(Constants.USER_ID, GET_USER_SELF);*/
		getUserInfo(toUserId, GET_USER_OPP);
	}

	/**
	 * 加载本地历史聊天记录
	 * @param i 0为初始化加载；1为下拉刷新加载
	 */
	private void loadHistoryChatMsg(int i) {
		List<ChatMsgEntity> chatMsgEntities = chatMsgDao.getChatMsgByTag(
				getChatTag(), page, pageSize);
		
		//当首次安装app，本地数据库为空时，则设置标识IS_EMPTY_LOADING，不装载历史记录
		if (chatMsgEntities.size() > 0) {
			page ++;
			IS_EMPTY_LOADING = (i == 0) ? false : IS_EMPTY_LOADING;
		} else {
			IS_EMPTY_LOADING = true;
		}

		if(!IS_EMPTY_LOADING){
			Collections.reverse(chatMsgEntities);
			chatMsgEntities.addAll(chatMsglist);
			chatMsglist.clear();
			chatMsglist.addAll(chatMsgEntities);
			chatMsgAdapter.notifyDataSetChanged();
		}

	}
	
	private Handler mHandler = new Handler () {
	  public void handleMessage(Message msg) {
	      switch (msg.what) { 
	            case MSG_RESIZE:  
                    chatContentsListView.smoothScrollToPosition(chatMsglist.size());
	                break; 
	 
	            default: 
	                break; 
	            } 
	            super.handleMessage(msg); 
	  };  
	};
	
	@Override
	public void addListener() {
	    /*ResizeUtil.assistActivity(this);*/
	    chatRootView.setOnResizeListener(new ResizeRelativeLayout.OnResizeListener() {
            
            @Override
            public void OnResize(int w, int h, int oldw, int oldh) {
                int change = BIGGER; 
                if (h < oldh) { 
                    change = SMALLER; 
                } 
                                 
                Message msg = new Message(); 
                msg.what = 1; 
                msg.arg1 = change; 
                mHandler.sendMessage(msg); 
            }
        });
	    
		chatContentsListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				//inputmmgr.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
				inputmmgr.hideSoftInputFromWindow(ChatActivity.this.getCurrentFocus().getWindowToken()
						,InputMethodManager.HIDE_NOT_ALWAYS);
				if (tweetPubViewpagerRl.isShown()) {
				    hideFace();
				    
				}
			}

		});
		
		chatSendImgBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				//判断网络连接状态给出提示
				if (null == IMManager.getConnection() || !IMManager.getConnection().isConnected()) {
					Toast.makeText(mContext, R.string.msg_net_error, Toast.LENGTH_LONG).show();
				} else {
					sendMsg();
				}
			}
		});
		chatInputEditText.addTextChangedListener(new TextWatcher() {
			private CharSequence mTemp;
			
			@Override
			public void afterTextChanged(Editable arg0) {
				//文本框内容为空时，发布按钮为不可用状态
				if (mTemp.length() > 0) {
					sendClickable(true);
				} else {
					sendClickable(false);
				}
			}

			@Override
			public void beforeTextChanged(CharSequence arg0, int arg1,
					int arg2, int arg3) {

			}

			@Override
			public void onTextChanged(CharSequence text, int arg1, int arg2,
					int arg3) {
				mTemp = text;
			}

		});

		chatActionBarLeftBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				ChatActivity.this.finish();

			}
		});
		chatActionBarRightTV.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				if (null != oppUserInfo) {
					UIHelper.showIMSingleChatSettingActivity(ChatActivity.this, oppUserInfo.getUserId()
							, oppUserInfo.getUserName(), oppUserInfo.getPhotoUrl());
				}
				
			}
		});

		chatContentsListView.setonRefreshListener(new OnRefreshListener() {
			@Override
			public void onRefresh() {
				App.getAppHandler().postDelayed(new Runnable() {

					@Override
					public void run() {
						Log.e(TAG, "onRefresh");
						loadHistoryChatMsg(1);
						chatContentsListView.onRefreshComplete(LIST_REFRESH);

					}
				}, 0);
			}

			@Override
			public void onLoadMore() {
				Log.e(TAG, "onLoadMore");
				// chatContentsListView.onRefreshComplete(LIST_LOAD_MORE);
			}
		});
		
		chatInputEditText.setOnTouchListener(new OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
            	// 隐藏表情选择器
				hideFace();
                return false;
            }
        });
		
		tweetPubFootbarFace.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// 表情选择器、软键盘切换
				showOrHideIMM();
			}
		});
	}
	
	/**
	 * 表情选择器、软键盘切换
	 */
	private void showOrHideIMM() {
		if (tweetPubViewpagerRl.getVisibility() == View.GONE) {
			// 隐藏软键盘
			inputmmgr.hideSoftInputFromWindow(ChatActivity.this.getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
			// 显示表情
			showFace();
		} else {
			// 显示软键盘
			//inputmmgr.showSoftInput(chat_edit_ll, InputMethodManager.SHOW_FORCED);
			inputmmgr.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
			// 隐藏表情
			hideFace();
			// 输入框获取地址
			chatInputEditText.requestFocus();
		}
	}
	
	/**
	 * 显示表情
	 */
	private void showFace() {
		tweetPubFootbarFace.setImageResource(R.drawable.widget_bar_keyboard);
		App.getAppHandler().postDelayed(new Runnable() {
            
            @Override
            public void run() {
                tweetPubViewpagerRl.setVisibility(View.VISIBLE);
            }
        }, 150);
	}
	
	/**
	 * 隐藏表情
	 */
	private void hideFace() {
		tweetPubFootbarFace.setImageResource(R.drawable.widget_bar_face);
		tweetPubViewpagerRl.setVisibility(View.GONE);
	}
	
	/**
	 * 设置发送按钮是否可用
	 * @param able
	 */
	private void sendClickable(boolean able) {
		if (able) {
			chatSendImgBtn.setClickable(able);
			chatSendImgBtn.setBackgroundDrawable(getResources().getDrawable(R.drawable.common_btn_selector));
		} else {
			chatSendImgBtn.setClickable(able);
			chatSendImgBtn.setBackgroundDrawable(getResources().getDrawable(R.drawable.btn_unused_gray));
		}
	}

	/**
	 * 发送消息
	 */

	private void sendMsg() {
		String senMsg = chatInputEditText.getText().toString();

		if (!senMsg.trim().equals("")) {
			ChatMsgEntity chatMsgEntity = new ChatMsgEntity();
			chatMsgEntity.setText(senMsg);
			chatMsgEntity.setFormId(getSelfId());
			chatMsgEntity.setToId(getOpId());
			chatMsgEntity.setDate(getDate());
			chatMsgEntity.setState(ChatMsgEntity.STATE_SENDING);
			chatMsgEntity.setMsgId(StringUtils.getRandomString(12));

			chatMsglist.add(chatMsgEntity);
			chatMsgAdapter.notifyDataSetChanged();

			chatContentsListView.smoothScrollToPosition(chatMsglist.size());
			insertChatMsg(chatMsgEntity);

			sendMessage(chatMsgEntity);
		} else {
		    Toast.makeText(mContext, R.string.msg_can_not_be_null, Toast.LENGTH_LONG).show();
		}
		chatInputEditText.setText(null);
	}

	/**
	 * 接受数据
	 */
	private void receiveMsg(ChatMsgEntity chatMsgEntity) {
		if (!chatMsglist.contains(chatMsgEntity)) {
			chatMsglist.add(chatMsgEntity);
			chatMsgAdapter.notifyDataSetChanged();

			chatContentsListView.smoothScrollToPosition(chatMsglist.size());
		}
	}

	private void insertChatMsg(ChatMsgEntity chatMsgEntity) {
		chatMsgEntity.setTag(getChatTag());
		chatMsgDao.insertChatMsg(chatMsgEntity);
		
		setChatHistory(chatMsgEntity);
	}

	private String getOpId() {
		return toUserId;
	}

	private String getSelfId() {
		return App.getUserId();
	}

	private String getDate() {
	
		return DateUtils.getDate();
	}

	// 广播接收者 - 广播的接收
	private BroadcastReceiver myBroadcastReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			String msgId = intent.getStringExtra("msgId");
			String toId = intent.getStringExtra("toId");
			String fromId = intent.getStringExtra("fromId");
			String msg = intent.getStringExtra("msg");
			String date = intent.getStringExtra("date");

			if (StringUtils.isEmpty(date)) {
				date = getDate();
			}

			Log.e(TAG, "toId:" + toId + ";fromId:" + fromId + ";msg:" + msg
					+ ";date:" + date);

			if(fromId.equals(getOpId()))
			{
				ChatMsgEntity chatMsgEntity = new ChatMsgEntity();
				chatMsgEntity.setId(msgId);
				chatMsgEntity.setText(msg);
				chatMsgEntity.setDate(date);
				chatMsgEntity.setFormId(fromId);
				chatMsgEntity.setToId(toId);
				
				receiveMsg(chatMsgEntity);
				/*abortBroadcast();*///note by zll at 20141018.如果在此断掉，则msgFragment无法显示最近聊天项。
			}
			
		}

	};

	private void sendMessage(ChatMsgEntity chatMsgEntity) {
		Log.e(TAG, "sendMessage begin;");
		//IM发送消息
		SendChatMsgAsyncTask asyncTask = new SendChatMsgAsyncTask(this, this, sendModel);
		asyncTask.setChatMsgEntity(chatMsgEntity);
		asyncTask.execute();
		Log.e(TAG, "sendMessage end;");
	}
	
	public void sendMsgFinish(boolean result,ChatMsgEntity chatMsgEntity)
	{
		if(result)
		{
			chatMsgEntity.setState(ChatMsgEntity.STATE_NORMAL);
		}
		else
		{
			//UIHelper.ToastMessage(context, R.string.error_network);
			chatMsgEntity.setState(ChatMsgEntity.STATE_ERROR);
		}
		
		//更新UI
		if(null != chatMsgAdapter)
		{
			chatMsgAdapter.notifyDataSetChanged();
		}
		
		//更新数据库
		chatMsgDao.updateChatMsgState(chatMsgEntity);
		
	}
	
	public void reSendMsgOnClick(View view)
	{
		Integer position =  (Integer)view.getTag();
		Log.e(TAG, "reSendMsgOnClick: "+position);
		if(position < chatMsglist.size())
		{
			ChatMsgEntity chatMsgEntity = chatMsglist.get(position);
			chatMsgEntity.setState(ChatMsgEntity.STATE_SENDING);
			chatMsgAdapter.notifyDataSetChanged();
			sendMessage(chatMsgEntity);
		}
	}
	
	private String getChatTag() {
		String tag =  getSelfId() +"-"+ getOpId();
		
		return tag;
	}

	private void setChatHistory(ChatMsgEntity chatMsgEntity) {
		ChatHistoryEntity chatHistoryEntity = new ChatHistoryEntity();
		chatHistoryEntity.setChatType(ChatHistoryEntity.CHAT_TYPE_SINGLE);
		chatHistoryEntity.setName(toUserId);
		chatHistoryEntity.setOppId(toUserId);
		chatHistoryEntity.setUserId(IMConstants.getLoginId());
		chatHistoryEntity.setUpdateTimeStamp(DateUtils.getTime(chatMsgEntity.getDate()));
		chatHistoryEntity.setTag(getChatTag());
		chatHistoryMsgDao.replaceChatHistotyMsg(chatHistoryEntity);
	}
	
	@Override
	public void onSendMsgFinish(Boolean result, ChatMsgEntity chatMsgEntity) {
		sendMsgFinish(result, chatMsgEntity);
	}
	
	@Override
	protected void onStop()
	{
		Log.e(TAG, "-------------onStop----------");
	    try {
			unregisterReceiver(myBroadcastReceiver);
		} catch (Exception e) {
			e.printStackTrace();
		}
	    super.onStop();
	}
	
	@Override
	protected void onDestroy() {
	    editor.putBoolean(Constants.IS_PERSONALCHATACTIVITY_OPEN, false);
	    editor.commit();
	    super.onDestroy();
	}
	
	@Override
	protected void onResume() {
		
		super.onResume();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		Log.d(TAG, "onActivityResult; requestCode:"+ requestCode+"; resultCode:"+resultCode);
		if (RESULT_OK == resultCode) {
			chatMsglist.clear();
			chatMsgAdapter.notifyDataSetChanged();
		}
	}
	
	@Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (tweetPubViewpagerRl.isShown()) {
                hideFace();
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }
	
}
