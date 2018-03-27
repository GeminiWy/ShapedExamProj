package com.nd.shapedexamproj.activity.my;


import android.graphics.Matrix;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.nd.shapedexamproj.App;
import com.nd.shapedexamproj.R;
import com.nd.shapedexamproj.activity.BaseActivity;
import com.nd.shapedexamproj.model.my.Flower;
import com.nd.shapedexamproj.util.Constants;
import com.tming.common.cache.net.TmingCacheHttp;
import com.tming.common.cache.net.TmingCacheHttp.RequestWithCacheCallBack;
import com.tming.common.view.RefreshableListView;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MyFlowerActivity extends BaseActivity implements OnClickListener{
	private List<View> pages;// Tab页面列表
	private int offset = 0;// 动画图片偏移量
	private int currentIndex = 0;// 当前页卡编号
	private int width = 0;// 动画图片宽度
	private int receivedPageNum = 1,sendedPageNum = 1,pageSize = 10;
	
	private Handler handler;
	private TmingCacheHttp cacheHttp;
	
	private List<Flower> receivedFlowers,sendedFlowers;
	private ViewPager viewPager;
	private ImageView goBack,cursorIv;
	private TextView currLocation,myFlowerReceived,myFlowerSended,myFlowerCanSend;
	private View  myFlowerReceivedLoadingView,myFlowerSendedLoadingView;
	private FlowerAdapter receivedFlowerAdapter,sendedFlowerAdapter;
	
	@Override
	public int initResource() {
		// TODO Auto-generated method stub
		return R.layout.my_flower;
	}

	@Override
	public void initComponent() {
		// TODO Auto-generated method stub
		goBack = (ImageView) findViewById(R.id.commonheader_left_iv);

		currLocation = (TextView) findViewById(R.id.commonheader_title_tv);
		currLocation.setText(getResources().getString(R.string.my_flower));
		
		cursorIv = (ImageView) findViewById(R.id.my_flower_cursor_iv);
		viewPager = (ViewPager) findViewById(R.id.my_flower_content_vp);
		
		myFlowerReceived = (TextView)findViewById(R.id.my_flower_received);
		myFlowerSended = (TextView)findViewById(R.id.my_flower_sended);
		myFlowerCanSend = (TextView)findViewById(R.id.my_flower_can_send);
		
		cacheHttp = TmingCacheHttp.getInstance(this);
		receivedFlowers = new ArrayList<Flower>();
		sendedFlowers = new ArrayList<Flower>();
		
		initHandler();
		initViewPager();
	}

	
	private void initHandler() {
		// TODO Auto-generated method stub
		handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				switch (msg.what) {
				case 1: 
					myFlowerReceivedLoadingView.setVisibility(View.GONE);
					//System.out.println("-------    "+receivedFlowers.size());
					receivedFlowerAdapter.notifyDataSetChanged();
					break;
				case 2: 
					
					break;
				}
			}
		};
	}

	/**
	 * 初始化ViewPager
	 */
	private void initViewPager() {
		// width = ((ImageView)
		// findViewById(R.id.listen_course_line_iv)).getWidth();
		
		DisplayMetrics dm = new DisplayMetrics();
		this.getWindowManager().getDefaultDisplay().getMetrics(dm);
		offset = (dm.widthPixels / 3 - width) / 3;
		Matrix matrix = new Matrix();
		matrix.postTranslate(offset, 0);
		cursorIv.setImageMatrix(matrix);
		
		pages = new ArrayList<View>();
		pages.add(getReceivedFlowerView());
		pages.add(getSendedFlowerView());
		pages.add(getSendedFlowerView());
		//pages.add(getCoursingView());
		viewPager.setAdapter(pagerAdapterImpl);
		viewPager.setCurrentItem(0);
		viewPager.setOnPageChangeListener(new OnPageChangeListenerImpl());
	}
	
	/**
	 * 获取收到的花View
	 * @return
	 */
	private View getReceivedFlowerView(){
		View receivedFlowerView = LayoutInflater.from(this).inflate(R.layout.refreshable_listview, null);
		myFlowerReceivedLoadingView = receivedFlowerView.findViewById(R.id.loading_layout);
		requestFlowerData(0,receivedPageNum,pageSize);
		RefreshableListView lv = (RefreshableListView) receivedFlowerView
				.findViewById(R.id.refreshable_listview);
		
		receivedFlowerAdapter = new FlowerAdapter(0);
		lv.setAdapter(receivedFlowerAdapter);
		
		return receivedFlowerView;
	}
	
	
	/**
	 * 获取送出的花View
	 * @return
	 */
	private View getSendedFlowerView(){
		View sendedFlowerView = LayoutInflater.from(this).inflate(R.layout.refreshable_listview, null);
		myFlowerSendedLoadingView = sendedFlowerView.findViewById(R.id.loading_layout);
		//requestFlowerData(1,sendedPageNum,pageSize);
		RefreshableListView lv = (RefreshableListView) sendedFlowerView
				.findViewById(R.id.refreshable_listview);
		return sendedFlowerView;
	}
	
	
	/**
	 * 解析获取鲜花数据
	 * @param result
	 * @return
	 */
	private List<Flower> flowerJSONPasing(int  type,String result) {
		
		try {
			JSONObject object = new JSONObject(result);
			if(object.getInt("flag")==1){
				JSONArray list = object.getJSONArray("data");
				if(list!=null && list.length()>0){
					for(int i=0;i<list.length();i++){
						System.out.println("add Flower");
						Flower flower = new Flower();
						flower.setSource(list.getJSONObject(i).getInt("source"));
						flower.setFlowerCount(list.getJSONObject(i).getInt("flowerCount"));
						flower.setUserId(list.getJSONObject(i).getString("userId"));
						flower.setReceiverId(list.getJSONObject(i).getInt("receiverId"));
						flower.setFlowerRelationId(list.getJSONObject(i).getInt("flowerRelationId"));
						flower.setSendDate(list.getJSONObject(i).getString("sendData"));
						if(type==0){
							receivedFlowers.add(flower);
						}else{//这里肯定是type==1
							sendedFlowers.add(flower);
						}
					}
				}
			}
			
			
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		if(type==0){
			return receivedFlowers;
		}else{
			return sendedFlowers;
		}
	}
	
	
	/**
	 * 获取鲜花数据 
	 * @param type 0:收到的记录  1:送出的记录
	 */
	private void requestFlowerData(final int type,int pageno,int pageSize) {
		// TODO Auto-generated method stub
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("userid", App.getUserId());
		params.put("type", type);
		params.put("pageno", pageno);
		params.put("pagesize", pageSize);
		
		cacheHttp.asyncRequestWithCache(Constants.LEIWEIBO_HOST + "flower/history.do", params,
				new RequestWithCacheCallBack<List<Flower>>() {

					@Override
					public List<Flower> onPreRequestCache(String cache)  throws Exception{
						System.out.println("cache : "+cache);
						return flowerJSONPasing(type,cache);
					}

					@Override
					public void onPreRequestSuccess(List<Flower> data) {
						handler.sendEmptyMessage(1);
					}

					@Override
					public List<Flower> onReqestSuccess(String respones) throws Exception {
						System.out.println("respones : "+respones);
						return flowerJSONPasing(type,respones);
					}

					@Override
					public void success(List<Flower> cacheRespones,
							List<Flower> newRespones) {
						handler.sendEmptyMessage(1);
					}

					@Override
					public void exception(Exception exception) {
						System.out.println("exception : "+exception.getMessage());
					}
				});
				
	}


	/**
	 * ViewPager页面切换监听
	 */
	private class OnPageChangeListenerImpl implements OnPageChangeListener {

		private int mWidth = offset * 3 + width;

		@Override
		public void onPageScrollStateChanged(int arg0) {

		}

		@Override
		public void onPageScrolled(int arg0, float arg1, int arg2) {
			
		}

		@Override
		public void onPageSelected(int position) {
			if (position == 0) {
				myFlowerReceived.setTextColor(getResources().getColor(R.color.title_green));
				myFlowerSended.setTextColor(getResources().getColor(R.color.black));
				myFlowerCanSend.setTextColor(getResources().getColor(R.color.black));
			} else if (position == 1) {
				myFlowerReceived.setTextColor(getResources().getColor(R.color.black));
				myFlowerSended.setTextColor(getResources().getColor(R.color.title_green));
				myFlowerCanSend.setTextColor(getResources().getColor(R.color.black));
			}else if (position == 2) {
				myFlowerReceived.setTextColor(getResources().getColor(R.color.black));
				myFlowerSended.setTextColor(getResources().getColor(R.color.black));
				myFlowerCanSend.setTextColor(getResources().getColor(R.color.title_green));
			}
			Animation animation = new TranslateAnimation(mWidth * currentIndex, mWidth * position,
					0, 0);
			currentIndex = position;
			animation.setFillAfter(true);
			animation.setDuration(300);
			cursorIv.startAnimation(animation);
		}

	}
	
	/**
	 * ViewPager适配器
	 */
	PagerAdapter pagerAdapterImpl = new PagerAdapter() {

		@Override
		public int getCount() {
			return pages.size();
		}

		@Override
		public boolean isViewFromObject(View view, Object object) {
			return view == object;
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			container.removeView(pages.get(position));
		}

		@Override
		public Object instantiateItem(ViewGroup container, int position) {
			container.addView(pages.get(position), 0);
			return pages.get(position);
		}
	};
	
	
	
	private class FlowerAdapter extends BaseAdapter {

		private int  type;
		
		public FlowerAdapter(int type) {
			super();
			// TODO Auto-generated constructor stub
			this.type = type;
		}

		@Override
		public int getCount() {
			
			if(type==0){
				return receivedFlowers.size();
			}else{
				return sendedFlowers.size();
			}
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
		public View getView(final int position, View convertView, ViewGroup parent) {
		    ViewHolder viewHolder = null;
			if (convertView == null) {
				viewHolder = new ViewHolder();
				convertView = LayoutInflater.from(MyFlowerActivity.this).inflate(
						R.layout.flower_item, null);
				
				viewHolder.userImage = (ImageView)convertView.findViewById(R.id.userImage);
				viewHolder.userName = (TextView)convertView.findViewById(R.id.userName);
				viewHolder.flowerCount = (TextView)convertView.findViewById(R.id.flowerCount);
				
				convertView.setTag(viewHolder);
			} else {
				viewHolder = (ViewHolder) convertView.getTag();
			}
			
			return convertView;
		}
		
		
		final class ViewHolder {
			private ImageView userImage;
			private TextView userName;
			private TextView flowerCount;
		}
		
	}
	
	@Override
	public void initData() {
		// TODO Auto-generated method stub

	}

	@Override
	public void addListener() {
		// TODO Auto-generated method stub
		goBack.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		if(v == goBack){
			finish();
		}
	}

}
