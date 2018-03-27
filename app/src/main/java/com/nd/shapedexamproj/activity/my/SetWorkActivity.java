package com.nd.shapedexamproj.activity.my;

import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.nd.shapedexamproj.R;
import com.nd.shapedexamproj.activity.BaseActivity;
import com.nd.shapedexamproj.util.IndustryUtil;
import com.nd.shapedexamproj.util.Utils;

/**
 * 
 * 职业信息
 * 
 * @author Linlg
 * 
 *         Create on 2014-4-3
 */
public class SetWorkActivity extends BaseActivity implements OnClickListener {
	private EditText workEt;
	private Button cleanEditBtn;
	private Button itBtn, gyBtn, syBtn, jrBtn, whBtn, ysBtn, ywBtn, flBtn, jyBtn, zfBtn, xsBtn, otherBtn;
	private int mSelectItem = 0;
	
	@Override
	public int initResource() {
		return R.layout.my_edit_industry;
	}

	@Override
	public void initComponent() {
		initView();
	}

	@Override
	public void initData() {
		
	}

	@Override
	public void addListener() {
		findViewById(R.id.myeditindustry_head_layout).findViewById(R.id.commonheader_left_iv)
		.setOnClickListener(this);
		((TextView) findViewById(R.id.myeditindustry_head_layout).findViewById(R.id.commonheader_title_tv)).setText("职业信息");
		findViewById(R.id.myeditindustry_head_layout).findViewById(R.id.commonheader_right_btn).setVisibility(View.GONE);
		findViewById(R.id.myeditindustry_it_layout).setOnClickListener(this);
		findViewById(R.id.myeditindustry_gy_layout).setOnClickListener(this);
		findViewById(R.id.myeditindustry_sy_layout).setOnClickListener(this);
		findViewById(R.id.myeditindustry_jr_layout).setOnClickListener(this);
		findViewById(R.id.myeditindustry_wh_layout).setOnClickListener(this);
		findViewById(R.id.myeditindustry_ys_layout).setOnClickListener(this);
		findViewById(R.id.myeditindustry_yw_layout).setOnClickListener(this);
		findViewById(R.id.myeditindustry_fl_layout).setOnClickListener(this);
		findViewById(R.id.myeditindustry_jy_layout).setOnClickListener(this);
		findViewById(R.id.myeditindustry_zf_layout).setOnClickListener(this);
		findViewById(R.id.myeditindustry_xs_layout).setOnClickListener(this);
		findViewById(R.id.myeditindustry_other_layout).setOnClickListener(this);

		cleanEditBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				workEt.setText("");
			}
		});
		
        String errContentMaxLength = getResources().getString(R.string.error_max_length);
        errContentMaxLength = String.format(errContentMaxLength, EditPersonalActivity.EDIT_MAX_LENGTH);
		
		Utils.addEditViewMaxLengthListener(this, workEt, EditPersonalActivity.EDIT_MAX_LENGTH, errContentMaxLength);
		
	}

	private void initView() {
		workEt = (EditText) findViewById(R.id.myeditindustry_et);
		workEt.setText(getIntent().getStringExtra("work"));
		cleanEditBtn = (Button) findViewById(R.id.myeditindustry_clean_btn);
		
		((TextView) findViewById(R.id.myeditindustry_it_layout).findViewById(
				R.id.myeditindustryitem_title_tv)).setText(IndustryUtil.INDUSTRY_IT);
		((TextView) findViewById(R.id.myeditindustry_gy_layout).findViewById(
				R.id.myeditindustryitem_title_tv)).setText(IndustryUtil.INDUSTRY_EMPLOYMENT);
		((TextView) findViewById(R.id.myeditindustry_sy_layout).findViewById(
				R.id.myeditindustryitem_title_tv)).setText(IndustryUtil.INDUSTRY_BUSINESS);
		((TextView) findViewById(R.id.myeditindustry_jr_layout).findViewById(
				R.id.myeditindustryitem_title_tv)).setText(IndustryUtil.INDUSTRY_FINANCE);
		((TextView) findViewById(R.id.myeditindustry_wh_layout).findViewById(
				R.id.myeditindustryitem_title_tv)).setText(IndustryUtil.INDUSTRY_CULTURE);
		((TextView) findViewById(R.id.myeditindustry_ys_layout).findViewById(
				R.id.myeditindustryitem_title_tv)).setText(IndustryUtil.INDUSTRY_ART);
		((TextView) findViewById(R.id.myeditindustry_yw_layout).findViewById(
				R.id.myeditindustryitem_title_tv)).setText(IndustryUtil.INDUSTRY_MEDICAL);
		((TextView) findViewById(R.id.myeditindustry_fl_layout).findViewById(
				R.id.myeditindustryitem_title_tv)).setText(IndustryUtil.INDUSTRY_LAW);
		((TextView) findViewById(R.id.myeditindustry_jy_layout).findViewById(
				R.id.myeditindustryitem_title_tv)).setText(IndustryUtil.INDUSTRY_EDUCATION);
		((TextView) findViewById(R.id.myeditindustry_zf_layout).findViewById(
				R.id.myeditindustryitem_title_tv)).setText(IndustryUtil.INDUSTRY_GOVERNMENT);
		((TextView) findViewById(R.id.myeditindustry_xs_layout).findViewById(
				R.id.myeditindustryitem_title_tv)).setText(IndustryUtil.INDUSTRY_STUDENT);
		((TextView) findViewById(R.id.myeditindustry_other_layout).findViewById(
				R.id.myeditindustryitem_title_tv)).setText(IndustryUtil.INDUSTRY_OTHERS);
		

		((TextView) findViewById(R.id.myeditindustry_it_layout).findViewById(
				R.id.myeditindustryitem_decription_tv)).setText(IndustryUtil.INDUSTRY_SUB_IT);
		((TextView) findViewById(R.id.myeditindustry_gy_layout).findViewById(
				R.id.myeditindustryitem_decription_tv)).setText(IndustryUtil.INDUSTRY_SUB_EMPLOYMENT);
		((TextView) findViewById(R.id.myeditindustry_sy_layout).findViewById(
				R.id.myeditindustryitem_decription_tv)).setText(IndustryUtil.INDUSTRY_SUB_BUSINESS);
		((TextView) findViewById(R.id.myeditindustry_jr_layout).findViewById(
				R.id.myeditindustryitem_decription_tv)).setText(IndustryUtil.INDUSTRY_SUB_FINANCE);
		((TextView) findViewById(R.id.myeditindustry_wh_layout).findViewById(
				R.id.myeditindustryitem_decription_tv)).setText(IndustryUtil.INDUSTRY_SUB_CULTURE);
		((TextView) findViewById(R.id.myeditindustry_ys_layout).findViewById(
				R.id.myeditindustryitem_decription_tv)).setText(IndustryUtil.INDUSTRY_SUB_ART);
		((TextView) findViewById(R.id.myeditindustry_yw_layout).findViewById(
				R.id.myeditindustryitem_decription_tv)).setText(IndustryUtil.INDUSTRY_SUB_MEDICAL);
		((TextView) findViewById(R.id.myeditindustry_fl_layout).findViewById(
				R.id.myeditindustryitem_decription_tv)).setText(IndustryUtil.INDUSTRY_SUB_LAW);
		((TextView) findViewById(R.id.myeditindustry_jy_layout).findViewById(
				R.id.myeditindustryitem_decription_tv)).setText(IndustryUtil.INDUSTRY_SUB_EDUCATION);
		((TextView) findViewById(R.id.myeditindustry_zf_layout).findViewById(
				R.id.myeditindustryitem_decription_tv)).setText(IndustryUtil.INDUSTRY_SUB_GOVERNMENT);
		((TextView) findViewById(R.id.myeditindustry_xs_layout).findViewById(
				R.id.myeditindustryitem_decription_tv)).setText(IndustryUtil.INDUSTRY_SUB_STUDENT);
		((TextView) findViewById(R.id.myeditindustry_other_layout).findViewById(
				R.id.myeditindustryitem_decription_tv)).setText(IndustryUtil.INDUSTRY_SUB_OTHERS);

		
		((TextView) findViewById(R.id.myeditindustry_it_layout).findViewById(
				R.id.myeditindustryitem_title_tv)).setBackgroundColor(getResources().getColor(R.color.industry_it));
		((TextView) findViewById(R.id.myeditindustry_gy_layout).findViewById(
				R.id.myeditindustryitem_title_tv)).setBackgroundColor(getResources().getColor(R.color.industry_gong));
		((TextView) findViewById(R.id.myeditindustry_sy_layout).findViewById(
				R.id.myeditindustryitem_title_tv)).setBackgroundColor(getResources().getColor(R.color.industry_shang));
		((TextView) findViewById(R.id.myeditindustry_jr_layout).findViewById(
				R.id.myeditindustryitem_title_tv)).setBackgroundColor(getResources().getColor(R.color.industry_jin));
		((TextView) findViewById(R.id.myeditindustry_wh_layout).findViewById(
				R.id.myeditindustryitem_title_tv)).setBackgroundColor(getResources().getColor(R.color.industry_wen));
		((TextView) findViewById(R.id.myeditindustry_ys_layout).findViewById(
				R.id.myeditindustryitem_title_tv)).setBackgroundColor(getResources().getColor(R.color.industry_yishu));
		((TextView) findViewById(R.id.myeditindustry_yw_layout).findViewById(
				R.id.myeditindustryitem_title_tv)).setBackgroundColor(getResources().getColor(R.color.industry_doctor));
		((TextView) findViewById(R.id.myeditindustry_fl_layout).findViewById(
				R.id.myeditindustryitem_title_tv)).setBackgroundColor(getResources().getColor(R.color.industry_fa));
		((TextView) findViewById(R.id.myeditindustry_jy_layout).findViewById(
				R.id.myeditindustryitem_title_tv)).setBackgroundColor(getResources().getColor(R.color.industry_jiao));
		((TextView) findViewById(R.id.myeditindustry_zf_layout).findViewById(
				R.id.myeditindustryitem_title_tv)).setBackgroundColor(getResources().getColor(R.color.industry_zheng));
		((TextView) findViewById(R.id.myeditindustry_xs_layout).findViewById(
				R.id.myeditindustryitem_title_tv)).setBackgroundColor(getResources().getColor(R.color.industry_xue));
		((TextView) findViewById(R.id.myeditindustry_other_layout).findViewById(
				R.id.myeditindustryitem_title_tv)).setBackgroundColor(getResources().getColor(R.color.industry_other));
		

		itBtn = ((Button) findViewById(R.id.myeditindustry_it_layout).findViewById(
				R.id.myeditindustryitem_radio_btn));
		gyBtn = ((Button) findViewById(R.id.myeditindustry_gy_layout).findViewById(
				R.id.myeditindustryitem_radio_btn));
		syBtn = ((Button) findViewById(R.id.myeditindustry_sy_layout).findViewById(
				R.id.myeditindustryitem_radio_btn));
		jrBtn = ((Button) findViewById(R.id.myeditindustry_jr_layout).findViewById(
				R.id.myeditindustryitem_radio_btn));
		whBtn = ((Button) findViewById(R.id.myeditindustry_wh_layout).findViewById(
				R.id.myeditindustryitem_radio_btn));
		ysBtn = ((Button) findViewById(R.id.myeditindustry_ys_layout).findViewById(
				R.id.myeditindustryitem_radio_btn));
		ywBtn = ((Button) findViewById(R.id.myeditindustry_yw_layout).findViewById(
				R.id.myeditindustryitem_radio_btn));
		flBtn = ((Button) findViewById(R.id.myeditindustry_fl_layout).findViewById(
				R.id.myeditindustryitem_radio_btn));
		jyBtn = ((Button) findViewById(R.id.myeditindustry_jy_layout).findViewById(
				R.id.myeditindustryitem_radio_btn));
		zfBtn = ((Button) findViewById(R.id.myeditindustry_zf_layout).findViewById(
				R.id.myeditindustryitem_radio_btn));
		xsBtn = ((Button) findViewById(R.id.myeditindustry_xs_layout).findViewById(
				R.id.myeditindustryitem_radio_btn));
		otherBtn = ((Button) findViewById(R.id.myeditindustry_other_layout).findViewById(
				R.id.myeditindustryitem_radio_btn));

		defaultSelect(getIntent().getIntExtra("industryid", 0));

	}

	private void defaultSelect(int id) {
		switch (id) {
		case 0:
			mSelectItem = IndustryUtil.INDUSTRY_ID_OTHERS;
			otherBtn.setBackgroundResource(R.drawable.classroom_icon_radio2);
			break;
		case 1:
			mSelectItem = IndustryUtil.INDUSTRY_ID_IT;
			itBtn.setBackgroundResource(R.drawable.classroom_icon_radio2);
			break;
		case 2:
			mSelectItem = IndustryUtil.INDUSTRY_ID_EMPLOYMENT;
			gyBtn.setBackgroundResource(R.drawable.classroom_icon_radio2);
			break;
		case 3:
			mSelectItem = IndustryUtil.INDUSTRY_ID_BUSINESS;
			syBtn.setBackgroundResource(R.drawable.classroom_icon_radio2);
			break;
		case 4:
			mSelectItem = IndustryUtil.INDUSTRY_ID_FINANCE;
			jrBtn.setBackgroundResource(R.drawable.classroom_icon_radio2);
			break;
		case 5:
			mSelectItem = IndustryUtil.INDUSTRY_ID_CULTURE;
			whBtn.setBackgroundResource(R.drawable.classroom_icon_radio2);
			break;
		case 6:
			mSelectItem = IndustryUtil.INDUSTRY_ID_ART;
			ysBtn.setBackgroundResource(R.drawable.classroom_icon_radio2);
			break;
		case 7:
			mSelectItem = IndustryUtil.INDUSTRY_ID_MEDICAL;
			ywBtn.setBackgroundResource(R.drawable.classroom_icon_radio2);
			break;
		case 8:
			mSelectItem = IndustryUtil.INDUSTRY_ID_LAW;
			flBtn.setBackgroundResource(R.drawable.classroom_icon_radio2);
			break;
		case 9:
			mSelectItem = IndustryUtil.INDUSTRY_ID_EDUCATION;
			jyBtn.setBackgroundResource(R.drawable.classroom_icon_radio2);
			break;
		case 10:
			mSelectItem = IndustryUtil.INDUSTRY_ID_GOVERNMENT;
			zfBtn.setBackgroundResource(R.drawable.classroom_icon_radio2);
			break;
		case 11:
			mSelectItem = IndustryUtil.INDUSTRY_ID_STUDENT;
			xsBtn.setBackgroundResource(R.drawable.classroom_icon_radio2);
			break;
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.commonheader_left_iv:
			Intent it = new Intent();
			it.putExtra("industryid", mSelectItem);
			String work = workEt.getText().toString();
			it.putExtra("work", work);
			
			setResult(EditPersonalActivity.RESULTCODE_WORK, it);
			finish();
			break;
		case R.id.myeditindustry_it_layout:
			clearBtnStatus();
			mSelectItem = IndustryUtil.INDUSTRY_ID_IT;
			itBtn.setBackgroundResource(R.drawable.classroom_icon_radio2);
			break;
		case R.id.myeditindustry_gy_layout:
			clearBtnStatus();
			mSelectItem = IndustryUtil.INDUSTRY_ID_EMPLOYMENT;
			gyBtn.setBackgroundResource(R.drawable.classroom_icon_radio2);
			break;
		case R.id.myeditindustry_sy_layout:
			clearBtnStatus();
			mSelectItem = IndustryUtil.INDUSTRY_ID_BUSINESS;
			syBtn.setBackgroundResource(R.drawable.classroom_icon_radio2);
			break;
		case R.id.myeditindustry_jr_layout:
			clearBtnStatus();
			mSelectItem = IndustryUtil.INDUSTRY_ID_FINANCE;
			jrBtn.setBackgroundResource(R.drawable.classroom_icon_radio2);
			break;
		case R.id.myeditindustry_wh_layout:
			clearBtnStatus();
			mSelectItem = IndustryUtil.INDUSTRY_ID_CULTURE;
			whBtn.setBackgroundResource(R.drawable.classroom_icon_radio2);
			break;
		case R.id.myeditindustry_ys_layout:
			clearBtnStatus();
			mSelectItem = IndustryUtil.INDUSTRY_ID_ART;
			ysBtn.setBackgroundResource(R.drawable.classroom_icon_radio2);
			break;
		case R.id.myeditindustry_yw_layout:
			clearBtnStatus();
			mSelectItem = IndustryUtil.INDUSTRY_ID_MEDICAL;
			ywBtn.setBackgroundResource(R.drawable.classroom_icon_radio2);
			break;
		case R.id.myeditindustry_fl_layout:
			clearBtnStatus();
			mSelectItem = IndustryUtil.INDUSTRY_ID_LAW;
			flBtn.setBackgroundResource(R.drawable.classroom_icon_radio2);
			break;
		case R.id.myeditindustry_jy_layout:
			clearBtnStatus();
			mSelectItem = IndustryUtil.INDUSTRY_ID_EDUCATION;
			jyBtn.setBackgroundResource(R.drawable.classroom_icon_radio2);
			break;
		case R.id.myeditindustry_zf_layout:
			clearBtnStatus();
			mSelectItem = IndustryUtil.INDUSTRY_ID_GOVERNMENT;
			zfBtn.setBackgroundResource(R.drawable.classroom_icon_radio2);
			break;
		case R.id.myeditindustry_xs_layout:
			clearBtnStatus();
			mSelectItem = IndustryUtil.INDUSTRY_ID_STUDENT;
			xsBtn.setBackgroundResource(R.drawable.classroom_icon_radio2);
			break;
		case R.id.myeditindustry_other_layout:
			clearBtnStatus();
			mSelectItem = IndustryUtil.INDUSTRY_ID_OTHERS;
			otherBtn.setBackgroundResource(R.drawable.classroom_icon_radio2);
			break;
		}
	}

	private void clearBtnStatus() {
		itBtn.setBackgroundResource(R.drawable.classroom_icon_radio1);
		gyBtn.setBackgroundResource(R.drawable.classroom_icon_radio1);
		syBtn.setBackgroundResource(R.drawable.classroom_icon_radio1);
		jrBtn.setBackgroundResource(R.drawable.classroom_icon_radio1);
		whBtn.setBackgroundResource(R.drawable.classroom_icon_radio1);
		ysBtn.setBackgroundResource(R.drawable.classroom_icon_radio1);
		ywBtn.setBackgroundResource(R.drawable.classroom_icon_radio1);
		flBtn.setBackgroundResource(R.drawable.classroom_icon_radio1);
		jyBtn.setBackgroundResource(R.drawable.classroom_icon_radio1);
		zfBtn.setBackgroundResource(R.drawable.classroom_icon_radio1);
		xsBtn.setBackgroundResource(R.drawable.classroom_icon_radio1);
		otherBtn.setBackgroundResource(R.drawable.classroom_icon_radio1);
	}

	
}
