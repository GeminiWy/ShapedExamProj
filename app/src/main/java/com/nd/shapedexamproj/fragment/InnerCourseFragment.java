package com.nd.shapedexamproj.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.*;

import com.nd.shapedexamproj.App;
import com.nd.shapedexamproj.R;
import com.nd.shapedexamproj.activity.course.CourseListActivity;
import com.nd.shapedexamproj.activity.major.MajorDetailActivity;
import com.nd.shapedexamproj.model.course.CourseCategory;
import com.nd.shapedexamproj.util.Constants;
import com.nd.shapedexamproj.util.StringUtils;
import com.nd.shapedexamproj.util.UIHelper;
import com.tming.common.cache.net.TmingCacheHttp;
import com.tming.common.cache.net.TmingCacheHttp.RequestWithCacheCallBack;
import com.tming.common.util.Helper;
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
 * 
 * 
 * @version 1.0.0 
 * @author Abay Zhuang <br/>
 *		   Create at 2014-5-22
 */
public class InnerCourseFragment extends BaseFlowFragment{
    
    private static final int TAB_COURSR = 1, TAB_PROFRESSIONAL = 2;
	private Context context;
	private LinkedList<CourseCategory> tempCategoryList ,tempProfressionalList;
	private RefreshableListView courseCategoryLV, professionalLV;
	private RelativeLayout courseTabLay, professionalTabLay, allCourseItemLay;
	private RelativeLayout searchLay;
	private EditText searchEd;
	private LinearLayout courseCateLay, professionalLay, courseListTabLine, professionalTabLine;
	private TextView courseTabTV, professionalTabTV;
	private LeftListAdapter categoryAdapter, profressionalAdapter;
//	private final static int COURSELIST = 1, PROFESSIONALLIST = 2;
	private int tabIndex = 1;
	private final static int CONNECT_FAILED = 0;
	private final static int LOADING = 1;
	private final static int SUCCESS = 11;
	private final static int FAILED = 22;
	private final static int LOADED = 23;
	
	/**
     * 获取课程消息
     * */
    private final int GET_COUSER_INFO_MSG = 31;
    /**
     * 获取专业消息
     * */
    private final int GET_SPECIALTY_INFO_MSG = 32;  
    
	private int pageNo = 1;
	private int courseCategoryPageNo = 1, majorPageNo;
	private LinearLayout loading_layout;
	private TmingCacheHttp cacheHttp;
	private Handler myHandler;
	
	@Override
	public int initResource() {
		return R.layout.course_4_inner_layout;
	}

	@Override
	public void initComponent(View v) {
	    courseTabLay = (RelativeLayout) v.findViewById(R.id.tab_course);
        courseTabLay.setOnClickListener(itemClickListener);
        professionalTabLay = (RelativeLayout) v.findViewById(R.id.tab_professional);
        professionalTabLay.setOnClickListener(itemClickListener);
        courseCateLay = (LinearLayout) v.findViewById(R.id.course_cate_lay);
        professionalLay = (LinearLayout) v.findViewById(R.id.profesional_lay);
        courseListTabLine = (LinearLayout) v.findViewById(R.id.courselist_tab_on);
        professionalTabLine = (LinearLayout) v.findViewById(R.id.professional_tab_on);
        courseTabTV = (TextView) v.findViewById(R.id.tab_course_tv);
        professionalTabTV = (TextView) v.findViewById(R.id.tab_professional_tv);
        allCourseItemLay = (RelativeLayout) v.findViewById(R.id.allcourse_item);
        allCourseItemLay.setOnClickListener(itemClickListener);
        courseCategoryLV = (RefreshableListView) v.findViewById(R.id.course_category_lv);
        professionalLV = (RefreshableListView) v.findViewById(R.id.professional_lv);
        loading_layout = (LinearLayout) v.findViewById(R.id.loading_layout);
        searchLay = (RelativeLayout) v.findViewById(R.id.courselist_search_rl);
        searchEd = (EditText) v.findViewById(R.id.search_keyword);
        
        initHandle();
	}

	@Override
	public void initData() {
		context = getBaseActivity();
        categoryAdapter = new LeftListAdapter();
        courseCategoryLV.setAdapter(categoryAdapter);

        profressionalAdapter = new LeftListAdapter();
        professionalLV.setAdapter(profressionalAdapter);
        
        tempCategoryList = new LinkedList<CourseCategory>();
        tempProfressionalList = new LinkedList<CourseCategory>();
//      changeToTab(TAB_COURSR);
        courseCategoryRefresh();
        profressionalRefresh();
    }

	@Override
	public void initListener() {	
		courseTabLay.setOnClickListener(itemClickListener);
		professionalTabLay.setOnClickListener(itemClickListener);
		allCourseItemLay.setOnClickListener(itemClickListener);
		//nonDegreeGoodCousreItemLay.setOnClickListener(itemClickListener);
		searchLay.setOnClickListener(itemClickListener);
		searchEd.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View arg0) {
                //跳转到搜索activity，默认的tab标签为：0
                UIHelper.showSearchModelActivity(context);
            }
        });
		courseCategoryLV.setonRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh() {
                courseCategoryRefresh();
            }

            @Override
            public void onLoadMore() {
                // 分页加1
                ++courseCategoryPageNo;
                courseCategoryLoad();
            }
        });

        // 专业列表下拉刷新 added by WuYuLong
        professionalLV.setonRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh() {
                profressionalRefresh();
            }

            @Override
            public void onLoadMore() {
                // 分页加1
                ++majorPageNo;
                majorLoad();
            }
        });
		
	}
	private void courseCategoryRefresh()
    {
        courseCategoryPageNo = 1;
        tempCategoryList.clear();
        courseCategoryLoad();
    }
    
    private void profressionalRefresh()
    {
        majorPageNo = 1;
        tempProfressionalList.clear();
        
        majorLoad();
    }

    /**
     * 课程分类网络加载
     */
    private void courseCategoryLoad() {
        cacheHttp = TmingCacheHttp.getInstance();
        HashMap<String, Object> map = new HashMap<String, Object>();
        map.put("pageNum", courseCategoryPageNo);
        map.put("pageSize", Constants.PAGESIZE);

        cacheHttp.asyncRequestWithCache(Constants.COURSECATEGORY_URL, map,
                new RequestWithCacheCallBack<String>() {

                    @Override
                    public String onPreRequestCache(String cache)
                            throws Exception {
                        return cache;
                    }

                    @Override
                    public void onPreRequestSuccess(String data) { 
//                      loadCourseData(data);
                        Message msg = new Message();
                        msg.what = GET_COUSER_INFO_MSG;
                        msg.obj = data;
                        myHandler.sendMessage(msg); 
                    }

                    @Override
                    public String onReqestSuccess(String respones)
                            throws Exception {
                        return respones;
                    }

                    @Override
                    public void success(String cacheRespones, String newRespones) {
//                      loadCourseData(newRespones);
                        Message msg = new Message();
                        msg.what = GET_COUSER_INFO_MSG;
                        msg.obj = newRespones;
                        myHandler.sendMessage(msg); 
                    }

                    @Override
                    public void exception(Exception exception) {
                        courseCategoryLV.onRefreshComplete();
                    }
                });
    }

    private void loadCourseData(String data) {
        if (!StringUtils.isEmpty(data)) {
            try {
                JSONObject jobj = new JSONObject(data);
                if (null != jobj) {
                    if (jobj.has("flag") && jobj.getInt("flag") == 1) {
                        Log.e("resjson",
                                ((JSONObject) jobj.get("res")).toString());
                        JSONObject dataJson = ((JSONObject) jobj.get("res"))
                                .getJSONObject("data");
                        Log.e("datajson", dataJson.toString());

                        JSONArray listArr = dataJson.getJSONArray("list");
                        if (null != listArr && listArr.length() > 0) {
                            for (int i = 0; i < listArr.length(); i++) {
                                JSONObject listJobj = listArr.getJSONObject(i);
                                CourseCategory item = new CourseCategory();
                                item.courseCategoryId = listJobj
                                        .getString("cou_kind_id");
                                item.courseCategoryName = listJobj
                                        .getString("name");
                                tempCategoryList.add(item);
                            } 
                            categoryAdapter.clear();
                            categoryAdapter.courseCategoryList
                                    .addAll(tempCategoryList);
                        }
                    } else { 
                    }
                }
            } catch (Exception e) { 
                e.printStackTrace();
            }
            categoryAdapter.notifyDataSetChanged();
            courseCategoryLV.onRefreshComplete();
        }
    }

    private void loadProfessionalData(String data) {
        try {
            JSONObject jobj = new JSONObject(data);
            if (null != jobj) {
                if (jobj.has("flag") && jobj.getInt("flag") == 1) {
                    Log.e("resjson", ((JSONObject) jobj.get("res")).toString());
                    JSONObject dataJson = ((JSONObject) jobj.get("res"))
                            .getJSONObject("data");
                    Log.e("datajson", dataJson.toString());

                    JSONArray listArr = dataJson.getJSONArray("list");
                    for (int i = 0; i < listArr.length(); i++) {
                        JSONObject listObj = listArr.getJSONObject(i);
                        CourseCategory item = new CourseCategory();
                        item.courseCategoryId = listObj.getString("term_id");
                        item.courseCategoryName = listObj.getString("name");
                        tempProfressionalList.add(item);
                    } 
                    profressionalAdapter.clear();
                    profressionalAdapter.courseCategoryList
                            .addAll(tempProfressionalList);
                } else { 
                }
            }
        } catch (Exception e) { 
            e.printStackTrace();
        } 
        profressionalAdapter.notifyDataSetChanged();
        professionalLV.onRefreshComplete(); 
    }

    /**
     * 专业数据网络加载
     */
    private void majorLoad() {
        cacheHttp = TmingCacheHttp.getInstance(context);
        HashMap<String, Object> map = new HashMap<String, Object>();
        map.put("pageNum", majorPageNo);
        map.put("pageSize", Constants.PAGESIZE);

        cacheHttp.asyncRequestWithCache(Constants.PROFRESSIONAL_URL, map,
                new RequestWithCacheCallBack<String>() {

                    @Override
                    public String onPreRequestCache(String cache)
                            throws Exception {
                        return cache;
                    }

                    @Override
                    public void onPreRequestSuccess(String data) {
//                      loadProfessionalData(data);
                        Message msg = new Message();
                        msg.what = GET_SPECIALTY_INFO_MSG;
                        msg.obj = data;
                        myHandler.sendMessage(msg); 
                    }

                    @Override
                    public String onReqestSuccess(String respones)
                            throws Exception {
                        return respones;
                    }

                    @Override
                    public void success(String cacheRespones, String newRespones) {
//                      loadProfessionalData(newRespones);
                        Message msg = new Message();
                        msg.what = GET_SPECIALTY_INFO_MSG;
                        msg.obj = newRespones;
                        myHandler.sendMessage(msg); 
                    }

                    @Override
                    public void exception(Exception exception) { 
                        professionalLV.onRefreshComplete(); 
                    }
                });
    }

    /**
     * 接收处理上下拖动刷新列表数据
     * */
    private void initHandle() {
        myHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) { 
                super.handleMessage(msg);
                String result = (String) msg.obj;
                if (TextUtils.isEmpty(result)) {
                    return;
                }
                switch(msg.what){
                case GET_COUSER_INFO_MSG:                   
                    loadCourseData(result);
                    break;
                case GET_SPECIALTY_INFO_MSG:
                    loadProfessionalData(result);
                    break;
                }
            }
        };
    }
	
    private OnClickListener itemClickListener = new OnClickListener() {

        @Override
        public void onClick(View view) {
            switch (view.getId()) {
            case R.id.tab_course:
                changeToTab(TAB_COURSR);

                break;
            case R.id.tab_professional:
                changeToTab(TAB_PROFRESSIONAL);

                break;
            case R.id.allcourse_item:
                
                openCourseListActivity("0", 0, "所有课程");
                break;
            /*
             * case R.id.nondegree_good_course_item: Constants.leftInnerType =
             * 1; Intent intent2 = new Intent();
             * intent2.setAction("nondegreegoodcourse"); Constants.leftInnerName
             * = "非学历精品课程"; Helper.sendLocalBroadCast(context, intent2); break;
             */
            default:
                break;
            }
        }
    };
	
	@SuppressLint("ResourceAsColor")
    private void changeToTab(int tab) {
        tabIndex = tab;
        if (tabIndex == TAB_COURSR) {
            courseCateLay.setVisibility(View.VISIBLE);
            professionalLay.setVisibility(View.GONE);
            courseListTabLine.setVisibility(View.VISIBLE);
            professionalTabLine.setVisibility(View.INVISIBLE);
        } else {
            professionalLay.setVisibility(View.VISIBLE);
            courseCateLay.setVisibility(View.GONE);
            professionalTabLine.setVisibility(View.VISIBLE);
            courseListTabLine.setVisibility(View.INVISIBLE);
        }
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

    /**
     * <p> 要素点击监听 </p>
     */
	private class layoutClickListener implements OnClickListener {
		private int position;

		public layoutClickListener(int position) {
			this.position = position;
		}

		@Override
		public void onClick(View v) {
			Intent intent2 = new Intent();
			intent2.setAction("backToMain");
			Helper.sendLocalBroadCast(App.getAppContext(), intent2);
			
			if (1 == tabIndex) {
				CourseCategory courseCategory = categoryAdapter.getItem(position);
				categoryAdapter.setSelectedPosition(position);
				categoryAdapter.notifyDataSetChanged();
				
				//跳转显示课程列表
				openCourseListActivity(courseCategory.courseCategoryId, 2, courseCategory.courseCategoryName);
				
			} else {
				CourseCategory courseCategory = profressionalAdapter.getItem(position);
				profressionalAdapter.setSelectedPosition(position);
				profressionalAdapter.notifyDataSetChanged();
				//跳转显示专业详情
				if (TextUtils.isEmpty(courseCategory.courseCategoryName)) {
                    courseCategory.courseCategoryName = "";
                }
				openMajorDetailActivity(courseCategory.courseCategoryId, 3, courseCategory.courseCategoryName);
				
			}
			
		}
	} 
	
	/**
     * 打开课程列表
     * */
    private void openCourseListActivity(String leftInnerId, int leftInnerType, String leftInnerName){
        Constants.leftInnerId = leftInnerId;
        Constants.leftInnerType = leftInnerType;
        Constants.leftInnerName = leftInnerName; 
        Intent intent = new Intent(context,CourseListActivity.class);
        Bundle bundle = new Bundle();  
        bundle.putString("comefrom", "InnerCourseFragment");
        intent.putExtras(bundle);           
        startActivity(intent);      
    }   
	
    private void openMajorDetailActivity(String leftInnerId, int leftInnerType, String leftInnerName) {
        Constants.leftInnerId = leftInnerId;
        Constants.leftInnerType = leftInnerType;
        Constants.leftInnerName = leftInnerName; 
        Intent intent = new Intent(context,MajorDetailActivity.class);
        Bundle bundle = new Bundle();  
        bundle.putString("comefrom", "InnerCourseFragment");
        intent.putExtras(bundle);           
        startActivity(intent);
    }
    
}
