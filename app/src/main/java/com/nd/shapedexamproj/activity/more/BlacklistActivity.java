package com.nd.shapedexamproj.activity.more;

import android.app.AlertDialog;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.*;

import com.nd.shapedexamproj.App;
import com.nd.shapedexamproj.R;
import com.nd.shapedexamproj.activity.BaseActivity;
import com.nd.shapedexamproj.model.blacklist.Blacklist;
import com.nd.shapedexamproj.model.my.PersonalInfo;
import com.nd.shapedexamproj.util.Constants;
import com.nd.shapedexamproj.util.IndustryUtil;
import com.nd.shapedexamproj.util.Utils;
import com.tming.common.cache.image.ImageCacheTool;
import com.tming.common.cache.image.ImageCacheTool.ImageLoadCallBack;
import com.tming.common.cache.net.TmingCacheHttp;
import com.tming.common.cache.net.TmingCacheHttp.RequestWithCacheCallBack;
import com.tming.common.net.TmingHttp;
import com.tming.common.net.TmingHttp.RequestCallback;
import com.tming.common.view.RefreshableListView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BlacklistActivity extends BaseActivity implements OnClickListener {

	private TmingCacheHttp cacheHttp;
	private Handler handler;
	private ImageCacheTool imageCacheTool;

	private View loadingView;
	private RefreshableListView blacklist;
	private ImageView goBack;
	private TextView currLocation, notBlacklist;
	private BlacklistAdapter blacklistAdapter;
	private List<Blacklist> blacklists;
	private int pageNum = 1, pageSize = 10;
	private AlertDialog waitDialog;
	
	@Override
	public int initResource() {
		// TODO Auto-generated method stub
		return R.layout.blacklist;
	}

	@Override
	public void initComponent() {
		// TODO Auto-generated method stub
		goBack = (ImageView) findViewById(R.id.commonheader_left_iv);

		currLocation = (TextView) findViewById(R.id.commonheader_title_tv);
		currLocation.setText(getResources().getString(R.string.black_list));

		loadingView = findViewById(R.id.loading_layout);

		notBlacklist = (TextView) findViewById(R.id.notBlacklist);

		View waitLayout = getLayoutInflater().inflate(R.layout.blacklist_moveout_dialog, null);
		waitDialog = new AlertDialog.Builder(this).setView(
				waitLayout).create();
		//waitDialog.setCancelable(false);
		
		blacklist = (RefreshableListView) findViewById(R.id.refreshable_listview);
		
		/*
		 * blacklist.setDividerHeight(Utils.dip2px(this, 8));
		 */
		// 重设RefreshableListView的左右边距，这里设为8dp，原xml设置10dp,感觉看起来不大协调
		RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
				LinearLayout.LayoutParams.MATCH_PARENT,
				LinearLayout.LayoutParams.MATCH_PARENT); // , 1是可选写的
		lp.setMargins(Utils.dip2px(this, 10), 0, Utils.dip2px(this, 10), 0);
		blacklist.setLayoutParams(lp);

	}

	
	private void initHandler() {
		// TODO Auto-generated method stub
		handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				switch (msg.what) {
				case -1:
					//loadingView.setVisibility(View.GONE);
					String result = (String) msg.obj;
					parseBlacklistJsonResult(result);
					//blacklistAdapter.notifyDataSetChanged();
					break;

				case 0:
					waitDialog.show();
					//removeBlacklist(((Blacklist)msg.obj).getBlackid());
					removeBlacklist((Blacklist)msg.obj);
					/*
					blacklists.remove(blacklists.get(msg.arg1));
					blacklistAdapter.notifyDataSetChanged();
					if(blacklists.size()==0){
						resetByNotBlacklist();
					}
					break;
					*/
					
				case 1:
					loadingView.setVisibility(View.GONE);
					blacklistAdapter.notifyDataSetChanged();
					if(blacklists.size()==0){
						resetByNotBlacklist();
					}
					break;

				case 2:
					if(msg.arg1==1){
						waitDialog.dismiss();
						blacklists.remove(msg.obj);
						blacklistAdapter.notifyDataSetChanged();
						if(blacklists.size()==0){
							resetByNotBlacklist();
						}
					}else if(msg.arg1 == -1){
						
					}
					break;
				}
			}
		};
	}
	
	
	@Override
	public void initData() {
		// TODO Auto-generated method stub
		/*
		 * for (int i = 1; i < 15; i++) { blacklists.add(new Blacklist(i, 10 +
		 * i, 100 + i)); }
		 */
		blacklists = new ArrayList<Blacklist>();
		blacklistAdapter = new BlacklistAdapter();
		blacklist.setAdapter(blacklistAdapter);
		
		cacheHttp = TmingCacheHttp.getInstance(this);
		initHandler();
		loadingView.setVisibility(View.GONE);
		imageCacheTool = ImageCacheTool.getInstance(this);
		requestMyDynamicsList(App.getUserId(), pageNum, pageSize);
	}

	
	/**
	 * 移除黑名单
	 * @param blacklist
	 */
	private void removeBlacklist(final Blacklist blacklist){
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("blackid", blacklist.getBlackid());
		TmingHttp.asyncRequest(Constants.HOST_DEBUG
				+ "userblacklist/delUserblacklist.html", params,
				new RequestCallback<Integer>() {

					@Override
					public Integer onReqestSuccess(String respones)
							throws Exception {
						// TODO Auto-generated method stub
						return new JSONObject(respones).getInt("flag");
					}

					@Override
					public void success(Integer respones) {
						// TODO Auto-generated method stub
						Message msg = new Message();
						msg.what = 2;
						msg.arg1 = respones;
						msg.obj = blacklist;
						handler.sendMessage(msg);
					}

					@Override
					public void exception(Exception exception) {
						// TODO Auto-generated method stub
						Message msg = new Message();
						msg.what = 2;
						msg.arg1 = -1;
						msg.obj = blacklist;
						handler.sendMessage(msg);
					}
			
		});
	}
	
	/**
	 * 获取黑名单数据
	 * 
	 * @param userId
	 * @param pageNum
	 * @param pageSize
	 */
	private void requestMyDynamicsList(String userId, int pageNum, int pageSize) {
		// TODO Auto-generated method stub
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("userid", userId);
		params.put("pageNum", pageNum);
		params.put("pageSize", pageSize);
		cacheHttp.asyncRequestWithCache(Constants.HOST_DEBUG
				+ "userblacklist/getUserblacklistList.html", params,
				new RequestWithCacheCallBack<List<Blacklist>>() {

					@Override
					public List<Blacklist> onPreRequestCache(String cache)
							throws Exception {
						// TODO Auto-generated method stub
						//System.out.println("cache : " + cache);
						Message msg = new Message();
						msg.what = -1;
						msg.obj = cache;
						handler.sendMessage(msg);
						return null;
					}

					@Override
					public void onPreRequestSuccess(List<Blacklist> data) {
						// TODO Auto-generated method stub

					}

					@Override
					public List<Blacklist> onReqestSuccess(String respones)
							throws Exception {
						// TODO Auto-generated method stub
						//System.out.println("respones : " + respones);
						Message msg = new Message();
						msg.what = -1;
						msg.obj = respones;
						handler.sendMessage(msg);
						return null;
					}

					@Override
					public void success(List<Blacklist> cacheRespones,
							List<Blacklist> newRespones) {
						// TODO Auto-generated method stub

					}

					@Override
					public void exception(Exception exception) {
						// TODO Auto-generated method stub

					}

				});
	}

	

	
	
	/**
	 * 个人信息网络请求
	 */
	private void requestPersonal(final Blacklist blacklist) {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("userid", blacklist.getUserid());
		cacheHttp.asyncRequestWithCache(Constants.HOST + "user/getUser.html", map,
				new RequestWithCacheCallBack<PersonalInfo>() {

					@Override
					public PersonalInfo onPreRequestCache(String cache) throws Exception {
						return personalInfoJSONPasing(cache);
					}

					@Override
					public void onPreRequestSuccess(PersonalInfo data) {
						//setPersonalView(data);
						blacklist.setUserImgAddr(data.photoUrl);
						blacklist.setUserName(data.getUserName());
						blacklists.add(blacklist);
						
						handler.sendEmptyMessage(1);
					}

					@Override
					public PersonalInfo onReqestSuccess(String respones) throws Exception {
						return personalInfoJSONPasing(respones);
					}

					@Override
					public void success(PersonalInfo cacheRespones, PersonalInfo newRespones) {
						//setPersonalView(newRespones);
						System.out.println(cacheRespones.getUserName()+" , "+newRespones.getUserName());
						blacklist.setUserImgAddr(newRespones.photoUrl);
						blacklist.setUserName(newRespones.getUserName());
						handler.sendEmptyMessage(1);
					}

					@Override
					public void exception(Exception exception) {

					}
				});
	}
	
	
	
	/**
	 * 个人信息Json解析
	 */
	private PersonalInfo personalInfoJSONPasing(String result) {
		PersonalInfo personalInfo = new PersonalInfo();
		try {
			//System.out.println("个人信息："+result);
			JSONObject personal = new JSONObject(result);
			JSONObject user = personal.getJSONObject("user");
			personalInfo.sex = user.getInt("sex");
			personalInfo.age = user.getInt("age");
			personalInfo.setUserName(user.getString("username"));
			//personalInfo.phone = user.getString("phone");
			//personalInfo.city = user.getString("city");
			//personalInfo.hobby = Base64.decode(user.getString("hobby"));
			//personalInfo.juniormiddleSchool = Base64.decode(user.getString("juniormiddleschool"));
			//personalInfo.seniormiddleSchool = Base64.decode(user.getString("seniormiddleschool"));
			//personalInfo.primarySchool = Base64.decode(user.getString("primaryschool"));
			//personalInfo.university = Base64.decode(user.getString("university"));
			//personalInfo.star = user.getString("constellation");
			//personalInfo.profession = Base64.decode(user.getString("profession"));
			//personalInfo.explanation = Base64.decode(user.getString("explanation"));
			personalInfo.photoUrl = user.getString("photo");
			personalInfo.industry = IndustryUtil.getIndustry(user.getInt("industryid")).split("-")[1];
			//personalInfo.company = Base64.decode(user.getString("company"));
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return personalInfo;
	}
	
	
	
	/**
	 * 解析黑名单数据
	 * 
	 * @param result
	 */
	private void parseBlacklistJsonResult(String result) {
		// TODO Auto-generated method stub
		try {
			JSONObject object = new JSONObject(result);
			if (!object.isNull("flag") && object.getInt("flag") == 1) {
				if (!object.isNull("list")) {
					JSONArray list = object.getJSONArray("list");
					if (list != null && list.length() > 0) {
						for (int i = 0; i < list.length(); i++) {
							Blacklist blacklist = new Blacklist();
							blacklist.setBlackid(list.getJSONObject(i).getInt(
									"blackid"));
							blacklist.setBlackuserid(list.getJSONObject(i)
									.getInt("blackuserid"));
							blacklist.setUserid(list.getJSONObject(i).getInt(
									"userid"));
							
							requestPersonal(blacklist);
							
						}
					}else{
						handler.sendEmptyMessage(1);
					}
				}
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}

	@Override
	public void addListener() {
		// TODO Auto-generated method stub
		goBack.setOnClickListener(this);
	}

	class BlacklistAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return blacklists.size();
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			ViewHolder viewHolder;
			if (convertView == null) {
				convertView = LayoutInflater.from(BlacklistActivity.this)
						.inflate(R.layout.blacklist_item, null);

				viewHolder = new ViewHolder();
				viewHolder.userImage = (ImageView) convertView
						.findViewById(R.id.userImage);
				viewHolder.userName = (TextView) convertView
						.findViewById(R.id.userName);
				viewHolder.moveOut = (Button) convertView
						.findViewById(R.id.moveOut);
				
				viewHolder.indicator = convertView.findViewById(R.id.indicator);
				convertView.setTag(viewHolder);
			} else {
				viewHolder = (ViewHolder) convertView.getTag();
			}
			if(blacklists.get(position).getUserName()!=null){
				viewHolder.userName.setText(blacklists.get(position).getUserName());
			}
			

			viewHolder.moveOut.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					Message msg = new Message();
					msg.obj = blacklists.get(position);
					msg.what = 0;
					handler.sendMessage(msg);
				}
			});

			
			final ImageView userImage = viewHolder.userImage;
			
			
			if(blacklists.get(position).getUserImgAddr()!=null){
				try {
					imageCacheTool.asyncLoadImage(new URL(blacklists.get(position).getUserImgAddr()), new ImageLoadCallBack(){

						@Override
						public void loadResult(Bitmap bitmap) {
							// TODO Auto-generated method stub
							userImage.setImageBitmap(bitmap);
						}

						@Override
						public void progress(int progress) {
							// TODO Auto-generated method stub
							
						}
						
					});
				} catch (MalformedURLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			return convertView;
		}

		final class ViewHolder {
			private ImageView userImage;
			private TextView userName;
			private Button moveOut;
			private View indicator;
		}

	}

	

	private void resetByNotBlacklist() {
		//blacklist.setVisibility(View.GONE);
		notBlacklist.setVisibility(View.VISIBLE);
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		if (v == goBack) {
			finish();
		}
	}

}
