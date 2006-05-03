/*
 * $Id: Subject.java,v 1.1 2006-05-03 22:58:12 arungupta Exp $
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
import com.sun.xml.wss.saml.internal.saml11.jaxb10.impl.SubjectTypeImpl;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

/**
 * The <code>Subject</code> element specifies one or more subjects. It contains either or
both of the following elements:<code>NameIdentifier</code>;
An identification of a subject by its name and security domain.
<code>SubjectConfirmation</code>;
Information that allows the subject to be authenticated.

If a <code>Subject</code> element contains more than one subject specification,
the issuer is asserting that the surrounding statement is true for
all of the subjects specified. For example, if both a
<code>NameIdentifier</code> and a <code>SubjectConfirmation</code> element are
present, the issuer is asserting that the statement is true of both subjects
being identified. A <Subject> element SHOULD NOT identify more than one
principal.
*/
public class Subject extends com.sun.xml.wss.saml.internal.saml11.jaxb10.impl.SubjectImpl 
    implements com.sun.xml.wss.saml.Subject {
    
    protected static Logger log = Logger.getLogger(
            LogDomainConstants.WSS_API_DOMAIN,
            LogDomainConstants.WSS_API_DOMAIN_BUNDLE);


    
    /**
     * Constructs a Subject object from a <code>NameIdentifier</code>
     * object and a <code>SubjectConfirmation</code> object.
     *
     * @param nameIdentifier <code>NameIdentifier</code> object.
     * @param subjectConfirmation <code>SubjectConfirmation</code> object.
     * @exception SAMLException if it could not process the
     *            Element properly, implying that there is an error in the
     *            sender or in the element definition.
     */
    public Subject(
        NameIdentifier nameIdentifier, SubjectConfirmation subjectConfirmation)
        {
        
        if ( nameIdentifier != null)
            setNameIdentifier(nameIdentifier);
        
        if ( subjectConfirmation != null)
            setSubjectConfirmation(subjectConfirmation);
    }

    /**
     * This constructor builds a subject element from an existing XML block
     * which has already been built into a DOM.
     *
     * @param subjectElement An Element representing DOM tree for Subject object
     * @exception SAMLException if it could not process the Element properly,
     *            implying that there is an error in the sender or in the
     *            element definition.
     */
    public static SubjectTypeImpl fromElement(org.w3c.dom.Element element)
        throws SAMLException {
        try {
            JAXBContext jc =
                    JAXBContext.newInstance("com.sun.xml.wss.saml.internal.saml11.jaxb10");
            javax.xml.bind.Unmarshaller u = jc.createUnmarshaller();
            return (SubjectTypeImpl)u.unmarshal(element);
        } catch ( Exception ex) {
            throw new SAMLException(ex.getMessage());
        }
    }

}
