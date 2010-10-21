/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
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
 * WSITAuthConfigProvider.java
 *
 * Created on November 1, 2006, 10:53 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.sun.xml.wss.provider.wsit;

import com.sun.xml.ws.api.model.wsdl.WSDLBoundOperation;
import com.sun.xml.ws.api.model.wsdl.WSDLPort;
import com.sun.xml.ws.policy.Policy;
import com.sun.xml.ws.policy.PolicyException;
import com.sun.xml.ws.policy.PolicyMap;
import com.sun.xml.ws.policy.PolicyMapKey;
import com.sun.xml.ws.security.policy.SecurityPolicyVersion;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.message.AuthException;
import javax.security.auth.message.config.AuthConfigFactory;
import javax.security.auth.message.config.AuthConfigProvider;
import javax.security.auth.message.config.ClientAuthConfig;
import javax.security.auth.message.config.ServerAuthConfig;
import javax.xml.ws.WebServiceException;

/**
 *
 * @author kumar.jayanti
 */
public class WSITAuthConfigProvider implements AuthConfigProvider {

    //Map properties = null;    
    String description = "WSIT AuthConfigProvider";
    
    //ClientAuthConfig clientConfig = null;
    //ServerAuthConfig serverConfig = null;
    WeakHashMap clientConfigMap = new WeakHashMap();
    WeakHashMap serverConfigMap = new WeakHashMap();
    
    private ReentrantReadWriteLock rwLock;
    private ReentrantReadWriteLock.ReadLock rLock;
    private ReentrantReadWriteLock.WriteLock wLock;
    
    /** Creates a new instance of WSITAuthConfigProvider */
    public WSITAuthConfigProvider(Map props, AuthConfigFactory factory) {
        //properties = props;
        //this.factory = factory;
        if (factory != null) {
            factory.registerConfigProvider(this, "SOAP", null,description);
        }
        this.rwLock = new ReentrantReadWriteLock(true);
        this.rLock = rwLock.readLock();
        this.wLock = rwLock.writeLock(); 
    }

    @SuppressWarnings("unchecked")
    public  ClientAuthConfig getClientAuthConfig(String layer, String appContext, CallbackHandler callbackHandler) throws AuthException {
        
        ClientAuthConfig clientConfig = null;
        this.rLock.lock();
        try {
            clientConfig = (ClientAuthConfig)this.clientConfigMap.get(appContext);
            if (clientConfig != null) {
                    return clientConfig;
            }
        } finally {
            this.rLock.unlock();
        }
        // make sure you don't hold the rlock when you request the wlock
        // or you will encounter dealock
        this.wLock.lock();
        try {
            // recheck the precondition, since the rlock was released.
            if (clientConfig == null) {
                clientConfig = new WSITClientAuthConfig(layer, appContext, callbackHandler);
                this.clientConfigMap.put(appContext, clientConfig);
            }
            return clientConfig;
        } finally {
            this.wLock.unlock();
        }
    }
    
    @SuppressWarnings("unchecked")
    public  ServerAuthConfig getServerAuthConfig(String layer, String appContext, CallbackHandler callbackHandler) throws AuthException {
        ServerAuthConfig serverConfig = null;
        this.rLock.lock();
         try {
             serverConfig = (ServerAuthConfig)this.serverConfigMap.get(appContext);
             if (serverConfig != null) {
                 return serverConfig;
             }
         } finally {
             this.rLock.unlock();
         }
        // make sure you don't hold the rlock when you request the wlock
        // or you will encounter dealock
         this.wLock.lock();
         try {
             // recheck the precondition, since the rlock was released.
             if (serverConfig == null) {
                 serverConfig = new WSITServerAuthConfig(layer, appContext, callbackHandler);
                 this.serverConfigMap.put(appContext,serverConfig);
             }
             return serverConfig;
         } finally {
             this.wLock.unlock();
         }
    }

    public void refresh() {
    }
    
    /**
     * Checks to see whether WS-Security is enabled or not.
     *
     * @param policyMap policy map for {@link this} assembler
     * @param wsdlPort wsdl:port
     * @return true if Security is enabled, false otherwise
     */
    
    public static boolean isSecurityEnabled(PolicyMap policyMap, WSDLPort wsdlPort) {
        if (policyMap == null || wsdlPort == null)
            return false;
        
        try {
            PolicyMapKey endpointKey = policyMap.createWsdlEndpointScopeKey(wsdlPort.getOwner().getName(),
                    wsdlPort.getName());
            Policy policy = policyMap.getEndpointEffectivePolicy(endpointKey);
            
            if ((policy != null) && 
                    (policy.contains(SecurityPolicyVersion.SECURITYPOLICY200507.namespaceUri) ||
                        policy.contains(SecurityPolicyVersion.SECURITYPOLICY12NS.namespaceUri)||
                        policy.contains(SecurityPolicyVersion.SECURITYPOLICY200512.namespaceUri))) {
                return true;
            }
            
            for (WSDLBoundOperation wbo : wsdlPort.getBinding().getBindingOperations()) {
                PolicyMapKey operationKey = policyMap.createWsdlOperationScopeKey(wsdlPort.getOwner().getName(),
                        wsdlPort.getName(),
                        wbo.getName());
                policy = policyMap.getOperationEffectivePolicy(operationKey);
                if ((policy != null) && 
                       (policy.contains(SecurityPolicyVersion.SECURITYPOLICY200507.namespaceUri) ||
                            policy.contains(SecurityPolicyVersion.SECURITYPOLICY12NS.namespaceUri)))
                    return true;
                
                policy = policyMap.getInputMessageEffectivePolicy(operationKey);
                if ((policy != null) && 
                        (policy.contains(SecurityPolicyVersion.SECURITYPOLICY200507.namespaceUri) ||
                            policy.contains(SecurityPolicyVersion.SECURITYPOLICY12NS.namespaceUri)))
                    return true;
                
                policy = policyMap.getOutputMessageEffectivePolicy(operationKey);
                if ((policy != null) && 
                        (policy.contains(SecurityPolicyVersion.SECURITYPOLICY200507.namespaceUri) ||
                            policy.contains(SecurityPolicyVersion.SECURITYPOLICY12NS.namespaceUri)))
                    return true;
                
                policy = policyMap.getFaultMessageEffectivePolicy(operationKey);
                if ((policy != null) && 
                        (policy.contains(SecurityPolicyVersion.SECURITYPOLICY200507.namespaceUri) ||
                            policy.contains(SecurityPolicyVersion.SECURITYPOLICY12NS.namespaceUri)))
                    return true;
            }
        } catch (PolicyException e) {
            throw new WebServiceException(e);
        }
        
        return false;
    }
    
}
