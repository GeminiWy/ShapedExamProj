package com.nd.shapedexamproj.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.nd.shapedexamproj.R;

/**
 * 手动版本检查更新
 * @author ysy
 */
public class VersionCheckUpdateAdapter extends AutoVersionCheckUpdateAdapter {

	private AlertDialog alertDialog;
	
	public VersionCheckUpdateAdapter(Context context) {
		super(context);
	}

	@Override
	public void checkUpdateBefore() {
		if (waitDialog == null) {
			View vWait = View.inflate(context,
					R.layout.common_dialog_layout_wait, null);
			waitDialog = new AlertDialog.Builder(context).setView(
					vWait).show();
		} else {
			waitDialog.show();
		}
	}

	@Override
	public void checkUpadteVersionIsNewest() {
		showAlertDialog(context.getString(R.string.settings_dialog_update_no_update));
	}

	@Override
	public void checkUpdateNetworkUnavailable() {
		showAlertDialog(context.getString(R.string.please_open_network));
	}

	@Override
	public void checkUpdateError(Exception e) {
		showAlertDialog(context.getString(R.string.settings_dialog_check_fail));
	}
	
	private void showAlertDialog(String text) {
		if (waitDialog != null && waitDialog.isShowing()) {
			waitDialog.dismiss();
		}
		View vLatest = LayoutInflater.from(context).inflate(
				R.layout.common_dialog_layout_certain, null);
		TextView tv = (TextView) vLatest
				.findViewById(R.id.common_dialog_content);
		tv.setText(text);
		alertDialog = new AlertDialog.Builder(context)
				.setView(vLatest).show();
		Button btn = (Button) vLatest.findViewById(R.id.positive_button);
		btn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				alertDialog.dismiss();
			}
		});
	}
}
