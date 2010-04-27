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
import com.sun.xml.ws.security.policy.SecurityAssertionValidator;
import com.sun.xml.ws.security.policy.Header;
import com.sun.xml.ws.security.policy.SecurityPolicyVersion;
import com.sun.xml.wss.impl.MessageConstants;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;


/**
 *
 * @author K.Venugopal@sun.com
 */


public class SignedParts extends PolicyAssertion implements com.sun.xml.ws.security.policy.SignedParts, SecurityAssertionValidator {
    private AssertionFitness fitness = AssertionFitness.IS_VALID;
    private boolean body;
    private boolean attachments;
    private String attachmentProtectionType = MessageConstants.SWA11_ATTACHMENT_CONTENT_SIGNATURE_TRANSFORM;
    private boolean populated = false;
    private Set<PolicyAssertion> targets = new HashSet<PolicyAssertion>();
    private SecurityPolicyVersion spVersion;
    
    /**
     * Creates a new instance of SignedParts
     */
    public SignedParts() {
        spVersion = SecurityPolicyVersion.SECURITYPOLICY200507;
    }
    
    public SignedParts(AssertionData name,Collection<PolicyAssertion> nestedAssertions, AssertionSet nestedAlternative) {
        super(name,nestedAssertions,nestedAlternative);
        String nsUri = getName().getNamespaceURI();
        spVersion = PolicyUtil.getSecurityPolicyVersion(nsUri);
    }
    
    public void addBody() {
        
    }
    
    public boolean hasBody() {
        populate();
        return body;
    }
    
    public boolean hasAttachments() {
        populate();
        return attachments;
    }
    
    public String attachmentProtectionType(){
        populate();
        return attachmentProtectionType;
    }
    
    public AssertionFitness validate(boolean isServer) {
        return populate(isServer);
    }
    private void populate(){
        populate(false);
    }
    
    private synchronized AssertionFitness populate(boolean isServer) {
        if(!populated){
            if(this.hasNestedAssertions()){
                Iterator <PolicyAssertion> it = this.getNestedAssertionsIterator();
                while( it.hasNext() ) {
                    PolicyAssertion as = (PolicyAssertion) it.next();
                    if(PolicyUtil.isBody(as, spVersion)){
                        // assertions.remove(as);
                        body = true;
                        // break;
                    } else if(PolicyUtil.isAttachments(as, spVersion)){
                        attachments = true;
                        if(as.hasParameters()){
                            Iterator <PolicyAssertion> attachIter = as.getParametersIterator();
                            while(attachIter.hasNext()){
                                PolicyAssertion attachType = attachIter.next();
                                if(PolicyUtil.isAttachmentCompleteTransform(attachType, spVersion)){
                                    attachmentProtectionType = MessageConstants.SWA11_ATTACHMENT_COMPLETE_SIGNATURE_TRANSFORM;
                                } else if(PolicyUtil.isAttachmentContentTransform(attachType, spVersion)){
                                    attachmentProtectionType = MessageConstants.SWA11_ATTACHMENT_CONTENT_SIGNATURE_TRANSFORM;
                                }
                            }
                        }
                    } else{
                        targets.add(as);
                    }
                }
                //targets = assertions;
            }
            populated = true;
        }
        return fitness;
    }
    
    public void addHeader(Header header) {
        
    }
    
    public Iterator getHeaders() {
        populate();
        if(targets == null){
            return Collections.emptyList().iterator();
        }
        return targets.iterator();
    }
}
