package com.nd.shapedexamproj.view.major;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;

import com.nd.shapedexamproj.R;
import com.nd.shapedexamproj.util.Constants;
import com.nd.shapedexamproj.util.UIHelper;
import com.tming.common.cache.net.TmingCacheHttp;
import com.tming.common.cache.net.TmingCacheHttp.RequestWithCacheCallBack;
import com.tming.common.view.RefreshableListView;
import com.tming.common.view.RefreshableListView.OnRefreshListener;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
/**
 * 专业详情-专业课程
 * @author wyl
 * create in 2014.03.18
 */
public class MajorCourseList extends RelativeLayout{
	private Context context;
	private RefreshableListView listView;
	private String major_id;
	private View view;
	private TmingCacheHttp cacheHttp ;
	private List<HashMap<String,Object>> courseList;
	private int pageNum;
	
	public MajorCourseList(Context context,String major_id) {
		super(context);
		this.context = context;
		this.major_id = major_id;
		initComponent();
		initData();
	}

	private void initComponent(){
		courseList = new ArrayList<HashMap<String,Object>>();
		
		view = LayoutInflater.from(context).inflate(R.layout.major_course_list, this);
		listView = (RefreshableListView)findViewById(R.id.lv_major_course_list);
		
		//课程列表下拉刷新 added by WuYuLong
		listView.setonRefreshListener(new OnRefreshListener() {			
			@Override
			public void onRefresh() {
				courseReflash();
			}
			
			@Override
			public void onLoadMore() {
				++pageNum;
				requestData();
			}
		});
	}
	
	public void initData() {
		if(this.major_id != null && !"".equals(this.major_id)){
			courseReflash();
		}
	}
	
	private void courseReflash(){
		pageNum = 1;
		courseList.clear();
		requestData();
	}
	
	/**
	 * 获取网络数据
	 */
	Runnable dataLoadRunnable = new Runnable() {
		@Override
		public void run() {
			requestData();
		}
	};
	
	/**
	 * 请求网络数据
	 */
	private void requestData(){
		String url = Constants.COURSE_URL ;
		Map<String ,Object> map = new HashMap<String,Object>();
		map.put("specialty", major_id);
		map.put("pageNum", pageNum);
		map.put("pageSize", Constants.PAGESIZE);
		cacheHttp = TmingCacheHttp.getInstance(context);
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
				
			}
		});
	}
	
	/**
	 * 网络请求成功后，加载数据
	 * @param data
	 */
	private void loadData(String data){
		String dataStr = "";
		dataStr = data;
		JSONObject resJson = null;
		JSONObject dataJSON = new JSONObject();
		JSONArray courseArray = new JSONArray();
		try{
			try {
				resJson = new JSONObject(dataStr);
			} catch (JSONException e) {
				e.printStackTrace();
			}
			
			if(resJson != null && resJson.getInt("flag") == 1){
				if(courseList == null || courseList.size() == 0){
					courseList = new ArrayList<HashMap<String,Object>>();
				}
				HashMap<String,Object> courseItemMap = new HashMap<String,Object>();
				try {
					if(resJson.getJSONObject("res").getInt("code") == 1){
						dataJSON = resJson.getJSONObject("res").getJSONObject("data");
						if(dataJSON.optJSONArray("list") != null){
							courseArray = dataJSON.getJSONArray("list");
							int size = courseArray.length();
							for (int i = 0; i < size; i++) {
								JSONObject courseItem = courseArray.getJSONObject(i);
								courseItemMap = new HashMap<String,Object>();
								courseItemMap.put("course_id", courseItem.getString("course_id"));
								courseItemMap.put("course_name", courseItem.getString("name"));
								
								if(!courseList.contains(courseItemMap)){
									courseList.add(courseItemMap);
								}
							}
						}
					}
					
				} catch (JSONException e) {
					e.printStackTrace();
				}
				//创建SimpleAdapter适配器将数据绑定到item显示控件上  
				SimpleAdapter adapter = new SimpleAdapter (context, courseList, R.layout.major_course_item,
						new String[]{"course_name", "course_id"}, new int[]{R.id.course_name, R.id.course_id});  
		        //实现列表的显示  
			    listView.setAdapter(adapter);
			    //绑定点击事件
			    listView.setOnItemClickListener(new ItemClickListener());
			    listView.onRefreshComplete();
			}
		} catch (Exception e) {
		}
	}
	
	private String jsonParsing(String data){
		return data;
	}
	
	/**
	 * 点击列表，跳转到课程详情页
	 * @author Administrator
	 *
	 */
	private class ItemClickListener implements OnItemClickListener {
		@Override
		public void onItemClick(AdapterView<?> parent, View arg1, int position, long id) {
			ListView listView = (ListView) parent;  
            HashMap<String, Object> data = (HashMap<String, Object>) listView.getItemAtPosition(position);  
            String course_id = data.get("course_id").toString();
            String course_name = data.get("course_name").toString();
            
            //跳转到课程详情页
			UIHelper.showCourseDetail(context, course_id, course_name);
		}
	}
}
