/*
 * $Id: SubjectConfirmationData.java,v 1.1.2.2 2010-07-14 14:09:05 m_potociar Exp $
 */

/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 * 
 * Contributor(s):
 * 
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */

package com.sun.xml.wss.saml.assertion.saml20.jaxb20;

import com.sun.xml.security.core.dsig.KeyInfoType;
import com.sun.xml.wss.saml.SAMLException;


import com.sun.xml.wss.logging.LogDomainConstants;
import com.sun.xml.wss.saml.internal.saml20.jaxb20.SubjectConfirmationDataType;
import com.sun.xml.wss.saml.util.SAML20JAXBUtil;
import com.sun.xml.wss.util.DateUtils;
import java.text.ParseException;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.GregorianCalendar;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.DatatypeConfigurationException;
import java.security.PublicKey;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBContext;
import org.w3c.dom.Element;

/**
 * The <code>SubjectConfirmationData</code> element specifies a subject by specifying data that
 * authenticates the subject.
 */
public class SubjectConfirmationData extends SubjectConfirmationDataType
        implements com.sun.xml.wss.saml.SubjectConfirmationData {
    
    protected PublicKey keyInfoKeyValue = null;
    private Date notBeforeDate = null;
    private Date notOnOrAfterDate = null;
    protected static final Logger log = Logger.getLogger(
            LogDomainConstants.WSS_API_DOMAIN,
            LogDomainConstants.WSS_API_DOMAIN_BUNDLE);
    
    
    public SubjectConfirmationData(){
        
    }
                
    /**
     * Constructs a subject confirmation element from an existing
     * XML block.
     *
     * @param SubjectConfirmationDataElement a DOM Element representing the
     *        <code>SubjectConfirmationData</code> object.
     * @throws SAMLException
     */
    public static SubjectConfirmationDataType fromElement(org.w3c.dom.Element element)
    throws SAMLException {
        try {
            JAXBContext jc = SAML20JAXBUtil.getJAXBContext();
                    
            javax.xml.bind.Unmarshaller u = jc.createUnmarshaller();
            return (SubjectConfirmationDataType)u.unmarshal(element);
        } catch ( Exception ex) {
            throw new SAMLException(ex.getMessage());
        }
    }
    
    /**
     * Constructs an <code>SubjectConfirmationData</code> instance.
     *
     * @param confirmationMethods A set of <code>confirmationMethods</code>
     *        each of which is a URI (String) that identifies a protocol
     *        used to authenticate a <code>Subject</code>. Please refer to
     *        <code>draft-sstc-core-25</code> Section 7 for
     *        a list of URIs identifying common authentication protocols.
     * @param SubjectConfirmationDataData Additional authentication information to
     *        be used by a specific authentication protocol. Can be passed as
     *        null if there is no <code>SubjectConfirmationDataData</code> for the
     *        <code>SubjectConfirmationData</code> object.
     * @param keyInfo An XML signature element that specifies a cryptographic
     *        key held by the <code>Subject</code>.
     * @exception SAMLException if the input data is invalid or
     *            <code>confirmationMethods</code> is empty.
     */
    public SubjectConfirmationData(
        String address, String inResponseTo, GregorianCalendar notBefore, 
        GregorianCalendar notOnOrAfter, String recipient, Element keyInfo) throws SAMLException {
        
        JAXBContext jc = null;
        javax.xml.bind.Unmarshaller u = null;
        
        //Unmarshal to JAXB KeyInfo Object and set it
        try {
            jc = SAML20JAXBUtil.getJAXBContext();
           u = jc.createUnmarshaller();
        } catch ( Exception ex) {
            throw new SAMLException(ex.getMessage());
        }
        
//        try {
//            if ( keyInfo != null) {
//                setKeyInfo((KeyInfoType)((JAXBElement)u.unmarshal(keyInfo)).getValue());
//            }
//            if ( SubjectConfirmationDataData != null) {
//                setSubjectConfirmationDataData((SubjectConfirmationDataType)((JAXBElement)u.unmarshal(SubjectConfirmationDataData)).getValue());
//            }
//        } catch (Exception ex) {
//            // log here
//            throw new SAMLException(ex);
//        }
        setAddress(address);
        setInResponseTo(inResponseTo);
        if ( notBefore != null) {
            try {
                DatatypeFactory factory = DatatypeFactory.newInstance();
                setNotBefore(factory.newXMLGregorianCalendar(notBefore));
            }catch ( DatatypeConfigurationException ex ) {
                //ignore
            }
        }
        
        if ( notOnOrAfter != null) {
            try {
                DatatypeFactory factory = DatatypeFactory.newInstance();
                setNotOnOrAfter(factory.newXMLGregorianCalendar(notOnOrAfter));
            }catch ( DatatypeConfigurationException ex ) {
                //ignore
            }
        }
        
        setRecipient(recipient);        
        
        try {
            if (keyInfo != null) {
                //this.getContent().add(keyInfo);
                this.getContent().add((KeyInfoType) ((JAXBElement) u.unmarshal(keyInfo)).getValue());
            }
        } catch (Exception ex) {
            // log here
            throw new SAMLException(ex);
        }
    }

    public SubjectConfirmationData(SubjectConfirmationDataType subConfDataType){
        setAddress(subConfDataType.getAddress());
        setInResponseTo(subConfDataType.getInResponseTo());
        setNotBefore(subConfDataType.getNotBefore());
        setNotOnOrAfter(subConfDataType.getNotOnOrAfter());
        setRecipient(subConfDataType.getRecipient());
    }
    
    public Date getNotBeforeDate() {
        if (notBeforeDate == null) {
            if (super.getNotBefore() != null) {
                try {
                    notBeforeDate = DateUtils.stringToDate(super.getNotBefore().toString());
                } catch (ParseException ex) {
                    Logger.getLogger(SubjectConfirmationData.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        return notBeforeDate;
    }

    public Date getNotOnOrAfterDate() {
        if (notOnOrAfterDate == null) {
            if (super.getNotOnOrAfter() != null) {
                try {
                    notOnOrAfterDate = DateUtils.stringToDate(super.getNotOnOrAfter().toString());
                } catch (ParseException ex) {
                    Logger.getLogger(SubjectConfirmationData.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        return notOnOrAfterDate;
    }
}
