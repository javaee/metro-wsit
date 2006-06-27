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

package com.sun.xml.ws.policy.jaxws.addressing;

import java.util.ArrayList;
import javax.xml.namespace.QName;
import com.sun.xml.ws.policy.spi.PolicySelector;

/**
 *
 * @author japod
 */
public class AddressingPolicySelector extends PolicySelector{
    
    private static final ArrayList<QName> supportedAssertions = new ArrayList<QName>();
    
    static {
        String wsapNamespaceUri = "http://schemas.xmlsoap.org/ws/2004/09/policy/addressing";
        String wsaw1NamespaceUri = "http://www.w3.org/2005/08/addressing";
        String wsaw2NamespaceUri = "http://www.w3.org/2005/08/addressing/wsdl";
        String anUsingAddressing = "UsingAddressing";
        String anAnonymous = "Anonymous";
        supportedAssertions.add(new QName(wsapNamespaceUri, anUsingAddressing));
        supportedAssertions.add(new QName(wsaw1NamespaceUri, anUsingAddressing));
        supportedAssertions.add(new QName(wsaw2NamespaceUri, anUsingAddressing));
        supportedAssertions.add(new QName(wsaw1NamespaceUri, anAnonymous));
        supportedAssertions.add(new QName(wsaw2NamespaceUri, anAnonymous));
    }
    
    /**
     * Creates a new instance of AddressingPolicySelector
     */
    public AddressingPolicySelector() {
        super(supportedAssertions);
    }
    
}
