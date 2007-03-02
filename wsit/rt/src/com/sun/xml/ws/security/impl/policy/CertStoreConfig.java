/*
 * CertStoreConfig.java
 *
 * Created on March 1, 2007, 3:39 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.sun.xml.ws.security.impl.policy;

import com.sun.xml.ws.policy.AssertionSet;
import com.sun.xml.ws.policy.PolicyAssertion;
import com.sun.xml.ws.policy.sourcemodel.AssertionData;
import java.util.Collection;
import javax.xml.namespace.QName;

/**
 *
 * @author Kumar Jayanti
 */
public class CertStoreConfig extends PolicyAssertion implements com.sun.xml.ws.security.policy.CertStoreConfig {
    
    private static QName cbh = new QName("callbackHandler");
    private static QName certSelector = new QName("certSelector");
    private static QName crlSelector = new QName("crlSelector");
   
    
    public CertStoreConfig(AssertionData name,Collection<PolicyAssertion> nestedAssertions, AssertionSet nestedAlternative) {
        super(name,nestedAssertions,nestedAlternative);
    }
    /** Creates a new instance of CertStoreConfig */
    public CertStoreConfig() {
    }

    public String getCallbackHandlerClassName() {
         return this.getAttributeValue(cbh);
    }

    public String getCertSelectorClassName() {
         return this.getAttributeValue(certSelector);
    }

    public String getCRLSelectorClassName() {
         return this.getAttributeValue(crlSelector);
    }
    
}
