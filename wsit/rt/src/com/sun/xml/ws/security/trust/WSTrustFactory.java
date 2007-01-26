/*
 * $Id: WSTrustFactory.java,v 1.9 2007-01-26 05:57:48 jdg6688 Exp $
 */

/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the License).  You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the license at
 * https://glassfish.dev.java.net/public/CDDLv1.0.html.
 * See the License for the specific language governing
 * permissions and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL
 * Header Notice in each file and include the License file
 * at https://glassfish.dev.java.net/public/CDDLv1.0.html.
 * If applicable, add the following below the CDDL Header,
 * with the fields enclosed by brackets [] replaced by
 * you own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Copyright 2006 Sun Microsystems Inc. All Rights Reserved
 */

package com.sun.xml.ws.security.trust;

import com.sun.xml.ws.api.security.trust.STSAttributeProvider;
import com.sun.xml.ws.api.security.trust.STSAuthorizationProvider;
import com.sun.xml.ws.api.security.trust.WSTrustContract;
import com.sun.xml.ws.api.security.trust.WSTrustException;
import com.sun.xml.ws.api.security.trust.config.STSConfiguration;
import com.sun.xml.ws.api.security.trust.config.STSConfigurationProvider;
import com.sun.xml.ws.security.trust.elements.RequestSecurityToken;
import com.sun.xml.ws.security.trust.elements.RequestSecurityTokenResponse;

import com.sun.xml.ws.security.trust.impl.DefaultSTSAttributeProvider;
import com.sun.xml.ws.security.trust.impl.DefaultSTSAuthorizationProvider;
import com.sun.xml.ws.security.trust.impl.WSTrustClientContractImpl;
import com.sun.xml.ws.security.trust.impl.TrustPluginImpl;

import com.sun.xml.ws.util.ServiceFinder;


import java.util.logging.Level;
import java.util.logging.Logger;
import com.sun.xml.ws.security.trust.logging.LogDomainConstants;
import com.sun.xml.ws.security.trust.logging.LogStringsMessages;

/**
 * A Factory for creating concrete WS-Trust contract instances
 */
public class WSTrustFactory {
   
    private static final Logger log =
            Logger.getLogger(
            LogDomainConstants.TRUST_IMPL_DOMAIN,
            LogDomainConstants.TRUST_IMPL_DOMAIN_BUNDLE);
    
    /**
     * return a concrete implementation for the TrustPlugin.
     */
    public static TrustPlugin newTrustPlugin(final Configuration config) {
        return new TrustPluginImpl(config);
    }
    
    /**
     * Return a concrete implementor of WSTrustContract.
     * <p>
     * Note: This contract is based on JAXB Beans generated for ws-trust.xsd schema elements
     * </p>
     * @Exception UnsupportedOperationException if this factory does not support this contract
     */
    public static WSTrustContract<RequestSecurityToken, RequestSecurityTokenResponse> newWSTrustContract(final STSConfiguration config, final String appliesTo) throws WSTrustException {
        //final STSConfiguration stsConfig = (STSConfiguration)config;
        //TrustSPMetadata spMetadata = stsConfig.getTrustSPMetadata(appliesTo);
       // if(spMetadata == null){
           // spMetadata = stsConfig.getTrustSPMetadata(WSTrustConstants.DEFAULT_APPLIESTO);
       // }
        //if (config. == null){
           // log.log(Level.SEVERE,
              //      LogStringsMessages.WST_0004_UNKNOWN_SERVICEPROVIDER(appliesTo));
           // throw new WSTrustException(LogStringsMessages.WST_0004_UNKNOWN_SERVICEPROVIDER(appliesTo));
       // }
        String type = config.getType();
        if(log.isLoggable(Level.FINE)) {
            log.log(Level.FINE,
                    LogStringsMessages.WST_1002_PROVIDER_TYPE(type));
        }
        WSTrustContract<RequestSecurityToken, RequestSecurityTokenResponse> contract = null;
        try {
            Class clazz = null;
            final ClassLoader loader = Thread.currentThread().getContextClassLoader();
            
            if (loader == null) {
                clazz = Class.forName(type);
            } else {
                clazz = loader.loadClass(type);
            }
            
            if (clazz != null) {
                contract = (WSTrustContract<RequestSecurityToken, RequestSecurityTokenResponse>) clazz.newInstance();
                contract.init(config);
            }
        } catch (ClassNotFoundException ex) {
            contract = null;
            log.log(Level.SEVERE,
                    LogStringsMessages.WST_0005_CLASSNOTFOUND_NULL_CONTRACT(type), ex);
            throw new WSTrustException(LogStringsMessages.WST_0005_CLASSNOTFOUND_NULL_CONTRACT(type), ex);
        } catch (Exception ex) {
            log.log(Level.SEVERE,
                    LogStringsMessages.WST_0038_INIT_CONTRACT_FAIL(), ex);
            throw new WSTrustException(LogStringsMessages.WST_0038_INIT_CONTRACT_FAIL(), ex);
        }
        
        return contract;
    }
    
    
    /**
     * return a concrete implementor for WS-Trust Client Contract
     */
    public  static WSTrustClientContract createWSTrustClientContract(final Configuration config) {
        return new WSTrustClientContractImpl(config);
    }
    
     /**
     * Returns the single instance of STSAuthorizationProvider
     * Use the usual services mechanism to find implementing class.  If not
     * found, use <code>com.sun.xml.ws.security.trust.impl.DefaultSTSAuthorizationProvider</code> 
     * by default.
     *
     */ 
    public static STSAuthorizationProvider getSTSAuthorizationProvider() {
        
        STSAuthorizationProvider authzProvider = null;
        final ServiceFinder<STSAuthorizationProvider> finder = 
                         ServiceFinder.find(STSAuthorizationProvider.class);
        if (finder != null && finder.toArray().length > 0) {
            authzProvider = finder.toArray()[0];
        } else {
            authzProvider = new DefaultSTSAuthorizationProvider();
        }
        return authzProvider;
     }
    
      /**
     * Returns the single instance of STSAttributeProvider
     * Use the usual services mechanism to find implementing class.  If not
     * found, use <code>com.sun.xml.ws.security.trust.impl.DefaultSTSAttributeProvider</code> 
     * by default.
     *
     */ 
    public static STSAttributeProvider getSTSAttributeProvider() {
        
        STSAttributeProvider attrProvider = null;
        final ServiceFinder<STSAttributeProvider> finder = 
                ServiceFinder.find(STSAttributeProvider.class);
        if (finder != null && finder.toArray().length > 0) {
            attrProvider = finder.toArray()[0];
        } else {
            attrProvider = new DefaultSTSAttributeProvider();
        }
        return attrProvider;
    }
    
    public static STSConfiguration getRuntimeSTSConfiguration(){
        STSConfigurationProvider configProvider = null;
        final ServiceFinder<STSConfigurationProvider> finder = 
                ServiceFinder.find(STSConfigurationProvider.class);
        if (finder != null && finder.toArray().length > 0) {
            configProvider = finder.toArray()[0];
        } 
        
        if (configProvider != null){
            return configProvider.getSTSConfiguration();
        }
        
        return null;
    }

    private WSTrustFactory() {
        //private constructor
    }
}
