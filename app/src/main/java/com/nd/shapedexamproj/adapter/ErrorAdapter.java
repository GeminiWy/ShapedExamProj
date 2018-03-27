package com.nd.shapedexamproj.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nd.shapedexamproj.R;
import com.nd.shapedexamproj.model.ErrorQuestion;
import com.nd.shapedexamproj.util.UIHelper;
import com.tming.common.adapter.CommonBaseAdapter;

public class ErrorAdapter extends CommonBaseAdapter<ErrorQuestion> implements OnClickListener{
	
	private Context context ;
	public ErrorAdapter(Context context) {
		super(context);
		this.context = context ;
	}

	@Override
	public void onClick(View v) {
		
	}

	@Override
	public View infateItemView(Context context) {
		View convertView = LayoutInflater.from(context).inflate(
				R.layout.error_question_item, null);
		ViewHolder holder = new ViewHolder();
		holder.error_question_item_rl = (RelativeLayout) convertView.findViewById(R.id.error_question_item_rl);
		holder.titleTv = (TextView) convertView
				.findViewById(R.id.error_question_item_title_tv);
		holder.numberTv = (TextView) convertView
				.findViewById(R.id.error_question_item_number_tv);
		convertView.setTag(holder);
		
		return convertView;
	}

	@Override
	public void setViewHolderData(BaseViewHolder arg0,final ErrorQuestion arg1) {
		ViewHolder holder = (ViewHolder) arg0;
		
		holder.error_question_item_rl.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				/*Intent it = new Intent(context, RedoErrorsActivity.class);
				it.putExtra("courseId", arg1.courseId);
				it.putExtra("courseName", arg1.courseName);
				context.startActivity(it);*/
                UIHelper.showDoingWrongHomeworkActivity(context, arg1.courseId, arg1.courseName);
			}
		});

		holder.titleTv.setText(arg1.courseName);
		holder.numberTv.setText("" + arg1.error);
	}
	
	private class ViewHolder extends BaseViewHolder{
		private RelativeLayout error_question_item_rl;
		private TextView titleTv;
		private TextView numberTv;
	}
	
}
