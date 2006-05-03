/*
 * $Id: Evidence.java,v 1.1 2006-05-03 22:58:14 arungupta Exp $
 */

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

package com.sun.xml.wss.saml.assertion.saml11.jaxb20;

import com.sun.org.apache.xml.internal.security.utils.XMLUtils;

import com.sun.xml.wss.saml.SAMLException;


import com.sun.xml.wss.logging.LogDomainConstants;
import com.sun.xml.wss.impl.MessageConstants;
import com.sun.xml.wss.saml.internal.saml11.jaxb20.EvidenceType;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.Set;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

/**
 * The <code>Evidence</code> element specifies an assertion either by
 * reference or by value. An assertion is specified by reference to the value of
 * the assertion's  <code>AssertionIDReference</code> element.
 * An assertion is specified by value by including the entire
 * <code>Assertion</code> object
 */
public class Evidence extends EvidenceType
    implements com.sun.xml.wss.saml.Evidence {
    
    protected static Logger log = Logger.getLogger(
            LogDomainConstants.WSS_API_DOMAIN,
            LogDomainConstants.WSS_API_DOMAIN_BUNDLE);

    /**
     * Constructs an <code>Evidence</code> object from a block of existing XML
     * that has already been built into a DOM.
     *
     * @param assertionSpecifierElement A <code>org.w3c.dom.Element</code>
     *        representing DOM tree for <code>Evidence</code> object.
     * @exception SAMLException if it could not process the Element properly,
     *            implying that there is an error in the sender or in the
     *            element definition.
     */
    public static EvidenceType fromElement(org.w3c.dom.Element element)
        throws SAMLException {
        try {
            JAXBContext jc =
                JAXBContext.newInstance("com.sun.xml.wss.saml.internal.saml11.jaxb20");
            javax.xml.bind.Unmarshaller u = jc.createUnmarshaller();
            return (EvidenceType)u.unmarshal(element);
        } catch ( Exception ex) {
            throw new SAMLException(ex.getMessage());
        }
    }

    
    private void setAssertionIDReferenceOrAssertion(List evidence) {
        this.assertionIDReferenceOrAssertion = evidence;
    }
    
    
    /**
     * Constructs an Evidence from a Set of <code>Assertion</code> and
     * <code>AssertionIDReference</code> objects.
     *
     * @param assertionIDRef Set of <code>AssertionIDReference</code> objects.
     * @param assertion Set of <code>Assertion</code> objects.
     * @exception SAMLException if either Set is empty or has invalid object.
     */
    public Evidence(List assertionIDRef, List assertion)
        {
        
        if ( assertionIDRef != null)
            setAssertionIDReferenceOrAssertion(assertionIDRef);
        else if ( assertion != null)
            setAssertionIDReferenceOrAssertion(assertion);
    }
}
