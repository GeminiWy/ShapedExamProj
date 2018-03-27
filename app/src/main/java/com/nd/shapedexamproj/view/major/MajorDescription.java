package com.nd.shapedexamproj.view.major;

import android.content.Context;
import android.text.Html;
import android.text.Spanned;
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
public class MajorDescription extends RelativeLayout{
	
	private Context context;
	private TextView major_introduction;
	private View view;
	private String introduction;
	
	public MajorDescription(Context context,String introduction) {
		super(context);
		this.context = context;
		this.introduction = introduction;
		initComponent();
	}

	private void initComponent(){
		view = LayoutInflater.from(context).inflate(R.layout.major_description, this);
		major_introduction = (TextView) view.findViewById(R.id.major_introduction);
		//解析HTML
		introduction = introduction.replace("\n", "<p>");
		Spanned introductionSpanned = Html.fromHtml(introduction);
		major_introduction.setText(introductionSpanned);
	}
}
