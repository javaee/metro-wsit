/*
 * PacketMessageInfo.java
 *
 * Created on November 6, 2006, 10:43 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.sun.xml.wss.provider.wsit;

import com.sun.xml.ws.api.message.Packet;
import java.util.HashMap;
import java.util.Map;
import javax.security.auth.message.MessageInfo;

/**
 *
 * @author kumar.jayanti 
 */
public class PacketMessageInfo implements MessageInfo {
    Map properties = new HashMap();
    Packet reqPacket = null;
    //Packet responsePacket = null;
    /** Creates a new instance of PacketMessageInfo */
    public PacketMessageInfo() {
    }

    public Object getRequestMessage() {
        //return reqPacket;
        return (Packet)properties.get("REQ_PACKET");
    }

    public Object getResponseMessage() {
        //return responsePacket;
        return (Packet)properties.get("RES_PACKET");
    }

    @SuppressWarnings("unchecked")
    public void setRequestMessage(Object object) {
        reqPacket = (Packet)object;
        properties.put("REQ_PACKET", reqPacket);
    }

    @SuppressWarnings("unchecked")
    public void setResponseMessage(Object object) {
        //responsePacket = (Packet)object;
        properties.put("RES_PACKET", reqPacket);
    }

    public Map getMap() {
        return properties;
    }
    
}
