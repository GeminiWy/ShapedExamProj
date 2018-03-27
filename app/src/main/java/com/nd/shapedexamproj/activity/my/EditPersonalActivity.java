package com.nd.shapedexamproj.activity.my;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.*;

import com.nd.shapedexamproj.App;
import com.nd.shapedexamproj.R;
import com.nd.shapedexamproj.activity.BaseActivity;
import com.nd.shapedexamproj.model.my.PersonalInfo;
import com.nd.shapedexamproj.util.BirthdayUtil;
import com.nd.shapedexamproj.util.Constants;
import com.nd.shapedexamproj.util.IndustryUtil;
import com.nd.shapedexamproj.util.StringUtils;
import com.nd.shapedexamproj.view.CommonDialog;
import com.tming.common.cache.net.TmingCacheHttp;
import com.tming.common.net.TmingHttp;
import com.tming.common.util.Log;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 
 * 个人信息编辑
 * 
 * @author Linlg
 * 
 *         Create on 2014-3-27
 */
public class EditPersonalActivity extends BaseActivity {
	
	private final static String TAG = "EditPersonalActivity";

	private final int BUTTON_AGE = 1;
	private final int BUTTON_STAR = 2;
	private final int BUTTON_PHONE = 3;
	private final int BUTTON_WORK = 4;
	private final int BUTTON_COMPANY = 5;
	private final int BUTTON_SCHOOL = 6;
	private final int BUTTON_HOBBY = 7;
	private final int BUTTON_EXPLANATION = 8;
	private final int BUTTON_SAVE = 9;
	
	//编辑页面的resultCode 100-编辑星座&年龄;200-编辑电话 ;300-编辑行业;400-编辑公司;500-编辑学校;600-编辑兴趣;700-编辑签名;
	public final static int RESULTCODE_DATE = 100, RESULTCODE_PHONE = 200, RESULTCODE_WORK = 300, 
			RESULTCODE_COMPANY = 400, RESULTCODE_SCHOOL = 500, RESULTCODE_HOBBY = 600, RESULTCODE_INFO = 700;
	
	//设置输入的最长长度
	public static final int EDIT_MAX_LENGTH = 60;
	private Context mContext;
	private RelativeLayout mHeadRL;
	private ImageView mBackIV;
	private TextView mHeadTitleTV;
	private Button mHeadRightBtn;
	private TextView sexTv, ageTv, starTv, phoneTv, workTv, companyTv, schoolTv, hobbyTv,explanationTv;
	private TmingCacheHttp mCacheHttp;
	private CommonDialog mCommonDialog;
	private View loadingLayout;
	private PersonalInfo mPersonalInfo;
	private boolean mIsEdited = false;
	private int mResultCode;

	@Override
	public int initResource() {
		return R.layout.my_edit;
	}

	@Override
	public void initComponent() {
		mContext = this;
		mCacheHttp = TmingCacheHttp.getInstance(EditPersonalActivity.this);
		mCommonDialog = new CommonDialog(mContext);
		mCommonDialog.setTitleTextView("", View.GONE);
		mCommonDialog.setMessageTextView(R.string.dlg_content, View.VISIBLE);
		initView();
	}

	@Override
	public void initData() {
		requestPersonal();
	}

	@Override
	public void addListener() {
		((RelativeLayout) findViewById(R.id.myinfo_age_rl))
				.setOnClickListener(new OnClickListenerImpl(BUTTON_AGE));
		((RelativeLayout) findViewById(R.id.myinfo_star_rl))
				.setOnClickListener(new OnClickListenerImpl(BUTTON_STAR));
		((RelativeLayout) findViewById(R.id.myinfo_phone_rl))
				.setOnClickListener(new OnClickListenerImpl(BUTTON_PHONE));
		((RelativeLayout) findViewById(R.id.myinfo_work_layout))
				.setOnClickListener(new OnClickListenerImpl(BUTTON_WORK));
		((RelativeLayout) findViewById(R.id.myinfo_company_layout))
				.setOnClickListener(new OnClickListenerImpl(BUTTON_COMPANY));
		((RelativeLayout) findViewById(R.id.myinfo_school_layout))
				.setOnClickListener(new OnClickListenerImpl(BUTTON_SCHOOL));
		((RelativeLayout) findViewById(R.id.myinfo_hobby_layout))
				.setOnClickListener(new OnClickListenerImpl(BUTTON_HOBBY));
		((RelativeLayout) findViewById(R.id.myinfo_explantion_layout))
				.setOnClickListener(new OnClickListenerImpl(BUTTON_EXPLANATION));

		mBackIV.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if(mIsEdited)
				{
					mCommonDialog.show();
				}
				else
				{
					finish();
				}
				
			}
		});
		
		mHeadRightBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				submitInfo();
				
			}
		});
		
		mCommonDialog.setPositiveButton(R.string.dlg_content_posbtn, new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				mCommonDialog.dismiss();
			}
		});
		
		mCommonDialog.setNegativeButton(R.string.dlg_content_negbtn, new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				finish();
				
			}
		});
	}

	/**
	 * 初始化视图
	 */
	private void initView() {

		mHeadRL = (RelativeLayout)findViewById(R.id.myedit_head_layout);
		mBackIV = (ImageView)mHeadRL.findViewById(R.id.commonheader_left_iv);
		mHeadTitleTV = (TextView) findViewById(R.id.commonheader_title_tv);
		mHeadRightBtn = (Button) findViewById(R.id.commonheader_right_btn);
		
		mHeadRightBtn.setVisibility(View.VISIBLE);
		mHeadRightBtn.setText(R.string.save);

		mHeadTitleTV.setText(R.string.persion_info_title);
		
		sexTv = (TextView) findViewById(R.id.myinfo_sex_data);
		ageTv = (TextView)  findViewById(R.id.myinfo_age_data);
		starTv = (TextView) findViewById(R.id.myinfo_star_data);
		phoneTv = (TextView) findViewById(R.id.myinfo_phone_data);

		workTv = (TextView) findViewById(R.id.myinfo_work_data);
		companyTv = (TextView) findViewById(R.id.myinfo_company_data);
		schoolTv = (TextView) findViewById(R.id.myinfo_school_data);
		hobbyTv = (TextView) findViewById(R.id.myinfo_hobby_data);
		explanationTv = (TextView) findViewById(R.id.myinfo_explantion_data);
		loadingLayout = (View) findViewById(R.id.loading_layout);
	}

	/**
	 * 个人信息网络请求
	 */
	private void requestPersonal() {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("userid", App.getUserId());
		mCacheHttp.asyncRequestWithCache(Constants.GET_USER_URL, map,
				new TmingCacheHttp.RequestWithCacheCallBack<PersonalInfo>() {

					@Override
					public PersonalInfo onPreRequestCache(String cache) throws Exception {
						mPersonalInfo = PersonalInfo.personalInfoJSONPasing(cache);
						return mPersonalInfo;
					}

					@Override
					public void onPreRequestSuccess(PersonalInfo data) {
						setEditView(data);
					}

					@Override
					public PersonalInfo onReqestSuccess(String respones) throws Exception {
						mPersonalInfo = PersonalInfo.personalInfoJSONPasing(respones);
						return mPersonalInfo;
					}

					@Override
					public void success(PersonalInfo cacheRespones, PersonalInfo newRespones) {
						setEditView(newRespones);
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
	 * 设置个人信息编辑视图
	 */
	private void setEditView(PersonalInfo personalInfo) {
	
		if(personalInfo == null){
			return;
		}
	
		setTextView(sexTv, personalInfo.getSexString(), R.string.text_empty);
		setTextView(workTv, getPersonalWork(personalInfo.getProfession(), personalInfo.getIndustry()), R.string.text_empty_work);
		setTextView(ageTv, "" + personalInfo.getAge(), R.string.text_empty);
		setTextView(starTv, personalInfo.getStar(), R.string.text_empty_star);
		setTextView(hobbyTv, personalInfo.getHobby(), R.string.text_empty_hobby);
		
		String phoneVisiable = "";
		if (personalInfo.phoneVisible == 1) {
			phoneVisiable = "(仅教师可见)" ;
		} else if (personalInfo.phoneVisible == 2) {
			phoneVisiable = "(好友可见)" ;
		} else if (personalInfo.phoneVisible == 3) {
			phoneVisiable = "(所有人可见)" ;
		}
		
		setTextView(phoneTv,personalInfo.getPhone() + phoneVisiable,R.string.text_empty);
		setTextView(explanationTv, personalInfo.getExplanation(), R.string.text_empty_explanation);
		setTextView(companyTv, personalInfo.getCompany(), R.string.text_empty_company);
		setTextView(schoolTv, "", R.string.text_empty_school);

		setTextView(schoolTv, getSchooleText(mPersonalInfo.getPrimarySchool(), mPersonalInfo.getJuniormiddleSchool(), 
				mPersonalInfo.getSeniormiddleSchool(), mPersonalInfo.getUniversity()), R.string.text_empty_school);
	}

	private String getPersonalWork(String profession, String industry)
	{
		String workText = profession;
		if (!StringUtils.isEmpty(workText)) {
		    workText += "\n";
		}
		if(!StringUtils.isEmpty(industry))
		{
			workText += industry;
		}

		return workText;
	}

	/**
	 * 按钮监听事件
	 */
	private class OnClickListenerImpl implements OnClickListener {
		private int which;

		public OnClickListenerImpl(int which) {
			this.which = which;
		}

		@Override
		public void onClick(View v) {
			if(mPersonalInfo == null){
				return;
			}
			switch (which) {
			case BUTTON_AGE:
				Intent age = new Intent(EditPersonalActivity.this, SetTimeActivity.class);
				age.putExtra("birthday", mPersonalInfo.getBrithday());
				age.putExtra("title", getResources().getString(R.string.edit_title_brithday));
				startActivityForResult(age, RESULTCODE_DATE);
				break;
			case BUTTON_STAR:
				Intent star = new Intent(EditPersonalActivity.this, SetTimeActivity.class);
				star.putExtra("birthday", mPersonalInfo.getBrithday());
				star.putExtra("title", getResources().getString(R.string.edit_title_brithday));
				startActivityForResult(star, RESULTCODE_DATE);
				break;
			case BUTTON_PHONE:
				Intent call = new Intent(EditPersonalActivity.this, SetPhoneActivity.class);
				call.putExtra("phone", mPersonalInfo.getPhone());
				call.putExtra("phonevisible", mPersonalInfo.getPhoneVisible());
				startActivityForResult(call, RESULTCODE_PHONE);
				break;
			case BUTTON_WORK:
				Intent working = new Intent(EditPersonalActivity.this, SetWorkActivity.class);
				working.putExtra("industryid", mPersonalInfo.getIndustryId());
				working.putExtra("work", mPersonalInfo.getProfession());
				startActivityForResult(working, RESULTCODE_WORK);
				break;
			case BUTTON_COMPANY:
				Intent companying = new Intent(EditPersonalActivity.this, SetCompanyActivity.class);
				companying.putExtra("title", getResources().getString(R.string.edit_title_company));
				companying.putExtra("which", SetCompanyActivity.WHICH_COMPANY);
				companying.putExtra("info", mPersonalInfo.getCompany());
				startActivityForResult(companying, RESULTCODE_COMPANY);
				break;
			case BUTTON_SCHOOL:
				Intent schooling = new Intent(EditPersonalActivity.this, SetSchoolActivity.class);
				schooling.putExtra("primaryschool", mPersonalInfo.getPrimarySchool());
				schooling.putExtra("juniormiddleschool", mPersonalInfo.getJuniormiddleSchool());
				schooling.putExtra("seniormiddleschool", mPersonalInfo.getSeniormiddleSchool());
				schooling.putExtra("university", mPersonalInfo.getUniversity());
				startActivityForResult(schooling, RESULTCODE_SCHOOL);
				break;
			case BUTTON_HOBBY:
				Intent hobbying = new Intent(EditPersonalActivity.this, SetCompanyActivity.class);
				hobbying.putExtra("title", getResources().getString(R.string.edit_title_hobby));
				hobbying.putExtra("which", SetCompanyActivity.WHICH_HOBBY);
				hobbying.putExtra("info", mPersonalInfo.getHobby());
				startActivityForResult(hobbying, RESULTCODE_HOBBY);
				break;
			case BUTTON_EXPLANATION:
				Intent explanationing = new Intent(EditPersonalActivity.this,
						SetCompanyActivity.class);
				explanationing.putExtra("title", getResources().getString(R.string.edit_title_explanation));
				explanationing.putExtra("which", SetCompanyActivity.WHICH_EXPLANATION);
				explanationing.putExtra("info", mPersonalInfo.getExplanation());
				startActivityForResult(explanationing, RESULTCODE_INFO);
				break;
			}
		}
	}

	/**
	 * 保存提交信息
	 */
	private void submitInfo() {
	    loadingLayout.setVisibility(View.VISIBLE);
		mResultCode = RESULT_OK;

		if(mPersonalInfo == null){
			return;
		}

		Map<String, Object> map = new HashMap<String, Object>();
		map.put("userid", mPersonalInfo.getUserId());
		map.put("birthday", mPersonalInfo.getBrithday());
		map.put("explanation", mPersonalInfo.getExplanation());
		map.put("hobby", mPersonalInfo.getHobby());
		map.put("primaryschool", mPersonalInfo.getPrimarySchool());
		map.put("juniormiddleschool", mPersonalInfo.getJuniormiddleSchool());
		map.put("seniormiddleschool", mPersonalInfo.getSeniormiddleSchool());
		map.put("university", mPersonalInfo.getUniversity());
		map.put("industryid", mPersonalInfo.getIndustryId());
		map.put("profession", mPersonalInfo.getProfession());
		map.put("company",mPersonalInfo.getCompany());
		map.put("phone", mPersonalInfo.getPhone());
		map.put("phonevisible", mPersonalInfo.getPhoneVisible());
		TmingHttp.asyncRequest(Constants.UPDATE_USER_URL, map,
				new TmingHttp.RequestCallback<Integer>() {

					@Override
					public Integer onReqestSuccess(String respones) throws Exception {
						Log.d(TAG, respones);
						return new JSONObject(respones).getInt("flag");
					}

					@Override
					public void success(Integer respones) {
						loadingLayout.setVisibility(View.GONE);
						if (respones == Constants.SUCCESS_MSG) {
							EditPersonalActivity.this.finish();
							sendBroadcast(new Intent(
									"com.tming.openuniversity.activity.my.showpersonalactivity"));
							mIsEdited = false;
						} else {
							Toast.makeText(EditPersonalActivity.this, R.string.edit_persioninfo_error,
									Toast.LENGTH_SHORT).show();
						}
					}

					@Override
					public void exception(Exception exception) {
						loadingLayout.setVisibility(View.GONE);
						Toast.makeText(mContext,
								getResources().getString(R.string.net_error),
								Toast.LENGTH_SHORT).show();
						exception.printStackTrace();
					}
				});
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		Log.d(TAG, "onActivityResult; requestCode:"+ requestCode+"; resultCode:"+resultCode);

		if (requestCode == RESULTCODE_DATE && resultCode == RESULTCODE_DATE) { // 编辑星座&年龄
			String birthday = "" + data.getIntExtra("getyear", 0) + "-" + data.getIntExtra("getmonth", 0)
					+ "-" + data.getIntExtra("getday", 0);
			Log.d(TAG, "onActivityResult: birthday-"+birthday);
			
			if(!mPersonalInfo.getBrithday().equals(birthday))
			{
				mPersonalInfo.setBrithday(birthday);
				String star = BirthdayUtil.getConstellation(birthday);
				getCurrentAge(data);
				starTv.setText(star);
				mIsEdited = true;
			}
			

		} else if (requestCode == RESULTCODE_PHONE && resultCode == RESULTCODE_PHONE) { // 编辑电话
			String phone = data.getStringExtra("phone");
			int phonevisible = data.getIntExtra("phonevisible", 0);
			
			if(!mPersonalInfo.getPhone().equals(phone) || mPersonalInfo.getPhoneVisible() != phonevisible)
			{
				mPersonalInfo.setPhone(phone);
				mPersonalInfo.setPhoneVisible(phonevisible);
				phoneTv.setText(getPhoneText(phonevisible,phone));
				mIsEdited = true;
			}
			
		} else if (requestCode == RESULTCODE_WORK && resultCode == RESULTCODE_WORK) { // 编辑行业
			String work = data.getStringExtra("work");
			int industryid = data.getIntExtra("industryid", 0);
			Log.d(TAG, "onActivityResult: work:"+work+";industryid:"+industryid);
			if(!mPersonalInfo.getProfession().equals(work) || mPersonalInfo.getIndustryId() != industryid)
			{
				mPersonalInfo.setProfession(work);
				mPersonalInfo.setIndustryId(industryid);
				String industrySub = IndustryUtil.getIndustrySub(industryid);
				setTextView(workTv, getPersonalWork(work, industrySub), R.string.text_empty_work);
				mIsEdited = true;
			}
			
			
		} else if (requestCode == RESULTCODE_COMPANY && resultCode == RESULTCODE_COMPANY) { // 编辑公司
			String company = data.getStringExtra("info");
			if(!mPersonalInfo.getCompany().equals(company))
			{
				mPersonalInfo.setCompany(company);
				setTextView(companyTv, company, R.string.text_empty_company);
				mIsEdited = true;
			}

		} else if (requestCode == RESULTCODE_SCHOOL && resultCode == RESULTCODE_SCHOOL) { // 编辑学校
			String primaryschool = data.getStringExtra("primaryschool");
			String juniormiddleschool = data.getStringExtra("juniormiddleschool");
			String seniormiddleschool = data.getStringExtra("seniormiddleschool");
			String university = data.getStringExtra("university");
			Log.d(TAG, "onActivityResult: primaryschool-"+primaryschool+";juniormiddleschool-"+juniormiddleschool
					+";seniormiddleschool-"+seniormiddleschool+";university-"+university);
			
			if(!mPersonalInfo.getPrimarySchool().equals(primaryschool) || !mPersonalInfo.getJuniormiddleSchool().equals(juniormiddleschool)
					|| !mPersonalInfo.getSeniormiddleSchool().equals(seniormiddleschool) || !mPersonalInfo.getUniversity().equals(university))
			{
				mPersonalInfo.setPrimarySchool(primaryschool);
				mPersonalInfo.setJuniormiddleSchool(juniormiddleschool);
				mPersonalInfo.setSeniormiddleSchool(seniormiddleschool);
				mPersonalInfo.setUniversity(university);
				
				setTextView(schoolTv, getSchooleText(primaryschool, juniormiddleschool, seniormiddleschool, university), R.string.text_empty_school);
			}
			
			
		} else if (requestCode == RESULTCODE_HOBBY && resultCode == RESULTCODE_HOBBY) { // 编辑兴趣
			String hobby = data.getStringExtra("info");
			mPersonalInfo.setHobby(hobby);
			setTextView(hobbyTv, hobby, R.string.text_empty_hobby);
			
		} else if (requestCode == RESULTCODE_INFO && resultCode == RESULTCODE_INFO) { // 编辑签名
			String explanation = data.getStringExtra("info");
			mPersonalInfo.setExplanation(explanation);;
			setTextView(explanationTv, explanation, R.string.text_empty_explanation);
		}
	}
	
	private void getCurrentAge(final Intent data) {
	    String url = Constants.CURRENT_TIME_IN_MILLIS;
		TmingHttp.asyncRequest(url, null, new TmingHttp.RequestCallback<Long>() {

			@Override
			public Long onReqestSuccess(String respones) throws Exception {
				JSONObject jsonObject = new JSONObject(respones);
				long data = jsonObject.getLong("data");
				return data;
			}

			@Override
			public void success(Long respones) {
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh-mm-ss");
				String date = sdf.format(new Date(respones));
				String currYear = date.substring(0, date.indexOf("-"));
				int year = Integer.parseInt(currYear);
				ageTv.setText("" + (year - data.getIntExtra("getyear", 0)));
			}

			@Override
			public void exception(Exception exception) {

			}


		});
	}
	
	
	private String getPhoneText(final int phoneVisible,String phone)
	{
		String phoneText = "";
		if (phoneVisible == PersonalInfo.PHONE_VISIABLE_TEACHER) {
			phoneText = getResources().getString(R.string.visiable_teacher);
		} else if (phoneVisible == PersonalInfo.PHONE_VISIABLE_FRIENDS) {
			phoneText = getResources().getString(R.string.visiable_friends);
		} else if (phoneVisible == PersonalInfo.PHONE_VISIABLE_ALL) {
			phoneText = getResources().getString(R.string.visiable_all);
		}
		return phoneText + phone;
	}
	
	private String getSchooleText(String primaryschool, String juniormiddleschool, String seniormiddleschool, String university)
	{
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
		
		return schoolText;
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

	@Override
	public void finish() {
		setResult(mResultCode);
		super.finish();
	}
	
	
	
}