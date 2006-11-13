/*
 * WSITServerAuthConfig.java
 *
 * Created on November 1, 2006, 11:41 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.sun.xml.wss.provider.wsit;

import com.sun.xml.ws.policy.PolicyMap;
import java.util.Map;
import javax.security.auth.Subject;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.message.AuthException;
import javax.security.auth.message.MessageInfo;
import javax.security.auth.message.config.ServerAuthConfig;
import javax.security.auth.message.config.ServerAuthContext;

/**
 *
 * @author kumar jayanti
 */
public class WSITServerAuthConfig implements ServerAuthConfig {
    
    String layer = null;
    String appContext = null;
    CallbackHandler cbh = null;
    
    WSITServerAuthContext serverAuthContext = null;
    
    /** Creates a new instance of WSITServerAuthConfig */
    public WSITServerAuthConfig(String layer, String appContext, CallbackHandler callbackHandler) {
        this.layer = layer;
        this.appContext = appContext;
        this.cbh = callbackHandler;
    }

    public synchronized ServerAuthContext getAuthContext(String operation, Subject subject, Map map) throws AuthException {
         PolicyMap  pMap = (PolicyMap)map.get("POLICY");
         if (pMap.isEmpty()) {
            return null;
         }
         //TODO: Not thread safe, make it thread safe
         if (serverAuthContext == null) {
             serverAuthContext = new WSITServerAuthContext(operation, subject, map);
         }
         return serverAuthContext;
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
