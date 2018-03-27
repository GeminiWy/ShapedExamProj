package com.nd.shapedexamproj.activity.my;

import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.*;

import com.nd.shapedexamproj.R;
import com.nd.shapedexamproj.activity.BaseActivity;
import com.nd.shapedexamproj.util.StringUtils;

/**
 * 
 * 编辑电话
 * 
 * @author Linlg
 * 
 *         Create on 2014-4-3
 */
public class SetPhoneActivity extends BaseActivity implements OnClickListener {
	private EditText phoneEt;
	private Button teacherBtn, friendBtn, allBtn;
	private ImageView phoneBtn;
	private RelativeLayout teacherLayout, friendLayout, allLayout;
	private int status = 0;

/*	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.my_edit_phone);
		initView();
	}*/

	private void initView() {
		phoneEt = (EditText) findViewById(R.id.myeditphone_et);
		phoneEt.setText(getIntent().getStringExtra("phone"));
		phoneBtn = (ImageView) findViewById(R.id.myeditphone_x_btn);
		teacherBtn = (Button) findViewById(R.id.myeditphone_teacher_btn);
		friendBtn = (Button) findViewById(R.id.myeditphone_friend_btn);
		allBtn = (Button) findViewById(R.id.myeditphone_all_btn);
		teacherLayout = (RelativeLayout) findViewById(R.id.myeditphone_teacher_rl);
		friendLayout = (RelativeLayout) findViewById(R.id.myeditphone_friend_rl);
		allLayout = (RelativeLayout) findViewById(R.id.myeditphone_all_rl);
		((TextView) findViewById(R.id.myeditphone_head_layout).findViewById(
				R.id.commonheader_title_tv)).setText(R.string.phone_num);

		if (getIntent().getIntExtra("phonevisible", 0) == 1) {
			teacherBtn.setBackgroundResource(R.drawable.classroom_icon_radio2);
			status = 1;
		} else if (getIntent().getIntExtra("phonevisible", 0) == 2) {
			friendBtn.setBackgroundResource(R.drawable.classroom_icon_radio2);
			status = 2;
		} else if (getIntent().getIntExtra("phonevisible", 0) == 3) {
			allBtn.setBackgroundResource(R.drawable.classroom_icon_radio2);
			status = 3;
		}

		findViewById(R.id.myeditphone_head_layout).findViewById(R.id.commonheader_left_iv)
				.setOnClickListener(this);
		findViewById(R.id.myeditphone_head_layout).findViewById(R.id.commonheader_right_btn)
				.setVisibility(View.GONE);
		teacherLayout.setOnClickListener(this);
		friendLayout.setOnClickListener(this);
		allLayout.setOnClickListener(this);
		phoneBtn.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.commonheader_left_iv:
			/*if (phoneEt.getText().toString().equals("")) {
				Toast.makeText(SetPhoneActivity.this, "号码不能为空", Toast.LENGTH_SHORT).show();
				return;
			} else {*/
			String phone = phoneEt.getText().toString().trim();
			
			if(StringUtils.isOnlyNumber(phone)) {
				Intent it = new Intent();
				it.putExtra("phone", phone == null ? "" : phone);
				it.putExtra("phonevisible", status);
				setResult(EditPersonalActivity.RESULTCODE_PHONE, it);
				finish();
			} else {
				Toast.makeText(SetPhoneActivity.this, getResources().getString(R.string.number_only), Toast.LENGTH_SHORT).show();
			}
			/*}*/
			break;
		case R.id.myeditphone_x_btn:
			phoneEt.setText("");
			break;
		case R.id.myeditphone_teacher_rl:
			clearBtnStatus();
			status = 1;
			teacherBtn.setBackgroundResource(R.drawable.classroom_icon_radio2);
			break;
		case R.id.myeditphone_friend_rl:
			clearBtnStatus();
			status = 2;
			friendBtn.setBackgroundResource(R.drawable.classroom_icon_radio2);
			break;
		case R.id.myeditphone_all_rl:
			clearBtnStatus();
			status = 3;
			allBtn.setBackgroundResource(R.drawable.classroom_icon_radio2);
			break;
		}
	}

	/**
	 * 清除radio选中状态
	 */
	private void clearBtnStatus() {
		teacherBtn.setBackgroundResource(R.drawable.classroom_icon_radio1);
		friendBtn.setBackgroundResource(R.drawable.classroom_icon_radio1);
		allBtn.setBackgroundResource(R.drawable.classroom_icon_radio1);
	}

	@Override
	public int initResource() {
		return R.layout.my_edit_phone;
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
		
	}
}
