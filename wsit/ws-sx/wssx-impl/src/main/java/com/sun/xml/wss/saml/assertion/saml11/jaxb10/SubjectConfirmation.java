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
 * $Id: SubjectConfirmation.java,v 1.2 2010-10-21 15:37:59 snajper Exp $
 */

package com.sun.xml.wss.saml.assertion.saml11.jaxb10;

import com.sun.xml.wss.saml.NameID;
import com.sun.xml.wss.saml.SAMLException;
import com.sun.xml.bind.util.ListImpl;

import com.sun.xml.wss.logging.LogDomainConstants;
import com.sun.xml.wss.saml.SubjectConfirmationData;
import com.sun.xml.wss.saml.internal.saml11.jaxb10.KeyInfoType;
import com.sun.xml.wss.saml.internal.saml11.jaxb10.SubjectConfirmationType;
import com.sun.xml.wss.saml.internal.saml11.jaxb10.impl.SubjectConfirmationDataImpl;
import com.sun.xml.wss.saml.internal.saml11.jaxb10.impl.SubjectConfirmationTypeImpl;
import com.sun.xml.wss.saml.util.SAMLJAXBUtil;
import java.util.LinkedList;
import java.util.List;
import org.w3c.dom.Element;
import java.util.logging.Logger;


import java.security.PublicKey;

import java.util.ArrayList;
import javax.xml.bind.JAXBContext;

/**
 * The <code>SubjectConfirmation</code> element specifies a subject by specifying data that
 * authenticates the subject.
 */
public class SubjectConfirmation extends com.sun.xml.wss.saml.internal.saml11.jaxb10.impl.SubjectConfirmationImpl
        implements com.sun.xml.wss.saml.SubjectConfirmation {
    
    protected PublicKey keyInfoKeyValue = null;
    
    protected static final Logger log = Logger.getLogger(
            LogDomainConstants.WSS_API_DOMAIN,
            LogDomainConstants.WSS_API_DOMAIN_BUNDLE);
    
      
    public SubjectConfirmation(){
        
    }
    @SuppressWarnings("unchecked")
    public void setConfirmationMethod(List confirmationMethod) {
        _ConfirmationMethod = new ListImpl(confirmationMethod);
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
    @SuppressWarnings("unchecked")
    public SubjectConfirmation(java.lang.String confirmationMethod) {
        
        List cm = new LinkedList();
        cm.add(confirmationMethod);
        setConfirmationMethod(cm);
    }
    
    public SubjectConfirmation(SubjectConfirmationType subConfType) {                
        setConfirmationMethod(subConfType.getConfirmationMethod());
    }
    
    /**
     * Constructs a subject confirmation element from an existing
     * XML block.
     *
     * @param element a DOM Element representing the
     *        <code>SubjectConfirmation</code> object.
     * @throws SAMLException
     */
    public static SubjectConfirmationTypeImpl fromElement(org.w3c.dom.Element element)
    throws SAMLException {
        try {
            JAXBContext jc =
                    SAMLJAXBUtil.getJAXBContext();
            javax.xml.bind.Unmarshaller u = jc.createUnmarshaller();
            return (SubjectConfirmationTypeImpl)u.unmarshal(element);
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
            jc = SAMLJAXBUtil.getJAXBContext();
            u = jc.createUnmarshaller();
        } catch ( Exception ex) {
            throw new SAMLException(ex.getMessage());
        }
        
        try {
            if ( keyInfo != null) {
                setKeyInfo((KeyInfoType)u.unmarshal(keyInfo));
            }
            
            if ( subjectConfirmationData != null) {
                setSubjectConfirmationData((SubjectConfirmationDataImpl)u.unmarshal(subjectConfirmationData));
            }
        } catch (Exception ex) {
            // log here
            throw new SAMLException(ex);
        }
        setConfirmationMethod(confirmationMethods);
    }

    public Object getSubjectConfirmationDataForSAML11() {
        return (Object)super.getSubjectConfirmationData();
    }

    public SubjectConfirmationData getSubjectConfirmationDataForSAML20() {
        throw new UnsupportedOperationException("Not supported for SAML 1.0");
    }

    public NameID getNameId() {
        throw new UnsupportedOperationException("Not supported for SAML 1.0");
    }
    public List<String> getConfirmationMethod() {
        List base = super.getConfirmationMethod();
        if (base == null) {
            return null;
        }
        List<String> ret = new ArrayList<String>();
        for (Object obj: base) {
            ret.add((String)obj);
        }
        return ret;
    }
}
