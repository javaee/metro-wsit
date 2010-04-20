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

package com.sun.xml.ws.api.security.trust.client;

import com.sun.xml.ws.api.security.secconv.client.SCTokenConfiguration;
import com.sun.xml.ws.api.security.IssuedTokenContext;
import com.sun.xml.ws.api.security.IssuedTokenContextImpl;

import com.sun.xml.ws.api.security.trust.WSTrustException;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Jiandong Guo
 */
public class IssuedTokenManager {
    private final Map<String, IssuedTokenProvider> itpMap = new HashMap<String, IssuedTokenProvider>();
    private final Map<String, String> itpClassMap = new HashMap<String, String>();
    private static IssuedTokenManager manager = new IssuedTokenManager();
    
    /** Creates a new instance of IssuedTokenManager */
    private IssuedTokenManager() {
        addDefaultProviders();
    }
    
    public static IssuedTokenManager getInstance(){
        synchronized (IssuedTokenManager.class) {
             return manager;
         }
    }
    
    public IssuedTokenContext createIssuedTokenContext(IssuedTokenConfiguration config, String appliesTo){
        IssuedTokenContext ctx = new IssuedTokenContextImpl();
        ctx.getSecurityPolicy().add(config);
        ctx.setEndpointAddress(appliesTo);
        
        return ctx;
    }
    
    public void getIssuedToken(IssuedTokenContext ctx)throws WSTrustException {
        IssuedTokenConfiguration config = (IssuedTokenConfiguration)ctx.getSecurityPolicy().get(0);
        IssuedTokenProvider provider = getIssuedTokenProvider(config.getProtocol());
        provider.issue(ctx);
    }
    
    public void renewIssuedToken(IssuedTokenContext ctx)throws WSTrustException {
        IssuedTokenConfiguration config = (IssuedTokenConfiguration)ctx.getSecurityPolicy().get(0);
        IssuedTokenProvider provider = getIssuedTokenProvider(config.getProtocol());
        provider.renew(ctx);
    }
    
    public void cancelIssuedToken(IssuedTokenContext ctx)throws WSTrustException {
        IssuedTokenConfiguration config = (IssuedTokenConfiguration)ctx.getSecurityPolicy().get(0);
        IssuedTokenProvider provider = getIssuedTokenProvider(config.getProtocol());
        provider.cancel(ctx);
    }
    
    public void validateIssuedToken(IssuedTokenContext ctx)throws WSTrustException {
        IssuedTokenConfiguration config = (IssuedTokenConfiguration)ctx.getSecurityPolicy().get(0);
        IssuedTokenProvider provider = getIssuedTokenProvider(config.getProtocol());
        provider.validate(ctx);
    }
    
    private void addDefaultProviders(){
        itpClassMap.put(STSIssuedTokenConfiguration.PROTOCOL_10, "com.sun.xml.ws.security.trust.impl.client.STSIssuedTokenProviderImpl");
        itpClassMap.put(STSIssuedTokenConfiguration.PROTOCOL_13, "com.sun.xml.ws.security.trust.impl.client.STSIssuedTokenProviderImpl");
        itpClassMap.put(SCTokenConfiguration.PROTOCOL_10, "com.sun.xml.ws.security.secconv.impl.client.SCTokenProviderImpl");
        itpClassMap.put(SCTokenConfiguration.PROTOCOL_13, "com.sun.xml.ws.security.secconv.impl.client.SCTokenProviderImpl");
    }

    private IssuedTokenProvider getIssuedTokenProvider(String protocol) throws WSTrustException {
        IssuedTokenProvider itp = null;
        synchronized (itpMap){
            itp = (IssuedTokenProvider)itpMap.get(protocol);
            if (itp == null){
                String type = itpClassMap.get(protocol);
                if (type != null){
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
                            Class<IssuedTokenProvider> typedClass = (Class<IssuedTokenProvider>)clazz;
                            itp = (IssuedTokenProvider)typedClass.newInstance();
                            itpMap.put(protocol, itp);
                        }
                    } catch (Exception e) {
                        throw new WSTrustException("IssueTokenProvider for the protocol: "+protocol+ "is not supported", e);
                    }
                }else{
                    throw new WSTrustException("IssueTokenProvider for the protocol: "+protocol+ "is not supported");
                }
            }
        }
        
        return itp;
    }
}
