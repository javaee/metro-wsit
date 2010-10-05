/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2010 Sun Microsystems, Inc. All rights reserved.
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

/*
 * KeyInfoConfirmationData.java
 *
 * Created on September 20, 2006, 1:08 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.sun.xml.wss.saml.assertion.saml20.jaxb20;

import com.sun.xml.security.core.dsig.KeyInfoType;
import com.sun.xml.wss.logging.LogDomainConstants;
import com.sun.xml.wss.logging.LogStringsMessages;
import com.sun.xml.wss.saml.SAMLException;
import com.sun.xml.wss.saml.util.SAML20JAXBUtil;
import com.sun.xml.wss.saml.util.SAMLJAXBUtil;
import com.sun.xml.wss.util.DateUtils;
import java.security.PublicKey;
import java.text.ParseException;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import org.w3c.dom.Element;

/**
 *
 * @author root
 */
public class KeyInfoConfirmationData extends com.sun.xml.wss.saml.internal.saml20.jaxb20.KeyInfoConfirmationDataType
        implements com.sun.xml.wss.saml.KeyInfoConfirmationData {
        
    protected PublicKey keyInfoKeyValue = null;
   // public static KeyInfoType keyInfo = null;
    
    protected static final Logger log = Logger.getLogger(
            LogDomainConstants.WSS_API_DOMAIN,
            LogDomainConstants.WSS_API_DOMAIN_BUNDLE);
                
    /**
     * Constructs a KeyInfoConfirmationData element from an existing
     * XML block.
     *
     * @param KeyInfoConfirmationData a DOM Element representing the
     *        <code>KeyInfoConfirmationData</code> object.
     * @throws SAMLException
     */
    public static KeyInfoConfirmationData fromElement(org.w3c.dom.Element element)
    throws SAMLException {
        try {
            JAXBContext jc = SAML20JAXBUtil.getJAXBContext();
                    
            javax.xml.bind.Unmarshaller u = jc.createUnmarshaller();
            return (KeyInfoConfirmationData)u.unmarshal(element);
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
        
    public KeyInfoConfirmationData(Element keyInfo) throws SAMLException {
        
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
                this.setKeyInfo(((KeyInfoType)((JAXBElement)u.unmarshal(keyInfo)).getValue()));
            }
        } catch (Exception ex) {
            // log here
            throw new SAMLException(ex);
        }
    }
    
    public void setKeyInfo(KeyInfoType value) {
        //this.keyInfo = value;
         this.getContent().add(value);
    }

    public Date getNotBeforeDate() {
        if(super.getNotBefore() != null){
            Date getNotBeforeDate = null;
            try {
                getNotBeforeDate = DateUtils.stringToDate(super.getNotBefore().toString());
            } catch (ParseException ex) {
                log.log(Level.SEVERE, LogStringsMessages.WSS_0430_SAML_GET_NOT_BEFORE_DATE_OR_GET_NOT_ON_OR_AFTER_DATE_PARSE_FAILED(), ex);
            }
            return getNotBeforeDate;
        }
        return null;
    }

    public Date getNotOnOrAfterDate() {
        if(super.getNotBefore() != null){
            Date getNotBeforeDate = null;
            try {
                getNotBeforeDate = DateUtils.stringToDate(super.getNotOnOrAfter().toString());
            } catch (ParseException ex) {
                log.log(Level.SEVERE,LogStringsMessages.WSS_0430_SAML_GET_NOT_BEFORE_DATE_OR_GET_NOT_ON_OR_AFTER_DATE_PARSE_FAILED(), ex);
            }
            return getNotBeforeDate;
        }
        return null;
    }
}
