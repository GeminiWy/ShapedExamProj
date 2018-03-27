package com.nd.shapedexamproj.activity;


import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.*;
import android.widget.RelativeLayout.LayoutParams;

import com.nd.shapedexamproj.App;
import com.nd.shapedexamproj.R;
import com.nd.shapedexamproj.activity.im.ChatActivity;
import com.nd.shapedexamproj.activity.my.PersonalDynamicsActivity;
import com.nd.shapedexamproj.model.my.PersonalInfo;
import com.nd.shapedexamproj.model.playground.Timeline;
import com.nd.shapedexamproj.net.ServerApi;
import com.nd.shapedexamproj.util.Constants;
import com.nd.shapedexamproj.util.FlowerDialog;
import com.nd.shapedexamproj.util.ImageUtil;
import com.nd.shapedexamproj.util.StringUtils;
import com.nd.shapedexamproj.util.TimelinePostTimeAgoUtil;
import com.nd.shapedexamproj.view.CircularImage;
import com.nd.shapedexamproj.view.InputBottomBar;
import com.nd.shapedexamproj.view.numberpicker.OnWheelChangedListener;
import com.nd.shapedexamproj.view.numberpicker.WheelView;
import com.tming.common.cache.image.ImageCacheTool;
import com.tming.common.net.TmingHttp;
import com.tming.common.net.TmingHttp.RequestCallback;
import com.tming.common.util.Helper;
import com.tming.common.util.Log;
import com.tming.common.util.PhoneUtil;
import com.tming.common.view.TouchImageView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
/**
 * 查看他人信息页
 * @author zll
 * create in 2014-4-2
 */
public class PersonalActivity extends BaseActivity{
	
	private final static String TAG = "PersonalActivity";
	public static final int RELATED_ADDED=1;
    public static final int RELATED_NOTADDED=0;

	private Context mContext;
	private LinearLayout my_detail_bottom_ll ,loading_layout ;
	private Button my_detail_follow_btn,my_detail_send_msg_btn,my_detail_send_flower_btn;
	protected PopupWindow mImagePopupWindow;
	
	private RelativeLayout mPersonalHeadRL, personalPhoneRL ,personalMajorRL,personalCompanyRL,
	        personalSchoolRL,personalHobbyRL,personalDescRL;
	private RelativeLayout mPersonalCoverRL;
	private TextView mNameTV , myTeacherMarkTv;
	private ImageView myTeacherIcon, my_back_btn;
	private CircularImage mUsreImgCIV,mUserImgBg;
	private ImageView mDynamicImg;
	private TextView mClassTV,mySpecialtyTv;
	
	private TextView mPhoneDataTV;
	private TextView  mMajorDataTV;
	private TextView  mCompanyDataTV;
	private TextView  mSchoolDataTV;
	private TextView  mHobbyDataTV;
	private TextView  mDescDataTV;
	private RelativeLayout mPersonalDyNamicRL;
	private TextView  mDyNamicNumTV;
	private TextView  mDyNamicDataTV;
	private TextView  mDyNamicDateTV;
	private TextView  mActivityNumTV;
	private TextView  mActivityData1TV;
	private TextView  mActivityData2TV;
	private TextView  mGroupNumTV;
	private TextView  mGroupData1TV;
	private TextView  mGroupData2TV;
    private View mSeperateLineView;

	private String friendid = "0";
    private boolean mBottomBarVisiable=true; //底部功能按钮是否打开，true:打开 ，false：关闭
	private PersonalInfo mPersonalInfo;
	private Timeline mTimeline;
	private int rest_flowers ;	//剩余的鲜花数
	private int mFriendState;//与好友的状态
	private int mLoadFlag;
	private int phoneVisible = 1;//默认仅老师可见
	private ImageCacheTool imageCacheTool;
    private int mUserRelatedState,mOldState;
	@Override
	public int initResource() {
		return R.layout.personal_activity;
	}
	
	@Override
	public void initComponent(){
		mContext = this;
		friendid = getIntent().getStringExtra("friendid");
        mBottomBarVisiable=getIntent().getBooleanExtra ("bottomVisiable",true);
		imageCacheTool = ImageCacheTool.getInstance();
		loading_layout = (LinearLayout) findViewById(R.id.loading_layout);
		my_detail_bottom_ll = (LinearLayout) findViewById(R.id.my_detail_bottom_ll);
		my_detail_follow_btn = (Button) findViewById(R.id.my_detail_follow_btn);
		my_detail_send_msg_btn = (Button) findViewById(R.id.my_detail_send_msg_btn);
		my_detail_send_flower_btn = (Button) findViewById(R.id.my_detail_send_flower_btn);
        mSeperateLineView=(View) findViewById(R.id.seperate_line_view);
		mPersonalHeadRL = (RelativeLayout) findViewById(R.id.personal_cover_layout);
		mPersonalCoverRL = (RelativeLayout) findViewById(R.id.my_cover_rl);
		mNameTV = (TextView) mPersonalHeadRL.findViewById(R.id.my_name_tv);
		myTeacherMarkTv = (TextView) mPersonalHeadRL.findViewById(R.id.my_teacher_mark_tv);
		myTeacherIcon = (ImageView) findViewById(R.id.my_teacher_icon);
		mUserImgBg = (CircularImage) mPersonalHeadRL.findViewById(R.id.my_photo_bg_iv);
		mUserImgBg.setImageDrawable(getResources().getDrawable(R.drawable.photo_bg_icon));
		mUsreImgCIV = (CircularImage) mPersonalHeadRL.findViewById(R.id.my_photo_iv);
		mySpecialtyTv = (TextView) mPersonalHeadRL.findViewById(R.id.my_specialty_tv);
		mClassTV = (TextView) mPersonalHeadRL.findViewById(R.id.my_class_tv);

		personalPhoneRL = (RelativeLayout) findViewById(R.id.personal_phone_rl);
		personalMajorRL = (RelativeLayout) findViewById(R.id.personal_major_rl);
		personalCompanyRL = (RelativeLayout) findViewById(R.id.personal_company_rl);
		personalSchoolRL = (RelativeLayout) findViewById(R.id.personal_school_rl);
		personalHobbyRL = (RelativeLayout) findViewById(R.id.personal_hobby_rl);
		personalDescRL = (RelativeLayout) findViewById(R.id.personal_desc_rl);
		
		mPhoneDataTV = (TextView) findViewById(R.id.person_phone_data);
		mMajorDataTV = (TextView) findViewById(R.id.person_major_data);
		mCompanyDataTV = (TextView) findViewById(R.id.person_company_data);
		mSchoolDataTV = (TextView) findViewById(R.id.person_school_data);
		mHobbyDataTV = (TextView) findViewById(R.id.person_hobby_data);
		mDescDataTV = (TextView) findViewById(R.id.person_desc_data);
		mPersonalDyNamicRL = (RelativeLayout) findViewById(R.id.personal_dynamic_rl);
		mDyNamicNumTV = (TextView) findViewById(R.id.person_dynamic_num_tv);
		mDynamicImg = (ImageView)findViewById(R.id.person_dynamic_img);
		mDyNamicDataTV = (TextView) findViewById(R.id.person_dynamic_data);
		mDyNamicDateTV = (TextView) findViewById(R.id.person_dynamic_date);
		mActivityNumTV = (TextView) findViewById(R.id.person_activity_num_tv);
		mActivityData1TV = (TextView) findViewById(R.id.person_activity_data1);
		mActivityData2TV = (TextView) findViewById(R.id.person_activity_data2);
		mGroupNumTV = (TextView) findViewById(R.id.person_group_num_tv);
		mGroupData1TV = (TextView) findViewById(R.id.person_group_data1);
		mGroupData2TV = (TextView) findViewById(R.id.person_group_data2);
		
		my_back_btn = (ImageView) findViewById(R.id.my_back_btn);
		my_back_btn.setVisibility(View.VISIBLE);
		/*if (StringUtils.isEmpty(friendid) || friendid.equals(App.getUserId()) || StringUtils.isEmpty(App.getUserId())) {
		    //如果是自己的界面或游客，则屏蔽
            mBottomBarVisiable=false;
		}*/
        // 全部隐藏底部
        setBottomBarVisiable(false);
	}

    /**
     * <p>设定底部操作按钮是否可见</p>
     * @param visiable
     */
    public void setBottomBarVisiable(boolean visiable){

        if (visiable){
            mSeperateLineView.setVisibility(View.VISIBLE);
            my_detail_bottom_ll.setVisibility(View.VISIBLE);
        } else {
            mSeperateLineView.setVisibility(View.GONE);
            my_detail_bottom_ll.setVisibility(View.GONE);
        }
    }
	
	/**
	 * 获取关注状态
	 */
	private void getFriendState(){
		String url = Constants.GET_FRIEDN_STATE ;
		Log.d(TAG, url);
		Map<String, Object> params = new HashMap<String,Object>();
		params.put("followid", friendid);			
		params.put("userid", App.getUserId());
		TmingHttp.asyncRequest(url, params, new TmingHttp.RequestCallback<Integer>() {

			@Override
			public Integer onReqestSuccess(String respones) throws Exception {

				Log.i(TAG, respones);
				JSONObject timeLineJs = new JSONObject(respones);
				int flag = timeLineJs.getInt("flag");
				if (Constants.SUCCESS_MSG == flag) {
					if (!timeLineJs.isNull("data")) {
						JSONArray dataAry = timeLineJs.getJSONArray("data");
						if (null != dataAry && dataAry.length() > 0) {
							JSONObject item = dataAry.getJSONObject(0);
							long userId = item.getLong("userId");
							int rType = item.getInt("rtype");
							// 根据返回结果的Userid判断好友关系
							if (App.getUserId().equals(String.valueOf(userId))) {
								mFriendState = rType;
							} else {
								if (rType == Constants.PERSON_RELATION_FOLLOW) {
									mFriendState = Constants.PERSON_RELATION_FOLLOWED;
								} else if (rType == Constants.PERSON_RELATION_FOLLOWED) {
									mFriendState = Constants.PERSON_RELATION_FOLLOW;
								} else {
									mFriendState = rType;
								}
							}
						} else {
							mFriendState = Constants.PERSON_RELATION_EMPTY;
						}
					} else {
						mFriendState = Constants.PERSON_RELATION_EMPTY;
					}
				} else {
					mFriendState = Constants.PERSON_RELATION_EMPTY;
				}
				return flag;
			}

			@Override
			public void success(Integer result) {
				if (Constants.SUCCESS_MSG == result) {
					if (isFollow()) {
						my_detail_follow_btn.setText(R.string.un_follow);
						mUserRelatedState = RELATED_ADDED;
					} else {
						my_detail_follow_btn.setText(R.string.follow);
						mUserRelatedState = RELATED_NOTADDED;
					}
					mOldState = mUserRelatedState;
				} else {
					Toast.makeText(PersonalActivity.this, R.string.api_error, Toast.LENGTH_SHORT).show();
					return;
				}
			}

			@Override
			public void exception(Exception exception) {
				exception.printStackTrace();
			}

		});
	}
	
	/**
     * 获取关注状态
     */
    private void getPhoneVisibleState(){
        String url = Constants.GET_FRIEDN_STATE ;
        Log.d(TAG, url);
        Map<String, Object> params = new HashMap<String,Object>();
        params.put("followid", friendid);           
        params.put("userid", App.getUserId());
		TmingHttp.asyncRequest(url, params, new TmingHttp.RequestCallback<Integer>() {

			@Override
			public Integer onReqestSuccess(String respones) throws Exception {

				Log.i(TAG, respones);
				JSONObject timeLineJs = new JSONObject(respones);
				int flag = timeLineJs.getInt("flag");
				if (Constants.SUCCESS_MSG == flag) {
					if (!timeLineJs.isNull("data")) {
						JSONArray dataAry = timeLineJs.getJSONArray("data");
						if (null != dataAry && dataAry.length() > 0) {
							JSONObject item = dataAry.getJSONObject(0);
							long userId = item.getLong("userId");
							int rType = item.getInt("rtype");
							mFriendState = item.getInt("rtype");
							// 根据返回结果的Userid判断好友关系
							if (App.getUserId().equals(String.valueOf(userId))) {
								mFriendState = rType;
							} else {
								if (rType == Constants.PERSON_RELATION_FOLLOW) {
									mFriendState = Constants.PERSON_RELATION_FOLLOWED;
								} else if (rType == Constants.PERSON_RELATION_FOLLOWED) {
									mFriendState = Constants.PERSON_RELATION_FOLLOW;
								} else {
									mFriendState = rType;
								}
							}
						} else {
							mFriendState = Constants.PERSON_RELATION_EMPTY;
						}
					} else {
						mFriendState = Constants.PERSON_RELATION_EMPTY;
					}

				} else {
					mFriendState = Constants.PERSON_RELATION_EMPTY;
				}

				return flag;

			}

			@Override
			public void success(Integer result) {
				if (Constants.SUCCESS_MSG == result) {
					if (mFriendState == Constants.PERSON_RELATION_FRIEND && phoneVisible == 2) {//如果互相关注且规则是好友可见，则号码可见
						personalPhoneRL.setVisibility(View.VISIBLE);
					} else {
						personalPhoneRL.setVisibility(View.GONE);
					}
				} else {
					Toast.makeText(PersonalActivity.this, R.string.api_error, Toast.LENGTH_SHORT).show();
					return;
				}

			}

			@Override
			public void exception(Exception exception) {
				exception.printStackTrace();
			}

		});
    }
	
	@Override
	public void initData() {
		/**
		 * 隐藏年龄和性别预留位，在有获取数据之后，再显示预留位出来。
		 * */
		getUserInfo();
		getFriendState();
		getUserDyNamicData();
	}

    /**
	 * 是否显示我的个人年龄，性别布局
	 * */
	private void setMyInfoLL(){	
		/**
		 * 处理获取不到数据的时候，会出现个人信息预留的位置背景
		 * */
		/*if(mPersonalInfo == null){//暂时隐藏年龄，性别
			myInfoLL.setVisibility(View.GONE);
		} else {
		    if (mPersonalInfo.usertype == Constants.USER_TYPE_STUDENT) {
		        myInfoLL.setVisibility(View.VISIBLE);
		    }
		} */
	}
	
	/**
	 * <p> 获取用户信息 </p>
	 */
	private void getUserInfo()
	{
		//菊花出现
		setLoadLayout();
		String url = Constants.GET_USER_URL;
		Map<String ,Object> params = new HashMap<String,Object>();
		params.put("userid", friendid);
		
		Log.i(TAG, url);
		
		TmingHttp.asyncRequest(url, params, new RequestCallback<Integer>() {

			@Override
			public Integer onReqestSuccess(String respones) throws Exception {
				Log.e(TAG, respones);
				final int result = jsonParsing(respones);
				mPersonalInfo = PersonalInfo.personalInfoJSONPasing(respones);
                /**
                 * 增加
                 */
				return result;
			}

			@Override
			public void success(Integer respones) {
				//菊花消失
				dismissLoadLayout();
				if(respones != Constants.SUCCESS_MSG){
					Toast.makeText(PersonalActivity.this, R.string.api_error, Toast.LENGTH_SHORT).show(); 
					return;
				}
				else
				{
					initUserData();
				}
			}

			@Override
			public void exception(Exception exception) {
				dismissLoadLayout();
				Toast.makeText(PersonalActivity.this, getResources().getString(R.string.net_error), Toast.LENGTH_SHORT).show(); 
				setMyInfoLL();
				exception.printStackTrace();
			}
			
		});
	}

    /**
     * <p>设定用户状态</p>
     */
	private void initUserData()
	{
		if (mPersonalInfo.usertype == 0) {  //教师界面
			myTeacherIcon.setVisibility(View.VISIBLE);
			myTeacherMarkTv.setVisibility(View.VISIBLE);
		} else{
			setMyInfoLL();
		}
		
		mNameTV.setText(mPersonalInfo.getUserName());
		ImageUtil.asyncLoadImage(mUsreImgCIV, mPersonalInfo.getPhotoUrl(), R.drawable.all_use_icon_photo);
		
		new Thread() {
			public void run() {
				final Bitmap bitmap = ImageUtil.returnBitMap(mPersonalInfo.getUserCoverImgUrl());
				App.getAppHandler().post(new Runnable() {

					@Override
					public void run() {
						if (bitmap == null)
						{
							mPersonalCoverRL.setBackgroundResource(R.drawable.all_use_icon_bg);
							return;
						}
						mPersonalCoverRL.setBackgroundDrawable(new BitmapDrawable(bitmap));
					}
				});
			};
		}.start();

		if (mPersonalInfo.usertype != Constants.USER_TYPE_STUDENT) {//非学生不显示专业班级
            mySpecialtyTv.setVisibility(View.INVISIBLE);
            mClassTV.setVisibility(View.INVISIBLE);
        } else {
    		//教学点-专业
    		if(!StringUtils.isEmpty(mPersonalInfo.teachingpointName) && !StringUtils.isEmpty(mPersonalInfo.specialtyName))
    		{
    		    mySpecialtyTv.setText(mPersonalInfo.teachingpointName + "-" + mPersonalInfo.specialtyName );
    		}
    		//班级
    		if(!StringUtils.isEmpty(mPersonalInfo.getClassName()))
    		{
    			mClassTV.setText(mPersonalInfo.getClassName());
    		}
        }
		if (App.getUserType() != 0 && App.getUserType() != 1) {//非教师需要进行判断
		
    		if (mPersonalInfo.phoneVisible == 1) { //仅教师可见
                if (App.getUserType() == 0 || App.getUserType() == 1) {
                    personalPhoneRL.setVisibility(View.VISIBLE);
                } else {
                    personalPhoneRL.setVisibility(View.GONE);
                }
            } else if (mPersonalInfo.phoneVisible == 2) {   //TODO 好友可见
                phoneVisible = 2;
                getPhoneVisibleState();
                
            } else if (mPersonalInfo.phoneVisible == 3) {//所有人可见
                personalPhoneRL.setVisibility(View.VISIBLE);
            }
		} else {//教师身份全部可见
		    personalPhoneRL.setVisibility(View.VISIBLE);
		}
		mPhoneDataTV.setText(mPersonalInfo.getPhone());
		setTextView(mMajorDataTV, getPersonalWork(mPersonalInfo.getProfession(), mPersonalInfo.getIndustry()), R.string.text_empty_work);
		setTextView(mCompanyDataTV, mPersonalInfo.getCompany(), R.string.text_empty_company);
		
		mDyNamicNumTV.setText(""+mPersonalInfo.getTimelineNum());
	
		setTextView(mHobbyDataTV, mPersonalInfo.getHobby(), R.string.text_empty_hobby);
		setTextView(mDescDataTV, mPersonalInfo.getExplanation(), R.string.text_empty_explanation2);
		
		setTextView(mSchoolDataTV, getSchooleText(mPersonalInfo.getPrimarySchool(), mPersonalInfo.getJuniormiddleSchool(), mPersonalInfo.getJuniormiddleSchool(), mPersonalInfo.getUniversity()), R.string.text_empty_school);
		

	}
	
	private void getUserDyNamicData() {
		setLoadLayout();
		TmingHttp.asyncRequest(ServerApi.Timeline.getMyTimelineListUrl(friendid, 1, 1), null, new RequestCallback<Timeline>() {

			@Override
			public Timeline onReqestSuccess(String respones) throws Exception {
				JSONArray jsonArray = new JSONObject(respones).getJSONArray("data");
				if (jsonArray.length() > 0) {
					Timeline timeline = new Timeline();
					timeline.initWithJsonObject(jsonArray.getJSONObject(0));
					return timeline;
				}
				return null;
			}

			@Override
			public void success(Timeline result) {
				//菊花消失
				dismissLoadLayout();
				mTimeline = result;
				if(null != mTimeline) {
					String contentVal = mTimeline.getContent();
					if (mTimeline.getImages().size() != 0) {
						try {
							imageCacheTool.asyncLoadImage(new URL(mTimeline.getImages().get(0).getUrlString()), mDynamicImg);
						} catch (MalformedURLException e) {
							e.printStackTrace();
						}
					}
					mDyNamicDataTV.setText(InputBottomBar.parseFaceByText(mContext, contentVal, (int) (mDyNamicDataTV.getTextSize())));
					mDyNamicDateTV.setText(TimelinePostTimeAgoUtil.TimelinePostTimeAgo(mTimeline.getPostTime()));
				} else {
					mDyNamicDataTV.setText(R.string.dynamic_empty);
				}
			}

			@Override
			public void exception(Exception exception) {
				dismissLoadLayout();
				Toast.makeText(PersonalActivity.this, getResources().getString(R.string.net_error), Toast.LENGTH_SHORT).show();
				exception.printStackTrace();
			}
			
		});
		
	}
	
	
	@Override
	public void addListener(){
		my_detail_follow_btn.setOnClickListener(new myBtnClickListener());
		my_detail_send_msg_btn.setOnClickListener(new myBtnClickListener());
		my_detail_send_flower_btn.setOnClickListener(new myBtnClickListener());
		mPersonalDyNamicRL.setOnClickListener(new myBtnClickListener());
		personalPhoneRL.setOnClickListener(new myBtnClickListener());
		my_back_btn.setOnClickListener(new myBtnClickListener());
	}
	
	/**
	 * 改变按钮颜色
	 */
	/*private void changeBtnStyle(String tag){
		if(tag.equals("9")){
			send_9_flower_btn.setBackgroundDrawable(getResources().getDrawable(R.drawable.send_flower_btn_disabled));
			send_9_flower_btn.setTextColor(0x888888);
		}
	}*/
	
	/**
	 * Adds changing listener for wheel that updates the wheel label
	 * @param wheel the wheel
	 * @param label the wheel label
	 */
	private void addChangingListener(final WheelView wheel, final String label) {
		wheel.addChangingListener(new OnWheelChangedListener() {
			public void onChanged(WheelView wheel, int oldValue, int newValue) {
				wheel.setLabel(newValue != 1 ? label + "s" : label);
			}
		});
	}
	
	private class myBtnClickListener implements OnClickListener{

		@Override
		public void onClick(View v) {
			if(!Helper.isConn(PersonalActivity.this)){
				Toast.makeText(PersonalActivity.this, getResources().getString(R.string.please_open_network), Toast.LENGTH_SHORT).show();
				return;
			}
			switch(v.getId()){
			case R.id.my_detail_follow_btn:		//加关注
				//菊花出现
				setLoadLayout();
				String url = "";
				Map<String ,Object> params = new HashMap<String,Object>();
				params.put("userid", App.getUserId());
				params.put("followid", friendid);
				if(isFollow()){
					url = Constants.DELETE_FRIENDS_URL;
					Log.e(TAG, "取消关注");
				} else {
					url = Constants.ADD_FRIENDS_URL ;
					params.put("type", Constants.PERSON_RELATION_FOLLOW);
					Log.e(TAG, "加关注");
				}
				my_detail_follow_btn.setClickable(false);
                //发送关注请求
				changeFriendState(url, params);
				break;
			case R.id.my_detail_send_msg_btn:	//发消息
				Intent intent = new Intent(mContext, ChatActivity.class);
				intent.putExtra("toUserid", friendid);
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				mContext.startActivity(intent);
				
				break;
			case R.id.my_detail_send_flower_btn:	//送花
				// 获取剩余鲜花数
				getRestFlowers();
				FlowerDialog flowerDialog = new FlowerDialog(PersonalActivity.this);
				//弹出对话框
				flowerDialog.showSendFlowerDialog(rest_flowers);
				break;
			case R.id.personal_dynamic_rl:
				Intent personalDynamicsIntent = new Intent(mContext,PersonalDynamicsActivity.class);
				if(!TextUtils.isEmpty(mNameTV.getText().toString())){
					personalDynamicsIntent.putExtra("otherUserName", mNameTV.getText().toString());
				}else{
					personalDynamicsIntent.putExtra("otherUserName", "");
				}
				personalDynamicsIntent.putExtra("userid", friendid);
				startActivity(personalDynamicsIntent);
				break;
			case R.id.personal_phone_rl:	//直接拨打电话
				/*if (!AuthorityManager.getInstance().isTeacherAuthority() && !AuthorityManager.getInstance().isOfficeAuthority())
					break;*///改为所有身份都能打电话
				if (!mPersonalInfo.getPhone().equals("")) {
					Intent it = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + mPersonalInfo.getPhone()));
					startActivity(it);
				}
				break;
			case R.id.my_back_btn:
                //如果老状态和最后状态不一致
                if (mOldState!=mUserRelatedState){
                    //修改App状态字
                    App.mRelatedUserStateFlag=Constants.USER_RELATEDSTAT_CHANGED;
                }
                //返回
				PersonalActivity.this.finish();
			default:
				break;
			}
		}
	}
	/**
	 * 加关注或取消关注
	 * @param url
	 * @param params
	 */
	private void changeFriendState(String url,Map<String ,Object> params){
		
		Log.d(TAG, url);
		TmingHttp.asyncRequest(url, params, new TmingHttp.RequestCallback<Integer>() {

			@Override
			public Integer onReqestSuccess(String respones) throws Exception {
				return jsonParsing(respones);
			}

			@Override
			public void success(Integer respones) {
				//菊花消失
				dismissLoadLayout();
				if (respones != Constants.SUCCESS_MSG) {
					Toast.makeText(PersonalActivity.this, R.string.api_error, Toast.LENGTH_SHORT).show();
					return;
				} else {
					if (isFollow()) {
						my_detail_follow_btn.setText(getResources().getString(R.string.follow));
						mFriendState = Constants.PERSON_RELATION_EMPTY;
						mUserRelatedState = RELATED_NOTADDED;
						Toast.makeText(PersonalActivity.this, R.string.follow_del, Toast.LENGTH_SHORT).show();
					} else {
						my_detail_follow_btn.setText(getResources().getString(R.string.un_follow));
						mFriendState = Constants.PERSON_RELATION_FOLLOW;
						mUserRelatedState = RELATED_ADDED;
						Toast.makeText(PersonalActivity.this, R.string.follow_add, Toast.LENGTH_SHORT).show();
					}
				}
				my_detail_follow_btn.setClickable(true);
			}

			@Override
			public void exception(Exception exception) {
				dismissLoadLayout();
				Toast.makeText(PersonalActivity.this, getResources().getString(R.string.net_error), Toast.LENGTH_SHORT).show();
				exception.printStackTrace();
				my_detail_follow_btn.setClickable(true);
			}

		});
	}
	
	private int jsonParsing(String result){
		Log.d(TAG, result);
		int flag = 0;
		try {
			JSONObject jobj = new JSONObject(result);
			flag = jobj.getInt("flag");
			if(flag != 1 && !jobj.isNull("msg")){
				String msg = jobj.getString("msg");
				Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
			}
			Log.d(TAG, "=======");
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		return flag;
	}
	
	/**
	 * 获取剩余鲜花数
	 */
	private void getRestFlowers(){
		
		String url = Constants.GET_REST_FLOWER;
		Map<String,Object> map = new HashMap<String,Object>();
		map.put("userid", App.getUserId());
		
		TmingHttp.asyncRequest(url, map, new RequestCallback<Integer>() {

			@Override
			public Integer onReqestSuccess(String respones) throws Exception {
				JSONObject jobj = new JSONObject (respones);
				int flowerCount = jobj.getInt("flowerCount");
				return flowerCount;
			}

			@Override
			public void success(Integer respones) {
				rest_flowers = respones ;
			}

			@Override
			public void exception(Exception exception) {
				exception.printStackTrace();
			}
		});
	}

	
	/**
	 * ImageView的点击监听
	 */
	private OnClickListener mImageViewClickListener = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			popupImageView(v, (Bitmap) v.getTag());
		}
	};
	
	public void popupImageView(View v, Bitmap bitmap) {
		if (bitmap == null)
			return;
		if(mImagePopupWindow == null) {
			LayoutInflater flater = LayoutInflater.from(this);
			View popView = flater.inflate(R.layout.popup_img, null);
	
			TouchImageView pop_img = (TouchImageView) popView.findViewById(R.id.pop_image);
			pop_img.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					mImagePopupWindow.dismiss();
				}
			});
			this.zoomImageControlt(pop_img, popView);
			mImagePopupWindow = new PopupWindow(popView, LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT);
			mImagePopupWindow.setAnimationStyle(R.style.Animation_CommentStyle);
		}
		TouchImageView pop_img = (TouchImageView) (mImagePopupWindow.getContentView().findViewById(R.id.pop_image));
		pop_img.setImageBitmap(bitmap);
		int[] display = PhoneUtil.getDisplayWidthHeight(this);
		int bitHeight = bitmap.getHeight();
		int bitWidth = bitmap.getWidth();
		mImagePopupWindow.showAtLocation(v, Gravity.BOTTOM, (display[0] - bitWidth) / 2, (display[1] - bitHeight) / 2);
		mImagePopupWindow.update();
	}
	
	private void zoomImageControlt(final TouchImageView popImg, View popView) {
		ZoomControls zoom = (ZoomControls) popView
				.findViewById(R.id.zoom_controler);
		zoom.setPadding(0, 0, 10, 20);

		zoom.setOnZoomInClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				popImg.zoomOutCenter();
			}
		});
		zoom.setOnZoomOutClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				popImg.zoomInCenter();
			}
		});
	}
	
	private void setLoadLayout()
	{
		mLoadFlag++;
		if(mLoadFlag > 0)
		{
			loading_layout.setVisibility(View.VISIBLE);
		}
	}
	
	private void dismissLoadLayout()
	{
		mLoadFlag--;
		if(mLoadFlag <= 0)
		{
			loading_layout.setVisibility(View.GONE);
		}
	}
	
	private boolean isFollow()
	{
		boolean result = false;
		if(mFriendState == Constants.PERSON_RELATION_FOLLOW || mFriendState == Constants.PERSON_RELATION_FRIEND){
			result = true;
		} else{
			result = false;
		}
		return result;
	}
	
	private String getPersonalWork(String profession, String industry)
	{
		String workText = profession.trim();
		if (!StringUtils.isEmpty(workText)) {
		    workText += "\n";
		}
		
		if(!StringUtils.isEmpty(industry))
		{
			workText += industry;
		}

		return workText;
	}
	
	private void setTextView(TextView textView, String str, int stringId)
	{
		if(StringUtils.isEmpty(str)) {
		    if (stringId == R.string.text_empty_work) {
		        personalMajorRL.setVisibility(View.GONE);
		    } else if (stringId == R.string.text_empty_company) {
		        personalCompanyRL.setVisibility(View.GONE);
		    } else if (stringId == R.string.text_empty_hobby) {
		        personalHobbyRL.setVisibility(View.GONE);
		    } else if (stringId == R.string.text_empty_school) {
		        personalSchoolRL.setVisibility(View.GONE);
		    }
			textView.setText(stringId);
			textView.setTextColor(getResources().getColor(R.color.light_black));
		} else {
			textView.setText(str);
			textView.setTextColor(getResources().getColor(R.color.black));
		}
		
	}
	
	private String getSchooleText(String primaryschool, String juniormiddleschool, String seniormiddleschool, String university)
	{
	    if(StringUtils.isEmpty(mPersonalInfo.getUniversity()) && StringUtils.isEmpty(mPersonalInfo.getSeniormiddleSchool()) && 
	            StringUtils.isEmpty(mPersonalInfo.getJuniormiddleSchool()) && StringUtils.isEmpty(mPersonalInfo.getPrimarySchool())) {
	        personalSchoolRL.setVisibility(View.GONE);
	        return "";
	    }
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

    @Override
    public void onBackPressed() {
        //如果老状态和最后状态不一致
        if (mOldState!=mUserRelatedState){
            //修改App状态字
            App.mRelatedUserStateFlag= Constants.USER_RELATEDSTAT_CHANGED;
        }
        //返回
        PersonalActivity.this.finish();
        super.onBackPressed();
    }

}
