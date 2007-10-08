/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
import java.util.Collection;

import java.util.Map;
import javax.xml.namespace.QName;
import com.sun.xml.ws.security.policy.SecurityAssertionValidator;
import com.sun.xml.ws.security.policy.SecurityAssertionValidator.AssertionFitness;


/**
 *
 * @author K.Venugopal@sun.com
 */
public class HttpsToken extends PolicyAssertion implements com.sun.xml.ws.security.policy.HttpsToken, SecurityAssertionValidator{
    private AssertionFitness fitness = AssertionFitness.IS_VALID;
    private boolean populated = false;
    private boolean requireCC = false;
    private String id = "";
    private static QName rccQname = new QName(Constants.SECURITY_POLICY_NS, Constants.RequireClientCertificate);
    /**
     * Creates a new instance of HttpsToken
     */
    public HttpsToken() {
        id= PolicyUtil.randomUUID();
    }
    
    public HttpsToken(AssertionData name,Collection<PolicyAssertion> nestedAssertions, AssertionSet nestedAlternative) {
        super(name,nestedAssertions,nestedAlternative);
        id= PolicyUtil.randomUUID();
    }
    
    public void setRequireClientCertificate(boolean value) {
        Map<QName, String> attrs = this.getAttributes();
        QName rccQname = new QName(Constants.SECURITY_POLICY_NS, Constants.RequireClientCertificate);
        attrs.put(rccQname,Boolean.toString(value));
        requireCC = value;
    }
    
    public boolean isRequireClientCertificate() {
        populate();
        return this.requireCC;
    }
    
    public String getIncludeToken() {
        throw new UnsupportedOperationException("This method is not supported for HttpsToken");
    }
    
    public void setIncludeToken(String type) {
        throw new UnsupportedOperationException("This method is not supported for HttpsToken");
    }
    
    public String getTokenId() {
        return id;
    }
    
    public AssertionFitness validate(boolean isServer) {
        return populate(isServer);
    }
    private void populate(){
        populate(false);
    }
    
    private synchronized AssertionFitness populate(boolean isServer) {
        
        if(!populated){
            String value = this.getAttributeValue(rccQname);
            requireCC = Boolean.valueOf(value);
            populated = true;
            
        }
        return fitness;
        
    }
}
