package com.nd.shapedexamproj.view.major;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nd.shapedexamproj.R;

/**
 * 专业详情-专业介绍
 * @author wyl
 * create in 2014.03.18
 */
public class MajorChargeStandard extends RelativeLayout{
	
	private Context context;
	private TextView major_introduction;
	private View view;
	private String cost;
	
	public MajorChargeStandard(Context context,String cost) {
		super(context);
		this.context = context;
		this.cost = cost;
		initComponent();
	}

	private void initComponent(){
		view = LayoutInflater.from(context).inflate(R.layout.major_charge_standard, this);
		major_introduction = (TextView) view.findViewById(R.id.major_cost);
		major_introduction.setText(cost);
	}
}
