package com.nd.shapedexamproj.activity.my;

import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.TextView;

import com.nd.shapedexamproj.R;
import com.nd.shapedexamproj.activity.BaseActivity;
import com.nd.shapedexamproj.util.Utils;

/**
 * 
 * 编辑公司&兴趣爱好&个人说明
 * 
 * @author Linlg
 * 
 *         Create on 2014-4-3
 */
public class SetCompanyActivity extends BaseActivity implements OnClickListener {
	
	public static final int EDIT_MAX_LENGTH_1 = 60, EDIT_MAX_LENGTH_2 = 100;
	public static final int WHICH_COMPANY = 1, WHICH_HOBBY = 2, WHICH_EXPLANATION = 3;
	
	private EditText infoEt;
	private int which;


	private void initView() {
		findViewById(R.id.myeditcompany_head_layout).findViewById(R.id.commonheader_left_iv)
				.setOnClickListener(this);
		findViewById(R.id.myeditcompany_head_layout).findViewById(R.id.commonheader_right_btn).setVisibility(View.GONE);
		((TextView) findViewById(R.id.myeditcompany_head_layout).findViewById(
				R.id.commonheader_title_tv)).setText(getIntent().getStringExtra("title"));
		which = getIntent().getIntExtra("which", 0);
		infoEt = (EditText) findViewById(R.id.myeditcompany_et);
		infoEt.setText(getIntent().getStringExtra("info"));
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.commonheader_left_iv:
			//if (!infoEt.getText().toString().equals("")) {
				Intent it = new Intent();
				it.putExtra("info", infoEt.getText().toString());
				switch (which) {
				case WHICH_COMPANY: // 编辑公司
					setResult(EditPersonalActivity.RESULTCODE_COMPANY, it);
					break;
				case WHICH_HOBBY: // 编辑兴趣
					setResult(EditPersonalActivity.RESULTCODE_HOBBY, it);
					break;
				case WHICH_EXPLANATION: // 编辑签名
					setResult(EditPersonalActivity.RESULTCODE_INFO, it);
					break;
				}
			//}
			finish();
			break;
		}
	}

	@Override
	public int initResource() {
		return R.layout.my_edit_company;
	}

	@Override
	public void initComponent() {
		initView();
	}

	@Override
	public void initData() {

		if(WHICH_COMPANY == which)
		{
			infoEt.setHint(R.string.hint_edit_company);
		}
		else if(WHICH_HOBBY == which)
		{
			infoEt.setHint(R.string.hint_edit_hobby);
		}
		else if(WHICH_EXPLANATION == which)
		{
			infoEt.setHint(R.string.hint_edit_explanation);
		}
	}

	@Override
	public void addListener() {
		
		int maxLength = EDIT_MAX_LENGTH_1;
		if(WHICH_COMPANY == which)
		{
			maxLength = EDIT_MAX_LENGTH_1;
		}
		else if(WHICH_HOBBY == which)
		{
			maxLength = EDIT_MAX_LENGTH_2;
		}
		else if(WHICH_EXPLANATION == which)
		{
			maxLength = EDIT_MAX_LENGTH_2;
		}
	    String errContentMaxLength = getResources().getString(R.string.error_max_length);
	    errContentMaxLength = String.format(errContentMaxLength, maxLength);

		Utils.addEditViewMaxLengthListener(this, infoEt, maxLength, errContentMaxLength);
	}
}
