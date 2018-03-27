package com.nd.shapedexamproj.im.model;

import android.content.Context;

import com.nd.shapedexamproj.im.manager.IMManager;
import com.nd.shapedexamproj.im.manager.SessionManager;
import com.nd.shapedexamproj.im.manager.UserManager;
import com.nd.shapedexamproj.im.manager.plugin.GB2Alpha;
import com.tming.common.util.Log;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.RosterGroup;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smackx.ServiceDiscoveryManager;
import org.jivesoftware.smackx.muc.HostedRoom;
import org.jivesoftware.smackx.packet.DiscoverItems;
import org.jivesoftware.smackx.packet.DiscoverItems.Item;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
/**
 * @Title: RosterModel
 * @Description: IM花名册模块
 * @author WuYuLong
 * @date 2014-5-7
 * @version V1.0
 */
public class RosterModel {
	public static final int RESULT_SUCCESS = 0, RESULT_ERROR_ADD = 1,
			RESULT_ERROR_EXIST = 2, RESULT_ERROR_UNDEFINE = 3, RESULT_ERROR_SELF = 4;
	
	private final String TAG = "RosterModel";
	private GB2Alpha alphaUtil = new GB2Alpha();
	private HashMap<String, String> rosterMap;
    
	/**
	 * 获取花名册列表
	 * @param context
	 * @return
	 */
	public List<HashMap<String, String>> getRoster(Context context) {
		List<HashMap<String, String>> rosterList = new ArrayList<HashMap<String, String>>();
		
		final SessionManager sessionManager = IMManager.getSessionManager();

		//获取链接
		try {
			if(sessionManager.getConnection() != null){
				//连接丢失,重新连接
				if(!sessionManager.getConnection().isConnected()){
					UserManager.userLogin(context, false);
				}
				
				Roster roster = sessionManager.getConnection().getRoster();
				
				if (roster != null) {
				    //smark底层的bug，roster.getEntries()获取的值跟实际的值不一样，所以需要使用反射机制获取roster中的Collection<RosterEntry>，
				    Field entriesField = roster.getClass().getDeclaredField("entries");
				    entriesField.setAccessible(true);
				    ConcurrentHashMap<String, RosterEntry> entries = (ConcurrentHashMap<String, RosterEntry>) entriesField.get(roster);
				    Collection<RosterEntry> rosterEntries = entries.values();
					for (final RosterEntry rosterEntry : rosterEntries) {
						
						if (rosterEntry.getUser() != null ) {
						    if (rosterEntry.getName() != null) {//smark本身bug，A加B为好友，如果A已经在B的好友列表里，则rosterEntry.getName()会等于null。
						        rosterMap = new HashMap<String, String>();
						        rosterMap.put("userid", getJid(rosterEntry.getUser()));
    						    rosterMap.put("name",  rosterEntry.getName());
    						    //转拼音首字母
    						    rosterMap.put("sortKey",  alphaUtil.String2Alpha(rosterEntry.getName()));
    						    rosterMap.put("type",  rosterEntry.getType().toString());
                                rosterList.add(rosterMap);
    						    
						    } 
						}
					} 
					
					//首字母缩写排序
					Collections.sort(rosterList, new Comparator<HashMap<String, String>>() {
						public int compare(HashMap<String, String> o1, HashMap<String, String> o2) {
			            	return o1.get("sortKey").compareTo(o2.get("sortKey"));
						}
					});
				}
			}
		} catch (Exception e) {
			rosterList = null;
			e.printStackTrace();
		}
		Log.e(TAG, "rosterList.size() = " + rosterList.size());
		return rosterList;
	}
	
	/**
	 * 切割获取用户id
	 * @param user
	 * @return
	 */
	private String getJid(String user){
		String userId = "";
		if(!"".equals(user)){
			userId = user.substring(0, user.lastIndexOf("@"));
		}
		return userId;
	}
	
    /**
     * 花名册增加联系人
     * @param context
     * @param jid
     * @param name
     * @return
     */
	public int addRoster(Context context, String jid, String name) {

		int result = RESULT_ERROR_UNDEFINE;
		boolean isExist = false;
		final SessionManager sessionManager = IMManager.getSessionManager();
		if (sessionManager == null) {
		    return RESULT_ERROR_ADD;
		}
		//连接丢失,重新连接
		if(!sessionManager.getConnection().isConnected()){
			UserManager.userLogin(context, false);
		}
		
		//判断是不是是否已经存在好友列表里
		Roster roster = sessionManager.getConnection().getRoster();
		if(roster != null){
			for (Iterator<RosterEntry> i = roster.getEntries().iterator(); i.hasNext(); ) { 
				RosterEntry rosterEntry = i.next();
				if(rosterEntry.getUser() != null && rosterEntry.getName() != null){
					if(jid.equals(rosterEntry.getUser())){
						isExist = true;
						break;
					}
				}
			}
		}

		if (!isExist) {
			try {
				sessionManager.getConnection().getRoster()
						.createEntry(jid, name, null);
				result = RESULT_SUCCESS;
			} catch (XMPPException e) {
				result = RESULT_ERROR_ADD;
				e.printStackTrace();
			} catch (Exception ex) {
				result = RESULT_ERROR_ADD;
				ex.printStackTrace();
			}
		} else {
			result = RESULT_ERROR_EXIST;
		}

		return result;
	}
	
	/** 
     * 获取所有分组
     *  
     * @return 所有分组 
     */ 
	public List<RosterGroup> getGroups(Context context) {  
		final SessionManager sessionManager = IMManager.getSessionManager();
    	
		if (sessionManager.getConnection() == null){
			return null;
		} else {
			//连接丢失,重新连接
			if(!sessionManager.getConnection().isConnected()){
				UserManager.userLogin(context, false);
			}
			List<RosterGroup> grouplist = new ArrayList<RosterGroup>();  
			Collection<RosterGroup> rosterGroup = sessionManager.getConnection().getRoster().getGroups();  
			Iterator<RosterGroup> i = rosterGroup.iterator();  
			while (i.hasNext()) {  
				grouplist.add(i.next());  
			}  
			return grouplist;
		}
    }
    
    /** 
     * 初始化会议室列表 
     */ 
    public List<HostedRoom> getHostRooms(Context context, String serverName) {
    	final SessionManager sessionManager = IMManager.getSessionManager();
    	
    	if (sessionManager.getConnection() == null){
            return null;
    	} else {
			//连接丢失,重新连接
			if(!sessionManager.getConnection().isConnected()){
				UserManager.userLogin(context, false);
			}
	        Collection<HostedRoom> hostrooms = null;  
	        List<HostedRoom> roominfos = new ArrayList<HostedRoom>();  
	        try {  
	            new ServiceDiscoveryManager(sessionManager.getConnection());  
	            //hostrooms = MultiUserChat.getHostedRooms(sessionManager.getConnection(), serverName);  
	               ServiceDiscoveryManager serviceDiscoveryManager = ServiceDiscoveryManager.getInstanceFor(sessionManager.getConnection());
	                DiscoverItems discoverItems = serviceDiscoveryManager.discoverItems(serverName);
	                Iterator<Item> items =  discoverItems.getItems();
	                List<HostedRoom> hostedRooms = new ArrayList<HostedRoom>();
	                while (items.hasNext()) {
	                    Item item = items.next();
	                    roominfos.add(new HostedRoom(item));
                        Log.e(TAG, "Get Jid:"+item.getEntityID());
	                }
	            
	            /*for (HostedRoom entry : hostrooms) {  
	                roominfos.add(entry);  
	                Log.i("room",  "名字：" + entry.getName() + " - ID:" + entry.getJid());  
	            }  */
	            Log.i("room", "服务会议数量:" + roominfos.size());  
	        } catch (XMPPException e) {
	        	roominfos = null;
	        	Log.e(TAG, e.getMessage());
	        } catch (Exception e) {  
	        	roominfos = null;
	        	Log.e(TAG, e.getMessage());
	        }
	        return roominfos;
    	}
    }
}
