/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 * 
 * Contributor(s):
 * 
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */

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

import java.util.logging.Level;
import java.util.logging.Logger;
import com.sun.xml.wss.provider.wsit.logging.LogDomainConstants;
import com.sun.xml.wss.provider.wsit.logging.LogStringsMessages;
import java.util.Collections;
import java.util.WeakHashMap;

/**
 *
 * @author kumar jayanti
 */
public class WSITClientAuthConfig implements ClientAuthConfig {

    private static final Logger log =
            Logger.getLogger(
            LogDomainConstants.WSIT_PVD_DOMAIN,
            LogDomainConstants.WSIT_PVD_DOMAIN_BUNDLE);
    private String layer = null;
    private String appContext = null;
    private CallbackHandler callbackHandler = null;
    private WSITClientAuthContext clientAuthContext = null;
    //private PolicyMap policyMap = null;
    private ReentrantReadWriteLock rwLock;
    private ReentrantReadWriteLock.ReadLock rLock;
    private ReentrantReadWriteLock.WriteLock wLock;
    private volatile boolean secEnabled;
    private Map<Object, WSITClientAuthContext> tubetoClientAuthContextHash = Collections.synchronizedMap(new WeakHashMap<Object, WSITClientAuthContext>());
    /** Creates a new instance of WSITClientAuthConfig */
    public WSITClientAuthConfig(String layer, String appContext, CallbackHandler callbackHandler) {
        this.layer = layer;
        this.appContext = appContext;
        this.callbackHandler = callbackHandler;
        this.rwLock = new ReentrantReadWriteLock(true);
        this.rLock = rwLock.readLock();
        this.wLock = rwLock.writeLock();
    }

    public ClientAuthContext getAuthContext(String operation, Subject subject, Map rawMap) throws AuthException {
        @SuppressWarnings("unchecked") Map<Object, Object> map = rawMap;

        PolicyMap pMap = (PolicyMap) map.get("POLICY");
        WSDLPort port = (WSDLPort) map.get("WSDL_MODEL");
        Object tubeOrPipe = map.get(PipeConstants.SECURITY_PIPE);
        map.put(PipeConstants.AUTH_CONFIG, this);

        if (pMap == null || pMap.isEmpty()) {
            return null;
        }
        if (tubeOrPipe == null) {
            //this is a cloned pipe
            return clientAuthContext;
        }
        //now check if security is enabled
        //if the policy has changed due to redeploy recheck if security is enabled
        try {
            rLock.lock(); // acquire read lock
            if (!secEnabled || !tubetoClientAuthContextHash.containsKey(tubeOrPipe)) {
                rLock.unlock(); // must unlock read, before acquiring write lock
                wLock.lock(); // acquire write lock
                try {
                    if (!secEnabled || !tubetoClientAuthContextHash.containsKey(tubeOrPipe)) { //re-check
                        if (!WSITAuthConfigProvider.isSecurityEnabled(pMap, port)) {
                            return null;
                        }
                        secEnabled = true;
                    }
                } finally {
                    rLock.lock(); // reacquire read before releasing write lock
                    wLock.unlock(); //release write lock
                }
            }
        } finally {
            rLock.unlock(); // release read lock
        }


        boolean authContextInitialized = false;
        this.rLock.lock();
        try {
            if (clientAuthContext != null) {
                //probably the app was redeployed
                //if so reacquire the AuthContext
                if (tubetoClientAuthContextHash.containsKey(tubeOrPipe)) {
                    authContextInitialized = true;
                    clientAuthContext = (WSITClientAuthContext) tubetoClientAuthContextHash.get(tubeOrPipe);
                }
            }
        } finally {
            this.rLock.unlock();
        }

        if (!authContextInitialized) {
            this.wLock.lock();
            try {
                // recheck the precondition, since the rlock was released.                
                if (clientAuthContext == null || !tubetoClientAuthContextHash.containsKey(tubeOrPipe)) {
                    clientAuthContext = new WSITClientAuthContext(operation, subject, map, callbackHandler);
                    tubetoClientAuthContextHash.put(tubeOrPipe, clientAuthContext);
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

    public ClientAuthContext cleanupAuthContext(Object tubeOrPipe) {
        return this.tubetoClientAuthContextHash.remove(tubeOrPipe);
    }

    @SuppressWarnings("unchecked")
    public JAXBElement startSecureConversation(Map map) {
        //check if we need to start secure conversation
        JAXBElement ret = null;
        try {
            MessageInfo info = (MessageInfo) map.get("SECURITY_TOKEN");
            if (info != null) {
                Packet packet = (Packet) info.getMap().get(WSITAuthContextBase.REQ_PACKET);
                if (packet != null) {
                    if (clientAuthContext != null) {
                        ret = ((WSITClientAuthContext) clientAuthContext).startSecureConversation(packet);
                        //map.put("SECURITY_TOKEN", ret);
                        info.getMap().put("SECURITY_TOKEN", ret);
                    } else {
                        log.log(Level.SEVERE,
                                LogStringsMessages.WSITPVD_0024_NULL_CLIENT_AUTH_CONTEXT());
                        throw new WSSecureConversationException(
                                LogStringsMessages.WSITPVD_0024_NULL_CLIENT_AUTH_CONTEXT());
                    }
                } else {
                    log.log(Level.SEVERE, LogStringsMessages.WSITPVD_0025_NULL_PACKET());
                    throw new RuntimeException(LogStringsMessages.WSITPVD_0025_NULL_PACKET());
                }
            }
        } catch (WSSecureConversationException ex) {
            log.log(Level.SEVERE, LogStringsMessages.WSITPVD_0026_ERROR_STARTING_SC(), ex);
            throw new RuntimeException(LogStringsMessages.WSITPVD_0026_ERROR_STARTING_SC(), ex);
        }
        return ret;
    }
}
