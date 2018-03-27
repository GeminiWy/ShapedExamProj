package com.nd.shapedexamproj.activity.major;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nd.shapedexamproj.R;
import com.nd.shapedexamproj.activity.BaseActivity;
import com.nd.shapedexamproj.util.Constants;
import com.nd.shapedexamproj.view.major.MajorChargeStandard;
import com.nd.shapedexamproj.view.major.MajorCourseList;
import com.nd.shapedexamproj.view.major.MajorDescription;
import com.nd.shapedexamproj.view.major.MajorRuleList;
import com.tming.common.cache.net.TmingCacheHttp;
import com.tming.common.cache.net.TmingCacheHttp.RequestWithCacheCallBack;
import com.tming.common.util.Helper;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 专业详情页
 * @author wyl
 * create in 2014.03.18
 */
public class MajorDetailActivity extends BaseActivity {
	private RelativeLayout common_head_layout;
	
	private Button common_head_right_btn ;
	private ImageView commonheader_left_iv;
	private TextView commonheader_title_tv;
	private ViewPager vPager;
	private List<View> viewsList ;
	
	private TextView introduction_title,rule_title,course_title,cost_title;
	private TextView tv_major_levels_content,tv_major_year_content,tv_major_graduate_credit_content,tv_major_educational_credit_content;
	private List<TextView> tv_list;
	private View loadingView;
	
	private int white,light_black,title_green;
	private String major_id = "";//专业id
	private String major_name = "";//专业名
	private ImageView desc_cursor ,list_cursor,question_cursor,discuss_cursor;
	private List<ImageView> cursor_list ;
	private Drawable cursor ;
	private int bmpW = 0,offset = 0; // 游标的宽度和偏移量
	private int currIndex = 1;// 当前页卡编号 
	
	//add by Abay Zhuang
	private RelativeLayout sign_up_rl;
	private Button  sign_up_btn;
	
	private TmingCacheHttp cacheHttp ;
	private int pageNum = 1,pageSize = 10;
	/**
	 * 用来判断从那个地方进来
	 * */
	private String from = "";
	@Override
	public int initResource() {
		return R.layout.major_detail_activity;
	}

	@Override
	public void initComponent() {
		from = getIntent().getStringExtra("comefrom");
		//设置头部透明色
		common_head_layout = (RelativeLayout) findViewById(R.id.major_detail_head);
		common_head_layout.setBackgroundColor(getResources().getColor(R.color.transparent));
		//头部按钮
//		common_head_left_btn = (Button) findViewById(R.id.common_head_left_btn);
		commonheader_left_iv = (ImageView) findViewById(R.id.commonheader_left_iv);
		commonheader_title_tv = (TextView) findViewById(R.id.commonheader_title_tv);
		
		common_head_right_btn = (Button) findViewById(R.id.common_head_right_btn);
		common_head_right_btn.setVisibility(View.INVISIBLE);
		//分类标题
		introduction_title = (TextView) findViewById(R.id.introduction_title);
		rule_title = (TextView) findViewById(R.id.rule_title);
		course_title = (TextView) findViewById(R.id.course_title);
		cost_title = (TextView) findViewById(R.id.cost_title);
		
		tv_list = new ArrayList<TextView> ();
		tv_list.add(introduction_title);
		tv_list.add(rule_title);
		tv_list.add(course_title);
		tv_list.add(cost_title);
		
		cursor = getResources().getDrawable(R.drawable.line);
		//分类游标
		desc_cursor = (ImageView) findViewById(R.id.desc_cursor);
		list_cursor = (ImageView) findViewById(R.id.list_cursor);
		question_cursor = (ImageView) findViewById(R.id.question_cursor);
		discuss_cursor = (ImageView) findViewById(R.id.discuss_cursor);
		
		cursor_list = new ArrayList<ImageView>();
		cursor_list .add(desc_cursor);
		cursor_list.add(list_cursor);
		cursor_list.add(question_cursor);
		cursor_list.add(discuss_cursor);
		
		vPager  = (ViewPager) findViewById(R.id.major_detail_vPager);
		
		//颜色资源
		white = getResources().getColor(R.color.white);
		light_black = getResources().getColor(R.color.light_black);
		title_green = getResources().getColor(R.color.title_green);
		
		//专业基本信息
		tv_major_levels_content = (TextView)findViewById(R.id.major_levels_content);
		tv_major_year_content = (TextView)findViewById(R.id.major_year_content);
		tv_major_graduate_credit_content = (TextView)findViewById(R.id.major_graduate_credit_content);
		tv_major_educational_credit_content = (TextView)findViewById(R.id.major_educational_credit_content);
	
		loadingView = findViewById(R.id.loading_layout);	
		loadingView.setVisibility(View.VISIBLE);
	}
	
	/**
	 * 
	 *@Title:  setAuthority
	 *@Description: 配置权限信息
	 *@Since: 2014-5-19下午4:48:49
	 */
	public void initAuthoriy(){
		sign_up_rl = (RelativeLayout) findViewById(R.id.major_signup_rl);
		sign_up_btn = (Button) findViewById(R.id.major_signup_btn);
//		if (Constants.USER_TYPE == Constants.USER_TYPE_INNER){
//			sign_up_rl.setVisibility(View.VISIBLE);
//			sign_up_btn.setOnClickListener(new OnClickListener() {
//				
//				@Override
//				public void onClick(View v) {
//					
//					UIHelper.showOnlineRegisterFirst(MajorDetailActivity.this);
//				}
//			});
//		}else {
			sign_up_rl.setVisibility(View.GONE);
//		}
		
		
	}
	
	
	@Override
	public void initData() {
		/*Intent intent = getIntent();
		String major_id = intent.getStringExtra(Constants.MAJOR_ID);
		this.major_id = major_id;
		this.major_id = "5306e5bb5aec22e5";*/ 
		
		this.major_name = Constants.leftInnerName;
		this.major_id = Constants.leftInnerId; 
		
		if(this.major_id != null && !"".equals(this.major_id)){
			loadingView.setVisibility(View.VISIBLE);
			requestData();
		} 
	}
	
	/**
	 * 请求网络数据
	 */
	private void requestData(){
		String url = Constants.PROFRESSIONAL_INFO_URL ;
		Map<String ,Object> map = new HashMap<String,Object>();
		map.put("specialtyid", major_id);
		cacheHttp = TmingCacheHttp.getInstance(this);
		cacheHttp.asyncRequestWithCache(url, map, new RequestWithCacheCallBack<String>(){

			@Override
			public String onPreRequestCache(String cache)
					throws Exception {
				return jsonParsing(cache);
			}

			@Override
			public void onPreRequestSuccess(String data) {
				loadData(data);
			}

			@Override
			public String onReqestSuccess(String respones)
					throws Exception {
				return jsonParsing(respones);
			}

			@Override
			public void success(String cacheRespones,
					String newRespones) {
				loadData(newRespones);
			}

			@Override
			public void exception(Exception exception) {
				loadingView.setVisibility(View.GONE);
			}
		});
	}
	
	/**
	 * 网络请求成功后，加载数据
	 * @param data
	 */
	private void loadData(String data){
		//String dataStr = "{\"flag\":1,\"res\":{\"code\":1,\"data\":{\"info\":{\"name\":\"计算机科学与技术\",\"type\":\"本科\",\"years\":3,\"credit\":[76,80],\"charge\":[200,90],\"introduction\":\"本专业为农村培养社会主义建设需要的，德、智、体全面发展的，能从事乡镇企业管理等工作的高等应用型专门人才。掌握本专业所需的基础知识，具有较强的自学能力及分析、解决本专业实际问题和组织生产的初步能力。培养目标:培养掌握农村经济发展和企业经营管理的基本理论和操作技能，从事乡镇企业管理的高级管理专门人才。学生核心能力:乡镇企业开办程序、产品市场营销、财务核算、经济法规、人力资源管理、资本运作应用技能。就业方向:乡镇生产、经营类企业的营销、财务和人力资源管理工作。...\",\"rules\":{\"1\":{\"name\":\"公共基础课\",\"credit\":[15,15]},\"2\":{\"name\":\"实践综合课\",\"credit\":[15,15]},\"3\":{\"name\":\"专业课\",\"credit\":[15,15]}}}}}}";
		String dataStr = "";
		dataStr = data;
		JSONObject resJson = null;
		JSONObject infoJSON = new JSONObject();
		try{
			try {
				resJson = new JSONObject(dataStr);
			} catch (JSONException e) {  
				e.printStackTrace();
			}
			
			if(resJson != null && resJson.getInt("flag") == 1){
			
				String major_name = "";
				String major_levels_value = "";
				String major_year_value = "";
				String major_graduate_credit_value = "";
				String major_educational_credit_value = "";
				String introduction = "";
				JSONArray ruleJsonArray = null;
				String graduate_credit_value = "";
				String educational_credit_value = "";
				String cost = "";
				try {
					if(resJson.getJSONObject("res").getInt("code") == 1){
						infoJSON = resJson.getJSONObject("res").getJSONObject("data").getJSONObject("info");
						
						major_name = this.major_name;//infoJSON.getString("name");
						major_levels_value = infoJSON.getString("type");
						major_year_value = infoJSON.getString("years") + getResources().getString(R.string.major_year_util);
						major_graduate_credit_value = infoJSON.getJSONArray("credit").get(0).toString() + getResources().getString(R.string.major_credit_util);
						major_educational_credit_value = infoJSON.getJSONArray("credit").get(1).toString() + getResources().getString(R.string.major_credit_util);;
						
						introduction = infoJSON.getString("introduction");
						ruleJsonArray = infoJSON.getJSONArray("rules");
						
						cost = getResources().getString(R.string.major_cost_value);
						graduate_credit_value = infoJSON.getJSONObject("charge").getString("credit_cash");
						educational_credit_value = "" + infoJSON.getJSONObject("charge").getInt("building_cash");
						cost = cost.replace("{0}", educational_credit_value);
						cost = cost.replace("{1}", graduate_credit_value);
					}
				} catch (JSONException e) {  
					e.printStackTrace();
				}
				
				//头部标题
				commonheader_title_tv.setText(major_name == null ? "" : major_name);
				//专业基本信息
				tv_major_levels_content.setText(major_levels_value);
				tv_major_year_content.setText(major_year_value);
				tv_major_graduate_credit_content.setText(major_graduate_credit_value);
				tv_major_educational_credit_content.setText(major_educational_credit_value);
				
				//加载标签页
				viewsList = new ArrayList<View>();
				if(introduction != null){
					introduction=introduction.replaceAll("&amp;", "&");
				}
				viewsList.add(new MajorDescription(this,introduction));
				viewsList.add(new MajorRuleList(this, ruleJsonArray));
				viewsList.add(new MajorCourseList(this,major_id));
				viewsList.add(new MajorChargeStandard(this,cost));
				
				vPager.setAdapter(new CourseDetailPagerAdapter());
				vPager.setCurrentItem(0);  //开始默认选中的标签
				vPager.setOnPageChangeListener(new myPageChangeListener()); 
			
			}
		} catch (Exception e) {  
		} 
		loadingView.setVisibility(View.GONE);
	}
	
	private String jsonParsing(String data){
		return data;
	}
	
	@Override
	public void addListener() {
		commonheader_left_iv.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(TextUtils.isEmpty(from)){
					backMain();
				}else{
					finish();
				}
			}
		});
		
		//viewpager头部监听
		introduction_title.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(vPager.getCurrentItem() != 0){
					vPager.setCurrentItem(0, true);
				}
			}
		});
		rule_title.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(vPager.getCurrentItem() != 1){
					vPager.setCurrentItem(1, true);
				}
			}
		});
		course_title.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(vPager.getCurrentItem() != 2){
					vPager.setCurrentItem(2, true);
				}
			}
		});
		cost_title.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(vPager.getCurrentItem() != 3){
					vPager.setCurrentItem(3, true);
				}
			}
		});
		
		
	}
	
	/**
	 * 改变标题颜色
	 * @author zll
	 * create in 2014-3-6
	 */
	private void changeTitleColor(int position){
		for(int i =0;i<tv_list.size();i++){
			if(i == position){
				tv_list.get(i).setTextColor(title_green);
				cursor_list.get(i).setBackgroundDrawable(cursor);
			} else {
				tv_list.get(i).setTextColor(light_black);
				cursor_list.get(i).setBackgroundDrawable(null);
			}
		}
	}
	
	/**
	 * 课程详情页viewPager适配器
	 * @author zll
	 * create in 2014-3-6
	 */
	private class CourseDetailPagerAdapter extends PagerAdapter{
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
			container.addView(viewsList.get(position),0);
			
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
	private class myPageChangeListener implements OnPageChangeListener{
		int one = offset * 2 + bmpW;// 页卡1 -> 页卡2 偏移量  
		@Override
		public void onPageScrollStateChanged(int arg0) {
			
		}
		@Override
		public void onPageScrolled(int arg0, float arg1, int arg2) {
			
		}
		@Override
		public void onPageSelected(int arg0) {
			changeTitleColor(arg0);
			switch(arg0){
			case 0:
				break;
			case 1:
				break;
			case 2:
				break;
			case 3:
				break;
			}
		}
	}
	
	/**
	 * 返回到主界面
	 */
	private void backMain() {
		Intent intent = new Intent();
		intent.setAction("backToShowLeftMenu");
		Helper.sendLocalBroadCast(this, intent);
	}
}
