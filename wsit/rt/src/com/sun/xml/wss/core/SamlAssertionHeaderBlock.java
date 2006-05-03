/*
 * $Id: SamlAssertionHeaderBlock.java,v 1.1 2006-05-03 22:57:33 arungupta Exp $
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

package com.sun.xml.wss.core;

//import com.sun.xml.wss.saml.assertion.saml11.jaxb10.SAMLUtil;
import java.math.BigInteger;
import java.util.logging.Logger;
import java.util.logging.Level;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.sun.xml.wss.saml.SAMLException;

import com.sun.xml.wss.impl.misc.SecurityHeaderBlockImpl;
import com.sun.xml.wss.logging.LogDomainConstants;
import com.sun.xml.wss.impl.MessageConstants;
import com.sun.xml.wss.impl.XMLUtil;
import com.sun.xml.wss.XWSSecurityException;

//import com.sun.xml.wss.saml.assertion.saml11.jaxb10.Assertion;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;


/**
 * The schema definition for a SAML <code>Assertion</code> is as follows:
 * <xmp>
 * <element name="Assertion" type="saml:AssertionType"/>
 * <complexType name="AssertionType">
 *     <sequence>
 *         <element ref="saml:Conditions" minOccurs="0"/>
 *         <element ref="saml:Advice" minOccurs="0"/>
 *         <choice maxOccurs="unbounded">
 *             <element ref="saml:Statement"/>
 *             <element ref="saml:SubjectStatement"/>
 *             <element ref="saml:AuthenticationStatement"/>
 *             <element ref="saml:AuthorizationDecisionStatement"/>
 *             <element ref="saml:AttributeStatement"/>
 *         </choice>
 *         <element ref="ds:Signature" minOccurs="0"/>
 *     </sequence>
 *     <attribute name="MajorVersion" type="integer" use="required"/>
 *     <attribute name="MinorVersion" type="integer" use="required"/>
 *     <attribute name="AssertionID" type="saml:IDType" use="required"/>
 *     <attribute name="Issuer" type="string" use="required"/>
 *     <attribute name="IssueInstant" type="dateTime" use="required"/>
 * </complexType>
 * </xmp>
 *
 * @author Axl Mattheus
 */
public class SamlAssertionHeaderBlock extends SecurityHeaderBlockImpl implements SecurityToken {
    private static Logger log =
    Logger.getLogger(
    LogDomainConstants.WSS_API_DOMAIN,
    LogDomainConstants.WSS_API_DOMAIN_BUNDLE);
    
    
    /**
     *
     * @param element
     * @return
     * @throws XWSSecurityException
     */
    public static SecurityHeaderBlock fromSoapElement(SOAPElement element)
    throws XWSSecurityException {
        return SecurityHeaderBlockImpl.fromSoapElement(
        element, SamlAssertionHeaderBlock.class);
    }
    
    private Document contextDocument_ = null;
    private Element delegateAssertion_ = null;
    
    
    /**
     * Constructs code>SamlAssertionHeaderBlock</code> from an existing SAML
     * <code>Assertion</code>.
     *
     * @param assertion
     * @throws XWSSecurityException
     */
    public SamlAssertionHeaderBlock(Element assertion, Document doc) throws  XWSSecurityException {
        if (null != assertion) {
            delegateAssertion_ = assertion;
            contextDocument_ = doc;
        } else {
            throw new XWSSecurityException("Assertion may not be null.");
        }
    }
    
    /**
     * Constructs a SAML <code>Assertion</code> header block from an existing
     * <code>SOAPElement</code>.
     *
     * @param element an existing SAML assertion element.
     * @throws XWSSecurityException when the element is not a valid template
     *         for a SAML <code>Assertion</code>.
     */
    public SamlAssertionHeaderBlock(SOAPElement element)
    throws XWSSecurityException {
        contextDocument_ = element.getOwnerDocument();
        
        delegateAssertion_ = element;
        
        
        setSOAPElement(element);
    }
    
    /* (non-Javadoc)
     * @see com.sun.xml.wss.SecurityHeaderBlock#getAsSoapElement()
     */
    public SOAPElement getAsSoapElement() throws XWSSecurityException {
        
        
        // uncomment after making SamlAssertionHeaderBlock like others (using a dirty flag).
        if (delegateElement != null) {
            return delegateElement;
        }
        
        if (null == contextDocument_) {
            try {
                contextDocument_ = XMLUtil.newDocument();
            } catch (ParserConfigurationException e) {
                throw new XWSSecurityException(e);
            }
        }
        
        try {
            SOAPElement se = (SOAPElement)contextDocument_.importNode(delegateAssertion_, true);
            setSOAPElement(se);
            
        } catch (Exception e) {
            throw new XWSSecurityException(e);
        }
        
        return super.getAsSoapElement();
    }
    
    
    /**
     * @return
     */
    public Document getContextDocument() {
        return contextDocument_;
    }
    
    
    /**
     * @return
     */
    public Element getDelegateAssertion() {
        return delegateAssertion_;
    }

    /**
     * Set the signature for the Request.
     *
     * @param elem <code>ds:Signature</code> element.
     * @return A boolean value: true if the operation succeeds; false otherwise.
     */
    /*public boolean setSignature(Element elem) {
        try {
            JAXBContext jc =
                JAXBContext.newInstance("com.sun.xml.wss.saml.internal");
            javax.xml.bind.Unmarshaller u = jc.createUnmarshaller();
            delegateAssertion_.setSignature((SignatureType)u.unmarshal(elem));
            return true;
        } catch ( Exception ex) {
            return false;
        }
    }*/

    
}
