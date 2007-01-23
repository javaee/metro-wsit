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
 * Address.java
 *
 * Created on February 17, 2006, 12:48 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.sun.xml.ws.addressing.impl.policy;

import com.sun.xml.ws.policy.AssertionSet;
import com.sun.xml.ws.policy.PolicyAssertion;
import com.sun.xml.ws.policy.sourcemodel.AssertionData;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.logging.Level;
import static com.sun.xml.ws.security.impl.policy.Constants.logger;
/**
 *
 * @author Abhijit Das
 */
public class Address extends com.sun.xml.ws.policy.PolicyAssertion implements com.sun.xml.ws.addressing.policy.Address {
    
    private boolean populated = false;
    private URI address;
    
    /**
     * Creates a new instance of Address
     */
    public Address() {
    }
    
    public Address(AssertionData name,Collection<PolicyAssertion> nestedAssertions, AssertionSet nestedAlternative) {
        super(name,nestedAssertions,nestedAlternative);
    }
    private void populate() {
        if ( !populated ) {
            try {
                if(this.getValue() != null){
                    this.address = new URI(this.getValue().trim());
                }
                populated = true;
            } catch (URISyntaxException ex) {
                if(logger.getLevel() == Level.SEVERE){
                    logger.log(Level.SEVERE,"SP0100.invalid.epr-address",ex);
                }
            }
        }
    }
    
    public URI getURI() {
        populate();
        return address;
    }
    
    
    public String getNamespaceURI() {
        throw new UnsupportedOperationException();
    }
    
}
