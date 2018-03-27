package com.nd.shapedexamproj.view.my;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import com.nd.shapedexamproj.App;
import com.nd.shapedexamproj.R;
import com.nd.shapedexamproj.model.my.PaymentInfo;
import com.tming.common.view.RefreshableListView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.LinkedList;
import java.util.List;
/**
 * 我的缴费列表
 * @author zll
 * create in 2014-4-24
 */
public class MyPaymentView extends RelativeLayout{
	
	private Context context ;
	/**
	 * 状态：1=待支付，2=已缴费,3=已退款
	 */
	private int status ;	
	private int	page = 1;	//当前页
	private int num = 20;	//每页总数
	
	private View view ;
	private RefreshableListView payment_list;
	private LinearLayout loading_layout;
	private RelativeLayout error_layout;
	private Button error_btn ;
	private List<PaymentInfo> list ;
	private PaymentAdapter adapter;
	
	
	public MyPaymentView(Context context, int status) {
		super(context);
		this.context = context;
		this.status = status;
		initComponent();
	}
	
	private void initComponent(){
		view = LayoutInflater.from(context).inflate(R.layout.my_payment_view, this);
		payment_list = (RefreshableListView) view.findViewById(R.id.refreshable_listview);
		loading_layout = (LinearLayout) view.findViewById(R.id.loading_layout);
		error_layout = (RelativeLayout) view.findViewById(R.id.error_layout);
		error_btn = (Button) view.findViewById(R.id.error_btn);
		
		list = new LinkedList<PaymentInfo>();
		adapter = new PaymentAdapter();
		payment_list.setAdapter(adapter);
	}
	
	/**
	 * 解析数据
	 */
	private void jsonParsing(String result){
		try {
			JSONObject jobj = new JSONObject(result);
			int code = jobj.getInt("code");
			if (code != 1) {
				App.dealWithFlag(code);
				return ;
			}
			JSONArray listArray = jobj.getJSONObject("data").getJSONArray("list");
			for (int i = 0; i < listArray.length(); i++) {
				JSONObject listObject = listArray.getJSONObject(i);
				PaymentInfo info = new PaymentInfo();
				info.pay_type = listObject.getString("pay_type");
				info.pay_info = listObject.getString("pay_info");
				info.add_time = listObject.getString("add_time");
				info.pay_amount = listObject.getString("pay_amount");
				info.order_id = listObject.getString("order_id");
				info.pay_time = listObject.getString("pay_time");
				
				list.add(info);
			}
			
			
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
	}
	
	private class ViewHolder {
		private Button mypayment_item_icon;
		private TextView mypayment_item_title;
		private TextView mypayment_time;
		//----------------------第二行------------------------
		private LinearLayout mypayment_money_ll_right;
		private TextView mypayment_money_tv_right;
		//----------------------第三行----------------------
		private LinearLayout mypayment_money_ll;
		private TextView mypayment_money_tv;
		//----------------------在线付款--------------------------
		private Button mypayment_pay_btn;
		
	}
	
	/**
	 * 缴费列表适配器
	 * @author zll
	 * create in 2014-4-24
	 */
	private class PaymentAdapter extends BaseAdapter{

		@Override
		public int getCount() {
			return list.size();
		}

		@Override
		public Object getItem(int position) {
			return list.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder ;
			if(convertView == null){
				holder = new ViewHolder();
				convertView = LayoutInflater.from(context).inflate(R.layout.my_payment_list_item, null);
				holder.mypayment_item_icon = (Button) convertView.findViewById(R.id.mypayment_item_icon);
				holder.mypayment_item_title = (TextView) convertView.findViewById(R.id.mypayment_item_title);
				holder.mypayment_time = (TextView) convertView.findViewById(R.id.mypayment_time);
				
				holder.mypayment_money_ll_right = (LinearLayout) convertView.findViewById(R.id.mypayment_money_ll_right);
				holder.mypayment_money_tv_right = (TextView) convertView.findViewById(R.id.mypayment_money_tv_right);
				
				holder.mypayment_money_ll = (LinearLayout) convertView.findViewById(R.id.mypayment_money_ll);
				holder.mypayment_money_tv = (TextView) convertView.findViewById(R.id.mypayment_money_tv);
				
				holder.mypayment_pay_btn = (Button) convertView.findViewById(R.id.mypayment_pay_btn);
				
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			
			if(status == 1){		//待支付
				holder.mypayment_money_ll_right.setVisibility(View.GONE);
				
				
			} else if (status == 2) {		//已缴费
				holder.mypayment_pay_btn.setVisibility(View.GONE);
				holder.mypayment_money_ll.setVisibility(View.GONE);
				
				
				
				
			} else if (status == 3) {		//退款
				holder.mypayment_pay_btn.setVisibility(View.GONE);
				
				
			}
			
			
			return convertView;
		}
		
	}
	
}
