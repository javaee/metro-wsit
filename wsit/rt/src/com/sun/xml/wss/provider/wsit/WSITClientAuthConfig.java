/*
 * WSITClientAuthConfig.java
 *
 * Created on November 1, 2006, 11:40 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.sun.xml.wss.provider.wsit;

import com.sun.xml.ws.policy.PolicyMap;
import com.sun.xml.ws.security.secconv.SecureConversationInitiator;
import java.util.Map;
import javax.security.auth.Subject;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.message.AuthException;
import javax.security.auth.message.MessageInfo;
import javax.security.auth.message.config.ClientAuthConfig;
import javax.security.auth.message.config.ClientAuthContext;

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
    
    /** Creates a new instance of WSITClientAuthConfig */
    public WSITClientAuthConfig(String layer, String appContext, CallbackHandler callbackHandler) {
        this.layer = layer;
        this.appContext = appContext;
        this.cbh = callbackHandler;
    }

    public synchronized ClientAuthContext getAuthContext(String operation, Subject subject, Map map) throws AuthException {
        PolicyMap  pMap = (PolicyMap)map.get("POLICY");
        if (pMap.isEmpty()) {
            return null;
        }
        if (clientAuthContext == null) {
            clientAuthContext = new WSITClientAuthContext(operation, subject, map);
        }
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
    
}
