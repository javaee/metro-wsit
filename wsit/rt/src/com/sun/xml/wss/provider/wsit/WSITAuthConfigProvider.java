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
    
    /** Creates a new instance of WSITAuthConfigProvider */
    public WSITAuthConfigProvider(Map props) {
        properties = props;
    }

    public synchronized ClientAuthConfig getClientAuthConfig(String layer, String appContext, CallbackHandler callbackHandler) throws AuthException {
        if (clientConfig == null) {
            clientConfig = new WSITClientAuthConfig(layer, appContext, callbackHandler);
        }
        return clientConfig;
    }
    
    public synchronized ServerAuthConfig getServerAuthConfig(String layer, String appContext, CallbackHandler callbackHandler) throws AuthException {
        if (serverConfig == null) {
            serverConfig = new WSITServerAuthConfig(layer, appContext, callbackHandler);
        }
        return serverConfig;
    }

    public void refresh() {
    }


    public AuthConfigProvider newInstance(Map map) {
        throw new UnsupportedOperationException(
                "newInstance(Map) method not supported by WSIT Provider");
    }

    public AuthConfigProvider registerNewInstance(AuthConfigFactory authConfigFactory, Map map) {
        WSITAuthConfigProvider provider = new WSITAuthConfigProvider(map); 
        authConfigFactory.registerConfigProvider(provider, "SOAP", null,description);
        return provider;
    }
    
}
