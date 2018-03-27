package com.nd.shapedexamproj.fragment;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;

import com.nd.shapedexamproj.App;
import com.nd.shapedexamproj.AuthorityManager;
import com.nd.shapedexamproj.R;
import com.nd.shapedexamproj.activity.SettingActivity;
import com.nd.shapedexamproj.activity.more.FeedbackActivity;
import com.nd.shapedexamproj.im.manager.UserManager;
import com.nd.shapedexamproj.util.Constants;
import com.nd.shapedexamproj.util.UIHelper;

/**
 * 更多界面
 * @author zll
 * create in 2014-3-30
 */
public class MoreFragment extends BaseFragment{
	
	private Button more_setting_btn ,more_feedback_btn, more_quit_btn;
	private ListView more_recommend_list ;
	private View more_fragment ;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		more_fragment = inflater.inflate(R.layout.more_activity, container, false);
		initComponent();
		initData();
		addListener();
		initAuthority();
		return more_fragment;
		
	}
	
	private void initComponent() {
		more_setting_btn = (Button) more_fragment.findViewById(R.id.more_setting_btn);
		more_feedback_btn = (Button)more_fragment.findViewById(R.id.more_feedback_btn);
		more_quit_btn = (Button) more_fragment.findViewById(R.id.more_quit_btn);
		
	}

	private void initData() {
		
	}
	
	private void initAuthority(){
		if (AuthorityManager.getInstance().isInnerAuthority()){
			more_quit_btn.setText(R.string.fjou_user_login);
			more_quit_btn.setTextColor(getResources().getColor(R.color.red_txt));
		}
	}
	

	private void addListener() {
		more_setting_btn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(App.getAppContext(),SettingActivity.class);
				startActivity(intent);
			}
		});
		
		more_feedback_btn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
//				
//				Intent intent = new Intent (App.getAppContext(),PersonalActivity.class);
//				startActivity(intent);

				Intent intent = new Intent (App.getAppContext(),FeedbackActivity.class);
				startActivity(intent);
				
			}
		});
		more_quit_btn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if (AuthorityManager.getInstance().isInnerAuthority()){
					quitOpt();
				} else {
					quitDialog();
				}
			}
		});
		
	}
	
	
	private void quitOpt(){
		SharedPreferences preferences = getActivity().getSharedPreferences(
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
		
		//AppManager.getAppManager().currentActivity().finish();
//		getBaseActivity().finish();
//		Intent intent = new Intent(App.getAppContext(),LoginActivity.class);
//		startActivity(intent);
//		AppManager.getAppManager().finishAllActivity();
		//finishOtherActivities();
		UIHelper.showLogin(getBaseActivity());
		getBaseActivity().finish();
	}
	
//	/**
//	 * 销毁其他的Activity
//	 */
//	private void finishOtherActivities(){
//		Log.e("size", "销毁activity个数：" +Constants.activity_list.size());
//		for(int i = 0;i < Constants.activity_list.size();i ++){
//			
//			Constants.activity_list.get(i).finish();
//			
//		}
//		
//	}
	
	
	/**
	 * 显示匿名用户提示框 by Abay Zhuang
	 */
	private AlertDialog dialog;
	private void quitDialog() {
		Builder builder = new Builder(getBaseActivity());
		View view = LayoutInflater.from(getBaseActivity()).inflate(
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
