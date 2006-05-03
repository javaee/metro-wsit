/*
 * $Id: AuthenticationStatement.java,v 1.1 2006-05-03 22:58:13 arungupta Exp $
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
import com.sun.xml.wss.saml.internal.saml11.jaxb20.AuthenticationStatementType;
import java.util.Calendar;
import java.util.GregorianCalendar;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import java.util.TimeZone;
import java.util.Date;
import java.util.List;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

/**
 * The <code>AuthenticationStatement</code> element supplies a
 * statement by the issuer that its subject was authenticated by a
 * particular means at a particular time. The
 * <code>AuthenticationStatement</code> element is of type
 * <code>AuthenticationStatementType</code>, which extends the
 * <code>SubjectStatementAbstractType</code> with the additional element and
 * attributes.
 */
public class AuthenticationStatement extends AuthenticationStatementType
        implements com.sun.xml.wss.saml.AuthenticationStatement {
    
    protected static Logger log = Logger.getLogger(
            LogDomainConstants.WSS_API_DOMAIN,
            LogDomainConstants.WSS_API_DOMAIN_BUNDLE);
    
    
    /**
     *Default constructor
     */
    protected AuthenticationStatement() {
        super();
    }
    
    /**
     * This constructor builds an authentication statement element from an
     * existing XML block.
     *
     * @param element representing a DOM tree element.
     * @exception SAMLException if there is an error in the sender or in the
     *            element definition.
     */
    public static AuthenticationStatementType fromElement(Element element) throws SAMLException {
        try {
            JAXBContext jc =
                    JAXBContext.newInstance("com.sun.xml.wss.saml.internal.saml11.jaxb20");
            javax.xml.bind.Unmarshaller u = jc.createUnmarshaller();
            return (AuthenticationStatementType)u.unmarshal(element);
        } catch ( Exception ex) {
            throw new SAMLException(ex.getMessage());
        }
    }
    
    
    private void setAuthorityBinding(List authorityBinding) {
        this.authorityBinding = authorityBinding;
    }
    
    /**
     * Constructor for authentication statement
     *
     * @param authMethod (optional) A String specifies the type of authentication
     *        that took place.
     * @param authInstant (optional) A GregorianCalendar specifies the time at which the
     *        authentication that took place.
     * @param subject (required) A Subject object
     * @param subjectLocality (optional) A <code>SubjectLocality</code> object.
     * @param authorityBinding (optional) A List of <code>AuthorityBinding</code>
     *        objects.
     * @exception SAMLException if there is an error in the sender.
     */
    public AuthenticationStatement(
            String authMethod, GregorianCalendar authInstant, Subject subject,
            SubjectLocality subjectLocality, List authorityBinding) {
        
        if ( authMethod != null)
            setAuthenticationMethod(authMethod);
        
        if ( authInstant != null) {
            try {
                DatatypeFactory factory = DatatypeFactory.newInstance();
                setAuthenticationInstant(factory.newXMLGregorianCalendar(authInstant));
            }catch ( DatatypeConfigurationException ex ) {
                //ignore
            }
        }
        
        if ( subject != null)
            setSubject(subject);
        
        if ( subjectLocality != null)
            setSubjectLocality(subjectLocality);
        
        if ( authorityBinding != null)
            setAuthorityBinding(authorityBinding);
    }
}
