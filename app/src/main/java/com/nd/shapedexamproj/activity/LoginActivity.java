/**
 * 
 */
package com.nd.shapedexamproj.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.Window.Callback;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import android.widget.RelativeLayout.LayoutParams;

import com.nd.shapedexamproj.App;
import com.nd.shapedexamproj.R;
import com.nd.shapedexamproj.adapter.CustomLoginOptionsAdapter;
import com.nd.shapedexamproj.im.manager.UserManager;
import com.nd.shapedexamproj.util.Constants;
import com.nd.shapedexamproj.util.PhpApiUtil;
import com.nd.shapedexamproj.util.UIHelper;
import com.nd.shapedexamproj.view.ResizeLinearLayout;
import com.tming.common.util.Helper;
import com.tming.common.util.Log;
import com.tming.common.util.PhoneUtil;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * @author Caiyx 2014-3-18
 */
public class LoginActivity extends BaseActivity implements Callback {
	private static final String TAG = LoginActivity.class.getSimpleName();
	/**
	 * 最大的用户记录值
	 * */
	private static final int MAX_LOGIN_ORIGINAL_VALUES_NUM = 3; 
	/**
	 * 删除选中的用户名数据
	 * */
	private static final int MSG_REMOVE_SELECT_ITEM_DATA = 52;  	
	private static final int MSG_GET_SELECT_ITEM_DATA = 51; 
	private static final int MSG_IM_LOGIN = 22;
	private static final int MSG_RESIZE = 100;
	private ResizeLinearLayout contentView;
	private ImageView loginIc;
	private ImageView pwdIc;
	private EditText usernameET; 
	
	private EditText pwdET;
	private LinearLayout loginLay;
	private TextView findPwdTV, touristTV,loadingTextView;
	private String account, userPwd;
	private String savedUserName, saveUserPwd;
	private Context context;
	private LinearLayout loading_layout;
	
	private RelativeLayout loginFindRl;
	 //PopupWindow对象  
	private PopupWindow selectPopupWindow = null;
	
	//下拉框依附组件  
	private RelativeLayout userNameRl; 
	//下拉框依附组件宽度，也将作为下拉框的宽度  
	private int pwidth; 
	//下拉箭头图片组件  
	private ImageView userNameSpinnerIv; 
	//展示所有下拉选项的ListView  
    private ListView listView = null;    
    private View loginwindow;
    
    /**
     * 用来保持上一次选择的用户名
     * */
    private String lastSelectUsearName = "";
    //是否初始化完成标志    
    private boolean flag = false;   
          
    /**
     * 用户登录下拉框适配器
     * */
    private CustomLoginOptionsAdapter mCustomLoginOptionsAdapter;

	private ArrayList<String> mOriginalValues = new ArrayList<String>();
	private SharedPreferences mLoginRecordSp;
	
	private ScrollView mScrollView;  
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.tming.openuniversity.activity.BaseActivity#initView()
	 */
	@Override
	public int initResource() {
		return R.layout.login;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.tming.openuniversity.activity.BaseActivity#initData()
	 */
	@Override
	public void initData() {		  
		mLoginRecordSp = getSharedPreferences(Constants.SHARE_PREFERENCES_USERNAME_FILE_NAME, Context.MODE_PRIVATE);
		getLoginData(); 	
		getLastLoginUserName();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.tming.openuniversity.activity.BaseActivity#initComponent()
	 */
	@Override
	public void initComponent() {
		this.context = this;
		contentView = (ResizeLinearLayout) findViewById(R.id.login_view_ll);		
		loginIc = (ImageView) findViewById(R.id.username_ic);
		pwdIc = (ImageView) findViewById(R.id.pwd_ic);
		// 初始化界面组件
		userNameRl = (RelativeLayout) findViewById(R.id.login_input_username_lay);
		userNameSpinnerIv = (ImageView) findViewById(R.id.username_spinner_ic);
		usernameET = (EditText) findViewById(R.id.login_username);
		pwdET = (EditText) findViewById(R.id.login_pwd);
		loginLay = (LinearLayout) findViewById(R.id.login_ll);
		loginFindRl = (RelativeLayout)findViewById(R.id.login_find_rl);
		findPwdTV = (TextView) findViewById(R.id.findpwd);
		touristTV = (TextView) findViewById(R.id.tourist_inner);
		loading_layout = (LinearLayout) findViewById(R.id.loading_layout);
        loadingTextView=(TextView) findViewById(R.id.loading_tv);
        loadingTextView.setText(R.string.login_loading_text);
		mScrollView = (ScrollView) findViewById(R.id.scroll);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.tming.openuniversity.activity.BaseActivity#addListener()
	 */
	@Override
	public void addListener() {
		contentView.setOnTouchListener(loginTouchListener);
		usernameET.setOnTouchListener(loginTouchListener);
		pwdET.setOnTouchListener(loginTouchListener);
		loginLay.setOnClickListener(loginClickListener);
		findPwdTV.setOnClickListener(loginClickListener);
		touristTV.setOnClickListener(loginClickListener);
		// 设置点击下拉箭头图片事件，点击弹出PopupWindow浮动下拉框
		userNameSpinnerIv.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (flag) {
					UIHelper.closeInputWindow(LoginActivity.this, v);
					// 显示PopupWindow窗口
					popupWindwShowing();
				}
			}
		});
		
		contentView.setOnResizeListener(new ResizeLinearLayout.OnResizeListener() {
            @Override
            public void OnResize(int w, int h, int oldw, int oldh) {
                Message msg = new Message(); 
                msg.what = MSG_RESIZE; 
                handler.sendMessage(msg); 
            }
        });
	}
	
	/**
	 * 软键盘弹出时，移动屏幕
	 * */
	private void scrollScreenToBottom(){ 
		/**
		 * 这里必须要给一个延迟，如果不加延迟则第一次没有效果，第二次点击才有效果
		 * */  
		handler.postDelayed(new Runnable() {

			@Override
			public void run() { 
				// 将ScrollView滚动到底
                // mScrollView.fullScroll(View.FOCUS_DOWN);
				mScrollView.scrollTo(0, 80);  
			}
		}, 100);     
	}
	  
	/**
	 * 没有在onCreate方法中调用initWedget()，而是在onWindowFocusChanged方法中调用，
	 * 是因为initWedget()中需要获取PopupWindow浮动下拉框依附的组件宽度，在onCreate方法中是无法获取到该宽度的
	 */
	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		while (!flag) {
			initWedget();
			flag = true;
		}
	}

	private OnTouchListener loginTouchListener = new OnTouchListener() {

		@Override
		public boolean onTouch(View view, MotionEvent event) {
			if (view.getId() == R.id.login_username) {
				if (view.isFocusable()) { 
					pwdIc.setImageResource(R.drawable.login_password); 
					loginIc.setImageResource(R.drawable.login_id_on);     
				}
			} else if (view.getId() == R.id.login_pwd) {
				if (view.isFocusable()) { 
					loginIc.setImageResource(R.drawable.login_id);
					pwdIc.setImageResource(R.drawable.login_password_on);   
				}
			} else {  
				loginIc.setImageResource(R.drawable.login_id);
				pwdIc.setImageResource(R.drawable.login_password);
				// 收起软键盘
				((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE))
						.hideSoftInputFromWindow(LoginActivity.this
								.getCurrentFocus().getWindowToken(),
								InputMethodManager.HIDE_NOT_ALWAYS);
			}
			return false;
		}
	};

	private OnClickListener loginClickListener = new OnClickListener() {

		@Override
		public void onClick(View view) {
			switch (view.getId()) {
			case R.id.login_ll:
				userLogin();
				break;
			case R.id.findpwd:
				break;
			case R.id.tourist_inner:
			    App.setsUserType(Constants.USER_TYPE_INNER);
			    App.setsUserId("");
			    mLoginRecordSp.edit().putString("userId", "");
			    mLoginRecordSp.edit().putInt("userType", -1);
			    mLoginRecordSp.edit().commit();
			    UIHelper.showMain(LoginActivity.this);
				break;
			default:
				break;
			}
		}
	};

	/**
	 * 登录处理
	 */
	private void userLogin() {
		account = usernameET.getText().toString().trim();
		userPwd = pwdET.getText().toString().trim();
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("account", account);
		map.put("passport", userPwd);

		if (null == account || "".equals(account)) {
			Helper.ToastUtil(this, getResources().getString(R.string.login_enter_username));
		} else if (null == userPwd || "".equals(userPwd)) {
			Helper.ToastUtil(this, getResources().getString(R.string.login_enter_password));
		} else {
            // 收起软键盘
            ((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE))
                    .hideSoftInputFromWindow(LoginActivity.this
                                    .getCurrentFocus().getWindowToken(),
                            InputMethodManager.HIDE_NOT_ALWAYS);
		    if (PhoneUtil.checkNetworkEnable(context) == PhoneUtil.NETSTATE_DISABLE) {
		        Helper.ToastUtil(this, getResources().getString(R.string.please_open_network));
		    } else {
    			loading_layout.setVisibility(View.VISIBLE);
    			new Thread(loginRunnable).start();
		    }
		}
	}

	private Runnable loginRunnable = new Runnable() {

		@Override
		public void run() {
			// 请求服务端进行登录
			HashMap<String, Object> map = new HashMap<String, Object>();
			map.put("username", account);
			map.put("passport", userPwd);

			JSONObject jobj = null;
			try {
				jobj = PhpApiUtil.getJSONObject(Constants.XK_USER_LOGIN, map);
				handler.obtainMessage(23).sendToTarget(); // 刷新界面
				if (null != jobj) {
					int code = jobj.getInt("_c");
					final String errorMsg = jobj.getString("_m");
					if (code == 0) {
						JSONObject dataJson = jobj.getJSONObject("user_info");
						String userId = dataJson.getString("uid");
						String stuId = dataJson.getString("stu_id");
						String userName = dataJson.getString("name");
						int userType = dataJson.getInt("type");

						/**
						 * 保持登录成功的数据
						 * */
						saveLoginData();
						saveLastLoginUserName();

						SharedPreferences preferences = getSharedPreferences(Constants.SHARE_PREFERENCES_NAME, Context.MODE_PRIVATE);
						Editor pfcEdit = preferences.edit();
						pfcEdit.putString("userId", userId);
						pfcEdit.putString("stuId", stuId);
						pfcEdit.putInt("userType", userType);
						pfcEdit.putString("userName", userName);
						pfcEdit.putInt("comefrom", 1);//来源于开大还是形考，0表示开大，1表示形考
						pfcEdit.commit();

						App.setsUserId(userId);
						App.setsStudentId(stuId);
						App.setsUserType(userType);

						Intent mainIntent = new Intent();
						mainIntent.setClass(context, MainActivity.class);
						startActivity(mainIntent);

						finish();
					} else {
						App.getAppHandler().post(new Runnable() {
							@Override
							public void run() {
								Toast.makeText(LoginActivity.this,errorMsg,Toast.LENGTH_SHORT).show();
							}
						});
					}
				} else {
                    handler.sendEmptyMessage(0);
				}
			} catch (Exception e) {
				e.printStackTrace();
                handler.sendEmptyMessage(0);
			}
		}
	};

	private Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			Bundle bundle = msg.getData();  
			switch (msg.what) {
			case 0:
				loading_layout.setVisibility(View.GONE);
				Helper.ToastUtil(context, getResources().getString(R.string.login_code_error));
				break;
			case 13:
				mScrollView.scrollTo(0, 40);
				break;
			case -3:
				loading_layout.setVisibility(View.GONE);
				Helper.ToastUtil(context, getResources().getString(R.string.please_open_network));
				break;
			case 23:
				loading_layout.setVisibility(View.GONE);
				break;
			case MSG_IM_LOGIN:
			  //启动IM登录
	            UserManager.userLogin(context, true);
	            break;
			case MSG_GET_SELECT_ITEM_DATA:
				//选中下拉项，下拉框消失  
			     int selIndex = bundle.getInt("selIndex");  
			     if(!lastSelectUsearName.equals(mOriginalValues.get(selIndex))){
			    	 pwdET.setText("");			    	 
			     }
			     usernameET.setText(mOriginalValues.get(selIndex));
				 lastSelectUsearName = mOriginalValues.get(selIndex);
			     dismiss();
				break;
			case MSG_REMOVE_SELECT_ITEM_DATA:
				boolean isDelUserName = false;
				// 移除下拉项数据
				int delIndex = bundle.getInt("delIndex");
			    if(lastSelectUsearName.equals(mOriginalValues.get(delIndex))){
			    	isDelUserName = true; 		    	 
			    }
			    removeHistory(mOriginalValues.get(delIndex));
				mOriginalValues.remove(delIndex);
				
				if(isDelUserName){
					 usernameET.setText("");
				}
				if(mOriginalValues.size() > 0){
					usernameET.setText(mOriginalValues.get(0));
					lastSelectUsearName = mOriginalValues.get(0);
				}
				// 刷新下拉列表 
				mCustomLoginOptionsAdapter.notifyDataSetChanged();
				break;
			case MSG_RESIZE:
			    scrollScreenToBottom();
			    break;
			default:
				break;

			}
		};
	};

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		setContentView(R.layout.login);
	}

	private long lastPressBackTime = 0;

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (System.currentTimeMillis() - lastPressBackTime < 3000) {
				System.exit(0);
				return true;
			}
			Helper.ToastUtil(this, getString(R.string.exit_hint));
			lastPressBackTime = System.currentTimeMillis();
			return true;
		}

		return super.onKeyDown(keyCode, event);
	}

	
	/**
	 * 获取用户名的历史关键字
	 */
	private void getLoginData() {
		String history = mLoginRecordSp.getString(Constants.SHARE_PREFERENCES_USERNAME_DATA, "");
		if (!history.trim().equals("")) {
			String[] results = history.split(",");
			for (int i = 0; i < results.length; i++) {
				mOriginalValues.add(results[i]);
			} 	 
		}
	}
	
	/**
	 *  获取上一次登录成功的用户名
	 * */
	private void getLastLoginUserName(){
		/**
		 * 如果有历史记录
		 * 获取最后一次成功登陆的账号  0-表示不是最后一次成功登陆的账号   1-最后一次成功登陆的账号
		 * */
		String lastLoginUsername = "";
		if(mOriginalValues.size() > 0){
			lastLoginUsername = mLoginRecordSp.getString(Constants.SHARE_PREFERENCES_LAST_LOGIN_USERNAME_DATA, "");
			if(!TextUtils.isEmpty(lastLoginUsername)){ 
				usernameET.setText(lastLoginUsername);
				lastSelectUsearName = lastLoginUsername;	
			}else{ 
				usernameET.setText(mOriginalValues.get(0));
				lastSelectUsearName = mOriginalValues.get(0);
			}  
		}
	}
	
	/**
	 * 保存登录用户名的内容
	 */
	private void saveLoginData() {    
		String msg = usernameET.getText().toString().trim();
		Log.e(TAG, "msg 用户名:=" + msg);
		if (msg != null && msg.trim().length() != 0) {
			String history = mLoginRecordSp.getString(Constants.SHARE_PREFERENCES_USERNAME_DATA, "");
			boolean contain = isLoginContain(msg);
			if (!contain) {
				// 保证最多只保存8次历史记录,每次都删除最老的（最前面），添加都添加在最后面
				if (mOriginalValues.size() >= MAX_LOGIN_ORIGINAL_VALUES_NUM) {
					/**
					 * 获取最后输入的两个联系人的号码字符串
					 * */
					int lastIndex = history.lastIndexOf(",") - 1; 
					String temp = history.substring(0, lastIndex);
					temp = history.substring(temp.lastIndexOf(",") + 1); 
					history = history.substring(0, history.length() - temp.length()); 
					
					mOriginalValues.remove(MAX_LOGIN_ORIGINAL_VALUES_NUM - 1); 
				}
				StringBuilder sb = new StringBuilder(history);
				sb.insert(0, msg.trim() + ",");
				mLoginRecordSp.edit().putString(Constants.SHARE_PREFERENCES_USERNAME_DATA, sb.toString()).commit(); 
				mOriginalValues.add(msg.trim());
				mCustomLoginOptionsAdapter.notifyDataSetInvalidated();
			}
			else{
				sortLoginRecord(msg, history); 
			}			
		}
	}
	 	
	/**
	 * 保存最后一次成功登陆的账号  
	 * 0-表示不是最后一次成功登陆的账号   1-最后一次成功登陆的账号
	 */
	private void saveLastLoginUserName(){
		//TODO 保存用户最后一次成功登陆的用户名（还是要保存最后一次选择的用户名）
		String msg = usernameET.getText().toString().trim(); 
		if(mOriginalValues != null && mOriginalValues.size() > 0){
			mLoginRecordSp.edit().putString(Constants.SHARE_PREFERENCES_LAST_LOGIN_USERNAME_DATA, msg).commit();
		}
	}
	
	/**
	 * 判断是否已经保存该字符串 
	 * @param msg 需要判断的字符串
	 * @return 不包含：false；包含：true
	 */
	private boolean isLoginContain(String msg){
		String history = mLoginRecordSp.getString(Constants.SHARE_PREFERENCES_USERNAME_DATA, "");
		if (!history.trim().equals("")) {
			String[] results = history.split(",");
			for (int i = 0; i < results.length; i++) {
				if(results[i].equals(msg.trim())){
					return true;
				}
			}
			return false;
		}else{
			return false;
		}
	}
	
	/**
	 * 
	 * @param removeMsg    要移除的字符串
	 */
	private void removeHistory(String removeMsg) {
		/**
		 * 在hisroy中要移除removemsg的话，不能直接移除，因为如果要用indexof找removeMsg的话，
		 * 很可能是在别的字符串中间，如要移除abc，有个aabcc； 如果要用removeMsg +
		 * ","这种方法的话，很有可能遇到如要删除，abc+","最后删除了aabc+","； 如果要用"," + removeMsg +
		 * ","的话，那么很有可能要删除的就在第一个位置，那么就无法找到。 所以经过如下方法，是最可靠的方法，虽然可能有点繁琐。
		 */
		String history = mLoginRecordSp.getString(Constants.SHARE_PREFERENCES_USERNAME_DATA, "");
		String[] msgs = history.split(",");
		history = "";
		for (int i = 0; i < msgs.length; i++) {
			if (msgs[i].equals(removeMsg)) {
				continue;
			} else {
				history = history + msgs[i] + ",";
			}
		}
		mLoginRecordSp.edit().putString(Constants.SHARE_PREFERENCES_USERNAME_DATA, history).commit();
	}
	
	/**
	 * 把包含在历史记录里面的记录重新排序，确保选中的记录会出现在第一次记录上。
	 * @param msg 需要判断的字符串 
	 * @param history 需要排序历史记录字符串 
	 */
	private void sortLoginRecord(String msg, String history){ 
		for (int i = 0; i < mOriginalValues.size(); i++) {
			if (mOriginalValues.get(i).equals(msg)) {
				/**
				 * 如果是最后一项就没必要再排序，否则就重新排序
				 * */
				if(i == 0){
					Log.e(TAG, "sortLoginRecord 11");
					return;
				}
				 
				// 移除下拉项数据  		  
				mOriginalValues.remove(i);				 
				break;
			} 
		}    
		
		StringBuilder sb = new StringBuilder(getNewSortLoginRecord(msg, history));
		mLoginRecordSp.edit().putString(Constants.SHARE_PREFERENCES_USERNAME_DATA, sb.toString()).commit();
		mOriginalValues.add(msg.trim());
		mCustomLoginOptionsAdapter.notifyDataSetInvalidated();
	}
	
	/**
	 * 把包含在历史记录里面的记录重的字符串从新新排序，按照最新的保存排列。
	 * @param msg 需要判断的字符串 
	 * @param history 需要排序历史记录字符串 
	 * */
	private String  getNewSortLoginRecord(String msg, String history){
		String msgTemp = msg.trim() + ",";
		int lastIndex = history.lastIndexOf(msgTemp) - 1; 
		String temp = history.substring(0, lastIndex); 
		if((history.length() - (temp.length() + msgTemp.length())) > 0){
			String buf = history.substring(temp.length() + msgTemp.length(), history.length() );
			temp += buf;			
		}
		temp = msgTemp + temp; 
		
		return temp;
	}
	
	/**
	 * 初始化界面控件
	 */
	private void initWedget() {
		// 初始化界面组件
		userNameRl = (RelativeLayout) findViewById(R.id.login_input_username_lay);
		// 获取下拉框依附的组件宽度
		int width = userNameRl.getWidth();
		pwidth = width; 

		// 初始化PopupWindow
		initPopuWindow(width);
	}
 
	/**
	 * 初始化PopupWindow
	 */
	private void initPopuWindow(int width) { 
		// PopupWindow浮动下拉框布局
		loginwindow = (View) this.getLayoutInflater().inflate(R.layout.login_username_spinner_options, null);
		listView = (ListView) loginwindow.findViewById(R.id.list);
 
		//设置自定义Adapter  
		mCustomLoginOptionsAdapter = new CustomLoginOptionsAdapter(LoginActivity.this, handler, MAX_LOGIN_ORIGINAL_VALUES_NUM, mOriginalValues, mLoginRecordSp);
	    listView.setAdapter(mCustomLoginOptionsAdapter);

		selectPopupWindow = new PopupWindow(loginwindow, pwidth, LayoutParams.WRAP_CONTENT, true);

		selectPopupWindow.setOutsideTouchable(true);

		/**
		 * 这一句是为了实现弹出PopupWindow后，当点击屏幕其他部分及Back键时PopupWindow会消失，
		 * 没有这一句则效果不能出来，但并不会影响背景 
		 * */ 
		selectPopupWindow.setBackgroundDrawable(new BitmapDrawable());
	}

	/**
	 * 显示PopupWindow窗口
	 * 
	 *
	 */
	public void popupWindwShowing() {
		/**
		 * 将selectPopupWindow作为parent的下拉框显示，并指定selectPopupWindow在Y方向上向上偏移3pix，
		 * 这是为了防止下拉框与文本框之间产生缝隙，影响界面美化 
		 * */ 
		selectPopupWindow.showAsDropDown(userNameRl, 0, -3);
	}

	/**
	 * PopupWindow消失
	 */
	public void dismiss() {
		if(selectPopupWindow != null && selectPopupWindow.isShowing()){
			selectPopupWindow.dismiss();
		}
	}

}
