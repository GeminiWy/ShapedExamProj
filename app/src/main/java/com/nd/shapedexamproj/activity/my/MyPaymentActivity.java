package com.nd.shapedexamproj.activity.my;

import android.graphics.Matrix;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.Html;
import android.util.DisplayMetrics;
import android.util.Log;
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
import com.nd.shapedexamproj.model.my.PaymentInfo;
import com.nd.shapedexamproj.util.Constants;
import com.nd.shapedexamproj.util.Utils;
import com.tming.common.cache.net.TmingCacheHttp;
import com.tming.common.cache.net.TmingCacheHttp.RequestWithCacheCallBack;
import com.tming.common.view.RefreshableListView;
import com.tming.common.view.RefreshableListView.OnRefreshListener;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;
/**
 * 我的缴费页
 * @author zll  lcj
 * create in 2014-4-24
 */
public class MyPaymentActivity extends BaseActivity implements OnClickListener{
	
	private static final String TAG = "MyPaymentActivity";
	
	private List<View> pages;// Tab页面列表
	private int offset = 0;// 动画图片偏移量
	private int currentIndex = 0;// 当前页卡编号
	private int width = 0;// 动画图片宽度
	private int notPayPageNum = 1,arleadyPayPageNum = 1,chargeBackPageNum = 1,pageSize = 10;
	
	private List<PaymentInfo> notPayInfos,alreadyPayInfos,chargeBackInfos;
	
//	private Handler handler;
	private TmingCacheHttp cacheHttp;
	
	private ViewPager viewPager;
	private TextView currLocation,notPayTv,alrealyPayTv,chargebackTv;
	private ImageView goBack,pageIndicator;
	private View  notPayReceivedLoadingView,alreadyPayLoadingView,chargeBackLoadingView;
	private MyPaymentAdapter notPayAdapter,arleadyPayAdapter,chargeBackAdapter;
	private RefreshableListView notPayLV, arleadyPayLV, chargeBackLV;
	
	@Override
	public int initResource() {
		return R.layout.my_payment_activity;
	}

	@Override
	public void initComponent() {
		goBack = (ImageView) findViewById(R.id.commonheader_left_iv);
		goBack.setOnClickListener(this);
		
		findViewById(R.id.commonheader_right_btn).setVisibility(View.GONE);
		pageIndicator = (ImageView)findViewById(R.id.pageIndicator);
		viewPager = (ViewPager) findViewById(R.id.mypayment_list_vPager);
		
		notPayTv = (TextView)findViewById(R.id.not_pay_tv);
		alrealyPayTv = (TextView)findViewById(R.id.alrealy_pay_tv);
		chargebackTv = (TextView)findViewById(R.id.chargeback_tv);
		currLocation = (TextView) findViewById(R.id.commonheader_title_tv);
		currLocation.setText(getResources().getString(R.string.my_payment));
		cacheHttp = TmingCacheHttp.getInstance(this);
//		initHandler();
		initViewPager();
	}

	
//	private void initHandler() {
//		handler = new Handler() {
//			@Override
//			public void handleMessage(Message msg) {
//				super.handleMessage(msg);
//				Log.i(TAG, "msg.what:"+msg.what);
//				switch (msg.what) {
//				case -1:
//					String result = (String)msg.obj;
//					int status = msg.arg1;
//					parsePaymentDataJsonResult(status, result);
//					this.sendEmptyMessage(status);//转发给相应的缴费类型，就是底下的 1 2 3 case分支
//					break;
//				case 1: 
//					notPayReceivedLoadingView.setVisibility(View.GONE);
//					notPayAdapter.notifyDataSetChanged();
//					lv.onRefreshComplete();
//					break;
//				case 2: 
//					alreadyPayLoadingView.setVisibility(View.GONE);
//					arleadyPayAdapter.notifyDataSetChanged();
//					lv.onRefreshComplete();
//					break;
//					
//				case 3: 
//					chargeBackLoadingView.setVisibility(View.GONE);
//					chargeBackAdapter.notifyDataSetChanged();
//					lv.onRefreshComplete();
//					break;
//				}
//			}
//		};
//	}
	
	private void loadData(int status, List<PaymentInfo> data){
		if (status == 1){ 
			notPayReceivedLoadingView.setVisibility(View.GONE);
			for(int i = 0;i < data.size();i ++){
				PaymentInfo paymentInfo = data.get(i);
				if(!notPayInfos.contains(paymentInfo)){
					notPayInfos.add(paymentInfo);
				}
			}
			
			notPayAdapter.notifyDataSetChanged();
			notPayLV.onRefreshComplete();
			
		} else if(status == 2){
			alreadyPayLoadingView.setVisibility(View.GONE);
			for(int i = 0;i < data.size();i ++){
				PaymentInfo paymentInfo = data.get(i);
				if(!alreadyPayInfos.contains(paymentInfo)){
					alreadyPayInfos.add(paymentInfo);
				}
			}
			
			arleadyPayAdapter.notifyDataSetChanged();
			arleadyPayLV.onRefreshComplete();
			
		} else if(status == 3){
			chargeBackLoadingView.setVisibility(View.GONE);
			for(int i = 0;i < data.size();i ++){
				PaymentInfo paymentInfo = data.get(i);
				if(!chargeBackInfos.contains(paymentInfo)){
					chargeBackInfos.add(paymentInfo);
				}
			}
			
			chargeBackAdapter.notifyDataSetChanged();
			chargeBackLV.onRefreshComplete();
		}
	}
	
	/**
	 * 初始化ViewPager
	 */
	private void initViewPager() {
		
		DisplayMetrics dm = new DisplayMetrics();
		this.getWindowManager().getDefaultDisplay().getMetrics(dm);
		offset = (dm.widthPixels / 3 - width) / 3;
		Matrix matrix = new Matrix();
		matrix.postTranslate(offset, 0);
		pageIndicator.setImageMatrix(matrix);
		
		notPayInfos = new ArrayList<PaymentInfo>();
		alreadyPayInfos = new ArrayList<PaymentInfo>();
		chargeBackInfos = new ArrayList<PaymentInfo>();
		
		pages = new ArrayList<View>();
		
		pages.add(getNotPayView());
		pages.add(getAlreadyPayView());
		pages.add(getChargeBackView());
		viewPager.setAdapter(pagerAdapterImpl);
		viewPager.setCurrentItem(0);
		viewPager.setOnPageChangeListener(new OnPageChangeListenerImpl());
		
		notPayTv.setOnClickListener(this);
		alrealyPayTv.setOnClickListener(this);
		chargebackTv.setOnClickListener(this);
	}
	
	
	
	
	/**
	 * 获取缴费View
	 * @return
	 */
	private View getNotPayView(){
		View payView = LayoutInflater.from(this).inflate(R.layout.refreshable_listview, null);
		
		notPayLV = (RefreshableListView) payView
				.findViewById(R.id.refreshable_listview);
		notPayLV.setDividerHeight(Utils.dip2px(this, 5));
		
		notPayReceivedLoadingView = payView.findViewById(R.id.loading_layout);
		requestPaymentData(1,notPayPageNum);
		notPayAdapter = new MyPaymentAdapter(1);
		notPayLV.setAdapter(notPayAdapter);
			
		return payView;
	}
	
	private View getAlreadyPayView(){
		View payView = LayoutInflater.from(this).inflate(R.layout.refreshable_listview, null);
		
		arleadyPayLV = (RefreshableListView) payView
				.findViewById(R.id.refreshable_listview);
		arleadyPayLV.setDividerHeight(Utils.dip2px(this, 5));
		
		alreadyPayLoadingView = payView.findViewById(R.id.loading_layout);
		requestPaymentData(2,arleadyPayPageNum);
		arleadyPayAdapter = new MyPaymentAdapter(2);
		arleadyPayLV.setAdapter(arleadyPayAdapter);
		return payView;
	}
	
	private View getChargeBackView(){
		View payView = LayoutInflater.from(this).inflate(R.layout.refreshable_listview, null);
		
		chargeBackLV = (RefreshableListView) payView
				.findViewById(R.id.refreshable_listview);
		chargeBackLV.setDividerHeight(Utils.dip2px(this, 5));
		
		chargeBackLoadingView = payView.findViewById(R.id.loading_layout);
		
		requestPaymentData(3,chargeBackPageNum);
		chargeBackAdapter = new MyPaymentAdapter(3);
		chargeBackLV.setAdapter(chargeBackAdapter);
		return payView;
	}
	
	/**
	 * 异步请求缴费记录
	 * @param status 1=待支付，2=已缴费,3=已退款
	 * @param pageNum
	 */
	private void requestPaymentData(final int status,int pageNum) {
		// TODO Auto-generated method stub
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("userid", App.getUserId());
		params.put("status", status);
		params.put("pageNum", pageNum);
		params.put("pagesize", pageSize);
		/*
		TmingHttp.asyncRequest(Constants.HOST + "payment/payList.html", params, new RequestCallback<List<PaymentInfo>>() {

			@Override
			public List<PaymentInfo> onReqestSuccess(String respones)
					throws Exception {
				// TODO Auto-generated method stub
				System.out.println("respones : "+respones);
				return parsePaymentDataJsonResult(status, respones);
			}

			@Override
			public void success(List<PaymentInfo> respones) {
				// TODO Auto-generated method stub
				handler.sendEmptyMessage(status);
			}

			@Override
			public void exception(Exception exception) {
				// TODO Auto-generated method stub
				
				System.out.println("exception : "+exception.getMessage());
				
				//String result ="{\"flag\":1,\"res\":{\"code\":1,\"data\":{\"total\":2,\"list\":[{\"pay_type\":\"1\",\"pay_info\":\"C++选课缴费\",\"add_time\":1400222768,\"pay_amount\":78,\"order_id\":\"516ceb354692d1ea\"},{\"pay_type\":\"2\",\"pay_info\":\"C++\",\"add_time\":1400222768,\"pay_amount\":78,\"order_id\":\"516ceb354692d1ea\"},{\"pay_type\":\"3\",\"pay_info\":\"JAVA选课缴费\",\"add_time\":1400222768,\"pay_amount\":78,\"order_id\":\"516ceb354692d1ea\"}]}}}";
				//parsePaymentDataJsonResult(status, result);
				//handler.sendEmptyMessage(status);
				
				
			}
		});
		*/
		/*
		 * 测试发现使用cacheHttp会同时返回缓存及服务器返回数据，且数据不一样,故这里不调用cacheHttp
		 * //{"flag":1,"res":{"code":1,"data":{"total":2,"list":[{"pay_type":"1","pay_info":"C++选课缴费","add_time":1400222768,"pay_amount":78,"order_id":"516ceb354692d1ea"},{"pay_type":"1","pay_info":"C++","add_time":1400222768,"pay_amount":78,"order_id":"516ceb354692d1ea"}]}}}
           //{"flag":1,"res":{"code":1,"data":{"total":2,"list":[{"pay_type":"1","pay_info":"C++选课缴费","add_time":1400222888,"pay_amount":78,"order_id":"516ceb354692d1ea"},{"pay_type":"1","pay_info":"C++","add_time":1400222888,"pay_amount":78,"order_id":"516ceb354692d1ea"}]}}}
		*/			
		cacheHttp.asyncRequestWithCache(Constants.HOST + "payment/payList.html", params,
				new RequestWithCacheCallBack<List<PaymentInfo>>() {

					@Override
					public List<PaymentInfo> onPreRequestCache(String cache)
							throws Exception {
						/*System.out.println("cache : "+cache);
						Message msg = new Message();
						msg.what = -1;
						msg.arg1 = status;
						msg.obj = cache;
						handler.handleMessage(msg);*/
						return parsePaymentDataJsonResult(status, cache);
					}

					@Override
					public void onPreRequestSuccess(List<PaymentInfo> data) {
						loadData(status, data);
					}
					
					@Override
					public List<PaymentInfo> onReqestSuccess(String respones)
							throws Exception {
						/*System.out.println("respones : "+respones);
						Message msg = new Message();
						msg.what = -1;
						msg.arg1 = status;
						msg.obj = respones;
						handler.handleMessage(msg);*/
						return parsePaymentDataJsonResult(status, respones);
					}

					@Override
					public void success(List<PaymentInfo> cacheRespones,
							List<PaymentInfo> newRespones) {
						
						if (status == 1) {
							notPayReceivedLoadingView.setVisibility(View.GONE);
							notPayAdapter.replaceItem(cacheRespones, newRespones);
							notPayAdapter.notifyDataSetChanged();
							notPayLV.onRefreshComplete();
							
						} else if (status == 2) {
							alreadyPayLoadingView.setVisibility(View.GONE);
							arleadyPayAdapter.replaceItem(cacheRespones, newRespones);
							arleadyPayAdapter.notifyDataSetChanged();
							arleadyPayLV.onRefreshComplete();
							
						} else if (status == 3) {
							chargeBackLoadingView.setVisibility(View.GONE);
							chargeBackAdapter.replaceItem(cacheRespones, newRespones);
							chargeBackAdapter.notifyDataSetChanged();
							chargeBackLV.onRefreshComplete();
							
						}
					}

					@Override
					public void exception(Exception exception) {
						System.out.println("exception : "+exception.getMessage());
					}
			
		});
	}

	/**
	 * 解析缴费数据
	 * @param status 1=待支付，2=已缴费,3=已退款
	 * @param result 请求返回的json数据
	 * @return
	 */
	private List<PaymentInfo> parsePaymentDataJsonResult(int status, String result) {
		List<PaymentInfo> notPayInfos = new LinkedList<PaymentInfo>();
		List<PaymentInfo> alreadyPayInfos = new LinkedList<PaymentInfo>();
		List<PaymentInfo> chargeBackInfos = new LinkedList<PaymentInfo>();
		
		Log.i(TAG, "result:"+result);
		try {
			JSONObject object = new JSONObject(result);
			if(object.getInt("flag")==1){
				JSONArray list = object.getJSONObject("res").getJSONObject("data").getJSONArray("list");
				System.out.println("parsePaymentDataJsonResult  "+status+" ,  "+list.length());
				if(list!=null && list.length()>0){
					for(int i=0;i<list.length();i++){
						
						PaymentInfo paymentInfo = new PaymentInfo();
						paymentInfo.pay_type = list.getJSONObject(i).getString("pay_type");
						paymentInfo.pay_info = list.getJSONObject(i).getString("pay_info");
						paymentInfo.add_time = list.getJSONObject(i).getString("add_time");
						paymentInfo.pay_amount = list.getJSONObject(i).getString("pay_amount");
						paymentInfo.order_id = list.getJSONObject(i).getString("order_id");
						//paymentInfo.pay_time = list.getJSONObject(i).getString("pay_time");//返回数据无 pay_time
						if(status == 1){
							notPayInfos.add(paymentInfo);
						}else if(status == 2){
							alreadyPayInfos.add(paymentInfo);
						}else if(status == 3){
							chargeBackInfos.add(paymentInfo);
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		if(status == 1){
			return notPayInfos;
		}else if(status == 2){
			return alreadyPayInfos;
		}else if(status == 3){
			return chargeBackInfos;
		}else{
			return null;
		}
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
				notPayTv.setTextColor(getResources().getColor(R.color.title_green));
				alrealyPayTv.setTextColor(getResources().getColor(R.color.black));
				chargebackTv.setTextColor(getResources().getColor(R.color.black));
			} else if (position == 1) {
				notPayTv.setTextColor(getResources().getColor(R.color.black));
				alrealyPayTv.setTextColor(getResources().getColor(R.color.title_green));
				chargebackTv.setTextColor(getResources().getColor(R.color.black));
			}else if (position == 2) {
				notPayTv.setTextColor(getResources().getColor(R.color.black));
				alrealyPayTv.setTextColor(getResources().getColor(R.color.black));
				chargebackTv.setTextColor(getResources().getColor(R.color.title_green));
			}
			Animation animation = new TranslateAnimation(mWidth * currentIndex, mWidth * position,
					0, 0);
			currentIndex = position;
			animation.setFillAfter(true);
			animation.setDuration(300);
			pageIndicator.startAnimation(animation);
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
	
	
	
	private class MyPaymentAdapter extends BaseAdapter {

		private int  status;
		
		public MyPaymentAdapter(int status) {
			super();
			this.status = status;
		}

		@Override
		public int getCount() {
			
			if(status==1){
				return notPayInfos.size();
			}else if(status == 2){
				return alreadyPayInfos.size();
			}else if(status == 3){
				return chargeBackInfos.size();
			}else{
				return 0;
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
		public View getView(final int position, View convertView,
				ViewGroup parent) {
			// System.out.println(status+"  ||||  "+position);
			ViewHolder viewHolder = null;
			if (convertView == null) {
				viewHolder = new ViewHolder();
				convertView = LayoutInflater.from(MyPaymentActivity.this)
						.inflate(R.layout.my_payment_listview_item, null);

				// convertView.findViewById(R.id.mypayment_money_ll).setVisibility(View.INVISIBLE);
				viewHolder.payType = (TextView) convertView
						.findViewById(R.id.payType);
				viewHolder.payInfo = (TextView) convertView
						.findViewById(R.id.payInfo);
				viewHolder.payTime = (TextView) convertView
						.findViewById(R.id.payTime);
				viewHolder.notPayOrChargeBackAmount = (TextView) convertView
						.findViewById(R.id.notPayOrChargeBackAmount);
				viewHolder.onlinePayment = (TextView) convertView
						.findViewById(R.id.onlinePayment);
				viewHolder.recordAmount = (TextView) convertView
						.findViewById(R.id.recordAmount);
				convertView.setTag(viewHolder);
			} else {
				viewHolder = (ViewHolder) convertView.getTag();
			}

			if (status == 1) {

				if ("1".equals(notPayInfos.get(position).pay_type)) {
					viewHolder.payType.setBackgroundColor(getResources()
							.getColor(R.color.payment_green));
					viewHolder.payType.setText("建");

				} else if ("2".equals(notPayInfos.get(position).pay_type)) {
					viewHolder.payType.setBackgroundColor(getResources()
							.getColor(R.color.payment_blue));
					viewHolder.payType.setText("选");

				} else if ("3".equals(notPayInfos.get(position).pay_type)) {
					viewHolder.payType.setBackgroundColor(getResources()
							.getColor(R.color.payment_red));
					viewHolder.payType.setText("补");

				}
				viewHolder.onlinePayment.setVisibility(View.VISIBLE);

				viewHolder.onlinePayment
						.setOnClickListener(new OnClickListener() {

							@Override
							public void onClick(View v) {
								// TODO Auto-generated method stub
								System.out.println("在线付款");
							}
						});

				viewHolder.payInfo.setText(notPayInfos.get(position).pay_info);
				viewHolder.payTime.setText("创建于："
						+ notPayInfos.get(position).add_time);
				viewHolder.notPayOrChargeBackAmount.setVisibility(View.VISIBLE);
				viewHolder.notPayOrChargeBackAmount.setText(Html
						.fromHtml("<font color=#9d9d9d size=14sp>金额：</font>"
								+ "<font color=#f99c0a>"
								+ notPayInfos.get(position).pay_amount
								+ "</font>"
								+ "<font color=#9d9d9d size=14sp>元</font>"));
			} else if (status == 2) {
				if ("1".equals(alreadyPayInfos.get(position).pay_type)) {
					viewHolder.payType.setBackgroundColor(getResources()
							.getColor(R.color.payment_green));
					viewHolder.payType.setText("建");

				} else if ("2".equals(alreadyPayInfos.get(position).pay_type)) {
					viewHolder.payType.setText("选");
					viewHolder.payType.setBackgroundColor(getResources()
							.getColor(R.color.payment_green));

				} else if ("3".equals(alreadyPayInfos.get(position).pay_type)) {
					viewHolder.payType.setText("补");
					viewHolder.payType.setBackgroundColor(getResources()
							.getColor(R.color.payment_blue));
				}

				viewHolder.payInfo
						.setText(alreadyPayInfos.get(position).pay_info);
				// viewHolder.onlinePayment.setVisibility(View.GONE);
				viewHolder.payTime.setText("缴费日期："
						+ alreadyPayInfos.get(position).add_time);
				viewHolder.recordAmount.setVisibility(View.VISIBLE);
				// viewHolder.payRecordAmount.setText(arleadryPayInfos.get(position).pay_amount);
				viewHolder.recordAmount.setText(Html
						.fromHtml("<font color=#9d9d9d size=14sp>金额：</font>"
								+ "<font color=#f99c0a>"
								+ alreadyPayInfos.get(position).pay_amount
								+ "</font>"
								+ "<font color=#9d9d9d size=14sp>元</font>"));
				// viewHolder.notPayOrChargeBackAmount.setVisibility(View.GONE);
			}/**/else if (status == 3) {
				if ("1".equals(chargeBackInfos.get(position).pay_type)) {
					viewHolder.payType.setBackgroundColor(getResources()
							.getColor(R.color.payment_green));
					viewHolder.payType.setText("建");

				} else if ("2".equals(chargeBackInfos.get(position).pay_type)) {
					viewHolder.payType.setText("选");
					viewHolder.payType.setBackgroundColor(getResources()
							.getColor(R.color.payment_green));

				} else if ("3".equals(chargeBackInfos.get(position).pay_type)) {
					viewHolder.payType.setText("补");
					viewHolder.payType.setBackgroundColor(getResources()
							.getColor(R.color.payment_blue));
				}

				viewHolder.payInfo
						.setText(chargeBackInfos.get(position).pay_info);
				// viewHolder.onlinePayment.setVisibility(View.GONE);
				viewHolder.payTime.setText("退款日期："
						+ chargeBackInfos.get(position).add_time);
				viewHolder.recordAmount.setVisibility(View.VISIBLE);
				viewHolder.recordAmount.setText(Html
						.fromHtml("<font color=#9d9d9d size=14sp>金额：</font>"
								+ "<font color=#f99c0a>"
								+ chargeBackInfos.get(position).pay_amount
								+ "</font>"
								+ "<font color=#9d9d9d size=14sp>元</font>"));
				viewHolder.notPayOrChargeBackAmount.setVisibility(View.VISIBLE);
				viewHolder.notPayOrChargeBackAmount.setText(Html
						.fromHtml("<font color=#9d9d9d size=14sp>退款原因：</font>"
								+ "<font color=#f99c0a>本期未开课</font>"));
			}

			return convertView;
		}
		
		public void replaceItem(List<PaymentInfo> oldData, List<PaymentInfo> newData) {
			if (status == 1) {
				// 如果不存在旧数据
				if (oldData == null || oldData.size() == 0) {
					notPayInfos.addAll(newData);
					notifyDataSetChanged();
					return;
				}
				
				// 做替换
				int oldStartIndex = notPayInfos.indexOf(oldData.get(0));
				int oldEndIndex = notPayInfos.indexOf(oldData.get(oldData.size() - 1));
				if (oldStartIndex != -1 && oldEndIndex != -1) {
					for (int i = oldStartIndex; i <= oldEndIndex; i++) {
						notPayInfos.remove(oldStartIndex);
					}
					int insertStartIndex = oldStartIndex; 
					for (int i = 0; i < newData.size(); i++) {
						notPayInfos.add(insertStartIndex++, newData.get(i));
					}
				}
			} else if(status == 2) {
				// 如果不存在旧数据
				if (oldData == null || oldData.size() == 0) {
					alreadyPayInfos.addAll(newData);
					notifyDataSetChanged();
					return;
				}
				
				// 做替换
				int oldStartIndex = alreadyPayInfos.indexOf(oldData.get(0));
				int oldEndIndex = alreadyPayInfos.indexOf(oldData.get(oldData.size() - 1));
				if (oldStartIndex != -1 && oldEndIndex != -1) {
					for (int i = oldStartIndex; i <= oldEndIndex; i++) {
						alreadyPayInfos.remove(oldStartIndex);
					}
					int insertStartIndex = oldStartIndex; 
					for (int i = 0; i < newData.size(); i++) {
						alreadyPayInfos.add(insertStartIndex++, newData.get(i));
					}
				}
			} else if(status == 3) {
				// 如果不存在旧数据
				if (oldData == null || oldData.size() == 0) {
					chargeBackInfos.addAll(newData);
					notifyDataSetChanged();
					return;
				}
				
				// 做替换
				int oldStartIndex = chargeBackInfos.indexOf(oldData.get(0));
				int oldEndIndex = chargeBackInfos.indexOf(oldData.get(oldData.size() - 1));
				if (oldStartIndex != -1 && oldEndIndex != -1) {
					for (int i = oldStartIndex; i <= oldEndIndex; i++) {
						chargeBackInfos.remove(oldStartIndex);
					}
					int insertStartIndex = oldStartIndex; 
					for (int i = 0; i < newData.size(); i++) {
						chargeBackInfos.add(insertStartIndex++, newData.get(i));
					}
				}
			}
			super.notifyDataSetChanged();
		}
		
		
		final class ViewHolder {
			private TextView payType;
			private TextView payInfo;
			private TextView payTime;
			private TextView notPayOrChargeBackAmount;
			private TextView onlinePayment;
			private TextView recordAmount;
		}
		
	}
	
	
	@Override
	public void initData() {
		notPayLV.setonRefreshListener(new OnRefreshListener() {
			
			@Override
			public void onRefresh() {
				notPayPageNum = 1;
				requestPaymentData(1,notPayPageNum);
			}
			
			@Override
			public void onLoadMore() {
				++ notPayPageNum ;
				requestPaymentData(1,notPayPageNum);
			}
		});
		arleadyPayLV.setonRefreshListener(new OnRefreshListener() {
			
			@Override
			public void onRefresh() {
				arleadyPayPageNum = 1;
				requestPaymentData(2,arleadyPayPageNum);
			}
			
			@Override
			public void onLoadMore() {
				++ arleadyPayPageNum ;
				requestPaymentData(2,arleadyPayPageNum);
			}
		});
		chargeBackLV.setonRefreshListener(new OnRefreshListener() {
			
			@Override
			public void onRefresh() {
				chargeBackPageNum = 1;
				requestPaymentData(3,chargeBackPageNum);
			}
			
			@Override
			public void onLoadMore() {
				++ chargeBackPageNum ;
				requestPaymentData(3,chargeBackPageNum);
			}
		});
		
		
	}

	@Override
	public void addListener() {
		
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		if(v == goBack){
			finish();
		}else if(v == notPayTv){
			viewPager.setCurrentItem(0);
		}else if(v == alrealyPayTv){
			viewPager.setCurrentItem(1);
		}else if(v == chargebackTv){
			viewPager.setCurrentItem(2);
		}
	}

}
