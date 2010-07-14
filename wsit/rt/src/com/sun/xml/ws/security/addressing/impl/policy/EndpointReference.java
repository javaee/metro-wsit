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

/*
 * EndpointReference.java
 *
 * Created on February 17, 2006, 12:41 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.sun.xml.ws.security.addressing.impl.policy;

import com.sun.xml.ws.policy.AssertionSet;
import com.sun.xml.ws.policy.PolicyAssertion;
import com.sun.xml.ws.policy.sourcemodel.AssertionData;
import com.sun.xml.ws.security.impl.policy.PolicyUtil;
import java.util.Collection;

import java.util.Iterator;

/**
 *
 * @author Abhijit Das
 */
public class EndpointReference extends com.sun.xml.ws.policy.PolicyAssertion  {
    
    private Address address;
    private boolean populated = false;
    
    /**
     * Creates a new instance of EndpointReference
     */
    public EndpointReference(AssertionData name,Collection<PolicyAssertion> nestedAssertions, AssertionSet nestedAlternative) {
        super(name,nestedAssertions,nestedAlternative);
    }
    
    public Address getAddress() {
        populate();
        return address;
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
    

}
