package com.nd.shapedexamproj.activity.my;

import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.nd.shapedexamproj.App;
import com.nd.shapedexamproj.R;
import com.nd.shapedexamproj.activity.BaseActivity;
import com.nd.shapedexamproj.util.Constants;
import com.tming.common.cache.net.TmingCacheHttp;
import com.tming.common.cache.net.TmingCacheHttp.RequestWithCacheCallBack;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.*;

/**
 * 
 * 考试安排
 * 
 * @author Linlg
 * 
 *         Create on 2014-4-4
 */
public class ExamActivity extends BaseActivity implements OnClickListener {
	private ListView contentLv;
	private TmingCacheHttp cacheHttp;
	private int pageNum = 1, pageSize = 10;
	/*
	 * @Override protected void onCreate(Bundle savedInstanceState) {
	 * super.onCreate(savedInstanceState); cacheHttp =
	 * TmingCacheHttp.getInstance(this); setContentView(R.layout.my_exam);
	 * initView(); requestInfo(); }
	 */

	private void initView() {
		findViewById(R.id.commonheader_right_btn).setVisibility(View.GONE);
		contentLv = (ListView) findViewById(R.id.myexam_lv);
		findViewById(R.id.commonheader_left_iv).setOnClickListener(this);
		((TextView) findViewById(R.id.commonheader_title_tv)).setText("考试安排");
	}

	private void requestInfo() {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("pageNum", pageNum);
		map.put("pageSize", pageSize);
		map.put("userid", App.getUserId());
		cacheHttp.asyncRequestWithCache(Constants.HOST + "exam/list.html", map,
				new RequestWithCacheCallBack<List<ExamInfo>>() {

					@Override
					public List<ExamInfo> onPreRequestCache(String cache)
							throws Exception {
						return examJSONParsing(cache);
					}

					@Override
					public void onPreRequestSuccess(List<ExamInfo> data) {
						setExamView(data);
					}

					@Override
					public List<ExamInfo> onReqestSuccess(String respones)
							throws Exception {
						return examJSONParsing(respones);
					}

					@Override
					public void success(List<ExamInfo> cacheRespones,
							List<ExamInfo> newRespones) {
						setExamView(newRespones);
					}

					@Override
					public void exception(Exception exception) {

					}
				});
	}

	private void setExamView(List<ExamInfo> examInfos) {
		contentLv.setAdapter(new ExamAdapter(examInfos));
	}

	private List<ExamInfo> examJSONParsing(String result) {
		List<ExamInfo> examInfos = new ArrayList<ExamInfo>();
		try {
			JSONObject dataObj = new JSONObject(result).getJSONObject("res")
					.getJSONObject("data");
			if(!dataObj.isNull("list")){
				JSONObject listObj = dataObj.getJSONObject("list");
				Iterator itr = listObj.keys();
				
				while(itr.hasNext()){
					String key = (String) itr.next();
					JSONObject keyObj = listObj.getJSONObject(key);
					
					ExamInfo examInfo = new ExamInfo();
					examInfo.examId = keyObj.getString("course_id");
					examInfo.examTitle = keyObj.getString("course_name");
					if(keyObj.isNull("exam_address")){
						examInfo.examAddress = keyObj.getString("exam_address");
					}
					if(keyObj.isNull("exam_pos")){
						examInfo.examPosition = keyObj.getString("exam_pos");
					}
					/*examInfo.examDate = keyObj.getLong("exam_begin");*/
					examInfo.examTime = keyObj.getString("exam_begin");
					examInfo.examType = keyObj.getString("kind");
					examInfos.add(examInfo);
					
				}
				/*for (int i = 0; i < listObj.length(); i++) {
					ExamInfo examInfo = new ExamInfo();
					examInfo.examTitle = listObj.getJSONObject(i).getString(
							"exam_title");
					examInfo.examAddress = listObj.getJSONObject(i).getString(
							"exam_address");
					examInfo.examNumber = listObj.getJSONObject(i).getInt(
							"exam_seatnum");
					examInfo.examDate = listObj.getJSONObject(i).getLong("exam_date");
					examInfo.examType = listObj.getJSONObject(i)
							.getString("exam_type");
					examInfos.add(examInfo);
				}*/
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return examInfos;
	}

	private class ExamAdapter extends BaseAdapter {
		private List<ExamInfo> examInfos = new ArrayList<ExamInfo>();

		public ExamAdapter(List<ExamInfo> examInfos) {
			this.examInfos = examInfos;
		}

		@Override
		public int getCount() {
			return examInfos.size();
		}

		@Override
		public Object getItem(int position) {
			return null;
		}

		@Override
		public long getItemId(int position) {
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder = null;
			if (convertView == null) {
				holder = new ViewHolder();
				convertView = View.inflate(ExamActivity.this,
						R.layout.my_exam_item, null);
				holder.titleTv = (TextView) convertView
						.findViewById(R.id.myexam_title_tv);
				holder.dateTv = (TextView) convertView
						.findViewById(R.id.myexam_time_tv);
				holder.typeTv = (TextView) convertView
						.findViewById(R.id.myexam_exam_tv);
				holder.addressTv = (TextView) convertView
						.findViewById(R.id.myexam_address_tv);
				holder.numberTv = (TextView) convertView
						.findViewById(R.id.myexam_number_tv);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			
			holder.dateTv.setText(examInfos.get(position).examTime);
			holder.titleTv.setText(examInfos.get(position).examTitle);
			String examType = "";
			if (examInfos.get(position).examType.equals("0")) {
				examType = "闭卷" ;
			} else {
				examType = "开卷" ;
			}
			holder.typeTv.setText("考核方式：" + examType);
			holder.addressTv.setText("考试地点："
					+ examInfos.get(position).examAddress);
			holder.numberTv.setText("考场座位号：" + examInfos.get(position).examPosition);
			return convertView;
		}

	}

	private class ViewHolder {
		private TextView titleTv;
		private TextView dateTv;
		private TextView typeTv;
		private TextView addressTv;
		private TextView numberTv;
	}

	public class ExamInfo {
		
		public String examId;
		/**
		 * 考试时间
		 */
		public long examDate; //TODO 接口只返回时间点
		/**
		 * 考试时间
		 */
		public String examTime ;
		
		/**
		 * 考试类型，开卷：1，闭卷：0
		 */
		public String examType = "";
		
		public String examAddress = "";
		/**
		 * 科目
		 */
		private String examTitle;
		/**
		 * 考试座位
		 */
		public String examPosition = "";
	}

	@Override
	public int initResource() {
		return R.layout.my_exam;
	}

	@Override
	public void initComponent() {
		cacheHttp = TmingCacheHttp.getInstance(this);
		initView();
		requestInfo();
	}

	@Override
	public void initData() {

	}

	@Override
	public void addListener() {

	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.commonheader_left_iv:
			finish();
			break;
		}
	}
}
