package com.nd.shapedexamproj.fragment;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.*;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.*;

import com.nd.shapedexamproj.App;
import com.nd.shapedexamproj.R;
import com.nd.shapedexamproj.activity.SettingActivity;
import com.nd.shapedexamproj.activity.my.ExamActivity;
import com.nd.shapedexamproj.activity.my.MyFlowerActivity;
import com.nd.shapedexamproj.activity.my.MyNoteActivity;
import com.nd.shapedexamproj.activity.my.MyPaymentActivity;
import com.nd.shapedexamproj.activity.my.MyQuestionsActivity;
import com.nd.shapedexamproj.activity.my.MyScoreActivity;
import com.nd.shapedexamproj.activity.my.PersonalDynamicsActivity;
import com.nd.shapedexamproj.activity.my.SetImageActivity;
import com.nd.shapedexamproj.anim.RotateAnimation;
import com.nd.shapedexamproj.im.manager.UserManager;
import com.nd.shapedexamproj.util.Constants;
import com.nd.shapedexamproj.util.PhpApiUtil;
import com.nd.shapedexamproj.util.SharedPreferUtils;
import com.nd.shapedexamproj.util.UIHelper;
import com.nd.shapedexamproj.view.CircularImage;
import com.nd.shapedexamproj.view.PersonalScrollView;
import com.tming.common.AppManager;
import com.tming.common.cache.image.ImageCacheTool;
import com.tming.common.cache.net.TmingCacheHttp;
import com.tming.common.util.Helper;
import com.tming.common.util.PhoneUtil;
import com.tming.openuniversity.model.stu.Student;
import org.json.JSONObject;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * @项目名称: OpenUniversity
 * @版本号 V1.1.0
 * @所在包: com.tming.openuniversity.fragment
 * @文件名: MyFragment
 * @文件描述: “我的” tab页面
 * @创建人: Linlg
 * @创建时间: 2014/3/31
 * @修改人: xuwenzhuo
 * @修改时间: 2014/12/16
 * @Copyright: 2014 Tming All rights reserved.
 */

@SuppressLint("ValidFragment")
public class MyFragment extends BaseFragment implements RotateAnimation.InterpolatedTimeListener, PersonalScrollView.onTurnListener {
	
	private static final String TAG = "MyFragment";
	
	private final int BUTTON_EDIT = 1;
	private final int BUTTON_PHOTO = 2;
	private final int BUTTON_FLOWER = 3;
	private final int BUTTON_USER = 4;
	private final int BUTTON_QUESTION = 5;
	private final int BUTTON_NOTE = 6;
	private final int BUTTON_SCORE = 8;
	private final int BUTTON_PAYMENT = 9;
	private final int BUTTON_EXAM = 10;
	private final int HEAD_PHOTO = 11;
	private final int COVER_LAYOUT = 12;
	private final int TV_MY_DYNAMIC = 13;
	private final int BUTTON_SETTING = 14;
	private final int BUTTON_QUIT = 15;

	private View view;
	private ImageCacheTool imageCacheTool;
	private LinearLayout loading_layout ;
	private TextView mNameTv,mStunumTv,mIdnumTv,mJxdTv,mProTv, mClassTv;

	private Button quitBtn;
	private ImageView mySettingImg;
	private CircularImage photoIv,mUserImgBg;
	private String photoUrl;

	//add
	private TextView teacherTv;
	private Dialog dialog;

	// -------------------滚动视图--------
	private ImageView my_cover_iv;
	private ScrollView personalScrollView;

	// --------------------------------
	
	private SharedPreferences spf ;
	private SharedPreferences.Editor editor;
	public MyFragment() {
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		view = inflater.inflate(R.layout.my_detail, container, false);
		imageCacheTool = ImageCacheTool.getInstance();
	  	spf = getActivity().getSharedPreferences(Constants.SHARE_PREFERENCES_NAME, Context.MODE_PRIVATE);
		editor = spf.edit();

		IntentFilter filter = new IntentFilter();
		filter.addAction("com.tming.kd.my.resetimg");
		if (receiver != null) {
		    getActivity().registerReceiver(receiver, filter);
		}
		initView();
		return view;
	}
 	
	@Override
	public void onDestroy() {
	    super.onDestroy();
	    if (receiver != null) {
	        getActivity().unregisterReceiver(receiver);
	    }
	}
	
	/**
	 * 
	 *@Title:  initAuthoriy
	 *@Description: 配置权限信息
	 *@Since: 2014-5-19下午4:48:49
	 */
	public void initAuthoriy(){
		if (App.getUserType() != Constants.USER_TYPE_STUDENT) {
			teacherTv.setVisibility(View.VISIBLE);
		}
	}

    /**
     * 用户头像更改
     */
	private BroadcastReceiver receiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
            String newPhoto = intent.getStringExtra("newImageUrl");
            if (newPhoto != null) {
                photoUrl = newPhoto;
            }
            removeImageCacheByUrl(photoUrl);
			ImageCacheTool.asyncLoadImage(photoIv, photoUrl, R.drawable.all_use_icon_photo);
            ChangeCourseHeadPhoto(photoUrl);
		}
	};

    /**
     * 修改课程页面中的用户头像
     * @param photoStr 新头像地址
     */
    private void ChangeCourseHeadPhoto(String photoStr){
        Intent intent=new Intent();
        intent.setAction(StudentCourseFragment.MYPHOTO_CHANGE);
        intent.putExtra("newImageUrl",photoStr);
        getActivity().sendBroadcast(intent);
    }

	/**
	 * 初始化视图
	 */
	private void initView() {
		mNameTv = (TextView) view.findViewById(R.id._my_name_tv);
		mStunumTv = (TextView) view.findViewById(R.id.my_stunum_tv);
		mIdnumTv = (TextView) view.findViewById(R.id.my_idnum_tv);
		mJxdTv = (TextView) view.findViewById(R.id.my_jxd_tv);
		mProTv = (TextView) view.findViewById(R.id.my_pro_tv);
		mClassTv = (TextView) view.findViewById(R.id.my_cls_tv);
		teacherTv = (TextView) view.findViewById(R.id.my_teacher_mark_tv);

		view.findViewById(R.id.my_cover_rl).setOnClickListener(
				new OnClickListenerImpl(COVER_LAYOUT)); // TODO 加监听后下拉有问题
		
		mySettingImg = (ImageView) view.findViewById(R.id.my_setting_btn);
		mySettingImg.setOnClickListener(new OnClickListenerImpl(BUTTON_SETTING));

		mUserImgBg = (CircularImage) view.findViewById(R.id.my_photo_bg_iv);
		mUserImgBg.setImageDrawable(getResources().getDrawable(R.drawable.photo_bg_icon));
		photoIv = (CircularImage) view.findViewById(R.id.my_photo_iv);
		photoIv.setOnClickListener(new OnClickListenerImpl(HEAD_PHOTO));

		personalScrollView = (ScrollView) view.findViewById(R.id.my_detail_sv);
		my_cover_iv = (ImageView) view.findViewById(R.id.my_cover_iv);

		loading_layout = (LinearLayout) view.findViewById(R.id.loading_layout);
		quitBtn = (Button) view.findViewById(R.id.setting_quit_btn);
		quitBtn.setOnClickListener(new OnClickListenerImpl(BUTTON_QUIT));
	}

	/**
	 * 点击事件监听
	 */
	private class OnClickListenerImpl implements OnClickListener {
		private int which;

		public OnClickListenerImpl(int which) {
			this.which = which;
		}

		@Override
		public void onClick(View v) {
			switch (which) {
			case HEAD_PHOTO: // 更换头像 TODO 形考没有换头像功能
				/*Intent it = new Intent(getActivity(), SetImageActivity.class);
				it.putExtra("title", "更换头像");
				startActivityForResult(it, 1000);*/
				break;
			case COVER_LAYOUT: // 更换背景
//				Intent it1 = new Intent(context, SetImageActivity.class);
//				it1.putExtra("title", "更换封面");
//				startActivityForResult(it1, 1000);
				break;
			case BUTTON_EDIT:
				// editLayout.setVisibility(View.VISIBLE);
				/* showEditDialog(); */
				// startActivity(new Intent(context,
				// DownloadPageActivity.class));
				break;

			case BUTTON_PHOTO:
				break;

			case BUTTON_FLOWER:
				Intent intent = new Intent(getActivity(), MyFlowerActivity.class);
				startActivity(intent);
				break;

			case BUTTON_USER:
			    UIHelper.showEditPersonalActivity(getActivity());
				break;

			case BUTTON_QUESTION:
				 Intent questionIntent = new Intent(getActivity(), MyQuestionsActivity.class);
				 startActivity(questionIntent);
				break;
			case BUTTON_NOTE:
				System.out.println("BUTTON_NOTE");
				 Intent noteIt = new Intent(getActivity(), MyNoteActivity.class);
				 startActivity(noteIt);
				break;
			case BUTTON_SCORE: // 成绩查询
				Intent intent2 = new Intent(getActivity(), MyScoreActivity.class);
				startActivity(intent2);
				break;
			case BUTTON_PAYMENT: // 我的缴费
				Intent paymentIntent = new Intent(getActivity(),MyPaymentActivity.class);
				startActivity(paymentIntent);
				break;
			case BUTTON_EXAM: // 考试安排
				App.getAppContext().startActivity(
						new Intent(App.getAppContext(), ExamActivity.class)
								.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
				break;
			case TV_MY_DYNAMIC:
				Intent personalDynamicsIntent = new Intent(getActivity(), PersonalDynamicsActivity.class);
				personalDynamicsIntent.putExtra("userid", App.getUserId());
				personalDynamicsIntent.putExtra("otherUserName", "");
				startActivity(personalDynamicsIntent);
				break;
			case BUTTON_SETTING:
			    Intent settingIntent = new Intent(App.getAppContext(),SettingActivity.class);
                startActivity(settingIntent);
			    break;
			case BUTTON_QUIT:
				quitOpt();
				break;
			}
		}
	}

	private void quitOpt(){
		SharedPreferences preferences = SharedPreferUtils.getSharedPreferences(getActivity());
		//清理IM连接
		UserManager.disconnectAccount();

		SharedPreferences.Editor pfcEdit = preferences.edit();
		pfcEdit.putString("userId", "");
		pfcEdit.putString("stuId", "");
		pfcEdit.putInt("userType", -1);
		pfcEdit.commit();
		App.setsUserType(-1);
		App.clearUserId();
		//跳转到登录页，并且把所有其他界面销毁

		UIHelper.showLogin(getActivity());
		AppManager.getAppManager().finishAllActivity();
	}

	/**
	 * 编辑框
	 */
	private void showEditDialog() {
		View editView = LayoutInflater.from(getActivity()).inflate(R.layout.my_edit_dialog, null);
		dialog = new Dialog(getActivity(), R.style.dialog);
		editView.findViewById(R.id.my_cancel_btn).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				dialog.dismiss();
			}
		});
		editView.findViewById(R.id.my_changephoto_btn).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				dialog.dismiss();
				Intent it = new Intent(getActivity(), SetImageActivity.class);
				it.putExtra("title", "更换头像");
				startActivityForResult(it, 1000);
			}
		});
		editView.findViewById(R.id.my_changecover_btn).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				dialog.dismiss();
				Intent it = new Intent(getActivity(), SetImageActivity.class);
				it.putExtra("title", "更换封面");
				startActivityForResult(it, 1000);
			}
		});

		dialog.setContentView(editView);
		dialog.show();
	}

	/**
	 *  请求形考用户信息
	 */
	private void requestPersonalInfo() {

		Map<String, Object> map = new HashMap<String, Object>();
		map.put("stu_id", App.getsStudentId());
		PhpApiUtil.sendData(Constants.XK_STU_INFO, map, new TmingCacheHttp.RequestWithCacheCallBackV2<Student>() {

			@Override
			public Student parseData(String data) throws Exception {
				JSONObject jsonObject = new JSONObject(data);
				Student student = new Student();
				student.initWithJsonObject(jsonObject);
				return student;
			}

			@Override
			public void cacheDataRespone(Student data) {
				if (getActivity() == null) {
					return;
				}
				if (data == null) {
					return;
				}
				setStudentInfoView(data);
			}

			@Override
			public void requestNewDataRespone(Student cacheRespones, Student newRespones) {
				if (getActivity() == null) {
					return;
				}

				if (newRespones == null) {
					return;
				}
				setStudentInfoView(newRespones);
			}

			@Override
			public void exception(Exception exception) {
				exception.printStackTrace();
			}

			@Override
			public void onFinishRequest() {
				loading_layout.setVisibility(View.GONE);
			}
		});
	}
	/**
	 *  在界面上设置学生信息
	 *
	 * @param studentInfo 学生个人信息
	 */
	private void setStudentInfoView(Student studentInfo) {
		try {
			photoUrl = studentInfo.avatar;
			mNameTv.setText(studentInfo.studentName);
			mStunumTv.setText(studentInfo.studentNum);
			mIdnumTv.setText(studentInfo.idNum);
			mJxdTv.setText(studentInfo.jxd);
			mProTv.setText(studentInfo.pro);
			mClassTv.setText(studentInfo.cls_name);

			if (PhoneUtil.checkNetworkEnable(getActivity()) != PhoneUtil.NETSTATE_DISABLE) {
				removeImageCacheByUrl(photoUrl);
			}
			imageCacheTool.asyncLoadImage(new URL(photoUrl), photoIv, R.drawable.all_use_icon_photo, 100, 100);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 根据url地址清除图片缓存
	 *
	 * @param photoUrl
	 */
	private void removeImageCacheByUrl(String photoUrl) {
	    ImageCacheTool cacheTool = ImageCacheTool.getInstance();
        cacheTool.removeCache(photoUrl);
        int playGroundCacheWidth = Helper.dip2px(getActivity(), PlaygroundFragment.LOAD_HEAD_IMAGE_WH_DP);
        cacheTool.removeCache(photoUrl, playGroundCacheWidth, playGroundCacheWidth);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

	}

	@Override
	public void onTurn() {

	}

	@Override
	public void interpolatedTime(float interpolatedTime) {

	}
	
	@Override
	public void onResume() {
		requestPersonalInfo();
		initAuthoriy();
		super.onResume();
	}
	/**
	 * 刷新界面
	 */
	public void refreshMyFragment() {
		requestPersonalInfo();
        initAuthoriy();
	}
}
