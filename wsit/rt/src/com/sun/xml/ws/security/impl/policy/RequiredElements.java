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
 * RequiredElements.java
 *
 * Created on April 18, 2006, 7:13 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.sun.xml.ws.security.impl.policy;

import com.sun.xml.ws.policy.AssertionSet;
import com.sun.xml.ws.policy.PolicyAssertion;
import com.sun.xml.ws.policy.sourcemodel.AssertionData;
import com.sun.xml.ws.security.policy.SecurityAssertionValidator;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import javax.xml.namespace.QName;
import static com.sun.xml.ws.security.impl.policy.Constants.*;

/**
 *
 * @author mayank.mishra@Sun.com
 */
public class RequiredElements extends PolicyAssertion implements com.sun.xml.ws.security.policy.RequiredElements, SecurityAssertionValidator {
    
    private String xpathVersion;
    private List<String> targetList;
    private boolean populated = false;
    private static QName XPathVersion = new QName("XPathVersion");
    private AssertionFitness fitness = AssertionFitness.IS_VALID;
    /** Creates a new instance of RequiredElements */
    public RequiredElements() {
    }
    
    public RequiredElements(AssertionData name,Collection<PolicyAssertion> nestedAssertions, AssertionSet nestedAlternative) {
        super(name,nestedAssertions,nestedAlternative);
    }
    
    public String getXPathVersion() {
        populate();
        return xpathVersion;
    }
    
    public void setXPathVersion(String version) {
        this.xpathVersion = version;
    }
    
    public void addTarget(String target) {
        if ( targetList == null ) {
            targetList = new ArrayList<String>();
        }
        targetList.add(target);
    }
    
    public void removeTarget(String target) {
        if ( targetList != null ) {
            targetList.remove(target);
        }
    }
    
    public Iterator getTargets() {
        populate();
        if ( targetList != null ) {
            return targetList.iterator();
        }
        return Collections.emptyList().iterator();
    }
    
    
    public AssertionFitness validate(boolean isServer) {
        return populate(isServer);
    }
    private void populate(){
        populate(false);
    }
    
    private synchronized AssertionFitness populate(boolean isServer) {
        if(!populated){
            this.xpathVersion = (String)this.getAttributeValue(XPathVersion);
            if ( this.hasNestedAssertions() ) {
                Iterator <PolicyAssertion> it = this.getNestedAssertionsIterator();
                if ( it.hasNext() ) {
                    PolicyAssertion assertion = it.next();
                    if ( PolicyUtil.isXPath(assertion)) {
                        addTarget(assertion.getValue());
                    } else{
                        if(!assertion.isOptional()){
                            log_invalid_assertion(assertion, isServer,RequiredElements);
                            fitness = AssertionFitness.HAS_UNKNOWN_ASSERTION;
                        }
                    }
                }
            }
            populated = true;
        }
        return fitness;
    }
}
