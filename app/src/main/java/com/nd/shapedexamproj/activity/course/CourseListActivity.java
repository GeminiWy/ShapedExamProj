/**
 * 
 */
package com.nd.shapedexamproj.activity.course;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.nd.shapedexamproj.App;
import com.nd.shapedexamproj.R;
import com.nd.shapedexamproj.activity.BaseActivity;
import com.nd.shapedexamproj.adapter.CourseListItemAdapter;
import com.nd.shapedexamproj.model.Course;
import com.nd.shapedexamproj.util.Constants;
import com.tming.common.net.TmingHttp;
import com.tming.common.net.TmingHttpException;
import com.tming.common.net.TmingResponse;
import com.tming.common.util.Helper;
import com.tming.common.util.Log;
import com.tming.common.view.RefreshableListView;
import com.tming.common.view.RefreshableListView.OnRefreshListener;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * @author Caiyx
 * 2014-3-17
 */
public class CourseListActivity extends BaseActivity {
	private Context context;

    public final static int QUERYFLAG=23;
	private final static int CONNECT_FAILED = 0;
	private final static int SUCCESS = 11;
    private final static int PAGESIZE = 20;
	private final static int FAILED = 22;
    private final static int NONETWORK=88;

	private TextView courseListNameTV, noDataTv;
	private ImageView backBtnImg;
	private int pageNo = 1;
//	private ItemAdapter adapter;
	private CourseListItemAdapter adapter ;
	private RefreshableListView listView;
	private ArrayList<Course> dataList = new ArrayList<Course>();
	private String url = "";
	private HashMap<String, Object> map = new HashMap<String, Object>();
	private LinearLayout loading_layout;
	private int pageNum = 1;

    private Handler handler = new Handler() {

        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case CONNECT_FAILED:

                    break;
                case QUERYFLAG:
                    listView.onRefreshComplete();
                    loading_layout.setVisibility(View.GONE);
                    //adapter.notifyDataSetChanged();
                    break;
                case NONETWORK:
                    //Toast.makeText(context, "Please open network connections.", Toast.LENGTH_SHORT).show();

                    break;
			    /*
			    case 99: // 显示进度条
				    if(downloadTask == null || downloadTask.getStatus() == Status.FINISHED) {
                        favorite_download_layout.setVisibility(VISIBLE);
                        favorite_down_process_tv.setText("0%");
                        downloadTask = new DownloadTask();
                        downloadTask.execute();
                    }
				break;
				*/
                case SUCCESS:
                    //Toast.makeText(context, "success", Toast.LENGTH_SHORT).show();
                    break;
                case FAILED:
				    /*
				     * Toast.makeText(context, "Delete failed",
				     * Toast.LENGTH_SHORT).show();
				     */
                    break;

            }
        };
    };

	/**
	 *  判断来源，是否是全部学习资源 
	 **/
	private String mCheckAllStudyResActivity;
	
	@Override
	public int initResource() {
		return R.layout.course_list;
	}

	@Override
	public void initData() {

		SharedPreferences preferences = getSharedPreferences(Constants.SHARE_PREFERENCES_NAME,Context.MODE_PRIVATE);
		loading_layout.setVisibility(View.VISIBLE);
		
		Bundle bundle = this.getIntent().getExtras();  
		if(bundle != null){
			mCheckAllStudyResActivity = bundle.getString("comefrom");
		}

        new Thread(dataLoadRunnable).start();
	}

	@Override
	public void initComponent() {
		this.context = this;
		findViewById(R.id.list_head_right).setVisibility(View.INVISIBLE);
		backBtnImg = (ImageView) findViewById(R.id.list_head_left);

		courseListNameTV = (TextView) findViewById(R.id.list_head_tv);
		noDataTv = (TextView) findViewById(R.id.no_data_tv);
		listView = (RefreshableListView) findViewById(R.id.courselist);
        loading_layout = (LinearLayout) findViewById(R.id.loading_layout);

		listView.setonRefreshListener(new OnRefreshListener() {			
			@Override
			public void onRefresh() {
				pageNum = 1;
				new Thread(dataLoadRunnable).start();
			}
			
			@Override
			public void onLoadMore() {
				++pageNum;
				new Thread(dataLoadRunnable).start();
			}
		});
		
		adapter = new CourseListItemAdapter(CourseListActivity.this,1);
		listView.setAdapter(adapter);
		listView.setOnItemClickListener(adapter);
		courseListNameTV.setText(Constants.leftInnerName);
		resetParameter();
	}

    /**
     * 控件点击事件 
     */
    private OnClickListener cClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.list_head_left:
                    if( mCheckAllStudyResActivity != null ) {
                        finish();
                    } else{
                        backMain();
                    }
                    break;
                default:
                    break;
            }
        }
    };

	@Override
	public void addListener() {
        //返回按钮
        backBtnImg.setOnClickListener(cClickListener);
	}

    @Override
    protected void onResume() {
        super.onResume();
        clearAllData();
        resetParameter();
        refreshData();
    }

    /**
     *  清除之前的数据，Activity复用 
     */
    private void clearAllData(){

        if (null!=adapter){
            adapter.clear();
            adapter.notifyDataSetChanged();
        }
        dataList.clear();
        noDataTv.setText("");
    }

    /**
     *  重新设定参数 
     */
    private void resetParameter(){
        pageNum=1;
        if (Constants.leftInnerType == 0) { //所有课程
            url = Constants.COURSE_URL;
        } else if (Constants.leftInnerType == 1) {
            // 非学历精品课程

        } else if (Constants.leftInnerType == 2) {
            // 课程分类下的课程
            url = Constants.COURSE_URL;
        } else if (Constants.leftInnerType == 3) {
            // 专业列表
            url = Constants.PROFRESSIONAL_URL;
        }
    }

    /**
     *  重新获取数据
     */
    private void refreshData(){
        courseListNameTV.setText(Constants.leftInnerName);
        loading_layout.setVisibility(View.VISIBLE);
        new Thread(dataLoadRunnable).start();
    }

    /**
	 *  返回到主界面并显示左侧菜单
	 */
	private void backMain() {
        String actionStr=Constants.IS_LEFTMENU_VISIABLE?"backToShowLeftMenu":"backToMain";
		Intent intent = new Intent();
		intent.setAction(actionStr);
		Helper.sendLocalBroadCast(this, intent);
	}
	
	/**
	 *  获取网络数据
	 */
	Runnable dataLoadRunnable = new Runnable() {
		@Override
		public void run() {
			parseRssJsonObject();
            //刷新界面
			handler.obtainMessage(QUERYFLAG).sendToTarget();
		}
	};
	
	/**
	 *  解析网络数据 
	 */
	private boolean parseRssJsonObject() {
		 ArrayList<Course> tmpList = new ArrayList<Course>();
		if (!"".equals(url)) {
			JSONObject jobj = null;
			try {
				if (TextUtils.isEmpty(url)) {
					return false;
				}

                if (Constants.leftInnerType == 0){
                    // 查看所有课程，清除相关参数
                    map.remove("category");
                    map.remove("specialty");
                } else if (Constants.leftInnerType == 2){
				    //课程分类
					map.put("category", Constants.leftInnerId);
				} else if (Constants.leftInnerType == 3) {
				    //专业列表
					map.put("specialty", Constants.leftInnerId);
				}
                map.put("pageNum", pageNum);
                map.put("pageSize", PAGESIZE);
				jobj = getJSONObject(url, map);

				if (null != jobj) {
					if (jobj.has("flag") && jobj.getInt("flag") == 1) {
						Log.e("resjson", ((JSONObject) jobj.get("res")).toString());
						JSONObject dataJson = ((JSONObject) jobj.get("res")).getJSONObject("data");
						Log.e("datajson", dataJson.toString());
						/*Log.e("listsize", "" + dataJson.getJSONObject("list").length());*/
						JSONArray listArr = dataJson.getJSONArray("list");
						if (null != listArr && listArr.length() > 0) {
							for(int i =0; i< listArr.length(); i++) {
								JSONObject listJobj = listArr.getJSONObject(i);
								Course item = new Course();
								if(Constants.leftInnerType == 2){
								    //课程分类
									item.course_id = listJobj.getString("course_id");
								} else if(Constants.leftInnerType == 0){
								    //所有课程
									item.course_id = listJobj.getString("course_id");
								} else if(Constants.leftInnerType == 3){
									item.course_id = listJobj.getString("pro_id");
								}
								
								item.course_name = listJobj.getString("name");
								tmpList.add(item);
							}
							
							for(int i = 0;i < tmpList.size();i ++) {
								Course course = tmpList.get(i);
								if(!dataList.contains(course)) {
									dataList.add(course);
								}
							}
							App.getAppHandler().post(new Runnable() {
								
								@Override
								public void run() {
									adapter.clear();
									adapter.addItemCollection(dataList);
									adapter.notifyDataSetChanged();
									noDataTv.setText("");
								}
							});
							
						} else {
							showNoDataTv();
						}
					} else {
						showNoDataTv();
					}
				} else {
					showNoDataTv();
				}
			} catch (JSONException e) {
				e.printStackTrace();
			} finally{
				
			}
		}
		return true;
	}

    /**
     *  显示没有数据
     */
	private void showNoDataTv() {
		if (adapter.getCount() != 0) {
			return ;
		}
		App.getAppHandler().post(new Runnable() {
			@Override
			public void run() {
				noDataTv.setText(getResources().getString(R.string.no_course));
			}
			
		});
	}
	
	/**
	 *  获取jSONObject对象
	 */
	private JSONObject getJSONObject(String url, HashMap<String, Object> map) {
		JSONObject jobj = null;
		TmingHttp tmingHttp = new TmingHttp();
		try {
			TmingResponse response = tmingHttp.tmingHttpRequest(url, map);
			jobj = response.asJSONObject();
			return jobj;
        } catch (TmingHttpException e) {

        } catch (OutOfMemoryError e) {
			
		}
		return null;
	}

}