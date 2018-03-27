package com.nd.shapedexamproj.activity.my;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.*;
import android.widget.RelativeLayout.LayoutParams;

import com.nd.shapedexamproj.App;
import com.nd.shapedexamproj.R;
import com.nd.shapedexamproj.activity.BaseActivity;
import com.nd.shapedexamproj.model.Course;
import com.nd.shapedexamproj.model.Term;
import com.nd.shapedexamproj.util.Constants;
import com.nd.shapedexamproj.util.StringUtils;
import com.nd.shapedexamproj.util.UIHelper;
import com.tming.common.adapter.CommonBaseAdapter;
import com.tming.common.cache.net.TmingCacheHttp;
import com.tming.common.net.TmingHttp;
import com.tming.common.net.TmingHttp.RequestCallback;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
/**
 * 成绩查询页
 * @author zll
 * create in 2014-4-24
 */
public class MyScoreActivity extends BaseActivity implements OnClickListener{
	
	private static final String TAG = "MyScoreActivity";
	
	private Button common_head_right_btn;
	private ImageView commonheader_left_iv;
	private TextView commonheader_title_tv, no_data_tv;
	
	private LinearLayout loadingLayout;
	private RelativeLayout errorLayout;
	private Button errorBtn ;
	private PopupWindow pop ;
	private View pop_view = null;
	
	private ScoreListAdapter adapter;
	private ListView myscore_list;
	private Button test_score_btn,homework_score_btn ;
	private List<Course> mCourselist;
	private List<Term> termList = new LinkedList<Term>(); ; //学期列表
	private Term mCurrentTerm;//当前选中的学期
	private TmingCacheHttp cacheHttp ;
	private boolean isFreshMan = false;//是否是新生
	/**
	 * 学期专业id
	 */
	private String term_id = App.sTermId;		
	private int pageSize = 10;
	private int pageNum = 1;
	
	
	@Override
	public int initResource() {
		return R.layout.my_score_activity;
	}

	@Override
	public void initComponent() {
		commonheader_left_iv = (ImageView) findViewById(R.id.commonheader_left_iv);
		commonheader_title_tv = (TextView) findViewById(R.id.commonheader_title_tv);
		commonheader_title_tv.setText(getResources().getString(R.string.score_query));
		no_data_tv = (TextView) findViewById(R.id.no_data_tv);
		common_head_right_btn = (Button) findViewById(R.id.common_head_right_btn);
		common_head_right_btn.setVisibility(View.GONE);
		common_head_right_btn.setFocusable(false);
		
		loadingLayout = (LinearLayout) findViewById(R.id.loading_layout);
		errorLayout = (RelativeLayout) findViewById(R.id.error_layout);
		errorBtn = (Button) findViewById(R.id.error_btn);
		
		myscore_list = (ListView) findViewById(R.id.myscore_list);
		
		test_score_btn = (Button) findViewById(R.id.test_score_btn);
		homework_score_btn = (Button) findViewById(R.id.homework_score_btn);
		homework_score_btn.setFocusable(false);
		cacheHttp = TmingCacheHttp.getInstance(this);
		
	}
	
	@Override
	public void initData() {
		mCourselist = new LinkedList<Course>();
		
		cacheHttp = TmingCacheHttp.getInstance(this);
		
		adapter = new ScoreListAdapter(this);
		myscore_list.setAdapter(adapter);
		
		requestTermList();
	}
	
	/**
	 * 获取学期列表
	 */
	private void requestTermList(){
		String url = Constants.MY_TERM_LIST;
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("pageNum", pageNum);
		map.put("pageSize", pageSize);
		map.put("userid", App.getUserId());
		TmingHttp.asyncRequest(url, map, new RequestCallback<List<Term>>() {

			@Override
			public List<Term> onReqestSuccess(String respones) throws Exception {
				return Term.ParseTermList(respones);
			}

			@Override
			public void success(List<Term> respones) {
				termList.clear();
				termList.addAll(respones);
				if (respones.size() == 1) {
				    isFreshMan = true;
				} else {
				    isFreshMan = false;
				}
				
				for(Term term : termList) {
					if(term.isActionTerm())
					{
						mCurrentTerm = term;
						break;
					}
				}
				setTerm(mCurrentTerm);
				
			}

			@Override
			public void exception(Exception exception) {
				Toast.makeText(MyScoreActivity.this, getResources().getString(R.string.net_error_tip), 
						Toast.LENGTH_SHORT).show();
				loadingLayout.setVisibility(View.GONE);
				errorLayout.setVisibility(View.VISIBLE);
			}
			
		});
	}
	/**
	 * 查询成绩列表
	 */
	private void requestScoreList(){
		String url = Constants.MY_SCORE;
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("term_id", term_id);
		if(null != mCurrentTerm){
			map.put("step_id", mCurrentTerm.getId());
		}
		map.put("userid", App.getUserId());
		map.put("pageSize", pageSize);
		map.put("pageNum", pageNum);
		
		TmingHttp.asyncRequest(url, map, new RequestCallback<List<Course>>() {

			@Override
			public List<Course> onReqestSuccess(String respones)
					throws Exception {
				return jsonParsing(respones);
			}

			@Override
			public void success(List<Course> respones) {
				loadData(respones);
			}

			@Override
			public void exception(Exception exception) {
				
			}
		});
		
		/*cacheHttp.asyncRequestWithCache(url, map, new RequestWithCacheCallBack<List<Course>>() {

			@Override
			public List<Course> onPreRequestCache(String cache)
					throws Exception {
				return jsonParsing(cache);
			}

			@Override
			public void onPreRequestSuccess(List<Course> data) {
				loadData(data);
			}

			@Override
			public List<Course> onReqestSuccess(String respones)
					throws Exception {
				return jsonParsing(respones);
			}

			@Override
			public void success(List<Course> cacheRespones,
					List<Course> newRespones) {
				loadData(cacheRespones,newRespones);
			}

			@Override
			public void exception(Exception exception) {
				
			}
		
		});*/
		
	}
	/**
	 * 解析数据
	 * @param str
	 */
	private List<Course> jsonParsing(String str){
		List<Course> courList = new LinkedList<Course>();
		try {
			JSONObject jobj = new JSONObject(str);
			int flag = jobj.getInt("flag");
			if (flag != 1) {
				App.dealWithFlag(flag);
				return null;
			}
			JSONObject dataObject = jobj.getJSONObject("res").getJSONObject("data");
			int total = dataObject.getInt("total");
			if(total == 0){		//没有成绩列表

				return courList;
			}
			JSONArray listArray = dataObject.getJSONArray("list");
			
			for (int i = 0; i < listArray.length(); i++) {
				Course course = new Course();
				JSONObject listObject = listArray.getJSONObject(i);
				course.course_id = listObject.getString("course_id");
				course.course_name = listObject.getString("course_name");
				String score = listObject.getString("course_score");
				course.score = Float.parseFloat(score);
				course.is_exemption = listObject.getInt("is_exemption");
				courList.add(course);
			}
			
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		return courList;
	}
	
	private void loadData(List<Course> data){
		loadingLayout.setVisibility(View.GONE);
		errorLayout.setVisibility(View.GONE);
		if (data == null) {
			return ;
		}
		if (data.size() == 0 ) {
			no_data_tv.setVisibility(View.VISIBLE);
		} else {
			no_data_tv.setVisibility(View.GONE);
		}
		adapter.clear();
		adapter.addItemCollection(data);
		adapter.notifyDataSetChanged();
	}
	
	private void loadData(List<Course> cacheRespones, List<Course> newRespones) {
		loadingLayout.setVisibility(View.GONE);
		errorLayout.setVisibility(View.GONE);
		if (newRespones == null) {
			return ;
		}
		if(newRespones.size() == 0 && adapter.getCount() == 0){
			no_data_tv.setVisibility(View.VISIBLE);
			return;
		} else {
			no_data_tv.setVisibility(View.GONE);
		}
		
		adapter.replaceItem(cacheRespones, newRespones);
		adapter.notifyDataSetChanged();
	}
	
	private class myPopClickListener implements OnClickListener{
		private Term term;
		public myPopClickListener(Term term){
			this.term = term ;
		}
		
		@Override
		public void onClick(View v) {
			mCurrentTerm = term;
			setTerm(mCurrentTerm);
			pop.dismiss();
		}
		
	}
	
	private void showPopAtLocation(){
		// 引入弹出窗配置文件  
        pop_view = (RelativeLayout) LayoutInflater.from(MyScoreActivity.this).inflate(R.layout.popup_empty_layout,null);
        LinearLayout popup_empty_ll = (LinearLayout) pop_view.findViewById(R.id.popup_empty_ll);
		for(int i = 0;i < termList.size();i ++){
	        Term term = termList.get(i);
			View itemView = LayoutInflater.from(MyScoreActivity.this).inflate(R.layout.popup_list_item, null);
	        Button termNameBtn = (Button) itemView.findViewById(R.id.popup_item_btn) ;
	        termNameBtn.setText(term.name);
	        termNameBtn.setOnClickListener(new myPopClickListener(term));
	        
	        popup_empty_ll.addView(itemView);
		}
		// 创建PopupWindow对象  
        pop = new PopupWindow(pop_view, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, false);  
        // 需要设置一下此参数，点击外边可消失  
        pop.setBackgroundDrawable(new BitmapDrawable());  
        //设置点击窗口外边窗口消失  
        pop.setOutsideTouchable(true);  
        // 设置此参数获得焦点，否则无法点击  
        pop.setFocusable(true);
        
        pop.showAsDropDown(common_head_right_btn, 0, 0);
	}
	
	
	@Override
	public void addListener() {
		homework_score_btn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
			    UIHelper.showFinishedHomework(MyScoreActivity.this, null);
			}
		});
		common_head_right_btn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
			    if (!isFreshMan) {
			        showPopAtLocation();
			    }
			}
		});
		commonheader_left_iv.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				finish();
			}
		});
		errorBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				requestTermList();
			}
		});
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.common_head_left_btn:
			finish();
			break;
			
		default:
			break;
		}
	}
	
	
	private class ScoreListAdapter extends CommonBaseAdapter<Course>{
		
		public ScoreListAdapter(Context context) {
			super(context);
		}
		
		/*public ScoreListAdapter(List<Course> cacheList)
		{
			this.cacheList = cacheList;
		}*/

		/*@Override
		public int getCount() {
			return cacheList.size();
		}

		@Override
		public Object getItem(int position) {
			return cacheList.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			ViewHolder holder;
			
			if(convertView == null){
				holder = new ViewHolder();
				convertView = LayoutInflater.from(MyScoreActivity.this).inflate(R.layout.finished_homework_item, null);
				holder.tv_homework_name = (TextView) convertView.findViewById(R.id.tv_homework_name);
				holder.tv_homework_score = (TextView) convertView.findViewById(R.id.tv_homework_score);
				holder.homework_item_rl = (RelativeLayout) convertView.findViewById(R.id.homework_item_rl);
				
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			
			Course course = cacheList.get(position);
			holder.tv_homework_name.requestFocus();
			holder.tv_homework_name.setText(course.course_name);
			
			if(course.score < 60 && course.score >= 0 ){
				holder.tv_homework_score.setTextColor(getResources().getColor(R.color.red));
				holder.tv_homework_score.setText(""+course.score + "分");
			} else if(course.score >= 60){
				holder.tv_homework_score.setTextColor(getResources().getColor(R.color.title_green));
				holder.tv_homework_score.setText(""+course.score + "分");
			} else if(course.score == -1){
				holder.tv_homework_score.setTextColor(getResources().getColor(R.color.light_black));
				holder.tv_homework_score.setText("暂无");
			}
			
			
			return convertView;
		}*/

		@Override
		public View infateItemView(Context context) {
			
			ViewHolder holder = new ViewHolder();
			View convertView = LayoutInflater.from(MyScoreActivity.this).inflate(R.layout.finished_homework_item, null);
			holder.tv_homework_name = (TextView) convertView.findViewById(R.id.tv_homework_name);
			holder.tv_homework_score = (TextView) convertView.findViewById(R.id.tv_homework_score);
			holder.homework_item_rl = (RelativeLayout) convertView.findViewById(R.id.homework_item_rl);
			
			convertView.setTag(holder);
			return convertView;
		}

		@Override
		public void setViewHolderData(BaseViewHolder arg0,
				Course course) {
			ViewHolder holder = (ViewHolder) arg0;
			holder.tv_homework_name.requestFocus();
			holder.tv_homework_name.setText(course.course_name);
			if (course.is_exemption == 0) {
			
    			if (course.score < 60 && course.score >= 0 ) {
    				holder.tv_homework_score.setTextColor(getResources().getColor(R.color.red));
    				holder.tv_homework_score.setText(""+course.score + "分");
    			} else if(course.score >= 60) {
    				holder.tv_homework_score.setTextColor(getResources().getColor(R.color.title_green));
    				holder.tv_homework_score.setText(""+course.score + "分");
    			} else if(course.score == -1) {
    				holder.tv_homework_score.setTextColor(getResources().getColor(R.color.light_black));
    				holder.tv_homework_score.setText(getResources().getString(R.string.no_score_));
    			}
			} else {
			    holder.tv_homework_score.setText(getResources().getString(R.string.no_need_to_study));
			}
		}
		private class ViewHolder extends BaseViewHolder{
			private TextView tv_homework_name;
			private TextView tv_homework_score;
			private RelativeLayout homework_item_rl;
		}
		
	}
	
	private void setTerm(Term term)
	{
		try {
			mCourselist.clear();
			if (!StringUtils.isEmpty(term.getName())) {
			    common_head_right_btn.setVisibility(View.VISIBLE);
			    common_head_right_btn.setText(term.getName());
			} else {
			    common_head_right_btn.setVisibility(View.GONE);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		requestScoreList();
	}
	
}
