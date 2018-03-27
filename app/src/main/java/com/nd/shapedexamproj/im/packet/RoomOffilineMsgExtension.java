package com.nd.shapedexamproj.im.packet;

import com.tming.common.util.Log;
import org.jivesoftware.smack.packet.PacketExtension;
import org.jivesoftware.smack.provider.PacketExtensionProvider;
import org.xmlpull.v1.XmlPullParser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * 
 * <p> 群离线消息</p>
 * <p>Created by zll on 2014-8-16</p>
 */
public class RoomOffilineMsgExtension implements PacketExtension{
    
    private static final String TAG = "RoomOffilineMsgExtension";
    
    private List<HashMap<String, Object>> offlineMsgItems = new ArrayList<HashMap<String, Object>>();
    
    public List<HashMap<String, Object>> getOfflineMsgItems() {
        return offlineMsgItems;
    }
    
    @Override
    public String getElementName() {
        return "x";
    }

    @Override
    public String getNamespace() {
        return "http://jabber.org/protocol/muc#offline";
    }

    @Override
    public String toXML() {
        return "";
    }
    
    public static class Provider implements PacketExtensionProvider {

        @Override
        public PacketExtension parseExtension(XmlPullParser xmlPullParser) throws Exception {
            RoomOffilineMsgExtension roomOfflineMsgExtension = new RoomOffilineMsgExtension();
            HashMap<String, Object> groupOffLineMsg = null;
            
            boolean done = false;
            
            while (!done) {
                int eventType = xmlPullParser.next();
                String elementName = xmlPullParser.getName();
                Log.d(TAG, "elementName:" + elementName);
                if (eventType == XmlPullParser.START_TAG) {
                    if (elementName.equals("from")) {
                        groupOffLineMsg = new HashMap<String, Object>(); 
                        groupOffLineMsg.put("sec", xmlPullParser.getAttributeValue("", "sec"));
                        groupOffLineMsg.put("jid", xmlPullParser.getAttributeValue("", "jid"));
                        Log.d(TAG, "from:"+ groupOffLineMsg.get("jid") + ";sec:" + groupOffLineMsg.get("sec"));

                    } else if (elementName.equals("body")) {
                        String body = xmlPullParser.nextText();
                        groupOffLineMsg.put("body", body);
                        Log.d(TAG, "body:"+ body);
                        roomOfflineMsgExtension.offlineMsgItems.add(groupOffLineMsg);
                        groupOffLineMsg = null;
                    }
                } else if (eventType == XmlPullParser.END_TAG && elementName.equals("x")) {
                    done = true;
                }
            }
            return roomOfflineMsgExtension;
        }
        
        
    }
}
