/*
 * $Id: NameIdentifier.java,v 1.1 2006-05-03 22:58:11 arungupta Exp $
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
import com.sun.xml.wss.saml.internal.saml11.jaxb10.impl.NameIdentifierTypeImpl;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;


/**
 *The NameIdentifier element specifies a <code>Subject</code> by a combination
 * of a name and a security domain governing the name of the <code>Subject</code>.
 */
public class NameIdentifier extends com.sun.xml.wss.saml.internal.saml11.jaxb10.impl.NameIdentifierImpl 
    implements com.sun.xml.wss.saml.NameIdentifier {

    protected static Logger log = Logger.getLogger(
            LogDomainConstants.WSS_API_DOMAIN,
            LogDomainConstants.WSS_API_DOMAIN_BUNDLE);


    /**
     * Constructs a <code>NameIdentifer</code> element from an existing XML
     * block.
     *
     * @param nameIdentifierElement A <code>org.w3c.dom.Element</code>
     *        representing DOM tree for <code>NameIdentifier</code> object
     * @exception SAMLException if it could not process the
     *            <code>org.w3c.dom.Element</code> properly, implying that there
     *            is an error in the sender or in the element definition.
     */
    public static NameIdentifierTypeImpl fromElement(org.w3c.dom.Element element)
        throws SAMLException {
        try {
            JAXBContext jc =
                JAXBContext.newInstance("com.sun.xml.wss.saml.internal.saml11.jaxb10");
            javax.xml.bind.Unmarshaller u = jc.createUnmarshaller();
            return (NameIdentifierTypeImpl)u.unmarshal(element);
        } catch ( Exception ex) {
            throw new SAMLException(ex.getMessage());
        }
    }

    
    /**
     * Constructs a <code>NameQualifier</code> instance.
     *
     * @param name The string representing the name of the Subject
     * @param nameQualifier The security or administrative domain that qualifies
     *        the name of the <code>Subject</code>. This is optional could be
     *        null or "".
     * @param format The syntax used to describe the name of the
     *        <code>Subject</code>. This optional, could be null or "".
     * @exception SAMLException if the input has an error.
     */
    public NameIdentifier(String name, String nameQualifier, String format)
        {
        if ( name != null)
            setValue(name);
        
        if ( nameQualifier != null)
            setNameQualifier(nameQualifier);
        
        if ( format != null)
            setFormat(format);
    }
}
