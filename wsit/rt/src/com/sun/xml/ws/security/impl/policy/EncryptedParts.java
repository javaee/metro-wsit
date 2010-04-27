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
package com.sun.xml.ws.security.impl.policy;


import com.sun.xml.ws.policy.AssertionSet;
import com.sun.xml.ws.policy.PolicyAssertion;
import com.sun.xml.ws.policy.sourcemodel.AssertionData;

import com.sun.xml.ws.security.policy.Header;
import com.sun.xml.ws.security.policy.SecurityPolicyVersion;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import javax.xml.namespace.QName;
import static com.sun.xml.ws.security.impl.policy.Constants.*;
import com.sun.xml.ws.security.policy.SecurityAssertionValidator;
/**
 *
 * @author K.Venugopal@sun.com Abhijit.Das@Sun.com
 */

public class EncryptedParts extends PolicyAssertion implements com.sun.xml.ws.security.policy.EncryptedParts, SecurityAssertionValidator {
    private boolean _body;
    private boolean _attachments;
    private List<Header> header;
    private boolean populated = false;
    private AssertionFitness fitness = AssertionFitness.IS_VALID;
    private SecurityPolicyVersion spVersion;
    
    /** Creates a new instance of EncryptedPartImpl */
    public EncryptedParts() {
        spVersion = SecurityPolicyVersion.SECURITYPOLICY200507;
    }
    public EncryptedParts(AssertionData name,Collection<PolicyAssertion> nestedAssertions, AssertionSet nestedAlternative) {
        super(name,nestedAssertions,nestedAlternative);
        String nsUri = getName().getNamespaceURI();
        spVersion = PolicyUtil.getSecurityPolicyVersion(nsUri);
    }
    
    public void addBody() {
        this._body = true;
    }
    
    public boolean hasBody(){
        populate();
        return this._body;
    }
    
    public boolean hasAttachments(){
        populate();
        return this._attachments;
    }
    
    public void addTarget(QName targetName) {
        throw new UnsupportedOperationException();
    }
    
    public Iterator getTargets() {
        populate();
        if(header == null){
            return Collections.emptyList().iterator();
        }
        return header.iterator();
    }
    
    //    public QName getName() {
    //        return Constants._EncryptedParts_QNAME;
    //    }
    
    public AssertionFitness validate(boolean isServer) {
        return populate(isServer);
    }
    private void populate(){
        populate(false);
    }
    
    private synchronized AssertionFitness populate(boolean isServer) {
        if(!populated){
            if ( this.hasNestedAssertions() ) {
                
                Iterator <PolicyAssertion> it = this.getNestedAssertionsIterator();
                while( it.hasNext() ) {
                    PolicyAssertion assertion = it.next();
                    if ( PolicyUtil.isBody(assertion, spVersion)) {
                        this._body = true;
                    } else if(PolicyUtil.isAttachments(assertion, spVersion)){
                        this._attachments = true;
                    } else {
                        if(header == null){
                            header = new ArrayList<Header>();
                        }
                        if(PolicyUtil.isHeader(assertion, spVersion)){
                            this.header.add((Header)assertion);
                        }else{
                            if(!assertion.isOptional()){
                                log_invalid_assertion(assertion, isServer,EncryptedParts);
                                fitness = AssertionFitness.HAS_UNKNOWN_ASSERTION;
                            }
                        }
                    }
                }
            }
            populated = true;
        }
        return fitness;
    }
    
    public void removeTarget(QName targetName) {
        throw new UnsupportedOperationException();
    }
    
    public void removeBody() {
        throw new UnsupportedOperationException();
    }
}
