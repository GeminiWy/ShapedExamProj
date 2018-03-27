package com.nd.shapedexamproj.im.model.listener;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.nd.shapedexamproj.App;
import com.nd.shapedexamproj.R;
import com.nd.shapedexamproj.activity.im.ChatActivity;
import com.nd.shapedexamproj.db.ChatMsgDao;
import com.nd.shapedexamproj.entity.ChatMsgEntity;
import com.nd.shapedexamproj.im.resource.IMConstants;
import com.nd.shapedexamproj.util.Constants;
import com.nd.shapedexamproj.util.DateUtils;
import com.nd.shapedexamproj.util.SharedPreferUtils;
import com.nd.shapedexamproj.util.StringUtils;
import com.tming.common.util.Log;

public class ChatMsgRecBroadcastReceiver extends BroadcastReceiver {

	private static final String TAG = ChatMsgRecBroadcastReceiver.class
			.getSimpleName();

	private ChatMsgDao chatMsgDao;
	private Context mContext;
	private SharedPreferences spf = null;
	boolean is_notification_open = true;
	boolean is_personal_chat_open = true;
	boolean is_personal_chat_activity_open = false;//是否打开了单聊页面
	/*
	 * IntentFilter intentFilter = new IntentFilter( "IM_MSG_RECEIVER" );
	 * registerReceiver( myBroadcastReceiver , intentFilter);
	 */

	@Override
	public void onReceive(Context context, Intent intent) {

		mContext = context;
		spf = SharedPreferUtils.getSharedPreferences(App.getAppContext());
		is_notification_open = spf.getBoolean(Constants.IS_NOTIFICATION_OPEN, true);
        is_personal_chat_open = spf.getBoolean(Constants.IS_PERSONALCHAT_OPEN, true);
        is_personal_chat_activity_open = spf.getBoolean(Constants.IS_PERSONALCHATACTIVITY_OPEN, false);

		String msgId = intent.getStringExtra("msgId");
		String toId = intent.getStringExtra("toId");
		String fromName = intent.getStringExtra("fromName");
		String fromId = intent.getStringExtra("fromId");
		String msg = intent.getStringExtra("msg");
		String date = intent.getStringExtra("date");
		chatMsgDao = ChatMsgDao.getInstance(mContext);
		
		Log.e(TAG, "msgId" + msgId + "toId:" + toId + ";fromId:" + fromId + ";msg:" + msg
                + ";date:" + date);
		
		if (StringUtils.isEmpty(date)) {
			date = DateUtils.getDate();
		}
		

		ChatMsgEntity chatMsgEntity = new ChatMsgEntity();
		chatMsgEntity.setId(msgId);
		chatMsgEntity.setText(msg);
		chatMsgEntity.setDate(date);
		chatMsgEntity.setFormName(fromName);
		chatMsgEntity.setFormId(fromId);
		chatMsgEntity.setToId(toId);
		
		sendBroadcast(msgId, toId, fromName, fromId, msg, date);
		//saveChatMsg(chatMsgEntity);//改成在receiverFilter里插入数据库
		if ( is_notification_open && is_personal_chat_open) {
		    if (!is_personal_chat_activity_open) {
		        createNotification(chatMsgEntity);
		    } 
		}
		
	}

	/**
     * 
     * <p>单聊统一由此发送广播</P>
     *
     * @param
     */
    private void sendBroadcast(String msgId,String toId, String fromName, String fromId, String msg, String date) {
        Intent intent = new Intent();
        intent.setAction(IMConstants.ACTION_SINGLE_RECEIVER_SECOND);
		intent.putExtra("msgId",msgId);
        intent.putExtra("toId", toId);
        intent.putExtra("fromName", fromName);
        intent.putExtra("fromId", fromId);
        intent.putExtra("msg", msg);
        intent.putExtra("date", date);
        mContext.sendOrderedBroadcast(intent, null);
    }

	private void createNotification(ChatMsgEntity chatMsgEntity)
	{
		//消息通知栏
        //定义NotificationManager

        String ns = Context.NOTIFICATION_SERVICE;

        NotificationManager mNotificationManager = (NotificationManager)mContext.getSystemService(ns);

        //定义通知栏展现的内容信息
        long when = System.currentTimeMillis();
//        Notification notification = new Notification();
		Notification.Builder nbuilder = new Notification.Builder(mContext);
		nbuilder.setSmallIcon(R.drawable.tips_ico);
		nbuilder.setTicker(chatMsgEntity.getText());
		nbuilder.setWhen(when);
//        notification.icon = R.drawable.tips_ico;
		// // 这个参数是通知提示闪出来的值.
//		notification.tickerText = chatMsgEntity.getText();
//		notification.when = when;
//		notification.flags = Notification.FLAG_AUTO_CANCEL;

        
        //定义下拉通知栏时要展现的内容信息
        Context context = mContext.getApplicationContext();
        //CharSequence contentTitle = chatMsgEntity.getFormId();
        CharSequence contentTitle = chatMsgEntity.getFormName();
        CharSequence contentText = chatMsgEntity.getText();
        String tag = chatMsgEntity.getFormId();
        String name = contentTitle.toString();

        Log.e(TAG, "==tag:" + chatMsgEntity.getFormId() + ";name:" + name);
        Intent notificationIntent = new Intent(context, ChatActivity.class);
        notificationIntent.putExtra("tag", tag);
        notificationIntent.putExtra("toUserName",name);
        notificationIntent.putExtra("toUserid",chatMsgEntity.getFormId());
        

        PendingIntent contentIntent = PendingIntent.getActivity(context, 1,notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

//        notification.setLatestEventInfo(context, contentTitle, contentText,contentIntent);

		nbuilder.setContentTitle(contentTitle);
		nbuilder.setContentText(contentText);
		nbuilder.setContentIntent(contentIntent);

		Notification notification = nbuilder.build();
		notification.flags = Notification.FLAG_AUTO_CANCEL;

        //用mNotificationManager的notify方法通知用户生成标题栏消息通知
        mNotificationManager.notify(1, notification);
	}
}
