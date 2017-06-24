/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2017 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
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

package com.sun.xml.ws.security.trust;

import com.sun.xml.ws.api.security.trust.STSAttributeProvider;
import com.sun.xml.ws.api.security.trust.STSAuthorizationProvider;
import com.sun.xml.ws.api.security.trust.STSTokenProvider;
import com.sun.xml.ws.api.security.trust.WSTrustContract;
import com.sun.xml.ws.api.security.trust.WSTrustException;
import com.sun.xml.ws.api.security.trust.config.STSConfiguration;
import com.sun.xml.ws.api.security.trust.config.STSConfigurationProvider;
import com.sun.xml.ws.security.trust.elements.BaseSTSRequest;
import com.sun.xml.ws.security.trust.elements.BaseSTSResponse;

import com.sun.xml.ws.security.trust.impl.DefaultSAMLTokenProvider;
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
    public static TrustPlugin newTrustPlugin() {
        return new TrustPluginImpl();
    }
    
    /**
     * Return a concrete implementor of WSTrustContract.
     * <p>
     * Note: This contract is based on JAXB Beans generated for ws-trust.xsd schema elements
     * </p>
     * @Exception UnsupportedOperationException if this factory does not support this contract
     */
    public static WSTrustContract<BaseSTSRequest, BaseSTSResponse> newWSTrustContract(final STSConfiguration config, final String appliesTo) throws WSTrustException {
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
        WSTrustContract<BaseSTSRequest, BaseSTSResponse> contract = null;
        try {
            Class<?> clazz = null;
            final ClassLoader loader = Thread.currentThread().getContextClassLoader();
            
            if (loader == null) {
                clazz = Class.forName(type);
            } else {
                clazz = loader.loadClass(type);
            }
            
            if (clazz != null) {
                @SuppressWarnings("unchecked")
                Class<WSTrustContract<BaseSTSRequest, BaseSTSResponse>> typedClass = (Class<WSTrustContract<BaseSTSRequest, BaseSTSResponse>>) clazz;
                contract = typedClass.newInstance();
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
    public  static WSTrustClientContract createWSTrustClientContract() {
        return new WSTrustClientContractImpl();
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
        java.util.Iterator it = finder.iterator();
        if(it.hasNext()){
            authzProvider = (STSAuthorizationProvider)it.next();
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
        java.util.Iterator it = finder.iterator();
        if(it.hasNext()){
            attrProvider = (STSAttributeProvider)it.next();
        } else {
            attrProvider = new DefaultSTSAttributeProvider();
        }
        return attrProvider;
    }
    
    public static STSTokenProvider getSTSTokenProvider() {
        
        STSTokenProvider tokenProvider = null;
        final ServiceFinder<STSTokenProvider> finder = 
                ServiceFinder.find(STSTokenProvider.class);
        java.util.Iterator it = finder.iterator();
        if(it.hasNext()){
            tokenProvider = (STSTokenProvider)it.next();
        } else {
            tokenProvider = new DefaultSAMLTokenProvider();
        }
        return tokenProvider;
    }
    
    public static STSConfiguration getRuntimeSTSConfiguration(){
        STSConfigurationProvider configProvider = null;
        final ServiceFinder<STSConfigurationProvider> finder = 
                ServiceFinder.find(STSConfigurationProvider.class);
        java.util.Iterator it = finder.iterator();
        if(it.hasNext()){
            configProvider = (STSConfigurationProvider)it.next();
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
