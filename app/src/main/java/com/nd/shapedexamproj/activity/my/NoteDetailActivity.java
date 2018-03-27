package com.nd.shapedexamproj.activity.my;

import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nd.shapedexamproj.R;
import com.nd.shapedexamproj.activity.BaseActivity;
import com.nd.shapedexamproj.model.note.MyNote;
import com.nd.shapedexamproj.util.UIHelper;

import java.text.SimpleDateFormat;
import java.util.Date;
/**
 * 我的笔记详情
 * @author zll
 *
 */
public class NoteDetailActivity extends BaseActivity implements OnClickListener{
	
	private RelativeLayout notedetail_head_item ;
	private TextView notedetail_head_title_tv, notedetail_record_time_tv;
	
	MyNote myNote ;
	@Override
	public int initResource() {
		return R.layout.my_note_detail_activity;
	}

	@Override
	public void initComponent() {
		findViewById(R.id.commonheader_left_iv).setOnClickListener(this);
		findViewById(R.id.common_head_right_btn).setVisibility(View.GONE);
		((TextView)findViewById(R.id.commonheader_title_tv)).setText("笔记详情");
		myNote = (MyNote) getIntent().getSerializableExtra("note");
		
		notedetail_head_item = (RelativeLayout) findViewById(R.id.notedetail_head_item);
		notedetail_head_title_tv = (TextView) findViewById(R.id.notedetail_head_title_tv);
		notedetail_record_time_tv = (TextView) findViewById(R.id.notedetail_record_time_tv);	
		
		/*String noteContent = Helper.replaceHtmlText(myNote.noteContent);*/
		((TextView) findViewById(R.id.notedetail_content_tv)).setText(myNote.noteContent);
		((TextView) findViewById(R.id.notedetail_content_tv)).setMovementMethod(ScrollingMovementMethod.getInstance());
		
	}

	@Override
	public void initData() {
		if (myNote != null) {
			notedetail_head_title_tv.setText("《" + myNote.courseName + "》" + myNote.coursewareName) ;
			try {
				SimpleDateFormat spf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
				long noteAddTime = Long.parseLong(myNote.noteAddTime);
				String recordTime = spf.format(new Date(noteAddTime * 1000));
				notedetail_record_time_tv.setText(recordTime);
			} catch (Exception e){
				e.printStackTrace();
			}
		}
	}

	@Override
	public void addListener() {
		notedetail_head_item.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				//点击进入视频播放页对应记录的时间节点
				UIHelper.showCourseDetail(NoteDetailActivity.this, myNote.courseId, myNote.courseName);
			}
		});
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.commonheader_left_iv:
			finish();
			break;
		}
	}

}
