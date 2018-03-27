package com.nd.shapedexamproj.activity.more;

import android.app.AlertDialog;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.*;

import com.nd.shapedexamproj.App;
import com.nd.shapedexamproj.AuthorityManager;
import com.nd.shapedexamproj.R;
import com.nd.shapedexamproj.activity.BaseActivity;
import com.nd.shapedexamproj.util.Constants;
import com.nd.shapedexamproj.util.Utils;
import com.tming.common.net.TmingHttp;
import com.tming.common.net.TmingHttp.RequestCallback;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class FeedbackActivity extends BaseActivity implements OnClickListener {

    private static final int INPUT_MAX_LENGTH_CONTENT = 300;
	private ImageView goBack;
	private TextView currLocation, feedbackPrompt;
	private Button commit, confirm;
	private EditText feedback;
	private RelativeLayout feedbackDialog;
	private  AlertDialog dialog = null;
	@Override
	public int initResource() {
		return R.layout.feedback;
	}

	@Override
	public void initComponent() {
		goBack = (ImageView) findViewById(R.id.commonheader_left_iv);

		currLocation = (TextView) findViewById(R.id.commonheader_title_tv);
		currLocation.setText(getResources().getString(R.string.feedback));

		commit = (Button) findViewById(R.id.commonheader_right_btn);
		commit.setText(getResources().getString(R.string.feedback_commit));
		commit.setVisibility(View.VISIBLE);

		feedback = (EditText) findViewById(R.id.feedbak);
		feedbackDialog = (RelativeLayout) getLayoutInflater().inflate(
				R.layout.common_dialog_layout_certain, null);
		feedbackPrompt = (TextView) feedbackDialog.findViewById(R.id.common_dialog_content);
		confirm = (Button) feedbackDialog.findViewById(R.id.positive_button);
		// System.out.println(Constants.USER_ID);
	}

	@Override
	public void initData() {

	}

	@Override
	public void addListener() {
		goBack.setOnClickListener(this);
		commit.setOnClickListener(this);
		
		String errTitleMaxLength = getResources().getString(R.string.coach_content_max_length);
        errTitleMaxLength = String.format(errTitleMaxLength, INPUT_MAX_LENGTH_CONTENT);
        
        Utils.addEditViewMaxLengthListener(this, feedback, INPUT_MAX_LENGTH_CONTENT, errTitleMaxLength);
	}
	
	@Override
	public void onClick(View v) {
		if (v == goBack) {
			finish();
		} else if (v == commit) {
			if (feedback.getText().toString().trim().length() < 1) {
				Toast.makeText(this, R.string.feedback_blank_error, Toast.LENGTH_SHORT).show();
			} else if (feedback.getText().toString().trim().length() < 6) {
				Toast.makeText(this, R.string.feedback_least_length, Toast.LENGTH_SHORT).show();
			} else {
				commit.setClickable(false);
				commitFeedback();
			}
		}
	}

	/**
	 * 提交意见反馈
	 */
	private void commitFeedback() {
		Map<String, Object> params = new HashMap<String, Object>();
		if (AuthorityManager.getInstance().isInnerAuthority()) {
		    params.put("userid", "0");
		} else {
		    params.put("userid", App.getUserId());
		}
		params.put("content", feedback.getText().toString());
		params.put("platformtype", 1);
		// 查询反馈接口：
		TmingHttp.asyncRequest(Constants.HOST_DEBUG
				+ "feedback/addFeedback.html", params,
				new RequestCallback<Integer>() {

					@Override
					public Integer onReqestSuccess(String respones)
							throws Exception {
						System.out.println("json respones : " + respones);
						int flag = 0;
						try {
							JSONObject jobj = new JSONObject(respones);
							flag = jobj.getInt("flag");

						} catch (JSONException e) {
							e.printStackTrace();
						}

						return flag;
					}

					@Override
					public void success(Integer respones) {
						if (respones == 1) {// 提交成功
							showDialog(getResources().getString(R.string.feedback_commit_success), true);

						} else {// 提交失败

							showDialog(getResources().getString(R.string.feedback_commit_server_error), false);
						}
						commit.setClickable(true);
					}

					@Override
					public void exception(Exception exception) {
						System.out.println(exception.getMessage());
						commit.setClickable(true);
						showDialog(getResources().getString(R.string.feedback_commit_net_error), false);

					}

				});

	}

	/**
	 * 提交反馈提醒
	 * 
	 * @param msg
	 *            提交反馈后 提示信息
	 * @param isSuccess
	 *            本次提交反馈 是否成功
	 */
	private void showDialog(String msg, final boolean isSuccess) {
		if (isSuccess) {
			feedbackPrompt.setText(getResources().getString(R.string.feedback_commit_success));
		} else {
			feedbackPrompt.setText(msg);
		}
		if(dialog == null){
			dialog = new AlertDialog.Builder(this).setView(
					feedbackDialog).create();
		}
		
		dialog.show();

		confirm.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (isSuccess) {
				    dialog.dismiss();
					finish();
				} else {
					dialog.dismiss();
				}
			}
		});
	}

}
