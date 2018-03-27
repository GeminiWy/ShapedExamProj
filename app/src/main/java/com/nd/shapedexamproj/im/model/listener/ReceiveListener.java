package com.nd.shapedexamproj.im.model.listener;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.nd.shapedexamproj.App;
import com.nd.shapedexamproj.db.ChatMsgDao;
import com.nd.shapedexamproj.db.ChatRoomMsgDao;
import com.nd.shapedexamproj.db.ChatUserInfoDao;
import com.nd.shapedexamproj.entity.ChatGroupMsgEntity;
import com.nd.shapedexamproj.entity.ChatMsgEntity;
import com.nd.shapedexamproj.entity.GroupItemInfo;
import com.nd.shapedexamproj.entity.PersonPresenceEntity;
import com.nd.shapedexamproj.im.model.RoomModel;
import com.nd.shapedexamproj.im.packet.RoomMemmberQueryIQ;
import com.nd.shapedexamproj.im.packet.RoomOffilineMsgExtension;
import com.nd.shapedexamproj.im.packet.RoomPacketExtension;
import com.nd.shapedexamproj.im.resource.IMConstants;
import com.nd.shapedexamproj.model.my.PersonalInfo;
import com.nd.shapedexamproj.util.Constants;
import com.nd.shapedexamproj.util.DateUtils;
import com.nd.shapedexamproj.util.StringUtils;
import com.nd.shapedexamproj.util.Utils;
import com.tming.common.net.TmingHttp;
import com.tming.common.net.TmingHttp.RequestCallback;
import com.tming.common.util.Helper;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.packet.*;
import org.jivesoftware.smack.packet.Message.Type;
import org.jivesoftware.smackx.packet.MUCUser;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * @Title: ReceiveModel
 * @Description: IM接受消息监听器
 * @author WuYuLong
 * @date 2014-5-7
 * @version V1.0
 */
public class ReceiveListener implements PacketListener {

    private static final String TAG = ReceiveListener.class.getSimpleName();
    private ChatMsgDao chatMsgDao;
    private ChatRoomMsgDao chatRoomMsgDao;
    private ChatUserInfoDao chatUserInfoDao;
    
    private int page;
    private int pageSize = 20;
    
    Executor executor = Executors.newSingleThreadExecutor();//使用线程池控制消息的顺序
    
    @Override
    public void processPacket(final Packet packet) {
        Log.e(TAG, "ReceiveListener begin");
        executor.execute(new Runnable() {

            public void run() {
                handleIncomingPacket(packet);
            }
        });
    }

    /**
     * 解析消息
     * 
     * @param packet
     */
    private void handleIncomingPacket(Packet packet) {
        /*
         * if (packet instanceof Message) { Log.e(TAG,
         * "handleIncomingPacket begin") ; final Message message =
         * (Message)packet; Log.e(TAG,
         * "ReceiveListener接收到消息 message.getBody():"+message.getBody()) ; }
         */
        if (packet instanceof Message) {
            Log.e(TAG, "rec packet: type-Message");
            final Message message = (Message) packet;
            Type msgType = message.getType();
            String subject = message.getSubject();// 聊天室主题
            if (null == subject) {
                // 单聊
                if (Type.chat == msgType) {
                    chatUserInfoDao = ChatUserInfoDao.getInstance(App.getAppContext());
                    // 判断应用设置
                    ChatMsgEntity chatMsgEntity = null;
                    if (message != null && message.getBody() != null && !"".equals(message.getBody())) {
                        chatMsgDao = ChatMsgDao.getInstance(App.getAppContext());
                        String toId = message.getTo();
                        String fromId = message.getFrom();
                        String msg = message.getBody();
                        String msgId = StringUtils.getRandomString(12);
                        String FromIdarray[] = fromId.split("@");
                        fromId = FromIdarray[0];
                        String ToIdarray[] = toId.split("@");
                        toId = ToIdarray[0];
                        if (fromId.equals(toId)) {
                            // toId = App.getUserId();
                        }
                        Log.e(TAG, "Single Message: toId:" + toId + ";fromId:" + fromId + ";msg:" + msg);
                        String date = DateUtils.getDate();
                        // 插入数据库
                        chatMsgEntity = new ChatMsgEntity();
                        chatMsgEntity.setText(msg);
                        chatMsgEntity.setDate(date);
                        chatMsgEntity.setFormId(fromId);
                        chatMsgEntity.setToId(toId);
                        chatMsgEntity.setMsgId(msgId);
                        saveChatMsg(chatMsgEntity);
                        chatMsgEntity = null;

                        final String msgIdFinal = msgId;
                        final String toIdFinal = toId;
                        final String fromIdFinal = fromId;
                        final String msgFinal = msg;
                        final String dateFinal = date;
                        // 获取用户信息  如果本地数据库存在该用户信息，则不请求网络
                        PersonalInfo mPersonalInfo = chatUserInfoDao.getChatUserInfo(fromId);
                        if (mPersonalInfo != null) {
                            String username = mPersonalInfo.getUserName();
                            sendSingleMsg(msgIdFinal,toIdFinal,username,fromIdFinal,msgFinal,dateFinal);
                            return;
                        }
                        String url = Constants.GET_USER_URL;
                        Map<String, Object> params = new HashMap<String, Object>();
                        params.put("userid", fromId);
                        TmingHttp.asyncRequest(url, params, new RequestCallback<Integer>() {

                            PersonalInfo mPersonalInfo = null;
                            String username = "";

                            @Override
                            public Integer onReqestSuccess(String respones) throws Exception {
                                Log.i(TAG, respones);
                                int result = Utils.jsonParsing(respones);
                                mPersonalInfo = PersonalInfo.personalInfoJSONPasing(respones);
                                return result;
                            }

                            @Override
                            public void success(Integer respones) {
                                if (respones != 1) {
                                    return;
                                } else {
                                    chatUserInfoDao.inSertChatUserInfo(mPersonalInfo);
                                    username = mPersonalInfo.getUserName();
                                    sendSingleMsg(msgIdFinal,toIdFinal,username,fromIdFinal,msgFinal,dateFinal);
                                }
                            }

                            @Override
                            public void exception(Exception exception) {
                                exception.printStackTrace();
                                chatUserInfoDao.inSertChatUserInfo(mPersonalInfo);
                                username = mPersonalInfo.getUserName();
                                sendSingleMsg(msgIdFinal,toIdFinal,username,fromIdFinal,msgFinal,dateFinal);
                            }
                        });
                    }
                } else if (Type.groupchat == msgType) {
                    // 群聊
                    Log.e(TAG, "Room Message.");
                    // 判断应用设置
                    String toId = message.getTo();
                    String from = message.getFrom();
                    String msg = message.getBody();
                    String fromArray[] = from.split("/");
                    String fromRoomJid = "";
                    String fromUserId = "";// 群中的成员的id
                    if (fromArray.length > 0) {
                        fromRoomJid = fromArray[0];
                    }
                    if (fromArray.length > 1) {
                        fromUserId = fromArray[1];
                    }
                    String ToIdarray[] = toId.split("@");
                    toId = ToIdarray[0];
                    String date = DateUtils.getDate();
                    
                    ChatGroupMsgEntity chatMsgEntity = new ChatGroupMsgEntity();
                    chatMsgEntity.setText(msg);
                    chatMsgEntity.setDate(date);
                    chatMsgEntity.setGroupId(fromRoomJid);
                    chatMsgEntity.setToId(toId);
                    chatMsgEntity.setFromUserId(fromUserId);
                    
                    Log.e(TAG, "groupChat: toId:" + toId + ";from:" + from + ";msg:" + msg + ";fromRoomJid:" + fromRoomJid + ";fromUserId:"
                            + fromUserId + ";msg:" + msg + ";date:" + date);
                    
                    dealWithGroupMsg(from, chatMsgEntity);
                }
            } else {
                IMConstants.setUserState(IMConstants.STATE_ONLINE);
                Intent intentS = new Intent();
                intentS.setAction(IMConstants.IM_STATE_ACTION);
                App.getAppContext().sendBroadcast(intentS);
            }
        } else if (packet instanceof Presence) {
            String packetId = packet.getPacketID();
            Log.e(TAG, "rec packet: type-Presence");
            /*
             * <presence to="boy@example1.com/openuniversity_1.0"
             * from="1@conference.example1.com/1000887947"> <x
             * xmlns="http://jabber.org/protocol/muc#user"> <item
             * affiliation="member" role="none"><reason>< /reason> <actor
             * jid="1000887947@example1.com"/> </item><status code="102"/> </x>
             * </presence>
             */
            Presence presence = (Presence) packet;
            if (presence.getType() == Presence.Type.subscribe) {
                // TODO 被请求方 收到请求好友通知
                Log.e(TAG, "from:" + presence.getFrom() + "to:" + presence.getTo());
            } else {}
            Iterator<PacketExtension> iterE = presence.getExtensions().iterator();
            for (; iterE.hasNext();) {
                PacketExtension packetExtension = iterE.next();
                String from = presence.getFrom();
                String toId = presence.getTo();
                String fromArray[] = from.split("/");
                String fromRoomJid = "";
                String fromUserId = "";
                if (fromArray.length > 0) {
                    fromRoomJid = fromArray[0];
                }
                if (fromArray.length > 1) {
                    fromUserId = fromArray[1];
                }
                String ToIdarray[] = toId.split("@");
                toId = ToIdarray[0];
                Log.e(TAG, "Presence :" + packetExtension.toXML());
                
                if (packetExtension instanceof MUCUser) {
                    MUCUser mucUser = (MUCUser) packetExtension;
                    Log.e(TAG, "mucUser getActor:" + mucUser.getItem().getActor());
                    Log.e(TAG, "mucUser getRole:" + mucUser.getItem().getRole());
                    PersonPresenceEntity personPresenceEntity = new PersonPresenceEntity();
                    personPresenceEntity.setUserJId(mucUser.getItem().getActor());
                    personPresenceEntity.setUserNickName(fromUserId);
                    personPresenceEntity.setFrom(from);
                    personPresenceEntity.setFromRoomJid(fromRoomJid);
                    Log.e(IMConstants.ACTION_ROOM_USERINFO_NAME, "Send");
                    Intent intent = new Intent();
                    intent.setAction(IMConstants.ACTION_ROOM_USERINFO_NAME);
                    // intent.putExtra("GroupUserInfo", personPresenceEntity);
                    Helper.sendLocalBroadCast(App.getAppContext(), intent);
                    IMConstants.getRoomPersonInfoList().add(personPresenceEntity);
                } else if (packetExtension instanceof RoomPacketExtension) {
                    // 解析聊天室登录返回的列表
                    if (packetId.startsWith(RoomModel.JOIN_ROOM_HEAD)) {
                        RoomPacketExtension roomPacketExtension = (RoomPacketExtension) packetExtension;
                        List<HashMap<String, Object>> roomList = roomPacketExtension.getGroupItems();
                        ArrayList<GroupItemInfo> groupList = new ArrayList<GroupItemInfo>();
                        int size = roomList.size();
                        for (int i = 0; i < size; i++) {
                            GroupItemInfo groupItemInfo = new GroupItemInfo();
                            String groupName = (String) roomList.get(i).get("name");
                            if (StringUtils.isEmpty(groupName)) {//如果群组名称为空，则不显示
                                continue;
                            }
                            groupItemInfo.setName(groupName);
                            groupItemInfo.setJid(String.valueOf(roomList.get(i).get("grpid")));
                            groupList.add(groupItemInfo);
                            // 获取群离线消息(改成到msgFragment中获取群离线消息)
                            /*new SendGroupMsgModel().getOfflineMsg(groupItemInfo.getJid());*/
                        }
                        if (size > 0) {
                            IMConstants.getGroupList().clear();
                            IMConstants.getGroupList().addAll(groupList);
                        }
                        Intent intent = new Intent(IMConstants.ACTION_ROOM_LIST);
                        Bundle bundle = new Bundle();
                        bundle.putParcelableArrayList("group_list", groupList);
                        intent.putExtras(bundle);
                        // intent.setAction(IMConstants.ACTION_ROOM_LIST);
                        App.getAppContext().sendBroadcast(intent);
                    } 
                } else if (packetExtension instanceof RoomOffilineMsgExtension) {
                    chatRoomMsgDao = ChatRoomMsgDao.getInstance(App.getAppContext());
                    
                    if (packetId.startsWith(RoomModel.OFFLINE_MSG_HEAD)) {
                        RoomOffilineMsgExtension roomPacketExtension = (RoomOffilineMsgExtension) packetExtension;
                        List<HashMap<String, Object>> msgList = roomPacketExtension.getOfflineMsgItems();
                        
                        if (msgList == null || msgList.size() == 0) return;
                        
                        String tag =  getSelfId() +"-"+ fromRoomJid;
                        List<ChatGroupMsgEntity> chatMsgEntities = chatRoomMsgDao.getChatMsgByTag(tag, page, pageSize);

                        for (int i = msgList.size() - 1; i >= 0; i-- ) {
                            String msg = String.valueOf(msgList.get(i).get("body"));
                            long seconds = Long.parseLong((String) msgList.get(i).get("sec"));
                            String date = DateUtils.getDateWithTime(seconds * 1000);
                            String fromUserJid = String.valueOf(msgList.get(i).get("jid"));
                            
                            String fromUserId2 = "";
                            String fromUserArr[] = fromUserJid.split("@");
                            fromUserId2 = fromUserArr[0];
                            
                            ChatGroupMsgEntity chatMsgEntity = new ChatGroupMsgEntity();
                            chatMsgEntity.setText(msg);
                            chatMsgEntity.setDate(date);
                            chatMsgEntity.setGroupId(fromRoomJid);
                            chatMsgEntity.setToId(toId);
                            chatMsgEntity.setFromUserId(fromUserId2);
                            
                            Log.e(TAG, "offlineMsg: toId:" + toId + ";from:" + from + ";msg:" + msg + ";fromRoomJid:" + fromRoomJid + ";fromUserId:"
                                    + fromUserId2 + ";msg:" + msg + ";date:" + date);
                            if (!chatMsgEntities.contains(chatMsgEntity)) {//避免因信息重复推送产生数据重复
                                dealWithGroupMsg(from, chatMsgEntity);
                            }
                        }
                        
                        
                    }
                }
            }
        } else if (packet instanceof IQ) {
            Log.e(TAG, "rec packet: type-IQ");
            String packetId = packet.getPacketID();
            if (packetId.startsWith(RoomModel.ROOM_MEMMBER_HEAD)) {
                // 解析聊天室成员IQ包
                RoomMemmberQueryIQ roomMemmberQueryIQ = (RoomMemmberQueryIQ) packet;
                List<HashMap<String, Object>> roomMemmberList = roomMemmberQueryIQ.getItems();
                List<PersonPresenceEntity> personList = new ArrayList<PersonPresenceEntity>();
                int size = roomMemmberList.size();
                for (int i = 0; i < size; i++) {
                    PersonPresenceEntity entity = new PersonPresenceEntity();
                    entity.setUserJId(String.valueOf(roomMemmberList.get(i).get("jid")));
                    entity.setUserNickName(String.valueOf(roomMemmberList.get(i).get("nick")));
                    personList.add(entity);
                }
                Intent intent = new Intent();
                intent.setAction(IMConstants.ACTION_ROOM_MEMMBER_LIST);
                intent.putExtra("group_memmbers", (Serializable) personList);
                App.getAppContext().sendBroadcast(intent);
            }
        }
        Log.e(TAG, "=============================rec end=============================");
    }
    
    private String getSelfId() {
        return IMConstants.getLoginId();
    }
    /**
     * 
     * <p>处理在线的或离线的群消息</P>
     *
     */
    private void dealWithGroupMsg(String from, ChatGroupMsgEntity chatMsgEntity) {
        chatUserInfoDao = ChatUserInfoDao.getInstance(App.getAppContext());
        // 接收群消息时，如果发现是自己发的消息，就不进行发送广播 以及 再次记录本地数据库
        if (!App.getUserId().equals(chatMsgEntity.getFromUserId())) {
            chatRoomMsgDao = ChatRoomMsgDao.getInstance(App.getAppContext());
            
            saveChatGroupMsg(chatMsgEntity);
            
            final String tmpToId = chatMsgEntity.getToId(); 
            final String tmpFrom = from;
            final String tmpFromRoomJid = chatMsgEntity.getGroupId();
            final String tmpFromUserId = chatMsgEntity.getFromUserId();
            final String tmpMsg = chatMsgEntity.getText();
            final String tmpDate = chatMsgEntity.getDate();
            
            chatMsgEntity = null;
            // 获取用户信息  如果本地数据库存在该用户信息，则不请求网络
            PersonalInfo mPersonalInfo = chatUserInfoDao.getChatUserInfo(tmpFromUserId);
            if (mPersonalInfo != null) {
                String username = mPersonalInfo.getUserName();
                String userimg = mPersonalInfo.getPhotoUrl();
                saveFromUserName(tmpFromUserId, username, userimg);
                
                sendRoomMsg(tmpToId,tmpFrom,tmpFromRoomJid,username,userimg,tmpMsg,tmpDate);
                return;
            }
            
            String url = Constants.GET_USER_URL;
            Map<String, Object> params = new HashMap<String, Object>();
            params.put("userid", tmpFromUserId);
            TmingHttp.asyncRequest(url, params, new RequestCallback<Integer>() {

                PersonalInfo mPersonalInfo = null;
                String username = "", userimg = "";

                @Override
                public Integer onReqestSuccess(String respones) throws Exception {
                    Log.i(TAG, respones);
                    int result = Utils.jsonParsing(respones);
                    mPersonalInfo = PersonalInfo.personalInfoJSONPasing(respones);
                    return result;
                }

                @Override
                public void success(Integer respones) {
                    if (respones != 1) {
                        return;
                    } else {
                        username = mPersonalInfo.getUserName();
                        userimg = mPersonalInfo.getPhotoUrl();
                        saveFromUserName(tmpFromUserId, username, userimg);
                        chatUserInfoDao.inSertChatUserInfo(mPersonalInfo);
                        
                        sendRoomMsg(tmpToId,tmpFrom,tmpFromRoomJid,username,userimg,tmpMsg,tmpDate);
                    }
                }

                @Override
                public void exception(Exception exception) {
                    exception.printStackTrace();
                    username = mPersonalInfo.getUserName();
                    userimg = mPersonalInfo.getPhotoUrl();
                    saveFromUserName(tmpFromUserId, username, userimg);
                    chatUserInfoDao.inSertChatUserInfo(mPersonalInfo);
                    
                    sendRoomMsg(tmpToId,tmpFrom,tmpFromRoomJid,username,userimg,tmpMsg,tmpDate);
                }
            });
        }
    }
    /**
     * <p>发送群聊信息</P>
     * @param toId
     * @param from
     * @param fromRoomJid
     * @param fromUserName
     * @param fromUserImg
     * @param msg
     * @param date
     */
    private void sendRoomMsg(String toId,String from,String fromRoomJid,String fromUserName,String fromUserImg,String msg,String date) {
        Intent intent = new Intent();
        intent.setAction(IMConstants.ACTION_ROOM_RECEIVER_NAME);
        intent.putExtra("toId", toId);
        intent.putExtra("from", from);
        intent.putExtra("fromRoomJid", fromRoomJid);
        intent.putExtra("fromUserName", fromUserName);
        intent.putExtra("fromUserImg", fromUserImg);
        intent.putExtra("msg", msg);
        intent.putExtra("date", date);
        App.getAppContext().sendOrderedBroadcast(intent, null); // 发送广播
    }
    /**
     * <p>发送单聊信息</P>
     *
     * @param toId
     * @param fromName
     * @param fromId
     * @param msg
     * @param date
     */
    private void sendSingleMsg(String msgId,String toId, String fromName, String fromId, String msg, String date) {
        Intent intent = new Intent();
        intent.setAction(IMConstants.ACTION_SINGLE_RECEIVER_NAME);
        intent.putExtra("msgId",msgId);
        intent.putExtra("toId", toId);
        intent.putExtra("fromName", fromName);
        intent.putExtra("fromId", fromId);
        intent.putExtra("msg", msg);
        intent.putExtra("date", date);
        App.getAppContext().sendOrderedBroadcast(intent, null); // 发送广播
    }
    
    /**
     * <p>插入数据库</P>
     *
     * @param chatMsgEntity
     */
    private void saveChatMsg(ChatMsgEntity chatMsgEntity)
    {
        chatMsgEntity.setTag(chatMsgEntity.getToId()+"-"+chatMsgEntity.getFormId());
        chatMsgDao.insertChatMsg(chatMsgEntity);
    }
    /**
     * 
     * <p>插入群聊数据库</P>
     *
     * @param chatMsgEntity
     */
    private void saveChatGroupMsg(ChatGroupMsgEntity chatMsgEntity)
    {
        chatMsgEntity.setTag(chatMsgEntity.getToId()+"-"+chatMsgEntity.getGroupId());
        chatRoomMsgDao.insertChatMsg(chatMsgEntity);
    }
    /**
     * 
     * <p>保存用户名,头像到消息表中</P>
     *
     * @param fromUserId
     * @param fromUserName
     */
    private void saveFromUserName(String fromUserId, String fromUserName, String fromUserImg) {
        if (fromUserId != null && fromUserName != null) {
            ChatGroupMsgEntity entity = new ChatGroupMsgEntity();
            entity.setFromUserId(fromUserId);
            entity.setFromUserName(fromUserName);
            entity.setFormUserImgurl(fromUserImg);
            chatRoomMsgDao.updateChatRoomMsgUserName(entity);
        }
    }
    
}
