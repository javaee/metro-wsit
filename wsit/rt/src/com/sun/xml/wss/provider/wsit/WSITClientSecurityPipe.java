/*
 * WSITClientSecurityPipe.java
 *
 * Created on November 6, 2006, 6:36 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.sun.xml.wss.provider.wsit;

import com.sun.xml.ws.api.message.Packet;
import com.sun.xml.ws.api.pipe.Pipe;
import com.sun.xml.ws.api.pipe.PipeCloner;

import java.util.Map;
import javax.security.auth.message.AuthException;
import javax.security.auth.message.MessageInfo;
import javax.security.auth.message.config.AuthConfigProvider;
import javax.security.auth.message.config.ClientAuthConfig;
import javax.security.auth.message.config.ClientAuthContext;
import javax.xml.ws.WebServiceException;

/**
 *
 * @author kumar.jayanti
 */
public class WSITClientSecurityPipe implements Pipe {
    
    Pipe nextPipe = null;
    Map  properties = null;
    
    // instance variables for the configs
    ClientAuthConfig clientConfig = null;
    ClientAuthContext clientAuthContext = null;
    
    public WSITClientSecurityPipe(WSITClientSecurityPipe that) {
        this.clientConfig = that.clientConfig;
        this.clientAuthContext = that.clientAuthContext;
        this.properties = that.properties;
    }
    
    /** Creates a new instance of WSITClientSecurityPipe */
     @SuppressWarnings("unchecked")
    public WSITClientSecurityPipe(Map properties, Pipe nextPipe) {
        this.properties = properties;
        this.nextPipe = nextPipe;
        properties.put("NEXT_PIPE", nextPipe);
        //TODO: Load the Class by reflection.
        //Hack for now
        AuthConfigProvider provider = new WSITAuthConfigProvider(properties, null);
        try {
            clientConfig = provider.getClientAuthConfig("SOAP", null, null);
            //initialize the clientAuthContext
            clientAuthContext = clientConfig.getAuthContext(null, null, properties);
            properties.put("SC_INITIATOR", clientAuthContext);
        } catch (AuthException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    public Packet process(Packet packet) {
        MessageInfo messageInfo = new PacketMessageInfo();
        try {
        messageInfo.getMap().put("REQ_PACKET",packet);
        clientAuthContext.secureRequest(messageInfo, null);
        
        Packet retPacket = nextPipe.process((Packet)messageInfo.getMap().get("REQ_PACKET"));
        messageInfo.getMap().put("RES_PACKET", retPacket);
        if (retPacket.getMessage() == null) {
            return retPacket;
        }
         
        clientAuthContext.validateResponse(messageInfo, null, null);
        } catch (AuthException e) {
            throw new WebServiceException(e);
        }
        return (Packet)messageInfo.getMap().get("RES_PACKET");
    }

    public void preDestroy() {
        try {
        clientAuthContext.cleanSubject(null, null);
        } catch (AuthException e) {
            throw new RuntimeException(e);
        }
        if (nextPipe != null) {
            nextPipe.preDestroy();
        }
    }

    public Pipe copy(PipeCloner cloner) {
        Pipe clonedNextPipe = cloner.copy(nextPipe);
        Pipe copied = new WSITClientSecurityPipe(this);
        ((WSITClientSecurityPipe)copied).setNextPipe(clonedNextPipe);
        cloner.add(this, copied);
        return copied;    
    }
    
    public void setNextPipe(Pipe next) {
        this.nextPipe = next;
    }
    
}
