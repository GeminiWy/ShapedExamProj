package com.nd.shapedexamproj.im.model.filter;

import android.util.Log;

import com.nd.shapedexamproj.db.ChatMsgDao;
import com.nd.shapedexamproj.db.ChatRoomMsgDao;

import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.packet.Packet;
/**
 * @Title: ReceiveFilter
 * @Description: IM接受消息过滤器
 * @author WuYuLong
 * @date 2014-5-7
 * @version V1.0
 */
public class ReceiveFilter implements PacketFilter {
	
	private static final String TAG = ReceiveFilter.class.getSimpleName();
	/*private String toId = "", fromId = "", msg = "", date = "";*/
	private ChatMsgDao chatMsgDao;
	private ChatRoomMsgDao chatRoomMsgDao;
	
    @Override
    public boolean accept(Packet packet) {
    	Log.e(TAG, "rec packet:" +packet.toXML());
    	return true;
        //System.out.println("Activity----accept" + packet.toXML());
    	
    	/*if(packet instanceof Message) {
    		Log.e(TAG, "rec packet: type-Message");
    		final Message message = (Message)packet;
        	
        	Type msgType = message.getType();
        	String subject = message.getSubject();//聊天室主题

			if ( null == subject) {
				//单聊
	        	if(Type.chat == msgType) {
	        		//判断应用设置
	        		if(message != null && message.getBody() != null && !"".equals(message.getBody())){
	        		    
	        		    chatMsgDao = ChatMsgDao.getInstance(App.getAppContext());
	        			Log.e(TAG, "Single Message.");
	        	    	Log.e(TAG, "message.getFrom():" + message.getFrom()) ;
	        	    	Log.e(TAG, "message.getTo():" + message.getTo()) ;
	        	        Log.e(TAG, "message.getBody():" + message.getBody()) ;
	     
	        	        String toId = message.getTo();
	        	        String fromId = message.getFrom();
	        	        String msg = message.getBody();
	        			
	        			String FromIdarray[]= fromId.split("@");
	        			fromId = FromIdarray[0];
	        			
	        			String ToIdarray[]= toId.split("@");
	        			toId = ToIdarray[0];
	        			if(fromId.equals(toId))
	        			{
	        				//toId = App.getUserId();
	        			}
	        			Log.e(TAG, "toId:" + toId + ";fromId:" + fromId + ";msg:" + msg);
	        			
	        			String date = DateUtils.getDate();
	        			
	        			//插入数据库
                        ChatMsgEntity chatMsgEntity = new ChatMsgEntity();
                        chatMsgEntity.setText(msg);
                        chatMsgEntity.setDate(date);
//                        chatMsgEntity.setFormName("");//TODO 换到其他地方插
                        chatMsgEntity.setFormId(fromId);
                        chatMsgEntity.setToId(toId); 
                        saveChatMsg(chatMsgEntity);
	        			
	        			final String toIdFinal = toId;
	        			final String fromIdFinal = fromId;
	        			final String msgFinal = msg;
	        			final String dateFinal = date;
	        			
	        			//获取用户信息
	        			String url = Constants.GET_USER_URL;
	        			Map<String ,Object> params = new HashMap<String,Object>();
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
	        					if(respones != 1){
	        						return;
	        					} else {
	        					    username = mPersonalInfo.getUserName();
	        					    
	        	        	        Intent intent = new Intent();
	        	        			intent.setAction(IMConstants.ACTION_SINGLE_RECEIVER_NAME);
	        	        			intent.putExtra("toId", toIdFinal);
	        	        			intent.putExtra("fromName", username);
	        	        			intent.putExtra("fromId", fromIdFinal);
	        	        			intent.putExtra("msg", msgFinal);
	        	        			intent.putExtra("date", dateFinal);
	        	        			App.getAppContext().sendOrderedBroadcast(intent, null);   //发送广播
	        					}
	        				}

	        				@Override
	        				public void exception(Exception exception) {
	        					exception.printStackTrace();
	        					
	        					Intent intent = new Intent();
        	        			intent.setAction(IMConstants.ACTION_SINGLE_RECEIVER_NAME);
        	        			intent.putExtra("toId", toIdFinal);
        	        			username = mPersonalInfo.getUserName();
        	        			intent.putExtra("fromName", username);
        	        			intent.putExtra("fromId", fromIdFinal);
        	        			intent.putExtra("msg", msgFinal);
        	        			intent.putExtra("date", dateFinal);
        	        			App.getAppContext().sendOrderedBroadcast(intent, null);   //发送广播
	        				}
	        				
	        			});
	        			
	            	}
	        	} else if(Type.groupchat == msgType){
	        		//群聊
	        		Log.e(TAG, "Room Message.");
	        		//判断应用设置
	        		String toId = message.getTo();
	    			String from = message.getFrom();
	    			String msg = message.getBody();
	        		
	    			String fromArray[] = from.split("/");
	    			String fromRoomJid = "";
	    			String fromRoomPersonnelJid = "";//群中的成员的id
	    			String fromUserName = "";
	    			if(fromArray.length > 0) {
	    				fromRoomJid = fromArray[0];
	    				fromRoomPersonnelJid = fromArray[1]; 
	    			}
	    			
	    			if(fromArray.length > 1) {
	    				fromUserName = fromArray[1];
	    			}
	    			
	    			String ToIdarray[]= toId.split("@");
	    			toId = ToIdarray[0];
	    			
	    			String date = DateUtils.getDate();
	    			Log.e(TAG, "toId:" + toId + ";from:" + from + ";msg:" + msg);
	    			
	    			//接收群消息时，如果发现是自己发的消息，就不进行发送广播 以及 再次记录本地数据库
	    			if(!Constants.USER_ID.equals(fromRoomPersonnelJid)){
	    			    chatRoomMsgDao = ChatRoomMsgDao.getInstance(App.getAppContext());
	    			    
	    			    ChatGroupMsgEntity chatMsgEntity = new ChatGroupMsgEntity();
	    		        chatMsgEntity.setText(msg);
	    		        chatMsgEntity.setDate(date);
	    		        chatMsgEntity.setGroupId(fromRoomJid);
	    		        chatMsgEntity.setToId(toId);
	    		        chatMsgEntity.setFromUserId(fromUserName);
	    			    
	    		        saveChatGroupMsg(chatMsgEntity);
	    			    
		        		Intent intent = new Intent();
		     			intent.setAction(IMConstants.ACTION_ROOM_RECEIVER_NAME);
		     			intent.putExtra("toId", toId);
		     			intent.putExtra("from", from);
		     			intent.putExtra("fromRoomJid", fromRoomJid);
		     			intent.putExtra("fromUserName", fromUserName);
		     			intent.putExtra("msg", msg);
		     			intent.putExtra("date", date);
		     			App.getAppContext().sendOrderedBroadcast(intent, null);   //发送广播
	    			}
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
    		
    		<presence to="boy@example1.com/openuniversity_1.0" from="1@conference.example1.com/1000887947">
				<x xmlns="http://jabber.org/protocol/muc#user">
					<item affiliation="member" role="none"><reason><
						/reason>
						<actor jid="1000887947@example1.com"/>
					</item><status code="102"/>
				</x>
			</presence>
    		Presence presence = (Presence)packet;
    		 
    		if (presence.getType() == Presence.Type.subscribe) {
    		    //TODO 被请求方 收到请求好友通知
    		    Log.e(TAG, "from:" + presence.getFrom() + "to:" + presence.getTo());
    		} else {
    		    
    		}
    		
    		
    		Iterator<PacketExtension> iterE  = presence.getExtensions().iterator();
    		for(; iterE.hasNext(); )
    		{
    			PacketExtension packetExtension = iterE.next();
    			String from = presence.getFrom();
    			String to = presence.getTo();
    			
    			String fromArray[] = from.split("/");
    			String fromRoomJid = "";
    			String fromUserName = "";
    			if(fromArray.length > 0) {
    				fromRoomJid = fromArray[0];
    			}
    			
    			if(fromArray.length > 1) {
    				fromUserName = fromArray[1];
    			}
    			
    			String toId = "";
    			String ToIdarray[]= toId.split("@");
    			toId = ToIdarray[0];
    			
    			Log.e(TAG, "Presence :"+ packetExtension.toXML());//<x..>... </x>
    			Log.e(TAG, "Presence getNamespace:"+ packetExtension.getNamespace());//xmlns
    			Log.e(TAG, "Presence getElementName:"+ packetExtension.getElementName());//x
    			if (packetExtension instanceof MUCUser) {
    				MUCUser mucUser = (MUCUser)packetExtension;
        			Log.e(TAG, "mucUser getActor:"+ mucUser.getItem().getActor());
        			Log.e(TAG, "mucUser getRole:"+ mucUser.getItem().getRole());
        			
        			PersonPresenceEntity personPresenceEntity = new PersonPresenceEntity();
        			personPresenceEntity.setUserJId(mucUser.getItem().getActor());
        			personPresenceEntity.setUserNickName(fromUserName);
        			personPresenceEntity.setFrom(from);
        			personPresenceEntity.setFromRoomJid(fromRoomJid);
        			
        			Log.e(IMConstants.ACTION_ROOM_USERINFO_NAME, "Send");
        			Intent intent = new Intent();
        			intent.setAction(IMConstants.ACTION_ROOM_USERINFO_NAME);
        			//intent.putExtra("GroupUserInfo",  personPresenceEntity);
        			Helper.sendLocalBroadCast(App.getAppContext(), intent);
        			
        			IMConstants.getRoomPersonInfoList().add(personPresenceEntity);
    			} else if(packetExtension instanceof RoomPacketExtension){
    				//解析聊天室登录返回的列表
    				if(packetId.startsWith(RoomModel.JOIN_ROOM_HEAD)){
    					RoomPacketExtension roomPacketExtension = (RoomPacketExtension)packetExtension;
    					List<HashMap<String, Object>> roomList = roomPacketExtension.getGroupItems();
    					ArrayList<GroupItemInfo> groupList = new ArrayList<GroupItemInfo>();
    					
    					int size = roomList.size();
    					for (int i = 0; i < size; i++) {
    					    GroupItemInfo groupItemInfo = new GroupItemInfo();
    					    groupItemInfo.setName(String.valueOf(roomList.get(i).get("name")));
    					    groupItemInfo.setJid(String.valueOf(roomList.get(i).get("grpid")));
    					    groupList.add(groupItemInfo);
    					    
    					    //获取离线消息
//    			            new SendGroupMsgModel().getOfflineMsg(groupItemInfo.getJid());
						}
    					
    					if (size > 0) {
    					    IMConstants.getGroupList().clear();
    					    IMConstants.getGroupList().addAll(groupList);
    					}
    					
    					Intent intent = new Intent(IMConstants.ACTION_ROOM_LIST);
    					Bundle bundle = new Bundle();
    					bundle.putParcelableArrayList("group_list", groupList);
    					intent.putExtras(bundle);
//    					intent.setAction(IMConstants.ACTION_ROOM_LIST);
    					App.getAppContext().sendBroadcast(intent);
    				} else if (packetId.startsWith(RoomModel.OFFLINE_MSG_HEAD)) {
    				    RoomPacketExtension roomPacketExtension = (RoomPacketExtension)packetExtension;
                        List<HashMap<String, Object>> msgList = roomPacketExtension.getOfflineMsgItems();
                        
                        for (int i = 0;i < msgList.size();i ++) {
                            ChatGroupMsgEntity entity = new ChatGroupMsgEntity();
                            entity.setText(String.valueOf(msgList.get(i).get("body")));
                            
                            String timeStamp = String.valueOf(msgList.get(i).get("sec"));
                            entity.setDate(DateUtils.getDateWithTime(Long.parseLong(timeStamp)));
                            entity.setGroupId(fromRoomJid);
                            entity.setToId(String.valueOf(msgList.get(i).get("jid")));
                            entity.setFromUserId(fromUserName);
                            
                            saveChatGroupMsg(entity);
                        }
                        
                        Intent intent = new Intent();
                        intent.setAction(IMConstants.ACTION_OFFLINE_ROOM_MSG);
                        intent.putExtra("offline_msg", (Serializable)msgList);
                        App.getAppContext().sendBroadcast(intent, null);
    				}
    			}
    		
    			
    			
    		}

    		
    	} else if(packet instanceof IQ) {
    		Log.e(TAG, "rec packet: type-IQ");
    		String packetId = packet.getPacketID();
    		
			if(packetId.startsWith(RoomModel.ROOM_MEMMBER_HEAD)){
				//解析聊天室成员IQ包
				RoomMemmberQueryIQ roomMemmberQueryIQ = (RoomMemmberQueryIQ)packet;
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
				intent.putExtra("group_memmbers", (Serializable)personList);
				App.getAppContext().sendBroadcast(intent);
			}
    		
    	}
    	
    	
    	
    	Log.e(TAG, "=============================rec end=============================");
        return true;*/
    }
    
}
