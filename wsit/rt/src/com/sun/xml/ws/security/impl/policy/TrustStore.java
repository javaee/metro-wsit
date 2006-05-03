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
package com.sun.xml.ws.security.impl.policy;

import com.sun.xml.ws.policy.AssertionSet;
import com.sun.xml.ws.policy.PolicyAssertion;
import com.sun.xml.ws.policy.sourcemodel.AssertionData;
import java.util.Collection;
import javax.xml.namespace.QName;

/**
 *
 * @author K.Venugopal@sun.com
 */
public class TrustStore extends KeyStore implements com.sun.xml.ws.security.policy.TrustStore{
    
    private static QName  peerAlias = new QName("peeralias");
    private static QName  stsAlias = new QName("stsalias");
    private static QName  serviceAlias = new QName("servicealias");
    
    /** Creates a new instance of TrustStore */
    public TrustStore() {
    }
    
    public TrustStore(AssertionData name,Collection<PolicyAssertion> nestedAssertions, AssertionSet nestedAlternative) {
        super(name,nestedAssertions,nestedAlternative);
    }
    public String getPeerAlias() {
        return this.getAttributeValue(peerAlias);
    }
    
    public String getSTSAlias() {
        return this.getAttributeValue(stsAlias);
    }
    
    public String getServiceAlias() {
        return this.getAttributeValue(serviceAlias);
    }
}
