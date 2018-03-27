package com.nd.shapedexamproj.activity.remind;

import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nd.shapedexamproj.App;
import com.nd.shapedexamproj.R;
import com.nd.shapedexamproj.activity.BaseActivity;
import com.nd.shapedexamproj.util.Constants;
import com.tming.common.cache.net.TmingCacheHttp;
import com.tming.common.cache.net.TmingCacheHttp.RequestWithCacheCallBack;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

/**
 * @Title: MyRemindActivity
 * @Description: 我的提醒
 * @author WuYuLong
 * @date 2014-5-7
 * @version V1.0
 */
public class MyRemindActivity extends BaseActivity {
	private static final String TAG = "MyRemindActivity";
	private Context context;
	private RelativeLayout common_head_layout;
	private Button common_head_right_btn;
	private ImageView commonheader_left_iv;
	private TextView commonheader_title_tv;
	private View loadingView;//网络指示标
	
	private TmingCacheHttp cacheHttp;
	private HashMap<String, Object> map;

	private TextView remind_exam_content_tv;
	
	@Override
	public int initResource() {
		return R.layout.remind_my_activity;
	}

	@Override
	public void initComponent() {
		context = this;
		//设置头部透明色
		common_head_layout = (RelativeLayout) findViewById(R.id.remind_my_head);
		common_head_layout.setBackgroundColor(getResources().getColor(R.color.transparent));
		//头部按钮
		commonheader_left_iv = (ImageView) findViewById(R.id.commonheader_left_iv);
		commonheader_title_tv = (TextView) findViewById(R.id.commonheader_title_tv);
		commonheader_title_tv.setText(R.string.remind_my_activity_title);
		commonheader_title_tv.setText("  " + commonheader_title_tv.getText());
		
//		common_head_left_btn = (Button) findViewById(R.id.common_head_left_btn);
//		common_head_left_btn.setText(R.string.remind_my_activity_title);
//		common_head_left_btn.setText("  " + common_head_left_btn.getText());
		common_head_right_btn = (Button) findViewById(R.id.common_head_right_btn);
		common_head_right_btn.setVisibility(View.INVISIBLE);
		
		remind_exam_content_tv  = (TextView) findViewById(R.id.msg_fragment_item_content);
	}

	@Override
	public void initData() {
		cacheHttp = TmingCacheHttp.getInstance(App.getAppContext());
		map = new HashMap<String, Object>();
		noticeReflash();
	}
	
	/**
	 * 首次加载网络数据 或 刷新
	 */
	private void noticeReflash(){
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
		
		String url = Constants.EXAM_REMIND_LAST;
		map.put("userid", App.getUserId());
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
		JSONObject jobj = null;
		JSONObject dataJson = null;
		JSONObject infoJson = null;
		try{
			try {
				jobj = new JSONObject(data);
			} catch (JSONException e) {
				e.printStackTrace();
			} 
			if (null != jobj) {
				if (jobj.has("flag") && jobj.getInt("flag") == 1) {
					
					int code = jobj.getJSONObject("res").getInt("code");
					if (code == 1) {
						dataJson = ((JSONObject) jobj.get("res")).getJSONObject("data");
						infoJson = dataJson.optJSONObject("info");
						if(infoJson != null){
							String content = infoJson.optString("content");
							if(content != null){
								remind_exam_content_tv.setText(content);
							}
						}
					}
				}
			}
		} catch (Exception e){
			
		} finally {
		    //关闭网络指示标
		    loadingView.setVisibility(View.GONE);
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
}
