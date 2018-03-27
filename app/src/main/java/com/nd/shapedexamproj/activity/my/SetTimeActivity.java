package com.nd.shapedexamproj.activity.my;


import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.DatePicker;
import android.widget.DatePicker.OnDateChangedListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nd.shapedexamproj.R;
import com.nd.shapedexamproj.activity.BaseActivity;
import com.nd.shapedexamproj.util.StringUtils;
import com.nd.shapedexamproj.util.UIHelper;
import com.tming.common.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

/**
 * 
 * 编辑年龄&星座
 * 
 * @author Linlg
 * 
 *         Create on 2014-4-1
 */
public class SetTimeActivity extends BaseActivity {
	
	private final static String TAG = "SetTimeActivity";
	
	private static final int MIN_YEAR = 1920;
	
	private DatePicker datePicker;
	private RelativeLayout mHeadRL;
	private ImageView mBackIV;
	private TextView mHeadTitleTV;

	@Override
	public int initResource() {
		return R.layout.my_edit_time;
	}

	@Override
	public void initComponent() {
		datePicker = (DatePicker) findViewById(R.id.myedittime_dp);
		mHeadRL = (RelativeLayout)findViewById(R.id.myedittime_head_layout);
		mHeadRL.findViewById(R.id.commonheader_right_btn).setVisibility(View.GONE);
		mBackIV = (ImageView)mHeadRL.findViewById(R.id.commonheader_left_iv);
		mHeadTitleTV = (TextView)mHeadRL.findViewById(R.id.commonheader_title_tv);
		
	}

	@Override
	public void initData() {
		String birthday = getIntent().getStringExtra("birthday");
		String title = getIntent().getStringExtra("title");
		mHeadTitleTV.setText(title);
		Log.d(TAG, birthday);
		List<String> dateList = new ArrayList<String>();
		if(StringUtils.isEmpty(birthday))
		{
			dateList.add("1990");
			dateList.add("1");
			dateList.add("1");
			
		}
		else
		{
			String[] date = birthday.split("-");
			dateList = new ArrayList<String>(Arrays.asList(date));
		}
		
		datePicker.init(Integer.parseInt(dateList.get(0)), Integer.parseInt(dateList.get(1)) - 1,
				Integer.parseInt(dateList.get(2)), new OnDateChangedListener() {

				    @Override
				    public void onDateChanged(DatePicker view, int year, int monthOfYear,int dayOfMonth) {

				        if(isDateAfter(view)){
				            Calendar mCalendar = Calendar.getInstance();
				            view.init(mCalendar.get(Calendar.YEAR) - 1, 11, 31, this);
				            UIHelper.ToastMessage(SetTimeActivity.this, "请选择小于今年的日期");
				        }
				        
				        if (isDateBefore(view)) {
					         view.init(MIN_YEAR, 0, 1, this);
				        }
				    }
		});
	}

	private boolean isDateAfter(DatePicker tempView) {
		//只要年份控制
	        Calendar mCalendar = Calendar.getInstance();
	        mCalendar.set(mCalendar.get(Calendar.YEAR) - 1, 11, 31, 0, 0, 0);
	        Calendar tempCalendar = Calendar.getInstance();
	        tempCalendar.set(tempView.getYear(), tempView.getMonth(), tempView.getDayOfMonth(), 0, 0, 0);
	        if(tempCalendar.after(mCalendar))
	            return true;
	        else 
	            return false;
	}
	
	private boolean isDateBefore(DatePicker tempView) {
    
        final int selectYear = tempView.getYear();
        if(selectYear < MIN_YEAR)
            return true;
        else 
            return false;
}
		
	@Override
	public void addListener() {
		mBackIV.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent it = new Intent();
				it.putExtra("getyear", datePicker.getYear());
				it.putExtra("getmonth", datePicker.getMonth() + 1);
				it.putExtra("getday", datePicker.getDayOfMonth());
				SetTimeActivity.this.setResult(EditPersonalActivity.RESULTCODE_DATE, it);
				finish();
			}
		});
		
		
	}
}
