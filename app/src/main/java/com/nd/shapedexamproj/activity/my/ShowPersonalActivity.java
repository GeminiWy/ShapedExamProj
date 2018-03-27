package com.nd.shapedexamproj.activity.my;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.nd.shapedexamproj.App;
import com.nd.shapedexamproj.R;
import com.nd.shapedexamproj.activity.BaseActivity;
import com.nd.shapedexamproj.model.my.PersonalInfo;
import com.nd.shapedexamproj.util.Constants;
import com.nd.shapedexamproj.util.StringUtils;
import com.nd.shapedexamproj.util.UIHelper;
import com.tming.common.cache.net.TmingCacheHttp;
import com.tming.common.cache.net.TmingCacheHttp.RequestWithCacheCallBack;
import com.tming.common.util.Log;

import java.util.HashMap;
import java.util.Map;

/**
 * 
 * 个人信息展示(暂时不用，直接跳到EditPersonalActivity)
 * 
 * @author Linlg
 * 
 *         Create on 2014-3-28
 */
public class ShowPersonalActivity extends BaseActivity {
	
	private final static String TAG = "ShowPersonalActivity";
	
	private Context mContext;
	
	private TmingCacheHttp cacheHttp;
	private TextView sexTv,ageTv, starTv, phoneTv, workTv, companyTv, schoolTv, hobbyTv, explanationTv;
	
	private ImageView myinfo_sex_img,myinfo_age_img,myinfo_star_img,myinfo_phone_img,
	myinfo_work_img,myinfo_company_img ,myinfo_school_img,myinfo_hobby_img,myinfo_explantion_img;
	
	private boolean isEditable;		//是否可编辑
	private PersonalInfo mPersonalInfo;

	@Override
	public int initResource() {
		return R.layout.my_info;
	}

	@Override
	public void initComponent() {
		mContext = this;
		cacheHttp = TmingCacheHttp.getInstance();
		initView();
	}

	@Override
	public void initData() {
		requestPersonal();
	}

	@Override
	public void addListener() {
		IntentFilter filter = new IntentFilter();
		filter.addAction("com.tming.openuniversity.activity.my.showpersonalactivity");
		registerReceiver(receiver, filter);
	}
	
	@Override
	protected void onDestroy() {
		unregisterReceiver(receiver);
		super.onDestroy();
		
	}
	/**
	 * 初始化视图
	 */
	private void initView() {
		
		// 编辑按钮
		Button editBtn = (Button) findViewById(R.id.myinfo_head_layout).findViewById(
				R.id.commonheader_right_btn);
		editBtn.setText(R.string.edit);
		editBtn.setVisibility(View.VISIBLE);
		editBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				/*Intent it = new Intent(App.getAppContext(), EditPersonalActivity.class);
				ShowPersonalActivity.this.startActivityForResult(it, 0);*/
				UIHelper.showEditPersonalActivity(ShowPersonalActivity.this);
				/*if(isEditable){
					isEditable = false;
					setEditable(false);
				} else {
					isEditable = true;
					setEditable(true);
				}*/

			}
		});

		((TextView) findViewById(R.id.myinfo_head_layout).findViewById(R.id.commonheader_title_tv))
				.setText(R.string.persion_info_title);
		findViewById(R.id.myinfo_head_layout).findViewById(R.id.commonheader_left_iv)
				.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						finish();
					}
				});

		sexTv = (TextView) findViewById(R.id.myinfo_sex_data);
		ageTv = (TextView)  findViewById(R.id.myinfo_age_data);
		starTv = (TextView) findViewById(R.id.myinfo_star_data);
		phoneTv = (TextView) findViewById(R.id.myinfo_phone_data);

		workTv = (TextView) findViewById(R.id.myinfo_work_data);
		companyTv = (TextView) findViewById(R.id.myinfo_company_data);
		schoolTv = (TextView) findViewById(R.id.myinfo_school_data);
		hobbyTv = (TextView) findViewById(R.id.myinfo_hobby_data);
		explanationTv = (TextView) findViewById(R.id.myinfo_explantion_data);
		//--------------------右边的箭头------------------------
		myinfo_sex_img = (ImageView) findViewById(R.id.myinfo_sex_img);
		myinfo_age_img = (ImageView) findViewById(R.id.myinfo_age_img);
		myinfo_star_img = (ImageView) findViewById(R.id.myinfo_star_img);
		myinfo_phone_img = (ImageView) findViewById(R.id.myinfo_phone_img);
		myinfo_work_img = (ImageView) findViewById(R.id.myinfo_work_img);
		myinfo_company_img = (ImageView) findViewById(R.id.myinfo_company_img);
		myinfo_school_img = (ImageView) findViewById(R.id.myinfo_school_img);
		myinfo_hobby_img = (ImageView) findViewById(R.id.myinfo_hobby_img);
		myinfo_explantion_img = (ImageView) findViewById(R.id.myinfo_explantion_img);
	}
	/**
	 * 设置可否编辑
	 */
	private void setEditable(boolean isEditable){
		if(isEditable){
			myinfo_age_img.setVisibility(View.VISIBLE);
			myinfo_star_img.setVisibility(View.VISIBLE);
			myinfo_phone_img.setVisibility(View.VISIBLE);
			myinfo_work_img.setVisibility(View.VISIBLE);
			myinfo_company_img.setVisibility(View.VISIBLE);
			myinfo_school_img.setVisibility(View.VISIBLE);
			myinfo_hobby_img.setVisibility(View.VISIBLE);
			myinfo_explantion_img.setVisibility(View.VISIBLE);
		} else {
			myinfo_age_img.setVisibility(View.GONE);
			myinfo_star_img.setVisibility(View.GONE);
			myinfo_phone_img.setVisibility(View.GONE);
			myinfo_work_img.setVisibility(View.GONE);
			myinfo_company_img.setVisibility(View.GONE);
			myinfo_school_img.setVisibility(View.GONE);
			myinfo_hobby_img.setVisibility(View.GONE);
			myinfo_explantion_img.setVisibility(View.GONE);
		}
	}
	
	/**
	 * 个人信息网络请求
	 */
	private void requestPersonal() {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("userid", App.getUserId());
		cacheHttp.asyncRequestWithCache(Constants.GET_USER_URL, map,
				new RequestWithCacheCallBack<PersonalInfo>() {

				@Override
				public PersonalInfo onPreRequestCache(String cache) throws Exception {
					mPersonalInfo = PersonalInfo.personalInfoJSONPasing(cache);
					return mPersonalInfo;
				}
	
				@Override
				public void onPreRequestSuccess(PersonalInfo data) {
					setPersonalView(data);
				}
	
				@Override
				public PersonalInfo onReqestSuccess(String respones) throws Exception {
					mPersonalInfo = PersonalInfo.personalInfoJSONPasing(respones);
					return mPersonalInfo;
				}
	
				@Override
				public void success(PersonalInfo cacheRespones, PersonalInfo newRespones) {
					setPersonalView(newRespones);
				}
	
				@Override
				public void exception(Exception exception) {
					Toast.makeText(mContext,
							getResources().getString(R.string.net_error),
							Toast.LENGTH_SHORT).show();
					exception.printStackTrace();
				}
		});
		
		
	}

	/**
	 * 设置个人信息视图
	 */
	private void setPersonalView(PersonalInfo personalInfo) {
		if(personalInfo == null){
			return;
		}
		
		setTextView(sexTv, personalInfo.getSexString(), R.string.text_empty);
		setTextView(workTv, getPersonalWork(personalInfo.getProfession(), personalInfo.getIndustry()), R.string.text_empty_work);
		setTextView(ageTv, "" + personalInfo.getAge(), R.string.text_empty);
		setTextView(starTv, personalInfo.getStar(), R.string.text_empty);
		setTextView(phoneTv,personalInfo.getPhone(),R.string.text_empty);
		setTextView(hobbyTv, personalInfo.getHobby(), R.string.text_empty_hobby);
		setTextView(explanationTv, personalInfo.getExplanation(), R.string.text_empty_explanation);
		setTextView(companyTv, personalInfo.getCompany(), R.string.text_empty_company);
		setTextView(schoolTv, "", R.string.text_empty_school);

		String schoolText = "";
		if(!StringUtils.isEmpty(mPersonalInfo.getUniversity()))
		{
			schoolText += getResources().getString(R.string.university_school)+mPersonalInfo.getUniversity();
		}

		if(!StringUtils.isEmpty(mPersonalInfo.getSeniormiddleSchool()))
		{
			schoolText += "\n";
			schoolText += getResources().getString(R.string.senior_school)+mPersonalInfo.getSeniormiddleSchool();
		}
		
		if(!StringUtils.isEmpty(mPersonalInfo.getJuniormiddleSchool()))
		{
			schoolText += "\n";
			schoolText += getResources().getString(R.string.junior_school)+mPersonalInfo.getJuniormiddleSchool();
		}
		
		if(!StringUtils.isEmpty(mPersonalInfo.getPrimarySchool()))
		{
			schoolText += "\n";
			schoolText += getResources().getString(R.string.primary_school)+mPersonalInfo.getPrimarySchool();
		}

		setTextView(schoolTv, schoolText, R.string.text_empty_school);
	}

	private void setTextView(TextView textView, String str, int stringId)
	{
		if(StringUtils.isEmpty(str))
		{
			textView.setText(stringId);
			textView.setTextColor(getResources().getColor(R.color.light_black));
		}
		else
		{
			textView.setText(str);
			textView.setTextColor(getResources().getColor(R.color.black));
		}
		
	}

	private String getPersonalWork(String profession, String industry)
	{
		String workText = profession;
		if(!StringUtils.isEmpty(industry))
		{
			workText += "\n";
			workText += industry;
		}

		return workText;
	}

	
	private BroadcastReceiver receiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			System.out.println("com.tming.openuniversity.activity.my.showpersonalactivity");
			requestPersonal();
		}

	};
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		Log.d(TAG, "onActivityResult; requestCode:"+ requestCode+"; resultCode:"+resultCode);
		
		if( RESULT_OK == resultCode)
		{
			Toast.makeText(mContext, R.string.edit_persioninfo_success, Toast.LENGTH_LONG).show();
		}
	}
}
