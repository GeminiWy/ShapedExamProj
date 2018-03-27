package com.nd.shapedexamproj.im.task;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;

import com.nd.shapedexamproj.App;
import com.nd.shapedexamproj.im.model.LoginModel;
import com.nd.shapedexamproj.im.model.RoomModel;
import com.nd.shapedexamproj.im.resource.IMConstants;
import com.tming.common.util.Helper;
import com.tming.common.util.Log;

public class LoginAsyncTask extends AsyncTask<String, Integer, Integer> {

	private static final String TAG = "LoginAsyncTask";

	private Context mContext;
	private LoginModel mLoginModel;
	private String userid;
	private String pwd;
	private boolean mIsGetOffLineMsg;

	public LoginAsyncTask(Context context, LoginModel loginModel, boolean isGetOffLineMsg) {
		mContext = context;
		mLoginModel = loginModel;
		userid = App.getUserId();
		pwd = IMConstants.passWord_t0;
//		pwd = getMD5Str(userid);
		mIsGetOffLineMsg = isGetOffLineMsg;//判断是否获取离线消息
	}

	private String getMD5Str(String userId) {
	    Log.e(TAG, "加密前：" + userId + ":" + IMConstants.areaServerName + ":password");
	    String result = Helper.getMD5String(userId + ":" + IMConstants.areaServerName + ":password");
	    Log.e(TAG, "加密后：" + result);
	    return result;
	}
	
	@Override
	protected Integer doInBackground(String... arg0) {
		Log.e(TAG, "doInBackground begin");
		int loginState = mLoginModel.login(userid, pwd, mIsGetOffLineMsg);
		return loginState;
	}

	@Override
	protected void onPostExecute(Integer result) {
		Log.e(TAG, "onPostExecute result:"+result);
		int state = IMConstants.STATE_OFFLINE;
		if (LoginModel.LOGIN_STATE_SUCCESS == result) {
			state = IMConstants.STATE_ONLINE;
			
			IMConstants.setUserState(state);
			Intent intent = new Intent();
			intent.setAction(IMConstants.IM_STATE_ACTION);
			mContext.sendBroadcast(intent);
			
			//登录聊天室，并获取群列表
	        RoomModel roomModel = new RoomModel();
	        roomModel.sendJoinRoomRequest();
			
		} else if(LoginModel.LOGIN_STATE_PWD_ERROR == result) {
			state = IMConstants.STATE_OFFLINE;
			//UIHelper.ToastMessage(mContext, R.string.msg_connect_pwd_error);
		} else if (LoginModel.LOGIN_STATE_ERROR == result) {
			state = IMConstants.STATE_OFFLINE;
			//UIHelper.ToastMessage(mContext, R.string.msg_connect_error);
		} else {
			state = IMConstants.STATE_OFFLINE;
		}
		IMConstants.setUserState(state);
		Intent intent = new Intent();
		intent.setAction(IMConstants.IM_STATE_ACTION);
		mContext.sendBroadcast(intent);
		super.onPostExecute(result);
	}

	@Override
	protected void onPreExecute() {
		/*IMConstants.setUserState(IMConstants.STATE_LINKING);
		Intent intent = new Intent();
		intent.setAction(IMConstants.IM_STATE_ACTION);
		mContext.sendBroadcast(intent);*/
		super.onPreExecute();
	}

	@Override
	protected void onProgressUpdate(Integer... values) {
		super.onProgressUpdate(values);
	}

}
