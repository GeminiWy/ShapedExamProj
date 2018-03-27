package com.nd.shapedexamproj.util;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.nd.shapedexamproj.App;
import com.nd.shapedexamproj.R;
import com.nd.shapedexamproj.view.numberpicker.NumericWheelAdapter;
import com.nd.shapedexamproj.view.numberpicker.OnWheelScrollListener;
import com.nd.shapedexamproj.view.numberpicker.WheelView;
import com.tming.common.net.TmingHttp;
import com.tming.common.net.TmingHttp.RequestCallback;
import com.tming.common.util.Log;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
/**
 * 送花对话框
 * @author zll
 * create in 2014-4-8
 */
public class FlowerDialog {
	
	
	private Context context ;
	
	private Button send_9_flower_btn ,send_99_flower_btn,send_all_flower_btn;
	
	private int rest_flowers ;	//剩余的鲜花数
	
	private int light_black ;
	private Drawable send_flower_btn_disabled = null;
	
	
	public FlowerDialog(Context context ){
		this.context = context ;
		light_black = context.getResources().getColor(R.color.light_black);
		 send_flower_btn_disabled = context.getResources().getDrawable(R.drawable.send_flower_btn_disabled);
	}
	
	/**
	 * 打开送鲜花的对话框
	 */
	int current_num ;
	Dialog dialog = null;
	public void showSendFlowerDialog(final int rest_flower_num){
		
		
		View send_flower_view = LayoutInflater.from(App.getAppContext()).inflate(R.layout.send_flower_view, null);
		final WheelView wheelView = (WheelView) send_flower_view.findViewById(R.id.send_flower_numpicker);
		send_9_flower_btn = (Button) send_flower_view.findViewById(R.id.send_9_flower_btn);
		if(rest_flower_num < 9){
			send_9_flower_btn.setClickable(false);
			send_9_flower_btn.setBackgroundDrawable(send_flower_btn_disabled);
			send_9_flower_btn.setTextColor(light_black);
		}
		send_99_flower_btn = (Button) send_flower_view.findViewById(R.id.send_99_flower_btn);
		if(rest_flower_num < 99){
			send_99_flower_btn.setClickable(false);
			send_99_flower_btn.setBackgroundDrawable(send_flower_btn_disabled);
			send_99_flower_btn.setTextColor(light_black);
		}
		
		send_all_flower_btn = (Button) send_flower_view.findViewById(R.id.send_all_flower_btn);
		if(rest_flower_num == 0){
			send_all_flower_btn.setClickable(false);
			send_all_flower_btn.setBackgroundDrawable(send_flower_btn_disabled);
			send_all_flower_btn.setTextColor(light_black);
		}
		TextView send_flower_rest_tv = (TextView) send_flower_view.findViewById(R.id.send_flower_rest_tv);
		send_flower_rest_tv.setText("剩余" + rest_flower_num + "朵花");
		Button negative_button = (Button) send_flower_view.findViewById(R.id.negative_button);
		Button positive_button = (Button) send_flower_view.findViewById(R.id.positive_button);
		
		wheelView.setAdapter(new NumericWheelAdapter(0, rest_flower_num, "%02d"));
		/*wheelView.setLabel("mins");*/  	//设置选中项上的文字
		wheelView.setCyclic(true);
		/*wheelView.setCurrentItem(1);*/
		
		OnWheelScrollListener scrollListener = new OnWheelScrollListener() {

			@Override
			public void onScrollingStarted(WheelView wheel) {
				
			}

			@Override
			public void onScrollingFinished(WheelView wheel) {
				current_num = wheelView.getCurrentItem();
				Log.e("花", current_num+"朵");
			}
			
		};
		wheelView.addScrollingListener(scrollListener);
		
		Builder builder = new Builder(context).setView(send_flower_view);
		send_9_flower_btn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(rest_flower_num >= 9){
					current_num = 9;
					wheelView.setCurrentItem(current_num,true);
				}
			}
		});
		send_99_flower_btn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(rest_flower_num >= 99){
					current_num = 99;
					wheelView.setCurrentItem(current_num,true);
				}
			}
		});
		send_all_flower_btn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				current_num = rest_flower_num ;
				wheelView.setCurrentItem(current_num,true);
			}
		});
		negative_button.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(dialog != null){
					dialog .dismiss();
				}
			}
		});
		positive_button.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				//送花
				if(current_num != 0){
					sendFlower(current_num,dialog);
				} else {
					Toast.makeText(context, "您的花已经送完了", Toast.LENGTH_SHORT).show();
				}
			}
		});
		
		dialog = builder.show();
	}
	
	/**
	 * 送花
	 */
	private void sendFlower(int flowercount,final Dialog dialog){
		String url = Constants.SEND_FLOWER;
		Map<String,Object> map = new HashMap<String,Object>();
		map.put("userid", App.getUserId());
		map.put("receiverid", 22);		//TODO 用假数据
		map.put("source", 0);			//直接送：0，动态里送：1
		map.put("flowercount", flowercount);
		
		TmingHttp.asyncRequest(url, map, new RequestCallback<String>() {

			@Override
			public String onReqestSuccess(String respones) throws Exception {
				JSONObject jobj = new JSONObject();
				return jobj.getString("flag");
			}

			@Override
			public void success(String respones) {
				if(respones.equals("1")){
					Toast.makeText(context, "送花成功", Toast.LENGTH_SHORT).show();
				} else {
					Toast.makeText(context, "参数错误", Toast.LENGTH_SHORT).show();
				}
				if(dialog != null){
					dialog.dismiss();
				}
			}

			@Override
			public void exception(Exception exception) {
				Toast.makeText(context, "网络异常", Toast.LENGTH_SHORT).show();
			}
		});
	}
	
	
}
