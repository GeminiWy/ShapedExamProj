package com.nd.shapedexamproj.im.packet;

import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.provider.IQProvider;
import org.xmlpull.v1.XmlPullParser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * ClassName:RoomMemmberQueryIQ <br/>
 * description: 聊天室成员IQ包 <br/>
 * Date: 2014-6-11 上午11:26:16 <br/>
 * 
 * @author WuYuLong
 * @version
 * @since JDK 1.6
 * @see
 */
public class RoomMemmberQueryIQ extends IQ {
    private List<HashMap<String, Object>> items = new ArrayList<HashMap<String, Object>>();

    public List<HashMap<String, Object>> getItems() {
        return items;
    }
	
    private String positionElement;

	public RoomMemmberQueryIQ(String element) {
		setPositionElement(element);
		toXML();
	}

	public String getPositionElement() {
		return positionElement;
	}

	public void setPositionElement(String positionElement) {
		this.positionElement = positionElement;
	}
	
    @Override
    public String getChildElementXML() {
        return positionElement;
    }

    /**
     * Internal Search service Provider.
     */
    public static class Provider implements IQProvider {

        /**
         * Provider Constructor.
         */
        public Provider() {
            super();
        }

        public IQ parseIQ(XmlPullParser parser) throws Exception {
        	RoomMemmberQueryIQ sharedGroupsIQ = new RoomMemmberQueryIQ("");
        	
        	boolean done = false;
        	int eventType = parser.getEventType();
            while (!done) {
                eventType = parser.next();
                if (eventType == XmlPullParser.START_TAG && "item".equals(parser.getName())) {
                	HashMap<String, Object> item = new HashMap<String, Object>(); 
                	item.put("status", parser.getAttributeValue("", "status"));
                	item.put("nick", parser.getAttributeValue("", "nick"));
                	item.put("jid", parser.getAttributeValue("", "jid"));
                	item.put("affiliation", parser.getAttributeValue("", "affiliation"));
                	
                	sharedGroupsIQ.items.add(item);
                } else if (eventType == XmlPullParser.END_TAG) {
                    if ("query".equals(parser.getName())) {
                        done = true;
                    }
                }
            }
        	
            return sharedGroupsIQ;
        }
    }
}
