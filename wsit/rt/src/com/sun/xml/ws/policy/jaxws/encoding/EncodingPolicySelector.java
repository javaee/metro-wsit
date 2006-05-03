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

package com.sun.xml.ws.policy.jaxws.encoding;

import com.sun.xml.ws.policy.spi.PolicySelector;
import java.util.ArrayList;
import javax.xml.namespace.QName;

/**
 *
 * @author japod
 */
public class EncodingPolicySelector extends PolicySelector{
    
    private static final ArrayList<QName> supportedAssertions = new ArrayList<QName>();
    
    static {
        String wsomaNamespaceUri = "http://schemas.xmlsoap.org/ws/2004/09/policy/optimizedmimeserialization";
        String wspeNamespaceUri = "http://schemas.xmlsoap.org/ws/2004/09/policy/encoding";
        
        String anOptimizedMimeSerialization = "OptimizedMimeSerialization";
        String anUtf816FFFECharacterEncoding = "Utf816FFFECharacterEncoding";
        
        supportedAssertions.add(new QName(wsomaNamespaceUri,anOptimizedMimeSerialization));
        supportedAssertions.add(new QName(wspeNamespaceUri,anUtf816FFFECharacterEncoding));
    }
    
    /**
     * Creates a new instance of EncodingPolicySelector
     */
    public EncodingPolicySelector() {
        super(supportedAssertions);
    }
    
}
