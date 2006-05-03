/*
 * $Id: SubjectConfirmation.java,v 1.1 2006-05-03 22:58:15 arungupta Exp $
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

import com.sun.xml.wss.saml.internal.saml11.jaxb20.KeyInfoType;
import com.sun.xml.wss.saml.internal.saml11.jaxb20.SubjectConfirmationType;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import javax.xml.bind.JAXBElement;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.Set;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;


import java.security.PublicKey;

import com.sun.org.apache.xml.internal.security.keys.KeyInfo;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

/**
 * The <code>SubjectConfirmation</code> element specifies a subject by specifying data that
 * authenticates the subject.
 */
public class SubjectConfirmation extends SubjectConfirmationType
        implements com.sun.xml.wss.saml.SubjectConfirmation {
    
    protected PublicKey keyInfoKeyValue = null;
    
    protected static Logger log = Logger.getLogger(
            LogDomainConstants.WSS_API_DOMAIN,
            LogDomainConstants.WSS_API_DOMAIN_BUNDLE);
    
    
    
    private void setConfirmationMethod(List confirmationMethod) {
        this.confirmationMethod = confirmationMethod;
    }
    
    /**
     * From scratch constructor for a single confirmation method.
     *
     * @param confirmationMethod A URI (String) that identifies a protocol used
     *        to authenticate a <code>Subject</code>. Please refer to
     *        <code>draft-sstc-core-25</code> Section 7 for a list of URIs
     *        identifying common authentication protocols.
     * @exception SAMLException if the input data is null.
     */
    public SubjectConfirmation(java.lang.String confirmationMethod) {
        
        List cm = new LinkedList();
        cm.add(confirmationMethod);
        setConfirmationMethod(cm);
    }
    
    /**
     * Constructs a subject confirmation element from an existing
     * XML block.
     *
     * @param subjectConfirmationElement a DOM Element representing the
     *        <code>SubjectConfirmation</code> object.
     * @throws SAMLException
     */
    public static SubjectConfirmationType fromElement(org.w3c.dom.Element element)
    throws SAMLException {
        try {
            JAXBContext jc =
                    JAXBContext.newInstance("com.sun.xml.wss.saml.internal.saml11.jaxb20");
            javax.xml.bind.Unmarshaller u = jc.createUnmarshaller();
            return (SubjectConfirmationType)u.unmarshal(element);
        } catch ( Exception ex) {
            throw new SAMLException(ex.getMessage());
        }
    }
    
    /**
     * Constructs an <code>SubjectConfirmation</code> instance.
     *
     * @param confirmationMethods A set of <code>confirmationMethods</code>
     *        each of which is a URI (String) that identifies a protocol
     *        used to authenticate a <code>Subject</code>. Please refer to
     *        <code>draft-sstc-core-25</code> Section 7 for
     *        a list of URIs identifying common authentication protocols.
     * @param subjectConfirmationData Additional authentication information to
     *        be used by a specific authentication protocol. Can be passed as
     *        null if there is no <code>subjectConfirmationData</code> for the
     *        <code>SubjectConfirmation</code> object.
     * @param keyInfo An XML signature element that specifies a cryptographic
     *        key held by the <code>Subject</code>.
     * @exception SAMLException if the input data is invalid or
     *            <code>confirmationMethods</code> is empty.
     */
    public SubjectConfirmation(
            List confirmationMethods, Element subjectConfirmationData,
            Element keyInfo) throws SAMLException {
        
        JAXBContext jc = null;
        javax.xml.bind.Unmarshaller u = null;
        
        //Unmarshal to JAXB KeyInfo Object and set it
        try {
            jc = JAXBContext.newInstance("com.sun.xml.wss.saml.internal.saml11.jaxb20");
            u = jc.createUnmarshaller();
        } catch ( Exception ex) {
            throw new SAMLException(ex.getMessage());
        }
        
        try {
            if ( keyInfo != null) {
                setKeyInfo((KeyInfoType)((JAXBElement)u.unmarshal(keyInfo)).getValue());
            }
            if ( subjectConfirmationData != null) {
                setSubjectConfirmationData((SubjectConfirmationType)((JAXBElement)u.unmarshal(subjectConfirmationData)).getValue());
            }
        } catch (Exception ex) {
            // log here
            throw new SAMLException(ex);
        }
        setConfirmationMethod(confirmationMethods);
    }
}
