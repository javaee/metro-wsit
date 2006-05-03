/*
 * $Id: AttributeStatement.java,v 1.1 2006-05-03 22:58:10 arungupta Exp $
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

package com.sun.xml.wss.saml.assertion.saml11.jaxb10;

import com.sun.xml.bind.util.ListImpl;
import com.sun.xml.wss.saml.SAMLException;

import com.sun.xml.wss.logging.LogDomainConstants;
import com.sun.xml.wss.impl.MessageConstants;
import com.sun.xml.wss.saml.internal.saml11.jaxb10.impl.AttributeStatementTypeImpl;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.List;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

/**
 *The <code>AttributeStatement</code> element supplies a statement by the issuer that the
 *specified subject is associated with the specified attributes.
 */
public class AttributeStatement extends com.sun.xml.wss.saml.internal.saml11.jaxb10.impl.AttributeStatementImpl 
    implements com.sun.xml.wss.saml.AttributeStatement {
    
    protected static Logger log = Logger.getLogger(
            LogDomainConstants.WSS_API_DOMAIN,
            LogDomainConstants.WSS_API_DOMAIN_BUNDLE);


    private void setAttributes(List attr) {
        this._Attribute = new ListImpl(attr);
    }
    
    /**
     *Dafault constructor
     */
    public AttributeStatement(Subject subj, List attr) {
        setSubject(subj);
        setAttributes(attr);
    }

    /**
     * Constructs an <code>AttributStatement</code> element from an existing
     * XML block
     * @param element representing a DOM tree element
     * @exception SAMLException if there is an error in the sender or in the
     *            element definition.
     */
    public static AttributeStatementTypeImpl fromElement(Element element) throws SAMLException {
        try {
            JAXBContext jc =
                    JAXBContext.newInstance("com.sun.xml.wss.saml.internal.saml11.jaxb10");
            javax.xml.bind.Unmarshaller u = jc.createUnmarshaller();
            return (AttributeStatementTypeImpl)u.unmarshal(element);
        } catch ( Exception ex) {
            throw new SAMLException(ex.getMessage());
        }
    }
}
