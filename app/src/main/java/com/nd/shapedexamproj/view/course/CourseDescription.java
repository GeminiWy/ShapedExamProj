package com.nd.shapedexamproj.view.course;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import com.nd.shapedexamproj.App;
import com.nd.shapedexamproj.AuthorityManager;
import com.nd.shapedexamproj.R;
import com.nd.shapedexamproj.model.Course;
import com.nd.shapedexamproj.util.Constants;
import com.nd.shapedexamproj.util.StringUtils;
import com.nd.shapedexamproj.util.UIHelper;
import com.nd.shapedexamproj.view.ScrollableTextView;
import com.tming.common.cache.net.TmingCacheHttp;
import com.tming.common.cache.net.TmingCacheHttp.RequestWithCacheCallBack;
import com.tming.common.util.PhoneUtil;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
/**
 * 课程简介
 * @author zll
 * create in 2014-3-7
 */
public class CourseDescription extends RelativeLayout{
	
	private static final String TAG = "CourseDescription";
	
	private Context context;
	private TextView course_title ,courseDecScoreTV,course_dec_score ,course_dec_period ,course_dec_student_num ,
					course_describe_tv;
	/**
	 * 责任老师
	 * */
//	private TextView responsTeacherTv;
	
	/**
	 * 责任老师
	 * */
	private TextView lecturerTeacherTv;
	
	private View view;
	private String courseid;
	private TmingCacheHttp cacheHttp ;
	
	private LinearLayout courseDecHeadTeacherLL, courseDecCourseTeacherLL, loadingLayout;
	private RelativeLayout error_layout;
	private Button error_btn ;
	 	
	public CourseDescription(Context context,String courseid) {
		super(context);
		this.context = context;
		this.courseid = courseid ;
		initComponent();
		addListener();
	}

	private void initData(){ 
	}
	
	private void initComponent(){
		view = LayoutInflater.from(context).inflate(R.layout.course_description, this);
		
		course_title = (TextView) view.findViewById(R.id.course_title);
		courseDecScoreTV = (TextView) view.findViewById(R.id.course_dec_score_tv);
		course_dec_score = (TextView) view.findViewById(R.id.course_dec_score);
		course_dec_period = (TextView) view.findViewById(R.id.course_dec_period);
		course_dec_student_num = (TextView) view.findViewById(R.id.course_dec_student_num);
		courseDecHeadTeacherLL = (LinearLayout) view.findViewById(R.id.course_dec_head_teacher_ll);
		courseDecCourseTeacherLL = (LinearLayout) view.findViewById(R.id.course_dec_course_teacher_ll);
		course_describe_tv = (TextView) view.findViewById(R.id.course_describe_tv);

		loadingLayout = (LinearLayout) view.findViewById(R.id.loading_layout);
		error_layout = (RelativeLayout) view.findViewById(R.id.error_layout);
		error_btn = (Button) view.findViewById(R.id.error_btn);
		requestData();
	}
	/**
	 * 请求网络数据
	 */
	private void requestData(){
		String url = Constants.COURSE_DESC ;
		Map<String ,Object> map = new HashMap<String,Object>();
		map.put("courseid", courseid);
		//if (AuthorityManager.getInstance().isStudentAuthority())
		map.put("userid", App.getUserId());
		cacheHttp = TmingCacheHttp.getInstance(context);
		cacheHttp.asyncRequestWithCache(url, map, new RequestWithCacheCallBack<Course>() {

			@Override
			public Course onPreRequestCache(String cache) throws Exception {
				return jsonParsing(cache);
			}

			@Override
			public void onPreRequestSuccess(Course data) {
				loadData(data);
			}

			@Override
			public Course onReqestSuccess(String respones) throws Exception {
				return jsonParsing(respones);
			}

			@Override
			public void success(Course cacheRespones, Course newRespones) {
				loadData(newRespones);
			}

			@Override
			public void exception(Exception exception) {
				loadingLayout.setVisibility(View.GONE);
				error_layout.setVisibility(VISIBLE);
			}
			
		});
	}
	/**
	 * 解析数据
	 */
	private Course jsonParsing(String result){
		Course course = null;
		int flag = 0;
//		List<String> coacher_list = new ArrayList<String>();
//		List<String> lecturer_list = new ArrayList<String>();
		
		JSONObject jobj;
		try {
			jobj = new JSONObject(result);
			flag = jobj.getInt("flag");
			if(flag != 1){
				App.dealWithFlag(flag);
				return null;
			}
			JSONObject infoObj = jobj.getJSONObject("res").getJSONObject("data").getJSONObject("info");
			if(infoObj != null ){
				course = new Course();
				course.course_name = infoObj.getString("name");
				if(!infoObj.isNull("credit")){
					course.credit = infoObj.getString("credit");
				}
				course.lessons = infoObj.getString("lessons");
				course.users = infoObj.getString("users");
				course.introduction = infoObj.getString("introduction");
				course.lecturer = infoObj.getString("lecturer");
				
				if(!infoObj.isNull("coacher")){
					JSONObject coacherObj = infoObj.getJSONObject("coacher");
					Iterator<String> coacherItr = coacherObj.keys();
					while(coacherItr.hasNext()){
						String coacherId = coacherItr.next();
						String coacher = coacherObj.getString(coacherId);
						course.coacherId = coacherId;
//						Log.e(TAG, "course.coacher := " + course.coacher);
//						Log.e(TAG, "coacher := " + coacher);
						course.coacher += coacher + " ";
						course.coacherMap.put(coacherId, coacher);
					}
				}
				if(!infoObj.isNull("counselor") ){
					JSONObject counselorObj = infoObj.getJSONObject("counselor");
					Iterator<String> counselorItr = counselorObj.keys();
					while(counselorItr.hasNext()){
						String counselorId = counselorItr.next();
						String counselor = counselorObj.getString(counselorId);
						course.counselor += counselor + " ";
						course.counselorId = counselorId;
						course.counselorMap.put(counselorId, counselor);
					}
				}
				
			}
			
		} catch (JSONException e) {
			e.printStackTrace();
		}
		 
		return course ;
	}
	/**
	 * 加载数据
	 */
	private void loadData(Course course){
		loadingLayout.setVisibility(GONE);
		error_layout.setVisibility(GONE);
		if(course != null){
			course_title.setText(course.course_name);
			if (StringUtils.isEmpty(course.credit)) {//如果学分为空则不显示
			    courseDecScoreTV.setVisibility(GONE);
			    course_dec_score.setVisibility(View.GONE);
			}
			course_dec_score.setText(course.credit);
			course_dec_period.setText(course.lessons);
			course_dec_student_num.setText(course.users);
			 
			Set<String> coacherIdSet = course.coacherMap.keySet();
			Iterator<String> coacherIdItr = coacherIdSet.iterator();

            MultiLineTextView multiTextView=new MultiLineTextView(context,course,courseDecHeadTeacherLL.getWidth()-20);
            courseDecHeadTeacherLL.addView(multiTextView);

			/*Set<String> lecturerIdSet = course.counselorMap.keySet();//辅导老师
			Iterator<String> lecturerIdItr = lecturerIdSet.iterator();
			while (lecturerIdItr.hasNext()) {
				String lecturerId = lecturerIdItr.next();
				String lecturer = course.counselorMap.get(lecturerId);
				
				TextView textView = createTextView(lecturer, lecturerId);
				courseDecCourseTeacherLL.addView(textView);
				
			}*/
			
			if(lecturerTeacherTv == null){
				lecturerTeacherTv = createNoListenerTextView(course.lecturer);//课程讲师
				courseDecCourseTeacherLL.addView(lecturerTeacherTv);
			}else{
				lecturerTeacherTv.setText(course.lecturer);
			}
			String txt = getResources().getString(R.string.no_course_description);
			course_describe_tv.setText(StringUtils.isEmpty(course.introduction) ? txt : course.introduction);
			
		}
	}
	/**
	 * 
	 * @param name	用户名
	 * @param userId	用户id
	 * @return
	 */
	private TextView createTextView (String name, final String userId) {
		TextView textView = new TextView (context);
		LayoutParams params =new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);//new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);

        params.leftMargin = 5;
		textView.setLayoutParams(params);
		textView.setTextColor(getResources().getColor(R.color.title_green));
		textView.setText(name);
		textView.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
			    if (AuthorityManager.getInstance()
                        .isInnerAuthority()) {
			        AuthorityManager.getInstance().showInnerDialog(context);
                    return;
                }
				UIHelper.showFriendInfoActivity(context, userId);
			}
		});
		return textView ;
	}

    /**
     * 新建跑马灯textview
     * @param name
     * @param userId
     * @return
     */
    private TextView createScrollTextView(String name,final String userId){

        ScrollableTextView textView = new ScrollableTextView (context,null);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        params.leftMargin = 5;
        //textView.setLayoutParams(params);
        textView.setTextColor(getResources().getColor(R.color.title_green));
        textView.setText(name);
        textView.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (AuthorityManager.getInstance()
                        .isInnerAuthority()) {
                    AuthorityManager.getInstance().showInnerDialog(context);
                    return;
                }
                UIHelper.showFriendInfoActivity(context, userId);
            }
        });
        return textView ;
    }
	/**
	 * 没有监听的textView
	 * @param name
	 * @return
	 */
	private TextView createNoListenerTextView (String name) {
		TextView textView = new TextView (context);
		LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		params.leftMargin = 5;
		textView.setLayoutParams(params);
		textView.setTextColor(getResources().getColor(R.color.light_black));
		textView.setText(name);
		return textView;
	}
	
	private void addListener(){
		error_btn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(PhoneUtil.checkNetworkEnable(context) != PhoneUtil.NETSTATE_ENABLE){
					Toast.makeText(context, getResources().getString(R.string.please_open_network),
							Toast.LENGTH_SHORT).show();
				} else {
					error_layout.setVisibility(GONE);
					loadingLayout.setVisibility(VISIBLE);
					requestData();
				}
			}
		});
	}
	
	
}
