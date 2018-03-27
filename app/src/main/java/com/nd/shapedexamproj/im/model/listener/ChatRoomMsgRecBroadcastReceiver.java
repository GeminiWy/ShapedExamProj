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
import com.nd.shapedexamproj.activity.im.ChatGroupActivity;
import com.nd.shapedexamproj.db.ChatRoomMsgDao;
import com.nd.shapedexamproj.entity.ChatGroupMsgEntity;
import com.nd.shapedexamproj.entity.GroupItemInfo;
import com.nd.shapedexamproj.im.resource.IMConstants;
import com.nd.shapedexamproj.util.Constants;
import com.nd.shapedexamproj.util.DateUtils;
import com.nd.shapedexamproj.util.SharedPreferUtils;
import com.nd.shapedexamproj.util.StringUtils;
import com.tming.common.util.Log;

public class ChatRoomMsgRecBroadcastReceiver extends BroadcastReceiver {

	private static final String TAG = ChatRoomMsgRecBroadcastReceiver.class
			.getSimpleName();

	private ChatRoomMsgDao chatRoomMsgDao;
	private Context mContext;
	private SharedPreferences spf = null;
	boolean is_notification_open = true;
	boolean is_group_chat_open = true;
	boolean is_group_chat_activity_open = false;//是否打开了群聊界面
	/*
	 * IntentFilter intentFilter = new IntentFilter( "IM_MSG_RECEIVER" );
	 * registerReceiver( myBroadcastReceiver , intentFilter);
	 */

	@Override
	public void onReceive(Context context, Intent intent) {

		mContext = context;
		spf = SharedPreferUtils.getSharedPreferences(App.getAppContext());
		is_notification_open = SharedPreferUtils.getSharedPreferences(App.getAppContext()).getBoolean(Constants.IS_NOTIFICATION_OPEN, true);
		is_group_chat_open = spf.getBoolean(Constants.IS_GROUPCHAT_OPEN, true);
		is_group_chat_activity_open = spf.getBoolean(Constants.IS_GROUPCHATACTIVITY_OPEN, false);
		
		String toId = intent.getStringExtra("toId");
		String from = intent.getStringExtra("from");
		String msg = intent.getStringExtra("msg");
		String date = intent.getStringExtra("date");
		
		String fromRoomJid = intent.getStringExtra("fromRoomJid");
		String fromUserName = intent.getStringExtra("fromUserName");
		String fromUserImg = intent.getStringExtra("fromUserImg");
		if (StringUtils.isEmpty(date)) {
			date = DateUtils.getDate();
		}
		chatRoomMsgDao = ChatRoomMsgDao.getInstance(mContext);
		
		Log.e(TAG, "toId:" + toId + ";from:" + from + ";msg:" + msg
				+ ";date:" + date + ",fromUserImg:" + fromUserImg);

		ChatGroupMsgEntity chatMsgEntity = new ChatGroupMsgEntity();//TODO 添加群名称
		chatMsgEntity.setText(msg);
		chatMsgEntity.setDate(date);
		chatMsgEntity.setGroupId(fromRoomJid);
		chatMsgEntity.setToId(toId);
		chatMsgEntity.setFromUserId(fromUserName);
		chatMsgEntity.setFormUserImgurl(fromUserImg);
		
		sendBroadcast( toId, from, fromRoomJid, fromUserName, msg, date, fromUserImg);
//		saveChatMsg(chatMsgEntity);//改成在ReceiverFilter中插入数据库
		if (is_notification_open && is_group_chat_open) {
		    if (!is_group_chat_activity_open) {
		        createNotification(chatMsgEntity);
		    }
		}
		
	}
	
	/**
     * 
     * <p>群聊聊统一由此发送广播</P>
     *
     * @param
     */
    private void sendBroadcast(ChatGroupMsgEntity chatGroupMsgEntity) {
        Intent intent = new Intent();
        intent.setAction(IMConstants.ACTION_ROOM_RECEIVER_SECOND);
        intent.putExtra("chatMsgEntity", chatGroupMsgEntity);
        mContext.sendBroadcast(intent);
    }
    /**
     * 
     * <p>群聊统一由此发送广播</P>
     *
     * @param
     */
    private void sendBroadcast(String toId, String from, String fromRoomJid, String fromUserName, String msg, String date, String fromUserImg) {
        Intent intent = new Intent();
        intent.setAction(IMConstants.ACTION_ROOM_RECEIVER_SECOND);
        intent.putExtra("toId", toId);
        intent.putExtra("from", from);
        intent.putExtra("fromRoomJid", fromRoomJid);
        intent.putExtra("fromUserName", fromUserName);
        intent.putExtra("fromUserImg", fromUserImg);
        intent.putExtra("msg", msg);
        intent.putExtra("date", date);
        mContext.sendOrderedBroadcast(intent, null);
    }
	private void saveChatMsg(ChatGroupMsgEntity chatMsgEntity)
	{
		chatMsgEntity.setTag(chatMsgEntity.getToId()+"-"+chatMsgEntity.getGroupId());
		chatRoomMsgDao.insertChatMsg(chatMsgEntity);
	}
	
	private void createNotification(ChatGroupMsgEntity chatMsgEntity)
	{
		//TODO 因为版本问题，将notification修改为用builder配置
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
        CharSequence contentTitle = "";
        CharSequence contentText = chatMsgEntity.getText();
        
        GroupItemInfo groupItemInfo = IMConstants.findGroupItemInfo(chatMsgEntity.getGroupId());
        if (null != groupItemInfo) {
            if (StringUtils.isEmpty(groupItemInfo.getName())) {//如果群组名称为空则不显示通知
                return;
            }
        	contentTitle = groupItemInfo.getName();
        } else {
            Log.e(TAG, "==群信息为空==");
            return;
        }
        
        Intent notificationIntent = new Intent(context, ChatGroupActivity.class);
        notificationIntent.putExtra("roomJid", chatMsgEntity.getGroupId());
        notificationIntent.putExtra("roomName",groupItemInfo.getName());

        PendingIntent contentIntent = PendingIntent.getActivity(context, 1,notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);


        nbuilder.setContentTitle(contentTitle);
        nbuilder.setContentText(contentText);
        nbuilder.setContentIntent(contentIntent);
//        notification.setLatestEventInfo(context, contentTitle, contentText,contentIntent);

        Notification notification = nbuilder.build();
        notification.flags = Notification.FLAG_AUTO_CANCEL;

        //用mNotificationManager的notify方法通知用户生成标题栏消息通知
        mNotificationManager.notify(1, notification);
	}
}
