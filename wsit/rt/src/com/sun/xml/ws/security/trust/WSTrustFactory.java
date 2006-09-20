/*
 * $Id: WSTrustFactory.java,v 1.2 2006-09-20 23:58:46 manveen Exp $
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

import com.sun.xml.ws.security.trust.impl.WSTrustClientContractImpl;
import com.sun.xml.ws.security.trust.impl.TrustPluginImpl;
import com.sun.xml.ws.security.trust.impl.STSConfiguration;
import com.sun.xml.ws.security.trust.impl.TrustSPMetadata;

import java.net.URI;

import java.util.logging.Level;
import java.util.logging.Logger;
import com.sun.xml.ws.security.trust.logging.LogDomainConstants;

/**
 * A Factory for creating concrete WS-Trust contract instances
 */
public class WSTrustFactory {
    
    private static Logger log =
            Logger.getLogger(
            LogDomainConstants.TRUST_IMPL_DOMAIN,
            LogDomainConstants.TRUST_IMPL_DOMAIN_BUNDLE);
    
    /**
     * return a concrete implementation for the TrustPlugin.
     */
    public static TrustPlugin newTrustPlugin(Configuration config) {
        return new TrustPluginImpl(config);
    }
    
    /**
     * Return a concrete implementor of WSTrustContract.
     * <p>
     * Note: This contract is based on JAXB Beans generated for ws-trust.xsd schema elements
     * </p>
     * @Exception UnsupportedOperationException if this factory does not support this contract
     */
    public static WSTrustContract newWSTrustContract(Configuration config, String appliesTo) throws WSTrustException {
        STSConfiguration stsConfig = (STSConfiguration)config;
        TrustSPMetadata spMetadata = stsConfig.getTrustSPMetadata(appliesTo);
        if(spMetadata == null){
            spMetadata = stsConfig.getTrustSPMetadata(WSTrustConstants.DEFAULT_APPLIESTO);
        }
        if (spMetadata == null){
            log.log(Level.SEVERE,
                    "WST1004.unknown.serviceprovider",
                    new Object[] {appliesTo});
            throw new WSTrustException("Unknown target service provider " + appliesTo);
        }
        String type = spMetadata.getType();
        if (type == null)
            type = stsConfig.getDefaultType();
        
        log.log(Level.FINE,
                "WST1002.provider.type",
                new Object[] {type});
                
        WSTrustContract contract = null;
        try {
            Class clazz = null;
            ClassLoader loader = Thread.currentThread().getContextClassLoader();
            
            if (loader == null) {
                clazz = Class.forName(type);
            } else {
                clazz = loader.loadClass(type);
            }
            
            if (clazz != null) {
                contract = (WSTrustContract) clazz.newInstance();
                contract.init(spMetadata);
            }
        } catch (ClassNotFoundException ex) {
            contract = null;
            log.log(Level.SEVERE,
                "WST0005.classnotfound.null.contract",
                new Object[] {type});
            ex.printStackTrace();
        } catch (Exception ex) {
            throw new WSTrustException(ex.toString(), ex);
        }
        
        return contract;
    }
    
    /**
     * return a concrete implementor of WSTrustSourceContract
     * <p>
     * Note: This contract is useful when the STS is implemented as a JAXWS Provider
     * </p>
     * @Exception UnsupportedOperationException if this factory does not support this contract
     */
    public static WSTrustSourceContract newWSTrustSourceContract(Configuration config) {
        throw new UnsupportedOperationException("To Do");
    }
    
    /**
     * return a concrete implementor of WSTrustDomContract
     * <p>
     * Note: This contract is useful when the STS is implemented as a JAXWS Provider
     * </p>
     * @Exception UnsupportedOperationException if this factory does not support this contract
     */
    public static WSTrustDOMContract newWSTrustDOMContract(Configuration config) {
        throw new UnsupportedOperationException("To be overridden by Actual Factories");
    }
    
    /**
     * return a concrete implementor for WS-Trust Client Contract
     */
    public  static WSTrustClientContract createWSTrustClientContract(Configuration config) {
        return new WSTrustClientContractImpl(config);
    }
}
