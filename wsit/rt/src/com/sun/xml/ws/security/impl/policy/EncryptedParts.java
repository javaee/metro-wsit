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

import com.sun.xml.ws.security.policy.Header;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import javax.xml.namespace.QName;
import static com.sun.xml.ws.security.impl.policy.Constants.*;
import com.sun.xml.ws.security.policy.SecurityAssertionValidator;
/**
 *
 * @author K.Venugopal@sun.com Abhijit.Das@Sun.com
 */

public class EncryptedParts extends PolicyAssertion implements com.sun.xml.ws.security.policy.EncryptedParts, SecurityAssertionValidator {
    private boolean _body;
    private List<Header> header;
    private boolean populated = false;
    private boolean isServer = false;
    /** Creates a new instance of EncryptedPartImpl */
    public EncryptedParts() {
    }
    public EncryptedParts(AssertionData name,Collection<PolicyAssertion> nestedAssertions, AssertionSet nestedAlternative) {
        super(name,nestedAssertions,nestedAlternative);
    }
    
    public void addBody() {
        this._body = true;
    }
    
    public boolean hasBody(){
        populate();
        return this._body;
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
    
    public boolean validate() {
        try{
            populate();
            return true;
        }catch(UnsupportedPolicyAssertion upaex) {
            return false;
        }
    }
    
    private synchronized void populate() {        
        if(!populated){
            if ( this.hasNestedAssertions() ) {
                
                Iterator <PolicyAssertion> it = this.getNestedAssertionsIterator();
                while( it.hasNext() ) {
                    PolicyAssertion assertion = it.next();
                    if ( PolicyUtil.isBody(assertion)) {
                        this._body = true;
                    } else {
                        if(header == null){
                            header = new ArrayList<Header>();
                        }
                        if(PolicyUtil.isHeader(assertion)){
                            this.header.add((Header)assertion);
                        }else{
                            if(!assertion.isOptional()){
                                if(logger.getLevel() == Level.SEVERE){
                                    logger.log(Level.SEVERE,"SP0100.invalid.security.assertion",new Object[]{assertion,"EncyptedParts"});
                                }
                                if(isServer){
                                    throw new UnsupportedPolicyAssertion("Policy assertion "+
                                            assertion+" is not supported under EncyptedParts assertion");
                                }
                            }
                        }
                    }
                }
            }
            populated = true;
        }        
    }
    
    public void removeTarget(QName targetName) {
        throw new UnsupportedOperationException();
    }
    
    public void removeBody() {
        throw new UnsupportedOperationException();
    }
}
