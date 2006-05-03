/*
 * $Id: AttributeDesignator.java,v 1.1 2006-05-03 22:58:10 arungupta Exp $
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

import com.sun.xml.wss.saml.SAMLException;

import com.sun.xml.wss.logging.LogDomainConstants;
import com.sun.xml.wss.impl.MessageConstants;
import com.sun.xml.wss.saml.internal.saml11.jaxb10.impl.AttributeDesignatorTypeImpl;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

/**
 * The <code>AttributeDesignator</code> element identifies an attribute
 * name within an attribute namespace. The element is used in an attribute query
 * to request that attribute values within a specific namespace be returned.
 */
public class AttributeDesignator extends com.sun.xml.wss.saml.internal.saml11.jaxb10.impl.AttributeDesignatorImpl 
    implements com.sun.xml.wss.saml.AttributeDesignator {
    
    protected static Logger log = Logger.getLogger(
            LogDomainConstants.WSS_API_DOMAIN,
            LogDomainConstants.WSS_API_DOMAIN_BUNDLE);


    /**
     *Default constructor
     */
    protected AttributeDesignator() {
        super();
    }

    /**
     * Constructs an attribute designator element from an existing XML block.
     *
     * @param element representing a DOM tree element.
     * @exception SAMLException if that there is an error in the sender or
     *            in the element definition.
     */
    public static AttributeDesignatorTypeImpl fromElement(Element element) throws SAMLException {
        try {
            JAXBContext jc =
                    JAXBContext.newInstance("com.sun.xml.wss.saml.internal.saml11.jaxb10");
            javax.xml.bind.Unmarshaller u = jc.createUnmarshaller();
            return (AttributeDesignatorTypeImpl)u.unmarshal(element);
        } catch ( Exception ex) {
            throw new SAMLException(ex.getMessage());
        }
    }

    /**
     * Constructs an instance of <code>AttributeDesignator</code>.
     *
     * @param name the name of the attribute.
     * @param nameSpace the namespace in which <code>AttributeName</code>
     *        elements are interpreted.
     * @exception SAMLException if there is an error in the sender or in the
     *            element definition.
     */
    public AttributeDesignator(String name, String nameSpace) {
        setAttributeName(name);
        setAttributeNamespace(nameSpace);
    }
}
