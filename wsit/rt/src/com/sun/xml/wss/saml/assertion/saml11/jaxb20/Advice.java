/*
 * $Id: Advice.java,v 1.1 2006-05-03 22:58:13 arungupta Exp $
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

import com.sun.xml.wss.saml.SAMLException;

import com.sun.xml.wss.logging.LogDomainConstants;
import com.sun.xml.wss.impl.MessageConstants;
import com.sun.xml.wss.saml.internal.saml11.jaxb20.AdviceType;


//import com.sun.xml.bind.util.ListImpl;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.List;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 *The <code>Advice</code> element contains additional information that the issuer wishes to
 *provide. This information MAY be ignored by applications without affecting
 *either the semantics or validity. Advice elements MAY be specified in
 *an extension schema.
 */
public class Advice  extends com.sun.xml.wss.saml.internal.saml11.jaxb20.AdviceType implements com.sun.xml.wss.saml.Advice {
    protected static Logger log = Logger.getLogger(
            LogDomainConstants.WSS_API_DOMAIN,
            LogDomainConstants.WSS_API_DOMAIN_BUNDLE);



    
    public static AdviceType fromElement(Element element) throws SAMLException {
        try {
            JAXBContext jc =
                    JAXBContext.newInstance("com.sun.xml.wss.saml.internal.saml11.jaxb20");
            javax.xml.bind.Unmarshaller u = jc.createUnmarshaller();
            return (AdviceType)u.unmarshal(element);
        } catch ( Exception ex) {
            throw new SAMLException(ex.getMessage());
        }
    }

    
    private void setAssertionIDReferenceOrAssertionOrAny(
            List assertionIDReferenceOrAssertionOrAny) {
        this.assertionIDReferenceOrAssertionOrAny = assertionIDReferenceOrAssertionOrAny;
    }
    
    /**
     * Constructor
     *
     * @param assertionidreference A List of <code>AssertionIDReference</code>.
     * @param assertion A List of Assertion
     * @param otherelement A List of any element defined as
     *        <code>&lt;any namespace="##other" processContents="lax"&gt;</code>;
     */
    public Advice(List assertionidreference, List assertion, List otherelement) {
        if ( null != assertionidreference ) {
            setAssertionIDReferenceOrAssertionOrAny(assertionidreference);
        } else if ( null != assertion) {
            setAssertionIDReferenceOrAssertionOrAny(assertion);
        } else if ( null != otherelement) {
            setAssertionIDReferenceOrAssertionOrAny(otherelement);
        }
    }
}
