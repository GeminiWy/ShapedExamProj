package com.nd.shapedexamproj.activity.notice;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.*;
import android.widget.AdapterView.OnItemClickListener;

import com.nd.shapedexamproj.App;
import com.nd.shapedexamproj.R;
import com.nd.shapedexamproj.activity.BaseActivity;
import com.nd.shapedexamproj.adapter.NoticeAdapter;
import com.nd.shapedexamproj.model.notice.NoticeInfo;
import com.nd.shapedexamproj.util.Constants;
import com.nd.shapedexamproj.util.PhpApiUtil;
import com.tming.common.cache.net.TmingCacheHttp;
import com.tming.common.net.TmingHttp.RequestCallback;
import com.tming.common.view.RefreshableListView;
import com.tming.common.view.RefreshableListView.OnRefreshListener;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.*;

/**
 * @Title: NoticeListActivity
 * @Description: 系统公告列表
 * @author WuYuLong
 * @date 2014-5-7
 * @version V1.0
 */
public class NoticeListActivity extends BaseActivity {
	private static final String TAG = "NoticeListActivity";
	private Context context;
	private RelativeLayout common_head_layout;
	private Button common_head_right_btn;
	private ImageView commonheader_left_iv;
	private TextView commonheader_title_tv;
	private TextView no_data_tv;
	private View loadingView;//网络指示标
	
	private TmingCacheHttp cacheHttp;
	private List<NoticeInfo> noticeList;
	
	private int pageNum;
	
	private RefreshableListView notice_list_relv;
	// 实现列表的显示
	private NoticeAdapter noticeAdapter ;
	private SharedPreferences spf ;
	@Override
	public int initResource() {
		return R.layout.notice_list_activity;
	}

	@Override
	public void initComponent() {
		context = this;
		spf = getSharedPreferences(Constants.SHARE_PREFERENCES_NAME, Context.MODE_PRIVATE);
		//设置头部透明色
		common_head_layout = (RelativeLayout) findViewById(R.id.notice_list_head);
		common_head_layout.setBackgroundColor(getResources().getColor(R.color.transparent));
		//头部按钮
		commonheader_left_iv = (ImageView) findViewById(R.id.commonheader_left_iv);
		commonheader_title_tv = (TextView) findViewById(R.id.commonheader_title_tv);
		commonheader_title_tv.setText(R.string.notice_list_activity_title);
		commonheader_title_tv.setText(commonheader_title_tv.getText());
//		common_head_left_btn = (Button) findViewById(R.id.common_head_left_btn);
//		common_head_left_btn.setText(R.string.notice_list_activity_title);
//		common_head_left_btn.setText("  " + common_head_left_btn.getText());
		common_head_right_btn = (Button) findViewById(R.id.common_head_right_btn);
		common_head_right_btn.setVisibility(View.INVISIBLE);
		
		no_data_tv = (TextView) findViewById(R.id.no_data_tv);
		notice_list_relv = (RefreshableListView)findViewById(R.id.notice_list_relv);
		//课程列表下拉刷新 added by WuYuLong
		notice_list_relv.setonRefreshListener(new OnRefreshListener() {			
			@Override
			public void onRefresh() {
				noticeReflash();
			}
			
			@Override
			public void onLoadMore() {
				++pageNum;
				requestData();
			}
		});
	}

	@Override
	public void initData() {
		noticeList = new ArrayList<NoticeInfo>();
		noticeAdapter = new NoticeAdapter(context, noticeList);
		notice_list_relv.setAdapter(noticeAdapter);
		
		cacheHttp = TmingCacheHttp.getInstance(App.getAppContext());
		
		noticeReflash();
	}
	
	/**
	 * 首次加载网络数据 或 刷新
	 */
	private void noticeReflash(){
		pageNum = 1;
		noticeList.clear();
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
		//开启网络指示标
		loadingView = findViewById(R.id.loading_layout);
		Map<String,Object> map = new HashMap<String,Object>();
		map.put("uid", App.getUserId());
		map.put("page", pageNum);
		map.put("size", 20);
		PhpApiUtil.sendData(Constants.ANNOUNCEMENT_LIST, map, new RequestCallback<String>() {

			@Override
			public String onReqestSuccess(String respones) throws Exception {
				return jsonParsing(respones);
			}

			@Override
			public void success(String respones) {
				loadData(respones);
			}

			@Override
			public void exception(Exception exception) {
				notice_list_relv.onRefreshComplete();
				if (noticeList.size() <= 0) {
					no_data_tv.setVisibility(View.VISIBLE);
				} else {
					no_data_tv.setVisibility(View.GONE);
				}
			}

		});
	}
	
	/**
	 * 网络请求成功后，加载数据
	 * @param data
	 */
	private void loadData(String data){
		JSONObject jobj = null;
		JSONArray infoArray = null;
		JSONObject noticeJson = null;
		try{
			try {
				jobj = new JSONObject(data);
			} catch (JSONException e) {
				e.printStackTrace();
			} 
			if (null != jobj) {
				int code = jobj.getInt("_c");
				if (code == 0) {
					infoArray = jobj.optJSONArray("list");
					if(infoArray != null){
						int size = infoArray.length();
						for (int i = 0; i < size; i++) {
							NoticeInfo noticeItem = new NoticeInfo();
							noticeJson = infoArray.getJSONObject(i);
							noticeItem.setNoticeId(noticeJson.getString("topic_id"));
							noticeItem.setTitle(noticeJson.getString("title"));
							noticeItem.setContent(noticeJson.getString("content"));
							noticeItem.setTime(noticeJson.getString("time"));

							noticeList.add(noticeItem);
						}
					}
				}
				Collections.sort(noticeList, new Comparator<NoticeInfo>(){

                    @Override
                    public int compare(NoticeInfo lhs, NoticeInfo rhs) {
                        return rhs.getTime().compareTo(lhs.getTime());
                    }});
				
				noticeAdapter.notifyDataSetChanged();
				if(noticeList.size() <= 0){
					no_data_tv.setVisibility(View.VISIBLE);
				} else {
					no_data_tv.setVisibility(View.GONE);
				}
				
			    //绑定点击事件
				notice_list_relv.setOnItemClickListener(new ItemClickListener());
			}
		} catch (Exception e){
			
		} finally {
		    //关闭网络指示标
		    loadingView.setVisibility(View.GONE);
		    //关闭刷新
		    notice_list_relv.onRefreshComplete();
		}
	}
	
	/**
	 * 回调方法
	 * @param data
	 * @return
	 */
	private String jsonParsing(String data){
		return data;
	}

	@Override
	public void addListener() {
		commonheader_left_iv.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// backMain();
				finish();
			}
		});
	}
	
	/**
	 * 点击列表，跳转到详情页
	 * @author Administrator
	 *
	 */
	private class ItemClickListener implements OnItemClickListener {
		@Override
		public void onItemClick(AdapterView<?> parent, View arg1, int position, long id) {
			int currPosition = position - 1;
			ListView listView = (ListView) parent;
            NoticeInfo notice = noticeList.get(currPosition);
			//UIHelper.showNoticeDetailActivity(context, topic_id);
			Intent intent = new Intent(App.getAppContext(), NoticeDetailActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			/*intent.putExtra("topic_id", topic_id);*/
            intent.putExtra("title", notice.getTitle());
            intent.putExtra("content", notice.getContent());
            intent.putExtra("time", notice.getTime());
			App.getAppContext().startActivity(intent);
		}
	}
}
