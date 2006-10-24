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

package com.sun.xml.ws.policy.jaxws.impl;

import com.sun.xml.ws.policy.jaxws.encoding.FastInfosetModelConfiguratorProvider;
import com.sun.xml.ws.policy.spi.PolicySelector;
import java.util.ArrayList;
import javax.xml.namespace.QName;

/**
 * Implements SPI for selecting wsit related Microsoft's proprietary assertions.
 *
 * @author japod
 */
public class MicrosoftProprietaryPolicySelector extends PolicySelector{
    
    private static final ArrayList<QName> supportedAssertions = new ArrayList<QName>();
    
    static {
        supportedAssertions.add(new QName(
                "http://schemas.xmlsoap.org/ws/2004/09/policy/optimizedmimeserialization",
                "OptimizedMimeSerialization"));
        supportedAssertions.add(new QName(
                "http://schemas.xmlsoap.org/ws/2004/09/policy/encoding",
                "Utf816FFFECharacterEncoding"));
        supportedAssertions.add(new QName(
                "http://schemas.microsoft.com/net/2005/02/rm/policy",
                "MaxReceiveBufferSize"));
        supportedAssertions.add(new QName(
                "http://schemas.microsoft.com/net/2005/02/rm/policy",
                "RmFlowControl"));
    }
    
    /** Creates a new instance of MicrosoftProprietaryPolicySelector */
    public MicrosoftProprietaryPolicySelector() {
        super(supportedAssertions);
    }
    
}
