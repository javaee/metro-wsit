/*
 * $Id: AuthorityBinding.java,v 1.1 2006-05-03 22:58:10 arungupta Exp $
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
import com.sun.xml.wss.saml.internal.saml11.jaxb10.impl.AuthorityBindingTypeImpl;
import javax.xml.namespace.QName;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

/**
 * The <code>AuthorityBinding</code> element may be used to indicate
 * to a replying party receiving an <code>AuthenticationStatement</code> that
 * a SAML authority may be available to provide additional information about
 * the subject of the statement. A single SAML authority may advertise its
 * presence over multiple protocol binding, at multiple locations, and as
 * more than one kind of authority by sending multiple elements as needed.
 */
public class AuthorityBinding extends com.sun.xml.wss.saml.internal.saml11.jaxb10.impl.AuthorityBindingImpl 
    implements com.sun.xml.wss.saml.AuthorityBinding {
    
    protected static Logger log = Logger.getLogger(
            LogDomainConstants.WSS_API_DOMAIN,
            LogDomainConstants.WSS_API_DOMAIN_BUNDLE);

 
    /**
     * Constructs an <code>AuthorityBinding</code> element from an existing XML
     * block.
     *
     * @param element representing a DOM tree element.
     * @exception SAMLException if there is an error in the sender or in the
     *            element definition.
     */
    public static AuthorityBindingTypeImpl fromElement(Element element) throws SAMLException {
        try {
            JAXBContext jc =
                    JAXBContext.newInstance("com.sun.xml.wss.saml.internal.saml11.jaxb10");
            javax.xml.bind.Unmarshaller u = jc.createUnmarshaller();
            return (AuthorityBindingTypeImpl)u.unmarshal(element);
        } catch ( Exception ex) {
            throw new SAMLException(ex.getMessage());
        }
    }

    /**
     *Constructor
     *@param authKind A integer representing the type of SAML protocol queries
     *       to which the authority described by this element will
     *       respond. If you do NO specify this attribute, pass in
     *       value "-1".
     *@param location A URI describing how to locate and communicate with the
     *       authority, the exact syntax of which depends on the
     *       protocol binding in use.
     *@param binding A String representing a URI reference identifying the SAML
     *       protocol binding to use in  communicating with the authority.
     *@exception SAMLException if there is an error in the sender or in the
     *           element definition.
     */
    public AuthorityBinding(QName authKind, String location, String binding)
        {
        setAuthorityKind(authKind);
        setLocation(location);
        setBinding(binding);
    }
}
