/*
 * $Id: RequestedSecurityTokenImpl.java,v 1.4 2007-01-04 00:47:34 manveen Exp $
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

package com.sun.xml.ws.security.trust.impl.elements;

import org.w3c.dom.Element;

import javax.xml.namespace.QName;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;

import com.sun.xml.ws.security.Token;
import com.sun.xml.ws.security.trust.WSTrustException;
import com.sun.xml.ws.security.trust.GenericToken;
import com.sun.xml.ws.security.trust.WSTrustElementFactory;
import com.sun.xml.ws.security.trust.elements.RequestedSecurityToken;
import com.sun.xml.ws.security.trust.impl.bindings.RequestedSecurityTokenType;
import com.sun.xml.ws.security.secconv.WSSCConstants;
import com.sun.xml.ws.security.secconv.impl.elements.SecurityContextTokenImpl;
import com.sun.xml.ws.security.secconv.impl.bindings.SecurityContextTokenType;

import com.sun.xml.wss.saml.assertion.saml11.jaxb20.Assertion;
import com.sun.xml.wss.saml.internal.saml11.jaxb20.AssertionType;

import com.sun.istack.NotNull;

import java.util.logging.Level;
import java.util.logging.Logger;
import com.sun.xml.ws.security.trust.logging.LogDomainConstants;

import com.sun.xml.ws.security.trust.logging.LogStringsMessages;

/**
 * Implementation for the RequestedSecurityToken.
 * 
 * @author Manveen Kaur
 */
public class RequestedSecurityTokenImpl extends RequestedSecurityTokenType implements RequestedSecurityToken {

    private static Logger log =
            Logger.getLogger(
            LogDomainConstants.TRUST_IMPL_DOMAIN,
            LogDomainConstants.TRUST_IMPL_DOMAIN_BUNDLE);

    Token containedToken = null;
    
    private final static QName SecurityContextToken_QNAME = 
            new QName("http://schemas.xmlsoap.org/ws/2005/02/sc", "SecurityContextToken");
    
    private final static QName SAML11_Assertion_QNAME = 
            new QName("urn:oasis:names:tc:SAML:1.0:assertion", "Assertion");
    
    private final static QName EncryptedData_QNAME = new QName("http://www.w3.org/2001/04/xmlenc#", "EncryptedData");
    
    /**
      * Empty default constructor.
      */
    public RequestedSecurityTokenImpl() {
    }
    
    public RequestedSecurityTokenImpl(@NotNull final RequestedSecurityTokenType rdstType){
        Object rdst = rdstType.getAny();
        if (rdst instanceof JAXBElement){
            JAXBElement rdstEle = (JAXBElement)rdst; 
           QName name = rdstEle.getName();
            if(SecurityContextToken_QNAME.equals(name)){
                SecurityContextTokenType sctType = (SecurityContextTokenType)rdstEle.getValue();
                setToken(new SecurityContextTokenImpl(sctType));
            }/*else if(EncryptedData_QNAME.equals(name)){
               EncryptedDataType edType = (EncryptedDataType)rdstEle.getValue();
               setToken(edType);  
            }else if(SAML11_Assertion_QNAME.equals(name)){
                AssertionType assertionType = (AssertionType)rdstEle.getValue();
                setToken(new Assertion(assertionType));
            }*/
            else{
                setAny(rdstEle);
                containedToken = new GenericToken((Element)rdstEle.getValue());
            }
        }
        else{
            setToken(new GenericToken((Element)rdst));
        }
    }

    public RequestedSecurityTokenImpl(Token token) {
        setToken(token);
    }
    
    /**
     * Constructs a <code>RequestedSecurityToken</code> element from
     * an existing XML block.
     *
     * @param requestedSecurityTokenElement A
     *        <code>org.w3c.dom.Element</code> representing DOM tree
     *        for <code>RequestedSecurityToken</code> object.
     * @exception WSTrustException if it could not process the
     *            <code>org.w3c.dom.Element</code> properly, implying that
     *            there is an error in the sender or in the element definition.
     */
    public static RequestedSecurityTokenType fromElement(@NotNull final org.w3c.dom.Element element)
        throws WSTrustException {
        try {
            JAXBContext jc =
                WSTrustElementFactory.getContext();
            javax.xml.bind.Unmarshaller u = jc.createUnmarshaller();
            
            JAXBElement<RequestedSecurityTokenType> rstType = u.unmarshal(element, RequestedSecurityTokenType.class);
            return rstType.getValue();
        } catch (JAXBException ex) {
            log.log(Level.SEVERE,
                    LogStringsMessages.WST_0021_ERROR_UNMARSHAL_DOM_ELEMENT(ex));                        
            throw new WSTrustException("Error in unmarshalling DOM Element", ex);
        }
    }

    /*
     * Return the security token contained in the RequestedSecurityToken.
     */
    public Token getToken() {
        return containedToken;
    }
    
    public void setToken(Token token) {
        if (token != null)  {
            String tokenType = token.getType();
            if (WSSCConstants.SECURITY_CONTEXT_TOKEN.equals(tokenType)){
                JAXBElement<SecurityContextTokenType> sctElement =
                (new com.sun.xml.ws.security.secconv.impl.bindings.ObjectFactory()).createSecurityContextToken((SecurityContextTokenType)token);
                setAny(sctElement);
            }else {
                Element element = (Element)token.getTokenValue();
                setAny(element);
            }
        }
        containedToken = token;
    }
}
