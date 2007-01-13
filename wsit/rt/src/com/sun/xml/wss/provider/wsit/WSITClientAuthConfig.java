/*
 * WSITClientAuthConfig.java
 *
 * Created on November 1, 2006, 11:40 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.sun.xml.wss.provider.wsit;

import com.sun.xml.ws.api.message.Packet;
import com.sun.xml.ws.api.model.wsdl.WSDLPort;
import com.sun.xml.ws.policy.PolicyMap;
import com.sun.xml.ws.security.secconv.WSSecureConversationException;
import java.util.Map;
import javax.security.auth.Subject;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.message.AuthException;
import javax.security.auth.message.MessageInfo;
import javax.security.auth.message.config.ClientAuthConfig;
import javax.security.auth.message.config.ClientAuthContext;
import javax.xml.bind.JAXBElement;
import java.util.concurrent.locks.ReentrantReadWriteLock;


/**
 *
 * @author kumar jayanti
 */
public class WSITClientAuthConfig implements ClientAuthConfig {
    
    String layer = null;
    String appContext = null;
    //ignore the CBH here
    CallbackHandler cbh = null;
    WSITClientAuthContext clientAuthContext = null;

    private ReentrantReadWriteLock rwLock;
    private ReentrantReadWriteLock.ReadLock rLock;
    private ReentrantReadWriteLock.WriteLock wLock;
    private String secDisabled = null;
    private static final String TRUE="true";
    private static final String FALSE="false";
    
    /** Creates a new instance of WSITClientAuthConfig */
    public WSITClientAuthConfig(String layer, String appContext, CallbackHandler callbackHandler) {
        this.layer = layer;
        this.appContext = appContext;
        this.cbh = callbackHandler;
        this.rwLock = new ReentrantReadWriteLock(true);
        this.rLock = rwLock.readLock();
        this.wLock = rwLock.writeLock(); 
    }

    public ClientAuthContext getAuthContext(String operation, Subject subject, Map map) throws AuthException {
        PolicyMap  pMap = (PolicyMap)map.get("POLICY");
        WSDLPort port =(WSDLPort)map.get("WSDL_MODEL");
        if (pMap == null || pMap.isEmpty()) {
            return null;
        }
        
        //now check if security is enabled
         if (this.secDisabled == null) {
             if (!WSITAuthConfigProvider.isSecurityEnabled(pMap,port)) {
                 this.secDisabled = TRUE;
                 return null;
             } else {
                 this.secDisabled = FALSE;
             }
         }
         
         if (this.secDisabled == TRUE) {
             return null;
         }

        
        boolean authContextInitialized = false;
        
        try {
            this.rLock.lock();
            if (clientAuthContext != null) {
                authContextInitialized = true;
            }
        } finally {
            this.rLock.unlock();
        }
        
        if (!authContextInitialized) {
            try {
                this.wLock.lock();
                // recheck the precondition, since the rlock was released.
                if (clientAuthContext == null) {
                    clientAuthContext = new WSITClientAuthContext(operation, subject, map);
                }
            } finally {
                this.wLock.unlock();
            }
        }
       
        this.startSecureConversation(map);
        return clientAuthContext;
    }

    public String getMessageLayer() {
        return layer;
    }

    public String getAppContext() {
        return appContext;
    }

    public String getOperation(MessageInfo messageInfo) {
        return null;
    }

    public void refresh() {
    }

    public String getAuthContextID(MessageInfo messageInfo) {
        return null;
    }

    public boolean isProtected() {
        return true;
    }

    @SuppressWarnings("unchecked")
    public JAXBElement startSecureConversation(Map map) {
        //check if we need to start secure conversation
        JAXBElement ret = null;
        try {
            MessageInfo info = (MessageInfo)map.get("SECURITY_TOKEN");
            if (info != null) {
                Packet packet = (Packet)info.getMap().get(WSITAuthContextBase.REQ_PACKET);
                if (packet != null) {
                    if (clientAuthContext != null) {
                        ret =  ((WSITClientAuthContext)clientAuthContext).startSecureConversation(packet);
                        map.put("SECURITY_TOKEN", ret);
                    } else {
                        throw new WSSecureConversationException("Error: Client Authentication Context was not Initialized");
                    }
                } else {
                    throw new RuntimeException(
                            "A Packet required for starting a secure session was not supplied to getAuthContext()");
                }
            }
        } catch (WSSecureConversationException ex) {
            throw new RuntimeException(ex);
        }
        return ret;
    }
    
}
