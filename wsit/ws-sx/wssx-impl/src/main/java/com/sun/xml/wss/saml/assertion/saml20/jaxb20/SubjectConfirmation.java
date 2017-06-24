/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010-2017 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
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

/*
 * $Id: SubjectConfirmation.java,v 1.2 2010-10-21 15:38:04 snajper Exp $
 */

package com.sun.xml.wss.saml.assertion.saml20.jaxb20;

import com.sun.xml.wss.saml.SAMLException;
import com.sun.xml.wss.logging.LogDomainConstants;
import com.sun.xml.wss.saml.internal.saml20.jaxb20.SubjectConfirmationType;
import com.sun.xml.wss.saml.util.SAML20JAXBUtil;
import java.util.logging.Logger;


import java.security.PublicKey;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.JAXBContext;

/**
 * The <code>SubjectConfirmation</code> element specifies a subject by specifying data that
 * authenticates the subject.
 */
public class SubjectConfirmation extends SubjectConfirmationType
        implements com.sun.xml.wss.saml.SubjectConfirmation {
    
    protected PublicKey keyInfoKeyValue = null;  
    
    protected static final Logger log = Logger.getLogger(
            LogDomainConstants.WSS_API_DOMAIN,
            LogDomainConstants.WSS_API_DOMAIN_BUNDLE);
      
    public SubjectConfirmation(){
        
    }
    
/**
     * From scratch constructor for a single confirmation method.
     *
     */
    public SubjectConfirmation(NameID nameID, java.lang.String method) {
        
//        List cm = new LinkedList();
//        cm.add(method);
        setNameID(nameID);
        setMethod(method);
    }
    
    /**
     * Constructs a subject confirmation element from an existing
     * XML block.
     *
     * @param element a DOM Element representing the
     *        <code>SubjectConfirmation</code> object.
     * @throws SAMLException
     */
    public static SubjectConfirmationType fromElement(org.w3c.dom.Element element)
    throws SAMLException {
        try {
            JAXBContext jc = SAML20JAXBUtil.getJAXBContext();
                    
            javax.xml.bind.Unmarshaller u = jc.createUnmarshaller();
            return (SubjectConfirmationType)u.unmarshal(element);
        } catch ( Exception ex) {
            throw new SAMLException(ex.getMessage());
        }
    }
    
    /**
     * Constructs an <code>SubjectConfirmation</code> instance.
     *
     * @param subjectConfirmationData Additional authentication information to
     *        be used by a specific authentication protocol. Can be passed as
     *        null if there is no <code>subjectConfirmationData</code> for the
     *        <code>SubjectConfirmation</code> object.
     * @exception SAMLException if the input data is invalid or
     *            <code>confirmationMethods</code> is empty.
     */
    public SubjectConfirmation(
            NameID nameID, SubjectConfirmationData subjectConfirmationData,
            java.lang.String confirmationMethod) throws SAMLException {
        
       // JAXBContext jc = null;
        //javax.xml.bind.Unmarshaller u = null;
        
        //Unmarshal to JAXB KeyInfo Object and set it
       // try {
        //    jc = SAML20JAXBUtil.getJAXBContext();
         //   u = jc.createUnmarshaller();
        //} catch ( Exception ex) {
         //   throw new SAMLException(ex.getMessage());
        //}
        
//        try {
//            if ( keyInfo != null) {
//                setKeyInfo((KeyInfoType)((JAXBElement)u.unmarshal(keyInfo)).getValue());
//            }
//            if ( subjectConfirmationData != null) {
//                setSubjectConfirmationData((SubjectConfirmationType)((JAXBElement)u.unmarshal(subjectConfirmationData)).getValue());
//            }
//        } catch (Exception ex) {
//            // log here
//            throw new SAMLException(ex);
//        }
        setNameID(nameID); 
        if ( subjectConfirmationData != null)
            setSubjectConfirmationData(subjectConfirmationData);
        setMethod(confirmationMethod);
    }
    
     public SubjectConfirmation(
            NameID nameID, KeyInfoConfirmationData keyInfoConfirmationData,
            java.lang.String confirmationMethod) throws SAMLException {
        
        setNameID(nameID); 
        if (keyInfoConfirmationData != null)
            setSubjectConfirmationData(keyInfoConfirmationData);
        setMethod(confirmationMethod);
    }
     
     
    public SubjectConfirmation(SubjectConfirmationType subConfType){      
        if(subConfType.getNameID() != null){
            NameID nameId = new NameID(subConfType.getNameID());
            setNameID(nameId); 
        }
        if(subConfType.getSubjectConfirmationData() != null){
            SubjectConfirmationData subConData = new SubjectConfirmationData(subConfType.getSubjectConfirmationData());
            setSubjectConfirmationData(subConData);
        }        
        setMethod(subConfType.getMethod());
    }
    
    public List<String> getConfirmationMethod() {
         List<String> confirmMethods = new ArrayList<String>();
         confirmMethods.add(super.getMethod());
        return confirmMethods;
    }
           
    public Object getSubjectConfirmationDataForSAML11() {
        throw new UnsupportedOperationException("Not supported for SAML 2.0");
    }
    public SubjectConfirmationData getSubjectConfirmationDataForSAML20() {
        return (SubjectConfirmationData) super.getSubjectConfirmationData();
    }

    public NameID getNameId() {
        return (NameID) super.getNameID();
    }
}
