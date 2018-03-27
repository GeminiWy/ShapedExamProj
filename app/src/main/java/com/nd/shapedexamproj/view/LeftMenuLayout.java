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
 * <p> MainActivity 左侧菜单 </p>
 * <p> Modified by xuwenzhuo 2013/11/04 </p>
 */
public class LeftMenuLayout extends RelativeLayout {

    private static final String TAG = "LeftMenuLayout";
    private Context context;
    private final static int TAB_COURSR = 1;
    private final static int TAB_PROFRESSIONAL = 2;
    private final int GET_COUSER_INFO_MSG = 31;//获取课程消息
    private final int GET_SPECIALTY_INFO_MSG = 32;////获取专业消息

    private final static int CONNECT_FAILED = 0;
    private final static int LOADING = 1;
    private final static int SUCCESS = 11;
    private final static int FAILED = 22;
    private final static int LOADED = 23;

    private LinkedList<CourseCategory> categoryList, profressionalList;
    private RefreshableListView courseCategoryLV;
    private RefreshableListView professionalLV;
    private RelativeLayout courseTabLay, professionalTabLay, allCourseItemLay,
            nonDegreeGoodCousreItemLay;
    private RelativeLayout searchLay;
    private EditText searchEd;
    private LinearLayout courseCateLay, professionalLay, courseListTabLine,
            professionalTabLine, tabLayout;
    private TextView courseTabTV, professionalTabTV;
    private LeftListAdapter categoryAdapter;
    private LeftListAdapter profressionalAdapter;
    private int tabIndex = TAB_COURSR;

    private int pageNo = 1;
    private int courseCategoryPageNo = 1, majorPageNo = 1;
    private LinearLayout loading_layout;
    private TmingCacheHttp cacheHttp;
    private Handler myHandler;

    public LeftMenuLayout(Context context, AttributeSet attrs) {
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

    private void courseCategoryRefresh() {
        courseCategoryPageNo = 1;
        categoryList.clear();
        courseCategoryLoad();
    }

    private void profressionalRefresh() {
        majorPageNo = 1;
        profressionalList.clear();

        majorLoad();
    }


    public void addListener() {
        // 课程列表下拉刷新 added by WuYuLong
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

        searchLay.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                //跳转到搜索界面， 默认tab标签为 0
                UIHelper.showSearchModelActivity(context, 0);
            }
        });

        searchEd.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                //跳转到搜索Activity，默认的tab标签为0
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
//		changeToTab(TAB_COURSR);
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
        if (!AuthorityManager.getInstance().isInnerAuthority()) {
            tabLayout.setVisibility(View.GONE);
        }
    }

    /**
     * <p> 课程分类网络加载 </p>
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
                        //loadCourseData(data);
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
                        //loadCourseData(newRespones);
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
     * <p> 加载课程数据 </p>
     *
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
     * <p> 加载学科数据 </p>
     *
     * @param data
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
//						loadProfessionalData(data);
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
//						loadProfessionalData(newRespones);
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
     * <P> 接收处理上下拖动刷新列表数据 </P>
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
                switch (msg.what) {
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
                    backToMain();
                    showAllCourses();

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

    /**
     * <p> 查看所有课程</p>
     */
    private void showAllCourses() {
        Constants.leftInnerId = "0";
        Constants.leftInnerType = 0;
        Constants.leftInnerName = "所有课程";
        Intent intent = new Intent();
        intent.setAction("allcourse");
        Helper.sendLocalBroadCast(context, intent);
    }

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

        // public LeftListAdapter(int adapterType) {
        // this.adapterType = adapterType;
        // }
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

		/*
		 * private List<CourseCategory> datasAddCache = new
		 * ArrayList<CourseCategory>();
		 *
		 * public void addCourseCategory(CourseCategory courseCategory) {
		 * datasAddCache.add(courseCategory); }
		 *
		 * @Override public void notifyDataSetChanged() { if(datasAddCache !=
		 * null && datasAddCache.size() != 0){
		 * courseCategoryList.addAll(datasAddCache); datasAddCache.clear(); }
		 * super.notifyDataSetChanged(); }
		 */

        public void clear() {
            // datasAddCache.clear();
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
     * <p> 返回主界面 </p>
     */
    private void backToMain() {
        Intent backIntent = new Intent();
        backIntent.setAction("backToMain");
        Helper.sendLocalBroadCast(App.getAppContext(), backIntent);
    }

    /**
     * <p> 自定义要素点击操作</p>
     */
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
                //TODO 暂时注释
				/*
				categoryAdapter.setSelectedPosition(position);
				categoryAdapter.notifyDataSetChanged();
				*/
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
                //TODO 暂时注释
				/*
				profressionalAdapter.setSelectedPosition(position);
				profressionalAdapter.notifyDataSetChanged();
				*/
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
