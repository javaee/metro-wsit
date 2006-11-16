/*
 * WSITAuthConfigProvider.java
 *
 * Created on November 1, 2006, 10:53 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.sun.xml.wss.provider.wsit;

import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.message.AuthException;
import javax.security.auth.message.config.AuthConfigFactory;
import javax.security.auth.message.config.AuthConfigProvider;
import javax.security.auth.message.config.ClientAuthConfig;
import javax.security.auth.message.config.ServerAuthConfig;

/**
 *
 * @author kumar.jayanti
 */
public class WSITAuthConfigProvider implements AuthConfigProvider {
    
    Map properties = null;
    String id = null;
    String description = "WSIT AuthConfigProvider";
    
    ClientAuthConfig clientConfig = null;
    ServerAuthConfig serverConfig = null;
    AuthConfigFactory factory = null;
    
    private ReentrantReadWriteLock rwLock;
    private ReentrantReadWriteLock.ReadLock rLock;
    private ReentrantReadWriteLock.WriteLock wLock;
    
    /** Creates a new instance of WSITAuthConfigProvider */
    public WSITAuthConfigProvider(Map props, AuthConfigFactory factory) {
        properties = props;
        this.factory = factory;
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
    
}
