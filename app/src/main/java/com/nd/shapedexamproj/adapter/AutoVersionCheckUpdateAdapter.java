package com.nd.shapedexamproj.adapter;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.*;
import android.widget.CompoundButton.OnCheckedChangeListener;

import com.nd.shapedexamproj.App;
import com.nd.shapedexamproj.R;
import com.nd.shapedexamproj.util.Constants;
import com.nd.shapedexamproj.util.SharedPreferUtils;
import com.tming.common.download.APKNotificationDownloader;
import com.tming.common.download.BaseDownload;
import com.tming.common.util.Helper;
import com.tming.common.util.VersionUpdate.VersionCheckUpadteAdapterInterface;
import com.tming.common.util.VersionUpdate.VersionInfo;
import org.json.JSONObject;

import java.io.File;

/**
 * 首页自动检查升级
 * @author yusongying 
 *
 */
public class AutoVersionCheckUpdateAdapter implements VersionCheckUpadteAdapterInterface {

	protected Dialog waitDialog, hasNewVersionDialog;
	protected Context context;
	protected UpdateDownloader downloader;

	public AutoVersionCheckUpdateAdapter(Context context) {
		this.context = context;
	}

	@Override
	public VersionInfo checkUpdateJsonParse(String jsonString) throws Exception {
		JSONObject object = new JSONObject(jsonString);
		/*JSONArray records = object.getJSONArray("records");
		JSONObject info = records.getJSONObject(0);*/

		VersionInfo versionInfo = new VersionInfo();
		String versionCodeString = object.getString("versionCode");
		versionInfo.versionCode = Integer.parseInt(versionCodeString);
		versionInfo.versionName = object.getString("versionName");
		versionInfo.newVersionDesc = object.getString("newVersionDesc");
		versionInfo.newVersionDownloadUrl = object.getString("newVersionDownloadUrl");
		versionInfo.isForcedUpdate = object.getBoolean("isForcedUpdate");// 是否强制更新
		return versionInfo;
	}

	@Override
	public void checkUpadteHasNewVersion(final VersionInfo info) {
		Constants.HAS_NEW_VERSION = true ;
		if (waitDialog != null && waitDialog.isShowing()) {
			waitDialog.dismiss();
		}
		
		if (SharedPreferUtils.getSharedPreferences(context).getBoolean(Constants.IS_NOLONGER_NOTIFY, false)) {//是否不再提醒
			return;
		}
		
		View vDownload = LayoutInflater.from(context).inflate(R.layout.common_dialog_update, null);
		TextView tvVersion = (TextView) vDownload.findViewById(R.id.common_dialog_tv_version);
		tvVersion.setText(context.getResources().getString(R.string.latest_version) + info.versionName);
		TextView tvContent = (TextView) vDownload.findViewById(R.id.common_dialog_content);
		tvContent.setText(info.newVersionDesc);
		
		final CheckBox notifyCk = (CheckBox) vDownload.findViewById(R.id.common_dialog_ck);
		notifyCk.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				Editor editor = SharedPreferUtils.getSharedPreferences(context).edit();
				if (isChecked) {
					notifyCk.setChecked(true);
					editor.putBoolean(Constants.IS_NOLONGER_NOTIFY, true);
				} else {
					notifyCk.setChecked(false);
					editor.putBoolean(Constants.IS_NOLONGER_NOTIFY, false);
				}
				editor.commit();
			}
		});
		Button btnDownload = (Button) vDownload.findViewById(R.id.positive_button);
		Button btnCancel = (Button) vDownload.findViewById(R.id.negative_button);
		
		Builder builder = new Builder(context).setView(vDownload);
		btnDownload.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				hasNewVersionDialog.dismiss();
				
				String versionName = info.versionName;
				String url = info.newVersionDownloadUrl;
				if (App.createDownloadPath() == null) {
				    return;
                }
				try {
				    File saveToFile = new File(App.createDownloadPath(), context.getApplicationInfo().name + "_" + info.versionCode + ".apk");
    				if (!saveToFile.exists()) {
    				    APKNotificationDownloader.startDownload(context, info.newVersionDownloadUrl, saveToFile);
    				} else {
    				    Intent installIntent = new Intent(Intent.ACTION_VIEW);
    			        Uri uri = Uri.fromFile(saveToFile);
    			        installIntent.setDataAndType(uri, "application/vnd.android.package-archive");
    			        context.startActivity(installIntent);
    			        
    			        Toast.makeText(context, context.getResources().getString(R.string.download_finished), Toast.LENGTH_LONG).show();
    				}
				} catch (Exception e) {
                    e.printStackTrace();
                }
			}
		});
		builder.setCancelable(!info.isForcedUpdate);
		if(info.isForcedUpdate) {
			/*vDownload.findViewById(R.id.common_dialog_button_separator).setVisibility(View.GONE);*/
			btnCancel.setVisibility(View.GONE);
		} else {
			btnCancel.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) {
					hasNewVersionDialog.dismiss();
				}
			});
		}
		
		hasNewVersionDialog = builder.show();
	}

	@Override
	public void checkUpadteVersionIsNewest() {
		Constants.HAS_NEW_VERSION = false ;
	}

	@Override
	public void checkUpdateNetworkUnavailable() {
	}

	@Override
	public void checkUpdateError(Exception e) {
	}

	private class UpdateDownloader extends BaseDownload {
		
		private ProgressBar progressBar;
		private TextView showProgressTv;
		
		public UpdateDownloader(Context context, String downloadUrl,
				File saveToFile, ProgressBar progressBar, TextView tv) {
			super(context, downloadUrl, saveToFile);
			this.progressBar = progressBar;
			this.showProgressTv = tv;
		}
		
		@Override
		protected void publishProgressOnMainThread(int progress) {
			super.publishProgressOnMainThread(progress);
			progressBar.setProgress(progress);
			showProgressTv.setText("已下载：" + progress + "%");
		}
		
		@Override
		protected void publishResultOnMainThread(File saveToFile) {
			super.publishResultOnMainThread(saveToFile);
			hasNewVersionDialog.dismiss();
			Intent intent = new Intent();
	        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	        intent.setAction(Intent.ACTION_VIEW);
	        
	        intent.setDataAndType(Uri.fromFile(saveToFile), "application/vnd.android.package-archive");
	        context.startActivity(intent);
		}
		
		@SuppressLint("LongLogTag")
		@Override
		protected void publishExceptionOnMainThread(Exception e) {
			super.publishExceptionOnMainThread(e);
			e.printStackTrace();
//			Helper.ToastUtil(context, context.getResources().getText(R.string.settings_dialog_update_download_fail).toString());
			String text = context.getString(R.string.settings_dialog_update_download_fail);
			Log.d("publishExceptionOnMainThread", text);
			Helper.ToastUtil(context, text);
			hasNewVersionDialog.dismiss();
		}
		
		
	}

	@Override
	public void checkUpdateBefore() {
	}
}
