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

package com.sun.xml.ws.addressing.impl.policy;

import com.sun.xml.ws.api.addressing.AddressingVersion;
import com.sun.xml.ws.policy.AssertionSet;
import com.sun.xml.ws.policy.PolicyAssertion;
import com.sun.xml.ws.policy.sourcemodel.AssertionData;
import com.sun.xml.ws.policy.spi.AssertionCreationException;
import com.sun.xml.ws.policy.spi.PolicyAssertionCreator;
import com.sun.xml.ws.security.impl.policy.Constants;
import com.sun.xml.ws.security.impl.policy.SecurityPolicyAssertionCreator;
import java.util.Collection;

/**
 *
 * @author K.Venugopal@sun.com
 */
public class AddressingPolicyAssertionCreator extends SecurityPolicyAssertionCreator {
    private String [] nsSupportedList= new String[] { AddressingVersion.MEMBER.getNsUri(),AddressingVersion.W3C.getNsUri()};
    /** Creates a new instance of AddressingPolicyAssertionCreator */
    public AddressingPolicyAssertionCreator() {
    }
    
    
    public String[] getSupportedDomainNamespaceURIs() {
        return nsSupportedList;
    }
    
    protected Class getClass(AssertionData assertionData) throws AssertionCreationException{
        try {
            String className = assertionData.getName().getLocalPart();
            return Class.forName("com.sun.xml.ws.addressing.impl.policy." + className);
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
            throw new AssertionCreationException(assertionData,ex);
        }
    }
    
}
