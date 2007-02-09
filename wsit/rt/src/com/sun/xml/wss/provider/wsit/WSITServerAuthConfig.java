/*
 * WSITServerAuthConfig.java
 *
 * Created on November 1, 2006, 11:41 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.sun.xml.wss.provider.wsit;

import com.sun.xml.ws.api.model.wsdl.WSDLPort;
import com.sun.xml.ws.policy.PolicyMap;
import java.util.Map;
import javax.security.auth.Subject;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.message.AuthException;
import javax.security.auth.message.MessageInfo;
import javax.security.auth.message.config.ServerAuthConfig;
import javax.security.auth.message.config.ServerAuthContext;
import java.util.concurrent.locks.ReentrantReadWriteLock; 

/**
 *
 * @author kumar jayanti
 */
public class WSITServerAuthConfig implements ServerAuthConfig {
    
    private String layer = null;
    private String appContext = null;
    //CallbackHandler cbh = null;
    
    private WSITServerAuthContext serverAuthContext = null;
    private PolicyMap policyMap = null;
    private String secDisabled = null;
    
    private ReentrantReadWriteLock rwLock;
    private ReentrantReadWriteLock.ReadLock rLock;
    private ReentrantReadWriteLock.WriteLock wLock;
    
    private static final String TRUE="true";
    private static final String FALSE="false";
    
    /** Creates a new instance of WSITServerAuthConfig */
    public WSITServerAuthConfig(String layer, String appContext, CallbackHandler callbackHandler) {
        this.layer = layer;
        this.appContext = appContext;
        //this.cbh = callbackHandler;
        this.rwLock = new ReentrantReadWriteLock(true);
        this.rLock = rwLock.readLock();
        this.wLock = rwLock.writeLock();
    }

    public ServerAuthContext getAuthContext(String operation, Subject subject, Map map) throws AuthException {
         PolicyMap  pMap = (PolicyMap)map.get("POLICY");
         WSDLPort port =(WSDLPort)map.get("WSDL_MODEL");
         if (pMap == null || pMap.isEmpty()) {
             //TODO: log warning here if pMap == null
             return null;
         }
         
         //check if security is enabled
         //if policy has changed due to redeploy, check if security is enabled
         if (this.secDisabled == null || (policyMap != pMap)) {
             try {
                 this.wLock.lock();
                 if (this.secDisabled == null || (policyMap != pMap)) {
                     if (!WSITAuthConfigProvider.isSecurityEnabled(pMap,port)) {
                         this.secDisabled = TRUE;
                         return null;
                     } else {
                         this.secDisabled = FALSE;
                     }
                 }
             } finally {
                 this.wLock.unlock();
             }
         }
         
         if (this.secDisabled == TRUE) {
             return null;
         }
         
         try {
             this.rLock.lock();
             if (serverAuthContext != null) {
                 //return the cached one only if the same policyMap was passed in
                 if (policyMap == pMap) {
                     return serverAuthContext;
                 }
             }
         } finally {
             this.rLock.unlock();
         }
        // make sure you don't hold the rlock when you request the wlock
        // or you will encounter dealock
         
         try {
             this.wLock.lock();
             // recheck the precondition, since the rlock was released.
             if ((serverAuthContext == null) || (policyMap != pMap)) {
                 serverAuthContext = new WSITServerAuthContext(operation, subject, map);
                 policyMap = pMap;
             }
             return serverAuthContext;
         } finally {
             this.wLock.unlock();
         }
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
    
    
}
