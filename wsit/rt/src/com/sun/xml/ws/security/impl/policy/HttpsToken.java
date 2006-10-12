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
import java.util.Collection;

import java.util.Map;
import java.util.UUID;
import javax.xml.namespace.QName;
import com.sun.xml.ws.security.policy.SecurityAssertionValidator;


/**
 *
 * @author K.Venugopal@sun.com
 */
public class HttpsToken extends PolicyAssertion implements com.sun.xml.ws.security.policy.HttpsToken, SecurityAssertionValidator{
    
    private boolean populated = false;
    private boolean requireCC = false;
    private String id = "";
    private static QName rccQname = new QName(Constants.SECURITY_POLICY_NS, Constants.RequireClientCertificate);
    /**
     * Creates a new instance of HttpsToken
     */
    public HttpsToken() {
        UUID uid = UUID.randomUUID();
        id= uid.toString();
    }
    
    public HttpsToken(AssertionData name,Collection<PolicyAssertion> nestedAssertions, AssertionSet nestedAlternative) {
        super(name,nestedAssertions,nestedAlternative);
        UUID uid = UUID.randomUUID();
        id= uid.toString();
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
    
    public boolean validate() {
        try{
            populate();
            return true;
        }catch(UnsupportedPolicyAssertion upaex) {
            return false;
        }
    }
    
    private void populate(){
        if(populated){
            return;
        }
        synchronized (this.getClass()){
            if(!populated){
                String value = this.getAttributeValue(rccQname);
                requireCC = Boolean.valueOf(value);
                populated = true;
            }
        }
    }
}
