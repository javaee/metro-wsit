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
 * EndpointReference.java
 *
 * Created on February 17, 2006, 12:41 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.sun.xml.ws.addressing.impl.policy;

import com.sun.xml.ws.policy.AssertionSet;
import com.sun.xml.ws.policy.NestedPolicy;
import com.sun.xml.ws.policy.PolicyAssertion;
import com.sun.xml.ws.policy.PolicyAssertion;
import com.sun.xml.ws.policy.sourcemodel.AssertionData;
import com.sun.xml.ws.security.impl.policy.PolicyUtil;
import java.util.Collection;

import java.util.Iterator;

import static com.sun.xml.ws.security.impl.policy.Constants.logger;
import java.util.List;
import javax.xml.namespace.QName;
import javax.xml.ws.addressing.AddressingException;
import javax.xml.ws.addressing.AttributedURI;
import javax.xml.ws.addressing.Metadata;
import javax.xml.ws.addressing.ReferenceParameters;

/**
 *
 * @author Abhijit Das
 */
public class EndpointReference extends com.sun.xml.ws.policy.PolicyAssertion implements javax.xml.ws.addressing.EndpointReference {
    
    private AttributedURI address;
    private boolean populated = false;
    
    /**
     * Creates a new instance of EndpointReference
     */
    public EndpointReference(AssertionData name,Collection<PolicyAssertion> nestedAssertions, AssertionSet nestedAlternative) {
        super(name,nestedAssertions,nestedAlternative);
    }
    
    public AttributedURI getAddress() {
        populate();
        return address;
    }
    
    public void setAddress(AttributedURI address) {
        this.address = address;
    }
    
    private void populate() {
        if(populated){
            return;
        }
        synchronized (this.getClass()){
            if(!populated){
                if ( this.hasNestedAssertions() ) {
                    Iterator <PolicyAssertion> it = this.getNestedAssertionsIterator();
                    while ( it.hasNext() ) {
                        PolicyAssertion assertion = it.next();                        
                        if ( PolicyUtil.isAddress(assertion)) {
                            this.address = (Address) assertion;
                        }
                    }
                }
                populated = true;
            }
        }
    }
    
    public ReferenceParameters getReferenceParameters() {
        throw new UnsupportedOperationException();
    }
    
    public Metadata getMetadata() {
        throw new UnsupportedOperationException();
    }
    
    public String getNamespaceURI() {
        throw new UnsupportedOperationException();
    }
    
    public void addAttribute(QName qName, String string) throws AddressingException {
        throw new UnsupportedOperationException();
    }
    
    public List<Object> getElements() {
        throw new UnsupportedOperationException();
    }
    
    public void addElement(Object object) {
        throw new UnsupportedOperationException();
    }
    
    public boolean removeElement(Object object) {
        throw new UnsupportedOperationException();
    }
}
