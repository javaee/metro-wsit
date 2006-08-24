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
 * SignedElements.java
 *
 * Created on February 17, 2006, 9:08 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.sun.xml.ws.security.impl.policy;

import com.sun.xml.ws.policy.AssertionSet;
import com.sun.xml.ws.policy.PolicyAssertion;
import com.sun.xml.ws.policy.sourcemodel.AssertionData;
import com.sun.xml.ws.policy.Policy;
import com.sun.xml.ws.security.policy.SecurityAssertionValidator;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import javax.xml.namespace.QName;
import static com.sun.xml.ws.security.impl.policy.Constants.*;
/**
 *
 * @author K.Venugopal@sun.com
 */
public class SignedElements extends PolicyAssertion implements com.sun.xml.ws.security.policy.SignedElements, SecurityAssertionValidator{
    
    private String xpathVersion;
    private List targetList;
    private boolean populated = false;
    private static QName XPathVersion = new QName("XPathVersion");
    private static List<String> emptyList = Collections.emptyList();
    private boolean isServer = false;
    /** Creates a new instance of SignedElements */
    public SignedElements() {
    }
    
    public SignedElements(AssertionData name,Collection<PolicyAssertion> nestedAssertions, AssertionSet nestedAlternative) {
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
            targetList = new ArrayList();
        }
        targetList.add(target);
    }
    
    public void removeTarget(String target) {
        if ( targetList != null ) {
            targetList.remove(target);
        }
    }
    
    public Iterator<String> getTargets() {
        populate();
        if ( targetList != null ) {
            return targetList.iterator();
        }
        return emptyList.iterator();
    }
    
    public boolean validate() {
        try{
            populate();
            return true;
        }catch(UnsupportedPolicyAssertion upaex) {
            return false;
        }
    }
    
    
    
    private void populate() {
        if(populated){
            return;
        }
        synchronized (this.getClass()){
            if ( !populated ) {
                this.xpathVersion = this.getAttributeValue(XPathVersion);
                
                if ( this.hasNestedAssertions() ) {
                    
                    Iterator <PolicyAssertion> it = this.getNestedAssertionsIterator();
                    while( it.hasNext() ) {
                        PolicyAssertion assertion = (PolicyAssertion) it.next();
                        if ( PolicyUtil.isXPath(assertion )) {
                            addTarget(assertion.getValue());
                        }else{
                            if(!assertion.isOptional()){
                                if(logger.getLevel() == Level.SEVERE){
                                    logger.log(Level.SEVERE,"SP0100.invalid.security.assertion",new Object[]{assertion,"SignedElements"});
                                }
                                if(isServer){
                                    throw new UnsupportedPolicyAssertion("Policy assertion "+
                                              assertion+" is not supported under SignedElements assertion");
                                }
                            }
                        }
                    }
                }
                populated = true;
            }
        }
    }
}
