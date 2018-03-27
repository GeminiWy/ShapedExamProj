package com.nd.shapedexamproj.view;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nd.shapedexamproj.R;

/**
 * 
 * @ClassName: CommonDialog
 * @Title:
 * @Description:公共弹窗提示框
 * @Author:XueWenJian
 * @Since:2014年5月28日11:52:00
 * @Version:1.0
 */
public class CommonDialog extends Dialog{

	private Context mContext;
	
	private LinearLayout mHeadLL;
	private RelativeLayout mTopRL;
	private TextView mTitleTV;
	private TextView mTipTV;
	private TextView mContentTV;
	private Button mPositiveBtn;
	private Button mNegativeBtn;
	
	public CommonDialog(Context context) {
		super(context, R.style.CommonDialog);
		
		mContext = context;
		this.setContentView(R.layout.common_dialog_layout);
		
		initView();
		
	}

	private void initView()
	{
		mHeadLL = (LinearLayout)findViewById(R.id.common_dialog_head_LL);
		mTopRL = (RelativeLayout)findViewById(R.id.common_dialog_top_layout);
		mTitleTV = (TextView)findViewById(R.id.common_dialog_head_tv);
		mContentTV = (TextView)findViewById(R.id.common_dialog_tv_);
		mTipTV = (TextView)findViewById(R.id.common_dialog_content);
		mPositiveBtn = (Button)findViewById(R.id.negative_button);
		mNegativeBtn = (Button)findViewById(R.id.positive_button);
		
		mTopRL.setVisibility(View.GONE);
		mTipTV.setVisibility(View.INVISIBLE);

	}
	
	public void setTitleTextView(String text, int visiable)
	{
		mTitleTV.setText(text);
		mTitleTV.setVisibility(visiable);
		mHeadLL.setVisibility(visiable);
	}
	
	public void setTitleTextView(int textid, int visiable)
	{
		mTitleTV.setText(textid);
		mTitleTV.setVisibility(visiable);
	}
	
	public void setMessageTextView(String text, int visiable)
	{
		mContentTV.setText(text);
		mContentTV.setVisibility(visiable);
	}
	
	public void setMessageTextView(int textid, int visiable)
	{
		mContentTV.setText(textid);
		mContentTV.setVisibility(visiable);
	}
	
	public void setTipTextView(String text, int visiable)
	{
		mTipTV.setText(text);
		mTipTV.setVisibility(visiable);
	}
	
	public void setTipTextView(int textid, int visiable)
	{
		mTipTV.setText(textid);
		mTipTV.setVisibility(visiable);
	}
	
	public void setPositiveButton(String text, View.OnClickListener clickListener)
	{
		mPositiveBtn.setText(text);
		mPositiveBtn.setOnClickListener( clickListener);
	}
	
	public void setNegativeButton(String text, View.OnClickListener clickListener)
	{
		mNegativeBtn.setText(text);
		mNegativeBtn.setOnClickListener( clickListener);
	}
	
	public void setPositiveButton(int textId, View.OnClickListener clickListener)
	{
		mPositiveBtn.setText(textId);
		mPositiveBtn.setOnClickListener( clickListener);
	}
	
	public void setNegativeButton(int textId, View.OnClickListener clickListener)
	{
		mNegativeBtn.setText(textId);
		mNegativeBtn.setOnClickListener( clickListener);
	}
	
}
