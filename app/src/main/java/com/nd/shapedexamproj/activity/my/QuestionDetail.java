package com.nd.shapedexamproj.activity.my;

import android.widget.TextView;

import com.nd.shapedexamproj.R;
import com.nd.shapedexamproj.activity.BaseActivity;

public class QuestionDetail extends BaseActivity {

	@Override
	public int initResource() {
		return R.layout.question_detail;
	}

	@Override
	public void initComponent() {
		((TextView) findViewById(R.id.question_detail_title_tv)).setText(getIntent()
				.getStringExtra("question_title"));
		((TextView) findViewById(R.id.question_detail_question_tv)).setText(getIntent()
				.getStringExtra("question_problem"));
		((TextView) findViewById(R.id.question_detail_answer_tv)).setText(getIntent()
				.getStringExtra("question_answer"));
	}

	@Override
	public void initData() {
	}

	@Override
	public void addListener() {

	}

}
