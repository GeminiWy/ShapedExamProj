package com.nd.shapedexamproj.im.packet;

import com.nd.shapedexamproj.im.resource.IMConstants;

import org.jivesoftware.smack.packet.PacketExtension;
import org.jivesoftware.smack.provider.PacketExtensionProvider;
import org.xmlpull.v1.XmlPullParser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


/**
 * ClassName:RoomPresence <br/>
 * description: 聊天室上线包 <br/>
 * Date: 2014-6-11 上午11:26:16 <br/>
 * 
 * @author WuYuLong
 * @version
 * @since JDK 1.6
 * @see
 */
public class RoomPacketExtension implements PacketExtension{
    
    private static String TAG = "RoomPacketExtension";
    private List<HashMap<String, Object>> groupItems = new ArrayList<HashMap<String, Object>>();
    /*private List<HashMap<String, Object>> offlineMsgItems = new ArrayList<HashMap<String, Object>>();*/
    
    public List<HashMap<String, Object>> getGroupItems() {
        return groupItems;
    }
	
    
    
	@Override
	public String toXML() {
		return "";
	}
	
	@Override
	public String getNamespace() {
		return IMConstants.ROOM_PRESENCE_NAMESPACE;
	}
	
	@Override
	public String getElementName() {
		return "item";
	}
	
	 /**
     * Internal Search service Provider.
     */
    public static class Provider implements PacketExtensionProvider {

        /**
         * Provider Constructor.
         */
        public Provider() {
            super();
        }

		@Override
		public PacketExtension parseExtension(XmlPullParser parser)
				throws Exception {
            RoomPacketExtension packetExtension = new RoomPacketExtension();
            HashMap<String, Object> item = null;
            
        	boolean done = false;
        	int eventType ;
            while (!done) {
                eventType = parser.next();
                switch (eventType ) {
                    case XmlPullParser.START_TAG:
                        
                        if ("item".equals(parser.getName())) {
                            item = new HashMap<String, Object>(); 
                            item.put("name", parser.getAttributeValue("", "name"));
                            item.put("time", parser.getAttributeValue("", "time"));
                            item.put("maxstanzas", parser.getAttributeValue("", "maxstanzas"));
                            item.put("grpid", parser.getAttributeValue("", "grpid"));
                            
                            packetExtension.groupItems.add(item);
                            item = null;
                        } 
                        
                        break;
                    case XmlPullParser.END_TAG:
                        if ("x".equals(parser.getName())) {
                            done = true;
                        } 
                        
                        break;
                }
                
            }
            return packetExtension;
		}
    }
    
}
