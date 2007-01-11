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

/*
 * STSConfiguration.java
 *
 * Created on March 24, 2006, 1:19 PM
 *
 */

package com.sun.xml.ws.security.trust.impl;

import com.sun.xml.ws.security.trust.Configuration;
import com.sun.xml.ws.security.trust.sts.BaseSTSImpl;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Jiandong Guo
 */
public class STSConfiguration implements Configuration{
    Map<String, TrustSPMetadata> spMap;
    String defaultType = BaseSTSImpl.DEFAULT_IMPL;
    
    public STSConfiguration(){
        spMap = new HashMap<String, TrustSPMetadata>();
    }
    
    public void addTrustSPMetadata(final TrustSPMetadata data, final String spEndpoint){
        spMap.put(spEndpoint, data);
    }
    
    public TrustSPMetadata getTrustSPMetadata(final String spEndpoint){
        return (TrustSPMetadata)spMap.get(spEndpoint);
    }
    
    public String getDefaultType(){
        return this.defaultType;
    }
}
