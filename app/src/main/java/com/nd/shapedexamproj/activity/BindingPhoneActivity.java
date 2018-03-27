package com.nd.shapedexamproj.activity;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.nd.shapedexamproj.App;
import com.nd.shapedexamproj.R;
import com.nd.shapedexamproj.util.Constants;
import com.tming.common.net.TmingHttp;
import com.tming.common.net.TmingHttp.RequestCallback;
import com.tming.common.util.Log;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * 绑定手机页面
 * @author zll
 * create in 2014-3-31
 */
public class BindingPhoneActivity extends BaseActivity{
	
	private static String BINDING_STEP_1 = "binding_step_1";
	private static String BINDING_STEP_2 = "binding_step_2";
	
	private static String CHANGE_STEP_1 = "change_step_1";
	private static String CHANGE_STEP_2 = "change_step_2";
	private static String CHANGE_STEP_3 = "change_step_3";
	private static String CHANGE_STEP_4 = "change_step_4";
	
	
	/**
	 * 用来标识是绑定手机号还是更换手机号,值为：binding或change。当为binding时表示要绑定手机号。当为change时表示要更换手机号
	 */
	private String tag = "";		
	private String phone_num = "";	//已绑定的手机号
	private String et_phone_num = "";		//文本框中的电话号码
	private Button common_head_left_btn , common_head_right_btn ;
	private ImageView commonheader_left_iv;
	private TextView commonheader_title_tv;
	
	private TextView binding_phone_top_tv,binding_phone_tv2,view ,binding_phone_bottom_tv;
	private Button binding_phone_btn;
	private EditText binding_phone_et ;
	private String et_hint = "";		//文本框中的提示
	private SharedPreferences spf ;
	private Editor editor ;
	
	private static Map<String,Activity> activity_map = new HashMap<String ,Activity>();
	
	@Override
	public int initResource() {
		return R.layout.binding_phone_activity;
	}

	@Override
	public void initComponent() {
		tag = getIntent().getStringExtra("tag") ;
		et_phone_num = getIntent().getStringExtra("et_phone_num") == null ? "" : getIntent().getStringExtra("et_phone_num");
		
		spf = getSharedPreferences(Constants.SHARE_PREFERENCES_NAME, Context.MODE_PRIVATE);
		editor = spf.edit();
		
		phone_num = spf.getString(Constants.PHONE_NUM, "");//获取已绑定电话号码
		
		commonheader_left_iv = (ImageView) findViewById(R.id.commonheader_left_iv);
		commonheader_title_tv = (TextView) findViewById(R.id.commonheader_title_tv);
		commonheader_title_tv.setText(getResources().getString(R.string.bind_phone));
		
//		common_head_left_btn = (Button) findViewById(R.id.common_head_left_btn);
//		common_head_left_btn.setText(getResources().getString(R.string.bind_phone));
		findViewById(R.id.common_head_right_btn).setVisibility(View.GONE);
		//--------------------------------------------------------------------
		binding_phone_top_tv = (TextView) findViewById(R.id.binding_phone_top_tv);
		binding_phone_tv2 = (TextView) findViewById(R.id.binding_phone_tv2);
		binding_phone_bottom_tv = (TextView) findViewById(R.id.binding_phone_bottom_tv);
		
		binding_phone_et = (EditText) findViewById(R.id.binding_phone_et);
		binding_phone_btn = (Button) findViewById(R.id.binding_phone_btn);
		//--------------------------------------------------------------------
		if(phone_num.equals("")){				//未绑定号码的情况
			if(tag == null || tag.equals(BINDING_STEP_1)){
				//------------------------------------------------------------------
				binding_phone_top_tv.setText(getResources().getString(R.string.not_binding));
				binding_phone_tv2.setText(getResources().getString(R.string.sending_msg));
//				binding_phone_bottom_tv.setText(getResources().getString(R.string.binding_to_find_code));
				
				binding_phone_et.setHint(getResources().getString(R.string.binding_et_hint1));
				binding_phone_btn.setText(getResources().getString(R.string.next_step));
			} else if(tag.equals(BINDING_STEP_2) ){
				//---------------------------------------------------------------------
				binding_phone_top_tv.setText(getResources().getString(R.string.has_send_msg_to) + et_phone_num);
				countDown();		//倒计时
				binding_phone_bottom_tv.setVisibility(View.GONE);
				
				binding_phone_et.setHint(getResources().getString(R.string.binding_et_hint2));
				binding_phone_btn.setText(getResources().getString(R.string.finish));
			}
		} else {								//已绑定号码的情况
			if(tag == null || tag.equals(CHANGE_STEP_1)){
				//------------------------------------------------------------------------
				binding_phone_top_tv.setText(getResources().getString(R.string.recent_phone));
				binding_phone_tv2.setVisibility(View.GONE);
				binding_phone_bottom_tv.setText(getResources().getString(R.string.binding_to_find_code));
				
				binding_phone_et.setText(replaceIndex(phone_num, 3, 7));
				binding_phone_btn.setText(getResources().getString(R.string.change));
			} else if(tag.equals(CHANGE_STEP_2)){
				//------------------------------------------------------------------------
				binding_phone_top_tv.setVisibility(View.INVISIBLE);
				binding_phone_tv2.setVisibility(View.GONE);
				binding_phone_bottom_tv.setVisibility(View.GONE);
				
				binding_phone_et.setHint(getResources().getString(R.string.please_enter_code));
				binding_phone_btn.setText(getResources().getString(R.string.next_step));
			} else if(tag.equals(CHANGE_STEP_3)){
				//------------------------------------------------------------------------
				binding_phone_top_tv.setVisibility(View.INVISIBLE);
				binding_phone_tv2.setText(getResources().getString(R.string.sending_msg));
				binding_phone_bottom_tv.setVisibility(View.GONE);
				
				binding_phone_et.setHint(getResources().getString(R.string.binding_et_hint1));
				binding_phone_btn.setText(getResources().getString(R.string.next_step));
			} else if(tag.equals(CHANGE_STEP_4)){
				//---------------------------------------------------------------------
				binding_phone_top_tv.setText(getResources().getString(R.string.has_send_msg_to) + et_phone_num);
				countDown();		//倒计时
				binding_phone_bottom_tv.setVisibility(View.GONE);
				
				binding_phone_et.setHint(getResources().getString(R.string.binding_et_hint2));
				binding_phone_btn.setText(getResources().getString(R.string.finish));
			}
		}
		et_hint = binding_phone_et.getHint().toString();	//文本框中的提示
	}
	/**
	 * 倒计时
	 */
	int i = 5;
	private void countDown(){
		
		new Thread(){
			@Override
			public void run() {
				while(i >= 0){
					try {
						if(i == 0){
							App.getAppHandler().post(new Runnable(){
								@Override
								public void run() {
									binding_phone_tv2.setTextColor(getResources().getColor(R.color.title_green));
									binding_phone_tv2.setText("重新发送");
								}
							});
							break;
						}
						
						Log.e("count", i+"");
						App.getAppHandler().post(new Runnable(){
							@Override
							public void run() {
								binding_phone_tv2.setText(i+"秒后可重新发送");		//倒计时
								
							}
						});
						
						sleep(1000);
						i--;
						
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}.start();
	}
	
	/**
	 * 把电话号码中某一段替换成"*"
	 * @param str
	 * @param start
	 * @param end
	 * @return
	 */
	private String replaceIndex(String str,int start,int end){
		if(str == null || str.equals("")) return "";
		/*Log.e("替换", str.substring(0, start) + "****" + str.substring(end));*/
		return str.substring(0, start) + "****" + str.substring(end);
	}
	
	@Override
	public void initData() {
		
	}

	@Override
	public void addListener() {
		binding_phone_btn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				String text = binding_phone_et.getText().toString();
				
				
				String str = binding_phone_btn.getText().toString();
				if(phone_num.equals("")){		//未绑定
					if(str.equals("下一步")){
						//发送要绑定的手机号到服务端
						
						startActivityWithTag(BINDING_STEP_2,text);
					} else if(str.equals("完成")){
						//发送短信验证码到服务端
						
						Intent intent = new Intent (BindingPhoneActivity.this,BindingPhoneActivity.class);
						intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
						intent.putExtra("tag", CHANGE_STEP_1);
						intent.putExtra("et_phone_num", "");
						startActivity(intent);
						finish();
					}
				} else {			//已绑定
					if(str.equals("更换")){
						
						activity_map.put(CHANGE_STEP_1, BindingPhoneActivity.this);
						startActivityWithTag(CHANGE_STEP_2,null);
						
					} else if(str.equals("下一步") && et_hint.equals("请输入登录密码")){
						
						activity_map.put(CHANGE_STEP_2, BindingPhoneActivity.this);
						startActivityWithTag(CHANGE_STEP_3,null);
						
					} else if(str.equals("下一步") && et_hint.equals("请输入要绑定的手机号码")){
						
						activity_map.put(CHANGE_STEP_3, BindingPhoneActivity.this);
						startActivityWithTag(CHANGE_STEP_4,null);
						
					} else if(str.equals("完成")){
						
						finishAllActivities();
						Intent intent = new Intent (BindingPhoneActivity.this,BindingPhoneActivity.class);
						intent.putExtra("tag", CHANGE_STEP_1);
						intent.putExtra("et_phone_num", "");
						startActivity(intent);
						
						finish();
					}
				}
				
				
				
//				sendingMsg(text);
			}
		});
		commonheader_left_iv.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				finish();
			}
		});
		
	}
	/**
	 * 
	 * @param tag
	 */
	private void startActivityWithTag(String tag,String phone_num){
		Intent intent = new Intent (BindingPhoneActivity.this,BindingPhoneActivity.class);
		intent.putExtra("tag", tag);
		intent.putExtra("et_phone_num", phone_num == null ? "" : phone_num);
		startActivity(intent);
	}
	/**
	 * 销毁map中的Activity
	 */
	private void finishAllActivities(){
		Set<String> set = activity_map.keySet();
		Iterator<String> itr = set.iterator();
		while(itr.hasNext()){
			String key =  itr.next();
			activity_map.get(key).finish();
			activity_map.remove(key);
		}
	}
	
	/**
	 * 发送消息
	 * @param msg
	 */
	@SuppressWarnings("unchecked")
	private void sendingMsg(String msg){
		String url = "" ;
		Map<String, Object> map = new HashMap<String,Object>();
		
		TmingHttp.asyncRequest(url, map, new RequestCallback(){

			@Override
			public Object onReqestSuccess(String respones) throws Exception {
				return null;
			}

			@Override
			public void success(Object respones) {
				
			}

			@Override
			public void exception(Exception exception) {
				
			}
			
		});
		
	}
	
}
