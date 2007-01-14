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
import java.util.Map;
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
    private static final String SECURITY_POLICY_NAMESPACE_URI = 
                "http://schemas.xmlsoap.org/ws/2005/07/securitypolicy";
    //Map properties = null;
    String id = null;
    String description = "WSIT AuthConfigProvider";
    
    ClientAuthConfig clientConfig = null;
    ServerAuthConfig serverConfig = null;
    //AuthConfigFactory factory = null;
    
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

    public  ClientAuthConfig getClientAuthConfig(String layer, String appContext, CallbackHandler callbackHandler) throws AuthException {
        try {
            this.rLock.lock();
            if (clientConfig != null) {                
                return clientConfig;
            }
        } finally {
            this.rLock.unlock();
        }
        // make sure you don't hold the rlock when you request the wlock
        // or you will encounter dealock
        
        try {
            this.wLock.lock();
            // recheck the precondition, since the rlock was released.
            if (clientConfig == null) {
                clientConfig = new WSITClientAuthConfig(layer, appContext, callbackHandler);
            }
            return clientConfig;
        } finally {
            this.wLock.unlock();
        }
    }
    
    public  ServerAuthConfig getServerAuthConfig(String layer, String appContext, CallbackHandler callbackHandler) throws AuthException {
         try {
             this.rLock.lock();
             if (serverConfig != null) {
                 return serverConfig;
             }
         } finally {
             this.rLock.unlock();
         }
        // make sure you don't hold the rlock when you request the wlock
        // or you will encounter dealock
         
         try {
             this.wLock.lock();
             // recheck the precondition, since the rlock was released.
             if (serverConfig == null) {
                 serverConfig = new WSITServerAuthConfig(layer, appContext, callbackHandler);
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
            
            if ((policy != null) && policy.contains(SECURITY_POLICY_NAMESPACE_URI)) {
                return true;
            }
            
            for (WSDLBoundOperation wbo : wsdlPort.getBinding().getBindingOperations()) {
                PolicyMapKey operationKey = policyMap.createWsdlOperationScopeKey(wsdlPort.getOwner().getName(),
                        wsdlPort.getName(),
                        wbo.getName());
                policy = policyMap.getOperationEffectivePolicy(operationKey);
                if ((policy != null) && policy.contains(SECURITY_POLICY_NAMESPACE_URI))
                    return true;
                
                policy = policyMap.getInputMessageEffectivePolicy(operationKey);
                if ((policy != null) && policy.contains(SECURITY_POLICY_NAMESPACE_URI))
                    return true;
                
                policy = policyMap.getOutputMessageEffectivePolicy(operationKey);
                if ((policy != null) && policy.contains(SECURITY_POLICY_NAMESPACE_URI))
                    return true;
                
                policy = policyMap.getFaultMessageEffectivePolicy(operationKey);
                if ((policy != null) && policy.contains(SECURITY_POLICY_NAMESPACE_URI))
                    return true;
            }
        } catch (PolicyException e) {
            throw new WebServiceException(e);
        }
        
        return false;
    }
    
}
