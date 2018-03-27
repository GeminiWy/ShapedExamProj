package com.nd.shapedexamproj.activity.im;

import android.content.*;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
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
import com.nd.shapedexamproj.adapter.ChatRoomMsgAdapter;
import com.nd.shapedexamproj.db.ChatHistoryMsgDao;
import com.nd.shapedexamproj.db.ChatRoomMsgDao;
import com.nd.shapedexamproj.entity.ChatGroupMsgEntity;
import com.nd.shapedexamproj.entity.ChatHistoryEntity;
import com.nd.shapedexamproj.entity.ChatMsgEntity;
import com.nd.shapedexamproj.entity.GroupItemInfo;
import com.nd.shapedexamproj.entity.PersonPresenceEntity;
import com.nd.shapedexamproj.im.manager.IMManager;
import com.nd.shapedexamproj.im.model.SendGroupMsgModel;
import com.nd.shapedexamproj.im.packet.RoomPresence;
import com.nd.shapedexamproj.im.resource.IMConstants;
import com.nd.shapedexamproj.im.task.SendGroupMsgAsyncTask;
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
import com.tming.common.view.support.pulltorefresh.PullToRefreshBase;
import com.tming.common.view.support.pulltorefresh.PullToRefreshBase.OnRefreshListener2;
import com.tming.common.view.support.pulltorefresh.PullToRefreshListView;

import org.jivesoftware.smack.packet.Presence;

import java.util.*;

//import com.tming.openuniversity.task.SendChatMsgAsyncTask;
//import com.tming.openuniversity.util.UIHelper;
//import com.tming.openuniversity.util.Util;

/**
 * 
 * @ClassName: ChatGroupActivity
 * @Title:
 * @Description:IM聊天界面-群聊界面
 * @Author:XueWenJian
 * @Since:2014-4-29下午10:16:44
 * @Version:1.0
 */
public class ChatGroupActivity extends BaseActivity implements SendGroupMsgAsyncTask.OnSendMsgListener {

	private static final String TAG = "ChatGroupActivity";
	private static final int GET_USER_SELF = 0, GET_USER_OPP = 1;
	private static boolean IS_EMPTY_LOADING = false;
	private static final int INPUT_MAX_LENGTH = 300;
	
	private static final int BIGGER = 1; 
    private static final int SMALLER = 2; 
    private static final int MSG_RESIZE = 1;
	
	private Context mContext;
	private SharedPreferences spf = null;
    private Editor editor;
	
	private ResizeRelativeLayout chatRootView ;
	private PullToRefreshListView chatContentsPullListView;
	private ListView chatContentsListView;
	private EditText chatInputEditText;
	private Button chatSendImgBtn;
	private InputMethodManager inputmmgr;// 软键盘管理类
	private LinearLayout chat_edit_ll;
	private InputBottomBar mInputBottomBar;// 表情选择器
	private ImageView tweetPubFootbarFace;// 表情按钮
//	private GridView mGridView;
	private ViewPager tweetPubViewpager;
	private RelativeLayout tweetPubViewpagerRl;
	
	//private View actionbarView;
	private TextView chatActionBarTitile;
	private ImageView chatActionBarLeftBtn;
	private Button chatActionBarRightBtn;
	private TextView chatActionBarRightTV;
	
	private FragmentManager fragmentManager;
	private ChatRoomMsgAdapter chatMsgAdapter;
	private List<ChatGroupMsgEntity> chatMsglist;
	/*private String tag;
	private String name;
	private PersonalInfo oppUserInfo;*/
	private PersonalInfo selfUserInfo;
	private String mRoomJid;
	private String mRoomName;
	/*private GroupItemInfo mGroupItemInfo;*/
	private List<PersonPresenceEntity> oppPersonPresences;
	private List<PersonalInfo> oppPersonalInfos;
	private int page;
	private int pageSize = 20;
	private ChatRoomMsgDao chatMsgDao;
	private SendGroupMsgModel sendModel;
	private ChatHistoryMsgDao chatHistoryMsgDao;
	
	private int userInfoFlag;//标记是否请求用户数据完成

	@Override
	public int initResource() {
		return R.layout.chat_room_activity;
	}

	@Override
	public void initComponent() {
		mContext = this;
		
		spf = SharedPreferUtils.getSharedPreferences(App.getAppContext());
        editor = spf.edit();
        editor.putBoolean(Constants.IS_GROUPCHATACTIVITY_OPEN, true);
        editor.commit();
		
		if (AuthorityManager.getInstance().isInnerAuthority()) {
		    finish();
		}
		
		fragmentManager = getSupportFragmentManager();
		
		chatRootView = (ResizeRelativeLayout) findViewById(R.id.chat_room_root_rl);
		chatContentsPullListView = (PullToRefreshListView) findViewById(R.id.chat_content_lv);
		chatInputEditText = (EditText) findViewById(R.id.chat_text_et);
		chatSendImgBtn = (Button) findViewById(R.id.chat_send_imgbtn);
		inputmmgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

		chatContentsListView = chatContentsPullListView.getRefreshableView();

		chatActionBarTitile = (TextView) findViewById(R.id.commonheader_title_tv);
		chatActionBarLeftBtn = (ImageView)findViewById(R.id.commonheader_left_iv);
		chatActionBarRightTV = (TextView)findViewById(R.id.commonheader_more_tv);
		chatActionBarRightBtn = (Button)findViewById(R.id.commonheader_right_btn);

		/*IntentFilter intentFilter = new IntentFilter(IMConstants.ACTION_ROOM_RECEIVER_NAME);
		intentFilter.setPriority(2147483647);
		registerReceiver(myBroadcastReceiver, intentFilter);*/
		
		chatActionBarRightBtn.setVisibility(View.GONE);
		chatActionBarRightTV.setVisibility(View.VISIBLE);
		chatActionBarRightTV.setBackgroundResource(R.drawable.chat_user_icon);
		
		//表情软键盘
		tweetPubFootbarFace = (ImageView) findViewById(R.id.tweet_pub_footbar_face);
		tweetPubViewpager = (ViewPager) findViewById(R.id.tweet_pub_viewpager);
		tweetPubViewpagerRl = (RelativeLayout) findViewById(R.id.tweet_pub_viewpager_rl);
		chat_edit_ll = (LinearLayout) findViewById(R.id.chat_edit_ll);
		mInputBottomBar = new InputBottomBar(mContext,fragmentManager, chat_edit_ll, INPUT_MAX_LENGTH);
	}

	@Override
	public void initData() {
		mRoomJid = getIntent().getStringExtra("roomJid");
		mRoomName = getIntent().getStringExtra("roomName");
		
		Log.e(TAG, "RoomJid:" + mRoomJid);
		oppPersonalInfos = new ArrayList<PersonalInfo>();
		oppPersonPresences = new ArrayList<PersonPresenceEntity>();
		
		
		sendModel = new SendGroupMsgModel();

		chatMsgDao = ChatRoomMsgDao.getInstance(mContext);
		chatHistoryMsgDao = new ChatHistoryMsgDao(mContext);

		chatMsglist = new ArrayList<ChatGroupMsgEntity>();
		chatMsgAdapter = new ChatRoomMsgAdapter(mContext, chatMsglist, getSelfId());

		chatContentsPullListView.setAdapter(chatMsgAdapter);
		
		initUserData();
		
		loadHistoryChatMsg(0);
		if (!StringUtils.isEmpty(mRoomName)) {
		    chatActionBarTitile.setText("" + mRoomName);
		}
		//文本框内容为空时，发布按钮为不可用状态
		if("".equals(chatInputEditText.getText().toString())){
			sendClickable(false);
		}
	}
	
    /**
	 * 获取用户信息
	 */
	private void getUserInfo(String userid, final int flag) {
		String url = Constants.GET_USER_URL;
		Map<String ,Object> params = new HashMap<String,Object>();
		params.put("userid", userid);
		
		Log.i(TAG, url);
		
		TmingHttp.asyncRequest(url, params, new RequestCallback<Integer>() {
		    PersonalInfo mPersonalInfo = null;
			@Override
			public Integer onReqestSuccess(String respones) throws Exception {
				Log.i(TAG, respones);
				
				int result = Utils.jsonParsing(respones);
				mPersonalInfo = PersonalInfo.personalInfoJSONPasing(respones);
				
				if(Constants.SUCCESS_MSG ==  result) {
					if(flag == GET_USER_SELF){
						selfUserInfo = mPersonalInfo;
					} else if(flag == GET_USER_OPP){ 
					    /*chatMsgAdapter.setSelfUserInfo(selfUserInfo);*/
						oppPersonalInfos.add(mPersonalInfo);
					} else {
						result = -1;
					}
				}
				
				return result;
			}

			@Override
			public void success(Integer respones) {
				if(flag == GET_USER_OPP) {
				    if (StringUtils.isEmpty(mRoomName)) {
    				    mRoomName = mPersonalInfo.getUserName();
    				    chatActionBarTitile.setText("" + mRoomName);
				    }
					reduceUserInfoFlag();
				}
				
				if(respones != 1){
					return;
				} else {
					if(flag == GET_USER_OPP) {
						chatMsgAdapter.setOppPersonalInfos(oppPersonalInfos);
					}
					chatMsgAdapter.notifyDataSetChanged();
				}
			}

			@Override
			public void exception(Exception exception) {
				exception.printStackTrace();
			}
			
		});
	}


	private void initUserData() {
		//获取用户信息
		getUserInfo(App.getUserId(), GET_USER_SELF);
		
		//获得聊天室信息
		getGroupInfo();
	}
	/**
	 * <p>废弃，其他用户的信息头像，名字从数据库获取</P>
	 * @deprecated
	 */
	private void getGroupInfo() {
		
		//筛选群成员
		if(null != IMConstants.getRoomPersonInfoList()) {
			List<PersonPresenceEntity> entities = IMConstants.getRoomPersonInfoList();
			for(int index=0; index<entities.size(); index++) {
				PersonPresenceEntity entity =  entities.get(index);
				if (entity.getFromRoomJid().equals(mRoomJid)) {
					oppPersonPresences.add(entity);
					Log.i(TAG, entity.getUserJId());
					String userId = IMConstants.getUserIdByJID(entity.getUserJId());
					getUserInfo(userId, GET_USER_OPP);
					addUserInfoFlag();
				}
				
			}
			chatMsgAdapter.setOppPersonalPresences(oppPersonPresences);
		}
			
		
	}
	
	/**
	 * 加载本地历史聊天记录
	 * @param i 0为初始化加载；1为下拉刷新加载
	 */
	private void loadHistoryChatMsg(int i) {
		List<ChatGroupMsgEntity> chatMsgEntities = chatMsgDao.getChatMsgByTag(
				getChatTag(), page, pageSize);
		
		//当首次安装app，本地数据库为空时，则设置标识IS_EMPTY_LOADING，不装载历史记录
		if (chatMsgEntities.size() > 0) {
			page++;
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
		chatContentsPullListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				//inputmmgr.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
				inputmmgr.hideSoftInputFromWindow(ChatGroupActivity.this.getCurrentFocus().getWindowToken()
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
				//new GroupQuitAsyncTask().execute("");
				ChatGroupActivity.this.finish();

			}
		});
		chatActionBarRightTV.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				UIHelper.showIMRoomChatSettingActivity(ChatGroupActivity.this
						, mRoomJid, mRoomName, oppPersonPresences.size());
			}
		});

		chatContentsPullListView.setOnRefreshListener(new OnRefreshListener2<ListView>() {

			@Override
			public void onPullDownToRefresh(
					PullToRefreshBase<ListView> refreshView) {
				
				App.getAppHandler().postDelayed(new Runnable() {

					@Override
					public void run() {

						loadHistoryChatMsg(1);
						chatContentsPullListView.onRefreshComplete();

					}
				}, 0);
			}

			@Override
			public void onPullUpToRefresh(
					PullToRefreshBase<ListView> refreshView) {
				
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
			inputmmgr.hideSoftInputFromWindow(ChatGroupActivity.this.getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
			// 显示表情
			showFace();
		} else {
			// 显示软键盘
			//inputmmgr.showSoftInput(chat_edit_ll, InputMethodManager.SHOW_FORCED);
			inputmmgr.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
			// 隐藏表情
			hideFace();
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
			ChatGroupMsgEntity chatMsgEntity = new ChatGroupMsgEntity();
			chatMsgEntity.setText(senMsg);
			chatMsgEntity.setFromUserId(getSelfId());
			chatMsgEntity.setToId(getOpId());
			chatMsgEntity.setDate(getDate());
			chatMsgEntity.setGroupId(mRoomJid);
			chatMsgEntity.setState(ChatMsgEntity.STATE_SENDING);
			
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
	private void receiveMsg(ChatGroupMsgEntity chatMsgEntity) {
		
		boolean isSelf = chatMsgEntity.getFromUserId().equals(getSelfId());
		Log.e(TAG,"isSelf:"+isSelf);
		if(!isSelf) {
			chatMsglist.add(chatMsgEntity);
			chatMsgAdapter.notifyDataSetChanged();

		}

		chatContentsListView.smoothScrollToPosition(chatMsglist.size());

	}

	private void insertChatMsg(ChatGroupMsgEntity chatMsgEntity) {
		chatMsgEntity.setTag(getChatTag());
		chatMsgDao.insertChatMsg(chatMsgEntity);
		
		setChatHistory(chatMsgEntity);
	}

	private String getOpId() {
		return mRoomJid;
	}

	private String getSelfId() {
		return IMConstants.getLoginId();
	}


	private String getDate() {
	
		return DateUtils.getDate();
	}

	// 广播接收者 - 广播的接收
	private BroadcastReceiver myBroadcastReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			String toId = intent.getStringExtra("toId");
			String from = intent.getStringExtra("from");
			String msg = intent.getStringExtra("msg");
			String date = intent.getStringExtra("date");
			
			String fromRoomJid = intent.getStringExtra("fromRoomJid");
			String fromUserName = intent.getStringExtra("fromUserName");
			String fromUserImg = intent.getStringExtra("fromUserImg");
			
			if (StringUtils.isEmpty(date)) {
				date = getDate();
			}

			Log.e(TAG, "toId:" + toId + ";from:" + from +"; fromUserName :"+ fromUserName+ ";msg:" + msg
					+ ";date:" + date + "fromUserImg" + fromUserImg);

			boolean isInRoom = fromRoomJid.equals(getOpId());
			Log.e(TAG,"isInRoom:"+isInRoom);
			Log.e(TAG,"StringUtils.isEmpty(fromUserName):"+(StringUtils.isEmpty(fromUserName)));

			
			if(isInRoom &&  !StringUtils.isEmpty(fromUserName))
			{
				ChatGroupMsgEntity chatMsgEntity = new ChatGroupMsgEntity();
				chatMsgEntity.setText(msg);
				chatMsgEntity.setDate(date);
				chatMsgEntity.setGroupId(fromRoomJid);
				chatMsgEntity.setFromUserId(fromUserName);
				chatMsgEntity.setFormUserImgurl(fromUserImg);
				chatMsgEntity.setToId(toId);
				
				receiveMsg(chatMsgEntity);
				/*abortBroadcast();*///note by zll at 20141018.如果在此断掉，则msgFragment无法显示最近聊天项。
			}
			
		}

	};

	private void sendMessage(ChatGroupMsgEntity chatMsgEntity)
	{
		Log.e(TAG, "sendMessage begin;");
		//IM发送消息
		SendGroupMsgAsyncTask asyncTask = new SendGroupMsgAsyncTask(this, this, sendModel);
		asyncTask.setChatGroupMsgEntity(chatMsgEntity);
		asyncTask.execute();
		Log.e(TAG, "sendMessage end;");
		
	}
	
	public void sendMsgFinish(boolean result,ChatGroupMsgEntity chatMsgEntity)
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
		//chatMsgDao.updateChatMsgState(chatMsgEntity);
		
	}
	
	public void reSendMsgOnClick(View view)
	{
		Integer position =  (Integer)view.getTag();
		Log.e(TAG, "reSendMsgOnClick: "+position);
		if(position < chatMsglist.size())
		{
			ChatGroupMsgEntity chatMsgEntity = chatMsglist.get(position);
			chatMsgEntity.setState(ChatMsgEntity.STATE_SENDING);
			chatMsgAdapter.notifyDataSetChanged();
			sendMessage(chatMsgEntity);
		}
	}
	
	private String getChatTag() {
		String tag =  getSelfId() +"-"+ getOpId();
		
		return tag;
	}
	
	@Override
	public void onSendMsgFinish(Boolean result, ChatGroupMsgEntity chatMsgEntity) {
		sendMsgFinish(result, chatMsgEntity);
	}
	
	/**
	 * @deprecated
	  * @ClassName: GroupInfoAsyncTask
	  * @Description: 向Im请求组信息的异步任务
	  * @author XueWenJian
	  * @date 2014-6-10 下午8:04:00
	  *
	 */
	private class GroupInfoAsyncTask extends AsyncTask<String, Integer, Void> {

		@Override
		protected Void doInBackground(String... agrs) {
			SendGroupMsgModel model = new SendGroupMsgModel();

			RoomPresence presence = new RoomPresence(Presence.Type.available, "<x xmlns='http://jabber.org/protocol/muc'></x>");
			presence.setFrom(IMConstants.getUserJID());
			presence.setTo(mRoomJid+"/"+App.getUserId());
			
			IMManager.sendPresenceMessage(presence);
			return null;
			
		}

		@Override
		protected void onPostExecute(Void result) {
			Log.i(TAG, "GroupInfoAsyncTask onPostExecute");
			/*for(int index=0; index<result.size(); index++) {
				RosterEntry rosterEntry = result.get(index);
				Log.i(TAG, rosterEntry.getUser());
				String userId = IMConstants.getUserIdByJID(rosterEntry.getUser());
				oppPersonalId.add(userId);
				getUserInfo(userId, GET_USER_OPP);
			}*/
			super.onPostExecute(result);
		}

		@Override
		protected void onPreExecute() {

			super.onPreExecute();
		}

	}
	
	/**
	 * @deprecated
	 * <p> 退出群登录</p>
	 */
	private class GroupQuitAsyncTask extends AsyncTask<String, Integer, Void> {

		@Override
		protected Void doInBackground(String... arg0) {
			SendGroupMsgModel model = new SendGroupMsgModel();

			RoomPresence presence = new RoomPresence(Presence.Type.unavailable, "");
			presence.setFrom(IMConstants.getUserJID());
			presence.setTo(mRoomJid+"/"+ App.getUserId());
			
			IMManager.sendPresenceMessage(presence);
			return null;
		}
		
		@Override
		protected void onPostExecute(Void result) {
			Log.i(TAG, "GroupQuitAsyncTask onPostExecute");
			/*for(int index=0; index<result.size(); index++) {
				RosterEntry rosterEntry = result.get(index);
				Log.i(TAG, rosterEntry.getUser());
				String userId = IMConstants.getUserIdByJID(rosterEntry.getUser());
				oppPersonalId.add(userId);
				getUserInfo(userId, GET_USER_OPP);
			}*/
			super.onPostExecute(result);
		}

		@Override
		protected void onPreExecute() {

			super.onPreExecute();
		}
		
	}

	
	public class SimplePresence extends Presence {
		 private String positionElement;
		 
		public SimplePresence(Type arg0,String element) {
			super(arg0);
			setPositionElement("<x xmlns='http://jabber.org/protocol/muc'></x>");
			
		}
		
	
	    //get and set
	    public String getPositionElement() {
	        return positionElement;
	    }
	
	
	    public void setPositionElement(String positionElement) {
	        this.positionElement = positionElement;
	    }
	    
	    public String getChildElementXML() {
	        return getPositionElement();
	    }
	    
	    
	}
	
	@Override
	protected void onResume() {
		IntentFilter intentFilter = new IntentFilter(IMConstants.ACTION_ROOM_RECEIVER_SECOND);
		intentFilter.setPriority(2147483647);
		registerReceiver(myBroadcastReceiver, intentFilter);
		super.onResume();
	}
	
	@Override
	protected void onStop()
	{
		Log.e(TAG, "-------------onStop----------");
		try {
			unregisterReceiver(myBroadcastReceiver);
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		
	    super.onStop();
	}
	
	@Override
	protected void onDestroy() {
	    editor.putBoolean(Constants.IS_GROUPCHATACTIVITY_OPEN, false);
        editor.commit();
	    super.onDestroy();
	}
	
	private void addUserInfoFlag()
	{
		userInfoFlag++;
	}
	
	private void reduceUserInfoFlag()
	{
		userInfoFlag--;
		Log.d(TAG, "userInfoFlag: "+userInfoFlag);
		if(0 == userInfoFlag) {
			chatMsgAdapter.notifyDataSetChanged();
		}
	}
	
	private void setChatHistory(ChatGroupMsgEntity chatMsgEntity) {
		ChatHistoryEntity chatHistoryEntity = new ChatHistoryEntity();
		chatHistoryEntity.setChatType(ChatHistoryEntity.CHAT_TYPE_ROOM);
		
		GroupItemInfo groupItemInfo = IMConstants.findGroupItemInfo(mRoomJid);
		if (null != groupItemInfo) {
			chatHistoryEntity.setName(groupItemInfo.getName());
		} else {
			chatHistoryEntity.setName(mRoomJid);
		}
		
		chatHistoryEntity.setOppId(mRoomJid);
		chatHistoryEntity.setUserId(IMConstants.getLoginId());
		chatHistoryEntity.setUpdateTimeStamp(DateUtils.getTime(chatMsgEntity.getDate()));
		chatHistoryEntity.setTag(getChatTag());
		
		chatHistoryMsgDao.replaceChatHistotyMsg(chatHistoryEntity);
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
