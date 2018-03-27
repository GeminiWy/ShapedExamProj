package com.nd.shapedexamproj.activity;


import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.*;
import android.widget.CompoundButton.OnCheckedChangeListener;

import com.nd.shapedexamproj.App;
import com.nd.shapedexamproj.AuthorityManager;
import com.nd.shapedexamproj.R;
import com.nd.shapedexamproj.activity.more.BlacklistActivity;
import com.nd.shapedexamproj.activity.more.FeedbackActivity;
import com.nd.shapedexamproj.adapter.VersionCheckUpdateAdapter;
import com.nd.shapedexamproj.im.manager.UserManager;
import com.nd.shapedexamproj.util.Constants;
import com.nd.shapedexamproj.util.SharedPreferUtils;
import com.nd.shapedexamproj.util.UIHelper;
import com.nd.shapedexamproj.util.Utils;
import com.tming.common.AppManager;
import com.tming.common.cache.image.ImageCacheTool;
import com.tming.common.cache.net.TmingCacheHttp;
import com.tming.common.util.Helper;
import com.tming.common.util.Log;
import com.tming.common.util.VersionUpdate;
/**
 * 设置界面
 * @author zll
 * create in 2014-3-25
 */
public class SettingActivity extends BaseActivity{
	
	private Button setting_bind_phone_btn ,setting_quit_btn;
	private ImageView commonheader_left_iv;
	private TextView commonheader_title_tv;
	
	private LinearLayout settingNotificationLL;
	private RelativeLayout setting_remind_rl,setting_sys_annouce_rl,
				setting_group_talk_rl,setting_personal_talk_rl,
				setting_black_list_rl,setting_clear_cache_rl,setting_help_rl,setting_about_rl ,setting_feedback_ll;
	private TextView setting_version_update_tv ;
	private ImageView setting_wifi_img, setting_notification_img  ;
	
	private CheckBox setting_remind_cb,setting_sys_annouce_cb,setting_group_talk_cb,setting_personal_talk_cb;
	
	private TextView settings_progress_tv;
	private ProgressBar settings_progressBar ;
	
	SharedPreferences spf = null;
	Editor editor = null;
	
	AudioManager mAudioManager = null; 
	
	private TmingCacheHttp cacheHttp ;
	
	//清除缓存----------------------
	private long cacheSize = 0;// 缓存大小
	private long tempCacheSize;
	private ImageCacheTool imgCacheTool = null;
	private Thread clearThread = null;
	//---------------------------
	@Override
	public int initResource() {
		return R.layout.setting_activity;
	}


	@Override
	public void initComponent() {
		mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE); 
		
		spf = SharedPreferUtils.getSharedPreferences(this);
		editor = spf.edit();
		
//		common_head_left_btn = (Button) findViewById(R.id.common_head_left_btn);
//		common_head_left_btn.setText(getResources().getString(R.string.setting));
		commonheader_left_iv = (ImageView) findViewById(R.id.commonheader_left_iv);
		commonheader_title_tv = (TextView) findViewById(R.id.commonheader_title_tv);
		commonheader_title_tv.setText(getResources().getString(R.string.setting));
		findViewById(R.id.common_head_right_btn).setVisibility(View.GONE);
		
		setting_bind_phone_btn = (Button) findViewById(R.id.setting_bind_phone_btn);
		setting_quit_btn = (Button) findViewById(R.id.setting_quit_btn);
//		setting_wifi_rl = (RelativeLayout) findViewById(R.id.setting_wifi_rl);
//		setting_all_notification_rl = (RelativeLayout) findViewById(R.id.setting_all_notification_rl);
		settingNotificationLL = (LinearLayout) findViewById(R.id.setting_notification_ll);
		if (AuthorityManager.getInstance().isInnerAuthority()){
		    settingNotificationLL.setVisibility(View.GONE);
		    setting_quit_btn.setVisibility(View.GONE);
		}
		
		setting_remind_rl = (RelativeLayout) findViewById(R.id.setting_remind_rl);
		setting_sys_annouce_rl = (RelativeLayout) findViewById(R.id.setting_sys_annouce_rl);
		setting_group_talk_rl = (RelativeLayout) findViewById(R.id.setting_group_talk_rl);
		setting_personal_talk_rl = (RelativeLayout) findViewById(R.id.setting_personal_talk_rl);
		setting_black_list_rl = (RelativeLayout) findViewById(R.id.setting_black_list_rl);
		setting_clear_cache_rl = (RelativeLayout) findViewById(R.id.setting_clear_cache_rl);
		setting_version_update_tv = (TextView) findViewById(R.id.setting_version_update_tv);
		if(Constants.HAS_NEW_VERSION ){
			setting_version_update_tv.setText(getResources().getString(R.string.has_new_version));
		} else {
			setting_version_update_tv.setText(getResources().getString(R.string.already_latest));
		}
		
		setting_help_rl = (RelativeLayout) findViewById(R.id.setting_help_rl);
		setting_about_rl = (RelativeLayout) findViewById(R.id.setting_about_rl);
		setting_feedback_ll = (RelativeLayout) findViewById(R.id.setting_feedback_ll);
		
		//非wifi网络时进行提醒
		setting_wifi_img = (ImageView) findViewById(R.id.setting_wifi_img);
		//通知栏提示
		setting_notification_img = (ImageView) findViewById(R.id.setting_notification_img);
		//复选框
		setting_remind_cb = (CheckBox) findViewById(R.id.setting_remind_cb);
		setting_remind_cb.setChecked(true);
		setting_sys_annouce_cb = (CheckBox) findViewById(R.id.setting_sys_annouce_cb);
		setting_sys_annouce_cb.setChecked(true);
		setting_group_talk_cb = (CheckBox) findViewById(R.id.setting_group_talk_cb);
		setting_group_talk_cb.setChecked(true);
		setting_personal_talk_cb = (CheckBox) findViewById(R.id.setting_personal_talk_cb);
		setting_personal_talk_cb.setChecked(true);
		
		cacheHttp = TmingCacheHttp.getInstance(this);
		imgCacheTool = ImageCacheTool.getInstance(SettingActivity.this);
		//清除缓存
		settings_progress_tv = (TextView) findViewById(R.id.settings_progress_tv);
		settings_progress_tv.setText(getCacheDataSize().toString().trim());
		
		settings_progressBar = (ProgressBar) findViewById(R.id.settings_progressBar);
		if (settings_progress_tv.getText().toString().equals("0.00B")) {
			settings_progressBar.setProgress(0);
		}
		
	}
	
	@Override
	public void initData() {
		
		if(spf.getBoolean(Constants.IS_NOTIFY_WITHOUT_WIFI, true) )
		{
			setting_wifi_img.setImageResource(R.drawable.setting_check_select);
		} else
		{
			
			setting_wifi_img.setImageResource(R.drawable.setting_check);
		}
		
		if(spf.getBoolean(Constants.IS_NOTIFICATION_OPEN, true) )
		{
			setting_notification_img.setImageResource(R.drawable.setting_check_select);
			loadNoticeState();
		} 
		else 
		{	setNoticeEnabled(false);
			setting_notification_img.setImageResource(R.drawable.setting_check);
		}
		
	}
	@Override
	public void addListener() {
		
		commonheader_left_iv.setOnClickListener(new ItemClickListener());
		setting_bind_phone_btn.setOnClickListener(new ItemClickListener());
//		setting_wifi_rl.setOnClickListener(new ItemClickListener());
//		setting_all_notification_rl.setOnClickListener(new ItemClickListener());
		setting_remind_rl.setOnClickListener(new ItemClickListener());
		setting_sys_annouce_rl.setOnClickListener(new ItemClickListener());
		setting_group_talk_rl.setOnClickListener(new ItemClickListener());
		setting_personal_talk_rl.setOnClickListener(new ItemClickListener());
		setting_black_list_rl.setOnClickListener(new ItemClickListener());
		setting_clear_cache_rl.setOnClickListener(new ItemClickListener());
		setting_version_update_tv.setOnClickListener(new ItemClickListener());
		setting_help_rl.setOnClickListener(new ItemClickListener());
		setting_about_rl.setOnClickListener(new ItemClickListener());
		setting_feedback_ll.setOnClickListener(new ItemClickListener());
		setting_quit_btn.setOnClickListener(new ItemClickListener());
		
		setting_wifi_img.setOnClickListener(new ItemClickListener());
		setting_notification_img.setOnClickListener(new ItemClickListener());
		//复选框监听
		setting_remind_cb.setOnCheckedChangeListener(new CbOnCheckedChangeListener(Constants.IS_REMIND_OPEN));	//是否打开提醒
		setting_sys_annouce_cb.setOnCheckedChangeListener(new CbOnCheckedChangeListener(Constants.IS_ANNOUNCEMENT_OPEN));//是否打开系统公告
		setting_group_talk_cb.setOnCheckedChangeListener(new CbOnCheckedChangeListener(Constants.IS_GROUPCHAT_OPEN));//是否打开群聊
		setting_personal_talk_cb.setOnCheckedChangeListener(new CbOnCheckedChangeListener(Constants.IS_PERSONALCHAT_OPEN));//是否打开私聊功能
		//清除缓存
		settings_progress_tv.setOnClickListener(new ItemClickListener());
	}
	/**
	 * 设置复选框是否可选
	 * @param state
	 */
	private void setNoticeEnabled(boolean state){
		
		setting_remind_cb.setEnabled(state);
		setting_sys_annouce_cb.setEnabled(state);
		setting_group_talk_cb.setEnabled(state);
		setting_personal_talk_cb.setEnabled(state);
		
		setting_remind_cb.setChecked(state);
		setting_sys_annouce_cb.setChecked(state);
		setting_group_talk_cb.setChecked(state);
		setting_personal_talk_cb.setChecked(state);
		
		
	}
	
	private void loadNoticeState()
	{
		setting_remind_cb.setChecked(spf.getBoolean(Constants.IS_REMIND_OPEN, true));
		setting_sys_annouce_cb.setChecked(spf.getBoolean(Constants.IS_ANNOUNCEMENT_OPEN, true));
		setting_group_talk_cb.setChecked(spf.getBoolean(Constants.IS_GROUPCHAT_OPEN, true));
		setting_personal_talk_cb.setChecked(spf.getBoolean(Constants.IS_PERSONALCHAT_OPEN, true));
	}
	
	/**
	 * 复选框选中监听
	 * @author zll
	 * create in 2014-4-1
	 */
	private class CbOnCheckedChangeListener implements OnCheckedChangeListener{
		
		private String key ;
		public CbOnCheckedChangeListener(String key){
			this.key = key; 
		}
		
		@Override
		public void onCheckedChanged(CompoundButton buttonView,
				boolean isChecked) {
			if(isChecked){
				editor.putBoolean(key,true);
				Log.e("cb", key+"打开");
			} else {
				editor.putBoolean(key,false);
				Log.e("cb", key+"关闭");
			}
				editor.commit();
				
		}
		
	}
	
	private class ItemClickListener implements OnClickListener{

		@Override
		public void onClick(View v) {
			switch(v.getId()){
			case R.id.commonheader_left_iv:
				finish();
				break;
			case R.id.setting_bind_phone_btn:		//绑定手机号
				Log.e("wc", "绑定手机");
				Intent intent = new Intent(SettingActivity.this,BindingPhoneActivity.class);
				startActivity(intent);
				break;
			case R.id.setting_remind_rl:
				
				break;
			case R.id.setting_sys_annouce_rl:
				
				break;
			case R.id.setting_group_talk_rl:
				
				break;
			case R.id.setting_personal_talk_rl:
				
				break;
			case R.id.setting_black_list_rl:
				System.out.println("黑名单。。。。。");
				Intent blacklistIntent = new Intent(SettingActivity.this,BlacklistActivity.class);
				startActivity(blacklistIntent);
				break;
			case R.id.settings_progress_tv:
			    if (AuthorityManager.getInstance().isInnerAuthority()) {
			        AuthorityManager.getInstance().showInnerDialog(SettingActivity.this);
			        return;
			    }
			    
				if (settings_progress_tv.getText().toString().equals("0.00B")) {
					return;
				}
				// 文件清理
				if (clearThread == null || !clearThread.isAlive()) {
					clearThread = new Thread() {
						@Override
						public void run() {
							super.run();
							clearCache();
						}
					};
					clearThread.start();
				} else {
					Helper.ToastUtil(SettingActivity.this, "正在清除，请稍等...");
				}
				break;
			case R.id.setting_version_update_tv:		//检查更新
				/*View vWait = LayoutInflater.from(SettingActivity.this).inflate(
						R.layout.common_dialog_layout_wait, null);*/
				editor.putBoolean(Constants.IS_NOLONGER_NOTIFY, false);
				editor.commit();
				VersionUpdate.checkHasVersionUpdate(Constants.VERSION_CHECK, new VersionCheckUpdateAdapter(SettingActivity.this));
				break;
			case R.id.setting_help_rl:
				Intent guide_intent = new Intent(SettingActivity.this,GuideActivity.class);
				guide_intent.putExtra("flag", 1);
				startActivity(guide_intent);
				break;
			case R.id.setting_about_rl:
				Intent ab_intent = new Intent(SettingActivity.this,AboutActivity.class);
				startActivity(ab_intent);
				
				break;
			case R.id.setting_wifi_img:				//是否在非wifi网络提醒
				if(spf.getBoolean(Constants.IS_NOTIFY_WITHOUT_WIFI, true) ){
					App.cleanWifiNotification();
					setting_wifi_img.setImageResource(R.drawable.setting_check);
					editor.putBoolean(Constants.IS_NOTIFY_WITHOUT_WIFI,false);
				} else {
					setting_wifi_img.setImageResource(R.drawable.setting_check_select);
					editor.putBoolean(Constants.IS_NOTIFY_WITHOUT_WIFI,true);
					
				}
				editor.commit();
				App.onNetStateChange();
				break;
			case R.id.setting_notification_img:			//是否打开通知栏
				if(spf.getBoolean(Constants.IS_NOTIFICATION_OPEN, true) ){
					setting_notification_img.setImageResource(R.drawable.setting_check);
					SettingActivity.this.setNoticeEnabled(false);
					editor.putBoolean(Constants.IS_NOTIFICATION_OPEN,false);
				} else {
					setting_notification_img.setImageResource(R.drawable.setting_check_select);
					SettingActivity.this.setNoticeEnabled(true);
					editor.putBoolean(Constants.IS_NOTIFICATION_OPEN,true);
				}
				editor.commit();
				break;
			case R.id.setting_feedback_ll:
			    Intent feedBackIntent = new Intent (App.getAppContext(),FeedbackActivity.class);
                startActivity(feedBackIntent);
                break;
			case R.id.setting_quit_btn:
			    /*if (AuthorityManager.getInstance().isInnerAuthority()){//修改为去掉对话框 added by zll 2014.09.27
                    quitOpt();
                } else {
                    quitDialog();
                }*/
			    quitOpt();
			    break;
			}
		}
		
	}
	
	public void clearCache() {
		imgCacheTool.clearAllCache(handler);
		/*File dbFile = new File(OffLineDataDBOperator.getDb_path());
		if (dbFile.exists()) {
			Message message = handler.obtainMessage();
			message.what = ImageCacheTool.DELETE_FILE;
			Bundle bundle = new Bundle();
			bundle.putString("file", dbFile.getPath());
			bundle.putLong("fileSize", dbFile.length());
														
			message.setData(bundle);
			handler.sendMessage(message);
			dbFile.delete();
		}*/
	}
	
	private String getCacheDataSize() {
		try {
			cacheSize = imgCacheTool.getCachedSize();
			// 离线数据库内容
			/*boolean sdCardExist = Environment.getExternalStorageState().equals(
					android.os.Environment.MEDIA_MOUNTED); // 判断SD卡是否存在
			if (!sdCardExist) {
				cacheSize += new File(OffLineDataDBOperator.getDb_path())
						.length();
			}*/
			tempCacheSize = cacheSize;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return Utils.FormetFileSize(cacheSize);
	}
	
	
	
	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case ImageCacheTool.DELETE_FILE:
				Bundle bundle = msg.getData();
				long mSize = bundle.getLong("fileSize");
				cacheSize -= mSize;
				// System.out.println("mSize:"+mSize+"  cacheSize:"+cacheSize+"  tempCacheSize:"+tempCacheSize);
				settings_progress_tv.setText(Utils.FormetFileSize(cacheSize));
				if (cacheSize > 0) {
					settings_progressBar.setProgress((int) (cacheSize * 100 / tempCacheSize));
				} else {
					settings_progressBar.setProgress(0);
					Helper.ToastUtil(SettingActivity.this,getResources().getText(
									R.string.cache_cleared).toString());
				}
				break;
			default:
				break;
			}
		}
	};
	
	/**
	 * 设置系统静音
	 */
	private void setSystemSoundOff(){
		mAudioManager.setMicrophoneMute(true);
	}
	/**
	 * 取消静音
	 */
	private void setSystemSoundOn(){
		mAudioManager.setMicrophoneMute(false);
	}
	
	/**
	 * 保存显示的字体大小到sharedprefrences
	 * @param size
	 */
	protected void setFontSize(int size) {
		editor.putInt(Constants.SHARE_PREFERENCES_NAME, size);
		editor.commit();
	}
	
	private void quitOpt(){
        SharedPreferences preferences = getSharedPreferences(
                Constants.SHARE_PREFERENCES_NAME,
                Context.MODE_PRIVATE);
        //清理IM连接
        UserManager.disconnectAccount();
        
        Editor pfcEdit = preferences.edit();
        pfcEdit.putString("userId", "");
		pfcEdit.putString("stuId", "");
        pfcEdit.putInt("userType", -1);
        pfcEdit.commit();
        App.setsUserType(-1);
        App.clearUserId();
        //跳转到登录页，并且把所有其他界面销毁
        
        UIHelper.showLogin(this);
        AppManager.getAppManager().finishAllActivity();
        finish();
    }
    
    
    /**
     * 显示匿名用户提示框 by Abay Zhuang
     */
    private AlertDialog dialog;
    private void quitDialog() {
        Builder builder = new Builder(this);
        View view = LayoutInflater.from(this).inflate(
                R.layout.common_dialog_quit, null);
        // 初始化Dialog中的控件
        Button negative = (Button) view.findViewById(R.id.negative_button);// 评论
        Button positive = (Button) view.findViewById(R.id.positive_button);// 查看个人信息
        builder.setView(view);// 设置自定义布局view
        dialog = builder.show();
        negative.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        positive.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                dialog.dismiss();
                quitOpt();
                
            }
        });

    }
	
}
