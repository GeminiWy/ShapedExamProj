package com.nd.shapedexamproj.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import com.nd.shapedexamproj.App;
import com.nd.shapedexamproj.AuthorityManager;
import com.nd.shapedexamproj.R;
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
 * <p>左边菜单</p>
 * <p>Copy from Class LeftMenuLayut.java </p>
 * <p>Created by xuwenzhuo  2014/11/6 </p>
 */
public class LeftMenuLayoutWithShadow extends RelativeLayout {

    private Context context;
    private static final String TAG = "LeftMenuLayout";
	private static final int TAB_COURSR = 1, TAB_PROFRESSIONAL = 2;
    //获取课程消息
    private final int GET_COUSER_INFO_MSG = 31;
    //获取专业消息
    private final int GET_SPECIALTY_INFO_MSG = 32;

    private int tabIndex = TAB_COURSR;
    private EditText searchEd;
    private int courseCategoryPageNo = 1, majorPageNo = 1;
	private RefreshableListView courseCategoryLV;
	private RefreshableListView professionalLV;
	private TextView courseTabTV, professionalTabTV;
    private LinkedList<CourseCategory> categoryList, profressionalList;
	private LeftListAdapter categoryAdapter;
	private LeftListAdapter profressionalAdapter;
	private LinearLayout loading_layout;
	private TmingCacheHttp mCacheHttp;
	private Handler myHandler;

    private RelativeLayout courseTabLay, professionalTabLay, allCourseItemLay,searchLay;
    private LinearLayout courseCateLay, professionalLay, courseListTabLine,professionalTabLine, tabLayout;

    /**
     * <p> 构造函数 </p>
     * @param context
     * @param attrs
     */
	public LeftMenuLayoutWithShadow(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.context = context; 
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		initComponent();
		initData();
		addListener();
	}

	public void addListener() {
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
		
		searchLay.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) { 
				UIHelper.showSearchModelActivity(context);
			}
		});
		
		searchEd.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) { 
				UIHelper.showSearchModelActivity(context);
			}
		});
	}

	public void initData() {
		categoryAdapter = new LeftListAdapter();
		courseCategoryLV.setAdapter(categoryAdapter);

		profressionalAdapter = new LeftListAdapter();
		professionalLV.setAdapter(profressionalAdapter);
		
		categoryList = new LinkedList<CourseCategory>();
		profressionalList = new LinkedList<CourseCategory>();

		courseCategoryRefresh();
		profressionalRefresh();
	}

	public void initComponent() {
		courseTabLay = (RelativeLayout) findViewById(R.id.tab_course);
		courseTabLay.setOnClickListener(itemClickListener);
		professionalTabLay = (RelativeLayout) findViewById(R.id.tab_professional);
		professionalTabLay.setOnClickListener(itemClickListener);
		courseCateLay = (LinearLayout) findViewById(R.id.course_cate_lay);
		professionalLay = (LinearLayout) findViewById(R.id.profesional_lay);
		courseListTabLine = (LinearLayout) findViewById(R.id.courselist_tab_on);
		professionalTabLine = (LinearLayout) findViewById(R.id.professional_tab_on);
		tabLayout = (LinearLayout) findViewById(R.id.tab_ll);
		
		courseTabTV = (TextView) findViewById(R.id.tab_course_tv);
		professionalTabTV = (TextView) findViewById(R.id.tab_professional_tv);
		allCourseItemLay = (RelativeLayout) findViewById(R.id.allcourse_item);
		allCourseItemLay.setOnClickListener(itemClickListener);
		courseCategoryLV = (RefreshableListView) findViewById(R.id.course_category_lv);
		professionalLV = (RefreshableListView) findViewById(R.id.professional_lv);
		loading_layout = (LinearLayout) findViewById(R.id.loading_layout);
		searchLay = (RelativeLayout) findViewById(R.id.courselist_search_rl);
		searchEd = (EditText) findViewById(R.id.search_keyword);
		
		initAuthority();
		initHandle();
	}
	
	private void initAuthority() {
        //判定是否为游客用户
        if (!AuthorityManager.getInstance().isInnerAuthority()) {
            //不是游客用户隐藏专业和课堂 tab
            tabLayout.setVisibility(View.GONE);
        } 
    }

    /**
     * <p> 课程结构刷新 </p>
     */
    private void courseCategoryRefresh()
    {
        courseCategoryPageNo = 1;
        categoryList.clear();
        courseCategoryLoad();
    }

    /**
     * <p>专业信息刷新</p>
     */
    private void profressionalRefresh()
    {
        majorPageNo = 1;
        profressionalList.clear();
        majorLoad();
    }

    /**
	 * <p>课程分类网络加载</p>
	 */
    private void courseCategoryLoad() {
        mCacheHttp = TmingCacheHttp.getInstance();
        HashMap<String, Object> map = new HashMap<String, Object>();
        map.put("pageNum", courseCategoryPageNo);
        map.put("pageSize", Constants.PAGESIZE);

        mCacheHttp.asyncRequestWithCache(Constants.COURSECATEGORY_URL, map,
                new RequestWithCacheCallBack<String>() {

                    @Override
                    public String onPreRequestCache(String cache)
                            throws Exception {
                        return cache;
                    }

                    @Override
                    public void onPreRequestSuccess(String data) {
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
                        Message msg = new Message();
                        msg.what = GET_COUSER_INFO_MSG;
                        msg.obj = newRespones;
                        myHandler.sendMessage(msg);
                    }
                    @Override
                    public void exception(Exception exception) {

                    }
                });
    }

    /**
     * <p>课程信息 根据返回的 string data 解析json对象</p>
     * @param data
     */
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
								categoryList.add(item);
							} 
							categoryAdapter.clear();
							categoryAdapter.courseCategoryList
									.addAll(categoryList);
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

    /**
     * <p>专业信息 根据返回的 string data 解析json对象</p>
     * @param data 服务端返回的字符串
     */
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
						profressionalList.add(item);
					} 
					profressionalAdapter.clear();
					profressionalAdapter.courseCategoryList
							.addAll(profressionalList);
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
	 * <p> 专业数据网络加载 </p>
	 */
	private void majorLoad() {
		mCacheHttp = TmingCacheHttp.getInstance(context);
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("pageNum", majorPageNo);
		map.put("pageSize", Constants.PAGESIZE);

		mCacheHttp.asyncRequestWithCache(Constants.PROFRESSIONAL_URL, map,
				new RequestWithCacheCallBack<String>() {

					@Override
					public String onPreRequestCache(String cache)
							throws Exception {
						return cache;
					}

					@Override
					public void onPreRequestSuccess(String data) {
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
						Message msg = new Message();
						msg.what = GET_SPECIALTY_INFO_MSG;
						msg.obj = newRespones;
						myHandler.sendMessage(msg); 
					}

					@Override
					public void exception(Exception exception) { 

					}
				});
	}

	/**
	 * <p>接收处理上下拖动刷新列表数据</p>
	 */
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
                //游客模式 课程界面
				changeToTab(TAB_COURSR);
				break;
			case R.id.tab_professional:
                //游客模式 专业界面
				changeToTab(TAB_PROFRESSIONAL);
				break;
			case R.id.allcourse_item:
                //查看所有课程
				backToMain();
				Constants.leftInnerId = "0";
				Constants.leftInnerType = 0;
				Constants.leftInnerName = getResources().getString(R.string.all_course_text);
				Intent intent = new Intent();
				intent.setAction("allcourse");
				Helper.sendLocalBroadCast(context, intent);
				break;
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
		private List<CourseCategory> courseCategoryList = new ArrayList<CourseCategory>();
		private int selectedPosition = -1;
		private int adapterType = 1;

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
			viewHolder.courseCategoryNameTV
					.setText(courseCategory.courseCategoryName);
			viewHolder.courseCategoryLayout
					.setOnClickListener(new layoutClickListener(position));
			return convertView;
		}

		public void clear() {
			courseCategoryList.clear();
		}

		@Override
		public CourseCategory getItem(int position) {
			return position < courseCategoryList.size() ? courseCategoryList
					.get(position) : null;
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

		@Override
		public long getItemId(int position) {
			return 0;
		}

		public void remove(CourseCategory courseCategory) {
			courseCategoryList.remove(courseCategory);
			notifyDataSetChanged();
		}

		public void removeAll() {
			for (int i = 0; i < courseCategoryList.size(); i++) {
				courseCategoryList.remove(i);
			}
			notifyDataSetChanged();
		}
	}

	private final class ViewHolder {
		private RelativeLayout courseCategoryLayout;
		private TextView courseCategoryNameTV;
	}

    /**
     * <p>调用MainActivity 界面的返回函数</p>
     */
	private void backToMain() {
		Intent intent = new Intent();
		intent.setAction("backToMain");
		Helper.sendLocalBroadCast(App.getAppContext(), intent);
	}
	
	private class layoutClickListener implements OnClickListener {
		private int position;

		public layoutClickListener(int position) {
			this.position = position;
		}

		@Override
		public void onClick(View v) {
			backToMain();
			if (TAB_COURSR == tabIndex) {
				CourseCategory courseCategory = categoryAdapter
						.getItem(position);
				// 跳转显示课程列表
				Intent intent = new Intent();
				intent.setAction("courselist");
				intent.putExtra("courseCategoryId",
						courseCategory.courseCategoryId);
				intent.putExtra("courseCategoryName",
						courseCategory.courseCategoryName);
				Constants.leftInnerId = courseCategory.courseCategoryId;
				Constants.leftInnerType = 2;
				Log.e("adapter:Constants.leftInnerType", ""
						+ Constants.leftInnerType);
				Constants.leftInnerName = courseCategory.courseCategoryName;
				Helper.sendLocalBroadCast(context, intent);
			} else {
				CourseCategory courseCategory = profressionalAdapter
						.getItem(position);
				// 跳转显示专业详情
				Intent intent = new Intent();
				intent.setAction("professionaldetail");
				Constants.leftInnerId = courseCategory.courseCategoryId;
				Constants.leftInnerName = courseCategory.courseCategoryName;
				Constants.leftInnerType = 3;
				Helper.sendLocalBroadCast(context, intent);
			}
		}
	} 
}
