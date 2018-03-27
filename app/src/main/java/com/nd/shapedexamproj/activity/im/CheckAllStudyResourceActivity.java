package com.nd.shapedexamproj.activity.im;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.*;

import com.nd.shapedexamproj.R;
import com.nd.shapedexamproj.activity.BaseActivity;
import com.nd.shapedexamproj.activity.course.CourseListActivity;
import com.nd.shapedexamproj.model.course.CourseCategory;
import com.nd.shapedexamproj.net.ServerApi;
import com.nd.shapedexamproj.util.Constants;
import com.nd.shapedexamproj.util.StringUtils;
import com.nd.shapedexamproj.util.UIHelper;
import com.tming.common.cache.net.TmingCacheHttp;
import com.tming.common.net.TmingHttp;
import com.tming.common.util.Log;
import com.tming.common.view.RefreshableListView;
import com.tming.common.view.RefreshableListView.OnRefreshListener;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
 
 
/**
 * 查看所有学习资源列表
 * @author xiezz
 *
 */
public class CheckAllStudyResourceActivity extends BaseActivity {
	private static final String TAG = "CheckAllStudyResourceActivity"; 
	private final static int LOAD_TYPE_REFRESH = 1;
	private final static int LOAD_TYPE_MORE = 2; 
	/**
	 * 查看专业的默认ID
	 * */
	private final String DEFAULT_STUDY_RESOURCE_ID = "12131234"; 
	private Context context;
//	private CourseCategoryDBOperator ccDBOperator;
//	private MajorDBOperator cDBOperator;
	private LinkedList<CourseCategory> tempCategoryList;
	private RefreshableListView courseCategoryLV;
//	private RelativeLayout courseTabLay;
	private RelativeLayout allCourseItemLay;
	private RelativeLayout searchLay;
	private EditText searchEd;
	private TextView titleTv;
	private ImageView leftBackIv;
	private Button heardRightBtn;
	private LinearLayout courseCateLay;
//	private LinearLayout courseListTabLine;
	private TextView courseTabTV;
	private TextView courseNoDataTV;
	private LeftListAdapter categoryAdapter;  
	private int pageNo = 1; 
	private LinearLayout loadingView;
	private TmingCacheHttp cacheHttp;
	private Handler myHandler; 
		 
	@Override
	public int initResource() { 
		return R.layout.check_all_study_resource_activity;
	}

	@Override
	public void initComponent() { 
		//头部按钮 
		titleTv = (TextView) findViewById(R.id.commonheader_title_tv);
		titleTv.setText(R.string.check_all_study_resource_title);	
		heardRightBtn = (Button) findViewById(R.id.commonheader_right_btn);
		heardRightBtn.setVisibility(View.GONE);
		leftBackIv = (ImageView)findViewById(R.id.commonheader_left_iv);
		
//		courseTabLay = (RelativeLayout) findViewById(R.id.tab_course); 
		courseCateLay = (LinearLayout) findViewById(R.id.course_cate_lay); 
//		courseListTabLine = (LinearLayout) findViewById(R.id.courselist_tab_on); 
		courseTabTV = (TextView) findViewById(R.id.tab_course_tv); 
		allCourseItemLay = (RelativeLayout) findViewById(R.id.allcourse_item);
	//	nonDegreeGoodCousreItemLay = (RelativeLayout) v.findViewById(R.id.nondegree_good_course_item);
		courseCategoryLV = (RefreshableListView) findViewById(R.id.course_category_lv); 
		courseNoDataTV = (TextView) findViewById(R.id.no_data_tv);
		
		loadingView = (LinearLayout) findViewById(R.id.loading_layout);
		searchLay = (RelativeLayout) findViewById(R.id.courselist_search_rl);
		searchEd = (EditText) findViewById(R.id.search_keyword);
	}

	@Override
	public void initData() { 
		context = CheckAllStudyResourceActivity.this;
		tempCategoryList = new LinkedList<CourseCategory>();
		
		categoryAdapter = new LeftListAdapter();
		courseCategoryLV.setAdapter(categoryAdapter); 
//		ccDBOperator = CourseCategoryDBOperator.getInstance(context);
//		cDBOperator = MajorDBOperator.getInstance(context);
		cacheHttp = TmingCacheHttp.getInstance(context); 		 
		
		courseCategoryRefresh();	 
	}

	private void courseCategoryRefresh(){
		pageNo = 1;
		tempCategoryList.clear();
		loadingView.setVisibility(View.VISIBLE);
		courseCategoryLoad(LOAD_TYPE_REFRESH);
	}
	
	@Override
	public void addListener() {  
		allCourseItemLay.setOnClickListener(itemClickListener);
		//nonDegreeGoodCousreItemLay.setOnClickListener(itemClickListener);
		searchLay.setOnClickListener(itemClickListener);
		searchEd.setOnClickListener(itemClickListener);
		
		// 课程列表下拉刷新 added by WuYuLong
		courseCategoryLV.setonRefreshListener(new OnRefreshListener() {
			@Override
			public void onRefresh() {
				courseCategoryRefresh();
			}

			@Override
			public void onLoadMore() {
				// 分页加1
				++pageNo;
				courseCategoryLoad(LOAD_TYPE_MORE);
			}
		});
		 
		 
		leftBackIv.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) { 
				backListenCourse();
			}
		});
	}
	
	/**
	 * 返回到主界面并显示左侧菜单
	 */
	private void backListenCourse() {
		if(tempCategoryList.size() > 0){
			tempCategoryList.clear();
		}
		if(categoryAdapter != null){
			categoryAdapter.clear();
		}
		finish();
	}
	 
	/**
	 * 课程分类网络加载
	 */
	private void courseCategoryLoad(final int refreshType) {
		TmingHttp.asyncRequest(ServerApi.getCheckAllStudyResourceList(pageNo),
				new HashMap<String, Object>(),
				new TmingHttp.RequestCallback<String>() {

					@Override
					public String onReqestSuccess(String respones) throws Exception {
						// 数据组装
						Log.i(TAG, respones);
						return respones;
					}

					@Override
					public void success(String respones) {
						loadingView.setVisibility(View.GONE);
						courseCategoryLV.onRefreshComplete();

						if (StringUtils.isEmpty(respones)) {
							return;
						}
						try {
							JSONObject jobj = new JSONObject(respones);
							if (null != jobj) {
								if (jobj.has("flag") && jobj.getInt("flag") == 1) {
									if (LOAD_TYPE_REFRESH == refreshType) {
										categoryAdapter.clear();
										categoryAdapter.notifyDataSetChanged();
									}

									JSONObject dataJson = ((JSONObject) jobj.get("res")).getJSONObject("data");
									JSONArray listArr = dataJson.getJSONArray("list");
									if (null != listArr && listArr.length() > 0) {
										/**
										 * 如果课程有数据就添加查看所有专业
										 * */
										setCheckAllSpecialtyData();

										for (int i = 0; i < listArr.length(); i++) {
											JSONObject listJobj = listArr.getJSONObject(i);
											CourseCategory item = new CourseCategory();
											item.courseCategoryId = listJobj.getString("cou_kind_id");
											item.courseCategoryName = listJobj.getString("name");
											tempCategoryList.add(item);
										}
//										categoryAdapter.clear();
										categoryAdapter.courseCategoryList.addAll(tempCategoryList);
									}
								} else {
									Toast.makeText(CheckAllStudyResourceActivity.this, R.string.api_error, Toast.LENGTH_SHORT).show();
								}
							}
						} catch (Exception e) {
							e.printStackTrace();
						}

						/**
						 * 当数据为空时，弹出提示暂时无数据
						 * */
						if (categoryAdapter.courseCategoryList.size() == 0 && tempCategoryList.size() == 0) {
							courseNoDataTV.setVisibility(View.VISIBLE);
							courseNoDataTV.setText(getResources().getString(R.string.no_study_course));
							return;
						} else {
							courseNoDataTV.setVisibility(View.GONE);
						}
						categoryAdapter.notifyDataSetChanged();
					}

					@Override
					public void exception(Exception exception) {
						loadingView.setVisibility(View.GONE);
						courseCategoryLV.onRefreshComplete();
						Toast.makeText(CheckAllStudyResourceActivity.this, getResources().getString(R.string.net_error), Toast.LENGTH_SHORT).show();
						exception.printStackTrace();
					}

				});
		
//		cacheHttp = TmingCacheHttp.getInstance(context);
//		HashMap<String, Object> map = new HashMap<String, Object>();
//		map.put("pageNum", pageNo);
//		map.put("pageSize", Constants.PAGESIZE);
//
//		cacheHttp.asyncRequestWithCache(Constants.COURSECATEGORY_URL, map,
//				new RequestWithCacheCallBack<String>() {
//
//					@Override
//					public String onPreRequestCache(String cache)
//							throws Exception {
//						return cache;
//					}
//
//					@Override
//					public void onPreRequestSuccess(String data) {
//						loadCourseData(data); 
//					}
//
//					@Override
//					public String onReqestSuccess(String respones)
//							throws Exception {
//						return respones;
//					}
//
//					@Override
//					public void success(String cacheRespones, String newRespones) {
//						loadCourseData(newRespones); 
//					}
//
//					@Override
//					public void exception(Exception exception) {
//						loadingView.setVisibility(View.GONE);
//						courseCategoryLV.onRefreshComplete();   
//						Toast.makeText(CheckAllStudyResourceActivity.this, getResources().getString(R.string.net_error_tip), Toast.LENGTH_SHORT).show();
//					}
//				});
	}

	/**
	 * 相关课程数据解析
	 */
	private void loadCourseData(String data) {
		loadingView.setVisibility(View.GONE); 
		 
		courseCategoryLV.onRefreshComplete();
		if (StringUtils.isEmpty(data)) {
			return;
		}
		try {
			JSONObject jobj = new JSONObject(data);
			if (null != jobj) {
				if (jobj.has("flag") && jobj.getInt("flag") == 1) {
					Log.e("resjson",((JSONObject) jobj.get("res")).toString());
					JSONObject dataJson = ((JSONObject) jobj.get("res")).getJSONObject("data");
					Log.e("datajson", dataJson.toString());

					JSONArray listArr = dataJson.getJSONArray("list");
					if (null != listArr && listArr.length() > 0) {
						/**
						 * 如果课程有数据就添加查看所有专业
						 * */
						setCheckAllSpecialtyData();	
												
						for (int i = 0; i < listArr.length(); i++) {
							JSONObject listJobj = listArr.getJSONObject(i);
							CourseCategory item = new CourseCategory();
							item.courseCategoryId = listJobj.getString("cou_kind_id");
							item.courseCategoryName = listJobj.getString("name");
							tempCategoryList.add(item);
						}
						categoryAdapter.clear();
						categoryAdapter.courseCategoryList.addAll(tempCategoryList);
					} 
				}  
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		 
		/**
		 * 当数据为空时，弹出提示暂时无数据
		 * */
		if(categoryAdapter.courseCategoryList.size() == 0 && tempCategoryList.size() == 0){ 
			courseNoDataTV.setVisibility(View.VISIBLE);
			courseNoDataTV.setText(getResources().getString(R.string.no_study_course)); 
			return;
		} else {
			courseNoDataTV.setVisibility(View.GONE);
		}
		categoryAdapter.notifyDataSetChanged(); 
	}
	
	/**
	 * 添加查看所有专业数据.
	 * */
	private void setCheckAllSpecialtyData(){		
		CourseCategory fristItem = new CourseCategory();
		fristItem.courseCategoryId = DEFAULT_STUDY_RESOURCE_ID;
		fristItem.courseCategoryName = getResources().getString(R.string.check_by_specialty);
		tempCategoryList.add(fristItem);
	}
	
	private OnClickListener itemClickListener = new OnClickListener() {
		
		@Override
		public void onClick(View view) {
			switch (view.getId()) {
			case R.id.tab_course: 
				changeToTab();
				
				categoryAdapter.datasAddCache = tempCategoryList;
				categoryAdapter.notifyDataSetChanged();
				break; 
			case R.id.allcourse_item:
//				Constants.leftInnerId = "0";
//				Constants.leftInnerType = 0;
//				Constants.leftInnerName = "所有课程";
//				Intent intent = new Intent();
//				intent.setAction("allcourse");
//				Helper.sendLocalBroadCast(context, intent);
				OpenCourseListActivity();
				break;
//			case R.id.nondegree_good_course_item:
//				Constants.leftInnerType = 1;
//				Intent intent2 = new Intent();
//				intent2.setAction("nondegreegoodcourse");
//				Constants.leftInnerName = "非学历精品课程";
//				Helper.sendLocalBroadCast(context, intent2);
//				break;
			case R.id.search_keyword: 
				UIHelper.showSearchModelActivity(context); 
				break;
			case R.id.courselist_search_rl:
				UIHelper.showSearchModelActivity(context);
				break;
			default:
				break;
			}
		}
	};
	
	@SuppressLint("ResourceAsColor")
	private void changeToTab() {
		courseTabTV.setTextColor(this.getResources().getColor(R.color.tab_on_text_color));  
		courseCateLay.setVisibility(View.VISIBLE); 
//		courseListTabLine.setVisibility(View.VISIBLE); 		 
	}
	
	public class LeftListAdapter extends BaseAdapter {
		private LinkedList<CourseCategory> courseCategoryList = new LinkedList<CourseCategory>();
		private int selectedPosition = -1;
		private int adapterType = 1;

//		public LeftListAdapter(int adapterType) {
//			this.adapterType = adapterType;
//		}
		public LeftListAdapter() {
			
		}

		public void setAdapterType(int adapterType) {
			this.adapterType = adapterType;
		}
		
		public void setSelectedPosition(int position) {
			selectedPosition = position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup arg2) {
			ViewHolder viewHolder = null;
			if (convertView == null) {
				viewHolder = new ViewHolder();
				convertView = LayoutInflater.from(context).inflate(
						R.layout.course_item, null);

				viewHolder.courseCategoryLayout = (RelativeLayout) convertView
						.findViewById(R.id.course_category_lay);
				viewHolder.courseCategoryNameTV = (TextView) convertView
						.findViewById(R.id.course_categoryname);

				convertView.setTag(viewHolder);
			} else {
				viewHolder = (ViewHolder) convertView.getTag();
			}
			
			CourseCategory courseCategory = courseCategoryList.get(position);

			viewHolder.courseCategoryNameTV.setText(courseCategory.courseCategoryName);
			viewHolder.courseCategoryLayout.setOnClickListener(new layoutClickListener(position));
		
			return convertView;
		}

		private List<CourseCategory> datasAddCache = new ArrayList<CourseCategory>();

		public void addCourseCategory(CourseCategory courseCategory) {
			datasAddCache.add(courseCategory);
		}

		@Override
		public void notifyDataSetChanged() {
			if( datasAddCache != null  && datasAddCache.size()!=0){
				if(courseCategoryList.size() != 0){
					courseCategoryList.clear();
				}
				courseCategoryList.addAll(datasAddCache);
				datasAddCache.clear();
			}
			super.notifyDataSetChanged();
		}
		
		public void clear() {
			datasAddCache.clear();
			courseCategoryList.clear();
		}

		@Override
		public CourseCategory getItem(int position) {
			return position < courseCategoryList.size() ? courseCategoryList.get(position) : null;
		}

		public int getSelectedPosition() {
			return selectedPosition;
		}
		
		@Override
		public int getCount() {
			return courseCategoryList.size();
		}

		public void setDragItem(int position) {
			
		}

		public void clearDrag() {
			
		}

		public void changeCourseCategoryIndex(int position, int newPosition) {
			if (position != newPosition) {
				CourseCategory courseCategory = courseCategoryList.get(position);
				courseCategoryList.remove(courseCategory);
				courseCategoryList.add(newPosition, courseCategory);
				
				// 如果旧位置是选中的，就换到新的位置
				if(selectedPosition == position) {
					selectedPosition = newPosition;	
				} else if(selectedPosition == newPosition) {
					// 如果新的位置是当前选中的位置，就换到旧的位置
					selectedPosition = position;
				}
				
				notifyDataSetChanged();
			}
		}

		@Override
		public long getItemId(int position) {
			return 0;
		}

		public void remove(CourseCategory courseCategory) {
			courseCategoryList.remove(courseCategory);
			notifyDataSetChanged();
		}
	}
	
	private final class ViewHolder {
		private RelativeLayout courseCategoryLayout;
		private TextView courseCategoryNameTV;
	}
	
	private class layoutClickListener implements OnClickListener {
		private int position;

		public layoutClickListener(int position) {
			this.position = position;
		}

		@Override
		public void onClick(View v) {
//			Intent intent2 = new Intent();
//			intent2.setAction("backToMain");
//			Helper.sendLocalBroadCast(App.getAppContext(), intent2);
			
			CourseCategory courseCategory = categoryAdapter.getItem(position);
			categoryAdapter.setSelectedPosition(position);
			categoryAdapter.notifyDataSetChanged();
			
			/**
			 * 根据文本，选择专业或者课程查询
			 * */
			if(getResources().getString(R.string.check_by_specialty).equals(courseCategory.courseCategoryName)){ 
				Constants.leftInnerId = courseCategory.courseCategoryId;
				Constants.leftInnerType = 3;
//				Log.e("adapter:Constants.leftInnerType", "" + Constants.leftInnerType);
				Constants.leftInnerName = getResources().getString(R.string.check_all_specialty_title);  
			} else{  		 
				Constants.leftInnerId = courseCategory.courseCategoryId;
				Constants.leftInnerType = 2;
//				Log.e("adapter:Constants.leftInnerType", "" + Constants.leftInnerType);
				Constants.leftInnerName = courseCategory.courseCategoryName; 				
			}
			//跳转显示课程列表
			Intent intent = new Intent(CheckAllStudyResourceActivity.this,CourseListActivity.class);   
			Bundle bundle = new Bundle();  
	        bundle.putString("comeform", "checkallstudyres");  
	        intent.putExtras(bundle);
	        
			startActivity(intent);	
			 			  
		}
	}

	/**
	 * 打开课程列表
	 * */
	private void OpenCourseListActivity(){
		Constants.leftInnerId = "0";
		Constants.leftInnerType = 0;
		Constants.leftInnerName = "所有课程";
		Intent intent = new Intent(CheckAllStudyResourceActivity.this,CourseListActivity.class);
		Bundle bundle = new Bundle();  
        bundle.putString("comefrom", "checkallstudyres");  
        intent.putExtras(bundle);     		
		startActivity(intent);		
	}	 
}
