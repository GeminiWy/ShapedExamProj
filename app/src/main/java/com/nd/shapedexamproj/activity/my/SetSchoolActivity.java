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
 * 学校信息
 * 
 * @author Linlg
 * 
 *         Create on 2014-4-3
 */
public class SetSchoolActivity extends BaseActivity implements OnClickListener {

    private EditText primaryschoolEt, juniormiddleschoolEt, seniormiddleschoolEt, universityEt;

    /*
     * @Override protected void onCreate(Bundle savedInstanceState) {
     * super.onCreate(savedInstanceState);
     * setContentView(R.layout.my_edit_school); initView(); }
     */
    private void initView() {
        findViewById(R.id.myeditschool_head_layout).findViewById(R.id.commonheader_left_iv).setOnClickListener(this);
        ((TextView) findViewById(R.id.myeditschool_head_layout).findViewById(R.id.commonheader_title_tv))
                .setText("学校信息");
        findViewById(R.id.myeditschool_head_layout).findViewById(R.id.commonheader_right_btn).setVisibility(View.GONE);
        primaryschoolEt = (EditText) findViewById(R.id.myeditschool_primaryschool_et);
        primaryschoolEt.setText(getIntent().getStringExtra("primaryschool"));
        juniormiddleschoolEt = (EditText) findViewById(R.id.myeditschool_juniormiddleschool_et);
        juniormiddleschoolEt.setText(getIntent().getStringExtra("juniormiddleschool"));
        seniormiddleschoolEt = (EditText) findViewById(R.id.myeditschool_seniormiddleschool_et);
        seniormiddleschoolEt.setText(getIntent().getStringExtra("seniormiddleschool"));
        universityEt = (EditText) findViewById(R.id.myeditschool_university_et);
        universityEt.setText(getIntent().getStringExtra("university"));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.commonheader_left_iv:
                Intent it = new Intent();
                it.putExtra("primaryschool", primaryschoolEt.getText().toString());
                it.putExtra("juniormiddleschool", juniormiddleschoolEt.getText().toString());
                it.putExtra("seniormiddleschool", seniormiddleschoolEt.getText().toString());
                it.putExtra("university", universityEt.getText().toString());
                setResult(500, it);
                finish();
                break;
        }
    }

    @Override
    public int initResource() {
        return R.layout.my_edit_school;
    }

    @Override
    public void initComponent() {
        initView();
    }

    @Override
    public void initData() {}

    @Override
    public void addListener() {
        {
            String errContentMaxLength = getResources().getString(R.string.error_max_length);
            errContentMaxLength = String.format(errContentMaxLength, EditPersonalActivity.EDIT_MAX_LENGTH);
            Utils.addEditViewMaxLengthListener(this, primaryschoolEt, EditPersonalActivity.EDIT_MAX_LENGTH, errContentMaxLength);
        }
        
        {
            String errContentMaxLength = getResources().getString(R.string.error_max_length);
            errContentMaxLength = String.format(errContentMaxLength, EditPersonalActivity.EDIT_MAX_LENGTH);
            Utils.addEditViewMaxLengthListener(this, juniormiddleschoolEt, EditPersonalActivity.EDIT_MAX_LENGTH, errContentMaxLength);
        }
        
        {
            String errContentMaxLength = getResources().getString(R.string.error_max_length);
            errContentMaxLength = String.format(errContentMaxLength, EditPersonalActivity.EDIT_MAX_LENGTH);
            Utils.addEditViewMaxLengthListener(this, seniormiddleschoolEt, EditPersonalActivity.EDIT_MAX_LENGTH, errContentMaxLength);
            
        }
        
        
        {
            String errContentMaxLength = getResources().getString(R.string.error_max_length);
            errContentMaxLength = String.format(errContentMaxLength, EditPersonalActivity.EDIT_MAX_LENGTH);
            Utils.addEditViewMaxLengthListener(this, universityEt, EditPersonalActivity.EDIT_MAX_LENGTH, errContentMaxLength);
        }
    }
}
