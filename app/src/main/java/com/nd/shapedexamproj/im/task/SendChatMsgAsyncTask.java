package com.nd.shapedexamproj.im.task;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.nd.shapedexamproj.entity.ChatMsgEntity;
import com.nd.shapedexamproj.im.model.SendModel;

public class SendChatMsgAsyncTask extends AsyncTask<Integer, Integer, Boolean> {

	private static final String TAG = "SendChatMsgAsyncTask";

	private Context mContext;
	private SendModel mSendModel;
	private ChatMsgEntity mChatMsgEntity;
	private OnSendMsgListener mListener;
	
	public SendChatMsgAsyncTask(Context context, OnSendMsgListener listener, SendModel sendModel)
	{
		mContext = context;
		mSendModel = sendModel;
		mListener = listener;
	}

	public void setChatMsgEntity(ChatMsgEntity chatMsgEntity)
	{
		mChatMsgEntity = chatMsgEntity;
	}

	
	@Override
	protected Boolean doInBackground(Integer... arg0) {
		Boolean result = false;
		if(null != mChatMsgEntity)
		{
			String IMFromId = mChatMsgEntity.getFormId();
			String IMToId = mChatMsgEntity.getToId();
			result = mSendModel.sendMessage(mChatMsgEntity.getText(), IMFromId, IMToId);
		}
		Log.e(TAG, "result:"+result);
		return result;
	}

	@Override
	protected void onPostExecute(Boolean result) {
		super.onPostExecute(result);

		if (null != mListener) {
			mListener.onSendMsgFinish(result, mChatMsgEntity);
		}
		

	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
	}

	@Override
	protected void onProgressUpdate(Integer... values) {
		super.onProgressUpdate(values);
	}
	
	public interface OnSendMsgListener {
		
		void onSendMsgFinish(Boolean result, ChatMsgEntity chatMsgEntity);
	}
	
}
