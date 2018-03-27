package com.nd.shapedexamproj.view.course;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.*;

import com.nd.shapedexamproj.App;
import com.nd.shapedexamproj.AuthorityManager;
import com.nd.shapedexamproj.R;
import com.nd.shapedexamproj.activity.course.CourseDetailActivity;
import com.nd.shapedexamproj.util.Constants;
import com.nd.shapedexamproj.util.UIHelper;
import com.tming.common.util.Log;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * 课程详情页的非视频模块
 * 
 * @author zll create in 2014-3-23
 */
public class CourseDetailLayout extends RelativeLayout implements OnClickListener{
	
	private final static String TAG = "CourseDetailLayout";
	private Button btn_bottom_right;
	private ViewPager vPager;
	private List<View> viewsList;

	private ImageView desc_cursor, list_cursor, question_cursor, discuss_cursor;
	private List<ImageView> cursor_list;

	private String my_question, raise_question, do_test, do_homework;
	private int white, light_black, title_green;
	private TextView describe_title, ppt_title, question_title, discuss_title;
	private List<TextView> tv_list;

	private Context context;
	private String user_id = "0"; // 用户id
	private String course_id = "0"; // 课程id
	private String course_name = ""; // 课程名称
	private String proportion; // 已完成的作业比例

	private LinearLayout homeworkLayout;
	private Button myQuestionBtn, questionBtn;
	private WeakReference<CourseDetailActivity> mWeakReferenceActivity;

	/*
	 * public CourseDetailLayout(Context context, String course_id,String
	 * course_name) { super(context); this.context = context; this.course_id =
	 * course_id; this.course_name = course_name; }
	 */
	public CourseDetailLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.context = context;
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		initComponent();
		/* initData(); */
		addListener();
		initAuthority();
		
	}
	
	//初始化 授权
	private void initAuthority(){ 
		if (!AuthorityManager.getInstance().isStudentAuthority()){ 
			homeworkLayout.setVisibility(View.GONE);
			/*questionLayout.setVisibility(View.GONE);*/
		}
		 
		if (AuthorityManager.getInstance().isInnerAuthority()){ 
			discuss_cursor.setVisibility(View.GONE);
			discuss_title.setVisibility(View.GONE);
		}
	}
	
	private int isLink = 0;
	private BroadcastReceiver receiver = new BroadcastReceiver() {
		public void onReceive(Context context, Intent intent) {

			if (intent.getAction().equals(Constants.COURSE_DETAIL_ACTION)) {
				int link = intent.getIntExtra("is_link", isLink);
				if (link != isLink){
					isLink = link;
					initData();
					return;
				}
				
				String time = intent.getStringExtra("time");
				if (time != null && time.equals("1")) {
					course_id = intent.getStringExtra(Constants.COURSE_ID);
					course_name = intent.getStringExtra(Constants.COURSE_NAME);
					// 接收到广播后才开始加载数据
					initData();
				} else if (time != null && time.equals("2")) {
					proportion = intent.getStringExtra("proportion");
					showBottomProportion();
				}

			} else if (intent.getAction().equals(Constants.COURSE_DETAIL_FOCUS_CURR_VIDEO)) {
				CourseDetailActivity activity = mWeakReferenceActivity.get();
				if (activity != null) {
					coursewareList.setCurrentPlayVideoId(activity.getVideoId(),true);
				}
			}
		};
	};

	public void initComponent() {


		// 分类标题
		describe_title = (TextView) findViewById(R.id.describe_title);
		ppt_title = (TextView) findViewById(R.id.ppt_title);
		question_title = (TextView) findViewById(R.id.question_title);
		discuss_title = (TextView) findViewById(R.id.discuss_title);

		// 分类游标
		desc_cursor = (ImageView) findViewById(R.id.desc_cursor);
		list_cursor = (ImageView) findViewById(R.id.list_cursor);
		question_cursor = (ImageView) findViewById(R.id.question_cursor);
	    discuss_cursor = (ImageView) findViewById(R.id.discuss_cursor);

	
		vPager = (ViewPager) findViewById(R.id.course_detail_vPager);

		// 底部按钮
		// btn_bottom_left = (Button) findViewById(R.id.btn_bottom_left);
		btn_bottom_right = (Button) findViewById(R.id.btn_bottom_right);
		// 文字资源
		my_question = getResources().getString(R.string.my_question);
		raise_question = getResources().getString(R.string.raise_question);
		do_test = getResources().getString(R.string.do_test);
		do_homework = getResources().getString(R.string.do_homework);
		// 颜色资源
		white = getResources().getColor(R.color.white);
		light_black = getResources().getColor(R.color.light_black);
		title_green = getResources().getColor(R.color.title_green);

		homeworkLayout = (LinearLayout) findViewById(R.id.course_detail_bottom);
		/*questionLayout = (LinearLayout) findViewById(R.id.course_detail_question_ll);*/
		/*findViewById(R.id.course_detail_my_question_btn).setOnClickListener(this);
		findViewById(R.id.course_detail_question_btn).setOnClickListener(this);*/
		
		
		// 接收参数
		user_id = App.getUserId();
		// 注册广播
		IntentFilter selectFilter = new IntentFilter();
		selectFilter.addAction(Constants.COURSE_DETAIL_ACTION);
		selectFilter.addAction(Constants.COURSE_DETAIL_FOCUS_CURR_VIDEO);
		context.registerReceiver(receiver, selectFilter);
		
		tv_list = new ArrayList<TextView>();
		tv_list.add(describe_title);
		tv_list.add(ppt_title);
		tv_list.add(question_title);
	
		
		cursor_list = new ArrayList<ImageView>();
		cursor_list.add(desc_cursor);
		cursor_list.add(list_cursor);
		cursor_list.add(question_cursor);
						
				
//				if (!AuthorityManager.getInstance().isInnerAuthority()) {
//				    cursor_list.add(discuss_cursor);
//					tv_list.add(discuss_title);
//				}
		
	}

	private void showBottomProportion() {
		if (proportion != null && !proportion.equals("")) { // 做作业按钮上的信息
			btn_bottom_right.setText(getResources().getString(R.string.do_homework) + "("
					+ proportion + ")");
		}
	}

	
	private CourseDescription courseDescription;
	private CoursewareList   coursewareList;
	private CourseAnswer    courseAnswer;
	private CourseCoach     courseCoach;
	private CourseDetailPagerAdapter courseDetailPagerAdapter;
	private myPageChangeListener smyPageChangeListener;
	
	/**
	 * 是否在修
	 * @return
	 */
	private boolean isLearning(){
		return isLink == 1;
	}
	
	public void initData() {
		if (viewsList == null){
			viewsList = new ArrayList<View>();
		}else {
			viewsList.clear();
		}
		
		if (courseDescription == null){
			courseDescription = new CourseDescription(context, course_id);
		}
		
		if (coursewareList == null){
			coursewareList = new CoursewareList(context, course_id, course_name);
			CourseDetailActivity activity = mWeakReferenceActivity.get();
			if (activity != null) {
			    coursewareList.setCurrentPlayVideoId(activity.getVideoId(),true);
			}
		}
		
		if (courseAnswer == null){
			courseAnswer = new CourseAnswer(context, course_id, course_name);
		}
		
		viewsList.add(courseDescription);
		viewsList.add(coursewareList);
		viewsList.add(courseAnswer);
		Log.e("CourseDetail", "course_id : "+ course_id);
		//匿名或者不是在修课程//TODO （暂时隐藏辅导讨论区）
		/*if (!AuthorityManager.getInstance().isInnerAuthority() && isLearning()) {
			if (courseCoach == null)
				courseCoach = new CourseCoach(context, course_id, course_name);
			viewsList.add(courseCoach);
		    cursor_list.add(discuss_cursor);
			tv_list.add(discuss_title);
			discuss_cursor.setVisibility(View.VISIBLE);
			discuss_title.setVisibility(View.VISIBLE);
		}else {
			//viewsList.remove(courseCoach)
			cursor_list.remove(discuss_cursor);
			tv_list.remove(discuss_title);
			discuss_cursor.setVisibility(View.GONE);
			discuss_title.setVisibility(View.GONE);
		}*/

		if (courseDetailPagerAdapter == null)
			courseDetailPagerAdapter = new CourseDetailPagerAdapter();
		if (smyPageChangeListener == null)
			smyPageChangeListener = new myPageChangeListener();
		
		vPager.setAdapter(courseDetailPagerAdapter);
		vPager.setCurrentItem(1); // 开始默认选中第二页
		vPager.setOnPageChangeListener(smyPageChangeListener);
		
		/*showHomeWorkLayout();*///TODO 暂时屏蔽作业入口
	}

	
	private void addListener() {
		// viewpager头部监听
		describe_title.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (vPager.getCurrentItem() != 0) {
					vPager.setCurrentItem(0, true);
				}
			}
		});
		ppt_title.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (vPager.getCurrentItem() != 1) {
					vPager.setCurrentItem(1, true);
				}
			}
		});
		question_title.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (vPager.getCurrentItem() != 2) {
					vPager.setCurrentItem(2, true);
				}
			}
		});
		discuss_title.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (vPager.getCurrentItem() != 3) {
					vPager.setCurrentItem(3, true);
				}
			}
		});

		btn_bottom_right.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				UIHelper.showHomeworkListActivity(context,course_id);
			}
		});

	}

	/**
	 * 改变标题颜色
	 */
	private void changeTitleColor(int position) {
		for (int i = 0; i < tv_list.size(); i++) {
			if (i == position) {
				tv_list.get(i).setTextColor(title_green);
				cursor_list.get(i).setBackgroundColor(getResources().getColor(R.color.title_green));
			} else {
				tv_list.get(i).setTextColor(light_black);
				cursor_list.get(i).setBackgroundDrawable(null);
			}
		}
	}

	/**
	 * 课程详情页viewPager适配器
	 * 
	 * @author zll create in 2014-3-6
	 */
	private class CourseDetailPagerAdapter extends PagerAdapter {

		@Override
		public int getCount() {
			return viewsList.size();
		}

		@Override
		public boolean isViewFromObject(View arg0, Object arg1) {

			return arg0 == arg1;
		}

		@Override
		public Object instantiateItem(ViewGroup container, int position) {
			container.addView(viewsList.get(position), 0);

			return viewsList.get(position);
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			container.removeView((View) object);
		}
	}

	/**
	 * 页面切换监听
	 */
	private class myPageChangeListener implements OnPageChangeListener {

		// int one = offset * 2 + bmpW;// 页卡1 -> 页卡2 偏移量
		@Override
		public void onPageScrollStateChanged(int arg0) {

		}

		@Override
		public void onPageScrolled(int arg0, float arg1, int arg2) {

		}

		@Override
		public void onPageSelected(int arg0) {
			changeTitleColor(arg0);

			/*
			 * Animation animation = new TranslateAnimation(one*currIndex,
			 * one*arg0, 0, 0);//显然这个比较简洁，只有一行代码。 currIndex = arg0;
			 * animation.setFillAfter(true);// True:图片停在动画结束位置
			 * animation.setDuration(200);
			 * image_cursor.startAnimation(animation);
			 */ 
			//非学生
			if (!AuthorityManager.getInstance().isStudentAuthority()){ 
				return;
			} 
			
			switch (arg0) {
			case 0: // 课程简介 
				/*showHomeWorkLayout();*///TODO 暂时屏蔽作业入口
				break;
			case 1: // 课件列表 
				/*showHomeWorkLayout();*///TODO 暂时屏蔽作业入口
				break;
			case 2: // 课程答疑  
				homeworkLayout.setVisibility(View.GONE);
				break;
			case 3: // 辅导讨论区 
				homeworkLayout.setVisibility(View.GONE);
				break;
			} 
		}

	}

	private void showHomeWorkLayout(){ 
		if (AuthorityManager.getInstance().isStudentAuthority() && isLearning()){
			homeworkLayout.setVisibility(View.VISIBLE);
		}else { 
			homeworkLayout.setVisibility(View.GONE);
		}
	}
	
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		/*case R.id.course_detail_my_question_btn:
			Intent myIt = new Intent(context, MyQuestionsActivity.class);
			myIt.putExtra(Constants.COURSE_ID, course_id);
			context.startActivity(myIt);
			break;
		case R.id.course_detail_question_btn:
			UIHelper.showCourseNewQuestionActivity(context, course_id, course_name, "", "");
			break;*/
		}
	}
	
	@Override
	protected void onDetachedFromWindow() {
	    super.onDetachedFromWindow();
    	/*context.unregisterReceiver(receiver);*/
	}
	
	/**
	 * <p>课件列表视图</P>
	 * @return
	 */
	public CoursewareList getCouseWareListView() {
	    return coursewareList;
	}

	/**
	 * 设置所在Activity
	 * @param activity
	 */
	public void setActivity(CourseDetailActivity activity) {
        this.mWeakReferenceActivity = new WeakReference<CourseDetailActivity>(activity);
    }
	
	public void onActivityDestroy() {
	    getContext().unregisterReceiver(receiver);
	    
	    Intent intent = new Intent();
        intent.setAction("kd.coursedetail.unregister");
        getContext().sendBroadcast(intent);
	}
}
