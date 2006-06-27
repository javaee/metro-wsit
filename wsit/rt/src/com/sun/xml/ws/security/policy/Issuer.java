/*
 * $Id: Issuer.java,v 1.3 2006-06-27 21:58:31 ofung Exp $
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

package com.sun.xml.ws.security.policy;

import com.sun.xml.ws.addressing.policy.Address;
import com.sun.xml.ws.policy.PolicyAssertion;
import javax.xml.ws.addressing.EndpointReference;

/**
 * Specifies the issuer of the security token that is presented
 * in the message. The element's type is an endpoint reference as defined
 * in WS-Addressing.
 *
 * @author WS-Trust Implementation Team
 */
public interface Issuer {
    
    public Address getAddress();
    
    public String getPortType();
    
    public PolicyAssertion getServiceName();
    
    public PolicyAssertion getReferenceParameters();
    public PolicyAssertion getReferenceProperties();
    
}
